package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.LocationTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileFilters;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.SystemConfig;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-7-27
 * @License Apache License Version 2.0
 */
public abstract class BaseMapController extends BaseController {

    protected String title;
    protected boolean frameCompleted;
    protected WebEngine webEngine;
    protected int interval, frameIndex;

    @FXML
    protected WebView mapView;
    @FXML
    protected ControlMapOptions mapOptionsController;
    @FXML
    protected Label titleLabel, frameLabel;
    @FXML
    protected ComboBox<String> intervalSelector, frameSelector;
    @FXML
    protected Button pauseButton;
    @FXML
    protected CheckBox loopCheck;
    @FXML
    protected VBox snapBox;

    public BaseMapController() {
        baseTitle = Languages.message("Map");

        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        targetExtensionFilter = FileFilters.HtmlExtensionFilter;
    }

    /*
        methods need implementation
     */
    @Override
    public void initControls() {
        try {
            super.initControls();

            initWebEngine();

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
                intervalSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
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

            if (mapOptionsController != null) {
                mapOptionsController.initOptions(this);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void initMap(BaseController parent) {
        try {
            this.parentController = parent;
            initSplitPanes();

            if (SystemConfig.getBoolean("MapRunFirstTime" + AppValues.AppVersion, true)) {
                HtmlPopController controller = HtmlPopController.openHtml(parentController, LocationTools.gaodeMap());
                controller.handling(message("FirstRunInfo"));
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            controller.loadAddress(LocationTools.tiandituFile(true).toURI().toString());
                        });
                    }
                }, 2000);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            SystemConfig.setBoolean("MapRunFirstTime" + AppValues.AppVersion, false);
                            controller.closeStage();
                            if (parentController != null) {
                                parentController.reload();
                            }
                        });
                    }
                }, 4000);
            }
        } catch (Exception e) {
        }
    }

    protected void mapClicked(double longitude, double latitude) {

    }

    protected void mouseMoved(double longitude, double latitude) {

    }

    public void drawPoints() {
    }

    protected String writePointsTable() {
        return "";
    }

    public void drawFrames() {

    }

    public void drawFrame(String value) {

    }

    public void fixFrameIndex() {

    }

    public void drawFrame() {

    }

    public void reloadData() {

    }

    /*
        Common methods
     */
    public void initWebEngine() {
        try {
            if (mapView == null) {
                return;
            }

            webEngine = mapView.getEngine();
            webEngine.setJavaScriptEnabled(true);

            webEngine.setOnAlert((WebEvent<String> ev) -> {
                mapEvents(ev.getData());
            });

//            webEngine.setOnError((WebErrorEvent event) -> {
//                if (bottomLabel != null) {
//                    bottomLabel.setText(event.getMessage());
//                }
//            });
//            webEngine.setOnStatusChanged((WebEvent<String> ev) -> {
//                if (bottomLabel != null) {
//                    bottomLabel.setText(ev.getData());
//                }
//            });
//            webEngine.getLoadWorker().stateProperty().addListener(
//                    (ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) -> {
//                        try {
//                            switch (newState) {
//                                case RUNNING:
//                                    break;
//                                case SUCCEEDED:
//                                    break;
//                                case CANCELLED:
//                                    break;
//                                case FAILED:
//                                    break;
//                            }
//                        } catch (Exception e) {
//                            MyBoxLog.debug(e.toString());
//                        }
//                    });
//            webEngine.getLoadWorker().exceptionProperty().addListener(
//                    (ObservableValue<? extends Throwable> ov, Throwable ot, Throwable nt) -> {
//                        if (nt == null) {
//                            return;
//                        }
//                        bottomLabel.setText(nt.getMessage());
//                    });
//            webEngine.locationProperty().addListener(
//                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
//                        bottomLabel.setText(newv);
//                    });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void mapEvents(String data) {
        try {
//            MyBoxLog.debug(data);
            if (data.equals("Loaded")) {
                mapOptionsController.mapLoaded();
                return;
            } else if (data.startsWith("zoomSize:")) {
                int v = Integer.parseInt(data.substring("zoomSize:".length()));
                if (v != mapOptionsController.mapSize) {
                    mapOptionsController.setMapSize(v, false, true);
                }
                return;
            }
            boolean isClicked = true;
            if (data.startsWith("click:")) {
                data = data.substring(6);
            } else if (data.startsWith("move:")) {
                data = data.substring(5);
                isClicked = false;
            } else {
                return;
            }
            String[] values = data.split(",");
            double longitude = Double.valueOf(values[0]);
            double latitude = Double.valueOf(values[1]);
            if (isClicked) {
                mapClicked(longitude, latitude);
            } else {
                mouseMoved(longitude, latitude);
            }
            if (bottomLabel != null) {
                bottomLabel.setText(data);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void drawPoint(double longitude, double latitude,
            String label, String markerImage, String info, Color textColor) {
        try {
            if (webEngine == null
                    || !mapOptionsController.mapLoaded
                    || !LocationTools.validCoordinate(longitude, latitude)) {
                return;
            }
            String pLabel = jsString(label);
            String pInfo = jsString(mapOptionsController.popInfoCheck.isSelected() ? info : null);
            String pImage = markerImage;
            pImage = (pImage == null || pImage.trim().isBlank())
                    ? "null" : "'" + pImage.replaceAll("\\\\", "/") + "'";
            String pColor = textColor == null ? "null" : "'" + FxColorTools.color2rgb(textColor) + "'";
            webEngine.executeScript("addMarker("
                    + longitude + "," + latitude
                    + ", " + pLabel + ", " + pInfo + ", " + pImage
                    + ", " + mapOptionsController.markerSize
                    + ", " + mapOptionsController.textSize
                    + ", " + pColor + ", " + mapOptionsController.boldCheck.isSelected() + ");");
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected String jsString(String string) {
        return string == null ? "null"
                : "'" + string.replaceAll("'", AppValues.MyBoxSeparator).replaceAll("\n", "</BR>") + "'";
    }

    public String markerImage() {
        if (mapOptionsController.markerPointRadio == null || mapOptionsController.markerPointRadio.isSelected()) {
            return pointImage();
        }
        if (mapOptionsController.markerCircleRadio.isSelected()) {
            return circleImage();

        } else if (mapOptionsController.markerImageRadio.isSelected()) {
            if (mapOptionsController.markerImageFile != null && mapOptionsController.markerImageFile.exists()) {
                return mapOptionsController.markerImageFile.getAbsolutePath();
            }
        }
        return pointImage();
    }

    public String circleImage() {
        String path = "/" + StyleTools.getIconPath();
        return mara.mybox.fxml.FxFileTools.getInternalFile(path + "iconCircle.png", "map",
                AppVariables.ControlColor.name() + "Circle.png").getAbsolutePath();
    }

    public String pointImage() {
        String path = "/" + StyleTools.getIconPath();
        return mara.mybox.fxml.FxFileTools.getInternalFile(path + "iconLocation.png", "map",
                AppVariables.ControlColor.name() + "Point.png").getAbsolutePath();
    }

    public Color textColor() {
        if (mapOptionsController.setColorRadio.isSelected()) {
            return (Color) (mapOptionsController.colorSetController.rect.getFill());
        }
        return Color.BLACK;
    }

    @FXML
    @Override
    public void clearAction() {
        if (mapOptionsController.mapLoaded) {
            webEngine.executeScript("clearMap();");
        }
        titleLabel.setText("");
    }

    @FXML
    public void refreshAction() {
        drawPoints();
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
    public void aboutCoordinateSystem() {
        try {
            StringTable table = new StringTable(null, Languages.message("AboutCoordinateSystem"));
            table.newLinkRow("ChinaCommonGeospatialInformationServices", "https://www.tianditu.gov.cn/");
            table.newLinkRow("", "https://www.tianditu.gov.cn/world_coronavirusmap/");
            table.newLinkRow("ChineseCoordinateSystems", "https://politics.stackexchange.com/questions/40991/why-must-chinese-maps-be-obfuscated");
            table.newLinkRow("", "https://zhuanlan.zhihu.com/p/62243160");
            table.newLinkRow("", "https://blog.csdn.net/qq_36377037/article/details/86479796");
            table.newLinkRow("", "https://www.zhihu.com/question/31204062?sort=created");
            table.newLinkRow("", "https://blog.csdn.net/ssxueyi/article/details/102622156");
            table.newLinkRow("EPSGCodes", "http://epsg.io/4490");
            table.newLinkRow("", "http://epsg.io/4479");
            table.newLinkRow("", "http://epsg.io/4326");
            table.newLinkRow("", "http://epsg.io/3857");
            table.newLinkRow("TrackingData", "https://www.microsoft.com/en-us/download/details.aspx?id=52367");
            table.newLinkRow("", "https://www.datarepository.movebank.org/discover");
            table.newLinkRow("", "https://sumo.dlr.de/docs/Data/Scenarios/TAPASCologne.html");
            table.newLinkRow("", "https://blog.csdn.net/souvenir001/article/details/52180335");
            table.newLinkRow("", "https://www.cnblogs.com/genghenggao/p/9625511.html");
            table.newLinkRow("TianDiTuAPI", "http://lbs.tianditu.gov.cn/api/js4.0/guide.html");
            table.newLinkRow("TianDiTuKey", "https://console.tianditu.gov.cn/api/key");
            table.newLinkRow("GaoDeAPI", "https://lbs.amap.com/api/javascript-api/summary");
            table.newLinkRow("GaoDeKey", "https://console.amap.com/dev/index");
            File htmFile = HtmlWriteTools.writeHtml(table.html());
            openLink(htmFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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

    protected String snapName(boolean withFrame) {
        String name = titleLabel.getText();
        if (name.isBlank()) {
            name = (Languages.message("Locations") + "_" + DateTools.datetimeToString(new Date()));
        }
        if (withFrame) {
            name += (!frameLabel.getText().isBlank() ? "_" + frameLabel.getText() : "");
        }
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
        String filename = snapName(true) + ".png";
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
                        String format = FileNameTools.getFileSuffix(file);
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

    public void snapHtml() {
        final String htmlTitle = snapName(true);
        File htmlFile = chooseSaveFile(UserConfig.getPath(baseName + "TargetPath"),
                htmlTitle, FileFilters.HtmlExtensionFilter);
        if (htmlFile == null) {
            return;
        }
        recordFileWritten(htmlFile, VisitHistory.FileType.Html);

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
                        String subPath = FileNameTools.getFilePrefix(htmlFile.getName());
                        String path = htmlFile.getParent() + "/" + subPath;
                        (new File(path)).mkdirs();

                        StringBuilder s = new StringBuilder();
                        s.append("<h1  class=\"center\">").append(htmlTitle).append("</h1>\n");
                        s.append("<hr>\n");

                        if (task == null || isCancelled()) {
                            return false;
                        }

                        s.append(writePointsTable());
                        if (task == null || isCancelled()) {
                            return false;
                        }

                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(mapSnap, null);
                        ImageFileWriters.writeImageFile(bufferedImage, "jpg", path + File.separator + "map.jpg");
                        String imageName = subPath + "/map.jpg";
                        s.append("<div align=\"center\"><img src=\"").append(imageName).append("\"  style=\"max-width:95%;\"></div>\n");
                        s.append("<hr>\n");
                        if (task == null || isCancelled()) {
                            return false;
                        }

                        String html = HtmlWriteTools.html(htmlTitle, HtmlStyles.styleValue("Default"), s.toString());
                        TextFileTools.writeFile(htmlFile, html, Charset.forName("utf-8"));

                        if (task == null || isCancelled()) {
                            return false;
                        }
                        return htmlFile.exists();
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.error(e.toString());
                        return false;
                    }

                }

                @Override
                protected void whenSucceeded() {
                    browseURI(htmlFile.toURI());
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
