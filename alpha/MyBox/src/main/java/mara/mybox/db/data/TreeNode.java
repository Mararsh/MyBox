package mara.mybox.db.data;

import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TreeNode extends BaseData {

    public static final String RootIdentify = "MyBoxTreeRoot;;;";
    public static final String TitleSeparater = " > ";
    public static final String IDPrefix = "ID:";
    public static final String TimePrefix = "Time:";
    public static final String TagsPrefix = "Tags:";
    public static final String TagSeparater = ";;;";
    public static final String ValueSeparater = "_:;MyBoxNodeValue;:_";
    public static final String MoreSeparater = "MyBoxTreeNodeMore:";
    public static final String Root = "Root";

    protected long nodeid, parentid;
    protected String category, title;
    protected Date updateTime;

    private void init() {
        nodeid = -1;
        parentid = -2;
        category = null;
        title = null;
        updateTime = new Date();
    }

    public TreeNode() {
        init();
    }

    public TreeNode(TreeNode parent, String title) {
        init();
        this.parentid = parent.getNodeid();
        this.category = parent.getCategory();
        this.title = title;
    }

    public TreeNode copyIn(TreeNode parent) {
        TreeNode node = new TreeNode();
        node.setParentid(parent.getNodeid());
        node.setCategory(parent.getCategory());
        node.setTitle(title);
        return node;
    }

    public boolean isRoot() {
        return parentid == nodeid;
    }

    public boolean setNodeValue(String column, Object value) {
        if (column == null) {
            return false;
        }
        try {
            switch (column) {
                case "nodeid":
                    nodeid = value == null ? -1 : (long) value;
                    return true;
                case "parentid":
                    parentid = value == null ? -1 : (long) value;
                    return true;
                case "title":
                    title = value == null ? null : (String) value;
                    return true;
                case "category":
                    category = value == null ? null : (String) value;
                    return true;
                case "update_time":
                    updateTime = value == null ? null : (Date) value;
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public Object getNodeValue(String column) {
        if (column == null) {
            return null;
        }
        switch (column) {
            case "nodeid":
                return nodeid;
            case "parentid":
                return parentid;
            case "title":
                return title;
            case "category":
                return category;
            case "update_time":
                return updateTime;
        }
        return null;
    }

    @Override
    public boolean valid() {
        return category != null
                && title != null && !title.isBlank()
                && !title.contains(TitleSeparater);
    }

    /*
        Static methods
     */
    public static TreeNode create() {
        return new TreeNode();
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

    public long getParentid() {
        return parentid;
    }

    public TreeNode setParentid(long parentid) {
        this.parentid = parentid;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public TreeNode setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public TreeNode setCategory(String category) {
        this.category = category;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public TreeNode setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

}
