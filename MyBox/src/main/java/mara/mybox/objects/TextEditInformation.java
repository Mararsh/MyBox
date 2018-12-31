package mara.mybox.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.FileEditInformationFactory.Edit_Type;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.tools.TextTools.bomBytes;
import static mara.mybox.tools.TextTools.bomSize;
import static mara.mybox.tools.TextTools.countNumber;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2018-12-10 13:06:33
 * @Version 1.0
 * @Description
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
            if (file == null || pageSize <= 0) {
                return false;
            }
            try (FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                String NewLine = TextTools.lineBreak(lineBreak);
                char[] buf = new char[pageSize];
                int charIndex = 0, lineIndex = 1, len;
                String bufStr;
                while ((len = reader.read(buf)) != -1) {
                    charIndex += len;
                    bufStr = new String(buf, 0, len);
                    lineIndex += countNumber(bufStr, NewLine);
                }
                objectsNumber = charIndex;
                linesNumber = lineIndex;
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    @Override
    public String readPage() {
        return readPage(currentPage);
    }

    @Override
    public String readPage(int pageNumber) {
        try {
            if (file == null || pageSize <= 0 || pageNumber < 1) {
                return null;
            }
            int lineIndex = 1, lineStart = 1, lineEnd = 1, pageIndex = 1;
            String pageText = null;
            try (FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                String NewLine = TextTools.lineBreak(lineBreak);
                char[] buf = new char[pageSize];
                String bufStr;
                int len;
                while ((len = reader.read(buf)) != -1) {
                    bufStr = new String(buf, 0, len);
                    lineIndex += countNumber(bufStr, NewLine);
                    if (pageIndex == pageNumber) {
                        lineEnd = lineIndex;
                        pageText = bufStr;
                        break;
                    }
                    lineStart = lineIndex;
                    pageIndex++;
                }
                if (pageText == null) {
                    return null;
                }
            }
            currentPage = pageNumber;
            currentPageObjectStart = pageSize * (pageNumber - 1);
            currentPageObjectEnd = currentPageObjectStart + pageText.length();
            currentPageLineStart = lineStart;
            currentPageLineEnd = lineEnd;
            if (objectsNumber < 0 && pageText.length() < pageSize) {
                objectsNumber = (pageNumber - 1) * pageSize + pageText.length();
                linesNumber = lineEnd;
            }
            if (lineBreak.equals(Line_Break.CR)) {
                pageText = pageText.replaceAll("\r", "\n");
            }
            return pageText;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    @Override
    public boolean writeObject(String text) {
        try {
            if (file == null || charset == null || text == null || text.isEmpty()) {
                return false;
            }
            try (FileOutputStream outputStream = new FileOutputStream(file);
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
                if (withBom) {
                    byte[] bytes = bomBytes(charset.name());
                    outputStream.write(bytes);
                }
                if (lineBreak != Line_Break.LF) {
                    writer.write(text.replaceAll("\n", TextTools.lineBreak(lineBreak)));
                } else {
                    writer.write(text);
                }
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    @Override
    public boolean writePage(FileEditInformation sourceInfo, String text) {
        return writePage(sourceInfo, sourceInfo.getCurrentPage(), text);
    }

    @Override
    public boolean writePage(FileEditInformation sourceInfo, int pageNumber, String text) {
        try {
            if (sourceInfo.getFile() == null || sourceInfo.getCharset() == null
                    || sourceInfo.getPageSize() <= 0 || pageNumber < 1
                    || text == null || text.isEmpty()
                    || file == null || charset == null) {
                return false;
            }
            File targetFile = file;
            if (sourceInfo.getFile().equals(file)) {
                targetFile = FileTools.getTempFile();
            }
            try (FileInputStream inputStream = new FileInputStream(sourceInfo.getFile());
                    InputStreamReader reader = new InputStreamReader(inputStream, sourceInfo.getCharset());
                    FileOutputStream outputStream = new FileOutputStream(targetFile);
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
                if (sourceInfo.isWithBom()) {
                    inputStream.skip(bomSize(sourceInfo.getCharset().name()));
                }
                if (withBom) {
                    byte[] bytes = bomBytes(charset.name());
                    outputStream.write(bytes);
                }
                String taregtLineBreak = TextTools.lineBreak(lineBreak);
                String sourceLineBreak = TextTools.lineBreak(sourceInfo.getLineBreak());
                boolean sameLineBreak = taregtLineBreak.equals(sourceLineBreak);
                char[] buf = new char[sourceInfo.getPageSize()];
                int bufLen, pageIndex = 1;
                while ((bufLen = reader.read(buf)) != -1) {
                    if (pageIndex == pageNumber) {
                        if (lineBreak != Line_Break.LF) {
                            writer.write(text.replaceAll("\n", taregtLineBreak));
                        } else {
                            writer.write(text);
                        }
                    } else {
                        String bufStr = new String(buf, 0, bufLen);
                        if (sameLineBreak) {
                            writer.write(bufStr);
                        } else {
                            writer.write(bufStr.replaceAll(sourceLineBreak, taregtLineBreak));
                        }
                    }
                    pageIndex++;
                }
            }
            if (sourceInfo.getFile().equals(file)) {
                file.delete();
                targetFile.renameTo(file);
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    @Override
    public String findFirst() {
        currentFound = -1;
        return findNext();
    }

    @Override
    public String findNext() {
        try {
            if (file == null || findString == null || findString.isEmpty()) {
                return null;
            }
            int previousFound, previousPage, previousPos;
            int lineStart = 1, lineEnd = 1, pageIndex = 1, preStart = 1, preEnd;
            if (currentFound >= 0) {
                previousFound = currentFound;
                previousPage = previousFound / pageSize + 1;
                previousPos = previousFound % pageSize;
            } else {
                previousFound = -1;
                previousPage = -1;
                previousPos = -1;
            }
            String pageText = null, preText;
            try (FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                if (previousPage > 1) {
                    lineStart = lineEnd = currentPageLineStart;
                    pageIndex = previousPage;
                    reader.skip((previousPage - 1) * pageSize);
                }
                String NewLine = TextTools.lineBreak(lineBreak);
                char[] buf = new char[pageSize];
                int len, pos, findLen = findString.length();
                String bufStr = null, crossString = "";
                while ((len = reader.read(buf)) != -1) {
                    preEnd = lineStart;
                    preText = bufStr;
                    bufStr = new String(buf, 0, len);
                    lineEnd += countNumber(bufStr, NewLine);
                    if (pageIndex >= previousPage) {
                        if (previousPage == pageIndex) {
                            pos = (crossString + bufStr).indexOf(findString, previousPos + 1);
                        } else {
                            pos = (crossString + bufStr).indexOf(findString);
                        }
                        if (pos >= 0) {
                            currentFound = (pageIndex - 1) * pageSize + pos - crossString.length();
                            if (!crossString.isEmpty() && pos < findLen) {
                                pageText = preText;
                                lineStart = preStart;
                                lineEnd = preEnd;
                                pageIndex--;
                                break;
                            } else {
                                pageText = bufStr;
                                break;
                            }
                        }
                    }
                    int strLen = bufStr.length();
                    if (previousPage == pageIndex) {
                        crossString = bufStr.substring(Math.max(previousPos + 1, strLen - findLen + 1), strLen);
                    } else {
                        crossString = bufStr.substring(strLen - findLen + 1, strLen);
                    }
                    preStart = lineStart;
                    lineStart = lineEnd;
                    pageIndex++;
                }
                if (pageText == null) {
                    return null;
                }
            }
            currentPage = pageIndex;
            currentPageObjectStart = pageSize * (pageIndex - 1);
            currentPageObjectEnd = currentPageObjectStart + pageText.length();
            currentPageLineStart = lineStart;
            currentPageLineEnd = lineEnd;
            if (lineBreak.equals(Line_Break.CR)) {
                pageText = pageText.replaceAll("\r", "\n");
            }
            return pageText;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }

    }

    @Override
    public String findPrevious() {
        try {
            if (file == null || findString == null || findString.isEmpty()) {
                return null;
            }
            int cuFound, cuPage, cuPos;
            int lineEnd = 1, lineStart = 1, pageIndex = 1, preStart = 1, preEnd;
            if (currentFound >= 0) {
                cuFound = currentFound;
                cuPage = cuFound / pageSize + 1;
                cuPos = cuFound % pageSize;
            } else {
                return findNext();
            }
            String pageText = null, preText;
            int maxIndex = -1, maxLineStart = -1, maxLineEnd = -1, maxPage = 1;
            try (FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                String NewLine = TextTools.lineBreak(lineBreak);
                char[] buf = new char[pageSize];
                int len, pos, findLen = findString.length();
                String bufStr = null, crossString = "";
                while ((len = reader.read(buf)) != -1) {
                    preEnd = lineStart;
                    preText = bufStr;
                    bufStr = new String(buf, 0, len);
                    lineEnd += countNumber(bufStr, NewLine);
                    if (cuPage == pageIndex) {
                        if (cuPos > 0) {
                            pos = (crossString + bufStr).substring(0, cuPos + crossString.length() - 1).lastIndexOf(findString);
                        } else {
                            pos = -1;
                        }
                    } else {
                        pos = (crossString + bufStr).lastIndexOf(findString);
                    }
                    if (pos >= 0) {
                        if (!crossString.isEmpty() && pos < findLen) {
                            int actualPos = (pageIndex - 1) * pageSize - (crossString.length() - pos);
                            if (actualPos != cuFound) {
                                maxPage = pageIndex - 1;
                                maxIndex = actualPos;
                                pageText = preText;
                                maxLineStart = preStart;
                                maxLineEnd = preEnd;
                            }
                        } else {
                            maxPage = pageIndex;
                            maxIndex = pos + (pageIndex - 1) * pageSize - crossString.length();
                            pageText = bufStr;
                            maxLineStart = lineStart;
                            maxLineEnd = lineEnd;
                        }
                    }
                    if (pageIndex == cuPage) {
                        break;
                    }
                    int strLen = bufStr.length();
                    crossString = bufStr.substring(strLen - findLen + 1, strLen);
                    preStart = lineStart;
                    lineStart = lineEnd;
                    pageIndex++;
                }
                if (pageText == null) {
                    return null;
                }
            }
            currentFound = maxIndex;
            currentPage = maxPage;
            currentPageObjectStart = pageSize * (maxPage - 1);
            currentPageObjectEnd = currentPageObjectStart + pageText.length();
            currentPageLineStart = maxLineStart;
            currentPageLineEnd = maxLineEnd;
            if (lineBreak.equals(Line_Break.CR)) {
                pageText = pageText.replaceAll("\r", "\n");
            }
            return pageText;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }

    }

    @Override
    public String findLast() {
        try {
            if (file == null || findString == null || findString.isEmpty()) {
                return null;
            }
            int lineEnd = 1, lineStart = 1, pageIndex = 1, preStart = 1, preEnd;
            String pageText = null, preText;
            int maxIndex = -1, maxLineStart = -1, maxLineEnd = -1, maxPage = 1;
            try (FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                String NewLine = TextTools.lineBreak(lineBreak);
                char[] buf = new char[pageSize];
                int len, pos, findLen = findString.length();
                String bufStr = null, crossString = "", addedStr;
                while ((len = reader.read(buf)) != -1) {
                    preEnd = lineStart;
                    preText = bufStr;
                    bufStr = new String(buf, 0, len);
                    lineEnd += countNumber(bufStr, NewLine);
                    addedStr = crossString + bufStr;
                    pos = addedStr.lastIndexOf(findString);
                    if (pos >= 0) {
                        if (!crossString.isEmpty() && pos < findLen) {
                            maxPage = pageIndex - 1;
                            maxIndex = pageSize * (pageIndex - 1) + pos - crossString.length();
                            pageText = preText;
                            maxLineStart = preStart;
                            maxLineEnd = preEnd;
                        } else {
                            maxPage = pageIndex;
                            maxIndex = pageSize * (pageIndex - 1) + pos - crossString.length();
                            pageText = bufStr;
                            maxLineStart = lineStart;
                            maxLineEnd = lineEnd;
                        }
                    }
                    int strLen = addedStr.length();
                    crossString = addedStr.substring(Math.max(pos + 1, strLen - findLen + 1), strLen);
                    preStart = lineStart;
                    lineStart = lineEnd;
                    pageIndex++;
                }
                if (pageText == null) {
                    return null;
                }
            }
            currentFound = maxIndex;
            currentPage = maxPage;
            currentPageObjectStart = pageSize * (maxPage - 1);
            currentPageObjectEnd = currentPageObjectStart + pageText.length();
            currentPageLineStart = maxLineStart;
            currentPageLineEnd = maxLineEnd;
            if (lineBreak.equals(Line_Break.CR)) {
                pageText = pageText.replaceAll("\r", "\n");
            }
            return pageText;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }

    }

    @Override
    public int replaceAll() {
        if (file == null
                || replaceString == null || findString == null || findString.isEmpty()) {
            return 0;
        }
        int replaceAllNumber = 0;
        try {
            File targetFile = FileTools.getTempFile();
            try (FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader reader = new InputStreamReader(inputStream, charset);
                    FileOutputStream outputStream = new FileOutputStream(targetFile);
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
                if (withBom) {
                    byte[] bytes = bomBytes(charset.name());
                    inputStream.skip(bytes.length);
                    outputStream.write(bytes);
                }
                char[] buf = new char[pageSize];
                int bufLen, findLen = findString.length();
                String thisPage, crossString = "";
                while ((bufLen = reader.read(buf)) != -1) {
                    thisPage = crossString + new String(buf, 0, bufLen);
                    int[] ret = TextTools.lastAndCount(thisPage, findString);
                    int lastPos = ret[0];
                    int crossFrom, pagelen = thisPage.length();
                    if (lastPos >= 0) {
                        int num = ret[1];
                        replaceAllNumber += num;
                        if (lastPos + findLen == pagelen) {
                            crossString = "";
                            thisPage = thisPage.replaceAll(findString, replaceString);
                        } else {
                            crossFrom = Math.max(lastPos + findLen, pagelen - findLen + 1);
                            crossString = thisPage.substring(crossFrom, pagelen);
                            thisPage = thisPage.substring(0, crossFrom).replaceAll(findString, replaceString);
                        }
                    } else {
                        crossFrom = pagelen - findLen + 1;
                        crossString = thisPage.substring(crossFrom, pagelen);
                        thisPage = thisPage.substring(0, crossFrom);
                    }
                    writer.write(thisPage);
                }
                if (!crossString.isEmpty()) {
                    writer.write(crossString);
                }
            }
            file.delete();
            targetFile.renameTo(file);
            return replaceAllNumber;
        } catch (Exception e) {
            logger.debug(e.toString());
            return 0;
        }
    }

    @Override
    public File filter() {
        try {
            if (file == null || filterStrings == null || filterStrings == null) {
                return file;
            }
            File targetFile = FileTools.getTempFile();
            int lineEnd = 1, lineStart;
            try (FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader reader = new InputStreamReader(inputStream, charset);
                    FileOutputStream outputStream = new FileOutputStream(targetFile);
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                String NewLine = TextTools.lineBreak(lineBreak);
                char[] buf = new char[pageSize];
                int len;
                String bufStr, crossString = "";
                while ((len = reader.read(buf)) != -1) {
                    bufStr = crossString + new String(buf, 0, len);
                    lineStart = lineEnd;
                    String[] lines = bufStr.split(NewLine);
                    for (int i = 0; i < lines.length - 1; i++) {
                        if (isMatchFilters(lines[i])) {
                            String lineNumber = ValueTools.fillRightBlank(lineStart + i, 15);
                            writer.write(lineNumber + "    " + lines[i] + NewLine);
                        }
                    }
                    if (bufStr.endsWith(NewLine)) {
                        if (isMatchFilters(lines[lines.length - 1])) {
                            String lineNumber = ValueTools.fillRightBlank(lineStart + lines.length - 1, 15);
                            writer.write(lineNumber + "    " + lines[lines.length - 1] + NewLine);
                        }
                        crossString = "";
                    } else {
                        crossString = lines[lines.length - 1];
                    }
                    lineEnd += lines.length - 1;
                }
                if (!crossString.isEmpty()) {
                    if (isMatchFilters(crossString)) {
                        String lineNumber = ValueTools.fillRightBlank(lineEnd, 15);
                        writer.write(lineNumber + "    " + crossString);
                    }
                }
            }

            return targetFile;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }

    }

}
