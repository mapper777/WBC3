package application.work.tplParser;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Stack;

public class BldParser {
    BldScaner la;
    Lexem currentLexem;
    int cCnt = 0;
    Properties tStrings;
    Stack stk = new Stack();
    Integer stkItem;
    int curWordIndex;
    StringBuilder x=new StringBuilder();
    String[] words = {
            "(",
            ")",
            "=",
            "else",
            "endfor",
            "endif",
            "foreach",
            "if",
            "in"};

    StringBuffer txt = new StringBuffer();

    class PseudoCode {
        public int count = 1;
        private int label = 0;
        private ArrayList<PseudoCodeItem> items = new ArrayList();

        public void add(int typ, StringBuffer val) {
            items.add(new PseudoCodeItem(typ, label, val.toString()));
            label = 0;
        }

        public void add(int typ, int code, StringBuffer val) {
            items.add(new PseudoCodeItem(typ, label, code, val.toString()));
            label = 0;
        }

        public void add(int typ, String val) {
            items.add(new PseudoCodeItem(typ, label, val));
            label = 0;
        }

        public void add(int typ, int code, String val) {
            items.add(new PseudoCodeItem(typ, label, code, val));
            label = 0;
        }

        public void add(int typ) {
            items.add(new PseudoCodeItem(typ, label, null));
            label = 0;
        }

        public void setLabel(int l) {
            if (label > 0)
                add(0, "");
            label = l;
        }

        public ArrayList<PseudoCodeItem> setLabels() {
            int i, j, l;
            PseudoCodeItem it;
            if (label > 0)
                add(0, "");
            for (i = 0; i < items.size(); i++) {
                it = (PseudoCodeItem) items.get(i);
                if ((it.typ >= 6) && (it.label > 0)) {
                    l = it.label;
                    for (j = 0; j < items.size(); j++) {
                        if ((((PseudoCodeItem) items.get(j)).typ < 6) && (((PseudoCodeItem) items.get(j)).label == l)) {
                            it.ind = j;
                            break;
                        }
                    }
                }
            }
            return items;
        }
    }

    public String errString = "";
    PseudoCode pCode = new PseudoCode();
    Stack wStack = new Stack();
    private String w;

    public BldParser(ReaderFromFile tr, Properties ts) {
        la = new BldScaner(tr, tStrings);
        tStrings = ts;
        stk.push(Integer.valueOf(0));
        stk.push(Integer.valueOf(-1));
    }

    public ArrayList getPCode() {
        return pCode.setLabels();
    }
    public StringBuilder getTextTpl(){
        return x;
    }
    private int getWordIndex() {
        int i;
        currentLexem = la.getLexem();
        if ((currentLexem.groupIndex == 2) && (currentLexem.wordIndex == -1))
            w = currentLexem.textOfWord.toString().toLowerCase();
        else
            w = new String(currentLexem.textOfWord);
        x.append(" "+w);
        for (i = 0; i < words.length; i++)
            if (words[i].compareTo(w) == 0)
                return i + 3;
        return currentLexem.groupIndex;
    }

    private String getW() {
        if (currentLexem.groupIndex == 1) {
            if (w.length() > 21)
                w = w.substring(0, 20) + " ...";
            w = w.replaceAll("\"", "``");
        }
        return w;
    }

    private int getI(StringBuffer t) {
        String v = tStrings.getProperty(t.toString());
        if (v == null)
            return -1;
        return Integer.parseInt(v);
    }

    public int getStatistic(int i) {
        return cCnt;
    }

