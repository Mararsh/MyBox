package mara.mybox.db.data;

import static mara.mybox.db.data.DataNode.ValueSeparater;
import mara.mybox.tools.JsonTools;

/**
 * @Author Mara
 * @CreateDate 2024-8-2
 * @License Apache License Version 2.0
 */
public class InfoInTree extends DataNode {

    @Override
    public boolean valid() {
        return valid(this);
    }

    @Override
    public String toText() {
        String info = getInfo();
        String title = getDataTitle();
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

    @Override
    public String toXml(String prefix) {
        String info = getInfo();
        String xml = prefix + "<node>\n";
        String title = getDataTitle();
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

    @Override
    public String toHtml() {
        String info = getInfo();
        String html = "";
        String title = getDataTitle();
        if (title != null && !title.isBlank()) {
            html += "<H2>" + title.trim() + "</H2>\n";
        }
        if (info != null && !info.isBlank()) {
            html += info.trim();
        }
        return html;
    }

    @Override
    public String toJson(String prefix) {
        String info = getInfo();
        String title = getDataTitle();
        String json = "";
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


    /*
        Static methods
     */
    public static InfoInTree create() {
        return new InfoInTree();
    }

    public static boolean valid(InfoInTree data) {
        return data != null;
    }

    /*
        get/set
     */
    public long getInfoid() {
        try {
            Object v = getValue("infoid");
            return v == null ? -1 : (long) v;
        } catch (Exception e) {
            return -1;
        }
    }

    public InfoInTree setInfoid(long infoid) {
        setValue("infoid", infoid);
        return this;
    }

    public String getInfo() {
        try {
            Object v = getValue("info");
            return v == null ? null : (String) v;
        } catch (Exception e) {
            return null;
        }
    }

    public InfoInTree setInfo(String info) {
        setValue("info", info);
        return this;
    }

}
