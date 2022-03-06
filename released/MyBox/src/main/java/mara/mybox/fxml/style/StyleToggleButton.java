package mara.mybox.fxml.style;

import mara.mybox.value.Languages;
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
