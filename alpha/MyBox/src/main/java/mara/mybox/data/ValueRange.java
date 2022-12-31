package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2022-11-22
 * @License Apache License Version 2.0
 */
public class ValueRange {

    protected Object start, end;
    protected boolean includeStart, includeEnd;

    public static enum SplitType {
        Size, Number, List
    }

    public ValueRange() {
        start = null;
        end = null;
        includeStart = false;
        includeEnd = false;
    }

    @Override
    public String toString() {
        return (includeStart ? "[" : "(")
                + start + "," + end
                + (includeEnd ? "]" : ")");
    }

    /*
        get/set
     */
    public Object getStart() {
        return start;
    }

    public ValueRange setStart(Object start) {
        this.start = start;
        return this;
    }

    public Object getEnd() {
        return end;
    }

    public ValueRange setEnd(Object end) {
        this.end = end;
        return this;
    }

    public boolean isIncludeStart() {
        return includeStart;
    }

    public ValueRange setIncludeStart(boolean includeStart) {
        this.includeStart = includeStart;
        return this;
    }

    public boolean isIncludeEnd() {
        return includeEnd;
    }

    public ValueRange setIncludeEnd(boolean includeEnd) {
        this.includeEnd = includeEnd;
        return this;
    }

}
