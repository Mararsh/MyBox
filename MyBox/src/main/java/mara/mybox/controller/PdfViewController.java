package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import mara.mybox.data.DoubleRectangle;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.data.PdfInformation;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.PdfTools;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;
import org.apache.pdfbox.rendering.ImageType;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfViewController extends ImageViewerController {

    private PdfInformation pdfInformation;
    private int currentPage, currentPageTmp, percent, dpi;
    protected SimpleBooleanProperty infoLoaded;
    private boolean isTransparent, scrollEnd, scrollStart, scrolledSet;

    @FXML
    protected Label pageLabel;
    @FXML
    protected TextField pageInput;
    @FXML
    protected ComboBox<String> sizeBox, dpiBox;
    @FXML
    protected CheckBox transCheck;
    @FXML
    protected HBox pageNavBox;

    public PdfViewController() {
        baseTitle = AppVaribles.getMessage("PdfView");

        SourceFileType = VisitHistory.FileType.PDF;
        SourcePathType = VisitHistory.FileType.PDF;
        TargetFileType = VisitHistory.FileType.PDF;
        TargetPathType = VisitHistory.FileType.PDF;
        AddFileType = VisitHistory.FileType.PDF;
        AddPathType = VisitHistory.FileType.PDF;

        sourcePathKey = "PdfSourcePath";
        TipsLabelKey = "PdfViewTips";
        ImageSelectKey = "PdfViewSelectKey";
        ImageRulerXKey = "PdfViewRulerXKey";
        ImageRulerYKey = "PdfViewRulerYKey";
        ImagePopCooridnateKey = "PdfViewPopCooridnateKey";

        fileExtensionFilter = CommonValues.PdfExtensionFilter;
    }

    @Override
    public void initializeNext2() {
        try {
            infoLoaded = new SimpleBooleanProperty(false);

            operation1Box.disableProperty().bind(Bindings.not(infoLoaded));
            pageNavBox.disableProperty().bind(Bindings.not(infoLoaded));

            transCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    isTransparent = transCheck.isSelected();
                    loadPage();
                }
            });
            FxmlControl.quickTooltip(transCheck, new Tooltip(AppVaribles.getMessage("OnlyForTexts")));

            sizeBox.getItems().addAll(Arrays.asList("100", "75", "50", "125", "150", "200", "80", "25", "30", "15"));
            sizeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            percent = v;
                            setSize(percent);
                            FxmlControl.setEditorNormal(sizeBox);
                        } else {
                            FxmlControl.setEditorBadStyle(sizeBox);
                        }

                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(sizeBox);
                    }
                }
            });

            dpiBox.getItems().addAll(Arrays.asList("96", "72", "120", "160", "240", "300", "400", "600"));
            dpiBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            dpi = v;
                            loadPage();
                            FxmlControl.setEditorNormal(dpiBox);
                        } else {
                            FxmlControl.setEditorBadStyle(dpiBox);
                        }

                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(dpiBox);
                    }
                }
            });
            dpiBox.getSelectionModel().select(0);

            pageInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (pageNavBox.isDisabled()) {
                        currentPageTmp = 0;
                        pageInput.setStyle(null);
                        return;
                    }
                    try {
                        int v = Integer.valueOf(pageInput.getText()) - 1;
                        if (v >= 0 && v < pdfInformation.getNumberOfPages()) {
                            currentPageTmp = v;
                            pageInput.setStyle(null);
                            goButton.setDisable(false);
                        } else {
                            pageInput.setStyle(badStyle);
                            goButton.setDisable(true);
                        }
                    } catch (Exception e) {
                        pageInput.setStyle(badStyle);
                        goButton.setDisable(true);
                    }
                }
            });

            scrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (scrolledSet) {
                        scrolledSet = false;
                        return;
                    }
                    if (scrollPane.getVvalue() == scrollPane.getVmax()) {
                        if (scrollEnd) {
                            scrollEnd = false;
                            nextAction();
                        } else {
                            scrollEnd = true;
                            scrolledSet = true;
                            scrollPane.setVvalue(0.99);
                        }
                    } else {
                        scrollEnd = false;
                    }

                    if (scrollPane.getVvalue() == scrollPane.getVmin()) {
                        if (scrollStart) {
                            scrollStart = false;
                            previousAction();
                        } else {
                            scrollStart = true;
                            scrolledSet = true;
                            scrollPane.setVvalue(0.01);
                        }
                    } else {
                        scrollStart = false;
                    }
                }
            });
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initOperation2Box() {

        operation2Box.disableProperty().bind(
                Bindings.isNull(imageView.imageProperty())
        );

    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        FxmlControl.quickTooltip(nextButton, new Tooltip(getMessage("NextPage") + "\nENTER / PAGE DOWN"));
        FxmlControl.quickTooltip(previousButton, new Tooltip(getMessage("PreviousPage") + "\nPAGE UP"));
        FxmlControl.quickTooltip(firstButton, new Tooltip(getMessage("FirstPage") + "\nCTRL+HOME"));
        FxmlControl.quickTooltip(lastButton, new Tooltip(getMessage("LastPage") + "\nCTRL+END"));
    }

    private void setSize(int percent) {
        if (imageView.getImage() == null) {
            return;
        }
        scrolledSet = true;
        double w = imageView.getImage().getWidth();
        imageView.setFitWidth(w * percent / 100);
        double h = imageView.getImage().getHeight();
        imageView.setFitHeight(h * percent / 100);
    }

    @Override
    public void sourceFileChanged(final File file) {
        if (file == null) {
            return;
        }
        super.sourceFileChanged(file);
        loadFile(file, null, 0);
    }

    public void loadFile(File file, PdfInformation pdfInfo, int page) {
        try {
            imageView.setImage(null);
            imageView.setTranslateX(0);
            pdfInformation = null;
            currentPage = page;
            infoLoaded.set(false);
            pageInput.setText("1");
            pageLabel.setText("");
            percent = 0;
            if (file == null) {
                return;
            }
            sourceFile = file;
            if (pdfInfo != null) {
                pdfInformation = pdfInfo;
            } else {
                pdfInformation = new PdfInformation(sourceFile);
            }
            getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath());
            loadPage();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void loadInformation() {
        if (pdfInformation == null) {
            return;
        }
        bottomLabel.setText(AppVaribles.getMessage("CountingTotalNumber"));
        backgroundTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                pdfInformation.readInformation(null);
                infoLoaded.set(true);

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        bottomLabel.setText("");
                        pageLabel.setText("/" + pdfInformation.getNumberOfPages());
                    }
                });
            }
        };
        Thread thread = new Thread(backgroundTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadPage() {
        if (pdfInformation == null) {
            return;
        }
        if (currentPage < 0) {
            currentPage = 0;
        } else if (infoLoaded.get() && currentPage >= pdfInformation.getNumberOfPages()) {
            currentPage = pdfInformation.getNumberOfPages() - 1;
        }
        isSettingValues = true;
        pageInput.setText((currentPage + 1) + "");
        isSettingValues = false;
        previousButton.setDisable(currentPage <= 0);
        nextButton.setDisable(infoLoaded.get() && currentPage >= (pdfInformation.getNumberOfPages() - 1));
        bottomLabel.setText("");
        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                ImageType type = ImageType.RGB;
                if (isTransparent) {
                    type = ImageType.ARGB;
                }
                BufferedImage bufferedImage = PdfTools.page2image(sourceFile, null, currentPage, dpi, type);
                image = SwingFXUtils.toFXImage(bufferedImage, null);

                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setPreserveRatio(true);
                            imageView.setImage(image);
                            if (percent == 0) {
                                sizeBox.getSelectionModel().select("100");
                            }
                            setSize(percent);
                            refinePane();
                            setMaskStroke();
                            checkSelect();
                            if (!infoLoaded.get()) {
                                loadInformation();
                            }
                            setImageChanged(false);
                            imageView.requestFocus();
                        }
                    });
                }
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void setImageChanged(boolean imageChanged) {
        this.imageChanged = imageChanged;
        updateLabelTitle();
    }

    @Override
    public void setDafultMaskRectangleValues() {
        if (imageView == null || maskPane == null || maskRectangleLine == null) {
            return;
        }
        if (maskRectangleData == null
                || maskRectangleData.getWidth() > imageView.getImage().getWidth()
                || maskRectangleData.getHeight() > imageView.getImage().getHeight()) {
            maskRectangleData = new DoubleRectangle(0, 0,
                    imageView.getImage().getWidth() - 1, imageView.getImage().getHeight() - 1);
        }
    }

    @Override
    public PdfViewController refresh() {
        File oldfile = sourceFile;
        PdfInformation oldInfo = pdfInformation;
        int oldPage = currentPage;

        PdfViewController c = (PdfViewController) refreshBase();
        if (c == null) {
            return null;
        }
        c.loadFile(oldfile, oldInfo, oldPage);
        return c;
    }

    @FXML
    @Override
    public void infoAction() {
        if (pdfInformation == null) {
            return;
        }
        try {
            final PdfInformationController controller = (PdfInformationController) openStage(CommonValues.PdfInformationFxml);
            controller.setInformation(pdfInformation);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void nextAction() {
        if (pdfInformation == null
                || currentPage >= pdfInformation.getNumberOfPages() - 1) {
            return;
        }
        currentPage++;
        loadPage();
    }

    @FXML
    @Override
    public void previousAction() {
        if (pdfInformation == null || currentPage <= 0) {
            return;
        }
        currentPage--;
        loadPage();
    }

    @FXML
    @Override
    public void firstAction() {
        if (pdfInformation == null) {
            return;
        }
        currentPage = 0;
        loadPage();
    }

    @FXML
    @Override
    public void lastAction() {
        if (pdfInformation == null) {
            return;
        }
        currentPage = pdfInformation.getNumberOfPages() - 1;
        loadPage();

    }

    @FXML
    protected void goPageAction() {
        currentPage = currentPageTmp;
        loadPage();
    }

    public void setSizeBox() {
        scrolledSet = true;
        percent = (int) (imageView.getFitHeight() * 100 / imageView.getImage().getHeight());
        isSettingValues = true;
        sizeBox.getSelectionModel().select(percent + "");
        isSettingValues = false;
    }

    @FXML
    @Override
    public void loadedSize() {
        scrolledSet = true;
        super.loadedSize();
        setSizeBox();
    }

    @FXML
    @Override
    public void paneSize() {
        scrolledSet = true;
        super.paneSize();
        setSizeBox();
    }

    @FXML
    @Override
    public void zoomIn() {
        scrolledSet = true;
        super.zoomIn();
        setSizeBox();
    }

    @FXML
    @Override
    public void zoomOut() {
        scrolledSet = true;
        super.zoomOut();
        setSizeBox();
    }

    @FXML
    @Override
    public void saveAction() {
        saveAsAction();
    }

}
