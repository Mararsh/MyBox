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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import mara.mybox.controller.MapOptionsController.MapName;
import mara.mybox.data.Location;
import mara.mybox.data.StringTable;
import mara.mybox.data.VisitHistory;
import mara.mybox.data.tools.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.image.file.ImageGifFile;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.LocationTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-1-24
 * @License Apache License Version 2.0
 */
public class LocationDataMapController extends MapBaseController {

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
    protected VBox viewBox, dataOptionsBox;
    @FXML
    protected CheckBox overlayCheck, centerCheck, accumulateCheck, linkCheck;
    @FXML
    protected FlowPane sequenceOptionsPane;
    @FXML
    protected Button snapshotButton;

    public LocationDataMapController() {
        baseTitle = message("Map") + " - " + message("LocationData");
        TipsLabelKey = "LocationDataMapComments";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            displayGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                        if (isSettingValues) {
                            return;
                        }
                        AppVariables.setUserConfigValue(baseName + "Sequence", sequenceRadio.isSelected());
                        drawPoints();
                    }
            );
            overlayCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        if (isSettingValues) {
                            return;
                        }
                        AppVariables.setUserConfigValue(baseName + "Overlay", overlayCheck.isSelected());
                        drawPoints();
                    });
            accumulateCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        if (isSettingValues) {
                            return;
                        }
                        AppVariables.setUserConfigValue(baseName + "Accumulate", accumulateCheck.isSelected());
                        drawPoints();
                    });
            linkCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        if (isSettingValues) {
                            return;
                        }
                        AppVariables.setUserConfigValue(baseName + "Link", linkCheck.isSelected());
                        drawPoints();
                    });
            centerCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        if (isSettingValues) {
                            return;
                        }
                        AppVariables.setUserConfigValue(baseName + "Center", centerCheck.isSelected());
                        centered = !centerCheck.isSelected();
                    });

            isSettingValues = true;
            if (AppVariables.getUserConfigBoolean(baseName + "Sequence", true)) {
                sequenceRadio.fire();
            } else {
                distributionRadio.fire();
            }
            overlayCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Overlay", false));
            accumulateCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Accumulate", false));
            linkCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Link", false));
            centerCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Center", false));
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initMap(LocationDataController dataController) {
        this.dataController = dataController;
        parentController = dataController;

        initSplitPanes();
        FxmlControl.removeTooltip(snapshotButton);
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
            playBox.setVisible(false);
            if (dataOptionsBox.getChildren().contains(sequenceOptionsPane)) {
                dataOptionsBox.getChildren().remove(sequenceOptionsPane);
            }
            drawLocations();
            return;
        }
        if (drawn == null) {
            drawn = new BitSet(locations.size());
        } else {
            drawn.clear();
        }
        playBox.setVisible(true);
        if (!dataOptionsBox.getChildren().contains(sequenceOptionsPane)) {
            dataOptionsBox.getChildren().add(sequenceOptionsPane);
        }
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
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        List<Location> points = new ArrayList<>();
                        Location point;
                        for (Location location : locations) {
                            if (!location.validCoordinate()) {
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
                                            markerLabel(location), markerImage(location), location.info("</BR>"),
                                            textColor(location));
                                    if (!centered) {
                                        webEngine.executeScript("setCenter(" + location.getLongitude() + ", " + location.getLatitude() + ");");
                                        centered = true;
                                    }
                                    index++;
                                    frameLabel.setText(message("DataNumber") + ":" + index);
                                    if (index >= locations.size()) {
                                        if (timer != null) {
                                            timer.cancel();
                                            timer = null;
                                        }
                                        if (mapOptionsController.mapName == MapOptionsController.MapName.GaoDe
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
                if (parentController != null) {
                    parentController.openHandlingStage(task, Modality.WINDOW_MODAL, "Loading map data");
                } else {
                    openHandlingStage(task, Modality.WINDOW_MODAL, "Loading map data");
                }
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
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
        List<String> names = new ArrayList<>();
        names.addAll(dataController.tableDefinition.importAllFields());
        StringTable table = new StringTable(names);
        for (Location location : locations) {
            if (task == null || task.isCancelled()) {
                return "";
            }
            List<String> row = new ArrayList<>();
            row.addAll(Location.externalValues(location));
            table.add(row);
        }
        return StringTable.tableDiv(table);
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
                && location.validCoordinate()) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            label += location.getLongitude() + "," + location.getLatitude();
            if (location.getAltitude() != CommonValues.InvalidDouble) {
                label += "," + location.getAltitude();
            }
        }
        if (mapOptionsController.markerStartCheck.isSelected()
                && location.getStartTime() != CommonValues.InvalidLong) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            if (mapOptionsController.markerEndCheck.isSelected()
                    && location.getEndTime() != CommonValues.InvalidLong) {
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
                && location.getEndTime() != CommonValues.InvalidLong) {
            label += " - " + DateTools.textEra(location.getEndEra());
        }
        if (mapOptionsController.markerDurationCheck.isSelected()) {
            String s = location.getDurationText();
            if (s != null) {
                if (!label.isBlank()) {
                    label += "</BR>";
                }
                label += message("Duration") + ":" + s;
            }
        }
        if (mapOptionsController.markerSpeedCheck.isSelected()
                && location.getSpeed() != CommonValues.InvalidDouble) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            label += message("Speed") + ":" + location.getSpeed();
        }
        if (mapOptionsController.markerDirectionCheck.isSelected()
                && location.getDirection() != CommonValues.InvalidInteger) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            label += message("Direction") + ":" + location.getDirection();
        }
        if (mapOptionsController.markerValueCheck.isSelected()
                && location.getDataValue() != CommonValues.InvalidDouble) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            label += message("Value") + ":" + location.getDataValue();
        }
        if (mapOptionsController.markerSizeCheck.isSelected()
                && location.getDataValue() != CommonValues.InvalidDouble) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            label += message("Size") + ":" + location.getDataSize();
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
                task = new SingletonTask<Void>() {

                    List<String> startTimes;

                    @Override
                    protected boolean handle() {
                        List<Location> points = new ArrayList<>();
                        startTimes = new ArrayList<>();
                        Location point;
                        for (Location location : locations) {
                            if (!location.validCoordinate()) {
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
                if (parentController != null) {
                    parentController.openHandlingStage(task, Modality.WINDOW_MODAL, "Loading map data");
                } else {
                    openHandlingStage(task, Modality.WINDOW_MODAL, "Loading map data");
                }
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
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
                if (!location.validCoordinate()) {
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
                    markerLabel(location), markerImage(location), location.info("</BR>"),
                    color);
            if (linkCheck.isSelected() && lastPointIndex >= 0) {
                Location lastPoint = locations.get(lastPointIndex);
                String pColor = color == null ? "null" : "'" + FxmlColor.color2rgb(color) + "'";
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

    @FXML
    protected void popSnapMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("HtmlDataAndCurrentFrame"));
            menu.setOnAction((ActionEvent event) -> {
                super.htmlAction(title, viewBox);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("SnapCurrentFrame"));
            menu.setOnAction((ActionEvent event) -> {
                snapCurrentFrame();
            });
            popMenu.getItems().add(menu);

            if (sequenceRadio.isSelected()) {

                menu = new MenuItem(message("JpgAllFrames"));
                menu.setOnAction((ActionEvent event) -> {
                    snapAllFrames("jpg");
                });
                popMenu.getItems().add(menu);

                menu = new MenuItem(message("PngAllFrames"));
                menu.setOnAction((ActionEvent event) -> {
                    snapAllFrames("png");
                });
                popMenu.getItems().add(menu);

                menu = new MenuItem(message("GifAllFrames"));
                menu.setOnAction((ActionEvent event) -> {
                    snapAllFrames("gif");
                });
                popMenu.getItems().add(menu);

            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void snapCurrentFrame() {
        String name = titleLabel.getText()
                + (!frameLabel.getText().isBlank() ? " " + frameLabel.getText() : "")
                + ".png";
        File file = chooseSaveFile(AppVariables.getUserConfigPath(VisitHistoryTools.getPathKey(VisitHistory.FileType.Image)),
                name, CommonFxValues.ImageExtensionFilter);
        if (file == null) {
            return;
        }
        recordFileWritten(file, VisitHistory.FileType.Image);

        double scale = dpi / Screen.getPrimary().getDpi();
        scale = scale > 1 ? scale : 1;
        SnapshotParameters snapPara = new SnapshotParameters();
        snapPara.setFill(Color.TRANSPARENT);
        snapPara.setTransform(Transform.scale(scale, scale));

        Bounds bounds = viewBox.getLayoutBounds();
        int imageWidth = (int) Math.round(bounds.getWidth() * scale);
        int imageHeight = (int) Math.round(bounds.getHeight() * scale);
        WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
        final Image mapSnap = viewBox.snapshot(snapPara, snapshot);

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        String format = FileTools.getFileSuffix(file);
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
                    FxmlStage.openImageViewer(file);
                }

            };
            if (parentController != null) {
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    protected void snapAllFrames(String format) {
        if (distributionRadio.isSelected()) {
            super.htmlAction(title, viewBox);
            return;
        }
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVariables.getUserConfigPath(VisitHistoryTools.getPathKey(VisitHistory.FileType.Image));
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            recordFileWritten(directory, VisitHistory.FileType.Image);

            String name = title.replaceAll("\\\"|\n|:", "");
            String filePath = directory.getAbsolutePath() + File.separator + name + File.separator;
            new File(filePath).mkdirs();
            final String filePrefix = filePath + File.separator + name;

            if (loading != null) {
                loading.closeStage();
                loading = null;
            }
            initFrames();

            String rformat = format.equals("gif") ? "png" : format;
            final SnapshotParameters snapPara;
            final double scale;
            double scalev = dpi / Screen.getPrimary().getDpi();
            scale = scalev > 1 ? scalev : 1;
            snapPara = new SnapshotParameters();
            snapPara.setFill(Color.TRANSPARENT);
            snapPara.setTransform(Transform.scale(scale, scale));

            Bounds bounds = viewBox.getLayoutBounds();
            int imageWidth = (int) Math.round(bounds.getWidth() * scale);
            int imageHeight = (int) Math.round(bounds.getHeight() * scale);

            List<File> snapshots = new ArrayList<>();
            loading = openHandlingStage(Modality.WINDOW_MODAL);
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
                            loading.setInfo(message("Snapping") + ": " + (frameIndex + 1) + "/" + locations.size());
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
                        Image snap = viewBox.snapshot(snapPara, new WritableImage(imageWidth, imageHeight));
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
                                FxmlStage.openImageViewer(snapshots.get(0));
                            } else if (snapshots.size() > 1) {
                                if (format.equals("gif")) {
                                    File gifFile = new File(filePrefix + ".gif");
                                    Platform.runLater(() -> {
                                        if (loading != null) {
                                            loading.setInfo(message("Saving") + ": " + gifFile);
                                        }
                                    });
                                    ImageGifFile.writeImageFiles(snapshots, gifFile, interval, true);
                                    if (gifFile.exists()) {
                                        Platform.runLater(() -> {
                                            if (loading != null) {
                                                loading.setInfo(message("Opening") + ": " + gifFile);
                                            }
                                        });
                                        ImageGifViewerController controller
                                                = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml);
                                        controller.sourceFileChanged(gifFile);
                                    }
                                } else {
                                    browseURI(directory.toURI());
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
    public boolean leavingScene() {
        try {
            if (loading != null) {
                loading.closeStage();
                loading = null;
            }
        } catch (Exception e) {
        }
        return super.leavingScene();
    }

}
