package application.work.finAutomat;

import java.util.ArrayList;

public class CharToColumn {
    private int states;
    public String allArcs = "";
    private String[] rng = null;
    private ArrayList<FA_Row> rows = new ArrayList();

    public CharToColumn(FinAutomat fa) {
        states = fa.fromNode.length;
        FA_Row nRow = new FA_Row(states, null);
        nRow.to[0] = -1;
        rows.add(nRow);
        for (int i = 0; i < states; i++) {
            FA_Arc arc = fa.getFirstFrom(i);
            while (arc != null) {
                String mark = arc.getMark() == null ? null : arc.getMark().toString();
                for (int j = 0; j < rows.size(); j++) {
                    String s = rows.get(j).mark;
                    String newS = rest(s, mark);
                    if ((newS != null) && (newS.length() > 0) && (newS.length() != s.length())) {
                        rows.get(j).mark = newS;
                    }
                    mark = rest(mark, newS);
                }
                int len;
                if ((mark != null) && ((len = mark.length()) > 0)) {
                    boolean fl = true;
                    for (int j = 0; j < rows.size(); j++) {
                        String s = rows.get(j).mark;
                        boolean f = true;
                        if ((s != null) && (s.length() == len)) {
                            for (int k = 0; k < len; k++) {
                                int c = s.charAt(k);
                                if (mark.indexOf(c) < 0) {
                                    f = false;
                                    break;
                                }
                            }
                        } else
                            f = false;
                        if (f) {
                            fl = false;
                            break;
                        }
                    }
                    if (fl)
                        rows.add(new FA_Row(states, mark.toString()));
                }
                arc = arc.getNext();
            }
        }
        for (int i = rows.size() - 1; i >= 0; i--) {
            String m = rows.get(i).mark;
            for (int j = 0; j < i; j++) {
                m = rest(m, rows.get(j).mark);
            }
            if ((m == null) || (m.length() == 0))
                rows.remove(i);
            else
                rows.get(i).mark = m;
        }
        nRow = new FA_Row(states, null);
        nRow.to[0] = -1;
        rows.add(0, nRow);
        for (int i = 0; i < states; i++) {
            FA_Arc arc = fa.getFirstFrom(i);
            while (arc != null) {
//     deb+=""+arc.getNodeFrom()+" '"+arc.getMark()+"' "+arc.getNodeTo()+"|";
                int to = arc.getNodeTo();
                if (to < 0)
                    to -= 1;
                String mark = (arc.getMark() == null ? null : arc.getMark().toString());
                if ((mark == null) || (mark.length() == 0))
                    rows.get(0).to[i] = to;
                else
                    for (int j = 1; j < rows.size(); j++) {
                        boolean fl = false;
                        String m = rows.get(j).mark;
                        for (int k = 0; k < m.length(); k++) {
                            if (mark.indexOf(m.charAt(k)) >= 0) {
                                fl = true;
                                break;
                            }
                        }
                        if (fl)
                            rows.get(j).to[i] = to;
                    }
                arc = arc.getNext();
            }
        }
        for (int i = 0; i < states; i++) {
//     deb+="_";
//     for(int j=0;j<rows.size();j++)
//       deb+=","+rows.get(i).to[j];
            int to = rows.get(0).to[i];
            if (to < 0) {
                for (int j = 1; j < rows.size(); j++) {
                    int nTo = rows.get(j).to[i];
                    if (nTo == 0) {
                        if (i > 0)
                            rows.get(j).to[i] = to;
                    } else if (nTo < 0)
                        rows.get(j).to[i] = nTo & 0xbfffffff;
                }
                rows.get(0).to[i] = to & 0xbfffffff;
            } else if (to > 0) {
                for (int j = 1; j < rows.size(); j++)
                    if (rows.get(j).to[i] == 0)
                        rows.get(j).to[i] = to;
                    else
                        rows.get(j).to[i] &= 0xbfffffff;
            } else
                for (int j = 1; j < rows.size(); j++)
                    if (rows.get(j).to[i] < 0)
                        rows.get(j).to[i] &= 0xbfffffff;
        }
        for (int i = 0; i < rows.size(); i++) {
            FA_Row r = rows.get(i);
//     allArcs+=r.mark+": ";
//     for(int j=0;j<states;j++)
//       allArcs+=r.to[j]+", ";
            if (r.mark == null)
                r.ranges = null;
            else {
                char[] t = r.mark.toCharArray();
                int len = r.mark.length();
                for (int j = 1; j < len; j++)
                    for (int k = 0; k < len - 1; k++)
                        if (t[k + 1] < t[k]) {
                            char x = t[k];
                            t[k] = t[k + 1];
                            t[k + 1] = x;
                        }
                int st = 0, ic = 1;
                StringBuilder b = new StringBuilder();
                while (ic < len) {
                    while (t[ic - 1] + 1 == t[ic]) {
                        ic += 1;
                        if (ic >= len)
                            break;
                    }
                    b.append(t[st]);
                    if (st + 1 < ic) {
                        if (st + 2 < ic)
                            b.append("-");
                        b.append(t[ic - 1]);
                    }
                    st = ic;
                    if (++ic >= len)
                        break;
                    b.append('\u00A0');
                }
                if (b.length() == 0)
                    b.append(t[len - 1]);
                else if (b.charAt(b.length() - 1) != t[len - 1]) {
                    if (b.charAt(b.length() - 1) != t[len - 1] - 1)
                        b.append('\u00A0');
                    b.append(t[len - 1]);
                }
                r.ranges = b.toString();
            }
        }
        allArcs = rows.get(1).ranges;
        for (int i = 2; i < rows.size(); i++)
            allArcs += "\u00A0" + rows.get(i).ranges;
        rng = allArcs.split("\u00A0");
    }

