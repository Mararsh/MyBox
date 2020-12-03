package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import mara.mybox.data.ColorData;
import mara.mybox.data.StringTable;
import mara.mybox.data.VisitHistory.FileType;
import mara.mybox.data.tools.VisitHistoryTools;
import mara.mybox.db.TableColorData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.TableColorCell;
import mara.mybox.image.ImageColor;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import thridparty.TableAutoCommitCell;

/**
 * @Author Mara
 * @CreateDate 2020-1-7
 * @License Apache License Version 2.0
 */
public class ColorsManageController extends TableManageController<ColorData> {

    @FXML
    protected TableColumn<ColorData, Integer> colorValueColumn;
    @FXML
    protected TableColumn<ColorData, Color> colorColumn;
    @FXML
    protected TableColumn<ColorData, String> colorNameColumn, dataColumn, rgbaColumn, rgbColumn,
            sRGBColumn, HSBColumn, AdobeRGBColumn, AppleRGBColumn, ECIRGBColumn,
            sRGBLinearColumn, AdobeRGBLinearColumn, AppleRGBLinearColumn, CalculatedCMYKColumn,
            ECICMYKColumn, AdobeCMYKColumn, XYZColumn, CIELabColumn, LCHabColumn, CIELuvColumn, LCHuvColumn;
    @FXML
    protected TableColumn<ColorData, Boolean> inPaletteColumn;
    @FXML
    protected Button htmlButton, inButton, outButton;
    @FXML
    protected ColorPicker colorPicker;
    @FXML
    protected CheckBox mergeCheck, allColumnsCheck;
    @FXML
    protected ColorImportController colorImportController;

    public ColorsManageController() {
        baseTitle = AppVariables.message("ManageColors");
    }

