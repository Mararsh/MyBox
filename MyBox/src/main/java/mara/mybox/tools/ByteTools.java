package mara.mybox.tools;

import java.nio.charset.Charset;
import javafx.scene.control.IndexRange;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.data.FileEditInformation.Line_Break;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 17:43:53
 * @Description
 * @License Apache License Version 2.0
 */
public class ByteTools {

    public static int Invalid_Byte = -999;

    //  Big-Endian
    public static int bytesToInt(byte[] b) {
        return b[3] & 0xFF
                | (b[2] & 0xFF) << 8
                | (b[1] & 0xFF) << 16
                | (b[0] & 0xFF) << 24;
    }

    public static int bytesToUshort(byte[] b) {
        return b[1] & 0xFF
                | (b[0] & 0xFF) << 8;
    }

    public static byte[] intToBytes(int a) {
        return new byte[]{
            (byte) ((a >> 24) & 0xFF),
            (byte) ((a >> 16) & 0xFF),
            (byte) ((a >> 8) & 0xFF),
            (byte) (a & 0xFF)
        };
    }

    public static byte[] unsignedShortToBytes(int s) {
        return new byte[]{
            (byte) ((s >> 8) & 0xFF),
            (byte) (s & 0xFF)
        };
    }

    public static byte[] shortToBytes(short s) {
        return new byte[]{
            (byte) ((s >> 8) & 0xFF),
            (byte) (s & 0xFF)
        };
    }

    public static String byteToHex(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }
        return hex;
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
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
        return bytesToHexFormatWithCF(text.getBytes());
    }

    public static String bytesToHexFormatWithCF(byte[] bytes) {
        return bytesToHexFormatWithCF(bytes, bytesToHex("\n".getBytes()));
    }

    public static String bytesToHexFormatWithCF(byte[] bytes, String newLineHex) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex).append(" ");
        }
        String s = sb.toString();
        s = s.replace(newLineHex + " ", newLineHex + "\n");
        s = s.toUpperCase();
        return s;
    }

