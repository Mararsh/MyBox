package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.StringTable;
import mara.mybox.data.tools.GeographyCodeTools;
import mara.mybox.db.TableGeographyCode;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class GeographyCodeMapController extends MapBaseController {

    protected List<GeographyCode> geographyCodes;

    public GeographyCodeMapController() {
        baseTitle = message("Map") + " - " + message("GeographyCode");
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
            logger.error(e.toString());
        }
    }

    public void initMap(BaseController dataController) {
        parentController = dataController;
        initSplitPanes();
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
                                                markerLabel(geographyCode), markerImage(), geographyCode.info("</BR>"),
                                                textColor());
                                        if (!centered) {
                                            webEngine.executeScript("setCenter(" + geographyCode.getLongitude() + ", " + geographyCode.getLatitude() + ");");
                                            centered = true;
                                        }
                                        index++;
                                        frameLabel.setText(message("DataNumber") + ":" + index);
                                        if (index >= points.size()) {
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
                                    } catch (Exception e) {
//                                        logger.debug(e.toString());
                                    }
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
        names.addAll(new TableGeographyCode().importAllFields());
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
            if (geographyCode.getAltitude() != CommonValues.InvalidDouble) {
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
        if (parentController != null && parentController instanceof DataAnalysisController) {
            ((DataAnalysisController) parentController).reloadChart();
        }
    }

}
