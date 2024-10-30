package mara.mybox.db.data;

import static mara.mybox.db.data.DataNode.ValueSeparater;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.JsonTools;

/**
 * @Author Mara
 * @CreateDate 2024-8-2
 * @License Apache License Version 2.0
 */
public class InfoInTree extends BaseTreeData {

    protected long infoid;
    protected String info;

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
        String xml = prefix + "<node>\n";
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
        String html = "";
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

    public static boolean setDataValue(InfoInTree data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        MyBoxLog.debug(column + ": " + value);
        try {
            switch (column) {
                case "infoid":
                    data.setInfoid(value == null ? -1 : (long) value);
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "info":
                    data.setInfo(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getDataValue(InfoInTree data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "infoid":
                return data.getInfoid();
            case "title":
                return data.getTitle();
            case "info":
                return data.getInfo();
        }
        return null;
    }

    public static boolean valid(InfoInTree data) {
        return data != null;
    }

    /*
        get/set
     */
    public long getInfoid() {
        return infoid;
    }

    public InfoInTree setInfoid(long infoid) {
        this.infoid = infoid;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public InfoInTree setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public InfoInTree setInfo(String info) {
        this.info = info;
        return this;
    }

}
