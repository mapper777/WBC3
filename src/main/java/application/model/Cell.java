package application.model;

public class Cell {
    private final String text;      // то, что раньше было celS
    private final String cssClass;  // то, что раньше было cls

    public Cell(String text, String cssClass) {
        this.text = text;
        this.cssClass = cssClass;
    }
    public String getText()     { return text; }
    public String getCssClass() { return cssClass; }
}