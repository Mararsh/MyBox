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
import mara.mybox.tools.FileTmpTools;

/**
 * @Author Mara
 * @CreateDate 2018-12-10 13:06:33
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

            boolean byWidth = lineBreak == Line_Break.Width && lineBreakWidth > 0;
            if (!byWidth && lineBreakValue == null) {
                return false;
            }
            long byteIndex = 0, totalLBNumber = 0;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                int bufSize = FileTools.bufSize(file, 16), bufLen;
                byte[] buf = new byte[bufSize];
                while ((bufLen = inputStream.read(buf)) > 0) {
                    if (bufLen < bufSize) {
                        buf = ByteTools.subBytes(buf, 0, bufLen);
                    }
                    byteIndex += bufLen;
                    if (!byWidth) {
                        totalLBNumber += FindReplaceString.count(ByteTools.bytesToHexFormat(buf), lineBreakValue);
                    }
                }
            }
            objectsNumber = byteIndex;
            pagesNumber = objectsNumber / pageSize;
            if (objectsNumber % pageSize > 0) {
                pagesNumber++;
            }
            if (byWidth) {
                linesNumber = objectsNumber / lineBreakWidth + 1;
            } else {
                linesNumber = totalLBNumber + 1;
            }
            totalNumberRead = true;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    public String readPage(long pageNumber) {
        return readObjects(pageNumber * pageSize, pageSize);
    }

    @Override
    public String readObjects(long from, long number) {
        if (file == null || pageSize <= 0 || from < 0 || number < 0
                || (objectsNumber > 0 && from >= objectsNumber)) {
            return null;
        }
        boolean byWidth = lineBreak == Line_Break.Width && lineBreakWidth > 0;
        if (!byWidth && lineBreakValue == null) {
            return null;
        }
        String bufHex = null;
        long pageNumber = from / pageSize, byteIndex = 0, totalLBNumber = 0, pageLBNumber = 0, pageIndex = 0;
        int bufLen;
        try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            int bufSize;
            byte[] buf;
            boolean isCurrentPage;
            while (true) {
                isCurrentPage = pageIndex++ == pageNumber;
                if (isCurrentPage) {
                    bufSize = (int) (Math.max(pageSize, from - pageNumber * pageSize + number));
                } else {
                    bufSize = pageSize;
                }
                buf = new byte[bufSize];
                bufLen = inputStream.read(buf);
                if (bufLen <= 0) {
                    break;
                }
                if (bufLen < bufSize) {
                    buf = ByteTools.subBytes(buf, 0, bufLen);
                }
                byteIndex += bufLen;
                if (!byWidth) {
                    bufHex = ByteTools.bytesToHexFormat(buf);
                    pageLBNumber = FindReplaceString.count(bufHex, lineBreakValue);
                    totalLBNumber += pageLBNumber;
                }
                if (isCurrentPage) {
                    if (bufHex == null) {
                        bufHex = ByteTools.bytesToHexFormat(buf);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
        if (bufHex == null) {
            return null;
        }
        currentPage = pageNumber;
        currentPageObjectStart = byteIndex - bufLen;
        currentPageObjectEnd = byteIndex;
        if (byWidth) {
            currentPageLineStart = currentPageObjectStart / lineBreakWidth;
            currentPageLineEnd = currentPageObjectEnd / lineBreakWidth + 1;
        } else {
            currentPageLineStart = totalLBNumber - pageLBNumber;
            currentPageLineEnd = totalLBNumber + 1;
        }
        return bufHex;

    }

    @Override
    public String readObject(long index) {
        return readObjects(index, 1);
    }

    @Override
    public String readLines(long from, long number) {
        if (file == null || from < 0 || number < 0 || (linesNumber > 0 && from >= linesNumber)) {
            return null;
        }
        boolean byWidth = lineBreak == Line_Break.Width && lineBreakWidth > 0;
        if (!byWidth && lineBreakValue == null) {
            return null;
        }
        long byteIndex = 0, totalLBNumber = 0, pageLBNumber = 0, fromLBNumber = 0, fromByteIndex = 0;
        String pageHex = null;
        int bufLen;
        try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buf = new byte[(int) pageSize];
            long lbEnd = Math.min(from + number, linesNumber) - 1;
            String bufHex;
            while ((bufLen = inputStream.read(buf)) > 0) {
                if (bufLen < pageSize) {
                    buf = ByteTools.subBytes(buf, 0, bufLen);
                }
                bufHex = ByteTools.bytesToHexFormat(buf);
                byteIndex += bufLen;
                if (byWidth) {
                    totalLBNumber = byteIndex / lineBreakWidth;
                } else {
                    pageLBNumber = FindReplaceString.count(bufHex, lineBreakValue);
                    totalLBNumber += pageLBNumber;
                }
                if (totalLBNumber >= from) {
                    if (pageHex == null) {
                        pageHex = bufHex;
                        fromLBNumber = totalLBNumber - pageLBNumber;
                        fromByteIndex = byteIndex - bufLen;
                    } else {
                        pageHex += bufHex;
                    }
                    if (totalLBNumber >= lbEnd) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
        if (pageHex == null) {
            return null;
        }
        currentPage = fromByteIndex / pageSize;
        currentPageObjectStart = fromByteIndex;
        currentPageObjectEnd = byteIndex;
        if (byWidth) {
            currentPageLineStart = fromByteIndex / lineBreakWidth;
            currentPageLineEnd = byteIndex / lineBreakWidth + 1;
        } else {
            currentPageLineStart = fromLBNumber;
            currentPageLineEnd = totalLBNumber + 1;
        }
        return pageHex;
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
            File targetFile = FileTmpTools.getTempFile();
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
                    for (int i = 0;
                            i < lines.length;
                            ++i) {
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
            MyBoxLog.debug(e);
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
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    public boolean writePage(FileEditInformation sourceInfo, String hex) {
        try {
            if (file == null || hex == null || hex.isEmpty()
                    || sourceInfo.getFile() == null || sourceInfo.getCharset() == null) {
                return false;
            }
            int psize = sourceInfo.getPageSize();
            long pageStartByte = sourceInfo.getCurrentPageObjectStart(),
                    pLen = sourceInfo.getCurrentPageObjectEnd() - pageStartByte;
            if (psize <= 0 || pageStartByte < 0 || pLen <= 0) {
                return false;
            }
            File targetFile = file;
            if (sourceInfo.getFile().equals(file)) {
                targetFile = FileTmpTools.getTempFile();
            }
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceInfo.getFile()));
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
                int bufSize, bufLen;
                byte[] buf;
                long byteIndex = 0;
                while (true) {
                    if (byteIndex < pageStartByte) {
                        bufSize = (int) Math.min(psize, pageStartByte - byteIndex);
                    } else if (byteIndex == pageStartByte) {
                        outputStream.write(ByteTools.hexFormatToBytes(hex));
                        inputStream.skip(pLen);
                        byteIndex += pLen;
                        continue;
                    } else {
                        bufSize = psize;
                    }
                    buf = new byte[bufSize];
                    bufLen = inputStream.read(buf);
                    if (bufLen <= 0) {
                        break;
                    }
                    if (bufLen < bufSize) {
                        buf = ByteTools.subBytes(buf, 0, bufLen);
                    }
                    outputStream.write(buf);
                    byteIndex += bufLen;
                }
            }
            if (sourceInfo.getFile().equals(file)) {
                FileTools.rename(targetFile, file);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

}
