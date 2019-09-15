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

}
