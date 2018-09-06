package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.InputEvent;
import javafx.scene.paint.Color;
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
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.tools.FxImageTools;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class HtmlEditorController extends TextEditorController {

    private final String HtmlFilePathKey, HtmlImagePathKey;
    private WebEngine webEngine;
    private int cols, rows;
    protected int lastHtmlLen, snapHeight, snapCount;
    private boolean isSettingValues;
    private URL url;
    private List<Image> images;

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
    private TextField urlInput;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ToolBar browserToolbar;

    public HtmlEditorController() {

        HtmlFilePathKey = "HtmlFilePathKey";
        HtmlImagePathKey = "HtmlImagePathKey";

        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("html", "*.html", "*.htm"));
            }
        };

    }

    @Override
    protected void initializeNext() {
        try {
            urlInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        url = new URL(newValue);
                        if (url.getProtocol().toLowerCase().startsWith("http")) {
                            urlInput.setStyle(null);
                        } else {
                            urlInput.setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        urlInput.setStyle(badStyle);
                    }
                }
            });

            htmlEdior.addEventHandler(InputEvent.ANY, new EventHandler<InputEvent>() {
                @Override
                public void handle(InputEvent event) {
                    int len = htmlEdior.getHtmlText().length();
                    if (!isSettingValues && len != lastHtmlLen) {
                        fileChanged.set(true);
                    }
                    lastHtmlLen = len;
                    bottomText.setText(AppVaribles.getMessage("Total") + ": " + len);
                }
            });

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

            loadButton.disableProperty().bind(
                    Bindings.isEmpty(urlInput.textProperty())
                            .or(urlInput.styleProperty().isEqualTo(badStyle))
            );

            updateEditorButton.disableProperty().bind(
                    Bindings.isEmpty(urlInput.textProperty())
                            .or(urlInput.styleProperty().isEqualTo(badStyle))
            );

//            snapsotButton.disableProperty().bind(
//                    webEngine.documentProperty().isNull()
//            );
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void openAction(ActionEvent event) {
        try {
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
                AppVaribles.setConfigValue("LastPath", file.getParent());
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
        File path = new File(AppVaribles.getConfigValue(HtmlImagePathKey, System.getProperty("user.home")));
        fileChooser.setInitialDirectory(path);
        fileChooser.getExtensionFilters().addAll(CommonValues.ImageExtensionFilter);
        final File file = fileChooser.showSaveDialog(getMyStage());
        if (file == null) {
            return;
        }
        AppVaribles.setConfigValue("LastPath", file.getParent());
        AppVaribles.setConfigValue(HtmlImagePathKey, file.getParent());
        final File imageFile = file;
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
                                    snapshot = FxImageTools.cropImage(snapshot, 0, snapStep - (totalHeight - snapHeight),
                                            width - 1, (int) snapshot.getHeight() - 1);
                                } else {
                                    snapshot = FxImageTools.cropImage(snapshot, 0, 0,
                                            width - 1, (int) snapshot.getHeight() - 1);
                                }
                                images.add(snapshot);
                                snapHeight += snapStep;
                                if (totalHeight <= snapHeight) { // last snap
                                    Image finalImage = FxImageTools.combineSingleColumn(images);

                                    if (finalImage != null) {
                                        String format = FileTools.getFileSuffix(imageFile.getAbsolutePath());
                                        final BufferedImage bufferedImage = FxImageTools.getWritableData(finalImage, format);
                                        ImageFileWriters.writeImageFile(bufferedImage, format, imageFile.getAbsolutePath());
                                        openImageManufactureInNew(imageFile.getAbsolutePath());
                                    }

                                    bottomText.setText("");
                                } else {
                                    webEngine.executeScript("window.scrollTo(0, " + snapHeight + ");");
                                }
                            } catch (Exception e) {
                                logger.error(e.toString());
                            }
                        }
                    });
                }
            }
        }, 0, 500);

    }

}
