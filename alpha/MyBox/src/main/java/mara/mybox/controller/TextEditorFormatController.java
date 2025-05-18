package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class TextEditorFormatController extends BaseChildController {

    protected TextEditorController fileController;

    @FXML
    protected ComboBox<String> charsetSelector;
    @FXML
    protected Label bomLabel;

    public void setParameters(TextEditorController parent) {
        try {
            fileController = parent;
            if (fileController == null || fileController.sourceInformation == null) {
                close();
                return;
            }
            baseName = fileController.baseName;
            setFileType(fileController.TargetFileType);
            setTitle(message("Format") + " - " + fileController.getTitle());

            charsetSelector.getItems().addAll(TextTools.getCharsetNames());
            charsetSelector.setValue(fileController.sourceInformation.getCharset().name());
            if (fileController.sourceInformation.isWithBom()) {
                bomLabel.setText(message("WithBom"));
            } else {
                bomLabel.setText("");
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        UserConfig.setString(baseName + "SourceCharset", charsetSelector.getValue());
        fileController.refreshAction();
        if (closeAfterCheck.isSelected()) {
            close();
        }
    }


    /*
        static methods
     */
    public static TextEditorFormatController open(TextEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            TextEditorFormatController controller = (TextEditorFormatController) WindowTools.referredTopStage(
                    parent, Fxmls.TextEditorFormatFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
