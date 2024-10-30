package mara.mybox.db.data;

import static mara.mybox.db.data.DataNode.ValueSeparater;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.JsonTools;

/**
 * @Author Mara
 * @CreateDate 2024-8-2
 * @License Apache License Version 2.0
 */
public class Note extends BaseTreeData {

    protected long noteid;
    protected String note;

    @Override
    public boolean valid() {
        return valid(this);
    }

    @Override
    public boolean setDataValue(String column, Object value) {
        return setDataValue(this, column, value);
    }

    @Override
    public Object getDataValue(String column) {
        return getDataValue(this, column);
    }

    @Override
    public String toText() {
        if (title == null || title.isBlank()) {
            if (note == null || note.isBlank()) {
                return null;
            } else {
                return ValueSeparater + "\n" + note.trim();
            }
        } else {
            if (note == null || note.isBlank()) {
                return title.trim() + ValueSeparater;
            } else {
                return title.trim() + ValueSeparater + "\n" + note.trim();
            }
        }
    }

    @Override
    public String toXml(String prefix) {
        String xml = prefix + "<node>\n";
        if (title != null && !title.isBlank()) {
            xml += prefix + prefix + "<title>\n"
                    + prefix + prefix + prefix + "<![CDATA[" + title.trim() + "]]>\n"
                    + prefix + prefix + "</title>\n";
        }
        if (note != null && !note.isBlank()) {
            xml += prefix + prefix + "<note>\n"
                    + prefix + prefix + prefix + "<![CDATA[" + note.trim() + "]]>\n"
                    + prefix + prefix + "</note>\n";
        }
        xml += prefix + "</node>\n";
        return xml;
    }

    @Override
    public String toHtml() {
        String html = "";
        if (title != null && !title.isBlank()) {
            html += "<H2>" + title.trim() + "</H2>\n";
        }
        if (note != null && !note.isBlank()) {
            html += HtmlReadTools.body(note, false);
        }
        return html;
    }

    @Override
    public String toJson(String prefix) {
        String json = "";
        if (title != null && !title.isBlank()) {
            json += prefix + ",\n"
                    + prefix + "\"title\": " + JsonTools.encode(title.trim());
        }
        if (note != null && !note.isBlank()) {
            json += prefix + ",\n"
                    + prefix + "\"note\": " + JsonTools.encode(note.trim());
        }
        return json;
    }

    /*
        Static methods
     */
    public static Note create() {
        return new Note();
    }

    public static boolean setDataValue(Note data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "noteid":
                    data.setNoteid(value == null ? -1 : (long) value);
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "note":
                    data.setNote(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getDataValue(Note data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "noteid":
                return data.getNoteid();
            case "title":
                return data.getTitle();
            case "note":
                return data.getNote();
        }
        return null;
    }

    public static boolean valid(Note data) {
        return data != null;
    }

    public static Note fromInfo(String title, String info) {
        return Note.create().setTitle(title).setNote(info);
    }

    public static Note fromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        if (text.startsWith(ValueSeparater)) {
            return Note.create().setNote(text);
        } else {
            return Note.create().setNote(text);
        }
    }

    public static Note fromXml(String xml) {
        return null;
    }

    /*
        get/set
     */
    public long getNoteid() {
        return noteid;
    }

    public Note setNoteid(long noteid) {
        this.noteid = noteid;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Note setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getNote() {
        return note;
    }

    public Note setNote(String note) {
        this.note = note;
        return this;
    }

}
