package mara.mybox.data2d.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseData2DLoadController;
import mara.mybox.controller.Data2DAttributesController;
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
import mara.mybox.controller.Data2DManufactureController;
import mara.mybox.controller.Data2DMultipleLinearRegressionCombinationController;
import mara.mybox.controller.Data2DMultipleLinearRegressionController;
import mara.mybox.controller.Data2DNormalizeController;
import mara.mybox.controller.Data2DPercentageController;
import mara.mybox.controller.Data2DRowExpressionController;
import mara.mybox.controller.Data2DSaveAsController;
import mara.mybox.controller.Data2DSetStylesController;
import mara.mybox.controller.Data2DSetValuesController;
import mara.mybox.controller.Data2DSimpleLinearRegressionCombinationController;
import mara.mybox.controller.Data2DSimpleLinearRegressionController;
import mara.mybox.controller.Data2DSortController;
import mara.mybox.controller.Data2DStatisticController;
import mara.mybox.controller.Data2DTransposeController;
import mara.mybox.controller.DataFileCSVFormatController;
import mara.mybox.controller.DataFileExcelFormatController;
import mara.mybox.controller.DataFileExcelSheetsController;
import mara.mybox.controller.DataFileTextFormatController;
import mara.mybox.controller.DataTableQueryController;
import mara.mybox.controller.FileBrowseController;
import mara.mybox.data2d.Data2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-15
 * @License Apache License Version 2.0
 */
public class Data2DMenuTools {

