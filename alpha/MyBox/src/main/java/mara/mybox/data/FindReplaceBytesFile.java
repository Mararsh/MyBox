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
        try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile))) {
            int pageSize = FileTools.bufSize(sourceFile, 48);
            byte[] pageBytes = new byte[pageSize];
            String findString = findReplaceFile.getFileFindString();
            int findLen = findReplaceFile.isIsRegex() ? pageSize * 3 : findString.length();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setAnchor(0).setWrap(false);
            int bufLen, backFrom, hexLen;
            String pageHex, backString = "", checkedString;
            while ((bufLen = inputStream.read(pageBytes)) > 0) {
                if (bufLen < pageSize) {
                    pageBytes = ByteTools.subBytes(pageBytes, 0, bufLen);
                }
                pageHex = ByteTools.bytesToHexFormat(pageBytes);
                hexLen = pageHex.length();
                checkedString = backString + pageHex;
                findReplaceString.setInputString(checkedString).run();
                IndexRange lastRange = findReplaceString.getStringRange();
                if (lastRange != null) {
                    count += findReplaceString.getCount();
                    if (lastRange.getEnd() == checkedString.length()) {
                        backString = "";
                    } else {
                        backFrom = Math.max(3, hexLen - findLen + 3);
                        backFrom = Math.max(lastRange.getStart() - backString.length() + 3, backFrom);
                        backString = pageHex.substring(backFrom, hexLen);
                    }
                } else {
                    backFrom = Math.max(3, hexLen - findLen + 3);
                    backString = pageHex.substring(backFrom, hexLen);
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

    public static boolean findNextBytes(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        try {
            LongRange found = findNextBytesRange(sourceInfo, findReplaceFile);
            if (found == null) {
                return findReplaceFile != null && findReplaceFile.getError() == null;
            }
            String pageText = sourceInfo.readObjects(found.getStart() / 3, found.getLength() / 3);
            IndexRange stringRange = FindReplaceTextFile.pageRange(findReplaceFile);
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

    public static LongRange findNextBytesRange(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        if (sourceInfo == null || sourceInfo.getFile() == null || findReplaceFile == null
                || findReplaceFile.getFileFindString() == null || findReplaceFile.getFileFindString().isEmpty()) {
            if (findReplaceFile != null) {
                findReplaceFile.setError(message("InvalidParameters"));
            }
            return null;
        }
        findReplaceFile.setError(null);
        findReplaceFile.setFileRange(null);
        findReplaceFile.setStringRange(null);
        try {
            sourceInfo.setFindReplace(findReplaceFile);
            findReplaceFile.setFileInfo(sourceInfo);
            File sourceFile = sourceInfo.getFile();
            long fileLength = sourceFile.length();
            long hexStartPosition = (findReplaceFile.getPosition() / 3) * 3;
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
            LongRange found = findNextBytesRange(sourceFile, findReplaceString, hexStartPosition, fileLength * 3, maxBytesLen, findHexLen);
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

    public static LongRange findNextBytesRange(File file, FindReplaceString findReplaceString,
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
            int bufSize = (int) Math.min(maxBytesLen, (hexEndPosition - hexStartPosition) / 3);
            byte[] bufBytes = new byte[bufSize];
            IndexRange range;
            String bufHex, backString = "";
            long hexIndex = hexStartPosition;
            int bufLen, backFrom, hexLen, keepLen;
            LongRange found = null;
            while ((bufLen = inputStream.read(bufBytes)) > 0) {
                if (bufLen < bufSize) {
                    bufBytes = ByteTools.subBytes(bufBytes, 0, bufLen);
                }
                bufHex = ByteTools.bytesToHexFormat(bufBytes);
                hexLen = bufHex.length();
                hexIndex += hexLen;
                keepLen = (int) (hexIndex - hexEndPosition);
                if (keepLen > 0) {
                    bufHex = bufHex.substring(0, hexLen - keepLen);
                    hexLen = bufHex.length();
                }
                findReplaceString.setInputString(backString.concat(bufHex)).setAnchor(0).run();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    found = new LongRange();
                    found.setStart(hexIndex - hexLen + range.getStart() - backString.length());
                    found.setEnd(found.getStart() + range.getLength());
                    break;
                }
                if (keepLen >= 0) {
                    break;
                }
                backFrom = Math.max(3, hexLen - findHexLen + 3);
                if (backFrom < hexLen) {
                    backString = bufHex.substring(backFrom, hexLen);
                } else {
                    backString = "";
                }
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
            LongRange found = findPreviousBytesRange(sourceInfo, findReplaceFile);
            if (found == null) {
                return findReplaceFile != null && findReplaceFile.getError() == null;
            }
            String pageText = sourceInfo.readObjects(found.getStart() / 3, found.getLength() / 3);
            IndexRange stringRange = FindReplaceTextFile.pageRange(findReplaceFile);
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

    public static LongRange findPreviousBytesRange(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
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
            long hexEndPosition = (findReplaceFile.getPosition() / 3) * 3;
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
            LongRange found = findPreviousBytesRange(sourceFile, findReplaceString, 0, hexEndPosition, maxBytesLen, findHexLen);
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

    public static LongRange findPreviousBytesRange(File file, FindReplaceString findReplaceString,
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
            int bufSize = (int) Math.min(maxBytesLen, (hexEndPosition - hexStartPosition) / 3);
            byte[] bufBytes = new byte[bufSize];
            IndexRange range;
            String bufHex, backString = "", checkString;
            long hexIndex = hexStartPosition;
            int bufLen, backFrom, hexLen, checkLen, keepLen;
            LongRange found = null;
            while ((bufLen = inputStream.read(bufBytes)) > 0) {
                if (bufLen < maxBytesLen) {
                    bufBytes = ByteTools.subBytes(bufBytes, 0, bufLen);
                }
                bufHex = ByteTools.bytesToHexFormat(bufBytes);
                hexLen = bufHex.length();
                hexIndex += hexLen;
                keepLen = (int) (hexIndex - hexEndPosition);
                if (keepLen > 0) {
                    bufHex = bufHex.substring(0, hexLen - keepLen);
                    hexLen = bufHex.length();
                }
                checkString = backString.concat(bufHex);
                checkLen = checkString.length();
                findReplaceString.setInputString(checkString).setAnchor(checkLen).run();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    found = new LongRange();
                    found.setStart(hexIndex - hexLen + range.getStart() - backString.length());
                    found.setEnd(found.getStart() + range.getLength());
                    if (range.getEnd() == checkLen) {
                        backString = "";
                    } else {
                        backFrom = Math.max(range.getStart() - backString.length() + 3, Math.max(3, hexLen - findHexLen + 3));
                        if (backFrom < hexLen) {
                            backString = bufHex.substring(backFrom, hexLen);
                        } else {
                            backString = "";
                        }
                    }
                } else {
                    backFrom = Math.max(3, hexLen - findHexLen + 3);
                    if (backFrom < hexLen) {
                        backString = bufHex.substring(backFrom, hexLen);
                    } else {
                        backString = "";
                    }
                }
                if (keepLen >= 0) {
                    break;
                }
            }
            return found;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceString.setError(e.toString());
            return null;
        }
    }

    public static boolean replaceFirstBytes(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        try {
            if (findReplaceFile == null) {
                return false;
            }
            String replaceString = findReplaceFile.getReplaceString();
            if (replaceString == null) {
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
            if (stringRange.getStart() < 0 || stringRange.getStart() > stringRange.getEnd()
                    || stringRange.getEnd() > pageText.length()) {
                return false;
            }
            String output = pageText.substring(0, stringRange.getStart())
                    + replaceString
                    + pageText.substring(stringRange.getEnd(), pageText.length());
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
            String pageHex, backString = "", checkedString, replacedString;
            int pageLen, backFrom, lastReplacedLength;
            while ((pageLen = inputStream.read(pageBytes)) > 0) {
                if (pageLen < pageSize) {
                    pageBytes = ByteTools.subBytes(pageBytes, 0, pageLen);
                }
                pageHex = ByteTools.bytesToHexFormat(pageBytes);
                pageLen = pageHex.length();
                checkedString = backString + pageHex;
                findReplaceString.setInputString(checkedString).setAnchor(0).run();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    replacedString = findReplaceString.getOutputString();
                    lastReplacedLength = findReplaceString.getLastReplacedLength();
                    byte[] replacedBytes = ByteTools.hexFormatToBytes(replacedString.substring(0, lastReplacedLength));
                    outputStream.write(replacedBytes);
                    backString = replacedString.substring(lastReplacedLength, replacedString.length());
                    total += findReplaceString.getCount();
                    break;
                } else {
                    if (!backString.isEmpty()) {
                        byte[] backBytes = ByteTools.hexFormatToBytes(backString);
                        outputStream.write(backBytes);
                    }
                    backFrom = Math.max(3, pageLen - findLen + 3);
                    backString = pageHex.substring(backFrom, pageLen);
                    byte[] passBytes = ByteTools.hexFormatToBytes(pageHex.substring(0, backFrom));
                    outputStream.write(passBytes);
                }
            }
            if (!backString.isEmpty()) {
                byte[] backBytes = ByteTools.hexFormatToBytes(backString);
                outputStream.write(backBytes);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceFile.setError(e.toString());
            return false;
        }
        findReplaceFile.setCount(total);
        if (tmpFile != null && tmpFile.exists()) {
            if (total > 0) {
                findReplaceFile.backup(sourceFile);
                return FileTools.rename(tmpFile, sourceFile);
            } else {
                return true;
            }
        }
        return false;
    }

}
