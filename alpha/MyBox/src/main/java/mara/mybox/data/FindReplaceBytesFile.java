package mara.mybox.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javafx.scene.control.IndexRange;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TmpFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-11-9
 * @License Apache License Version 2.0
 */
public class FindReplaceBytesFile {

    public static boolean countBytes(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        if (sourceInfo == null || sourceInfo.getFile() == null || findReplaceFile == null
                || findReplaceFile.getFileFindString() == null || findReplaceFile.getFileFindString().isEmpty()) {
            if (findReplaceFile != null) {
                findReplaceFile.setError(message("InvalidParameters"));
            }
            return false;
        }
        sourceInfo.setFindReplace(findReplaceFile);
        findReplaceFile.setFileInfo(sourceInfo);
        int count = 0;
        File sourceFile = sourceInfo.getFile();
//        MyBoxLog.debug("sourceFile:" + sourceFile);
        try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile))) {
            int pageSize = FileTools.bufSize(sourceFile, 48);
//            MyBoxLog.debug(availableMem + " " + sourceFile.length() + " " + pageSize);
            byte[] pageBytes = new byte[pageSize];
            // findString should have been in hex format
            String findString = findReplaceFile.getFileFindString();
            int findLen = findReplaceFile.isIsRegex() ? pageSize * 3 : findString.length();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setAnchor(0).setWrap(false);
            int bufLen, crossFrom, hexLen;
            String pageHex, crossString = "", checkedString;
            while ((bufLen = inputStream.read(pageBytes)) > 0) {
                if (bufLen < pageSize) {
                    pageBytes = ByteTools.subBytes(pageBytes, 0, bufLen);
                }
                pageHex = ByteTools.bytesToHexFormat(pageBytes);
                hexLen = pageHex.length();
                checkedString = crossString + pageHex;
//                MyBoxLog.debug(pageLen + " " + checkedString.length());
                findReplaceString.setInputString(checkedString).run();
                IndexRange lastRange = findReplaceString.getStringRange();
                if (lastRange != null) {
                    count += findReplaceString.getCount();
                    if (lastRange.getEnd() == checkedString.length()) {
                        crossString = "";
                    } else {
                        crossFrom = Math.max(3, hexLen - findLen + 3);
                        crossFrom = Math.max(lastRange.getStart() - crossString.length() + 3, crossFrom);
                        crossString = pageHex.substring(crossFrom, hexLen);
                    }
                } else {
                    crossFrom = Math.max(3, hexLen - findLen + 3);
                    crossString = pageHex.substring(crossFrom, hexLen);
                }
            }
            findReplaceFile.setCount(count);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceFile.setError(e.toString());
            return false;
        }
    }

    public static LongIndex findNextBytesRange(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        if (sourceInfo == null || sourceInfo.getFile() == null || findReplaceFile == null
                || findReplaceFile.getFileFindString() == null || findReplaceFile.getFileFindString().isEmpty()) {
            if (findReplaceFile != null) {
                findReplaceFile.setError(message("InvalidParameters"));
            }
            return null;
        }
        try {
            sourceInfo.setFindReplace(findReplaceFile);
            findReplaceFile.setFileInfo(sourceInfo);
            File sourceFile = sourceInfo.getFile();
            long fileLength = sourceFile.length();
            long hexStartPosition = (findReplaceFile.getPosition() * 3) / 3;
            long bytesStartPosition = hexStartPosition / 3;
            if (bytesStartPosition < 0 || bytesStartPosition >= fileLength) {
                if (findReplaceFile.isWrap()) {
                    hexStartPosition = 0;
                    bytesStartPosition = 0;
                } else {
//                    findReplaceFile.setError(message("InvalidParameters"));
                    return null;
                }
            }
            int maxBytesLen = FileTools.bufSize(sourceFile, 48);
            String findString = findReplaceFile.getFileFindString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setAnchor(0).setWrap(false);
            // findString should have been in hex format
            int findHexLen = findReplaceFile.isIsRegex() ? maxBytesLen * 3 : findString.length();
            LongIndex found = findNextBytesRange(sourceFile, findReplaceString, hexStartPosition, fileLength * 3, maxBytesLen, findHexLen);
            if (found == null && findReplaceString.getError() != null) {
                findReplaceFile.setError(findReplaceString.getError());
                return null;
            }
            if (found == null && findReplaceFile.isWrap() && bytesStartPosition > 0 && bytesStartPosition < fileLength * 3) {
                long hexEndPosition;
                if (findReplaceFile.isIsRegex()) {
                    hexEndPosition = fileLength * 3;
                } else {
                    hexEndPosition = hexStartPosition + findHexLen;
                }
                found = findNextBytesRange(sourceFile, findReplaceString, 0, hexEndPosition, maxBytesLen, findHexLen);
            }
            if (found == null && findReplaceString.getError() != null) {
                findReplaceFile.setError(findReplaceString.getError());
                return null;
            }
            findReplaceFile.setFileRange(found);
            return found;
        } catch (Exception e) {
            findReplaceFile.setError(e.toString());
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static LongIndex findNextBytesRange(File file, FindReplaceString findReplaceString,
            long hexStartPosition, long hexEndPosition, long maxBytesLen, int findHexLen) {
        if (file == null || findReplaceString == null || hexStartPosition >= hexEndPosition) {
            return null;
        }
        findReplaceString.setError(null);
        findReplaceString.setStringRange(null);
        try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            if (hexStartPosition > 0) {
                inputStream.skip(hexStartPosition / 3);
            }
            int bufSize = (int) Math.min(maxBytesLen, hexStartPosition / 3);
            byte[] bufBytes = new byte[bufSize];
            IndexRange range;
            String bufHex, crossString = "";
            long hexStartIndex = hexStartPosition;
            int bufBytesLen, crossFrom, hexLen, crossLen = 0;
            LongIndex found = null;
            while ((bufBytesLen = inputStream.read(bufBytes)) > 0) {
                if (bufBytesLen < maxBytesLen) {
                    bufBytes = ByteTools.subBytes(bufBytes, 0, bufBytesLen);
                }
                bufHex = ByteTools.bytesToHexFormat(bufBytes);
                hexLen = bufHex.length();
                int extra = (int) (hexStartIndex + hexLen - hexEndPosition);
                if (extra > 0) {
                    bufHex = bufHex.substring(0, hexLen - extra);
                    hexLen = bufHex.length();
                }
                findReplaceString.setInputString(crossString.concat(bufHex)).setAnchor(0).run();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    found = new LongIndex();
                    found.setStart(hexStartIndex + range.getStart() - crossLen);
                    found.setEnd(found.getStart() + range.getLength());
                    break;
                }
                if (extra >= 0) {
                    break;
                }
                crossFrom = Math.max(3, hexLen - findHexLen + 3);
                crossString = bufHex.substring(crossFrom, hexLen);
                crossLen = crossString.length();
                hexStartIndex += hexLen;
            }
            return found;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceString.setError(e.toString());
            return null;
        }

    }

    public static boolean findNextBytes(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        try {
            LongIndex found = findNextBytesRange(sourceInfo, findReplaceFile);
            if (found == null) {
                return findReplaceFile != null && findReplaceFile.getError() == null;
            }
            String pageText = sourceInfo.locateRange(found);
            IndexRange stringRange = bytesRange(findReplaceFile, pageText);
            if (stringRange == null) {
                findReplaceFile.setError(message("InvalidData"));
                return false;
            }
            findReplaceFile.setFileRange(found);
            findReplaceFile.setStringRange(stringRange);
            findReplaceFile.setOuputString(pageText);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceFile.setError(e.toString());
            return false;
        }
    }

    public static LongIndex findPreviousBytesRange(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        if (sourceInfo == null || sourceInfo.getFile() == null || findReplaceFile == null
                || findReplaceFile.getFileFindString() == null || findReplaceFile.getFileFindString().isEmpty()) {
            if (findReplaceFile != null) {
                findReplaceFile.setError(message("InvalidParameters"));
            }
            return null;
        }
        try {
            sourceInfo.setFindReplace(findReplaceFile);
            findReplaceFile.setFileInfo(sourceInfo);
            File sourceFile = sourceInfo.getFile();
            long fileLength = sourceFile.length();
            long hexEndPosition = (findReplaceFile.getPosition() * 3) / 3;
            long bytesEndPosition = hexEndPosition / 3;
            if (bytesEndPosition <= 0 || bytesEndPosition > fileLength) {
                if (findReplaceFile.isWrap()) {
                    bytesEndPosition = fileLength;
                    hexEndPosition = bytesEndPosition * 3;
                } else {
//                    findReplaceFile.setError(message("InvalidParameters"));
                    return null;
                }
            }
            int maxBytesLen = FileTools.bufSize(sourceFile, 48);
            String findString = findReplaceFile.getFileFindString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setAnchor(0).setWrap(false);
            // findString should have been in hex format
            int findHexLen = findReplaceFile.isIsRegex() ? maxBytesLen * 3 : findString.length();
            LongIndex found = findPreviousBytesRange(sourceFile, findReplaceString, 0, hexEndPosition, maxBytesLen, findHexLen);
            if (found == null && findReplaceString.getError() != null) {
                findReplaceFile.setError(findReplaceString.getError());
                return null;
            }
            if (found == null && findReplaceFile.isWrap() && hexEndPosition > 0 && hexEndPosition <= fileLength * 3) {
                long hexStartPosition;
                if (findReplaceFile.isIsRegex()) {
                    hexStartPosition = 0;
                } else {
                    hexStartPosition = Math.max(0, hexEndPosition - findHexLen);
                }
                found = findPreviousBytesRange(sourceFile, findReplaceString, hexStartPosition, fileLength * 3, maxBytesLen, findHexLen);
            }
            if (found == null && findReplaceString.getError() != null) {
                findReplaceFile.setError(findReplaceString.getError());
                return null;
            }
            findReplaceFile.setFileRange(found);
            return found;
        } catch (Exception e) {
            findReplaceFile.setError(e.toString());
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static LongIndex findPreviousBytesRange(File file, FindReplaceString findReplaceString,
            long hexStartPosition, long hexEndPosition, long maxBytesLen, int findHexLen) {
        if (file == null || findReplaceString == null || hexStartPosition >= hexEndPosition) {
            return null;
        }
        findReplaceString.setError(null);
        findReplaceString.setStringRange(null);
        try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            if (hexStartPosition > 0) {
                inputStream.skip(hexStartPosition / 3);
            }
            int bufSize = (int) Math.min(maxBytesLen, hexStartPosition / 3);
            byte[] bufBytes = new byte[bufSize];
            IndexRange range;
            String bufHex, crossString = "", checkString;
            long hexStartIndex = hexStartPosition;
            int bufBytesLen, crossFrom, hexLen, crossLen = 0, checkLen;
            LongIndex found = null;
            while ((bufBytesLen = inputStream.read(bufBytes)) > 0) {
                if (bufBytesLen < maxBytesLen) {
                    bufBytes = ByteTools.subBytes(bufBytes, 0, bufBytesLen);
                }
                bufHex = ByteTools.bytesToHexFormat(bufBytes);
                hexLen = bufHex.length();
                int extra = (int) (hexStartIndex + hexLen - hexEndPosition);
                if (extra > 0) {
                    bufHex = bufHex.substring(0, hexLen - extra);
                    hexLen = bufHex.length();
                }
                checkString = crossString.concat(bufHex);
                checkLen = checkString.length();
                findReplaceString.setInputString(checkString).setAnchor(checkLen).run();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    found = new LongIndex();
                    found.setStart(hexStartIndex + range.getStart() - crossLen);
                    found.setEnd(found.getStart() + range.getLength());
                    if (range.getEnd() == checkString.length()) {
                        crossString = "";
                    } else {
                        crossFrom = Math.max(3, hexLen - findHexLen + 3);
                        crossFrom = Math.max(range.getStart() - crossString.length() + 3, crossFrom);
                        crossString = bufHex.substring(crossFrom, hexLen);
                    }
                } else {
                    crossFrom = Math.max(3, hexLen - findHexLen + 3);
                    crossString = bufHex.substring(crossFrom, hexLen);
                }
                if (extra >= 0) {
                    break;
                }
                hexStartIndex += hexLen;
            }
            return found;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceString.setError(e.toString());
            return null;
        }
    }

    public static boolean findPreviousBytes(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        try {
            LongIndex found = findPreviousBytesRange(sourceInfo, findReplaceFile);
            if (found == null) {
                return findReplaceFile != null && findReplaceFile.getError() == null;
            }
            String pageText = sourceInfo.locateRange(found);
            IndexRange stringRange = bytesRange(findReplaceFile, pageText);
            if (stringRange == null) {
                findReplaceFile.setError(message("InvalidData"));
                return false;
            }
            findReplaceFile.setFileRange(found);
            findReplaceFile.setStringRange(stringRange);
            findReplaceFile.setOuputString(pageText);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceFile.setError(e.toString());
            return false;
        }
    }

    public static boolean replaceFirstBytes(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        try {
            if (findReplaceFile == null) {
                return false;
            }
            findReplaceFile.setOperation(FindReplaceString.Operation.FindNext);
            findNextBytes(sourceInfo, findReplaceFile);
            IndexRange stringRange = findReplaceFile.getStringRange();
            findReplaceFile.setOperation(FindReplaceString.Operation.ReplaceFirst);
            if (stringRange == null) {
                return false;
            }
            String pageText = findReplaceFile.getOutputString();
            String replaceString = findReplaceFile.getReplaceString();
            String output = pageText.substring(0, stringRange.getStart()).concat(replaceString);
            if (stringRange.getEnd() < pageText.length()) {
                output += pageText.substring(stringRange.getEnd(), pageText.length());
            }
            findReplaceFile.setOuputString(output);
            return true;
        } catch (Exception e) {
            findReplaceFile.setError(e.toString());
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean replaceAllBytes(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        if (sourceInfo == null || sourceInfo.getFile() == null
                || findReplaceFile.getFileFindString() == null || findReplaceFile.getFileFindString().isEmpty()) {
            if (findReplaceFile != null) {
                findReplaceFile.setError(message("InvalidParameters"));
            }
            return false;
        }
        int total = 0;
        findReplaceFile.setError(null);
        findReplaceFile.setFileRange(null);
        findReplaceFile.setStringRange(null);
        sourceInfo.setFindReplace(findReplaceFile);
        findReplaceFile.setFileInfo(sourceInfo);
        File sourceFile = sourceInfo.getFile();
        File tmpFile = TmpFileTools.getTempFile();
        try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
                 BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
            int pageSize = FileTools.bufSize(sourceFile, 48);
            String findString = findReplaceFile.getFileFindString();
            String replaceString = findReplaceFile.getFileReplaceString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setReplaceString(replaceString)
                    .setAnchor(0).setWrap(false);
            int findLen = findReplaceFile.isIsRegex() ? pageSize * 3 : findString.length();
            byte[] pageBytes = new byte[pageSize];
            IndexRange range;
            String pageHex, crossString = "", checkedString, replacedString;
            int pageLen, crossFrom, lastReplacedLength;
            while ((pageLen = inputStream.read(pageBytes)) > 0) {
                if (pageLen < pageSize) {
                    pageBytes = ByteTools.subBytes(pageBytes, 0, pageLen);
                }
                pageHex = ByteTools.bytesToHexFormat(pageBytes);
                pageLen = pageHex.length();
                checkedString = crossString + pageHex;
                findReplaceString.setInputString(checkedString).setAnchor(0).run();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    replacedString = findReplaceString.getOutputString();
                    lastReplacedLength = findReplaceString.getLastReplacedLength();
                    byte[] replacedBytes = ByteTools.hexFormatToBytes(replacedString.substring(0, lastReplacedLength));
                    outputStream.write(replacedBytes);
                    crossString = replacedString.substring(lastReplacedLength, replacedString.length());
                    total += findReplaceString.getCount();
                    break;
                } else {
                    if (!crossString.isEmpty()) {
                        byte[] crossBytes = ByteTools.hexFormatToBytes(crossString);
                        outputStream.write(crossBytes);
                    }
                    crossFrom = Math.max(3, pageLen - findLen + 3);
                    crossString = pageHex.substring(crossFrom, pageLen);
                    byte[] passBytes = ByteTools.hexFormatToBytes(pageHex.substring(0, crossFrom));
                    outputStream.write(passBytes);
                }
            }
            if (!crossString.isEmpty()) {
                byte[] crossBytes = ByteTools.hexFormatToBytes(crossString);
                outputStream.write(crossBytes);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceFile.setError(e.toString());
            return false;
        }
        findReplaceFile.setCount(total);
        if (total > 0 && tmpFile != null && tmpFile.exists()) {
            findReplaceFile.backup(sourceFile);
            FileTools.rename(tmpFile, sourceFile);
        }
        return true;
    }

    public static IndexRange bytesRange(FindReplaceFile findReplace, String string) {
        if (findReplace == null) {
            return null;
        }
        IndexRange range = null;
        try {
            LongIndex fileRange = findReplace.getFileRange();
            if (fileRange != null) {
                MyBoxLog.console(fileRange.getStart() + " " + fileRange.getEnd());
            }
            FileEditInformation fileInfo = findReplace.getFileInfo();
            if (string != null && fileRange != null && fileInfo != null) {
                long pageSize = fileInfo.getPageSize();
                long bytesStart = fileRange.getStart() / 3;
                if (bytesStart <= fileInfo.getCurrentPageObjectStart()) {
                    int pageStart = (int) (bytesStart % pageSize) * 3;
                    int pageEnd = (int) (pageStart + fileRange.getLength());
                    range = new IndexRange(pageStart, pageEnd);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        findReplace.setStringRange(range);
        return range;
    }

}
