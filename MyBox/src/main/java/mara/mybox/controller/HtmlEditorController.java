package mara.mybox.controller;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.tools.FxmlImageTools;
import mara.mybox.tools.FxmlTools;
import mara.mybox.tools.PdfTools;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class HtmlEditorController extends TextEditorController {

    private final String HtmlFilePathKey, HtmlImagePathKey, HtmlSnapDelayKey, HtmlLastUrlKey, HtmlPdfPathKey;
    private WebEngine webEngine;
    private int cols, rows, delay, fontSize;
    protected int lastHtmlLen, snapHeight, snapCount;
    private boolean isSettingValues, isOneImage;
    private URL url;
    private List<Image> images;
    private File targetFile;

    @FXML
    private Button saveButton, openButton, createButton, loadButton, updateEditorButton, snapsotButton;
    @FXML
    private HTMLEditor htmlEdior;
    @FXML
    private WebView webView;
    @FXML
    private TextArea textArea;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab editorTab, codesTab, browserTab;
    @FXML
    private ComboBox urlBox, delayBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ToolBar browserToolbar, snapBar;
    @FXML
    private ToggleGroup snapGroup;
    @FXML
    private CheckBox windowSizeCheck;

    public HtmlEditorController() {

        HtmlFilePathKey = "HtmlFilePathKey";
        HtmlImagePathKey = "HtmlImagePathKey";
        HtmlSnapDelayKey = "HtmlSnapDelayKey";
        HtmlLastUrlKey = "HtmllastUrlKey";
        HtmlPdfPathKey = "HtmlPdfPathKey";

        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("html", "*.html", "*.htm"));
            }
        };

    }

    private boolean isPasteEvent(KeyEvent event) {
        return event.isShortcutDown() && event.getCode() == KeyCode.V;
    }

    @Override
    protected void initializeNext() {
        try {
            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> observable,
                        Tab oldValue, Tab newValue) {
                    isSettingValues = true;
                    if (newValue.equals(editorTab)) {
                        htmlEdior.setHtmlText(textArea.getText());
                    } else if (newValue.equals(codesTab)) {
                        textArea.setText(htmlEdior.getHtmlText());
                    }
                    isSettingValues = false;
                }
            });

            initEdtior();
            initBroswer();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initEdtior() {
        try {
            textArea.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    logger.debug(event.getEventType());
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

    protected void initBroswer() {
        try {
            // As my testing, only DragEvent.DRAG_EXITED, KeyEvent.KEY_TYPED, KeyEvent.KEY_RELEASED working for HtmlEdior
            htmlEdior.addEventHandler(DragEvent.DRAG_EXITED, new EventHandler<InputEvent>() { // work
                @Override
                public void handle(InputEvent event) {
                    checkEditorChanged();
                }
            });
            htmlEdior.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    checkEditorChanged();
                }
            });

            urlBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        url = new URL(newValue);
                        if (url.getProtocol().toLowerCase().startsWith("http")) {
                            urlBox.getEditor().setStyle(null);
                            AppVaribles.setConfigValue(HtmlLastUrlKey, newValue);
                            if (!urlBox.getItems().contains(newValue)) {
                                urlBox.getItems().add(newValue);
                            }
                        } else {
                            urlBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        urlBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            String savedUrl = AppVaribles.getConfigValue(HtmlLastUrlKey, "");
            if (!savedUrl.isEmpty()) {
                urlBox.getItems().add(savedUrl);
            }

            delayBox.getItems().addAll(Arrays.asList("50", "300", "1000", "100", "600", "2000", "3000", "5000", "10000"));
            delayBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        delay = Integer.valueOf(newValue);
                        if (delay > 0) {
                            delayBox.getEditor().setStyle(null);
                            AppVaribles.setConfigValue(HtmlSnapDelayKey, delay + "");
                        } else {
                            delay = 300;
                            delayBox.getEditor().setStyle(badStyle);
                        }

                    } catch (Exception e) {
                        delay = 300;
                        delayBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            delayBox.getSelectionModel().select(AppVaribles.getConfigValue(HtmlSnapDelayKey, "300"));

            snapGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    RadioButton selected = (RadioButton) snapGroup.getSelectedToggle();
                    if (AppVaribles.getMessage("OneImage").equals(selected.getText())) {
                        isOneImage = true;
                        windowSizeCheck.setDisable(true);
                    } else {
                        isOneImage = false;
                        windowSizeCheck.setDisable(false);
                    }
                }
            });

            if (AppVaribles.showComments) {
                Tooltip tips = new Tooltip(getMessage("htmlSnapComments"));
                tips.setFont(new Font(16));
                FxmlTools.setComments(snapBar, tips);
            }

            webEngine = webView.getEngine();
            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue ov, State oldState, State newState) {
                    if (null == newState) {
                        browserToolbar.setDisable(false);
                    } else {
                        switch (newState) {
                            case SUCCEEDED:
                                browserToolbar.setDisable(false);
                                bottomText.setText(AppVaribles.getMessage("Loaded"));
                                try {
                                    String s = (String) webEngine.executeScript("getComputedStyle(document.body).fontSize");
                                    fontSize = Integer.valueOf(s.substring(0, s.length() - 2));
                                } catch (Exception e) {
                                    fontSize = 14;
                                }
                                break;
                            case RUNNING:
                                bottomText.setText(AppVaribles.getMessage("Loading..."));
//                                browserToolbar.setDisable(true);
                                break;
                            default:
                                browserToolbar.setDisable(false);
                                break;
                        }
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void checkEditorChanged() {
        int len = htmlEdior.getHtmlText().length();
        if (!isSettingValues && len != lastHtmlLen) {
            fileChanged.set(true);
        }
        lastHtmlLen = len;
        bottomText.setText(AppVaribles.getMessage("Total") + ": " + len);
    }

    @FXML
    private void openAction(ActionEvent event) {
        try {
            if (!checkSavingForNextAction()) {
                return;
            }
            isSettingValues = true;
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
            AppVaribles.setConfigValue(LastPathKey, file.getParent());
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
            String conString = contents.toString();
            htmlEdior.setHtmlText(conString);
            textArea.setText(conString);
            webEngine.loadContent(conString);
            fileChanged.set(false);
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
            isSettingValues = false;

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void loadAction(ActionEvent event) {
        try {

            webEngine.load(url.toString());

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void updateBrowser(ActionEvent event) {
        try {
            webEngine.loadContent(htmlEdior.getHtmlText());
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void updateEditor(ActionEvent event) {
        try {
            if (!checkSavingForNextAction()) {
                return;
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(webEngine.getDocument()), new StreamResult(stringWriter));
            String contents = stringWriter.getBuffer().toString();

//            String contents = (String) webEngine.executeScript("document.documentElement.outerHTML");
            htmlEdior.setHtmlText(contents);
            textArea.setText(contents);
            fileChanged.set(true);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    private void saveAction() {
        try {
            isSettingValues = true;
            if (sourceFile == null) {
                final FileChooser fileChooser = new FileChooser();
                File path = new File(AppVaribles.getConfigValue(HtmlFilePathKey, System.getProperty("user.home")));
                fileChooser.setInitialDirectory(path);
                fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
                final File file = fileChooser.showSaveDialog(getMyStage());
                if (file == null) {
                    return;
                }
                AppVaribles.setConfigValue(LastPathKey, file.getParent());
                AppVaribles.setConfigValue(HtmlFilePathKey, file.getParent());
                sourceFile = file;
            }
            String contents;
            if (tabPane.getSelectionModel().getSelectedItem().equals(codesTab)) {
                contents = textArea.getText();
            } else {
                contents = htmlEdior.getHtmlText();
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter(sourceFile, false))) {
                out.write(contents);
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
            File path = new File(AppVaribles.getConfigValue(HtmlFilePathKey, System.getProperty("user.home")));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue(LastPathKey, file.getParent());
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
            htmlEdior.setHtmlText("");
            fileChanged.set(false);
            getMyStage().setTitle(getBaseTitle());
            isSettingValues = false;
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void snapshot(ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        File path;
        if (isOneImage) {
            path = new File(AppVaribles.getConfigValue(HtmlImagePathKey, System.getProperty("user.home")));
            fileChooser.getExtensionFilters().addAll(CommonValues.ImageExtensionFilter);
        } else {
            path = new File(AppVaribles.getConfigValue(HtmlPdfPathKey, System.getProperty("user.home")));
            fileChooser.getExtensionFilters().addAll(CommonValues.PdfExtensionFilter);
        }
        fileChooser.setInitialDirectory(path);
        final File file = fileChooser.showSaveDialog(getMyStage());
        if (file == null) {
            return;
        }
        AppVaribles.setConfigValue(LastPathKey, file.getParent());
        if (isOneImage) {
            AppVaribles.setConfigValue(HtmlImagePathKey, file.getParent());
        } else {
            AppVaribles.setConfigValue(HtmlPdfPathKey, file.getParent());
        }
        targetFile = file;
        images = new ArrayList();

        final int totalHeight = (Integer) webEngine.executeScript("document.body.scrollHeight");
        final int snapStep = (Integer) webEngine.executeScript("document.documentElement.clientHeight < document.body.clientHeight ? document.documentElement.clientHeight : document.body.clientHeight");
        final int width = (Integer) webEngine.executeScript("document.documentElement.clientWidth < document.body.clientWidth ? document.documentElement.clientWidth : document.body.clientWidth ");
        snapHeight = 0;
        snapCount = 0;
        webEngine.executeScript("window.scrollTo(0,0 );");
        bottomText.setText(AppVaribles.getMessage("SnapingImage..."));
        final SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (totalHeight <= snapHeight) {
                    this.cancel();
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Image snapshot = webView.snapshot(parameters, null);
                                if (totalHeight < snapHeight + snapStep) { // last snap
                                    snapshot = FxmlImageTools.cropImage(snapshot, 0, snapStep - (totalHeight - snapHeight),
                                            width - 1, (int) snapshot.getHeight() - 1);
                                } else {
                                    snapshot = FxmlImageTools.cropImage(snapshot, 0, 0,
                                            width - 1, (int) snapshot.getHeight() - 1);
                                }
                                images.add(snapshot);
                                snapHeight += snapStep;
                                if (totalHeight <= snapHeight) { // last snap

                                    boolean success = true;
                                    if (isOneImage) {
                                        Image finalImage = FxmlImageTools.combineSingleColumn(images);
                                        if (finalImage != null) {
                                            String format = FileTools.getFileSuffix(targetFile.getAbsolutePath());
                                            final BufferedImage bufferedImage = FxmlImageTools.getWritableData(finalImage, format);
                                            ImageFileWriters.writeImageFile(bufferedImage, format, targetFile.getAbsolutePath());
                                        } else {
                                            success = false;
                                        }
                                    } else {
                                        success = PdfTools.htmlIntoPdf(images, targetFile, windowSizeCheck.isSelected());
                                    }
                                    if (success && targetFile.exists()) {
                                        if (isOneImage) {
                                            openImageManufactureInNew(targetFile.getAbsolutePath());
                                        } else {
                                            Desktop.getDesktop().browse(targetFile.toURI());
                                        }
                                    } else {
                                        FxmlTools.popError(bottomText, AppVaribles.getMessage("Failed"));
                                    }

                                    bottomText.setText("");
                                } else {
                                    webEngine.executeScript("window.scrollTo(0, " + snapHeight + ");");
                                }
                            } catch (Exception e) {
                                logger.error(e.toString());
                                FxmlTools.popError(bottomText, AppVaribles.getMessage("Failed"));
                            }
                        }
                    });
                }
            }
        }, 0, delay);

    }

    @FXML
    private void zoomIn(ActionEvent event) {
        ++fontSize;
        webEngine.executeScript("document.body.style.fontSize = '" + fontSize + "px' ;");
    }

    @FXML
    private void zoomOut(ActionEvent event) {
        --fontSize;
        webEngine.executeScript("document.body.style.fontSize = '" + fontSize + "px' ;");
    }

    public void switchBroswerTab() {
        tabPane.getSelectionModel().select(browserTab);
    }

}
