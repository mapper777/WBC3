package application.work.grammar;

import java.util.ArrayList;

public class Grammar {
    //private boolean traceFlag=false;
    private Symbol startNT = null;
    private ArrayList rules, St, Fl, confTbl = null, confUTbl = null, actions;
    private SymbolSet symbSet;
    private int cntN, cntA, cntS, cntNod = -1, cntUnd = 0, cntDelRules = -1, lrType = 0;
    public StringBuffer rezU = new StringBuffer();

    public Grammar() {
        rules = new ArrayList();
        symbSet = new SymbolSet();
        symbSet.addSymbol(-1, "");
        Rule r = new Rule(null);
        ArrayList rls = new ArrayList();
        rls.add(r);
        rules.add(rls);
        St = new ArrayList();
        Fl = new ArrayList();
        actions = null;
        confTbl = null;
        cntN = 0;
        cntA = 0;
    }

    public int getCountOfSymbols(int whatSymbols) {
        switch (whatSymbols) {
            case 0:
                return (cntN + cntS);
            case 1:
                return cntN;
            case 2:
                return cntS;
            case 3:
                return cntA;
            case 4:
                return cntNod;
            case 5:
                return cntUnd;
            case 6:
                return testLL1() ? 1 : 0;
            case 7:
                return lrType;
            default:
                return 0;
        }
    }

    public String getListNamesDeletedSymbols() {
        return rezU.toString();
    }

    public SymbolSet getSymbolSet() {
        return symbSet;
    }

    public void setSymbolSet(SymbolSet nSet) {
        symbSet = nSet;
    }

    public void addRule(Rule newRule) {
        Symbol sl, sr, st = null;
        int i, cnt = 0;
        ArrayList rls = null;
        Rule r;
        sl = newRule.getLeftPart();
        i = 0;
        while (i >= 0) {
            sr = newRule.getSymbol(i);
            if (sr == null) break;
            if (sr.getType() > -4) {
                st = sr;
                cnt += 1;
            }
            i += 1;
        }
//  if((cnt==1)&&(sl==st))return;
        if (startNT == null) startNT = sl;
        for (i = 0; i < rules.size(); i++) {
            if ((rls = (ArrayList) rules.get(i)) == null) break;
            if (((Rule) rls.get(0)).getLeftPart() == sl) break;
            rls = null;
        }
        if (rls == null) {
            rls = new ArrayList();
            rls.add(newRule);
            rules.add(rls);
        } else {
            for (i = 0; i < rls.size(); i++) {
                if (newRule.compareTo((Rule) rls.get(i))) return;
            }
            rls.add(newRule);
        }
    }

    public Symbol getStartNT() {
        return startNT;
    }

    public void setStartNT(Symbol StartNT) {
        if ((StartNT == null) || (StartNT.getType() <= 0) || (startNT == StartNT)) return;
        startNT = StartNT;
        if (rules.size() <= 1) return;
        for (int i = 0; i < rules.size(); i++) {
            ArrayList rl = (ArrayList) rules.get(i);
            if ((rl.size() > 0) && (((Rule) rl.get(0)).getLeftPart() == startNT)) {
                if (i > 0) {
                    rules.set(i, rules.get(0));
                    rules.set(0, rl);
                }
                return;
            }
        }
    }

    public ArrayList getActions() {
        return actions;
    }

    public int getRulesCount() {
        int i, c = 0;
        if (rules != null)
            for (i = 0; i < rules.size(); i++) c += ((ArrayList) rules.get(i)).size();
        return c;
    }

    public Rule getRule(int index) {
        ArrayList rls;
        int r, i, ndx;
        if (index < 0) return null;
        ndx = index;
        i = 0;
        r = 0;
        while (ndx >= 0) {
            if (r >= rules.size()) return null;
            rls = (ArrayList) rules.get(r);
            if (ndx < rls.size()) return (Rule) rls.get(ndx);
            ndx -= rls.size();
            r += 1;
        }
        return null;
    }

    public Rule getRule(Symbol s, int index) {
        ArrayList rls;
        int i;
        if (index < 0) return null;
        for (i = 0; i < rules.size(); i++) {
            rls = (ArrayList) rules.get(i);
            if (((Rule) rls.get(0)).getLeftPart() == s) {
                return (index < rls.size() ? (Rule) rls.get(index) : null);
            }
        }
        return null;
    }

    // public ArrayList getRules(){return rules;}
    public ArrayList getRulesCopy(int minType) {
        ArrayList rez = new ArrayList(), rls, rll;
        Rule rlc, rln;
        Symbol smb;
        int i, j, k;
        for (i = 0; i < rules.size(); i++) {
            if ((rls = (ArrayList) rules.get(i)) != null) {
                rll = new ArrayList();
                for (j = 0; j < rls.size(); j++) {
                    if ((rlc = (Rule) rls.get(j)) != null) {
                        rln = new Rule(rlc.getLeftPart());
                        for (k = 0; ; k++) {
                            if ((smb = rlc.getSymbol(k)) == null) break;
                            if (smb.getType() > minType) rln.addSymbol(smb);
                        }
                        if (rln.getLength() == 0) {
                            if ((smb = symbSet.getSymbol(null)) == null) {
                                smb = symbSet.addSymbol(0, null);
                            }
                            rln.addSymbol(smb);
                        }
                        rll.add(rln);
                    }
                }
                if (rll.size() > 0)
                    rez.add(rll);
            }
        }
        return rez;
    }

    private void tranz(int[][] m) {
        int i, j, k, l;
        boolean a;
        l = m[0].length;
        cont:
        for (a = false, i = 0; i < l; i++)
            for (j = 0; j < l; j++)
                if (m[i][j] != 0) {
                    for (k = 0; k < l; k++) {
                        if (m[i][k] < m[j][k]) {
                            a = true;
                            m[i][k] = m[j][k];
                        }
                    }
                    if (a) {
                        i = -1;
                        a = false;
                        continue cont;
                    }
                }
    }

