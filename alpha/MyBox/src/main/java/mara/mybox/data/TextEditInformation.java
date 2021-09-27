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
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.tools.TextTools.bomBytes;
import mara.mybox.tools.TmpFileTools;

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
    public boolean readTotalNumbers() {
        if (file == null || pageSize <= 0 || lineBreakValue == null) {
            return false;
        }
        objectsNumber = 0;
        linesNumber = 0;
        pagesNumber = 1;
        long lineIndex = 0, charIndex = 0;
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                charIndex += line.length();
                lineIndex++;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
    public String readPage(long pageNumber) {
        try {
            if (file == null || pageSize <= 0 || pageNumber < 0) {
                return null;
            }
            long lineIndex = 0, pageStartLine = pageNumber * pageSize, pageEndLine = pageStartLine + pageSize;
            long charIndex = 0, pageStartChar = 0;
            StringBuilder pageText = new StringBuilder();
            boolean moreLine = false;
            try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
                String line, fixedLine;
                while ((line = reader.readLine()) != null) {
                    if (lineIndex >= pageEndLine) {
                        break;
                    }
                    if (lineIndex > 0) {
                        fixedLine = "\n" + line;
                    } else {
                        fixedLine = line;
                    }
                    charIndex += fixedLine.length();
                    if (lineIndex++ < pageStartLine) {
                        continue;
                    }
                    if (moreLine) {
                        pageText.append(fixedLine);
                    } else {
                        pageStartChar = charIndex - line.length();
                        pageText.append(line);
                        moreLine = true;
                    }
                }
            } catch (Exception e) {
                MyBoxLog.debug(e.toString());
                return null;
            }
            currentPage = pageNumber;
            currentPageObjectStart = pageStartChar;
            currentPageObjectEnd = pageStartChar + pageText.length();
            currentPageLineStart = pageStartLine;
            currentPageLineEnd = lineIndex;
//            MyBoxLog.console(currentPageObjectStart + "   " + currentPageObjectEnd + "    " + pageText.length());
            return pageText.toString();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    @Override
    public String locateLine(long line) {
        try {
            if (file == null || line < 0 || line >= linesNumber) {
                return null;
            }
            return readPage(line / pageSize);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    @Override
    public String locateObject(long index) {
        try {
            if (file == null || index < 0 || index >= objectsNumber) {
                return null;
            }
            long lineIndex = 0, pageStartLine = 0, charIndex = 0, pageStartChar = 0;
            StringBuilder pageText = new StringBuilder();
            boolean found = false, moreLine = false;
            try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
                String line, fixedLine;
                while ((line = reader.readLine()) != null) {
                    if (lineIndex++ > 0) {
                        fixedLine = "\n" + line;
                    } else {
                        fixedLine = line;
                    }
                    charIndex += fixedLine.length();
                    if (moreLine) {
                        pageText.append(fixedLine);
                    } else {
                        pageStartChar = charIndex - line.length();
                        pageText.append(line);
                        moreLine = true;
                    }
                    if (!found && charIndex >= index) {
                        found = true;
                    }
                    if (lineIndex == pageStartLine + pageSize) {
                        if (found) {
                            break;
                        }
                        pageStartLine = lineIndex;
                        pageText = new StringBuilder();
                        moreLine = false;
                    }
                }
            } catch (Exception e) {
                MyBoxLog.debug(e.toString());
                return null;
            }
            if (!found) {
                return null;
            }
            currentPage = pageStartLine / pageSize;;
            currentPageObjectStart = pageStartChar;
            currentPageObjectEnd = pageStartChar + pageText.length();
            currentPageLineStart = pageStartLine;
            currentPageLineEnd = lineIndex;
            return pageText.toString();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    @Override
    public String locateRange(LongIndex range) {
        if (file == null || range == null || range.start >= objectsNumber || range.start >= range.end) {
            return null;
        }
        long lineIndex = 0, pageStartLine = 0, charIndex = 0, pageStartChar = 0;
        StringBuilder pageText = new StringBuilder();
        boolean found = false, moreLine = false;
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line, fixedLine;
            while ((line = reader.readLine()) != null) {
                if (lineIndex++ > 0) {
                    fixedLine = "\n" + line;
                } else {
                    fixedLine = line;
                }
                charIndex += fixedLine.length();
                if (moreLine) {
                    pageText.append(fixedLine);
                } else {
                    pageStartChar = charIndex - line.length();
                    pageText.append(line);
                    moreLine = true;
                }
                if (!found && charIndex >= range.start) {
                    found = true;
                }
                if (found) {
                    if (charIndex >= range.end && lineIndex >= pageStartLine + pageSize) {
                        break;
                    }
                } else if (lineIndex == pageStartLine + pageSize) {
                    pageStartLine = lineIndex;
                    pageText = new StringBuilder();
                    moreLine = false;
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
        if (!found) {
            return null;
        }
        currentPage = pageStartLine / pageSize;
        currentPageObjectStart = pageStartChar;
        currentPageObjectEnd = pageStartChar + pageText.length();
        currentPageLineStart = pageStartLine;
        currentPageLineEnd = lineIndex;
        return pageText.toString();
    }

    @Override
    public File filter(boolean recordLineNumbers) {
        try {
            if (file == null || filterStrings == null || filterStrings.length == 0) {
                return file;
            }
            File targetFile = TmpFileTools.getTempFile();
            long lineIndex = 0;
            try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset));
                     BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, charset, false))) {
                String line;
                while ((line = reader.readLine()) != null) {
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
            MyBoxLog.debug(e.toString());
            return null;
        }

    }

    @Override
    public boolean writeObject(String text) {
        if (file == null || charset == null || text == null || text.isEmpty()) {
            return false;
        }
        try ( BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                 OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
            if (withBom) {
                byte[] bytes = bomBytes(charset.name());
                outputStream.write(bytes);
            }
            if (lineBreak != Line_Break.LF) {
                writer.write(text.replaceAll("\n", lineBreakValue));
            } else {
                writer.write(text);
            }
            writer.flush();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
        return true;
    }

    @Override
    public boolean writePage(FileEditInformation sourceInfo, String text) {
        return writePage(sourceInfo, sourceInfo.getCurrentPage(), text);
    }

    @Override
    public boolean writePage(FileEditInformation sourceInfo, long pageNumber, String pageText) {
        try {
            if (sourceInfo.getFile() == null || sourceInfo.getCharset() == null
                    || sourceInfo.getPageSize() <= 0 || pageNumber < 0
                    || pageText == null || pageText.isEmpty()
                    || file == null || charset == null || lineBreakValue == null) {
                return false;
            }
            File targetFile = file;
            if (sourceInfo.getFile().equals(file)) {
                targetFile = TmpFileTools.getTempFile();
            }
            long lineIndex = 0, pageLineStart = pageNumber * pageSize, pageLineEnd = pageLineStart + pageSize;
            try ( BufferedReader reader = new BufferedReader(new FileReader(sourceInfo.getFile(), sourceInfo.charset));
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                     OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
                if (withBom) {
                    byte[] bytes = bomBytes(charset.name());
                    outputStream.write(bytes);
                }
                String line, text;
                while ((line = reader.readLine()) != null) {
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
                MyBoxLog.debug(e.toString());
                return false;
            }
            if (sourceInfo.getFile().equals(file)) {
                FileTools.rename(targetFile, file);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

}
