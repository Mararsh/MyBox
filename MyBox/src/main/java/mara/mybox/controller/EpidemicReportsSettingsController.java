package mara.mybox.controller;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import mara.mybox.data.GeographyCode;
import mara.mybox.db.TableStringValue;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-4-23
 * @License Apache License Version 2.0
 */
public class EpidemicReportsSettingsController extends BaseController {

    private EpidemicReportsController reportsController;
    private EpidemicReportsChartController chartController;

    protected String dataSelect, dataQuerySQL, dataSizeSQL, dataPageSQL,
            currentTitle, clearDataSQL;
    protected String baseWhere, dataWhere, dataOrder, dataFetch;
    protected int snapWidth, chartLoadTime;
    protected List<Rectangle> colorRects;
    protected Map<String, String> locationColors;
    protected String predefinedColor, filledColor, inputtedColor, statisticColor,
            confirmedColor, healedColor, deadColor,
            increasedConfirmedColor, increasedHealedColor, increasedDeadColor,
            HealedConfirmedPermillageColor, DeadConfirmedPermillageColor,
            ConfirmedPopulationPermillageColor, DeadPopulationPermillageColor, HealedPopulationPermillageColor,
            ConfirmedAreaPermillageColor, HealedAreaPermillageColor, DeadAreaPermillageColor;

    @FXML
    protected TabPane tabsPane;
    @FXML
    protected Tab chartOptionsTab, sourcesColorsTab, valuesColorsTab, locationsColorsTab;
    @FXML
    protected Button palettePredefinedButton, paletteInputtedButton, paletteFilledButton,
            paletteStatisticButton, paletteConfirmedButton, paletteHealedButton, paletteDeadButton,
            paletteIncreasedConfirmedButton, paletteIncreasedHealedButton, paletteIncreasedDeadButton,
            paletteHealedConfirmedPermillageButton, paletteDeadConfirmedPermillageButton,
            paletteConfirmedPopulationPermillageButton, paletteDeadPopulationPermillageButton, paletteHealedPopulationPermillageButton,
            paletteConfirmedAreaPermillageButton, paletteHealedAreaPermillageButton, paletteDeadAreaPermillageButton;
    @FXML
    protected Rectangle predefinedRect, inputtedRect, filledRect, statisticRect,
            confirmedRect, healedRect, deadRect,
            IncreasedConfirmedRect, IncreasedHealedRect, IncreasedDeadRect,
            HealedConfirmedPermillageRect, DeadConfirmedPermillageRect,
            ConfirmedPopulationPermillageRect, DeadPopulationPermillageRect, HealedPopulationPermillageRect,
            ConfirmedAreaPermillageRect, HealedAreaPermillageRect, DeadAreaPermillageRect;
    @FXML
    private ComboBox<String> widthSelector, chartTimeSelector;
    @FXML
    protected VBox locationColorsBox;

    public EpidemicReportsSettingsController() {
        baseTitle = message("EpidemicReport");
        TipsLabelKey = "EpidemicReportsChartColorComments";
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();
            colorRects = new ArrayList();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initChartOptions() {
        try {
            dpi = 96;
            List<String> dpiValues = new ArrayList();
            dpiValues.addAll(Arrays.asList("96", "120", "160", "300"));
            String sValue = Toolkit.getDefaultToolkit().getScreenResolution() + "";
            if (dpiValues.contains(sValue)) {
                dpiValues.remove(sValue);
            }
            dpiValues.add(0, sValue);
            sValue = (int) Screen.getPrimary().getDpi() + "";
            if (dpiValues.contains(sValue)) {
                dpiValues.remove(sValue);
            }
            dpiValues.add(sValue);
            dpiSelector.getItems().addAll(dpiValues);
            dpiSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            dpi = Integer.parseInt(newValue);
                            AppVariables.setUserConfigValue("EpidemicReportDPI", dpi + "");
                        } catch (Exception e) {
                            dpi = 96;
                        }
                    });
            dpiSelector.getSelectionModel().select(AppVariables.getUserConfigValue("EpidemicReportDPI", "96"));

