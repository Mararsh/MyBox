package mara.mybox.image;

import java.awt.Color;

/**
 * @Author Mara
 * @CreateDate 2019-10-19
 * @Version 1.0
 * @License Apache License Version 2.0
 */
public class ImagePixel {

    protected int x, y;
    protected Color color;

    public static ImagePixel create(int x, int y, Color color) {
        return new ImagePixel().setX(x).setY(y).setColor(color);
    }

    public static ImagePixel create(Color color) {
        return new ImagePixel().setColor(color);
    }

    public int getX() {
        return x;
    }

    public ImagePixel setX(int x) {
        this.x = x;
        return this;
    }

    public int getY() {
        return y;
    }

    public ImagePixel setY(int y) {
        this.y = y;
        return this;
    }

    public Color getColor() {
        return color;
    }

    public ImagePixel setColor(Color color) {
        this.color = color;
        return this;
    }

}
