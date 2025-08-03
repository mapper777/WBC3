package application.work.templateToAutomat;

import application.work.finAutomat.CycleItem;
import application.work.finiteAutomat.CharToColumn;
import application.work.finiteAutomat.FA_Arc;
import application.work.finiteAutomat.FinAutomat;
import application.work.grammar.*;
import application.work.tplParser.PseudoCodeItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Stack;

public class TemplateToAutomates {

    public String tok = "0";
    public ArrayList<String> text = new ArrayList();
    ArrayList<Integer> numbers = null;
    ArrayList<Short> zeros = null;
    private FinAutomat fa;
    //  private CharToColumn ctI;
    private Grammar gram = null;
    private String dataS;
    private String user;
    private String[] startNT = null;
    //  private String langTo, tplName;
//  private SymbolSet symbolSet;
    private int[][] otn = new int[1][1];
    private String lrT[][] = null;
    private ArrayList<String> actions = null;
    public String fNames = "";
    private PseudoCodeItem item;
    private int sMode = 0,
            targetL = 0,
            cntN = 0,
            cntT = 0,
            cntA = 0;
    public StringBuilder result;

    public TemplateToAutomates(Grammar g, String l, String d) {
        gram = g;
        fa = new FinAutomat(l);
        int ind = d.indexOf('`');
        int nd = d.indexOf('\n');
        if (nd < 0)
            nd = ind + 1;
        if ((ind > 0) && (ind < nd)) {
            user = d.substring(0, ind);
            dataS = d.substring(ind + 1);
        } else {
            user = "";
            dataS = d;
        }
//      otn = gram.calcProperties();
        cntN = gram.getCountOfSymbols(1);
        cntT = gram.getCountOfSymbols(2);
        cntA = gram.getCountOfSymbols(3);
//      if (tplName.indexOf("xLR") > 0)
//          lrT = gram.getLRTable();
        actions = gram.getActions();
    }

    private String charToStr(char c) {
        String r = "";
        if ((c > 0) && (c < 32)) {
            switch (targetL) {
                case 0:  //jsp
                case 1:  //java
                    if (c == 10) {
                        return "\\n";
                    } else if (c == 13) {
                        return "\\r";
                    } else if (c < 16) {
                        return "\\u000" + Integer.toHexString((int) c);
                    } else {
                        return "\\u00" + Integer.toHexString((int) c);
                    }
                case 2:  //c#
                case 4:  //c++
                    if (c == 10) {
                        return "\\n";
                    } else if (c == 12) {
                        return "\\r";
                    } else if (c < 16) {
                        return "\\x000" + Integer.toHexString((int) c);
                    } else {
                        return "\\x00" + Integer.toHexString((int) c);
                    }
                case 3:  //c
                    if (c == 10) {
                        return "\\n";
                    }
                    if (c == 13) {
                        return "\\r";
                    }
                    if (c < 16) {
                        return "\\x0" + Integer.toHexString((int) c);
                    } else {
                        return "\\x0" + Integer.toHexString((int) c);
                    }
                case 5:  //pas
                    if (sMode == 0) {
                        sMode = 1;
                        return "'#" + String.valueOf((int) c);
                    } else {
                        return "#" + String.valueOf((int) c);
                    }
                case 6:  //vb
                    if (sMode == 0) {
                        sMode = 1;
                        return "\"+Chr(" + String.valueOf((int) c) + ")"; //"
                    } else {
                        return "+Chr(" + String.valueOf((int) c) + ")";
                    }
                case 7:  //php
                    if (c < 16) {
                        return "\\x0" + Integer.toHexString((int) c);
                    } else {
                        return "\\x0" + Integer.toHexString((int) c);
                    }
            }
            return String.valueOf(c);
        } else {
            if (sMode == 1) {
                if (targetL == 5) {
                    r = "'";
                } else if (targetL == 6) {
                    r = "+\"";
                }
                sMode = 0;
            }
            if (c == '\'') {  //'
                switch (targetL) {
                    case 0:  //jsp
                    case 1:  //java
                    case 2:  //c#
                    case 3:  //c
                    case 4:  //c++
                        return r + "\\\'";
                    case 7:  //php
                        return "'";
                    case 5:  //pas
                        return r + "\'\'";
                    case 6:  //vb
                        return r + "'";
                }
                return r + String.valueOf(c);
            } else if (c == '\"') {
                switch (targetL) {
                    case 0:  //jsp
                    case 1:  //java
                    case 2:  //c#
                    case 3:  //c
                    case 4:  //c++
                    case 7:  //php
                        return r + "\\\"";
                    case 5:  //pas
                        return r + "\"";
                    case 6:  //vb
                        return r + "\"\"";
                }
                return r + String.valueOf(c);
            } else if (c == '\\') {
                switch (targetL) {
                    case 0:  //jsp
                    case 1:  //java
                    case 2:  //c#
                    case 3:  //c
                    case 4:  //c++
                    case 7:  //php
                        return r + "\\\\";
                    case 5:  //pas
                        return r + "\\";
                    case 6:  //vb
                        return r + "\\";
                }
                return r + String.valueOf(c);
            } else {
                return r + String.valueOf(c);
            }
        }
    }

    private void buildCompact() {
        numbers = new ArrayList();
        zeros = new ArrayList();
        int varA;
        int varS;
        int varR;

        short k = 0;
        boolean added;
        String tmpString;
        numbers.add(lrT[0].length);

        for (int rowIndex = 0; rowIndex < lrT.length; rowIndex++) {
            for (int columnIndex = 0; columnIndex < lrT[rowIndex].length; columnIndex++) {
                varA = varS = varR = 0;
                if ((tmpString = lrT[rowIndex][columnIndex]) != null) {
                    while (tmpString.length() > 0) {
                        int i = tmpString.indexOf(" ");
                        if (i < 0) {
                            i = tmpString.length();
                        }
                        switch (tmpString.charAt(0)) {
                            case 'A':
                                varA = (Integer.parseInt(tmpString.substring(1, i)) + 1) << 20;
                                break;
                            case 'S':
                                if (tmpString.substring(1, i).compareTo("-1") == 0) {
                                    varS = -1;
                                } else {
                                    varS = 0xc0000000 | ((Integer.parseInt(tmpString.substring(1, i))) & 0xfffff);
                                }
                                break;
                            case 'G':
                                varS = 0x80000000 | ((Integer.parseInt(tmpString.substring(1, i))) & 0xfffff);
                                break;
                            case 'R':
                                int j = tmpString.indexOf(",");
                                varR = 0x40000000 | (Integer.parseInt(tmpString.substring(1, j)) << 10) | Integer.parseInt(tmpString.substring(j + 1, i));
                                break;
                        }
                        if (i >= tmpString.length()) {
                            break;
                        } else {
                            tmpString = tmpString.substring(i + 1);
                        }
                    }
                    if (varS != 0) {
                        varR = 0;
                    }
                }
                int result = varA | varR | varS;
                added = false;

                if (columnIndex == 0) {
                    numbers.add(result);
                    added = true;
                }

                if ((result == 0) && (columnIndex != lrT[rowIndex].length - 1) && added == false) {
                    k++;
                    added = true;
                }
                if ((result != 0) && (columnIndex != lrT[rowIndex].length - 1) && added == false) {
                    zeros.add(k);
                    numbers.add(result);
                    k = 0;
                    added = true;
                }
                if ((result == 0) && (columnIndex == lrT[rowIndex].length - 1) && added == false) {
                    k++;
                    zeros.add(k);
                    k = 0;
                    added = true;
                }
                if ((result != 0) && (columnIndex == lrT[rowIndex].length - 1) && added == false) {
                    zeros.add(k);
                    numbers.add(result);
                    k = 0;
                    zeros.add(k);
                    added = true;
                }
            }
        }
    }

    public ArrayList getText() {
        return text;
    }

