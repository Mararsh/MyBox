package mara.mybox.data2d;

import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.ControlData2D;
import mara.mybox.controller.ControlData2DEditTable;
import mara.mybox.controller.ControlData2DLoad;
import mara.mybox.controller.Data2DAddRowsController;
import mara.mybox.controller.Data2DChartBoxWhiskerController;
import mara.mybox.controller.Data2DChartComparisonBarsController;
import mara.mybox.controller.Data2DChartGroupBoxWhiskerController;
import mara.mybox.controller.Data2DChartGroupComparisonBarsController;
import mara.mybox.controller.Data2DChartGroupPieController;
import mara.mybox.controller.Data2DChartGroupSelfComparisonBarsController;
import mara.mybox.controller.Data2DChartGroupXYController;
import mara.mybox.controller.Data2DChartPieController;
import mara.mybox.controller.Data2DChartSelfComparisonBarsController;
import mara.mybox.controller.Data2DChartXYController;
import mara.mybox.controller.Data2DChartXYZController;
import mara.mybox.controller.Data2DConvertToDataBaseController;
import mara.mybox.controller.Data2DDeleteController;
import mara.mybox.controller.Data2DExportController;
import mara.mybox.controller.Data2DFrequencyController;
import mara.mybox.controller.Data2DGroupController;
import mara.mybox.controller.Data2DGroupStatisticController;
import mara.mybox.controller.Data2DLocationDistributionController;
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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-15
 * @License Apache License Version 2.0
 */
public class Data2DMenuTools {

