package mara.mybox.db.data;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;

/**
 * @Author Mara
 * @CreateDate 2021-3-31
 * @License Apache License Version 2.0
 */
public class ColorPalette extends BaseData {

    protected long cpid, paletteid;
    protected int colorValue;
    protected String name, description;
    protected ColorData data;
    protected float orderNumber;

    private void init() {
        cpid = -1;
        paletteid = 1;
        name = null;
        colorValue = -1;
        data = null;
    }

    public ColorPalette() {
        init();
    }

    public ColorPalette(long paletteid, String name) {
        init();
        this.paletteid = paletteid;
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
    public static ColorPalette create() {
        return new ColorPalette();
    }

    public static boolean setValue(ColorPalette data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "cpid":
                    data.setCpid(value == null ? -1 : (long) value);
                    return true;
                case "paletteid":
                    data.setPaletteid(value == null ? -1 : (long) value);
                    return true;
                case "name_in_palette":
                    data.setName(value == null ? null : (String) value);
                    return true;
                case "cvalue":
                    data.setColorValue(value == null ? AppValues.InvalidInteger : (int) value);
                    return true;
                case "order_number":
                    data.setOrderNumber(value == null ? -1 : (float) value);
                    return true;
                case "description":
                    data.setDescription(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public static Object getValue(ColorPalette data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "cpid":
                return data.getCpid();
            case "paletteid":
                return data.getPaletteid();
            case "name_in_palette":
                return data.getName();
            case "cvalue":
                return data.getColorValue();
            case "order_number":
                return data.getOrderNumber();
            case "description":
                return data.getDescription();
        }
        return null;
    }

    public static boolean valid(ColorPalette data) {
        return data != null && data.getPaletteid() >= 0
                && data.getColorValue() != AppValues.InvalidInteger;
    }

    /*
        get/set
     */
    public long getCpid() {
        return cpid;
    }

    public ColorPalette setCpid(long cpid) {
        this.cpid = cpid;
        return this;
    }

    public long getPaletteid() {
        return paletteid;
    }

    public ColorPalette setPaletteid(long paletteid) {
        this.paletteid = paletteid;
        return this;
    }

    public String getName() {
        return name;
    }

    public ColorPalette setName(String name) {
        this.name = name;
        return this;
    }

    public int getColorValue() {
        return colorValue;
    }

    public ColorPalette setColorValue(int colorValue) {
        this.colorValue = colorValue;
        return this;
    }

    public ColorData getData() {
        return data;
    }

    public ColorPalette setData(ColorData data) {
        this.data = data;
        return this;
    }

    public float getOrderNumber() {
        return orderNumber;
    }

    public ColorPalette setOrderNumber(float orderNumber) {
        this.orderNumber = orderNumber;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ColorPalette setDescription(String description) {
        this.description = description;
        return this;
    }

}
