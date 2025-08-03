package application.work.finAutomat;

public class FA_Arc {
    private int from;
    private int to;
    // private LexRegExp re;
    private StringBuilder mark;
    private FA_Arc next;

    /* public FA_Arc(int nodeFrom,int nodeTo,LexRegExp arcMark){
      from=nodeFrom;
      to=nodeTo;
      re=arcMark;
      mark=null;
      next=null;
     }*/
    public FA_Arc(StringBuilder mark, int nodeFrom, int nodeTo) {
        from = nodeFrom;
        to = nodeTo;
//  re=null;
        this.mark = ((mark == null) || (mark.toString().equals("null"))) ? null : mark;
        next = null;
    }

    public void setMark(StringBuilder mark) {
        if (mark == null)
            this.mark = null;
        else {
            if (this.mark == null)
                this.mark = new StringBuilder();
            else
                this.mark.delete(0, this.mark.length());
            this.mark.append(mark);
        }
    }

    public void setMark() {
        if (mark != null)
            mark.delete(0, mark.length());
/*  if((re!=null)&&(re.getType()==0)){
   if(mark==null)
     mark=new StringBuilder();
   if(re.getExprText().length()>0)
     mark.append(re.getExprText());
  }*/
    }

    public StringBuilder getMark() {
//  return new StringBuilder(mark==null?"":mark);
        return mark == null ? null : new StringBuilder(mark);
    }

    public StringBuilder getNMark() {
        return new StringBuilder(((mark == null) || (mark.equals("null"))) ? "" : mark);
//  return mark==null?null:new StringBuilder(mark);
    }

    public boolean compareMark(StringBuilder mrk) {
        if ((mark == null) && (mrk == null))
            return true;
        if ((mark == null) || (mrk == null))
            return false;
        if (mark.length() != mrk.length())
            return false;
        for (int i = 0; i < mrk.length(); i++) {
            String s = mrk.substring(i, i + 1);
            if (mark.indexOf(s) < 0)
                return false;
        }
        for (int i = 0; i < mark.length(); i++) {
            String s = mark.substring(i, i + 1);
            if (mrk.indexOf(s) < 0)
                return false;
        }
        return true;
    }

    public void delFromMark(StringBuilder delSimbols) {
        int i, j;
        String s;
        if ((mark != null) && (mark.length() > 0) && (delSimbols != null) && (delSimbols.length() > 0)) {
            for (i = 0; i < delSimbols.length(); i++) {
                s = delSimbols.substring(i, i + 1);
                if ((j = mark.indexOf(s)) >= 0)
                    mark.delete(j, j + 1);
                if (mark.length() == 0) break;
            }
        }
    }

    /* public LexRegExp getExpr(){
      return re;
     }
     public void setExpr(LexRegExp newRe){
      re=newRe;
     }*/
    public FA_Arc getNext() {
        return next;
    }

    public void setNext(FA_Arc newNext) {
        if ((newNext != null) && (next != null)) {
            FA_Arc curA = newNext;
            while (curA.next != null) curA = curA.next;
            curA.next = next;
        }
        next = newNext;
    }

    public int getNodeFrom() {
        return from;
    }

    public int getNodeTo() {
        return to;
    }

    public void setNodeFrom(int newNodeFrom) {
        from = newNodeFrom;
    }

    public void setNodeTo(int newNodeTo) {
        to = newNodeTo;
    }
}
