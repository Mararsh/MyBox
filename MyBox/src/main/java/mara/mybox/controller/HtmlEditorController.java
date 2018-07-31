package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.InputEvent;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class HtmlEditorController extends BaseController {

    private final String HtmlFilePathKey;
    private int cols, rows;

    @FXML
    private Button saveButton, openButton, createButton;
    @FXML
    private HTMLEditor htmlEdior;
    @FXML
    private WebView webView;
    @FXML
    private TextArea textArea;
    @FXML
    protected TextField bottomText;
    @FXML
    private TabPane tabPane;
    @FXML
    protected Tab editorTab, codesTab;

    public HtmlEditorController() {

        HtmlFilePathKey = "HtmlFilePathKey";

        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("html", "*.html", "*.htm"));
            }
        };

    }

    @Override

    protected void initializeNext() {
        try {
            htmlEdior.addEventHandler(InputEvent.ANY, new EventHandler<InputEvent>() {
                @Override
                public void handle(InputEvent event) {
                    bottomText.setText(AppVaribles.getMessage("Total") + ": " + htmlEdior.getHtmlText().length());
                }
            });

            textArea.addEventHandler(InputEvent.ANY, new EventHandler<InputEvent>() {
                @Override
                public void handle(InputEvent event) {
                    bottomText.setText(AppVaribles.getMessage("Total") + ": " + textArea.getText().length());
                }
            });

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> observable,
                        Tab oldValue, Tab newValue) {
                    if (AppVaribles.getMessage("Editor").equals(newValue.getText())) {
                        htmlEdior.setHtmlText(textArea.getText());
                    } else {
                        textArea.setText(htmlEdior.getHtmlText());
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
//            sourceFile = null;
//            htmlEdior.setHtmlText("");
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(HtmlFilePathKey, System.getProperty("user.home")));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue("LastPath", file.getParent());
            AppVaribles.setConfigValue(HtmlFilePathKey, file.getParent());
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
            if (AppVaribles.getMessage("Editor").equals(tabPane.getSelectionModel().getSelectedItem().getText())) {
                htmlEdior.setHtmlText(contents.toString());
            } else {
                textArea.setText(contents.toString());
            }
            getMyStage().setTitle(AppVaribles.getMessage("AppTitle") + "  " + sourceFile.getAbsolutePath());

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void saveAction(ActionEvent event) {
        try {
            if (sourceFile == null) {
                final FileChooser fileChooser = new FileChooser();
                File path = new File(AppVaribles.getConfigValue(HtmlFilePathKey, System.getProperty("user.home")));
                fileChooser.setInitialDirectory(path);
                fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
                final File file = fileChooser.showSaveDialog(getMyStage());
                if (file == null) {
                    return;
                }
                AppVaribles.setConfigValue("LastPath", file.getParent());
                AppVaribles.setConfigValue(HtmlFilePathKey, file.getParent());
                sourceFile = file;
            }
            String contents;
            if (AppVaribles.getMessage("Editor").equals(tabPane.getSelectionModel().getSelectedItem().getText())) {
                contents = htmlEdior.getHtmlText();
            } else {
                contents = textArea.getText();
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, false))) {
                out.write(contents);
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
            File path = new File(AppVaribles.getConfigValue(HtmlFilePathKey, System.getProperty("user.home")));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue("LastPath", file.getParent());
            AppVaribles.setConfigValue(HtmlFilePathKey, file.getParent());
            sourceFile = file;
            String contents;
            if (AppVaribles.getMessage("Editor").equals(tabPane.getSelectionModel().getSelectedItem().getText())) {
                contents = htmlEdior.getHtmlText();
            } else {
                contents = textArea.getText();
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, false))) {
                out.write(contents);
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
            htmlEdior.setHtmlText("");
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(HtmlFilePathKey, System.getProperty("user.home")));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue("LastPath", file.getParent());
            AppVaribles.setConfigValue(HtmlFilePathKey, file.getParent());
            sourceFile = file;
            getMyStage().setTitle(AppVaribles.getMessage("AppTitle") + "  " + sourceFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void saveAsImage(ActionEvent event) {
        try {
//
//            final WebView webView1 = new WebView();
//            webView1.setPrefWidth(1200);
//            webView1.setPrefHeight(800);
//
//            logger.debug(cols + "   " + rows);
//            final WebEngine webEngine = webView1.getEngine();
//            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
//                @Override
//                public void changed(ObservableValue ov, State oldState, State newState) {
//                    if (newState == State.SUCCEEDED) {
//                        try {
//                            Scene snapshotScene = new Scene(webView1);
//
//                            logger.debug(webView1.getBoundsInLocal().getWidth() + "   " + webView1.getBoundsInLocal().getHeight());
//                            logger.debug(webView1.getWidth() + "   " + webView1.getHeight());
//                            SnapshotParameters snapshotParameters = new SnapshotParameters();
////            snapshotParameters.setFill(Color.TRANSPARENT);
//                            Image image = webView1.snapshot(snapshotParameters, null);
//                            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", new File("d:\\tmp\\try.png"));
//                        } catch (Exception e) {
//                            logger.error(e.toString());
//                        }
//                    }
//                }
//            });
//            webEngine.loadContent(htmlEdior.getHtmlText());

//            TextArea textArea2 = new TextArea(htmlEdior.getHtmlText());
//
////            textArea2.setPrefHeight(textArea2.getPrefHeight() + textArea2.getScrollTop());
////            textArea2.setPrefWidth(textArea2.getPrefWidth() + textArea2.getScrollLeft());
//            Scene snapshotScene = new Scene(textArea2);
//            logger.debug(snapshotScene.getWidth() + "   " + snapshotScene.getHeight());
//
//            SnapshotParameters snapshotParameters = new SnapshotParameters();
//            snapshotParameters.setFill(Color.TRANSPARENT);
//
//            Image image = textArea2.snapshot(snapshotParameters, null);
//            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", new File("d:\\tmp\\try.png"));
//            BufferedImage screenshot = (new Robot()).createScreenCapture(new Rectangle(0, 0, (int) d.getWidth(), (int) d.getHeight()));
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

}
