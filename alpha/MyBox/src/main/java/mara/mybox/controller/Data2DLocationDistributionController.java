package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.data.GeoCoordinateSystem;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-10-11
 * @License Apache License Version 2.0
 */
public class Data2DLocationDistributionController extends BaseData2DHandleController {

    protected Data2DColumn labelColumn, longColumn, laColumn, sizeColumn;
    protected List<Integer> dataColsIndices;
    protected ToggleGroup csGroup;
    protected double maxValue, minValue;

    @FXML
    protected ComboBox<String> labelSelector, longitudeSelector, latitudeSelector, sizeSelector;
    @FXML
    protected Label noticeLabel;
    @FXML
    protected FlowPane csPane;
    @FXML
    protected ControlMapOptions mapOptionsController;
    @FXML
    protected ControlMap mapController;

    public Data2DLocationDistributionController() {
        baseTitle = message("LocationDistribution");
        TipsLabelKey = "";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            mapController.mapOptionsController = mapOptionsController;
            mapOptionsController.setParameters(mapController);

            mapController.drawNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    drawPoints();
                }
            });

            mapController.dataNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    startOperation();
                }
            });

            csGroup = new ToggleGroup();
            for (GeoCoordinateSystem.Value item : GeoCoordinateSystem.Value.values()) {
                RadioButton rb = new RadioButton(message(item.name()));
                csPane.getChildren().add(rb);
                csGroup.getToggles().add(rb);
            }
            ((RadioButton) csPane.getChildren().get(0)).setSelected(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
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
            if (message("zh", "ChineseHistoricalCapitals").equals(dname)
                    || message("en", "ChineseHistoricalCapitals").equals(dname)) {
                file = mapController.mapOptions.chineseHistoricalCapitalsImage();

            } else if (message("zh", "AutumnMovementPatternsOfEuropeanGadwalls").equals(dname)
                    || message("en", "AutumnMovementPatternsOfEuropeanGadwalls").equals(dname)) {
                file = mapController.mapOptions.europeanGadwallsImage();

            } else if (message("zh", "SpermWhalesGulfOfMexico").equals(dname)
                    || message("en", "SpermWhalesGulfOfMexico").equals(dname)) {
                file = mapController.mapOptions.spermWhalesImage();

            }
            if (file != null) {
                mapOptionsController.initImageFile(file);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        if (isSettingValues) {
            return true;
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
            outputColumns = new ArrayList<>();
            int col = data2D.colOrder(labelSelector.getValue());
            if (col < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("Label"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            labelColumn = data2D.column(col);
            dataColsIndices.add(col);
            outputColumns.add(labelColumn);

            col = data2D.colOrder(longitudeSelector.getValue());
            if (col < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("Longitude"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            longColumn = data2D.column(col);
            dataColsIndices.add(col);
            outputColumns.add(longColumn);

            col = data2D.colOrder(latitudeSelector.getValue());
            if (col < 0) {
                outOptionsError(message("SelectToHandle") + ": " + message("Latitude"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            laColumn = data2D.column(col);
            dataColsIndices.add(col);
            outputColumns.add(laColumn);

            String sizeName = sizeSelector.getValue();
            if (sizeName == null || message("NotSet").equals(sizeName)) {
                col = -1;
            } else {
                col = data2D.colOrder(sizeName);
            }
            if (col >= 0) {
                sizeColumn = data2D.column(col);
                dataColsIndices.add(col);
                outputColumns.add(sizeColumn);
            } else {
                sizeColumn = null;
            }

            if (otherColsIndices != null) {
                for (int c : otherColsIndices) {
                    if (!dataColsIndices.contains(c)) {
                        dataColsIndices.add(c);
                        outputColumns.add(data2D.column(c));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    long maxDataNumber = filterController.maxFilteredNumber;
                    maxDataNumber = Math.min(maxDataNumber > 0 ? maxDataNumber : Integer.MAX_VALUE,
                            mapController.mapOptions.getDataMax());
                    filterController.filter.setMaxPassed(maxDataNumber);
                    data2D.startTask(task, filterController.filter);
                    if (isAllPages()) {
                        outputData = data2D.allRows(dataColsIndices, false);
                    } else {
                        outputData = filtered(dataColsIndices, false);
                    }
                    data2D.stopFilter();
                    if (outputData == null || outputData.isEmpty()) {
                        error = message("NoData");
                        return false;
                    }
                    if (sizeColumn != null) {
                        maxValue = -Double.MAX_VALUE;
                        minValue = Double.MAX_VALUE;
                        double size;
                        for (List<String> row : outputData) {
                            try {
                                size = Double.valueOf(row.get(3));
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
                task = null;
                if (ok) {
                    drawPoints();
                }
            }

        };
        start(task);
    }

    public void drawPoints() {
        if (!mapController.mapLoaded || outputData == null || outputData.isEmpty()) {
            return;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        mapController.clearAction();
        GeoCoordinateSystem cs
                = new GeoCoordinateSystem(((RadioButton) csGroup.getSelectedToggle()).getText());
        int max = mapController.mapOptions.getDataMax();
        mapController.mapOptions.setMapSize(3);
        timer = new Timer();
        timer.schedule(new TimerTask() {

            private boolean frameEnd = true, centered = false;
            private int index = 0;

            @Override
            public void run() {
                Platform.runLater(() -> {
                    try {
                        if (!frameEnd || timer == null) {
                            return;
                        }
                        if (checkEnd()) {
                            return;
                        }
                        frameEnd = false;
                        List<String> row = outputData.get(index++);
                        GeographyCode code = GeographyCode.create().setCoordinateSystem(cs);
                        try {
                            double lo = Double.valueOf(row.get(1));
                            double la = Double.valueOf(row.get(2));
                            if (!GeographyCodeTools.validCoordinate(lo, la)) {
                                return;
                            }
                            code.setLongitude(lo).setLatitude(la);
                            if (mapController.mapOptions.isGaoDeMap()) {
                                code = GeographyCodeTools.toGCJ02(code);
                            } else {
                                code = GeographyCodeTools.toCGCS2000(code, false);
                            }
                        } catch (Exception e) {
//                            MyBoxLog.error(e.toString());
//                            MyBoxLog.console(row);
                            return;
                        }
                        if (code == null) {
                            return;
                        }
                        String info = "";
                        for (int c = 0; c < row.size(); c++) {
                            String v = row.get(c);
                            info += outputColumns.get(c).getColumnName() + ": "
                                    + (v == null ? "" : v) + "\n";
                        }
                        code.setLabel(row.get(0)).setInfo(info);
                        int markSize;
                        if (sizeColumn != null) {
                            double v;
                            try {
                                v = Double.valueOf(row.get(3));
                            } catch (Exception e) {
                                v = 0;
                            }
                            markSize = markSize(v);
                        } else {
                            markSize = mapController.mapOptions.getMarkerSize();
                        }
                        mapController.drawPoint(code,
                                mapController.mapOptions.getMarkerImageFile().getAbsolutePath(),
                                markSize,
                                mapController.mapOptions.getTextColor());

                        if (!centered) {
                            mapController.webEngine.executeScript("setCenter(" + code.getLongitude()
                                    + ", " + code.getLatitude() + ");");
                            centered = true;
                        }
                        mapController.titleLabel.setText(message("DataNumber") + ":" + index);
                        if (checkEnd()) {
                            return;
                        }
                        frameEnd = true;
                    } catch (Exception e) {
//                                        MyBoxLog.debug(e.toString());
                    }
                });
            }

            public boolean checkEnd() {
                if (outputData == null || index >= outputData.size() || index >= max) {
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    if (mapController.mapOptions.isGaoDeMap() && mapController.mapOptions.isFitView()) {
                        mapController.webEngine.executeScript("map.setFitView();");
                    }
                    return true;
                }
                return false;
            }

        }, 0, 1); // Interface may be blocked if put all points in map altogether.

    }

    // maximum marker size of GaoDe Map is 64
    protected int markSize(double value) {
        if (maxValue == minValue) {
            return mapController.mapOptions.getMarkerSize();
        }
        double size = 60d * (value - minValue) / (maxValue - minValue);
        return Math.min(60, Math.max(10, (int) size));
    }

    /*
        static
     */
    public static Data2DLocationDistributionController open(ControlData2DLoad tableController) {
        try {
            Data2DLocationDistributionController controller = (Data2DLocationDistributionController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DLocationDistributionFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
