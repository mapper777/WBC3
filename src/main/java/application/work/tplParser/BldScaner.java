package application.work.tplParser;

import lombok.Getter;

import java.util.Properties;
import java.util.Stack;

public class BldScaner {
    FAutomat lexAcceptor;
    Lexem Lexem = new Lexem();
    boolean ignoreLastWord;

    Stack lexStk = new Stack();
    StringBuffer beginOfWord = new StringBuffer();
    Properties tStrings;

    class FaMark {
        char[] mark;
        int hi = 0;

        public FaMark(String Mark) {
            int i, md, lo = 0;
            char chr;
            if (Mark == null) return;
            if ((hi = Mark.length()) == 0) return;
            mark = new char[hi--];
            mark[0] = Mark.charAt(0);
            for (md = 0, lo = 1; lo <= hi; lo++) {
                chr = Mark.charAt(lo);
                for (i = md++; i >= 0; --i) {
                    if (chr > mark[i]) break;
                    else if (chr == mark[i]) {
                        hi -= 1;
                        i -= 1;
                        break;
                    } else mark[i + 1] = mark[i];
                }
                mark[++i] = chr;
            }
        }

        public boolean charInMark(char Chr) {
            char chr;
            int l, h, md;
            if ((h = hi) < 0) return false;
            l = 0;
            while (h >= l) {
                md = (l + h) >> 1;
                chr = mark[md];
                if (chr == Chr) return true;
                else if (chr > Chr) h = md - 1;
                else l = md + 1;
            }
            return false;
        }
    }


    class FaState {
        FaMark[] markTo;
        int[] statesTo;
        int filled;

        public FaState(int toCount) {
            statesTo = new int[toCount];
            markTo = new FaMark[toCount];
            filled = 0;
        }

        public void setArc(int to, String mark) {
            if (filled < markTo.length) {
                statesTo[filled] = to;
                if ((mark == null) || (mark.length() == 0))
                    markTo[filled++] = null;
                else markTo[filled++] = new FaMark(mark);
            }
        }

        public int getState(char c) {
            int i;
            int stateTo = -2147483647;
            for (i = 0; i < filled; i++) {
                if (markTo[i] == null)
                    stateTo = statesTo[i];
                else if (markTo[i].charInMark(c))
                    return statesTo[i];
            }
            return stateTo;
        }

        public int getFinalState() {
            int i;
            for (i = 0; i < filled; i++) {
                if (statesTo[i] < 0)
                    return statesTo[i];
            }
            return -2147483647;
        }
    }

    class FAutomat {
        @Getter
        int index;
        FaState[] states;
        ReaderFromFile inp;
        boolean inputExists;
        String w;

        public FAutomat(int nodesCount, ReaderFromFile Inp) {
            index = 0;
            states = new FaState[nodesCount];
            inp = Inp;
            inputExists = true;
        }

        public FAutomat(int ind, int nodesCount, ReaderFromFile Inp) {
            index = ind;
            states = new FaState[nodesCount];
            inp = Inp;
            inputExists = true;
        }

        public void setState(int no, FaState st) {
            if ((no >= 0) && (no < states.length))
                states[no] = st;
        }

        public Lexem getLexem() {
            int curState = -1;
            int curChar = -1;
            w = "";
            if (inputExists)
                for (curState = 0; ; ) {
                    try {
                        curChar = inp.read();
                    } catch (Exception e) {
                        curChar = -1;
                    }
                    if (curChar == -1) {
                        inputExists = false;
                        curState = (curState == 0 ? -1 : states[curState].getFinalState());
                    } else {
                        curState = states[curState].getState((char) curChar);
                    }
                    if ((curState < 0) || (curState >= states.length) || (curChar < 0)) break;
                    w += (char) curChar;
                }
            if ((curState < -1) && (curState > -2147483647))
                inp.back(curChar);
            Lexem retLexem = new Lexem();
            retLexem.wordIndex = 0;
            retLexem.groupIndex = (curState >= 0 ? -2147483647 : curState);
            if (!w.isEmpty())
                retLexem.textOfWord = new StringBuffer(w);
            return retLexem;
        }
    }

    FAutomat[] lexAcceptors = new FAutomat[4];

