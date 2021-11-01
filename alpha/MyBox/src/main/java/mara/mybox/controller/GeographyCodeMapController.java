package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import mara.mybox.db.data.BaseData;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.table.DataFactory;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class GeographyCodeMapController extends BaseMapController {

    protected TableGeographyCode geoTable;
    protected BaseDataManageController dataController;
    protected List<GeographyCode> geographyCodes;

    public GeographyCodeMapController() {
        baseTitle = Languages.message("Map") + " - " + Languages.message("GeographyCode");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            geoTable = new TableGeographyCode();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            mapOptionsController.markerTextBox.getChildren().remove(mapOptionsController.locationTextPane);
            mapOptionsController.markerImagePane.getChildren().removeAll(
                    mapOptionsController.markerDataRadio, mapOptionsController.markerDatasetRadio);
            mapOptionsController.dataColorRadio.setVisible(false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initMap(BaseDataManageController dataController) {
        this.dataController = dataController;
        super.initMap(dataController);
    }

    @Override
    public void drawPoints() {
        try {
            if (webEngine == null
                    || geographyCodes == null || geographyCodes.isEmpty()
                    || !mapOptionsController.mapLoaded) {
                return;
            }
            webEngine.executeScript("clearMap();");
            frameLabel.setText("");
            if (geographyCodes == null || geographyCodes.isEmpty()) {
                return;
            }
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>(this) {

                    private List<GeographyCode> points;

                    @Override
                    protected boolean handle() {
                        points = new ArrayList<>();
                        if (mapOptionsController.mapName == ControlMapOptions.MapName.GaoDe) {
                            points = GeographyCodeTools.toGCJ02(geographyCodes);
                        } else if (mapOptionsController.mapName == ControlMapOptions.MapName.TianDiTu) {
                            points = GeographyCodeTools.toCGCS2000(geographyCodes, false);
                        }
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (points == null || points.isEmpty()) {
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
                                    try {
                                        if (!frameEnd || timer == null) {
                                            return;
                                        }
                                        if (points == null || points.isEmpty()) {
                                            if (timer != null) {
                                                timer.cancel();
                                                timer = null;
                                            }
                                            return;
                                        }
                                        frameEnd = false;
                                        GeographyCode geographyCode = points.get(index);
                                        drawPoint(geographyCode.getLongitude(), geographyCode.getLatitude(),
                                                markerLabel(geographyCode), markerImage(),
                                                DataFactory.displayData(geoTable, geographyCode, null, true),
                                                textColor());
                                        if (!centered) {
                                            webEngine.executeScript("setCenter(" + geographyCode.getLongitude()
                                                    + ", " + geographyCode.getLatitude() + ");");
                                            centered = true;
                                        }
                                        index++;
                                        frameLabel.setText(Languages.message("DataNumber") + ":" + index);
                                        if (index >= points.size()) {
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
                                    } catch (Exception e) {
//                                        MyBoxLog.debug(e.toString());
                                    }
                                });
                            }
                        }, 0, 1); // Interface may be blocked if put all points in map altogether.

                    }
                };
                start(task, "Loading map data");
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected List<String> displayNames() {
        return Arrays.asList("level", "coordinate_system", "longitude", "latitude",
                "chinese_name", "english_name", "alias1", "code1", "area", "population");
    }

    @Override
    protected String writePointsTable() {
        if (geographyCodes == null || geographyCodes.isEmpty()) {
            return "";
        }
        List<BaseData> list = new ArrayList<>();
        for (GeographyCode geographyCode : geographyCodes) {
            if (task == null || task.isCancelled()) {
                return "";
            }
            list.add(geographyCode);
        }
        return DataFactory.htmlDataList(geoTable, list, displayNames());
    }

    public String markerLabel(GeographyCode geographyCode) {
        String label = "";
        if (mapOptionsController.markerLabelCheck.isSelected()) {
            label += geographyCode.getName();
        }
        if (mapOptionsController.markerAddressCheck.isSelected()) {
            String name = geographyCode.getFullName();
            if (name != null && !name.isBlank()) {
                if (!mapOptionsController.markerLabelCheck.isSelected()
                        || !geographyCode.getName().equals(geographyCode.getFullName())) {
                    if (!label.isBlank()) {
                        label += "</BR>";
                    }
                    label += geographyCode.getFullName();
                }
            }
        }
        if (mapOptionsController.markerCoordinateCheck.isSelected()
                && GeographyCodeTools.validCoordinate(geographyCode)) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            label += geographyCode.getLongitude() + "," + geographyCode.getLatitude();
            if (geographyCode.getAltitude() != AppValues.InvalidDouble) {
                label += "," + geographyCode.getAltitude();
            }
        }
        return label;
    }

    protected void drawGeographyCodes(List<GeographyCode> codes, String title) {
        this.title = title;
        if (this.title == null) {
            this.title = "";
        } else {
            this.title = this.title.replaceAll("\n", " ");
        }
        titleLabel.setText(this.title);
        frameLabel.setText("");
        geographyCodes = codes;
        drawPoints();
    }

    @Override
    protected void snapAllMenu() {

    }

    @FXML
    @Override
    public void clearAction() {
        if (mapOptionsController.mapLoaded) {
            webEngine.executeScript("clearMap();");
        }
        geographyCodes = null;
        titleLabel.setText("");
        frameLabel.setText("");
    }

    @Override
    public void reloadData() {
        if (dataController != null) {
            dataController.reloadChart();
        }
    }

}
