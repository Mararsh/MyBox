package mara.mybox.db.data;

import java.io.File;
import java.util.Date;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxFileTools.getInternalFile;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class InfoNode extends BaseData {

    public static final String RootIdentify = "MyBoxTreeRoot;;;";
    public static final String NodeSeparater = " > ";
    public static final String TimePrefix = "Time:";
    public static final String TagsPrefix = "Tags:";
    public static final String MorePrefix = "MyBoxTreeNodeMore:";
    public static final String TagsSeparater = ";;;";
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
    protected String category, title, value, more;
    protected Date updateTime;

    private void init() {
        nodeid = -1;
        parentid = -2;
        category = null;
        title = null;
        value = null;
        more = null;
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

    public InfoNode copyTo(InfoNode parent) {
        InfoNode node = new InfoNode();
        node.setParentid(parent.getNodeid());
        node.setCategory(parent.getCategory());
        node.setTitle(title);
        node.setValue(value);
        node.setMore(more);
        return node;
    }

    public boolean isRoot() {
        return parentid == nodeid;
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
                case "value":
                    data.setValue(value == null ? null : (String) value);
                    return true;
                case "category":
                    data.setCategory(value == null ? null : (String) value);
                    return true;
                case "more":
                    data.setMore(value == null ? null : (String) value);
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
            case "value":
                return data.getValue();
            case "category":
                return data.getCategory();
            case "more":
                return data.getMore();
            case "update_time":
                return data.getUpdateTime();
        }
        return null;
    }

    public static boolean valid(InfoNode data) {
        return data != null && data.getCategory() != null
                && data.getTitle() != null && !data.getTitle().isBlank()
                && !data.getTitle().contains(NodeSeparater);
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

    public String getValue() {
        return value;
    }

    public InfoNode setValue(String value) {
        this.value = value;
        return this;
    }

    public String getMore() {
        return more;
    }

    public InfoNode setMore(String more) {
        this.more = more;
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
