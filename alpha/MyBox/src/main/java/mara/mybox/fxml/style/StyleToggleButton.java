package mara.mybox.fxml.style;

import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-30
 * @License Apache License Version 2.0
 */
public class StyleToggleButton {

    public static StyleData toggleButtonStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        if (id.startsWith("cat")) {
            return new StyleData(id, message("Meow"), "", "iconCat.png");
        }
        switch (id) {
            case "pickColorButton":
                return new StyleData("pickColorButton", message("PickColor"), "", "iconPickColor.png");
            case "fullScreenButton":
                return new StyleData(id, message("FullScreen"), "", "iconExpand.png");
            case "soundButton":
                return new StyleData(id, message("Mute"), "", "iconMute.png");
            default:
                return null;
        }
    }

}
