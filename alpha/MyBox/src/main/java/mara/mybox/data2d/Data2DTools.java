package mara.mybox.data2d;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.ControlData2D;
import mara.mybox.controller.ControlData2DEditTable;
import mara.mybox.controller.ControlData2DLoad;
import mara.mybox.controller.Data2DChartBoxWhiskerController;
import mara.mybox.controller.Data2DChartComparisonBarsController;
import mara.mybox.controller.Data2DChartPieController;
import mara.mybox.controller.Data2DChartSelfComparisonBarsController;
import mara.mybox.controller.Data2DChartXYController;
import mara.mybox.controller.Data2DChartXYZController;
import mara.mybox.controller.Data2DConvertToDataBaseController;
import mara.mybox.controller.Data2DDeleteController;
import mara.mybox.controller.Data2DExportController;
import mara.mybox.controller.Data2DFrequencyController;
import mara.mybox.controller.Data2DGroupEqualValuesController;
import mara.mybox.controller.Data2DMultipleLinearRegressionCombinationController;
import mara.mybox.controller.Data2DMultipleLinearRegressionController;
import mara.mybox.controller.Data2DNormalizeController;
import mara.mybox.controller.Data2DPercentageController;
import mara.mybox.controller.Data2DRowExpressionController;
import mara.mybox.controller.Data2DSetStylesController;
import mara.mybox.controller.Data2DSetValuesController;
import mara.mybox.controller.Data2DSimpleLinearRegressionCombinationController;
import mara.mybox.controller.Data2DSimpleLinearRegressionController;
import mara.mybox.controller.Data2DSortController;
import mara.mybox.controller.Data2DStatisticController;
import mara.mybox.controller.Data2DTransposeController;
import mara.mybox.controller.DataTableQueryController;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-15
 * @License Apache License Version 2.0
 */
public class Data2DTools {

    public static List<MenuItem> editMenu(ControlData2D controller) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;
            ControlData2DEditTable tableController = controller.getTableController();
            Data2D data2D = tableController.getData2D();
            boolean invalidData = data2D == null || !data2D.isValid();
            boolean isTmpData = data2D != null && data2D.isTmpData();
            boolean empty = invalidData || tableController.getTableData().isEmpty();

            Menu dataMenu = new Menu(message("Data"), StyleTools.getIconImage("iconData.png"));
            items.add(dataMenu);

            menu = new MenuItem(message("Save"), StyleTools.getIconImage("iconSave.png"));
            menu.setOnAction((ActionEvent event) -> {
                controller.save();
            });
            menu.setDisable(invalidData || !tableController.isDataSizeLoaded());
            dataMenu.getItems().add(menu);

            dataMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("Recover"), StyleTools.getIconImage("iconRecover.png"));
            menu.setOnAction((ActionEvent event) -> {
                controller.recover();
            });
            menu.setDisable(invalidData || isTmpData);
            dataMenu.getItems().add(menu);

            menu = new MenuItem(message("Refresh"), StyleTools.getIconImage("iconRefresh.png"));
            menu.setOnAction((ActionEvent event) -> {
                controller.refreshAction();
            });
            menu.setDisable(invalidData || isTmpData);
            dataMenu.getItems().add(menu);

            dataMenu.getItems().add(new SeparatorMenuItem());

            if (data2D != null && data2D.isDataFile()) {
                menu = new MenuItem(message("Open"), StyleTools.getIconImage("iconOpen.png"));
                menu.setOnAction((ActionEvent event) -> {
                    controller.selectSourceFile();
                });
                dataMenu.getItems().add(menu);
            }

            menu = new MenuItem(message("CreateData"), StyleTools.getIconImage("iconAdd.png"));
            menu.setOnAction((ActionEvent event) -> {
                controller.create();
            });
            dataMenu.getItems().add(menu);

            menu = new MenuItem(message("LoadContentInSystemClipboard"), StyleTools.getIconImage("iconImageSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                controller.loadContentInSystemClipboard();
            });
            dataMenu.getItems().add(menu);

            dataMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("Export"), StyleTools.getIconImage("iconExport.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DExportController.open(tableController);
            });
            menu.setDisable(empty);
            dataMenu.getItems().add(menu);

            menu = new MenuItem(message("ConvertToDatabaseTable"), StyleTools.getIconImage("iconDatabase.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DConvertToDataBaseController.open(tableController);
            });
            menu.setDisable(invalidData);
            dataMenu.getItems().add(menu);

            items.add(new SeparatorMenuItem());

            Menu modifyMenu = new Menu(message("Modify"), StyleTools.getIconImage("iconEdit.png"));
            items.add(modifyMenu);

            menu = new MenuItem(message("SetValues"), StyleTools.getIconImage("iconEqual.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSetValuesController.open(tableController);
            });
            menu.setDisable(empty);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("Delete"), StyleTools.getIconImage("iconDelete.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDeleteController.open(tableController);
            });
            menu.setDisable(empty);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("SetStyles"), StyleTools.getIconImage("iconColor.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSetStylesController.open(tableController);
            });
            menu.setDisable(empty || isTmpData);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("PasteContentInSystemClipboard"), StyleTools.getIconImage("iconPasteSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                tableController.pasteContentInSystemClipboard();
            });
            menu.setDisable(invalidData);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("PasteContentInMyBoxClipboard"), StyleTools.getIconImage("iconPaste.png"));
            menu.setOnAction((ActionEvent event) -> {
                tableController.pasteContentInMyboxClipboard();
            });
            menu.setDisable(invalidData);
            modifyMenu.getItems().add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static List<MenuItem> operationsMenu(ControlData2DLoad controller) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;
            Data2D data2D = controller.getData2D();
            boolean invalidData = data2D == null || !data2D.isValid();
            boolean empty = invalidData || controller.getTableData().isEmpty();

            Menu trimMenu = new Menu(message("Trim"), StyleTools.getIconImage("iconClean.png"));
            items.add(trimMenu);

            if (data2D != null && data2D.isTable()) {
                menu = new MenuItem(message("Query"), StyleTools.getIconImage("iconQuery.png"));
                menu.setOnAction((ActionEvent event) -> {
                    DataTableQueryController.open(controller);
                });
                menu.setDisable(empty);
                trimMenu.getItems().add(menu);
            }

            menu = new MenuItem(message("CopyFilterQuery"), StyleTools.getIconImage("iconCopy.png"));
            menu.setOnAction((ActionEvent event) -> {
                controller.copyAction();
            });
            menu.setDisable(empty);
            trimMenu.getItems().add(menu);

            menu = new MenuItem(message("Sort"), StyleTools.getIconImage("iconSort.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSortController.open(controller);
            });
            menu.setDisable(empty);
            trimMenu.getItems().add(menu);

            menu = new MenuItem(message("Transpose"), StyleTools.getIconImage("iconRotateRight.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DTransposeController.open(controller);
            });
            menu.setDisable(empty);
            trimMenu.getItems().add(menu);

            menu = new MenuItem(message("Normalize"), StyleTools.getIconImage("iconBinary.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DNormalizeController.open(controller);
            });
            menu.setDisable(empty);
            trimMenu.getItems().add(menu);

            Menu calMenu = new Menu(message("Calculation"), StyleTools.getIconImage("iconCalculator.png"));
            items.add(calMenu);

            menu = new MenuItem(message("RowExpression"), StyleTools.getIconImage("iconCalculate.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DRowExpressionController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("DescriptiveStatistics"), StyleTools.getIconImage("iconStatistic.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DStatisticController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("GroupEqualValues"), StyleTools.getIconImage("iconAnalyse.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DGroupEqualValuesController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("SimpleLinearRegression"), StyleTools.getIconImage("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSimpleLinearRegressionController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("SimpleLinearRegressionCombination"), StyleTools.getIconImage("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSimpleLinearRegressionCombinationController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("MultipleLinearRegression"), StyleTools.getIconImage("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DMultipleLinearRegressionController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("MultipleLinearRegressionCombination"), StyleTools.getIconImage("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DMultipleLinearRegressionCombinationController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("FrequencyDistributions"), StyleTools.getIconImage("iconDistribution.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DFrequencyController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("ValuePercentage"), StyleTools.getIconImage("iconPercentage.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DPercentageController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            Menu chartMenu = new Menu(message("Charts"), StyleTools.getIconImage("iconGraph.png"));
            items.add(chartMenu);

            menu = new MenuItem(message("XYChart"), StyleTools.getIconImage("iconXYChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartXYController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("PieChart"), StyleTools.getIconImage("iconPieChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartPieController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("BoxWhiskerChart"), StyleTools.getIconImage("iconBoxWhiskerChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartBoxWhiskerController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("SelfComparisonBarsChart"), StyleTools.getIconImage("iconBarChartH.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartSelfComparisonBarsController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("ComparisonBarsChart"), StyleTools.getIconImage("iconComparisonBarsChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartComparisonBarsController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("XYZChart"), StyleTools.getIconImage("iconXYZChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartXYZController.open(controller);
            });
            menu.setDisable(data2D == null || data2D.columnsNumber() < 3);
            chartMenu.getItems().add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static List<MenuItem> examplesMenu(ControlData2D controller) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu;
            String lang = Languages.isChinese() ? "zh" : "en";

            // https://data.stats.gov.cn/index.htm
            Menu chinaMenu = new Menu(message("StatisticDataOfChina"), StyleTools.getIconImage("iconChina.png"));
            items.add(chinaMenu);

            menu = new MenuItem(message("ChinaPopulation"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaPopulation_" + lang + ".csv",
                        "data", "ChinaPopulation_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.String, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double
                );
                controller.loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaCensus"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaCensus_" + lang + ".csv",
                        "data", "ChinaCensus_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.String, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double
                );
                controller.loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaGDP"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaGDP_" + lang + ".csv",
                        "data", "ChinaGDP_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.String, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double
                );
                controller.loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaCPI"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaCPI_" + lang + ".csv",
                        "data", "ChinaCPI_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.String, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double
                );
                controller.loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaFoodConsumption"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaFoods_" + lang + ".csv",
                        "data", "ChinaFoods_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.String, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double
                );
                controller.loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaGraduates"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaGraduates_" + lang + ".csv",
                        "data", "ChinaGraduates_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.String, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double
                );
                controller.loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaMuseums"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaMuseums_" + lang + ".csv",
                        "data", "ChinaMuseums_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.String, ColumnDefinition.ColumnType.Long, ColumnDefinition.ColumnType.Long, ColumnDefinition.ColumnType.Long,
                        ColumnDefinition.ColumnType.Long, ColumnDefinition.ColumnType.Long, ColumnDefinition.ColumnType.Long, ColumnDefinition.ColumnType.Long,
                        ColumnDefinition.ColumnType.Long, ColumnDefinition.ColumnType.Double
                );
                controller.loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaHealthPersonnel"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaHealthPersonnel_" + lang + ".csv",
                        "data", "ChinaHealthPersonnel_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.String, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double
                );
                controller.loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaMarriage"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaMarriage_" + lang + ".csv",
                        "data", "ChinaMarriage_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.String, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double
                );
                controller.loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaSportWorldChampions"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaSportWorldChampions_" + lang + ".csv",
                        "data", "ChinaSportWorldChampions_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.String, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer,
                        ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer
                );
                controller.loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("CrimesFiledByChinaPolice"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaCrimesFiledByPolice_" + lang + ".csv",
                        "data", "ChinaCrimesFiledByPolice_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.String, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer,
                        ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer,
                        ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer
                );
                controller.loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("CrimesFiledByChinaProcuratorate"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ChinaCrimesFiledByProcuratorate_" + lang + ".csv",
                        "data", "ChinaCrimesFiledByProcuratorate_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.String, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer,
                        ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer,
                        ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer,
                        ColumnDefinition.ColumnType.Integer, ColumnDefinition.ColumnType.Integer
                );
                controller.loadCSVFile(file, columnTypes);
            });
            chinaMenu.getItems().add(menu);

            chinaMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("ChinaNationalBureauOfStatistics"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                controller.browse("https://data.stats.gov.cn/");
            });
            chinaMenu.getItems().add(menu);

            Menu regressionMenu = new Menu(message("Regression"), StyleTools.getIconImage("iconLinearPgression.png"));
            items.add(regressionMenu);

            // https://www.scribbr.com/statistics/simple-linear-regression/
            menu = new MenuItem(message("IncomeHappiness"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/IncomeHappiness_" + lang + ".csv",
                        "data", "IncomeHappiness_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double
                );
                controller.loadCSVFile(file, columnTypes);
            });
            regressionMenu.getItems().add(menu);

            // https://github.com/krishnaik06/simple-Linear-Regression
            menu = new MenuItem(message("ExperienceSalary"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ExperienceSalary_" + lang + ".csv",
                        "data", "ExperienceSalary_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double
                );
                controller.loadCSVFile(file, columnTypes);
            });
            regressionMenu.getItems().add(menu);

            // http://archive.ics.uci.edu/ml/datasets/Iris
            menu = new MenuItem(message("IrisSpecies"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/IrisSpecies_" + lang + ".csv",
                        "data", "IrisSpecies_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.String
                );
                controller.loadCSVFile(file, columnTypes);
            });
            regressionMenu.getItems().add(menu);

            // https://github.com/tomsharp/SVR/tree/master/data
            menu = new MenuItem(message("BostonHousingPrices"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/BostonHousingPrices_" + lang + ".csv",
                        "data", "BostonHousingPrices_" + lang + ".csv", true);
                List<ColumnDefinition.ColumnType> columnTypes = Arrays.asList(
                        ColumnDefinition.ColumnType.String, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.String, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double,
                        ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double, ColumnDefinition.ColumnType.Double
                );
                controller.loadCSVFile(file, columnTypes);
            });
            regressionMenu.getItems().add(menu);
            return items;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
