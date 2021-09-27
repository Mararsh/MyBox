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
import mara.mybox.tools.TmpFileTools;

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
            objectsNumber = 0;
            linesNumber = 0;
            pagesNumber = 1;
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
            objectsNumber = file.length();
            pagesNumber = objectsNumber / pageSize;
            if (objectsNumber % pageSize > 0) {
                pagesNumber++;
            }
            totalNumberRead = true;
//            MyBoxLog.debug(objectsNumber + "   " + pageSize + " " + pagesNumber);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    @Override
    public String readPage(long pageNumber) {
        try {
            if (file == null || pageSize <= 0 || pageNumber < 0) {
                return null;
            }
            boolean byWidth = lineBreak == Line_Break.Width && lineBreakWidth > 0;
            if (!byWidth && lineBreakValue == null) {
                return null;
            }
            if (objectsNumber > 0 && pageSize * (pageNumber - 1) >= objectsNumber) {
                return null;
            }
            long pageStart = 0, pageEnd = 0, lineIndex = 0, lineStart = 0, pageIndex = 0;
            String pageHex = null;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buf = new byte[(int) pageSize];
                int bytesLen;
                while ((bytesLen = inputStream.read(buf)) > 0) {
                    if (bytesLen < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, bytesLen);
                    }
                    pageStart = pageEnd;
                    pageEnd += bytesLen;
                    pageHex = ByteTools.bytesToHexFormat(buf);
                    lineStart = lineIndex;
                    if (byWidth) {
                        lineIndex += bytesLen / lineBreakWidth;
                    } else {
                        lineIndex += FindReplaceString.count(pageHex, lineBreakValue);
                    }
                    if (pageIndex++ == pageNumber) {
                        break;
                    }
                }
            }
            if (pageHex == null) {
                return null;
            }
            currentPage = pageNumber;
            currentPageObjectStart = pageStart;
            currentPageObjectEnd = pageEnd;
            currentPageLineStart = lineStart;
            currentPageLineEnd = lineIndex;
            return pageHex;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    @Override
    public String locateLine(long line) {
        try {
            if (file == null || line < 0) {
                return null;
            }
            boolean byWidth = (lineBreak == Line_Break.Width && lineBreakWidth > 0);
            if (!byWidth && lineBreakValue == null) {
                return null;
            }
            long pageStart = 0, pageEnd = 0, lineEnd = 0, lineStart = 0, pageIndex = 0;
            String pageText = null;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buf = new byte[(int) pageSize];
                int bufLen;
                String pageHex;
                while ((bufLen = inputStream.read(buf)) > 0) {
                    if (bufLen < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, bufLen);
                    }
                    pageStart = pageEnd;
                    pageEnd += bufLen;
                    pageHex = ByteTools.bytesToHexFormat(buf);
                    lineStart = lineEnd;
                    if (byWidth) {
                        lineEnd += bufLen / lineBreakWidth;
                    } else {
                        lineEnd += FindReplaceString.count(pageHex, lineBreakValue);
                    }
                    if (line >= lineStart && line < lineEnd) {
                        pageText = pageHex;
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
    public String locateObject(long index) {
        try {
            if (file == null || index < 0 || index >= objectsNumber) {
                return null;
            }
            return readPage(index / pageSize);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    @Override
    public String locateRange(LongIndex range) {
        try {
            if (file == null || range == null || range.start >= objectsNumber || range.start >= range.end) {
                return null;
            }
            boolean byWidth = lineBreak == Line_Break.Width && lineBreakWidth > 0;
            if (!byWidth && lineBreakValue == null) {
                return null;
            }
            long pageStartByte = 0, bytesIndex = 0, lineIndex = 0, lineStart = 0, pageStartLine = 0, hexIndex = 0;
            String bufHex = "", pageHex = null;
            int hexLen;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buf = new byte[(int) pageSize];
                int bytesLen;
                boolean found = false;
                while ((bytesLen = inputStream.read(buf)) > 0) {
                    if (bytesLen < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, bytesLen);
                    }
                    if (!found) {
                        lineStart = lineIndex;
                        pageStartByte = bytesIndex;
                    }
                    bytesIndex += bytesLen;
                    bufHex = ByteTools.bytesToHexFormat(buf);
                    hexLen = bufHex.length();
                    hexIndex += hexLen;
                    if (byWidth) {
                        lineIndex += bytesLen / lineBreakWidth;
                    } else {
                        lineIndex += FindReplaceString.count(bufHex, lineBreakValue);
                    }
                    if (!found && hexIndex >= range.start) {
                        found = true;
                    }
                    if (found) {
                        int extra = (int) (hexIndex - range.end);
                        if (extra > 0) {
                            pageHex += bufHex.substring(0, hexLen - extra);
                        } else {
                            pageHex += bufHex;
                        }
                        if (extra >= 0) {
                            break;
                        }
                    } else {
                        pageHex = "";
                    }
                }
            }
            if (pageHex == null) {
                return null;
            }
            currentPage = pageStartLine;
            currentPageObjectStart = pageStartByte;
            currentPageObjectEnd = bytesIndex;
            currentPageLineStart = lineStart;
            currentPageLineEnd = lineIndex;
            return pageHex;
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
            File targetFile = TmpFileTools.getTempFile();
            int lineEnd = 0, lineStart = 0;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                     OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8")) {
                byte[] buf = new byte[(int) pageSize];
                int bufLen;
                String pageHex;
                while ((bufLen = inputStream.read(buf)) > 0) {
                    if (bufLen < pageSize) {
                        buf = ByteTools.subBytes(buf, 0, bufLen);
                    }
                    pageHex = ByteTools.bytesToHexFormat(buf);
                    pageHex = ByteTools.formatHex(pageHex, lineBreak, lineBreakWidth, lineBreakValue);
                    String[] lines = pageHex.split("\n");
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
                    || sourceInfo.getPageSize() <= 0 || pageNumber < 0) {
                return false;
            }
            File targetFile = file;
            if (sourceInfo.getFile().equals(file)) {
                targetFile = TmpFileTools.getTempFile();
            }
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceInfo.getFile()));
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
                int bufSize = (int) sourceInfo.getPageSize();
                byte[] buf = new byte[bufSize];
                int bufLen, pageIndex = 0;
                while ((bufLen = inputStream.read(buf)) > 0) {
                    if (pageIndex++ == pageNumber) {
                        outputStream.write(ByteTools.hexFormatToBytes(hex));
                    } else {
                        if (bufLen < bufSize) {
                            buf = ByteTools.subBytes(buf, 0, bufLen);
                        }
                        outputStream.write(buf);
                    }
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

}
