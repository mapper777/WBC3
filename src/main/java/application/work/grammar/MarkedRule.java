package application.work.grammar;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class MarkedRule {
    private Rule bRule;
    @Getter
    private ArrayList<Symbol> context;
    private int mIndex;
    @Setter
    @Getter
    private int stateTo;
    @Getter
    private int type;

    public MarkedRule(Rule baseRule, Symbol Context, int Type) {
        int i, t;
        context = new ArrayList<>();
        context.add(Context);
        mIndex = 0;
        bRule = baseRule;
        type = Type;
        if (mIndex >= bRule.getLength()) type |= 2;
        stateTo = -1;
    }

    public MarkedRule(int markerIndex, Rule baseRule, Symbol Context, int Type) {
        int i, t;
        context = new ArrayList<>();
        context.add(Context);
        bRule = baseRule;
        type = Type;
        mIndex = (markerIndex > (t = bRule.getLength()) ? t : markerIndex);
        if (mIndex >= bRule.getLength()) type |= 2;
        stateTo = -1;
    }

    public void setContext(Symbol newContext) {
        while (!context.isEmpty()) context.remove(0);
        context.add(newContext);
    }

    public int getMarkerIndex() {
        return mIndex;
    }

    public void setMarkerIndex(int newMarkerIndex) {
        if ((newMarkerIndex >= 0) && (newMarkerIndex < bRule.getLength())) {
            mIndex = newMarkerIndex;
            if (mIndex >= bRule.getLength()) type |= 2;
        }
    }

    public Rule getRule() {
        return bRule;
    }
}