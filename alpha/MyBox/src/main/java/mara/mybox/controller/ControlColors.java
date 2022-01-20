package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorDataTools;
import mara.mybox.db.data.ColorPalette;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableColorPaletteName;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fximage.PaletteTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.cell.TableColorCell;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-1-7
 * @License Apache License Version 2.0
 */
public class ControlColors extends BaseSysTableController<ColorData> {

    protected ColorsManageController manageController;
    protected TableColorPaletteName tableColorPaletteName;
    protected TableColorPalette tableColorPalette;
    protected TableColor tableColor;
    protected ColorPaletteName currentPalette;
    protected double rectSize;
    protected Rectangle clickedRect, enteredRect;
    protected DropShadow shadowEffect;

    @FXML
    protected ControlColorPaletteSelector palettesController;
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
    protected TableColumn<ColorData, Float> orderColumn;
    @FXML
    protected Button deletePaletteButton, renamePaletteButton, addPaletteButton,
            copyPaletteButton, addColorsButton, trimButton;
    @FXML
    protected CheckBox mergeCheck, allColumnsCheck;
    @FXML
    protected Label paletteLabel, colorsPaneLabel;
    @FXML
    protected TabPane paletteTabPane;
    @FXML
    protected Tab dataTab, colorsTab;
    @FXML
    protected FlowPane buttonsPane, colorsPane;
    @FXML
    protected TextArea colorArea;
    @FXML
    protected VBox colorsBox;

