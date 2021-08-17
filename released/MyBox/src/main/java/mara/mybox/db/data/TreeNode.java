package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TreeNode extends BaseData {

    public static final String NodeSeparater = " > ";
    protected long nodeid, parent;
    protected String title, attribute;

    private void init() {
        nodeid = -1;
        parent = -2;
        title = null;
        attribute = null;
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
        }
        return null;
    }

    public static boolean valid(TreeNode data) {
        return data != null
                && data.getTitle() != null && !data.getTitle().isBlank()
                && !data.getTitle().contains(NodeSeparater);
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

}
