package mara.mybox.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import javafx.scene.control.IndexRange;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.tools.TextTools.bomBytes;
import static mara.mybox.tools.TextTools.bomSize;
import static mara.mybox.value.AppVariables.logger;

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
                char[] buf = new char[pageSize];
                int charIndex = 0, lineIndex = 1, len;
                String bufStr;
                while ((len = reader.read(buf)) != -1) {
                    charIndex += len;
                    bufStr = new String(buf, 0, len);
                    lineIndex += StringTools.countNumber(bufStr, lineBreakValue);
                }
                objectsNumber = charIndex;
                linesNumber = lineIndex;
                totalNumberRead = true;
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
    public String readPage(long pageNumber) {
        try {
            if (file == null || pageSize <= 0 || pageNumber < 1) {
                return null;
            }
            long lineEnd = 1, lineStart = 1, pageIndex = 1;
            String pageText = null;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                char[] buf = new char[pageSize];
                String bufStr;
                int len;
                while ((len = reader.read(buf)) != -1) {
                    bufStr = new String(buf, 0, len);
                    lineEnd += StringTools.countNumber(bufStr, lineBreakValue);
                    if (pageIndex == pageNumber) {
                        pageText = bufStr;
                        break;
                    }
                    lineStart = lineEnd;
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
            if (!totalNumberRead && pageText.length() < pageSize) {
                objectsNumber = (pageNumber - 1) * pageSize + pageText.length();
                linesNumber = lineEnd;
                totalNumberRead = true;
            }
            if (!lineBreak.equals(Line_Break.LF)) {
                pageText = pageText.replaceAll(lineBreakValue, "\n");
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
            logger.debug(e.toString());
            return false;
        }
    }

    @Override
    public boolean writePage(FileEditInformation sourceInfo, String text) {
        return writePage(sourceInfo, sourceInfo.getCurrentPage(), text);
    }

    @Override
    public boolean writePage(FileEditInformation sourceInfo, long pageNumber,
            String text) {
        try {
            if (sourceInfo.getFile() == null || sourceInfo.getCharset() == null
                    || sourceInfo.getPageSize() <= 0 || pageNumber < 1
                    || text == null || text.isEmpty()
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
                char[] buf = new char[sourceInfo.getPageSize()];
                int bufLen, pageIndex = 1;
                while ((bufLen = reader.read(buf)) != -1) {
                    if (pageIndex == pageNumber) {
                        if (lineBreak != Line_Break.LF) {
                            writer.write(text.replaceAll("\n", lineBreakValue));
                        } else {
                            writer.write(text);
                        }
                    } else {
                        String bufStr = new String(buf, 0, bufLen);
                        if (sameLineBreak) {
                            writer.write(bufStr);
                        } else {
                            writer.write(bufStr.replaceAll(sourceLineBreak, lineBreakValue));
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
    public String findNext() {
        try {
            if (file == null || findString == null || findString.isEmpty()) {
                return null;
            }
            long previousFound;
            int previousPage, pageIndex = 1, previousPos;
            long lineStart = 1, lineEnd = 1, preStart = 1, preEnd;
            if (currentFound != null) {
                previousFound = currentFound.getStart();
                previousPage = (int) (previousFound / pageSize + 1);
                previousPos = (int) (previousFound % pageSize);
            } else {
                previousPage = -1;
                previousPos = -1;
            }
            String pageText = null, preText;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                if (previousPage > 1) {
                    lineStart = lineEnd = currentPageLineStart;
                    pageIndex = previousPage;
                    reader.skip((previousPage - 1) * pageSize);
                }
                char[] buf = new char[pageSize];
                String find = findString.replaceAll("\n", lineBreakValue);
                int len, findLen = findRegex ? 1 : find.length();
                IndexRange found;
                String bufStr = null, crossString = "";
                while ((len = reader.read(buf)) != -1) {
                    preEnd = lineStart;
                    preText = bufStr;
                    bufStr = new String(buf, 0, len);
                    lineEnd += StringTools.countNumber(bufStr, lineBreakValue);
                    if (pageIndex >= previousPage) {
                        if (previousPage == pageIndex) {
                            found = StringTools.first(crossString + bufStr, find, previousPos + 1,
                                    findRegex, caseInsensitive, true);
                        } else {
                            found = StringTools.first(crossString + bufStr, find, 0,
                                    findRegex, caseInsensitive, true);
                        }
                        if (found != null) {
                            currentFound = new LongIndex();
                            currentFound.start = (pageIndex - 1) * pageSize + found.getStart() - crossString.length();
                            currentFound.end = currentFound.start + found.getLength();
                            if (!crossString.isEmpty() && currentFound.start < found.getLength()) {
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
            long cuFound;
            int cuPage, cuPos;
            int lineEnd = 1, lineStart = 1, pageIndex = 1, preStart = 1, preEnd;
            if (currentFound != null) {
                cuFound = currentFound.start;
                cuPage = (int) (cuFound / pageSize + 1);
                cuPos = (int) (cuFound % pageSize);
            } else {
                return findNext();
            }
            String pageText = null, preText;
            long maxFoundStart = -1, maxFoundEnd = -1;
            int maxLineStart = -1, maxLineEnd = -1, maxPage = 1;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                char[] buf = new char[pageSize];
                String find = findString.replaceAll("\n", lineBreakValue);
                int len, findLen = findRegex ? 1 : find.length();
                IndexRange found;
                String bufStr = null, crossString = "";
                while ((len = reader.read(buf)) != -1) {
                    preEnd = lineStart;
                    preText = bufStr;
                    bufStr = new String(buf, 0, len);
                    lineEnd += StringTools.countNumber(bufStr, lineBreakValue);
                    if (cuPage == pageIndex) {
                        if (cuPos > 0) {
                            found = StringTools.last((crossString + bufStr).substring(0, cuPos + crossString.length() - 1),
                                    find, 0, findRegex, caseInsensitive, true);
                        } else {
                            found = null;
                        }
                    } else {
                        found = StringTools.last(crossString + bufStr, find, 0, findRegex, caseInsensitive, true);
                    }
                    if (found != null) {
                        if (!crossString.isEmpty() && found.getStart() < find.length()) {
                            long actualPos = (pageIndex - 1) * pageSize - (crossString.length() - found.getStart());
                            if (actualPos != cuFound) {
                                maxPage = pageIndex - 1;
                                maxFoundStart = actualPos;
                                maxFoundEnd = maxFoundStart + found.getLength();
                                pageText = preText;
                                maxLineStart = preStart;
                                maxLineEnd = preEnd;
                            }
                        } else {
                            maxPage = pageIndex;
                            maxFoundStart = found.getStart() + (pageIndex - 1) * pageSize - crossString.length();
                            maxFoundEnd = maxFoundStart + found.getLength();
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
            currentFound = new LongIndex(maxFoundStart, maxFoundEnd);
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
            long maxFoundStart = -1, maxFoundEnd = -1;
            int maxLineStart = -1, maxLineEnd = -1, maxPage = 1;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                char[] buf = new char[pageSize];
                String find = findString.replaceAll("\n", lineBreakValue);
                int len, findLen = findRegex ? 1 : find.length();
                IndexRange found;
                String bufStr = null, crossString = "", addedStr;
                while ((len = reader.read(buf)) != -1) {
                    preEnd = lineStart;
                    preText = bufStr;
                    bufStr = new String(buf, 0, len);
                    lineEnd += StringTools.countNumber(bufStr, lineBreakValue);
                    addedStr = crossString + bufStr;
                    found = StringTools.last(addedStr, find, 0, findRegex, caseInsensitive, true);
                    if (found != null) {
                        if (!crossString.isEmpty() && found.getStart() < found.getLength()) {
                            maxPage = pageIndex - 1;
                            maxFoundStart = pageSize * (pageIndex - 1) + found.getStart() - crossString.length();
                            maxFoundEnd = maxFoundStart + found.getLength();
                            pageText = preText;
                            maxLineStart = preStart;
                            maxLineEnd = preEnd;
                        } else {
                            maxPage = pageIndex;
                            maxFoundStart = pageSize * (pageIndex - 1) + found.getStart() - crossString.length();
                            maxFoundEnd = maxFoundStart + found.getLength();
                            pageText = bufStr;
                            maxLineStart = lineStart;
                            maxLineEnd = lineEnd;
                        }
                    }
                    int strLen = addedStr.length();
                    int pos = found != null ? found.getStart() : -1;
                    crossString = addedStr.substring(Math.max(pos + 1, strLen - findLen + 1), strLen);
                    preStart = lineStart;
                    lineStart = lineEnd;
                    pageIndex++;
                }
                if (pageText == null) {
                    return null;
                }
            }
            currentFound = new LongIndex();
            currentFound.start = maxFoundStart;
            currentFound.end = maxFoundEnd;
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
    public String locateLine() {
        try {
            if (file == null || currentLine <= 0) {
                return null;
            }
            int lineEnd = 1, lineStart = 1, pageIndex = 1;
            String pageText = null;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                char[] buf = new char[pageSize];
                int len;
                String bufStr;
                while ((len = reader.read(buf)) != -1) {
                    bufStr = new String(buf, 0, len);
                    lineEnd += StringTools.countNumber(bufStr, lineBreakValue);
                    if (currentLine >= lineStart && currentLine <= lineEnd) {
                        pageText = bufStr;
                        break;
                    }
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
    public int replaceAll() {
        if (file == null
                || replaceString == null || findString == null || findString.isEmpty()) {
            return 0;
        }
        int replaceAllNumber = 0;
        try {
            File targetFile = FileTools.getTempFile();
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     InputStreamReader reader = new InputStreamReader(inputStream, charset);
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                     OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
                if (withBom) {
                    byte[] bytes = bomBytes(charset.name());
                    inputStream.skip(bytes.length);
                    outputStream.write(bytes);
                }
                char[] buf = new char[pageSize];
                String find = findString.replaceAll("\n", lineBreakValue);
                String replace = replaceString.replaceAll("\n", lineBreakValue);
                int bufLen, findLen = findRegex ? 1 : find.length();
                String thisPage, crossString = "";
                while ((bufLen = reader.read(buf)) != -1) {
                    thisPage = crossString + new String(buf, 0, bufLen);
                    Map<String, Object> ret;
                    ret = StringTools.lastAndCount(thisPage, find, findRegex, caseInsensitive, true);
                    int lastPos = (int) ret.get("lastIndex");
                    int crossFrom, pagelen = thisPage.length();
                    if (lastPos >= 0) {
                        int num = (int) ret.get("count");
                        findLen = ((String) ret.get("lastMatch")).length();
                        replaceAllNumber += num;
                        String oldString;
                        if (lastPos + findLen == pagelen) {
                            crossString = "";
                            oldString = thisPage;
                        } else {
                            crossFrom = Math.max(lastPos + findLen, pagelen - findLen + 1);
                            crossString = thisPage.substring(crossFrom, pagelen);
                            oldString = thisPage.substring(0, crossFrom);
                        }
                        thisPage = StringTools.replaceAll(oldString, find, replace, findRegex, caseInsensitive, true);
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
    public int count() {
        if (file == null || findString == null || findString.isEmpty()) {
            return 0;
        }
        int count = 0;
        try {
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    byte[] bytes = bomBytes(charset.name());
                    inputStream.skip(bytes.length);
                }
                char[] buf = new char[pageSize];
                String find = findString.replaceAll("\n", lineBreakValue);
                int bufLen, findLen = findRegex ? 1 : find.length();
                String thisPage, crossString = "";
                while ((bufLen = reader.read(buf)) != -1) {
                    thisPage = crossString + new String(buf, 0, bufLen);
                    Map<String, Object> ret;
                    ret = StringTools.lastAndCount(thisPage, find, findRegex, caseInsensitive, true);
                    int lastPos = (int) ret.get("lastIndex");
                    int crossFrom, pagelen = thisPage.length();
                    if (lastPos >= 0) {
                        int num = (int) ret.get("count");
                        findLen = ((String) ret.get("lastMatch")).length();
                        count += num;
                        if (lastPos + findLen == pagelen) {
                            crossString = "";
                        } else {
                            crossFrom = Math.max(lastPos + findLen, pagelen - findLen + 1);
                            crossString = thisPage.substring(crossFrom, pagelen);
                        }
                    } else {
                        crossFrom = pagelen - findLen + 1;
                        crossString = thisPage.substring(crossFrom, pagelen);
                    }
                }
            }

            return count;
        } catch (Exception e) {
            logger.debug(e.toString());
            return 0;
        }
    }

    @Override
    public File filter(boolean recordLineNumbers) {
        try {
            if (file == null || filterStrings == null || filterStrings.length == 0) {
                return file;
            }
            File targetFile = FileTools.getTempFile();
            int lineEnd = 1, lineStart = 1;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     InputStreamReader reader = new InputStreamReader(inputStream, charset);
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                     OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
                if (withBom) {
                    byte[] bytes = bomBytes(charset.name());
                    inputStream.skip(bytes.length);
                    outputStream.write(bytes);
                }
                char[] buf = new char[pageSize];
                int len;
                String bufStr, crossString = "";
                while ((len = reader.read(buf)) != -1) {
                    bufStr = crossString + new String(buf, 0, len);
                    String[] lines = bufStr.split(lineBreakValue);
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
                    if (bufStr.endsWith(lineBreakValue)) {
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
                    lineStart = lineEnd;
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
            logger.debug(e.toString());
            return null;
        }

    }

}
