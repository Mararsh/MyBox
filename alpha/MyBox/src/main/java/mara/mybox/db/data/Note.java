package mara.mybox.db.data;

import static mara.mybox.db.data.DataNode.ValueSeparater;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.JsonTools;

/**
 * @Author Mara
 * @CreateDate 2024-8-2
 * @License Apache License Version 2.0
 */
public class Note extends DataNode {

    @Override
    public boolean valid() {
        return valid(this);
    }

    @Override
    public String toText() {
        String note = getNote();
        String title = getDataTitle();
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
        String title = getDataTitle();
        String note = getNote();
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
        String title = getDataTitle();
        String note = getNote();
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
        String title = getDataTitle();
        String note = getNote();
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

    public static boolean valid(Note data) {
        return data != null;
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
        try {
            Object v = getValue("noteid");
            return v == null ? -1 : (long) v;
        } catch (Exception e) {
            return -1;
        }
    }

    public Note setNoteid(long noteid) {
        setValue("noteid", noteid);
        return this;
    }

    public String getNote() {
        try {
            Object v = getValue("note");
            return v == null ? null : (String) v;
        } catch (Exception e) {
            return null;
        }
    }

    public Note setNote(String note) {
        setValue("note", note);
        return this;
    }

}
