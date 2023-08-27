package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2023-8-27
 * @License Apache License Version 2.0
 */
public class TreeCategory extends BaseData {

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

    protected String category, title, tableName;
    protected boolean isInternal;

    private void init() {
        category = null;
        title = null;
        tableName = null;
        isInternal = false;
    }

    public TreeCategory() {
        init();
    }


    /*
        Static methods
     */
    public static TreeCategory create() {
        return new TreeCategory();
    }

    public static boolean setValue(TreeCategory data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "category":
                    data.setCategory(value == null ? null : (String) value);
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "table_name":
                    data.setTableName(value == null ? null : (String) value);
                    return true;
                case "is_internal":
                    data.setIsInternal(value == null ? false : (boolean) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(TreeCategory data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "category":
                return data.getCategory();
            case "title":
                return data.getTitle();
            case "table_name":
                return data.getTableName();
            case "is_internal":
                return data.isIsInternal();
        }
        return null;
    }

    public static boolean valid(TreeCategory data) {
        return data != null && data.getCategory() != null
                && data.getTitle() != null
                && data.getTableName() != null;
    }

    /*
        get/set
     */
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isIsInternal() {
        return isInternal;
    }

    public void setIsInternal(boolean isInternal) {
        this.isInternal = isInternal;
    }

}
