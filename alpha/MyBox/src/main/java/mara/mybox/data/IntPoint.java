package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2018-11-11 12:23:02
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class IntPoint {

    private int x, y;

    public IntPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static int distance2(int x1, int y1, int x2, int y2) {
        int distanceX = x1 - x2;
        int distanceY = y1 - y2;
        return distanceX * distanceX + distanceY * distanceY;
    }

    public static int distance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(distance2(x1, y1, x2, y2));
    }

    public static int distance2(IntPoint A, IntPoint B) {
        int distanceX = A.getX() - B.getX();
        int distanceY = A.getY() - B.getY();
        return distanceX * distanceX + distanceY * distanceY;
    }

    public static int distance(IntPoint A, IntPoint B) {
        return (int) Math.sqrt(distance2(A, B));
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

}
