package mara.mybox.db.data;

import java.io.File;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxFileTools.getInternalFile;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TreeNode extends BaseData {

    public static final String NodeSeparater = " > ";
    public static final String Root = "Root";
    public static final String WebFavorite = "WebFavorite";
    public static final String Notebook = "Notebook";
    public static final String JShellCode = "JShellCode";
    public static final String SQL = "SQL";
    public static final String JavaScript = "JavaScript";
    public static final String InformationInTree = "InformationInTree";

    protected long nodeid, parent;
    protected String category, title, attribute, more;

    private void init() {
        nodeid = -1;
        parent = -2;
        title = null;
        attribute = null;
        more = null;
    }

    public TreeNode() {
        init();
    }

    public TreeNode(long parent, String title) {
        init();
        this.parent = parent;
        this.title = title;
    }

    public TreeNode(long parent, String title, String attribute) {
        init();
        this.parent = parent;
        this.title = title;
        this.attribute = attribute;
    }

    public boolean isRoot() {
        return parent == nodeid;
    }

    /*
        Static methods
     */
    public static TreeNode create() {
        return new TreeNode();
    }

    public static boolean setValue(TreeNode data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "nodeid":
                    data.setNodeid(value == null ? -1 : (long) value);
                    return true;
                case "parent":
                    data.setParent(value == null ? -1 : (long) value);
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "attribute":
                    data.setAttribute(value == null ? null : (String) value);
                    return true;
                case "category":
                    data.setCategory(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(TreeNode data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "nodeid":
                return data.getNodeid();
            case "parent":
                return data.getParent();
            case "title":
                return data.getTitle();
            case "attribute":
                return data.getAttribute();
            case "category":
                return data.getCategory();
        }
        return null;
    }

    public static boolean valid(TreeNode data) {
        return data != null
                && data.getTitle() != null && !data.getTitle().isBlank()
                && !data.getTitle().contains(NodeSeparater);
    }

    public static File exampleFile(String category) {
        String lang = Languages.isChinese() ? "zh" : "en";
        if (WebFavorite.equals(category)) {
            return getInternalFile("/data/db/WebFavorites_Examples_" + lang + ".txt",
                    "data", "WebFavorites_Examples_" + lang + ".txt");

        } else if (Notebook.equals(category)) {
            return getInternalFile("/data/db/Notes_Examples_" + lang + ".txt",
                    "data", "Notes_Examples_" + lang + ".txt");

        } else if (JShellCode.equals(category)) {
            return getInternalFile("/data/db/JShell_Examples_" + lang + ".txt",
                    "data", "JShell_Examples_" + lang + ".txt", true);

        } else if (SQL.equals(category)) {
            return getInternalFile("/data/db/Sql_Examples_" + lang + ".txt",
                    "data", "Sql_Examples_" + lang + ".txt", true);

        } else if (JavaScript.equals(category)) {
            return getInternalFile("/data/db/JavaScript_Examples_" + lang + ".txt",
                    "data", "JavaScript_Examples_" + lang + ".txt", true);

        } else if (InformationInTree.equals(category)) {
            return getInternalFile("/data/db/Tree_Examples_" + lang + ".txt",
                    "data", "Tree_Examples_" + lang + ".txt", true);

        } else {
            return null;
        }
    }

    /*
        get/set
     */
    public long getNodeid() {
        return nodeid;
    }

    public TreeNode setNodeid(long nodeid) {
        this.nodeid = nodeid;
        return this;
    }

    public long getParent() {
        return parent;
    }

    public TreeNode setParent(long parent) {
        this.parent = parent;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public TreeNode setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getAttribute() {
        return attribute;
    }

    public TreeNode setAttribute(String attribute) {
        this.attribute = attribute;
        return this;
    }

    public String getMore() {
        return more;
    }

    public TreeNode setMore(String more) {
        this.more = more;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public TreeNode setCategory(String category) {
        this.category = category;
        return this;
    }

}
