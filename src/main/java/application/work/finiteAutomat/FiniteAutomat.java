package application.work.finiteAutomat;

import java.util.ArrayList;

public class FiniteAutomat {
    int state = 0,
            nodeCnt = 0,
            finNodesCnt = 0,
            wordsCnt = 0;
    private boolean flagO;
    private FA_Arc rootArc = null;
    private FA_Arc[] fromNode = null;
    private ArrayList<String> grpNames;
    private ArrayList<String> regExprs;
    private ArrayList<String> actions;
    private ArrayList<String> rNames;
    private ArrayList<String> rActions;
    private NodeSet nSet;

    public StringBuilder nFN = new StringBuilder();
    public StringBuilder xFN = new StringBuilder();

    public FiniteAutomat() {
        grpNames = new ArrayList();
        regExprs = new ArrayList();
        actions = new ArrayList();
        rootArc = null;
        nSet = null;
        flagO = false;
    }

    public StringBuilder getAllArcs() {
        StringBuilder r = new StringBuilder();
        FA_Arc a = rootArc;
        while (a != null) {
            r.append("\nfr " + a.getNodeFrom() + " to " + a.getNodeTo() + " by '" + ((a.getExpr() == null) ? "null" : a.getExpr().getExprText()) + "'");
            a = a.getNext();
        }
        return r;
    }

