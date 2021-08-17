package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2019-04-02
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public interface DoubleShape {

    DoubleShape cloneValues();

    boolean isValid();

    boolean include(double x, double y);

    DoubleRectangle getBound();

    DoubleShape move(double offset);

    DoubleShape move(double offsetX, double offsetY);

}
