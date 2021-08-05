package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mara.mybox.bufferedimage.CombineTools;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.CropTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-10-20
 * @License Apache License Version 2.0
 */
public class HtmlSnapController extends BaseHtmlController {

    protected int delay, orginalStageHeight, orginalStageY;
    protected int lastHtmlLen, lastCodesLen;
    protected boolean isOneImage;
    protected List<File> snaps;
    protected int cols, rows;
    protected int lastTextLen;
    protected SnapshotParameters snapParameters;
    protected int snapFileWidth, snapFileHeight, snapsTotal,
            snapImageWidth, snapImageHeight, snapTotalHeight, snapHeight, snapStep;
    protected double snapScale;
    protected LoadingController loadingController;

    @FXML
    protected Button snapshotButton;
    @FXML
    protected ComboBox<String> delayBox;
    @FXML
    protected ToggleGroup snapGroup;
    @FXML
    protected CheckBox windowSizeCheck;
    @FXML
    protected HBox snapBox;

    public HtmlSnapController() {
        baseTitle = Languages.message("HtmlSnap");
        TipsLabelKey = "HtmlSnapComments";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            lastCodesLen = lastHtmlLen = 0;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initSnap();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initSnap() {
        try {
            if (snapBox == null) {
                return;
            }
            delay = 2000;
            delayBox.getItems().addAll(Arrays.asList("2", "3", "5", "1", "10"));
            delayBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            delay = v * 1000;
                            UserConfig.setUserConfigInt(baseName + "Delay", v);
                            ValidationTools.setEditorNormal(delayBox);
                        } else {
                            ValidationTools.setEditorBadStyle(delayBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(delayBox);
                    }
                }
            });
            delayBox.getSelectionModel().select(UserConfig.getUserConfigInt(baseName + "Delay", 2) + "");

            snapGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkOneImage();
                }
            });
            checkOneImage();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void checkOneImage() {
        RadioButton selected = (RadioButton) snapGroup.getSelectedToggle();
        if (Languages.message("OneImage").equals(selected.getText())) {
            isOneImage = true;
            windowSizeCheck.setDisable(true);

            TargetPathType = VisitHistory.FileType.Image;
            TargetFileType = VisitHistory.FileType.Image;
            targetExtensionFilter = FileFilters.ImageExtensionFilter;

        } else {
            isOneImage = false;
            windowSizeCheck.setDisable(false);

            TargetPathType = VisitHistory.FileType.PDF;
            TargetFileType = VisitHistory.FileType.PDF;
            targetExtensionFilter = FileFilters.PdfExtensionFilter;
        }
    }

    @Override
    protected void updateStatus(boolean changed) {
        String t = getBaseTitle();
        if (webviewController.address != null) {
            t += "  " + webviewController.address;
        } else if (sourceFile != null) {
            t += "  " + sourceFile.getAbsolutePath();
        }
        getMyStage().setTitle(t);
    }

    @FXML
    @Override
    public void saveAsAction() {
        File file;
        String name = "Snap" + (webviewController.sourceFile != null ? "_" + FileNameTools.filenameFilter(webviewController.sourceFile.getName()) : "")
                + "_" + DateTools.nowFileString();
        if (isOneImage) {
            file = chooseSaveFile(UserConfig.getUserConfigPath(VisitHistoryTools.getPathKey(VisitHistory.FileType.Image)),
                    name + ".png", FileFilters.ImageExtensionFilter);
        } else {
            file = chooseSaveFile(UserConfig.getUserConfigPath(VisitHistoryTools.getPathKey(VisitHistory.FileType.PDF)),
                    name + ".pdf", FileFilters.PdfExtensionFilter);
        }
        if (file == null) {
            return;
        }
        if (isOneImage) {
            recordFileWritten(file, VisitHistory.FileType.Image);
        } else {
            recordFileWritten(file, VisitHistory.FileType.PDF);
        }
        targetFile = file;
        loadWholePage();
    }

    protected void loadWholePage() {
        try {
            orginalStageHeight = (int) getMyStage().getHeight();
            orginalStageY = (int) myStage.getY();
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            myStage.setY(0);
            myStage.setHeight(primaryScreenBounds.getHeight());
//            myStage.setWidth(900);
//            webEngine.executeScript("document.body.style.fontSize = '15px' ;");

            snapshotButton.setDisable(true);
            final int maxDelay = delay * 30;
            final long startTime = new Date().getTime();

            if (loadingController != null) {
                loadingController.closeStage();
                loadingController = null;
            }
            loadingController = handling();

            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                int lastHeight = 0, newHeight = -1;

                @Override
                public void run() {
                    boolean quit = false;
                    if (new Date().getTime() - startTime > maxDelay) {
                        quit = true;
                    }
                    if (newHeight == lastHeight) {
                        quit = true;
                    }
                    if (quit) {
                        this.cancel();
                        return;
                    }

                    lastHeight = newHeight;
                    Platform.runLater(() -> {
                        try {
                            newHeight = (Integer) webEngine.executeScript("document.body.scrollHeight");
                            loadingController.setInfo(Languages.message("CurrentPageHeight") + ": " + newHeight);
                            if (newHeight == lastHeight) {
                                loadingController.setInfo(Languages.message("ExpandingPage"));
                                startSnap();
                            } else {
                                webEngine.executeScript("window.scrollTo(0," + newHeight + ");");
                            }

                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                            if (loadingController != null) {
                                loadingController.closeStage();
                                loadingController = null;
                            }
                        }
                    });

                }
            }, 0, delay);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void startSnap() {
        try {
            webEngine.executeScript("window.scrollTo(0,0 );");
            snapTotalHeight = (Integer) webEngine.executeScript("document.body.scrollHeight");
            snapStep = (Integer) webEngine.executeScript("document.documentElement.clientHeight < document.body.clientHeight ? document.documentElement.clientHeight : document.body.clientHeight");
            snapHeight = 0;
            webLabel.setText(Languages.message("SnapingImage..."));

            // http://news.kynosarges.org/2017/02/01/javafx-snapshot-scaling/
            final Bounds bounds = webView.getLayoutBounds();
            snapScale = dpi / Screen.getPrimary().getDpi();
            snapScale = snapScale > 1 ? snapScale : 1;
            snapImageWidth = (int) Math.round(bounds.getWidth() * snapScale);
            snapImageHeight = (int) Math.round(bounds.getHeight() * snapScale);
            snapParameters = new SnapshotParameters();
            snapParameters.setFill(Color.TRANSPARENT);
            snapParameters.setTransform(javafx.scene.transform.Transform.scale(snapScale, snapScale));

            snaps = new ArrayList<>();
            snapsTotal = snapTotalHeight % snapStep == 0
                    ? snapTotalHeight / snapStep : snapTotalHeight / snapStep + 1;
            snapFileWidth = snapFileHeight = 0;

            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        snap();
                    });
                }
            }, 2000);    // make sure page is loaded before snapping

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void snap() {
        try {
            if (loadingController == null) {
                return;
            }
            WritableImage snapshot = new WritableImage(snapImageWidth, snapImageHeight);
            snapshot = webView.snapshot(snapParameters, snapshot);
            Image cropped;
            if (snapTotalHeight < snapHeight + snapStep) { // last snap
                cropped = CropTools.cropOutsideFx(snapshot, 0,
                        (int) ((snapStep + snapHeight - snapTotalHeight) * snapScale),
                        (int) snapshot.getWidth() - 1, (int) snapshot.getHeight() - 1);
            } else {
                cropped = snapshot;
            }
            if (cropped.getWidth() > snapFileWidth) {
                snapFileWidth = (int) cropped.getWidth();
            }
            snapFileHeight += cropped.getHeight();
            snapHeight += snapStep;
            File tmpfile = TmpFileTools.getTempFile(".png");
            ImageFileWriters.writeImageFile(SwingFXUtils.fromFXImage(cropped, null), "png", tmpfile.getAbsolutePath());
            snaps.add(tmpfile);
            loadingController.setInfo(Languages.message("CurrentPageHeight") + ": " + snapHeight);
            if (snapTotalHeight > snapHeight) {
                webEngine.executeScript("window.scrollTo(0, " + snapHeight + ");");
                if (timer != null) {
                    timer.cancel();
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            if (loadingController == null) {
                                return;
                            }
                            snap();
                        });
                    }
                }, 300);    // make sure page is loaded before snapping

            } else { // last snap
                loadingController.setInfo(Languages.message("WritingFile"));
                boolean success = true;
                if (isOneImage) {
                    Runtime r = Runtime.getRuntime();
                    long availableMem = r.maxMemory() - (r.totalMemory() - r.freeMemory()) / (1024 * 1024L);
                    long requiredMem = snapFileWidth * snapFileHeight * 5L / (1024 * 1024) + 200;
                    if (availableMem < requiredMem) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle(getBaseTitle());
                        alert.setContentText(MessageFormat.format(Languages.message("MergedSnapshotTooLarge"), availableMem, requiredMem));
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        ButtonType buttonPdf = new ButtonType(Languages.message("SaveAsPdf"));
                        ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
                        alert.getButtonTypes().setAll(buttonPdf, buttonCancel);
                        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                        stage.setAlwaysOnTop(true);
                        stage.toFront();

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == buttonPdf) {
                            success = PdfTools.htmlIntoPdf(snaps, targetFile, windowSizeCheck.isSelected());
                        } else {
                            success = false;
                        }
                    } else {
                        BufferedImage finalImage = CombineTools.mergeImagesVertical(snaps, snapFileWidth, snapFileHeight);
                        if (finalImage != null) {
                            String format = FileNameTools.getFileSuffix(targetFile.getAbsolutePath());
                            ImageFileWriters.writeImageFile(finalImage, format, targetFile.getAbsolutePath());
                        } else {
                            success = false;
                        }
                    }
                } else {
                    success = PdfTools.htmlIntoPdf(snaps, targetFile, windowSizeCheck.isSelected());
                }
                snaps = null;
                if (success && targetFile.exists()) {
                    view(targetFile);
                } else {
                    popFailed();
                }

                webEngine.executeScript("window.scrollTo(0,0 );");
                webLabel.setText("");
                snapshotButton.setDisable(false);

                if (loadingController != null) {
                    loadingController.closeStage();
                    loadingController = null;
                }
                myStage.setY(orginalStageY);
                myStage.setHeight(orginalStageHeight);
            }

        } catch (Exception e) {
            webEngine.executeScript("window.scrollTo(0,0 );");
            popFailed();
            if (loadingController != null) {
                loadingController.closeStage();
                loadingController = null;
            }
        }

    }

}
