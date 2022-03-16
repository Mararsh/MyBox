package mara.mybox.db.data;

import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;

/**
 * @Author Mara
 * @CreateDate 2021-3-20
 * @License Apache License Version 2.0
 */
public class Tag extends BaseData {

    protected long tgid;
    protected String category, tag;
    protected Color color;

    private void init() {
        tgid = -1;
        category = null;
        tag = null;
        color = FxColorTools.randomColor();
    }

    public Tag() {
        init();
    }

    public Tag(String category, String tag) {
        init();
        this.category = category;
        this.tag = tag;
    }

    /*
        Static methods
     */
    public static Tag create() {
        return new Tag();
    }

    public static boolean setValue(Tag data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "tgid":
                    data.setTgid(value == null ? -1 : (long) value);
                    return true;
                case "category":
                    data.setCategory(value == null ? null : (String) value);
                    return true;
                case "tag":
                    data.setTag(value == null ? null : (String) value);
                    return true;
                case "color":
                    data.setColor(value == null ? null : Color.web((String) value));
                    return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(Tag data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "tgid":
                return data.getTgid();
            case "category":
                return data.getCategory();
            case "tag":
                return data.getTag();
            case "color":
                return data.getColor() == null ? null : data.getColor().toString();
        }
        return null;
    }

    public static boolean valid(Tag data) {
        return data != null
                && data.getTag() != null && !data.getTag().isBlank();
    }

    /*
        get/set
     */
    public long getTgid() {
        return tgid;
    }

    public Tag setTgid(long tgid) {
        this.tgid = tgid;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public Tag setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
