package gsprod;

public class Transition {

    private String source;
    private String label;
    private String target;

    public Transition(String source, String label, String target) {
        this.source = source;
        this.label = label;
        this.target = target;
    }

    public String getSource() {
        return this.source;
    }

    public String getLabel() {
        return this.label;
    }

    public String getTarget() {
        return this.target;
    }

    public String toString() {
        return String.format("%s -(%s)-> %s", this.source, this.label, this.target);
    }
}