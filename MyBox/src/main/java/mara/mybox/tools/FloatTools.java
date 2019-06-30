package mara.mybox.tools;

/**
 * @Author Mara
 * @CreateDate 2019-5-28 15:31:28
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FloatTools {

    public static float roundFloat2(float fvalue) {
        return (float) DoubleTools.scale(fvalue, 2);
    }

    public static float roundFloat5(float fvalue) {
        return (float) DoubleTools.scale(fvalue, 5);
    }

}
