package application.work.grammar;

import lombok.Getter;
import lombok.Setter;

public class RSymbol {
    private final Symbol SymboL;
    @Setter
    @Getter
    private int stateIndex;

    public RSymbol(Symbol Symbol) {
        SymboL = Symbol;
        stateIndex = -1;
    }

    public Symbol getSymbol() {
        return SymboL;
    }
}