package mara.mybox.controller;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-9-18
 * @Description
 * @License Apache License Version 2.0
 */
public class WebBrowser implements Initializable {

    private static String WebBrowserLastUrlKey;
    private WebView webView;
    private WebEngine webEngine;
    private int cols, rows, delay, fontSize, orginalStageHeight, orginalStageY;
    protected int lastHtmlLen, lastCodesLen, snapHeight, snapCount;
    private boolean isSettingValues, isOneImage;
    private URL url;
    private List<Image> images;
    private File targetFile;
    protected SimpleBooleanProperty loadedCompletely;

    public WebBrowser() {

        WebBrowserLastUrlKey = "WebBrowserLastUrlKey";
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {

            initializeNext();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initializeNext() {

    }

    protected void initBroswer(final WebView webView) {
        try {
            this.webView = webView;
            webEngine = webView.getEngine();

            loadedCompletely = new SimpleBooleanProperty(false);
            loadedCompletely.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (loadedCompletely.getValue()) {

                    } else {

                    }

                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public static void initUrlBox(final ComboBox urlBox) {
        try {
            urlBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        URL url = new URL(newValue);
                        if (url.getProtocol().toLowerCase().startsWith("http")) {
                            urlBox.getEditor().setStyle(null);
                            AppVaribles.setConfigValue(WebBrowserLastUrlKey, newValue);
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
            String savedUrl = AppVaribles.getConfigValue(WebBrowserLastUrlKey, "");
            if (!savedUrl.isEmpty()) {
                urlBox.getItems().add(savedUrl);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

//    private void checkOneImage() {
//        RadioButton selected = (RadioButton) snapGroup.getSelectedToggle();
//        if (AppVaribles.getMessage("OneImage").equals(selected.getText())) {
//            isOneImage = true;
//            windowSizeCheck.setDisable(true);
//        } else {
//            isOneImage = false;
//            windowSizeCheck.setDisable(false);
//        }
//    }
    private void loadAddress(ActionEvent event) {
        try {

            webEngine.load(url.toString());

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public String getBrowserContents() {
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

//    private void snapshot() {
//        final FileChooser fileChooser = new FileChooser();
//        File path;
//        if (isOneImage) {
//            path = new File(AppVaribles.getConfigValue(HtmlImagePathKey, CommonValues.UserFilePath));
//            fileChooser.getExtensionFilters().addAll(CommonValues.ImageExtensionFilter);
//        } else {
//            path = new File(AppVaribles.getConfigValue(HtmlPdfPathKey, CommonValues.UserFilePath));
//            fileChooser.getExtensionFilters().addAll(CommonValues.PdfExtensionFilter);
//        }
//        fileChooser.setInitialDirectory(path);
//        final File file = fileChooser.showSaveDialog(getMyStage());
//        if (file == null) {
//            return;
//        }
//        AppVaribles.setConfigValue(LastPathKey, file.getParent());
//        if (isOneImage) {
//            AppVaribles.setConfigValue(HtmlImagePathKey, file.getParent());
//        } else {
//            AppVaribles.setConfigValue(HtmlPdfPathKey, file.getParent());
//        }
//        targetFile = file;
//        images = new ArrayList();
//
//        loadWholePage();
//
//    }
//
//    private void startSnap() {
//
//        final int totalHeight = (Integer) webEngine.executeScript("document.body.scrollHeight");
//        final int snapStep = (Integer) webEngine.executeScript("document.documentElement.clientHeight < document.body.clientHeight ? document.documentElement.clientHeight : document.body.clientHeight");
//        final int width = (Integer) webEngine.executeScript("document.documentElement.clientWidth < document.body.clientWidth ? document.documentElement.clientWidth : document.body.clientWidth ");
//        snapHeight = 0;
//        snapCount = 0;
//        webEngine.executeScript("window.scrollTo(0,0 );");
//        updateEditor();
//
//        bottomText.setText(AppVaribles.getMessage("SnapingImage..."));
//        final SnapshotParameters parameters = new SnapshotParameters();
//        parameters.setFill(Color.TRANSPARENT);
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if (totalHeight <= snapHeight) {
//                    this.cancel();
//                } else {
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//
//                                Image snapshot = webView.snapshot(parameters, null);
//                                if (totalHeight < snapHeight + snapStep) { // last snap
//                                    snapshot = FxmlImageTools.cropImage(snapshot, 0, snapStep - (totalHeight - snapHeight),
//                                            width - 1, (int) snapshot.getHeight() - 1);
//                                } else {
//                                    snapshot = FxmlImageTools.cropImage(snapshot, 0, 0,
//                                            width - 1, (int) snapshot.getHeight() - 1);
//                                }
//                                images.add(snapshot);
//                                snapHeight += snapStep;
//                                if (totalHeight <= snapHeight) { // last snap
//
//                                    boolean success = true;
//                                    if (isOneImage) {
//                                        Image finalImage = FxmlImageTools.combineSingleColumn(images);
//                                        if (finalImage != null) {
//                                            String format = FileTools.getFileSuffix(targetFile.getAbsolutePath());
//                                            final BufferedImage bufferedImage = FxmlImageTools.getWritableData(finalImage, format);
//                                            ImageFileWriters.writeImageFile(bufferedImage, format, targetFile.getAbsolutePath());
//                                        } else {
//                                            success = false;
//                                        }
//                                    } else {
//                                        success = PdfTools.htmlIntoPdf(images, targetFile, windowSizeCheck.isSelected());
//                                    }
//                                    if (success && targetFile.exists()) {
//                                        if (isOneImage) {
//                                            openImageManufactureInNew(targetFile.getAbsolutePath());
//                                        } else {
//                                            Desktop.getDesktop().browse(targetFile.toURI());
//                                        }
//                                    } else {
//                                        popError(AppVaribles.getMessage("Failed"));
//                                    }
//
//                                    webEngine.executeScript("window.scrollTo(0,0 );");
//                                    bottomText.setText("");
//                                    snapsotButton.setDisable(false);
//                                } else {
//                                    webEngine.executeScript("window.scrollTo(0, " + snapHeight + ");");
//                                }
//                            } catch (Exception e) {
//                                logger.error(e.toString());
//                                webEngine.executeScript("window.scrollTo(0,0 );");
//                                popError(AppVaribles.getMessage("Failed"));
//                            }
//                        }
//                    });
//                }
//            }
//        }, 5000, delay);
//
//    }
//
//    private void loadWholePage() {
//        orginalStageHeight = (int) getMyStage().getHeight();
//        orginalStageY = (int) myStage.getY();
//        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
//        myStage.setY(0);
//        myStage.setHeight(primaryScreenBounds.getHeight());
//
//        int loadDelay = 5000;
//        loadedCompletely.set(false);
//        snapsotButton.setDisable(true);
//
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            int lastHeight = 0, newHeight = -1;
//
//            @Override
//            public void run() {
//                if (newHeight == lastHeight) {
//                    logger.debug(" Complete:" + newHeight);
//                    this.cancel();
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            myStage.setY(orginalStageY);
//                            myStage.setHeight(orginalStageHeight);
//                        }
//                    });
//                } else {
//                    lastHeight = newHeight;
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                newHeight = (Integer) webEngine.executeScript("document.body.scrollHeight");
//                                logger.debug(lastHeight + "  newHeight:" + newHeight);
//
//                                if (newHeight == lastHeight) {
//                                    startSnap();
//                                } else {
//                                    webEngine.executeScript("window.scrollTo(0," + newHeight + ");");
//                                }
//
//                            } catch (Exception e) {
//                                logger.error(e.toString());
//                            }
//                        }
//                    });
//                }
//            }
//        }, 0, loadDelay);
//
//    }
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

}
