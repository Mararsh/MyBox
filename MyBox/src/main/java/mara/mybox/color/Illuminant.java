package mara.mybox.color;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.tools.MatrixDoubleTools;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-5-21 12:09:26
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 *
 * Reference: * http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html
 * http://brucelindbloom.com/index.html?ColorCalculator.html
 * https://ww2.mathworks.cn/help/images/ref/whitepoint.html
 * http://www.thefullwiki.org/Standard_illuminant
 * http://www.wikizero.biz/index.php?q=aHR0cHM6Ly9lbi53aWtpcGVkaWEub3JnL3dpa2kvU3RhbmRhcmRfaWxsdW1pbmFudA
 */
public class Illuminant extends CIEData {

    public IlluminantType type;
    public Observer observer;
    public String name, comments, whitePointValues;
    public int temperature;

    public Illuminant(IlluminantType illuminant) {
        this.type = illuminant;
        init1931(illuminant);
    }

    public Illuminant(IlluminantType illuminant, Observer observer) {
        this.type = illuminant;
        if (observer == null || observer == Observer.CIE1931) {
            init1931(illuminant);
        } else if (observer == Observer.CIE1964) {
            init1964(illuminant);
        }
    }

    public final void init1931(IlluminantType illuminant) {
        this.type = illuminant;
        switch (illuminant) {
            case A:
                init("A", Observer.CIE1931, Illuminant1931A, Illuminant1931A_Temperature, Illuminant1931A_Comments);
                break;
            case B:
                init("B", Observer.CIE1931, Illuminant1931B, Illuminant1931B_Temperature, Illuminant1931B_Comments);
                break;
            case C:
                init("C", Observer.CIE1931, Illuminant1931C, Illuminant1931C_Temperature, Illuminant1931C_Comments);
                break;
            case D50:
                init("D50", Observer.CIE1931, Illuminant1931D50, Illuminant1931D50_Temperature, Illuminant1931D50_Comments);
                break;
            case D55:
                init("D55", Observer.CIE1931, Illuminant1931D55, Illuminant1931D55_Temperature, Illuminant1931D55_Comments);
                break;
            case D65:
                init("D65", Observer.CIE1931, Illuminant1931D65, Illuminant1931D65_Temperature, Illuminant1931D65_Comments);
                break;
            case D75:
                init("D75", Observer.CIE1931, Illuminant1931D75, Illuminant1931D75_Temperature, Illuminant1931D75_Comments);
                break;
            case E:
                init("E", Observer.CIE1931, Illuminant1931E, Illuminant1931E_Temperature, Illuminant1931E_Comments);
                break;
            case F1:
                init("F1", Observer.CIE1931, Illuminant1931F1, Illuminant1931F1_Temperature, Illuminant1931F1_Comments);
                break;
            case F2:
                init("F2", Observer.CIE1931, Illuminant1931F2, Illuminant1931F2_Temperature, Illuminant1931F2_Comments);
                break;
            case F3:
                init("F3", Observer.CIE1931, Illuminant1931F3, Illuminant1931F3_Temperature, Illuminant1931F3_Comments);
                break;
            case F4:
                init("F4:", Observer.CIE1931, Illuminant1931F4, Illuminant1931F4_Temperature, Illuminant1931F4_Comments);
                break;
            case F5:
                init("F5", Observer.CIE1931, Illuminant1931F5, Illuminant1931F5_Temperature, Illuminant1931F5_Comments);
                break;
            case F6:
                init("F6", Observer.CIE1931, Illuminant1931F6, Illuminant1931F6_Temperature, Illuminant1931F6_Comments);
                break;
            case F7:
                init("F7", Observer.CIE1931, Illuminant1931F7, Illuminant1931F7_Temperature, Illuminant1931F7_Comments);
                break;
            case F8:
                init("F8", Observer.CIE1931, Illuminant1931F8, Illuminant1931F8_Temperature, Illuminant1931F8_Comments);
                break;
            case F9:
                init("F9", Observer.CIE1931, Illuminant1931F9, Illuminant1931F9_Temperature, Illuminant1931F9_Comments);
                break;
            case F10:
                init("F10", Observer.CIE1931, Illuminant1931F10, Illuminant1931F10_Temperature, Illuminant1931F10_Comments);
                break;
            case F11:
                init("F11", Observer.CIE1931, Illuminant1931F11, Illuminant1931F11_Temperature, Illuminant1931F11_Comments);
                break;
            case F12:
                init("F12", Observer.CIE1931, Illuminant1931F12, Illuminant1931F12_Temperature, Illuminant1931F12_Comments);
                break;
            case ICC:
                init("ICC", Observer.CIE1931, IlluminantICC, IlluminantICC_Temperature, IlluminantICC_Comments);
                break;
        }

    }