    public BldScaner(ReaderFromFile rdr, Properties tStr) {
        tStrings = tStr;

        FaState state;

        lexAcceptors[0] = new FAutomat(0, 3, rdr);
        state = new FaState(2);
        state.setArc(1, "");
        state.setArc(2, "/");
        lexAcceptors[0].setState(0, state);

        state = new FaState(2);
        state.setArc(-2, "/");
        state.setArc(1, "");
        lexAcceptors[0].setState(1, state);

        state = new FaState(1);
        state.setArc(-3, "");
        lexAcceptors[0].setState(2, state);


        lexAcceptors[1] = new FAutomat(1, 4, rdr);
        state = new FaState(3);
        state.setArc(1, "");
        state.setArc(2, "/");
        state.setArc(3, "*");
        lexAcceptors[1].setState(0, state);

        state = new FaState(1);
        state.setArc(-2, "");
        lexAcceptors[1].setState(1, state);

        state = new FaState(1);
        state.setArc(-3, "");
        lexAcceptors[1].setState(2, state);

        state = new FaState(1);
        state.setArc(-4, "");
        lexAcceptors[1].setState(3, state);


        lexAcceptors[2] = new FAutomat(2, 3, rdr);
        state = new FaState(2);
        state.setArc(1, "");
        state.setArc(2, "^");
        lexAcceptors[2].setState(0, state);

        state = new FaState(1);
        state.setArc(-2, "");
        lexAcceptors[2].setState(1, state);

        state = new FaState(1);
        state.setArc(-3, "");
        lexAcceptors[2].setState(2, state);


        lexAcceptors[3] = new FAutomat(3, 8, rdr);
        state = new FaState(5);
        state.setArc(1, " ");
        state.setArc(2, "().");
        state.setArc(3, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        state.setArc(4, "=");
        state.setArc(5, "^");
        lexAcceptors[3].setState(0, state);

        state = new FaState(2);
        state.setArc(-2, "");
        state.setArc(1, " ");
        lexAcceptors[3].setState(1, state);

        state = new FaState(1);
        state.setArc(-3, "");
        lexAcceptors[3].setState(2, state);

        state = new FaState(2);
        state.setArc(-4, "");
        state.setArc(3, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.");
        lexAcceptors[3].setState(3, state);

        state = new FaState(1);
        state.setArc(-5, "");
        lexAcceptors[3].setState(4, state);

        state = new FaState(1);
        state.setArc(6, "*");
        lexAcceptors[3].setState(5, state);

        state = new FaState(1);
        state.setArc(7, "/");
        lexAcceptors[3].setState(6, state);

        state = new FaState(1);
        state.setArc(-6, "");
        lexAcceptors[3].setState(7, state);


        lexAcceptor = lexAcceptors[0];
        lexStk.push(lexAcceptor);
    }


    public Lexem getLexem() {
        if (lexStk.size() <= 0) {
            Lexem.groupIndex = -2147483647;
            return Lexem;
        }
        ignoreLastWord = true;
        while (ignoreLastWord) {
            ignoreLastWord = false;
            Lexem = lexAcceptor.getLexem();
            switch (lexAcceptor.getIndex()) {

                case 0:    // main
                    switch (Lexem.groupIndex) {
                        case -1: //EOF
                            Lexem.groupIndex = 0;
                            break;
                        case -2: //text
                            Lexem.groupIndex = 1;
                            if (beginOfWord.length() > 0) {
                                Lexem.textOfWord.insert(0, beginOfWord);
                                beginOfWord.delete(0, beginOfWord.length());
                            }
                            break;
                        case -3: //slash
                            ignoreLastWord = true;
                            beginOfWord.append("/");
                            lexStk.push(lexAcceptor);
                            lexAcceptor = lexAcceptors[1];
                            break;
                    }
                    break;
                case 1:    //slash
                    switch (Lexem.groupIndex) {
                        case -1: //EOF
                            Lexem.groupIndex = 0;
                            break;
                        case -2: //text
                            Lexem.groupIndex = 1;
                            ignoreLastWord = true;
                            beginOfWord.append(Lexem.textOfWord);
                            lexAcceptor = (FAutomat) lexStk.pop();
                            break;
                        case -3: //slash
                            ignoreLastWord = true;
                            beginOfWord.append("/");
                            break;
                        case -4: //star
                            ignoreLastWord = true;
                            beginOfWord.append("*");
                            lexStk.push(lexAcceptor);
                            lexAcceptor = lexAcceptors[2];
                            break;
                    }
                    break;
                case 2:    // star
                    switch (Lexem.groupIndex) {
                        case -1: //EOF
                            Lexem.groupIndex = 0;
                            break;
                        case -2: //text
                            Lexem.groupIndex = 1;
                            ignoreLastWord = true;
                            beginOfWord.append(Lexem.textOfWord);
                            lexStk.pop();
                            lexAcceptor = (FAutomat) lexStk.pop();
                            break;
                        case -3: //point
                            beginOfWord.delete(beginOfWord.length() - 2, beginOfWord.length());
                            if (beginOfWord.length() == 0) ignoreLastWord = true;
                            else {
                                Lexem.groupIndex = 1;
                                Lexem.textOfWord.delete(0, Lexem.textOfWord.length());
                                Lexem.textOfWord.append(beginOfWord);
                                beginOfWord.delete(0, beginOfWord.length());
                            }
                            lexStk.push(lexAcceptor);
                            lexAcceptor = lexAcceptors[3];
                            break;
                    }
                    break;
                case 3:    // point
                    switch (Lexem.groupIndex) {
                        case -1: //EOF
                            Lexem.groupIndex = 0;
                            break;
                        case -2: //space
                            ignoreLastWord = true;
                            break;
                        case -3: //symb
                            break;
                        case -4: //NameOfValue
                            Lexem.groupIndex = 2;
                            try {
                                Lexem.wordIndex = Integer.parseInt(tStrings.getProperty(Lexem.textOfWord.toString()));
                            } catch (Exception e) {
                                Lexem.wordIndex = -1;
                            }
                            break;
                        case -5: //subst
                            break;
                        case -6: //finish
                            ignoreLastWord = true;
                            lexStk.pop();
                            lexStk.pop();
                            lexAcceptor = (FAutomat) lexStk.pop();
                            break;
                    }
                    break;
            }
        }
        return Lexem;
    }
}