    public static Menu dataMenu(ControlData2D controller) {
        try {
            MenuItem menu;
            ControlData2DEditTable tableController = controller.getTableController();
            Data2D data2D = tableController.getData2D();
            boolean invalidData = data2D == null || !data2D.isValid();
            boolean isTmpData = data2D != null && data2D.isTmpData();
            boolean empty = invalidData || tableController.getTableData().isEmpty();

            Menu dataMenu = new Menu(message("Data"), StyleTools.getIconImageView("iconData.png"));

            menu = new MenuItem(message("Save"), StyleTools.getIconImageView("iconSave.png"));
            menu.setOnAction((ActionEvent event) -> {
                controller.save();
            });
            menu.setDisable(invalidData || !tableController.isDataSizeLoaded());
            dataMenu.getItems().add(menu);

            dataMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("Recover"), StyleTools.getIconImageView("iconRecover.png"));
            menu.setOnAction((ActionEvent event) -> {
                controller.recover();
            });
            menu.setDisable(invalidData || isTmpData);
            dataMenu.getItems().add(menu);

            menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
            menu.setOnAction((ActionEvent event) -> {
                controller.refreshAction();
            });
            menu.setDisable(invalidData || isTmpData);
            dataMenu.getItems().add(menu);

            dataMenu.getItems().add(new SeparatorMenuItem());

            if (data2D != null && data2D.isDataFile()) {
                menu = new MenuItem(message("Open"), StyleTools.getIconImageView("iconSelectFile.png"));
                menu.setOnAction((ActionEvent event) -> {
                    controller.selectSourceFile();
                });
                dataMenu.getItems().add(menu);
            }

            menu = new MenuItem(message("CreateData"), StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction((ActionEvent event) -> {
                controller.create();
            });
            dataMenu.getItems().add(menu);

            menu = new MenuItem(message("LoadContentInSystemClipboard"), StyleTools.getIconImageView("iconImageSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                controller.loadContentInSystemClipboard();
            });
            dataMenu.getItems().add(menu);

            dataMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("Export"), StyleTools.getIconImageView("iconExport.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DExportController.open(tableController);
            });
            menu.setDisable(empty);
            dataMenu.getItems().add(menu);

            menu = new MenuItem(message("ConvertToDatabaseTable"), StyleTools.getIconImageView("iconDatabase.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DConvertToDataBaseController.open(tableController);
            });
            menu.setDisable(invalidData);
            dataMenu.getItems().add(menu);

            menu = new MenuItem(message("Snapshot"), StyleTools.getIconImageView("iconSnapshot.png"));
            menu.setOnAction((ActionEvent event) -> {
                controller.getTableController().snapAction();
            });
            menu.setDisable(invalidData);
            dataMenu.getItems().add(menu);

            return dataMenu;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Menu modifyMenu(ControlData2D controller) {
        try {
            MenuItem menu;
            ControlData2DEditTable tableController = controller.getTableController();
            Data2D data2D = tableController.getData2D();
            boolean invalidData = data2D == null || !data2D.isValid();
            boolean isTmpData = data2D != null && data2D.isTmpData();
            boolean empty = invalidData || tableController.getTableData().isEmpty();

            Menu modifyMenu = new Menu(message("Modify"), StyleTools.getIconImageView("iconEdit.png"));

            menu = new MenuItem(message("AddRows"), StyleTools.getIconImageView("iconNewItem.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DAddRowsController.open(tableController);
            });
            menu.setDisable(empty);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("SetValues"), StyleTools.getIconImageView("iconEqual.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSetValuesController.open(tableController);
            });
            menu.setDisable(empty);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("Delete"), StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDeleteController.open(tableController);
            });
            menu.setDisable(empty);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("SetStyles"), StyleTools.getIconImageView("iconColor.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSetStylesController.open(tableController);
            });
            menu.setDisable(empty || isTmpData);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("PasteContentInSystemClipboard"), StyleTools.getIconImageView("iconPasteSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                tableController.pasteContentInSystemClipboard();
            });
            menu.setDisable(invalidData);
            modifyMenu.getItems().add(menu);

            menu = new MenuItem(message("PasteContentInMyBoxClipboard"), StyleTools.getIconImageView("iconPaste.png"));
            menu.setOnAction((ActionEvent event) -> {
                tableController.pasteContentInMyboxClipboard();
            });
            menu.setDisable(invalidData);
            modifyMenu.getItems().add(menu);

            return modifyMenu;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Menu trimMenu(ControlData2DLoad controller) {
        try {
            MenuItem menu;
            Data2D data2D = controller.getData2D();
            boolean invalidData = data2D == null || !data2D.isValid();
            boolean empty = invalidData || controller.getTableData().isEmpty();

            Menu trimMenu = new Menu(message("Trim"), StyleTools.getIconImageView("iconClean.png"));

            if (data2D != null && data2D.isTable()) {
                menu = new MenuItem(message("Query"), StyleTools.getIconImageView("iconQuery.png"));
                menu.setOnAction((ActionEvent event) -> {
                    DataTableQueryController.open(controller);
                });
                menu.setDisable(empty);
                trimMenu.getItems().add(menu);
            }

            menu = new MenuItem(message("CopyFilterQueryConvert"), StyleTools.getIconImageView("iconCopy.png"));
            menu.setOnAction((ActionEvent event) -> {
                controller.copyAction();
            });
            menu.setDisable(empty);
            trimMenu.getItems().add(menu);

            menu = new MenuItem(message("Sort"), StyleTools.getIconImageView("iconSort.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSortController.open(controller);
            });
            menu.setDisable(empty);
            trimMenu.getItems().add(menu);

            menu = new MenuItem(message("Transpose"), StyleTools.getIconImageView("iconRotateRight.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DTransposeController.open(controller);
            });
            menu.setDisable(empty);
            trimMenu.getItems().add(menu);

            menu = new MenuItem(message("Normalize"), StyleTools.getIconImageView("iconBinary.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DNormalizeController.open(controller);
            });
            menu.setDisable(empty);
            trimMenu.getItems().add(menu);

            menu = new MenuItem(message("SplitGroup"), StyleTools.getIconImageView("iconSplit.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DGroupController.open(controller);
            });
            menu.setDisable(empty);
            trimMenu.getItems().add(menu);

            return trimMenu;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Menu calMenu(ControlData2DLoad controller) {
        try {
            MenuItem menu;
            Data2D data2D = controller.getData2D();
            boolean invalidData = data2D == null || !data2D.isValid();
            boolean empty = invalidData || controller.getTableData().isEmpty();

            Menu calMenu = new Menu(message("Calculation"), StyleTools.getIconImageView("iconCalculator.png"));

            menu = new MenuItem(message("RowExpression"), StyleTools.getIconImageView("iconNewItem.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DRowExpressionController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("DescriptiveStatistics"), StyleTools.getIconImageView("iconStatistic.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DStatisticController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("GroupStatistic"), StyleTools.getIconImageView("iconAnalyse.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DGroupStatisticController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("SimpleLinearRegression"), StyleTools.getIconImageView("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSimpleLinearRegressionController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("SimpleLinearRegressionCombination"), StyleTools.getIconImageView("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSimpleLinearRegressionCombinationController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("MultipleLinearRegression"), StyleTools.getIconImageView("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DMultipleLinearRegressionController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("MultipleLinearRegressionCombination"), StyleTools.getIconImageView("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DMultipleLinearRegressionCombinationController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("FrequencyDistributions"), StyleTools.getIconImageView("iconDistribution.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DFrequencyController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            menu = new MenuItem(message("ValuePercentage"), StyleTools.getIconImageView("iconPercentage.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DPercentageController.open(controller);
            });
            menu.setDisable(empty);
            calMenu.getItems().add(menu);

            return calMenu;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Menu chartsMenu(ControlData2DLoad controller) {
        try {
            MenuItem menu;
            Data2D data2D = controller.getData2D();
            boolean invalidData = data2D == null || !data2D.isValid();
            boolean empty = invalidData || controller.getTableData().isEmpty();

            Menu chartMenu = new Menu(message("Charts"), StyleTools.getIconImageView("iconCharts.png"));

            menu = new MenuItem(message("XYChart"), StyleTools.getIconImageView("iconXYChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartXYController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("PieChart"), StyleTools.getIconImageView("iconPieChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartPieController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("BoxWhiskerChart"), StyleTools.getIconImageView("iconBoxWhiskerChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartBoxWhiskerController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("SelfComparisonBarsChart"), StyleTools.getIconImageView("iconBarChartH.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartSelfComparisonBarsController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("ComparisonBarsChart"), StyleTools.getIconImageView("iconComparisonBarsChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartComparisonBarsController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("XYZChart"), StyleTools.getIconImageView("iconXYZChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartXYZController.open(controller);
            });
            menu.setDisable(data2D == null || data2D.columnsNumber() < 3);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("LocationDistribution"), StyleTools.getIconImageView("iconLocation.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DLocationDistributionController.open(controller);
            });
            menu.setDisable(data2D == null || !data2D.includeCoordinate());
            chartMenu.getItems().add(menu);

            return chartMenu;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Menu groupChartsMenu(ControlData2DLoad controller) {
        try {
            MenuItem menu;
            Data2D data2D = controller.getData2D();
            boolean invalidData = data2D == null || !data2D.isValid();
            boolean empty = invalidData || controller.getTableData().isEmpty();

            Menu chartMenu = new Menu(message("GroupCharts"), StyleTools.getIconImageView("iconGraph.png"));

            menu = new MenuItem(message("GroupData") + " - " + message("XYChart"), StyleTools.getIconImageView("iconXYChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartGroupXYController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("GroupData") + " - " + message("PieChart"), StyleTools.getIconImageView("iconPieChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartGroupPieController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("GroupData") + " - " + message("BoxWhiskerChart"), StyleTools.getIconImageView("iconBoxWhiskerChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartGroupBoxWhiskerController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("GroupData") + " - " + message("SelfComparisonBarsChart"), StyleTools.getIconImageView("iconBarChartH.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartGroupSelfComparisonBarsController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

            menu = new MenuItem(message("GroupData") + " - " + message("ComparisonBarsChart"), StyleTools.getIconImageView("iconComparisonBarsChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartGroupComparisonBarsController.open(controller);
            });
            menu.setDisable(empty);
            chartMenu.getItems().add(menu);

//            menu = new MenuItem(message("LocationDistribution"), StyleTools.getIconImage("iconLocation.png"));
//            menu.setOnAction((ActionEvent event) -> {
//                Data2DLocationDistributionController.open(controller);
//            });
//            menu.setDisable(data2D == null || !data2D.includeCoordinate());
//            chartMenu.getItems().add(menu);
            return chartMenu;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
