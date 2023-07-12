package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2023-7-12
 * @License Apache License Version 2.0
 */
public class DoubleText extends DoubleRectangle {

    private String text;

    public DoubleText() {
    }

    public DoubleText(int x1, int y1, int x2, int y2) {
        super(Integer.MAX_VALUE, Integer.MAX_VALUE, x1, y1, x2, y2);
    }

    /*
        get/set
     */
    public String getText() {
        return text;
    }

    public DoubleText setText(String text) {
        this.text = text;
        return this;
    }

}