    @Override
    protected void initColumns() {
        try {
            dataColumn.setPrefWidth(400);
            tableView.getColumns().remove(dataColumn);

            colorValueColumn.setCellValueFactory(new PropertyValueFactory<>("colorValue"));

            colorNameColumn.setCellValueFactory(new PropertyValueFactory<>("colorName"));
            colorNameColumn.setCellFactory(TableAutoCommitCell.forTableColumn());
            colorNameColumn.setOnEditCommit((TableColumn.CellEditEvent<ColorData, String> t) -> {
                if (t == null) {
                    return;
                }
                ColorData row = t.getRowValue();
                row.setColorName(t.getNewValue());
                TableColorData.setName(row.getRgba(), t.getNewValue());
            });
            colorNameColumn.getStyleClass().add("editable-column");

            colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
            colorColumn.setCellFactory(new TableColorCell<>());

            inPaletteColumn.setCellValueFactory(new PropertyValueFactory<>("inPalette"));
            inPaletteColumn.setCellFactory((TableColumn<ColorData, Boolean> p) -> {
                CheckBoxTableCell<ColorData, Boolean> cell = new CheckBoxTableCell<>();
                cell.setSelectedStateCallback(new Callback<Integer, ObservableValue<Boolean>>() {
                    @Override
                    public ObservableValue<Boolean> call(Integer index) {
                        return tableData.get(index).getInPaletteProperty();
                    }
                });
                return cell;
            });
            inPaletteColumn.getStyleClass().add("editable-column");

            rgbaColumn.setCellValueFactory(new PropertyValueFactory<>("rgba"));
            rgbColumn.setCellValueFactory(new PropertyValueFactory<>("rgb"));
            sRGBColumn.setCellValueFactory(new PropertyValueFactory<>("srgb"));
            HSBColumn.setCellValueFactory(new PropertyValueFactory<>("hsb"));
            AdobeRGBColumn.setCellValueFactory(new PropertyValueFactory<>("adobeRGB"));
            AppleRGBColumn.setCellValueFactory(new PropertyValueFactory<>("appleRGB"));
            ECIRGBColumn.setCellValueFactory(new PropertyValueFactory<>("eciRGB"));
            sRGBLinearColumn.setCellValueFactory(new PropertyValueFactory<>("SRGBLinear"));
            AdobeRGBLinearColumn.setCellValueFactory(new PropertyValueFactory<>("adobeRGBLinear"));
            AppleRGBLinearColumn.setCellValueFactory(new PropertyValueFactory<>("appleRGBLinear"));
            CalculatedCMYKColumn.setCellValueFactory(new PropertyValueFactory<>("calculatedCMYK"));
            ECICMYKColumn.setCellValueFactory(new PropertyValueFactory<>("eciCMYK"));
            AdobeCMYKColumn.setCellValueFactory(new PropertyValueFactory<>("adobeCMYK"));
            XYZColumn.setCellValueFactory(new PropertyValueFactory<>("xyz"));
            CIELabColumn.setCellValueFactory(new PropertyValueFactory<>("cieLab"));
            LCHabColumn.setCellValueFactory(new PropertyValueFactory<>("lchab"));
            CIELuvColumn.setCellValueFactory(new PropertyValueFactory<>("cieLuv"));
            LCHuvColumn.setCellValueFactory(new PropertyValueFactory<>("lchuv"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initButtons() {
        try {
            colorImportController.setParentController(this);

            colorPicker.valueProperty().addListener(
                    (ObservableValue<? extends Color> ov, Color oldVal, Color newVal) -> {
                        if (isSettingValues || newVal == null) {
                            return;
                        }
                        ColorData data = TableColorData.write(newVal, false);
                        if (data != null) {
                            tableData.add(0, data);
                        } else {
                            scrollTo(newVal);
                        }
                    });

            htmlButton.disableProperty().bind(Bindings.isEmpty(tableData));

            allColumnsCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                checkColumns();
                loadTableData();
            });

            mergeCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                checkColumns();
                loadTableData();
            });

            checkColumns();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkColumns() {
        try {
            isSettingValues = true;
            tableView.getColumns().clear();
            tableView.getColumns().addAll(colorColumn, inPaletteColumn, colorNameColumn);
            if (mergeCheck.isSelected()) {
                if (allColumnsCheck.isSelected()) {
                    dataColumn.setCellValueFactory(new PropertyValueFactory<>("colorDisplay"));
                } else {
                    dataColumn.setCellValueFactory(new PropertyValueFactory<>("colorSimpleDisplay"));
                }
                tableView.getColumns().addAll(colorValueColumn, rgbaColumn, rgbColumn, dataColumn);

            } else if (allColumnsCheck.isSelected()) {
                tableView.getColumns().addAll(colorValueColumn, rgbaColumn, rgbColumn, sRGBColumn, HSBColumn,
                        AdobeRGBColumn, AppleRGBColumn, ECIRGBColumn,
                        sRGBLinearColumn, AdobeRGBLinearColumn, AppleRGBLinearColumn, CalculatedCMYKColumn,
                        ECICMYKColumn, AdobeCMYKColumn, XYZColumn, CIELabColumn, LCHabColumn, CIELuvColumn, LCHuvColumn);
            } else {
                tableView.getColumns().addAll(colorValueColumn, rgbaColumn, rgbColumn, sRGBColumn, HSBColumn);
            }
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        FxmlControl.setTooltip(inButton, message("PutInColorPalette"));
        FxmlControl.setTooltip(outButton, message("RemoveFromColorPalette"));

        loadTableData();
    }

    public void scrollTo(Color color) {
        int value = ImageColor.getRGB(color);
        for (ColorData data : tableData) {
            if (data.getColorValue() == value) {
                tableView.scrollTo(data);
                tableView.getSelectionModel().select(data);
                return;
            }
        }
    }

    @Override
    public int readDataSize() {
        return TableColorData.size();
    }

    @Override
    public List<ColorData> readPageData() {
        return TableColorData.readPage(currentPageStart, currentPageSize);
    }

    @Override
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        super.checkSelected();
        int selection = tableView.getSelectionModel().getSelectedIndices().size();
        inButton.setDisable(selection == 0);
        outButton.setDisable(selection == 0);
    }

    @FXML
    public void inPaletteAction() {
        List<ColorData> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    TableColorData.addDataInPalette(selected, false);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    loadTableData();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void outPaletteAction() {
        List<ColorData> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    TableColorData.removeFromPalette(selected);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    loadTableData();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    protected int deleteData(List<ColorData> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        return TableColorData.deleteData(data);
    }

    @Override
    protected boolean clearData() {
        return new TableColorData().clear();
    }

    @FXML
    @Override
    public void refreshAction() {
        loadTableData();
    }

    @Override
    public void itemDoubleClicked() {
        viewAction();
    }

    @FXML
    @Override
    public void viewAction() {
        ColorData selected = (ColorData) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        HtmlTools.viewHtml(message("Color"), selected.display().replaceAll("\n", "</BR>"));
    }

    @FXML
    protected void popCsvMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("ExportSelectedData"));
            menu.setOnAction((ActionEvent event) -> {
                exportCSV("selected");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ExportAllData"));
            menu.setOnAction((ActionEvent event) -> {
                exportCSV("all");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ExportCurrentPage"));
            menu.setOnAction((ActionEvent event) -> {
                exportCSV("page");
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void exportCSV(String type) {
        final List<ColorData> rows;
        String filename = message("Color");
        if ("selected".equals(type)) {
            rows = tableView.getSelectionModel().getSelectedItems();
            if (rows == null || rows.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            filename += "_" + message("Selected");
        } else {
            rows = tableData;
            if (rows == null || rows.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            if ("page".equals(type)) {
                filename += "_" + message("Page") + currentPage;
            } else {
                filename += "_" + message("All");
            }
        }
        List<FileChooser.ExtensionFilter> csvExtensionFilter = new ArrayList<>();
        csvExtensionFilter.add(new FileChooser.ExtensionFilter("csv", "*.csv"));
        final File file = chooseSaveFile(VisitHistoryTools.getSavedPath(FileType.Text),
                filename + ".csv", csvExtensionFilter);
        if (file == null) {
            return;
        }
        recordFileWritten(file, FileType.Text);
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    if ("all".equals(type)) {
                        ColorData.exportCSV(file);
                    } else {
                        ColorData.exportCSV(rows, file);
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (file.exists()) {
                        FxmlStage.openTextEditer(null, file);
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @FXML
    protected void popHtmlMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("ExportSelectedData"));
            menu.setOnAction((ActionEvent event) -> {
                exportHtml("selected");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ExportAllData"));
            menu.setOnAction((ActionEvent event) -> {
                exportHtml("all");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ExportCurrentPage"));
            menu.setOnAction((ActionEvent event) -> {
                exportHtml("page");
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void exportHtml(String type) {
        try {
            List<ColorData> rows;
            String title = message("Color");
            if ("selected".equals(type)) {
                rows = tableView.getSelectionModel().getSelectedItems();
                if (rows == null || rows.isEmpty()) {
                    popError(message("NoData"));
                    return;
                }
                title += "_" + message("Selected");
                displayHtml(title, rows);
            } else {
                rows = tableData;
                if (rows == null || rows.isEmpty()) {
                    popError(message("NoData"));
                    return;
                }
                if ("page".equals(type)) {
                    title += "_" + message("Page") + currentPage;
                    displayHtml(title, rows);
                } else {
                    synchronized (this) {
                        if (task != null && !task.isQuit()) {
                            return;
                        }
                        task = new SingletonTask<Void>() {

                            private List<ColorData> data;

                            @Override
                            protected boolean handle() {
                                data = TableColorData.readAll();
                                return true;
                            }

                            @Override
                            protected void whenSucceeded() {
                                displayHtml(message("Color") + "_" + message("All"), data);
                            }
                        };
                        openHandlingStage(task, Modality.WINDOW_MODAL);
                        task.setSelf(task);
                        Thread thread = new Thread(task);
                        thread.setDaemon(true);
                        thread.start();
                    }
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void displayHtml(String title, List<ColorData> rows) {
        try {
            if (rows == null || rows.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            List<String> names = new ArrayList<>();
            for (TableColumn column : tableView.getColumns()) {
                if (!column.equals(rgbaColumn) && !column.equals(rgbColumn)
                        && !column.equals(inPaletteColumn)) {
                    names.add(column.getText());
                }
            }
            StringTable table = new StringTable(names, title, 0);
            for (ColorData data : rows) {
                List<String> row = new ArrayList<>();
                for (TableColumn column : tableView.getColumns()) {
                    if (column.equals(colorValueColumn)) {
                        row.add(data.getColorValue() + "");
                    } else if (column.equals(colorColumn)) {
                        row.add(data.getRgba());
//                    } else if (column.equals(inPaletteColumn)) {
//                        row.add(message(data.getInPalette() + ""));
                    } else if (column.equals(colorNameColumn)) {
                        row.add(data.getColorName());
                    } else if (column.equals(sRGBColumn)) {
                        row.add(data.getSrgb());
                    } else if (column.equals(HSBColumn)) {
                        row.add(data.getHsb());
                    } else if (column.equals(AdobeRGBColumn)) {
                        row.add(data.getAdobeRGB());
                    } else if (column.equals(AppleRGBColumn)) {
                        row.add(data.getAppleRGB());
                    } else if (column.equals(ECIRGBColumn)) {
                        row.add(data.getEciRGB());
                    } else if (column.equals(sRGBLinearColumn)) {
                        row.add(data.getSRGBLinear());
                    } else if (column.equals(AdobeRGBLinearColumn)) {
                        row.add(data.getAdobeRGBLinear());
                    } else if (column.equals(AppleRGBLinearColumn)) {
                        row.add(data.getAppleRGBLinear());
                    } else if (column.equals(CalculatedCMYKColumn)) {
                        row.add(data.getCalculatedCMYK());
                    } else if (column.equals(ECICMYKColumn)) {
                        row.add(data.getEciCMYK());
                    } else if (column.equals(AdobeCMYKColumn)) {
                        row.add(data.getAdobeCMYK());
                    } else if (column.equals(XYZColumn)) {
                        row.add(data.getXyz());
                    } else if (column.equals(CIELabColumn)) {
                        row.add(data.getCieLab());
                    } else if (column.equals(LCHabColumn)) {
                        row.add(data.getLchab());
                    } else if (column.equals(CIELuvColumn)) {
                        row.add(data.getCieLuv());
                    } else if (column.equals(LCHuvColumn)) {
                        row.add(data.getLchuv());
                    } else if (column.equals(dataColumn)) {
                        if (allColumnsCheck.isSelected()) {
                            row.add(data.htmlDisplay());
                        } else {
                            row.add(data.htmlSimpleDisplay());
                        }
                    }
                }
                table.add(row);
            }
            table.editHtml();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void managePalette() {
        ColorPaletteManageController.oneOpen();
    }

    @FXML
    @Override
    public void closePopup(KeyEvent event) {
        super.closePopup(event);
        colorImportController.closePopup(event);
    }

    public static ColorsManageController oneOpen() {
        ColorsManageController controller = null;
        Stage stage = FxmlStage.findStage(message("ManageColors"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (ColorsManageController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (ColorsManageController) FxmlStage.openStage(CommonValues.ManageColorsFxml);
        }
        if (controller != null) {
            controller.getMyStage().toFront();
        }
        return controller;
    }

}
