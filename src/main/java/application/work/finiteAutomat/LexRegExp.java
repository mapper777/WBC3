package application.work.finiteAutomat;

public class LexRegExp {
    private int exprType;
    private StringBuilder exprText;
    private int pos;

    public LexRegExp(StringBuilder strE) {
        exprText = strE;
        exprType = -1;
        pos = defType();
    }

    public LexRegExp(int type, StringBuilder strE) {
        exprText = strE;
        exprType = type;
        pos = 0;
    }

    public LexRegExp(String strE) {
        exprText = new StringBuilder(strE);
        exprType = -1;
        pos = defType();
    }

    private int defType() {
        int i, j, k, brCnt, primE, fbp, lbp = 1, waitT = 0, waitP;
        char c;
        if (exprType == 0) return pos;
        while (lbp > 0) {
            for (exprType = -1, fbp = -1, lbp = -1, brCnt = 0, primE = -1,
                         waitP = exprText.length(), i = 0; i < exprText.length(); i++) {
                c = exprText.charAt(i);
                if (primE <= 0) {
                    switch (c) {
                        case '"':
                            k = i + 1;
                            if (exprText.charAt(k) == c) return i;
                            while (k < exprText.length()) {
                                if ((j = exprText.indexOf("\"", k)) < 0) return i;
                                if (exprText.charAt(j - 1) != '\\') break;
                                k = j + 1;
                            }
                            if (k >= exprText.length()) return i;
                            exprText.delete(i, i + 1);
                            j = i;
                            while (j < exprText.length()) {
                                if ((exprText.charAt(j) == '"') && (j < exprText.length())) {
                                    exprText.replace(j, j + 1, "");
                                    break;
                                } else if (exprText.charAt(j) == '\\') {
                                    if (j <= exprText.length() - 2) {
                                        exprText.replace(j, j + 2, "[" + exprText.substring(j, j + 2) + "]");
                                        j += 4;
                                    } else return i;
                                } else {
                                    exprText.replace(j, j + 1, "[" + exprText.substring(j, j + 1) + "]");
                                    j += 3;
                                }
                            }
                        case '[':
                            if ((primE < 0) || (brCnt > 0)) {
                                primE = 1;
                                break;
                            } else {
                                exprType = 6;
                                return i;
                            }
                        case '(':
                            if ((brCnt == 0) && ((waitT > 0) || (fbp >= 0) || (primE == 0))) {
                                exprType = 6;
                                return i;
                            }
                            brCnt += 1;
                            if (fbp < 0) fbp = i;
                            break;
                        case ')':
                            if (brCnt >= 0) {
                                brCnt -= 1;
                                lbp = i;
                                break;
                            } else {
                                return i;
                            }
                        case ' ':
                        case '\n':
                        case '\r':
                        case '\t':
                            break;
                        case '|':
                            if (brCnt == 0) {
                                if (primE == 0) {
                                    exprType = 5;
                                    return i;
                                } else return i;
                            }
                            break;
                        case '?':
                            if ((brCnt == 0) && (primE == 0)) {
                                waitT = 2;
                                waitP = i;
                            }
                            break;
                        case '*':
                            if ((brCnt == 0) && (primE == 0)) {
                                waitT = 3;
                                waitP = i;
                            }
                            break;
                        case '+':
                            if ((brCnt == 0) && (primE == 0)) {
                                waitT = 4;
                                waitP = i;
                            }
                            break;
                        default:
                            return i;
                    }
                } else if (c == '\\') i += 1;
                else if (c == ']') primE = 0;
            }
            if (brCnt == 0) {
                if (waitT == 0) {
                    if ((fbp >= 0) && (lbp > fbp)) {
                        exprText.delete(lbp, exprText.length());
                        exprText.delete(0, fbp + 1);
                    } else {
                        exprType = (primE == 0 ? 1 : (exprText.length() == 0 ? 1 : -1));
                        return exprText.length();
                    }
                } else if (fbp <= lbp) {
                    exprType = waitT;
                    return waitP;
                } else {
                    return exprText.length();
                }
            } else {
                return exprText.length();
            }
        }
        return -1;
    }

    public int getType() {
        return exprType;
    }

    public StringBuilder getExprText() {
        StringBuilder rt = new StringBuilder();
        rt.append(exprText);
        return rt;
    }

    public void setExprText(StringBuilder newText) {
        exprText.delete(0, exprText.length());
        exprText.append(newText);
    }

    public LexRegExp simplify() {
        int i, l;
        char c, b, e;
        LexRegExp r = null;
        if ((exprType <= 0) || (exprType > 6)) return null;
        if (exprType > 1) {
            if ((pos < 0) || (pos >= exprText.length())) {
                if (exprText.length() == 0) exprType = 1;
                return null;
            }
            if (exprType > 4) r = new LexRegExp(exprText.substring(pos + (exprType == 5 ? 1 : 0)));
            for (i = pos - 1; i >= 0; i--) {
                c = exprText.charAt(i);
                if ((c != ' ') && (c != '\t')) {
                    exprText.delete(i + 1, exprText.length());
                    break;
                }
            }
            for (i = 0; i < pos; i++) {
                c = exprText.charAt(i);
                if ((c != ' ') && (c != '\t')) {
                    if (i > 0) {
                        exprText.delete(0, i);
                    }
                    break;
                }
            }
            pos = defType();
            return r;
        } else {
            exprType = 0;
            l = exprText.lastIndexOf("]");
            i = exprText.indexOf("[") + 1;
            if (i >= l) {
                exprText.delete(0, exprText.length());
                return null;
            }
            exprText.delete(l, exprText.length());
            exprText.delete(0, i);
            i = 0;
            l = exprText.length();
            while (i < l) {
                if (((c = exprText.charAt(i)) == '\\') && (i + 1 < l)) {
                    switch (exprText.charAt(i + 1)) {
                        case 't':
                            exprText.replace(i, i + 2, "\t");
                            break;
                        case 'n':
                            exprText.replace(i, i + 2, "\n");
                            break;
                        case 'r':
                            exprText.replace(i, i + 2, "\r");
                            break;
                        case '\\':
                            break;
                        default:
                            exprText.delete(i, i + 1);
                    }
                    l -= 1;
                } else if ((c == '-') && (i > 0) && (i + 1 < l)) {
                    b = exprText.charAt(i - 1);
                    e = exprText.charAt(i + 1);
                    if (b > e) {
                        exprText.delete(i, i + 2);
                        exprText.insert(i - 1, e);
                        c = b;
                        b = e;
                        e = c;
                    } else exprText.delete(i, i + 1);
                    l -= 1;
                    while (++b < e) {
                        exprText.insert(i++, b);
                        l += 1;
                    }
                }
                i += 1;
            }
            i = 0;
            while (i < exprText.length()) {
                String s = exprText.substring(i, i + 1);
                if ((s != "\\") && (exprText.indexOf(s) < i)) {
                    exprText.delete(i, i + 1);
                } else i += 1;
            }
            return null;
        }
    }

    public int getPos() {
        return (exprType <= 0 ? 0 : pos);
    }
}