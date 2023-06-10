package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import mara.mybox.data.MapPoint;
import mara.mybox.db.data.BaseData;
import mara.mybox.db.data.BaseDataAdaptor;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeTools;
import static mara.mybox.db.data.GeographyCodeTools.validCoordinate;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class GeographyCodeMapController extends ControlMap {

    protected TableGeographyCode geoTable;
    protected BaseDataManageController dataController;
    protected List<GeographyCode> geographyCodes;
    protected int chartMaxData;

    @FXML
    protected TextField chartMaxInput;
    @FXML
    protected Label frameLabel;

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

            initMap();

            chartMaxData = UserConfig.getInt(baseName + "ChartMaxData", 500);
            if (chartMaxData <= 0) {
                chartMaxData = 500;
            }
            if (chartMaxInput != null) {
                chartMaxInput.setText(chartMaxData + "");
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void drawPoints() {
        if (webEngine == null || !mapLoaded
                || geographyCodes == null || geographyCodes.isEmpty()) {
            return;
        }
        mapPoints = null;
        webEngine.executeScript("clearMap();");
        frameLabel.setText("");
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            List<MapPoint> points;

            @Override
            protected boolean handle() {
                points = new ArrayList<>();
                GeographyCode tcode;
                String image = mapOptions.image();
                int textSize = mapOptions.textSize();
                int markSize = mapOptions.markSize();
                Color textColor = mapOptions.textColor();
                boolean isBold = mapOptions.isBold();
                int index = 0;
                for (GeographyCode code : geographyCodes) {
                    if (!validCoordinate(code)) {
                        continue;
                    }
                    if (mapOptions.isGaoDeMap()) {
                        tcode = GeographyCodeTools.toGCJ02(code);
                    } else {
                        tcode = GeographyCodeTools.toCGCS2000(code, false);
                    }
                    MapPoint mapPoint = new MapPoint(tcode);
                    mapPoint.setLabel(code.getName())
                            .setInfo(BaseDataAdaptor.displayData(geoTable, code, null, true))
                            .setMarkSize(markSize)
                            .setMarkerImage(image)
                            .setTextSize(textSize)
                            .setTextColor(textColor)
                            .setIsBold(isBold);
                    points.add(mapPoint);
                    if (chartMaxData > 0 && ++index >= chartMaxData) {
                        break;
                    }
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                initPoints(mapPoints);
                drawPoints(points);
            }
        };
        start(task, "Loading map data");
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
        return BaseDataAdaptor.htmlDataList(geoTable, list, displayNames());
    }

    protected void drawGeographyCodes(List<GeographyCode> codes, String title) {
        mapTitle = title == null ? "" : StringTools.replaceLineBreak(title);
        titleLabel.setText(mapTitle);
        frameLabel.setText("");
        geographyCodes = codes;
        drawPoints();
    }

    @FXML
    @Override
    public void clearAction() {
        if (mapLoaded) {
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

    @FXML
    public void goMaxAction() {
        if (chartMaxInput != null) {
            boolean ok;
            String s = chartMaxInput.getText();
            if (s == null || s.isBlank()) {
                chartMaxData = -1;
                ok = true;
            } else {
                try {
                    int v = Integer.parseInt(s);
                    if (v > 0) {
                        chartMaxData = v;

                        ok = true;
                    } else {
                        ok = false;
                    }
                } catch (Exception ex) {
                    ok = false;
                }
            }
            if (ok) {
                UserConfig.setInt(baseName + "ChartMaxData", chartMaxData);
                chartMaxInput.setStyle(null);
            } else {
                chartMaxInput.setStyle(UserConfig.badStyle());
                popError(message("Invalid") + ": " + message("Maximum"));
                return;
            }
        }

        drawPoints();
    }

}
