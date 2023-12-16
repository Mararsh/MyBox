package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class FileSplitTools {

    public static List<File> splitFileByFilesNumber(FxTask currentTask, File file, String filename, long filesNumber) {
        try {
            if (file == null || filesNumber <= 0) {
                return null;
            }
            long bytesNumber = file.length() / filesNumber;
            List<File> splittedFiles = new ArrayList<>();
            try (final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                String newFilename;
                int digit = (filesNumber + "").length();
                byte[] buf = new byte[(int) bytesNumber];
                int bufLen;
                int fileIndex = 1;
                int startIndex = 0;
                int endIndex = 0;
                while ((fileIndex < filesNumber) && (bufLen = inputStream.read(buf)) > 0) {
                    if (currentTask == null || !currentTask.isWorking()) {
                        return null;
                    }
                    endIndex += bufLen;
                    newFilename = filename + "-cut-f" + StringTools.fillLeftZero(fileIndex, digit) + "-b" + (startIndex + 1) + "-b" + endIndex;
                    try (final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newFilename))) {
                        if (bytesNumber > bufLen) {
                            buf = ByteTools.subBytes(buf, 0, bufLen);
                        }
                        outputStream.write(buf);
                    }
                    splittedFiles.add(new File(newFilename));
                    fileIndex++;
                    startIndex = endIndex;
                }
                if (currentTask == null || !currentTask.isWorking()) {
                    return null;
                }
                buf = new byte[(int) (file.length() - endIndex)];
                bufLen = inputStream.read(buf);
                if (bufLen > 0) {
                    endIndex += bufLen;
                    newFilename = filename + "-cut-f" + StringTools.fillLeftZero(fileIndex, digit) + "-b" + (startIndex + 1) + "-b" + endIndex;
                    try (final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newFilename))) {
                        outputStream.write(buf);
                    }
                    splittedFiles.add(new File(newFilename));
                }
            }
            return splittedFiles;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static List<File> splitFileByStartEndList(FxTask currentTask, File file, String filename, List<Long> startEndList) {
        try {
            if (file == null || startEndList == null || startEndList.isEmpty() || startEndList.size() % 2 > 0) {
                return null;
            }
            List<File> splittedFiles = new ArrayList<>();
            for (int i = 0; i < startEndList.size(); i += 2) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return null;
                }
                File f = cutFile(file, filename, startEndList.get(i), startEndList.get(i + 1));
                if (f != null) {
                    splittedFiles.add(f);
                }
            }
            return splittedFiles;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public static List<File> splitFileByBytesNumber(FxTask currentTask, File file, String filename, long bytesNumber) {
        try {
            if (file == null || bytesNumber <= 0) {
                return null;
            }
            List<File> splittedFiles = new ArrayList<>();
            try (final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                String newFilename;
                long fnumber = file.length() / bytesNumber;
                if (file.length() % bytesNumber > 0) {
                    fnumber++;
                }
                int digit = (fnumber + "").length();
                byte[] buf = new byte[(int) bytesNumber];
                int bufLen;
                int fileIndex = 1;
                int startIndex = 0;
                int endIndex = 0;
                while ((bufLen = inputStream.read(buf)) > 0) {
                    if (currentTask == null || !currentTask.isWorking()) {
                        return null;
                    }
                    endIndex += bufLen;
                    newFilename = filename + "-cut-f" + StringTools.fillLeftZero(fileIndex, digit) + "-b" + (startIndex + 1) + "-b" + endIndex;
                    try (final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newFilename))) {
                        if (bytesNumber > bufLen) {
                            buf = ByteTools.subBytes(buf, 0, bufLen);
                        }
                        outputStream.write(buf);
                    }
                    splittedFiles.add(new File(newFilename));
                    fileIndex++;
                    startIndex = endIndex;
                }
            }
            return splittedFiles;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    // 1-based start, that is: from (start - 1) to ( end - 1) actually
    public static File cutFile(File file, String filename, long startIndex, long endIndex) {
        try {
            if (file == null || startIndex < 1 || startIndex > endIndex) {
                return null;
            }
            File tempFile = FileTmpTools.getTempFile();
            String newFilename = filename + "-cut-b" + startIndex + "-b" + endIndex;
            try (final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                if (startIndex > 1) {
                    inputStream.skip(startIndex - 1);
                }
                int cutLength = (int) (endIndex - startIndex + 1);
                byte[] buf = new byte[cutLength];
                int bufLen;
                bufLen = inputStream.read(buf);
                if (bufLen <= 0) {
                    return null;
                }
                try (final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                    if (cutLength > bufLen) {
                        buf = ByteTools.subBytes(buf, 0, bufLen);
                        newFilename = filename + "-cut-b" + startIndex + "-b" + bufLen;
                    }
                    outputStream.write(buf);
                }
            }
            File actualFile = new File(newFilename);
            if (FileTools.override(tempFile, actualFile)) {
                return actualFile;
            } else {
                return null;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

}
