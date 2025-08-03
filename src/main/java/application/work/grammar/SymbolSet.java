package application.work.grammar;

import java.util.ArrayList;

public class SymbolSet {
    private ArrayList<Symbol> setS;

    public SymbolSet() {
        setS = new ArrayList<>();
    }

    public int getSize() {
        return setS.size();
    }

    public void deleteSymbol(int sIndex) {
        if ((setS != null) && (sIndex >= 0) && (sIndex < setS.size())) {
            setS.remove(sIndex);
        }
    }

    public Symbol addSymbol(int Type, String Name) {
        int i;
        Symbol cs;
        for (i = 0; i < setS.size(); i++) {
            cs = (Symbol) setS.get(i);
            if (cs.similar(Name) == 0) {
                if (cs.getType() == Type) return cs;
            }    // else if(Type!=-4)return null;}	//06.02.2007
        }
        cs = new Symbol(Type, Name);
        cs.setId(setS.size());
        setS.add(cs);
        return cs;
    }

    public Symbol getSymbol(int index) {
        if ((index < 0) || (index >= setS.size())) return null;
        return (Symbol) setS.get(index);
    }

    public Symbol getSymbol(String Name) {
        for (int i = 0; i < setS.size(); i++) {
            Symbol cs = (Symbol) setS.get(i);
            if (cs.similar(Name) == 0) return cs;
        }
        return null;
    }

    public int indexOf(Symbol smb) {
    //14.05.2009  return setS.indexOf(smb);
        String n = smb.getName();
        int t = smb.getType();
        Symbol s;
        for (int i = 0; i < setS.size(); i++)
            if ((t == (s = (Symbol) setS.get(i)).getType()) && (n.compareTo(s.getName()) == 0))
                return i;
        return -1;
    }

    public void sortById() {
        int i, j, siz, im;
        Symbol s, ss;
        String n;
        siz = setS.size();
        for (i = 0; i < siz; i++) {
            for (j = 0; j < siz; j++) {
                if ((s = (Symbol) setS.get(j)).getId() == i) {
                    if (i == j) break;
                    setS.set(j, setS.get(i));
                    setS.set(i, s);
                }
            }
        }
        for (i = 0; i < siz; i++) {
            if ((s = (Symbol) setS.get(i)).getType() == -3) {
                im = i;
                n = s.getName();
                for (j = i + 1; j < siz; j++) {
                    if (((s = (Symbol) setS.get(j)).getType() == -3) && (s.getName().compareTo(n) < 0)) {
                        im = j;
                        n = s.getName();
                    }
                }
                if (im > i) {
                    s = (Symbol) setS.get(im);
                    s.setId(i);
                    ss = (Symbol) setS.get(i);
                    ss.setId(im);
                    setS.set(im, ss);
                    setS.set(i, s);
                }
            }
        }
    }
}