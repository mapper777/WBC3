package application.work.finiteAutomat;

import java.util.ArrayList;

public class FinAutomat {
    public String src;
    int nodeCnt = 0;
    int finCnt = 0;
    private ArrayList<String> grpNames = new ArrayList();
    private ArrayList<String> actions = new ArrayList();
    FA_Arc[] fromNode;

    public FinAutomat(String s) {
        int i = s.indexOf(",\"arcs\":");
        String[] fs = s.substring(17, i - 2).split("];\\[");
        src = s.substring(i + 17, s.length() - 3);
        String[] all = src.split("],\\[\"from\",");
        src = s;
        finCnt = fs.length;
        for (i = 0; i < fs.length; i++) {
            String sn = fs[i].substring(fs[i].indexOf(",") + 2);
            grpNames.add(sn.substring(0, sn.indexOf(" ")));
            sn = sn.substring(sn.indexOf(" "), sn.length() - 1).trim();
            actions.add(sn);
        }
        nodeCnt = all.length;
        fromNode = new FA_Arc[nodeCnt];
        for (i = 0; i < nodeCnt; i++) {
            String[] ar = all[i].split("],\\[");
            FA_Arc last = fromNode[i];
            for (int j = 1; j < ar.length; j += 2) {
                FA_Arc arc = new FA_Arc(new StringBuilder(ar[j + 1].substring(6, ar[j + 1].length() - 1)), i, Integer.parseInt(ar[j].substring(5)));
                if (last == null)
                    fromNode[i] = arc;
                else
                    last.setNext(arc);
                last = arc;
            }
        }
/*    FA_Arc arc=new FA_Arc(new StringBuilder(""),0,0);
    arc.setNext(fromNode[0]);
    fromNode[0]=arc;*/
    }

    public boolean getFlagOther() {
        return true;
    }

    public int getWorkNodesCount() {
        return nodeCnt;
    }

    public int getGrpCount() {
        return finCnt;
    }

    public FA_Arc getFirstFrom(int fr) {
        return ((fr >= 0) && (fr < fromNode.length)) ? fromNode[fr] : null;
    }

    public String getName() {
        return "";
    }

    public String getGrpName(int ndx) {
        return ((ndx >= 0) && (ndx < grpNames.size())) ? grpNames.get(ndx) : "";
    }

    public String getAction(int ndx) {
        return ((ndx >= 0) && (ndx < actions.size())) ? actions.get(ndx) : "";
    }

    public StringBuilder getAll() {
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < nodeCnt; i++) {
            FA_Arc a = fromNode[i];
            while (a != null) {
                r.append("Fr " + a.getNodeFrom() + " to " + a.getNodeTo() + " by '" + a.getMark() + "' ");
                a = a.getNext();
            }
        }
        return r;
    }
}