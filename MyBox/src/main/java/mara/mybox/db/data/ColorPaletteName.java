package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-3-31
 * @License Apache License Version 2.0
 */
public class ColorPaletteName extends BaseData {

    protected long cpnid;
    protected String name;

    private void init() {
        cpnid = -1;
        name = null;
    }

    public ColorPaletteName() {
        init();
    }

    public ColorPaletteName(String name) {
        init();
        this.name = name;
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
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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

}
