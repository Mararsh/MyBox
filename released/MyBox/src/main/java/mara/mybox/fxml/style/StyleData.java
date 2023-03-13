package mara.mybox.fxml.style;

/**
 * @Author Mara
 * @CreateDate 2019-4-26 7:16:14
 * @License Apache License Version 2.0
 */
public class StyleData {

    public static enum StyleColor {
        Red, Blue, LightBlue, Pink, Orange, Green, Customize
    }

    private String id, name, comments, shortcut, iconName;

    public StyleData(String id) {
        this.id = id;
    }

    public StyleData(String id, String name, String shortcut, String iconName) {
        this.id = id;
        this.name = name;
        this.shortcut = shortcut;
        this.iconName = iconName;
    }

    public StyleData(String id, String name, String comments, String shortcut, String iconName) {
        this.id = id;
        this.name = name;
        this.comments = comments;
        this.shortcut = shortcut;
        this.iconName = iconName;
    }


    /*
        get/set
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
