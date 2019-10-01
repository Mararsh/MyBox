package mara.mybox.color;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Mara
 * @CreateDate 2019-6-7 9:38:03
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class CMYKColorSpace {

    public static enum ColorSpaceType {
        CMY, CMYK
    }

    public static List<String> names() {
        List<String> names = new ArrayList<>();
        for (ColorSpaceType cs : ColorSpaceType.values()) {
            names.add(cs + "");
        }
        return names;
    }

    public static double[] cmy2cmky(double[] cmy) {
        double[] cmyk = new double[4];
        cmyk[0] = cmy[0];
        cmyk[1] = cmy[1];
        cmyk[2] = cmy[2];
        cmyk[3] = 1;
        if (cmyk[0] < cmyk[3]) {
            cmyk[3] = cmyk[0];
        }
        if (cmyk[1] < cmyk[3]) {
            cmyk[3] = cmyk[1];
        }
        if (cmyk[2] < cmyk[3]) {
            cmyk[3] = cmyk[2];
        }
        if (cmyk[3] == 1) {
            cmyk[0] = 0;
            cmyk[1] = 0;
            cmyk[2] = 0;
        } else {
            cmyk[0] = (cmyk[0] - cmyk[3]) / (1 - cmyk[3]);
            cmyk[1] = (cmyk[1] - cmyk[3]) / (1 - cmyk[3]);
            cmyk[2] = (cmyk[2] - cmyk[3]) / (1 - cmyk[3]);
        }
        return cmyk;
    }

}
