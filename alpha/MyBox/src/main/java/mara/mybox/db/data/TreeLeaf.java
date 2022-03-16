package mara.mybox.db.data;

import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TreeLeaf extends BaseData {

    public static final String TimePrefix = "Time:";
    protected long leafid, parentid;
    protected String category, name, value, more;
    protected Date time;

    private void init() {
        leafid = -1;
        parentid = -2;
        name = null;
        value = null;
        more = null;
        time = new Date();
    }

    public TreeLeaf() {
        init();
    }


    /*
        Static methods
     */
    public static TreeLeaf create() {
        return new TreeLeaf();
    }

    public static boolean setValue(TreeLeaf data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "leafid":
                    data.setLeafid(value == null ? -1 : (long) value);
                    return true;
                case "parentid":
                    data.setParentid(value == null ? -1 : (long) value);
                    return true;
                case "category":
                    data.setCategory(value == null ? null : (String) value);
                    return true;
                case "name":
                    data.setName(value == null ? null : (String) value);
                    return true;
                case "value":
                    data.setValue(value == null ? null : (String) value);
                    return true;
                case "more":
                    data.setMore(value == null ? null : (String) value);
                    return true;
                case "update_time":
                    data.setTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(TreeLeaf data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "leafid":
                return data.getLeafid();
            case "parentid":
                return data.getParentid();
            case "category":
                return data.getCategory();
            case "name":
                return data.getName();
            case "value":
                return data.getValue();
            case "more":
                return data.getMore();
            case "update_time":
                return data.getTime();
        }
        return null;
    }

    public static boolean valid(TreeLeaf data) {
        return data != null && data.getName() != null && !data.getName().isBlank();
    }

    /*
        get/set
     */
    public long getLeafid() {
        return leafid;
    }

    public TreeLeaf setLeafid(long leafid) {
        this.leafid = leafid;
        return this;
    }

    public long getParentid() {
        return parentid;
    }

    public TreeLeaf setParentid(long parentid) {
        this.parentid = parentid;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public TreeLeaf setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getName() {
        return name;
    }

    public TreeLeaf setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public TreeLeaf setValue(String value) {
        this.value = value;
        return this;
    }

    public Date getTime() {
        return time;
    }

    public TreeLeaf setTime(Date time) {
        this.time = time;
        return this;
    }

    public String getMore() {
        return more;
    }

    public TreeLeaf setMore(String more) {
        this.more = more;
        return this;
    }

}
