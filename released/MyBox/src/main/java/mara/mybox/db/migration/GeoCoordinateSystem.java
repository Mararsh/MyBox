package mara.mybox.db.migration;

import static mara.mybox.value.Languages.message;

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
public class GeoCoordinateSystem implements Cloneable {

    protected Value value;

    public enum Value {
        CGCS2000, GCJ_02, WGS_84, BD_09, Mapbar
    }

    public GeoCoordinateSystem(Value value) {
        this.value = value == null ? defaultValue() : value;
    }

    public GeoCoordinateSystem(String name) {
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

    public GeoCoordinateSystem(short intValue) {
        try {
            value = Value.values()[intValue];
        } catch (Exception e) {
            value = defaultValue();
        }
    }

    public String name() {
        if (value == null) {
            value = defaultValue();
        }
        return value.name();
    }

    public String messageName() {
        return message(name());
    }

    public short shortValue() {
        if (value == null) {
            value = defaultValue();
        }
        return (short) value.ordinal();
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

    public static Value defaultValue() {
        return Value.CGCS2000;
    }

    public static GeoCoordinateSystem defaultCode() {
        return new GeoCoordinateSystem(GeoCoordinateSystem.defaultValue());
    }

    public static GeoCoordinateSystem Mapbar() {
        return new GeoCoordinateSystem(GeoCoordinateSystem.Value.Mapbar);
    }

    public static GeoCoordinateSystem BD09() {
        return new GeoCoordinateSystem(GeoCoordinateSystem.Value.BD_09);
    }

    public static GeoCoordinateSystem CGCS2000() {
        return new GeoCoordinateSystem(GeoCoordinateSystem.Value.CGCS2000);
    }

    public static GeoCoordinateSystem WGS84() {
        return new GeoCoordinateSystem(GeoCoordinateSystem.Value.WGS_84);
    }

    public static GeoCoordinateSystem GCJ02() {
        return new GeoCoordinateSystem(GeoCoordinateSystem.Value.GCJ_02);
    }

    public static short value(String name) {
        return new GeoCoordinateSystem(name).shortValue();
    }

    public static String name(short value) {
        return new GeoCoordinateSystem(value).name();
    }

    public static String messageName(short value) {
        return new GeoCoordinateSystem(value).messageName();
    }

    public static String messageNames() {
        String s = "";
        for (Value v : Value.values()) {
            if (!s.isBlank()) {
                s += "\n";
            }
            s += message(v.name());
        }
        return s;
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
