package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import mara.mybox.data.GeoCoordinateSystem;
import mara.mybox.data.MapPoint;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-11
 * @License Apache License Version 2.0
 */
public class Data2DLocationDistributionController extends BaseData2DChartController {

    protected String labelCol, longCol, laCol, sizeCol;
    protected ToggleGroup csGroup;
    protected double maxValue, minValue;
    protected List<MapPoint> dataPoints;
    protected int frameid, lastFrameid;

    @FXML
    protected ComboBox<String> labelSelector, longitudeSelector, latitudeSelector, sizeSelector;

    @FXML
    protected FlowPane csPane;
    @FXML
    protected ControlMapOptions mapOptionsController;
    @FXML
    protected ControlMap mapController;
    @FXML
    protected CheckBox accumulateCheck, centerCheck, linkCheck;
    @FXML
    protected ControlData2DView valuesController;

    public Data2DLocationDistributionController() {
        baseTitle = message("LocationDistribution");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            mapController.mapOptionsController = mapOptionsController;
            mapController.initMap();

            mapController.drawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    drawPoints();
                }
            });

            csGroup = new ToggleGroup();
            for (GeoCoordinateSystem.Value item : GeoCoordinateSystem.Value.values()) {
                RadioButton rb = new RadioButton(message(item.name()));
                csPane.getChildren().add(rb);
                csGroup.getToggles().add(rb);
            }
            ((RadioButton) csPane.getChildren().get(0)).setSelected(true);

            linkCheck.visibleProperty().bind(accumulateCheck.selectedProperty());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public Node snapNode() {
        return mapController.snapBox;
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();
            isSettingValues = true;
            labelSelector.getItems().clear();
            longitudeSelector.getItems().clear();
            latitudeSelector.getItems().clear();
            sizeSelector.getItems().clear();
            List<String> allNames = new ArrayList<>();
            List<String> longNames = new ArrayList<>();
            List<String> laNames = new ArrayList<>();
            for (Data2DColumn column : data2D.getColumns()) {
                String name = column.getColumnName();
                if (column.getType() == ColumnType.Longitude) {
                    longNames.add(name);
                } else if (column.getType() == ColumnType.Latitude) {
                    laNames.add(name);
                }
                allNames.add(name);
            }
            if (longNames.isEmpty() || laNames.isEmpty()) {
                okButton.setDisable(true);
                popError(message("NoCoordinateInData"));
                return;
            }
            okButton.setDisable(false);
            labelSelector.getItems().setAll(allNames);
            labelSelector.getSelectionModel().select(0);

            longitudeSelector.getItems().setAll(longNames);
            longitudeSelector.getSelectionModel().select(0);

            latitudeSelector.getItems().setAll(laNames);
            latitudeSelector.getSelectionModel().select(0);

            allNames.add(0, message("NotSet"));
            sizeSelector.getItems().setAll(allNames);
            sizeSelector.getSelectionModel().select(0);

            isSettingValues = false;

            String dname = data2D.getDataName();

            File file = null;
            if (dname != null) {
                dname = dname.replaceAll("\"", "");
                if (Languages.matchIgnoreCase("ChineseHistoricalCapitals", dname)) {
                    file = mapController.mapOptions.chineseHistoricalCapitalsImage();
                } else if (Languages.matchIgnoreCase("AutumnMovementPatternsOfEuropeanGadwalls", dname)) {
                    file = mapController.mapOptions.europeanGadwallsImage();
                } else if (Languages.matchIgnoreCase("SpermWhalesGulfOfMexico", dname)) {
                    file = mapController.mapOptions.spermWhalesImage();
                }
            }
            if (file != null) {
                mapOptionsController.initImageFile(file);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (isSettingValues) {
            return true;
        }
        if (!mapController.mapLoaded) {
            popError(message("MapNotReady"));
            return false;
        }
        boolean ok = super.checkOptions();
        noticeLabel.setVisible(isAllPages());
        return ok;
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            dataColsIndices = new ArrayList<>();
            labelCol = labelSelector.getValue();
            int col = data2D.colOrder(labelCol);
            if (col < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("Label"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            dataColsIndices.add(col);

            longCol = longitudeSelector.getValue();
            col = data2D.colOrder(longCol);
            if (col < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("Longitude"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            if (!dataColsIndices.contains(col)) {
                dataColsIndices.add(col);
            }

            laCol = latitudeSelector.getValue();
            col = data2D.colOrder(laCol);
            if (col < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("Latitude"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            if (!dataColsIndices.contains(col)) {
                dataColsIndices.add(col);
            }

            sizeCol = sizeSelector.getValue();
            if (sizeCol == null || message("NotSet").equals(sizeCol)) {
                sizeCol = null;
            } else {
                col = data2D.colOrder(sizeCol);
                if (col >= 0 && !dataColsIndices.contains(col)) {
                    dataColsIndices.add(col);
                }
            }

            if (otherColsIndices != null) {
                for (int c : otherColsIndices) {
                    if (!dataColsIndices.contains(c)) {
                        dataColsIndices.add(c);
                    }
                }
            }

            List<String> sortNames = sortNames();
            if (sortNames != null) {
                for (String name : sortNames) {
                    int c = data2D.colOrder(name);
                    if (!dataColsIndices.contains(c)) {
                        dataColsIndices.add(c);
                    }
                }
            }

            dataPoints = null;
            framesNumber = -1;
            frameid = -1;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        valuesController.loadNull();
        task = new FxSingletonTask<Void>(this) {

            private DataFileCSV csvData;

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(this);
                    csvData = sortedFile(data2D.dataName(), dataColsIndices, false);
                    if (csvData == null) {
                        return false;
                    }
                    csvData.saveAttributes();
                    outputData = csvData.allRows(false);
                    if (sizeCol != null) {
                        maxValue = -Double.MAX_VALUE;
                        minValue = Double.MAX_VALUE;
                        double size;
                        for (List<String> row : outputData) {
                            try {
                                size = Double.parseDouble(row.get(3));
                            } catch (Exception e) {
                                size = 0;
                            }
                            if (size > maxValue) {
                                maxValue = size;
                            }
                            if (size < minValue) {
                                minValue = size;
                            }
                        }
                    }
                    return initPoints(csvData);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {

            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                if (ok) {
                    drawPoints();
                    valuesController.loadDef(csvData);
                }
            }

        };
        start(task);
    }

    protected boolean initPoints(DataFileCSV csvData) {
        try {
            if (csvData == null || outputData == null) {
                return false;
            }
            dataPoints = new ArrayList<>();
            GeoCoordinateSystem cs
                    = new GeoCoordinateSystem(((RadioButton) csGroup.getSelectedToggle()).getText());
            GeographyCode code = GeographyCode.create();
            int longIndex = csvData.colOrder(longCol);
            int laIndex = csvData.colOrder(laCol);
            int labelIndex = csvData.colOrder(labelCol);
            int sizeIndex = csvData.colOrder(sizeCol);
            for (List<String> row : outputData) {
                double lo, la;
                try {
                    lo = Double.parseDouble(row.get(longIndex));
                    la = Double.parseDouble(row.get(laIndex));
                    if (!GeographyCodeTools.validCoordinate(lo, la)) {
                        continue;
                    }
                    if (code == null) {
                        code = GeographyCode.create();
                    }
                    code.setCoordinateSystem(cs).setLongitude(lo).setLatitude(la);
                    if (mapController.mapOptions.isGaoDeMap()) {
                        code = GeographyCodeTools.toGCJ02(code);
                    } else {
                        code = GeographyCodeTools.toCGCS2000(code, false);
                    }
                } catch (Exception e) {
                    MyBoxLog.console(e.toString());
                    continue;
                }
                if (code == null) {
                    continue;
                }
                MapPoint point = new MapPoint(code);
                String info = "";
                for (int i = 0; i < row.size(); i++) {
                    String v = row.get(i);
                    info += csvData.columnName(i) + ": "
                            + (v == null ? "" : v) + "\n";
                }
                int markSize;
                if (sizeCol != null) {
                    double v;
                    try {
                        v = Double.parseDouble(row.get(sizeIndex));
                    } catch (Exception e) {
                        v = 0;
                    }
                    markSize = markSize(v);
                } else {
                    markSize = mapController.mapOptions.getMarkerSize();
                }
                point.setLabel(row.get(labelIndex)).setInfo(info).setMarkSize(markSize);
                dataPoints.add(point);
            }
            outputData = null;
            return true;
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    protected void drawPoints() {
        if (task != null) {
            task.cancel();
        }
        playController.clear();
        mapController.clearAction();
        if (dataPoints == null || dataPoints.isEmpty()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            private List<MapPoint> mapPoints;
            private int size;

            @Override
            protected boolean handle() {
                try {
                    String image = mapController.mapOptions.image();
                    int textSize = mapController.mapOptions.textSize();
                    int markSize = mapController.mapOptions.markSize();
                    Color textColor = mapController.mapOptions.textColor();
                    boolean isBold = mapController.mapOptions.isBold();
                    mapPoints = new ArrayList<>();
                    size = 0;
                    for (MapPoint dataPoint : dataPoints) {
                        if (sizeCol != null) {
                            markSize = dataPoint.getMarkSize();
                        }
                        MapPoint mapPoint = new MapPoint()
                                .setLongitude(dataPoint.getLongitude())
                                .setLatitude(dataPoint.getLatitude())
                                .setLabel(dataPoint.getLabel())
                                .setInfo(dataPoint.getInfo())
                                .setMarkSize(markSize)
                                .setMarkerImage(image)
                                .setTextSize(textSize)
                                .setTextColor(textColor)
                                .setCs(dataPoint.getCs())
                                .setIsBold(isBold);
                        mapPoints.add(mapPoint);
                        size++;
                        if (chartMaxData > 0 && size >= chartMaxData) {
                            break;
                        }
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                if (ok) {
                    framesNumber = dataPoints.size();
                    lastFrameid = -1;
                    mapController.initPoints(mapPoints);
                    playController.play(size);
                }
            }

        };
        start(task);
    }

    // maximum marker size of GaoDe Map is 64
    protected int markSize(double value) {
        if (maxValue == minValue) {
            return mapController.mapOptions.getMarkerSize();
        }
        double size = 60d * (value - minValue) / (maxValue - minValue);
        return Math.min(60, Math.max(10, (int) size));
    }

    @Override
    public void loadFrame(int index) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (mapController.mapPoints == null
                        || framesNumber <= 0 || index < 0 || index > framesNumber) {
                    playController.clear();
                    return;
                }
                frameid = index;
                if (mapController.webEngine == null) {
                    return;
                }
                if (!accumulateCheck.isSelected() || frameid == 1) {
                    mapController.webEngine.executeScript("clearMap();");
                }
                MapPoint point = mapController.mapPoints.get(index);
                mapController.drawPoint(point);
                if (linkCheck.isVisible() && linkCheck.isSelected() && lastFrameid >= 1) {
                    MapPoint lastPoint = mapController.mapPoints.get(lastFrameid);
                    String pColor = "'" + FxColorTools.color2rgb(point.getTextColor()) + "'";
                    mapController.webEngine.executeScript("drawLine("
                            + lastPoint.getLongitude() + ", " + lastPoint.getLatitude() + ", "
                            + point.getLongitude() + ", " + point.getLatitude()
                            + ", " + pColor + ");");
                }
                if (centerCheck.isSelected() || frameid == 1) {
                    mapController.webEngine.executeScript("setCenter("
                            + point.getLongitude() + ", " + point.getLatitude() + ");");
                }
                lastFrameid = frameid;

                if (!playController.selectCurrentFrame()) {
                    IndexRange range = playController.currentRange();
                    if (range != null) {
                        List<String> labels = new ArrayList<>();
                        for (int i = range.getStart(); i < range.getEnd(); i++) {
                            labels.add((i + 1) + "  " + mapController.mapPoints.get(i).getLabel());
                        }
                        playController.setList(labels);
                    } else {
                        playController.setList(null);
                    }
                }

            }
        });
    }

    @Override
    public void drawChart() {
        drawPoints();
    }

    @Override
    public void cleanPane() {
        try {
            playController.clear();
            mapController.clearAction();
            valuesController.loadNull();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static Data2DLocationDistributionController open(BaseData2DLoadController tableController) {
        try {
            Data2DLocationDistributionController controller = (Data2DLocationDistributionController) WindowTools.branchStage(
                    tableController, Fxmls.Data2DLocationDistributionFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
