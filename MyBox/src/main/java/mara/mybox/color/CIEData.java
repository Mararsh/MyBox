package mara.mybox.color;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import static mara.mybox.color.ColorBase.array;
import static mara.mybox.color.ColorBase.arrayDouble;
import static mara.mybox.color.ColorBase.clipRGB;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.tools.DoubleTools.scale;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-5-20 18:51:37
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class CIEData {

    public int waveLength;  // nm
    public double X, Y, Z; // tristimulus values in 0~1.
    public double normalizedX, normalizedY, normalizedZ; // x + y + z = 1
    public double relativeX, relativeY, relativeZ; // y = 1;
    public double red = -1, green = -1, blue = -1; // sRGB
    public int redi = -1, greeni = -1, bluei = -1; // sRGB integer
    public ColorSpace colorSpace;
    public double[] channels;
    public int scale = 8;

    public CIEData() {

    }

    public CIEData(int waveLength, double X, double Y, double Z) {
        this.waveLength = waveLength;
        setTristimulusValues(X, Y, Z);
    }

    public CIEData(int waveLength, double X, double Y, double Z, ColorSpace cs) {
        this.waveLength = waveLength;
        setTristimulusValues(X, Y, Z);
        convert(cs);
    }

    public CIEData(double x, double y) {
        setxy(x, y);
    }

    public CIEData(Color color) {
        double[] xyz = SRGB.toXYZd50(color);
        setTristimulusValues(xyz[0], xyz[1], xyz[2]);
    }

    public CIEData(javafx.scene.paint.Color color) {
        double[] xyz = SRGB.toXYZd50(new Color((float) color.getRed(), (float) color.getGreen(), (float) color.getBlue()));
        setTristimulusValues(xyz[0], xyz[1], xyz[2]);
    }

    public final void setTristimulusValues(double X, double Y, double Z) {
        this.X = X;
        this.Y = Y;
        this.Z = Z;

        double[] xyz = CIEData.normalize(X, Y, Z);
        this.normalizedX = xyz[0];
        this.normalizedY = xyz[1];
        this.normalizedZ = xyz[2];

        xyz = CIEData.relative(X, Y, Z);
        this.relativeX = xyz[0];
        this.relativeY = xyz[1];
        this.relativeZ = xyz[2];
    }

    public final void setxy(double x, double y) {
        setxyY(x, y, 1.0);
    }

    public final void setxyY(double[] xyY) {
        setxyY(xyY[0], xyY[1], xyY[2]);
    }

    public final void setxyY(double x, double y, double Y) {
        this.normalizedX = x;
        this.normalizedY = y;
        this.normalizedZ = 1 - x - y;

        this.X = x * Y / y;
        this.Y = Y;
        this.Z = (1 - x - y) * Y / y;

        this.relativeX = x / y;
        this.relativeY = 1.0;
        this.relativeZ = (1 - x - y) / y;
    }

    public final void setRelativeXYZ(double[] XYZ) {
        setRelativeXYZ(XYZ[0], XYZ[1], XYZ[2]);
    }

    public final void setRelativeXYZ(double X, double Y, double Z) {
        this.relativeX = X;
        this.relativeY = Y;
        this.relativeZ = Z;

        double[] xyz = CIEData.normalize(X, Y, Z);
        this.normalizedX = xyz[0];
        this.normalizedY = xyz[1];
        this.normalizedZ = xyz[2];

        this.X = relativeX * 100;
        this.Y = relativeY * 100;
        this.Z = relativeZ * 100;
    }

    public final void setNormalziedXY(double[] xy) {
        setNormalziedXY(xy[0], xy[1]);
    }

    public final void setNormalziedXY(double x, double y) {
        this.normalizedX = x;
        this.normalizedY = y;
        this.normalizedZ = 1 - x - y;

        this.relativeX = x / y;
        this.relativeY = 1;
        this.relativeZ = (1 - x - y) / y;

        this.X = relativeX * 100;
        this.Y = relativeY * 100;
        this.Z = relativeZ * 100;
    }

    public final double[] convert(ColorSpace cs) {
        if (cs == null) {
            channels = CIERGB(this);
        } else {
            colorSpace = cs;
            if (cs.isCS_sRGB()) {
                channels = sRGB65(this);
            } else {
                channels = convert(cs, this);
            }
        }
        return channels;
    }

    public void scaleValues() {
        scaleValues(this.scale);
    }

    public void scaleValues(int scale) {
        this.scale = scale;
        X = scale(X, scale);
        Y = scale(Y, scale);
        Z = scale(Z, scale);
        normalizedX = scale(normalizedX, scale);
        normalizedY = scale(normalizedY, scale);
        normalizedZ = scale(normalizedZ, scale);
        relativeX = scale(relativeX, scale);
        relativeY = scale(relativeY, scale);
        relativeZ = scale(relativeZ, scale);
        if (channels != null) {
            for (int i = 0; i < channels.length; ++i) {
                channels[i] = scale(channels[i], scale);
            }
        }
    }


    /*
        Data
     */
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
        float[] fxyz = new float[3], outputs;
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

    public static double[] CIERGB(CIEData d) {
        double[] xyz = ColorBase.array(d.getRelativeX(), d.getRelativeY(), d.getRelativeZ());
        double[] outputs = CIEColorSpace.XYZd50toCIERGB(xyz);
        return outputs;
    }

    /*
        Source data: CIE standard Observer functions and CIE D50 Reference Illuminant
     */
    public File cie1931Observer2Degree1nmFile() {
        File f = FxmlControl.getInternalFile(
                "/data/CIE/CIE1931-2-degree-1nm.txt", "CIE", "CIE1931-2-degree-1nm.txt", false);
        return f;
    }

    public File cie1964Observer10Degree1nmFile() {
        File f = FxmlControl.getInternalFile(
                "/data/CIE/CIE1964-10-degree-1nm.txt", "CIE", "CIE1964-10-degree-1nm.txt", false);
        return f;
    }

    public File cie1931Observer2Degree5nmFile() {
        File f = FxmlControl.getInternalFile(
                "/data/CIE/CIE1931-2-degree-5nm.txt", "CIE", "CIE1931-2-degree-5nm.txt", false);
        return f;
    }

    public File cie1964Observer10Degree5nmFile() {
        File f = FxmlControl.getInternalFile(
                "/data/CIE/CIE1964-10-degree-5nm.txt", "CIE", "CIE1964-10-degree-5nm.txt", false);
        return f;
    }

    public List<CIEData> cie1931Observer2Degree1nmData() {
        return read(cie1931Observer2Degree1nmFile());
    }

    public List<CIEData> cie1931Observer2Degree1nmData(ColorSpace cs) {
        try {
            List<CIEData> data = cie1931Observer2Degree1nmData();
            for (CIEData d : data) {
                d.convert(cs);
                d.scaleValues();
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public List<CIEData> cie1964Observer10Degree1nmData() {
        return read(cie1964Observer10Degree1nmFile());
    }

    public List<CIEData> cie1964Observer10Degree1nmData(ColorSpace cs) {
        try {
            List<CIEData> data = cie1964Observer10Degree1nmData();
            for (CIEData d : data) {
                d.convert(cs);
                d.scaleValues();
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public List<CIEData> cie1931Observer2Degree5nmData() {
        return read(cie1931Observer2Degree5nmFile());
    }

    public List<CIEData> cie1931Observer2Degree5nmData(ColorSpace cs) {
        try {
            List<CIEData> data = cie1931Observer2Degree5nmData();
            for (CIEData d : data) {
                d.convert(cs);
                d.scaleValues();
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public List<CIEData> cie1964Observer10Degree5nmData() {
        return read(cie1964Observer10Degree5nmFile());
    }

    public List<CIEData> cie1964Observer10Degree5nmData(ColorSpace cs) {
        try {
            List<CIEData> data = cie1964Observer10Degree5nmData();
            for (CIEData d : data) {
                d.convert(cs);
                d.scaleValues();
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public String cie1931Observer2Degree1nmString(ColorSpace cs) {
        List<CIEData> data = cie1931Observer2Degree1nmData(cs);
        String title = message("CIE1931Observer2DegreeAndSRGB");
        return cieString(data, cs, title);
    }

    public String cie1964Observer10Degree1nmString(ColorSpace cs) {
        List<CIEData> data = cie1964Observer10Degree1nmData(cs);
        String title = message("CIE1964Observer10DegreeAndSRGB");
        return cieString(data, cs, title);
    }

    public String cie1931Observer2Degree1nmString(String iccProfile) {
        try {
            ICC_ColorSpace cs = new ICC_ColorSpace(ICC_Profile.getInstance(iccProfile));
            return cie1931Observer2Degree1nmString(cs);
        } catch (Exception e) {
            return null;
        }
    }

    public String cie1931Observer2Degree1nmString(ICC_Profile iccProfile) {
        try {
            ICC_ColorSpace cs = new ICC_ColorSpace(iccProfile);
            return cie1931Observer2Degree1nmString(cs);
        } catch (Exception e) {
            return null;
        }
    }

    public String cie1931Observer2Degree5nmString(ColorSpace cs) {
        List<CIEData> data = cie1931Observer2Degree5nmData(cs);
        String title = message("CIE1931Observer2DegreeAndSRGB");
        return cieString(data, cs, title);
    }

    public String cie1964Observer10Degree5nmString(ColorSpace cs) {
        List<CIEData> data = cie1964Observer10Degree5nmData(cs);
        String title = message("CIE1964Observer10DegreeAndSRGB");
        return cieString(data, cs, title);
    }

    public String cie1931Observer2Degree5nmString(String iccProfile) {
        try {
            ICC_ColorSpace cs = new ICC_ColorSpace(ICC_Profile.getInstance(iccProfile));
            return cie1931Observer2Degree5nmString(cs);
        } catch (Exception e) {
            return null;
        }
    }

    public String cie1931Observer2Degree5nmString(ICC_Profile iccProfile) {
        try {
            ICC_ColorSpace cs = new ICC_ColorSpace(iccProfile);
            return cie1931Observer2Degree5nmString(cs);
        } catch (Exception e) {
            return null;
        }
    }

    /*
        Static methods
     */
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

    public static CIEData readLine(String line) {
        try {
            String DataIgnoreChars = "\t|,|，|\\||\\{|\\}|\\[|\\]|\\\"|\\\'";
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
            MyBoxLog.debug(e.toString());
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
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static List<CIEData> read(File file) {
        try {
            List<CIEData> data = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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
            MyBoxLog.debug(e.toString());
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
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static String cieString(String texts) {
        return cieString(read(texts), null, null);
    }

    public static String cieString(File file) {
        return cieString(file, file.getAbsolutePath());
    }

    public static String cieString(File file, String title) {
        try {
            List<CIEData> data = read(file);
            return cieString(data, null, file.getAbsolutePath());
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static String cieString(File file, ColorSpace cs) {
        return cieString(file, cs, file.getAbsolutePath());
    }

    public static String cieString(File file, ColorSpace cs, String title) {
        try {
            List<CIEData> data = read(file, cs);
            return cieString(data, cs, file.getAbsolutePath());
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
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
            s.append(message("WaveLength")).append(sp).
                    append(message("TristimulusX")).append(sp).append(message("TristimulusY")).append(sp).append(message("TristimulusZ")).append(sp).
                    append(message("NormalizedX")).append(sp).append(message("NormalizedY")).append(sp).append(message("NormalizedZ")).append(sp).
                    append(message("RelativeX")).append(sp).append(message("RelativeY")).append(sp).append(message("RelativeZ")).append(sp);
            int num = 0;
            if (cs != null) {
                num = cs.getNumComponents();
                for (int i = 0; i < num; ++i) {
                    s.append(message(cs.getName(i))).append(sp);
                }
                if (cs.getType() == ColorSpace.TYPE_RGB) {
                    for (int i = 0; i < num; ++i) {
                        s.append(message(cs.getName(i))).append("-").append(message("Integer")).append(sp);
                    }
                }
            }
            s.append("\n");

            double[] channels;
            for (int i = 0; i < data.size(); ++i) {
                CIEData d = data.get(i);
                s.append(d.getWaveLength()).append(sp).
                        append(d.X).append(sp).
                        append(d.Y).append(sp).
                        append(d.Z).append(sp).
                        append(d.getNormalizedX()).append(sp).
                        append(d.getNormalizedY()).append(sp).
                        append(d.getNormalizedZ()).append(sp).
                        append(d.getRelativeX()).append(sp).
                        append(d.getRelativeY()).append(sp).
                        append(d.getRelativeZ()).append(sp);
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
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public double getX() {
        return X;
    }

    public void setX(double X) {
        this.X = X;
    }

    public double getY() {
        return Y;
    }

    public void setY(double Y) {
        this.Y = Y;
    }

    public double getZ() {
        return Z;
    }

    public void setZ(double Z) {
        this.Z = Z;
    }

    public int getScale() {
        return scale;
    }

    /*
    get/set
     */
    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getWaveLength() {
        return waveLength;
    }

    public void setWaveLength(int waveLength) {
        this.waveLength = waveLength;
    }

    public double getNormalizedX() {
        return normalizedX;
    }

    public void setNormalizedX(double normalizedX) {
        this.normalizedX = normalizedX;
    }

    public double getNormalizedY() {
        return normalizedY;
    }

    public void setNormalizedY(double normalizedY) {
        this.normalizedY = normalizedY;
    }

    public double getNormalizedZ() {
        return normalizedZ;
    }

    public void setNormalizedZ(double normalizedZ) {
        this.normalizedZ = normalizedZ;
    }

    public double getRed() {
        if (red == -1) {
            red = channels[0];
        }
        return red;
    }

    public void setRed(double red) {
        this.red = red;
    }

    public double getGreen() {
        if (green == -1) {
            green = channels[1];
        }
        return green;
    }

    public void setGreen(double green) {
        this.green = green;
    }

    public double getBlue() {
        if (blue == -1) {
            blue = channels[2];
        }
        return blue;
    }

    public void setBlue(double blue) {
        this.blue = blue;
    }

    public int getRedi() {
        if (redi == -1) {
            redi = (int) Math.round(255 * channels[0]);
        }
        return redi;
    }

    public void setRedi(int redi) {
        this.redi = redi;
    }

    public int getGreeni() {
        if (greeni == -1) {
            greeni = (int) Math.round(255 * channels[1]);
        }
        return greeni;
    }

    public void setGreeni(int greeni) {
        this.greeni = greeni;
    }

    public int getBluei() {
        if (bluei == -1) {
            bluei = (int) Math.round(255 * channels[2]);
        }
        return bluei;
    }

    public void setBluei(int bluei) {
        this.bluei = bluei;
    }

    public double getRelativeX() {
        return relativeX;
    }

    public void setRelativeX(double relativeX) {
        this.relativeX = relativeX;
    }

    public double getRelativeY() {
        return relativeY;
    }

    public void setRelativeY(double relativeY) {
        this.relativeY = relativeY;
    }

    public double getRelativeZ() {
        return relativeZ;
    }

    public void setRelativeZ(double relativeZ) {
        this.relativeZ = relativeZ;
    }

    public ColorSpace getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(ColorSpace colorSpace) {
        this.colorSpace = colorSpace;
    }

    public double[] getChannels() {
        return channels;
    }

    public void setChannels(double[] channels) {
        this.channels = channels;
    }

}