    public final void init1964(IlluminantType illuminant) {
        this.type = illuminant;
        switch (illuminant) {
            case A:
                init("A", Observer.CIE1964, Illuminant1964A, Illuminant1964A_Temperature, Illuminant1964A_Comments);
                break;
            case B:
                init("B", Observer.CIE1964, Illuminant1964B, Illuminant1964B_Temperature, Illuminant1964B_Comments);
                break;
            case C:
                init("C", Observer.CIE1964, Illuminant1964C, Illuminant1964C_Temperature, Illuminant1964C_Comments);
                break;
            case D50:
                init("D50", Observer.CIE1964, Illuminant1964D50, Illuminant1964D50_Temperature, Illuminant1964D50_Comments);
                break;
            case D55:
                init("D55", Observer.CIE1964, Illuminant1964D55, Illuminant1964D55_Temperature, Illuminant1964D55_Comments);
                break;
            case D65:
                init("D65", Observer.CIE1964, Illuminant1964D65, Illuminant1964D65_Temperature, Illuminant1964D65_Comments);
                break;
            case D75:
                init("D75", Observer.CIE1964, Illuminant1964D75, Illuminant1964D75_Temperature, Illuminant1964D75_Comments);
                break;
            case E:
                init("E", Observer.CIE1964, Illuminant1964E, Illuminant1964E_Temperature, Illuminant1964E_Comments);
                break;
            case F1:
                init("F1", Observer.CIE1964, Illuminant1964F1, Illuminant1964F1_Temperature, Illuminant1964F1_Comments);
                break;
            case F2:
                init("F2", Observer.CIE1964, Illuminant1964F2, Illuminant1964F2_Temperature, Illuminant1964F2_Comments);
                break;
            case F3:
                init("F3", Observer.CIE1964, Illuminant1964F3, Illuminant1964F3_Temperature, Illuminant1964F3_Comments);
                break;
            case F4:
                init("F4:", Observer.CIE1964, Illuminant1964F4, Illuminant1964F4_Temperature, Illuminant1964F4_Comments);
                break;
            case F5:
                init("F5", Observer.CIE1964, Illuminant1964F5, Illuminant1964F5_Temperature, Illuminant1964F5_Comments);
                break;
            case F6:
                init("F6", Observer.CIE1964, Illuminant1964F6, Illuminant1964F6_Temperature, Illuminant1964F6_Comments);
                break;
            case F7:
                init("F7", Observer.CIE1964, Illuminant1964F7, Illuminant1964F7_Temperature, Illuminant1964F7_Comments);
                break;
            case F8:
                init("F8", Observer.CIE1964, Illuminant1964F8, Illuminant1964F8_Temperature, Illuminant1964F8_Comments);
                break;
            case F9:
                init("F9", Observer.CIE1964, Illuminant1964F9, Illuminant1964F9_Temperature, Illuminant1964F9_Comments);
                break;
            case F10:
                init("F10", Observer.CIE1964, Illuminant1964F10, Illuminant1964F10_Temperature, Illuminant1964F10_Comments);
                break;
            case F11:
                init("F11", Observer.CIE1964, Illuminant1964F11, Illuminant1964F11_Temperature, Illuminant1964F11_Comments);
                break;
            case F12:
                init("F12", Observer.CIE1964, Illuminant1964F12, Illuminant1964F12_Temperature, Illuminant1964F12_Comments);
                break;
            case ICC:
                init("ICC", Observer.CIE1964, Illuminant1964D50, IlluminantICC_Temperature, IlluminantICC_Comments);
                break;
        }

    }

