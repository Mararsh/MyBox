package mara.mybox.data2d;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.List;
import mara.mybox.data.FindReplaceString;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.data2d.writer.DataFileTextWriter;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.tools.TextTools.validLine;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class DataFileText extends DataFile {

    public static final String CommentsMarker = "#";

    public DataFileText() {
        dataType = DataType.Texts;
    }

    public String[] delimters() {
        String[] delimiters = {",", " ", "    ", "        ", "\t", "|", "@",
            ";", ":", "*", "%", "$", "_", "&", "-", "=", "!", "\"",
            "'", "<", ">", "#"};
        return delimiters;
    }

    @Override
    public boolean checkForLoad() {
        if (charset == null && file != null) {
            charset = TextFileTools.charset(file);
        }
        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = guessDelimiter();
        }
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = ",";
        }
        return super.checkForLoad();
    }

    public String guessDelimiter() {
        if (file == null) {
            return null;
        }
        String[] delimiters = delimters();
        if (charset == null) {
            charset = TextFileTools.charset(file);
        }
        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }
        File validFile = FileTools.removeBOM(task, file);
        if (validFile == null || (task != null && !task.isWorking())) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(validFile, charset))) {
            String line1 = reader.readLine();
            while (line1 != null && !validLine(line1)) {
                line1 = reader.readLine();
            }
            if (line1 == null) {
                return null;
            }
            int[] count1 = new int[delimiters.length];
            int maxCount1 = 0, maxCountIndex1 = -1;
            for (int i = 0; i < delimiters.length; i++) {
                if (task != null && !task.isWorking()) {
                    reader.close();
                    return null;
                }
                count1[i] = FindReplaceString.count(task, line1, delimiters[i]);
                if (task != null && !task.isWorking()) {
                    reader.close();
                    return null;
                }
//                MyBoxLog.console(">>" + values[i] + "<<<   " + count1[i]);
                if (count1[i] > maxCount1) {
                    maxCount1 = count1[i];
                    maxCountIndex1 = i;
                }
            }
//            MyBoxLog.console(maxCount1);
            String line2 = reader.readLine();
            while (line2 != null && !validLine(line2)) {
                if (task != null && !task.isWorking()) {
                    reader.close();
                    return null;
                }
                line2 = reader.readLine();
            }
            if (line2 == null) {
                if (maxCountIndex1 >= 0) {
                    return delimiters[maxCountIndex1];
                } else {
                    hasHeader = false;
                    return null;
                }
            }
            int[] count2 = new int[delimiters.length];
            int maxCount2 = 0, maxCountIndex2 = -1;
            for (int i = 0; i < delimiters.length; i++) {
                if (task != null && !task.isWorking()) {
                    reader.close();
                    return null;
                }
                count2[i] = FindReplaceString.count(task, line2, delimiters[i]);
                if (task != null && !task.isWorking()) {
                    reader.close();
                    return null;
                }
//                MyBoxLog.console(">>" + values[i] + "<<<   " + count1[i]);
                if (count1[i] == count2[i] && count2[i] > maxCount2) {
                    maxCount2 = count2[i];
                    maxCountIndex2 = i;
                }
            }
//            MyBoxLog.console(maxCount2);
            if (maxCountIndex2 >= 0) {
                return delimiters[maxCountIndex2];
            } else {
                if (maxCountIndex1 >= 0) {
                    return delimiters[maxCountIndex1];
                } else {
                    hasHeader = false;
                    return null;
                }
            }
        } catch (Exception e) {
//            MyBoxLog.console(e.toString());
        }
        hasHeader = false;
        return null;
    }

    public List<String> parseFileLine(String line) {
        return TextTools.parseLine(line, delimiter);
    }

    public List<String> readValidLine(BufferedReader reader) {
        if (reader == null) {
            return null;
        }
        List<String> values = null;
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                values = parseFileLine(line);
                if (values != null && !values.isEmpty()) {
                    break;
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
        }
        return values;
    }

    @Override
    public Data2DWriter selfWriter() {
        DataFileTextWriter writer = new DataFileTextWriter();
        writer.setCharset(charset)
                .setDelimiter(delimiter)
                .setWriteHeader(hasHeader)
                .setTargetData(this)
                .setDataName(dataName)
                .setPrintFile(file)
                .setColumns(columns)
                .setHeaderNames(columnNames())
                .setRecordTargetFile(true)
                .setRecordTargetData(true);
        return writer;
    }

}
