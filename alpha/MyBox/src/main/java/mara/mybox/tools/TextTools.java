package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
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
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.data.TextEditInformation;
import mara.mybox.dev.MyBoxLog;
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
            List<String> setNames = new ArrayList<>();
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
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(info.getFile()))) {
                byte[] header = new byte[4];
                int bufLen;
                if ((bufLen = inputStream.read(header, 0, 4)) > 0) {
                    header = ByteTools.subBytes(header, 0, bufLen);
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
//            MyBoxLog.debug(e.toString());
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
                return new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE,
                    (byte) 0xFF};
            case "UTF-32LE":
                return new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0x00,
                    (byte) 0x00};
        }
        return null;
    }

    public static String readText(File file) {
        return readText(new TextEditInformation(file));
    }

    public static String readText(FileEditInformation info) {
        try {
            if (info == null || info.getFile() == null) {
                return null;
            }
            StringBuilder text = new StringBuilder();
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(info.getFile()));
                     InputStreamReader reader = new InputStreamReader(inputStream, info.getCharset())) {
                if (info.isWithBom()) {
                    inputStream.skip(bomSize(info.getCharset().name()));
                }
                char[] buf = new char[512];
                int len;
                while ((len = reader.read(buf)) > 0) {
                    text.append(buf, 0, len);
                }
            }
            return text.toString();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static boolean writeText(FileEditInformation info, String text) {
        try ( BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(info.getFile()));
                 OutputStreamWriter writer = new OutputStreamWriter(outputStream, info.getCharset())) {
            if (info.isWithBom()) {
                byte[] bytes = bomBytes(info.getCharset().name());
                outputStream.write(bytes);
            }
            writer.write(text);
            return true;

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(source.getFile()));
                     InputStreamReader reader = new InputStreamReader(inputStream, source.getCharset());
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(target.getFile()));
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
                while ((count = reader.read(buf)) > 0) {
                    String text = new String(buf, 0, count);
                    writer.write(text);
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
                return FileCopyTools.copyFile(source.getFile(), target.getFile(), true, true);
            }
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(source.getFile()));
                     InputStreamReader reader = new InputStreamReader(inputStream, source.getCharset());
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(target.getFile()));
                     OutputStreamWriter writer = new OutputStreamWriter(outputStream, target.getCharset())) {
                char[] buf = new char[4096];
                int count;
                while ((count = reader.read(buf)) > 0) {
                    String text = new String(buf, 0, count);
                    text = text.replaceAll(sourceLineBreak, taregtLineBreak);
                    writer.write(text);
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static long checkCharsNumber(String string) {
        try {
            String strV = string.trim().toLowerCase();
            long unit = 1;
            if (strV.endsWith("k")) {
                unit = 1024;
                strV = strV.substring(0, strV.length() - 1);
            } else if (strV.endsWith("m")) {
                unit = 1024 * 1024;
                strV = strV.substring(0, strV.length() - 1);
            } else if (strV.endsWith("g")) {
                unit = 1024 * 1024 * 1024L;
                strV = strV.substring(0, strV.length() - 1);
            }
            double v = Double.valueOf(strV.trim());
            if (v >= 0) {
                return Math.round(v * unit);
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    public static List<File> convert(FileEditInformation source, FileEditInformation target, int maxLines) {
        try {
            if (source == null || source.getFile() == null
                    || source.getCharset() == null
                    || target == null || target.getFile() == null
                    || target.getCharset() == null) {
                return null;
            }
            String sourceLineBreak = TextTools.lineBreakValue(source.getLineBreak());
            String taregtLineBreak = TextTools.lineBreakValue(target.getLineBreak());
            if (sourceLineBreak == null || taregtLineBreak == null) {
                return null;
            }
            List<File> files = new ArrayList<>();
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(source.getFile()));
                     BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, source.getCharset()))) {
                if (source.isWithBom()) {
                    inputStream.skip(bomSize(source.getCharset().name()));
                }
                if (maxLines > 0) {
                    int fileIndex = 0;
                    while (true) {
                        int linesNumber = 0;
                        String line = null;
                        File file = new File(FileNameTools.appendName(target.getFile().getAbsolutePath(), "-" + (++fileIndex)));
                        try ( BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                                 OutputStreamWriter writer = new OutputStreamWriter(outputStream, target.getCharset())) {
                            if (target.isWithBom()) {
                                byte[] bytes = bomBytes(target.getCharset().name());
                                outputStream.write(bytes);
                            }
                            while ((line = bufferedReader.readLine()) != null) {
                                writer.write(line + taregtLineBreak);
                                linesNumber++;
                                if (linesNumber >= maxLines) {
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                            return null;
                        }
                        if (file.exists() && file.length() > 0) {
                            files.add(file);
                        }
                        if (line == null) {
                            break;
                        }
                    }
                } else {
                    File file = target.getFile();
                    try ( BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                             OutputStreamWriter writer = new OutputStreamWriter(outputStream, target.getCharset())) {
                        if (target.isWithBom()) {
                            byte[] bytes = bomBytes(target.getCharset().name());
                            outputStream.write(bytes);
                        }
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            writer.write(line + taregtLineBreak);
                        }
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                        return null;
                    }
                    if (file.exists() && file.length() > 0) {
                        files.add(file);
                    }
                }
            } catch (Exception e) {
                MyBoxLog.debug(e.toString());
                return null;
            }
            return files;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
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
        for (int i = 0; i < text.length(); ++i) {
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

    public static Line_Break checkLineBreak(File file) {
        try ( InputStreamReader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)))) {
            int c;
            boolean cr = false;
            while ((c = reader.read()) > 0) {
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
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return Line_Break.LF;
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

    public static String delimiterText(String delimiter) {
        if (delimiter == null) {
            return null;
        }
        String text;
        switch (delimiter.toLowerCase()) {
            case "tab":
                text = "\t";
                break;
            case "blank":
                text = " ";
                break;
            case "blank4":
                text = "    ";
                break;
            case "blank8":
                text = "        ";
                break;
            default:
                text = delimiter;
                break;
        }
        return text;
    }

    public static String dataText(Object[][] data, String delimiter) {
        return dataText(data, delimiter, null, null);
    }

    public static String dataText(Object[][] data, String delimiter,
            List<String> colsNames, List<String> rowsNames) {
        if (data == null || data.length == 0 || delimiter == null) {
            return "";
        }
        try {
            StringBuilder s = new StringBuilder();
            String p = delimiterText(delimiter);
            int rowsNumber = data.length;
            int colsNumber = data[0].length;
            if (colsNames != null && colsNames.size() >= colsNumber) {
                if (rowsNames != null) {
                    s.append(p);
                }
                for (int j = 0; j < colsNumber; j++) {
                    s.append(colsNames.get(j));
                    if (j < colsNumber - 1) {
                        s.append(p);
                    }
                }
                s.append("\n");
            }
            for (int i = 0; i < rowsNumber; i++) {
                if (rowsNames != null) {
                    s.append(rowsNames.get(i)).append(p);
                }
                for (int j = 0; j < colsNumber; j++) {
                    s.append(data[i][j]);
                    if (j < colsNumber - 1) {
                        s.append(p);
                    }
                }
                s.append("\n");
            }
            return s.toString();
        } catch (Exception e) {
            MyBoxLog.console(e);
            return "";
        }
    }

}