    public final void init(String name, Observer observer,
            double[] xy, int temperature, String comments) {
        this.name = name;
        this.observer = observer;
        this.temperature = temperature;
        if (comments != null) {
            this.comments = message(comments);
        }
        setNormalziedXY(xy);
    }

    public double[][] whitePoint() {
        return MatrixDoubleTools.columnVector(relativeX, relativeY, relativeZ);
    }

    /*
        Static methods
     */
    public static List<Illuminant> all() {
        List<Illuminant> data = new ArrayList<>();
        for (IlluminantType type : IlluminantType.values()) {
            data.add(new Illuminant(type, Observer.CIE1931));
            data.add(new Illuminant(type, Observer.CIE1964));
        }
        return data;
    }

    public static List<Illuminant> all(int scale) {
        List<Illuminant> data = all();
        for (Illuminant d : data) {
            d.scaleValues(scale);
        }
        return data;
    }

    public static List<String> names() {
        List<String> data = new ArrayList<>();
        for (IlluminantType type : IlluminantType.values()) {
            data.add(type + " - " + Observer.CIE1931);
            data.add(type + " - " + Observer.CIE1964);
        }
        return data;
    }

    public static String string() {
        StringBuilder s = new StringBuilder();
        String sp = "\t";
        s.append(message("Illuminant")).append(sp).append(message("Observer")).append(sp).
                append(message("NormalizedX")).append(sp).append(message("NormalizedY")).append(sp).append(message("NormalizedZ")).append(sp).
                append(message("RelativeX")).append(sp).append(message("RelativeY")).append(sp).append(message("RelativeZ")).append(sp).
                append(message("ColorTemperature")).append(sp).append(message("Comments")).append("\n");

        List<Illuminant> data = all(8);
        for (Illuminant d : data) {
            s.append(d.name).append(sp).append(d.observer).append(sp).
                    append(d.getNormalizedX()).append(sp).
                    append(d.getNormalizedY()).append(sp).
                    append(d.getNormalizedZ()).append(sp).
                    append(d.getRelativeX()).append(sp).
                    append(d.getRelativeY()).append(sp).
                    append(d.getRelativeZ()).append(sp).
                    append(d.temperature).append("K").append(sp).
                    append(d.comments).append(sp).append("\n");
        }
        return s.toString();
    }

    public static IlluminantType type(String name) {
        switch (name) {
            case "A":
                return IlluminantType.A;
            case "B":
                return IlluminantType.B;
            case "C":
                return IlluminantType.C;
            case "D50":
                return IlluminantType.D50;
            case "D55":
                return IlluminantType.D55;
            case "D65":
                return IlluminantType.D65;
            case "D75":
                return IlluminantType.D75;
            case "E":
                return IlluminantType.E;
            case "F1":
                return IlluminantType.F1;
            case "F2":
                return IlluminantType.F2;
            case "F3":
                return IlluminantType.F3;
            case "F4":
                return IlluminantType.F4;
            case "F5":
                return IlluminantType.F5;
            case "F6":
                return IlluminantType.F6;
            case "F7":
                return IlluminantType.F7;
            case "F8":
                return IlluminantType.F8;
            case "F9":
                return IlluminantType.F9;
            case "F10":
                return IlluminantType.F10;
            case "F11":
                return IlluminantType.F11;
            case "F12":
                return IlluminantType.F12;
            case "ICC":
                return IlluminantType.ICC;
        }
        return null;
    }

