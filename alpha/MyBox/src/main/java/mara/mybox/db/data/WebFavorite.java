package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2024-8-2
 * @License Apache License Version 2.0
 */
public class WebFavorite extends BaseData {

    protected long addrid;
    protected String address, icon;

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
    public static WebFavorite create() {
        return new WebFavorite();
    }

    public static boolean setValue(WebFavorite data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "addrid":
                    data.setAddrid(value == null ? -1 : (long) value);
                    return true;
                case "address":
                    data.setAddress(value == null ? null : (String) value);
                    return true;
                case "icon":
                    data.setIcon(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(WebFavorite data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "addrid":
                return data.getAddrid();
            case "address":
                return data.getAddress();
            case "icon":
                return data.getIcon();
        }
        return null;
    }

    public static boolean valid(WebFavorite data) {
        return data != null
                && data.getAddress() != null && !data.getAddress().isBlank();
    }

    /*
        get/set
     */
    public long getAddrid() {
        return addrid;
    }

    public void setAddrid(long addrid) {
        this.addrid = addrid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

}