            snapWidth = 800;
            List<String> widthValues = new ArrayList();
            widthValues.addAll(Arrays.asList("800", "1000", "500", "300", "1200", "1500"));
            widthSelector.getItems().addAll(widthValues);
            widthSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            snapWidth = Integer.parseInt(newValue);
                            AppVariables.setUserConfigValue("EpidemicReportSnapWidth", snapWidth + "");
                        } catch (Exception e) {
                            snapWidth = 800;
                        }
                    });
            widthSelector.getSelectionModel().select(AppVariables.getUserConfigValue("EpidemicReportSnapWidth", "800"));

            chartLoadTime = 300;
            chartTimeSelector.getItems().addAll(Arrays.asList(
                    "300", "1000", "100", "200", "500", "50", "2000"
            ));
            chartTimeSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.valueOf(chartTimeSelector.getValue());
                            if (v > 0) {
                                chartLoadTime = v;
                                AppVariables.setUserConfigValue("EpidemicReportChartLoadTime", chartLoadTime + "");
                                FxmlControl.setEditorNormal(chartTimeSelector);
                            } else {
                                FxmlControl.setEditorBadStyle(chartTimeSelector);
                            }
                        } catch (Exception e) {
                            logger.error(e.toString());
                        }
                    });
            chartTimeSelector.getSelectionModel().select(AppVariables.getUserConfigValue("EpidemicReportChartLoadTime", "300"));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initSourcesColors() {
        predefinedColor = FxmlColor.rgb2Hex(Color.LINEN);
        try {
            String saved = TableStringValue.read("EpidemicReportsPredefinedDataColor");
            predefinedColor = saved == null ? predefinedColor : saved;
            predefinedRect.setFill(Color.web(predefinedColor));
        } catch (Exception e) {
            predefinedRect.setFill(Color.LINEN);
            TableStringValue.write("EpidemicReportsPredefinedDataColor", predefinedColor);
        }
        FxmlControl.setTooltip(predefinedRect, FxmlColor.colorNameDisplay((Color) predefinedRect.getFill()));

        filledColor = FxmlColor.rgb2Hex(Color.THISTLE);
        try {
            String saved = TableStringValue.read("EpidemicReportsFilledDataColor");
            filledColor = saved == null ? filledColor : saved;
            filledRect.setFill(Color.web(filledColor));
        } catch (Exception e) {
            filledRect.setFill(Color.THISTLE);
            TableStringValue.write("EpidemicReportsFilledDataColor", filledColor);
        }
        FxmlControl.setTooltip(filledRect, FxmlColor.colorNameDisplay((Color) filledRect.getFill()));

        inputtedColor = FxmlColor.rgb2Hex(Color.WHITE);
        try {
            String saved = TableStringValue.read("EpidemicReportsInputtedDataColor");
            inputtedColor = saved == null ? inputtedColor : saved;
            inputtedRect.setFill(Color.web(inputtedColor));
        } catch (Exception e) {
            inputtedRect.setFill(Color.WHITE);
            TableStringValue.write("EpidemicReportsInputtedDataColor", inputtedColor);
        }
        FxmlControl.setTooltip(inputtedRect, FxmlColor.colorNameDisplay((Color) inputtedRect.getFill()));

        statisticColor = FxmlColor.rgb2Hex(Color.DARKSEAGREEN);
        try {
            String saved = TableStringValue.read("EpidemicReportsStatisticDataColor");
            statisticColor = saved == null ? statisticColor : saved;
            statisticRect.setFill(Color.web(statisticColor));
        } catch (Exception e) {
            statisticRect.setFill(Color.DARKSEAGREEN);
            TableStringValue.write("EpidemicReportsStatisticDataColor", statisticColor);
        }
        FxmlControl.setTooltip(statisticRect, FxmlColor.colorNameDisplay((Color) statisticRect.getFill()));

    }

    protected void initValuesColors() {
        confirmedColor = FxmlColor.rgb2Hex(Color.BLUE);
        try {
            String saved = TableStringValue.read("EpidemicReportsConfirmedColor");
            confirmedColor = saved == null ? confirmedColor : saved;
            confirmedRect.setFill(Color.web(confirmedColor));
        } catch (Exception e) {
            confirmedRect.setFill(Color.BLUE);
            TableStringValue.write("EpidemicReportsConfirmedColor", confirmedColor);
        }
        FxmlControl.setTooltip(confirmedRect, FxmlColor.colorNameDisplay((Color) confirmedRect.getFill()));

        healedColor = FxmlColor.rgb2Hex(Color.RED);
        try {
            String saved = TableStringValue.read("EpidemicReportsHealedColor");
            healedColor = saved == null ? healedColor : saved;
            healedRect.setFill(Color.web(healedColor));
        } catch (Exception e) {
            healedRect.setFill(Color.RED);
            TableStringValue.write("EpidemicReportsHealedColor", healedColor);
        }
        FxmlControl.setTooltip(healedRect, FxmlColor.colorNameDisplay((Color) healedRect.getFill()));

        deadColor = FxmlColor.rgb2Hex(Color.BLACK);
        try {
            String saved = TableStringValue.read("EpidemicReportsDeadColor");
            deadColor = saved == null ? deadColor : saved;
            deadRect.setFill(Color.web(deadColor));
        } catch (Exception e) {
            deadRect.setFill(Color.BLACK);
            TableStringValue.write("EpidemicReportsDeadColor", deadColor);
        }
        FxmlControl.setTooltip(deadRect, FxmlColor.colorNameDisplay((Color) deadRect.getFill()));

        increasedConfirmedColor = FxmlColor.rgb2Hex(Color.SLATEBLUE);
        try {
            String saved = TableStringValue.read("EpidemicReportsIncreasedConfirmedColor");
            increasedConfirmedColor = saved == null ? increasedConfirmedColor : saved;
            IncreasedConfirmedRect.setFill(Color.web(increasedConfirmedColor));
        } catch (Exception e) {
            IncreasedConfirmedRect.setFill(Color.SLATEBLUE);
            TableStringValue.write("EpidemicReportsIncreasedConfirmedColor", increasedConfirmedColor);
        }
        FxmlControl.setTooltip(IncreasedConfirmedRect, FxmlColor.colorNameDisplay((Color) IncreasedConfirmedRect.getFill()));

        increasedHealedColor = FxmlColor.rgb2Hex(Color.HOTPINK);
        try {
            String saved = TableStringValue.read("EpidemicReportsIncreasedHealedColor");
            increasedHealedColor = saved == null ? increasedHealedColor : saved;
            IncreasedHealedRect.setFill(Color.web(increasedHealedColor));
        } catch (Exception e) {
            IncreasedHealedRect.setFill(Color.HOTPINK);
            TableStringValue.write("EpidemicReportsIncreasedHealedColor", increasedHealedColor);
        }
        FxmlControl.setTooltip(IncreasedHealedRect, FxmlColor.colorNameDisplay((Color) IncreasedHealedRect.getFill()));

        increasedDeadColor = FxmlColor.rgb2Hex(Color.GRAY);
        try {
            String saved = TableStringValue.read("EpidemicReportsIncreasedDeadColor");
            increasedDeadColor = saved == null ? increasedDeadColor : saved;
            IncreasedDeadRect.setFill(Color.web(increasedDeadColor));
        } catch (Exception e) {
            IncreasedDeadRect.setFill(Color.GRAY);
            TableStringValue.write("EpidemicReportsIncreasedDeadColor", increasedDeadColor);
        }
        FxmlControl.setTooltip(IncreasedDeadRect, FxmlColor.colorNameDisplay((Color) IncreasedDeadRect.getFill()));

        HealedConfirmedPermillageColor = FxmlColor.rgb2Hex(Color.PALEGREEN);
        try {
            String saved = TableStringValue.read("EpidemicReportsHealedConfirmedPermillageRect");
            HealedConfirmedPermillageColor = saved == null ? HealedConfirmedPermillageColor : saved;
            HealedConfirmedPermillageRect.setFill(Color.web(HealedConfirmedPermillageColor));
        } catch (Exception e) {
            HealedConfirmedPermillageRect.setFill(Color.PALEGREEN);
            TableStringValue.write("EpidemicReportsHealedConfirmedPermillageRect", HealedConfirmedPermillageColor);
        }
        FxmlControl.setTooltip(HealedConfirmedPermillageRect, FxmlColor.colorNameDisplay((Color) HealedConfirmedPermillageRect.getFill()));

        DeadConfirmedPermillageColor = FxmlColor.rgb2Hex(Color.STEELBLUE);
        try {
            String saved = TableStringValue.read("EpidemicReportsDeadConfirmedPermillageRect");
            DeadConfirmedPermillageColor = saved == null ? DeadConfirmedPermillageColor : saved;
            DeadConfirmedPermillageRect.setFill(Color.web(DeadConfirmedPermillageColor));
        } catch (Exception e) {
            DeadConfirmedPermillageRect.setFill(Color.STEELBLUE);
            TableStringValue.write("EpidemicReportsDeadConfirmedPermillageRect", DeadConfirmedPermillageColor);
        }
        FxmlControl.setTooltip(DeadConfirmedPermillageRect, FxmlColor.colorNameDisplay((Color) DeadConfirmedPermillageRect.getFill()));

        ConfirmedPopulationPermillageColor = FxmlColor.rgb2Hex(Color.MEDIUMPURPLE);
        try {
            String saved = TableStringValue.read("EpidemicReportsConfirmedPopulationPermillageColor");
            ConfirmedPopulationPermillageColor = saved == null ? ConfirmedPopulationPermillageColor : saved;
            ConfirmedPopulationPermillageRect.setFill(Color.web(ConfirmedPopulationPermillageColor));
        } catch (Exception e) {
            ConfirmedPopulationPermillageRect.setFill(Color.MEDIUMPURPLE);
            TableStringValue.write("EpidemicReportsConfirmedPopulationPermillageColor", ConfirmedPopulationPermillageColor);
        }
        FxmlControl.setTooltip(ConfirmedPopulationPermillageRect, FxmlColor.colorNameDisplay((Color) ConfirmedPopulationPermillageRect.getFill()));

        DeadPopulationPermillageColor = FxmlColor.rgb2Hex(Color.SADDLEBROWN);
        try {
            String saved = TableStringValue.read("EpidemicReportsDeadPopulationPermillageColor");
            DeadPopulationPermillageColor = saved == null ? DeadPopulationPermillageColor : saved;
            DeadPopulationPermillageRect.setFill(Color.web(DeadPopulationPermillageColor));
        } catch (Exception e) {
            DeadPopulationPermillageRect.setFill(Color.SADDLEBROWN);
            TableStringValue.write("EpidemicReportsDeadPopulationPermillageColor", DeadPopulationPermillageColor);
        }
        FxmlControl.setTooltip(DeadPopulationPermillageRect, FxmlColor.colorNameDisplay((Color) DeadPopulationPermillageRect.getFill()));

        HealedPopulationPermillageColor = FxmlColor.rgb2Hex(Color.LIGHTPINK);
        try {
            String saved = TableStringValue.read("EpidemicReportsHealedPopulationPermillageColor");
            HealedPopulationPermillageColor = saved == null ? HealedPopulationPermillageColor : saved;
            HealedPopulationPermillageRect.setFill(Color.web(HealedPopulationPermillageColor));
        } catch (Exception e) {
            HealedPopulationPermillageRect.setFill(Color.LIGHTPINK);
            TableStringValue.write("EpidemicReportsHealedPopulationPermillageColor", HealedPopulationPermillageColor);
        }
        FxmlControl.setTooltip(HealedPopulationPermillageRect, FxmlColor.colorNameDisplay((Color) HealedPopulationPermillageRect.getFill()));

        ConfirmedAreaPermillageColor = FxmlColor.rgb2Hex(Color.BLUEVIOLET);
        try {
            String saved = TableStringValue.read("EpidemicReportsConfirmedAreaPermillageColor");
            ConfirmedAreaPermillageColor = saved == null ? ConfirmedAreaPermillageColor : saved;
            ConfirmedAreaPermillageRect.setFill(Color.web(ConfirmedAreaPermillageColor));
        } catch (Exception e) {
            ConfirmedAreaPermillageRect.setFill(Color.BLUEVIOLET);
            TableStringValue.write("EpidemicReportsConfirmedAreaPermillageColor", ConfirmedAreaPermillageColor);
        }
        FxmlControl.setTooltip(ConfirmedAreaPermillageRect, FxmlColor.colorNameDisplay((Color) ConfirmedAreaPermillageRect.getFill()));

        HealedAreaPermillageColor = FxmlColor.rgb2Hex(Color.MEDIUMVIOLETRED);
        try {
            String saved = TableStringValue.read("EpidemicReportsHealedAreaPermillageColor");
            HealedAreaPermillageColor = saved == null ? HealedAreaPermillageColor : saved;
            HealedAreaPermillageRect.setFill(Color.web(HealedAreaPermillageColor));
        } catch (Exception e) {
            HealedAreaPermillageRect.setFill(Color.MEDIUMVIOLETRED);
            TableStringValue.write("EpidemicReportsHealedAreaPermillageColor", HealedAreaPermillageColor);
        }
        FxmlControl.setTooltip(HealedAreaPermillageRect, FxmlColor.colorNameDisplay((Color) HealedAreaPermillageRect.getFill()));

        DeadAreaPermillageColor = FxmlColor.rgb2Hex(Color.SILVER);
        try {
            String saved = TableStringValue.read("EpidemicReportsDeadAreaPermillageColor");
            DeadAreaPermillageColor = saved == null ? DeadAreaPermillageColor : saved;
            DeadAreaPermillageRect.setFill(Color.web(DeadAreaPermillageColor));
        } catch (Exception e) {
            DeadAreaPermillageRect.setFill(Color.SILVER);
            TableStringValue.write("EpidemicReportsDeadAreaPermillageColor", DeadAreaPermillageColor);
        }
        FxmlControl.setTooltip(DeadAreaPermillageRect, FxmlColor.colorNameDisplay((Color) DeadAreaPermillageRect.getFill()));

    }

    public void adjustTabs(boolean setCharts) {
        tabsPane.getTabs().clear();
        if (setCharts) {
            tabsPane.getTabs().addAll(chartOptionsTab, sourcesColorsTab, valuesColorsTab, locationsColorsTab);
        } else {
            tabsPane.getTabs().add(sourcesColorsTab);
        }
        initColors();
    }

    public void initColors() {
        if (confirmedColor == null) {
            initChartOptions();
            initValuesColors();
        }
        if (predefinedColor == null) {
            initSourcesColors();
        }

        makeLocationsColors();
    }

    protected void makeLocationsColors() {
        try {
            colorRects.clear();
            locationColorsBox.getChildren().clear();
            List<GeographyCode> locations = chartController.chartLocations;
            if (locations == null || locationColors == null) {
                return;
            }
            Collections.sort(locations, (GeographyCode p1, GeographyCode p2)
                    -> p1.getFullName().compareTo(p2.getFullName()));
            for (int i = 0; i < locations.size(); i++) {
                GeographyCode location = locations.get(i);
                String name = location.getFullName();
                String color = locationColors.get(name);
                Label label = new Label(name);
                Rectangle rect = new Rectangle();
                rect.setWidth(15);
                rect.setHeight(15);
                Color c = Color.web(color);
                rect.setFill(c);
                FxmlControl.setTooltip(rect, new Tooltip(FxmlColor.colorNameDisplay(c)));
                rect.setUserData(name);
                colorRects.add(rect);

                Button button = new Button();
                ImageView image = new ImageView(ControlStyle.getIcon("iconPalette.png"));
                image.setFitWidth(AppVariables.iconSize);
                image.setFitHeight(AppVariables.iconSize);
                button.setGraphic(image);
                button.setOnAction((ActionEvent event) -> {
                    showPalette(button, message("Settings") + " - " + name);
                });
                button.setUserData(i);
                VBox.setMargin(button, new Insets(0, 0, 0, 15));
                FxmlControl.setTooltip(button, message("Palette"));

                HBox line = new HBox();
                line.setAlignment(Pos.CENTER_LEFT);
                line.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                line.setSpacing(5);
                VBox.setVgrow(line, Priority.ALWAYS);
                HBox.setHgrow(line, Priority.ALWAYS);
                line.getChildren().addAll(label, rect, button);

                locationColorsBox.getChildren().add(line);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    public void applyValuesColors() {
        Color color = (Color) (confirmedRect.getFill());
        confirmedColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsConfirmedColor", confirmedColor);

        color = (Color) (healedRect.getFill());
        healedColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsHealedColor", healedColor);

        color = (Color) (deadRect.getFill());
        deadColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsDeadColor", deadColor);

        color = (Color) (IncreasedConfirmedRect.getFill());
        increasedConfirmedColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsIncreasedConfirmedColor", increasedConfirmedColor);

        color = (Color) (IncreasedHealedRect.getFill());
        increasedHealedColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsIncreasedHealedColor", increasedHealedColor);

        color = (Color) (IncreasedDeadRect.getFill());
        increasedDeadColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsIncreasedDeadColor", increasedDeadColor);

        color = (Color) (HealedConfirmedPermillageRect.getFill());
        HealedConfirmedPermillageColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsHealedConfirmedPermillageColor", HealedConfirmedPermillageColor);

        color = (Color) (DeadConfirmedPermillageRect.getFill());
        DeadConfirmedPermillageColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsDeadConfirmedPermillageColor", DeadConfirmedPermillageColor);

        color = (Color) (ConfirmedPopulationPermillageRect.getFill());
        ConfirmedPopulationPermillageColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsConfirmedPopulationPermillageColor", ConfirmedPopulationPermillageColor);

        color = (Color) (DeadPopulationPermillageRect.getFill());
        DeadPopulationPermillageColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsDeadPopulationPermillageColor", DeadPopulationPermillageColor);

        color = (Color) (HealedPopulationPermillageRect.getFill());
        HealedPopulationPermillageColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsHealedPopulationPermillageColor", HealedPopulationPermillageColor);

        color = (Color) (ConfirmedAreaPermillageRect.getFill());
        ConfirmedAreaPermillageColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsConfirmedAreaPermillageRect", ConfirmedAreaPermillageColor);

        color = (Color) (HealedAreaPermillageRect.getFill());
        HealedAreaPermillageColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsHealedAreaPermillageColor", HealedAreaPermillageColor);

        color = (Color) (DeadAreaPermillageRect.getFill());
        DeadAreaPermillageColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsDeadAreaPermillageColor", DeadAreaPermillageColor);

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
        locationColors.clear();
        for (Rectangle cRect : colorRects) {
            Color cColor = (Color) (cRect.getFill());
            String colorValue = FxmlColor.rgb2Hex(cColor);
            locationColors.put((String) cRect.getUserData(), colorValue);
        }
        TableStringValue.writeWithPrefix("EpidemicReportsLocationColor", locationColors);
        chartController.drawChart();
        popSuccessful();
    }

    @Override
    public boolean setColor(Control control, Color color) {
        if (control == null || color == null) {
            return false;
        }
        try {
            if (palettePredefinedButton.equals(control)) {
                predefinedRect.setFill(color);
                FxmlControl.setTooltip(predefinedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteInputtedButton.equals(control)) {
                inputtedRect.setFill(color);
                FxmlControl.setTooltip(inputtedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteFilledButton.equals(control)) {
                filledRect.setFill(color);
                FxmlControl.setTooltip(filledRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteStatisticButton.equals(control)) {
                statisticRect.setFill(color);
                FxmlControl.setTooltip(statisticRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteConfirmedButton.equals(control)) {
                confirmedRect.setFill(color);
                FxmlControl.setTooltip(confirmedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteHealedButton.equals(control)) {
                healedRect.setFill(color);
                FxmlControl.setTooltip(healedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteDeadButton.equals(control)) {
                deadRect.setFill(color);
                FxmlControl.setTooltip(deadRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteIncreasedConfirmedButton.equals(control)) {
                IncreasedConfirmedRect.setFill(color);
                FxmlControl.setTooltip(IncreasedConfirmedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteIncreasedHealedButton.equals(control)) {
                IncreasedHealedRect.setFill(color);
                FxmlControl.setTooltip(IncreasedHealedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteIncreasedDeadButton.equals(control)) {
                IncreasedDeadRect.setFill(color);
                FxmlControl.setTooltip(IncreasedDeadRect, new Tooltip(FxmlColor.colorNameDisplay(color)));
//
            } else if (paletteHealedConfirmedPermillageButton.equals(control)) {
                HealedConfirmedPermillageRect.setFill(color);
                FxmlControl.setTooltip(HealedConfirmedPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteDeadConfirmedPermillageButton.equals(control)) {
                DeadConfirmedPermillageRect.setFill(color);
                FxmlControl.setTooltip(DeadConfirmedPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteConfirmedPopulationPermillageButton.equals(control)) {
                ConfirmedPopulationPermillageRect.setFill(color);
                FxmlControl.setTooltip(ConfirmedPopulationPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteDeadPopulationPermillageButton.equals(control)) {
                DeadPopulationPermillageRect.setFill(color);
                FxmlControl.setTooltip(DeadPopulationPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteHealedPopulationPermillageButton.equals(control)) {
                HealedPopulationPermillageRect.setFill(color);
                FxmlControl.setTooltip(HealedPopulationPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteConfirmedAreaPermillageButton.equals(control)) {
                ConfirmedAreaPermillageRect.setFill(color);
                FxmlControl.setTooltip(ConfirmedAreaPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteHealedAreaPermillageButton.equals(control)) {
                HealedAreaPermillageRect.setFill(color);
                FxmlControl.setTooltip(HealedAreaPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (paletteDeadAreaPermillageButton.equals(control)) {
                DeadAreaPermillageRect.setFill(color);
                FxmlControl.setTooltip(DeadAreaPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

            } else if (control.getUserData() != null) {
                if (colorRects == null || colorRects.isEmpty()) {
                    return true;
                }
                int index = (int) (control.getUserData());
                Rectangle rect = colorRects.get(index);
                rect.setFill(color);
                FxmlControl.setTooltip(rect, new Tooltip(FxmlColor.colorNameDisplay(color)));
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            popError(e.toString());
            return false;
        }
    }

    @FXML
    public void palettePredefined() {
        showPalette(palettePredefinedButton, message("Settings") + " - " + message("PredefinedData"));
    }

    @FXML
    public void paletteInputted() {
        showPalette(paletteInputtedButton, message("Settings") + " - " + message("InputtedData"));

    }

    @FXML
    public void paletteFilled() {
        showPalette(paletteFilledButton, message("Settings") + " - " + message("FilledData"));

    }

    @FXML
    public void paletteStatistic() {
        showPalette(paletteStatisticButton, message("Settings") + " - " + message("StatisticData"));

    }

    @FXML
    public void paletteConfirmed() {
        showPalette(paletteConfirmedButton, message("Settings") + " - " + message("Confirmed"));

    }

    @FXML
    public void paletteHealed() {
        showPalette(paletteHealedButton, message("Settings") + " - " + message("Healed"));

    }

    @FXML
    public void paletteDead() {
        showPalette(paletteDeadButton, message("Settings") + " - " + message("Dead"));
    }

    @FXML
    public void paletteIncreasedConfirmed() {
        showPalette(paletteIncreasedConfirmedButton, message("Settings") + " - " + message("IncreasedConfirmed"));
    }

    @FXML
    public void paletteIncreasedHealed() {
        showPalette(paletteIncreasedHealedButton, message("Settings") + " - " + message("IncreasedHealed"));
    }

    @FXML
    public void paletteIncreasedDead() {
        showPalette(paletteIncreasedDeadButton, message("Settings") + " - " + message("IncreasedDead"));
    }

    @FXML
    public void paletteHealedConfirmedPermillage() {
        showPalette(paletteHealedConfirmedPermillageButton, message("Settings") + " - " + message("HealedConfirmedPermillage"));
    }

    @FXML
    public void paletteDeadConfirmedPermillage() {
        showPalette(paletteDeadConfirmedPermillageButton, message("Settings") + " - " + message("DeadConfirmedPermillage"));
    }

    @FXML
    public void paletteConfirmedPopulationPermillage() {
        showPalette(paletteConfirmedPopulationPermillageButton, message("Settings") + " - " + message("ConfirmedPopulationPermillage"));
    }

    @FXML
    public void paletteDeadPopulationPermillage() {
        showPalette(paletteDeadPopulationPermillageButton, message("Settings") + " - " + message("DeadPopulationPermillage"));
    }

    @FXML
    public void paletteHealedPopulationPermillage() {
        showPalette(paletteHealedPopulationPermillageButton, message("Settings") + " - " + message("HealedPopulationPermillage"));
    }

    @FXML
    public void paletteConfirmedAreaPermillage() {
        showPalette(paletteConfirmedAreaPermillageButton, message("Settings") + " - " + message("ConfirmedAreaPermillage"));
    }

    @FXML
    public void paletteHealedAreaPermillage() {
        showPalette(paletteHealedAreaPermillageButton, message("Settings") + " - " + message("HealedAreaPermillage"));
    }

    @FXML
    public void paletteDeadAreaPermillage() {
        showPalette(paletteDeadAreaPermillageButton, message("Settings") + " - " + message("DeadAreaPermillage"));
    }

    @FXML
    public void defaultSourcesColors() {
        Color color = Color.LINEN;
        predefinedRect.setFill(color);
        FxmlControl.setTooltip(predefinedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.THISTLE;
        filledRect.setFill(color);
        FxmlControl.setTooltip(filledRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.WHITE;
        inputtedRect.setFill(color);
        FxmlControl.setTooltip(inputtedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.DARKSEAGREEN;
        statisticRect.setFill(color);
        FxmlControl.setTooltip(statisticRect, new Tooltip(FxmlColor.colorNameDisplay(color)));
    }

    @FXML
    public void randomSourcesColor() {
        List<String> colors = FxmlColor.randomColorsHex(4);

        Color color = Color.web(colors.get(0));
        predefinedRect.setFill(color);
        FxmlControl.setTooltip(predefinedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(1));
        filledRect.setFill(color);
        FxmlControl.setTooltip(filledRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(2));
        inputtedRect.setFill(color);
        FxmlControl.setTooltip(inputtedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(3));
        statisticRect.setFill(color);
        FxmlControl.setTooltip(statisticRect, new Tooltip(FxmlColor.colorNameDisplay(color)));
    }

    @FXML
    public void applySourcesColors() {
        Color color = (Color) (predefinedRect.getFill());
        predefinedColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsPredefinedDataColor", predefinedColor);

        color = (Color) (filledRect.getFill());
        filledColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsFilledDataColor", filledColor);

        color = (Color) (inputtedRect.getFill());
        inputtedColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsInputtedDataColor", inputtedColor);

        color = (Color) (statisticRect.getFill());
        statisticColor = FxmlColor.rgb2Hex(color);
        TableStringValue.write("EpidemicReportsStatisticDataColor", statisticColor);

        reportsController.tableView.refresh();
        popSuccessful();
    }

    @FXML
    public void defaultValuesColors() {
        Color color = Color.BLUE;
        confirmedRect.setFill(color);
        FxmlControl.setTooltip(confirmedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.RED;
        healedRect.setFill(color);
        FxmlControl.setTooltip(healedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.BLACK;
        deadRect.setFill(color);
        FxmlControl.setTooltip(deadRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.SLATEBLUE;
        IncreasedConfirmedRect.setFill(color);
        FxmlControl.setTooltip(IncreasedConfirmedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.HOTPINK;
        IncreasedHealedRect.setFill(color);
        FxmlControl.setTooltip(IncreasedHealedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.GRAY;
        IncreasedDeadRect.setFill(color);
        FxmlControl.setTooltip(IncreasedDeadRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.PALEGREEN;
        HealedConfirmedPermillageRect.setFill(color);
        FxmlControl.setTooltip(HealedConfirmedPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.STEELBLUE;
        DeadConfirmedPermillageRect.setFill(color);
        FxmlControl.setTooltip(DeadConfirmedPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.MEDIUMPURPLE;
        ConfirmedPopulationPermillageRect.setFill(color);
        FxmlControl.setTooltip(ConfirmedPopulationPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.SADDLEBROWN;
        DeadPopulationPermillageRect.setFill(color);
        FxmlControl.setTooltip(DeadPopulationPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.LIGHTPINK;
        HealedPopulationPermillageRect.setFill(color);
        FxmlControl.setTooltip(HealedPopulationPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.BLUEVIOLET;
        ConfirmedAreaPermillageRect.setFill(color);
        FxmlControl.setTooltip(ConfirmedAreaPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.MEDIUMVIOLETRED;
        HealedAreaPermillageRect.setFill(color);
        FxmlControl.setTooltip(HealedAreaPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.SILVER;
        DeadAreaPermillageRect.setFill(color);
        FxmlControl.setTooltip(DeadAreaPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

    }

    @FXML
    public void randomValuesColors() {
        List<String> colors = FxmlColor.randomColorsHex(14);

        Color color = Color.web(colors.get(0));
        confirmedRect.setFill(color);
        FxmlControl.setTooltip(confirmedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(1));
        healedRect.setFill(color);
        FxmlControl.setTooltip(healedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(2));
        deadRect.setFill(color);
        FxmlControl.setTooltip(deadRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(3));
        IncreasedConfirmedRect.setFill(color);
        FxmlControl.setTooltip(IncreasedConfirmedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(4));
        IncreasedHealedRect.setFill(color);
        FxmlControl.setTooltip(IncreasedHealedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(5));
        IncreasedDeadRect.setFill(color);
        FxmlControl.setTooltip(IncreasedDeadRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(6));
        HealedConfirmedPermillageRect.setFill(color);
        FxmlControl.setTooltip(HealedConfirmedPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(7));
        DeadConfirmedPermillageRect.setFill(color);
        FxmlControl.setTooltip(DeadConfirmedPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(8));
        ConfirmedPopulationPermillageRect.setFill(color);
        FxmlControl.setTooltip(ConfirmedPopulationPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(9));
        DeadPopulationPermillageRect.setFill(color);
        FxmlControl.setTooltip(DeadPopulationPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(10));
        HealedPopulationPermillageRect.setFill(color);
        FxmlControl.setTooltip(HealedPopulationPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(11));
        ConfirmedAreaPermillageRect.setFill(color);
        FxmlControl.setTooltip(ConfirmedAreaPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(12));
        HealedAreaPermillageRect.setFill(color);
        FxmlControl.setTooltip(HealedAreaPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(13));
        DeadAreaPermillageRect.setFill(color);
        FxmlControl.setTooltip(DeadAreaPermillageRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

    }

    @FXML
    public void randomLocationsDataColors() {
        if (chartController == null || chartController.chartLocations == null) {
            return;
        }
        List<String> colors = FxmlColor.randomColorsHex(chartController.chartLocations.size());
        for (int i = 0; i < chartController.chartLocations.size(); i++) {
            GeographyCode location = chartController.chartLocations.get(i);
            locationColors.put(location.getFullName(), colors.get(i));
        }
        TableStringValue.writeWithPrefix("EpidemicReportsLocationColor", locationColors);
        makeLocationsColors();
    }

    public String color(String name) {
        try {
            if (name == null || name.isBlank()) {
                return null;
            }
            if (message("Confirmed").equals(name)) {
                return confirmedColor;

            } else if (message("Healed").equals(name)) {
                return healedColor;

            } else if (message("Dead").equals(name)) {
                return deadColor;

            } else if (message("IncreasedConfirmed").equals(name)) {
                return increasedConfirmedColor;

            } else if (message("IncreasedHealed").equals(name)) {
                return increasedHealedColor;

            } else if (message("IncreasedDead").equals(name)) {
                return increasedDeadColor;

            } else if (message("HealedConfirmedPermillage").equals(name)) {
                return HealedConfirmedPermillageColor;

            } else if (message("DeadConfirmedPermillage").equals(name)) {
                return DeadConfirmedPermillageColor;

            } else if (message("ConfirmedPopulationPermillage").equals(name)) {
                return ConfirmedPopulationPermillageColor;

            } else if (message("HealedPopulationPermillage").equals(name)) {
                return DeadPopulationPermillageColor;

            } else if (message("DeadPopulationPermillage").equals(name)) {
                return HealedPopulationPermillageColor;

            } else if (message("ConfirmedAreaPermillage").equals(name)) {
                return ConfirmedAreaPermillageColor;

            } else if (message("HealedAreaPermillage").equals(name)) {
                return HealedAreaPermillageColor;

            } else if (message("DeadAreaPermillage").equals(name)) {
                return DeadAreaPermillageColor;

            } else {
                return null;
            }

        } catch (Exception e) {
            logger.debug(e.toString());
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

    public String getPredefinedColor() {
        return predefinedColor;
    }

    public void setPredefinedColor(String predefinedColor) {
        this.predefinedColor = predefinedColor;
    }

    public String getFilledColor() {
        return filledColor;
    }

    public void setFilledColor(String filledColor) {
        this.filledColor = filledColor;
    }

    public String getInputtedColor() {
        return inputtedColor;
    }

    public void setInputtedColor(String inputtedColor) {
        this.inputtedColor = inputtedColor;
    }

    public String getStatisticColor() {
        return statisticColor;
    }

    public void setStatisticColor(String statisticColor) {
        this.statisticColor = statisticColor;
    }

    public String getConfirmedColor() {
        return confirmedColor;
    }

    public void setConfirmedColor(String confirmedColor) {
        this.confirmedColor = confirmedColor;
    }

    public String getHealedColor() {
        return healedColor;
    }

    public void setHealedColor(String healedColor) {
        this.healedColor = healedColor;
    }

    public String getDeadColor() {
        return deadColor;
    }

    public void setDeadColor(String deadColor) {
        this.deadColor = deadColor;
    }

}
