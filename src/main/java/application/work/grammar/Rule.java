package application.work.grammar;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class Rule {
    private Symbol lPart = null;
    private ArrayList<RSymbol> rPart = null;
    @Setter
    @Getter
    private ArrayList<Symbol> selSet = null;

    // private int firstStateIndex;
    public Rule(Symbol leftPart) {
        if ((leftPart != null) && ((leftPart.getType() & 1) == 1))
            lPart = leftPart;
        else if (leftPart == null)
            lPart = null;
        else return;
        rPart = new ArrayList<>();
    }

    public void addSymbol(Symbol Symbol) {
        if ((rPart != null) && (Symbol != null)) rPart.add(new RSymbol(Symbol));
    }

    public void deleteSymbol(int sIndex) {
        int i;
        if ((rPart != null) && (sIndex >= 0) && (sIndex < rPart.size()))
            for (i = sIndex; i < rPart.size() - 1; i++) {
                rPart.set(i, rPart.get(i + 1));
            }
        assert rPart != null;
        rPart.remove(rPart.size() - 1);
    }

    public Symbol getLeftPart() {
        return lPart;
    }

    public int getLength() {
        return (rPart == null ? 0 : rPart.size());
    }

    public int getLengthWOActions() {
        int i, t, c = 0;
        Symbol s;
        for (i = 0; i < rPart.size(); i++) {
            if ((s = ((RSymbol) rPart.get(i)).getSymbol()) != null) {
                t = s.getType();
                if ((t != 0) && (t > -4)) c += 1;
            }
        }
        return c;
    }

    public Symbol getSymbol(int index) {
        if (rPart == null) return null;
        if ((index >= 0) && (index < rPart.size())) return ((RSymbol) rPart.get(index)).getSymbol();
        else return null;
    }

    public RSymbol getRSymbol(int index) {
        if (rPart == null) return null;
        if ((index >= 0) && (index < rPart.size())) return (RSymbol) rPart.get(index);
        else return null;
    }

    public boolean compareTo(Rule r) {
        int i, k = 0;
        Symbol s, c;
        for (i = 0; i < rPart.size(); i++) {
            s = ((RSymbol) rPart.get(i)).getSymbol();
            if ((s != null) && (s.getType() > -4) && (s.getType() != 0)) {
                for (; k < r.getLength(); k++) {
                    c = r.getSymbol(k);
                    if ((c != null) && (c.getType() > -4) && (c.getType() != 0))
                        if (c.getId() != s.getId()) return false;
                        else break;
                }
                k += 1;
            }
        }
        for (; k < r.getLength(); k++) {
            c = r.getSymbol(k);
            if ((c != null) && (c.getType() > -4) && (c.getType() != 0)) return false;
        }
        return r.getLength() == rPart.size();
    }
// public void setFirstStateIndex(int newStateIndex){firstStateIndex=newStateIndex;}
// public int getFirstStateIndex(){return firstStateIndex;}
}