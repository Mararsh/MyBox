package mara.mybox.tools;

import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Random;
import javafx.scene.control.IndexRange;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 17:43:53
 * @Description
 * @License Apache License Version 2.0
 */
public class ValueTools {

    private static final Logger logger = LogManager.getLogger();

    public static int getRandomInt(int max) {
        Random r = new Random();
        return r.nextInt(max);
    }

    public static String fillNumber(int value, int digit) {
        String v = value + "";
        for (int i = v.length(); i < digit; i++) {
            v = "0" + v;
        }
        return v;
    }

    public static String formatData(long data) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(data);
    }

    public static float roundFloat2(float fvalue) {
        return (float) Math.round(fvalue * 100.0) / 100.0f;
    }

    public static float roundFloat3(float fvalue) {
        return (float) Math.round(fvalue * 1000.0) / 1000.0f;
    }

    public static float roundFloat5(float fvalue) {
        return (float) Math.round(fvalue * 100000.0) / 100000.0f;
    }

    public static double roundDouble3(double invalue) {
        return (double) Math.round(invalue * 1000.0) / 1000.0;
    }

    public static double roundDouble2(double invalue) {
        return (double) Math.round(invalue * 100.0) / 100.0;
    }

    public static double roundDouble4(double invalue) {
        return (double) Math.round(invalue * 10000.0) / 10000.0;
    }

    public static double roundDouble5(double invalue) {
        return (double) Math.round(invalue * 100000.0) / 100000.0;
    }

    public static float[] matrix2Array(float[][] m) {
        if (m == null || m.length == 0 || m[0].length == 0) {
            return null;
        }
        int h = m.length;
        int w = m[0].length;
        float[] a = new float[w * h];
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                a[j * w + i] = m[j][i];
            }
        }
        return a;
    }

    public static float[][] array2Matrix(float[] a, int w) {
        if (a == null || a.length == 0 || w < 1) {
            return null;
        }
        int h = a.length / w;
        if (h < 1) {
            return null;
        }
        float[][] m = new float[h][w];
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                m[j][i] = a[j * w + i];
            }
        }
        return m;
    }

    public static long getAvaliableMemory() {
        Runtime r = Runtime.getRuntime();
        return r.maxMemory() - (r.totalMemory() - r.freeMemory());
    }

    public static long getAvaliableMemoryMB() {
        return getAvaliableMemory() / (1024 * 1024);
    }

    public static String byteToHex(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }
        return hex;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String stringToHexFormat(String text) {
        return bytesToHexFormat(text.getBytes());
    }

    public static String bytesToHexFormat(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex).append(" ");
        }
        String s = sb.toString();
        String ret = bytesToHex("\n".getBytes());
        s = s.replace(ret + " ", ret + "\n");
        s = s.toUpperCase();
        return s;
    }

    public static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    public static byte[] hexToBytes(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    public static byte[] subBytes(byte[] bytes, int off, int length) {
        try {
            byte[] newBytes = new byte[length];
            System.arraycopy(bytes, off, newBytes, 0, length);
            return newBytes;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static byte[] mergeBytes(byte[] bytes1, byte[] bytes2) {
        try {
            byte[] bytes3 = new byte[bytes1.length + bytes2.length];
            System.arraycopy(bytes1, 0, bytes3, 0, bytes1.length);
            System.arraycopy(bytes2, 0, bytes3, bytes1.length, bytes2.length);
            return bytes3;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static IndexRange hexIndex(String text, Charset charset, IndexRange textRange) {
        int hIndex = 0, hBegin = 0, hEnd = 0;
        int cBegin = textRange.getStart();
        int cEnd = textRange.getEnd();
        if (cBegin == 0 && cEnd == 0) {
            return new IndexRange(0, 0);
        }
        byte[] cBytes;
        for (int i = 0; i < text.length(); i++) {
            if (cBegin == i) {
                hBegin = hIndex;
            }
            if (cEnd == i) {
                hEnd = hIndex;
            }
            cBytes = String.valueOf(text.charAt(i)).getBytes(charset);
            hIndex += cBytes.length * 3;
        }
        if (cBegin == text.length()) {
            hBegin = hIndex;
        }
        if (cEnd == text.length()) {
            hEnd = hIndex;
        }
        return new IndexRange(hBegin, hEnd);
    }

    public static int countNumber(String string, String subString) {
        int fromIndex = 0;
        int count = 0;
        while (true) {
            int index = string.indexOf(subString, fromIndex);
            if (index < 0) {
                break;
            }
            fromIndex = index + 1;
            count++;
        }
        return count;
    }

}
