package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-5-5
 * @License Apache License Version 2.0
 */
public class ImageBase64Controller extends BaseController {

    protected Charset charset;

    @FXML
    protected ToggleGroup formatGroup;
    @FXML
    protected TextArea resultArea;
    @FXML
    protected CheckBox tagCheck;

    public ImageBase64Controller() {
        baseTitle = message("ImageBase64");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image, VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tagCheck.setSelected(UserConfig.getBoolean(baseName + "Tag", true));
            tagCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Tag", tagCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public void sourceFileChanged(final File file) {
        sourceFile = file;
        clearAction();
    }

    @FXML
    @Override
    public void startAction() {
        if (sourceFile == null || UserConfig.badStyle().equals(sourceFileInput.getStyle())) {
            popError(message("InvalidParameter"));
            return;
        }
        if (sourceFile.length() > 100 * 1024) {
            if (!PopTools.askSure(this, baseTitle, message("GeneratedDataMayLarge"))) {
                return;
            }
        }
        clearAction();
        String format = ((RadioButton) formatGroup.getSelectedToggle()).getText();
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {

                private String imageBase64;

                @Override
                protected boolean handle() {
                    try {
                        imageBase64 = BufferedImageTools.base64(sourceFile, format);
                        if (tagCheck.isSelected()) {
                            imageBase64 = "<img src=\"data:image/" + format + ";base64," + imageBase64 + "\" >";
                        }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    long len = imageBase64.length();
                    String lenString = StringTools.format(len);
                    if (len > 1024 * 1024) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle(baseTitle);
                        alert.setContentText(message("Length") + ": " + lenString);
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        ButtonType buttonLoad = new ButtonType(message("Load"));
                        ButtonType buttonSave = new ButtonType(message("Save"));
                        ButtonType buttonCancel = new ButtonType(message("Cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
                        alert.getButtonTypes().setAll(buttonLoad, buttonSave, buttonCancel);
                        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                        stage.setAlwaysOnTop(true);
                        stage.toFront();
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result == null || !result.isPresent()) {
                            imageBase64 = null;
                            return;
                        }
                        if (result.get() == buttonSave) {
                            saveAs(imageBase64);
                            return;
                        }
                    }
                    resultArea.setText(imageBase64);
                    bottomLabel.setText(lenString);
                }

            };
            start(task);
        }
    }

    @FXML
    @Override
    public void saveAction() {
        saveAsAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        saveAs(resultArea.getText());
    }

    public void saveAs(String results) {
        if (results == null || results.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        String name = sourceFile != null ? FileNameTools.prefix(sourceFile.getName()) : DateTools.nowFileString();
        File file = chooseSaveFile(VisitHistory.FileType.Text, name + "_Base64");
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    return TextFileTools.writeFile(file, results) != null;
                }

                @Override
                protected void whenSucceeded() {
                    recordFileWritten(file);
                    browse(file.getParentFile());
                }

            };
            start(task);
        }

    }

    @FXML
    @Override
    public void clearAction() {
        resultArea.clear();
        bottomLabel.setText("");
    }

    @FXML
    @Override
    public void copyAction() {
        TextClipboardTools.copyToSystemClipboard(this, resultArea.getText());
    }

}
