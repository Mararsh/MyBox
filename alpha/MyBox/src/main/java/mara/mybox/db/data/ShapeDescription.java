package mara.mybox.db.data;

import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-4-24
 * @License Apache License Version 2.0
 */
public class ShapeDescription extends BaseData {

    private long sdid;
    protected ShapeType shapeType;
    protected String title, descpriton, more;
    protected Date updateTime;

    public static enum ShapeType {
        Line, Text, Arc, Cricle, Ellipse, Rectangle
    }

    public ShapeDescription() {
        init();
    }

    private void init() {
        sdid = -1;
        shapeType = null;

    }

    /*
        static methods
     */
    public static ShapeDescription create() {
        return new ShapeDescription();
    }

    public static boolean setValue(ShapeDescription data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "sdid":
                    data.setSdid(value == null ? -1 : (long) value);
                    return true;
                case "shape":
                    data.setShapeType(value == null ? null : type((String) value));
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "descpriton":
                    data.setDescpriton(value == null ? null : (String) value);
                    return true;
                case "more":
                    data.setMore(value == null ? null : (String) value);
                    return true;
                case "update_time":
                    data.setUpdateTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(ShapeDescription data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "sdid":
                return data.getSdid();
            case "shape":
                return data.getShapeType() == null ? null : data.getShapeType().name();
            case "title":
                return data.getTitle();
            case "descpriton":
                return data.getDescpriton();
            case "more":
                return data.getMore();
            case "update_time":
                return data.getUpdateTime();
        }
        return null;
    }

    public static boolean valid(ShapeDescription data) {
        return data != null && data.getShapeType() != null
                && data.getTitle() != null && data.getDescpriton() != null;
    }

    public static ShapeType type(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        for (ShapeType t : ShapeType.values()) {
            if (t.name().equalsIgnoreCase(type)) {
                return t;
            }
        }
        return null;
    }

    /*
        get/set
     */
    public long getSdid() {
        return sdid;
    }

    public void setSdid(long sdid) {
        this.sdid = sdid;
    }

    public ShapeType getShapeType() {
        return shapeType;
    }

    public void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescpriton() {
        return descpriton;
    }

    public void setDescpriton(String descpriton) {
        this.descpriton = descpriton;
    }

    public String getMore() {
        return more;
    }

    public void setMore(String more) {
        this.more = more;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
