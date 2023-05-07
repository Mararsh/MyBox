package mara.mybox.data;

import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2023-5-7
 * @License Apache License Version 2.0
 */
public class FindReplaceMatch {

    public final static int MatchedPrefixLength = 100;

    protected long line, start, end;  // 0-based, exclude end
    protected String matchedPrefix;

    public FindReplaceMatch() {
        line = start = end = -1;
        matchedPrefix = null;
    }

    /*
        static
     */
    public static FindReplaceMatch create() {
        return new FindReplaceMatch();
    }

    /*
        set
     */
    public FindReplaceMatch setLine(long line) {
        this.line = line;
        return this;
    }

    public FindReplaceMatch setStart(long start) {
        this.start = start;
        return this;
    }

    public FindReplaceMatch setEnd(long end) {
        this.end = end;
        return this;
    }

    public FindReplaceMatch setMatchedPrefix(String matchedPrefix) {
        this.matchedPrefix = StringTools.abbreviate(matchedPrefix, MatchedPrefixLength);
        return this;
    }

    /*
        get
     */
    public long getLine() {
        return line;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public String getMatchedPrefix() {
        return matchedPrefix;
    }

}
