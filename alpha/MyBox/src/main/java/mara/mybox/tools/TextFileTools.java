package mara.mybox.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.List;
import mara.mybox.data.TextEditInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class TextFileTools {

    public static String readTexts(FxTask task, File file) {
        return readTexts(task, file, charset(file));
    }

    public static String readTexts(FxTask task, File file, Charset charset) {
        if (file == null || charset == null) {
            return null;
        }
        StringBuilder s = new StringBuilder();
        File validFile = FileTools.removeBOM(task, file);
        if (validFile == null || (task != null && !task.isWorking())) {
            return null;
        }
        try (final BufferedReader reader = new BufferedReader(new FileReader(validFile, charset))) {
            String line = reader.readLine();
            if (line != null) {
                s.append(line);
                while ((line = reader.readLine()) != null) {
                    if (task != null && !task.isWorking()) {
                        break;
                    }
                    s.append(System.lineSeparator()).append(line);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return s.toString();
    }

    public static File writeFile(File file, String data) {
        return writeFile(file, data, Charset.forName("utf-8"));
    }

    public static File writeFile(File file, String data, Charset charset) {
        if (file == null || data == null) {
            return null;
        }
        file.getParentFile().mkdirs();
        Charset fileCharset = charset != null ? charset : Charset.forName("utf-8");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, fileCharset, false))) {
            writer.write(data);
            writer.flush();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
        return file;
    }

    public static File writeFile(String data) {
        return writeFile(FileTmpTools.getTempFile(), data);
    }

    public static Charset charset(File file) {
        try {
            if (file == null || !file.exists()) {
                return Charset.forName("utf-8");
            }
            TextEditInformation info = new TextEditInformation(file, null);
            if (TextTools.checkCharset(info)) {
                return info.getCharset();
            } else {
                return Charset.forName("utf-8");
            }
        } catch (Exception e) {
            return Charset.forName("utf-8");
        }
    }

    public static boolean isUTF8(File file) {
        Charset charset = charset(file);
        return charset.equals(Charset.forName("utf-8"));
    }

    public static boolean mergeTextFiles(FxTask task, List<File> files, File targetFile) {
        if (files == null || files.isEmpty() || targetFile == null) {
            return false;
        }
        targetFile.getParentFile().mkdirs();
        String line;
        try (final FileWriter writer = new FileWriter(targetFile, Charset.forName("utf-8"))) {
            for (File file : files) {
                File validFile = FileTools.removeBOM(task, file);
                try (final BufferedReader reader = new BufferedReader(new FileReader(validFile, charset(validFile)))) {
                    while ((line = reader.readLine()) != null) {
                        if (task != null && !task.isWorking()) {
                            return false;
                        }
                        writer.write(line + System.lineSeparator());
                    }
                }
            }
            writer.flush();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
        return true;
    }

    public static void writeLine(FxTask task, BufferedWriter writer, List<String> values, String delimiter) {
        try {
            if (writer == null || values == null || values.isEmpty() || delimiter == null) {
                return;
            }
            String delimiterValue = TextTools.delimiterValue(delimiter);
            int end = values.size() - 1;
            String line = "";
            for (int c = 0; c <= end; c++) {
                if (task != null && !task.isWorking()) {
                    return;
                }
                String value = values.get(c);
                if (value != null) {
                    line += value;
                }
                if (c < end) {
                    line += delimiterValue;
                }
            }
            writer.write(line + "\n");
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
        }
    }

}
