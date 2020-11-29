package mara.mybox.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import mara.mybox.dev.MyBoxLog;
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
    public boolean readTotalNumbers() {
        try {
            if (file == null || pageSize <= 0 || lineBreakValue == null) {
                return false;
            }
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                int textSize = FileTools.bufSize(file);
                char[] textBuf = new char[textSize];
                char[] charBuf = new char[1];
                boolean crlf = lineBreak.equals(Line_Break.CRLF);
                long charIndex = 0, lineIndex = 1, pageIndex = 0;
                int textLen, readLen = textSize;
                String text;
                while ((textLen = reader.read(textBuf, 0, readLen)) > 0) {
                    charIndex += textLen;
                    text = new String(textBuf, 0, textLen);
                    if (crlf && text.endsWith("\r") && reader.read(charBuf) == 1) {
                        text += new String(charBuf, 0, 1);
                        charIndex++;
                        readLen = textSize - 1;
                    } else {
                        readLen = textSize;
                    }
                    lineIndex += FindReplaceString.count(text, lineBreakValue);
                    pageIndex++;
                }
                objectsNumber = charIndex;
                linesNumber = lineIndex;
                pagesNumber = objectsNumber / pageSize;
                if (objectsNumber % pageSize > 0) {
                    pagesNumber++;
                }
                totalNumberRead = true;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    @Override
    public String readPage() {
        return readPage(currentPage);
    }

    @Override
    public String readPage(long pageNumber) {
        try {
            if (file == null || pageSize <= 0 || pageNumber < 1) {
                return null;
            }
            long pageStart = 0, pageEnd = 0, lineEnd = 1, lineStart = 1, pageIndex = 1;
            String pageText = null;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                char[] textBuf = new char[pageSize];
                char[] charBuf = new char[1];
                boolean crlf = lineBreak.equals(Line_Break.CRLF);
                String text;
                int textLen, readLen = pageSize;
                long findStart = -1, findEnd = -1;
                if (findReplace != null && findReplace.getFileRange() != null) {
                    findStart = findReplace.getFileRange().getStart();
                    findEnd = findReplace.getFileRange().getEnd();
                }
                while ((textLen = reader.read(textBuf, 0, readLen)) > 0) {
                    text = new String(textBuf, 0, textLen);
                    if (crlf && text.endsWith("\r") && reader.read(charBuf) == 1) {
                        text += new String(charBuf, 0, 1);
                        textLen++;
                        readLen = pageSize - 1;
                    } else {
                        readLen = pageSize;
                    }
                    pageStart = pageEnd;
                    pageEnd += textLen;
                    lineStart = lineEnd;
                    if (pageIndex == pageNumber) {
                        pageText = text;
                        if (findStart >= pageStart && findStart < pageEnd && findEnd > pageEnd) {
                            char[] findBuf = new char[(int) (findEnd - pageEnd)];
                            int findLen;
                            if ((findLen = reader.read(findBuf)) > 0) {
                                String findText = new String(findBuf, 0, findLen);
                                pageText += findText;
                                pageEnd += findLen;
                            }
                        }
                        lineEnd += FindReplaceString.count(pageText, lineBreakValue);
                        break;
                    } else {
                        lineEnd += FindReplaceString.count(text, lineBreakValue);
                    }
                    pageIndex++;
                }
            }
            if (pageText == null) {
                return null;
            }
            currentPage = pageNumber;
            currentPageObjectStart = pageStart;  // 0-based
            currentPageObjectEnd = pageEnd;      // excluded
            currentPageLineStart = lineStart;    // 1-based
            currentPageLineEnd = lineEnd;        // included
            if (!lineBreak.equals(Line_Break.LF)) {
                pageText = pageText.replaceAll(lineBreakValue, "\n");
            }
            if (findReplace != null && findReplace.getFileRange() != null) {
                findReplace.setPageReloaded(true);
                FindReplaceFile.stringRange(findReplace, pageText);
            }
            return pageText;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    @Override
    public boolean writeObject(String text) {
        try {
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
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    @Override
    public boolean writePage(FileEditInformation sourceInfo, String text) {
        return writePage(sourceInfo, sourceInfo.getCurrentPage(), text);
    }

    @Override
    public boolean writePage(FileEditInformation sourceInfo, long pageNumber, String pageText) {
        try {
            if (sourceInfo.getFile() == null || sourceInfo.getCharset() == null
                    || sourceInfo.getPageSize() <= 0 || pageNumber < 1
                    || pageText == null || pageText.isEmpty()
                    || file == null || charset == null || lineBreakValue == null) {
                return false;
            }
            File targetFile = file;
            if (sourceInfo.getFile().equals(file)) {
                targetFile = FileTools.getTempFile();
            }
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceInfo.getFile()));
                     InputStreamReader reader = new InputStreamReader(inputStream, sourceInfo.getCharset());
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                     OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
                if (sourceInfo.isWithBom()) {
                    inputStream.skip(bomSize(sourceInfo.getCharset().name()));
                }
                if (withBom) {
                    byte[] bytes = bomBytes(charset.name());
                    outputStream.write(bytes);
                }
                String sourceLineBreak = sourceInfo.getLineBreakValue();
                boolean sameLineBreak = lineBreakValue.equals(sourceLineBreak);
                boolean crlf = lineBreak.equals(Line_Break.CRLF);
                int bufSize = sourceInfo.getPageSize(), readLen = bufSize, textLen, pageIndex = 1;
                char[] textBuf = new char[bufSize];
                char[] charBuf = new char[1];
                while ((textLen = reader.read(textBuf, 0, readLen)) > 0) {
                    if (pageIndex == pageNumber) {
                        if (lineBreak != Line_Break.LF) {
                            writer.write(pageText.replaceAll("\n", lineBreakValue));
                        } else {
                            writer.write(pageText);
                        }
                    } else {
                        String text = new String(textBuf, 0, textLen);
                        if (crlf && pageText.endsWith("\r") && reader.read(charBuf) == 1) {
                            text += new String(charBuf, 0, 1);
                            textLen++;
                            readLen = bufSize - 1;
                        } else {
                            readLen = bufSize;
                        }
                        if (sameLineBreak) {
                            writer.write(text);
                        } else {
                            writer.write(text.replaceAll(sourceLineBreak, lineBreakValue));
                        }
                    }
                    pageIndex++;
                }
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

    @Override
    public String locateLine() {
        try {
            if (file == null || currentLine <= 0) {
                return null;
            }
            long pageStart = 0, pageEnd = 0, lineEnd = 1, lineStart = 1, pageIndex = 1;
            String pageText = null;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                char[] textBuf = new char[pageSize];
                char[] charBuf = new char[1];
                boolean crlf = lineBreak.equals(Line_Break.CRLF);
                int textLen, readLen = pageSize;
                String text;
                while ((textLen = reader.read(textBuf, 0, readLen)) > 0) {
                    text = new String(textBuf, 0, textLen);
                    if (crlf && text.endsWith("\r") && reader.read(charBuf) == 1) {
                        text += new String(charBuf, 0, 1);
                        textLen++;
                        readLen = pageSize - 1;
                    } else {
                        readLen = pageSize;
                    }
                    pageStart = pageEnd;
                    pageEnd += textLen;
                    lineStart = lineEnd;
                    lineEnd += FindReplaceString.count(text, lineBreakValue);
                    if (currentLine >= lineStart && currentLine <= lineEnd) {
                        pageText = text;
                        break;
                    }
                    pageIndex++;
                }
                if (pageText == null) {
                    return null;
                }
            }
            currentPage = pageIndex;
            currentPageObjectStart = pageStart;
            currentPageObjectEnd = pageEnd;
            currentPageLineStart = lineStart;
            currentPageLineEnd = lineEnd;
            if (!lineBreak.equals(Line_Break.LF)) {
                pageText = pageText.replaceAll(lineBreakValue, "\n");
            }
            return pageText;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }

    }

    @Override
    public File filter(boolean recordLineNumbers) {
        try {
            if (file == null || filterStrings == null || filterStrings.length == 0) {
                return file;
            }
            File targetFile = FileTools.getTempFile();
            long lineEnd = 1, lineStart = 1;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     InputStreamReader reader = new InputStreamReader(inputStream, charset);
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                     OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
                if (withBom) {
                    byte[] bytes = bomBytes(charset.name());
                    inputStream.skip(bytes.length);
                    outputStream.write(bytes);
                }
                char[] textBuf = new char[pageSize];
                char[] charBuf = new char[1];
                boolean crlf = lineBreak.equals(Line_Break.CRLF);
                int textLen, readLen = pageSize;
                String text, crossString = "";
                while ((textLen = reader.read(textBuf, 0, readLen)) > 0) {
                    text = new String(textBuf, 0, textLen);
                    if (crlf && text.endsWith("\r") && reader.read(charBuf) == 1) {
                        text += new String(charBuf, 0, 1);
                        textLen++;
                        readLen = pageSize - 1;
                    } else {
                        readLen = pageSize;
                    }
                    text = crossString + text;
                    String[] lines = text.split(lineBreakValue);
                    lineStart = lineEnd;
                    lineEnd += lines.length - 1;
                    for (int i = 0; i < lines.length - 1; ++i) {
                        if (isMatchFilters(lines[i])) {
                            if (recordLineNumbers) {
                                String lineNumber = StringTools.fillRightBlank(lineStart + i, 15);
                                writer.write(lineNumber + "    " + lines[i] + lineBreakValue);
                            } else {
                                writer.write(lines[i] + lineBreakValue);
                            }
                        }
                    }
                    if (text.endsWith(lineBreakValue)) {
                        if (isMatchFilters(lines[lines.length - 1])) {
                            if (recordLineNumbers) {
                                String lineNumber = StringTools.fillRightBlank(lineStart + lines.length - 1, 15);
                                writer.write(lineNumber + "    " + lines[lines.length - 1] + lineBreakValue);
                            } else {
                                writer.write(lines[lines.length - 1] + lineBreakValue);
                            }
                        }
                        crossString = "";
                    } else {
                        crossString = lines[lines.length - 1];
                    }
                }
                if (!crossString.isEmpty()) {
                    if (isMatchFilters(crossString)) {
                        if (recordLineNumbers) {
                            String lineNumber = StringTools.fillRightBlank(lineEnd, 15);
                            writer.write(lineNumber + "    " + crossString);
                        } else {
                            writer.write(crossString);
                        }
                    }
                }
            }

            return targetFile;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }

    }

}
