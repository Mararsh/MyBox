package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-4-23
 * @License Apache License Version 2.0
 */
public class EpidemicReportsColorsController extends BaseController {

    protected EpidemicReportsController reportsController;
    protected EpidemicReportsChartController chartController;

    protected String dataSelect, dataQuerySQL, dataSizeSQL, dataPageSQL,
            currentTitle, clearDataSQL;
    protected String baseWhere, dataWhere, dataOrder, dataFetch;

    @FXML
    protected TabPane tabsPane;
    @FXML
    protected Tab sourcesColorsTab, valuesColorsTab, locationsColorsTab;
    @FXML
    protected ColorSet predefinedColorSetController, inputtedColorSetController,
            filledColorSetController, statisticColorSetController,
            confirmedColorSetController, healedColorSetController, deadColorSetController,
            IncreasedConfirmedColorSetController, IncreasedHealedColorSetController, IncreasedDeadColorSetController,
            HealedConfirmedPermillageColorSetController, DeadConfirmedPermillageColorSetController,
            ConfirmedPopulationPermillageColorSetController, DeadPopulationPermillageColorSetController,
            HealedPopulationPermillageColorSetController, ConfirmedAreaPermillageColorSetController,
            HealedAreaPermillageColorSetController, DeadAreaPermillageColorSetController;
    @FXML
    protected VBox locationColorsBox;

    public EpidemicReportsColorsController() {
        baseTitle = message("EpidemicReport");
        TipsLabelKey = "EpidemicReportsChartColorComments";
    }

    public void reset() {
        initValuesColors();
        initSourcesColors();
        makeLocationsColors();
    }

    protected void initSourcesColors() {
        predefinedColorSetController.init(this, "EpidemicReport" + "PredefinedColor", Color.LINEN);
        inputtedColorSetController.init(this, "EpidemicReport" + "InputtedColor", Color.WHITE);
        filledColorSetController.init(this, "EpidemicReport" + "FilledColor", Color.THISTLE);
        statisticColorSetController.init(this, "EpidemicReport" + "StatisticColor", Color.DARKSEAGREEN);
    }

    protected void initValuesColors() {
        confirmedColorSetController.init(this, "EpidemicReportColor" + "Confirmed", Color.BLUE);
        healedColorSetController.init(this, "EpidemicReportColor" + "Healed", Color.RED);
        deadColorSetController.init(this, "EpidemicReportColor" + "Dead", Color.BLACK);
        IncreasedConfirmedColorSetController.init(this, "EpidemicReportColor" + "IncreasedConfirmed", Color.SLATEBLUE);
        IncreasedHealedColorSetController.init(this, "EpidemicReportColor" + "IncreasedHealed", Color.HOTPINK);
        IncreasedDeadColorSetController.init(this, "EpidemicReportColor" + "IncreasedDead", Color.GRAY);
        HealedConfirmedPermillageColorSetController.init(this, "EpidemicReportColor" + "HealedConfirmedPermillage", Color.PALEGREEN);
        DeadConfirmedPermillageColorSetController.init(this, "EpidemicReportColor" + "DeadConfirmedPermillage", Color.STEELBLUE);
        ConfirmedPopulationPermillageColorSetController.init(this, "EpidemicReportColor" + "ConfirmedPopulationPermillage", Color.MEDIUMPURPLE);
        DeadPopulationPermillageColorSetController.init(this, "EpidemicReportColor" + "DeadPopulationPermillage", Color.SADDLEBROWN);
        HealedPopulationPermillageColorSetController.init(this, "EpidemicReportColor" + "HealedPopulationPermillage", Color.LIGHTPINK);
        ConfirmedAreaPermillageColorSetController.init(this, "EpidemicReportColor" + "ConfirmedAreaPermillage", Color.BLUEVIOLET);
        HealedAreaPermillageColorSetController.init(this, "EpidemicReportColor" + "HealedAreaPermillage", Color.MEDIUMVIOLETRED);
        DeadAreaPermillageColorSetController.init(this, "EpidemicReportColor" + "DeadAreaPermillage", Color.SILVER);
    }