    public StringBuilder getFromArcs() {
        if (fromNode == null)
            return null;
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < fromNode.length; i++) {
            r.append("\n" + i + ":");
            FA_Arc a = fromNode[i];
            while (a != null) {
                r.append(" to " + a.getNodeTo() + " by '" + ((a.getMark() == null) ? ((a.getExpr() == null ? null : a.getExpr().getExprText())) : a.getMark()) + "'");
                a = a.getNext();
            }
        }
        return r;
    }

    public boolean getFlagOther() {
        return flagO;
    }

    public int addWordsDef(String nameGrpWords, String regularExpr, String action) {
        int nd;
        String a;
        if ((nameGrpWords == null) || ((nameGrpWords = nameGrpWords.trim()).length() == 0) || (regularExpr == null) || ((regularExpr = regularExpr.trim()).length() == 0))
            return -1;
        if (nameGrpWords.equals("Word!"))
            nameGrpWords += wordsCnt++;
        if ((nd = grpNames.indexOf(nameGrpWords)) < 0) {
            grpNames.add(nameGrpWords);
            regExprs.add("(" + regularExpr + ") ");
            actions.add(action);
        } else {
            regExprs.set(nd, regExprs.get(nd) + " | (" + regularExpr + ") ");
            if ((action != null) && ((action = action.trim()).length() > 0))
                actions.set(nd, (((a = actions.get(nd)) == null) ? "" : a) + action);
        }
        return grpNames.size();
    }

    public StringBuilder buildDFA() {
        int i, aCnt = 2;
        ArrayList<String> rN = null;
        while (--aCnt >= 0) {
            state = 0;
            nodeCnt = 0;
            wordsCnt = 0;
            finNodesCnt = 0;
            flagO = false;
            fromNode = null;
            rootArc = null;
            rNames = new ArrayList();
            nSet = null;
            for (i = 0; i < grpNames.size(); i++) {
                setWordsDef(grpNames.get(i), i < regExprs.size() ? regExprs.get(i) : "");//,(i<actions.size())?actions.get(i):null);
            }
            toEpsFree();
            markUsedNodes();
            rN = toDFA();
            for (i = 0; i < grpNames.size(); i++) {
                String gn = grpNames.get(i);
                if (gn.indexOf("Word!") == 0)
                    grpNames.set(i, "Word!");
                int xi, f = 0;
                while ((xi = gn.indexOf("\n")) >= 0) {
                    gn = gn.substring(0, xi) + gn.substring(xi + 1);
                    f = 1;
                }
                if (f == 1)
                    grpNames.set(i, gn);
            }
        }
        nFN.append("{\"finalStates\":[");
        for (int xi = 0; xi < rN.size(); xi++)
            nFN.append((xi > 0 ? ";" : "") + "[\"S-" + (xi + 1) + "\",\"" + rN.get(xi) + " " + ((xi >= rActions.size() || rActions.get(xi) == null) ? "" : rActions.get(xi)) + "\"]");
        nFN.append("]");
        return delEquNodes();
    }

    private int setWordsDef(String nameGrpWords, String regularExpr) {//,String action){
        int nCnt = ++nodeCnt;
        int wcnt;
        LexRegExp re = null;
        FA_Arc rArc, curArc, newArc = null;
        int finNode;
        if (state != 0)
            return -1;
//  xFN.append("["+nameGrpWords+": "+regularExpr+"] ");
        finNode = -1;
        for (wcnt = 0; wcnt < rNames.size(); wcnt++)
            if (nameGrpWords.equals(rNames.get(wcnt))) {
                finNode = wcnt;
                break;
            }
        if (finNode < 0) {
            finNode = --finNodesCnt;
            rNames.add(nameGrpWords);
        } else {
            finNode = -(finNode + 1);
        }
        re = new LexRegExp(regularExpr);
        curArc = new FA_Arc(0, nCnt, re);
        rArc = curArc;
        rArc.setNext(newArc = new FA_Arc(nCnt, finNode, null));
        wcnt = 0;
        while (curArc != null) {
            if ((re = curArc.getExpr()) != null) {
                switch (re.getType()) {
                    case -1:
                        return -2;
                    case 1:
                        re.simplify();
                    case 0:
                        curArc = curArc.getNext();
                        break;
                    case 2:
                        re.simplify();
                        nCnt += 1;
                        curArc.setNext(newArc = new FA_Arc(curArc.getNodeFrom(), curArc.getNodeTo(), null));
                        curArc.setNext(newArc = new FA_Arc(nCnt, curArc.getNodeTo(), null));
                        curArc.setNodeTo(nCnt);
                        break;
                    case 3:
                        curArc.setNext(newArc = new FA_Arc(curArc.getNodeFrom(), curArc.getNodeTo(), null));
                    case 4:
                        re.simplify();
                        nCnt += 1;
                        curArc.setNext(newArc = new FA_Arc(nCnt, curArc.getNodeTo(), null));
                        curArc.setNext(newArc = new FA_Arc(nCnt, nCnt, new LexRegExp(re.getExprText())));
                        curArc.setNodeTo(nCnt);
                        break;
                    case 5:
                        nCnt += 2;
                        curArc.setNext(newArc = new FA_Arc(nCnt - 1, curArc.getNodeTo(), null));
                        curArc.setNext(newArc = new FA_Arc(nCnt, curArc.getNodeTo(), null));
                        curArc.setNext(newArc = new FA_Arc(curArc.getNodeFrom(), nCnt, re.simplify()));
                        curArc.setNodeTo(nCnt - 1);
                        break;
                    case 6:
                        nCnt += 1;
                        curArc.setNext(newArc = new FA_Arc(nCnt, curArc.getNodeTo(), re.simplify()));
                        curArc.setNodeTo(nCnt);
                        break;
                    default:
                        return -2;
                }
            } else {
                curArc = curArc.getNext();
            }
        }
        if (rootArc == null) rootArc = rArc;
        else {
            newArc.setNext(rootArc);
            rootArc = rArc;
        }
        nodeCnt = nCnt;
/*int j;
FA_Arc cArc;
cArc=rootArc;
while(cArc!=null){
  if(cArc.getMark()!=null)
    xFN.append("From "+cArc.getNodeFrom()+" to "+cArc.getNodeTo()+" by '"+cArc.getMark().toString().replaceAll("\"", "\\\"")+"' ");
  else
    if(cArc.getExpr()!=null)
      xFN.append("From "+cArc.getNodeFrom()+" to "+cArc.getNodeTo()+" by '"+cArc.getExpr().getExprText()+"' ");
    else
    xFN.append("From "+cArc.getNodeFrom()+" to "+cArc.getNodeTo()+" by null ");
  cArc=cArc.getNext();
}*/
        return 0;
    }

    public void toEpsFree() {
        FA_Arc curArc, tArc, nArc, vArc, rArc;
        LexRegExp nExp, mExp;
        int i, ii, needArc;
        int[] p = new int[nodeCnt + 1];
        if (state == 0)
            state = 1;
        else
            return;
        if ((fromNode == null) || (fromNode.length != (nodeCnt + 1)))
            fromNode = new FA_Arc[nodeCnt + 1];
        for (i = 0; i <= nodeCnt; i++)
            fromNode[i] = null;
        curArc = rootArc;
        while (curArc != null) {
            tArc = curArc.getNext();
            curArc.setNext(null);
            needArc = 0;
            if ((nExp = curArc.getExpr()) != null) {
                if (nExp.getType() == 1)
                    nExp.simplify();
                needArc = nExp.getType();
            }
            if (needArc == 0) {
                i = curArc.getNodeFrom();
                if (fromNode[i] != null)
                    curArc.setNext(fromNode[i]);
                fromNode[i] = curArc;
            }
            curArc = tArc;
        }
        i = -1;
        needArc = 1;
        while (i < nodeCnt) {
            if (needArc > 0)
                for (ii = 0; ii <= nodeCnt; p[ii++] = 0) ;
            i += needArc;
            needArc = 1;

            rArc = null;

            curArc = fromNode[i];
            while (curArc != null) {
                tArc = curArc.getNext();
                if (((ii = curArc.getNodeTo()) < 0) || (p[ii] > 0) || ((nExp = curArc.getExpr()) != null)) {
                    curArc.setNext(null);
                    curArc.setNext(rArc);
                    rArc = curArc;
                } else {
                    p[ii] = 1;
                    vArc = fromNode[curArc.getNodeTo()];
                    while (vArc != null) {
                        nArc = vArc.getNext();
                        if (((nExp = vArc.getExpr()) != null) && (nExp.getExprText().length() > 0)) {
                            nExp = new LexRegExp(0, nExp.getExprText());
                        } else if (((ii = vArc.getNodeTo()) >= 0) && (ii != i)) needArc = 0;
                        curArc = new FA_Arc(i, vArc.getNodeTo(), nExp);
                        curArc.setNext(null);
                        curArc.setNext(rArc);
                        rArc = curArc;
                        curArc = null;
                        vArc = nArc;
                    }
                }
                curArc = tArc;
            }
            fromNode[i] = rArc;
        }
        rootArc = null;
    }

    private void markUsedNodes() {
        int i, n, p;
        FA_Arc curArc;
        if (state == 1)
            state = 2;
        else
            return;
        int[] m = new int[nodeCnt + 1];
        int[] t = new int[nodeCnt + 1];
        for (i = 0; i <= nodeCnt; i++) {
            m[i] = 0;
            t[i] = 0;
        }
        m[0] = -1;
        p = 1;
        while (p != 0) {
            p = 0;
            for (i = 0; i <= nodeCnt; i++) {
                if (m[i] < 0) {
                    m[i] = 1;
                    curArc = fromNode[i];
                    while (curArc != null) {
                        n = curArc.getNodeTo();
                        if (n >= 0) {
                            if (m[n] == 0) {
                                p = 1;
                                m[n] = -1;
                            }
                        } else t[i] = -1;
                        if ((curArc.getMark() == null) && (n >= 0))
                            curArc.setMark();
                        curArc = curArc.getNext();
                    }
                }
            }
        }
        for (i = 0; i <= nodeCnt; i++) {
            if ((m[i] == 0) && (fromNode[i] != null)) {
                fromNode[i] = null;
            }
        }
    }

    private ArrayList<String> toDFA() {
        char c;
        int aCnt, i, j, k, l, m, ton = 0, stp = 0, rabNo = 1, finNo = -1;
        int[] frNode = null, toNode = null;
        FA_Arc curArc, newArc;
        NodeSet cSet, lSet, tSet;
        StringBuilder rab, cs, ns;
        if (state == 2)
            state = 3;
        else
            return null;
        FA_Arc[] rFromNode = new FA_Arc[nodeCnt + 1];
        newArc = null;
        cs = new StringBuilder("");
        ns = new StringBuilder("");
        rootArc = null;
        aCnt = nodeCnt + finNodesCnt + 100;
        toNode = new int[aCnt];
        nSet = cSet = lSet = new NodeSet(1, toNode);
        nSet.setNo(0);
        stp = 0;
        while (cSet != null) {
            frNode = cSet.getNodes();
            if ((k = ns.length()) > 0)
                ns.delete(0, k);
            stp = 1;
            k = 0;
            i = 0;
            while (stp > 0) {
                if (frNode[i] >= 0) {
                    curArc = fromNode[frNode[i]];
                    while (curArc != null) {
                        switch (stp) {
                            case 1:
                                if ((ton = curArc.getNodeTo()) < 0) {
                                    for (j = 0; j < k; j++)
                                        if (toNode[j] == ton) {
                                            j = -1;
                                            break;
                                        }
                                    if (j >= 0) toNode[k++] = ton;
                                }
                                break;
                            case 2:
                                if (((rab = curArc.getMark()) != null) && (rab.length() == 0)) {
                                    ton = curArc.getNodeTo();
                                    for (j = 0; j < k; j++)
                                        if (toNode[j] == ton) {
                                            j = -1;
                                            break;
                                        }
                                    if (j >= 0) toNode[k++] = ton;
                                }
                                break;
                            default:
                                if (((rab = curArc.getMark()) == null) || (rab.length() == 0)) break;
                                if (ns.length() == 0) {
                                    ns.append(rab);
                                    toNode[k++] = curArc.getNodeTo();
                                }//break;}
                                if (cs.length() > 0) cs.delete(0, cs.length());
                                for (m = 0; m < ns.length(); m++) {
                                    c = ns.charAt(m);
                                    for (j = 0; j < rab.length(); j++)
                                        if (rab.charAt(j) == c) {
                                            cs.append(c);
                                            break;
                                        }
                                }
                                if (cs.length() > 0) {
                                    ns.delete(0, ns.length());
                                    ns.append(cs);
                                    ton = curArc.getNodeTo();
                                    for (j = 0; j < k; j++)
                                        if (toNode[j] == ton) {
                                            j = -1;
                                            break;
                                        }
                                    if (j >= 0) toNode[k++] = ton;
                                }
                        }
                        curArc = curArc.getNext();
                    }
                }
                if (++i >= frNode.length) {
                    if (k > 0) {
                        if (stp > 2) {
                            for (m = 0; m < frNode.length; m++) {
                                if ((frNode[m] >= 0) && ((curArc = fromNode[frNode[m]]) != null)) {
                                    while (curArc != null) {
                                        curArc.delFromMark(ns);
                                        curArc = curArc.getNext();
                                    }
                                }
                            }
                        }
                        m = 0;
                        if (stp == 0)
                            m = k;
                        while (m < k) {
                            if (toNode[m] >= 0) {
                                newArc = fromNode[toNode[m]];
                                while (newArc != null) {
                                    if ((ton = newArc.getNodeTo()) < 0) {
                                        for (j = 0; j < k; j++)
                                            if (toNode[m] == ton) {
                                                j = -1;
                                                break;
                                            }
                                        if (j >= 0) toNode[k++] = ton;
                                    }
                                    newArc = newArc.getNext();
                                }
                            }
                            m += 1;
                        }

                        tSet = nSet;
                        while (tSet != null) {
                            if (tSet.compareTo(k, toNode)) break;
                            tSet = tSet.getNext();
                        }
                        if (tSet == null) {
                            tSet = new NodeSet(k, toNode);
                            lSet.setNext(tSet);
                            lSet = tSet;
                            if (tSet.getNo() >= 0) tSet.setNo(rabNo++);
                            else tSet.setNo(finNo--);
                        }
                        newArc = new FA_Arc(0, 0, null);
                        newArc.setNodeFrom(cSet.getNo());
                        newArc.setNodeTo(tSet.getNo());
                        if (tSet.getNo() >= 0) {
                            newArc.setMark(ns);
                        } else
                            newArc.setExpr(null);
                        newArc.setNext(rootArc);
                        rootArc = newArc;
                    } else if (stp > 2) {
                        for (m = 0; m < frNode.length; m++)
                            if ((frNode[m] >= 0) && ((curArc = fromNode[frNode[m]]) != null)) {
                                while (curArc != null) {
                                    if (curArc.getNodeTo() >= 0) {
                                        curArc.setMark();
                                    }
                                    curArc = curArc.getNext();
                                }
                            }
                        stp = -2;
                    }
                    i = 0;
                    k = 0;
                    stp += 1;
                    if (ns.length() > 0) ns.delete(0, ns.length());
                }
            }
            stp = 1;
            cSet = cSet.getNext();
        }
        if (rabNo >= nodeCnt) rFromNode = new FA_Arc[rabNo];
        nodeCnt = rabNo - 1;
        curArc = rootArc;
        ton = -1;
        while (curArc != null) {
            newArc = curArc.getNext();
            curArc.setNext(null);
            if ((i = curArc.getNodeFrom()) >= 0) {
                if (rFromNode[i] == null) rFromNode[i] = curArc;
                else {
                    curArc.setNext(rFromNode[i]);
                    rFromNode[i] = curArc;
                }
                if ((i == 0) && ((k = curArc.getNodeTo()) > 0) && (curArc.getMark() != null) && (curArc.getMark().length() == 0)) {
                    flagO = true;
                    ton = k;
                }
            }
            curArc = newArc;
        }
        ArrayList<String> badNames = new ArrayList();
        tSet = nSet;
        while (tSet != null) {
            frNode = tSet.getNodes();
            k = frNode.length;
            if (cs.length() > 0) cs.delete(0, cs.length());
            if (ns.length() > 0) ns.delete(0, ns.length());
            ArrayList<String> curNames = new ArrayList();
            for (i = 0; i < k; i++) {
                if ((l = frNode[i]) < 0) {
                    l = -frNode[i] - 1;
                    if (l < rNames.size()) {
                        String n = rNames.get(l);
                        curNames.add(n);
                        cs.append(((cs.length() > 0) ? "," : "") + n);
                        int in = grpNames.indexOf(n);
                        ns.append(((ns.length() > 0) ? " " : "") + ((in >= 0) && (in < actions.size()) ? actions.get(in) : null));
                    } else
                        curNames.add("???");
                } else {
                    break;
                }
            }
            if (cs.length() > 0) {
                tSet.setGrpName(cs);
                if ((cs.indexOf(",") >= 0) && (cs.indexOf("!") >= 0)) {
                    for (int xi = 0; xi < curNames.size(); xi++)
                        if (curNames.get(xi).indexOf("Word!") == 0)
                            badNames.add(curNames.get(xi));
                }
            }
            if (ns.length() > 0) tSet.setAction(ns);
            tSet = tSet.getNext();
        }
        if (badNames.size() > 0) {
            for (int xi = 0; xi < badNames.size(); xi += 1) {
                int nd = grpNames.indexOf(badNames.get(xi));
                if (nd >= 0) {
                    grpNames.remove(nd);
                    regExprs.remove(nd);
                    actions.remove(nd);
                    nodeCnt -= 1;
                }
                nd = rNames.indexOf(badNames.get(xi));
                if (nd >= 0)
                    rNames.remove(nd);
            }
        }
        for (int xi = 0; xi < rFromNode.length; xi++)
            fromNode[xi] = rFromNode[xi];
        rActions = new ArrayList();
        tSet = nSet;//grpCnt=0;
        while (tSet != null) {
            if ((l = tSet.getNo()) < 0) {
                l = -l - 1;
                for (int il = rNames.size(); il <= l; il += 1)
                    rNames.add("");
                for (int il = rActions.size(); il <= l; il += 1)
                    rActions.add("");
                rNames.set(l, tSet.getGrpName());
                rActions.set(l, tSet.getAction());
            }
            tSet = tSet.getNext();
        }
        return rNames;
    }

    private StringBuilder delEquNodes() {
        int appCnt = 0, delCnt, i, j, k, to;
        ArrayList<Integer> fto[] = new ArrayList[nodeCnt + 1];
        FA_Arc curArc, rabArc, rArc, tstArc, tArc, frN[], tiArc, tjArc;
        StringBuilder mark, mrk;
        StringBuilder r = new StringBuilder("");
        String s;
        if (state != 3)
            return r;
        state = 4;
        frN = new FA_Arc[nodeCnt + 1];
        for (i = 0; i <= nodeCnt; i++) {
            fto[i] = new ArrayList();
            curArc = fromNode[i];
            rArc = null;
            frN[i] = null;
            while (curArc != null) {
                mark = curArc.getMark();
                appCnt = 0;
                if (mark == null) {
                    mark = new StringBuilder();
                    appCnt = 1;
                }
                to = curArc.getNodeTo();
                tstArc = curArc;
                while (tstArc != null) {
                    tArc = tstArc.getNext();
                    if ((tArc != null) && (tArc.getNodeTo() == to)) {
                        mrk = tArc.getMark();
                        if ((mrk != null) && ((k = mrk.length()) > 0)) {
                            for (j = 0; j < k; j++) {
                                s = mrk.substring(j, j + 1);
                                if (mark.indexOf(s) < 0) {
                                    mark.append(s);
                                    appCnt += 1;
                                }
                            }
                        }
                        tstArc.setNext(null);
                        tstArc.setNext(tArc.getNext());
                        tArc = tstArc.getNext();
                    }
                    tstArc = tArc;
                }
                if (fto[i].lastIndexOf(to) < 1)
                    fto[i].add(to);
                rabArc = new FA_Arc(null, 0, 1);
                if ((appCnt > 0) && (mark != null) && (curArc.getNodeTo() >= 0))
                    curArc.setMark(mark);
                rabArc.setNodeFrom(curArc.getNodeFrom());
                rabArc.setNodeTo(curArc.getNodeTo());
                rabArc.setMark(curArc.getMark());
                if (rArc == null)
                    frN[i] = rabArc;
                else
                    rArc.setNext(rabArc);
                rArc = rabArc;
                curArc = curArc.getNext();
            }
        }

        delCnt = 1;
        while (delCnt > 0) {
            for (i = 0; i <= nodeCnt; i++) {
                int aSize = fto[i].size();
                for (j = 1; j < aSize; j++)
                    for (k = j + 1; k < aSize; k++)
                        if (fto[i].get(k) < fto[i].get(j)) {
                            Integer t = fto[i].get(k);
                            fto[i].set(k, fto[i].get(j));
                            fto[i].set(j, t);
                        }
            }
            delCnt = 0;
            for (i = 0; i <= nodeCnt; i++) {
                for (j = i + 1; j <= nodeCnt; j++) {
                    if (((k = fto[i].size()) == fto[j].size()) && (frN[j] != null)) {
                        boolean fl = true;
                        for (int n = 0; n < k; n++)
                            if (fto[i].get(n) != fto[j].get(n) && ((fto[i].get(n) != i) || (fto[i].get(n) != j) || (fto[j].get(n) != i) || (fto[j].get(n) != j))) {
                                fl = false;
                                break;
                            }
                        if (fl) {
                            tiArc = frN[i];
                            int toI, toJ;
                            while (fl && (tiArc != null)) {
                                toI = tiArc.getNodeTo();
                                tjArc = frN[j];
                                while (tjArc != null) {
                                    toJ = tjArc.getNodeTo();
                                    if ((toI == toJ) || ((toI == i) && (toJ == j)) || ((toI == j) && (toJ == i))) {
                                        if (!tiArc.compareMark(tjArc.getMark())) {
                                            fl = false;
                                            break;
                                        }//else                    break;
                                    }
                                    tjArc = tjArc.getNext();
                                }
                                tiArc = tiArc.getNext();
                            }
                            if (fl) {
                                frN[j] = null;
                                for (k = 0; k <= nodeCnt; k++) {
                                    tiArc = frN[k];
                                    while (tiArc != null) {
                                        if (tiArc.getNodeTo() == j)
                                            tiArc.setNodeTo(i);
                                        tiArc = tiArc.getNext();
                                    }
                                }
                                for (k = 0; k <= nodeCnt; k++)
                                    for (int n = 0; n < fto[k].size(); n++)
                                        if (fto[k].get(n) == j)
                                            fto[k].set(n, i);
                                for (k = 0; k < fto[j].size(); k++)
                                    fto[j].set(k, 0);
                                delCnt = 1;
//              nodeCnt-=1;
                                break;
                            }
                        }
                    }
                    if (delCnt != 0)
                        break;
                }
                if (delCnt != 0)
                    break;
            }
        }
        delCnt = 0;
        for (i = 0, j = 0; i <= nodeCnt; i++) {
            if (frN[i] != null)
                delCnt += 1;
        }
        fromNode = new FA_Arc[delCnt];
        for (i = 0, j = 0; i <= nodeCnt; i++) {
            if (frN[i] != null) {
                fromNode[j] = frN[i];
                j += 1;
            }
        }
        for (i = 0, j = 0; i <= nodeCnt; i++) {
            if (frN[i] != null) {
                if (i != j) {
                    curArc = fromNode[j];
                    while (curArc != null) {
                        if (curArc.getNodeFrom() != j) {
                            curArc.setNodeFrom(j);
                        }
                        curArc = curArc.getNext();
                    }
                    for (k = 0; k < delCnt; k++) {
                        curArc = fromNode[k];
                        while (curArc != null) {
                            if (curArc.getNodeTo() == i) {
                                curArc.setNodeTo(j);
                            }
                            curArc = curArc.getNext();
                        }
                    }
                }
                j += 1;
            }
        }
        nodeCnt = delCnt;
        for (i = 0; i < nodeCnt; i++) {
            curArc = fromNode[i];
            while (curArc != null) {
                j = curArc.getNodeTo();
                if ((curArc.getMark() != null) && (curArc.getMark().length() > 0)) {
                    rArc = curArc;
                    rabArc = rArc.getNext();
                    while (rabArc != null) {
                        if ((j == rabArc.getNodeTo()) && (rabArc.getMark() != null) && (rabArc.getMark().length() > 0)) {
                            curArc.setMark(new StringBuilder(curArc.getMark().toString() + rabArc.getMark().toString()));
                            rArc.setNext(null);
                            rArc.setNext(rabArc.getNext());
                        } else
                            rArc = rabArc;
                        rabArc = rabArc.getNext();
                    }
                }
                curArc = curArc.getNext();
            }
        }
        for (i = 1; i < nodeCnt; i++) {
            curArc = fromNode[i];
            if ((curArc.getMark() == null) && (curArc.getNext() == null) && ((k = curArc.getNodeTo()) < 0)) {
                for (j = 0; j < nodeCnt; j++) {
                    curArc = fromNode[j];
                    while (curArc != null) {
                        if ((to = curArc.getNodeTo()) == i)
                            curArc.setNodeTo(k);
                        if (to > i)
                            curArc.setNodeTo(to - 1);
                        curArc = curArc.getNext();
                    }
                }
                for (j = i + 1; j < nodeCnt; j++)
                    fromNode[j - 1] = fromNode[j];
                nodeCnt -= 1;
            }
        }
        nFN.append(",\"arcs\":");
        for (i = 0, j = 0; i < nodeCnt; i++) {
            if (fromNode[i] != null) {
                delCnt += 1;
                nFN.append((j > 0 ? "," : "") + "[\"from\"," + i + "]");
                curArc = fromNode[i];
                while (curArc != null) {
                    if (curArc.getMark() != null)
                        nFN.append(",[\"to\"," + curArc.getNodeTo() + "],[\"by\",\"" + curArc.getMark().toString().replaceAll("\"", "\\\"") + "\"]");
                    else
                        nFN.append(",[\"to\"," + curArc.getNodeTo() + "],[\"by\",\"" + curArc.getMark() + "\"]");
                    curArc = curArc.getNext();
                }
                j += 1;
            }
        }
        nFN.append("]}");
        return r;
    }

    public int getWorkNodesCount() {
        return nodeCnt;
    }

    public String getGrpName(int i) {
        if ((i >= 0) && (i < grpNames.size()))
            return grpNames.get(i);
        else
            return null;
    }

    public String getRegExpr(int i) {
        if ((i >= 0) && (i < regExprs.size()))
            return regExprs.get(i);
        return null;
    }

    public String getAction(int i) {
        if ((i >= 0) && (i < actions.size()))
            return actions.get(i);
        return null;
    }

    public FA_Arc getFirstFrom(int f) {
        if ((f < 0) || (f >= fromNode.length))//(f>nodeCnt)||(fromNode==null))
            return null;
        return fromNode[f];
    }

    public FA_Arc getRootArc() {
        return rootArc;
    }

    public int getNamesCount() {
        return grpNames.size();
    }

    public int getGrpCount() {
        return grpNames.size();
    }

    public NodeSet getNodeSet() {
        return nSet;
    }

    public FA_Arc[] getFromNode() {
        return fromNode;
    }
}