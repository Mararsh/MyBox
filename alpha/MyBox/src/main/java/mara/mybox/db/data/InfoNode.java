package mara.mybox.db.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ImageScopeTools;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.Data2DDefinitionController;
import mara.mybox.controller.DatabaseSqlController;
import mara.mybox.controller.ImageMaterialController;
import mara.mybox.controller.ImageScopeController;
import mara.mybox.controller.InfoTreeManageController;
import mara.mybox.controller.JShellController;
import mara.mybox.controller.JavaScriptController;
import mara.mybox.controller.JexlController;
import mara.mybox.controller.MathFunctionController;
import mara.mybox.controller.NotesController;
import mara.mybox.controller.RowFilterController;
import mara.mybox.controller.WebFavoritesController;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2DTools;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import static mara.mybox.fxml.FxFileTools.getInternalFile;
import mara.mybox.fxml.FxTask;
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
    public static final String IDPrefix = "ID:";
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
    public static final String ImageScope = "ImageScope";
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

    public boolean equal(InfoNode node) {
        return equal(this, node);
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
            String lang = Languages.embedFileLang();
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
                case InfoNode.ImageScope:
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

    public static String majorInfo(InfoNode node) {
        try {
            if (node == null || node.getCategory() == null) {
                return null;
            }
            Map<String, String> values = parseInfo(node);
            if (values == null) {
                return node.getInfo();
            }
            switch (node.getCategory()) {
                case InfoNode.WebFavorite: {
                    String address = values.get("Address");
                    return address == null || address.isBlank() ? null : address.trim();
                }
                case InfoNode.JEXLCode: {
                    String script = values.get("Script");
                    return script == null || script.isBlank() ? null : script.trim();
                }
                case InfoNode.RowFilter: {
                    String script = values.get("Script");
                    return script == null || script.isBlank() ? null : script.trim();
                }
                case InfoNode.MathFunction: {
                    String exp = values.get("Expression");
                    return exp == null || exp.isBlank() ? null : exp.trim();
                }
            }
            return node.getInfo();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String nodeHtml(FxTask task, BaseController controller,
            InfoNode node, String title) {
        if (node == null) {
            return null;
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

        return table.div() + "<BR>" + infoHtml(task, controller,
                node.getCategory(), node.getInfo(), true, false);
    }

    public static String infoHtml(FxTask task, BaseController controller,
            String category, String s, boolean showIcon, boolean singleNotIn) {
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
                                String base64 = FxImageTools.base64(null, new File(icon), "png");
                                if (base64 != null) {
                                    html += "<img src=\"data:image/png;base64," + base64 + "\" width=" + 40 + " >";
                                }
                            } catch (Exception e) {
                            }
                        }
                        html += address + "</A>\n";
                    }
                }
                break;
            }
            case InfoNode.Data2DDefinition: {
                DataFileCSV csv = Data2DTools.definitionFromXML(task, controller, s);
                if (csv != null) {
                    html = Data2DTools.definitionToHtml(csv);
                }
                break;
            }
            case InfoNode.ImageScope: {
                ImageScope scope = ImageScopeTools.fromXML(task, controller, s);
                if (scope != null) {
                    html = ImageScopeTools.toHtml(scope);
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
            case InfoNode.Data2DDefinition:
            case InfoNode.ImageScope: {
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

    public static String infoJson(FxTask task, BaseController controller,
            String category, String s, String prefix) {
        if (s == null || s.isBlank()) {
            return "";
        }
        String json = "";
        switch (category) {
            case InfoNode.Data2DDefinition: {
                DataFileCSV csv = Data2DTools.definitionFromXML(task, controller, s);
                if (csv != null) {
                    json = prefix + ",\n"
                            + Data2DTools.definitionToJSON(csv, true, prefix);
                }
                break;
            }
            case InfoNode.ImageScope: {
                ImageScope scope = ImageScopeTools.fromXML(task, controller, s);
                if (scope != null) {
                    json = prefix + ",\n"
                            + ImageScopeTools.toJSON(scope, prefix);
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
                        json += prefix + ",\n"
                                + prefix + "\"" + message(key) + "\": " + JsonTools.encode(v);
                    }
                }
                break;
        }
        return json;
    }

    public static boolean equal(InfoNode node1, InfoNode node2) {
        if (node1 == null || node2 == null) {
            return false;
        }
        return node1.getNodeid() == node2.getNodeid();
    }

    public static boolean isWebFavorite(String category) {
        return Languages.matchIgnoreCase(category, InfoNode.WebFavorite);
    }

    public static InfoTreeManageController openManager(String category) {
        if (category == null) {
            return null;
        }
        switch (category) {
            case InfoNode.WebFavorite:
                return WebFavoritesController.oneOpen();
            case InfoNode.Notebook:
                return NotesController.oneOpen();
            case InfoNode.JShellCode:
                return JShellController.open("");
            case InfoNode.SQL:
                return DatabaseSqlController.open(false);
            case InfoNode.JavaScript:
                return JavaScriptController.loadScript("");
            case InfoNode.InformationInTree:
                return InfoTreeManageController.oneOpen();
            case InfoNode.JEXLCode:
                return JexlController.open("", "", "");
            case InfoNode.RowFilter:
                return RowFilterController.open();
            case InfoNode.MathFunction:
                return MathFunctionController.open();
            case InfoNode.ImageMaterial:
                return ImageMaterialController.open();
            case InfoNode.Data2DDefinition:
                return Data2DDefinitionController.open();
            case InfoNode.ImageScope:
                return ImageScopeController.open();

        }
        return null;
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
