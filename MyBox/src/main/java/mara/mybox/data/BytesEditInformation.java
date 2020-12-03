package mara.mybox.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;

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
            int bufSize = FileTools.bufSize(file, 16);
            if (lineBreak == Line_Break.Width && lineBreakWidth > 0) {
                try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                    byte[] buf = new byte[bufSize];
                    long totalLines = 0;
                    int bufLen;
                    while ((bufLen = inputStream.read(buf)) > 0) {
                        if (bufLen < bufSize) {
                            buf = ByteTools.subBytes(buf, 0, bufLen);
                        }
                        totalLines += bufLen / lineBreakWidth;
                        if (bufLen % lineBreakWidth > 0) {
                            totalLines++;
                        }
                    }
                    linesNumber = totalLines;
                }
            } else if (lineBreakValue != null) {
                try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                    byte[] buf = new byte[bufSize];
                    long totalLines = 1;
                    int bufLen;
                    while ((bufLen = inputStream.read(buf)) > 0) {
                        if (bufLen < bufSize) {
                            buf = ByteTools.subBytes(buf, 0, bufLen);
                        }
                        totalLines += FindReplaceString.count(ByteTools.bytesToHexFormat(buf), lineBreakValue);
                    }
                    linesNumber = totalLines;
                }
            }
            pagesNumber = objectsNumber / pageSize;
            if (objectsNumber % pageSize > 0) {
                pagesNumber++;
            }
            totalNumberRead = true;
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
            boolean byWidth = lineBreak == Line_Break.Width && lineBreakWidth > 0;
            if (!byWidth && lineBreakValue == null) {
                return null;
            }
            long startPosition = pageSize * (pageNumber - 1);
            if (file == null || pageSize <= 0 || pageNumber < 1
                    || (objectsNumber > 0 && startPosition >= objectsNumber)) {
                return null;
            }
            long pageStart = 0, pageEnd = 0, lineEnd = 1, lineStart = 1, pageIndex = 1;
            String pageHex = null;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buf = new byte[(int) pageSize];
                int pageLen, findLen, hexStart, hexEnd = 0;
                long findStart = -1, findEnd = -1;
                if (findReplace != null && findReplace.getFileRange() != null) {
                    findStart = findReplace.getFileRange().getStart();
                    findEnd = findReplace.getFileRange().getEnd();
                }
                while ((pageLen = inputStream.read(buf)) > 0) {
                    if (pageLen < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, pageLen);
                    }
                    pageStart = pageEnd;
                    pageEnd += pageLen;
                    pageHex = ByteTools.bytesToHexFormat(buf);
                    hexStart = hexEnd;
                    hexEnd += pageHex.length();
                    lineStart = lineEnd;
                    if (pageIndex == pageNumber) {
                        if (findStart >= hexStart && findStart < hexEnd && findEnd > hexEnd) {
                            int findSize = (int) (findEnd - hexEnd) / 3;
                            byte[] findBuf = new byte[findSize];
                            if ((findLen = inputStream.read(findBuf)) > 0) {
                                if (findLen < findSize) {
                                    findBuf = ByteTools.subBytes(findBuf, 0, findLen);
                                }
                                String findText = ByteTools.bytesToHexFormat(findBuf);
                                pageHex += findText;
                            }
                        }
                    }
                    if (byWidth) {
                        lineEnd += pageLen / lineBreakWidth;
                    } else {
                        lineEnd += FindReplaceString.count(pageHex, lineBreakValue);
                    }
                    if (pageIndex == pageNumber) {
                        break;
                    }
                    pageIndex++;
                }
            }
            if (pageHex == null) {
                return null;
            }
            currentPage = pageNumber;
            currentPageObjectStart = pageStart;
            currentPageObjectEnd = pageEnd;
            currentPageLineStart = lineStart;
            currentPageLineEnd = lineEnd;
            if (findReplace != null && findReplace.getFileRange() != null) {
                findReplace.setPageReloaded(true);
                FindReplaceFile.bytesRange(findReplace, pageHex);
            }
            return pageHex;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    @Override
    public boolean writeObject(String hex) {
        try {
            if (file == null || charset == null || hex == null || hex.isEmpty()) {
                return false;
            }
            try ( BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
                byte[] bytes = ByteTools.hexFormatToBytes(hex);
                outputStream.write(bytes);
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
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceInfo.getFile()));
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
                int bufSize = (int) sourceInfo.getPageSize();
                byte[] buf = new byte[bufSize];
                int bufLen, pageIndex = 1;
                while ((bufLen = inputStream.read(buf)) > 0) {
                    if (pageIndex == pageNumber) {
                        outputStream.write(ByteTools.hexFormatToBytes(hex));
                    } else {
                        if (bufLen < bufSize) {
                            buf = ByteTools.subBytes(buf, 0, bufLen);
                        }
                        outputStream.write(buf);
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
            boolean byWidth = (lineBreak == Line_Break.Width && lineBreakWidth > 0);
            if (!byWidth && lineBreakValue == null) {
                return null;
            }
            long pageStart = 0, pageEnd = 0, lineEnd = 1, lineStart = 1, pageIndex = 1;
            String pageText = null;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buf = new byte[(int) pageSize];
                int bufLen;
                String bufHex;
                while ((bufLen = inputStream.read(buf)) > 0) {
                    if (bufLen < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, bufLen);
                    }
                    pageStart = pageEnd;
                    pageEnd += bufLen;
                    bufHex = ByteTools.bytesToHexFormat(buf);
                    lineStart = lineEnd;
                    if (byWidth) {
                        lineEnd += bufLen / lineBreakWidth;
                    } else {
                        lineEnd += FindReplaceString.count(bufHex, lineBreakValue);
                    }
                    if (currentLine >= lineStart && currentLine <= lineEnd) {
                        pageText = bufHex;
                        break;
                    }
                    pageIndex++;
                }
            }
            if (pageText == null) {
                return null;
            }
            currentPage = pageIndex;
            currentPageObjectStart = pageStart;
            currentPageObjectEnd = pageEnd;
            currentPageLineStart = lineStart;
            currentPageLineEnd = lineEnd;
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
            boolean lbWidth = (lineBreak == Line_Break.Width && lineBreakWidth > 0);
            if (!lbWidth && lineBreakValue == null) {
                return null;
            }
            File targetFile = FileTools.getTempFile();
            int lineEnd = 1, lineStart = 1;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                     OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8")) {
                byte[] buf = new byte[(int) pageSize];
                int bufLen;
                String pageText;
                while ((bufLen = inputStream.read(buf)) > 0) {
                    if (bufLen < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, bufLen);
                    }
                    pageText = ByteTools.bytesToHexFormat(buf);
                    pageText = ByteTools.formatHex(pageText, lineBreak, lineBreakWidth, lineBreakValue);
                    String[] lines = pageText.split("\n");
                    lineEnd = lineStart + lines.length - 1;
                    for (int i = 0; i < lines.length; ++i) {
                        lines[i] += " ";
                        if (isMatchFilters(lines[i])) {
                            if (recordLineNumbers) {
                                String lineNumber = StringTools.fillRightBlank(lineStart + i, 15);
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
            MyBoxLog.debug(e.toString());
            return null;
        }

    }

}
