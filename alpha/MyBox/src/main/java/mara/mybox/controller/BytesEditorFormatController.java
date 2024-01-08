package mara.mybox.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.ByteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class BytesEditorFormatController extends BaseChildController {

    protected BytesEditorController fileController;

    @FXML
    protected TextField lbWidthInput, lbBytesInput;
    @FXML
    protected RadioButton lbWidthRadio, bytesRadio, lbLFRadio, lbCRRadio, lbCRLFRsadio;

    public void setParameters(BytesEditorController parent) {
        try {
            fileController = parent;
            if (fileController == null || fileController.sourceInformation == null) {
                close();
                return;
            }
            baseName = fileController.baseName;
            setFileType(fileController.TargetFileType);
            setTitle(message("Format") + " - " + fileController.getTitle());

            Line_Break lb = fileController.sourceInformation.getLineBreak();
            if (lb == null) {
                lb = Line_Break.Width;
            }
            String lineBreakValue;
            switch (lb) {
                case Value:
                    bytesRadio.setSelected(true);
                    lineBreakValue = "0D 0A";
                    break;
                case Width:
                    lbWidthRadio.setSelected(true);
                    lineBreakValue = "0D 0A";
                    break;
                case CR:
                    lbCRRadio.setSelected(true);
                    lineBreakValue = "0D ";
                    break;
                case CRLF:
                    lbCRLFRsadio.setSelected(true);
                    lineBreakValue = "0D 0A ";
                    break;
                default:
                    lbLFRadio.setSelected(true);
                    lineBreakValue = "0A ";
                    break;
            }
            lbBytesInput.setText(lineBreakValue);
            int lineBreakWidth = fileController.sourceInformation.getLineBreakWidth();
            if (lineBreakWidth < 0) {
                lineBreakWidth = 30;
            }
            lbWidthInput.setText(lineBreakWidth + "");

            lbBytesInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    if (!isSettingValues) {
                        checkBytesHex();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void checkBytesHex() {
        try {
            if (!bytesRadio.isSelected()) {
                lbBytesInput.setStyle(null);
                return;
            }
            final String v = ByteTools.formatTextHex(lbBytesInput.getText());
            if (v == null || v.isEmpty()) {
                lbBytesInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("BytesHex"));
            } else {
                lbBytesInput.setStyle(null);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        isSettingValues = true;
                        lbBytesInput.setText(v);
                        lbBytesInput.end();
                        isSettingValues = false;
                    }
                });
            }

        } catch (Exception e) {
            lbBytesInput.setStyle(UserConfig.badStyle());
        }

    }

    @FXML
    @Override
    public void okAction() {
        Line_Break lineBreak;
        String lineBreakValue = null;
        int lineBreakWidth = -1;
        if (lbWidthRadio.isSelected()) {
            lineBreak = Line_Break.Width;
            int v = Integer.parseInt(lbWidthInput.getText());
            if (v > 0) {
                lineBreakWidth = v;
                lbWidthInput.setStyle(null);
            } else {
                lbWidthInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("BytesNumber"));
                return;
            }

        } else if (bytesRadio.isSelected()) {
            lineBreak = Line_Break.Value;
            String v = lbBytesInput.getText();
            if (v == null || v.isEmpty()) {
                lbBytesInput.setStyle(UserConfig.badStyle());
                popError(message("InvalidParameter") + ": " + message("BytesHex"));
                return;
            } else {
                lineBreakValue = v;
                lbBytesInput.setStyle(null);
            }

        } else if (lbLFRadio.isSelected()) {
            lineBreak = Line_Break.LF;
            lineBreakValue = "0A ";

        } else if (lbCRRadio.isSelected()) {
            lineBreak = Line_Break.CR;
            lineBreakValue = "0D ";

        } else if (lbCRLFRsadio.isSelected()) {
            lineBreak = Line_Break.CRLF;
            lineBreakValue = "0D 0A ";
        } else {
            return;
        }
        UserConfig.setString(baseName + "LineBreak", lineBreak.toString());
        fileController.lineBreak = lineBreak;
        if (lineBreakValue != null) {
            UserConfig.setString(baseName + "LineBreakValue", lineBreakValue);
            fileController.lineBreakValue = lineBreakValue;
        }
        if (lineBreakWidth > 0) {
            UserConfig.setInt(baseName + "LineBreakWidth", lineBreakWidth);
            fileController.lineBreakWidth = lineBreakWidth;
        }
        fileController.refreshAction();
        if (closeAfterCheck.isSelected()) {
            close();
        }
    }


    /*
        static methods
     */
    public static BytesEditorFormatController open(BytesEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            BytesEditorFormatController controller = (BytesEditorFormatController) WindowTools.branchStage(
                    parent, Fxmls.BytesEditorFormatFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
