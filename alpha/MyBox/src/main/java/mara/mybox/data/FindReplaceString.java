package mara.mybox.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.IndexRange;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-11-5
 * @License Apache License Version 2.0
 */
public class FindReplaceString {

    protected Operation operation;
    protected boolean isRegex, caseInsensitive, multiline, dotAll, appendTail, wrap;
    protected String inputString, findString, replaceString, outputString;
    protected int anchor, count, lastStart, lastEnd, lastReplacedLength, unit;
    protected IndexRange stringRange;  // location in string
    protected String lastMatch, error;

    public enum Operation {
        ReplaceAll, ReplaceFirst, FindNext, FindPrevious, Count
    }

    public FindReplaceString() {
        isRegex = caseInsensitive = false;
        multiline = wrap = true;
        unit = 1;
    }

    public FindReplaceString reset() {
        count = 0;
        stringRange = null;
        lastMatch = null;
        outputString = inputString;
        error = null;
        lastStart = lastEnd = -1;
        return this;
    }

    public boolean run() {
        reset();
        if (operation == null
                || inputString == null || inputString.isEmpty()
                || findString == null || findString.isEmpty()) {
            return false;
        }
        int start, end = inputString.length();
        switch (operation) {
            case FindNext:
            case ReplaceFirst:
                if (anchor >= 0 && anchor <= inputString.length() - 1) {
                    start = anchor;
                } else if (!wrap) {
                    return true;
                } else {
                    start = 0;
                }
                break;
            case FindPrevious:
                start = 0;
                if (anchor > 0 && anchor <= inputString.length()) {
                    end = anchor;
                } else if (!wrap) {
                    return true;
                }
                break;
            default:
                start = 0;
                break;
        }
        run(start, end);
        if (lastMatch != null || !wrap) {
            return true;
        }
//        MyBoxLog.debug(operation + " " + wrap);
        switch (operation) {
            case FindNext:
            case ReplaceFirst:
                if (start <= 0) {
                    return true;
                }
                if (isRegex) {
                    end = inputString.length();
                    start = 0;
                } else {
                    end = start + findString.length();
                    start = 0;
                }
                return run(start, end);
            case FindPrevious:
                if (end > inputString.length()) {
                    return true;
                }
                if (isRegex) {
                    end = inputString.length();
                    start = unit;
                } else {
                    start = Math.max(unit, end - findString.length() + unit);
                    end = inputString.length();
                }
                run(start, end);
                return true;
            default:
                return true;
        }
    }