    public ArrayList makeModule(ArrayList<PseudoCodeItem> prg) {
        int i = 0,
                j = 0,
                k = 0,
                pInd = 0,
                maxInd = prg.size(),
                cCnt = 0,
                iTmp = 0,
                typeOfSymbol = 0,
                //    symbolStateIndex=-1,
                varA = 0,
                varS = 0,
                varR = 0;

        char c = ' ';
        boolean flag = false;
        String tmpString = "", tmpStr = "";

        StringBuilder tmpSBuffer = new StringBuilder();
        StringBuilder tmpStringB = new StringBuilder();
        String nameOfSymbol = null;
        boolean condition = true;
        Stack<CycleItem> stack = new Stack();
        CycleItem topOfStack = null;
        GregorianCalendar currentDate = new GregorianCalendar();

        int currentAutomat = 0;
        int currentState = -1;
        int finalState = -1;
        int currentColumn = -1;
        int currentRange = -1;
        int indexOfTerminal = -1;
        int indexOfWord = -1;
        int indexOfAction = -1;
        int indexOfState = -1;
        int indexOfColumn = -1;
        int indexOfRule = -1;
        int ruleIndex = -1;
        int indexOfGrammarSymbol = -1;
        int indexOfSymbolFromSS = -1;
        int indexOfGrammar = -1;
        int indexOfSymbolFromRightPart = -1;
        int indexOfSymbolFromFollowSet = -1;
        FinAutomat currentFAutomat = fa;
        CharToColumn currentTransliterator = new CharToColumn(fa);
        FA_Arc currentArc = null;
        FA_Arc tmpArc = null;
        Rule currentRule = null;
        Rule tmpRule = null;
        Symbol currentSymbol = null;
        Symbol currentWord = null;
        Symbol rightPartOFESymbol = null;
        ArrayList startNT_FollowSet = null;
//    ArrayList tmpFollowSet=null;
//    SymbolSet mainSymbolSet=(gram==null ? null : gram.getSymbolSet());
        Symbol currentNonTerminal = null;
        Symbol tmpSymbol = null;
        Symbol symbolFromSS = null;
        Symbol currentGrammarSymbol = null;
        RSymbol currentSymbolFromRightPart = null;
        ArrayList currentSelSet = null;
        Symbol currentAction = null;
        Rule currentARule = null;
        SymbolSet symbolSet = (gram == null ? null : gram.getSymbolSet());
//if(prg!=null)return null;
//text.add(currentTransliterator.allMarks.toString());
        for (j = 1, i = 0; i < prg.size(); i++) {
            item = prg.get(i);
            j += item.no;
            item.no = j;
        }
//try{
        while (pInd < maxInd) {
            if (cCnt++ > 999999) {
                break;
            }
            item = prg.get(pInd);
            switch (item.typ) {
                case 0:
                    pInd += 1;
                    break;
                case 1:
                    text.add(item.value);
                    pInd += 1;
                    break;
                case 2:
                    switch (item.code) {
                        case 0:  //fileNames
                            text.add(fNames);
                            break;
                        case 1:  //buildDate
                            String[] partD = (new Date()).toString().split(" ");
                            text.add("" + currentDate.get(currentDate.DAY_OF_MONTH) + "." + (currentDate.get(currentDate.MONTH) + 1) + "." + currentDate.get(currentDate.YEAR) + " " + partD[3]);
                            break;
                        case 2:  //lexAnalyser.data
                        case 3://parser.data
                            if (dataS != null) //                            text.add(macrosProc(dataS[1]));
                            {
                                text.add(dataS);
                            }
                            break;
                        case 4:  //arrayOfAutomat.length
                        case 5:  //arrayOfAutomat.count
                            text.add("1");
                            break;
                        case 6:  //currentAutomat.states.count
                            if (currentFAutomat != null) {
                                text.add("".valueOf(currentFAutomat.getWorkNodesCount() + 1));
                            } else {
                                return null;
                            }
                            break;
                        case 7:  //currentAutomat.statesOpt.count
                            if (currentTransliterator != null) {
                                text.add("".valueOf(currentTransliterator.getRows()));
                            } else {
                                return null;
                            }
                            break;
                        case 8:  //currentAutomat.finalStates.count
                            if (currentFAutomat != null) {
                                text.add("".valueOf(currentFAutomat.getGrpCount() + 1));
                            } else {
                                return null;
                            }
                            break;
                        case 9:  //currentAutomat.index
                            if (currentFAutomat != null) {
                                text.add("".valueOf(currentAutomat));
                            } else {
                                return null;
                            }
                            break;
                        case 10:  //currentAutomat.name
                            if (currentFAutomat != null) {
                                text.add(currentFAutomat.getName());
                            } else {
                                return null;
                            }
                            break;
                        case 11:  //currentAutomat.ranges.count
                            if (currentTransliterator != null) {
                                text.add("".valueOf(currentTransliterator.getRangesCount()));
                            } else {
                                return null;
                            }
                            break;
                        case 12:  //currentAutomat.columns.count
                            if (currentTransliterator != null) {
                                text.add("".valueOf(currentTransliterator.getColumnsCount()));
                            } else {
                                return null;
                            }
                            break;
                        case 13:  //currentAutomat.hasOtherColumn
                            if (currentFAutomat != null) {
                                text.add(currentFAutomat.getFlagOther() ? "1" : "0");
                            } else {
                                return null;
                            }
                            break;
                        case 14:  //currentState.arcs.count
                            if (currentFAutomat != null) {
                                if ((currentState < 0) || (currentState > currentFAutomat.getWorkNodesCount())) {
                                    return null;
                                }
                                tmpArc = currentFAutomat.getFirstFrom(currentState);
                                i = 0;
                                while (tmpArc != null) {
                                    i += 1;
                                    tmpArc = tmpArc.getNext();
                                }
                                text.add("".valueOf(i));
                            } else {
                                return null;
                            }
                            break;
                        case 15:  //currentStateAndPrevStates.arcs.count
                            if (currentFAutomat != null) {
                                if ((currentState < 0) || (currentState > currentFAutomat.getWorkNodesCount())) {
                                    return null;
                                }
                                for (i = 0, j = 0; j <= currentState; j++) {
                                    tmpArc = currentFAutomat.getFirstFrom(j);
                                    while (tmpArc != null) {
                                        i += 1;
                                        tmpArc = tmpArc.getNext();
                                    }
                                }
                                text.add("".valueOf(i));
                            } else {
                                return null;
                            }
                            break;
                        case 16:  //currentCell.stateTo
                            if (currentTransliterator != null) {
                                if ((currentState < 0) || (currentState > currentFAutomat.getWorkNodesCount()) || (currentColumn < 0) || (currentColumn >= currentTransliterator.getColumnsCount())) {
                                    return null;
                                }
                                if ((currentState == 0) && (currentColumn == 0)) {
                                    text.add("-1");
                                } else {
                                    text.add("".valueOf(currentTransliterator.getStateTo(currentState, currentColumn)));//+"["+currentState+","+currentColumn+"]"); //!!!
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 17:  //currentArc.stateTo
                            if (currentFAutomat != null) {
                                if ((currentArc == null) || (currentState < 0) || (currentState > currentFAutomat.getWorkNodesCount())) {
                                    return null;
                                }
                                i = currentArc.getNodeTo();
                                if (i < 0) {
//                    i -= (currentState > 0 ? 1 : 0);
                                    i -= 1;
                                }
                                text.add("".valueOf(i));
                            } else {
                                return null;
                            }
                            break;
                        case 18:  //currentState.index
                            if (currentFAutomat != null) {
                                if ((currentState < 0) || (currentState > currentFAutomat.getWorkNodesCount())) {
                                    return null;
                                }
                                text.add("".valueOf(currentState));
                            } else {
                                return null;
                            }
                            break;
                        case 19:  //finalState.index
                            if (currentFAutomat != null) {
                                if ((finalState < 0) || (finalState > currentFAutomat.getGrpCount())) {
                                    return null;
                                }
                                text.add("-" + (finalState + 2));
                            } else {
                                return null;
                            }
                            break;
                        case 20:  //finalState.indexForGrammar
                            if (currentFAutomat != null) {
                                if ((symbolSet != null) && ((tmpString = currentFAutomat.getGrpName(finalState)) != null)) {
                                    for (i = 0; i < symbolSet.getSize(); i++) {
                                        tmpSymbol = symbolSet.getSymbol(i);
                                        if ((tmpString.compareTo(tmpSymbol.getName()) == 0) && (tmpSymbol.getType() == -2)) {
                                            text.add("".valueOf(i - cntN));
                                            break;
                                        }
                                    }
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 21:  //EndOfFile.indexForGrammar
                            if (symbolSet != null) {
                                for (i = 0; i < symbolSet.getSize(); i++) {
                                    if (symbolSet.getSymbol(i).getType() == -1) {
                                        text.add("".valueOf(i - cntN));
                                        break;
                                    }
                                }
                            } else {
                                text.add("0");
                            }
                            break;
                        case 22:  //currentRange.columnIndex
                            if (currentTransliterator != null) {
                                text.add("".valueOf(currentTransliterator.getRangeIndex(currentRange)));
                            } else {
                                return null;
                            }
                            break;
                        case 23:  //currentRange.firstChar
                            if (currentTransliterator != null) {
                                if ((currentRange < 0) || (currentRange >= currentTransliterator.getRangesCount())) {
                                    return null;
                                }
                                c = currentTransliterator.getFirstCharOfRange(currentRange);
                                tmpString = charToStr(c);
                                if (sMode == 1) {
                                    if (targetL == 5) {
                                        tmpString += "'";
                                    } else if (targetL == 6) {
                                        tmpString += "+\"";
                                    }
                                    sMode = 0;
                                }
                                text.add(tmpString);
                            } else {
                                return null;
                            }
                            break;
                        case 24:  //currentRange.lastChar
                            if (currentTransliterator != null) {
                                if ((currentRange < 0) || (currentRange >= currentTransliterator.getRangesCount())) {
                                    return null;
                                }
                                c = currentTransliterator.getLastCharOfRange(currentRange);
                                tmpString = charToStr(c);
                                if (sMode == 1) {
                                    if (targetL == 5) {
                                        tmpString += "'";
                                    } else if (targetL == 6) {
                                        tmpString += "+\"";
                                    }
                                    sMode = 0;
                                }
                                text.add(tmpString);
                            } else {
                                return null;
                            }
                            break;
                        case 25:  //currentArc.mark
                            if (currentArc != null) {
                                if (((tmpSBuffer = currentArc.getMark()) != null) && (!tmpSBuffer.toString().equals("null"))) {
                                    if (tmpStringB.length() > 0) {
                                        tmpStringB.delete(0, tmpStringB.length());
                                    }
                                    for (i = 0; i < tmpSBuffer.length(); i++) {
                                        c = tmpSBuffer.charAt(i);
                                        tmpStringB.append(charToStr(c));
                                        if (sMode == 1) {
                                            if (targetL == 5) {
                                                tmpStringB.append(charToStr(c) + "'");
                                            } else if (targetL == 6) {
                                                tmpStringB.append(charToStr(c) + "+\"");
                                            }
                                            sMode = 0;
                                        }
                                    }  //"
                                    text.add(tmpStringB.toString());
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 26:  //currentArc.sortedMark
                            if (currentArc != null) {
                                if (((tmpSBuffer = currentArc.getMark()) != null) && (!tmpSBuffer.toString().equals("null"))) {
                                    if (tmpSBuffer.length() > 0) {
                                        tmpSBuffer.delete(0, tmpSBuffer.length());
                                    }
                                    char[] tmpSA = tmpStringB.toString().toCharArray();
                                    for (i = 0; i < tmpSA.length; i++) {
                                        c = tmpSA[i];
                                        for (j = i + 1; j < tmpSA.length; j++) {
                                            if (c > tmpSA[j]) {
                                                tmpSA[i] = tmpSA[j];
                                                tmpSA[j] = c;
                                                c = tmpSA[i];
                                            }
                                        }
                                    }
                                    if (tmpStringB.length() > 0) {
                                        tmpStringB.delete(0, tmpStringB.length());
                                    }
                                    tmpSBuffer = new StringBuilder(new String(tmpSA));
                                    for (i = 0; i < tmpSBuffer.length(); i++) {
                                        c = tmpSBuffer.charAt(i);
                                        tmpStringB.append(charToStr(c));
                                        if (sMode == 1) {
                                            if (targetL == 5) {
                                                tmpStringB.append(charToStr(c) + "'");
                                            } else if (targetL == 6) {
                                                tmpStringB.append(charToStr(c) + "+\"");
                                            }
                                            sMode = 0;
                                        }
                                    }  //"
                                    text.add(tmpStringB.toString());
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 27:  //currentArc.lengthOfMark
                            if (currentArc != null) {
                                if (((tmpSBuffer = currentArc.getMark()) != null) && (!tmpSBuffer.toString().equals("null"))) {
                                    text.add("".valueOf(tmpStringB.length()));
                                } else {
                                    text.add("0");
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 28:  //finalState.groupName
                            if (currentFAutomat != null) {
                                if ((finalState < 0) || (finalState > currentFAutomat.getGrpCount())) {
                                    return null;
                                }
                                if ((tmpString = currentFAutomat.getGrpName(finalState)) != null) {
                                    text.add(tmpString);
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 29:  //finalState.action
                            if (currentFAutomat != null) {
                                if ((tmpString = currentFAutomat.getAction(finalState)) != null) {
                                    text.add(tmpString);
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 30:  //grammar.startNonTerminal.name
                            if (gram != null) {
                                text.add(gram.getStartNT().getName().replaceAll("!", "___"));
                            } else {
                                return null;
                            }
                            break;
                        case 31:  //grammar.startNonTerminal.index
                            if (gram != null) {
                                iTmp = 0;
                                tmpSymbol = gram.getStartNT();
                                tmpString = tmpSymbol.getName();
                                j = tmpSymbol.getType();
                                for (i = 0; i < symbolSet.getSize(); i++) {
                                    if (((Symbol) symbolSet.getSymbol(i)).similar(tmpString, j) == 0) {
                                        iTmp = i;
                                        break;
                                    }
                                }
                                text.add("".valueOf(-iTmp - 1));
                            } else {
                                return null;
                            }
                            break;
                        case 32:  //grammar.countOfTerminals
                            text.add("".valueOf(cntT));
                            break;
                        case 33:  //grammar.countOfNonTerminals
                            text.add("".valueOf(cntN));
                            break;
                        case 34:  //grammar.rules.count
                            if (gram != null) {
                                text.add("".valueOf(gram.getRulesCount()));
                            } else {
                                return null;
                            }
                            break;
                        case 35:  //grammar.rules.sumLength
                            iTmp = 0;
                            if (gram != null) {
                                for (i = 0; i < gram.getRulesCount(); i += 1) {
                                    iTmp += gram.getRule(i).getLength() + 1;
                                }
                                text.add("".valueOf(iTmp));
                            } else {
                                return null;
                            }
                            break;
                        case 36:  //grammar.LRTable.states
                            if (lrT != null) {
                                text.add("".valueOf(lrT.length));
                            } else {
                                return null;
                            }
                            break;
                        case 37:  //grammar.LRTable.columns
                            if (lrT != null) {
                                text.add("".valueOf(lrT[0].length));
                            } else {
                                return null;
                            }
                            break;
                        case 38:  //currentNonTerminal.name
                            if (currentNonTerminal != null) {
                                if (currentNonTerminal.getType() == -1) {
                                    text.add("EOF");
                                } else {
                                    text.add(currentNonTerminal.getName().replaceAll("!", "___"));
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 39:  //currentNonTerminal.index
                            if ((symbolSet != null) && (currentNonTerminal != null)) {
                                if (currentNonTerminal.getType() == -1) {
                                    text.add("0");
                                } else {
                                    j = 0;
                                    tmpString = currentNonTerminal.getName();
                                    iTmp = currentNonTerminal.getType();
                                    for (i = 0; i < symbolSet.getSize(); i++) {
                                        if (((Symbol) symbolSet.getSymbol(i)).similar(tmpString, iTmp) == 0) {
                                            j = i;
                                            break;
                                        }
                                    }
                                    text.add("".valueOf(-j - 1));
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 40:  //currentNonTerminal.pIndex
                            if ((symbolSet != null) && (currentNonTerminal != null)) {
                                if (currentNonTerminal.getType() == -1) {
                                    text.add("0");
                                } else {
                                    j = 0;
                                    tmpString = currentNonTerminal.getName();
                                    iTmp = currentNonTerminal.getType();
                                    for (i = 0; i < symbolSet.getSize(); i++) {
                                        if (((Symbol) symbolSet.getSymbol(i)).similar(tmpString, iTmp) == 0) {
                                            j = i;
                                            break;
                                        }
                                    }
                                    text.add("".valueOf(j + 1));
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 41:  //currentSymbol.firstRule.state
                            if (gram != null) {
                                tmpSymbol = (currentSymbol == null ? currentSymbolFromRightPart.getSymbol() : currentSymbol);
                                if (tmpSymbol != null) {
                                    tmpString = tmpSymbol.getName();
                                    iTmp = tmpSymbol.getType();
                                    for (i = 0; ; i++) {
                                        if (gram.getRule(i) == null) {
                                            break;
                                        }
                                        if (gram.getRule(i).getLeftPart().similar(tmpString, iTmp) == 0) {
                                            text.add("".valueOf(i));
                                            break;
                                        }
                                    }
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 42:  //currentSymbol.name
                            if ((tmpSymbol = currentSymbol) == null) {
                                tmpSymbol = (currentSymbolFromRightPart == null ? null : currentSymbolFromRightPart.getSymbol());
                            }
                            if (tmpSymbol != null) {
                                if (tmpSymbol.getType() == 0) {
                                    text.add(tmpSymbol.getName().replaceAll("!", "___"));
                                } else if (tmpSymbol.getType() == -1) {
                                    text.add("EOF");
                                } else {
                                    text.add(tmpSymbol.getName().replaceAll("!", "___"));
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 43:  //currentSymbol.index
                            tmpSymbol = (currentSymbol == null ? currentSymbolFromRightPart.getSymbol() : currentSymbol);
                            if ((symbolSet != null) && (tmpSymbol != null)) {
                                iTmp = 0;
                                tmpString = tmpSymbol.getName();
                                j = tmpSymbol.getType();
                                for (i = 0; i < symbolSet.getSize(); i++) {
                                    if (((Symbol) symbolSet.getSymbol(i)).similar(tmpString, j) == 0) {
                                        iTmp = i;
                                        break;
                                    }
                                }
                                text.add("".valueOf(iTmp - cntN));
                            } else {
                                return null;
                            }
                            break;
                        case 44:  //currentSymbol.indexAsStartNonTerminal
                            tmpSymbol = (currentSymbol == null ? rightPartOFESymbol : currentSymbol);
                            if ((symbolSet != null) && (tmpSymbol != null)) {
                                tmpString = tmpSymbol.getName();
                                for (i = 0; i < startNT.length; i++) {
                                    if (tmpString.compareTo(startNT[i]) == 0) {
                                        text.add("".valueOf(-i - cntN + 1));
                                        break;
                                    }
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 45:  //currentStartNonTerminal.index
                            if (gram != null) {
                                text.add("".valueOf(-indexOfGrammar - cntN + 1));
                            } else {
                                return null;
                            }
                            break;
                        case 46:  //currentStartNonTerminal.name
                            if (gram != null) {
                                text.add(startNT[indexOfGrammar]);
                            } else {
                                return null;
                            }
                            break;
                        case 47:  //currentWord.text
                            if (currentWord != null) {
                                if (currentWord.getType() == -3) {
                                    String cw = currentWord.getName().trim().replaceAll("&lt;", "<").replaceAll("&gt;", ">");
                                    if (cw.charAt(0) == '"') {
                                        cw = cw.substring(1);
                                    }
                                    if ((cw.length() > 0) && (cw.charAt(cw.length() - 1)) == '"') {
                                        cw = cw.substring(0, cw.length() - 1);
                                    }
                                    if (cw.length() > 0) {
                                        text.add(cw);
                                    }
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 48:  //currentAction.text
                            if (currentAction != null) {
                                if (currentAction.getType() == 0) {
                                    text.add(currentAction.getName());
                                }
                            }
                            break;
                        case 49:  //currentAction.index
                            if ((gram != null) && (currentAction != null) && (currentAction.getType() == 0)) {
                                tmpString = currentAction.getName();
                                j = currentAction.getType();
                                for (i = 0; i < symbolSet.getSize(); i++) {
                                    if (((Symbol) symbolSet.getSymbol(i)).similar(tmpString, j) == 0) {
                                        text.add("".valueOf(indexOfAction - cntN));
                                        break;
                                    }
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 50:  //symbolSet.firstWord.index
                            if (gram != null) {
                                for (i = 0; i < symbolSet.getSize(); i++) {
                                    if (symbolSet.getSymbol(i).getType() == -3) {
                                        text.add("".valueOf(i - cntN));
                                        break;
                                    }
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 51:  //currentRule.index
                            if (gram != null) {
                                text.add("".valueOf(indexOfRule == -1 ? ruleIndex : indexOfRule));
                            } else {
                                return null;
                            }
                            break;
                        case 52:  //currentRule.length
                            if ((gram != null) && ((currentARule != null) || (currentRule != null))) {
                                text.add("".valueOf(currentARule == null ? (currentRule == null ? 1 : currentRule.getLengthWOActions() + 1) : currentARule.getLength() + 1));
                            } else {
                                return null;
                            }
                            break;
                        case 53:  //currentRule.lengthNoActions
                            if ((gram != null) && ((currentARule != null) || (currentRule != null))) {
                                text.add("".valueOf((currentARule == null ? (currentRule == null ? 1 : currentRule.getLengthWOActions() + 1) : currentARule.getLengthWOActions() + 1)));
                            } else {
                                return null;
                            }
                            break;
                        case 54:  //currentRule.firstSymbol
                            if ((gram != null) && (currentRule != null)) {
                                if ((tmpSymbol = currentRule.getSymbol(0)) != null) {
                                    text.add(tmpSymbol.getName());
                                } else {
                                    text.add("");
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 55:  //currentRule.firstSymbol.stateIndex
                            if ((gram != null) && (currentRule != null)) {
                                if ((currentRule.getRSymbol(0)) != null) {
                                    text.add("".valueOf(currentRule.getRSymbol(0).getStateIndex()));
                                } else {
                                    return null;
                                }
                            }
                            break;
                        case 56:  //currentSymbol.action
                            tmpSymbol = (currentSymbolFromRightPart == null ? currentSymbol : currentSymbolFromRightPart.getSymbol());
                            if ((tmpSymbol != null) && (tmpSymbol.getType() == 0)) {
                                text.add(tmpSymbol.getName());
                            }
                            break;
                        case 57:  //symbolFromSS.index
                            if ((gram != null) && (indexOfSymbolFromSS >= 0)) {
                                text.add("".valueOf(indexOfSymbolFromSS - cntN));
                            } else {
                                return null;
                            }
                            break;
                        case 58:  //symbolFromSS.name
                            if ((symbolSet != null) && (symbolSet.getSymbol(indexOfSymbolFromSS) != null)) {
                                text.add("".valueOf((tmpSymbol = symbolSet.getSymbol(indexOfSymbolFromSS)).getType() == -1 ? "EOF" : tmpSymbol.getName()));
                            } else {
                                return null;
                            }
                            break;
                        case 59:  //symbolFromFS.index
                            if ((startNT_FollowSet != null) && (indexOfSymbolFromFollowSet < startNT_FollowSet.size())) {
                                text.add("".valueOf(((Symbol) (startNT_FollowSet.get(indexOfSymbolFromFollowSet))).getId() - cntN));
                            } else {
                                return null;
                            }
                            break;
                        case 60:  //symbolFromFS.name
                            if ((startNT_FollowSet != null) && (indexOfSymbolFromFollowSet < startNT_FollowSet.size())) {
                                text.add(((Symbol) (startNT_FollowSet.get(indexOfSymbolFromFollowSet))).getName());
                            } else {
                                return null;
                            }
                            break;
                        case 61:  //rightPartOFESymbol.index
                            if ((gram != null) && (rightPartOFESymbol != null)) {
                                tmpString = rightPartOFESymbol.getName();
                                j = rightPartOFESymbol.getType();
                                for (i = 0; i < symbolSet.getSize(); i++) {
                                    if (((Symbol) symbolSet.getSymbol(i)).similar(tmpString, j) == 0) {
                                        text.add("".valueOf(i >= cntN ? (i - cntN) : (-i - 1)));
                                        break;
                                    }
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 62:  //rightPartOFESymbol.name
                            if ((gram != null) && (rightPartOFESymbol != null)) {
                                text.add(rightPartOFESymbol.getType() == -1 ? "EOF" : rightPartOFESymbol.getName());
                            } else {
                                return null;
                            }
                            break;
                        case 63:  //currentSymbol.type
                            if (currentSymbol != null) {
                                text.add("".valueOf(currentSymbol.getType()));
                            } else {
                                return null;
                            }
                            break;
                        case 64:  //currentSymbol.State.index
                            if (currentSymbolFromRightPart != null) {
                                text.add("".valueOf(currentSymbolFromRightPart.getStateIndex()));
                            } else {
                                return null;
                            }
                            break;
                        case 65:  //currentSymbol.nextState.index
                            if (currentSymbolFromRightPart != null) {
                                text.add("".valueOf(currentSymbolFromRightPart.getStateIndex() + 1));
                            } else {
                                return null;
                            }
                            break;
                        case 66:  //currentLRState.index
                            if (lrT != null) {
                                text.add("".valueOf(indexOfState));
                            } else {
                                return null;
                            }
                            break;
                        case 67:  //currentLRColumn.index
                            if (lrT != null) {
                                text.add("".valueOf(indexOfColumn < cntN ? (-indexOfColumn - 1) : indexOfColumn - cntN));
                            } else {
                                return null;
                            }
                            break;
                        case 68:  //currentLRAction.index
                            if ((actions != null) && (actions.size() > 0) && (indexOfAction >= 0) && (indexOfAction <= actions.size())) {
                                text.add("".valueOf(indexOfAction));
                            }
                            break;
                        case 69:  //currentLRAction.text
                            if ((actions != null) && (actions.size() > 0) && (indexOfAction >= 0) && (indexOfAction <= actions.size())) {
                                text.add(actions.get(indexOfAction - 1));
                            }
                            break;
                        case 70:  //currentRule.leftPart.index
                            if (gram != null) {
                                i = gram.getRule(ruleIndex).getLeftPart().getId();
                                text.add("".valueOf(i < cntN ? (-i - 1) : i - cntN));
                            } else {
                                return null;
                            }
                            break;
                        case 71:  //currentRule.leftPart.name
                            if (gram != null) {
                                text.add(gram.getRule(ruleIndex) == null ? "" : gram.getRule(ruleIndex).getLeftPart() == null ? "" : gram.getRule(ruleIndex).getLeftPart().getName());
                            } else {
                                return null;
                            }
                            break;
                        case 72:  //grammarSymbol.index
                            if (currentGrammarSymbol != null) {
                                i = currentGrammarSymbol.getId();
                                text.add("".valueOf(i < cntN ? (-i - 1) : i - cntN));
                            } else {
                                return null;
                            }
                            break;
                        case 73:  //grammarSymbol.name
                            if (currentGrammarSymbol != null) {
                                text.add(currentGrammarSymbol.getName());
                            } else {
                                return null;
                            }
                            break;
                        case 74:  //currentLRState.operation
                            varA = 0;
                            varS = 0;
                            varR = 0;
                            if ((tmpString = lrT[indexOfState][indexOfColumn]) != null) {
                                while (tmpString.length() > 0) {
                                    i = tmpString.indexOf(" ");
                                    if (i < 0) {
                                        i = tmpString.length();
                                    }
                                    switch (tmpString.charAt(0)) {
                                        case 'A':
                                            varA = (Integer.parseInt(tmpString.substring(1, i)) + 1) << 20;
                                            break;
                                        case 'S':
                                            if (tmpString.substring(1, i).compareTo("-1") == 0) {
                                                varS = -1;
                                            } else {
                                                varS = 0xc0000000 | ((Integer.parseInt(tmpString.substring(1, i))) & 0xfffff);
                                            }
                                            break;
                                        case 'G':
                                            varS = 0x80000000 | ((Integer.parseInt(tmpString.substring(1, i))) & 0xfffff);
                                            break;
                                        case 'R':
                                            j = tmpString.indexOf(",");
                                            varR = 0x40000000 | (Integer.parseInt(tmpString.substring(1, j)) << 10) | Integer.parseInt(tmpString.substring(j + 1, i));
                                            break;
                                    }
                                    if (i >= tmpString.length()) {
                                        break;
                                    } else {
                                        tmpString = tmpString.substring(i + 1);
                                    }
                                }
                                if (varS != 0) {
                                    varR = 0;
                                }
                            }
                            text.add("".valueOf(varA | varS | varR));
                            break;
                        case 75:  //currentLRState.LROperation
                            if ((tmpStr = lrT[indexOfState][indexOfColumn]) != null) {
                                tmpString = "";
                                while (tmpStr.length() > 0) {
                                    i = tmpStr.indexOf(" ");
                                    if (i < 0) {
                                        i = tmpStr.length();
                                    }
                                    switch (tmpStr.charAt(0)) {
                                        case 'A':
                                            tmpString += actions.get(Integer.parseInt(tmpStr.substring(1, i))) + (targetL == 6 ? "\n     " : "");
                                            break;
                                        case 'G':
                                            if (targetL == 5) {
                                                tmpString += "currentState:=" + tmpStr.substring(1, i) + ";stk.Push(currentState);columnIndex:=curWordIndex;";
                                            } else if (targetL == 7) {
                                                tmpString += "$currentState=" + tmpStr.substring(1, i) + ";$this->stackPush($currentState);$columnIndex=$this->curWordIndex;";
                                            } else if (targetL == 6) {
                                                tmpString += "currentState=" + tmpStr.substring(1, i) + ":stackPush(currentState):columnIndex=curWordIndex";
                                            } else {
                                                tmpString += "currentState=" + tmpStr.substring(1, i) + ";stackPush(currentState);columnIndex=curWordIndex;";
                                            }
                                            break;
                                        case 'S':
                                            if (targetL == 5) {
                                                if (Integer.parseInt(tmpStr.substring(1, i)) == -1) {
                                                    tmpString += "currentState:=-1;";
                                                } else {
                                                    tmpString += "currentState:=" + tmpStr.substring(1, i) + ";stk.Push(currentState);curWordIndex:=getWordIndex();columnIndex:=curWordIndex;";
                                                }
                                            } else if (targetL == 7) {
                                                if (Integer.parseInt(tmpStr.substring(1, i)) == -1) {
                                                    tmpString += "$currentState=-1;";
                                                } else {
                                                    tmpString += "$currentState=" + tmpStr.substring(1, i) + ";$this->stackPush($currentState);$this->curWordIndex=$this->getWordIndex();$columnIndex=$this->curWordIndex;";
                                                }
                                            } else if (targetL == 6) {
                                                if (Integer.parseInt(tmpStr.substring(1, i)) == -1) {
                                                    tmpString += "currentState=-1";
                                                } else {
                                                    tmpString += "currentState=" + tmpStr.substring(1, i) + ":stackPush(currentState):curWordIndex=getWordIndex():columnIndex=curWordIndex";
                                                }
                                            } else {
                                                if (Integer.parseInt(tmpStr.substring(1, i)) == -1) {
                                                    tmpString += "currentState=-1;";
                                                } else {
                                                    tmpString += "currentState=" + tmpStr.substring(1, i) + ";stackPush(currentState);curWordIndex=getWordIndex();columnIndex=curWordIndex;";
                                                }
                                            }
                                            break;
                                        case 'R':
                                            j = tmpStr.indexOf(",");
                                            if (j > 0) {
                                                if (targetL == 5) {
                                                    if (Integer.parseInt(tmpStr.substring(1, j)) > 0) {
                                                        tmpString += "if stackPop(" + tmpStr.substring(1, j) + ") then begin Result:=ERROR_VALUE;exit; end;";
                                                    }
                                                    k = -(Integer.parseInt(tmpStr.substring(j + 1, i)) + 1);
                                                    tmpString += "columnIndex:=" + k + ";currentState:=stk.Peek();";
                                                } else if (targetL == 7) {
                                                    if (Integer.parseInt(tmpStr.substring(1, j)) > 0) {
                                                        tmpString += "if($this->stackPop(" + tmpStr.substring(1, j) + "))return ERROR_VALUE;";
                                                    }
                                                    k = -(Integer.parseInt(tmpStr.substring(j + 1, i)) + 1);
                                                    tmpString += "$columnIndex=" + k + ";$currentState=$this->stateFromStack();";
                                                } else if (targetL == 6) {
                                                    if (Integer.parseInt(tmpStr.substring(1, j)) > 0) {
                                                        tmpString += "if stackPop(" + tmpStr.substring(1, j) + ") then parse=ERROR_VALUE:exit function\n     ";
                                                    }
                                                    k = -(Integer.parseInt(tmpStr.substring(j + 1, i)) + 1);
                                                    tmpString += "columnIndex=" + k + ":currentState=stateFromStack()";
                                                } else {
                                                    if (Integer.parseInt(tmpStr.substring(1, j)) > 0) {
                                                        tmpString += "if(stackPop(" + tmpStr.substring(1, j) + "))return ERROR_VALUE;";
                                                    }
                                                    k = -(Integer.parseInt(tmpStr.substring(j + 1, i)) + 1);
                                                    tmpString += "columnIndex=" + k + ";currentState=stateFromStack();";
                                                }
                                            } else if (targetL == 5) {
                                                tmpString += "{!!! " + tmpStr + "}";
                                            } else if (targetL == 6)
                                                ;
                                            else {
                                                tmpString += "/ *!!! " + tmpStr + "* /";
                                            }
                                            break;
                                    }
                                    if (i >= tmpStr.length()) {
                                        break;
                                    } else {
                                        tmpStr = tmpStr.substring(i + 1);
                                    }
                                }
                                tmpString = (tmpString + ((lrT[indexOfState][indexOfColumn] == null) ? "" : (targetL == 6 ? " '" : " //") + lrT[indexOfState][indexOfColumn]));
                                text.add(tmpString);
                            }
                            break;
                        case 76:  //LRAutomat.fullTable
                            if (numbers == null) {
                                buildCompact();
                            }
                            for (int iii = 0; iii < numbers.size(); iii++) {
                                text.add(numbers.get(iii).toString() + ((iii == (numbers.size()) - 1) ? "" : ","));
                                if ((iii % 32) == 0) {
                                    text.add("\n ");
                                }
                            }
                            break;
                        case 77:  //LRAutomat.fullTable
                            if (zeros == null) {
                                buildCompact();
                            }
                            for (int iii = 0; iii < zeros.size(); iii++) {
                                text.add(zeros.get(iii).toString() + ((iii == (zeros.size() - 1)) ? "" : ","));
                                if ((iii % 32) == 0) {
                                    text.add("\n ");
                                }
                            }
                            break;
                        case 78:  //LRAutomat.fullTable.states
                            text.add("".valueOf(lrT.length));
                            break;
                        case 79:  //symbol.name
                            if (gram != null) {
                                if (indexOfTerminal >= 0) {
//       for(;indexOfTerminal<symbolSet.getSize(); indexOfTerminal++){
//        if(symbolSet.getSymbol(indexOfTerminal).getType()<-1){
                                    text.add(symbolSet.getSymbol(indexOfTerminal).getName());
//         break;
//        }
//       }
                                }
                            }
                            break;
                        case 80:  //symbol.index
                            if (gram != null) {
                                if (indexOfTerminal >= 0) {
//       for(;indexOfTerminal<symbolSet.getSize(); indexOfTerminal++){
//        if(symbolSet.getSymbol(indexOfTerminal).getType()<-1){
                                    text.add("" + (symbolSet.getSymbol(indexOfTerminal).getId() - cntN));
//         break;
//        }
//       }
                                }
                            }
                            break;
                        case 81:
                            text.add(user);
                            break;
                        default:
                            return null;
                    }
                    pInd += 1;
                    break;

                case 3:
                    condition = false;
                    switch (item.code) {
                        case 0:  //finalState.action.exists
                            if ((finalState >= 0) && (currentFAutomat.getAction(finalState) != null)) {
                                condition = true;
                            }
                            break;
                        case 1:  //arrayOfAutomat.hasManyItems
//                    if(fa.length>1)
//                        condition=true;
                            break;
                        case 2:  //arrayOfAutomat.hasOneItem
//                    if(fa.length==1)
//                        condition=true;
                            break;
                        case 3:  //currentAutomat.hasNextAutomat
//                    if(currentAutomat<fa.length - 1)
//                        condition=true;
                            break;
                        case 4:  //currentAutomat.hasRangesOfChars
                            if (currentTransliterator.getRangesCount() > 0) {
                                condition = true;
                            }
                            break;
                        case 5:  //currentState.hasNextState
                            if (currentState < currentFAutomat.getWorkNodesCount()) {
                                condition = true;
                            }
                            break;
                        case 6:  //currentState.hasNextOptState
                            if (currentState < currentTransliterator.getRows() - 1) {
                                condition = true;
                            }
                            break;
                        case 7:  //currentArc.hasNextArc
                            if ((currentArc.getNext()) != null) {
                                condition = true;
                            }
                            break;
                        case 8:  //finalState.wordNameUseInGrammar
                            if ((gram != null) && (finalState >= 0) && (finalState <= currentFAutomat.getGrpCount() + 2)) {
                                tmpString = currentFAutomat.getGrpName(finalState);
                                if (tmpString != null) {
                                    for (i = 0; i < symbolSet.getSize() - 1; i++) {
                                        if ((symbolSet.getSymbol(i).getType() == -2) && (tmpString.compareTo(symbolSet.getSymbol(i).getName()) == 0)) {
                                            condition = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            break;
                        case 9:  //currentCell.hasNextCell
                            if (currentColumn < currentTransliterator.getColumnsCount() - 1) {
                                condition = true;
                            }
                            break;
                        case 10:  //grammar.exists
                            if (gram != null) {
                                condition = true;
                                if ((lrT == null) || (lrT.length == 0)) {
//                    otn=gram.calcProperties();
                                    lrT = gram.getLRTable();
                                    actions = gram.getActions();
//                    topOfStack.index=1;
//                    indexOfGrammar=topOfStack.index;
                                }
                            }
                            break;
                        case 11:  //grammar.isNotMainGrammar
                            if ((gram != null) && (indexOfGrammar > 1)) {
                                condition = true;
                            }
                            break;
                        case 12:  //grammar.pertainToLL1
                            if (gram != null) {
                                condition = gram.testLL1();
                            }
                            break;
                        case 13:  //grammar.symbolSet.hasWords
                            if (gram != null) {
                                for (i = 0; i < symbolSet.getSize(); i++) {
                                    if (symbolSet.getSymbol(i).getType() == -3) {
                                        condition = true;
                                        break;
                                    }
                                }
                            }
                            break;
                        case 14:  //symbolFromSS.isNotLast
                            if ((gram != null) && (currentSelSet != null) && (indexOfSymbolFromSS >= 0)) {
                                for (i = indexOfSymbolFromSS + 1; i <= symbolSet.getSize(); i++) {
                                    tmpSymbol = symbolSet.getSymbol(i);
                                    if (tmpSymbol != null) {
                                        tmpString = tmpSymbol.getName();
                                        j = tmpSymbol.getType();
                                        for (k = 0; k < currentSelSet.size(); k++) {
                                            tmpSymbol = (Symbol) currentSelSet.get(k);
                                        }
                                        if ((tmpSymbol != null) && (tmpSymbol.similar(tmpString, j) == 0)) {
                                            condition = true;
                                            break;
                                        }
                                        if (condition) {
                                            break;
                                        }
                                    }
                                }
                            }
                            break;
                        case 15:  //symbolFromSS.isEOF
                            if (symbolSet.getSymbol(indexOfSymbolFromSS).getType() == -1) {
                                condition = true;
                            }
                            break;
                        case 16:  //grammar.hasActions
                            if (gram != null) {
                                for (int indAct = 0; indAct <= symbolSet.getSize(); indAct++) {
                                    if ((currentAction = symbolSet.getSymbol(indAct)) == null) {
                                        break;
                                    }
                                    if ((currentAction.getType() == 0) && (currentAction.getName() != null) && (currentAction.getName().trim().length() != 0)) {
                                        condition = true;
                                        break;
                                    }
                                }
                            }
                            break;
                        case 17:  //currentRule.lastSymbolNotIsNonTerminal
                            if ((gram != null) && (currentRule != null) && (((i = currentRule.getLength()) > 0) && (((tmpSymbol = currentRule.getSymbol(i - 1)) != null) && (tmpSymbol.getType() <= 0)) || (i == 0))) {
                                condition = true;
                            }
                            break;
                        case 18:  //currentRule.lastSymbolIsNonTerminal
                            if ((gram != null) && (currentRule != null) && ((i = currentRule.getLength()) > 0) && (currentRule.getSymbol(i - 1).getType() > 0)) {
                                condition = true;
                            }
                            break;
                        case 19:  //currentSymbol.isTerminal
                            if (gram != null) {
                                if ((tmpSymbol = currentSymbol) == null) {
                                    tmpSymbol = (currentSymbolFromRightPart == null ? null : currentSymbolFromRightPart.getSymbol());
                                }
                            }
                            if ((tmpSymbol != null) && (tmpSymbol.getType() < 0) && (tmpSymbol.getType() > -4)) {
                                condition = true;
                            }
                            break;
                        case 20:  //currentSymbol.isNonTerminal
                            if (gram != null) {
                                if ((tmpSymbol = currentSymbol) == null) {
                                    tmpSymbol = (currentSymbolFromRightPart == null ? null : currentSymbolFromRightPart.getSymbol());
                                }
                                if ((tmpSymbol != null) && (tmpSymbol.getType() > 0)) {
                                    condition = true;
                                }
                            }
                            break;
                        case 21:  //currentSymbol.notIsNonTerminal
                            if (gram != null) {
                                if ((tmpSymbol = currentSymbol) == null) {
                                    tmpSymbol = (currentSymbolFromRightPart == null ? null : currentSymbolFromRightPart.getSymbol());
                                }
                            }
                            if ((tmpSymbol != null) && (tmpSymbol.getType() <= 0)) {
                                condition = true;
                            }
                            break;
                        case 22:  //currentSymbol.isStartNonTerminalForOtherGrammar
                            if ((gram != null) && (rightPartOFESymbol != null)) {
                                tmpString = rightPartOFESymbol.getName();
                                if ((tmpString != null) && (tmpString.compareTo(gram.getStartNT().getName()) != 0)) {
                                    for (i = 2; i < startNT.length; i++) {
                                        if (startNT[i].compareTo(tmpString) == 0) {
                                            condition = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            break;
                        case 23:  //currentSymbol.isAction
                            if (gram != null) {
                                if ((tmpSymbol = currentSymbol) == null) {
                                    tmpSymbol = (currentSymbolFromRightPart == null ? null : currentSymbolFromRightPart.getSymbol());
                                }
                            }
                            if ((tmpSymbol != null) && (tmpSymbol.getType() == 0)) {
                                condition = true;
                            }
                            break;
                        case 24:  //currentRule.rightPart.isNoEmpty
                            if ((gram != null) && (currentRule != null) && (((i = currentRule.getLength()) > 1) || (((i == 1) && (((tmpSymbol = currentRule.getSymbol(0)).getType() != 0) || (tmpSymbol.getName().length() > 0)))))) {
                                condition = true;
                            }
                            break;
                        case 25:  //currentRule.rightPart.hasSymbols
                            if ((gram != null) && (currentRule != null) && ((i = currentRule.getLength()) > 0)) {
                                for (i -= 1; i >= 0; i -= 1) {
                                    if (((tmpSymbol = currentRule.getSymbol(i)) != null) && (tmpSymbol.getType() > -4)) {
                                        if (tmpSymbol.getType() != 0) {
                                            condition = true;
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                            break;
                        case 26:  //currentRule.hasNextRule
                            if ((gram != null) && (indexOfRule >= 0)) {
                                if (((tmpRule = gram.getRule(indexOfRule + 1)) != null) && (tmpRule.getLeftPart() == currentNonTerminal)) {
                                    condition = true;
                                }
                            }
                            break;
                        case 27:  //currentSymbol.firstInRule
                            if (gram != null) {
                                condition = (indexOfSymbolFromRightPart == 0);
                            }
                            break;
                        case 28:  //currentSymbol.notFirstInRule
                            condition = (indexOfSymbolFromRightPart != 0);
                            break;
                        case 29:  //currentSymbol.lastInRule
                            if ((gram != null) && (currentRule != null)) {
                                condition = (indexOfSymbolFromRightPart == (currentRule.getLength() - 1));
                            }
                            break;
                        case 30:  //currentSymbol.notLastInRule
                            if ((gram != null) && (currentRule != null)) {
                                condition = (indexOfSymbolFromRightPart != (currentRule.getLength() - 1));
                            }
                            break;
                        case 31:  //currentLRState.isNotLast
                            if ((lrT != null) && (indexOfState < lrT.length - 1)) {
                                condition = true;
                            }
                            break;
                        case 32:  //currentLRState.hasAction
                            if (((tmpString = lrT[indexOfState][indexOfColumn]) != null) && (tmpString.indexOf("A") >= 0)) {
                                condition = true;
                            }
                            break;
                        case 33:  //currentLRColumn.isNotLast
                            if ((lrT != null) && (lrT.length > 0) && (indexOfColumn < lrT[0].length - 1)) {
                                condition = true;
                            }
                            break;
                        case 34:  //currentWord.hasNextWord
                            if (gram != null) {
                                for (i = indexOfWord + 1; i < symbolSet.getSize(); i++) {
                                    if (symbolSet.getSymbol(i).getType() == -3) {
                                        condition = true;
                                        break;
                                    }
                                }
                            }
                            break;
                        default:
                            return null;
                    }
                    pInd += 1;
                    break;

                case 4:
                    if (stack.size() <= 0) {
                        return null;
                    }
                    condition = true;
                    topOfStack = stack.peek();
                    switch (topOfStack.code) {
                        case 0:  //currentAutomat in arrayOfAutomat
//                    if((topOfStack.index=(topOfStack.index==-2147483647 ? 0 : topOfStack.index+1))>=fa.length){
//                        condition=false;
//                        currentFAutomat=null;
//                        currentTransliterator=null;
//                    }else{
//                        currentAutomat=topOfStack.index;
//                        currentFAutomat=fa[topOfStack.index];
//                        currentTransliterator=ctI[topOfStack.index];
//                    }
                            break;
                        case 1:  //currentState in currentAutomat.states
                            if (currentFAutomat != null) {
                                if ((topOfStack.index = (currentState = (topOfStack.index == -2147483647) ? 0 : topOfStack.index + 1)) > currentFAutomat.getWorkNodesCount() - 1) {
                                    condition = false;
                                    currentState = -1;
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 2:  //currentState in currentAutomat.statesOpt
                            if (currentTransliterator != null) {
                                if ((topOfStack.index = (currentState = (topOfStack.index == -2147483647) ? 0 : topOfStack.index + 1)) >= currentTransliterator.getRows()) {
                                    condition = false;
                                    currentState = -1;
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 3:  //finalState in currentAutomat.finalStates
                            if (currentFAutomat != null) {
                                if ((topOfStack.index = (finalState = (topOfStack.index == -2147483647) ? 0 : topOfStack.index + 1)) >= currentFAutomat.getGrpCount()) {
//text.add("topOfStack.index="+topOfStack.index+" currentFAutomat.getGrpCount="+currentFAutomat.getGrpCount());
                                    condition = false;
                                    finalState = -1;
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 4:  //currentRange in currentAutomat.rangesOfChar
                            if (currentTransliterator != null) {
                                if ((topOfStack.index = (currentRange = (topOfStack.index == -2147483647) ? 0 : topOfStack.index + 1)) >= currentTransliterator.getRangesCount()) {
                                    condition = false;
                                    currentRange = -1;
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 5:  //currentArc in currentState.arcs
                            if (currentFAutomat != null) {
                                if (topOfStack.index == -2147483647) {
                                    if ((currentArc = currentFAutomat.getFirstFrom(currentState)) == null) {
                                        condition = false;
                                    }
                                    topOfStack.index = 0;
                                } else {
                                    try {
                                        currentArc = currentArc.getNext();
                                    } catch (Exception ex) {
                                        currentArc = null;
                                    }
                                    if (currentArc == null) {
                                        condition = false;
                                    }
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 6:  //currentCell in currentState.cells
                            if (currentTransliterator != null) {
                                if ((topOfStack.index = (currentColumn = (topOfStack.index == -2147483647) ? 0 : topOfStack.index + 1)) > currentTransliterator.getColumnsCount() - 1) {
                                    condition = false;
                                    currentColumn = -1;
                                }
                            } else {
                                return null;
                            }
                            break;
                        case 7:  //symbol in grammar.startNonTerminal.followSet
//                    if(topOfStack.index==-2147483647){
//                        condition=false;
//                        if((grammars!=null)&&(gram!=null)){
//                            if(startNT_FollowSet==null){
//                                tmpFollowSet=grammars[0].getFollowRel();
//                                tmpSymbol=gram.getStartNT();
//                                nameOfSymbol=tmpSymbol.getName();
//                                typeOfSymbol=tmpSymbol.getType();
//                                for (i=0; i<mainSymbolSet.getSize(); i++){
//                                    if(mainSymbolSet.getSymbol(i).similar(nameOfSymbol, typeOfSymbol)==0){
//                                        startNT_FollowSet=(ArrayList) tmpFollowSet.get(i);
//                                        condition=true;
//                                        break;
//                                    }
//                                }
//                            }
//                            if((condition)&&(startNT_FollowSet.size()>0))
//                                topOfStack.index=0;
//                        }
//                    }else{
//                        if(++topOfStack.index>=startNT_FollowSet.size())
//                            condition=false;
//                    }
//                    indexOfSymbolFromFollowSet=-1;
//                    if(condition)
//                        indexOfSymbolFromFollowSet=topOfStack.index;
                            break;
                        case 8:  //grammar in grammars
                            indexOfGrammar = -1;
                            if (topOfStack.index == -2147483647) {
//                        if(grammars!=null){
//                            gram=grammar;
//                            otn=gram.calcProperties();
                                lrT = gram.getLRTable();
                                actions = gram.getActions();
                                topOfStack.index = 1;
                                indexOfGrammar = topOfStack.index;
//                        }else{
//                            gram=null;
//                            condition=false;
//                        }
//                    }else if((grammars!=null)&&(++topOfStack.index<grammars.length)){
//                        indexOfGrammar=topOfStack.index;
//                        gram=grammars[topOfStack.index];
//                        otn=gram.calcProperties();
                            } else {
                                gram = null;
                                condition = false;
                            }
                            break;
                        case 9:  //currentNonTerminal in grammar.symbolSet
                            currentNonTerminal = null;
                            condition = false;
                            if (gram != null) {
                                if (topOfStack.index == -2147483647) {
                                    topOfStack.index = 0;
                                    currentNonTerminal = gram.getStartNT();
                                    condition = true;
                                } else {
                                    if ((tmpRule = gram.getRule(topOfStack.index)) != null) {
                                        currentNonTerminal = tmpRule.getLeftPart();
                                        tmpString = currentNonTerminal.getName();
                                        j = currentNonTerminal.getType();
                                        for (topOfStack.index += 1; ; topOfStack.index += 1) {
                                            if ((tmpRule = gram.getRule(topOfStack.index)) != null) {
                                                if ((currentNonTerminal = tmpRule.getLeftPart()).similar(tmpString, j) != 0) {
                                                    condition = true;
                                                    break;
                                                }
                                            } else {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 10:  //currentRule in currentNonTerminal.rules
                            condition = false;
                            currentRule = null;
                            indexOfRule = -1;
                            if ((gram != null) && (currentNonTerminal != null)) {
                                nameOfSymbol = currentNonTerminal.getName();
                                typeOfSymbol = currentNonTerminal.getType();
                                flag = false;
                                j = (topOfStack.index == -2147483647 ? 0 : topOfStack.index + 1);
                                while ((currentRule = gram.getRule(j)) != null) {
                                    tmpSymbol = currentRule.getLeftPart();
                                    if (tmpSymbol.similar(nameOfSymbol, typeOfSymbol) == 0) {
                                        flag = true;
                                        break;
                                    }
                                    j += 1;
                                }
                                if (flag) {
                                    topOfStack.index = j;
                                    indexOfRule = j;
                                    condition = true;
                                }
                            }
                            break;
                        case 11:  //currentSymbol in currentRule.rightPart
                            indexOfSymbolFromRightPart = -1;
                            if (currentRule != null) {
                                topOfStack.index = (topOfStack.index == -2147483647 ? 0 : topOfStack.index + 1);
                                if ((currentSymbolFromRightPart = currentRule.getRSymbol(topOfStack.index)) == null) {
                                    condition = false;
                                }
                            } else {
                                currentSymbolFromRightPart = null;
                                condition = false;
                            }
                            if (condition) {
                                indexOfSymbolFromRightPart = topOfStack.index;
                            }
                            break;
                        case 12:  //currentWord in grammar.symbolSet
                            condition = false;
                            if (gram != null) {
                                topOfStack.index = (topOfStack.index == -2147483647 ? 0 : topOfStack.index + 1);
                                for (indexOfWord = topOfStack.index; indexOfWord <= symbolSet.getSize(); indexOfWord++) {
                                    if ((currentWord = symbolSet.getSymbol(indexOfWord)) != null) {
                                        if ((currentWord.getType() == -3) && (currentWord.getName().length() > 0)) {
                                            topOfStack.index = indexOfWord;
                                            condition = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (condition == false) {
                                indexOfWord = -1;
                                currentWord = null;
                            }
                            break;
                        case 13:  //symbolFromSS in currentRule.selectionSet
                            condition = false;
                            indexOfSymbolFromSS = -1;
                            if ((gram != null) && (currentRule != null)) {
                                if ((currentSelSet = gram.getSelSet(currentRule)) != null) {
                                    topOfStack.index = (topOfStack.index == -2147483647 ? 0 : topOfStack.index + 1);
                                    for (; topOfStack.index <= symbolSet.getSize(); topOfStack.index++) {
                                        if ((tmpSymbol = symbolSet.getSymbol(topOfStack.index)) != null) {
                                            nameOfSymbol = tmpSymbol.getName();
                                            typeOfSymbol = tmpSymbol.getType();
                                            for (i = 0; i < currentSelSet.size(); i++) {
                                                symbolFromSS = (Symbol) currentSelSet.get(i);
                                                if ((symbolFromSS != null) && (symbolFromSS.similar(nameOfSymbol, typeOfSymbol) == 0)) {
                                                    indexOfSymbolFromSS = topOfStack.index;
                                                    condition = true;
                                                    break;
                                                }
                                            }
                                            if (condition) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case 14:  //currentLRState in grammar.LRStates
                            condition = false;
                            indexOfState = -1;
                            if (gram != null) {
                                topOfStack.index = (topOfStack.index == -2147483647 ? 0 : topOfStack.index + 1);
                                if (topOfStack.index < lrT.length) {
                                    indexOfState = topOfStack.index;
                                    condition = true;
                                }
                            }
                            break;
                        case 15:  //rightPartOFESymbol in currentRule.rightPart.orderFromEnd
                            condition = false;
                            if ((gram != null) && (currentRule != null)) {
                                topOfStack.index = (topOfStack.index == -2147483647 ? currentRule.getLength() - 1 : topOfStack.index - 1);
                                for (; topOfStack.index >= 0; topOfStack.index -= 1) {
                                    if ((rightPartOFESymbol = currentRule.getSymbol(topOfStack.index)).getType() > -4) {
                                        condition = true;
                                        break;
                                    }
                                }
                            }
                            if (!condition) {
                                rightPartOFESymbol = null;
                            }
                            break;
                        case 16:  //currentAction in grammar.symbolSet
                            condition = false;
                            if (gram != null) {
                                indexOfAction = (topOfStack.index = (topOfStack.index == -2147483647 ? 0 : topOfStack.index + 1));
                                for (; indexOfAction <= symbolSet.getSize(); indexOfAction++) {
                                    if ((currentAction = symbolSet.getSymbol(indexOfAction)) == null) {
                                        break;
                                    }
                                    if ((currentAction.getType() == 0) && (currentAction.getName() != null) && (currentAction.getName().length() != 0)) {
                                        condition = true;
                                        break;
                                    }
                                }
                                topOfStack.index = indexOfAction;
                            }
                            if (!condition) {
                                indexOfAction = -1;
                                currentAction = null;
                            }
                            break;
                        case 17:  //currentLRColumn in LRState.actualColumns
                            condition = false;
                            indexOfColumn = -1;
                            if (gram != null) {
                                indexOfColumn = (topOfStack.index = (topOfStack.index == -2147483647 ? 0 : topOfStack.index + 1));
                                for (; indexOfColumn < lrT[indexOfState].length; indexOfColumn += 1) {
                                    if (lrT[indexOfState][indexOfColumn] != null) {
                                        condition = true;
                                        break;
                                    }
                                }
                                topOfStack.index = indexOfColumn;
                            }
                            break;
                        case 18:  //currentRule in grammar.allRules
                            condition = false;
                            currentARule = null;
                            if (gram != null) {
                                ruleIndex = topOfStack.index = (topOfStack.index == -2147483647 ? 0 : topOfStack.index + 1);
                                if (ruleIndex < gram.getRulesCount()) {
                                    currentARule = gram.getRule(ruleIndex);
                                    condition = true;
                                }
                            }
                            break;
                        case 19:  //grammarSymbol in currentRule.rightPart
                            condition = false;
                            currentGrammarSymbol = null;
                            if ((gram != null) && (currentARule != null)) {
                                indexOfGrammarSymbol = topOfStack.index = (topOfStack.index == -2147483647 ? 0 : topOfStack.index + 1);
                                for (; indexOfGrammarSymbol < currentARule.getLength(); indexOfGrammarSymbol += 1) {
                                    currentGrammarSymbol = currentARule.getSymbol(indexOfGrammarSymbol);
                                    if (((i = currentGrammarSymbol.getType()) > -4) && (i != 0)) {
                                        condition = true;
                                        break;
                                    }
                                }
                                topOfStack.index = indexOfGrammarSymbol;
                            }
                            break;
                        case 20:  //currentLRColumn in LRState.allColumns
                            condition = false;
                            indexOfColumn = -1;
                            if (gram != null) {
                                indexOfColumn = topOfStack.index = (topOfStack.index == -2147483647 ? 0 : topOfStack.index + 1);
                                if (indexOfColumn < lrT[indexOfState].length) {
                                    condition = true;
                                    break;
                                }
                            }
                            break;

                        case 21:  //currentLRAction in grammar.LRStates.actions
                            if (gram != null) {
                                indexOfAction = topOfStack.index = (topOfStack.index == -2147483647 ? 0 : topOfStack.index + 1);
                                if (++indexOfAction >= actions.size() + 1) {
                                    condition = false;
                                }
                            }
                            break;
                        case 22:  //nonTerminal_grammars.startNonTerminal
//                    if(grammars!=null){
//                        indexOfGrammar=(topOfStack.index=(topOfStack.index==-2147483647 ? 2 : topOfStack.index+1));
//                        if(indexOfGrammar>=grammars.length){
//                            condition=false;
//                            indexOfGrammar=1;
//                       }
//                   }
                            break;
                        case 23:  //currentWord in grammar.symbolSet
                            condition = false;
                            if (gram != null) {
                                topOfStack.index = (topOfStack.index == -2147483647 ? cntN + 1 : topOfStack.index + 1);
                                indexOfTerminal = topOfStack.index;
                                for (; indexOfTerminal < symbolSet.getSize(); indexOfTerminal++) {
                                    if ((symbolSet.getSymbol(indexOfTerminal).getType() < -1) && (symbolSet.getSymbol(indexOfTerminal).getType() > -4)) {
                                        break;
                                    }
                                }
                                if (indexOfTerminal < symbolSet.getSize()) {
                                    condition = true;
                                }
                            }
                            if (condition == false) {
                                indexOfTerminal = -1;
                            }
                            break;
                        default:
                            return null;
//                    return " forEach("+item.value.replace("_", " in ")+").: "+pInd;
                    }
                    if (condition == false) {
                        stack.pop();
                        topOfStack = (stack.size() > 0) ? stack.peek() : null;
                    }
                    pInd += 1;
                    break;
                case 5:
                    if (++pInd < prg.size()) {
                        stack.push(new CycleItem(prg.get(pInd).code, -2147483647));
                    } else {
                        return null;
                    }
                    break;
                case 6:
                    if (((iTmp = item.ind) < 0) || (iTmp >= maxInd)) {
                        return null;
                    } else {
                        pInd = iTmp;
                    }
                    break;
                case 7:
                    if (condition) {
                        pInd += 1;
                    } else if (((iTmp = item.ind) < 0) || (iTmp >= maxInd)) {
                        return null;
                    } else {
                        pInd = iTmp;
                    }
                    break;

                default:
                    pInd += 1;
                    break;
            }
        }
        return text;
    }
}