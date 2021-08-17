package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class WebFavorite extends BaseData {

    protected long faid, owner;
    protected String title, address, icon;

    private void init() {
        faid = -1;
        owner = -2;
        title = null;
        address = null;
        icon = null;
    }

    public WebFavorite() {
        init();
    }

    public WebFavorite(long owner, String title, String address) {
        init();
        this.owner = owner;
        this.title = title;
        this.address = address;
    }

    public WebFavorite(long owner, String title, String address, String icon) {
        init();
        this.owner = owner;
        this.title = title;
        this.address = address;
        this.icon = icon;
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
                case "faid":
                    data.setFaid(value == null ? -1 : (long) value);
                    return true;
                case "owner":
                    data.setOwner(value == null ? -1 : (long) value);
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "address":
                    data.setAddress(value == null ? null : (String) value);
                    return true;
                case "icon":
                    data.setIcon(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(WebFavorite data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "faid":
                return data.getFaid();
            case "owner":
                return data.getOwner();
            case "title":
                return data.getTitle();
            case "address":
                return data.getAddress();
            case "icon":
                return data.getIcon();
        }
        return null;
    }

    public static boolean valid(WebFavorite data) {
        return data != null
                && data.getTitle() != null && !data.getTitle().isBlank()
                && data.getAddress() != null && !data.getAddress().isBlank();
    }

    /*
        get/set
     */
    public long getFaid() {
        return faid;
    }

    public void setFaid(long faid) {
        this.faid = faid;
    }

    public long getOwner() {
        return owner;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
