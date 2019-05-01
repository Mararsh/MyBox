package mara.mybox.data;

/**
 * @param <N>
 * @Author Mara
 * @CreateDate 2019-04-22
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public interface ShapeData<N> {

    ShapeData cloneValues();

    boolean isValid();

    boolean include(N x, N y);

    ShapeData move(N offset);

    ShapeData move(N offsetX, N offsetY);

}
