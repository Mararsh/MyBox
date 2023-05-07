package mara.mybox.data;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.scene.control.IndexRange;
import mara.mybox.data.FindReplaceString.Operation;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2020-11-9
 * @License Apache License Version 2.0
 */
public class FindReplaceTextFile {

    public static boolean countText(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        if (sourceInfo == null || sourceInfo.getFile() == null
                || sourceInfo.getCharset() == null || sourceInfo.getLineBreakValue() == null
                || findReplaceFile == null || findReplaceFile.getFindString() == null || findReplaceFile.getFindString().isEmpty()) {
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
        String findString = findReplaceFile.getFindString();
        FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                .setFindString(findString).setAnchor(0).setWrap(false);
        int findLen = findReplaceFile.isIsRegex() ? maxLen : findString.length();
        int backFrom, textLen, backLen = 0;
        StringBuilder s = new StringBuilder();
        String text, checkString, backString = "";
        boolean moreLine = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceInfo.getCharset()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (moreLine) {
                    line = "\n" + line;
                } else {
                    moreLine = true;
                }
                s.append(line);
                textLen = s.length();
                if (textLen + backLen < maxLen) {    // read as more as possible
                    continue;
                }
                text = s.toString();
                checkString = backString.concat(text);
                findReplaceString.setInputString(checkString).setAnchor(0).handleString();
                IndexRange lastRange = findReplaceString.getStringRange();
                if (lastRange != null) {
                    count += findReplaceString.getCount();
                    if (lastRange.getEnd() == checkString.length()) {
                        backString = "";
                    } else {
                        backFrom = Math.max(lastRange.getStart() - backLen + 1,
                                Math.max(1, textLen - findLen + 1));
                        backString = text.substring(backFrom, textLen);
                    }
                } else {
                    backFrom = Math.max(1, textLen - findLen + 1);
                    backString = text.substring(backFrom, textLen);
                }
                backLen = backString.length();
                s = new StringBuilder();
            }
            if (!s.isEmpty()) {
                findReplaceString.setInputString(backString.concat(s.toString())).handleString();
                IndexRange lastRange = findReplaceString.getStringRange();
                if (lastRange != null) {
                    count += findReplaceString.getCount();
                }
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
            LongRange found = findNextTextRange(sourceInfo, findReplaceFile);
            if (found == null) {
                return findReplaceFile != null && findReplaceFile.getError() == null;
            }
            String pageText = sourceInfo.readObjects(found.getStart(), found.getLength());
            IndexRange stringRange = pageRange(findReplaceFile);
            if (stringRange == null) {
                findReplaceFile.setError(message("InvalidData"));
                return false;
            }
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

    public static LongRange findNextTextRange(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        if (sourceInfo == null || sourceInfo.getFile() == null
                || sourceInfo.getCharset() == null || sourceInfo.getLineBreakValue() == null
                || findReplaceFile == null || findReplaceFile.getFindString() == null
                || findReplaceFile.getFindString().isEmpty()) {
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
            String findString = findReplaceFile.getFindString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setAnchor(0).setWrap(false).setFindString(findString);
            int findLen = findReplaceFile.isIsRegex() ? maxLen : findString.length();
            LongRange found = findNextTextRange(sourceFile, charset, findReplaceString, startPosition, fileLength, findLen);
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
                found = findNextTextRange(sourceFile, charset, findReplaceString, 0, endPosition, findLen);
                if (found == null && findReplaceString.getError() != null) {
                    findReplaceFile.setError(findReplaceString.getError());
                    return null;
                }
            }
            findReplaceFile.setFileRange(found);
            return found;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceFile.setError(e.toString());
            return null;
        }
    }

    public static LongRange findNextTextRange(File file, Charset charset, FindReplaceString findReplaceString,
            long startPosition, long endPosition, int findLen) {
        if (file == null || charset == null || findReplaceString == null || startPosition >= endPosition) {
            return null;
        }
        findReplaceString.setError(null);
        findReplaceString.setStringRange(null);
        try (BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line;
            LongRange found = null;
            int charIndex = 0, textLen, lineLen, backLen = 0, keepLen;
            StringBuilder s = new StringBuilder();
            String text, backString = "";
            boolean moreLine = false, skipped = startPosition <= 0;
            long textStart = startPosition;
            IndexRange range;
            while ((line = reader.readLine()) != null) {
                if (moreLine) {
                    line = "\n" + line;
                } else {
                    moreLine = true;
                }
                lineLen = line.length();
                charIndex += lineLen;
                if (!skipped) {
                    keepLen = (int) (charIndex - startPosition);
                    if (keepLen < 0) {
                        continue;
                    }
                    s = new StringBuilder();
                    if (keepLen > 0) {                       // start from startPosition
                        s.append(line.substring((int) (lineLen - keepLen), lineLen));
                    }
                    textLen = s.length();
                    skipped = true;
                } else {
                    s.append(line);
                    textLen = s.length();
                }
                keepLen = (int) (charIndex - endPosition);
                if (keepLen < 0 && textLen + backLen < findLen) {  // find as early as possible
                    continue;
                }
                if (keepLen > 0) {                   // end at endPosition
                    s.delete((int) (textLen - keepLen), textLen);
                    textLen = s.length();
                }
                text = s.toString();
                findReplaceString.setInputString(backString.concat(text)).setAnchor(0).handleString();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    found = new LongRange();
                    found.setStart(textStart + range.getStart() - backLen);
                    found.setEnd(found.getStart() + range.getLength());
                    break;
                } else {                            // backword some string to match each possible
                    backString = text.substring(Math.max(1, textLen - findLen + 1), textLen);
                    backLen = backString.length();
                }
                textStart += textLen;
                s = new StringBuilder();
                if (keepLen >= 0) {
                    break;
                }
            }
            textLen = s.length();
            if (found == null && textLen > 0 && charIndex >= startPosition && charIndex < endPosition) {
                keepLen = (int) (charIndex - endPosition);
                if (keepLen > 0) {
                    s.delete((int) (textLen - keepLen), textLen);
                }
                findReplaceString.setInputString(backString.concat(s.toString())).setAnchor(0).handleString();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    found = new LongRange();
                    found.setStart(textStart + range.getStart() - backLen);
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
            LongRange found = findPreviousTextRange(sourceInfo, findReplaceFile);
            if (found == null) {
                return findReplaceFile != null && findReplaceFile.getError() == null;
            }
            String pageText = sourceInfo.readObjects(found.getStart(), found.getLength());
            IndexRange stringRange = pageRange(findReplaceFile);
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

    public static LongRange findPreviousTextRange(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        if (sourceInfo == null || sourceInfo.getFile() == null
                || sourceInfo.getCharset() == null || sourceInfo.getLineBreakValue() == null
                || findReplaceFile == null || findReplaceFile.getFindString() == null
                || findReplaceFile.getFindString().isEmpty()) {
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
            if (endPostion <= 0 || endPostion > fileLength) {
                if (findReplaceFile.isWrap()) {
                    endPostion = fileLength;
                } else {
//                    findReplaceFile.setError(message("InvalidParameters"));
                    return null;
                }
            }
            int maxLen = FileTools.bufSize(sourceFile, 16);
            String findString = findReplaceFile.getFindString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setAnchor(0).setWrap(false);
            int findLen = findReplaceFile.isIsRegex() ? maxLen : findString.length();
            LongRange found = findPreviousTextRange(sourceFile, charset, findReplaceString, 0, endPostion, maxLen, findLen);
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

    public static LongRange findPreviousTextRange(File file, Charset charset, FindReplaceString findReplaceString,
            long startPosition, long endPosition, long maxLen, int findLen) {
        if (file == null || charset == null || findReplaceString == null) {
            return null;
        }
        findReplaceString.setError(null);
        findReplaceString.setStringRange(null);
        try (BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line;
            LongRange found = null;
            int backFrom, textLen, backLen = 0, checkLen, lineLen, keepLen;
            StringBuilder s = new StringBuilder();
            String text, backString = "", checkString;
            boolean moreLine = false, skipped = startPosition <= 0;
            long charIndex = 0, textStart = startPosition;
            IndexRange range;
            while ((line = reader.readLine()) != null) {
                if (moreLine) {
                    line = "\n" + line;
                } else {
                    moreLine = true;
                }
                lineLen = line.length();
                charIndex += lineLen;
                if (!skipped) {                     // start from startPosition
                    keepLen = (int) (charIndex - startPosition);
                    if (keepLen < 0) {
                        continue;
                    }
                    s = new StringBuilder();
                    if (keepLen > 0) {
                        s.append(line.substring((int) (lineLen - keepLen), lineLen));
                    }
                    textLen = s.length();
                    skipped = true;
                } else {
                    s.append(line);
                    textLen = s.length();
                }
                keepLen = (int) (charIndex - endPosition);   // end at startPosition
                if (keepLen < 0 && textLen + backLen < maxLen) {  // read as more as possible
                    continue;
                }
                if (keepLen > 0) {
                    s.delete((int) (textLen - keepLen), textLen);
                    textLen = s.length();
                }
                text = s.toString();
                checkString = backString.concat(text);
                checkLen = checkString.length();
                findReplaceString.setInputString(checkString).setAnchor(checkLen).handleString();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    found = new LongRange();
                    found.setStart(textStart + range.getStart() - backLen);
                    found.setEnd(found.getStart() + range.getLength());
                    if (range.getEnd() == checkLen) {
                        backString = "";
                    } else {
                        backFrom = Math.max(1, textLen - findLen + 1);
                        backFrom = Math.max(range.getStart() - backLen + 1, backFrom);
                        backString = text.substring(backFrom, textLen);
                    }
                } else {
                    backFrom = Math.max(1, textLen - findLen + 1);
                    backString = text.substring(backFrom, textLen);
                }
                backLen = backString.length();
                textStart += textLen;
                s = new StringBuilder();
                if (keepLen >= 0) {
                    break;
                }
            }
            textLen = s.length();
            if (found == null && textLen > 0 && charIndex >= startPosition && charIndex < endPosition) {
                keepLen = (int) (charIndex - endPosition);
                if (keepLen > 0) {
                    s.delete((int) (textLen - keepLen), textLen);
                }
                checkString = backString.concat(s.toString());
                checkLen = checkString.length();
                findReplaceString.setInputString(checkString).setAnchor(checkLen).handleString();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    found = new LongRange();
                    found.setStart(textStart + range.getStart() - backLen);
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

    public static boolean replaceFirstText(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        try {
            if (findReplaceFile == null) {
                return false;
            }
            String replaceString = findReplaceFile.getReplaceString();
            if (replaceString == null) {
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

    public static boolean replaceAllText(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        if (sourceInfo == null || sourceInfo.getFile() == null || findReplaceFile == null
                || sourceInfo.getCharset() == null || sourceInfo.getLineBreakValue() == null
                || findReplaceFile.getFindString() == null || findReplaceFile.getFindString().isEmpty()) {
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
        File tmpFile = FileTmpTools.getTempFile();
//        MyBoxLog.debug(sourceFile + " --> " + tmpFile);
        Charset charset = sourceInfo.getCharset();
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile, charset));
                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile));
                OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
            String findString = findReplaceFile.getFindString();
            String replaceString = findReplaceFile.getReplaceString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setReplaceString(replaceString)
                    .setAnchor(0).setWrap(false);
            int maxLen = FileTools.bufSize(sourceFile, 16);
            int findLen = findReplaceFile.isIsRegex() ? maxLen : findString.length();
            int backFrom, textLen, backLen = 0, lastReplacedLength;
//            MyBoxLog.debug(" maxLen: " + maxLen + " findLen: " + findLen);
            StringBuilder s = new StringBuilder();
            boolean moreLine = false;
            String line, replacedString, text, backString = "", checkedString;
            IndexRange range;
            while ((line = reader.readLine()) != null) {
                if (moreLine) {
                    line = "\n" + line;
                } else {
                    moreLine = true;
                }
                s.append(line);
                textLen = s.length();
                if (textLen + backLen < maxLen) {   // read as more as possible
                    continue;
                }
                text = s.toString();
                checkedString = backString.concat(text);
                findReplaceString.setInputString(checkedString).setAnchor(0).handleString();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    replacedString = findReplaceString.getOutputString();
                    lastReplacedLength = findReplaceString.getLastReplacedLength();
                    writer.write(replacedString.substring(0, lastReplacedLength));
                    backString = replacedString.substring(lastReplacedLength, replacedString.length());
                    total += findReplaceString.getCount();
//                    MyBoxLog.debug(replacedString.length() + "   " + lastReplacedLength + "    " + backString.length() + "  " + findReplaceString.getCount());
                } else {
                    if (!backString.isEmpty()) {
                        writer.write(backString);
                    }
                    backFrom = Math.max(1, textLen - findLen + 1);
                    backString = text.substring(backFrom, textLen);
                    writer.write(text.substring(0, backFrom));
                }
                backLen = backString.length();
                s = new StringBuilder();
            }
            if (!s.isEmpty()) {
                //            MyBoxLog.debug(backString.length() + "   " + s.toString().length());
                findReplaceString.setInputString(backString.concat(s.toString())).setAnchor(0).handleString();
//            MyBoxLog.debug(findReplaceString.getOutputString().length());
                writer.write(findReplaceString.getOutputString());
                total += findReplaceString.getCount();
            }
            findReplaceFile.setCount(total);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceFile.setError(e.toString());
            return false;
        }
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

    public static IndexRange pageRange(FindReplaceFile findReplace) {
        if (findReplace == null) {
            return null;
        }
        IndexRange pageRange = null;
        LongRange fileRange = findReplace.getFileRange();
        FileEditInformation fileInfo = findReplace.getFileInfo();
        if (fileRange != null && fileInfo != null) {
            long pageStart = fileInfo.getCurrentPageObjectStart() * findReplace.getUnit();
            pageRange = new IndexRange((int) (fileRange.getStart() - pageStart), (int) (fileRange.getEnd() - pageStart));
        }
        findReplace.setStringRange(pageRange);
        return pageRange;
    }

    public static LongRange fileRange(FindReplaceFile findReplace) {
        if (findReplace == null) {
            return null;
        }
        LongRange fileRange = null;
        IndexRange pageRange = findReplace.getStringRange();
        FileEditInformation fileInfo = findReplace.getFileInfo();
        if (pageRange != null && fileInfo != null) {
            long pageStart = fileInfo.getCurrentPageObjectStart() * findReplace.getUnit();
            fileRange = new LongRange(pageRange.getStart() + pageStart, pageRange.getEnd() + pageStart);
        }
        findReplace.setFileRange(fileRange);
        return fileRange;
    }

    public static boolean findAllText(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        if (sourceInfo == null || sourceInfo.getFile() == null
                || sourceInfo.getCharset() == null || sourceInfo.getLineBreakValue() == null
                || findReplaceFile == null || findReplaceFile.getFindString() == null
                || findReplaceFile.getFindString().isEmpty()) {
            if (findReplaceFile != null) {
                findReplaceFile.setError(message("InvalidParameters"));
            }
            return false;
        }
        findReplaceFile.setError(null);
        findReplaceFile.setFileRange(null);
        findReplaceFile.setStringRange(null);
        findReplaceFile.setCount(0);
        findReplaceFile.setMatches(null);
        findReplaceFile.setMatchesData(null);
        sourceInfo.setFindReplace(findReplaceFile);
        findReplaceFile.setFileInfo(sourceInfo);
        File sourceFile = sourceInfo.getFile();
        int maxLen = FileTools.bufSize(sourceFile, 16);
        String findString = findReplaceFile.getFindString();
        FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                .setAnchor(0).setWrap(false).setFindString(findString);
        int findLen = findReplaceFile.isIsRegex() ? maxLen : findString.length();
        int count = 0;
        File tmpFile = FileTmpTools.getTempFile();
        try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceInfo.getCharset()));
                CSVPrinter csvPrinter = CsvTools.csvPrinter(tmpFile)) {
            String line;
            int textLen, backLen = 0, backFrom;
            StringBuilder s = new StringBuilder();
            String text, backString = "", checkedString;
            boolean moreLine = false;
            long textStart = 0;
            IndexRange range;
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Start"), message("End"), message("String")));
            csvPrinter.printRecord(names);
            List<String> row = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (moreLine) {
                    line = "\n" + line;
                } else {
                    moreLine = true;
                }
                s.append(line);
                textLen = s.length();
                if (textLen + backLen < maxLen) {   // read as more as possible
                    continue;
                }
                text = s.toString();
                checkedString = backString.concat(text);
                findReplaceString.setInputString(checkedString)
                        .setAnchor(0)
                        .handleString();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    long offset = textStart - backLen;
                    List<FindReplaceMatch> sMatches = findReplaceString.getMatches();
                    if (sMatches != null && !sMatches.isEmpty()) {
                        for (FindReplaceMatch m : sMatches) {
                            row.clear();
                            row.add((offset + m.getStart() + 1) + "");
                            row.add((offset + m.getEnd()) + "");
                            row.add(m.getMatchedPrefix());
                            csvPrinter.printRecord(row);
                            count++;
                        }
                    }
                    backString = checkedString.length() == range.getEnd() ? ""
                            : checkedString.substring(range.getEnd());
                } else {
                    backFrom = Math.max(1, textLen - findLen + 1);
                    backString = text.substring(backFrom, textLen);
                }
                textStart += textLen;
                backLen = backString.length();
                s = new StringBuilder();
            }
            if (!s.isEmpty()) {
                findReplaceString.setInputString(backString.concat(s.toString()))
                        .setAnchor(0)
                        .handleString();
                range = findReplaceString.getStringRange();
                if (range != null) {
                    long offset = textStart - backLen;
                    List<FindReplaceMatch> sMatches = findReplaceString.getMatches();
                    if (sMatches != null && !sMatches.isEmpty()) {
                        for (FindReplaceMatch m : sMatches) {
                            row.clear();
                            row.add((offset + m.getStart() + 1) + "");
                            row.add((offset + m.getEnd()) + "");
                            row.add(m.getMatchedPrefix());
                            csvPrinter.printRecord(row);
                            count++;
                        }
                    }
                }
            }
            csvPrinter.flush();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceString.setError(e.toString());
            return false;
        }
        if (tmpFile == null || !tmpFile.exists()) {
            return false;
        }
        if (count == 0) {
            return true;
        }
        DataFileCSV matchesData = findReplaceFile.initMatchesData(sourceFile);
        File matchesFile = matchesData.getFile();
        if (!FileTools.rename(tmpFile, matchesFile)) {
            return false;
        }
        matchesData.setRowsNumber(count);
        findReplaceFile.setCount(count);
        findReplaceFile.setMatchesData(matchesData);
        return true;
    }

}
