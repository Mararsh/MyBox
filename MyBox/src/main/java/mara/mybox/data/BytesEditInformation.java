package mara.mybox.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.tools.TextTools.countNumber;
import mara.mybox.tools.ValueTools;

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

    @Override
    public boolean readTotalNumbers() {
        try {
            if (file == null) {
                return false;
            }
            objectsNumber = file.length();
            if (lineBreak == Line_Break.Width && lineBreakWidth > 0) {
                try (FileInputStream inputStream = new FileInputStream(file)) {
                    byte[] buf = new byte[(int) pageSize];
                    long totalLines = 0;
                    int len;
                    while ((len = inputStream.read(buf)) != -1) {
                        totalLines += len / lineBreakWidth;
                        if (len % lineBreakWidth > 0) {
                            totalLines++;
                        }
                    }
                    linesNumber = totalLines;
                }
            } else if (lineBreakValue != null) {
                try (FileInputStream inputStream = new FileInputStream(file)) {
                    byte[] buf = new byte[IO_BUF_LENGTH];
                    long totalLines = 1;
                    int len;
                    while ((len = inputStream.read(buf)) != -1) {
                        if (len < IO_BUF_LENGTH) {
                            buf = ByteTools.subBytes(buf, 0, len);
                        }
                        totalLines += TextTools.countNumber(ByteTools.bytesToHexFormat(buf), lineBreakValue);
                    }
                    linesNumber = totalLines;
                }
            }
            totalNumberRead = true;
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

    public String readPageByWidth(long pageNumber) {
        try {
            if (file == null || pageSize <= 0 || pageNumber < 1) {
                return null;
            }
            if (lineBreak != Line_Break.Width || lineBreakWidth <= 0) {
                return null;
            }
            String pageText;
            byte[] buf = new byte[(int) pageSize];
            try (FileInputStream inputStream = new FileInputStream(file)) {
                inputStream.skip(pageSize * (pageNumber - 1));
                int len = inputStream.read(buf);
                if (len == -1) {
                    return null;
                }
                if (len < pageSize) {
                    buf = ByteTools.subBytes(buf, 0, len);
                }
                pageText = ByteTools.bytesToHex(buf);
                currentPage = pageNumber;
                currentPageObjectStart = pageSize * (pageNumber - 1);
                currentPageObjectEnd = currentPageObjectStart + len;
                long linesPerPage = pageSize / lineBreakWidth;
                if (pageSize % lineBreakWidth > 0) {
                    linesPerPage++;
                }
                currentPageLineStart = (pageNumber - 1) * linesPerPage + 1;
                long linesThisPage = len / lineBreakWidth;
                if (len % lineBreakWidth > 0) {
                    linesThisPage++;
                }
                currentPageLineEnd = currentPageLineStart + linesThisPage - 1;
                if (!totalNumberRead && len < pageSize) {
                    objectsNumber = (pageNumber - 1) * pageSize + len;
                    linesNumber = (pageNumber - 1) * linesPerPage + linesThisPage;
                    totalNumberRead = true;
                }
            }
            return pageText;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public String readPageByValue(long pageNumber) {
        try {
            if (file == null || pageSize <= 0 || pageNumber < 1 || lineBreakValue == null) {
                return null;
            }
            long lineIndex = 1, lineStart = 1, lineEnd = 1, pageIndex = 1;
            String pageText = null;
            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] buf = new byte[(int) pageSize];
                int len;
                while ((len = inputStream.read(buf)) != -1) {
                    if (len < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, len);
                    }
                    pageText = ByteTools.bytesToHexFormat(buf);
                    lineIndex += TextTools.countNumber(pageText, lineBreakValue);
                    if (pageIndex == pageNumber) {
                        lineEnd = lineIndex;
                        break;
                    }
                    lineStart = lineIndex;
                    pageIndex++;
                }
                currentPage = pageNumber;
                currentPageObjectStart = pageSize * (pageNumber - 1);
                currentPageObjectEnd = currentPageObjectStart + len;
                currentPageLineStart = lineStart;
                currentPageLineEnd = lineEnd;
                if (!totalNumberRead && len < pageSize) {
                    objectsNumber = (pageNumber - 1) * pageSize + len;
                    linesNumber = lineEnd;
                    totalNumberRead = true;
                }
            }
            return pageText;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    @Override
    public String readPage(long pageNumber) {

        if (lineBreak == Line_Break.Width && lineBreakWidth > 0) {
            return readPageByWidth(pageNumber);

        } else if (lineBreakValue != null) {
            return readPageByValue(pageNumber);

        } else {
            return null;
        }

    }

    @Override
    public boolean writeObject(String hex) {
        try {
            if (file == null || charset == null || hex == null || hex.isEmpty()) {
                return false;
            }
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] bytes = ByteTools.hexFormatToBytes(hex);
                outputStream.write(bytes);
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
    public boolean writePage(FileEditInformation sourceInfo, long pageNumber, String hex) {
        try {
            if (file == null || hex == null || hex.isEmpty()
                    || sourceInfo.getFile() == null || sourceInfo.getCharset() == null
                    || sourceInfo.getPageSize() <= 0 || pageNumber < 1) {
                return false;
            }
            File targetFile = file;
            if (sourceInfo.getFile().equals(file)) {
                targetFile = FileTools.getTempFile();
            }
            try (FileInputStream inputStream = new FileInputStream(sourceInfo.getFile());
                    FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                int psize = (int) sourceInfo.getPageSize();
                byte[] buf = new byte[psize];
                int bufLen, pageIndex = 1;
                while ((bufLen = inputStream.read(buf)) != -1) {
                    if (pageIndex == pageNumber) {
                        outputStream.write(ByteTools.hexFormatToBytes(hex));
                    } else {
                        if (psize > bufLen) {
                            buf = ByteTools.subBytes(buf, 0, bufLen);
                        }
                        outputStream.write(buf);
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
            boolean lbWidth = (lineBreak == Line_Break.Width && lineBreakWidth > 0);
            if (!lbWidth && lineBreakValue == null) {
                return null;
            }
            long previousFound;
            int previousPage, pageIndex = 1, previousPos;
            long lineStart = 1, lineEnd = 1, preStart = 1, preEnd, linesThisPage;
            if (currentFound >= 0) {
                previousFound = currentFound;
                previousPage = (int) (previousFound / pageSize + 1);
                previousPos = (int) (previousFound % pageSize) * 3;
            } else {
//                previousFound = -1;
                previousPage = -1;
                previousPos = -1;
            }
            String pageText = null, preText;
            try (FileInputStream inputStream = new FileInputStream(file)) {
                if (previousPage > 1) {
                    lineStart = lineEnd = currentPageLineStart;
                    pageIndex = previousPage;
                    inputStream.skip((previousPage - 1) * pageSize);
                }
                byte[] buf = new byte[(int) pageSize];
                int len, pos, findLen = findString.length();
                String bufHex = null, crossString = "";
                while ((len = inputStream.read(buf)) != -1) {
                    if (len < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, len);
                    }
                    preEnd = lineStart;
                    preText = bufHex;
                    bufHex = ByteTools.bytesToHexFormat(buf);
                    if (lbWidth) {
                        linesThisPage = len / lineBreakWidth;
                        if (len % lineBreakWidth > 0) {
                            linesThisPage++;
                        }
                        lineEnd = lineStart + linesThisPage - 1;
                    } else {
                        lineEnd += countNumber(bufHex, lineBreakValue);
                    }
                    if (pageIndex >= previousPage) {
                        if (previousPage == pageIndex) {
                            pos = (crossString + bufHex).indexOf(findString, previousPos + 3);
                        } else {
                            pos = (crossString + bufHex).indexOf(findString);
                        }
                        if (pos >= 0) {
                            currentFound = (pageIndex - 1) * pageSize + (pos - crossString.length()) / 3; // position of byte
                            if (!crossString.isEmpty() && pos < findLen) {
                                pageText = preText;
                                lineStart = preStart;
                                lineEnd = preEnd;
                                pageIndex--;
                                break;
                            } else {
                                pageText = bufHex;
                                break;
                            }
                        }
                    }
                    int strLen = bufHex.length();
                    if (previousPage == pageIndex) {
                        crossString = bufHex.substring(Math.max(previousPos + 3, strLen - findLen + 3), strLen);
                    } else {
                        crossString = bufHex.substring(strLen - findLen + 3, strLen);
                    }
                    preStart = lineStart;
                    if (lbWidth) {
                        lineStart = lineEnd + 1;
                    } else {
                        lineStart = lineEnd;
                    }
                    pageIndex++;
                }
                if (pageText == null) {
                    return null;
                }
            }
            currentPage = pageIndex;
            currentPageObjectStart = pageSize * (pageIndex - 1);
            currentPageObjectEnd = currentPageObjectStart + pageText.length() / 3;
            currentPageLineStart = lineStart;
            currentPageLineEnd = lineEnd;
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
            boolean lbWidth = (lineBreak == Line_Break.Width && lineBreakWidth > 0);
            if (!lbWidth && lineBreakValue == null) {
                return null;
            }
            long cuFound;
            int cuPage, cuPos;
            int lineEnd = 1, lineStart = 1, pageIndex = 1, preStart = 1, preEnd, linesThisPage;
            if (currentFound >= 0) {
                cuFound = currentFound;
                cuPage = (int) (cuFound / pageSize + 1);
                cuPos = (int) (cuFound % pageSize) * 3;
            } else {
                return findNext();
            }
            String pageText = null, preText;
            long maxIndex = -1;
            int maxLineStart = -1, maxLineEnd = -1, maxPage = 1;
            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] buf = new byte[(int) pageSize];
                int len, pos, findLen = findString.length();
                String bufHex = null, crossString = "";
                while ((len = inputStream.read(buf)) != -1) {
                    if (len < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, len);
                    }
                    preEnd = lineStart;
                    preText = bufHex;
                    bufHex = ByteTools.bytesToHexFormat(buf);
                    if (lbWidth) {
                        linesThisPage = len / lineBreakWidth;
                        if (len % lineBreakWidth > 0) {
                            linesThisPage++;
                        }
                        lineEnd = lineStart + linesThisPage - 1;
                    } else {
                        lineEnd += countNumber(bufHex, lineBreakValue);
                    }
                    if (cuPage == pageIndex) {
                        if (cuPos > 0) {
                            pos = (crossString + bufHex).substring(0, cuPos + crossString.length() - 3).lastIndexOf(findString);
                        } else {
                            pos = -1;
                        }
                    } else {
                        pos = (crossString + bufHex).lastIndexOf(findString);
                    }
                    if (pos >= 0) {
                        if (!crossString.isEmpty() && pos < findLen) {
                            long actualPos = (pageIndex - 1) * pageSize - (crossString.length() - pos) / 3;
                            if (actualPos != cuFound) {
                                maxPage = pageIndex - 1;
                                maxIndex = actualPos;
                                pageText = preText;
                                maxLineStart = preStart;
                                maxLineEnd = preEnd;
                            }
                        } else {
                            maxPage = pageIndex;
                            maxIndex = (pageIndex - 1) * pageSize + (pos - crossString.length()) / 3;
                            pageText = bufHex;
                            maxLineStart = lineStart;
                            maxLineEnd = lineEnd;
                        }
                    }
                    if (pageIndex == cuPage) {
                        break;
                    }
                    int strLen = bufHex.length();
                    crossString = bufHex.substring(strLen - findLen + 3, strLen);
                    preStart = lineStart;
                    if (lbWidth) {
                        lineStart = lineEnd + 1;
                    } else {
                        lineStart = lineEnd;
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
            currentPageObjectEnd = currentPageObjectStart + pageText.length() / 3;
            currentPageLineStart = maxLineStart;
            currentPageLineEnd = maxLineEnd;
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
            boolean lbWidth = (lineBreak == Line_Break.Width && lineBreakWidth > 0);
            if (!lbWidth && lineBreakValue == null) {
                return null;
            }
            int lineEnd = 1, lineStart = 1, pageIndex = 1, preStart = 1, preEnd, linesThisPage;
            String pageText = null, preText;
            long maxIndex = -1;
            int maxLineStart = -1, maxLineEnd = -1, maxPage = 1;
            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] buf = new byte[(int) pageSize];
                int len, pos, findLen = findString.length();
                String bufHex = null, crossString = "", addedStr;
                while ((len = inputStream.read(buf)) != -1) {
                    if (len < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, len);
                    }
                    preEnd = lineStart;
                    preText = bufHex;
                    bufHex = ByteTools.bytesToHexFormat(buf);
                    if (lbWidth) {
                        linesThisPage = len / lineBreakWidth;
                        if (len % lineBreakWidth > 0) {
                            linesThisPage++;
                        }
                        lineEnd = lineStart + linesThisPage - 1;
                    } else {
                        lineEnd += countNumber(bufHex, lineBreakValue);
                    }
                    addedStr = crossString + bufHex;
                    pos = addedStr.lastIndexOf(findString);
                    if (pos >= 0) {
                        if (!crossString.isEmpty() && pos < findLen) {
                            maxPage = pageIndex - 1;
                            maxIndex = pageSize * (pageIndex - 1) + (pos - crossString.length()) / 3;
                            pageText = preText;
                            maxLineStart = preStart;
                            maxLineEnd = preEnd;
                        } else {
                            maxPage = pageIndex;
                            maxIndex = pageSize * (pageIndex - 1) + (pos - crossString.length()) / 3;
                            pageText = bufHex;
                            maxLineStart = lineStart;
                            maxLineEnd = lineEnd;
                        }
                    }
                    int strLen = addedStr.length();
                    crossString = addedStr.substring(Math.max(pos + 3, strLen - findLen + 3), strLen);
                    preStart = lineStart;
                    if (lbWidth) {
                        lineStart = lineEnd + 1;
                    } else {
                        lineStart = lineEnd;
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
            currentPageObjectEnd = currentPageObjectStart + pageText.length() / 3;
            currentPageLineStart = maxLineStart;
            currentPageLineEnd = maxLineEnd;
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
            boolean lbWidth = (lineBreak == Line_Break.Width && lineBreakWidth > 0);
            if (!lbWidth && lineBreakValue == null) {
                return null;
            }
            int lineEnd = 1, lineStart = 1, pageIndex = 1, linesThisPage;
            String pageText = null;
            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] buf = new byte[(int) pageSize];
                int len;
                String bufHex;
                while ((len = inputStream.read(buf)) != -1) {
                    if (len < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, len);
                    }
                    bufHex = ByteTools.bytesToHexFormat(buf);
                    if (lbWidth) {
                        linesThisPage = len / lineBreakWidth;
                        if (len % lineBreakWidth > 0) {
                            linesThisPage++;
                        }
                        lineEnd = lineStart + linesThisPage - 1;
                    } else {
                        lineEnd += countNumber(bufHex, lineBreakValue);
                    }
                    if (currentLine >= lineStart && currentLine <= lineEnd) {
                        pageText = bufHex;
                        break;
                    }
                    if (lbWidth) {
                        lineStart = lineEnd + 1;
                    } else {
                        lineStart = lineEnd;
                    }
                    pageIndex++;
                }
                if (pageText == null) {
                    return null;
                }
            }
            currentPage = pageIndex;
            currentPageObjectStart = pageSize * (pageIndex - 1);
            currentPageObjectEnd = currentPageObjectStart + pageText.length() / 3;
            currentPageLineStart = lineStart;
            currentPageLineEnd = lineEnd;
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
                    FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                byte[] buf = new byte[(int) pageSize];
                int bufLen, findLen = findString.length();
                String thisPage, crossString = "";
                while ((bufLen = inputStream.read(buf)) != -1) {
                    if (bufLen < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, bufLen);
                    }
                    thisPage = crossString + ByteTools.bytesToHexFormat(buf);
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
                            crossFrom = Math.max(lastPos + findLen, pagelen - findLen + 3);
                            crossString = thisPage.substring(crossFrom, pagelen);
                            thisPage = thisPage.substring(0, crossFrom).replaceAll(findString, replaceString);
                        }
                    } else {
                        crossFrom = pagelen - findLen + 3;
                        crossString = thisPage.substring(crossFrom, pagelen);
                        thisPage = thisPage.substring(0, crossFrom);
                    }
                    outputStream.write(ByteTools.hexFormatToBytes(thisPage));
                }
                if (!crossString.isEmpty()) {
                    outputStream.write(ByteTools.hexFormatToBytes(crossString));
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
            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] buf = new byte[(int) pageSize];
                int bufLen, findLen = findString.length();
                String thisPage, crossString = "";
                while ((bufLen = inputStream.read(buf)) != -1) {
                    if (bufLen < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, bufLen);
                    }
                    thisPage = crossString + ByteTools.bytesToHexFormat(buf);
                    int[] ret = TextTools.lastAndCount(thisPage, findString);
                    int lastPos = ret[0];
                    int crossFrom, pagelen = thisPage.length();
                    if (lastPos >= 0) {
                        int num = ret[1];
                        count += num;
                        if (lastPos + findLen == pagelen) {
                            crossString = "";
                        } else {
                            crossFrom = Math.max(lastPos + findLen, pagelen - findLen + 3);
                            crossString = thisPage.substring(crossFrom, pagelen);
                        }
                    } else {
                        crossFrom = pagelen - findLen + 3;
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
            boolean lbWidth = (lineBreak == Line_Break.Width && lineBreakWidth > 0);
            if (!lbWidth && lineBreakValue == null) {
                return null;
            }
            File targetFile = FileTools.getTempFile();
            int lineEnd = 1, lineStart = 1;
            try (FileInputStream inputStream = new FileInputStream(file);
                    FileOutputStream outputStream = new FileOutputStream(targetFile);
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8")) {
                byte[] buf = new byte[(int) pageSize];
                int bufLen;
                String pageText;
                while ((bufLen = inputStream.read(buf)) != -1) {
                    if (bufLen < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, bufLen);
                    }
                    pageText = ByteTools.bytesToHexFormat(buf);
                    pageText = ByteTools.formatHex(pageText, lineBreak, lineBreakWidth, lineBreakValue);
                    String[] lines = pageText.split("\n");
                    lineEnd = lineStart + lines.length - 1;
                    for (int i = 0; i < lines.length; i++) {
                        lines[i] += " ";
                        if (isMatchFilters(lines[i])) {
                            if (recordLineNumbers) {
                                String lineNumber = ValueTools.fillRightBlank(lineStart + i, 15);
                                writer.write(lineNumber + "    " + lines[i] + System.lineSeparator());
                            } else {
                                writer.write(lines[i] + System.lineSeparator());
                            }
                        }
                    }
                    if (lbWidth) {
                        lineStart = lineEnd + 1;
                    } else {
                        lineStart = lineEnd;
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
