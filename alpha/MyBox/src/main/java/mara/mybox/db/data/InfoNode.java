package mara.mybox.db.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.controller.HtmlTableController;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2DTools;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxFileTools.getInternalFile;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.JsonTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class InfoNode extends BaseData {

    public static final String RootIdentify = "MyBoxTreeRoot;;;";
    public static final String TitleSeparater = " > ";
    public static final String TimePrefix = "Time:";
    public static final String TagsPrefix = "Tags:";
    public static final String TagSeparater = ";;;";
    public static final String ValueSeparater = "_:;MyBoxNodeValue;:_";
    public static final String MoreSeparater = "MyBoxTreeNodeMore:";
    public static final String Root = "Root";
    public static final String InformationInTree = "InformationInTree";
    public static final String Notebook = "Notebook";
    public static final String WebFavorite = "WebFavorite";
    public static final String SQL = "SQL";
    public static final String JShellCode = "JShellCode";
    public static final String JEXLCode = "JEXLCode";
    public static final String JavaScript = "JavaScript";
    public static final String MathFunction = "MathFunction";
    public static final String RowFilter = "RowFilter";
    public static final String ImageMaterial = "ImageMaterial";
    public static final String Data2DDefinition = "Data2DDefinition";

    protected long nodeid, parentid;
    protected String category, title, info;
    protected Date updateTime;

    private void init() {
        nodeid = -1;
        parentid = -2;
        category = null;
        title = null;
        info = null;
        updateTime = new Date();
    }

    public InfoNode() {
        init();
    }

    public InfoNode(InfoNode parent, String title) {
        init();
        this.parentid = parent.getNodeid();
        this.category = parent.getCategory();
        this.title = title;
    }

    public InfoNode copyIn(InfoNode parent) {
        InfoNode node = new InfoNode();
        node.setParentid(parent.getNodeid());
        node.setCategory(parent.getCategory());
        node.setTitle(title);
        node.setInfo(info);
        return node;
    }

    public boolean isRoot() {
        return parentid == nodeid;
    }

    public String getValue() {
        if (info == null) {
            return null;
        }
        return info.replaceAll(ValueSeparater, " ").trim();
    }

    public String getIcon() {
        if (category == null || !category.equals(InfoNode.WebFavorite)) {
            return null;
        }
        Map<String, String> values = parseInfo(this);
        return values.get("Icon");
    }


    /*
        Static methods
     */
    public static InfoNode create() {
        return new InfoNode();
    }

    public static boolean setValue(InfoNode data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "nodeid":
                    data.setNodeid(value == null ? -1 : (long) value);
                    return true;
                case "parentid":
                    data.setParentid(value == null ? -1 : (long) value);
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "info":
                    data.setInfo(value == null ? null : encodeInfo(data.getCategory(), (String) value));
                    return true;
                case "category":
                    data.setCategory(value == null ? null : (String) value);
                    return true;
                case "update_time":
                    data.setUpdateTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(InfoNode data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "nodeid":
                return data.getNodeid();
            case "parentid":
                return data.getParentid();
            case "title":
                return data.getTitle();
            case "info":
                return encodeInfo(data);
            case "category":
                return data.getCategory();
            case "update_time":
                return data.getUpdateTime();
        }
        return null;
    }

    public static boolean valid(InfoNode data) {
        return data != null && data.getCategory() != null
                && data.getTitle() != null && !data.getTitle().isBlank()
                && !data.getTitle().contains(TitleSeparater);
    }

    public static File exampleFile(String category) {
        if (null == category) {
            return null;
        } else {
            String lang = Languages.getLangName();
            return getInternalFile("/data/examples/" + category + "_Examples_" + lang + ".txt",
                    "data", category + "_Examples_" + lang + ".txt", true);
        }
    }

    public static Map<String, String> parseInfo(InfoNode node) {
        if (node == null) {
            return new LinkedHashMap<>();
        }
        return parseInfo(node.getCategory(), node.getInfo());
    }

    public static Map<String, String> parseInfo(String category, String s) {
        Map<String, String> values = new LinkedHashMap<>();
        try {
            if (category == null) {
                return values;
            }
            String info = s == null || s.isBlank() ? null : s.trim();
            switch (category) {
                case InfoNode.WebFavorite:
                    values.put("Address", null);
                    values.put("Icon", null);
                    if (info != null) {
                        if (info.contains(ValueSeparater)) {
                            String[] ss = info.split(ValueSeparater);
                            if (ss.length > 0) {
                                values.put("Address", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                values.put("Icon", ss[1].trim());
                            }
                        } else if (info.contains(MoreSeparater)) {
                            String[] ss = info.split(MoreSeparater);
                            if (ss.length > 0) {
                                values.put("Address", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                values.put("Icon", ss[1].trim());
                            }
                        } else {
                            values.put("Address", info);
                        }
                    }
                    break;
                case InfoNode.Notebook:
                    values.put("Note", info);
                    break;
                case InfoNode.JShellCode:
                    values.put("Codes", info);
                    break;
                case InfoNode.SQL:
                    values.put("SQL", info);
                    break;
                case InfoNode.JavaScript:
                    values.put("Script", info);
                    break;
                case InfoNode.InformationInTree:
                    values.put("Info", info);
                    break;
                case InfoNode.JEXLCode:
                    values.put("Script", null);
                    values.put("Context", null);
                    values.put("Parameters", null);
                    if (info != null) {
                        String[] ss = null;
                        if (info.contains(ValueSeparater)) {
                            ss = info.split(ValueSeparater);
                        } else if (info.contains(MoreSeparater)) {
                            ss = info.split(MoreSeparater);
                        }
                        if (ss != null) {
                            if (ss.length > 0) {
                                values.put("Script", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                values.put("Context", ss[1].trim());
                            }
                            if (ss.length > 2) {
                                values.put("Parameters", ss[2].trim());
                            }
                        } else {
                            values.put("Script", info);
                        }
                    }
                    break;
                case InfoNode.RowFilter:
                    values.put("Script", null);
                    values.put("Condition", "true");
                    values.put("Maximum", "-1");
                    if (info != null) {
                        if (info.contains(ValueSeparater)) {
                            String[] ss = info.split(ValueSeparater);
                            if (ss.length > 0) {
                                values.put("Script", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                values.put("Condition", ss[1].trim());
                            }
                            if (ss.length > 2) {
                                values.put("Maximum", ss[2].trim());
                            }
                        } else if (info.contains(MoreSeparater)) {
                            String[] ss = info.split(MoreSeparater);
                            values.put("Script", ss[0].trim());
                            if (ss.length > 1) {
                                ss = ss[1].split(";;;");
                                if (ss.length > 0) {
                                    values.put("Condition", ss[0].trim());
                                }
                                if (ss.length > 1) {
                                    values.put("Maximum", ss[1].trim());
                                }
                            }
                        } else {
                            values.put("Script", info);
                        }
                    }
                    break;
                case InfoNode.MathFunction:
                    values.put("MathFunctionName", null);
                    values.put("Variables", null);
                    values.put("Expression", null);
                    values.put("FunctionDomain", null);
                    if (info != null) {
                        if (info.contains(ValueSeparater)) {
                            String[] ss = info.split(ValueSeparater);
                            if (ss.length > 0) {
                                values.put("MathFunctionName", ss[0].trim());
                            }
                            if (ss.length > 1) {
                                values.put("Variables", ss[1].trim());
                            }
                            if (ss.length > 2) {
                                values.put("Expression", ss[2].trim());
                            }
                            if (ss.length > 3) {
                                values.put("FunctionDomain", ss[3].trim());
                            }
                        } else {
                            String prefix = "Names:::";
                            if (info.startsWith(prefix)) {
                                info = info.substring(prefix.length());
                                int pos = info.indexOf("\n");
                                String names;
                                if (pos >= 0) {
                                    names = info.substring(0, pos);
                                    info = info.substring(pos);
                                } else {
                                    names = info;
                                    info = null;
                                }
                                pos = names.indexOf(",");
                                if (pos >= 0) {
                                    values.put("MathFunctionName", names.substring(0, pos));
                                    String vs = names.substring(pos).trim();
                                    if (vs.length() > 0) {
                                        values.put("Variables", vs.substring(1));
                                    }
                                } else {
                                    values.put("MathFunctionName", names);
                                }
                            }
                            if (info != null && info.contains(MoreSeparater)) {
                                String[] ss = info.split(MoreSeparater);
                                values.put("Expression", ss[0].trim());
                                if (ss.length > 1) {
                                    values.put("FunctionDomain", ss[1].trim());
                                }
                            } else {
                                values.put("Expression", info);
                            }
                        }
                    }
                    break;
                case InfoNode.ImageMaterial:
                    values.put("Value", info);
                    break;
                case InfoNode.Data2DDefinition:
                    values.put("XML", info);
                    break;
                default:
                    values.put("Info", info);
                    break;
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return values;
    }

    public static String encodeInfo(InfoNode node) {
        if (node == null) {
            return null;
        }
        return encodeInfo(node.getCategory(), node.getInfo());
    }

    public static String encodeInfo(String category, String s) {
        String info = s == null || s.isBlank() ? null : s.trim();
        try {
            if (category == null || info == null) {
                return info;
            }
            Map<String, String> values = parseInfo(category, info);
            if (values == null) {
                return info;
            }
            switch (category) {
                case InfoNode.WebFavorite: {
                    String address = values.get("Address");
                    String icon = values.get("Icon");
                    if (icon == null || icon.isBlank()) {
                        return address == null || address.isBlank() ? null : address.trim();
                    }
                    return (address == null ? "" : address.trim()) + ValueSeparater + "\n"
                            + icon.trim();
                }
                case InfoNode.JEXLCode: {
                    String script = values.get("Script");
                    String context = values.get("Context");
                    String parameters = values.get("Parameters");
                    if ((parameters == null || parameters.isBlank())
                            && (context == null || context.isBlank())) {
                        return script == null || script.isBlank() ? null : script.trim();
                    }
                    return (script == null ? "" : script.trim()) + ValueSeparater + "\n"
                            + (context == null ? "" : context.trim()) + ValueSeparater + "\n"
                            + (parameters == null ? "" : parameters.trim());
                }
                case InfoNode.RowFilter: {
                    String script = values.get("Script");
                    String condition = values.get("Condition");
                    String max = values.get("Maximum");
                    if ((condition == null || condition.isBlank())
                            && (max == null || max.isBlank())) {
                        return script == null || script.isBlank() ? null : script.trim();
                    }
                    long maxl = -1;
                    try {
                        maxl = Long.parseLong(max);
                    } catch (Exception e) {
                    }
                    if ("true".equalsIgnoreCase(condition) && maxl <= 0) {
                        return script == null || script.isBlank() ? null : script.trim();
                    }
                    return (script == null ? "" : script.trim()) + ValueSeparater + "\n"
                            + (condition == null ? "" : condition.trim()) + ValueSeparater + "\n"
                            + (max == null ? "" : max.trim());
                }
                case InfoNode.MathFunction: {
                    String name = values.get("MathFunctionName");
                    String variables = values.get("Variables");
                    String exp = values.get("Expression");
                    String domain = values.get("FunctionDomain");
                    if ((name == null || name.isBlank())
                            && (variables == null || variables.isBlank())
                            && (domain == null || domain.isBlank())) {
                        return exp == null || exp.isBlank() ? null : exp.trim();
                    }
                    return (name == null ? "" : name.trim()) + ValueSeparater + "\n"
                            + (variables == null ? "" : variables.trim()) + ValueSeparater + "\n"
                            + (exp == null ? "" : exp.trim()) + ValueSeparater + "\n"
                            + (domain == null ? "" : domain.trim());
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return info;
    }

    public static void view(InfoNode node, String title) {
        if (node == null) {
            return;
        }
        List<String> names = new ArrayList<>();
        names.addAll(Arrays.asList(message("Name"), message("Value")));
        StringTable table = new StringTable(names, title);
        List<String> row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Category"), message(node.getCategory())));
        table.add(row);

        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("ID"), node.getNodeid() >= 0 ? node.getNodeid() + "" : message("NewItem")));
        table.add(row);

        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Title"), node.getTitle()));
        table.add(row);

        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("UpdateTime"), DateTools.datetimeToString(node.getUpdateTime())));
        table.add(row);

        String html = table.div() + "<BR>"
                + infoHtml(node.getCategory(), node.getInfo(), true, false);

        HtmlTableController.open(title, html);
    }

    public static String infoHtml(String category, String s, boolean showIcon, boolean singleNotIn) {
        if (s == null || s.isBlank()) {
            return "";
        }
        String html = "";
        switch (category) {
            case InfoNode.Notebook:
                html = s;
                break;
            case InfoNode.WebFavorite: {
                Map<String, String> values = InfoNode.parseInfo(category, s);
                if (values != null) {
                    String address = values.get("Address");
                    if (address != null && !address.isBlank()) {
                        html = "<A href=\"" + address + "\">";
                        String icon = values.get("Icon");
                        if (showIcon && icon != null && !icon.isBlank()) {
                            try {
                                html += "<IMG src=\"" + new File(icon).toURI().toString() + "\" width=40/>";
                            } catch (Exception e) {
                            }
                        }
                        html += address + "</A>\n";
                    }
                }
                break;
            }
            case InfoNode.Data2DDefinition: {
                DataFileCSV csv = Data2DTools.fromXML(s);
                if (csv != null) {
                    StringTable attrTable = new StringTable();
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(message("DataName"), csv.getDataName()));
                    attrTable.add(row);
                    row = new ArrayList<>();
                    row.addAll(Arrays.asList(message("DecimalScale"), csv.getScale() + ""));
                    attrTable.add(row);
                    row = new ArrayList<>();
                    row.addAll(Arrays.asList(message("MaxRandom"), csv.getMaxRandom() + ""));
                    attrTable.add(row);
                    row = new ArrayList<>();
                    String comments = csv.getComments();
                    if (comments != null && !comments.isBlank()) {
                        row.addAll(Arrays.asList(message("Description"),
                                "<PRE><CODE>" + comments + "</CODE></PRE>"));
                    }
                    attrTable.add(row);
                    html = attrTable.div();
                    String columnsHtml = Data2DTools.toHtml(null, csv.getColumns());
                    if (columnsHtml != null && !columnsHtml.isBlank()) {
                        html += "<BR>" + columnsHtml;
                    }
                }
                break;
            }
            default: {
                Map<String, String> values = parseInfo(category, s);
                if (values != null) {
                    StringTable table = new StringTable();
                    String pv = s;
                    for (String key : values.keySet()) {
                        String v = values.get(key);
                        if (v == null || v.isBlank()) {
                            continue;
                        }
                        pv = "<PRE><CODE>" + v + "</CODE></PRE>";
                        List<String> row = new ArrayList<>();
                        row.addAll(Arrays.asList(message(key), pv));
                        table.add(row);
                    }
                    if (!table.isEmpty()) {
                        if (singleNotIn && table.size() == 1) {
                            html = pv;
                        } else {
                            html = table.div();
                        }
                    }
                }
                break;
            }
        }
        return html;
    }

    public static String infoXml(String category, String s, String prefix) {
        if (s == null || s.isBlank()) {
            return "";
        }
        String xml = "";
        switch (category) {
            case InfoNode.Data2DDefinition: {
                xml = s + "\n";
                break;
            }
            default:
                Map<String, String> values = parseInfo(category, s);
                if (values != null) {
                    for (String key : values.keySet()) {
                        String v = values.get(key);
                        if (v == null || v.isBlank()) {
                            continue;
                        }
                        String name = message(key);
                        xml += prefix + "<" + name + ">\n"
                                + "<![CDATA[" + v + "]]>\n"
                                + prefix + "</" + name + ">\n";
                    }
                }
                break;
        }
        return xml;
    }

    public static String infoJson(String category, String s, String prefix) {
        if (s == null || s.isBlank()) {
            return "";
        }
        String json = "";
        switch (category) {
            case InfoNode.Data2DDefinition: {
                DataFileCSV csv = Data2DTools.fromXML(s);
                if (csv != null) {
                    json += ",\n" + prefix + "\"" + message("DataName") + "\": "
                            + JsonTools.encode(csv.getDataName());
                    json += ",\n" + prefix + "\"" + message("DecimalScale") + "\": "
                            + csv.getScale();
                    json += ",\n" + prefix + "\"" + message("MaxRandom") + "\": "
                            + csv.getMaxRandom();
                    String comments = csv.getComments();
                    if (comments != null && !comments.isBlank()) {
                        json += ",\n" + prefix + "\"" + message("Description") + "\": "
                                + JsonTools.encode(comments);
                    }
                    List<Data2DColumn> columns = csv.getColumns();
                    if (columns != null && !columns.isEmpty()) {
                        json += ",\n" + prefix + "\"" + message("Columns") + "\": [\n";
                        String pprefix = prefix + "    ";
                        String comma = "";
                        for (ColumnDefinition column : columns) {
                            json += comma + pprefix + "{\n"
                                    + pprefix + "\"" + message("Column") + "\": "
                                    + JsonTools.encode(column.getColumnName()) + ",\n";
                            json += pprefix + "\"" + message("Type") + "\": "
                                    + JsonTools.encode(column.getType().name()) + ",\n";
                            json += pprefix + "\"" + message("Length") + "\": "
                                    + column.getLength() + ",\n";
                            json += pprefix + "\"" + message("Width") + "\": "
                                    + column.getWidth() + ",\n";
                            String v = column.getFormatDisplay();
                            if (v != null && !v.isBlank()) {
                                json += pprefix + "\"" + message("Format") + "\": "
                                        + JsonTools.encode(v) + ",\n";
                            }
                            json += pprefix + "\"" + message("NotNull") + "\": "
                                    + column.isNotNull() + ",\n";
                            json += pprefix + "\"" + message("Editable") + "\": "
                                    + column.isEditable() + ",\n";
                            json += pprefix + "\"" + message("PrimaryKey") + "\": "
                                    + column.isIsPrimaryKey() + ",\n";
                            json += pprefix + "\"" + message("AutoGenerated") + "\": "
                                    + column.isAuto() + ",\n";
                            v = column.getDefaultValue();
                            if (v != null && !v.isBlank()) {
                                json += pprefix + "\"" + message("DefaultValue") + "\": "
                                        + JsonTools.encode(v) + ",\n";
                            }
                            json += pprefix + "\"" + message("Color") + "\": "
                                    + JsonTools.encode(column.getColor().toString()) + ",\n";
                            json += pprefix + "\"" + message("ToInvalidValue") + "\": "
                                    + JsonTools.encode(column.getInvalidAs().name()) + ",\n";
                            json += pprefix + "\"" + message("DecimalScale") + "\": "
                                    + column.getScale() + ",\n";
                            json += pprefix + "\"" + message("Century") + "\": "
                                    + column.getCentury() + ",\n";
                            json += pprefix + "\"" + message("FixTwoDigitYears") + "\": "
                                    + column.isFixTwoDigitYear();
                            v = column.getDescription();
                            if (v != null && !v.isBlank()) {
                                json += ",\n" + pprefix + "\"" + message("Description") + "\": "
                                        + JsonTools.encode(v) + "\n";
                            } else {
                                json += "\n";
                            }
                            json += pprefix + "}\n";
                            comma = pprefix + ",\n";
                        }
                        json += prefix + "]\n";
                    }
                }
                break;
            }
            default:
                Map<String, String> values = parseInfo(category, s);
                if (values != null) {
                    for (String key : values.keySet()) {
                        String v = values.get(key);
                        if (v == null || v.isBlank()) {
                            continue;
                        }
                        json += ",\n" + prefix + "\"" + message(key) + "\": " + JsonTools.encode(v);
                    }
                }
                break;
        }
        return json;
    }

    /*
        get/set
     */
    public long getNodeid() {
        return nodeid;
    }

    public InfoNode setNodeid(long nodeid) {
        this.nodeid = nodeid;
        return this;
    }

    public long getParentid() {
        return parentid;
    }

    public InfoNode setParentid(long parentid) {
        this.parentid = parentid;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public InfoNode setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public InfoNode setInfo(String info) {
        this.info = info;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public InfoNode setCategory(String category) {
        this.category = category;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public InfoNode setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

}
