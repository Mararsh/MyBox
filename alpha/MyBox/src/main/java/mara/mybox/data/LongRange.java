package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2020-10-6
 * @License Apache License Version 2.0
 */
public class LongRange {

    protected long start = -1, end = -1, length = -1;

    public LongRange() {
        start = -1;
        end = -1;
        length = -1;
    }

    public LongRange(long start) {
        this.start = start;
    }

    public LongRange(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long getLength() {
        if (length < 0) {
            length = end - start;
        }
        return length;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public void setLength(long length) {
        this.length = length;
    }

}
