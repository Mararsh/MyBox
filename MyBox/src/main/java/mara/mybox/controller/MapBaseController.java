package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import mara.mybox.data.StringTable;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.LocationTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.VisitHistoryTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-7-27
 * @License Apache License Version 2.0
 */
public class MapBaseController extends BaseController {

    protected String title;
    protected boolean frameCompleted;
    protected WebEngine webEngine;
    protected int interval, frameIndex;

    @FXML
    protected WebView mapView;
    @FXML
    protected MapOptionsController mapOptionsController;
    @FXML
    protected Label titleLabel, frameLabel;
    @FXML
    protected ComboBox<String> intervalSelector, frameSelector;
    @FXML
    protected HBox playBox;
    @FXML
    protected Button pauseButton;
    @FXML
    protected CheckBox loopCheck;

    public MapBaseController() {
        baseTitle = AppVariables.message("Map");

        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Html);
        targetExtensionFilter = CommonFxValues.HtmlExtensionFilter;
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

            interval = 200;
            if (intervalSelector != null) {
                intervalSelector.getItems().addAll(Arrays.asList(
                        "200", "500", "1000", "50", "100", "300", "800", "1500", "2000", "3000", "5000", "10000"
                ));
                intervalSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            try {
                                int v = Integer.valueOf(intervalSelector.getValue());
                                if (v > 0) {
                                    interval = v;
                                    AppVariables.setUserConfigValue(baseName + "Interval", interval + "");
                                    FxmlControl.setEditorNormal(intervalSelector);
                                    if (isSettingValues) {
                                        return;
                                    }
                                    drawFrames();
                                } else {
                                    FxmlControl.setEditorBadStyle(intervalSelector);
                                }
                            } catch (Exception e) {
                                logger.error(e.toString());
                            }
                        });
                isSettingValues = true;
                intervalSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "Interval", "200"));
                isSettingValues = false;
            }

            if (loopCheck != null) {
                loopCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                            AppVariables.setUserConfigValue(baseName + "Loop", loopCheck.isSelected());
                        });
                loopCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Loop", true));
            }

            if (mapOptionsController != null) {
                mapOptionsController.initOptions(this);
            }

        } catch (Exception e) {
            logger.error(e.toString());
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
//            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0");

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
//                            logger.debug(e.toString());
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
            logger.error(e.toString());
        }

    }

    public void mapEvents(String data) {
        try {
//            logger.debug(data);
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
            logger.debug(e.toString());
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
                    ? "null" : "'" + StringTools.replaceAll(pImage, "\\", "/") + "'";
            String pColor = textColor == null ? "null" : "'" + FxmlColor.color2rgb(textColor) + "'";
            webEngine.executeScript("addMarker("
                    + longitude + "," + latitude
                    + ", " + pLabel + ", " + pInfo + ", " + pImage
                    + ", " + mapOptionsController.markerSize
                    + ", " + mapOptionsController.textSize
                    + ", " + pColor + ", " + mapOptionsController.boldCheck.isSelected() + ");");
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected String jsString(String string) {
        return string == null ? "null"
                : "'" + string.replaceAll("'", CommonValues.MyBoxSeparator).replaceAll("\n", "</BR>") + "'";
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
        String path = "/" + ControlStyle.getIconPath();
        return FxmlControl.getInternalFile(path + "iconCircle.png", "map",
                AppVariables.ControlColor.name() + "Circle.png").getAbsolutePath();
    }

    public String pointImage() {
        String path = "/" + ControlStyle.getIconPath();
        return FxmlControl.getInternalFile(path + "iconLocation.png", "map",
                AppVariables.ControlColor.name() + "Point.png").getAbsolutePath();
    }

    public Color textColor() {
        if (mapOptionsController.setColorRadio.isSelected()) {
            return (Color) (mapOptionsController.colorSetController.rect.getFill());
        }
        return Color.BLACK;
    }

    @FXML
    public void htmlAction() {
        htmlAction(this.title, mapView);
    }

    public void htmlAction(String title, Node snapNode) {
        final String htmlTitle;
        if (title == null || title.isBlank()) {
            htmlTitle = (message("Locations") + "_" + DateTools.datetimeToString(new Date()));
        } else {
            htmlTitle = title;
        }
        File htmlFile = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                htmlTitle, CommonFxValues.HtmlExtensionFilter, true);
        if (htmlFile == null) {
            return;
        }
        recordFileWritten(htmlFile);

        double scale = dpi / Screen.getPrimary().getDpi();
        scale = scale > 1 ? scale : 1;
        SnapshotParameters snapPara = new SnapshotParameters();
        snapPara.setFill(Color.TRANSPARENT);
        snapPara.setTransform(Transform.scale(scale, scale));

        Bounds bounds = snapNode.getLayoutBounds();
        int imageWidth = (int) Math.round(bounds.getWidth() * scale);
        int imageHeight = (int) Math.round(bounds.getHeight() * scale);
        WritableImage snapshot = new WritableImage(imageWidth, imageHeight);
        final Image mapSnap = snapNode.snapshot(snapPara, snapshot);

        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        String subPath = FileTools.getFilePrefix(htmlFile.getName());
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

                        String html = HtmlTools.html(htmlTitle, s.toString());
                        FileTools.writeFile(htmlFile, html);

                        if (task == null || isCancelled()) {
                            return false;
                        }
                        return htmlFile.exists();
                    } catch (Exception e) {
                        error = e.toString();
                        logger.error(e.toString());
                        return false;
                    }

                }

                @Override
                protected void whenSucceeded() {
                    browseURI(htmlFile.toURI());
                }

            };
            if (parentController != null) {
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

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
            ControlStyle.setIcon(pauseButton, ControlStyle.getIcon("iconPlay.png"));
            FxmlControl.setTooltip(pauseButton, new Tooltip(message("Continue")));
            previousButton.setDisable(false);
            nextButton.setDisable(false);
            pauseButton.setUserData("paused");
        } else {
            ControlStyle.setIcon(pauseButton, ControlStyle.getIcon("iconPause.png"));
            FxmlControl.setTooltip(pauseButton, new Tooltip(message("Pause")));
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
            StringTable table = new StringTable(null, message("AboutCoordinateSystem"));
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
            File htmFile = HtmlTools.writeHtml(table.html());
            FxmlStage.browseURI(getMyStage(), htmFile.toURI());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public boolean leavingScene() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (webEngine != null) {
                webEngine.getLoadWorker().cancel();
                webEngine = null;
            }
            mapOptionsController.leavingScene();

        } catch (Exception e) {
        }
        return super.leavingScene();

    }

}
