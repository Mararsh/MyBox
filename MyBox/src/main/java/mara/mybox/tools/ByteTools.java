package mara.mybox.tools;

import static mara.mybox.objects.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 17:43:53
 * @Description
 * @License Apache License Version 2.0
 */
public class ByteTools {

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

    public static String bytesToHexFormat(byte[] bytes, String newLineHex) {
        String s = bytesToHexFormat(bytes);
        s = s.replace("0A ", newLineHex.trim() + "\n");
        return s;
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
        s = s.toUpperCase();
        return s;
    }

    public static byte hexToByte(String inHex) {
        try {
            return (byte) Integer.parseInt(inHex, 16);
        } catch (Exception e) {
            return new Byte("63"); // "?"
        }
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

    public static byte[] hexFormatToBytes(String hexFormat) {
        String hex = hexFormat.replaceAll(" ", "").replaceAll("\n", "");
        return hexToBytes(hex);
    }

//    public static String hexFormatAdjust(String hexFormat, String newLineHex) {
//        StringBuilder sb = new StringBuilder();
//        String[] lines = hexFormat.split("\n");
//        for (String line: lines) {
//
//        }
//        for (int i = 0; i < bytes.length; i++) {
//            String hex = Integer.toHexString(bytes[i] & 0xFF);
//            if (hex.length() < 2) {
//                sb.append(0);
//            }
//            sb.append(hex).append(" ");
//        }
//        String s = sb.toString();
//        s = s.replace(newLineHex + " ", newLineHex + "\n");
//        s = s.toUpperCase();
//        return s;
//    }
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

    public static int countNumber(byte[] bytes, byte[] subBytes) {
        return TextTools.countNumber(bytesToHex(bytes), bytesToHex(subBytes));
    }

    public static int countNumber(byte[] bytes, byte c) {
        return TextTools.countNumber(bytesToHex(bytes), byteToHex(c));
    }

    public static int countNumber(byte[] bytes, String hex) {
        return TextTools.countNumber(bytesToHex(bytes), hex);
    }

}
