package application.work.finiteAutomat;

public class NodeSet {
    private int no;
    private int cnt;
    private int[] nodes;
    private NodeSet next;
    private String grpName = null;
    private String action = null;
    private FA_Arc arc = null;

    public NodeSet(int count, int[] nodes) {
        int i;
        next = null;
        no = -1;
        if ((cnt = count) > 0) {
            this.nodes = new int[cnt];
            for (i = 0; i < cnt; i++)
                if ((this.nodes[i] = nodes[i]) >= 0)
                    no = 0;
        }
    }

    public NodeSet getNext() {
        return next;
    }

    public void setNext(NodeSet n) {
        if ((n != null) && (next != null)) {
            NodeSet curNS = n;
            while (curNS.next != null)
                curNS = curNS.next;
            curNS.next = next;
        }
        next = n;
    }

    public boolean compareTo(int count, int[] nodes) {
        int i, j, k, r;
        if ((r = count) != cnt) return false;
        for (i = 0; i < cnt; i++) {
            k = nodes[i];
            for (j = 0; j < cnt; j++)
                if (this.nodes[j] == k) {
                    r -= 1;
                    break;
                }
        }
        return (r == 0);
    }

    public void addNode(int nodeNo) {
        int i;
        for (i = 0; i < cnt; i++)
            if (nodes[i] == nodeNo)
                return;
        int[] newN = new int[cnt + 1];
        for (i = 0; i < cnt; i++)
            newN[i] = nodes[i];
        newN[cnt++] = nodeNo;
        nodes = newN;
    }

    public boolean inSet(int n) {
        int i;
        for (i = 0; i < cnt; i++) {
            if (nodes[i] == n) return true;
        }
        return false;
    }

    public int getNodeNo(int no) {
        if ((no >= 0) && (no < cnt))
            return nodes[no];
        return 1000000001;
    }

    public int[] getNodes() {
        return nodes;
    }

    public int getCount() {
        return cnt;
    }

    public void setGrpName(StringBuilder gn) {
        grpName = new String(gn);
    }

    public String getGrpName() {
        return grpName;
    }

    public void setAction(StringBuilder an) {
        action = new String(an);
    }

    public String getAction() {
        return action;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int newNo) {
        no = newNo;
    }

    public FA_Arc getArc() {
        return arc;
    }

    public void addArc(FA_Arc newArc) {
        newArc.setNext(null);
        newArc.setNext(arc);
        arc = newArc;
    }
}
