package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-7-27
 * @License Apache License Version 2.0
 */
public abstract class BaseMapFramesController extends BaseMapController {

    protected boolean frameCompleted;
    protected int interval, frameIndex;

    @FXML
    protected Label frameLabel;
    @FXML
    protected ComboBox<String> intervalSelector, frameSelector;
    @FXML
    protected Button pauseButton;
    @FXML
    protected CheckBox loopCheck;

    public BaseMapFramesController() {
        baseTitle = Languages.message("Map");

        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        targetExtensionFilter = FileFilters.HtmlExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (frameSelector != null) {
                frameSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (isSettingValues) {
                                return;
                            }
                            if (timer != null) {
                                timer.cancel();
                                timer = null;
                            }
                            drawFrame(newValue);
                        });
            }

            interval = UserConfig.getInt(baseName + "Interval", 200);
            if (intervalSelector != null) {
                intervalSelector.getItems().addAll(Arrays.asList(
                        "200", "500", "1000", "50", "5", "3", "1", "10", "100", "300", "800", "1500", "2000", "3000", "5000", "10000"
                ));
                intervalSelector.setValue(interval + "");
                intervalSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            try {
                                int v = Integer.valueOf(intervalSelector.getValue());
                                if (v > 0) {
                                    interval = v;
                                    UserConfig.setInt(baseName + "Interval", interval);
                                    ValidationTools.setEditorNormal(intervalSelector);
                                    if (isSettingValues) {
                                        return;
                                    }
                                    drawFrames();
                                } else {
                                    ValidationTools.setEditorBadStyle(intervalSelector);
                                }
                            } catch (Exception e) {
                                MyBoxLog.error(e.toString());
                            }
                        });
            }

            if (loopCheck != null) {
                loopCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + "Loop", loopCheck.isSelected());
                });
                loopCheck.setSelected(UserConfig.getBoolean(baseName + "Loop", true));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void drawFrames() {

    }

    public void drawFrame(String value) {

    }

    public void fixFrameIndex() {

    }

    public void drawFrame() {

    }

    protected void setPause(boolean setAsPaused) {
        if (pauseButton == null) {
            return;
        }
        if (setAsPaused) {
            StyleTools.setNameIcon(pauseButton, Languages.message("Continue"), "iconPlay.png");
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            pauseButton.setUserData("paused");
        } else {
            StyleTools.setNameIcon(pauseButton, Languages.message("Pause"), "iconPause.png");
            previousButton.setDisable(true);
            nextButton.setDisable(true);
            pauseButton.setUserData("playing");
        }
        pauseButton.applyCss();
    }

    public void drawFrame(int index) {
        frameIndex = index;
        fixFrameIndex();
        drawFrame();
    }

    @FXML
    public void pauseAction() {
        if (pauseButton == null) {
            return;
        }
        Platform.runLater(() -> {
            if (pauseButton.getUserData() != null && "paused".equals(pauseButton.getUserData())) {
                setPause(false);
                drawFrames();

            } else {
                setPause(true);
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                frameIndex--;
            }
        });
    }

    @FXML
    @Override
    public void previousAction() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        drawFrame(frameIndex - 1);
        setPause(true);
    }

    @FXML
    @Override
    public void nextAction() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        drawFrame(frameIndex + 1);
        setPause(true);
    }

    @FXML
    protected void popSnapMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(Languages.message("HtmlDataAndCurrentFrame"));
            menu.setOnAction((ActionEvent event) -> {
                snapHtml();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(Languages.message("SnapCurrentFrame"));
            menu.setOnAction((ActionEvent event) -> {
                snapCurrentFrame();
            });
            popMenu.getItems().add(menu);

            snapAllMenu();

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected String snapFrameName() {
        String name = titleLabel.getText();
        if (name.isBlank()) {
            name = (Languages.message("Locations") + "_" + DateTools.datetimeToString(new Date()));
        }
        name += (!frameLabel.getText().isBlank() ? "_" + frameLabel.getText() : "");
        name += "_dpi" + dpi;
        return FileNameTools.filter(name);
    }

    protected void snapAllMenu() {
        MenuItem menu = new MenuItem(Languages.message("JpgAllFrames"));
        menu.setOnAction((ActionEvent event) -> {
            snapAllFrames("jpg");
        });
        popMenu.getItems().add(menu);

        menu = new MenuItem(Languages.message("PngAllFrames"));
        menu.setOnAction((ActionEvent event) -> {
            snapAllFrames("png");
        });
        popMenu.getItems().add(menu);

        menu = new MenuItem(Languages.message("GifAllFrames"));
        menu.setOnAction((ActionEvent event) -> {
            snapAllFrames("gif");
        });
        popMenu.getItems().add(menu);
    }

    protected void snapCurrentFrame() {
        String filename = snapFrameName() + ".png";
        File file = chooseSaveFile(UserConfig.getPath(VisitHistoryTools.getPathKey(VisitHistory.FileType.Image)),
                filename, FileFilters.ImageExtensionFilter);
        if (file == null) {
            return;
        }
        recordFileWritten(file, VisitHistory.FileType.Image);

        double scale = NodeTools.dpiScale(dpi);
        scale = scale > 1 ? scale : 1;
        SnapshotParameters snapPara = new SnapshotParameters();
        snapPara.setFill(Color.TRANSPARENT);
        snapPara.setTransform(Transform.scale(scale, scale));
        final Image mapSnap = snapBox.snapshot(snapPara, null);

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try {
                        String format = FileNameTools.suffix(file.getName());
                        format = format == null || format.isBlank() ? "png" : format;
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(mapSnap, null);
                        ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                        return file.exists();
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.error(e.toString());
                        return false;
                    }

                }

                @Override
                protected void whenSucceeded() {
                    ControllerTools.openImageViewer(file);
                }

            };
            start(task);
        }

    }

    protected void snapAllFrames(String format) {

    }

    @Override
    public void cleanPane() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (webEngine != null) {
                webEngine.getLoadWorker().cancel();
                webEngine = null;
            }
            mapOptionsController.cleanPane();

        } catch (Exception e) {
        }
        super.cleanPane();

    }

}
