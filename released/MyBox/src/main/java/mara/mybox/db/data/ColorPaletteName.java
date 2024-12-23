package mara.mybox.db.data;

import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-3-31
 * @License Apache License Version 2.0
 */
public class ColorPaletteName extends BaseData {

    protected long cpnid;
    protected String name;
    protected Date visitTime;

    private void init() {
        cpnid = -1;
        name = null;
        visitTime = new Date();
    }

    public ColorPaletteName() {
        init();
    }

    public ColorPaletteName(String name) {
        init();
        this.name = name;
    }

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
    public static ColorPaletteName create() {
        return new ColorPaletteName();
    }

    public static boolean setValue(ColorPaletteName data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "cpnid":
                    data.setCpnid(value == null ? -1 : (long) value);
                    return true;
                case "palette_name":
                    data.setName(value == null ? null : (String) value);
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

    public static Object getValue(ColorPaletteName data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "cpnid":
                return data.getCpnid();
            case "palette_name":
                return data.getName();
            case "visit_time":
                return data.getVisitTime();
        }
        return null;
    }

    public static boolean valid(ColorPaletteName data) {
        return data != null
                && data.getName() != null && !data.getName().isBlank();
    }

    /*
        get/set
     */
    public long getCpnid() {
        return cpnid;
    }

    public void setCpnid(long cpnid) {
        this.cpnid = cpnid;
    }

    public String getName() {
        return name;
    }

    public ColorPaletteName setName(String name) {
        this.name = name;
        return this;
    }

    public Date getVisitTime() {
        return visitTime;
    }

    public ColorPaletteName setVisitTime(Date visitTime) {
        this.visitTime = visitTime;
        return this;
    }

}
