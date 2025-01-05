package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.GeographyCode.CoordinateSystem;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.GeographyCodeTools;
import static mara.mybox.tools.GeographyCodeTools.coordinateSystemByName;
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
    protected List<GeographyCode> dataPoints;
    protected int frameid, lastFrameid;

    @FXML
    protected TabPane chartTabPane;
    @FXML
    protected Tab chartTab, dataTab;
    @FXML
    protected ComboBox<String> labelSelector, longitudeSelector, latitudeSelector, sizeSelector;
    @FXML
    protected FlowPane csPane;
    @FXML
    protected BaseMapController mapController;
    @FXML
    protected CheckBox accumulateCheck, centerCheck, linkCheck;
    @FXML
    protected ControlData2DView valuesController;

    public Data2DLocationDistributionController() {
        baseTitle = message("LocationDistribution");
    }

    @Override
    public void initOptions() {
        try {
            super.initOptions();

            mapController.initMap();

            csGroup = new ToggleGroup();
            for (CoordinateSystem item : CoordinateSystem.values()) {
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
    public void sourceChanged() {
        try {
            super.sourceChanged();
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
                startButton.setDisable(true);
                popError(message("NoCoordinateInData"));
                return;
            }
            startButton.setDisable(false);
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

            String dname = data2D.getName();

            File file = null;
            if (dname != null) {
                dname = dname.replaceAll("\"", "");
                if (Languages.matchIgnoreCase("ChineseHistoricalCapitals", dname)) {
                    file = mapController.chineseHistoricalCapitalsImage();
                } else if (Languages.matchIgnoreCase("AutumnMovementPatternsOfEuropeanGadwalls", dname)) {
                    file = mapController.europeanGadwallsImage();
                } else if (Languages.matchIgnoreCase("SpermWhalesGulfOfMexico", dname)) {
                    file = mapController.spermWhalesImage();
                }
            }
            if (file != null) {
                mapController.setMarkerImageFile(file);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkParameters() {
        if (isSettingValues) {
            return true;
        }
        if (!mapController.mapLoaded) {
            popError(message("MapNotReady"));
            return false;
        }
        boolean ok = super.checkParameters();
        noticeLabel.setVisible(isAllPages());
        return ok;
    }

    @Override
    public boolean checkOptions() {
        try {
            if (!super.checkOptions()) {
                return false;
            }
            dataColsIndices = new ArrayList<>();
            labelCol = labelSelector.getValue();
            int col = data2D.colOrder(labelCol);
            if (col < 0) {
                popError(message("SelectToHandle") + ": " + message("Label"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            dataColsIndices.add(col);
            MyBoxLog.console(labelCol);

            longCol = longitudeSelector.getValue();
            col = data2D.colOrder(longCol);
            if (col < 0) {
                popError(message("SelectToHandle") + ": " + message("Longitude"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            if (!dataColsIndices.contains(col)) {
                dataColsIndices.add(col);
            }

            laCol = latitudeSelector.getValue();
            col = data2D.colOrder(laCol);
            if (col < 0) {
                popError(message("SelectToHandle") + ": " + message("Latitude"));
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
        taskSuccessed = false;
        task = new FxSingletonTask<Void>(this) {

            private DataFileCSV csvData;

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(this);
                    csvData = sortedFile(data2D.getName(), dataColsIndices, false);
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
                    taskSuccessed = initPoints(csvData);
                    return taskSuccessed;
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
                if (taskSuccessed) {
                    drawCodes();
                    valuesController.loadDef(csvData);
                    rightPane.setDisable(false);
                } else {
                    closeTask(ok);
                }
            }

        };
        start(task, false);
    }

    protected boolean initPoints(DataFileCSV csvData) {
        try {
            if (csvData == null || outputData == null) {
                return false;
            }
            dataPoints = new ArrayList<>();
            CoordinateSystem cs = coordinateSystemByName(
                    ((RadioButton) csGroup.getSelectedToggle()).getText());
            GeographyCode code;
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
                    code = new GeographyCode();
                    code.setCoordinateSystem(cs).setLongitude(lo).setLatitude(la);
                    if (mapController.isGaoDeMap()) {
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
                if (sizeCol != null) {
                    double v;
                    try {
                        v = Double.parseDouble(row.get(sizeIndex));
                    } catch (Exception e) {
                        v = -1;
                    }
                    code.setMarkSize(markSize(v));
                }
                code.setLabel(row.get(labelIndex));
                dataPoints.add(code);
            }
            outputData = null;
            return true;
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    protected void drawCodes() {
        if (task != null) {
            task.cancel();
        }
        playController.clear();
        mapController.clearAction();
        taskSuccessed = false;
        if (dataPoints == null || dataPoints.isEmpty()) {
            closeTask(false);
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            private List<GeographyCode> codes;
            private int size;

            @Override
            protected boolean handle() {
                try {
                    codes = new ArrayList<>();
                    size = 0;
                    for (GeographyCode code : dataPoints) {
                        codes.add(code);
                        size++;
                        if (chartMaxData > 0 && size >= chartMaxData) {
                            break;
                        }
                    }
                    taskSuccessed = true;
                    return taskSuccessed;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                framesNumber = dataPoints.size();
                lastFrameid = -1;
                mapController.setCodes(codes);
                playController.play(size);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                closeTask(ok);
            }

        };
        start(task, false);
    }

    // maximum marker size of GaoDe Map is 64
    protected int markSize(double value) {
        if (maxValue == minValue) {
            return mapController.markSize();
        }
        double size = 60d * (value - minValue) / (maxValue - minValue);
        return Math.min(60, Math.max(10, (int) size));
    }

    @Override
    public void loadFrame(int index) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (mapController.geoCodes == null
                        || framesNumber <= 0 || index < 0 || index > framesNumber) {
                    playController.clear();
                    return;
                }
                frameid = index;
                if (mapController.webEngine == null) {
                    return;
                }
                if (!accumulateCheck.isSelected() || frameid == 1) {
                    mapController.clearMap();
                }
                GeographyCode code = mapController.geoCodes.get(index);
                mapController.showCode(code);
                if (linkCheck.isVisible() && linkCheck.isSelected() && lastFrameid >= 1) {
                    GeographyCode lastCode = mapController.geoCodes.get(lastFrameid);
                    mapController.drawLine(lastCode, code);
                }
                if (centerCheck.isSelected() || frameid == 1) {
                    mapController.moveCenter(code);
                }
                lastFrameid = frameid;

                if (!playController.selectCurrentFrame()) {
                    IndexRange range = playController.currentRange();
                    if (range != null) {
                        List<String> labels = new ArrayList<>();
                        for (int i = range.getStart(); i < range.getEnd(); i++) {
                            labels.add((i + 1) + "  " + mapController.geoCodes.get(i).getLabel());
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
        drawCodes();
    }

    @FXML
    @Override
    public boolean menuAction() {
        Tab tab = chartTabPane.getSelectionModel().getSelectedItem();
        if (tab == chartTab) {
            return mapController.menuAction();

        } else if (tab == dataTab) {
            return valuesController.menuAction();

        }
        return false;
    }

    @FXML
    @Override
    public boolean popAction() {
        Tab tab = chartTabPane.getSelectionModel().getSelectedItem();
        if (tab == chartTab) {
            return mapController.popAction();

        } else if (tab == dataTab) {
            return valuesController.popAction();

        }
        return false;
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
