package application.work.tplParser;

public class PseudoCodeItem {
    public int typ;
    public int label;
    public int ind;
    public int code;
    public int no;
    public String value;

    public PseudoCodeItem(int t, int l, String v) {
        typ = t;
        label = l;
        value = v;
        code = -1;
        no = 0;
        if (t == 1) {
            for (int ind = 0; ind < v.length(); ind++)
                if (v.charAt(ind) == '\n')
                    no += 1;
        }
    }

    public PseudoCodeItem(int t, int l, int c, String v) {
        typ = t;
        label = l;
        value = v;
        code = c;
        no = 0;
        if (t == 1) {
            for (int ind = 0; ind < v.length(); ind++)
                if (v.charAt(ind) == '\n')
                    no += 1;
        }
    }
}
