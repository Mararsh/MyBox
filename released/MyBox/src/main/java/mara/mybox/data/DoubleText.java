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

    public static DoubleText xywh(double x, double y, double width, double height) {
        DoubleText t = new DoubleText();
        t.setX(x);
        t.setY(y);
        t.setWidth(width);
        t.setHeight(height);
        return t;
    }

    @Override
    public boolean isValid() {
        return text != null;
    }

    @Override
    public boolean isEmpty() {
        return !isValid();
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
