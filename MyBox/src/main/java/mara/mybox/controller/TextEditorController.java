package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.stage.FileChooser;
import mara.mybox.controller.BaseController;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class TextEditorController extends BaseController {

    private final String TextFilePathKey;
    private int cols, rows;

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
                    bottomText.setText(AppVaribles.getMessage("Total") + ": " + textArea.getText().length());
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void openAction(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(TextFilePathKey, System.getProperty("user.home")));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue("LastPath", file.getParent());
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
            getMyStage().setTitle(AppVaribles.getMessage("AppTitle") + "  " + sourceFile.getAbsolutePath());
            bottomText.setText("Cols:" + cols + " rows:" + rows + " total:" + textArea.getText().length());

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void saveAction(ActionEvent event) {
        try {
            if (sourceFile == null) {
                final FileChooser fileChooser = new FileChooser();
                File path = new File(AppVaribles.getConfigValue(TextFilePathKey, System.getProperty("user.home")));
                fileChooser.setInitialDirectory(path);
                fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
                final File file = fileChooser.showSaveDialog(getMyStage());
                if (file == null) {
                    return;
                }
                AppVaribles.setConfigValue("LastPath", file.getParent());
                AppVaribles.setConfigValue(TextFilePathKey, file.getParent());
                sourceFile = file;
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, false))) {
                out.write(textArea.getText());
                out.flush();
            }
            getMyStage().setTitle(AppVaribles.getMessage("AppTitle") + "  " + sourceFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void saveAsAction(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(TextFilePathKey, System.getProperty("user.home")));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue("LastPath", file.getParent());
            AppVaribles.setConfigValue(TextFilePathKey, file.getParent());
            sourceFile = file;
            try (BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, false))) {
                out.write(textArea.getText());
                out.flush();
            }
            getMyStage().setTitle(AppVaribles.getMessage("AppTitle") + "  " + sourceFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void createAction(ActionEvent event) {
        try {
            sourceFile = null;
            textArea.setText("");
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(TextFilePathKey, System.getProperty("user.home")));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue("LastPath", file.getParent());
            AppVaribles.setConfigValue(TextFilePathKey, file.getParent());
            sourceFile = file;
            getMyStage().setTitle(AppVaribles.getMessage("AppTitle") + "  " + sourceFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

}
