package mara.mybox.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2018-12-10 13:06:33
 * @License Apache License Version 2.0
 */
public class BytesEditInformation extends FileEditInformation {

    public BytesEditInformation() {
        editType = Edit_Type.Bytes;
    }

    public BytesEditInformation(File file) {
        super(file);
        editType = Edit_Type.Bytes;
        initValues();
    }

    @Override
    public boolean readTotalNumbers(FxTask currentTask) {
        try {
            if (file == null) {
                return false;
            }
            pagination.objectsNumber = 0;
            pagination.rowsNumber = 0;
            pagination.pagesNumber = 1;

            boolean byWidth = lineBreak == Line_Break.Width && lineBreakWidth > 0;
            if (!byWidth && lineBreakValue == null) {
                return false;
            }
            long byteIndex = 0, totalLBNumber = 0;
            try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                int bufSize = FileTools.bufSize(file, 16), bufLen;
                byte[] buf = new byte[bufSize];
                while ((bufLen = inputStream.read(buf)) > 0) {
                    if (currentTask != null && !currentTask.isWorking()) {
                        return false;
                    }
                    if (bufLen < bufSize) {
                        buf = ByteTools.subBytes(buf, 0, bufLen);
                    }
                    byteIndex += bufLen;
                    if (!byWidth) {
                        totalLBNumber += FindReplaceString.count(currentTask,
                                ByteTools.bytesToHexFormat(buf), lineBreakValue);
                    }
                }
            }
            pagination.objectsNumber = byteIndex;
            pagination.pagesNumber = pagination.objectsNumber / pagination.pageSize;
            if (pagination.objectsNumber % pagination.pageSize > 0) {
                pagination.pagesNumber++;
            }
            if (byWidth) {
                pagination.rowsNumber = pagination.objectsNumber / lineBreakWidth + 1;
            } else {
                pagination.rowsNumber = totalLBNumber + 1;
            }
            totalNumberRead = true;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    public String readPage(FxTask currentTask, long pageNumber) {
        return readObjects(currentTask, pageNumber * pagination.pageSize, pagination.pageSize);
    }

    @Override
    public String readObjects(FxTask currentTask, long from, long number) {
        if (file == null || pagination.pageSize <= 0 || from < 0 || number < 0
                || (pagination.objectsNumber > 0 && from >= pagination.objectsNumber)) {
            return null;
        }
        boolean byWidth = lineBreak == Line_Break.Width && lineBreakWidth > 0;
        if (!byWidth && lineBreakValue == null) {
            return null;
        }
        String bufHex = null;
        long pageNumber = from / pagination.pageSize, byteIndex = 0, totalLBNumber = 0, pageLBNumber = 0, pageIndex = 0;
        int bufLen;
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            int bufSize;
            byte[] buf;
            boolean isCurrentPage;
            while (true) {
                if (currentTask != null && !currentTask.isWorking()) {
                    return null;
                }
                isCurrentPage = pageIndex++ == pageNumber;
                if (isCurrentPage) {
                    bufSize = (int) (Math.max(pagination.pageSize, from - pageNumber * pagination.pageSize + number));
                } else {
                    bufSize = pagination.pageSize;
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
                    pageLBNumber = FindReplaceString.count(currentTask, bufHex, lineBreakValue);
                    if (currentTask != null && !currentTask.isWorking()) {
                        return null;
                    }
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
        if (currentTask != null && !currentTask.isWorking()) {
            return null;
        }
        if (bufHex == null) {
            return null;
        }
        pagination.currentPage = pageNumber;
        pagination.startObjectOfCurrentPage = byteIndex - bufLen;
        pagination.endObjectOfCurrentPage = byteIndex;
        if (byWidth) {
            pagination.startRowOfCurrentPage = pagination.startObjectOfCurrentPage / lineBreakWidth;
            pagination.endRowOfCurrentPage = pagination.endObjectOfCurrentPage / lineBreakWidth + 1;
        } else {
            pagination.startRowOfCurrentPage = totalLBNumber - pageLBNumber;
            pagination.endRowOfCurrentPage = totalLBNumber + 1;
        }
        return bufHex;

    }

    @Override
    public String readObject(FxTask currentTask, long index) {
        return readObjects(currentTask, index, 1);
    }

    @Override
    public String readLines(FxTask currentTask, long from, long number) {
        if (file == null || from < 0 || number < 0
                || (pagination.rowsNumber > 0 && from >= pagination.rowsNumber)) {
            return null;
        }
        boolean byWidth = lineBreak == Line_Break.Width && lineBreakWidth > 0;
        if (!byWidth && lineBreakValue == null) {
            return null;
        }
        long byteIndex = 0, totalLBNumber = 0, pageLBNumber = 0, fromLBNumber = 0, fromByteIndex = 0;
        String pageHex = null;
        int bufLen;
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buf = new byte[(int) pagination.pageSize];
            long lbEnd = Math.min(from + number, pagination.rowsNumber) - 1;
            String bufHex;
            while ((bufLen = inputStream.read(buf)) > 0) {
                if (currentTask != null && !currentTask.isWorking()) {
                    return null;
                }
                if (bufLen < pagination.pageSize) {
                    buf = ByteTools.subBytes(buf, 0, bufLen);
                }
                bufHex = ByteTools.bytesToHexFormat(buf);
                byteIndex += bufLen;
                if (byWidth) {
                    totalLBNumber = byteIndex / lineBreakWidth;
                } else {
                    pageLBNumber = FindReplaceString.count(currentTask, bufHex, lineBreakValue);
                    if (currentTask != null && !currentTask.isWorking()) {
                        return null;
                    }
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
        if (currentTask != null && !currentTask.isWorking()) {
            return null;
        }
        if (pageHex == null) {
            return null;
        }
        pagination.currentPage = fromByteIndex / pagination.pageSize;
        pagination.startObjectOfCurrentPage = fromByteIndex;
        pagination.endObjectOfCurrentPage = byteIndex;
        if (byWidth) {
            pagination.startRowOfCurrentPage = fromByteIndex / lineBreakWidth;
            pagination.endRowOfCurrentPage = byteIndex / lineBreakWidth + 1;
        } else {
            pagination.startRowOfCurrentPage = fromLBNumber;
            pagination.endRowOfCurrentPage = totalLBNumber + 1;
        }
        return pageHex;
    }

    @Override
    public File filter(FxTask currentTask, boolean recordLineNumbers) {
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
            try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8")) {
                byte[] buf = new byte[pagination.pageSize];
                int bufLen;
                String pageHex;
                while ((bufLen = inputStream.read(buf)) > 0) {
                    if (currentTask != null && !currentTask.isWorking()) {
                        return null;
                    }
                    if (bufLen < pagination.pageSize) {
                        buf = ByteTools.subBytes(buf, 0, bufLen);
                    }
                    pageHex = ByteTools.bytesToHexFormat(buf);
                    pageHex = ByteTools.formatHex(pageHex, lineBreak, lineBreakWidth, lineBreakValue);
                    String[] lines = pageHex.split("\n");
                    lineEnd = lineStart + lines.length - 1;
                    for (int i = 0; i < lines.length; ++i) {
                        if (currentTask != null && !currentTask.isWorking()) {
                            return null;
                        }
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
    public boolean writeObject(FxTask currentTask, String hex) {
        try {
            if (file == null || charset == null || hex == null || hex.isEmpty()) {
                return false;
            }
            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
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
    public boolean writePage(FxTask currentTask, FileEditInformation sourceInfo, String hex) {
        try {
            MyBoxLog.console(file);
            if (file == null || hex == null || hex.isEmpty()
                    || sourceInfo.getFile() == null || sourceInfo.getCharset() == null) {
                return false;
            }
            int psize = sourceInfo.pagination.pageSize;
            long pageStartByte = sourceInfo.pagination.startObjectOfCurrentPage,
                    pLen = sourceInfo.pagination.endObjectOfCurrentPage - pageStartByte;
            if (psize <= 0 || pageStartByte < 0 || pLen <= 0) {
                return false;
            }
            File targetFile = file;
            if (sourceInfo.getFile().equals(file)) {
                targetFile = FileTmpTools.getTempFile();
            }
            try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceInfo.getFile()));
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
                int bufSize, bufLen;
                byte[] buf;
                long byteIndex = 0;
                while (true) {
                    if (currentTask != null && !currentTask.isWorking()) {
                        return false;
                    }
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
