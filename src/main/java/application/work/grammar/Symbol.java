package application.work.grammar;

import lombok.Getter;
import lombok.Setter;

public class Symbol {
    @Setter
    @Getter
    private int id;
    @Getter
    @Setter
    private int type;
    private final String name;

    public Symbol(int Type, String Name) {
        this.type = Type;
        this.id = -1;
        this.name = Name;
    }

    public int similar(String Name) {
        return (name == null ? (Name == null ? 0 : 1) : (Name == null ? 1 : name.compareTo(Name)));
    }

    public int similar(String Name, int Type) {
        return (name == null ? (Name == null ? 0 : 1) : (Name == null ? 1 : name.compareTo(Name)))
                | ((type > 0 ? 1 : type) == (Type > 0 ? 1 : Type) ? 0 : 1);
    }

    public String getName() {
        return (name == null ? "" : name);
    }

}
