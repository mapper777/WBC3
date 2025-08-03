package application.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Option {
    private String id;
    private String label;
    private boolean disabled;
    private boolean selected;

    public Option(String id, String label, boolean disabled, boolean selected) {
        this.id = id;
        this.label = label;
        this.disabled = disabled;
        this.selected = selected;
    }
}