    public int[][] calcProperties() {
        int i, j, k, ii, jj, kk, ss, cnt, siz;
        boolean again, added;
        int[] ann, nod, und;
        int[][] rr, rx, rz;
        ArrayList gCopy, ntr, strt, vtmp, rll;
        Rule r, rln;
        Symbol s, tst = null, smb;
        ss = symbSet.getSize();
        cntA = ss;
        ann = new int[ss];
        nod = new int[ss];
        und = new int[ss];

        for (i = 0; i < ss; i++) {
            ann[i] = 0;
            nod[i] = 1;
            und[i] = 1;
        }
//03.04.2012 ????? ? ?????????? ?????? ??????
//ti.put(rules.size(),getStartNT().getName());
        cnt = 3 * rules.size();
        do {
            again = false;
            gCopy = getRulesCopy(-5);
            rules = new ArrayList();
            r = new Rule(null);
            ntr = new ArrayList();
            ntr.add(r);
            rules.add(ntr);
//s=startNT;
            for (i = 0; i < gCopy.size(); i++) {
                added = false;
                if ((ntr = (ArrayList) gCopy.get(i)) != null) {
                    siz = 0;
                    for (j = 0; j < ntr.size(); j++) {
                        if (((r = (Rule) ntr.get(j)) != null) && ((tst = r.getLeftPart()) != null)) {
                            tst.setType(1);
                            if (r.getLength() != 1)
                                break;
                            if (((s = r.getSymbol(0)) != null) && (s.getName().length() > 0)) {
                                for (k = 0; k < gCopy.size(); k++) {
                                    if ((k != i) && (((ArrayList) gCopy.get(k)).size() > 0)) {
                                        vtmp = (ArrayList) gCopy.get(k);
                                        smb = ((Rule) vtmp.get(0)).getLeftPart();
                                        if ((smb != null) && (s.getName().compareTo(smb.getName()) == 0)) {
                                            siz += 1;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (siz == ntr.size())
                        added = true;
                }
                for (j = 0; j < ntr.size(); j++) {
                    if (((r = (Rule) ntr.get(j)) != null) && ((tst = r.getLeftPart()) != null)) {
                        rln = new Rule(tst);
                        if (added) {
                            if (((s = r.getSymbol(0)) != null) && (s.getName().length() > 0)) {
                                for (k = 0; k < gCopy.size(); k++) {
                                    if ((k != i) && (((ArrayList) gCopy.get(k)).size() > 0)) {
                                        vtmp = (ArrayList) gCopy.get(k);
                                        smb = ((Rule) vtmp.get(0)).getLeftPart();
                                        if ((smb != null) && (s.getName().compareTo(smb.getName()) == 0) && (s.getName().compareTo(tst.getName()) != 0)) {
                                            for (ii = 0; ii < vtmp.size(); ii++) {
                                                r = (Rule) vtmp.get(ii);
                                                for (kk = 0; ; kk++) {
                                                    if ((smb = r.getSymbol(kk)) == null)
                                                        break;
                                                    rln.addSymbol(smb);
                                                }
                                                addRule(rln);
                                                rln = new Rule(tst);
                                            }
                                            again = true;
                                        }
                                    }
                                }
                            }
                        } else {
                            if (((r.getLength() == 1) && ((r.getSymbol(0).getName().compareTo(tst.getName()) == 0))))
                                again = true;
                            else {
                                for (k = 0; ; k++) {
                                    if ((smb = r.getSymbol(k)) == null)
                                        break;
                                    rln.addSymbol(smb);
                                }
                                addRule(rln);
                            }
                        }
                    }
                }
            }
            if (cnt-- < 0)
                break;
        } while (again);
        for (i = 0; i < ss; i++) {
            if ((s = symbSet.getSymbol(i)) != startNT)
                if (s.getType() > 0) s.setType(-2);
        }
        nod[startNT.getId()] = 0;
        startNT.setType(3);

        outside:
        for (again = false, i = 0; i < gCopy.size(); i++) {
            ntr = (ArrayList) gCopy.get(i);
            for (j = 0; j < ntr.size(); j++) {
                if ((r = (Rule) ntr.get(j)) != null) {
                    r.setSelSet(null);
                    if ((s = r.getLeftPart()) != null) {
                        if ((s != null) && (s.getType() == -2)) s.setType(1);
                        if (nod[s.getId()] == 0) {
                            for (k = 0; ; k++) {
                                if ((s = r.getSymbol(k)) == null) break;
                                if (s.getId() < nod.length)
                                    if (nod[s.getId()] != 0) {
                                        nod[s.getId()] = 0;
                                        again = true;
                                    }
                            }
                        }
                    }
                }
            }
            if (again) {
                i = -1;
                again = false;
                continue outside;
            }
        }
        for (i = 0; i < nod.length; i++)
            if (nod[i] > 0) {
                if (((s = symbSet.getSymbol(i)) != null) && (s.getType() > -4)) {
                    if (rezU.length() > 0) rezU.append(", ");
                    rezU.append(s.getName());
                    rezU.append(" :n");
                    cntNod += 1;
                }
            }

        for (i = 0; i < gCopy.size(); i++) {
            ntr = (ArrayList) gCopy.get(i);
            if ((r = (Rule) ntr.get(0)) != null) {
                s = r.getLeftPart();
                if ((s == null) || (nod[s.getId()] > 0)) {
                    cntDelRules += ntr.size();
                    gCopy.remove(i);
                    rules.remove(i);
                    i -= 1;
                    continue;
                }
            }
        }


        outnoch:
        for (again = false, i = 0; i < gCopy.size(); i++) {
            ntr = (ArrayList) gCopy.get(i);
            for (j = 0; j < ntr.size(); j++) {
                if ((r = (Rule) ntr.get(j)) == null) continue;
                for (again = true, k = 0; ; k++) {
                    if ((s = r.getSymbol(k)) == null) break;
                    if (s.getType() > 0) {
                        again = false;
                        break;
                    }
                }
                if (again) {
                    s = r.getLeftPart();
                    if ((s != null) && (s.getType() > 0) && (und[s.getId()] == 1)) {
                        und[s.getId()] = 0;
                        ii = 0;
                        ArrayList rs;
                        Symbol ls;
                        woutside:
                        while (ii < gCopy.size()) {
                            if ((rs = (ArrayList) gCopy.get(ii)) != null) {
                                if ((ls = ((Rule) rs.get(0)).getLeftPart()) != null) {
                                    if (s == ls) {
                                        gCopy.remove(ii);
                                        continue woutside;
                                    }
                                    Rule curr;
                                    jj = 0;
                                    while (jj < rs.size()) {
                                        if ((curr = (Rule) rs.get(jj)) != null) {
                                            kk = 0;
                                            while (kk < curr.getLength()) {
                                                if (curr.getSymbol(kk) == s) {
                                                    curr.deleteSymbol(kk);
                                                } else kk += 1;
                                            }
                                        }
                                        jj += 1;
                                    }
                                }
                            }
                            ii += 1;
                        }
                        i = -1;
                        again = false;
                        continue outnoch;
                    }
                }
            }
        }
        for (i = 0; i < und.length; i++)
            if (und[i] > 0) {
//   rezU.append("_"+i+":");
                if (((s = symbSet.getSymbol(i)) != null) && (s.getType() > 0)) {
                    if (rezU.length() > 0) rezU.append(", ");
                    rezU.append(s.getName());
                    rezU.append(" :u");
                    cntUnd += 1;
                }
            }

        i = 0;
        k = -1;
        while (i < rules.size()) {
            ntr = (ArrayList) rules.get(i);
            if (ntr != null) {
                s = ((Rule) ntr.get(0)).getLeftPart();
                if ((s == null) || ((s.getType() & 1) != 1) || (und[s.getId()] == 1)) {
                    cntDelRules += ntr.size();
                    rules.remove(i);
                    continue;
                }
            }
            i += 1;
        }
        if ((startNT == null) && (rules.size() > 0)) {
            ntr = (ArrayList) rules.get(0);
            if (ntr.size() > 0) startNT = ((Rule) ntr.get(0)).getLeftPart();
        }

        i = 0;
        while (i < symbSet.getSize()) {
            s = symbSet.getSymbol(i);
            j = s.getId();
            if ((((j > 0) && (j < nod.length)) && (s.getType() != 0) && ((nod[j] > 0) && (s.getType() > -4))) || ((j > 0) && (j < und.length) && (und[j] > 0) && (s.getType() > 0))) {
//ti.put(j,nod[j],und[j],s.getType(),s.getName());
                symbSet.deleteSymbol(i);
            } else
                i += 1;
        }

        gCopy = getRulesCopy(-4);
        ann:
        for (again = false, i = 0; i < gCopy.size(); i++) {
            ntr = (ArrayList) gCopy.get(i);
            for (j = 0; j < ntr.size(); j++) {
                if ((r = (Rule) ntr.get(j)) != null) {
                    s = r.getLeftPart();
                    if ((s != null) && ((s.getType() & 1) != 0)) {
                        if ((r.getLength() == 0) || ((r.getLength() == 1) && (r.getSymbol(0).getType() == 0))) {
                            s.setType(s.getType() | 4);
                            for (ii = 0; ii < gCopy.size(); ii++) {
                                ArrayList rs = (ArrayList) gCopy.get(ii);
                                for (jj = 0; jj < rs.size(); jj++) {
                                    Rule rl = (Rule) rs.get(jj);
                                    if (rl.getLeftPart() == s) {
                                        gCopy.remove(ii);
                                        ii -= 1;
                                        break;
                                    } else {
                                        for (kk = 0; ; kk += 1) {
                                            if (rl.getSymbol(kk) == null) break;
                                            else if (rl.getSymbol(kk) == s) {
                                                rl.deleteSymbol(kk);
                                                kk -= 1;
                                            }
                                        }
                                    }
                                }
                            }
                            i = -1;
                            continue ann;
                        }
                    }
                }
            }
        }
        startNT.setId(0);
        cntA = symbSet.getSize();
        k = 1;
        for (i = 0; i < cntA; i++)
            if (((s = symbSet.getSymbol(i)).getType() > 0) && (s != startNT))
                s.setId(k++);
        cntN = k;
        for (i = 0; i < cntA; i++) {
            if ((s = symbSet.getSymbol(i)).getType() == -1)
                s.setId(k++);
        }
        for (i = 0; i < cntA; i++)
            if ((s = symbSet.getSymbol(i)).getType() == -2)
                s.setId(k++);
        for (i = 0; i < cntA; i++)
            if ((s = symbSet.getSymbol(i)).getType() == -3)
                s.setId(k++);
        for (i = 0; i < cntA; i++)
            if ((s = symbSet.getSymbol(i)).getType() == 0)
                if (s.getName().length() == 0)
                    s.setId(-1);
                else
                    s.setId(k++);
        for (i = 0; i < cntA; i++)
            if ((s = symbSet.getSymbol(i)).getType() == -4)
                s.setId(k++);
        symbSet.sortById();
        for (cntS = 0, i = 0; i < cntA; i++)
            if (((k = (symbSet.getSymbol(i)).getType()) > -4) && (k < 0)) cntS += 1;
        k = 0;
        for (i = 0; i < cntA; i++)
            if ((j = symbSet.getSymbol(i).getId()) > k) k = j;
        k += 1;
        rr = new int[k][k];
        for (i = 0; i < k; i++)
            for (j = 0; j < k; j++)
                rr[i][j] = 0;
        gCopy = getRulesCopy(-4);
        for (i = 0; i < gCopy.size(); i++) {
            ntr = (ArrayList) gCopy.get(i);
            for (j = 0; j < ntr.size(); j++) {
                if ((r = (Rule) ntr.get(j)) == null) continue;
                s = r.getLeftPart();
                if ((s != null) && ((s.getType() & 1) != 0)) {
                    ii = s.getId();
                    for (ss = 0; ii < k; ss++) {
                        if ((s = r.getSymbol(ss)) == null) break;
                        if (((jj = s.getType()) != 0) && (s.getId() < k))
                            rr[ii][s.getId()] = 1;
                        if ((jj <= 0) || ((jj > 0) && ((jj & 4) == 0))) break;
                    }
                }
            }
        }
        for (i = 0; i < k; i++)
            if (rr[i][i] != 0) {
                s = (Symbol) symbSet.getSymbol(i);
                if (s.getType() > 0) s.setType(s.getType() | 8);
            }
        tranz(rr);
        for (i = 0; i < k; i++)
            if (rr[i][i] != 0) {
                s = (Symbol) symbSet.getSymbol(i);
                if ((s.getType() & 8) == 0)
                    if (s.getType() > 0) s.setType(s.getType() | 24);
            }

        for (i = 0; i < k; i++)
            for (j = 0; j < k; j++)
                rr[i][j] = 0;
        for (i = 0; i < gCopy.size(); i++) {
            ntr = (ArrayList) gCopy.get(i);
            for (j = 0; j < ntr.size(); j++) {
                if ((r = (Rule) ntr.get(j)) == null) continue;
                s = r.getLeftPart();
                if ((s != null) && ((s.getType() & 1) != 0)) {
                    ii = s.getId();
                    for (ss = r.getLength() - 1; ss >= 0; ss--) {
                        if ((s = r.getSymbol(ss)) == null) break;
                        if (((jj = s.getType()) != 0) && (s.getId() < k))
                            rr[ii][s.getId()] = 1;
                        if ((jj <= 0) || ((jj > 0) && ((jj & 4) == 0))) break;
                    }
                }
            }
        }
        for (i = 0; i < k; i++)
            if (rr[i][i] != 0) {
                s = (Symbol) symbSet.getSymbol(i);
                s.setType(s.getType() | 32);
            }
        tranz(rr);
        for (i = 0; i < k; i++)
            if (rr[i][i] != 0) {
                s = (Symbol) symbSet.getSymbol(i);
                if ((s.getType() & 32) == 0)
                    s.setType(s.getType() | 96);
            }
        for (i = 0; i < k; i++)
            for (j = 0; j < k; j++)
                rr[i][j] = 0;
        for (i = 0; i < gCopy.size(); i++) {
            ntr = (ArrayList) gCopy.get(i);
            for (j = 0; j < ntr.size(); j++) {
                if ((r = (Rule) ntr.get(j)) == null) continue;
                s = r.getLeftPart();
                if ((s != null) && ((s.getType() & 1) != 0)) {
                    ii = s.getId();
                    for (ss = 0; ss < r.getLength(); ss++) {
                        if ((s = r.getSymbol(ss)) == null) break;
                        if (((jj = s.getType()) != 0) && (s.getId() < k))
                            rr[ii][s.getId()] = 1;
                    }
                }
            }
        }
        for (i = 0; i < k; i++)
            if (rr[i][i] != 0) {
                s = (Symbol) symbSet.getSymbol(i);
                if ((s.getType() & 120) == 0)
                    s.setType(s.getType() | 128);
            }
        tranz(rr);
        for (i = 0; i < k; i++)
            if (rr[i][i] != 0) {
                s = (Symbol) symbSet.getSymbol(i);
                if ((s.getType() & 248) == 0)
                    s.setType(s.getType() | 384);
            }

        k = cntA;
        rr = new int[k][k];
        for (i = 0; i < k; i++)
            for (j = 0; j < k; j++)
                if (i == j) rr[i][j] = 1;
                else rr[i][j] = 0;
        for (i = 0; i < gCopy.size(); i++) {
            ntr = (ArrayList) gCopy.get(i);
            for (j = 0; j < ntr.size(); j++) {
                if ((r = (Rule) ntr.get(j)) == null) continue;
                s = r.getLeftPart();
                if (s != null) {    //&&((s.getType()&1)!=0)){
                    ii = s.getId();
                    for (ss = 0; ss < r.getLength(); ss++) {
                        if ((s = r.getSymbol(ss)) != null) {
                            if (((jj = s.getType()) != 0) && (jj > -4) && (s.getId() < k))
                                rr[ii][s.getId()] = 1;
                            if (((jj < 0) && (jj > -4)) || ((jj > 0) && ((jj & 4) == 0))) break;
                        }
                    }
                }
            }
        }
        tranz(rr);
        for (i = 0; i < cntN; i++) {
            strt = new ArrayList();
            for (j = cntN; j < cntA; j++) {
                if (rr[i][j] > 0) strt.add(symbSet.getSymbol(j));
            }
            St.add(strt);
        }

        k = cntA;
        rr = new int[k][k];
        rx = new int[k][k];
        rz = new int[k][k];
        for (i = 0; i < k; i++)
            for (j = 0; j < k; j++) {
                if (i == j) rr[i][j] = 1;
                else rr[i][j] = 0;
                rx[i][j] = 0;
                rz[i][j] = 0;
            }

        for (i = 0; i < gCopy.size(); i++) {
            ntr = (ArrayList) gCopy.get(i);
            for (j = 0; j < ntr.size(); j++) {
                if ((r = (Rule) ntr.get(j)) == null) continue;
                s = r.getLeftPart();
                ss = s.getId();
                if ((ss >= 0) && (ss < cntA))
                    for (ii = r.getLength() - 1; (ii >= 0) && (ss >= 0); ii -= 1) {
                        s = r.getSymbol(ii);
                        if ((kk = s.getId()) < 0) break;
                        if ((kk < cntA) && ((jj = s.getType()) != 0) && (jj > -4)) {
                            rr[ss][kk] = 1;
                            if ((jj < 0) || ((jj > 0) && ((jj & 4) == 0))) break;
                        }
                    }
            }
        }
        tranz(rr);

        rx[0][cntN] = 1;
        for (i = 0; i < gCopy.size(); i++) {
            ntr = (ArrayList) gCopy.get(i);
            for (j = 0; j < ntr.size(); j++) {
                if ((r = (Rule) ntr.get(j)) == null) continue;
                for (ii = 0; ii < r.getLength() - 1; ii++) {
                    s = r.getSymbol(ii);
                    ss = s.getId();
                    if ((ss >= 0) && (ss < cntA))
                        for (jj = ii + 1; jj < r.getLength(); jj++) {
                            s = r.getSymbol(jj);
                            if (((kk = s.getId()) >= 0) && (kk < cntA) && (ss >= 0) && (ss < cntA)) rx[ss][kk] = 1;
                            kk = s.getType();
                            if ((kk < 0) || ((kk > 0) && ((kk & 4) == 0))) break;
                        }
                }
            }
        }
        for (i = 0; i < k; i++)
            for (j = 0; j < k; j++)
                for (ii = 0; ii < k; ii++)
                    if ((rx[ii][i] > 0) && (rr[ii][j] > 0)) {
                        rz[j][i] = 1;
                        break;
                    }
        for (i = 0; i < cntN; i++) {
            strt = new ArrayList();
            for (j = 0; j < k; j++) {
                if (rz[i][j] > 0) {
                    s = symbSet.getSymbol(j);
                    if (s.getType() < 0) {
                        for (ii = 0; ii < strt.size(); ii++)
                            if (s == (Symbol) strt.get(ii)) {
                                ii = -2;
                                break;
                            }
                        if (ii >= 0) strt.add(s);
                    } else if ((s.getType() > 0) && (s.getId() >= 0)) {
                        ntr = (ArrayList) St.get(s.getId());
                        for (jj = 0; jj < ntr.size(); jj++) {
                            s = (Symbol) ntr.get(jj);
                            for (ii = 0; ii < strt.size(); ii++)
                                if (s == (Symbol) strt.get(ii)) {
                                    ii = -2;
                                    break;
                                }
                            if (ii >= 0) strt.add(s);
                        }
                    }
                }
            }
            Fl.add(strt);
        }

        return rx;
    }


    public ArrayList getStartRel() {
        if (St.size() == 0) calcProperties();
        return St;
    }

    public ArrayList getFollowRel() {
        if (Fl.size() == 0) calcProperties();
        return Fl;
    }

    public ArrayList<Symbol> getSelSet(Rule r) {
        ArrayList ss = null, po, es;
        String en;
        Symbol symb;
        int i, j, typ = 0, tp;
        if ((r == null) || ((ss = r.getSelSet()) != null)) return ss;
        ss = new ArrayList();
        es = new ArrayList();
        for (i = 0; i < r.getLength(); i++) {
            if ((symb = r.getSymbol(i)) != null) {
                tp = symb.getType();
//    if(()==0)break;
                if (tp > 0) {
                    if (((j = symb.getId()) < 0) || (j >= St.size())) break;
                    typ = tp;
                    po = (ArrayList) St.get(symb.getId());
                    for (j = 0; j < po.size(); j++)
                        if (ss.indexOf(symb = (Symbol) po.get(j)) < 0) ss.add(symb);
                    if ((typ & 4) == 0) break;
                } else if ((tp > -4) && (tp != 0)) {
                    typ = tp;
                    if (ss.indexOf(symb) < 0) ss.add(symb);
                    break;
                } else if (tp == -4) {
                    en = symb.getName();
                    for (j = cntN; j < cntN + cntS; j++)
                        if (en.compareTo((symb = symbSet.getSymbol(j)).getName()) == 0) {
                            es.add(symb);
                            break;
                        }
                }
            }
        }
        if ((typ == 0) || ((typ > 0) && ((typ & 4) != 0))) {
            symb = r.getLeftPart();
            po = (ArrayList) Fl.get(symb.getId());
            for (i = 0; i < po.size(); i++)
                if (ss.indexOf(symb = (Symbol) po.get(i)) < 0) ss.add(symb);
        }
//if(es.size()>0)
//ti.put(es.size(),ss.size(),0,0);
        for (j = 0; j < es.size(); j++) {
            en = ((Symbol) es.get(j)).getName();
            for (i = 0; i < ss.size(); i++)
                if (en.compareTo(((Symbol) ss.get(i)).getName()) == 0) {
                    ss.remove(i);
                    break;
                }
        }
        r.setSelSet(ss);
        return ss;
    }

    public boolean testLL1() {
        int i, j, k, ii, rcnt;
        ArrayList gCopy, ntr, selF, selS;
        Rule r;
        Symbol s;
        RSymbol rs;
        gCopy = rules;//getRulesCopy();
        for (i = 0; i < gCopy.size(); i++) {
            ntr = (ArrayList) gCopy.get(i);
            for (j = 0; j < ntr.size() - 1; j++) {
                r = (Rule) ntr.get(j);
                selF = getSelSet(r);
                for (k = j + 1; k < ntr.size(); k++) {
                    r = (Rule) ntr.get(k);
                    selS = getSelSet(r);
                    for (ii = 0; ii < selF.size(); ii++)
                        if (selS.indexOf(s = (Symbol) selF.get(ii)) >= 0) {
                            return false;
                        }
                }
            }
        }
        rcnt = getRulesCount();
        for (i = 0; i < rules.size(); i++) {
            ntr = (ArrayList) rules.get(i);
            for (j = 0; j < ntr.size(); j++) {
                r = (Rule) ntr.get(j);
                if (r.getLength() == 0) r.addSymbol(symbSet.getSymbol(symbSet.getSize() - 1));
                for (k = 0; ; k++) {
                    if ((rs = r.getRSymbol(k)) == null) break;
                    rs.setStateIndex(rcnt++);
                }
            }
        }
        return true;
    }

    public ArrayList getFirstChain(Rule r) {
        ArrayList ss, po;
        Symbol symb;
        int i, j, typ = 0;
        ss = new ArrayList();
        try {    //20.10.2011
            for (i = 0; i < r.getLength(); i++) {
                symb = r.getSymbol(i);
                typ = symb.getType();
                if (typ > 0) {
                    po = (ArrayList) St.get(symb.getId());
                    for (j = 0; j < po.size(); j++)
                        if (ss.indexOf(symb = (Symbol) po.get(j)) < 0) ss.add(symb);
                    if ((typ & 4) == 0) break;
                } else if ((typ > -4) && (typ != 0)) {
                    if (ss.indexOf(symb) < 0) ss.add(symb);
                    break;
                }
            }
        } catch (Exception e) {
            ;
        }    //20.10.2011
        return ss;
    }

    private int baseExists(ArrayList nb) {
        int i, j, p, k, cnt, c;
        ArrayList ob;
        Symbol s;
        Rule r;
        MarkedRule mr;
        for (j = 0; j < confTbl.size(); j++) {
            ob = (ArrayList) confTbl.get(j);
            for (cnt = 0, i = 0; i < ob.size(); i++)
                if ((((MarkedRule) ob.get(i)).getType() & 1) == 0) cnt += 1;
            if (cnt == nb.size()) {
                for (c = cnt, i = 0; i < nb.size(); i++) {
                    mr = (MarkedRule) nb.get(i);
                    p = mr.getMarkerIndex();
                    r = mr.getRule();
                    s = (Symbol) mr.getContext().get(0);
                    for (k = 0; k < ob.size(); k++) {
                        if ((((mr = (MarkedRule) ob.get(k)).getType() & 1) == 0) && (p == mr.getMarkerIndex()) && (r == mr.getRule()) && (s == (Symbol) mr.getContext().get(0)))
                            c -= 1;
                    }
                }
                if (c == 0) return j;
            }
        }
        return -1;
    }

    private void baseClosing(ArrayList nrl) {
        int i, ps, t, j, k;
        MarkedRule mr;
        Rule r, nr;
        Symbol s, ns;
        ArrayList sSet;
        boolean addCont;
        for (i = 0; i < nrl.size(); i++) {
            mr = (MarkedRule) nrl.get(i);
            r = mr.getRule();
            s = null;
            t = 0;
            ps = 0;
            if (r == null) {
                if (mr.getMarkerIndex() == 0) {
                    s = startNT;
                    t = 3;
                }
            } else
                for (ps = mr.getMarkerIndex(); ps < r.getLength(); ps++) {
                    s = r.getSymbol(ps);
                    if (s != null)
                        if (!(((t = s.getType()) == 0) || (t < -3))) break;
                }
            if ((s != null) && (s.getType() > 0)) {
                if (r == null) {
                    sSet = new ArrayList();
                    sSet.add(symbSet.getSymbol(cntN));
                } else {
                    nr = new Rule(s);
                    addCont = true;
                    for (j = ps + 1; j < r.getLength(); j++) {
                        ns = r.getSymbol(j);
                        if ((((k = ns.getType()) < 0) && (k > -4)) || ((k > 0) && ((k & 4) == 0))) addCont = false;
                        if (k != 0) nr.addSymbol(ns);
                    }
                    sSet = getFirstChain(nr);
                    if (addCont) sSet.add(mr.getContext().get(0));
                }
                j = 0;
                while (j >= 0) {
                    if ((r = getRule(s, j)) == null) break;
                    for (t = 0; t < sSet.size(); t++) {
                        ns = (Symbol) sSet.get(t);
                        for (k = 0; k < nrl.size(); k++) {
                            mr = (MarkedRule) nrl.get(k);
                            if ((mr.getRule() == r) && ((Symbol) mr.getContext().get(0) == ns) && (mr.getMarkerIndex() == 0)) {
                                ns = null;
                                break;
                            }
                        }
                        if (ns != null) {
                            nrl.add(new MarkedRule(0, r, ns, 1));
                        }
                    }
                    j += 1;
                }
            }
        }
    }

    public ArrayList getConfTable() {
        ArrayList mrl, src;
        MarkedRule r;
        Symbol s, ss;
        Rule rul, fict;
        int i, j, p, to, l;
        if (confTbl != null) return confTbl;
        confTbl = new ArrayList();
        src = new ArrayList();
        fict = new Rule(null);
        fict.addSymbol(startNT);
        fict.addSymbol(symbSet.getSymbol(cntN));
        r = new MarkedRule(fict, symbSet.getSymbol(cntN), 0);
        mrl = new ArrayList();
        mrl.add(r);
        baseClosing(mrl);
        confTbl.add(mrl);
        for (i = 0; i < confTbl.size(); i++) {
            mrl = (ArrayList) confTbl.get(i);
            src.clear();
            for (j = 0; j < mrl.size(); j++) {
                r = (MarkedRule) mrl.get(j);
                src.add(new MarkedRule(r.getMarkerIndex(), r.getRule(), (Symbol) r.getContext().get(0), r.getType()));
            }
            while (src.size() > 0) {
                mrl = new ArrayList();
                r = (MarkedRule) src.get(0);
                rul = r.getRule();
                l = rul.getLength();
                j = r.getMarkerIndex();
                if ((s = rul.getSymbol(j)) != null) {
                    while ((s != null) && (s.getType() == 0)) {
                        r.setMarkerIndex(++j);
                        s = rul.getSymbol(j);
                    }
                }
                if (s != null) {
                    j = 0;
                    while (j < src.size()) {
                        r = (MarkedRule) src.get(j);
                        rul = r.getRule();
                        p = r.getMarkerIndex();
                        if ((ss = rul.getSymbol(p)) != null) {
                            if ((ss != null) && (ss.getType() == 0)) {
                                r.setMarkerIndex(++p);
                                ss = rul.getSymbol(p);
                            }
                        }
                        if ((s == ss) || ((ss == null) && (p < rul.getLength()))) {
                            mrl.add(new MarkedRule(p + 1, rul, (Symbol) r.getContext().get(0), 0));
                            src.remove(j);
                        } else j += 1;
                    }
                    to = -1;
                    if ((mrl.size() > 0) && ((to = baseExists(mrl)) < 0)) {
                        if (!(((i == 1) && (s.getId() == cntN)) || (s.getId() == -1))) {
                            to = confTbl.size();
                            baseClosing(mrl);
                            confTbl.add(mrl);
                        }
                    }
                    mrl = (ArrayList) confTbl.get(i);
                    for (j = 0; j < mrl.size(); j++) {
                        r = (MarkedRule) mrl.get(j);
                        rul = r.getRule();
                        if ((ss = rul.getSymbol(p = r.getMarkerIndex())) != null) {
                            if (ss.getType() == 0) ss = rul.getSymbol(p + 1);
                        }
                        if (s == ss) r.setStateTo(to);
                    }
                } else src.remove(0);
            }
            if (confTbl.size() > 100000000) break;
        }
        return confTbl;
    }

    public ArrayList getUniConfTbl() {
        int f, i, j, k, l, n, m, c, p, t, u;
        ArrayList mrl, mrp, ss, sx;
        MarkedRule mr = null, mx, mt;
        Rule r;
        Symbol s;
        if (confTbl == null) getConfTable();
        if (confUTbl != null) return confUTbl;
        confUTbl = new ArrayList();
        l = confTbl.size();
        n = cntA - 1;
        for (i = 0; i < l; i++) {
            mrl = (ArrayList) confTbl.get(i);
            mrp = new ArrayList();
            for (j = 0; j < mrl.size(); j++) {
                mr = (MarkedRule) mrl.get(j);
                mrp.add(new MarkedRule(mr.getMarkerIndex(), mr.getRule(), (Symbol) mr.getContext().get(0), mr.getType()));
                ((MarkedRule) mrp.get(mrp.size() - 1)).setStateTo(mr.getStateTo());
            }
            confUTbl.add(mrp);
        }
        for (i = 0; i < l; i++) {
            mrl = (ArrayList) confUTbl.get(i);
            for (j = 0; j < mrl.size(); j++) {
                mr = (MarkedRule) mrl.get(j);
                r = (Rule) mr.getRule();
                ss = mr.getContext();
                k = j + 1;
                while (k < mrl.size()) {
                    mx = (MarkedRule) mrl.get(k);
                    if ((r == (Rule) mx.getRule()) && (mr.getMarkerIndex() == mx.getMarkerIndex())) {
                        if (ss.indexOf(mx.getContext().get(0)) < 0) ss.add(mx.getContext().get(0));
                        mrl.remove(k);
                    } else k += 1;
                }
            }
        }
        unistate:
        for (i = 0; i < confUTbl.size(); i++) {
            mrl = (ArrayList) confUTbl.get(i);
            for (j = i + 1; j < confUTbl.size(); j++) {
                mrp = (ArrayList) confUTbl.get(j);
                if (mrl.size() == mrp.size()) {
                    for (c = mrl.size(), k = 0; k < mrl.size(); k++) {
                        mr = (MarkedRule) mrl.get(k);
                        for (n = 0; n < mrp.size(); n++) {
                            mx = (MarkedRule) mrp.get(n);
                            if ((mr.getRule() == mx.getRule()) && (mr.getMarkerIndex() == mx.getMarkerIndex())) {
                                p = mx.getType() & 2;
                                sx = mx.getContext();
                                f = 1;
                                for (t = 0; t < sx.size(); t++) {
                                    s = (Symbol) sx.get(t);
                                    for (u = 0; u < mrl.size(); u++) {
                                        if ((mt = (MarkedRule) mrl.get(u)).getContext().indexOf(s) >= 0) {
                                            if (mt.getRule() != mx.getRule())
                                                if ((p == 2) && ((mt.getType() & 2) == 2)) {
                                                    f = 0;
                                                    break;
                                                }
                                        }
                                    }
                                }
                                c -= f;
                                break;
                            }
                        }
                    }
                    if (c == 0) {
                        for (k = 0; k < mrl.size(); k++) {
                            mr = (MarkedRule) mrl.get(k);
                            for (n = 0; n < mrp.size(); n++)
                                if (mr.getRule() == ((MarkedRule) mrp.get(n)).getRule()) {
                                    ss = mr.getContext();
                                    sx = ((MarkedRule) mrp.get(n)).getContext();
                                    for (m = 0; m < sx.size(); m++)
                                        if (ss.indexOf(sx.get(m)) < 0) ss.add(sx.get(m));
                                }
                        }
                        confUTbl.remove(j);
                        for (n = 0; n < confUTbl.size(); n++) {
                            mrp = (ArrayList) confUTbl.get(n);
                            for (k = 0; k < mrp.size(); k++) {
                                mx = (MarkedRule) mrp.get(k);
                                if ((c = mx.getStateTo()) == j) mx.setStateTo(i);
                                if (c > j) mx.setStateTo(c - 1);
                            }
                        }
                        i = 0;
                        continue unistate;
                    }
                }
            }
        }
        return confUTbl;
    }

    public String[][] getLRTable() {
        int f, i, j, k, l, n, m, c, p, t, u, nd = 0, aCnt = 0;
        boolean e;
        ArrayList mrl, mrp, ss, sx;
        MarkedRule mr = null, mx, mt;
        Rule r, nr;
        Symbol s;
        String[][] lrt;
        String[][] lrx;
        String sa, sb, wa, rab;
        StringBuffer xx = null;
        actions = new ArrayList();

        if (confUTbl == null) getUniConfTbl();
        l = confUTbl.size();
        n = cntA - 1;
        if (l <= 0) return null;

        int cntX = cntN + cntS;
        int lrtyp;
        lrt = new String[confUTbl.size()][cntX];
        for (i = 0; i < confUTbl.size(); i++) {
            lrtyp = 0;
            mrl = (ArrayList) confUTbl.get(i);
            for (j = 0; j < mrl.size(); j++) {
                mr = (MarkedRule) mrl.get(j);
                r = (Rule) mr.getRule();
                k = mr.getMarkerIndex();
                sx = new ArrayList();
                while (k >= 0) {
                    if (k < r.getLength()) {
                        s = r.getSymbol(k);
                        if (s != null) {
                            if (s.getType() == -4) {
                                for (p = cntN; p < cntX; p++) {
                                    if (((Symbol) symbSet.getSymbol(p)).getName().compareTo(s.getName()) == 0) {
                                        sx.add(symbSet.getSymbol(p));
                                        break;
                                    }
                                }
                            } else if (s.getType() != 0) break;
                        }
                        k += 1;
                    } else break;
                }
                k = mr.getMarkerIndex();
                wa = "";
                while (k >= 0) {
                    if (k < r.getLength()) {
                        s = r.getSymbol(k);
                        if (s == null) break;
                        if ((s.getId() >= 0) && (s.getId() < cntX)) {
                            if (s.getType() > 0) sb = "G" + mr.getStateTo() + " ";
                            else {
                                sb = "S" + mr.getStateTo() + " ";
                                lrtyp |= 1;
                            }
                            if ((sa = lrt[i][(m = s.getId())]) == null)
                                lrt[i][m] = wa + sb;
                            else if (sa.compareTo(sb) != 0) {
                                if (sb.length() > 0) {
                                    xx = new StringBuffer(sa);
                                    while ((xx.length() > 0) && ((nd = xx.indexOf(sb)) >= 0)) {
                                        if (nd > 0) xx.delete(0, nd);
                                        if (((nd = sb.length()) == xx.length()) || ((nd < xx.length()) && (nd > 0) && (xx.charAt(nd - 1) == ' '))) {
                                            sb = "";
                                            break;
                                        }
                                        if ((nd = xx.indexOf(" ")) < 0) nd = xx.length();
                                        xx.delete(0, nd);
                                    }
                                }
                                if (wa.length() > 0) {
                                    xx = new StringBuffer(sa);
                                    while ((xx.length() > 0) && ((nd = xx.indexOf(wa)) >= 0)) {
                                        if (nd > 0) xx.delete(0, nd);
                                        if (((nd = wa.length()) == xx.length()) || ((nd < xx.length()) && (nd > 0) && (xx.charAt(nd - 1) == ' '))) {
                                            wa = "";
                                            break;
                                        }
                                        if ((nd = xx.indexOf(" ")) < 0) nd = xx.length();
                                        xx.delete(0, nd);
                                    }
                                }
                                lrt[i][m] = wa + sa + " " + sb;
                            }
                            break;
                        } else {
                            if ((s.getType() == 0) && ((rab = s.getName()).length() > 0)) {
                                aCnt = actions.size();
                                for (t = 0; t < aCnt; t++)
                                    if (rab.compareTo((String) actions.get(t)) == 0) {
                                        wa = "A" + t + " ";
                                        break;
                                    }
                                if (wa.length() == 0) {
                                    actions.add(rab);
                                    wa = "A" + aCnt + " ";
                                }
                            }
                            k += 1;
                        }
                    } else {
                        ss = mr.getContext();
                        for (p = 0; p < ss.size(); p++)
                            if (((m = (s = (Symbol) ss.get(p)).getId()) < cntX) && (sx.indexOf(s) < 0)) {
                                sb = "R" + r.getLengthWOActions() + "," + r.getLeftPart().getId() + " ";
                                lrtyp |= 2;
                                if ((sa = lrt[i][m]) == null)
                                    lrt[i][m] = wa + sb;
                                else {
                                    if (sa.compareTo(sb) != 0) {
                                        if (sb.length() > 0) {
                                            xx = new StringBuffer(sa);
                                            while ((xx.length() > 0) && ((nd = xx.indexOf(sb)) >= 0)) {
                                                if (nd > 0) xx.delete(0, nd);
                                                if (((nd = sb.length()) == xx.length()) || ((nd < xx.length()) && (nd > 0) && (xx.charAt(nd - 1) == ' '))) {
                                                    sb = "";
                                                    break;
                                                }
                                                if ((nd = xx.indexOf(" ")) < 0) nd = xx.length();
                                                xx.delete(0, nd);
                                            }
                                        }
                                        if (wa.length() > 0) {
                                            xx = new StringBuffer(sa);
                                            while ((xx.length() > 0) && ((nd = xx.indexOf(wa)) >= 0)) {
                                                if (nd > 0) xx.delete(0, nd);
                                                if (((nd = wa.length()) == xx.length()) || ((nd < xx.length()) && (nd > 0) && (xx.charAt(nd - 1) == ' '))) {
                                                    wa = "";
                                                    break;
                                                }
                                                if ((nd = xx.indexOf(" ")) < 0) nd = xx.length();
                                                xx.delete(0, nd);
                                            }
                                        }
                                        if ((sb.length() > 0) && ((sa.indexOf("R") >= 0) || (sa.indexOf("S") >= 0)))
                                            lrType |= 8;
                                        lrt[i][m] = sa + " " + wa + sb;
                                    }
                                }
                            }
                        break;
                    }
                }
            }
            if (lrtyp == 3) lrType |= 2;
        }

        n = lrt.length;
        m = lrt[0].length;
        newsearch:
        for (i = 0; i < n; i++) {
            for (j = i + 1; j < n; j++) {
                for (f = 1, k = 0; k < m; k++) {
                    sa = lrt[i][k];
                    sb = lrt[j][k];
                    if (!(((sa == null) && (sb == null)) || ((sa != null) && (sb != null) && (sa.compareTo(sb) == 0)))) {
                        f = 0;
                        break;
                    }
                }
                if (f == 1) {
                    for (p = j, k = j + 1; k < n; p++, k++)
                        for (l = 0; l < m; l++)
                            lrt[p][l] = lrt[k][l];
                    sa = "G" + j + " ";
                    sb = "S" + j + " ";
                    t = -1;
                    for (k = 0; k < n; k++)
                        for (l = 0; l < m; l++)
                            if ((wa = lrt[k][l]) != null) {
                                if (t < 0) t = k - 1;
                                if ((p = wa.indexOf(sa)) >= 0) {
                                    rab = "";
                                    if (p > 0) rab = wa.substring(0, p);
                                    rab += "G" + i + " ";
                                    p += sa.length();
                                    if (p < wa.length()) rab += wa.substring(p);
                                    lrt[k][l] = rab;
                                }
                                if ((p = wa.indexOf(sb)) >= 0) {
                                    rab = "";
                                    if (p > 0) rab = wa.substring(0, p);
                                    rab += "S" + i + " ";
                                    p += sb.length();
                                    if (p < wa.length()) rab += wa.substring(p);
                                    lrt[k][l] = rab;
                                }
                                if ((p = wa.indexOf("G")) < 0) p = wa.indexOf("S");
                                if (p >= 0) {
                                    rab = wa.substring(0, p + 1);
                                    wa = wa.substring(p + 1);
                                    if ((p = wa.indexOf(" ")) < 0) p = wa.length();
                                    nd = Integer.parseInt(wa.substring(0, p));
                                    if (nd > j) {
                                        lrt[k][l] = rab + (nd - 1) + (p >= wa.length() ? " " : wa.substring(p));
                                    }
                                }
                            }
                    n -= 1;
                    j -= 1;
                    if (t >= 0) {
                        i = t;
                        continue newsearch;
                    }
                }
            }
        }
        if (lrt.length == n)
            return lrt;
        else {
            lrx = new String[n][cntX];
            for (i = 0; i < n; i++)
                for (j = 0; j < cntX; j++)
                    lrx[i][j] = lrt[i][j];
            return lrx;
        }
    }
}