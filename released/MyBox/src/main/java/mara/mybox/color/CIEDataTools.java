package mara.mybox.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static mara.mybox.color.ColorBase.array;
import static mara.mybox.color.ColorBase.arrayDouble;
import static mara.mybox.color.ColorBase.clipRGB;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-5-20 18:51:37
 * @License Apache License Version 2.0
 */
public class CIEDataTools {

    public static double[] CIERGB(CIEData d) {
        double[] xyz = ColorBase.array(d.getRelativeX(), d.getRelativeY(), d.getRelativeZ());
        double[] outputs = CIEColorSpace.XYZd50toCIERGB(xyz);
        return outputs;
    }

    public static double[][] normalize(double[][] rgb) {
        double[][] n = new double[3][3];
        n[0] = normalize(rgb[0]);
        n[1] = normalize(rgb[1]);
        n[2] = normalize(rgb[2]);
        return n;
    }

    public static double[] normalize(double[] XYZ) {
        return normalize(XYZ[0], XYZ[1], XYZ[2]);
    }

    public static double[] normalize(double X, double Y, double Z) {
        double[] xyz = new double[3];
        double sum = X + Y + Z;
        if (sum == 0) {
            return array(X, Y, Z);
        }
        xyz[0] = X / sum;
        xyz[1] = Y / sum;
        xyz[2] = Z / sum;
        return xyz;
    }

    public static double[][] relative(double[][] rgb) {
        double[][] r = new double[3][3];
        r[0] = relative(rgb[0]);
        r[1] = relative(rgb[1]);
        r[2] = relative(rgb[2]);
        return r;
    }

    public static double[] relative(double[] xyz) {
        return relative(xyz[0], xyz[1], xyz[2]);
    }

    public static double[] relative(double x, double y, double z) {
        if (y == 0) {
            return array(x, y, z);
        }
        double[] xyz = new double[3];
        xyz[0] = x / y;
        xyz[1] = 1.0;
        xyz[2] = z / y;
        return xyz;
    }

    public static List<CIEData> cie1931Observer2Degree1nmData() {
        return read(cie1931Observer2Degree1nmFile());
    }