    public static double[] whitePoint1931(IlluminantType illuminant) {
        switch (illuminant) {
            case A:
                return Illuminant1931A;
            case B:
                return Illuminant1931B;
            case C:
                return Illuminant1931C;
            case D50:
                return Illuminant1931D50;
            case D55:
                return Illuminant1931D55;
            case D65:
                return Illuminant1931D65;
            case D75:
                return Illuminant1931D65;
            case E:
                return Illuminant1931E;
            case F1:
                return Illuminant1931F1;
            case F2:
                return Illuminant1931F2;
            case F3:
                return Illuminant1931F3;
            case F4:
                return Illuminant1931F4;
            case F5:
                return Illuminant1931F5;
            case F6:
                return Illuminant1931F6;
            case F7:
                return Illuminant1931F7;
            case F8:
                return Illuminant1931F8;
            case F9:
                return Illuminant1931F9;
            case F10:
                return Illuminant1931F10;
            case F11:
                return Illuminant1931F11;
            case F12:
                return Illuminant1931F12;
            case ICC:
                return IlluminantICC;
        }
        return null;
    }

    public static double[] whitePoint1964(IlluminantType illuminant) {
        switch (illuminant) {
            case A:
                return Illuminant1964A;
            case B:
                return Illuminant1964B;
            case C:
                return Illuminant1964C;
            case D50:
                return Illuminant1964D50;
            case D55:
                return Illuminant1964D55;
            case D65:
                return Illuminant1964D65;
            case D75:
                return Illuminant1964D65;
            case E:
                return Illuminant1964E;
            case F1:
                return Illuminant1964F1;
            case F2:
                return Illuminant1964F2;
            case F3:
                return Illuminant1964F3;
            case F4:
                return Illuminant1964F4;
            case F5:
                return Illuminant1964F5;
            case F6:
                return Illuminant1964F6;
            case F7:
                return Illuminant1964F7;
            case F8:
                return Illuminant1964F8;
            case F9:
                return Illuminant1964F9;
            case F10:
                return Illuminant1964F10;
            case F11:
                return Illuminant1964F11;
            case F12:
                return Illuminant1964F12;
            case ICC:
                return Illuminant1964D50;
        }
        return null;
    }

    /*
        Data
     */
    public static enum IlluminantType {
        A, B, C, D50, D55, D65, D75, E, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, ICC
    }

    public static enum Observer {
        CIE1931, CIE1964
    }

    // x, y
    public static String Illuminant1931A_Comments = "IlluminantAComments";
    public static int Illuminant1931A_Temperature = 2856;
    public static double[] Illuminant1931A = {
        0.44757, 0.40745
    };

    public static String Illuminant1931B_Comments = "IlluminantBComments";
    public static int Illuminant1931B_Temperature = 4874;
    public static double[] Illuminant1931B = {
        0.34842, 0.35161
    };

    public static String Illuminant1931C_Comments = "IlluminantCComments";
    public static int Illuminant1931C_Temperature = 6774;
    public static double[] Illuminant1931C = {
        0.31006, 0.31616
    };

    public static String Illuminant1931D50_Comments = "IlluminantD50Comments";
    public static int Illuminant1931D50_Temperature = 5003;
    public static double[] Illuminant1931D50 = {
        0.34567, 0.35850
    };

    public static String Illuminant1931D55_Comments = "IlluminantD55Comments";
    public static int Illuminant1931D55_Temperature = 5503;
    public static double[] Illuminant1931D55 = {
        0.33242, 0.34743
    };

    public static String Illuminant1931D65_Comments = "IlluminantD65Comments";
    public static int Illuminant1931D65_Temperature = 6504;
    public static double[] Illuminant1931D65 = {
        0.31271, 0.32902
    };

    public static String Illuminant1931D75_Comments = "IlluminantD75Comments";
    public static int Illuminant1931D75_Temperature = 7504;
    public static double[] Illuminant1931D75 = {
        0.29902, 0.31485
    };

