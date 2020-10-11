package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2020-10-6
 * @License Apache License Version 2.0
 */
public class LongIndex {

    protected long start = -1, end = -1, length = -1;

    public LongIndex() {
        start = -1;
        end = -1;
        length = -1;
    }

    public LongIndex(long start) {
        this.start = start;
    }

    public LongIndex(long start, long end) {
        this.start = start;
        this.end = start;
    }

    public long getLength() {
        if (length < 0) {
            length = end - start + 1;
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