    public ControlColors() {
        baseTitle = message("ManageColors");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableColorPaletteName = new TableColorPaletteName();
            tableColorPalette = new TableColorPalette();
            tableColor = new TableColor();
            tableColorPalette.setTableColor(tableColor);
            rectSize = AppVariables.iconSize * 0.8;
            shadowEffect = new DropShadow();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV);
    }

    @Override
    public void initControls() {
    }

    public void setParameters(ColorsManageController manageController) {
        try {
            this.manageController = manageController;
            this.parentController = manageController;
            this.baseName = manageController.baseName;
            thisPane.requestFocus();

            setControls();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setControls() {
        try {
            super.initControls();

            palettesController.setParent(manageController);
            palettesController.palettesList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event != null && event.getButton() == MouseButton.SECONDARY) {
                        popPaletteMenu(event);
                    }
                }
            });

            palettesController.palettesList.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends ColorPaletteName> ov, ColorPaletteName t, ColorPaletteName t1) -> {
                        if (isSettingValues) {
                            return;
                        }
                        currentPalette = palettesController.palettesList.getSelectionModel().getSelectedItem();
                        boolean isAll = isAllColors();
                        deletePaletteButton.setDisable(isAll);
                        renamePaletteButton.setDisable(isAll);
                        copyPaletteButton.setDisable(isAll);
                        trimButton.setDisable(isAll);
                        if (!isAll) {
                            UserConfig.setString(baseName + "Palette", currentPalette.getName());
                        }
                        refreshPalette();
                    });

            paletteTabPane.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) -> {
                        if (newTab == dataTab) {
                            deleteButton.setDisable(tableView.getSelectionModel().getSelectedItem() == null);
                        } else {
                            deleteButton.setDisable(clickedRect == null);
                        }
                    });

            refreshPalettes();
            hideRightPane();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            dataColumn.setPrefWidth(400);
            tableView.getColumns().remove(dataColumn);

            colorValueColumn.setCellValueFactory(new PropertyValueFactory<>("colorValue"));

            orderColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumner"));
            orderColumn.setCellFactory(TableAutoCommitCell.forFloatColumn());
            orderColumn.setOnEditCommit((TableColumn.CellEditEvent<ColorData, Float> t) -> {
                if (t == null || isAllColors()) {
                    return;
                }
                ColorData row = t.getRowValue();
                Float v = t.getNewValue();
                if (row == null || v == null || v == row.getOrderNumner()) {
                    return;
                }
                row.setOrderNumner(v);
                tableColorPalette.setOrder(currentPalette.getCpnid(), row, v);
                refreshPalette();
            });
            orderColumn.getStyleClass().add("editable-column");

            colorNameColumn.setCellValueFactory(new PropertyValueFactory<>("colorName"));
            colorNameColumn.setCellFactory(TableAutoCommitCell.forStringColumn());
            colorNameColumn.setOnEditCommit((TableColumn.CellEditEvent<ColorData, String> t) -> {
                if (t == null) {
                    return;
                }
                ColorData row = t.getRowValue();
                if (row == null) {
                    return;
                }
                String v = t.getNewValue();
                String o = row.getColorName();
                if (v == null && o == null
                        || v != null && v.equals(o)) {
                    return;
                }
                row.setColorName(v);
                if (currentPalette != null) {
                    tableColorPalette.setName(currentPalette.getCpnid(), row, v);
                } else {
                    tableColor.setName(row.getRgba(), v);
                }
                refreshPalette();
            });
            colorNameColumn.getStyleClass().add("editable-column");

            colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
            colorColumn.setCellFactory(new TableColorCell<>());

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
            super.initButtons();

            exportButton.disableProperty().bind(Bindings.isEmpty(tableData));

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
            tableView.getColumns().addAll(rowsSelectionColumn, colorColumn, colorNameColumn);
            if (!isAllColors()) {
                tableView.getColumns().add(orderColumn);
            }
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
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(deletePaletteButton, message("DeletePalette"));
            NodeStyleTools.setTooltip(renamePaletteButton, message("RenamePalette"));
            NodeStyleTools.setTooltip(addPaletteButton, message("AddPalette"));
            NodeStyleTools.setTooltip(copyPaletteButton, message("CopyPalette"));
            NodeStyleTools.setTooltip(addColorsButton, message("AddColors"));
            NodeStyleTools.setTooltip(trimButton, message("TrimOrderInPalette"));
            NodeStyleTools.setTooltip(tipsView, message("ColorsManageTips") + "\n\n" + message("TableTips"));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    /*
        palettes list
     */
    public void refreshPalettes() {
        palettesController.loadPalettes();
    }

    protected boolean isAllColors() {
        return currentPalette == null
                || currentPalette.getName().equals(palettesController.allColors.getName());
    }

    protected void popPaletteMenu(MouseEvent event) {
        if (isSettingValues) {
            return;
        }
        ColorPaletteName palette = palettesController.palettesList.getSelectionModel().getSelectedItem();
        boolean isALl = palette.getName().equals(palettesController.allColors.getName());
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu = new MenuItem(palette.getName());
        menu.setStyle("-fx-text-fill: #2e598a;");
        items.add(menu);
        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("AddPalette"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            palettesController.addPalette();
        });
        items.add(menu);

        menu = new MenuItem(message("DeletePalette"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            deletePalette();
        });
        menu.setDisable(isALl);
        items.add(menu);

        menu = new MenuItem(message("RenamePalette"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            renamePalette();
        });
        menu.setDisable(isALl);
        items.add(menu);

        menu = new MenuItem(message("CopyPalette"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            copyPalette();
        });
        menu.setDisable(isALl);
        items.add(menu);

        items.add(new SeparatorMenuItem());

        menu = new MenuItem(message("Export"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            exportCSV("all");
        });
        items.add(menu);

        menu = new MenuItem(message("Refresh"));
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            refreshPalettes();
        });
        items.add(menu);

        items.add(new SeparatorMenuItem());
        menu = new MenuItem(message("PopupClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction((ActionEvent menuItemEvent) -> {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = null;
        });
        items.add(menu);

        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(items);
        popMenu.show(tableView, event.getScreenX(), event.getScreenY());

    }

    @FXML
    protected void deletePalette() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            ColorPaletteName selected = palettesController.palettesList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                popError(message("NoData"));
                return;
            }
            if (!PopTools.askSure(this,baseTitle, selected.getName(), message("DeletePalette"))) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    return tableColorPaletteName.deleteData(selected) > 0;
                }

                @Override
                protected void whenSucceeded() {
                    palettesController.palettesList.getItems().remove(selected);
                    popSuccessful();

                }

            };
            start(task);
        }
    }

    @FXML
    protected void renamePalette() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            ColorPaletteName selected = palettesController.palettesList.getSelectionModel().getSelectedItem();
            if (selected == null || selected.getName().equals(message("AllColors"))) {
                popError(message("SelectColorPalette"));
                return;
            }
            String name = PopTools.askValue(baseTitle, message("RenamePalette") + "\n" + selected.getName(),
                    message("NewName"), selected.getName() + "m");
            if (name == null || name.isBlank()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (tableColorPaletteName.find(conn, name) != null) {
                            error = "AlreadyExisted";
                            return false;
                        }
                        selected.setName(name);
                        return tableColorPaletteName.updateData(conn, selected) != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (currentPalette != null && selected.getCpnid() == currentPalette.getCpnid()) {
                        palettesController.palettesList.refresh();
                        paletteLabel.setText(currentPalette.getName());
                    }
                    popSuccessful();
                }
            };
            start(task);
        }
    }

    @FXML
    protected void copyPalette() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            ColorPaletteName selected = palettesController.palettesList.getSelectionModel().getSelectedItem();
            if (selected == null || selected.getName().equals(message("AllColors"))) {
                popError(message("SelectColorPalette"));
                return;
            }
            String name = PopTools.askValue(baseTitle, message("CopyPalette") + "\n" + selected.getName(),
                    message("Name"), selected.getName() + " " + message("Copy"));
            if (name == null || name.isBlank()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                private ColorPaletteName newPalatte;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection();
                             PreparedStatement query = conn.prepareStatement(TableColorPalette.QueryPalette)) {
                        if (tableColorPaletteName.find(conn, name) != null) {
                            error = "AlreadyExisted";
                            return false;
                        }
                        newPalatte = new ColorPaletteName(name);
                        newPalatte = tableColorPaletteName.insertData(conn, newPalatte);
                        long paletteid = newPalatte.getCpnid();
                        query.setLong(1, selected.getCpnid());
                        conn.setAutoCommit(false);
                        try ( ResultSet results = query.executeQuery()) {
                            while (results.next()) {
                                ColorPalette data = tableColorPalette.readData(results);
                                data.setPaletteid(paletteid);
                                tableColorPalette.insertData(conn, data);
                            }
                        }
                        conn.commit();
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    palettesController.palettesList.getItems().add(newPalatte);
                    popSuccessful();
                }
            };
            start(task);
        }
    }

    @FXML
    protected void popExamplesMenu(MouseEvent mouseEvent) {
        PaletteTools.popPaletteExamplesMenu(this, mouseEvent, tableColorPaletteName, tableColorPalette, tableColor);
    }

    /*
       Palette
     */
    public void refreshPalette() {
        paletteLabel.setText(isAllColors() ? message("AllColors") : currentPalette.getName());

        checkColumns();
        loadTableData();
    }

    protected void loadPaletteLast(ColorPaletteName palette) {
        currentPage = Integer.MAX_VALUE;
        currentPalette = palettesController.allColors;
        if (palette != null) {
            for (ColorPaletteName p : palettesController.palettesList.getItems()) {
                if (p.getName().equals(palette.getName())) {
                    currentPalette = p;
                    break;
                }
            }
        }
        isSettingValues = true;
        palettesController.palettesList.getSelectionModel().clearSelection();
        isSettingValues = false;
        palettesController.palettesList.getSelectionModel().select(currentPalette);
        paletteTabPane.getSelectionModel().select(colorsTab);
    }

    public boolean isCurrent(String paletteName) {
        return currentPalette != null && currentPalette.getName().equals(paletteName);
    }

    @FXML
    @Override
    public void addAction() {
        ColorInputController.oneOpen(manageController);
    }

    @FXML
    protected void popExportMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("ExportAllData") + " - CSV");
            menu.setOnAction((ActionEvent event) -> {
                exportCSV("all");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ExportCurrentPage") + " - CSV");
            menu.setOnAction((ActionEvent event) -> {
                exportCSV("page");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ExportSelectedData") + " - CSV");
            menu.setOnAction((ActionEvent event) -> {
                exportCSV("selected");
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("ExportAllData") + " - Html");
            menu.setOnAction((ActionEvent event) -> {
                exportHtml("all");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ExportCurrentPage") + " - Html");
            menu.setOnAction((ActionEvent event) -> {
                exportHtml("page");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ExportSelectedData") + " - Html");
            menu.setOnAction((ActionEvent event) -> {
                exportHtml("selected");
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

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void exportCSV(String type) {
        final List<ColorData> rows;
        boolean isAll = isAllColors();
        String filename = isAll ? message("AllColors") : currentPalette.getName();
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
        final File file = chooseSaveFile(FileType.CSV, filename + ".csv");
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                @Override
                protected boolean handle() {
                    if ("all".equals(type)) {
                        if (isAll) {
                            ColorDataTools.exportCSV(tableColor, file);
                        } else {
                            ColorDataTools.exportCSV(tableColorPalette, file, currentPalette);
                        }
                    } else {
                        ColorDataTools.exportCSV(rows, file, !isAll);
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (file.exists()) {
                        recordFileWritten(file, FileType.Text);
                        DataFileCSVController.open(file, Charset.forName("UTF-8"), true, ',');
                    }
                }
            };
            start(task);
        }

    }

    public void exportHtml(String type) {
        try {
            List<ColorData> rows;
            boolean isAll = isAllColors();
            String title = isAll ? message("AllColors") : currentPalette.getName();
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
                    title += "_" + message("Page") + (currentPage + 1);
                    displayHtml(title, rows);
                } else {
                    String atitle = title + "_" + message("All");
                    synchronized (this) {
                        if (task != null && !task.isQuit()) {
                            return;
                        }
                        task = new SingletonTask<Void>(this) {

                            private List<ColorData> data;

                            @Override
                            protected boolean handle() {
                                if (isAll) {
                                    data = tableColor.readAll();
                                } else {
                                    data = tableColorPalette.colors(currentPalette.getCpnid());
                                }
                                return data != null;
                            }

                            @Override
                            protected void whenSucceeded() {
                                displayHtml(atitle, data);
                            }
                        };
                        start(task);
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
                        && !column.equals(orderColumn)) {
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
                            row.add(ColorData.htmlValue(data));
                        } else {
                            row.add(ColorData.htmlSimpleValue(data));
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

    @Override
    public void sourceFileChanged(File file) {
        PaletteTools.importPalette(this, tableColorPaletteName, tableColorPalette, tableColor,
                file, isAllColors() ? null : currentPalette.getName(), false);
    }

    @FXML
    @Override
    public void deleteAction() {
        if (paletteTabPane.getSelectionModel().getSelectedItem() == dataTab) {
            super.deleteAction();
            return;
        }
        if (clickedRect != null) {
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>(this) {

                    private int deletedCount = 0;

                    @Override
                    protected boolean handle() {
                        deletedCount = tableColorPalette.delete(currentPalette.getCpnid(), (ColorData) (clickedRect.getUserData()));
                        return deletedCount >= 0;
                    }

                    @Override
                    protected void whenSucceeded() {
                        popInformation(message("Deleted") + ":" + deletedCount);
                        if (deletedCount > 0) {
                            afterDeletion();
                        }
                    }
                };
                start(task);
            }
        }
    }

    @Override
    protected int clearData() {
        if (isAllColors()) {
            return tableColor.clearData();
        } else {
            return tableColorPalette.clear(currentPalette.getCpnid());
        }
    }

    @Override
    public void resetView() {
        super.resetView();
        colorsPane.getChildren().clear();
        colorArea.clear();
    }

    @FXML
    protected void trimAction() {
        if (isAllColors()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    return tableColorPalette.trim(currentPalette.getCpnid());
                }

                @Override
                protected void whenSucceeded() {
                    refreshPalette();
                }

            };
            start(task);
        }
    }

    @FXML
    public void queryAction() {
        openStage(Fxmls.ColorQueryFxml);
    }

    /*
       Data
     */
    @Override
    public long readDataSize() {
        if (isAllColors()) {
            return tableColor.size();
        } else {
            return tableColorPalette.size(currentPalette.getCpnid());
        }
    }

    @Override
    public List<ColorData> readPageData() {
        if (isAllColors()) {
            return tableColor.query(startRowOfCurrentPage, pageSize);
        } else {
            return tableColorPalette.colors(currentPalette.getCpnid(), startRowOfCurrentPage, pageSize);
        }
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        makeColorsPane();
    }

    @Override
    protected int deleteData(List<ColorData> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        if (isAllColors()) {
            return tableColor.deleteData(data);
        } else {
            return tableColorPalette.delete(currentPalette.getCpnid(), data);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        refreshPalette();
    }

    @Override
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        ColorData color = tableView.getSelectionModel().getSelectedItem();
        copyButton.setDisable(color == null);
        if (color != null) {
            showRightPane();
            colorArea.setText(color.display());
        }
        super.checkButtons();
    }

    @FXML
    @Override
    public void copyAction() {
        ColorCopyController controller = (ColorCopyController) openStage(Fxmls.ColorCopyFxml);
        controller.setValues(manageController);
        controller.toFront();
    }

    protected void addColor(Color color) {
        if (color == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
                task = null;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        ColorData colorData = tableColor.write(conn, new ColorData(color), false);
                        if (colorData == null) {
                            return false;
                        }
                        if (!isAllColors()) {
                            tableColorPalette.findAndCreate(conn, currentPalette.getCpnid(), colorData, false);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    loadPaletteLast(currentPalette);
                }

            };
            start(task);
        }
    }


    /*
        Colors pane
     */
    public void makeColorsPane() {
        synchronized (this) {
            colorsPane.getChildren().clear();
            colorArea.clear();
            colorsPaneLabel.setVisible(!isAllColors());
            if (tableData == null || tableData.isEmpty()) {
                return;
            }
            colorsPane.setVisible(false);
            SingletonTask colorsTask = new SingletonTask<Void>(this) {

                private List<Rectangle> rects;

                @Override
                protected boolean handle() {
                    List<ColorData> colors = new ArrayList<>();
                    colors.addAll(tableData);
                    Collections.sort(colors, new Comparator<ColorData>() {
                        @Override
                        public int compare(ColorData c1, ColorData c2) {
                            float diff = c1.getOrderNumner() - c2.getOrderNumner();
                            if (diff > 0) {
                                return 1;
                            } else if (diff < 0) {
                                return -1;
                            } else {
                                return 0;
                            }
                        }
                    });
                    rects = new ArrayList<>();
                    for (ColorData data : colors) {
                        Rectangle rect = makeColorRect(data);
                        if (rect != null) {
                            rects.add(rect);
                        }
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    colorsPane.getChildren().setAll(rects);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                colorsPane.applyCss();
                                colorsPane.layout();
                            });
                        }
                    }, 600);
                }

                @Override
                protected void finalAction() {
                    colorsPane.setVisible(true);
                }
            };
            start(colorsTask, false);
        }
    }

    protected Rectangle makeColorRect(ColorData data) {
        try {
            if (data == null) {
                return null;
            }
            Rectangle rect = new Rectangle(rectSize, rectSize);
            rect.setUserData(data);
            NodeStyleTools.setTooltip(rect, new Tooltip(data.display()));
            Color color = data.getColor();
            rect.setFill(color);
            rect.setStroke(Color.BLACK);
            rect.setOnMouseClicked((MouseEvent event) -> {
                Platform.runLater(() -> {
                    rectClicked(event);
                });
            });
            rect.setOnMouseEntered((MouseEvent event) -> {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        rectEntered(rect);
                    }
                });
            });

            if (!isAllColors()) {
                rect.setOnDragDetected(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        try {
                            Dragboard dragboard = rect.startDragAndDrop(TransferMode.ANY);
                            ClipboardContent content = new ClipboardContent();
                            content.putString(FxColorTools.color2rgba(color));
                            dragboard.setContent(content);
                            event.consume();
                        } catch (Exception e) {
                            MyBoxLog.debug(e.toString());
                        }
                    }
                });
                rect.setOnDragOver(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent event) {
                        try {
                            rectEntered(rect);
                            event.acceptTransferModes(TransferMode.ANY);
                            event.consume();
                        } catch (Exception e) {
                            MyBoxLog.debug(e.toString());
                        }
                    }
                });
                rect.setOnDragDropped(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent event) {
                        colorDropped(event, rect);
                    }
                });
            }
            return rect;
        } catch (Exception e) {
            return null;
        }
    }

    public void rectClicked(MouseEvent event) {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        try {
            Rectangle rect = (Rectangle) event.getSource();
            ColorData data = (ColorData) rect.getUserData();
            if (clickedRect != null) {
                clickedRect.setEffect(null);
                clickedRect.setWidth(rectSize);
                clickedRect.setHeight(rectSize);
                clickedRect.setStroke(Color.BLACK);
                clickedRect.setUserData(data);
            }
            rect.setEffect(shadowEffect);
            rect.setWidth(rectSize * 1.6);
            rect.setHeight(rectSize * 1.6);
            rect.setStroke(Color.RED);
            clickedRect = rect;
            colorArea.setText(data.display());
            deleteButton.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        isSettingValues = false;
        showRightPane();
    }

    public void rectEntered(Rectangle rect) {
        if (isSettingValues || rect.equals(enteredRect) || rect.equals(clickedRect)) {
            return;
        }
        isSettingValues = true;
        if (enteredRect != null && !enteredRect.equals(clickedRect)) {
            enteredRect.setEffect(null);
            enteredRect.setWidth(rectSize);
            enteredRect.setHeight(rectSize);
            enteredRect.setStroke(Color.BLACK);
        }
        rect.setEffect(shadowEffect);
        rect.setWidth(rectSize * 1.4);
        rect.setHeight(rectSize * 1.4);
        rect.setStroke(Color.BLUE);
        enteredRect = rect;
        isSettingValues = false;
    }

    public void colorDropped(DragEvent event, Rectangle targetRect) {
        if (event == null) {
            return;
        }
        if (targetRect == null || isAllColors()) {
            event.setDropCompleted(true);
            event.consume();
            return;
        }
        List<Node> nodes = colorsPane.getChildren();
        if (nodes.isEmpty()) {
            event.setDropCompleted(true);
            event.consume();
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            Color sourceColor = Color.web(event.getDragboard().getString());
            task = new SingletonTask<Void>(this) {

                private List<ColorData> colors = null;

                @Override
                protected boolean handle() {
                    try {
                        ColorData targetData = (ColorData) targetRect.getUserData();
                        int sourceValue = FxColorTools.color2Value(sourceColor);
                        int targetValue = targetData.getColorValue();
                        int sourceIndex = -1, targetIndex = -1;
                        colors = new ArrayList<>();
                        ColorData sourceColor = null;
                        for (int i = 0; i < nodes.size(); i++) {
                            Node node = nodes.get(i);
                            ColorData data = (ColorData) node.getUserData();
                            if (data.getColorValue() == sourceValue) {
                                sourceIndex = i;
                                sourceColor = data;
                            }
                            if (data.getColorValue() == targetValue) {
                                targetIndex = i;
                            }
                            colors.add(data);
                        }
                        if (sourceIndex < 0 || targetIndex < 0) {
                            return true;
                        }
                        float f0 = colors.get(0).getOrderNumner();
                        float fn = colors.get(colors.size() - 1).getOrderNumner();
                        if (f0 == fn) {
                            fn = f0 + 0.0001f;
                        }
                        float offset = (fn - f0) / (colors.size() - 1);
                        colors.remove(sourceIndex);
                        colors.add(sourceIndex < targetIndex ? targetIndex : targetIndex + 1, sourceColor);
                        for (int i = 0; i < colors.size(); i++) {
                            ColorData data = colors.get(i);
                            data.setOrderNumner(f0 + offset * i);
                        }
                        tableColorPalette.write(currentPalette.getCpnid(), colors, true);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    refreshPalette();
                }

                @Override
                protected void finalAction() {
                    event.setDropCompleted(true);
                    event.consume();
                    task = null;
                }

            };
            start(task);
        }
    }

}
