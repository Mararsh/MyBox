package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.stage.FileChooser;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class TextEditorController extends BaseController {

    private final String TextFilePathKey;
    private int cols, rows;
    protected SimpleBooleanProperty fileChanged;
    protected int lastTextLen;
    private boolean isSettingValues;

    @FXML
    private Button openButton, createButton, saveButton;
    @FXML
    private TextArea textArea;
    @FXML
    protected TextField bottomText;

    public TextEditorController() {

        TextFilePathKey = "TextFilePathKey";

        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("txt", "*.txt"));
                add(new FileChooser.ExtensionFilter("*", "*.*"));
            }
        };
    }

    @Override
    protected void initializeNext() {
        try {
            textArea.addEventHandler(InputEvent.ANY, new EventHandler<InputEvent>() {
                @Override
                public void handle(InputEvent event) {
                    int len = textArea.getText().length();
                    if (!isSettingValues && len != lastTextLen) {
                        fileChanged.set(true);
                    }
                    lastTextLen = len;
                    bottomText.setText(AppVaribles.getMessage("Total") + ": " + len);
                }
            });

            fileChanged = new SimpleBooleanProperty(false);
            fileChanged.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (isSettingValues) {
                        return;
                    }
                    String t = getBaseTitle();
                    if (sourceFile != null) {
                        t += "  " + sourceFile.getAbsolutePath();
                    }
                    if (fileChanged.getValue()) {
                        getMyStage().setTitle(t + "*");
                    } else {
                        getMyStage().setTitle(t);
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void openAction(ActionEvent event) {
        try {
            isSettingValues = true;
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(TextFilePathKey, CommonValues.UserFilePath));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue(LastPathKey, file.getParent());
            AppVaribles.setConfigValue(TextFilePathKey, file.getParent());
            sourceFile = file;

            StringBuilder contents = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                String lineTxt;
                cols = 0;
                rows = 0;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    if (lineTxt.length() > rows) {
                        rows = lineTxt.length();
                    }
                    cols++;
                    contents.append(lineTxt).append(System.getProperty("line.separator"));
                }
            }
            textArea.setText(contents.toString());
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
            bottomText.setText("Cols:" + cols + " rows:" + rows + " total:" + textArea.getText().length());

            fileChanged.set(false);
            isSettingValues = false;

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void saveAction() {
        try {
            isSettingValues = true;
            if (sourceFile == null) {
                final FileChooser fileChooser = new FileChooser();
                File path = new File(AppVaribles.getConfigValue(TextFilePathKey, CommonValues.UserFilePath));
                fileChooser.setInitialDirectory(path);
                fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
                final File file = fileChooser.showSaveDialog(getMyStage());
                if (file == null) {
                    return;
                }
                AppVaribles.setConfigValue(LastPathKey, file.getParent());
                AppVaribles.setConfigValue(TextFilePathKey, file.getParent());
                sourceFile = file;
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, false))) {
                out.write(textArea.getText());
                out.flush();
            }
            fileChanged.set(false);
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
            isSettingValues = false;
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void saveAsAction(ActionEvent event) {
        try {
            isSettingValues = true;
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(TextFilePathKey, CommonValues.UserFilePath));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue(LastPathKey, file.getParent());
            AppVaribles.setConfigValue(TextFilePathKey, file.getParent());
            sourceFile = file;
            try (BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, false))) {
                out.write(textArea.getText());
                out.flush();
            }
            fileChanged.set(false);
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
            isSettingValues = false;
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void createAction(ActionEvent event) {
        try {

            isSettingValues = true;
            sourceFile = null;
            textArea.setText("");
            fileChanged.set(false);
            getMyStage().setTitle(getBaseTitle());
            isSettingValues = false;

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public boolean stageReloading() {
//        logger.debug("stageReloading");
        return checkSavingForNextAction();
    }

    @Override
    public boolean stageClosing() {
//        logger.debug("stageClosing");
        if (!checkSavingForNextAction()) {
            return false;
        }
        return super.stageClosing();
    }

    public boolean checkSavingForNextAction() {
//        logger.debug(fileChanged.getValue());

        if (fileChanged == null || !fileChanged.getValue()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("FileChanged"));
            ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("Save"));
            ButtonType buttonNotSave = new ButtonType(AppVaribles.getMessage("NotSave"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                return true;
            } else if (result.get() == buttonNotSave) {
                return true;
            } else {
                return false;
            }
        }
    }

}
