package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.CropTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-10-20
 * @License Apache License Version 2.0
 */
public class HtmlSnapController extends WebAddressController {

    protected int delay, orginalStageWidth, orginalStageHeight, orginalStageX, orginalStageY;
    protected int lastHtmlLen, lastCodesLen;
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
    protected ComboBox<String> delaySelector;

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
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            delay = UserConfig.getInt(baseName + "Delay", 2);
            if (delay <= 0) {
                delay = 2000;
            }
            delaySelector.getItems().addAll(Arrays.asList("2", "3", "5", "1", "10"));
            delaySelector.getSelectionModel().select(delay + "");
            delaySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            delay = v * 1000;
                            UserConfig.setInt(baseName + "Delay", v);
                            ValidationTools.setEditorNormal(delaySelector);
                        } else {
                            ValidationTools.setEditorBadStyle(delaySelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(delaySelector);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @FXML
    @Override
    public void snapAction() {
        try {
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            orginalStageWidth = (int) getMyStage().getWidth();
            orginalStageHeight = (int) getMyStage().getHeight();
            orginalStageX = (int) myStage.getX();
            orginalStageY = (int) myStage.getY();

            int pageWidth = (Integer) webEngine.executeScript("document.documentElement.scrollWidth || document.body.scrollWidth;");
            if (pageWidth > 0) {
                myStage.setX(0);
                myStage.setWidth(pageWidth + 40);
            }
            myStage.setY(0);
            myStage.setHeight(primaryScreenBounds.getHeight());

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
                            loadingController.setInfo(message("Height") + ": " + newHeight);
                            startSnap();

                        } catch (Exception e) {
                            MyBoxLog.error(e);
                            stopSnap();
                        }
                    });
                    Platform.requestNextPulse();

                }
            }, 0, delay);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    protected void startSnap() {
        try {
            if (isCanceled()) {
                return;
            }
            webEngine.executeScript("window.scrollTo(0,0 );");
            snapTotalHeight = (Integer) webEngine.executeScript("document.body.scrollHeight");
            snapStep = (Integer) webEngine.executeScript("document.documentElement.clientHeight < document.body.clientHeight ? "
                    + "document.documentElement.clientHeight : document.body.clientHeight");
            snapHeight = 0;
            setWebViewLabel(message("SnapingImage..."));

            // http://news.kynosarges.org/2017/02/01/javafx-snapshot-scaling/
            final Bounds bounds = webView.getLayoutBounds();
            snapScale = NodeTools.dpiScale(dpi);
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
                    Platform.requestNextPulse();
                }
            }, 2000);    // make sure page is loaded before snapping

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void snap() {
        try {
            if (isCanceled()) {
                return;
            }
            Image snapshot = webView.snapshot(snapParameters, null);
            Image cropped;
            if (snapTotalHeight < snapHeight + snapStep) { // last snap
                cropped = CropTools.cropOutsideFx(null, snapshot, 0,
                        (int) ((snapStep + snapHeight - snapTotalHeight) * snapScale),
                        (int) snapshot.getWidth(), (int) snapshot.getHeight());
            } else {
                cropped = snapshot;
            }
            if (cropped.getWidth() > snapFileWidth) {
                snapFileWidth = (int) cropped.getWidth();
            }
            snapFileHeight += cropped.getHeight();
            snapHeight += snapStep;
            File tmpfile = FileTmpTools.getTempFile(".png");
            ImageFileWriters.writeImageFile(null,
                    SwingFXUtils.fromFXImage(cropped, null), "png", tmpfile.getAbsolutePath());
            snaps.add(tmpfile);
            if (isCanceled()) {
                return;
            }
            loadingController.setInfo(message("CurrentPageHeight") + ": " + snapHeight
                    + "\n" + message("Count") + ": " + snaps.size());
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
                        Platform.requestNextPulse();
                    }
                }, 300);    // make sure page is loaded before snapping

            } else { // last snap
                if (isCanceled()) {
                    return;
                }
                stopSnap();
            }

        } catch (Exception e) {
            webEngine.executeScript("window.scrollTo(0,0 );");
            popFailed();
            stopSnap();
        }

    }

    protected boolean isCanceled() {
        if (loadingController == null || loadingController.canceled()) {
            stopSnap();
            return true;
        } else {
            return false;
        }
    }

    protected void stopSnap() {
        Platform.runLater(() -> {
            try {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                if (loadingController != null) {
                    loadingController.closeStage();
                    loadingController = null;
                }
                if (snaps != null && !snaps.isEmpty()) {
                    ImagesEditorController.openFiles(snaps);
                }
                if (webEngine != null) {
                    webEngine.getLoadWorker().cancel();
                    webEngine.executeScript("window.scrollTo(0,0 );");
                    setWebViewLabel("");
                }
                if (getMyStage() != null) {
                    myStage.setX(orginalStageX);
                    myStage.setY(orginalStageY);
                    myStage.setWidth(orginalStageWidth);
                    myStage.setHeight(orginalStageHeight);
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
        });
        Platform.requestNextPulse();
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