    protected void makeLocationsColors() {
        try {
            locationColorsBox.getChildren().clear();
            List<GeographyCode> locations = chartController.chartLocations;
            if (locations == null) {
                return;
            }
            Collections.sort(locations, (GeographyCode p1, GeographyCode p2)
                    -> p1.getFullName().compareTo(p2.getFullName()));
            List<String> colors = new ArrayList<>();
            for (int i = 0; i < locations.size(); i++) {
                GeographyCode location = locations.get(i);
                String locationName = location.getFullName();
                String controllerName = "EpidemicReportLocationColor" + locationName;
                Label label = new Label(locationName);
                FXMLLoader fxmlLoader = new FXMLLoader(
                        FxmlStage.class.getResource(CommonValues.ColorSetFxml), AppVariables.currentBundle);
                Pane pane = fxmlLoader.load();
                ColorSet controller = (ColorSet) fxmlLoader.getController();
                controller.init(this, controllerName, Color.web(FxmlColor.randomRGB()));

                String color = controller.rgb();
                if (colors.contains(color)) {
                    color = FxmlColor.randomRGBExcept(colors);
                    controller.setColor(Color.web(color));
                }
                colors.add(color);

                HBox line = new HBox();
                line.setAlignment(Pos.CENTER_LEFT);
                line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                line.setSpacing(5);
                VBox.setVgrow(line, Priority.ALWAYS);
                HBox.setHgrow(line, Priority.ALWAYS);
                line.getChildren().addAll(label, pane);

                locationColorsBox.getChildren().add(line);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void adjustTabs(boolean setCharts) {
        tabsPane.getTabs().clear();
        if (setCharts) {
            tabsPane.getTabs().addAll(sourcesColorsTab, valuesColorsTab, locationsColorsTab);
        } else {
            tabsPane.getTabs().add(sourcesColorsTab);
        }
        reset();
    }

    @FXML
    public void applyValuesColors() {
        if (chartController.timer != null) {
            chartController.timer.cancel();
            chartController.timer = null;
        }
        chartController.drawChart();
        popSuccessful();
    }

    @FXML
    public void applyLocationsColors() {
        if (chartController.timer != null) {
            chartController.timer.cancel();
            chartController.timer = null;
        }
        chartController.drawChart();
        popSuccessful();
    }

    @FXML
    public void defaultSourcesColors() {
        predefinedColorSetController.setColor(Color.LINEN);
        inputtedColorSetController.setColor(Color.WHITE);
        filledColorSetController.setColor(Color.THISTLE);
        statisticColorSetController.setColor(Color.DARKSEAGREEN);
    }

    @FXML
    public void randomSourcesColor() {
        List<String> colors = FxmlColor.randomRGB(4);
        predefinedColorSetController.setColor(Color.web(colors.get(0)));
        inputtedColorSetController.setColor(Color.web(colors.get(1)));
        filledColorSetController.setColor(Color.web(colors.get(2)));
        statisticColorSetController.setColor(Color.web(colors.get(3)));
    }

    @FXML
    public void applySourcesColors() {
        reportsController.tableView.refresh();
        popSuccessful();
    }

    @FXML
    public void defaultValuesColors() {
        confirmedColorSetController.setColor(Color.BLUE);
        healedColorSetController.setColor(Color.RED);
        deadColorSetController.setColor(Color.BLACK);
        IncreasedConfirmedColorSetController.setColor(Color.SLATEBLUE);
        IncreasedHealedColorSetController.setColor(Color.HOTPINK);
        IncreasedDeadColorSetController.setColor(Color.GRAY);
        HealedConfirmedPermillageColorSetController.setColor(Color.PALEGREEN);
        DeadConfirmedPermillageColorSetController.setColor(Color.STEELBLUE);
        ConfirmedPopulationPermillageColorSetController.setColor(Color.MEDIUMPURPLE);
        DeadPopulationPermillageColorSetController.setColor(Color.SADDLEBROWN);
        HealedPopulationPermillageColorSetController.setColor(Color.LIGHTPINK);
        ConfirmedAreaPermillageColorSetController.setColor(Color.BLUEVIOLET);
        HealedAreaPermillageColorSetController.setColor(Color.MEDIUMVIOLETRED);
        DeadAreaPermillageColorSetController.setColor(Color.SILVER);
    }

    @FXML
    public void randomValuesColors() {
        List<String> colors = FxmlColor.randomRGB(14);
        confirmedColorSetController.setColor(Color.web(colors.get(0)));
        healedColorSetController.setColor(Color.web(colors.get(1)));
        deadColorSetController.setColor(Color.web(colors.get(2)));
        IncreasedConfirmedColorSetController.setColor(Color.web(colors.get(3)));
        IncreasedHealedColorSetController.setColor(Color.web(colors.get(4)));
        IncreasedDeadColorSetController.setColor(Color.web(colors.get(5)));
        HealedConfirmedPermillageColorSetController.setColor(Color.web(colors.get(6)));
        DeadConfirmedPermillageColorSetController.setColor(Color.web(colors.get(7)));
        ConfirmedPopulationPermillageColorSetController.setColor(Color.web(colors.get(8)));
        DeadPopulationPermillageColorSetController.setColor(Color.web(colors.get(9)));
        HealedPopulationPermillageColorSetController.setColor(Color.web(colors.get(10)));
        ConfirmedAreaPermillageColorSetController.setColor(Color.web(colors.get(11)));
        HealedAreaPermillageColorSetController.setColor(Color.web(colors.get(12)));
        DeadAreaPermillageColorSetController.setColor(Color.web(colors.get(13)));
    }

    @FXML
    public void randomLocationsDataColors() {
        if (chartController == null || chartController.chartLocations == null) {
            return;
        }
        int size = chartController.chartLocations.size();
        List<String> colors = FxmlColor.randomRGB(size);
        for (int i = 0; i < size; i++) {
            GeographyCode location = chartController.chartLocations.get(i);
            String controllerName = "EpidemicReportLocationColor" + location.getFullName();
            AppVariables.setUserConfigValue(controllerName, colors.get(i));
        }
        makeLocationsColors();
    }

    public String rgb(String name) {
        try {
            if (name == null || name.isBlank()) {
                return null;
            }
            if (message("Confirmed").equals(name)) {
                return confirmedColorSetController.rgb();

            } else if (message("Healed").equals(name)) {
                return healedColorSetController.rgb();

            } else if (message("Dead").equals(name)) {
                return deadColorSetController.rgb();

            } else if (message("IncreasedConfirmed").equals(name)) {
                return IncreasedConfirmedColorSetController.rgb();

            } else if (message("IncreasedHealed").equals(name)) {
                return IncreasedHealedColorSetController.rgb();

            } else if (message("IncreasedDead").equals(name)) {
                return IncreasedDeadColorSetController.rgb();

            } else if (message("HealedConfirmedPermillage").equals(name)) {
                return HealedConfirmedPermillageColorSetController.rgb();

            } else if (message("DeadConfirmedPermillage").equals(name)) {
                return DeadConfirmedPermillageColorSetController.rgb();

            } else if (message("ConfirmedPopulationPermillage").equals(name)) {
                return ConfirmedPopulationPermillageColorSetController.rgb();

            } else if (message("HealedPopulationPermillage").equals(name)) {
                return HealedPopulationPermillageColorSetController.rgb();

            } else if (message("DeadPopulationPermillage").equals(name)) {
                return DeadPopulationPermillageColorSetController.rgb();

            } else if (message("ConfirmedAreaPermillage").equals(name)) {
                return ConfirmedAreaPermillageColorSetController.rgb();

            } else if (message("HealedAreaPermillage").equals(name)) {
                return HealedAreaPermillageColorSetController.rgb();

            } else if (message("DeadAreaPermillage").equals(name)) {
                return DeadAreaPermillageColorSetController.rgb();

            } else {
                return null;
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public String locationColor(String name) {
        try {
            if (name == null || name.isBlank()) {
                return null;
            }
            String controllerName = "EpidemicReportLocationColor" + name;
            Color color = Color.web(AppVariables.getUserConfigValue(controllerName, Color.GREY.toString()));
            return FxmlColor.color2rgb(color);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    /*
    get/set
     */
    public void setReportsController(EpidemicReportsController reportsController) {
        this.reportsController = reportsController;
    }

    public EpidemicReportsChartController getChartController() {
        return chartController;
    }

    public void setChartController(EpidemicReportsChartController chartController) {
        this.chartController = chartController;
    }

    public EpidemicReportsController getReportsController() {
        return reportsController;
    }

}
