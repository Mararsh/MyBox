package mara.mybox.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javafx.scene.control.IndexRange;
import mara.mybox.objects.FileEditInformation;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.FileEditInformation.Line_Break;

import thridparty.EncodingDetect;

/**
 * @Author Mara
 * @CreateDate 2018-12-10 13:06:33
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class TextTools {

    public static List<Charset> getCharsets() {
        List<Charset> sets = new ArrayList<>();
        sets.addAll(Arrays.asList(Charset.forName("UTF-8"),
                Charset.forName("GBK"), Charset.forName("GB2312"),
                Charset.forName("GB18030"), Charset.forName("BIG5"),
                Charset.forName("ASCII"), Charset.forName("ISO-8859-1"),
                Charset.forName("UTF-16"), Charset.forName("UTF-16BE"), Charset.forName("UTF-16LE"),
                Charset.forName("UTF-32"), Charset.forName("UTF-32BE"), Charset.forName("UTF-32LE")
        ));
        if (sets.contains(Charset.defaultCharset())) {
            sets.remove(Charset.defaultCharset());
        }
        sets.add(0, Charset.defaultCharset());

        Map<String, Charset> all = Charset.availableCharsets();
        for (Charset set : all.values()) {
            if (Charset.isSupported(set.name()) && !sets.contains(set)) {
                sets.add(set);
            }
        }
        return sets;
    }

    public static List<String> getCharsetNames() {
        try {
            List<Charset> sets = getCharsets();
            List<String> setNames = new ArrayList();
            for (Charset set : sets) {
                setNames.add(set.name());
            }
            return setNames;
        } catch (Exception e) {
            return null;
        }
    }

    public static String checkCharsetByBom(byte[] bytes) {
        if (bytes.length < 2) {
            return null;
        }
        if ((bytes[0] == (byte) 0xFE) && (bytes[1] == (byte) 0xFF)) {
            return "UTF-16BE";
        }
        if ((bytes[0] == (byte) 0xFF) && (bytes[1] == (byte) 0xFE)) {
            return "UTF-16LE";
        }
        if (bytes.length < 3) {
            return null;
        }
        if ((bytes[0] == (byte) 0xEF) && (bytes[1] == (byte) 0xBB) && (bytes[2] == (byte) 0xBF)) {
            return "UTF-8";
        }
        if (bytes.length < 4) {
            return null;
        }
        if ((bytes[0] == (byte) 0x00) && (bytes[1] == (byte) 0x00)
                && (bytes[2] == (byte) 0xFE) && (bytes[3] == (byte) 0xFF)) {
            return "UTF-32BE";
        }
        if ((bytes[0] == (byte) 0xFF) && (bytes[1] == (byte) 0xFE)
                && (bytes[2] == (byte) 0x00) && (bytes[3] == (byte) 0x00)) {
            return "UTF-32LE";
        }
        return null;
    }

    public static boolean checkCharset(FileEditInformation info) {
        try {
            if (info == null || info.getFile() == null) {
                return false;
            }
            String setName;
            info.setWithBom(false);
            try (FileInputStream inputStream = new FileInputStream(info.getFile())) {
                byte[] header = new byte[4];
                if ((inputStream.read(header, 0, 4) != -1)) {
                    setName = checkCharsetByBom(header);
                    if (setName != null) {
                        info.setCharset(Charset.forName(setName));
                        info.setWithBom(true);
                        return true;
                    }
                }
            }
            setName = EncodingDetect.detect(info.getFile());
            info.setCharset(Charset.forName(setName));
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static int bomSize(String charset) {
        switch (charset) {
            case "UTF-16":
            case "UTF-16BE":
                return 2;
            case "UTF-16LE":
                return 2;
            case "UTF-8":
                return 3;
            case "UTF-32":
            case "UTF-32BE":
                return 4;
            case "UTF-32LE":
                return 4;
        }
        return 0;
    }

    public static String bomHex(String charset) {
        switch (charset) {
            case "UTF-16":
            case "UTF-16BE":
                return "FE FF";
            case "UTF-16LE":
                return "FF FE";
            case "UTF-8":
                return "EF BB BF";
            case "UTF-32":
            case "UTF-32BE":
                return "00 00 FE FF";
            case "UTF-32LE":
                return "FF FE 00 00";
        }
        return null;
    }

    public static byte[] bomBytes(String charset) {
        switch (charset) {
            case "UTF-16":
            case "UTF-16BE":
                return new byte[]{(byte) 0xFE, (byte) 0xFF};
            case "UTF-16LE":
                return new byte[]{(byte) 0xFF, (byte) 0xFE};
            case "UTF-8":
                return new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            case "UTF-32":
            case "UTF-32BE":
                return new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF};
            case "UTF-32LE":
                return new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00};
        }
        return null;
    }

    public static String readText(FileEditInformation info) {
        try {
            if (info == null || info.getFile() == null) {
                return null;
            }
            StringBuilder text = new StringBuilder();
            try (FileInputStream inputStream = new FileInputStream(info.getFile());
                    InputStreamReader reader = new InputStreamReader(inputStream, info.getCharset())) {
                if (info.isWithBom()) {
                    inputStream.skip(bomSize(info.getCharset().name()));
                }
                char[] buf = new char[512];
                int len;
                while ((len = reader.read(buf)) != -1) {
                    text.append(buf, 0, len);
                }
            }
            return text.toString();
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static boolean writeText(FileEditInformation info, String text) {
        try {
            try (FileOutputStream outputStream = new FileOutputStream(info.getFile());
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, info.getCharset())) {
                if (info.isWithBom()) {
                    byte[] bytes = bomBytes(info.getCharset().name());
                    outputStream.write(bytes);
                }
                writer.write(text);
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean convertCharset(FileEditInformation source, FileEditInformation target) {
        try {
            if (source == null || source.getFile() == null
                    || source.getCharset() == null
                    || target == null || target.getFile() == null
                    || target.getCharset() == null) {
                return false;
            }
            try (FileInputStream inputStream = new FileInputStream(source.getFile());
                    InputStreamReader reader = new InputStreamReader(inputStream, source.getCharset());
                    FileOutputStream outputStream = new FileOutputStream(target.getFile());
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, target.getCharset())) {
                if (source.isWithBom()) {
                    inputStream.skip(bomSize(source.getCharset().name()));
                }
                if (target.isWithBom()) {
                    byte[] bytes = bomBytes(target.getCharset().name());
                    outputStream.write(bytes);
                }
                char[] buf = new char[512];
                int count;
                while ((count = reader.read(buf)) != -1) {
                    String text = new String(buf, 0, count);
                    writer.write(text);
                }
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static boolean convertLineBreak(FileEditInformation source, FileEditInformation target) {
        try {
            if (source == null || source.getFile() == null
                    || source.getCharset() == null
                    || target == null || target.getFile() == null
                    || target.getCharset() == null) {
                return false;
            }
            String sourceLineBreak = TextTools.lineBreakValue(source.getLineBreak());
            String taregtLineBreak = TextTools.lineBreakValue(target.getLineBreak());
            if (sourceLineBreak == null || taregtLineBreak == null) {
                return false;
            }
            if (source.getLineBreak() == target.getLineBreak()) {
                return FileTools.copyFile(source.getFile(), target.getFile(), true, true);
            }
            try (FileInputStream inputStream = new FileInputStream(source.getFile());
                    InputStreamReader reader = new InputStreamReader(inputStream, source.getCharset());
                    FileOutputStream outputStream = new FileOutputStream(target.getFile());
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, target.getCharset())) {
                char[] buf = new char[4096];
                int count;
                while ((count = reader.read(buf)) != -1) {
                    String text = new String(buf, 0, count);
                    text = text.replaceAll(sourceLineBreak, taregtLineBreak);
                    writer.write(text);
                }
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public static IndexRange hexIndex(String text, Charset charset,
            String lineBreakValue, IndexRange textRange) {
        int hIndex = 0;
        int hBegin = 0;
        int hEnd = 0;
        int cBegin = textRange.getStart();
        int cEnd = textRange.getEnd();
        if (cBegin == 0 && cEnd == 0) {
            return new IndexRange(0, 0);
        }
        int lbLen = lineBreakValue.getBytes(charset).length * 3 + 1;
        for (int i = 0; i < text.length(); i++) {
            if (cBegin == i) {
                hBegin = hIndex;
            }
            if (cEnd == i) {
                hEnd = hIndex;
            }
            char c = text.charAt(i);
            if (c == '\n') {
                hIndex += lbLen;
            } else {
                hIndex += String.valueOf(c).getBytes(charset).length * 3;
            }
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
        if (string == null || string.isEmpty()
                || subString == null || subString.isEmpty()
                || string.length() < subString.length()) {
            return 0;
        }
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

    public static int[] lastAndCount(String string, String subString) {
        int[] results = new int[2];
        results[0] = -1;
        results[1] = 0;
        if (string == null || string.isEmpty()
                || subString == null || subString.isEmpty()
                || string.length() < subString.length()) {
            return results;
        }
        int fromIndex = 0;
        int count = 0;
        int last = -1;
        while (true) {
            int index = string.indexOf(subString, fromIndex);
            if (index < 0) {
                break;
            }
            last = index;
            fromIndex = index + 1;
            count++;
        }
        results[0] = last;
        results[1] = count;
        return results;
    }

    public static Line_Break checkLineBreak(File file) {
        try {
            if (file == null) {
                return Line_Break.LF;
            }
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file))) {
                int c;
                boolean cr = false;
                while ((c = reader.read()) != -1) {
                    if ((char) c == '\r') {
                        cr = true;
                    } else if ((char) c == '\n') {
                        if (cr) {
                            return Line_Break.CRLF;
                        } else {
                            return Line_Break.LF;
                        }
                    } else if (c != 0 && cr) {
                        return Line_Break.CR;
                    }
                }
            }
            return Line_Break.LF;
        } catch (Exception e) {
            logger.debug(e.toString());
            return Line_Break.LF;
        }
    }

    public static String lineBreakValue(Line_Break lb) {
        switch (lb) {
            case LF:
                return "\n";
            case CRLF:
                return "\r\n";
            case CR:
                return "\r";
            default:
                return "\n";
        }
    }

    public static byte[] lineBreakBytes(Line_Break lb, Charset charset) {
        switch (lb) {
            case LF:
                return "\n".getBytes(charset);
            case CRLF:
                return "\r\n".getBytes(charset);
            case CR:
                return "\r".getBytes(charset);
            default:
                return "\n".getBytes(charset);
        }
    }

    public static String lineBreakHex(Line_Break lb, Charset charset) {
        return ByteTools.bytesToHex(lineBreakBytes(lb, charset));
    }

    public static String lineBreakHexFormat(Line_Break lb, Charset charset) {
        return ByteTools.bytesToHexFormat(lineBreakBytes(lb, charset));
    }

}