    public boolean run(int start, int end) {
        try {
//            MyBoxLog.debug(operation + " start:" + start + " end:" + end + " findString:>>" + findString + "<<  unit:" + unit);
//            MyBoxLog.debug("findString.length()：" + findString.length());
//            MyBoxLog.debug("replaceString:>>" + replaceString + "<<");
//            MyBoxLog.debug("\n------\n" + inputString + "\n-----");
            reset();
            int mode = (isRegex ? 0x00 : Pattern.LITERAL)
                    | (caseInsensitive ? Pattern.CASE_INSENSITIVE : 0x00)
                    | (dotAll ? Pattern.DOTALL : 0x00)
                    | (multiline ? Pattern.MULTILINE : 0x00);
            Pattern pattern = Pattern.compile(findString, mode);
            Matcher matcher = pattern.matcher(inputString);
            matcher.region(Math.max(0, start), Math.min(inputString.length(), end));
            StringBuffer s = new StringBuffer();
            if (replaceString != null && !replaceString.isBlank()) {
                replaceString = Matcher.quoteReplacement(replaceString);
            }
            OUTER:
            while (matcher.find()) {
                count++;
                lastMatch = matcher.group();
                lastStart = matcher.start();
                lastEnd = matcher.end();
                if (null == operation) {
                    if (matcher.start() >= end) {
                        break OUTER;
                    }
                    matcher.region(lastStart + unit, end);
                } else {
//                    MyBoxLog.debug(count + " " + matcher.start() + " " + matcher.end() + " " + lastMatch);
//                    MyBoxLog.debug(inputString.substring(0, matcher.start()) + "\n----------------");
                    switch (operation) {
                        case FindNext:
                            break OUTER;
                        case ReplaceFirst:
                            matcher.appendReplacement(s, replaceString);
                            break OUTER;
                        case ReplaceAll:
                            matcher.appendReplacement(s, replaceString);
//                            MyBoxLog.debug("\n---" + count + "---\n" + s.toString() + "\n-----");
                            break;
                        default:
                            if (matcher.start() >= end) {
                                break;
                            }
                            matcher.region(lastStart + unit, end);
                            break;
                    }
                }
            }
//            MyBoxLog.debug("count:" + count);
            if (lastMatch != null) {
                stringRange = new IndexRange(lastStart, lastEnd);
                if (operation == Operation.ReplaceAll || operation == Operation.ReplaceFirst) {
                    String replacedPart = s.toString();
                    lastReplacedLength = replacedPart.length();
//                    MyBoxLog.debug("lastReplacedLength:" + lastReplacedLength);
                    matcher.appendTail(s);
                    outputString = s.toString();
//                    MyBoxLog.debug("\n---outputString---\n" + outputString + "\n-----");
//                    MyBoxLog.debug("inputString:" + inputString.length() + " outputString:" + outputString.length());
                }
//                MyBoxLog.debug(operation + " stringRange:" + stringRange.getStart() + " " + stringRange.getEnd() + " len:" + stringRange.getLength()
//                        + " findString:>>" + findString + "<< lastMatch:>>" + lastMatch + "<<");
            } else {
                outputString = inputString + "";
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            error = e.toString();
            return false;
        }
    }

    public String replaceStringAll(String string, String find, String replace) {
        setInputString(string).setFindString(find).setReplaceString(replace).setAnchor(0).run();
        return outputString;
    }

    /*
        static methods
     */
    public static FindReplaceString create() {
        return new FindReplaceString();
    }

    public static FindReplaceString finder(boolean isRegex, boolean isCaseInsensitive) {
        return create().setOperation(FindReplaceString.Operation.FindNext)
                .setAnchor(0).setIsRegex(isRegex).setCaseInsensitive(isCaseInsensitive).setMultiline(true);
    }

    public static FindReplaceString counter(boolean isRegex, boolean isCaseInsensitive) {
        return create().setOperation(FindReplaceString.Operation.Count)
                .setAnchor(0).setIsRegex(isRegex).setCaseInsensitive(isCaseInsensitive).setMultiline(true);
    }

    public static int count(String string, String find) {
        return count(string, find, 0, false, false, true);
    }

    public static int count(String string, String find, int from,
            boolean isRegex, boolean caseInsensitive, boolean multiline) {
        FindReplaceString stringFind = create()
                .setOperation(Operation.Count)
                .setInputString(string).setFindString(find).setAnchor(from)
                .setIsRegex(isRegex).setCaseInsensitive(caseInsensitive).setMultiline(multiline);
        stringFind.run();
        return stringFind.getCount();
    }

    public static IndexRange next(String string, String find, int from,
            boolean isRegex, boolean caseInsensitive, boolean multiline) {
        FindReplaceString stringFind = create()
                .setOperation(Operation.FindNext)
                .setInputString(string).setFindString(find).setAnchor(from)
                .setIsRegex(isRegex).setCaseInsensitive(caseInsensitive).setMultiline(multiline);
        stringFind.run();
        return stringFind.getStringRange();
    }

    public static IndexRange previous(String string, String find, int from,
            boolean isRegex, boolean caseInsensitive, boolean multiline) {
        FindReplaceString stringFind = create()
                .setOperation(Operation.FindPrevious)
                .setInputString(string).setFindString(find).setAnchor(from)
                .setIsRegex(isRegex).setCaseInsensitive(caseInsensitive).setMultiline(multiline);
        stringFind.run();
        return stringFind.getStringRange();
    }

    public static String replaceAll(String string, String find, String replace) {
        return replaceAll(string, find, replace, 0, false, false, true);
    }

    public static String replaceAll(String string, String find, String replace, int from,
            boolean isRegex, boolean caseInsensitive, boolean multiline) {
        FindReplaceString stringFind = create()
                .setOperation(Operation.ReplaceAll)
                .setInputString(string).setFindString(find).setReplaceString(replace).setAnchor(from)
                .setIsRegex(isRegex).setCaseInsensitive(caseInsensitive).setMultiline(multiline);
        stringFind.run();
        return stringFind.getOutputString();
    }

    public static String replaceAll(FindReplaceString findReplace, String string, String find, String replace) {
        findReplace.setInputString(string).setFindString(find).setReplaceString(replace).setAnchor(0).run();
        return findReplace.getOutputString();
    }

    public static String replace(String string, String find, String replace) {
        return replaceAll(string, find, replace, 0, false, false, true);
    }

    public static String replace(String string, String find, String replace, int from,
            boolean isRegex, boolean caseInsensitive, boolean multiline) {
        FindReplaceString stringFind = create()
                .setOperation(Operation.ReplaceFirst)
                .setInputString(string).setFindString(find).setReplaceString(replace).setAnchor(from)
                .setIsRegex(isRegex).setCaseInsensitive(caseInsensitive).setMultiline(multiline);
        stringFind.run();
        return stringFind.getOutputString();
    }


    /*
        get/set
     */
    public boolean isIsRegex() {
        return isRegex;
    }

    public FindReplaceString setIsRegex(boolean isRegex) {
        this.isRegex = isRegex;
        return this;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public FindReplaceString setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
        return this;
    }

    public boolean isMultiline() {
        return multiline;
    }

    public FindReplaceString setMultiline(boolean multiline) {
        this.multiline = multiline;
        return this;
    }

    public String getInputString() {
        return inputString;
    }

    public boolean isDotAll() {
        return dotAll;
    }

    public FindReplaceString setDotAll(boolean dotAll) {
        this.dotAll = dotAll;
        return this;
    }

    public int getLastEnd() {
        return lastEnd;
    }

    public void setLastEnd(int lastEnd) {
        this.lastEnd = lastEnd;
    }

    public FindReplaceString setInputString(String inputString) {
        this.inputString = inputString;
        return this;
    }

    public String getFindString() {
        return findString;
    }

    public FindReplaceString setFindString(String findString) {
        this.findString = findString;
        return this;
    }

    public String getReplaceString() {
        return replaceString;
    }

    public FindReplaceString setReplaceString(String replaceString) {
        this.replaceString = replaceString;
        return this;
    }

    public String getOutputString() {
        return outputString;
    }

    public FindReplaceString setOuputString(String outputString) {
        this.outputString = outputString;
        return this;
    }

    public int getCount() {
        return count;
    }

    public FindReplaceString setCount(int count) {
        this.count = count;
        return this;
    }

    public IndexRange getStringRange() {
        return stringRange;
    }

    public FindReplaceString setStringRange(IndexRange lastRange) {
        this.stringRange = lastRange;
        return this;
    }

    public String getLastMatch() {
        return lastMatch;
    }

    public FindReplaceString setLastMatch(String lastMatch) {
        this.lastMatch = lastMatch;
        return this;
    }

    public FindReplaceString setAnchor(int anchor) {
        this.anchor = anchor;
        return this;
    }

    public int getAnchor() {
        return anchor;
    }

    public Operation getOperation() {
        return operation;
    }

    public FindReplaceString setOperation(Operation operation) {
        this.operation = operation;
        return this;
    }

    public boolean isAppendTail() {
        return appendTail;
    }

    public FindReplaceString setAppendTail(boolean appendTail) {
        this.appendTail = appendTail;
        return this;
    }

    public int getLastStart() {
        return lastStart;
    }

    public FindReplaceString setLastStart(int lastStart) {
        this.lastStart = lastStart;
        return this;
    }

    public int getLastReplacedLength() {
        return lastReplacedLength;
    }

    public FindReplaceString setLastReplacedLength(int lastReplacedLength) {
        this.lastReplacedLength = lastReplacedLength;
        return this;
    }

    public boolean isWrap() {
        return wrap;
    }

    public FindReplaceString setWrap(boolean wrap) {
        this.wrap = wrap;
        return this;
    }

    public int getUnit() {
        return unit;
    }

    public FindReplaceString setUnit(int step) {
        this.unit = step;
        return this;
    }

    public String getError() {
        return error;
    }

    public FindReplaceString setError(String error) {
        this.error = error;
        return this;
    }

}