    public static List<CIEData> cie1931Observer2Degree1nmData(ColorSpace cs) {
        try {
            List<CIEData> data = cie1931Observer2Degree1nmData();
            for (CIEData d : data) {
                d.convert(cs);
                d.scaleValues();
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static List<CIEData> cie1931Observer2Degree5nmData() {
        return read(cie1931Observer2Degree5nmFile());
    }

    public static List<CIEData> cie1931Observer2Degree5nmData(ColorSpace cs) {
        try {
            List<CIEData> data = cie1931Observer2Degree5nmData();
            for (CIEData d : data) {
                d.convert(cs);
                d.scaleValues();
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static List<CIEData> read(String texts) {
        try {
            String[] lines = texts.split("\n");
            List<CIEData> data = new ArrayList<>();
            for (String line : lines) {
                CIEData d = readLine(line);
                if (d != null) {
                    data.add(d);
                }
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static List<CIEData> read(File file) {
        try {
            List<CIEData> data = new ArrayList<>();
            File validFile = FileTools.removeBOM(file);
            try (final BufferedReader reader = new BufferedReader(new FileReader(validFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    CIEData d = readLine(line);
                    if (d != null) {
                        data.add(d);
                    }
                }
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static List<CIEData> read(File file, ColorSpace cs) {
        try {
            List<CIEData> data = read(file);
            for (CIEData d : data) {
                d.convert(cs);
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static StringTable cieTable(List<CIEData> data, ColorSpace cs, String title) {
        try {
            if (data == null || data.isEmpty()) {
                return null;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(Languages.message("WaveLength"), Languages.message("TristimulusX"), Languages.message("TristimulusY"), Languages.message("TristimulusZ"),
                    Languages.message("NormalizedX"), Languages.message("NormalizedY"), Languages.message("NormalizedZ"), Languages.message("RelativeX"), Languages.message("RelativeY"), Languages.message("RelativeZ")));
            int num = 0;
            if (cs != null) {
                num = cs.getNumComponents();
                for (int i = 0; i < num; ++i) {
                    names.add(Languages.message(cs.getName(i)));
                }
                if (cs.getType() == ColorSpace.TYPE_RGB) {
                    for (int i = 0; i < num; ++i) {
                        names.add(Languages.message(cs.getName(i)) + "-" + Languages.message("Integer"));
                    }
                }
            }
            StringTable table = new StringTable(names, title);
            double[] channels;
            for (int i = 0; i < data.size(); ++i) {
                CIEData d = data.get(i);
                List<String> row = new ArrayList<>();
                row.addAll(Arrays.asList(d.getWaveLength() + "", d.X + "", d.Y + "", d.Z + "", d.getNormalizedX() + "", d.getNormalizedX() + "", d.getNormalizedX() + "", d.getRelativeX() + "", d.getRelativeY() + "", d.getRelativeZ() + ""));
                if (cs != null) {
                    channels = d.getChannels();
                    for (int j = 0; j < num; ++j) {
                        row.add(channels[j] + "");
                    }
                    if (cs.getType() == ColorSpace.TYPE_RGB) {
                        for (int j = 0; j < num; ++j) {
                            row.add(Math.round(channels[j] * 255) + "");
                        }
                    }
                }
                table.add(row);
            }
            return table;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static File cie1964Observer10Degree1nmFile() {
        File f = FxFileTools.getInternalFile("/data/CIE/CIE1964-10-degree-1nm.txt", "CIE", "CIE1964-10-degree-1nm.txt", false);
        return f;
    }

    public static File cie1931Observer2Degree5nmFile() {
        File f = FxFileTools.getInternalFile("/data/CIE/CIE1931-2-degree-5nm.txt", "CIE", "CIE1931-2-degree-5nm.txt", false);
        return f;
    }

    /*
    Source data: CIE standard Observer functions and CIE D50 Reference Illuminant
     */
    public static File cie1931Observer2Degree1nmFile() {
        File f = FxFileTools.getInternalFile("/data/CIE/CIE1931-2-degree-1nm.txt", "CIE", "CIE1931-2-degree-1nm.txt", false);
        return f;
    }

    public static CIEData readLine(String line) {
        try {
            String DataIgnoreChars = "\t|,|\uff0c|\\||\\{|\\}|\\[|\\]|\\\"|\\'";
            line = line.replaceAll(DataIgnoreChars, " ");
            String[] values = line.split("\\s+");
            List<Double> dList = new ArrayList<>();
            for (String v : values) {
                try {
                    double d = Double.parseDouble(v);
                    dList.add(d);
                    if (dList.size() >= 4) {
                        CIEData data = new CIEData((int) Math.round(dList.get(0)), dList.get(1), dList.get(2), dList.get(3));
                        data.scaleValues();
                        return data;
                    }
                } catch (Exception e) {
                    break;
                }
            }
            return null;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static File cie1964Observer10Degree5nmFile() {
        File f = FxFileTools.getInternalFile("/data/CIE/CIE1964-10-degree-5nm.txt", "CIE", "CIE1964-10-degree-5nm.txt", false);
        return f;
    }

    public static List<CIEData> cie1964Observer10Degree1nmData() {
        return read(cie1964Observer10Degree1nmFile());
    }

    public static List<CIEData> cie1964Observer10Degree1nmData(ColorSpace cs) {
        try {
            List<CIEData> data = cie1964Observer10Degree1nmData();
            for (CIEData d : data) {
                d.convert(cs);
                d.scaleValues();
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static String cieString(String texts) {
        return cieString(read(texts), null, null);
    }

    public static String cieString(List<CIEData> data, ColorSpace cs, String title) {
        try {
            if (data == null || data.isEmpty()) {
                return null;
            }
            String sp = "\t";
            StringBuilder s = new StringBuilder();
            if (title != null) {
                s.append(title).append("\n\n");
            }
            s.append(Languages.message("WaveLength")).append(sp).append(Languages.message("TristimulusX")).append(sp).append(Languages.message("TristimulusY")).append(sp).append(Languages.message("TristimulusZ")).append(sp).append(Languages.message("NormalizedX")).append(sp).append(Languages.message("NormalizedY")).append(sp).append(Languages.message("NormalizedZ")).append(sp).append(Languages.message("RelativeX")).append(sp).append(Languages.message("RelativeY")).append(sp).append(Languages.message("RelativeZ")).append(sp);
            int num = 0;
            if (cs != null) {
                num = cs.getNumComponents();
                for (int i = 0; i < num; ++i) {
                    s.append(Languages.message(cs.getName(i))).append(sp);
                }
                if (cs.getType() == ColorSpace.TYPE_RGB) {
                    for (int i = 0; i < num; ++i) {
                        s.append(Languages.message(cs.getName(i))).append("-").append(Languages.message("Integer")).append(sp);
                    }
                }
            }
            s.append("\n");
            double[] channels;
            for (int i = 0; i < data.size(); ++i) {
                CIEData d = data.get(i);
                s.append(d.getWaveLength()).append(sp).append(d.X).append(sp).append(d.Y).append(sp).append(d.Z).append(sp).append(d.getNormalizedX()).append(sp).append(d.getNormalizedY()).append(sp).append(d.getNormalizedZ()).append(sp).append(d.getRelativeX()).append(sp).append(d.getRelativeY()).append(sp).append(d.getRelativeZ()).append(sp);
                if (cs != null) {
                    channels = d.getChannels();
                    for (int j = 0; j < num; ++j) {
                        s.append(channels[j]).append(sp);
                    }
                    if (cs.getType() == ColorSpace.TYPE_RGB) {
                        for (int j = 0; j < num; ++j) {
                            s.append(Math.round(channels[j] * 255)).append(sp);
                        }
                    }
                }
                s.append("\n");
            }
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static List<CIEData> cie1964Observer10Degree5nmData() {
        return read(cie1964Observer10Degree5nmFile());
    }

    public static List<CIEData> cie1964Observer10Degree5nmData(ColorSpace cs) {
        try {
            List<CIEData> data = cie1964Observer10Degree5nmData();
            for (CIEData d : data) {
                d.convert(cs);
                d.scaleValues();
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static float[] convert(CIEData d, String iccProfile) {
        try {
            ICC_ColorSpace cs = new ICC_ColorSpace(ICC_Profile.getInstance(iccProfile));
            return convert(d, cs);
        } catch (Exception e) {
            return null;
        }
    }

    public static float[] convert(CIEData d, ICC_Profile iccProfile) {
        try {
            ICC_ColorSpace cs = new ICC_ColorSpace(iccProfile);
            return convert(d, cs);
        } catch (Exception e) {
            return null;
        }
    }

    public static float[] convert(CIEData d, ICC_ColorSpace cs) {
        float[] fxyz = new float[3];
        float[] outputs;
        fxyz[0] = (float) d.getRelativeX();
        fxyz[1] = (float) d.getRelativeY();
        fxyz[2] = (float) d.getRelativeZ();
        outputs = cs.fromCIEXYZ(fxyz);
        return outputs;
    }

    // https://www.w3.org/TR/css-color-4/#lab-to-rgb
    // http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html
    // http://brucelindbloom.com/index.html?Eqn_ChromAdapt.html
    /*
    Source data: CIE standard Observer functions and CIE D50 Reference Illuminant
     */
    public static double[] convert(ColorSpace cs, CIEData d) {
        double[] xyz = ColorBase.array(d.getRelativeX(), d.getRelativeY(), d.getRelativeZ());
        float[] fxyz = cs.fromCIEXYZ(ColorBase.arrayFloat(xyz));
        double[] outputs = clipRGB(arrayDouble(fxyz));
        return outputs;
    }

    public static double[] sRGB65(CIEData d) {
        double[] xyz = ColorBase.array(d.getRelativeX(), d.getRelativeY(), d.getRelativeZ());
        return CIEColorSpace.XYZd50toSRGBd65(xyz);
    }

}
