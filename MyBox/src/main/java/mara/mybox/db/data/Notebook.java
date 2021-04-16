package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-2-28
 * @License Apache License Version 2.0
 */
public class Notebook extends BaseData {

    public static final int RootID = 1;
    public static final String NotebooksSeparater = " > ";
    protected long nbid, owner;
    protected String name, description;

    private void init() {
        nbid = -1;
        owner = 1;
        name = null;
        description = null;
    }

    public Notebook() {
        init();
    }

    public Notebook(long owner, String name) {
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
    public static Notebook create() {
        return new Notebook();
    }

    public static boolean setValue(Notebook data, String column, Object value) {
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

    public static Object getValue(Notebook data, String column) {
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

    public static boolean valid(Notebook data) {
        return data != null
                && data.getName() != null && !data.getName().isBlank()
                && !data.getName().contains(NotebooksSeparater);
    }

    /*
        get/set
     */
    public long getNbid() {
        return nbid;
    }

    public Notebook setNbid(long nbid) {
        this.nbid = nbid;
        return this;
    }

    public long getOwner() {
        return owner;
    }

    public Notebook setOwner(long owner) {
        this.owner = owner;
        return this;
    }

    public String getName() {
        return name;
    }

    public Notebook setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Notebook setDescription(String description) {
        this.description = description;
        return this;
    }

}
