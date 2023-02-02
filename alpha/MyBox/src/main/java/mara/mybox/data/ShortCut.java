package mara.mybox.data;

import javafx.scene.image.ImageView;
import mara.mybox.fxml.style.StyleTools;

/**
 * @Author Mara
 * @CreateDate 2022-3-1
 * @License Apache License Version 2.0
 */
public class ShortCut {

    protected String functionKey, action, possibleAlternative;
    protected ImageView icon;

    public ShortCut(String key, String combine, String action, String alt, String iconName) {
        functionKey = key;
        if (combine != null && !combine.isBlank()) {
            functionKey += "+" + combine;
        }
        this.action = action;
        this.possibleAlternative = alt;
        if (iconName != null && !iconName.isBlank()) {
            icon = StyleTools.getIconImageView(iconName);
        }
    }

    /*
        get/set
     */
    public String getFunctionKey() {
        return functionKey;
    }

    public void setFunctionKey(String functionKey) {
        this.functionKey = functionKey;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPossibleAlternative() {
        return possibleAlternative;
    }

    public void setPossibleAlternative(String possibleAlternative) {
        this.possibleAlternative = possibleAlternative;
    }

    public ImageView getIcon() {
        return icon;
    }

    public void setIcon(ImageView icon) {
        this.icon = icon;
    }

}
