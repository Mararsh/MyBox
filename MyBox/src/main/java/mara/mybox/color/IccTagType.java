package mara.mybox.color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static mara.mybox.color.IccProfile.toBytes;
import mara.mybox.tools.ByteTools;
import static mara.mybox.tools.ByteTools.bytesToInt;
import static mara.mybox.tools.ByteTools.bytesToUshort;
import static mara.mybox.tools.ByteTools.intToBytes;
import static mara.mybox.tools.ByteTools.shortToBytes;
import static mara.mybox.tools.ByteTools.subBytes;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import static mara.mybox.value.CommonValues.Indent;

/**
 * @Author Mara
 * @CreateDate 2019-5-7 20:41:49
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class IccTagType {

    // All in Big-Endian
    public static String[][] IlluminantTypes = {
        {"0", "Unknown"},
        {"1", "D50"},
        {"2", "D65"},
        {"3", "D93"},
        {"4", "F2"},
        {"5", "D55"},
        {"6", "A"},
        {"7", "Equi-Power (E)"},
        {"8 ", "F8"}
    };

    public static String[][] ObserverTypes = {
        {"0", "Unknown"},
        {"1", "CIE 1931 2 Degree Observer"},
        {"2", "CIE 1964 10 Degree Observer"},};

    public static String[][] GeometryTypes = {
        {"0", "Unknown"},
        {"1", "0°:45° or 45°:0°"},
        {"2", "0°:d  or  d:0°"},};

    /*
         Decode values of Base Types
     */
    public static int bytes4ToInt(byte[] b) {
        return b[3] & 0xFF
                | (b[2] & 0xFF) << 8
                | (b[1] & 0xFF) << 16
                | (b[0] & 0xFF) << 24;
    }

    public static int uInt8Number(byte b) {
        return b & 0xFF;
    }

    public static double u8Fixed8Number(byte[] b) {
        int integer = b[0] & 0xFF;
        double fractional = (b[1] & 0xFF) / 256d;
        return integer + fractional;
    }

    public static double s15Fixed16Number(byte[] b) {
        int integer = b[1] & 0xFF | b[0] << 8;
        double fractional = (b[3] & 0xFF | (b[2] & 0xFF) << 8) / 65536d;
        return integer + fractional;
    }

    public static double u16Fixed16Number(byte[] b) {
        int integer = b[1] & 0xFF | (b[0] & 0xFF) << 8;
        double fractional = (b[3] & 0xFF | (b[2] & 0xFF) << 8) / 65536d;
        return integer + fractional;
    }

    public static int uInt16Number(byte[] b) {
        int integer = b[1] & 0xFF | (b[0] & 0xFF) << 8;
        return integer;
    }

    public static long uInt32Number(byte[] b) {
        long v = b[3] & 0xFF
                | (b[2] & 0xFF) << 8
                | (b[1] & 0xFF) << 16
                | (b[0] & 0xFF) << 24;
        return v;
    }

    public static String dateTimeString(byte[] v) {
        if (v == null || v.length != 12) {
            return "";
        }
        String d = bytesToUshort(subBytes(v, 0, 2)) + "-"
                + StringTools.fillLeftZero(uInt16Number(subBytes(v, 2, 2)), 2) + "-"
                + StringTools.fillLeftZero(uInt16Number(subBytes(v, 4, 2)), 2) + " "
                + StringTools.fillLeftZero(uInt16Number(subBytes(v, 6, 2)), 2) + ":"
                + StringTools.fillLeftZero(uInt16Number(subBytes(v, 8, 2)), 2) + ":"
                + StringTools.fillLeftZero(uInt16Number(subBytes(v, 10, 2)), 2);
        return d;
    }

    public static double[] XYZNumber(byte[] v) {
        if (v == null || v.length != 12) {
            return null;
        }
        double[] xyz = new double[3];
        xyz[0] = IccTagType.s15Fixed16Number(subBytes(v, 0, 4));
        xyz[1] = IccTagType.s15Fixed16Number(subBytes(v, 4, 4));
        xyz[2] = IccTagType.s15Fixed16Number(subBytes(v, 8, 4));
        return xyz;
    }

    public static List<String> illuminantTypes() {
        List<String> types = new ArrayList();
        for (String[] item : IlluminantTypes) {
            types.add(item[1]);
        }
        return types;
    }

    public static String illuminantType(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            return "Unknown";
        }
        String type = bytesToInt(bytes) + "";
        for (String[] item : IlluminantTypes) {
            if (item[0].equals(type)) {
                return item[1];
            }
        }
        return "Unknown";
    }

    public static List<String> observerTypes() {
        List<String> types = new ArrayList();
        for (String[] item : ObserverTypes) {
            types.add(item[1]);
        }
        return types;
    }

    public static String observerType(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            return "Unknown";
        }
        String type = bytesToInt(bytes) + "";
        for (String[] item : ObserverTypes) {
            if (item[0].equals(type)) {
                return item[1];
            }
        }
        return "Unknown";
    }

    public static List<String> geometryTypes() {
        List<String> types = new ArrayList();
        for (String[] item : GeometryTypes) {
            types.add(item[1]);
        }
        return types;
    }

    public static String geometryType(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            return "Unknown";
        }
        String type = bytesToInt(bytes) + "";
        for (String[] item : GeometryTypes) {
            if (item[0].equals(type)) {
                return item[1];
            }
        }
        return "Unknown";
    }

    /*
        Decode value of Tag Type
     */
    public static double[][] XYZ(byte[] bytes) {
        if (bytes == null || bytes.length < 12
                || !"XYZ ".equals(new String(subBytes(bytes, 0, 4)))) {
            return null;
        }
        int size = (bytes.length - 8) / 12;
        double[][] xyzArray = new double[size][3];
        for (int i = 0; i < size; i++) {
            double[] xyz = XYZNumber(subBytes(bytes, 8 + i * 12, 12));
            xyzArray[i] = xyz;
        }
        return xyzArray;
    }

    public static String dateTime(byte[] bytes) {
        if (bytes == null || bytes.length < 20
                || !"dtim".equals(new String(subBytes(bytes, 0, 4)))) {
            return null;
        }
        return dateTimeString(subBytes(bytes, 8, 12));
    }

    public static String signature(byte[] bytes) {
        if (bytes == null || !"sig ".equals(new String(subBytes(bytes, 0, 4)))) {
            return null;
        }
        String value = new String(subBytes(bytes, 8, 4));
        return value;
    }

    public static String text(byte[] bytes) {
        if (bytes == null || !"text".equals(new String(subBytes(bytes, 0, 4)))) {
            return null;
        }
        String value = new String(subBytes(bytes, 8, bytes.length - 8));
        return value;
    }

    public static Map<String, Object> multiLocalizedUnicode(byte[] bytes) {
        if (bytes == null || !"desc".equals(new String(subBytes(bytes, 0, 4)))) {
            return null;
        }
        String signature = new String(subBytes(bytes, 0, 4));
        switch (signature) {
            case "desc":
                return textDescription(bytes);
            case "mluc":
                return multiLocalizedUnicodes(bytes);
            default:
                return null;
        }
    }

    // REVISION of ICC.1:1998-09
    public static Map<String, Object> textDescription(byte[] bytes) {
        if (bytes == null || !"desc".equals(new String(subBytes(bytes, 0, 4)))) {
            return null;
        }
        Map<String, Object> values = new HashMap();

        int AsciiLength = (int) uInt32Number(subBytes(bytes, 8, 4));
        values.put("AsciiLength", AsciiLength);
        String value = new String(subBytes(bytes, 12, AsciiLength));
        values.put("Ascii", value);

        try {
            values.put("UnicodeCode", bytesToInt(subBytes(bytes, 12 + AsciiLength, 4)));
            int UnicodeLength = (int) uInt32Number(subBytes(bytes, 16 + AsciiLength, 4));
            values.put("UnicodeLength", UnicodeLength);
            if (UnicodeLength == 0) {
                values.put("Unicode", "");
            } else {
                values.put("Unicode", new String(subBytes(bytes, 20 + AsciiLength, UnicodeLength * 2), "UTF-16"));
            }

            values.put("ScriptCodeCode", uInt16Number(subBytes(bytes, 20 + AsciiLength + UnicodeLength * 2, 2)));
            int ScriptCodeLength = uInt8Number(bytes[22 + AsciiLength + UnicodeLength * 2]);
            values.put("ScriptCodeLength", ScriptCodeLength);
            if (ScriptCodeLength == 0) {
                values.put("ScriptCode", "");
            } else {
                byte[] scriptCode = subBytes(bytes, 23 + AsciiLength + UnicodeLength * 2, ScriptCodeLength);
                values.put("ScriptCode", new String(scriptCode));
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }

        return values;
    }

    // REVISION of ICC.1:2004-10
    public static Map<String, Object> multiLocalizedUnicodes(byte[] bytes) {
        logger.debug(new String(subBytes(bytes, 0, 4)));
        if (bytes == null || !"mluc".equals(new String(subBytes(bytes, 0, 4)))) {
            return null;
        }
        Map<String, Object> values = new HashMap();

        int num = (int) uInt32Number(subBytes(bytes, 8, 4));
        int size = (int) uInt32Number(subBytes(bytes, 12, 4));
        values.put("number", num);
        values.put("size", size);
        logger.debug(num + " " + size);
        int offset = 16;
        for (int i = 0; i < num; i++) {
            int languageCode = uInt16Number(subBytes(bytes, offset + i * 12, 2));
            values.put("languageCode" + i, languageCode);
            int countryCode = uInt16Number(subBytes(bytes, offset + i * 12 + 2, 2));
            values.put("countryCode" + i, countryCode);
            int length = (int) uInt32Number(subBytes(bytes, offset + i * 12 + 4, 4));
            values.put("length" + i, length);
            int ioffset = (int) uInt32Number(subBytes(bytes, offset + i * 12 + 8, 4));
            values.put("offset" + i, ioffset);
            logger.debug(languageCode + " " + countryCode + " " + length + " " + ioffset);
        }

//        try {
//            values.put("UnicodeCode", bytesToInt(subBytes(bytes, 12 + AsciiLength, 4)));
//            int UnicodeLength = (int) uInt32Number(subBytes(bytes, 16 + AsciiLength, 4));
//            logger.debug(UnicodeLength);
//            values.put("UnicodeLength", UnicodeLength);
//            if (UnicodeLength == 0) {
//                values.put("Unicode", "");
//            } else {
//                values.put("Unicode", new String(subBytes(bytes, 20 + AsciiLength, UnicodeLength)));
//            }
//
//            values.put("ScriptCodeCode", uInt16Number(subBytes(bytes, 20 + AsciiLength + UnicodeLength, 2)));
//            int ScriptCodeLength = uInt8Number(bytes[22 + AsciiLength + UnicodeLength]);
//            logger.debug(ScriptCodeLength);
//            values.put("ScriptCodeLength", ScriptCodeLength);
//            if (ScriptCodeLength == 0) {
//                values.put("ScriptCode", "");
//            } else {
//                byte[] scriptCode = subBytes(bytes, 23 + AsciiLength + UnicodeLength, ScriptCodeLength);
//                values.put("ScriptCode", new String(scriptCode));
//            }
//        } catch (Exception e) {
//            logger.debug(e.toString());
//        }
        return values;
    }

    public static double[] curve(byte[] bytes) {
        if (bytes == null || !"curv".equals(new String(subBytes(bytes, 0, 4)))) {
            return null;
        }
        int count = bytesToInt(subBytes(bytes, 8, 4));
        double[] values;
        switch (count) {
            case 0:
                values = new double[1];
                values[0] = 1.0d;
                break;
            case 1:
                values = new double[1];
                values[0] = u8Fixed8Number(subBytes(bytes, 12, 2));
                break;
            default:
                values = new double[count];
                for (int i = 0; i < count; i++) {
                    values[i] = uInt16Number(subBytes(bytes, 12 + i * 2, 2)) / 65535d;
                }
                break;
        }
        return values;
    }

    public static Map<String, Object> viewingConditions(byte[] bytes) {
        if (bytes == null || !"view".equals(new String(subBytes(bytes, 0, 4)))) {
            return null;
        }
        Map<String, Object> values = new HashMap();
        values.put("illuminant", XYZNumber(subBytes(bytes, 8, 12)));
        values.put("surround", XYZNumber(subBytes(bytes, 20, 12)));
        values.put("illuminantType", illuminantType(subBytes(bytes, 32, 4)));
        return values;
    }

    public static Map<String, Object> measurement(byte[] bytes) {
        if (bytes == null || !"meas".equals(new String(subBytes(bytes, 0, 4)))) {
            return null;
        }
        Map<String, Object> values = new HashMap();
        values.put("observer", observerType(subBytes(bytes, 8, 4)));
        values.put("tristimulus", XYZNumber(subBytes(bytes, 12, 12)));
        values.put("geometry", geometryType(subBytes(bytes, 24, 4)));
        values.put("flare", u16Fixed16Number(subBytes(bytes, 28, 4)));
        values.put("illuminantType", illuminantType(subBytes(bytes, 32, 4)));
        return values;
    }

    public static double[] s15Fixed16Array(byte[] bytes) {
        if (bytes == null || bytes.length < 4
                || !"sf32".equals(new String(subBytes(bytes, 0, 4)))) {
            return null;
        }
        int size = (bytes.length - 8) / 4;
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
            double v = IccTagType.s15Fixed16Number(subBytes(bytes, 8 + i * 4, 4));
            array[i] = v;
        }
        return array;
    }

    public static Map<String, Object> lut(byte[] bytes, boolean normalizedLut) {
        if (bytes == null) {
            return null;
        }
        String type = new String(subBytes(bytes, 0, 4));
        switch (type) {
            case "mft1":
                return lut8(bytes, normalizedLut);
            case "mft2":
                return lut16(bytes, normalizedLut);
            case "mAB ":
                return lutAToB(bytes, normalizedLut);
            default:
                return null;
        }
    }

    public static Map<String, Object> lut8(byte[] bytes, boolean normalizedLut) {
        try {
            if (bytes == null || !"mft1".equals(new String(subBytes(bytes, 0, 4)))) {
                return null;
            }
            Map<String, Object> values = new HashMap();
            values.put("type", "lut8");
            int InputChannelsNumber = uInt8Number(bytes[8]);
            values.put("InputChannelsNumber", InputChannelsNumber);
            int OutputChannelsNumber = uInt8Number(bytes[9]);
            values.put("OutputChannelsNumber", OutputChannelsNumber);
            int GridPointsNumber = uInt8Number(bytes[10]);
            values.put("GridPointsNumber", GridPointsNumber);
            double e1 = IccTagType.s15Fixed16Number(subBytes(bytes, 12, 4));
            values.put("e1", e1);
            double e2 = IccTagType.s15Fixed16Number(subBytes(bytes, 16, 4));
            values.put("e2", e2);
            double e3 = IccTagType.s15Fixed16Number(subBytes(bytes, 20, 4));
            values.put("e3", e3);
            double e4 = IccTagType.s15Fixed16Number(subBytes(bytes, 24, 4));
            values.put("e4", e4);
            double e5 = IccTagType.s15Fixed16Number(subBytes(bytes, 28, 4));
            values.put("e5", e5);
            double e6 = IccTagType.s15Fixed16Number(subBytes(bytes, 32, 4));
            values.put("e6", e6);
            double e7 = IccTagType.s15Fixed16Number(subBytes(bytes, 36, 4));
            values.put("e7", e7);
            double e8 = IccTagType.s15Fixed16Number(subBytes(bytes, 40, 4));
            values.put("e8", e8);
            double e9 = IccTagType.s15Fixed16Number(subBytes(bytes, 44, 4));
            values.put("e9", e9);
            int offset = 48;
            int maxItems = AppVaribles.getUserConfigInt("ICCMaxDecodeNumber", 500);
            List<List<Double>> InputTables = new ArrayList();
            for (int n = 0; n < 256; n++) {
                List<Double> InputTable = new ArrayList();
                for (int i = 0; i < InputChannelsNumber; i++) {
                    double v = uInt8Number(bytes[offset++]);
                    if (normalizedLut) {
                        InputTable.add(v / 255);
                    } else {
                        InputTable.add(v);
                    }
                }
                InputTables.add(InputTable);
                if (InputTables.size() >= maxItems) {
                    values.put("InputTablesTruncated", true);
                    break;
                }
            }
            values.put("InputTables", InputTables);
            List<List<Double>> CLUTTables = new ArrayList();
            double dimensionSize = Math.pow(GridPointsNumber, InputChannelsNumber);
            for (int i = 0; i < dimensionSize; i++) {
                List<Double> GridPoint = new ArrayList();
                for (int o = 0; o < OutputChannelsNumber; o++) {
                    double v = uInt8Number(bytes[offset++]);
                    if (normalizedLut) {
                        GridPoint.add(v / 255);
                    } else {
                        GridPoint.add(v);
                    }
                }
                CLUTTables.add(GridPoint);
                if (CLUTTables.size() >= maxItems) {
                    values.put("CLUTTablesTruncated", true);
                    break;
                }
            }
            values.put("CLUTTables", CLUTTables);
            List<List<Double>> OutputTables = new ArrayList();
            for (int m = 0; m < 256; m++) {
                List<Double> OutputTable = new ArrayList();
                for (int i = 0; i < OutputChannelsNumber; i++) {
                    double v = uInt8Number(bytes[offset++]);
                    if (normalizedLut) {
                        OutputTable.add(v / 255);
                    } else {
                        OutputTable.add(v);
                    }
                }
                OutputTables.add(OutputTable);
                if (OutputTables.size() >= maxItems) {
                    values.put("OutputTablesTruncated", true);
                    break;
                }
            }
            values.put("OutputTables", OutputTables);

            return values;
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, Object> lut16(byte[] bytes, boolean normalizedLut) {
        try {
            if (bytes == null || !"mft2".equals(new String(subBytes(bytes, 0, 4)))) {
                return null;
            }
            Map<String, Object> values = new HashMap();
            values.put("type", "lut16");
            int InputChannelsNumber = uInt8Number(bytes[8]);
            values.put("InputChannelsNumber", InputChannelsNumber);
            int OutputChannelsNumber = uInt8Number(bytes[9]);
            values.put("OutputChannelsNumber", OutputChannelsNumber);
            int GridPointsNumber = uInt8Number(bytes[10]);
            values.put("GridPointsNumber", GridPointsNumber);
            double e1 = IccTagType.s15Fixed16Number(subBytes(bytes, 12, 4));
            values.put("e1", e1);
            double e2 = IccTagType.s15Fixed16Number(subBytes(bytes, 16, 4));
            values.put("e2", e2);
            double e3 = IccTagType.s15Fixed16Number(subBytes(bytes, 20, 4));
            values.put("e3", e3);
            double e4 = IccTagType.s15Fixed16Number(subBytes(bytes, 24, 4));
            values.put("e4", e4);
            double e5 = IccTagType.s15Fixed16Number(subBytes(bytes, 28, 4));
            values.put("e5", e5);
            double e6 = IccTagType.s15Fixed16Number(subBytes(bytes, 32, 4));
            values.put("e6", e6);
            double e7 = IccTagType.s15Fixed16Number(subBytes(bytes, 36, 4));
            values.put("e7", e7);
            double e8 = IccTagType.s15Fixed16Number(subBytes(bytes, 40, 4));
            values.put("e8", e8);
            double e9 = IccTagType.s15Fixed16Number(subBytes(bytes, 44, 4));
            values.put("e9", e9);
            int InputTablesNumber = uInt16Number(subBytes(bytes, 48, 2));
            values.put("InputTablesNumber", InputTablesNumber);
            int OutputTablesNumber = uInt16Number(subBytes(bytes, 50, 2));
            values.put("OutputTablesNumber", OutputTablesNumber);

            int offset = 52;
            int maxItems = AppVaribles.getUserConfigInt("ICCMaxDecodeNumber", 500);
            List<List<Double>> InputTables = new ArrayList();
            for (int n = 0; n < InputTablesNumber; n++) {
                List<Double> InputTable = new ArrayList();
                for (int i = 0; i < InputChannelsNumber; i++) {
                    double v = uInt16Number(subBytes(bytes, offset, 2));
                    if (normalizedLut) {
                        InputTable.add(v / 65535);
                    } else {
                        InputTable.add(v);
                    }
                    offset += 2;
                }
                InputTables.add(InputTable);
                if (InputTables.size() >= maxItems) {
                    values.put("InputTablesTruncated", true);
                    break;
                }
            }
            values.put("InputTables", InputTables);
            List<List<Double>> CLUTTables = new ArrayList();
            double dimensionSize = Math.pow(GridPointsNumber, InputChannelsNumber);
            for (int i = 0; i < dimensionSize; i++) {
                List<Double> GridPoint = new ArrayList();
                for (int o = 0; o < OutputChannelsNumber; o++) {
                    double v = uInt16Number(subBytes(bytes, offset, 2));
                    if (normalizedLut) {
                        GridPoint.add(v / 65535);
                    } else {
                        GridPoint.add(v);
                    }
                    offset += 2;
                }
                CLUTTables.add(GridPoint);
                if (CLUTTables.size() >= maxItems) {
                    values.put("CLUTTablesTruncated", true);
                    break;
                }
            }
            values.put("CLUTTables", CLUTTables);
            List<List<Double>> OutputTables = new ArrayList();
            for (int m = 0; m < OutputTablesNumber; m++) {
                List<Double> OutputTable = new ArrayList();
                for (int i = 0; i < OutputChannelsNumber; i++) {
                    double v = uInt16Number(subBytes(bytes, offset, 2));
                    if (normalizedLut) {
                        OutputTable.add(v / 65535);
                    } else {
                        OutputTable.add(v);
                    }
                    offset += 2;
                }
                OutputTables.add(OutputTable);
                if (OutputTables.size() >= maxItems) {
                    values.put("OutputTablesTruncated", true);
                    break;
                }
            }
            values.put("OutputTables", OutputTables);

            return values;
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, Object> lutAToB(byte[] bytes, boolean normalizedLut) {
        try {
            if (bytes == null || !"mAB ".equals(new String(subBytes(bytes, 0, 4)))) {
                return null;
            }
            Map<String, Object> values = new HashMap();
            values.put("lutAToB", "lutAToB");
            int InputChannelsNumber = uInt8Number(bytes[8]);
            values.put("InputChannelsNumber", InputChannelsNumber);
            int OutputChannelsNumber = uInt8Number(bytes[9]);
            values.put("OutputChannelsNumber", OutputChannelsNumber);
            long OffsetBCurve = uInt32Number(subBytes(bytes, 12, 4));
            values.put("OffsetBCurve", OffsetBCurve);
            long OffsetMatrix = uInt32Number(subBytes(bytes, 16, 4));
            values.put("OffsetMatrix", OffsetMatrix);
            long OffsetMCurve = uInt32Number(subBytes(bytes, 20, 4));
            values.put("OffsetMCurve", OffsetMCurve);
            long OffsetCLUT = uInt32Number(subBytes(bytes, 24, 4));
            values.put("OffsetCLUT", OffsetCLUT);
            long OffsetACurve = uInt32Number(subBytes(bytes, 28, 4));
            values.put("OffsetACurve", OffsetACurve);

            return values;
        } catch (Exception e) {
            return null;
        }
    }

    /*
        Encode value of Base Type
     */
    public static byte[] u8Fixed8Number(double d) {
        byte[] bytes = new byte[2];
        int integer = (int) d;
        bytes[0] = (byte) (integer & 0xFF);

        int fractional = (int) Math.round((d - integer) * 256);
        bytes[1] = (byte) (fractional & 0xFF);

        return bytes;
    }

    public static byte[] uInt16Number(int d) {
        return ByteTools.unsignedShortToBytes(d);
    }

    public static byte[] s15Fixed16Number(double d) {
        byte[] bytes = new byte[4];
        short s = (short) d;
        byte[] shortBytes = shortToBytes(s);
        System.arraycopy(shortBytes, 0, bytes, 0, 2);

        int fractional = (int) Math.round((d - s) * 65536);
        byte[] fractionalBytes = intToBytes(fractional);
        System.arraycopy(fractionalBytes, 2, bytes, 2, 2);

        return bytes;
    }

    public static byte[] u16Fixed16Number(double d) {
        byte[] bytes = new byte[4];
        int integer = (int) d;
        byte[] integerBytes = intToBytes(integer);
        System.arraycopy(integerBytes, 2, bytes, 0, 2);

        int fractional = (int) Math.round((d - integer) * 65536);
        byte[] fractionalBytes = intToBytes(fractional);
        System.arraycopy(fractionalBytes, 2, bytes, 2, 2);

        return bytes;
    }

    public static byte[] uInt32Number(long a) {
        return new byte[]{
            (byte) ((a >> 24) & 0xFF),
            (byte) ((a >> 16) & 0xFF),
            (byte) ((a >> 8) & 0xFF),
            (byte) (a & 0xFF)
        };
    }

    public static byte[] dateTimeBytes(String v) {
        try {
            String d = v.trim();
            if (d.length() == 17) {
                d = "19" + d;
            }
            if (d.length() != 19
                    || d.indexOf('-') != 4 || d.indexOf('-', 5) != 7
                    || d.indexOf(' ') != 10
                    || d.indexOf(':') != 13 || d.indexOf(':', 14) != 16) {
                return null;
            }
            byte[] bytes = new byte[12];

            int i = Integer.parseInt(d.substring(0, 4));
            byte[] vBytes = ByteTools.intToBytes(i);
            System.arraycopy(vBytes, 2, bytes, 0, 2);

            i = Integer.parseInt(d.substring(5, 7));
            vBytes = ByteTools.intToBytes(i);
            System.arraycopy(vBytes, 2, bytes, 2, 2);

            i = Integer.parseInt(d.substring(8, 10));
            vBytes = ByteTools.intToBytes(i);
            System.arraycopy(vBytes, 2, bytes, 4, 2);

            i = Integer.parseInt(d.substring(11, 13));
            vBytes = ByteTools.intToBytes(i);
            System.arraycopy(vBytes, 2, bytes, 6, 2);

            i = Integer.parseInt(d.substring(14, 16));
            vBytes = ByteTools.intToBytes(i);
            System.arraycopy(vBytes, 2, bytes, 8, 2);

            i = Integer.parseInt(d.substring(17, 19));
            vBytes = ByteTools.intToBytes(i);
            System.arraycopy(vBytes, 2, bytes, 10, 2);

            return bytes;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static double[] XYZNumberDoubles(String values) {
        try {
            String[] strings = StringTools.splitBySpace(values.replace("\n", " "));
            if (strings.length < 3) {
                return null;
            }
            double[] doubles = new double[3];
            for (int i = 0; i < doubles.length; i++) {
                doubles[i] = Double.parseDouble(strings[i]);
            }
            return doubles;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static byte[] XYZNumber(String values) {
        return XYZNumber(XYZNumberDoubles(values));
    }

    public static byte[] XYZNumber(double[] xyz) {
        byte[] bytes = new byte[12];
        byte[] xBytes = s15Fixed16Number(xyz[0]);
        System.arraycopy(xBytes, 0, bytes, 0, 4);
        byte[] yBytes = s15Fixed16Number(xyz[1]);
        System.arraycopy(yBytes, 0, bytes, 0, 4);
        byte[] zBytes = s15Fixed16Number(xyz[2]);
        System.arraycopy(zBytes, 0, bytes, 8, 4);
        return bytes;
    }

    public static byte[] illuminantType(String value) {
        for (String[] item : IlluminantTypes) {
            if (item[1].equals(value)) {
                return uInt32Number(Integer.parseInt(item[0]));
            }
        }
        return uInt32Number(0);
    }

    public static byte[] observerType(String value) {
        for (String[] item : ObserverTypes) {
            if (item[1].equals(value)) {
                return uInt32Number(Integer.parseInt(item[0]));
            }
        }
        return uInt32Number(0);
    }

    public static byte[] geometryType(String value) {
        for (String[] item : GeometryTypes) {
            if (item[1].equals(value)) {
                return uInt32Number(Integer.parseInt(item[0]));
            }
        }
        return uInt32Number(0);
    }

    /*
        Encode value of Tag Type
     */
    public static byte[] text(String value) {
        try {
            byte[] sigBytes = toBytes("text");
            byte[] valueBytes = toBytes(value);
            byte[] tagBytes = new byte[valueBytes.length + 8];
            System.arraycopy(sigBytes, 0, tagBytes, 0, 4);
            System.arraycopy(valueBytes, 0, tagBytes, 8, valueBytes.length);
            return tagBytes;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static byte[] signature(String value) {
        try {
            byte[] sigBytes = toBytes("sig ");
            byte[] valueBytes = toBytes(value);
            byte[] tagBytes = new byte[12];
            System.arraycopy(sigBytes, 0, tagBytes, 0, 4);
            System.arraycopy(valueBytes, 0, tagBytes, 8, Math.min(4, valueBytes.length));
            return tagBytes;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static byte[] dateTime(String value) {
        try {
            byte[] sigBytes = toBytes("dtim");
            byte[] valueBytes = dateTimeBytes(value);
            byte[] tagBytes = new byte[20];
            System.arraycopy(sigBytes, 0, tagBytes, 0, 4);
            System.arraycopy(valueBytes, 0, tagBytes, 8, 12);
            return tagBytes;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static double[][] XYZDoubles(String values) {
        try {
            String[] strings = StringTools.splitBySpace(values.replace("\n", " "));
            if (strings.length % 3 != 0) {
                return null;
            }
            double[][] doubles = new double[strings.length / 3][3];
            for (int i = 0; i < doubles.length; i++) {
                doubles[i][0] = Double.parseDouble(strings[i * 3]);
                doubles[i][1] = Double.parseDouble(strings[i * 3 + 1]);
                doubles[i][2] = Double.parseDouble(strings[i * 3 + 2]);
            }
            return doubles;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static byte[] XYZ(String values) {
        return XYZ(XYZDoubles(values));
    }

    public static byte[] XYZ(double[][] values) {
        try {
            byte[] sigBytes = toBytes("XYZ ");
            byte[] tagBytes = new byte[12 * values.length + 8];
            System.arraycopy(sigBytes, 0, tagBytes, 0, 4);
            for (int i = 0; i < values.length; i++) {
                double[] xyz = values[i];
                byte[] xyzBytes = XYZNumber(xyz);
                System.arraycopy(xyzBytes, 0, tagBytes, 8 + 12 * i, 12);
            }
            return tagBytes;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static byte[] multiLocalizedUnicode(IccTag tag, String newAscii) {
        try {
            Map<String, Object> values = (Map<String, Object>) tag.getValue();
            byte[] sigBytes = toBytes("desc");
            byte[] valueBytes = tag.getBytes();
            byte[] newAsciiBytes = toBytes(newAscii);
            byte[] newAsciiBytesLength = uInt32Number(newAsciiBytes.length);
            int AsciiLength = (int) values.get("AsciiLength");

            int size = tag.getBytes().length;
            byte[] tagBytes = new byte[size + newAsciiBytes.length - AsciiLength];
            System.arraycopy(sigBytes, 0, tagBytes, 0, 4);
            System.arraycopy(newAsciiBytesLength, 0, tagBytes, 8, 4);
            System.arraycopy(newAsciiBytes, 0, tagBytes, 12, newAsciiBytes.length);
            System.arraycopy(valueBytes, 12 + AsciiLength, tagBytes,
                    12 + newAsciiBytes.length, size - (12 + AsciiLength));
            return tagBytes;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static double[] curveDoubles(String values) {
        try {
            String[] strings = StringTools.splitBySpace(values.replace("\n", " "));
            double[] doubles = new double[strings.length];
            for (int i = 0; i < doubles.length; i++) {
                doubles[i] = Double.parseDouble(strings[i]);
            }
            return doubles;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static byte[] curve(String values) {
        return curve(curveDoubles(values));
    }

    public static byte[] curve(double[] values) {
        try {
            byte[] sigBytes = toBytes("curv");
            int count = values.length;
            if (count == 1) {
                if (values[0] == 1.0d) {
                    byte[] tagBytes = new byte[12];
                    System.arraycopy(sigBytes, 0, tagBytes, 0, 4);
                    byte[] countBytes = uInt32Number(0);
                    System.arraycopy(countBytes, 0, tagBytes, 8, 4);
                    return tagBytes;
                } else {
                    byte[] tagBytes = new byte[14];
                    System.arraycopy(sigBytes, 0, tagBytes, 0, 4);
                    byte[] countBytes = uInt32Number(1);
                    System.arraycopy(countBytes, 0, tagBytes, 8, 4);
                    byte[] valueBytes = u8Fixed8Number(values[0]);
                    System.arraycopy(valueBytes, 0, tagBytes, 12, 2);
                    return tagBytes;
                }

            } else {
                byte[] tagBytes = new byte[12 + 2 * count];
                System.arraycopy(sigBytes, 0, tagBytes, 0, 4);
                byte[] countBytes = uInt32Number(count);
                System.arraycopy(countBytes, 0, tagBytes, 8, 4);
                for (int i = 0; i < count; i++) {
                    byte[] valueBytes = uInt16Number((int) (values[i] * 65535));
                    System.arraycopy(valueBytes, 0, tagBytes, 12 + i * 2, 2);

                }
                return tagBytes;
            }

        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static byte[] viewingConditions(Map<String, Object> values) {
        try {
            byte[] sigBytes = toBytes("view");
            byte[] tagBytes = new byte[36];
            System.arraycopy(sigBytes, 0, tagBytes, 0, 4);

            double[] illuminant = (double[]) values.get("illuminant");
            byte[] illuminantBytes = XYZNumber(illuminant);
            System.arraycopy(illuminantBytes, 0, tagBytes, 8, 12);

            double[] surround = (double[]) values.get("surround");
            byte[] surroundBytes = XYZNumber(surround);
            System.arraycopy(surroundBytes, 0, tagBytes, 20, 12);

            String type = (String) values.get("illuminantType");
            byte[] typeBytes = illuminantType(type);
            System.arraycopy(typeBytes, 0, tagBytes, 32, 4);

            return tagBytes;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static byte[] measurement(Map<String, Object> values) {
        try {
            byte[] sigBytes = toBytes("meas");
            byte[] tagBytes = new byte[36];
            System.arraycopy(sigBytes, 0, tagBytes, 0, 4);

            String observer = (String) values.get("observer");
            byte[] observerBytes = observerType(observer);
            System.arraycopy(observerBytes, 0, tagBytes, 8, 4);

            double[] tristimulus = (double[]) values.get("tristimulus");
            byte[] tristimulusBytes = XYZNumber(tristimulus);
            System.arraycopy(tristimulusBytes, 0, tagBytes, 12, 12);

            String geometry = (String) values.get("geometry");
            byte[] geometryBytes = geometryType(geometry);
            System.arraycopy(geometryBytes, 0, tagBytes, 24, 4);

            double flare = (double) values.get("flare");
            byte[] flareBytes = u16Fixed16Number(flare);
            System.arraycopy(flareBytes, 0, tagBytes, 28, 4);

            String type = (String) values.get("illuminantType");
            byte[] typeBytes = illuminantType(type);
            System.arraycopy(typeBytes, 0, tagBytes, 32, 4);

            return tagBytes;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static double[] s15Fixed16ArrayDoubles(String values) {
        try {
            String[] strings = StringTools.splitBySpace(values.replace("\n", " "));
            double[] doubles = new double[strings.length];
            for (int i = 0; i < doubles.length; i++) {
                doubles[i] = Double.parseDouble(strings[i]);
            }
            return doubles;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static byte[] s15Fixed16Array(String values) {
        return s15Fixed16Array(s15Fixed16ArrayDoubles(values));
    }

    public static byte[] s15Fixed16Array(double[] values) {
        try {
            byte[] sigBytes = toBytes("sf32");
            int count = values.length;
            byte[] tagBytes = new byte[8 + 4 * count];
            System.arraycopy(sigBytes, 0, tagBytes, 0, 4);

            for (int i = 0; i < count; i++) {
                byte[] valueBytes = s15Fixed16Number(values[i]);
                System.arraycopy(valueBytes, 0, tagBytes, 8 + i * 4, 4);
            }
            return tagBytes;

        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    // Encode of LUT is not available since LUT is not editable in current version.
    // The specification of ICC profile is unfriendly for developers and users.
    // Can not understand why it is so complex and so boring~

    /*
        Display of Tag Type
     */
    public static String textDescriptionDisplay(Map<String, Object> values) {
        return (String) values.get("Ascii");
    }

    public static String textDescriptionFullDisplay(IccTag tag) {
        Map<String, Object> values = (Map<String, Object>) tag.getValue();
        return textDescriptionFullDisplay(values);
    }

    public static String textDescriptionFullDisplay(Map<String, Object> values) {
        String s = "";
        s += message("AsciiLength") + ": " + values.get("AsciiLength") + "\n";
        s += values.get("Ascii") + "\n\n";

        try {
            if (values.get("UnicodeCode") != null) {
                s += message("UnicodeCode") + ": " + values.get("UnicodeCode") + "  ";
                s += message("UnicodeLength") + ": " + values.get("UnicodeLength") + "\n";
                s += values.get("Unicode") + "\n\n";
            }

            if (values.get("ScriptCodeCode") != null) {
                s += message("ScriptCodeCode") + ": " + values.get("ScriptCodeCode") + "  ";
                s += message("ScriptCodeLength") + ": " + values.get("ScriptCodeLength") + "\n";
                s += values.get("ScriptCode");
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }

        return s;
    }

    public static String XYZNumberDisplay(IccTag tag) {
        double[][] values = (double[][]) tag.getValue();
        return XYZNumberDisplay(values);
    }

    public static String XYZNumberDisplay(double[][] values) {
        String s = "";
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                s += "\n";
            }
            double[] row = values[i];
            s += row[0] + Indent + row[1] + Indent + row[2];
        }
        return s;
    }

    public static String curveDisplay(IccTag tag) {
        double[] values = (double[]) tag.getValue();
        return curveDisplay(values);
    }

    public static String curveDisplay(double[] values) {
        String s = "";
        for (int i = 0; i < values.length; i++) {
            s += values[i] + Indent;
        }
        return s;
    }

    public static String viewingConditionsDisplay(IccTag tag) {
        Map<String, Object> values = (Map<String, Object>) tag.getValue();
        return viewingConditionsDisplay(values);
    }

    public static String viewingConditionsDisplay(Map<String, Object> values) {
        String s = "";
        double[] illuminant = (double[]) values.get("illuminant");
        s += message("Illuminant") + ": "
                + illuminant[0] + Indent + illuminant[1] + Indent + illuminant[2] + "\n";
        double[] surround = (double[]) values.get("surround");
        s += message("Surround") + ": "
                + surround[0] + Indent + surround[1] + Indent + surround[2] + "\n";
        String type = (String) values.get("illuminantType");
        s += message("Type") + ": " + type;
        return s;
    }

    public static String measurementDisplay(IccTag tag) {
        Map<String, Object> values = (Map<String, Object>) tag.getValue();
        return measurementDisplay(values);
    }

    public static String measurementDisplay(Map<String, Object> values) {
        String s = "";
        String observer = (String) values.get("observer");
        s += message("Observer") + ": " + observer + "\n";
        double[] tristimulus = (double[]) values.get("tristimulus");
        s += message("Tristimulus") + ": "
                + tristimulus[0] + Indent + tristimulus[1] + Indent + tristimulus[2] + "\n";
        String geometry = (String) values.get("geometry");
        s += message("Geometry") + ": " + geometry + "\n";
        double flare = (double) values.get("flare");
        s += message("Flare") + ": " + flare + "\n";
        String type = (String) values.get("illuminantType");
        s += message("Type") + ": " + type;
        return s;
    }

    public static String s15Fixed16ArrayDisplay(IccTag tag) {
        double[] values = (double[]) tag.getValue();
        return s15Fixed16ArrayDisplay(values);
    }

    public static String s15Fixed16ArrayDisplay(double[] values) {
        String s = "";
        for (int i = 0; i < values.length; i++) {
            if (i % 3 == 0 && i > 0 && i < values.length - 1) {
                s += "\n";
            }
            s += values[i] + Indent;
        }
        return s;
    }

    public static String lutDisplay(IccTag tag) {
        Map<String, Object> values = (Map<String, Object>) tag.getValue();
        return lutDisplay(values);
    }

    public static String lutDisplay(Map<String, Object> values) {
        String s = "";
        s += message("InputChannelsNumber") + ": " + values.get("InputChannelsNumber") + "  ";
        s += message("OutputChannelsNumber") + ": " + values.get("OutputChannelsNumber") + "  ";
        if (values.get("type") == null) {
            s += message("Type") + ": " + message("NotDecoded") + "\n";
        } else {
            String type = (String) values.get("type");
            s += message("Type") + ": " + type + "\n";
            if (type.equals("lut8") || type.equals("lut16")) {
                s += message("GridPointsNumber") + ": " + values.get("GridPointsNumber") + "\n";
                s += message("Matrix") + ": \n";
                s += Indent + values.get("e1") + Indent + values.get("e2") + Indent + values.get("e3") + "\n";
                s += Indent + values.get("e4") + Indent + values.get("e5") + Indent + values.get("e6") + "\n";
                s += Indent + values.get("e7") + Indent + values.get("e8") + Indent + values.get("e9") + "\n";

                if (type.equals("lut16")) {
                    s += message("InputTablesNumber") + ": " + values.get("InputTablesNumber") + "   ";
                    s += message("OutputTablesNumber") + ": " + values.get("OutputTablesNumber") + "\n";
                }
                s += message("InputTables") + ": " + "\n";
                List<List<Object>> InputTables = (List<List<Object>>) values.get("InputTables");
                for (List<Object> input : InputTables) {
                    s += Indent;
                    for (Object v : input) {
                        s += v + Indent;
                    }
                    s += "\n";
                }
                if (values.get("InputTablesTruncated") != null) {
                    s += Indent + "-----" + message("Truncated") + "-----" + "\n";
                }
                s += message("CLUTTables") + ": " + "\n";
                List<List<Object>> CLUTTables = (List<List<Object>>) values.get("CLUTTables");
                for (List<Object> GridPoint : CLUTTables) {
                    s += Indent;
                    for (Object v : GridPoint) {
                        s += v + Indent;
                    }
                    s += "\n";
                }
                if (values.get("CLUTTablesTruncated") != null) {
                    s += Indent + "-----" + message("Truncated") + "-----" + "\n";
                }
                s += message("OutputTables") + ": " + "\n";
                List<List<Object>> OutputTables = (List<List<Object>>) values.get("OutputTables");
                for (List<Object> output : OutputTables) {
                    s += Indent;
                    for (Object v : output) {
                        s += v + Indent;
                    }
                    s += "\n";
                }
                if (values.get("OutputTablesTruncated") != null) {
                    s += Indent + "-----" + message("Truncated") + "-----" + "\n";
                }
            } else {
                s += "OffsetBCurve:" + values.get("OffsetBCurve") + "\n";
                s += "OffsetMatrix:" + values.get("OffsetMatrix") + "\n";
                s += "OffsetMCurve:" + values.get("OffsetMCurve") + "\n";
                s += "OffsetCLUT:" + values.get("OffsetCLUT") + "\n";
                s += "OffsetACurve:" + values.get("OffsetACurve") + "\n";

            }
        }

        return s;
    }

    /*
        Read data of Tag Type
     */
}