    public boolean parse() {
        int cnt=0;
        curWordIndex = getWordIndex();
        while (stk.size() > 0) {
            stkItem = (Integer) stk.pop();
            switch (stkItem.intValue()) {
                case 0:
                    return ((stk.size() == 0) && (curWordIndex == 0));
                case -1:
                    switch (curWordIndex) {
                        case 0:
                        case 1:
                        case 5:
                        case 9:
                        case 10:
                            stk.push(Integer.valueOf(12));
                            stk.push(Integer.valueOf(-2));
                            break;
                        default:
                            errString = "Wanted EOF, =, if, forEach, <text for substitution>, but no" + getW() + "'";
                            return false;
                    }
                    break;

                case -2:
                    switch (curWordIndex) {
                        case 1:
                            stk.push(Integer.valueOf(-2));
                            stk.push(Integer.valueOf(1));
                            stk.push(Integer.valueOf(13));
                            break;
                        case 5:
                            stk.push(Integer.valueOf(-2));
                            stk.push(Integer.valueOf(2));
                            stk.push(Integer.valueOf(15));
                            stk.push(Integer.valueOf(5));
                            stk.push(Integer.valueOf(14));
                            break;
                        case 9:
                            stk.push(Integer.valueOf(-2));
                            stk.push(Integer.valueOf(7));
                            stk.push(Integer.valueOf(17));
                            stk.push(Integer.valueOf(-2));
                            stk.push(Integer.valueOf(-3));
                            stk.push(Integer.valueOf(9));
                            stk.push(Integer.valueOf(16));
                            break;
                        case 10:
                            stk.push(Integer.valueOf(19));
                            stk.push(Integer.valueOf(-5));
                            stk.push(Integer.valueOf(-2));
                            stk.push(Integer.valueOf(-4));
                            stk.push(Integer.valueOf(10));
                            stk.push(Integer.valueOf(18));
                            break;
                        case 0:
                        case 6:
                        case 7:
                        case 8:
                            break;
                        default:
                            errString = "Wanted EOF, =, if, else, endIf, forEach, endFor, <text for substitution>, but no '" + getW() + "'";
                            return false;
                    }
                    break;

                case -3:
                    switch (curWordIndex) {
                        case 3:
                            stk.push(Integer.valueOf(4));
                            stk.push(Integer.valueOf(21));
                            stk.push(Integer.valueOf(2));
                            stk.push(Integer.valueOf(20));
                            stk.push(Integer.valueOf(11));
                            stk.push(Integer.valueOf(2));
                            stk.push(Integer.valueOf(13));
                            stk.push(Integer.valueOf(3));
                            break;
                        default:
                            errString = "Wanted (, but no '" + getW() + "'";
                            return false;
                    }
                    break;

                case -4:
                    switch (curWordIndex) {
                        case 3:
                            stk.push(Integer.valueOf(4));
                            stk.push(Integer.valueOf(23));
                            stk.push(Integer.valueOf(2));
                            stk.push(Integer.valueOf(22));
                            stk.push(Integer.valueOf(3));
                            break;
                        default:
                            errString = "Wanted (, but no '" + getW() + "'";
                            return false;
                    }
                    break;

                case -5:
                    switch (curWordIndex) {
                        case 8:
                            stk.push(Integer.valueOf(-2));
                            stk.push(Integer.valueOf(8));
                            stk.push(Integer.valueOf(24));
                            break;
                        case 6:
                            stk.push(Integer.valueOf(-2));
                            stk.push(Integer.valueOf(8));
                            stk.push(Integer.valueOf(26));
                            stk.push(Integer.valueOf(-2));
                            stk.push(Integer.valueOf(6));
                            stk.push(Integer.valueOf(25));
                            break;
                        default:
                            errString = "Wanted else, endIf, but no '" + getW() + "'";
                            return false;
                    }
                    break;


                case 12:
                    if (txt.length() > 0) pCode.add(1, txt);
                    txt.delete(0, txt.length());
                    pCode.setLabels();//pCode.exec();
                    break;

                case 13:
                    txt.append(currentLexem.textOfWord);
                    break;

                case 14:
                    if (txt.length() > 0) {
                        pCode.add(1, txt);
                        txt.delete(0, txt.length());
                    }
                    break;

                case 15:
                    pCode.add(2, getI(currentLexem.textOfWord), currentLexem.textOfWord.toString());
                    break;

                case 16:
                    if (txt.length() > 0) {
                        pCode.add(1, txt);
                        txt.delete(0, txt.length());
                    }
                    pCode.add(5);
                    pCode.setLabel(pCode.count);
                    wStack.push(Integer.valueOf(pCode.count));
                    pCode.count += 2;
                    break;

                case 17:
                    if (txt.length() > 0) {
                        pCode.add(1, txt);
                        txt.delete(0, txt.length());
                    }
                    pCode.setLabel(((Integer) wStack.peek()).intValue());
                    pCode.add(6);
                    pCode.setLabel(((Integer) wStack.pop()).intValue() + 1);
                    pCode.add(0);
                    break;

                case 18:
                    if (txt.length() > 0) {
                        pCode.add(1, txt);
                        txt.delete(0, txt.length());
                    }
                    wStack.push(Integer.valueOf(pCode.count));
                    pCode.count += 2;
                    break;

                case 19:
                    wStack.pop();
                    break;

                case 20:
                    txt.append("_");
                    txt.append(currentLexem.textOfWord);
                    pCode.add(4, getI(txt), txt);
                    txt.delete(0, txt.length());
                    break;

                case 21:
                    pCode.setLabel(((Integer) wStack.peek()).intValue() + 1);
                    pCode.add(7);
                    break;

                case 22:
                    pCode.add(3, getI(currentLexem.textOfWord), currentLexem.textOfWord);
                    break;

                case 23:
                    pCode.setLabel(((Integer) wStack.peek()).intValue());
                    pCode.add(7);
                    break;

                case 24:
                    if (txt.length() > 0) {
                        pCode.add(1, txt);
                        txt.delete(0, txt.length());
                    }
                    pCode.setLabel(((Integer) wStack.peek()).intValue());
                    break;

                case 25:
                    if (txt.length() > 0) {
                        pCode.add(1, txt);
                        txt.delete(0, txt.length());
                    }
                    pCode.setLabel(((Integer) wStack.peek()).intValue() + 1);
                    pCode.add(6);
                    pCode.setLabel(((Integer) wStack.peek()).intValue());
                    break;

                case 26:
                    if (txt.length() > 0) pCode.add(1, txt);
                    txt.delete(0, txt.length());
                    pCode.setLabel(((Integer) wStack.peek()).intValue() + 1);
                    break;

                default:
                    if (stkItem.intValue() != curWordIndex) {
                        w = "";
                        switch (curWordIndex) {
                            case 1:
                                w = "')'";
                                break;
                            case 2:
                                w = "'in'";
                                break;
                            case 3:
                                w = "'('";
                                break;
                            case 4:
                                w = "')'";
                                break;
                            case 5:
                                w = "'='";
                                break;
                            case 6:
                                w = "'else'";
                                break;
                            case 7:
                                w = "'endFor'";
                                break;
                            case 8:
                                w = "'endIf'";
                                break;
                            case 9:
                                w = "'forEach'";
                                break;
                            case 10:
                                w = "'if'";
                                break;
                            default:
                                w = "Terminal with code " + "".valueOf(curWordIndex);
                        }
                        errString = "Wanted " + w;
                        return false;
                    }
                    curWordIndex = getWordIndex();
                    break;
            }
            cCnt += 1;
        }
        errString = "Parser stack is empty.";
        return false;
    }
}