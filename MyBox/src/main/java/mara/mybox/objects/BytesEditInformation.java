package mara.mybox.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.FileEditInformationFactory.Edit_Type;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.tools.TextTools.bomBytes;
import static mara.mybox.tools.TextTools.bomSize;
import static mara.mybox.tools.TextTools.countNumber;

/**
 * @Author Mara
 * @CreateDate 2018-12-10 13:06:33
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class BytesEditInformation extends FileEditInformation {

    public BytesEditInformation() {
        editType = Edit_Type.Bytes;
        initValues();
    }

    public BytesEditInformation(File file) {
        super(file);
        editType = Edit_Type.Bytes;
        initValues();
    }

    public String readBytes() {
        try {
            if (file == null || pageSize <= 0 || currentPage < 1) {
                return null;
            }
            StringBuilder text = new StringBuilder();
            try (FileInputStream inputStream = new FileInputStream(file)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                byte[] buf = new byte[512];
                int from = pageSize * (currentPage - 1);
                int to = pageSize * currentPage;
                int byteIndex = 0, lineIndex = 1, len, lineStart = 1, lineEnd = 1, byteCount = 0;
                boolean readCompleted = false;
                boolean bytesKnown = (objectsNumber > 0);
                boolean linesKnown = (linesNumber > 0);
                byte[] newBytes;
                String NewLineHex = TextTools.lineBreakHex(lineBreak);
                while ((len = inputStream.read(buf)) != -1) {
                    if (!readCompleted && byteIndex + len >= from) {
                        if (from >= byteIndex) {
                            lineStart = lineIndex + ByteTools.countNumber(ByteTools.subBytes(buf, 0, from - byteIndex), NewLineHex);
                            if (to >= byteIndex + len) {
                                newBytes = ByteTools.subBytes(buf, from - byteIndex, byteIndex + len - from);
                                text.append(ByteTools.bytesToHexFormat(newBytes));
                                byteCount += newBytes.length;
                                lineEnd = lineIndex + ByteTools.countNumber(buf, NewLineHex);
                            } else {
                                newBytes = ByteTools.subBytes(buf, from, to - from);
                                text.append(ByteTools.bytesToHexFormat(newBytes));
                                byteCount += newBytes.length;
                                lineEnd = lineIndex + ByteTools.countNumber(ByteTools.subBytes(buf, 0, to - byteIndex), NewLineHex);
                                readCompleted = true;
                                if (bytesKnown && linesKnown) {
                                    break;
                                }
                            }
                        } else if (to > 0 && to >= byteIndex && to <= byteIndex + len) {
                            newBytes = ByteTools.subBytes(buf, 0, to - byteIndex);
                            text.append(ByteTools.bytesToHexFormat(newBytes));
                            byteCount += newBytes.length;
                            lineEnd = lineIndex + ByteTools.countNumber(newBytes, NewLineHex);
                            readCompleted = true;
                            if (bytesKnown && linesKnown) {
                                break;
                            }
                        } else {
                            byteCount += len;
                            text.append(ByteTools.bytesToHexFormat(buf));
                            lineEnd = lineIndex + ByteTools.countNumber(buf, NewLineHex);
                        }
                    }
                    byteIndex += len;
                    lineIndex += ByteTools.countNumber(buf, NewLineHex);
                }
                if (!bytesKnown) {
                    objectsNumber = byteIndex;
                }
                if (!linesKnown) {
                    linesNumber = lineIndex;
                }
                currentPageObjectStart = from;
                currentPageObjectEnd = byteCount;
                currentPageLineStart = lineStart;
                currentPageLineEnd = lineEnd;
            }
            String NewLineFormat = TextTools.lineBreakHexFormat(lineBreak);
            return text.toString().replace(NewLineFormat, NewLineFormat.trim() + "\n");
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
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
                    } else {
                        lineStart = lineIndex;
                    }
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
            int lineIndex = 1, lineStart = 1, lineEnd = 1, pageIndex = 1;
            if (currentFound >= 0) {
                previousFound = currentFound;
                previousPage = previousFound / pageSize + 1;
                previousPos = previousFound % pageSize;
            } else {
                previousFound = -1;
                previousPage = -1;
                previousPos = -1;
            }
            String pageText = null;
            try (FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                if (previousPage > 1) {
                    lineIndex = lineStart = lineEnd = currentPageLineStart;
                    pageIndex = previousPage;
                    reader.skip((previousPage - 1) * pageSize);
                }
                String NewLine = TextTools.lineBreak(lineBreak);
                char[] buf = new char[pageSize];
                int len, pos = -1;
                String bufStr;
                while ((len = reader.read(buf)) != -1) {
                    bufStr = new String(buf, 0, len);
                    lineIndex += countNumber(bufStr, NewLine);
                    if (previousPage > pageIndex) {

                    } else if (previousPage == pageIndex) {
                        pos = bufStr.indexOf(findString, previousPos + 1);
                    } else {
                        pos = bufStr.indexOf(findString);
                    }
                    if (pos >= 0) {
                        currentFound = pos + (pageIndex - 1) * pageSize;
                        lineEnd = lineIndex;
                        pageText = bufStr;
                        break;
                    } else {
                        lineStart = lineIndex;
                    }
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
            int thisFound, thisPage, thisPos;
            int lineIndex = 1, lineStart, pageIndex = 1;
            if (currentFound >= 0) {
                thisFound = currentFound;
                thisPage = thisFound / pageSize + 1;
                thisPos = thisFound % pageSize;
            } else {
                return findNext();
            }
            String pageText = null;
            int maxIndex = -1, maxLineStart = -1, maxLineEnd = -1, maxPage = 1;
            try (FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                String NewLine = TextTools.lineBreak(lineBreak);
                char[] buf = new char[pageSize];
                int len, pos, stringLen = findString.length();
                String bufStr;
                while ((len = reader.read(buf)) != -1) {
                    bufStr = new String(buf, 0, len);
                    lineStart = lineIndex;
                    lineIndex += countNumber(bufStr, NewLine);
                    if (thisPage < pageIndex) {
                        break;
                    } else if (thisPage == pageIndex) {
                        pos = bufStr.substring(0, thisPos + stringLen - 1).lastIndexOf(findString);
                        if (pos >= 0) {
                            maxPage = pageIndex;
                            maxIndex = pageSize * (pageIndex - 1) + pos;
                            pageText = bufStr;
                            maxLineStart = lineStart;
                            maxLineEnd = lineIndex;
                        }
                        break;
                    } else {
                        pos = bufStr.lastIndexOf(findString);
                        if (pos >= 0) {
                            maxPage = pageIndex;
                            maxIndex = pageSize * (pageIndex - 1) + pos;
                            pageText = bufStr;
                            maxLineStart = lineStart;
                            maxLineEnd = lineIndex;
                        }
                    }
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
            int lineIndex = 1, lineStart, pageIndex = 1;
            String pageText = null;
            int maxIndex = -1, maxLineStart = -1, maxLineEnd = -1, maxPage = 1;
            try (FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                String NewLine = TextTools.lineBreak(lineBreak);
                char[] buf = new char[pageSize];
                int len, pos;
                String bufStr;
                while ((len = reader.read(buf)) != -1) {
                    bufStr = new String(buf, 0, len);
                    lineStart = lineIndex;
                    lineIndex += countNumber(bufStr, NewLine);
                    pos = bufStr.lastIndexOf(findString);
                    if (pos >= 0) {
                        maxPage = pageIndex;
                        maxIndex = pageSize * (pageIndex - 1) + pos;
                        pageText = bufStr;
                        maxLineStart = lineStart;
                        maxLineEnd = lineIndex;
                    }
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
                int bufLen;
                String bufStr;
                while ((bufLen = reader.read(buf)) != -1) {
                    bufStr = new String(buf, 0, bufLen);
                    int num = TextTools.countNumber(bufStr, findString);
                    if (num > 0) {
                        replaceAllNumber += num;
                        bufStr = bufStr.replaceAll(findString, replaceString);
                    }
                    writer.write(bufStr);
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
            Map<Integer, String> filtered = new HashMap<>();
            try (FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader reader = new InputStreamReader(inputStream, charset);
                    FileOutputStream outputStream = new FileOutputStream(targetFile);
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                String NewLine = TextTools.lineBreak(lineBreak);
                char[] buf = new char[pageSize];
                int len, pos;
                String bufStr;
                boolean lastEnded = true, thisEnded = false;
                while ((len = reader.read(buf)) != -1) {
                    bufStr = new String(buf, 0, len);
                    thisEnded = bufStr.endsWith(NewLine);
                    lineStart = lineEnd;
                    String[] lines = bufStr.split(NewLine);
                    for (int i = 0; i < lines.length; i++) {
                        boolean found = false;
                        for (String filter : filterStrings) {
                            found = lines[i].contains(filter);
                            if (found) {
                                break;
                            }
                        }
                        if (found) {
                            if (i == 0 && !lastEnded) {
                                if (lines.length > 1 || thisEnded) {
                                    writer.write(lines[i] + NewLine);
                                } else {
                                    writer.write(lines[i]);
                                }
                            } else if (i == lines.length - 1) {
                                if (thisEnded) {
                                    writer.write((lineStart + i) + "    " + lines[i] + NewLine);
                                } else {
                                    writer.write((lineStart + i) + "    " + lines[i]);
                                }
                            } else {
                                writer.write((lineStart + i) + "    " + lines[i] + NewLine);
                            }
                        }
                    }
                    lineEnd += lines.length - 1;
                    lastEnded = thisEnded;
                }
            }
            return targetFile;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }

    }
}
