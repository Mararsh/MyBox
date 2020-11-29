package mara.mybox.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import javafx.scene.control.IndexRange;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.TextTools.bomBytes;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-11-9
 * @License Apache License Version 2.0
 */
public class FindReplaceFile extends FindReplaceString {

    protected boolean multiplePages, pageReloaded;
    protected FileEditInformation fileInfo;
    protected String fileFindString, fileReplaceString;
    protected long position;
    protected LongIndex fileRange;  // location in whole file

    public FindReplaceFile() {
        multiplePages = false;
    }

    @Override
    public FindReplaceString reset() {
        super.reset();
        fileRange = null;
        pageReloaded = false;
        return this;
    }

    public FindReplaceString findReplaceString() {
        FindReplaceString findReplaceString = new FindReplaceString()
                .setOperation(operation)
                .setInputString(inputString)
                .setFindString(findString)
                .setAnchor(anchor)
                .setUnit(unit)
                .setReplaceString(replaceString)
                .setIsRegex(isRegex)
                .setCaseInsensitive(caseInsensitive)
                .setMultiline(multiline)
                .setDotAll(dotAll)
                .setWrap(wrap);
        return findReplaceString;
    }

    public boolean page() {
        reset();
        if (operation == null || fileInfo == null
                || findString == null || findString.isEmpty()) {
            return false;
        }
        fileInfo.setFindReplace(this);
//        MyBoxLog.debug("operation:" + operation + " isWhole:" + isWhole + " unit:" + unit
//                + " anchor:" + anchor + " position:" + position + " page:" + fileInfo.getCurrentPage());
        if (!multiplePages) {
            return run();
        }
        // try current page at first
        if (operation == Operation.FindNext || operation == Operation.ReplaceFirst
                || operation == Operation.FindPrevious) {
            FindReplaceString findReplaceString = findReplaceString().setWrap(false);
            findReplaceString.run();
            if (findReplaceString.getStringRange() != null) {
                stringRange = findReplaceString.getStringRange();
                lastMatch = findReplaceString.getLastMatch();
                outputString = findReplaceString.getOutputString();
                lastReplacedLength = findReplaceString.getLastReplacedLength();
//                MyBoxLog.debug("stringRange:" + stringRange.getStart() + " " + stringRange.getEnd());
                fileRange = fileRange(this, inputString);
//                MyBoxLog.debug("fileRange:" + fileRange.getStart() + " " + fileRange.getEnd());
                return true;
            }
        }
        return false;
    }

    public boolean file() {
        reset();
        if (operation == null || fileInfo == null
                || findString == null || findString.isEmpty()) {
            return false;
        }
        fileInfo.setFindReplace(this);
//        MyBoxLog.debug("operation:" + operation + " isWhole:" + isWhole + " unit:" + unit
//                + " anchor:" + anchor + " position:" + position + " page:" + fileInfo.getCurrentPage());
        if (!multiplePages) {
            return run();
        }

//        MyBoxLog.debug("findString.length()：" + findString.length());
//        MyBoxLog.debug(fileInfo.getEditType());
        fileFindString = findString;
        fileReplaceString = replaceString;
//        MyBoxLog.debug("findString.length()：" + findString.length());
        if (fileInfo.getEditType() != Edit_Type.Bytes) {
            fileFindString = findString.replaceAll("\n", fileInfo.getLineBreakValue());
            if (replaceString != null && !replaceString.isEmpty()) {
                fileReplaceString = replaceString.replaceAll("\n", fileInfo.getLineBreakValue());
            }
//            MyBoxLog.debug("fileFindString.length()：" + fileFindString.length());
            switch (operation) {
                case Count:
                    return countText(fileInfo, this);
                case FindNext:
                    return findNextText(fileInfo, this);
                case FindPrevious:
                    return findPreviousText(fileInfo, this);
                case ReplaceFirst:
                    return replaceFirstText(fileInfo, this);
                case ReplaceAll:
                    return replaceAllText(fileInfo, this);
                default:
                    break;
            }
        } else {
            switch (operation) {
                case Count:
                    return countBytes(fileInfo, this);
                case FindNext:
                    return findNextBytes(fileInfo, this);
                case FindPrevious:
                    return findPreviousBytes(fileInfo, this);
                case ReplaceFirst:
                    return replaceFirstBytes(fileInfo, this);
                case ReplaceAll:
                    return replaceAllBytes(fileInfo, this);
                default:
                    break;
            }
        }
        return true;
    }

