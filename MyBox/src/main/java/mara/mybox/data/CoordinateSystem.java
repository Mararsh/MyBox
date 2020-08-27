package mara.mybox.data;

import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-7-4
 * @License Apache License Version 2.0
 */
/*
http://epsg.io/4490
http://epsg.io/4479
http://epsg.io/4326
http://epsg.io/3857
https://blog.csdn.net/An1090239782/article/details/100572140
https://blog.csdn.net/xcymorningsun/article/details/79254163
https://blog.csdn.net/qq_34149805/article/details/65634252
 */
public class CoordinateSystem implements Cloneable {

    protected Value value;

    public enum Value {
        CGCS2000, GCJ_02, WGS_84, BD_09, Mapbar
    }

    public CoordinateSystem(Value value) {
        this.value = value == null ? defaultValue() : value;
    }

    public CoordinateSystem(String name) {
        this.value = defaultValue();
        if (name == null) {
            return;
        }
        for (Value item : Value.values()) {
            if (name.equals(item.name()) || name.equals(message(item.name()))) {
                this.value = item;
                return;
            }
        }
    }

    public CoordinateSystem(short intValue) {
        switch (intValue) {
            case 0:
                value = Value.CGCS2000;
                break;
            case 1:
                value = Value.GCJ_02;
                break;
            case 2:
                value = Value.WGS_84;
                break;
            case 3:
                value = Value.BD_09;
                break;
            case 4:
                value = Value.Mapbar;
                break;
            default:
                value = defaultValue();
                break;
        }
    }

    public String name() {
        if (value == null) {
            value = defaultValue();
        }
        return value.name();
    }

    public short intValue() {
        if (value == null) {
            value = defaultValue();
        }
        switch (value) {
            case CGCS2000:
                return 0;
            case GCJ_02:
                return 1;
            case WGS_84:
                return 2;
            case BD_09:
                return 3;
            case Mapbar:
                return 4;
            default:
                return -1;
        }
    }

    public String gaodeConvertService() {
        if (value == null) {
            value = defaultValue();
        }
        switch (value) {
            case CGCS2000:
                return "gps";
            case GCJ_02:
                return "autonavi";
            case WGS_84:
                return "gps";
            case BD_09:
                return "baidu";
            case Mapbar:
                return "mapbar";
            default:
                return "autonavi";
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        } catch (Exception e) {
            return null;
        }
    }

    /*
        Static methods
     */
    public static CoordinateSystem defaultCode() {
        return new CoordinateSystem(defaultValue());
    }

    public static Value defaultValue() {
        return Value.CGCS2000;
    }

    public static CoordinateSystem CGCS2000() {
        return new CoordinateSystem(Value.CGCS2000);
    }

    public static CoordinateSystem GCJ02() {
        return new CoordinateSystem(Value.GCJ_02);
    }

    public static CoordinateSystem WGS84() {
        return new CoordinateSystem(Value.WGS_84);
    }

    public static CoordinateSystem BD09() {
        return new CoordinateSystem(Value.BD_09);
    }

    public static CoordinateSystem Mapbar() {
        return new CoordinateSystem(Value.Mapbar);
    }

    /*
        get/set
     */
    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

}