    private String rest(String m, String mrk) {
        int i = 0, k;
        if ((m == null) || (mrk == null))
            return m;
        while (i < mrk.length()) {
            if ((k = m.indexOf(mrk.charAt(i))) >= 0)
                if (k < m.length() - 1)
                    m = m.substring(0, k) + m.substring(k + 1);
                else
                    m = m.substring(0, k);
            i += 1;
        }
        return m;
    }

    public int getStates() {
        return states;
    }

    public int getColumnsCount() {
        return rows.size();
    }

    public int getCols() {
        return states;
    }

    public int getRows() {
        return states;
    }

    public String getAllRanges(int k) {
        if ((k < 0) || (k >= rows.size()))
            return null;
        return rows.get(k).ranges;
    }

    public int getRangesCount() {
        return rng.length;
    }

    /*public String getRanges(int k,int n){
      if((k<0)||(k>=rows.size())||(n<0))
        return null;
     String[] rn=rows.get(k).ranges.split("\u00A0");
     if(n>=rn.length)
       return null;
     return rn[n];
    }*/
    public int getCell(int st, int c) {
        return rows.get(st).to[c];
    }

    public int getStateTo(int st, int cl) {
        if ((cl < 0) || (cl >= rows.size()) || (st < 0) || (st >= states))
            return 0;
        return rows.get(cl).to[st];
    }

    public int getRangeIndex(int n) {
        if ((n < 0) || (n >= rng.length))
            return -1;
        char let = rng[n].charAt(0);
        for (int i = 0; i < rows.size(); i++)
            if ((rows.get(i).mark != null) && (rows.get(i).mark.indexOf(let) >= 0))
                return i;
        return -1;
    }

    public char getFirstCharOfRange(int n) {
        if ((n < 0) || (n >= rng.length) || (rng[n].length() == 0))
            return '\u0000';
        return rng[n].charAt(0);
    }

    public char getLastCharOfRange(int n) {
        if ((n < 0) || (n >= rng.length) || (rng[n].length() == 0))
            return '\u0000';
        return rng[n].charAt(rng[n].length() - 1);
    }
/*
 public String getAllMark(){
   StringBuilder m=new StringBuilder("");
   for(int i=0;i<rows.size();i++)
     m.append((rows.get(i).ranges==null?"null":rows.get(i).ranges+"/"+rows.get(i).ranges.length())+" | ");
   return m.toString();
 }*/
}
