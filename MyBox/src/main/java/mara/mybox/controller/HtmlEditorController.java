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
import java.util.Date;
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
import javafx.geometry.Rectangle2D;
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
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
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
    private int cols, rows, delay, fontSize, orginalStageHeight, orginalStageY;
    protected int lastHtmlLen, lastCodesLen, snapHeight, snapCount;
    private boolean isSettingValues, isOneImage;
    private URL url;
    private List<Image> images;
    private File targetFile;
    protected SimpleBooleanProperty loadedCompletely;
    private Stage snapingStage;
    private float zoomScale;

    @FXML
    private Button saveButton, openButton, createButton, loadButton, updateEditorButton, snapsotButton;
    @FXML
    private HTMLEditor htmlEdior;
    @FXML
    private WebView webView;
    @FXML
    private TextArea codesArea;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab editorTab, codesTab, browserTab;
    @FXML
    private ComboBox urlBox, delayBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private HBox browserActionBar;
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

            lastCodesLen = lastHtmlLen = 0;
            isSettingValues = false;
            fontSize = 14;
            zoomScale = 1.0f;

            initCodeEdtior();
            initBroswer();

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> observable,
                        Tab oldValue, Tab newValue) {
                    if (fileChanged.get()) {
                        isSettingValues = true;
                        if (newValue.equals(editorTab)) {
                            htmlEdior.setHtmlText(codesArea.getText());
                        } else if (newValue.equals(codesTab)) {
                            codesArea.setText(htmlEdior.getHtmlText());
                        }
                        isSettingValues = false;
                    }
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

    protected void initHtmlEdtior() {
        try {
            // As my testing, only DragEvent.DRAG_EXITED, KeyEvent.KEY_TYPED, KeyEvent.KEY_RELEASED working for HtmlEdior
            htmlEdior.addEventHandler(DragEvent.DRAG_EXITED, new EventHandler<InputEvent>() { // work
                @Override
                public void handle(InputEvent event) {
                    checkHtmlEditorChanged();
                }
            });
            htmlEdior.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    logger.debug("setOnKeyReleased");
                    checkHtmlEditorChanged();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void checkHtmlEditorChanged() {
        int len = htmlEdior.getHtmlText().length();
        logger.debug(isSettingValues + "  " + len + " " + lastHtmlLen);
        if (!isSettingValues && len != lastHtmlLen) {
            fileChanged.set(true);
        }
        lastHtmlLen = len;
        bottomText.setText(AppVaribles.getMessage("Total") + ": " + len);
    }

    protected void initCodeEdtior() {
        try {
            codesArea.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    int len = codesArea.getText().length();
                    if (!isSettingValues && len != lastTextLen) {
                        fileChanged.set(true);
                    }
                    lastCodesLen = len;
                    bottomText.setText(AppVaribles.getMessage("Total") + ": " + len);
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initBroswer() {
        try {

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

            delayBox.getItems().addAll(Arrays.asList("2000", "3000", "1000", "10000"));
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
            delayBox.getSelectionModel().select(AppVaribles.getConfigValue(HtmlSnapDelayKey, "2000"));

            snapGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkOneImage();
                }
            });
            checkOneImage();

            if (AppVaribles.showComments) {
                Tooltip tips = new Tooltip(getMessage("htmlSnapComments"));
                tips.setFont(new Font(16));
                FxmlTools.setComments(browserActionBar, tips);
            }

            webEngine = webView.getEngine();
            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue ov, State oldState, State newState) {
                    if (null == newState) {
                        browserActionBar.setDisable(true);
                    } else {
                        switch (newState) {
                            case SUCCEEDED:
                                browserActionBar.setDisable(false);
//                                bottomText.setText(AppVaribles.getMessage("Loaded"));
                                try {
                                    String s = (String) webEngine.executeScript("getComputedStyle(document.body).fontSize");
                                    fontSize = Integer.valueOf(s.substring(0, s.length() - 2));
                                } catch (Exception e) {
                                    fontSize = 14;
                                }
                                break;
                            case RUNNING:
//                                bottomText.setText(AppVaribles.getMessage("Loading..."));
                                browserActionBar.setDisable(true);
                                break;
                            default:
                                browserActionBar.setDisable(true);
                                break;
                        }
                    }
                }
            });

            loadedCompletely = new SimpleBooleanProperty(false);
            loadedCompletely.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (loadedCompletely.getValue()) {
                        startSnap();
                    } else {

                    }

                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void checkOneImage() {
        RadioButton selected = (RadioButton) snapGroup.getSelectedToggle();
        if (AppVaribles.getMessage("OneImage").equals(selected.getText())) {
            isOneImage = true;
            windowSizeCheck.setDisable(true);
        } else {
            isOneImage = false;
            windowSizeCheck.setDisable(false);
        }
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
            codesArea.setText(conString);
            webEngine.loadContent(conString);
            lastCodesLen = lastHtmlLen = contents.length();
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
            logger.debug("updateBrowser");
            webEngine.loadContent(htmlEdior.getHtmlText());
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void updateEditor() {
        try {
            logger.debug("updateEditor");
            if (!checkSavingForNextAction()) {
                return;
            }

//            String contents = getBrowserContents();
//            logger.debug(contents.length());
            String contents = (String) webEngine.executeScript("document.documentElement.outerHTML");
            logger.debug(contents.length());

            htmlEdior.setHtmlText(contents);
            codesArea.setText(contents);
            fileChanged.set(true);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    private String getBrowserContents() {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(webEngine.getDocument()), new StreamResult(stringWriter));
            String contents = stringWriter.getBuffer().toString();
            return contents;
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
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
                contents = codesArea.getText();
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
                contents = codesArea.getText();
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
            codesArea.setText("");
            lastCodesLen = lastHtmlLen = 0;
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

        loadWholePage();

    }

    private void loadWholePage() {
        try {
            loadedCompletely.set(false);
            snapsotButton.setDisable(true);
            final int maxDelay = 60000;
            final long startTime = new Date().getTime();
            snapingStage = openHandlingStage(Modality.WINDOW_MODAL);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                int lastHeight = 0, newHeight = -1;

                @Override
                public void run() {
                    if (new Date().getTime() - startTime > maxDelay) {
                        logger.debug(" TimeOver:" + newHeight);
                        this.cancel();
                        return;
                    }
                    if (newHeight == lastHeight) {
                        logger.debug(" Complete:" + newHeight);
                        this.cancel();
                    } else {
                        lastHeight = newHeight;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    newHeight = (Integer) webEngine.executeScript("document.body.scrollHeight");
                                    logger.debug(lastHeight + "  newHeight:" + newHeight);

                                    if (newHeight == lastHeight) {
                                        startSnap();
                                    } else {
                                        webEngine.executeScript("window.scrollTo(0," + newHeight + ");");
                                    }

                                } catch (Exception e) {
                                    logger.error(e.toString());
                                    if (snapingStage != null) {
                                        snapingStage.close();
                                    }
                                }
                            }
                        });
                    }
                }
            }, 0, delay);

        } catch (Exception e) {
            logger.error(e.toString());
            if (snapingStage != null) {
                snapingStage.close();
            }
        }

    }

    private void startSnap() {
        try {
            orginalStageHeight = (int) getMyStage().getHeight();
            orginalStageY = (int) myStage.getY();
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            myStage.setY(0);
            myStage.setHeight(primaryScreenBounds.getHeight());
            final int totalHeight = (Integer) webEngine.executeScript("document.body.scrollHeight");
            final int snapStep = (Integer) webEngine.executeScript("document.documentElement.clientHeight < document.body.clientHeight ? document.documentElement.clientHeight : document.body.clientHeight");
            final int width = (Integer) webEngine.executeScript("document.documentElement.clientWidth < document.body.clientWidth ? document.documentElement.clientWidth : document.body.clientWidth ");
            snapHeight = 0;
            snapCount = 0;
            webEngine.executeScript("window.scrollTo(0,0 );");
//        updateEditor();

            int scrollDelay = 300;
            bottomText.setText(AppVaribles.getMessage("SnapingImage..."));
            final SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (totalHeight <= snapHeight) {
                        this.cancel();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                myStage.setY(orginalStageY);
                                myStage.setHeight(orginalStageHeight);
                            }
                        });
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
                                            popError(AppVaribles.getMessage("Failed"));
                                        }

                                        webEngine.executeScript("window.scrollTo(0,0 );");
                                        bottomText.setText("");
                                        snapsotButton.setDisable(false);
                                        snapingStage.close();
                                    } else {
                                        webEngine.executeScript("window.scrollTo(0, " + snapHeight + ");");
                                    }
                                } catch (Exception e) {
                                    logger.error(e.toString());
                                    webEngine.executeScript("window.scrollTo(0,0 );");
                                    snapingStage.close();
                                    popError(AppVaribles.getMessage("Failed"));
                                }
                            }
                        });
                    }
                }
            }, delay, scrollDelay);
        } catch (Exception e) {
            logger.error(e.toString());
            if (snapingStage != null) {
                snapingStage.close();
            }
        }
    }

    @FXML
    private void zoomIn(ActionEvent event) {
//        ++fontSize;
//        webEngine.executeScript("document.body.style.fontSize = '" + fontSize + "px' ;");
        zoomScale += 0.1f;
        webView.setZoom(zoomScale);
    }

    @FXML
    private void zoomOut(ActionEvent event) {
//        --fontSize;
//        webEngine.executeScript("document.body.style.fontSize = '" + fontSize + "px' ;");
        zoomScale -= 0.1f;
        webView.setZoom(zoomScale);
    }

    @FXML
    private void backAction(ActionEvent event) {
        webEngine.executeScript("window.history.back();");
    }

    @FXML
    private void forwardAction(ActionEvent event) {
        webEngine.executeScript("window.history.forward();");
    }

    @FXML
    private void refreshAction(ActionEvent event) {
        webEngine.executeScript("location.reload() ;");
    }

    public void switchBroswerTab() {
        tabPane.getSelectionModel().select(browserTab);
    }

}