//    public static String bytesToHexFormat(byte[] bytes, String newLineValue) {
//        String s = bytesToHexFormat(bytes);
//        s = s.replace("\n", newLineHex.trim() + "\n");
//        logger.debug(newLineHex);
//        return s;
//    }
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
        s = s.toUpperCase();
        return s;
    }

    public static String bytesToHexFormat(byte[] bytes, int newLineWidth) {
        StringBuilder sb = new StringBuilder();
        int count = 1;
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex).append(" ");
            if (count % newLineWidth == 0) {
                sb.append("\n");
            }
            count++;
        }
        String s = sb.toString();
        s = s.toUpperCase();
        return s;
    }

    public static byte[] hexToBytes(String inHex) {
        try {
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
        } catch (Exception e) {
            return null;
        }
    }

    public static byte hexToByte(String inHex) {
        try {
            return (byte) Integer.parseInt(inHex, 16);
        } catch (Exception e) {
            return 0;
        }
    }

    public static byte hexToByteAnyway(String inHex) {
        try {
            return (byte) Integer.parseInt(inHex, 16);
        } catch (Exception e) {
            return new Byte("63"); // "?"
        }
    }

    public static int hexToInt(String inHex) {
        try {
            if (inHex.length() == 0 || inHex.length() > 2) {
                return Invalid_Byte;
            }
            String hex = inHex;
            if (inHex.length() == 1) {
                hex = "0" + hex;
            }
            return Integer.parseInt(hex, 16);
        } catch (Exception e) {
            return Invalid_Byte;
        }
    }

    public static String validateByteHex(String inHex) {
        try {
            if (inHex.length() == 0 || inHex.length() > 2) {
                return null;
            }
            String hex = inHex;
            if (inHex.length() == 1) {
                hex = "0" + hex;
            }
            Integer.parseInt(hex, 16);
            return hex;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isByteHex(String inHex) {
        try {
            if (inHex.length() != 2) {
                return false;
            }
            Integer.parseInt(inHex, 16);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isBytesHex(String inHex) {
        try {
            int hexlen = inHex.length();
            if (hexlen % 2 == 1) {
                return false;
            }
            for (int i = 0; i < hexlen; i += 2) {
                Integer.parseInt(inHex.substring(i, i + 2), 16);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String validateTextHex(String text) {
        try {
            String inHex = text.replaceAll(" ", "").replaceAll("\n", "").toUpperCase();
            int hexlen = inHex.length();
            if (hexlen % 2 == 1) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            String b;
            for (int i = 0; i < hexlen; i += 2) {
                b = inHex.substring(i, i + 2);
                Integer.parseInt(b, 16);
                sb.append(b).append(" ");
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] hexToBytesAnyway(String inHex) {
        try {
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
                result[j] = hexToByteAnyway(inHex.substring(i, i + 2));
                j++;
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] hexFormatToBytes(String hexFormat) {
        String hex = hexFormat.replaceAll(" ", "").replaceAll("\n", "");
        return hexToBytesAnyway(hex);
    }

    public static byte[] subBytes(byte[] bytes, int off, int length) {
        try {
            byte[] newBytes = new byte[length];
            System.arraycopy(bytes, off, newBytes, 0, length);
            return newBytes;
        } catch (Exception e) {
            logger.debug(bytes.length + " " + off + " " + length);
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

    public static int countNumber(byte[] bytes, byte[] subBytes) {
        return TextTools.countNumber(bytesToHex(bytes), bytesToHex(subBytes));
    }

    public static int countNumber(byte[] bytes, byte c) {
        return TextTools.countNumber(bytesToHex(bytes), byteToHex(c));
    }

    public static int countNumber(byte[] bytes, String hex) {
        return TextTools.countNumber(bytesToHex(bytes), hex);
    }

    public static int lineIndex(String lineText, Charset charset, int offset) {
        int hIndex = 0;
        byte[] cBytes;
        for (int i = 0; i < lineText.length(); i++) {
            char c = lineText.charAt(i);
            cBytes = String.valueOf(c).getBytes(charset);
            int clen = cBytes.length * 3;
            if (offset <= hIndex + clen) {
                return i;
            }
            hIndex += clen;
        }
        return -1;
    }

    public static IndexRange textIndex(String hex, Charset charset,
            IndexRange hexRange) {
        int hIndex = 0, cindex = 0;
        int cBegin = 0;
        int cEnd = 0;
        int hexBegin = hexRange.getStart();
        int hexEnd = hexRange.getEnd();
        if (hexBegin == 0 && hexEnd <= 0) {
            return new IndexRange(0, 0);
        }
        boolean gotStart = false, gotEnd = false;
        String[] lines = hex.split("\n");
        StringBuilder text = new StringBuilder();
        String lineText;
        for (String lineHex : lines) {
            lineText = new String(ByteTools.hexFormatToBytes(lineHex), charset);
            lineText = lineText.replaceAll("\n", " ").replaceAll("\r", " ");
            if (!gotStart && hexBegin >= hIndex && hexBegin <= (hIndex + lineHex.length())) {
                cBegin = cindex + lineIndex(lineText, charset, hexBegin - hIndex);
                gotStart = true;
            }
            if (hexEnd >= hIndex && hexEnd <= (hIndex + lineHex.length())) {
                cEnd = cindex + lineIndex(lineText, charset, hexEnd - hIndex) + 1;
                gotEnd = true;
                break;
            }
            hIndex += lineHex.length() + 1;
            cindex += lineText.length() + 1;
        }
        if (!gotStart) {
            cBegin = text.length() - 1;
        }
        if (!gotEnd) {
            cEnd = text.length();
        }
        if (cBegin > cEnd) {
            cEnd = cBegin;
        }
        return new IndexRange(cBegin, cEnd);
    }

    public static int indexOf(String hexString, String hexSubString, int initFrom) {
        if (hexString == null || hexSubString == null
                || hexString.length() < hexSubString.length()) {
            return -1;
        }
        int from = initFrom, pos = 0;
        while (pos >= 0) {
            pos = hexString.indexOf(hexSubString, from);
            if (pos % 2 == 0) {
                return pos;
            }
            from = pos + 1;
        }
        return -1;
    }

    public static String formatHex(String hexString,
            Line_Break lineBreak, int lineBreakWidth, String lineBreakValue) {
        String text = hexString;
        if (lineBreak == Line_Break.Width && lineBreakWidth > 0) {
            int step = 3 * lineBreakWidth;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length(); i += step) {
                if (i + step < text.length()) {
                    sb.append(text.substring(i, i + step - 1)).append("\n");
                } else {
                    sb.append(text.substring(i, text.length() - 1));
                }
            }
            text = sb.toString();
        } else if (lineBreakValue != null) {
            if (text.endsWith(lineBreakValue)) {
                text = text.replaceAll(lineBreakValue, lineBreakValue.trim() + "\n");
                text = text.substring(0, text.length() - 1);
            } else {
                text = text.replaceAll(lineBreakValue, lineBreakValue.trim() + "\n");
            }
        }
        return text;
    }

    public static int checkBytesValue(String string) {
        try {
            String strV = string.trim().toLowerCase();
            int unit = 1;
            if (strV.endsWith("k")) {
                unit = 1024;
                strV = strV.substring(0, strV.length() - 1);
            } else if (strV.endsWith("m")) {
                unit = 1024 * 1024;
                strV = strV.substring(0, strV.length() - 1);
            } else if (strV.endsWith("g")) {
                unit = 1024 * 1024 * 1024;
                strV = strV.substring(0, strV.length() - 1);
            }
            int v = Integer.valueOf(strV);
            if (v >= 0) {
                return v * unit;
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }


}
