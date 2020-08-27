package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.StringTable;
import mara.mybox.tools.GeographyCodeTools;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class GeographyCodeMapController extends MapBaseController {

    protected DataAnalysisController dataController;
    protected List<GeographyCode> geographyCodes;

    public GeographyCodeMapController() {
        baseTitle = message("Map") + " - " + message("GeographyCode");
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            mapOptionsController.markerTextBox.getChildren().remove(mapOptionsController.locationTextPane);
            mapOptionsController.markerImagePane.getChildren().removeAll(
                    mapOptionsController.markerDataRadio, mapOptionsController.markerDatasetRadio);
            mapOptionsController.dataColorRadio.setVisible(false);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initMap(DataAnalysisController dataController) {
        this.dataController = dataController;
        parentController = dataController;

        initSplitPanes();
    }

    @Override
    public void drawPoints() {
        try {
            if (geographyCodes == null || geographyCodes.isEmpty()
                    || !mapOptionsController.mapLoaded) {
                return;
            }
            webEngine.executeScript("clearMap();");
            if (geographyCodes == null || geographyCodes.isEmpty()) {
                return;
            }
            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private List<GeographyCode> points;

                    @Override
                    protected boolean handle() {
                        points = new ArrayList<>();
                        if (mapOptionsController.mapName == MapOptionsController.MapName.GaoDe) {
                            points = GeographyCodeTools.toGCJ02(geographyCodes);
                        } else if (mapOptionsController.mapName == MapOptionsController.MapName.TianDiTu) {
                            points = GeographyCodeTools.toCGCS2000(geographyCodes);
                        }
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        boolean center = false;
                        for (GeographyCode geographyCode : points) {
                            if (!geographyCode.validCoordinate()) {
                                continue;
                            }
                            drawPoint(geographyCode.getLongitude(), geographyCode.getLatitude(),
                                    markerLabel(geographyCode), markerImage(), geographyCode.info("</BR>"),
                                    textColor());
                            if (!center) {
                                webEngine.executeScript("setCenter(" + geographyCode.getLongitude() + ", " + geographyCode.getLatitude() + ");");
                                center = true;
                            }
                        }
                        if (mapOptionsController.mapName == MapOptionsController.MapName.GaoDe
                                && mapOptionsController.fitViewCheck.isSelected()) {
                            webEngine.executeScript("map.setFitView();");
                        }
                    }
                };
                if (parentController != null) {
                    parentController.openHandlingStage(task, Modality.WINDOW_MODAL, "Loading map data");
                } else {
                    openHandlingStage(task, Modality.WINDOW_MODAL, "Loading map data");
                }
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    protected String writePointsTable() {
        if (geographyCodes == null || geographyCodes.isEmpty()) {
            return "";
        }
        List<String> names = new ArrayList<>();
        names.addAll(GeographyCodeTools.externalNames());
        StringTable table = new StringTable(names);
        for (GeographyCode geographyCode : geographyCodes) {
            if (task == null || task.isCancelled()) {
                return "";
            }
            List<String> row = new ArrayList<>();
            row.addAll(GeographyCodeTools.externalValues(geographyCode));
            table.add(row);
        }
        return StringTable.tableDiv(table);
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
                && geographyCode.validCoordinate()) {
            if (!label.isBlank()) {
                label += "</BR>";
            }
            label += geographyCode.getLongitude() + "," + geographyCode.getLatitude();
            if (geographyCode.getAltitude() != Double.MAX_VALUE) {
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
        geographyCodes = codes;
        drawPoints();
    }

    @FXML
    @Override
    public void clearAction() {
        if (mapOptionsController.mapLoaded) {
            webEngine.executeScript("clearMap();");
        }
        geographyCodes = null;
    }

    @Override
    public void reloadData() {
        dataController.reloadChart();
    }

}
