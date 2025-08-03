package application.work.finiteAutomat;

public class FA_Row {
    String mark;
    String ranges;
    int[] to;

    public FA_Row(int states, String mrk) {
        mark = mrk;
        to = new int[states];
    }
}