    public static String Illuminant1931E_Comments = "IlluminantEComments";
    public static int Illuminant1931E_Temperature = 5454;
    public static double[] Illuminant1931E = {
        0.33333, 0.33333
    };

    public static String Illuminant1931F1_Comments = "IlluminantF1Comments";
    public static int Illuminant1931F1_Temperature = 6430;
    public static double[] Illuminant1931F1 = {
        0.31310, 0.33727
    };

    public static String Illuminant1931F2_Comments = "IlluminantF2Comments";
    public static int Illuminant1931F2_Temperature = 4230;
    public static double[] Illuminant1931F2 = {
        0.37208, 0.37529
    };

    public static String Illuminant1931F3_Comments = "IlluminantF3Comments";
    public static int Illuminant1931F3_Temperature = 3450;
    public static double[] Illuminant1931F3 = {
        0.40910, 0.39430
    };

    public static String Illuminant1931F4_Comments = "IlluminantF4Comments";
    public static int Illuminant1931F4_Temperature = 2940;
    public static double[] Illuminant1931F4 = {
        0.44018, 0.40329
    };

    public static String Illuminant1931F5_Comments = "IlluminantF5Comments";
    public static int Illuminant1931F5_Temperature = 6350;
    public static double[] Illuminant1931F5 = {
        0.31379, 0.34531
    };

    public static String Illuminant1931F6_Comments = "IlluminantF6Comments";
    public static int Illuminant1931F6_Temperature = 4150;
    public static double[] Illuminant1931F6 = {
        0.37790, 0.38835
    };

    public static String Illuminant1931F7_Comments = "IlluminantF7Comments";
    public static int Illuminant1931F7_Temperature = 6500;
    public static double[] Illuminant1931F7 = {
        0.31292, 0.32933
    };

    public static String Illuminant1931F8_Comments = "IlluminantF8Comments";
    public static int Illuminant1931F8_Temperature = 5000;
    public static double[] Illuminant1931F8 = {
        0.34588, 0.35875
    };

    public static String Illuminant1931F9_Comments = "IlluminantF9Comments";
    public static int Illuminant1931F9_Temperature = 4150;
    public static double[] Illuminant1931F9 = {
        0.37417, 0.37281
    };

    public static String Illuminant1931F10_Comments = "IlluminantF10Comments";
    public static int Illuminant1931F10_Temperature = 5000;
    public static double[] Illuminant1931F10 = {
        0.34609, 0.35986
    };

    public static String Illuminant1931F11_Comments = "IlluminantF11Comments";
    public static int Illuminant1931F11_Temperature = 4000;
    public static double[] Illuminant1931F11 = {
        0.38052, 0.37713
    };

    public static String Illuminant1931F12_Comments = "IlluminantF12Comments";
    public static int Illuminant1931F12_Temperature = 3000;
    public static double[] Illuminant1931F12 = {
        0.43695, 0.40441
    };

    public static String IlluminantICC_Comments = "IlluminantICCComments";
    public static int IlluminantICC_Temperature = 5003;
    public static double[] IlluminantICC = {
        0.34570, 0.35854
    };

    public static String Illuminant1964A_Comments = "IlluminantAComments";
    public static int Illuminant1964A_Temperature = 2856;
    public static double[] Illuminant1964A = {
        0.45117, 0.40594
    };

    public static String Illuminant1964B_Comments = "IlluminantBComments";
    public static int Illuminant1964B_Temperature = 4874;
    public static double[] Illuminant1964B = {
        0.3498, 0.3527
    };

    public static String Illuminant1964C_Comments = "IlluminantCComments";
    public static int Illuminant1964C_Temperature = 6774;
    public static double[] Illuminant1964C = {
        0.31039, 0.31905
    };

    public static String Illuminant1964D50_Comments = "IlluminantD50Comments";
    public static int Illuminant1964D50_Temperature = 5003;
    public static double[] Illuminant1964D50 = {
        0.34773, 0.35952
    };

