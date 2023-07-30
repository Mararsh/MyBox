package mara.mybox.data;

import java.util.List;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2023-7-12
 * @License Apache License Version 2.0
 */
public class DoublePath extends DoubleRectangle {

    protected String content;
    protected List<DoublePathSegment> segments;
    protected int scale;

    public DoublePath() {
        init();
    }

    final public void init() {
        content = null;
        segments = null;
        scale = 3;
    }

    public DoublePath(String content) {
        init();
        parse(content);
    }

    public final List<DoublePathSegment> parse(String content) {
        this.content = content;

        DoublePathParser parser = new DoublePathParser().parse(content, scale);
        if (parser == null) {
            segments = null;
        } else {
            segments = parser.getSegments();
        }
        return segments;
    }

    public String typesetting(String separator) {
        return segmentsToPath(segments, separator);
    }

    /*
        static
     */
    public static String segmentsToPath(List<DoublePathSegment> segments, String separator) {
        try {
            if (segments == null || segments.isEmpty()) {
                return null;
            }
            String path = null;
            for (DoublePathSegment seg : segments) {
                if (path != null) {
                    path += separator + seg.text();
                } else {
                    path = seg.text();
                }
            }
            return path;
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    public static String typesetting(String content, String separator) {
        try {
            DoublePath path = new DoublePath(content);
            return segmentsToPath(path.getSegments(), separator);
        } catch (Exception e) {
            MyBoxLog.console(e);
            return content;
        }
    }

    /*
        set
     */
    public void setContent(String content) {
        this.content = content;
    }

    public void setSegments(List<DoublePathSegment> segments) {
        this.segments = segments;
    }

    /*
        get
     */
    public String getContent() {
        return content;
    }

    public List<DoublePathSegment> getSegments() {
        return segments;
    }

}
