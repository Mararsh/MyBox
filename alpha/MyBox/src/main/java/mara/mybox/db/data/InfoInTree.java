package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-8-2
 * @License Apache License Version 2.0
 */
public class InfoInTree extends BaseData {

    protected long infoid;
    protected String title, info;

    @Override
    public boolean valid() {
        return valid(this);
    }

    @Override
    public boolean setValue(String column, Object value) {
        return setValue(this, column, value);
    }

    @Override
    public Object getValue(String column) {
        return getValue(this, column);
    }

    /*
        Static methods
     */
    public static InfoInTree create() {
        return new InfoInTree();
    }

    public static boolean setValue(InfoInTree data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "infoid":
                    data.setInfoid(value == null ? -1 : (long) value);
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "info":
                    data.setInfo(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(InfoInTree data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "infoid":
                return data.getInfoid();
            case "title":
                return data.getTitle();
            case "info":
                return data.getInfo();
        }
        return null;
    }

    public static boolean valid(InfoInTree data) {
        return data != null;
    }

    /*
        get/set
     */
    public long getInfoid() {
        return infoid;
    }

    public InfoInTree setInfoid(long infoid) {
        this.infoid = infoid;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public InfoInTree setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public InfoInTree setInfo(String info) {
        this.info = info;
        return this;
    }

}