    public static List<MenuItem> dataMenus(Data2DManufactureController dataController) {
        try {
            Data2D data2D = dataController.getData2D();
            if (data2D == null || !data2D.isValid()) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            boolean isEditing = dataController.isEditing();
            boolean isTmpData = data2D.isTmpData();
            boolean notLoaded = !dataController.isDataSizeLoaded();

            MenuItem menu;

            items.add(new SeparatorMenuItem());

            if (isEditing) {
                menu = new MenuItem(message("DefineData"), StyleTools.getIconImageView("iconMeta.png"));
                menu.setOnAction((ActionEvent event) -> {
                    Data2DAttributesController.open(dataController);
                });
                menu.setDisable(data2D.isPagesChanged());
                items.add(menu);

                menu = new MenuItem(message("Save") + "    Ctrl+S " + message("Or") + " Alt+S",
                        StyleTools.getIconImageView("iconSave.png"));
                menu.setOnAction((ActionEvent event) -> {
                    dataController.saveAction();
                });
                menu.setDisable(notLoaded);
                items.add(menu);

                items.add(new SeparatorMenuItem());

                menu = new MenuItem(message("Recover") + "    Ctrl+R " + message("Or") + " Alt+R",
                        StyleTools.getIconImageView("iconRecover.png"));
                menu.setOnAction((ActionEvent event) -> {
                    dataController.recoverAction();
                });
                menu.setDisable(isTmpData);
                items.add(menu);
            }

            menu = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.refreshAction();
            });
            menu.setDisable(isTmpData);
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("SaveAs") + "    Ctrl+B " + message("Or") + " Alt+B",
                    StyleTools.getIconImageView("iconSaveAs.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSaveAsController.open(dataController);
            });
            items.add(menu);

            menu = new MenuItem(message("Export"), StyleTools.getIconImageView("iconExport.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DExportController.open(dataController);
            });
            items.add(menu);

            menu = new MenuItem(message("ConvertToDatabaseTable"), StyleTools.getIconImageView("iconDatabase.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DConvertToDataBaseController.open(dataController);
            });
            items.add(menu);

            if (dataController.isTableMode()) {
                menu = new MenuItem(message("Snapshot"), StyleTools.getIconImageView("iconSnapshot.png"));
                menu.setOnAction((ActionEvent event) -> {
                    dataController.snapAction();
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            CheckMenuItem tmpItem = new CheckMenuItem(message("Data2DTmpDataUnderGeneratedPath"));
            tmpItem.setSelected(UserConfig.getBoolean("Data2DTmpDataUnderGeneratedPath", false));
            tmpItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("Data2DTmpDataUnderGeneratedPath", tmpItem.isSelected());
                }
            });
            items.add(tmpItem);

            Menu helpMenu = new Menu(message("Help"), StyleTools.getIconImageView("iconClaw.png"));
            helpMenu.getItems().addAll(helpMenus(dataController));
            items.add(helpMenu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> operationsMenus(Data2DManufactureController dataController) {
        try {
            if (!dataController.isEditing()) {
                return null;
            }
            Data2D data2D = dataController.getData2D();
            if (data2D == null || !data2D.isValid()) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            boolean isTmpData = data2D.isTmpData();
            boolean noneSelected = dataController.isNoneSelected();
            boolean isTableMode = dataController.isTableMode();
            MenuItem menu;

            menu = new MenuItem(message("AddRows") + "   CTRL+N / ALT+N",
                    StyleTools.getIconImageView("iconNewItem.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    dataController.addRowsAction();
                }
            });
            items.add(menu);

            if (isTableMode) {
                menu = new MenuItem(message("EditSelectedRow"), StyleTools.getIconImageView("iconEdit.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        dataController.editAction();
                    }
                });
                menu.setDisable(noneSelected);
                items.add(menu);

                menu = new MenuItem(message("DeleteSelectedRows") + "   DELETE / CTRL+D / ALT+D", StyleTools.getIconImageView("iconDelete.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        dataController.deleteRowsAction();
                    }
                });
                menu.setDisable(noneSelected);
                items.add(menu);

                menu = new MenuItem(message("MoveDown"), StyleTools.getIconImageView("iconDown.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        dataController.moveDownAction();
                    }
                });
                menu.setDisable(noneSelected);
                items.add(menu);

                menu = new MenuItem(message("MoveUp"), StyleTools.getIconImageView("iconUp.png"));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        dataController.moveUpAction();
                    }
                });
                menu.setDisable(noneSelected);
                items.add(menu);
            }

            menu = new MenuItem(message("Clear") + "   CTRL+L / ALT+L", StyleTools.getIconImageView("iconClear.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    dataController.clearAction();
                }
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("CopyToSystemClipboard"), StyleTools.getIconImageView("iconCopySystem.png"));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    dataController.copyToSystemClipboard();
                }
            });
            items.add(menu);

            menu = new MenuItem(message("PasteContentInSystemClipboard"), StyleTools.getIconImageView("iconPasteSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.pasteContentInSystemClipboard();
            });
            items.add(menu);

            menu = new MenuItem(message("PasteContentInMyBoxClipboard"), StyleTools.getIconImageView("iconPaste.png"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.pasteContentInMyboxClipboard();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("SetValues"), StyleTools.getIconImageView("iconEqual.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSetValuesController.open(dataController);
            });
            items.add(menu);

            menu = new MenuItem(message("DeleteWithConditions"), StyleTools.getIconImageView("iconDelete.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDeleteController.open(dataController);
            });
            items.add(menu);

            if (isTableMode) {
                menu = new MenuItem(message("SetStyles"), StyleTools.getIconImageView("iconColor.png"));
                menu.setOnAction((ActionEvent event) -> {
                    Data2DSetStylesController.open(dataController);
                });
                menu.setDisable(isTmpData);
                items.add(menu);

                items.add(new SeparatorMenuItem());

                CheckMenuItem focusMenu = new CheckMenuItem(message("CommitModificationWhenDataCellLoseFocus"),
                        StyleTools.getIconImageView("iconInput.png"));
                focusMenu.setSelected(AppVariables.commitModificationWhenDataCellLoseFocus);
                focusMenu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        AppVariables.lostFocusCommitData(focusMenu.isSelected());
                    }
                });
                items.add(focusMenu);

            }

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> fileMenus(Data2DManufactureController dataController) {
        try {
            Data2D data2D = dataController.getData2D();
            if (data2D == null || !data2D.isDataFile()) {
                return null;
            }
            File file = data2D.getFile();
            if (file == null) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;
            String baseName = dataController.getBaseName();

            if (data2D.isExcel()) {
                menu = new MenuItem(message("Sheet"), StyleTools.getIconImageView("iconFrame.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    DataFileExcelSheetsController.open(dataController);
                });
                items.add(menu);
            }

            menu = new MenuItem(message("Format"), StyleTools.getIconImageView("iconFormat.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                if (data2D.isCSV()) {
                    DataFileCSVFormatController.open(dataController);
                } else if (data2D.isTexts()) {
                    DataFileTextFormatController.open(dataController);
                } else if (data2D.isExcel()) {
                    DataFileExcelFormatController.open(dataController);
                }
            });
            items.add(menu);

            CheckMenuItem backItem = new CheckMenuItem(message("BackupWhenSave"));
            backItem.setSelected(UserConfig.getBoolean(baseName + "BackupWhenSave", true));
            backItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "BackupWhenSave", backItem.isSelected());
                }
            });
            items.add(backItem);

            menu = new MenuItem(message("FileBackups"), StyleTools.getIconImageView("iconBackup.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                dataController.openBackups();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("SaveAs") + "    Ctrl+B " + message("Or") + " Alt+B",
                    StyleTools.getIconImageView("iconSaveAs.png"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.saveAsAction();
            });
            items.add(menu);

            if (data2D.isTexts() || data2D.isCSV()) {
                menu = new MenuItem(message("Texts"), StyleTools.getIconImageView("iconTxt.png"));
                menu.setOnAction((ActionEvent event) -> {
                    dataController.editTextFile();
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("OpenDirectory"), StyleTools.getIconImageView("iconOpenPath.png"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.openSourcePath();
            });
            items.add(menu);

            menu = new MenuItem(message("BrowseFiles"), StyleTools.getIconImageView("iconList.png"));
            menu.setOnAction((ActionEvent event) -> {
                FileBrowseController.open(dataController);
            });
            items.add(menu);

            menu = new MenuItem(message("SystemMethod"), StyleTools.getIconImageView("iconSystemOpen.png"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.systemMethod();
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> trimMenus(BaseData2DLoadController controller) {
        try {
            Data2D data2D = controller.getData2D();
            if (data2D == null || !data2D.isValid()) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu;

            if (data2D.isTable()) {
                menu = new MenuItem(message("Query"), StyleTools.getIconImageView("iconQuery.png"));
                menu.setOnAction((ActionEvent event) -> {
                    DataTableQueryController.open(controller);
                });
                items.add(menu);
            }

            menu = new MenuItem(message("CopyFilterQueryConvert"), StyleTools.getIconImageView("iconCopy.png"));
            menu.setOnAction((ActionEvent event) -> {
                controller.copyAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Sort"), StyleTools.getIconImageView("iconSort.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSortController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("Transpose"), StyleTools.getIconImageView("iconRotateRight.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DTransposeController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("Normalize"), StyleTools.getIconImageView("iconBinary.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DNormalizeController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("SplitGroup"), StyleTools.getIconImageView("iconSplit.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DGroupController.open(controller);
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> calMenus(BaseData2DLoadController controller) {
        try {
            Data2D data2D = controller.getData2D();
            if (data2D == null || !data2D.isValid()) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("RowExpression"), StyleTools.getIconImageView("iconNewItem.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DRowExpressionController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("DescriptiveStatistics"), StyleTools.getIconImageView("iconStatistic.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DStatisticController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("GroupStatistic"), StyleTools.getIconImageView("iconAnalyse.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DGroupStatisticController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("SimpleLinearRegression"), StyleTools.getIconImageView("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSimpleLinearRegressionController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("SimpleLinearRegressionCombination"), StyleTools.getIconImageView("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DSimpleLinearRegressionCombinationController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("MultipleLinearRegression"), StyleTools.getIconImageView("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DMultipleLinearRegressionController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("MultipleLinearRegressionCombination"), StyleTools.getIconImageView("iconLinearPgression.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DMultipleLinearRegressionCombinationController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("FrequencyDistributions"), StyleTools.getIconImageView("iconDistribution.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DFrequencyController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("ValuePercentage"), StyleTools.getIconImageView("iconPercentage.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DPercentageController.open(controller);
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> verifyMenus(Data2DManufactureController dataController) {
        try {
            Data2D data2D = dataController.getData2D();
            if (data2D == null || !data2D.isValid()) {
                return null;
            }

            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("VerifyCurrentPage"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.verifyCurrentPage();
            });
            items.add(menu);

            menu = new MenuItem(message("VerifyAllData"));
            menu.setOnAction((ActionEvent event) -> {
                dataController.verifyAllData();
            });
            items.add(menu);

            if (dataController.getData2D().alwayValidate()) {
                return items;
            }

            CheckMenuItem validateEditItem = new CheckMenuItem(message("ValidateDataWhenEdit"));
            validateEditItem.setSelected(dataController.isValidateEdit());
            validateEditItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    dataController.setValidataEdit(validateEditItem.isSelected());
                }
            });
            items.add(validateEditItem);

            CheckMenuItem validateSaveItem = new CheckMenuItem(message("ValidateDataWhenSave"));
            validateSaveItem.setSelected(dataController.isValidateSave());
            validateSaveItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    dataController.setValidataSave(validateSaveItem.isSelected());
                }
            });
            items.add(validateSaveItem);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> chartMenus(BaseData2DLoadController controller) {
        try {
            Data2D data2D = controller.getData2D();
            if (data2D == null || !data2D.isValid()) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("XYChart"), StyleTools.getIconImageView("iconXYChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartXYController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("PieChart"), StyleTools.getIconImageView("iconPieChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartPieController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("BoxWhiskerChart"), StyleTools.getIconImageView("iconBoxWhiskerChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartBoxWhiskerController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("SelfComparisonBarsChart"), StyleTools.getIconImageView("iconBarChartH.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartSelfComparisonBarsController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("ComparisonBarsChart"), StyleTools.getIconImageView("iconComparisonBarsChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartComparisonBarsController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("XYZChart"), StyleTools.getIconImageView("iconXYZChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartXYZController.open(controller);
            });
            menu.setDisable(data2D.columnsNumber() < 3);
            items.add(menu);

            menu = new MenuItem(message("LocationDistribution"), StyleTools.getIconImageView("iconLocation.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DLocationDistributionController.open(controller);
            });
            menu.setDisable(!data2D.includeCoordinate());
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> groupChartMenus(BaseData2DLoadController controller) {
        try {
            Data2D data2D = controller.getData2D();
            if (data2D == null || !data2D.isValid()) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("GroupData") + " - " + message("XYChart"), StyleTools.getIconImageView("iconXYChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartGroupXYController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("GroupData") + " - " + message("PieChart"), StyleTools.getIconImageView("iconPieChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartGroupPieController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("GroupData") + " - " + message("BoxWhiskerChart"), StyleTools.getIconImageView("iconBoxWhiskerChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartGroupBoxWhiskerController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("GroupData") + " - " + message("SelfComparisonBarsChart"), StyleTools.getIconImageView("iconBarChartH.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartGroupSelfComparisonBarsController.open(controller);
            });
            items.add(menu);

            menu = new MenuItem(message("GroupData") + " - " + message("ComparisonBarsChart"), StyleTools.getIconImageView("iconComparisonBarsChart.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DChartGroupComparisonBarsController.open(controller);
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<MenuItem> helpMenus(BaseData2DLoadController controller) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem about2D = new MenuItem(message("AboutData2D"));
            about2D.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    controller.openHtml(HelpTools.aboutData2D());
                }
            });
            items.add(about2D);

            MenuItem aboutRowExpression = new MenuItem(message("AboutRowExpression"));
            aboutRowExpression.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    controller.openHtml(HelpTools.aboutRowExpression());
                }
            });
            items.add(aboutRowExpression);

            MenuItem aboutGrouping = new MenuItem(message("AboutGroupingRows"));
            aboutGrouping.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    controller.openHtml(HelpTools.aboutGroupingRows());
                }
            });
            items.add(aboutGrouping);

            MenuItem aboutDataAnalysis = new MenuItem(message("AboutDataAnalysis"));
            aboutDataAnalysis.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    controller.openHtml(HelpTools.aboutDataAnalysis());
                }
            });
            items.add(aboutDataAnalysis);

            items.add(new SeparatorMenuItem());

            MenuItem guidemenu = new MenuItem(message("UserGuideDataTools"));
            guidemenu.setStyle("-fx-text-fill: #2e598a;");
            guidemenu.setOnAction((ActionEvent event) -> {
                if (Languages.isChinese()) {
                    controller.browse("https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-zh.pdf");
                } else {
                    controller.browse("https://mara-mybox.sourceforge.io/guide/MyBox-DataTools-en.pdf");
                }
            });
            items.add(guidemenu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
