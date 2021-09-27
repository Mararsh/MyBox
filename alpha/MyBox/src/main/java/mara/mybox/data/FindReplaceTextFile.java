package mara.mybox.data;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import javafx.scene.control.IndexRange;
import mara.mybox.data.FindReplaceString.Operation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TmpFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-11-9
 * @License Apache License Version 2.0
 */
public class FindReplaceTextFile {

    public static boolean countText(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        if (sourceInfo == null || sourceInfo.getFile() == null
                || sourceInfo.getCharset() == null || sourceInfo.getLineBreakValue() == null
                || findReplaceFile == null || findReplaceFile.getFileFindString() == null || findReplaceFile.getFileFindString().isEmpty()) {
            if (findReplaceFile != null) {
                findReplaceFile.setError(message("InvalidParameters"));
            }
            return false;
        }
        sourceInfo.setFindReplace(findReplaceFile);
        findReplaceFile.setFileInfo(sourceInfo);
        int count = 0;
        File sourceFile = sourceInfo.getFile();
        int maxLen = FileTools.bufSize(sourceFile, 16);
//            MyBoxLog.debug(availableMem + " " + sourceFile.length() + " " + pageSize);
        String findString = findReplaceFile.getFileFindString();
        FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                .setFindString(findString).setAnchor(0).setWrap(false);
//            MyBoxLog.debug("findString.length()：" + findString.length());
        int findLen = findReplaceFile.isIsRegex() ? maxLen : findString.length();
        int crossFrom, textLen, crossLen = 0;
        StringBuilder s = new StringBuilder();
        String text, checkedString, crossString = "";
        boolean moreLine = false;
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceInfo.getCharset()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (moreLine) {
                    line = "\n" + line;
                }
                s.append(line);
                moreLine = true;
                textLen = s.length();
                if (textLen + crossLen < maxLen) {
                    continue;
                }
                text = s.toString();
                checkedString = crossString.concat(text);
//                MyBoxLog.debug(pagelen + " " + checkedString.length());
                findReplaceString.setInputString(checkedString).setAnchor(0).run();
                IndexRange lastRange = findReplaceString.getStringRange();
                if (lastRange != null) {
                    count += findReplaceString.getCount();
                    if (lastRange.getEnd() == checkedString.length()) {
                        crossString = "";
                    } else {
                        crossFrom = Math.max(lastRange.getStart() - crossString.length() + 1,
                                Math.max(1, textLen - findLen + 1));
                        crossString = text.substring(crossFrom, textLen);
                    }
                } else {
                    crossFrom = Math.max(1, textLen - findLen + 1);
                    crossString = text.substring(crossFrom, textLen);
                }
                crossLen = crossString.length();
                s = new StringBuilder();
            }
            text = s.toString();
            checkedString = crossString.concat(text);
            findReplaceString.setInputString(checkedString).run();
            IndexRange lastRange = findReplaceString.getStringRange();
            if (lastRange != null) {
                count += findReplaceString.getCount();
            }
            findReplaceFile.setCount(count);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean findNextText(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        try {
            LongIndex found = findNextTextRange(sourceInfo, findReplaceFile);
            if (found == null) {
                return findReplaceFile != null && findReplaceFile.getError() == null;
            }
            String pageText = sourceInfo.locateRange(found);
            IndexRange stringRange = stringRange(findReplaceFile, pageText);
            if (stringRange == null) {
                findReplaceFile.setError(message("InvalidData"));
                return false;
            }
//            MyBoxLog.debug("stringRange.getStart() :" + stringRange.getStart() + " stringRange.getEnd():" + stringRange.getEnd());
            findReplaceFile.setFileRange(found);
            findReplaceFile.setStringRange(stringRange);
            findReplaceFile.setOuputString(pageText);
            return true;
        } catch (Exception e) {
            findReplaceFile.setError(e.toString());
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static LongIndex findNextTextRange(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        if (sourceInfo == null || sourceInfo.getFile() == null
                || sourceInfo.getCharset() == null || sourceInfo.getLineBreakValue() == null
                || findReplaceFile == null || findReplaceFile.getFileFindString() == null
                || findReplaceFile.getFileFindString().isEmpty()) {
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
            Charset charset = sourceInfo.getCharset();
            long fileLength = sourceFile.length();
            long startPosition = findReplaceFile.getPosition();
            if (startPosition < 0 || startPosition >= fileLength) {
                if (findReplaceFile.isWrap()) {
                    startPosition = 0;
                } else {
//                    findReplaceFile.setError(message("InvalidParameters"));
                    return null;
                }
            }
            int maxLen = FileTools.bufSize(sourceFile, 16);
            String findString = findReplaceFile.getFileFindString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setAnchor(0).setWrap(false).setFindString(findString);
            int findLen = findReplaceFile.isIsRegex() ? maxLen : findString.length();
            LongIndex found = findNextTextRange(sourceFile, charset, findReplaceString, startPosition, fileLength, maxLen, findLen);
            if (found == null && findReplaceString.getError() != null) {
                findReplaceFile.setError(findReplaceString.getError());
                return null;
            }
            if (found == null && findReplaceFile.isWrap() && startPosition > 0 && startPosition < fileLength) {
                long endPosition;
                if (findReplaceFile.isIsRegex()) {
                    endPosition = fileLength;
                } else {
                    endPosition = startPosition + findLen;
                }
                found = findNextTextRange(sourceFile, charset, findReplaceString, 0, endPosition, maxLen, findLen);
            }
            if (found == null && findReplaceString.getError() != null) {
                findReplaceFile.setError(findReplaceString.getError());
                return null;
            }
            findReplaceFile.setFileRange(found);
            return found;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceFile.setError(e.toString());
            return null;
        }
    }

    public static LongIndex findNextTextRange(File file, Charset charset, FindReplaceString findReplaceString,
            long startPosition, long endPosition, long maxLen, int findLen) {
        if (file == null || charset == null || findReplaceString == null || startPosition >= endPosition) {
            return null;
        }
        findReplaceString.setError(null);
        findReplaceString.setStringRange(null);
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line;
            LongIndex found = null;
            int crossFrom, textLen, crossLen = 0;
            StringBuilder s = new StringBuilder();
            String text, crossString = "";
            boolean moreLine = false, skipped = startPosition <= 0;
            long textStart = startPosition;
            IndexRange range;
            while ((line = reader.readLine()) != null) {
                if (moreLine) {
                    line = "\n" + line;
                }
                s.append(line);
                moreLine = true;
                textLen = s.length();
                if (!skipped) {
                    if (textLen < startPosition) {
                        continue;
                    }
                    s.delete(0, (int) startPosition);
                    textLen = s.length();
                    skipped = true;
                }
                long cEnd = textStart + textLen;
                int extra = (int) (cEnd - endPosition);
                if (extra < 0 && textLen + crossLen < maxLen) {
                    continue;
                }
                if (extra > 0) {
                    s.delete((int) (textLen - extra), textLen);
                    textLen = s.length();
                }
                text = s.toString();
                findReplaceString.setInputString(crossString.concat(text)).setAnchor(0).run();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    found = new LongIndex();
                    found.setStart(textStart + range.getStart() - crossLen);
                    found.setEnd(found.getStart() + range.getLength());
                    break;
                } else {
                    crossFrom = Math.max(1, textLen - findLen + 1);
                    crossString = text.substring(crossFrom, textLen);
                    crossLen = crossString.length();
                }
                textStart += textLen;
                s = new StringBuilder();
                if (extra >= 0) {
                    break;
                }
            }
            if (found == null) {
                findReplaceString.setInputString(crossString.concat(s.toString())).setAnchor(0).run();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    found = new LongIndex();
                    found.setStart(textStart + range.getStart() - crossLen);
                    found.setEnd(found.getStart() + range.getLength());
                }
            }
            return found;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceString.setError(e.toString());
            return null;
        }
    }

    public static boolean findPreviousText(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        try {
            LongIndex found = findPreviousTextRange(sourceInfo, findReplaceFile);
            if (found == null) {
                return findReplaceFile != null && findReplaceFile.getError() == null;
            }
            String pageText = sourceInfo.locateRange(found);
            IndexRange stringRange = stringRange(findReplaceFile, pageText);
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

    public static LongIndex findPreviousTextRange(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        if (sourceInfo == null || sourceInfo.getFile() == null
                || sourceInfo.getCharset() == null || sourceInfo.getLineBreakValue() == null
                || findReplaceFile == null || findReplaceFile.getFileFindString() == null
                || findReplaceFile.getFileFindString().isEmpty()) {
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
            Charset charset = sourceInfo.getCharset();
            long fileLength = sourceFile.length();
            long endPostion = findReplaceFile.getPosition();
//            MyBoxLog.debug("getPosition:" + findReplaceFile.getPosition() + "   findEnd:" + findEnd + "   fileLength:" + fileLength);
            if (endPostion <= 0 || endPostion > fileLength) {
                if (findReplaceFile.isWrap()) {
                    endPostion = fileLength;
                } else {
//                    findReplaceFile.setError(message("InvalidParameters"));
                    return null;
                }
            }
//            MyBoxLog.debug("getPosition:" + findReplaceFile.getPosition() + "   findEnd:" + findEnd + "   fileLength:" + fileLength);
            int maxLen = FileTools.bufSize(sourceFile, 16);
//            MyBoxLog.debug(pageSize + " " + sourceFile.length() + " " + availableMem);
            String findString = findReplaceFile.getFileFindString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setAnchor(0).setWrap(false);
            int findLen = findReplaceFile.isIsRegex() ? maxLen : findString.length();
            LongIndex found = findPreviousTextRange(sourceFile, charset, findReplaceString, 0, endPostion, maxLen, findLen);
//            MyBoxLog.debug("(found != null):" + (found != null) + "   findReplaceString.getError():" + findReplaceString.getError());
            if (found == null && findReplaceString.getError() != null) {
                findReplaceFile.setError(findReplaceString.getError());
                return null;
            }
            if (found == null && findReplaceFile.isWrap() && endPostion > 0 && endPostion <= fileLength) {
                long startPosition;
                if (findReplaceFile.isIsRegex()) {
                    startPosition = 0;
                } else {
                    startPosition = Math.max(0, endPostion - findLen);
                }
//                MyBoxLog.debug("pageStart:" + pageStart + "   findEnd:" + findEnd + "   maxLen:" + maxLen + "   findLen:" + findLen);
                found = findPreviousTextRange(sourceFile, charset, findReplaceString, startPosition, fileLength, maxLen, findLen);
                if (found == null && findReplaceString.getError() != null) {
                    findReplaceFile.setError(findReplaceString.getError());
                    return null;
                }
            }
            findReplaceFile.setFileRange(found);
            return found;
        } catch (Exception e) {
            findReplaceFile.setError(e.toString());
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static LongIndex findPreviousTextRange(File file, Charset charset, FindReplaceString findReplaceString,
            long startPosition, long endPosition, long maxLen, int findLen) {
        if (file == null || charset == null || findReplaceString == null) {
            return null;
        }
        findReplaceString.setError(null);
        findReplaceString.setStringRange(null);
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line;
            LongIndex found = null;
            int crossFrom, textLen, crossLen = 0, checkLen;
            StringBuilder s = new StringBuilder();
            String text, crossString = "", checkString;
            boolean moreLine = false, skipped = startPosition <= 0;
            long textStart = startPosition;
            IndexRange range;
            while ((line = reader.readLine()) != null) {
                if (moreLine) {
                    line = "\n" + line;
                }
                s.append(line);
                moreLine = true;
                textLen = s.length();
                if (!skipped) {
                    if (textLen < startPosition) {
                        continue;
                    }
                    s.delete(0, (int) startPosition);
                    textLen = s.length();
                    skipped = true;
                }
                long totalLen = textStart + textLen;
                int extra = (int) (totalLen - endPosition);
                if (extra < 0 && textLen + crossLen < maxLen) {
                    continue;
                }
                if (extra > 0) {
//                    MyBoxLog.debug("cEnd:" + cEnd + "   findEnd:" + findEnd + "   textLen:" + textLen + "   extra:" + extra);
//                    MyBoxLog.debug("s:>>>>>" + s.toString() + "<<<<<");
                    s.delete((int) (textLen - extra), textLen);
                    textLen = s.length();
//                    MyBoxLog.debug("s:>>>>>" + s.toString() + "<<<<<");
                }
                text = s.toString();
//                    MyBoxLog.debug(checkedString + "\n--------------------\n ");
                checkString = crossString.concat(text);
                checkLen = checkString.length();
//                MyBoxLog.debug("textLen:" + textLen + "    checkLen:" + checkLen);
                findReplaceString.setInputString(checkString).setAnchor(checkLen).run();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    found = new LongIndex();
//                        MyBoxLog.debug(range.getStart());
                    found.setStart(textStart + range.getStart() - crossLen);
                    found.setEnd(found.getStart() + range.getLength());
//                    MyBoxLog.debug("foundStart:" + found.getStart() + "   foundEnd:" + found.getEnd());
                    if (range.getEnd() == checkLen) {
                        crossString = "";
                    } else {
                        crossFrom = Math.max(1, textLen - findLen + 1);
//                            MyBoxLog.debug("crossFrom:" + crossFrom + " pageLen:" + pageLen + " findLen:" + findLen);
                        crossFrom = Math.max(range.getStart() - crossLen + 1, crossFrom);
//                            MyBoxLog.debug("range.getStart() :" + range.getStart() + " crossString.length():" + crossString.length() + " crossFrom:" + crossFrom);
                        crossString = text.substring(crossFrom, textLen);
                    }
                } else {
                    crossFrom = Math.max(1, textLen - findLen + 1);
                    crossString = text.substring(crossFrom, textLen);
                }
                crossLen = crossString.length();
//                MyBoxLog.debug("crossString:" + crossString + "   crossLen:" + crossLen);
                textStart += textLen;
                s = new StringBuilder();
                if (extra >= 0) {
                    break;
                }
            }
            if (found == null) {
                checkString = crossString.concat(s.toString());
                checkLen = checkString.length();
//                MyBoxLog.debug("checkedString:>>>>>" + checkedString + "<<<<<");
                findReplaceString.setInputString(checkString).setAnchor(checkLen).run();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    found = new LongIndex();
//                        MyBoxLog.debug(range.getStart());
                    found.setStart(textStart + range.getStart() - crossLen);
                    found.setEnd(found.getStart() + range.getLength());
//                    MyBoxLog.debug("foundStart:" + found.getStart() + "   foundEnd:" + found.getEnd());
                }
            }
//            MyBoxLog.debug("(found != null):" + (found != null));
            return found;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceString.setError(e.toString());
            return null;
        }
    }

    public static boolean replaceFirstText(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        try {
            if (findReplaceFile == null) {
                return false;
            }
            findReplaceFile.setOperation(Operation.FindNext);
            findNextText(sourceInfo, findReplaceFile);
            IndexRange stringRange = findReplaceFile.getStringRange();
            findReplaceFile.setOperation(Operation.ReplaceFirst);
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

    public static boolean replaceAllText(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        if (sourceInfo == null || sourceInfo.getFile() == null || findReplaceFile == null
                || sourceInfo.getCharset() == null || sourceInfo.getLineBreakValue() == null
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
        Charset charset = sourceInfo.getCharset();
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceInfo.getCharset()));
                 BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile));
                 OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
            sourceInfo.setFindReplace(findReplaceFile);
            findReplaceFile.setFileInfo(sourceInfo);
            String findString = findReplaceFile.getFileFindString();
            String replaceString = findReplaceFile.getFileReplaceString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setReplaceString(replaceString)
                    .setAnchor(0).setWrap(false);
            int maxLen = FileTools.bufSize(sourceFile, 16);
            int findLen = findReplaceFile.isIsRegex() ? maxLen : findString.length();
            int crossFrom, textLen, crossLen = 0, lastReplacedLength;
            StringBuilder s = new StringBuilder();
            boolean moreLine = false;
            String line, replacedString, text, crossString = "";
            IndexRange range;
            while ((line = reader.readLine()) != null) {
                if (moreLine) {
                    line = "\n" + line;
                }
                s.append(line);
                moreLine = true;
                textLen = s.length();
                if (textLen + crossLen < maxLen) {
                    continue;
                }
                text = s.toString();
                findReplaceString.setInputString(crossString.concat(text)).setAnchor(0).run();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    replacedString = findReplaceString.getOutputString();
                    lastReplacedLength = findReplaceString.getLastReplacedLength();
                    writer.write(replacedString.substring(0, lastReplacedLength));
                    crossString = replacedString.substring(lastReplacedLength, replacedString.length());
                    total += findReplaceString.getCount();
                    break;
                } else {
                    if (!crossString.isEmpty()) {
                        writer.write(crossString);
                    }
                    crossFrom = Math.max(1, textLen - findLen + 1);
                    crossString = text.substring(crossFrom, textLen);
                    writer.write(text.substring(0, crossFrom));
                }
                crossLen = crossString.length();
                s = new StringBuilder();
            }
            findReplaceString.setInputString(crossString.concat(s.toString())).setAnchor(0).run();
            writer.write(findReplaceString.getOutputString());
            total += findReplaceString.getCount();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceFile.setError(e.toString());
            return false;
        }
        findReplaceFile.setCount(total);
        if (total > 0 && tmpFile != null && tmpFile.exists()) {
            findReplaceFile.backup(sourceFile);
            return FileTools.rename(tmpFile, sourceFile);
        }
        return true;
    }

    public static IndexRange stringRange(FindReplaceFile findReplace, String pageText) {
        if (findReplace == null) {
            return null;
        }
        IndexRange range = null;
        try {
            LongIndex fileRange = findReplace.getFileRange();
            FileEditInformation fileInfo = findReplace.getFileInfo();
            if (pageText != null && fileRange != null && fileInfo != null) {
                long fileStart = fileRange.getStart();
                if (fileStart >= fileInfo.getCurrentPageObjectStart() && fileStart < fileInfo.getCurrentPageObjectEnd()) {
                    int pageStart = (int) (fileStart - fileInfo.getCurrentPageObjectStart());
                    int pageEnd = (int) (pageStart + fileRange.getLength());
                    range = new IndexRange(pageStart, pageEnd);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplace.setError(e.toString());
        }
        findReplace.setStringRange(range);
        return range;
    }

    public static LongIndex fileRange(FindReplaceFile findReplace, String string) {
        if (findReplace == null) {
            return null;
        }
        LongIndex fileRange = null;
        IndexRange stringRange = findReplace.getStringRange();
        if (string != null && stringRange != null && findReplace.getFileInfo() != null) {
            int start = stringRange.getStart();
            FileEditInformation fileInfo = findReplace.getFileInfo();
//            MyBoxLog.debug("stringRange:" + stringRange.getStart() + " " + stringRange.getEnd() + " getCurrentPage:" + fileInfo.getCurrentPage());
            long fileStart = start;
            fileStart += fileInfo.getCurrentPageObjectStart() * findReplace.getUnit();
            fileRange = new LongIndex(fileStart, fileStart + stringRange.getLength());
//            MyBoxLog.debug("fileRange:" + fileRange.getStart() + " " + fileRange.getEnd());
        }
        findReplace.setFileRange(fileRange);
        return fileRange;
    }

}
