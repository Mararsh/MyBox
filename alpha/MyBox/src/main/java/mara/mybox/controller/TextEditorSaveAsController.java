package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data.FileEditInformation.Line_Break;
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
public class TextEditorSaveAsController extends BaseChildController {

    protected TextEditorController fileController;
    protected Line_Break lineBreak;

    @FXML
    protected ComboBox<String> targetCharsetSelector;
    @FXML
    protected CheckBox targetBomCheck;
    @FXML
    protected ToggleGroup lineBreakGroup;
    @FXML
    protected RadioButton crlfRadio, lfRadio, crRadio;

    public void setParameters(TextEditorController parent) {
        try {
            fileController = parent;
            if (fileController == null) {
                close();
                return;
            }
            baseName = fileController.baseName;
            setFileType(fileController.TargetFileType);
            setTitle(message("SaveAs") + " - " + fileController.getTitle());

            targetCharsetSelector.getItems().addAll(TextTools.getCharsetNames());
            targetCharsetSelector.setValue(UserConfig.getString(baseName + "TargetCharset", "UTF-8"));
            targetCharsetSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if ("UTF-8".equals(newValue) || "UTF-16BE".equals(newValue)
                            || "UTF-16LE".equals(newValue) || "UTF-32BE".equals(newValue)
                            || "UTF-32LE".equals(newValue)) {
                        targetBomCheck.setDisable(false);
                    } else {
                        targetBomCheck.setDisable(true);
                        if ("UTF-16".equals(newValue) || "UTF-32".equals(newValue)) {
                            targetBomCheck.setSelected(true);
                        } else {
                            targetBomCheck.setSelected(false);
                        }
                    }
                }
            });

            String savedLB = UserConfig.getString(baseName + "TargetLineBreak", Line_Break.LF.toString());
            if (savedLB.equals(Line_Break.CR.toString())) {
                crRadio.setSelected(true);
            } else if (savedLB.equals(Line_Break.CRLF.toString())) {
                crlfRadio.setSelected(true);
            } else {
                lfRadio.setSelected(true);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
            if (crRadio.isSelected()) {
                lineBreak = Line_Break.CR;
            } else if (crlfRadio.isSelected()) {
                lineBreak = Line_Break.CRLF;
            } else {
                lineBreak = Line_Break.LF;
            }
            MyBoxLog.console(lineBreak.toString());
            UserConfig.setString(baseName + "TargetLineBreak", lineBreak.toString());
            UserConfig.setString(baseName + "TargetCharset", targetCharsetSelector.getValue());
            UserConfig.setBoolean(baseName + "TargetBOM", targetBomCheck.isSelected());

            fileController.saveAsType = saveAsType;

            if (closeAfterCheck.isSelected()) {
                close();
            }
            fileController.saveAs();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        static methods
     */
    public static TextEditorSaveAsController open(TextEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            TextEditorSaveAsController controller = (TextEditorSaveAsController) WindowTools.branchStage(
                    parent, Fxmls.TextEditorSaveAsFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
