package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.DirectoryChooser;
import mara.mybox.controller.ControlMapOptions.MapName;
import mara.mybox.db.data.BaseData;
import mara.mybox.db.data.Location;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.db.table.DataFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.imagefile.ImageGifFile;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.LocationTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-1-24
 * @License Apache License Version 2.0
 */
public class LocationDataMapController extends BaseMapController {

    protected LocationDataController dataController;
    protected String dataSet;
    protected List<Location> locations;
    protected boolean centered, snapEnd;
    protected int lastPointIndex;
    protected BitSet drawn;
    protected LoadingController loading;

    @FXML
    protected RadioButton sequenceRadio, distributionRadio;
    @FXML
    protected ToggleGroup displayGroup;
    @FXML
    protected VBox dataOptionsBox, playBox;
    @FXML
    protected CheckBox overlayCheck, centerCheck, accumulateCheck, linkCheck;
    @FXML
    protected Button snapshotButton;

    public LocationDataMapController() {
        baseTitle = Languages.message("Map") + " - " + Languages.message("LocationData");
        TipsLabelKey = "LocationDataMapComments";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            displayGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Sequence", sequenceRadio.isSelected());
                drawPoints();
            }
            );
            overlayCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Overlay", overlayCheck.isSelected());
                drawPoints();
            });
            accumulateCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Accumulate", accumulateCheck.isSelected());
                drawPoints();
            });
            linkCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Link", linkCheck.isSelected());
                drawPoints();
            });
            centerCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Center", centerCheck.isSelected());
                centered = !centerCheck.isSelected();
            });

            isSettingValues = true;
            if (UserConfig.getBoolean(baseName + "Sequence", true)) {
                sequenceRadio.fire();
            } else {
                distributionRadio.fire();
            }
            overlayCheck.setSelected(UserConfig.getBoolean(baseName + "Overlay", false));
            accumulateCheck.setSelected(UserConfig.getBoolean(baseName + "Accumulate", false));
            linkCheck.setSelected(UserConfig.getBoolean(baseName + "Link", false));
            centerCheck.setSelected(UserConfig.getBoolean(baseName + "Center", false));
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initMap(LocationDataController dataController) {
        this.dataController = dataController;
        super.initMap(dataController);
        NodeStyleTools.removeTooltip(snapshotButton);
    }

    protected void drawLocationData(List<Location> data, String title) {
        this.title = title;
        if (this.title == null) {
            this.title = "";
        } else {
            this.title = this.title.replaceAll("\n", " ");
        }
        titleLabel.setText(this.title);
        frameLabel.setText("");
        locations = data;
        drawPoints();
    }

    @Override
    public void drawPoints() {
        if (isSettingValues) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        clearMap();
        if (locations == null || locations.isEmpty()) {
            return;
        }
        Location center = locations.get(0);
        webEngine.executeScript("setCenter(" + center.getLongitude() + ", " + center.getLatitude() + ");");
        if (distributionRadio.isSelected()) {
            playBox.setDisable(true);
            drawLocations();
            return;
        }
        if (drawn == null) {
            drawn = new BitSet(locations.size());
        } else {
            drawn.clear();
        }
        playBox.setDisable(false);
        drawSequence();
    }

    protected void drawLocations() {
        try {
            if (!mapOptionsController.mapLoaded) {
                return;
            }
            webEngine.executeScript("clearMap();");
            frameLabel.setText("");
            if (locations == null || locations.isEmpty()) {
                return;
            }
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>(this) {

                    @Override
                    protected boolean handle() {
                        List<Location> points = new ArrayList<>();
                        Location point;
                        for (Location location : locations) {
                            if (!Location.valid(location)) {
                                continue;
                            }
                            if (mapOptionsController.mapName == MapName.GaoDe) {
                                point = LocationTools.toGCJ02(location);
                                if (point != null) {
                                    points.add(point);
                                }
                            } else if (mapOptionsController.mapName == MapName.TianDiTu) {
                                point = LocationTools.toCGCS2000(location);
                                if (point != null) {
                                    points.add(point);
                                }
                            }
                        }
                        locations = points;
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (locations == null || locations.isEmpty()) {
                            return;
                        }
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                        timer = new Timer();
                        timer.schedule(new TimerTask() {

                            private int index = 0;
                            private boolean frameEnd = true, centered = false;

                            @Override
                            public void run() {
                                Platform.runLater(() -> {
                                    if (!frameEnd || timer == null) {
                                        return;
                                    }
                                    if (locations == null || locations.isEmpty()) {
                                        if (timer != null) {
                                            timer.cancel();
                                            timer = null;
                                        }
                                        return;
                                    }
                                    frameEnd = false;
                                    Location location = locations.get(index);
                                    drawPoint(location.getLongitude(), location.getLatitude(),
                                            markerLabel(location), markerImage(location),
                                            DataFactory.displayData(dataController.tableDefinition, location, null, true),
                                            textColor(location));
                                    if (!centered) {
                                        webEngine.executeScript("setCenter(" + location.getLongitude() + ", " + location.getLatitude() + ");");
                                        centered = true;
                                    }
                                    index++;
                                    frameLabel.setText(Languages.message("DataNumber") + ":" + index);
                                    if (index >= locations.size()) {
                                        if (timer != null) {
                                            timer.cancel();
                                            timer = null;
                                        }
                                        if (mapOptionsController.mapName == ControlMapOptions.MapName.GaoDe
                                                && mapOptionsController.fitViewCheck.isSelected()) {
                                            webEngine.executeScript("map.setFitView();");
                                        }
                                    }
                                    frameEnd = true;
                                });
                            }
                        }, 0, 1); // Interface may be blocked if put all points in map altogether.
                    }
                };
                start(task);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    protected String writePointsTable() {
        if (locations != null && !locations.isEmpty()) {
            return writeLocationsTable();
        } else {
            return "";
        }
    }

    protected String writeLocationsTable() {
        if (locations == null || locations.isEmpty()) {
            return "";
        }
        List<BaseData> list = new ArrayList<>();
        for (Location location : locations) {
            if (task == null || task.isCancelled()) {
                return "";
            }
            list.add(location);
        }
        return DataFactory.htmlDataList(dataController.tableDefinition, list, null);
    }

    public String markerImage(Location location) {
        if (mapOptionsController.markerDatasetRadio != null
                && mapOptionsController.markerDatasetRadio.isSelected()) {
            if (location.getDataset() != null && location.getDataset().getImage() != null) {
                File image = location.getDataset().getImage();
                if (image.exists()) {
                    return image.getAbsolutePath();
                }
            }
        } else if (mapOptionsController.markerDataRadio != null
                && mapOptionsController.markerDataRadio.isSelected()) {
            File file = location.getImage();
            if (file != null && file.exists()) {
                return file.getAbsolutePath();
            }
        }
        return markerImage();
    }

    public String markerLabel(Location location) {
        String label = "";
        if (mapOptionsController.markerDatasetCheck.isSelected()) {
            label += location.getDatasetName();
        }
        if (mapOptionsController.markerLabelCheck.isSelected()) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            label += location.getLabel();
        }
        if (mapOptionsController.markerAddressCheck.isSelected()
                && location.getAddress() != null && !location.getAddress().isBlank()) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            label += location.getAddress();
        }
        if (mapOptionsController.markerCoordinateCheck.isSelected()
                && Location.valid(location)) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            label += location.getLongitude() + "," + location.getLatitude();
            if (location.getAltitude() != AppValues.InvalidDouble) {
                label += "," + location.getAltitude();
            }
        }
        if (mapOptionsController.markerStartCheck.isSelected()
                && location.getStartTime() != AppValues.InvalidLong) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            if (mapOptionsController.markerEndCheck.isSelected()
                    && location.getEndTime() != AppValues.InvalidLong) {
                if (location.getStartTime() != location.getEndTime()) {
                    label += DateTools.textEra(location.getStartEra()) + " - "
                            + DateTools.textEra(location.getEndEra());
                } else {
                    label += DateTools.textEra(location.getStartEra());
                }
            } else {
                label += DateTools.textEra(location.getStartEra());
            }
        } else if (mapOptionsController.markerEndCheck.isSelected()
                && location.getEndTime() != AppValues.InvalidLong) {
            label += " - " + DateTools.textEra(location.getEndEra());
        }
        if (mapOptionsController.markerDurationCheck.isSelected()) {
            String s = location.getDurationText();
            if (s != null) {
                if (!label.isBlank()) {
                    label += "</BR>";
                }
                label += Languages.message("Duration") + ":" + s;
            }
        }
        if (mapOptionsController.markerSpeedCheck.isSelected()
                && location.getSpeed() != AppValues.InvalidDouble) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            label += Languages.message("Speed") + ":" + location.getSpeed();
        }
        if (mapOptionsController.markerDirectionCheck.isSelected()
                && location.getDirection() != AppValues.InvalidInteger) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            label += Languages.message("Direction") + ":" + location.getDirection();
        }
        if (mapOptionsController.markerValueCheck.isSelected()
                && location.getDataValue() != AppValues.InvalidDouble) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            label += Languages.message("Value") + ":" + location.getDataValue();
        }
        if (mapOptionsController.markerSizeCheck.isSelected()
                && location.getDataValue() != AppValues.InvalidDouble) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            label += Languages.message("Size") + ":" + location.getDataSize();
        }
        return label;
    }

    public Color textColor(Location location) {
        if (mapOptionsController.setColorRadio.isSelected()) {
            return (Color) (mapOptionsController.colorSetController.rect.getFill());
        } else if (location != null && location.getDataset() != null) {
            return location.getDataset().getTextColor();
        }
        return Color.BLACK;
    }

    protected void drawSequence() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            setPause(false);
            if (!mapOptionsController.mapLoaded) {
                return;
            }
            clearMap();
            if (locations == null || locations.isEmpty()) {
                return;
            }
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>(this) {

                    List<String> startTimes;

                    @Override
                    protected boolean handle() {
                        List<Location> points = new ArrayList<>();
                        startTimes = new ArrayList<>();
                        Location point;
                        for (Location location : locations) {
                            if (!Location.valid(location)) {
                                continue;
                            }
                            if (mapOptionsController.mapName == MapName.GaoDe) {
                                point = LocationTools.toGCJ02(location);
                                if (point != null) {
                                    points.add(point);
                                }
                            } else if (mapOptionsController.mapName == MapName.TianDiTu) {
                                point = LocationTools.toCGCS2000(location);
                                if (point != null) {
                                    points.add(point);
                                }
                            }
                        }
                        locations = points;
                        Collections.sort(locations, new Comparator<Location>() {
                            @Override
                            public int compare(Location v1, Location v2) {
                                if (v1.getStartTime() > v2.getStartTime()) {
                                    return 1;
                                } else if (v1.getStartTime() < v2.getStartTime()) {
                                    return -1;
                                } else {
                                    return 0;
                                }
                            }
                        });
                        for (int i = 0; i < locations.size(); i++) {
                            Location location = locations.get(i);
                            String period = location.getPeriodText();
                            if (period != null) {
                                startTimes.add(i + "  " + period);
                            } else {
                                startTimes.add(i + "   ");
                            }
                        }
                        drawn = new BitSet(locations.size());
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        isSettingValues = true;
                        frameSelector.getItems().clear();
                        frameSelector.getItems().addAll(startTimes);
                        isSettingValues = false;
                        initFrames();
                        drawFrames();
                    }
                };
                start(task);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void initFrames() {
        if (isSettingValues) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        setPause(false);
        frameIndex = 0;
        frameCompleted = true;
        clearMap();
    }

    @Override
    public void drawFrames() {
        if (isSettingValues) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (locations == null || locations.isEmpty()) {
            return;
        }
        if (interval <= 0) {
            interval = 1000;
        }
        frameCompleted = true;
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (!frameCompleted || timer == null) {
                        return;
                    }
                    if (locations == null || locations.isEmpty()) {
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                        return;
                    }
                    drawFrame(frameIndex);
                    frameIndex++;
                    if (locations.size() == 1
                            || (!loopCheck.isSelected() && frameIndex >= locations.size())) {
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                    }
                });
            }
        }, 0, interval);
    }

    @Override
    public void fixFrameIndex() {
        if (frameIndex > locations.size() - 1) {
            frameIndex -= locations.size();
        } else if (frameIndex < 0) {
            frameIndex += locations.size();
        }
    }

    @Override
    public void drawFrame(String frame) {
        if (frame == null || frame.isBlank()) {
            return;
        }
        try {
            int pos = frame.indexOf(" ");
            if (pos < 0) {
                return;
            }
            String value = frame.substring(0, pos);
            int v = Integer.valueOf(value.trim());
            if (v >= 0 && v < locations.size()) {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                drawFrame(v);
                setPause(true);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void drawFrame() {
        try {
            if (!mapOptionsController.mapLoaded || !frameCompleted
                    || locations == null || locations.isEmpty()) {
                return;
            }
            frameCompleted = false;
            if (!accumulateCheck.isSelected() || frameIndex <= 0) {
                clearMap();
            }
            Location frame = locations.get(frameIndex);
            String mapTitle = frame.getPeriodText();
            if (mapTitle != null) {
                frameLabel.setText(mapTitle);
            } else {
                frameLabel.setText(frameIndex + "");
            }
            centered = !centerCheck.isSelected();
            if (!overlayCheck.isSelected() || mapTitle == null) {
                drawPoint(frame);
                lastPointIndex = frameIndex;
                frameCompleted = true;
                return;
            }
            long frameStart = frame.getStartTime();
            long frameEnd = frame.getEndTime();
            for (int i = 0; i < locations.size(); ++i) {
                if (drawn.get(i)) {
                    continue;
                }
                Location location = locations.get(i);
                if (!Location.valid(location)) {
                    continue;
                }
                long locationStart = location.getStartTime();
                long locationEnd = location.getEndTime();
                if (locationEnd < frameStart || locationStart > frameEnd) {
                    continue;
                }
                if (drawPoint(location)) {
                    lastPointIndex = i;
                    drawn.set(i);
                }
            }
            frameCompleted = true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public boolean drawPoint(Location location) {
        try {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            if (!centered) {
                webEngine.executeScript("setCenter(" + longitude + ", " + latitude + ");");
                centered = true;
            }
            Color color = textColor(location);
            drawPoint(longitude, latitude,
                    markerLabel(location), markerImage(location),
                    DataFactory.displayData(dataController.tableDefinition, location, null, true),
                    color);
            if (linkCheck.isSelected() && lastPointIndex >= 0) {
                Location lastPoint = locations.get(lastPointIndex);
                String pColor = color == null ? "null" : "'" + FxColorTools.color2rgb(color) + "'";
                webEngine.executeScript("drawLine("
                        + lastPoint.getLongitude() + ", " + lastPoint.getLatitude() + ", "
                        + longitude + ", " + latitude + ", " + pColor + ");");
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public void clearMap() {
        if (mapOptionsController.mapLoaded) {
            webEngine.executeScript("clearMap();");
        }
        lastPointIndex = -1;
        if (locations != null && !locations.isEmpty()) {
            if (drawn == null) {
                drawn = new BitSet(locations.size());
            } else {
                drawn.clear();
            }
        }
        frameLabel.setText("");
    }

    @FXML
    @Override
    public void clearAction() {
        initFrames();
        locations = null;
    }

    @Override
    public void reloadData() {
        if (isSettingValues) {
            return;
        }
        dataController.reloadChart();
    }

    @Override
    protected void snapAllMenu() {
        if (sequenceRadio.isSelected()) {
            super.snapAllMenu();
        }
    }

    @Override
    protected void snapAllFrames(String format) {
        if (distributionRadio.isSelected()) {
            snapHtml();
            return;
        }
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = UserConfig.getPath(VisitHistoryTools.getPathKey(VisitHistory.FileType.Image));
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            recordFileWritten(directory, VisitHistory.FileType.Image);

            String snapName = snapName(false);
            File filePath = new File(directory.getAbsolutePath() + File.separator + snapName + File.separator);
            filePath.mkdirs();
            String filePrefix = filePath + File.separator + snapName;

            if (loading != null) {
                loading.closeStage();
                loading = null;
            }
            initFrames();

            String rformat = format.equals("gif") ? "png" : format;
            final SnapshotParameters snapPara;
            final double scale;
            double scalev = NodeTools.dpiScale(dpi);
            scale = scalev > 1 ? scalev : 1;
            snapPara = new SnapshotParameters();
            snapPara.setFill(Color.TRANSPARENT);
            snapPara.setTransform(Transform.scale(scale, scale));

            Bounds bounds = snapBox.getLayoutBounds();
            int imageWidth = (int) Math.round(bounds.getWidth() * scale);
            int imageHeight = (int) Math.round(bounds.getHeight() * scale);

            List<File> snapshots = new ArrayList<>();
            loading = handling();
            snapEnd = true;
            timer = new Timer();
            timer.schedule(new TimerTask() {

                private Timer frameTimer;

                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (!snapEnd || timer == null) {
                            return;
                        }
                        if (loading == null || loading.isIsCanceled()
                                || locations == null || locations.isEmpty()) {
                            if (timer != null) {
                                timer.cancel();
                                timer = null;
                            }
                            if (frameTimer != null) {
                                frameTimer.cancel();
                                frameTimer = null;
                            }
                            if (loading != null) {
                                loading.closeStage();
                                loading = null;
                            }
                            initFrames();
                            drawFrames();
                            return;
                        }
                        snapEnd = false;
                        Platform.runLater(() -> {
                            loading.setInfo(Languages.message("Snapping") + ": " + (frameIndex + 1) + "/" + locations.size());
                        });
                        if (frameTimer != null) {
                            frameTimer.cancel();
                            frameTimer = null;
                        }
                        drawFrame(frameIndex);
                        frameTimer = new Timer();
                        frameTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (frameTimer == null) {
                                    return;
                                }
                                if (frameCompleted) {
                                    if (frameTimer != null) {
                                        frameTimer.cancel();
                                        frameTimer = null;
                                    }
                                    Platform.runLater(() -> {
                                        snap();
                                    });
                                }
                            }
                        }, 50, 50);
                    });
                }

                private void snap() {
                    try {
                        Image snap = snapBox.snapshot(snapPara, new WritableImage(imageWidth, imageHeight));
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snap, null);
                        File imageFile = new File(filePrefix + "_Frame" + frameIndex + "." + rformat);
                        ImageFileWriters.writeImageFile(bufferedImage, rformat, imageFile.getAbsolutePath());
                        snapshots.add(imageFile);

                        frameIndex++;
                        if (frameIndex >= locations.size()) {
                            if (timer != null) {
                                timer.cancel();
                                timer = null;
                            }
                            if (frameTimer != null) {
                                frameTimer.cancel();
                                frameTimer = null;
                            }
                            if (snapshots.size() == 1) {
                                ControllerTools.openImageViewer(snapshots.get(0));
                            } else if (snapshots.size() > 1) {
                                if (format.equals("gif")) {
                                    File gifFile = new File(filePrefix + ".gif");
                                    Platform.runLater(() -> {
                                        if (loading != null) {
                                            loading.setInfo(Languages.message("Saving") + ": " + gifFile);
                                        }
                                    });
                                    ImageGifFile.writeImageFiles(snapshots, gifFile, interval, true);
                                    if (gifFile.exists()) {
                                        Platform.runLater(() -> {
                                            if (loading != null) {
                                                loading.setInfo(Languages.message("Opening") + ": " + gifFile);
                                            }
                                        });
                                        ImagesEditorController controller = (ImagesEditorController) openStage(Fxmls.ImagesEditorFxml);
                                        controller.open(gifFile);
                                    }
                                } else {
                                    browseURI(filePath.toURI());
                                }
                            }
                            if (loading != null) {
                                loading.closeStage();
                                loading = null;
                            }
                            initFrames();
                            drawFrames();
                        }

                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                    snapEnd = true;
                }

            }, 0, 100);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    @Override
    public void cleanPane() {
        try {
            if (loading != null) {
                loading.closeStage();
                loading = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
