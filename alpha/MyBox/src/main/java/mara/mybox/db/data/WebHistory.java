/*
 * Apache License Version 2.0
 */
package mara.mybox.db.data;

import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-5-4
 * @License Apache License Version 2.0
 */
public class WebHistory extends BaseData {

    private long whid;
    private String address, title, icon;
    private Date visitTime;

    private void init() {
        whid = -1;
    }

    public WebHistory() {
        init();
    }

    /*
        Static methods
     */
    public static WebHistory create() {
        return new WebHistory();
    }

    public static boolean setValue(WebHistory data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "whid":
                    data.setWhid(value == null ? -1 : (long) value);
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
                case "visit_time":
                    data.setVisitTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(WebHistory data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "whid":
                return data.getWhid();
            case "title":
                return data.getTitle();
            case "address":
                return data.getAddress();
            case "icon":
                return data.getIcon();
            case "visit_time":
                return data.getVisitTime();
        }
        return null;
    }

    public static boolean valid(WebHistory data) {
        return data != null
                && data.getVisitTime() != null
                && data.getAddress() != null && !data.getAddress().isBlank();
    }

    /*
        get/set
     */
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getWhid() {
        return whid;
    }

    public void setWhid(long whid) {
        this.whid = whid;
    }

    public Date getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(Date visitTime) {
        this.visitTime = visitTime;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

}
