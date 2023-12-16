package mara.mybox.data;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.tools.TextTools.bomBytes;
import static mara.mybox.tools.TextTools.bomSize;

/**
 * @Author Mara
 * @CreateDate 2018-12-10 13:06:33
 * @License Apache License Version 2.0
 */
public class TextEditInformation extends FileEditInformation {

    public TextEditInformation() {
        editType = Edit_Type.Text;
        initValues();
    }

    public TextEditInformation(File file) {
        super(file);
        editType = Edit_Type.Text;
        initValues();
    }

    @Override
    public boolean readTotalNumbers(FxTask currentTask) {
        if (file == null || pageSize <= 0 || lineBreakValue == null) {
            return false;
        }
        objectsNumber = 0;
        linesNumber = 0;
        pagesNumber = 1;
        long lineIndex = 0, charIndex = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (currentTask != null && !currentTask.isWorking()) {
                    return false;
                }
                charIndex += line.length();
                lineIndex++;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
        linesNumber = lineIndex;
        pagesNumber = linesNumber / pageSize;
        if (linesNumber % pageSize > 0) {
            pagesNumber++;
        }
        objectsNumber = charIndex + (linesNumber > 0 ? linesNumber - 1 : 0);
        totalNumberRead = true;
        return true;
    }

    @Override
    public String readPage(FxTask currentTask, long pageNumber) {
        return readLines(currentTask, pageNumber * pageSize, pageSize);
    }

    @Override
    public String readLines(FxTask currentTask, long from, long number) {
        if (file == null || from < 0 || number <= 0 || (linesNumber > 0 && from >= linesNumber)) {
            return null;
        }
        long lineIndex = 0, charIndex = 0, lineStart = 0;
        StringBuilder pageText = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            lineStart = (from / pageSize) * pageSize;
            long lineEnd = Math.max(from + number, lineStart + pageSize);
            String line, fixedLine;
            boolean moreLine = false;
            while ((line = reader.readLine()) != null) {
                if (currentTask != null && !currentTask.isWorking()) {
                    return null;
                }
                if (lineIndex > 0) {
                    fixedLine = "\n" + line;
                } else {
                    fixedLine = line;
                }
                charIndex += fixedLine.length();
                if (lineIndex++ < lineStart) {
                    continue;
                }
                if (moreLine) {
                    pageText.append(fixedLine);
                } else {
                    pageText.append(line);
                    moreLine = true;
                }
                if (lineIndex >= lineEnd) {
                    break;
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
        currentPage = lineStart / pageSize;
        currentPageObjectStart = charIndex - pageText.length();
        currentPageObjectEnd = charIndex;
        currentPageLineStart = lineStart;
        currentPageLineEnd = lineIndex;
        return pageText.toString();
    }

    @Override
    public String readObjects(FxTask currentTask, long from, long number) {
        if (file == null || from < 0 || number <= 0 || (objectsNumber > 0 && from >= objectsNumber)) {
            return null;
        }
        long charIndex = 0, lineIndex = 0, lineStart = 0;
        StringBuilder pageText = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            long to = from + number;
            boolean moreLine = false;
            String line, fixedLine;
            while ((line = reader.readLine()) != null) {
                if (currentTask != null && !currentTask.isWorking()) {
                    return null;
                }
                if (lineIndex > 0) {
                    fixedLine = "\n" + line;
                } else {
                    fixedLine = line;
                }
                if (moreLine) {
                    pageText.append(fixedLine);
                } else {
                    pageText.append(line);
                    moreLine = true;
                }
                charIndex += fixedLine.length();
                if (++lineIndex == lineStart + pageSize && charIndex < from) {
                    lineStart = lineIndex;
                    pageText = new StringBuilder();
                    moreLine = false;
                }
                if (charIndex >= to && lineIndex >= lineStart + pageSize) {
                    break;
                }

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
        currentPage = lineStart / pageSize;;
        currentPageObjectStart = charIndex - pageText.length();
        currentPageObjectEnd = charIndex;
        currentPageLineStart = lineStart;
        currentPageLineEnd = lineIndex;
        return pageText.toString();
    }

    @Override
    public File filter(FxTask currentTask, boolean recordLineNumbers) {
        try {
            if (file == null || filterStrings == null || filterStrings.length == 0) {
                return file;
            }
            File targetFile = FileTmpTools.getTempFile();
            long lineIndex = 0;
            try (BufferedReader reader = new BufferedReader(new FileReader(file, charset));
                    BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, charset, false))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (currentTask != null && !currentTask.isWorking()) {
                        return null;
                    }
                    if (isMatchFilters(line)) {
                        if (recordLineNumbers) {
                            line = StringTools.fillRightBlank(lineIndex, 15) + line;
                        }
                        writer.write(line + lineBreakValue);
                    }
                    lineIndex++;
                }
                writer.flush();
            }
            return targetFile;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }

    }

    @Override
    public boolean writeObject(FxTask currentTask, String text) {
        if (file == null || charset == null || text == null || text.isEmpty()) {
            return false;
        }
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
            if (withBom) {
                byte[] bytes = bomBytes(charset.name());
                outputStream.write(bytes);
            }
            if (currentTask != null && !currentTask.isWorking()) {
                return false;
            }
            if (lineBreak != Line_Break.LF) {
                writer.write(text.replaceAll("\n", lineBreakValue));
            } else {
                writer.write(text);
            }
            writer.flush();
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean writePage(FxTask currentTask, FileEditInformation sourceInfo, String pageText) {
        try {
            if (sourceInfo.getFile() == null || sourceInfo.getCharset() == null
                    || sourceInfo.getPageSize() <= 0 || pageText == null
                    || file == null || charset == null || lineBreakValue == null) {
                return false;
            }
            File targetFile = file;
            if (sourceInfo.getFile().equals(file)) {
                targetFile = FileTmpTools.getTempFile();
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(sourceInfo.getFile(), sourceInfo.charset));
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
                if (sourceInfo.isWithBom()) {
                    reader.skip(bomSize(sourceInfo.getCharset().name()));
                }
                if (withBom) {
                    byte[] bytes = bomBytes(charset.name());
                    outputStream.write(bytes);
                }
                if (currentTask != null && !currentTask.isWorking()) {
                    return false;
                }
                String line, text;
                long lineIndex = 0, pageLineStart = sourceInfo.getCurrentPageLineStart(),
                        pageLineEnd = sourceInfo.getCurrentPageLineEnd();
                while ((line = reader.readLine()) != null) {
                    if (currentTask != null && !currentTask.isWorking()) {
                        return false;
                    }
                    text = null;
                    if (lineIndex < pageLineStart || lineIndex >= pageLineEnd) {
                        text = line;
                    } else if (lineIndex == pageLineStart) {
                        if (lineBreak != Line_Break.LF) {
                            text = pageText.replaceAll("\n", lineBreakValue);
                        } else {
                            text = pageText;
                        }
                    }
                    if (text != null) {
                        if (lineIndex > 0) {
                            text = lineBreakValue + text;
                        }
                        writer.write(text);
                    }
                    lineIndex++;
                }
                writer.flush();
            } catch (Exception e) {
                MyBoxLog.debug(e);
                return false;
            }
            if (currentTask != null && !currentTask.isWorking()) {
                return false;
            }
            if (sourceInfo.getFile().equals(file)) {
                FileTools.override(targetFile, file);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

}