    /*
        static methods
     */
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
        try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
                 InputStreamReader reader = new InputStreamReader(inputStream, sourceInfo.getCharset())) {
            if (sourceInfo.isWithBom()) {
                byte[] bytes = bomBytes(sourceInfo.getCharset().name());
                inputStream.skip(bytes.length);
//                MyBoxLog.debug(bytes.length);
            }
            int pageSize = FileTools.bufSize(sourceFile);
//            MyBoxLog.debug(availableMem + " " + sourceFile.length() + " " + pageSize);
            char[] textBuf = new char[pageSize];
            char[] charBuf = new char[1];
            boolean crlf = sourceInfo.getLineBreak().equals(Line_Break.CRLF);
            String findString = findReplaceFile.getFileFindString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setAnchor(0).setWrap(false);
//            MyBoxLog.debug("findString.length()：" + findString.length());
            int findLen = findReplaceFile.isRegex ? pageSize : findString.length();
            int crossFrom, textLen;
            String text, crossString = "", checkedString;
            while ((textLen = reader.read(textBuf)) > 0) {
                text = new String(textBuf, 0, textLen);
                if (crlf && text.endsWith("\r") && reader.read(charBuf) == 1) {
                    text += new String(charBuf, 0, 1);
                    textLen++;
                }
                checkedString = crossString + text;
//                MyBoxLog.debug(pagelen + " " + checkedString.length());
                findReplaceString.setInputString(checkedString).run();
                IndexRange lastRange = findReplaceString.getStringRange();
                if (lastRange != null) {
                    count += findReplaceString.getCount();
                    if (lastRange.getEnd() == checkedString.length()) {
                        crossString = "";
                    } else {
                        crossFrom = Math.max(1, textLen - findLen + 1);
                        crossFrom = Math.max(lastRange.getStart() - crossString.length() + 1, crossFrom);
                        crossString = text.substring(crossFrom, textLen);
                    }
                } else {
                    crossFrom = Math.max(1, textLen - findLen + 1);
                    crossString = text.substring(crossFrom, textLen);
                }
            }
            findReplaceFile.setCount(count);
            return true;
        } catch (Exception e) {
            findReplaceFile.setError(e.toString());
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static LongIndex findNextTextRange(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
//        MyBoxLog.debug(sourceInfo.getFile());
        if (sourceInfo == null || sourceInfo.getFile() == null
                || sourceInfo.getCharset() == null || sourceInfo.getLineBreakValue() == null
                || findReplaceFile == null || findReplaceFile.getFileFindString() == null
                || findReplaceFile.getFileFindString().isEmpty()) {
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
            long position = findReplaceFile.getPosition();
            if (position < 0 || position >= fileLength) {
                if (findReplaceFile.isWrap()) {
                    position = 0;
                } else {
                    findReplaceFile.setError(message("InvalidParameters"));
                    return null;
                }
            }
            int pageSize = FileTools.bufSize(sourceFile);
//            MyBoxLog.debug(pageSize + " " + sourceFile.length() + " " + availableMem);
//            String findString = findReplaceFile.getFileFindString().replaceAll("\n", sourceInfo.getLineBreakValue());
            String findString = findReplaceFile.getFileFindString();
//            MyBoxLog.debug("findString.length()：" + findString.length());
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setAnchor(0).setWrap(false);
            int findLen = findReplaceFile.isRegex ? pageSize : findString.length();
            LongIndex found = null;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
                     InputStreamReader reader = new InputStreamReader(inputStream, sourceInfo.getCharset())) {
                if (sourceInfo.isWithBom()) {
                    byte[] bytes = bomBytes(sourceInfo.getCharset().name());
                    inputStream.skip(bytes.length);
//                    MyBoxLog.debug(bytes.length);
                }
                if (position > 0) {
                    reader.skip(position);
                }
                int bufSize = (int) Math.min(pageSize, fileLength - position);
//                MyBoxLog.debug("bufSize:" + bufSize + " position:" + position);
                char[] textBuf = new char[bufSize];
                char[] charBuf = new char[1];
                boolean crlf = sourceInfo.getLineBreak().equals(Line_Break.CRLF);
//                MyBoxLog.debug(pageSize + " " + findReplaceFile.getPosition());
                IndexRange range;
                String text, crossString = "", checkedString;
                int textLen, crossFrom;
                long textStart = position;
//                MyBoxLog.debug("pageStart:" + textStart);
                while ((textLen = reader.read(textBuf)) > 0) {
                    text = new String(textBuf, 0, textLen);
                    if (crlf && text.endsWith("\r") && reader.read(charBuf) == 1) {
                        text += new String(charBuf, 0, 1);
                        textLen++;
                    }
                    checkedString = crossString + text;
//                    checkedString = checkedString.replaceAll(sourceInfo.getLineBreakValue(), sourceInfo.getLineBreakValue());
//                    MyBoxLog.debug("pageLen:" + pageLen + " checkedString.length:" + checkedString.length());
//                    MyBoxLog.debug(checkedString + "\n--------------------\n ");
                    findReplaceString.setInputString(checkedString).setAnchor(0).run();
                    range = findReplaceString.getStringRange();
                    if (range != null) {
                        found = new LongIndex();
//                        MyBoxLog.debug(range.getStart());
                        found.start = textStart + range.getStart() - crossString.length();
                        found.end = found.start + range.getLength();
//                        MyBoxLog.debug(found.start + " " + found.end);
                        break;
                    } else {
                        crossFrom = Math.max(1, textLen - findLen + 1);
                        crossString = text.substring(crossFrom, textLen);
//                        MyBoxLog.debug("crossFrom:" + crossFrom + " >>>" + crossString + "<<<");
                    }
                    textStart += textLen;
                }
            }
            if (found == null && findReplaceFile.isWrap() && position > 0 && position < fileLength) {
                try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
                         InputStreamReader reader = new InputStreamReader(inputStream, sourceInfo.getCharset())) {
                    if (sourceInfo.isWithBom()) {
                        byte[] bytes = bomBytes(sourceInfo.getCharset().name());
                        inputStream.skip(bytes.length);
                    }
                    long end;
                    if (findReplaceFile.isIsRegex()) {
                        end = fileLength - 1;
                    } else {
                        end = position + findLen - 1;
                    }
//                    MyBoxLog.debug(end + " " + findReplaceFile.getPosition());
                    int bufSize = (int) Math.min(pageSize, end);
//                    MyBoxLog.debug("bufSize:" + bufSize + " end:" + end);
                    char[] textBuf = new char[bufSize];
                    char[] charBuf = new char[1];
                    boolean crlf = sourceInfo.getLineBreak().equals(Line_Break.CRLF);
                    IndexRange range;
                    String text, crossString = "", checkedString;
                    long textStart = 0;
                    int textLen, crossFrom;
                    boolean reachEnd = false;
//                    MyBoxLog.debug("pageStart:" + pageStart + " end:" + end);
                    while ((textLen = reader.read(textBuf)) > 0) {
                        text = new String(textBuf, 0, textLen);
                        if (crlf && text.endsWith("\r") && reader.read(charBuf) == 1) {
                            text += new String(charBuf, 0, 1);
                            textLen++;
                        }
                        if (textStart + textLen >= end) {
                            text = text.substring(0, (int) (end - textStart));
                            textLen = text.length();
                            reachEnd = true;
//                            MyBoxLog.debug(pageStart + " " + pageLen + " " + end);
                        }
                        checkedString = crossString + text;
//                        MyBoxLog.debug("pageLen:" + pageLen + " checkedString.length:" + checkedString.length());
//                        MyBoxLog.debug(pageLen + " " + checkedString.length());
//                        MyBoxLog.debug(checkedString + "\n--------------------\n ");
                        findReplaceString.setInputString(checkedString).setAnchor(0).run();
                        range = findReplaceString.getStringRange();
                        if (range != null) {
                            found = new LongIndex();
                            found.start = textStart + range.getStart() - crossString.length();
                            found.end = found.start + range.getLength();
//                            MyBoxLog.debug(found.start + " " + found.end);
                            break;
                        }
                        if (reachEnd) {
                            break;
                        }
                        crossFrom = Math.max(1, textLen - findLen + 1);
                        crossString = text.substring(crossFrom, textLen);
//                        MyBoxLog.debug("crossFrom:" + crossFrom + " >>>" + crossString + "<<<");
                        textStart += textLen;
//                        MyBoxLog.debug(pageStart + " " + crossString.length());
                    }
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

    public static boolean findNextText(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        try {
            LongIndex found = findNextTextRange(sourceInfo, findReplaceFile);
            if (found == null) {
                return findReplaceFile != null && findReplaceFile.getError() == null;
            }
//            MyBoxLog.debug("(findReplaceFile.getFileRange() != null) :" + (findReplaceFile.getFileRange() != null));
//            MyBoxLog.debug("found.getStart() :" + found.getStart() + " found.getEnd():" + found.getEnd());
            long pageSize = sourceInfo.getPageSize();
            int pageNumber = (int) (found.getStart() / pageSize + 1);
            String pageText = sourceInfo.readPage(pageNumber);
            IndexRange stringRange = findReplaceFile.getStringRange();
            if (stringRange == null) {
                stringRange = stringRange(findReplaceFile, pageText);
            }
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

    public static LongIndex findPreviousTextRange(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
//        MyBoxLog.debug(sourceInfo.getFile());
        if (sourceInfo == null || sourceInfo.getFile() == null
                || sourceInfo.getCharset() == null || sourceInfo.getLineBreakValue() == null
                || findReplaceFile == null || findReplaceFile.getFileFindString() == null
                || findReplaceFile.getFileFindString().isEmpty()) {
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
            long position = findReplaceFile.getPosition();
//            MyBoxLog.debug("pageSize:" + pageSize + " position:" + position);
            if (position <= 0 || position > fileLength) {
                if (findReplaceFile.isWrap()) {
                    position = fileLength;
                } else {
                    findReplaceFile.setError(message("InvalidParameters"));
                    return null;
                }
            }
            int pageSize = FileTools.bufSize(sourceFile);
//            MyBoxLog.debug(pageSize + " " + sourceFile.length() + " " + availableMem);
            String findString = findReplaceFile.getFileFindString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setAnchor(0).setWrap(false);
            int findLen = findReplaceFile.isRegex ? pageSize : findString.length();
            LongIndex found = null;
            long foundStart = -1, foundEnd = -1;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
                     InputStreamReader reader = new InputStreamReader(inputStream, sourceInfo.getCharset())) {
                if (sourceInfo.isWithBom()) {
                    byte[] bytes = bomBytes(sourceInfo.getCharset().name());
                    inputStream.skip(bytes.length);
//                    MyBoxLog.debug(bytes.length);
                }
                int bufSize = (int) Math.min(pageSize, position);
                char[] textBuf = new char[bufSize];
                char[] charBuf = new char[1];
                boolean crlf = sourceInfo.getLineBreak().equals(Line_Break.CRLF);
//                MyBoxLog.debug("bufSize:" + bufSize + " getPosition:" + position);
                IndexRange range;
                String text, crossString = "", checkedString;
                long pageStart = 0;
                int textLen, crossFrom;
                boolean reachEnd = false;
//                MyBoxLog.debug(pageStart);
                while ((textLen = reader.read(textBuf)) > 0) {
                    text = new String(textBuf, 0, textLen);
                    if (crlf && text.endsWith("\r") && reader.read(charBuf) == 1) {
                        text += new String(charBuf, 0, 1);
                        textLen++;
                    }
                    if (pageStart + textLen >= position) {
                        text = text.substring(0, (int) (position - pageStart));
                        textLen = text.length();
                        reachEnd = true;
//                        MyBoxLog.debug("pageStart:" + pageStart + " pageLen:" + pageLen + " end:" + position);
                    }
                    checkedString = crossString + text;
//                    MyBoxLog.debug("pageLen:" + pageLen + " checkedString.length():" + checkedString.length());
//                    MyBoxLog.debug(checkedString + "\n--------------------\n ");
                    findReplaceString.setInputString(checkedString).setAnchor(checkedString.length()).run();
                    range = findReplaceString.getStringRange();
                    if (range != null) {
//                        MyBoxLog.debug(range.getStart());
                        foundStart = pageStart + range.getStart() - crossString.length();
                        foundEnd = foundStart + range.getLength();
//                        MyBoxLog.debug("foundStart:" + foundStart + " " + foundEnd + " crossString.length():" + crossString.length());
                        if (range.getEnd() == checkedString.length()) {
                            crossString = "";
                        } else {
                            crossFrom = Math.max(1, textLen - findLen + 1);
//                            MyBoxLog.debug("crossFrom:" + crossFrom + " pageLen:" + pageLen + " findLen:" + findLen);
                            crossFrom = Math.max(range.getStart() - crossString.length() + 1, crossFrom);
//                            MyBoxLog.debug("range.getStart() :" + range.getStart() + " crossString.length():" + crossString.length() + " crossFrom:" + crossFrom);
                            crossString = text.substring(crossFrom, textLen);
                        }
                    } else {
                        crossFrom = Math.max(1, textLen - findLen + 1);
                        crossString = text.substring(crossFrom, textLen);
                    }
                    if (reachEnd) {
                        break;
                    }
//                        MyBoxLog.debug(crossFrom + " " + crossString);
                    pageStart += textLen;
                }
            }
            if (foundStart >= 0) {
                found = new LongIndex(foundStart, foundEnd);
            } else if (findReplaceFile.isWrap() && position > 0 && position < fileLength) {
                try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
                         InputStreamReader reader = new InputStreamReader(inputStream, sourceInfo.getCharset())) {
                    if (sourceInfo.isWithBom()) {
                        byte[] bytes = bomBytes(sourceInfo.getCharset().name());
                        inputStream.skip(bytes.length);
                    }
                    long pageStart;
                    if (findReplaceFile.isIsRegex()) {
                        pageStart = 1;
                    } else {
                        pageStart = Math.max(1, position - findLen + 1);
                    }
                    inputStream.skip(pageStart);
                    int bufSize = (int) Math.min(pageSize, fileLength - pageStart);
//                    MyBoxLog.debug("bufSize:" + bufSize + " getPosition:" + pageStart);
                    char[] textBuf = new char[bufSize];
                    char[] charBuf = new char[1];
                    boolean crlf = sourceInfo.getLineBreak().equals(Line_Break.CRLF);
                    IndexRange range;
                    String text, crossString = "", checkedString;
                    int textLen, crossFrom;
//                    MyBoxLog.debug(pageStart);
                    while ((textLen = reader.read(textBuf)) > 0) {
                        text = new String(textBuf, 0, textLen);
                        if (crlf && text.endsWith("\r") && reader.read(charBuf) == 1) {
                            text += new String(charBuf, 0, 1);
                            textLen++;
                        }
                        checkedString = crossString + text;
//                        MyBoxLog.debug(pageLen + " " + checkedString.length());
//                        MyBoxLog.debug(checkedString + "\n--------------------\n ");
                        findReplaceString.setInputString(checkedString).setAnchor(checkedString.length()).run();
                        range = findReplaceString.getStringRange();
                        if (range != null) {
                            foundStart = pageStart + range.getStart() - crossString.length();
                            foundEnd = foundStart + range.getLength();
//                            MyBoxLog.debug("foundStart:" + foundStart + " " + foundEnd + " crossString.length():" + crossString.length());
                            if (range.getEnd() == checkedString.length()) {
                                crossString = "";
                            } else {
                                crossFrom = Math.max(1, textLen - findLen + 1);
//                                MyBoxLog.debug("crossFrom:" + crossFrom + " pageLen:" + pageLen + " findLen:" + findLen);
                                crossFrom = Math.max(range.getStart() - crossString.length() + 1, crossFrom);
//                                MyBoxLog.debug("range.getStart() :" + range.getStart() + " crossString.length():" + crossString.length() + " crossFrom:" + crossFrom);
                                crossString = text.substring(crossFrom, textLen);
                            }
                        } else {
                            crossFrom = Math.max(1, textLen - findLen + 1);
                            crossString = text.substring(crossFrom, textLen);
                        }
                        pageStart += textLen;
//                        MyBoxLog.debug(pageStart + " " + crossString.length());
                    }
                }
                if (foundStart >= 0) {
                    found = new LongIndex(foundStart, foundEnd);
//                    MyBoxLog.debug("found:" + foundStart + " " + foundEnd);
                }
            }
//            if (found != null) {
//                MyBoxLog.debug("found:" + found.getStart() + " " + found.getEnd());
//            }
            findReplaceFile.setFileRange(found);
            return found;
        } catch (Exception e) {
            findReplaceFile.setError(e.toString());
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static boolean findPreviousText(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        try {
            LongIndex found = findPreviousTextRange(sourceInfo, findReplaceFile);
            if (found == null) {
                return findReplaceFile != null && findReplaceFile.getError() == null;
            }
            long pageSize = sourceInfo.getPageSize();
            int pageNumber = (int) (found.getStart() / pageSize + 1);
            String pageText = sourceInfo.readPage(pageNumber);
            IndexRange stringRange = findReplaceFile.getStringRange();
            if (stringRange == null) {
                stringRange = stringRange(findReplaceFile, pageText);
            }
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

    public static boolean replaceFirstText(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        try {
            if (findReplaceFile == null) {
                return false;
            }
            findReplaceFile.setOperation(Operation.FindNext);
            boolean found = findNextText(sourceInfo, findReplaceFile);
            findReplaceFile.setOperation(Operation.ReplaceFirst);
            if (!found) {
                return false;
            }
            IndexRange stringRange = findReplaceFile.getStringRange();
            String pageText = findReplaceFile.getOutputString();
            String replaceString = findReplaceFile.getReplaceString();
            String output = pageText.substring(0, stringRange.getStart()) + replaceString;
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
        try {
            sourceInfo.setFindReplace(findReplaceFile);
            findReplaceFile.setFileInfo(sourceInfo);
            File sourceFile = sourceInfo.getFile();
//            MyBoxLog.debug(sourceFile);
            int pageSize = FileTools.bufSize(sourceFile);
//            MyBoxLog.debug(pageSize + " " + sourceFile.length() + " " + availableMem);
            String findString = findReplaceFile.getFileFindString();
            String replaceString = findReplaceFile.getFileReplaceString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setReplaceString(replaceString)
                    .setAnchor(0).setWrap(false);
            int findLen = findReplaceFile.isRegex ? pageSize : findString.length();
            File tmpFile = FileTools.getTempFile();
            Charset charset = sourceInfo.getCharset();
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
                     InputStreamReader reader = new InputStreamReader(inputStream, charset);
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile));
                     OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
                if (sourceInfo.isWithBom()) {
                    byte[] bytes = bomBytes(sourceInfo.getCharset().name());
                    inputStream.skip(bytes.length);
                }
                char[] textBuf = new char[pageSize];
                char[] charBuf = new char[1];
                boolean crlf = sourceInfo.getLineBreak().equals(Line_Break.CRLF);
//                MyBoxLog.debug("pageSize:" + pageSize);
                IndexRange range;
                String text, crossString = "", checkedString, replacedString;
                int textLen, crossFrom, lastReplacedLength;
                while ((textLen = reader.read(textBuf)) > 0) {
                    text = new String(textBuf, 0, textLen);
                    if (crlf && text.endsWith("\r") && reader.read(charBuf) == 1) {
                        text += new String(charBuf, 0, 1);
                        textLen++;
                    }
                    checkedString = crossString + text;
//                    MyBoxLog.debug("pageLen:" + textLen + " checkedString.length:" + checkedString.length());
//                    MyBoxLog.debug(checkedString + "\n--------------------\n ");
                    findReplaceString.setInputString(checkedString).setAnchor(0).run();
                    range = findReplaceString.getStringRange();
                    if (range != null) {
                        replacedString = findReplaceString.getOutputString();
                        lastReplacedLength = findReplaceString.getLastReplacedLength();
                        writer.write(replacedString.substring(0, lastReplacedLength));
                        crossString = replacedString.substring(lastReplacedLength, replacedString.length());
                        total += findReplaceString.getCount();
//                        MyBoxLog.debug("replacedString:" + replacedString.length() + " lastReplacedLength:" + lastReplacedLength + " total:" + total);
                        break;
                    } else {
                        if (!crossString.isEmpty()) {
                            writer.write(crossString);
//                            MyBoxLog.debug("crossString:" + crossString.length());
                        }
                        crossFrom = Math.max(1, textLen - findLen + 1);
                        crossString = text.substring(crossFrom, textLen);
                        writer.write(text.substring(0, crossFrom));
//                        MyBoxLog.debug("crossFrom:" + crossFrom);
                    }
                }
                if (!crossString.isEmpty()) {
                    writer.write(crossString);
//                    MyBoxLog.debug("crossString:" + crossString.length());
                }
            } catch (Exception e) {
                MyBoxLog.debug(e.toString());
                findReplaceFile.setError(e.toString());
                return false;
            }
            findReplaceFile.setCount(total);
            if (total > 0 && tmpFile != null && tmpFile.exists()) {
                FileTools.rename(tmpFile, sourceFile);
            }
            return true;
        } catch (Exception e) {
            findReplaceFile.setError(e.toString());
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static IndexRange stringRange(FindReplaceFile findReplace, String string) {
        if (findReplace == null) {
            return null;
        }
        IndexRange range = null;
        try {
            LongIndex fileRange = findReplace.getFileRange();
            if (string != null && fileRange != null && findReplace.getFileInfo() != null) {
                FileEditInformation fileInfo = findReplace.getFileInfo();
                int stringLength = string.length();
                long pageSize = fileInfo.getPageSize();
                int startPageNumber = (int) (fileRange.getStart() / pageSize + 1);
//                MyBoxLog.debug("startPageNumber:" + startPageNumber + " pageSize:" + pageSize
//                        + "  fileRange:" + fileRange.getStart() + " " + fileRange.getEnd() + " len:" + fileRange.getLength()
//                        + " getCurrentPage:" + fileInfo.getCurrentPage());
                if (startPageNumber == fileInfo.getCurrentPage()) {
                    int pageStart = (int) (fileRange.getStart() - fileInfo.getCurrentPageObjectStart());
                    int pageEnd = (int) (pageStart + fileRange.getLength());
//                    MyBoxLog.debug("pageSize:" + pageSize + " pageStart:" + pageStart + " pageEnd:" + pageEnd);
                    if (pageStart > 0 && fileInfo.getLineBreak() == FileEditInformation.Line_Break.CRLF) {
                        String sub;
                        int startLinesNumber, endLinesNumber;
                        if (pageStart < stringLength) {
                            sub = string.substring(0, pageStart).replaceAll("\n", "\r\n").substring(0, pageStart);
                            startLinesNumber = FindReplaceString.count(sub, "\n");
//                            MyBoxLog.debug("startLinesNumber:" + startLinesNumber);
                        } else {
                            startLinesNumber = FindReplaceString.count(string, "\n");
//                            MyBoxLog.debug("startLinesNumber:" + startLinesNumber);
                        }
                        if (pageEnd < stringLength) {
                            sub = string.substring(0, pageEnd).replaceAll("\n", "\r\n").substring(0, pageEnd);
                            endLinesNumber = FindReplaceString.count(sub, "\n");
//                            MyBoxLog.debug("endLinesNumber:" + endLinesNumber);
                        } else {
                            endLinesNumber = FindReplaceString.count(string, "\n");
//                            MyBoxLog.debug("endLinesNumber:" + endLinesNumber);
                        }
                        pageStart -= startLinesNumber;
                        pageEnd -= endLinesNumber;
//                        MyBoxLog.debug("pageStart:" + pageStart + " pageEnd:" + pageEnd);
                    }
                    range = new IndexRange(pageStart, pageEnd);
//                    MyBoxLog.debug("stringRange:" + range.getStart() + " " + range.getEnd() + " len:" + range.getLength());
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
            if (fileInfo.getEditType() != Edit_Type.Bytes && fileInfo.getLineBreak().equals(Line_Break.CRLF)) {
                String sub = string.substring(0, start);
                int linesNumber = FindReplaceString.count(sub, "\n");
                fileStart += linesNumber;
            }
            fileStart += fileInfo.getCurrentPageObjectStart() * findReplace.getUnit();
            fileRange = new LongIndex(fileStart, fileStart + stringRange.getLength());
//            MyBoxLog.debug("fileRange:" + fileRange.getStart() + " " + fileRange.getEnd());
        }
        findReplace.setFileRange(fileRange);
        return fileRange;
    }

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
            Runtime r = Runtime.getRuntime();
            long availableMem = r.maxMemory() - (r.totalMemory() - r.freeMemory());
            int pageSize = (int) Math.min(sourceFile.length(), availableMem / 48);
//            MyBoxLog.debug(availableMem + " " + sourceFile.length() + " " + pageSize);
            byte[] pageBytes = new byte[pageSize];
            // findString should have been in hex format
            String findString = findReplaceFile.getFileFindString();
            int findLen = findReplaceFile.isRegex ? pageSize * 3 : findString.length();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setAnchor(0).setWrap(false);
            int crossFrom, pageLen;
            String pageHex, crossString = "", checkedString;
            while ((pageLen = inputStream.read(pageBytes)) > 0) {
                if (pageLen < pageSize) {
                    pageBytes = ByteTools.subBytes(pageBytes, 0, pageLen);
                }
                pageHex = ByteTools.bytesToHexFormat(pageBytes);
                pageLen = pageHex.length();
                checkedString = crossString + pageHex;
//                MyBoxLog.debug(pageLen + " " + checkedString.length());
                findReplaceString.setInputString(checkedString).run();
                IndexRange lastRange = findReplaceString.getStringRange();
                if (lastRange != null) {
                    count += findReplaceString.getCount();
                    if (lastRange.getEnd() == checkedString.length()) {
                        crossString = "";
                    } else {
                        crossFrom = Math.max(3, pageLen - findLen + 3);
                        crossFrom = Math.max(lastRange.getStart() - crossString.length() + 3, crossFrom);
                        crossString = pageHex.substring(crossFrom, pageLen);
                    }
                } else {
                    crossFrom = Math.max(3, pageLen - findLen + 3);
                    crossString = pageHex.substring(crossFrom, pageLen);
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
            long position = findReplaceFile.getPosition();
            long bytesPosition = position / 3;
            if (bytesPosition < 0 || bytesPosition >= fileLength) {
                if (findReplaceFile.isWrap()) {
                    position = 0;
                    bytesPosition = 0;
                } else {
                    findReplaceFile.setError(message("InvalidParameters"));
                    return null;
                }
            }
            Runtime r = Runtime.getRuntime();
            long availableMem = r.maxMemory() - (r.totalMemory() - r.freeMemory());
            int pageSize = (int) Math.min(fileLength, availableMem / 48);
//            MyBoxLog.debug(pageSize + " " + sourceFile.length() + " " + availableMem);
            String findString = findReplaceFile.getFileFindString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setAnchor(0).setWrap(false);
            // findString should have been in hex format
            int findLen = findReplaceFile.isRegex ? pageSize * 3 : findString.length();
            LongIndex found = null;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile))) {
                if (bytesPosition > 0) {
                    inputStream.skip(bytesPosition);
                }
                int bufSize = (int) Math.min(pageSize, fileLength - bytesPosition);
//                MyBoxLog.debug("bufSize:" + bufSize + " position:" + position);
                byte[] pageBytes = new byte[bufSize];
//                MyBoxLog.debug(pageSize + " " + findReplaceFile.getPosition());
                IndexRange range;
                String pageHex, crossString = "", checkedString;
                long pageStart = position;
                int pageLen, crossFrom;
//                MyBoxLog.debug("pageStart:" + pageStart);
                while ((pageLen = inputStream.read(pageBytes)) > 0) {
                    if (pageLen < pageSize) {
                        pageBytes = ByteTools.subBytes(pageBytes, 0, pageLen);
                    }
                    pageHex = ByteTools.bytesToHexFormat(pageBytes);
                    pageLen = pageHex.length();
                    checkedString = crossString + pageHex;
//                    MyBoxLog.debug("pageLen:" + pageLen + " checkedString.length:" + checkedString.length());
//                    MyBoxLog.debug(checkedString + "\n--------------------\n ");
                    findReplaceString.setInputString(checkedString).setAnchor(0).run();
                    range = findReplaceString.getStringRange();
                    if (range != null) {
                        found = new LongIndex();
//                        MyBoxLog.debug(range.getStart());
                        found.start = pageStart + range.getStart() - crossString.length();
                        found.end = found.start + range.getLength();
//                        MyBoxLog.debug(found.start + " " + found.end);
                        break;
                    } else {
                        crossFrom = Math.max(3, pageLen - findLen + 3);
                        crossString = pageHex.substring(crossFrom, pageLen);
//                        MyBoxLog.debug("crossFrom:" + crossFrom + " >>>" + crossString + "<<<");
                    }
                    pageStart += pageLen;
                }
            }
            if (found == null && findReplaceFile.isWrap() && bytesPosition > 0 && bytesPosition < fileLength) {
                try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile))) {
                    long end;
                    if (findReplaceFile.isIsRegex()) {
                        end = fileLength * 3 - 3;
                    } else {
                        end = position + findLen - 3;
                    }
                    int bufSize = (int) Math.min(pageSize, end / 3);
                    byte[] pageBytes = new byte[bufSize];
                    IndexRange range;
                    String pageHex, crossString = "", checkedString;
                    long pageStart = 0;
                    int pageLen, crossFrom;
                    boolean reachEnd = false;
                    while ((pageLen = inputStream.read(pageBytes)) > 0) {
                        if (pageLen < pageSize) {
                            pageBytes = ByteTools.subBytes(pageBytes, 0, pageLen);
                        }
                        pageHex = ByteTools.bytesToHexFormat(pageBytes);
                        pageLen = pageHex.length();
                        if (pageStart + pageLen >= end) {
                            pageHex = pageHex.substring(0, (int) (end - pageStart));
                            pageLen = pageHex.length();
                            reachEnd = true;
//                            MyBoxLog.debug(pageStart + " " + pageLen + " " + end);
                        }
                        checkedString = crossString + pageHex;
//                        MyBoxLog.debug("pageLen:" + pageLen + " checkedString.length:" + checkedString.length());
//                        MyBoxLog.debug(pageLen + " " + checkedString.length());
//                        MyBoxLog.debug(checkedString + "\n--------------------\n ");
                        findReplaceString.setInputString(checkedString).setAnchor(0).run();
                        range = findReplaceString.getStringRange();
                        if (range != null) {
                            found = new LongIndex();
                            found.start = pageStart + range.getStart() - crossString.length();
                            found.end = found.start + range.getLength();
//                            MyBoxLog.debug(found.start + " " + found.end);
                            break;
                        }
                        if (reachEnd) {
                            break;
                        }
                        crossFrom = Math.max(3, pageLen - findLen + 3);
                        crossString = pageHex.substring(crossFrom, pageLen);
//                        MyBoxLog.debug("crossFrom:" + crossFrom + " >>>" + crossString + "<<<");
                        pageStart += pageLen;
//                        MyBoxLog.debug(pageStart + " " + crossString.length());
                    }
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

    public static boolean findNextBytes(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        try {
            LongIndex found = findNextBytesRange(sourceInfo, findReplaceFile);
            if (found == null) {
                return findReplaceFile != null && findReplaceFile.getError() == null;
            }
            long pageSize = sourceInfo.getPageSize();
            long bytesStart = found.getStart() / 3;
            int pageNumber = (int) (bytesStart / pageSize + 1);
            String pageText = sourceInfo.readPage(pageNumber);
            IndexRange stringRange = findReplaceFile.getStringRange();
            if (stringRange == null) {
                stringRange = bytesRange(findReplaceFile, pageText);
            }
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
            long position = (findReplaceFile.getPosition() / 3) * 3;
            long bytesPosition = position / 3;
//            MyBoxLog.debug("position:" + position + " bytesPosition:" + bytesPosition);
            if (bytesPosition <= 0 || bytesPosition > fileLength) {
                if (findReplaceFile.isWrap()) {
                    position = fileLength * 3;
                    bytesPosition = fileLength;
                } else {
                    findReplaceFile.setError(message("InvalidParameters"));
                    return null;
                }
            }
//            MyBoxLog.debug("position:" + position + " bytesPosition:" + bytesPosition);
            Runtime r = Runtime.getRuntime();
            long availableMem = r.maxMemory() - (r.totalMemory() - r.freeMemory());
            int pageSize = (int) Math.min(fileLength, availableMem / 48);
//            MyBoxLog.debug("pageSize:" + pageSize + " fileLength:" + fileLength + " availableMem:" + availableMem);
            String findString = findReplaceFile.getFileFindString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setAnchor(0).setWrap(false);
            // findString should have been in hex format
            int findLen = findReplaceFile.isRegex ? pageSize * 3 : findString.length();
//            MyBoxLog.debug("findLen:" + findLen);
            LongIndex found = null;
            long foundStart = -1, foundEnd = -1;
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile))) {
                int bufSize = (int) Math.min(pageSize, bytesPosition);
//                MyBoxLog.debug("bufSize:" + bufSize + " bytesPosition:" + bytesPosition);
                byte[] pageBytes = new byte[bufSize];
                IndexRange range;
                String pageHex, crossString = "", checkedString;
                long pageStart = 0;
                int pageLen, crossFrom;
                boolean reachEnd = false;
//                MyBoxLog.debug("pageStart:" + pageStart + " FindString:" + findReplaceFile.getFileFindString());
                while ((pageLen = inputStream.read(pageBytes)) > 0) {
                    if (pageLen < pageSize) {
                        pageBytes = ByteTools.subBytes(pageBytes, 0, pageLen);
                    }
//                    MyBoxLog.debug("pageLen:" + pageLen + " pageSize:" + pageSize + " position:" + position);
                    pageHex = ByteTools.bytesToHexFormat(pageBytes);
                    pageLen = pageHex.length();
                    if (pageStart + pageLen >= position) {
//                        MyBoxLog.debug("pageStart:" + pageStart + " pageLen:" + pageLen + " position:" + position);
                        pageHex = pageHex.substring(0, (int) (position - pageStart));
                        pageLen = pageHex.length();
                        reachEnd = true;
//                        MyBoxLog.debug("pageStart:" + pageStart + " pageLen:" + pageLen + " position:" + position);
                    }
                    checkedString = crossString + pageHex;
//                    MyBoxLog.debug("pageLen:" + pageLen + " checkedString.length():" + checkedString.length());
//                    MyBoxLog.debug(checkedString + "\n--------------------\n ");
                    findReplaceString.setInputString(checkedString).setAnchor(checkedString.length()).run();
                    range = findReplaceString.getStringRange();
                    if (range != null) {
//                        MyBoxLog.debug(range.getStart());
                        foundStart = pageStart + range.getStart() - crossString.length();
                        foundEnd = foundStart + range.getLength();
//                        MyBoxLog.debug("foundStart:" + foundStart + "," + foundEnd + " crossString.length():" + crossString.length());
                        if (range.getEnd() == checkedString.length()) {
                            crossString = "";
                        } else {
                            crossFrom = Math.max(3, pageLen - findLen + 3);
//                            MyBoxLog.debug("crossFrom:" + crossFrom + " pageLen:" + pageLen + " findLen:" + findLen);
                            crossFrom = Math.max(range.getStart() - crossString.length() + 3, crossFrom);
//                            MyBoxLog.debug("range.getStart() :" + range.getStart() + " crossString.length():" + crossString.length() + " crossFrom:" + crossFrom);
                            crossString = pageHex.substring(crossFrom, pageLen);
                        }
                    } else {
                        crossFrom = Math.max(1, pageLen - findLen + 3);
                        crossString = pageHex.substring(crossFrom, pageLen);
                    }
                    if (reachEnd) {
                        break;
                    }
//                        MyBoxLog.debug(crossFrom + " " + crossString);
                    pageStart += pageLen;
                }
            }
            if (foundStart >= 0) {
                found = new LongIndex(foundStart, foundEnd);
            } else if (findReplaceFile.isWrap() && bytesPosition > 0 && bytesPosition < fileLength) {
                try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile))) {
                    long pageStart;
                    if (findReplaceFile.isIsRegex()) {
                        pageStart = 3;
                    } else {
                        pageStart = Math.max(3, position - findLen + 3);
                    }
                    inputStream.skip(pageStart / 3);
                    int bufSize = (int) Math.min(pageSize, fileLength - pageStart / 3);
//                    MyBoxLog.debug("bufSize:" + bufSize + " pageStart:" + pageStart);
                    byte[] pageBytes = new byte[bufSize];
                    IndexRange range;
                    String pageHex, crossString = "", checkedString;
                    int pageLen, crossFrom;
//                    MyBoxLog.debug(pageStart);
                    while ((pageLen = inputStream.read(pageBytes)) > 0) {
                        if (pageLen < pageSize) {
                            pageBytes = ByteTools.subBytes(pageBytes, 0, pageLen);
                        }
                        pageHex = ByteTools.bytesToHexFormat(pageBytes);
                        pageLen = pageHex.length();
                        checkedString = crossString + pageHex;
//                        MyBoxLog.debug(pageLen + " " + checkedString.length());
//                        MyBoxLog.debug(checkedString + "\n--------------------\n ");
                        findReplaceString.setInputString(checkedString).setAnchor(checkedString.length()).run();
                        range = findReplaceString.getStringRange();
                        if (range != null) {
                            foundStart = pageStart + range.getStart() - crossString.length();
                            foundEnd = foundStart + range.getLength();
//                            MyBoxLog.debug("foundStart:" + foundStart + " " + foundEnd + " crossString.length():" + crossString.length());
                            if (range.getEnd() == checkedString.length()) {
                                crossString = "";
                            } else {
                                crossFrom = Math.max(3, pageLen - findLen + 3);
//                                MyBoxLog.debug("crossFrom:" + crossFrom + " pageLen:" + pageLen + " findLen:" + findLen);
                                crossFrom = Math.max(range.getStart() - crossString.length() + 3, crossFrom);
//                                MyBoxLog.debug("range.getStart() :" + range.getStart() + " crossString.length():" + crossString.length() + " crossFrom:" + crossFrom);
                                crossString = pageHex.substring(crossFrom, pageLen);
                            }
                        } else {
                            crossFrom = Math.max(3, pageLen - findLen + 3);
                            crossString = pageHex.substring(crossFrom, pageLen);
                        }
                        pageStart += pageLen;
//                        MyBoxLog.debug(pageStart + " " + crossString.length());
                    }
                }
                if (foundStart >= 0) {
                    found = new LongIndex(foundStart, foundEnd);
//                    MyBoxLog.debug("found:" + foundStart + " " + foundEnd);
                }
            }
//            if (found != null) {
//                MyBoxLog.debug("found:" + found.getStart() + " " + found.getEnd());
//            }
            findReplaceFile.setFileRange(found);
            return found;
        } catch (Exception e) {
            findReplaceFile.setError(e.toString());
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static boolean findPreviousBytes(FileEditInformation sourceInfo, FindReplaceFile findReplaceFile) {
        try {
            LongIndex found = findPreviousBytesRange(sourceInfo, findReplaceFile);
            if (found == null) {
                return findReplaceFile != null && findReplaceFile.getError() == null;
            }
            long pageSize = sourceInfo.getPageSize();
            long bytesStart = found.getStart() / 3;
            int pageNumber = (int) (bytesStart / pageSize + 1);
            String pageText = sourceInfo.readPage(pageNumber);
            IndexRange stringRange = findReplaceFile.getStringRange();
            if (stringRange == null) {
                stringRange = bytesRange(findReplaceFile, pageText);
            }
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
            LongIndex found = findNextBytesRange(sourceInfo, findReplaceFile);
            if (found == null) {
                return findReplaceFile != null && findReplaceFile.getError() == null;
            }
            long pageSize = sourceInfo.getPageSize();
            long bytesStart = found.getStart() / 3;
            int pageNumber = (int) (bytesStart / pageSize + 1);
            String pageText = sourceInfo.readPage(pageNumber);
            IndexRange stringRange = findReplaceFile.getStringRange();
            if (stringRange == null) {
                stringRange = stringRange(findReplaceFile, pageText);
            }
            if (stringRange == null) {
                findReplaceFile.setError(message("InvalidData"));
                return false;
            }
            String replaceString = findReplaceFile.getReplaceString();
            String output = pageText.substring(0, stringRange.getStart())
                    + replaceString;
            if (stringRange.getEnd() < pageText.length()) {
                output += pageText.substring(stringRange.getEnd(), pageText.length());
            }
            findReplaceFile.setFileRange(new LongIndex(found.getStart(), found.getStart() + replaceString.length()));
            findReplaceFile.setStringRange(new IndexRange(stringRange.getStart(), stringRange.getStart() + replaceString.length()));
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
        try {
            sourceInfo.setFindReplace(findReplaceFile);
            findReplaceFile.setFileInfo(sourceInfo);
            File sourceFile = sourceInfo.getFile();
            Runtime r = Runtime.getRuntime();
            long availableMem = r.maxMemory() - (r.totalMemory() - r.freeMemory());
            int pageSize = (int) Math.min(sourceFile.length(), availableMem / 48);
//            MyBoxLog.debug(pageSize + " " + sourceFile.length() + " " + availableMem);
            String findString = findReplaceFile.getFileFindString();
            String replaceString = findReplaceFile.getFileReplaceString();
            FindReplaceString findReplaceString = findReplaceFile.findReplaceString()
                    .setFindString(findString).setReplaceString(replaceString)
                    .setAnchor(0).setWrap(false);
            // findString should have been in hex format
            int findLen = findReplaceFile.isRegex ? pageSize * 3 : findString.length();
            File tmpFile = FileTools.getTempFile();
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                byte[] pageBytes = new byte[pageSize];
//                MyBoxLog.debug("pageSize:" + pageSize);
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
//                    MyBoxLog.debug("pageLen:" + pageLen + " checkedString.length:" + checkedString.length());
//                    MyBoxLog.debug(checkedString + "\n--------------------\n ");
                    findReplaceString.setInputString(checkedString).setAnchor(0).run();
                    range = findReplaceString.getStringRange();
                    if (range != null) {
                        replacedString = findReplaceString.getOutputString();
                        lastReplacedLength = findReplaceString.getLastReplacedLength();
                        byte[] replacedBytes = ByteTools.hexFormatToBytes(replacedString.substring(0, lastReplacedLength));
                        outputStream.write(replacedBytes);
                        crossString = replacedString.substring(lastReplacedLength, replacedString.length());
                        total += findReplaceString.getCount();
//                        MyBoxLog.debug("replacedString:" + replacedString.length() + " lastReplacedLength:" + lastReplacedLength + " total:" + total);
                        break;
                    } else {
                        if (!crossString.isEmpty()) {
                            byte[] crossBytes = ByteTools.hexFormatToBytes(crossString);
                            outputStream.write(crossBytes);
//                            MyBoxLog.debug("crossString:" + crossString.length());
                        }
                        crossFrom = Math.max(3, pageLen - findLen + 3);
                        crossString = pageHex.substring(crossFrom, pageLen);
                        byte[] passBytes = ByteTools.hexFormatToBytes(pageHex.substring(0, crossFrom));
                        outputStream.write(passBytes);
//                        MyBoxLog.debug("crossFrom:" + crossFrom);
                    }
                }
                if (!crossString.isEmpty()) {
                    byte[] crossBytes = ByteTools.hexFormatToBytes(crossString);
                    outputStream.write(crossBytes);
//                    MyBoxLog.debug("crossString:" + crossString.length());
                }
            } catch (Exception e) {
                MyBoxLog.debug(e.toString());
                findReplaceFile.setError(e.toString());
                return false;
            }
            findReplaceFile.setCount(total);
            if (total > 0 && tmpFile != null && tmpFile.exists()) {
                FileTools.rename(tmpFile, sourceFile);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            findReplaceFile.setError(e.toString());
            return false;
        }
    }

    public static IndexRange bytesRange(FindReplaceFile findReplace, String string) {
        if (findReplace == null) {
            return null;
        }
        IndexRange range = null;
        try {
            LongIndex fileRange = findReplace.getFileRange();
            if (string != null && fileRange != null && findReplace.getFileInfo() != null) {
                FileEditInformation fileInfo = findReplace.getFileInfo();
                long pageSize = fileInfo.getPageSize();
                long bytesStart = fileRange.getStart() / 3;
                int startPageNumber = (int) (bytesStart / pageSize + 1);
//                MyBoxLog.debug("startPageNumber:" + startPageNumber + " pageSize:" + pageSize + " bytesStart:" + bytesStart
//                        + "  fileRange:" + fileRange.getStart() + " " + fileRange.getEnd() + " len:" + fileRange.getLength()
//                        + " getCurrentPage:" + fileInfo.getCurrentPage());
                if (startPageNumber == fileInfo.getCurrentPage()) {
                    int pageStart = (int) (bytesStart % pageSize) * 3;
                    int pageEnd = (int) (pageStart + fileRange.getLength());
//                    MyBoxLog.debug("pageSize:" + pageSize + " pageStart:" + pageStart + " pageEnd:" + pageEnd);
                    range = new IndexRange(pageStart, pageEnd);
//                    MyBoxLog.debug("stringRange:" + range.getStart() + " " + range.getEnd() + " len:" + range.getLength());
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        findReplace.setStringRange(range);
        return range;
    }

    /*
        get/set
     */
    public boolean isMultiplePages() {
        return multiplePages;
    }

    public FindReplaceFile setMultiplePages(boolean multiplePages) {
        this.multiplePages = multiplePages;
        return this;
    }

    public FileEditInformation getFileInfo() {
        return fileInfo;
    }

    public FindReplaceFile setFileInfo(FileEditInformation fileInfo) {
        this.fileInfo = fileInfo;
        return this;
    }

    public LongIndex getFileRange() {
        return fileRange;
    }

    public FindReplaceFile setFileRange(LongIndex lastFound) {
        this.fileRange = lastFound;
        return this;
    }

    public long getPosition() {
        return position;
    }

    public FindReplaceFile setPosition(long position) {
        this.position = position;
        return this;
    }

    public boolean isPageReloaded() {
        return pageReloaded;
    }

    public FindReplaceFile setPageReloaded(boolean pageReloaded) {
        this.pageReloaded = pageReloaded;
        return this;
    }

    public String getFileFindString() {
        return fileFindString;
    }

    public FindReplaceFile setFileFindString(String fileFindString) {
        this.fileFindString = fileFindString;
        return this;
    }

    public String getFileReplaceString() {
        return fileReplaceString;
    }

    public FindReplaceFile setFileReplaceString(String fileReplaceString) {
        this.fileReplaceString = fileReplaceString;
        return this;
    }

}
