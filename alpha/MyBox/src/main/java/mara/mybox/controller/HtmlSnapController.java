package mara.mybox.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.CropTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileNameTools;
import static mara.mybox.tools.PdfTools.defaultFont;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileFilters;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;

/**
 * @Author Mara
 * @CreateDate 2020-10-20
 * @License Apache License Version 2.0
 */
public class HtmlSnapController extends BaseWebViewController {

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
        baseTitle = message("HtmlSnap");
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

            delay = UserConfig.getInt(baseName + "Delay", 2000);
            if (delay <= 0) {
                delay = 2000;
            }
            delayBox.getItems().addAll(Arrays.asList("2", "3", "5", "1", "10"));
            delayBox.getSelectionModel().select(delay + "");
            delayBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            delay = v * 1000;
                            UserConfig.setInt(baseName + "Delay", v);
                            ValidationTools.setEditorNormal(delayBox);
                        } else {
                            ValidationTools.setEditorBadStyle(delayBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(delayBox);
                    }
                }
            });

            snapGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkType();
                }
            });
            checkType();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void checkType() {
        RadioButton selected = (RadioButton) snapGroup.getSelectedToggle();
        if (message("OneImage").equals(selected.getText())) {
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

    @FXML
    @Override
    public void saveAsAction() {
        File file = chooseSaveFile(defaultTargetName("Snap-"));
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
                    if (isCanceled() || newHeight == lastHeight || new Date().getTime() - startTime > maxDelay) {
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
                            loadingController.setInfo(message("CurrentPageHeight") + ": " + newHeight);
                            if (newHeight == lastHeight) {
                                loadingController.setInfo(message("ExpandingPage"));
                                startSnap();
                            } else {
                                webEngine.executeScript("window.scrollTo(0," + newHeight + ");");
                            }

                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                            stopSnap();
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
            if (isCanceled()) {
                return;
            }
            webEngine.executeScript("window.scrollTo(0,0 );");
            snapTotalHeight = (Integer) webEngine.executeScript("document.body.scrollHeight");
            snapStep = (Integer) webEngine.executeScript("document.documentElement.clientHeight < document.body.clientHeight ? document.documentElement.clientHeight : document.body.clientHeight");
            snapHeight = 0;
            webViewLabel.setText(message("SnapingImage..."));

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
            if (isCanceled()) {
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
            if (isCanceled()) {
                return;
            }
            loadingController.setInfo(message("CurrentPageHeight") + ": " + snapHeight);
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
                            if (isCanceled()) {
                                return;
                            }
                            snap();
                        });
                    }
                }, 300);    // make sure page is loaded before snapping

            } else { // last snap
                if (isCanceled()) {
                    return;
                }
                loadingController.setInfo(message("WritingFile"));
                boolean success = true;
                if (isOneImage) {
                    Runtime r = Runtime.getRuntime();
                    long availableMem = r.maxMemory() - (r.totalMemory() - r.freeMemory()) / (1024 * 1024L);
                    long requiredMem = snapFileWidth * snapFileHeight * 5L / (1024 * 1024) + 200;
                    if (availableMem < requiredMem) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle(getBaseTitle());
                        alert.setContentText(MessageFormat.format(message("MergedSnapshotTooLarge"), availableMem, requiredMem));
                        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                        ButtonType buttonPdf = new ButtonType(message("SaveAsPdf"));
                        ButtonType buttonCancel = new ButtonType(message("Cancel"));
                        alert.getButtonTypes().setAll(buttonPdf, buttonCancel);
                        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                        stage.setAlwaysOnTop(true);
                        stage.toFront();

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == buttonPdf) {
                            success = htmlIntoPdf(snaps, targetFile, windowSizeCheck.isSelected());
                        } else {
                            success = false;
                        }
                    } else {
                        BufferedImage finalImage = mergeImagesVertical(snaps, snapFileWidth, snapFileHeight);
                        if (finalImage != null) {
                            if (isCanceled()) {
                                success = false;
                            } else {
                                String format = FileNameTools.getFileSuffix(targetFile.getAbsolutePath());
                                ImageFileWriters.writeImageFile(finalImage, format, targetFile.getAbsolutePath());
                            }
                        } else {
                            success = false;
                        }
                    }
                } else {
                    success = htmlIntoPdf(snaps, targetFile, windowSizeCheck.isSelected());
                }

                stopSnap();

                if (success && targetFile.exists()) {
                    view(targetFile);
                } else {
                    popFailed();
                }

            }

        } catch (Exception e) {
            webEngine.executeScript("window.scrollTo(0,0 );");
            popFailed();
            stopSnap();
        }

    }

    public BufferedImage mergeImagesVertical(List<File> files, int width, int height) {
        if (files == null || files.isEmpty()) {
            return null;
        }
        try {
            BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = target.createGraphics();
            int y = 0;
            for (File file : files) {
                if (isCanceled()) {
                    return null;
                }
                BufferedImage image = ImageFileReaders.readImage(file);
                if (image == null) {
                    continue;
                }
                int imageWidth = (int) image.getWidth();
                int imageHeight = (int) image.getHeight();
                g.drawImage(image, 0, y, imageWidth, imageHeight, null);
                y += imageHeight;
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public boolean htmlIntoPdf(List<File> files, File targetFile, boolean isImageSize) {
        if (files == null || files.isEmpty()) {
            return false;
        }
        try {
            int count = 0;
            try ( PDDocument document = new PDDocument(AppVariables.pdfMemUsage)) {
                PDPageContentStream content;
                PDFont font = defaultFont(document);
                PDDocumentInformation info = new PDDocumentInformation();
                info.setCreationDate(Calendar.getInstance());
                info.setModificationDate(Calendar.getInstance());
                info.setProducer("MyBox v" + AppValues.AppVersion);
                document.setDocumentInformation(info);
                document.setVersion(1.0f);
                PDRectangle pageSize = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
                int marginSize = 20, total = files.size();
                for (File file : files) {
                    if (isCanceled()) {
                        document.close();
                        return false;
                    }
                    BufferedImage bufferedImage = ImageFileReaders.readImage(file);
                    if (bufferedImage == null) {
                        continue;
                    }
                    PDImageXObject imageObject;
                    imageObject = LosslessFactory.createFromImage(document, bufferedImage);
                    if (isImageSize) {
                        pageSize = new PDRectangle(imageObject.getWidth() + marginSize * 2, imageObject.getHeight() + marginSize * 2);
                    }
                    PDPage page = new PDPage(pageSize);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    float w, h;
                    if (isImageSize) {
                        w = imageObject.getWidth();
                        h = imageObject.getHeight();
                    } else {
                        if (imageObject.getWidth() > imageObject.getHeight()) {
                            w = page.getTrimBox().getWidth() - marginSize * 2;
                            h = imageObject.getHeight() * w / imageObject.getWidth();
                        } else {
                            h = page.getTrimBox().getHeight() - marginSize * 2;
                            w = imageObject.getWidth() * h / imageObject.getHeight();
                        }
                    }
                    content.drawImage(imageObject, marginSize, page.getTrimBox().getHeight() - marginSize - h, w, h);

                    content.beginText();
                    content.setFont(font, 12);
                    content.newLineAtOffset(w + marginSize - 80, 5);
                    content.showText((++count) + " / " + total);
                    content.endText();

                    content.close();
                }

                if (isCanceled()) {
                    document.close();
                    return false;
                }
                PDPage page = document.getPage(0);
                PDPageXYZDestination dest = new PDPageXYZDestination();
                dest.setPage(page);
                dest.setZoom(1f);
                dest.setTop((int) page.getCropBox().getHeight());
                PDActionGoTo action = new PDActionGoTo();
                action.setDestination(dest);
                document.getDocumentCatalog().setOpenAction(action);

                document.save(targetFile);
                return true;
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }

    }

    protected boolean isCanceled() {
        if (loadingController == null || loadingController.isIsCanceled()) {
            stopSnap();
            return true;
        } else {
            return false;
        }
    }

    protected void stopSnap() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (loadingController != null) {
            loadingController.closeStage();
            loadingController = null;
        }
        snaps = null;
        webEngine.getLoadWorker().cancel();
        webEngine.executeScript("window.scrollTo(0,0 );");
        webViewLabel.setText("");
        myStage.setY(orginalStageY);
        myStage.setHeight(orginalStageHeight);
    }

    @Override
    public void cleanPane() {
        try {
            stopSnap();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
