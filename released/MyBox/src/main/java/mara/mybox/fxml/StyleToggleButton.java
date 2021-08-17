package mara.mybox.fxml;

import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

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
        switch (id) {
            case "pickColorButton":
                return new StyleData("pickColorButton", Languages.message("PickColor"), Languages.message("ColorPickerComments"), "", "iconPickColor.png");
            case "pickFillColorButton":
                return new StyleData("pickFillColorButton", Languages.message("PickColor"), Languages.message("ColorPickerComments"), "", "iconPickColor.png");
            case "fullScreenButton":
                return new StyleData(id, Languages.message("FullScreen"), "", "iconExpand.png");
            case "soundButton":
                return new StyleData(id, Languages.message("Mute"), "", "iconMute.png");
            default:
                return null;
        }
    }

}