    public static String Illuminant1964D55_Comments = "IlluminantD55Comments";
    public static int Illuminant1964D55_Temperature = 5503;
    public static double[] Illuminant1964D55 = {
        0.33411, 0.34877
    };

    public static String Illuminant1964D65_Comments = "IlluminantD65Comments";
    public static int Illuminant1964D65_Temperature = 6504;
    public static double[] Illuminant1964D65 = {
        0.31382, 0.33100
    };

    public static String Illuminant1964D75_Comments = "IlluminantD75Comments";
    public static int Illuminant1964D75_Temperature = 7504;
    public static double[] Illuminant1964D75 = {
        0.29968, 0.31740
    };

    public static String Illuminant1964E_Comments = "IlluminantEComments";
    public static int Illuminant1964E_Temperature = 5454;
    public static double[] Illuminant1964E = {
        0.33333, 0.33333
    };

    public static String Illuminant1964F1_Comments = "IlluminantF1Comments";
    public static int Illuminant1964F1_Temperature = 6430;
    public static double[] Illuminant1964F1 = {
        0.31811, 0.33559
    };

    public static String Illuminant1964F2_Comments = "IlluminantF2Comments";
    public static int Illuminant1964F2_Temperature = 4230;
    public static double[] Illuminant1964F2 = {
        0.37925, 0.36733
    };

    public static String Illuminant1964F3_Comments = "IlluminantF3Comments";
    public static int Illuminant1964F3_Temperature = 3450;
    public static double[] Illuminant1964F3 = {
        0.41761, 0.38324
    };

    public static String Illuminant1964F4_Comments = "IlluminantF4Comments";
    public static int Illuminant1964F4_Temperature = 2940;
    public static double[] Illuminant1964F4 = {
        0.44920, 0.39074
    };

    public static String Illuminant1964F5_Comments = "IlluminantF5Comments";
    public static int Illuminant1964F5_Temperature = 6350;
    public static double[] Illuminant1964F5 = {
        0.31975, 0.34246
    };

    public static String Illuminant1964F6_Comments = "IlluminantF6Comments";
    public static int Illuminant1964F6_Temperature = 4150;
    public static double[] Illuminant1964F6 = {
        0.38660, 0.37847
    };

    public static String Illuminant1964F7_Comments = "IlluminantF7Comments";
    public static int Illuminant1964F7_Temperature = 6500;
    public static double[] Illuminant1964F7 = {
        0.31569, 0.32960
    };

    public static String Illuminant1964F8_Comments = "IlluminantF8Comments";
    public static int Illuminant1964F8_Temperature = 5000;
    public static double[] Illuminant1964F8 = {
        0.34902, 0.35939
    };

    public static String Illuminant1964F9_Comments = "IlluminantF9Comments";
    public static int Illuminant1964F9_Temperature = 4150;
    public static double[] Illuminant1964F9 = {
        0.37829, 0.37045
    };

    public static String Illuminant1964F10_Comments = "IlluminantF10Comments";
    public static int Illuminant1964F10_Temperature = 5000;
    public static double[] Illuminant1964F10 = {
        0.35090, 0.35444
    };

    public static String Illuminant1964F11_Comments = "IlluminantF11Comments";
    public static int Illuminant1964F11_Temperature = 4000;
    public static double[] Illuminant1964F11 = {
        0.38541, 0.37123
    };

    public static String Illuminant1964F12_Comments = "IlluminantF12Comments";
    public static int Illuminant1964F12_Temperature = 3000;
    public static double[] Illuminant1964F12 = {
        0.44256, 0.39717
    };

    /*
        get/set
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public IlluminantType getType() {
        return type;
    }

    public void setType(IlluminantType type) {
        this.type = type;
    }

    public Observer getObserver() {
        return observer;
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }

    public String getWhitePointValues() {
        return whitePointValues;
    }

    public void setWhitePointValues(String whitePointValues) {
        this.whitePointValues = whitePointValues;
    }

}
