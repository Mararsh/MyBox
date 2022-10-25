package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import mara.mybox.data.MapPoint;
import mara.mybox.db.data.BaseData;
import mara.mybox.db.data.BaseDataAdaptor;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeTools;
import static mara.mybox.db.data.GeographyCodeTools.validCoordinate;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class GeographyCodeMapController extends BaseMapFramesController {

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

    public void initMap(BaseDataManageController dataController) {
        this.dataController = dataController;
        super.checkFirstRun(dataController);
    }

    @Override
    public void setDataMax() {
        if (!mapLoaded || isSettingValues) {
            return;
        }
        dataController.reloadChart();
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
        task = new SingletonTask<Void>(this) {

            List<MapPoint> points;

            @Override
            protected boolean handle() {
                points = new ArrayList<>();
                GeographyCode tcode;
                int max = mapOptions.getDataMax();
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
                    if (++index >= max) {
                        break;
                    }
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
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
        mapTitle = title == null ? "" : title.replaceAll("\n", " ");
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

}
