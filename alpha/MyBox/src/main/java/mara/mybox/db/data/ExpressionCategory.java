package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-10-12
 * @License Apache License Version 2.0
 */
public class ExpressionCategory extends BaseData {

    public static final int RootID = 1;
    public static final String CategorySeparater = " > ";
    protected long nbid, owner;
    protected String name, description;

    private void init() {
        nbid = -1;
        owner = 1;
        name = null;
        description = null;
    }

    public ExpressionCategory() {
        init();
    }

    public ExpressionCategory(long owner, String name) {
        init();
        this.owner = owner;
        this.name = name;
    }

    public boolean isRoot() {
        return nbid == RootID;
    }

    /*
        Static methods
     */
    public static ExpressionCategory create() {
        return new ExpressionCategory();
    }

    public static boolean setValue(ExpressionCategory data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "nbid":
                    data.setNbid(value == null ? -1 : (long) value);
                    return true;
                case "owner":
                    data.setOwner(value == null ? -1 : (long) value);
                    return true;
                case "name":
                    data.setName(value == null ? null : (String) value);
                    return true;
                case "description":
                    data.setDescription(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(ExpressionCategory data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "nbid":
                return data.getNbid();
            case "owner":
                return data.getOwner();
            case "name":
                return data.getName();
            case "description":
                return data.getDescription();
        }
        return null;
    }

    public static boolean valid(ExpressionCategory data) {
        return data != null
                && data.getName() != null && !data.getName().isBlank()
                && !data.getName().contains(CategorySeparater);
    }

    /*
        get/set
     */
    public long getNbid() {
        return nbid;
    }

    public ExpressionCategory setNbid(long nbid) {
        this.nbid = nbid;
        return this;
    }

    public long getOwner() {
        return owner;
    }

    public ExpressionCategory setOwner(long owner) {
        this.owner = owner;
        return this;
    }

    public String getName() {
        return name;
    }

    public ExpressionCategory setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ExpressionCategory setDescription(String description) {
        this.description = description;
        return this;
    }

}
