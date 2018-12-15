package mara.mybox.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import mara.mybox.objects.FileEncoding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thridparty.EncodingDetect;

/**
 * @Author Mara
 * @CreateDate 2018-12-10 13:06:33
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FileEncodingTools {

    private static final Logger logger = LogManager.getLogger();

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

    public static boolean checkCharset(FileEncoding fileEncoding) {
        try {
            if (fileEncoding == null || fileEncoding.getFile() == null) {
                return false;
            }
            String setName;
            fileEncoding.setWithBom(false);
            try (FileInputStream inputStream = new FileInputStream(fileEncoding.getFile())) {
                byte[] header = new byte[4];
                if ((inputStream.read(header, 0, 4) != -1)) {
                    setName = checkCharsetByBom(header);
                    if (setName != null) {
                        fileEncoding.setCharset(Charset.forName(setName));
                        fileEncoding.setWithBom(true);
                        return true;
                    }
                }
            }
            setName = EncodingDetect.detect(fileEncoding.getFile());
            fileEncoding.setCharset(Charset.forName(setName));
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

    public static String readText(FileEncoding fileEncoding) {
        try {
            if (fileEncoding == null || fileEncoding.getFile() == null) {
                return null;
            }
            StringBuilder text = new StringBuilder();
            try (FileInputStream inputStream = new FileInputStream(fileEncoding.getFile());
                    InputStreamReader reader = new InputStreamReader(inputStream, fileEncoding.getCharset())) {
                if (fileEncoding.isWithBom()) {
                    inputStream.skip(bomSize(fileEncoding.getCharset().name()));
                }
                char[] buf = new char[512];
                int count;
                while ((count = reader.read(buf)) != -1) {
                    text.append(buf, 0, count);
                }
            }
            return text.toString();
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static boolean writeText(FileEncoding fileEncoding, String text) {
        try {
            try (FileOutputStream outputStream = new FileOutputStream(fileEncoding.getFile());
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, fileEncoding.getCharset())) {
                if (fileEncoding.isWithBom()) {
                    byte[] bytes = bomBytes(fileEncoding.getCharset().name());
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

    public static boolean convertCharset(FileEncoding sourceEncoding, FileEncoding targetEncoding) {
        try {
            if (sourceEncoding == null || sourceEncoding.getFile() == null
                    || sourceEncoding.getCharset() == null
                    || targetEncoding == null || targetEncoding.getFile() == null
                    || targetEncoding.getCharset() == null) {
                return false;
            }
            try (FileInputStream inputStream = new FileInputStream(sourceEncoding.getFile());
                    InputStreamReader reader = new InputStreamReader(inputStream, sourceEncoding.getCharset());
                    FileOutputStream outputStream = new FileOutputStream(targetEncoding.getFile());
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, targetEncoding.getCharset())) {
                if (sourceEncoding.isWithBom()) {
                    inputStream.skip(bomSize(sourceEncoding.getCharset().name()));
                }
                if (targetEncoding.isWithBom()) {
                    byte[] bytes = bomBytes(targetEncoding.getCharset().name());
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

}
