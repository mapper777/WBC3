package application.service;

import application.model.BuildRequest;
import application.model.BuildResult;
import application.repository.BaseRepository;
import application.work.finiteAutomat.CharToColumn;
import application.work.finiteAutomat.FA_Arc;
import application.work.finiteAutomat.FinAutomat;
import application.work.finiteAutomat.FiniteAutomat;

import com.couchbase.client.java.json.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class ScannerService {
    private final BaseRepository baseRepository;
    int what = 0;
    StringBuilder rx = null;
    StringBuilder blo = null;
    int k, c, j;

    public ScannerService(BaseRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    public BuildResult buildScanner(BuildRequest req) {
        BuildResult res = new BuildResult();
        StringBuilder errStr = new StringBuilder();

        FA_Arc curArc = null;
        String rez = "", val = "";
        Integer w = null;


        FiniteAutomat fa = new FiniteAutomat();
        res.setName(req.getName());
        res.setExtension(req.getExtension());
        res.setSyntax(req.getSyntax());
        res.setTemplate(req.getTemplate());

        List<List<String>> lexRules = req.getLexic();

        if (lexRules.isEmpty()) {
            errStr = new StringBuilder("Empty lexic");
        }

        for (int lrIndex = 0; lrIndex < lexRules.size(); lrIndex += 1) {
            List<String> re = lexRules.get(lrIndex);
            if (fa.addWordsDef(re.get(0), re.get(1), (re.size() > 2) ? re.get(2) : "") < 0) {
                errStr.append("Error in lexic rule ").append(lrIndex).append("\n");
            }
        }

        fa.buildDFA();
        String sc = fa.nFN.toString();
        w = req.getWhat();
        res.setScanner(sc);
        res.setWhat(req.getWhat());
        res.setErrors(errStr.toString());
        FinAutomat finA = new FinAutomat(sc);
        CharToColumn ctInd = new CharToColumn(finA);
        int nd = ctInd.getColumnsCount();


        if (errStr.isEmpty() && (w == null || w == 0 || w > 10)) {
            what = w == null ? 0 : w.intValue();
            res.setForwardToParser(true);
            return res;
        }

        res.setForwardToParser(false);
        res.setNd(nd);
        res.setCols(ctInd.getCols());

        List<String> ranges = new ArrayList<>();

        res.setRanges(ranges);
        if (what == 1) {
            int cols = ctInd.getCols();
            String cls = "", celS = "";

            for (int k = 0; k < cols; k++) {
                for (k = 0; k < nd; k++) {
                    String rng = ctInd.getAllRanges(k);
                    if (rng == null) {
                        rng = "EOF";
                    } else {
                        rng = rng.
                                replaceAll(" ", " \\\\d32")
                                .replaceAll("\t", " \\\\d09")
                                .replaceAll("\r", " \\\\d10")
                                .replaceAll("\n", " \\\\d13");
                    }

                    for (int i = 0; i < cols; i++) {
                        cls = "";
                        int celI = ctInd.getCell(k, i);
                        if (celI == 0) {
                            cls = "er";
                            celS = "E";
                        } else if (celI < 0) {
                            if ((celI & 0x40000000) == 0) {
                                cls = "fn";
                                celS = "" + (celI | 0x40000000);
                            } else {
                                cls = "fv";
                                celS = "" + celI;
                            }
                        } else {
                            celS = "" + celI;
                        }

                    }
                }
            }
            if ((what == 5) || (what == 6)) {

                if (what == 5) {
                    cols = ctInd.getCols();
                    // String cls = "", celS = "";
                    for (int k = 0; k < cols; k++) {
                        // <td><b><%if (k == 0) {%>▼<%} else {%>&nbsp;<%}%></b>&nbsp;<%}%>
                        // эта логика выносится в thymeleaf?
                        for (k = 0; k < cols; k++) {
//                        <td>&nbsp;<b><%=k%>
//                                </b>&nbsp;
                        }
                    }

                    int fQoute = -1, fRet = -1;
                    for (int k = 0; k < nd; k++) {
                        String rng = ctInd.getAllRanges(k);
                        String[] rngs;
                        String rngx = "";
                        if (rng == null) {
                            rngx = "EOF";
                        } else {
                            rngs = rng.split("\u00A0");
                            if (rngs != null) {
                                for (int bi = 0; bi < rngs.length; bi++) {
                                    if ((rngs[bi].length() == 3) && (rngs[bi].charAt(1) == '-')) {
                                        char cnb = rngs[bi].charAt(0);
                                        char che = rngs[bi].charAt(2);
                                        while (cnb <= che)
                                            rngx += cnb++;
                                    } else {
                                        rngx += rngs[bi];
                                    }
                                }

                                fQoute = rngx.indexOf('"');
                                fRet = rngx.indexOf("\r");
                            }
                        }
//                                       <td class="tb" align="right" rng="<%=rngx%>" <%if(fQoute>=0){%>quote="1"<%}%>
//                        <%if(fRet>=0){%>retc="1"<%}%>>[&nbsp;<%
                        if (rng == null)
                            rng = "EOF";
                        else {
                            rng = rng.
                                    replaceAll(" ", " \\\\d32").
                                    replaceAll("\t", " \\\\d09").
                                    replaceAll("\r", " \\\\d10").
                                    replaceAll("\n", " \\\\d13");
                        }
//                      %>
//                            <%=rng%>&nbsp;]&nbsp;
//                            <%
                        for (int i = 0; i < cols; i++) {
                            cls = "";
                            int celI = ctInd.getCell(k, i);
//    int celI=ctInd.getStateTo(i,k);
                            if (celI == 0) {
                                cls = "er";
                                celS = "E";
                            } else if (celI < 0) {
                                if ((celI & 0x40000000) == 0) {
                                    cls = "fn";
                                    celS = "" + (celI | 0x40000000);
                                } else {
                                    cls = "fv";
                                    celS = "" + celI;
                                }
                            } else
                                celS = "" + celI;

//                        %>
//                    <td class="<%=cls%>" align="right">&nbsp;<%=celS%>&nbsp;
//                <%
//                    }
//                }
//                %></table>

                        }
                    }
                } else if (what == 6) {
                    for (int j = 0; j < finA.getWorkNodesCount(); j++) {
//                    %>
//                    <tr class="tb">
//                            <td class="tb">&nbsp;
//                        <td class="tb" align=right valign=top><b><%=j%>:</b>&nbsp;
//                        <td class="tb" valign=top>&nbsp;|&nbsp;
//                        <td class="tb"><%
//                    if (j == 0) {%>&nbsp;<span style='color:brown'><i>EOF</i>
//                            </span>-><b>-1</b>&nbsp;&nbsp;
//                                <%}

                        curArc = finA.getFirstFrom(j);
                        while (curArc != null) {
                            rx = curArc.getMark();
                            if (rx != null) {
                                blo = new StringBuilder();
                                k = 0;
                                while (k < rx.length()) {
                                    c = rx.charAt(k);
                                    if ((k < rx.length() - 2) && (rx.charAt(k + 1) == (c + 1)) && (rx.charAt(k + 2) == (c + 2))) {
                                        if (c > ' ') {
                                            blo.append((char) c);
                                        } else {
                                            blo.append(c > 9 ? "\\d":"\\d0");
                                            blo.append(c);
                                        }
                                        blo.append('-');
                                        k += 2;
                                        while (k < rx.length() - 1) {
                                            if (rx.charAt(k) + 1 != rx.charAt(k + 1)) {
                                                break;
                                            }
                                            k += 1;
                                        }
                                        c = rx.charAt(k);
                                        if (c > ' ') {
                                            blo.append((char) c);
                                        } else {
                                            blo.append(((int) c) > 9 ? "\\d" : "\\d0");
                                            blo.append((int) c);
                                        }
                                        k += 1;
                                    } else {
                                        if (k == rx.length() - 1) {
                                            if (c > ' ') {
                                                blo.append((char) c);
                                            } else {
                                                blo.append(((int) c) > 9 ? "\\d" : "\\d0");
                                                blo.append((int) c);
                                            }
                                        } else {
                                            c = rx.charAt(k);
                                            if (c > ' ') {
                                                blo.append((char) c);
                                            } else {
                                                blo.append(((int) c) > 9 ? "\\d" : "\\d0");
                                                blo.append((int) c);
                                            }
                                        }
                                        k += 1;
                                    }
                                }
                                rx = blo;
                            }

//                              %>&nbsp;[<%=(((rx == null) || (rx.length() == 0)) ? "<span style='color:brown'><i>other</i></span>" : rx.toString())%>]-><b>
//                                <%if (curArc.getNodeTo() < 0) {%><span style="color:magenta"><%=curArc.getNodeTo()-1%>
//                                <%}else{%><span><%=curArc.getNodeTo()%><%}%>
//    </span></b>&nbsp;&nbsp;
//                    <%

                            curArc = curArc.getNext();
                        }
                    }
                }
            }

            if (what == 2) {
//                <tr class="tb">
//                    <td class="tb" style='color:blue' align=right>Состояния
//                    <td class="tb">&nbsp;|&nbsp;
//        <td class="tb" style='color:blue'>Переходы
//                    <tr class="tb">
//                    <td class="tb" style='color:blue' align=right>(вершины графа):
//        <td class="tb">&nbsp;|&nbsp;
//        <td class="tb" style='color:blue'>(дуги графа):<%
                for (j = 0; j < finA.getWorkNodesCount(); j++) {
                    if (j == 0) {
                        //%>&nbsp;<span style='color:brown'><i>EOF</i></span>-><b>-1</b>&nbsp;&nbsp;<%
                    }

                    curArc = finA.getFirstFrom(j);
                    while (curArc != null) {
                        rx = curArc.getMark();
                        if (rx != null) {
                            blo = new StringBuilder();
                            k = 0;
                            while (k < rx.length()) {
                                c = rx.charAt(k);
                                if ((k < rx.length() - 2) && (rx.charAt(k + 1) == (c + 1)) && (rx.charAt(k + 2) == (c + 2))) {
                                    if (c > ' ') {
                                        blo.append((char) c);
                                    } else {
                                        blo.append(((int) c) > 9 ? "\\d" : "\\d0");
                                        blo.append((int) c);
                                    }
                                    blo.append('-');
                                    k += 2;
                                    while (k < rx.length() - 1) {
                                        if (rx.charAt(k) + 1 != rx.charAt(k + 1)) {
                                            break;
                                        }
                                        k += 1;
                                    }
                                    c = (char) rx.charAt(k);
                                    if (c > ' ') {
                                        blo.append((char) c);
                                    } else {
                                        blo.append(((int) c) > 9 ? "\\d" : "\\d0");
                                        blo.append((int) c);
                                    }
                                    k += 1;
                                } else {
                                    if (k == rx.length() - 1) {
                                        if (c > ' ') {
                                            blo.append((char) c);
                                        } else {
                                            blo.append(((int) c) > 9 ? "\\d" : "\\d0");
                                            blo.append((int) c);
                                        }
                                    } else {// if (k == rx.length() - 1) {
                                        c = (char) rx.charAt(k);
                                        if (c > ' ') {
                                            blo.append((char) c);
                                        } else {
                                            blo.append(((int) c) > 9 ? "\\d" : "\\d0");
                                            blo.append((int) c);
                                        }
                                    }
                                    k += 1;
                                }
                            }
                            rx = blo;
                        }
//                          %>&nbsp;[<%=(((rx == null) || (rx.length() == 0)) ? "<span style='color:brown'><i>other</i></span>" : rx.toString())%>]-><b>
//                            <%if (curArc.getNodeTo() < 0) {%><span style="color:magenta"><%=curArc.getNodeTo()-1%>
//                            <%}else{%><span><%=curArc.getNodeTo()%><%}%>
//    </span></b>&nbsp;&nbsp;<%

                        curArc = curArc.getNext();
                    }
                }
            }
            if (what < 13) {
                int colS = nd + 1;
//             <tr class="tb">
//                    <td class="tb">
//                    <td colspan="<%=colS%>" class="tb">&nbsp;
//    <tr class="tb">
//                    <td class="tb">
//                    <td class="tb" colspan="<%=colS%>" style='color:blue'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Финальные состояния и
//            действия:
//    <tr class="tb">
//                    <td class="tb">
//                    <td colspan="<%=colS+1%>" class="tb">
//                    <table id="finalS" class="tb" align="center">
//                    <tr class="tb">
//                    <td class="tb">
//                    <td class="tb"><b>&nbsp;&nbsp;&nbsp;&nbsp;N</b>
//                    <td class="tb"><b>Токен</b>
//                    <td class="tb"><b>Группа слов</b>
//                    <td class="tb">&nbsp;&nbsp;
//                    <td class="tb"><b>Действие</b>
//                    <tr class="tb">
//                    <td class="tb">
//                    <td class="tb" align=right><span style="color:magenta"><b>-1</b></span>
//                    <td class="tb">&nbsp;->&nbsp;0&nbsp;
//                    <td class="tb"><b>EOF</b>
//                    <td class="tb">
//                    <td class="tb"><span style='color:gray'><i>Lexem.groupIndex=0;</i></span>
                for (j = 0; j < finA.getGrpCount(); j++) {
                    rez = finA.getGrpName(j);
                    val = "";
//                 <tr class="tb">
//                        <td class="tb">
//                        <td class="tb" align=right valign=top><span style="color:magenta"><b><%=-2 - j%></b></span>
//                        <td class="tb" valign=top><%=(val == "" ? "" : "&nbsp;->&nbsp;" + val)%>&nbsp;
//                    <td class="tb" valign=top<%if((rez!=null)&&(rez.indexOf(",")>0)) {err=true;%>
//                    style="color:red"<%}%>><b><%=rez%>
//                        </b>
//                        <td class="tb">
//                        <td class="tb" valign=top>
//                        <%=(val == "" ? "" : "<span style='color:gray'><i>Lexem.groupIndex=" + val + ";</i></span>")%><%=(finA.getAction(j) == null ? "" : finA.getAction(j))%><%

                }
            }
        } else {
//            %>
//            Error: <%=errStr%><%
        }
        return res;
    }

    public String getDocs(String collName) {
        StringBuilder list = new StringBuilder();

        try {
            List<JsonObject> docs = baseRepository.find(collName);
            if (docs != null) {
                for (JsonObject doc : docs) {
                    if (doc.containsKey("rules") && doc.get("rules") != null) {
                        list.append(doc.getString("rules")).append(" ");
                    }
                }
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

        return list.toString();
    }

}
