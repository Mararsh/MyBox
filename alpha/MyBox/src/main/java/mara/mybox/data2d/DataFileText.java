package mara.mybox.data2d;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import mara.mybox.data.FindReplaceString;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTmpTools;
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
        type = Type.Texts;
    }

    public DataFileText(File file) {
        type = Type.Texts;
        this.file = file;
        guessDelimiter();
    }

    public String[] delimters() {
        String[] delimiters = {",", " ", "    ", "        ", "\t", "|", "@",
            "#", ";", ":", "*", "%", "$", "_", "&", "-", "=", "!", "\"",
            "'", "<", ">"};
        return delimiters;
    }

    public void setOptions(boolean hasHeader, Charset charset, String delimiter) {
        options = new HashMap<>();
        options.put("hasHeader", hasHeader);
        options.put("charset", charset);
        options.put("delimiter", delimiter);
    }

    @Override
    public void applyOptions() {
        try {
            if (options == null) {
                return;
            }
            if (options.containsKey("hasHeader")) {
                hasHeader = (boolean) (options.get("hasHeader"));
            }
            if (options.containsKey("charset")) {
                charset = (Charset) (options.get("charset"));
            }
            if (options.containsKey("delimiter")) {
                delimiter = (String) (options.get("delimiter"));
            }
        } catch (Exception e) {
        }
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

    public final String guessDelimiter() {
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
                count1[i] = FindReplaceString.count(line1, delimiters[i]);
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
                count2[i] = FindReplaceString.count(line2, delimiters[i]);
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
    public boolean savePageData(Data2D targetData) {
        if (targetData == null || !(targetData instanceof DataFileText)) {
            return false;
        }
        DataFileText targetTextFile = (DataFileText) targetData;
        File tmpFile = FileTmpTools.getTempFile();
        File tFile = targetTextFile.getFile();
        if (tFile == null) {
            return false;
        }
        targetTextFile.checkForLoad();
        Charset tCharset = targetTextFile.getCharset();
        String tDelimiter = targetTextFile.getDelimiter();
        checkForLoad();
        boolean tHasHeader = targetTextFile.isHasHeader();
        if (file != null && file.exists() && file.length() > 0) {
            File validFile = FileTools.removeBOM(task, file);
            if (validFile == null || (task != null && !task.isWorking())) {
                return false;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(validFile, charset));
                    BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, tCharset, false))) {
                List<String> colsNames = columnNames();
                if (hasHeader) {
                    readValidLine(reader);
                }
                if (tHasHeader && colsNames != null) {
                    TextFileTools.writeLine(task, writer, colsNames, tDelimiter);
                } else {
                    targetTextFile.setHasHeader(false);
                }
                long rIndex = -1;
                String line;
                while ((line = reader.readLine()) != null && task != null && task.isWorking()) {
                    List<String> row = parseFileLine(line);
                    if (row == null || row.isEmpty()) {
                        continue;
                    }
                    if (++rIndex < startRowOfCurrentPage || rIndex >= endRowOfCurrentPage) {
                        TextFileTools.writeLine(task, writer, fileRow(row), tDelimiter);
                    } else if (rIndex == startRowOfCurrentPage) {
                        writePageData(writer, tDelimiter);
                    }
                }
                if (rIndex < 0) {
                    writePageData(writer, tDelimiter);
                }
                writer.flush();
            } catch (Exception e) {
                MyBoxLog.console(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        } else {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, tCharset, false))) {
                List<String> colsNames = columnNames();
                if (tHasHeader && colsNames != null) {
                    TextFileTools.writeLine(task, writer, colsNames, tDelimiter);
                } else {
                    targetTextFile.setHasHeader(false);
                }
                writePageData(writer, tDelimiter);
            } catch (Exception e) {
                MyBoxLog.console(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        }
        return FileTools.override(tmpFile, tFile);
    }

    public boolean writePageData(BufferedWriter writer, String delimiter) {
        try {
            if (writer == null || delimiter == null) {
                return false;
            }
            if (!isColumnsValid()) {
                return true;
            }
            for (int r = 0; r < tableRowsNumber(); r++) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                TextFileTools.writeLine(task, writer, tableRow(r, false, false), delimiter);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public File tmpTxtFile(String dname, List<String> cols, List<List<String>> data) {
        try {
            if (cols == null || cols.isEmpty()) {
                if (data == null || data.isEmpty()) {
                    return null;
                }
            }
            File tmpFile = tmpFile(dname, "tmp", "txt");
            String fDelimiter = ",";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, Charset.forName("UTF-8"), false))) {
                if (cols != null && !cols.isEmpty()) {
                    TextFileTools.writeLine(task, writer, cols, fDelimiter);
                }
                if (data != null) {
                    for (int r = 0; r < data.size(); r++) {
                        if (task != null && task.isCancelled()) {
                            break;
                        }
                        TextFileTools.writeLine(task, writer, data.get(r), fDelimiter);
                    }
                }
            }
            return tmpFile;
        } catch (Exception e) {
            MyBoxLog.console(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

}
