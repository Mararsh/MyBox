package mara.mybox.db.data;

import static mara.mybox.db.data.DataNode.ValueSeparater;
import mara.mybox.tools.JsonTools;

/**
 * @Author Mara
 * @CreateDate 2024-11-1
 * @License Apache License Version 2.0
 */
public class DataValuesTools {

    public static String toText(DataValues data) {
        String info = (String) data.getValue("info");
        String title = (String) data.getValue("title");
        if (title == null || title.isBlank()) {
            if (info == null || info.isBlank()) {
                return null;
            } else {
                return ValueSeparater + "\n" + info.trim();
            }
        } else {
            if (info == null || info.isBlank()) {
                return title.trim() + ValueSeparater;
            } else {
                return title.trim() + ValueSeparater + "\n" + info.trim();
            }
        }
    }

    public static String toXml(DataValues data, String prefix) {
        String xml = prefix + "<node>\n";
        String info = (String) data.getValue("info");
        String title = (String) data.getValue("title");
        if (title != null && !title.isBlank()) {
            xml += prefix + prefix + "<title>\n"
                    + prefix + prefix + prefix + "<![CDATA[" + title.trim() + "]]>\n"
                    + prefix + prefix + "</title>\n";
        }
        if (info != null && !info.isBlank()) {
            xml += prefix + prefix + "<info>\n"
                    + prefix + prefix + prefix + "<![CDATA[" + info.trim() + "]]>\n"
                    + prefix + prefix + "</info>\n";
        }
        xml += prefix + "</node>\n";
        return xml;
    }

    public static String toHtml(DataValues data) {
        String html = "";
        String info = (String) data.getValue("info");
        String title = (String) data.getValue("title");
        if (title != null && !title.isBlank()) {
            html += "<H2>" + title.trim() + "</H2>\n";
        }
        if (info != null && !info.isBlank()) {
            html += info.trim();
        }
        return html;
    }

    public static String toJson(DataValues data, String prefix) {
        String json = "";
        String info = (String) data.getValue("info");
        String title = (String) data.getValue("title");
        if (title != null && !title.isBlank()) {
            json += prefix + ",\n"
                    + prefix + "\"title\": " + JsonTools.encode(title.trim());
        }
        if (info != null && !info.isBlank()) {
            json += prefix + ",\n"
                    + prefix + "\"info\": " + JsonTools.encode(info.trim());
        }
        return json;
    }

}
