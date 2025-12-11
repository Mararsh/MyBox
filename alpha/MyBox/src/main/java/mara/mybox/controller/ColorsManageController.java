package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorDataTools;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableColorPaletteName;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.MenuTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableAutoCommitCell;
import mara.mybox.fxml.cell.TableColorCell;
import static mara.mybox.fxml.image.FxColorTools.color2css;
import mara.mybox.fxml.image.PaletteTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-1-7
 * @License Apache License Version 2.0
 */
public class ColorsManageController extends BaseSysTableController<ColorData> {

    protected TableColorPaletteName tableColorPaletteName;
    protected TableColorPalette tableColorPalette;
    protected TableColor tableColor;

    @FXML
    protected ControlColorPaletteSelector palettesController;
    @FXML
    protected TableColumn<ColorData, Integer> colorValueColumn;
    @FXML
    protected TableColumn<ColorData, Color> colorColumn, invertColumn, complementaryColumn;
    @FXML
    protected TableColumn<ColorData, String> colorNameColumn, dataColumn,
            rgbaColumn, rgbColumn, hueColumn, rybColumn, saturationColumn, brightnessColumn, opacityColumn,
            sRGBColumn, HSBColumn, AdobeRGBColumn, AppleRGBColumn, ECIRGBColumn,
            sRGBLinearColumn, AdobeRGBLinearColumn, AppleRGBLinearColumn, CalculatedCMYKColumn,
            ECICMYKColumn, AdobeCMYKColumn, descColumn,
            XYZColumn, CIELabColumn, LCHabColumn, CIELuvColumn, LCHuvColumn,
            invertRGBColumn, complementaryRGBColumn;
    @FXML
    protected TableColumn<ColorData, Float> orderColumn;
    @FXML
    protected Button addColorsButton, customizeButton, trimButton;
    @FXML
    protected ToggleGroup showGroup;
    @FXML
    protected RadioButton colorsRadio, valuesRadio, allRadio, simpleMergedRadio, allMergedRadio;
    @FXML
    protected Label paletteLabel;
    @FXML
    protected TabPane paletteTabPane;
    @FXML
    protected Tab dataTab, colorsTab;
    @FXML
    protected ControlColorsPane colorsController;
    @FXML
    protected FlowPane buttonsPane;
    @FXML
    protected HtmlTableController infoController;
    @FXML
    protected VBox colorsBox;

    public ColorsManageController() {
        baseTitle = message("ManageColors");
        TipsLabelKey = message("ColorsManageTips");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableColorPaletteName = new TableColorPaletteName();
            tableColorPalette = new TableColorPalette();
            tableColor = new TableColor();
            tableColorPalette.setTableColor(tableColor);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            palettesController.setParameter(this, true);
            colorsController.setManager(this);

            palettesController.renamedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    setTitle(baseTitle + " - " + palettesController.currentPaletteName());
                    paletteLabel.setText(palettesController.currentPaletteName());
                }
            });

            palettesController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    refreshPalette();
                }
            });

            refreshPalettes();

            infoController.initStyle(HtmlStyles.TableStyle);

            colorsController.clickNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    displayColorInfo(colorsController.clickedColor());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();
            dataColumn.setPrefWidth(400);
            tableView.getColumns().remove(dataColumn);

            colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
            colorColumn.setCellFactory(new TableColorCell<>());

            invertColumn.setCellValueFactory(new PropertyValueFactory<>("invertColor"));
            invertColumn.setCellFactory(new TableColorCell<>());
            invertRGBColumn.setCellValueFactory(new PropertyValueFactory<>("invertRGB"));

            complementaryColumn.setCellValueFactory(new PropertyValueFactory<>("complementaryColor"));
            complementaryColumn.setCellFactory(new TableColorCell<>());
            complementaryRGBColumn.setCellValueFactory(new PropertyValueFactory<>("complementaryRGB"));

            colorValueColumn.setCellValueFactory(new PropertyValueFactory<>("colorValue"));

            orderColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumner"));
            orderColumn.setCellFactory(TableAutoCommitCell.forFloatColumn());
            orderColumn.setOnEditCommit((TableColumn.CellEditEvent<ColorData, Float> t) -> {
                if (t == null || palettesController.isAllColors()) {
                    return;
                }
                ColorData row = t.getRowValue();
                Float v = t.getNewValue();
                if (row == null || v == null || v == row.getOrderNumner()) {
                    return;
                }
                row.setOrderNumner(v);
                tableColorPalette.setOrder(palettesController.currentPaletteId(), row, v);
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
                if (palettesController.currentPalette != null) {
                    tableColorPalette.setName(palettesController.currentPaletteId(), row, v);
                } else {
                    tableColor.setName(row.getRgba(), v);
                }
                refreshPalette();
            });
            colorNameColumn.getStyleClass().add("editable-column");

            rgbaColumn.setCellValueFactory(new PropertyValueFactory<>("rgba"));
            rgbColumn.setCellValueFactory(new PropertyValueFactory<>("rgb"));

            rybColumn.setCellValueFactory(new PropertyValueFactory<>("rybAngle"));
            hueColumn.setCellValueFactory(new PropertyValueFactory<>("hue"));
            saturationColumn.setCellValueFactory(new PropertyValueFactory<>("saturation"));
            brightnessColumn.setCellValueFactory(new PropertyValueFactory<>("brightness"));
            opacityColumn.setCellValueFactory(new PropertyValueFactory<>("opacity"));

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
            descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void initButtons() {
        try {
            super.initButtons();

            exportButton.disableProperty().bind(Bindings.isEmpty(tableData));

            showGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    checkColumns();
                    loadTableData();
                }
            });

            checkColumns();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void checkColumns() {
        try {
            isSettingValues = true;
            tableView.getColumns().clear();
            tableView.getColumns().addAll(rowsSelectionColumn, colorColumn, colorNameColumn);
            if (!palettesController.isAllColors()) {
                tableView.getColumns().add(orderColumn);
            }

            if (simpleMergedRadio.isSelected()) {
                dataColumn.setCellValueFactory(new PropertyValueFactory<>("colorSimpleDisplay"));
                tableView.getColumns().addAll(rgbaColumn, rgbColumn, dataColumn,
                        invertColumn, invertRGBColumn, complementaryColumn, complementaryRGBColumn);

            } else if (allMergedRadio.isSelected()) {
                dataColumn.setCellValueFactory(new PropertyValueFactory<>("colorDisplay"));
                tableView.getColumns().addAll(rgbaColumn, rgbColumn, dataColumn,
                        invertColumn, invertRGBColumn, complementaryColumn, complementaryRGBColumn);

            } else if (valuesRadio.isSelected()) {
                tableView.getColumns().addAll(rgbaColumn, rgbColumn,
                        rybColumn, hueColumn, saturationColumn, brightnessColumn, opacityColumn,
                        HSBColumn, sRGBColumn, CalculatedCMYKColumn,
                        invertColumn, invertRGBColumn, complementaryColumn, complementaryRGBColumn,
                        colorValueColumn);

            } else if (allRadio.isSelected()) {
                tableView.getColumns().addAll(rgbaColumn, rgbColumn,
                        rybColumn, hueColumn, saturationColumn, brightnessColumn, opacityColumn,
                        HSBColumn, sRGBColumn, CalculatedCMYKColumn,
                        invertColumn, invertRGBColumn, complementaryColumn, complementaryRGBColumn,
                        AdobeRGBColumn, AppleRGBColumn, ECIRGBColumn, sRGBLinearColumn, AdobeRGBLinearColumn,
                        AppleRGBLinearColumn, ECICMYKColumn, AdobeCMYKColumn, XYZColumn, CIELabColumn,
                        LCHabColumn, CIELuvColumn, LCHuvColumn,
                        colorValueColumn);

            } else {
                tableView.getColumns().addAll(rybColumn, hueColumn, saturationColumn, brightnessColumn, opacityColumn,
                        invertColumn, invertRGBColumn, complementaryColumn, complementaryRGBColumn,
                        HSBColumn, rgbaColumn, rgbColumn);
            }

            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(addColorsButton, message("AddColors"));
            NodeStyleTools.setTooltip(trimButton, message("TrimOrderInPalette"));
            NodeStyleTools.setTooltip(customizeButton, message("CustomizeColors"));

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        palettes list
     */
    public void refreshPalettes() {
        palettesController.loadPalettes();
    }

    /*
       Palette
     */
    public void refreshPalette() {
        trimButton.setDisable(palettesController.isAllColors());
        setTitle(baseTitle + " - " + palettesController.currentPaletteName());
        paletteLabel.setText(palettesController.currentPaletteName());

        checkColumns();
        loadTableData();
    }

    public void loadPaletteLast(ColorPaletteName palette) {
        loadPalette(palette != null ? palette.getName() : null);
    }

    public void loadPalette(String paletteName) {
        pagination.currentPage = Integer.MAX_VALUE;
        palettesController.loadPalette(paletteName);
        paletteTabPane.getSelectionModel().select(colorsTab);
    }

    @FXML
    @Override
    public void addAction() {
        ColorsInputController.oneOpen(this);
    }

    @FXML
    protected void showExportMenu(Event event) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("ExportAllData") + " - CSV");
            menu.setOnAction((ActionEvent e) -> {
                exportCSV("all");
            });
            items.add(menu);

            menu = new MenuItem(message("ExportCurrentPage") + " - CSV");
            menu.setOnAction((ActionEvent e) -> {
                exportCSV("page");
            });
            items.add(menu);

            menu = new MenuItem(message("ExportSelectedData") + " - CSV");
            menu.setOnAction((ActionEvent e) -> {
                exportCSV("selected");
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("ExportAllData") + " - Html");
            menu.setOnAction((ActionEvent e) -> {
                exportHtml("all");
            });
            items.add(menu);

            menu = new MenuItem(message("ExportCurrentPage") + " - Html");
            menu.setOnAction((ActionEvent e) -> {
                exportHtml("page");
            });
            items.add(menu);

            menu = new MenuItem(message("ExportSelectedData") + " - Html");
            menu.setOnAction((ActionEvent e) -> {
                exportHtml("selected");
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            items.add(MenuTools.popCheckMenu("ColorExport"));

            popEventMenu(event, items);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void popExportMenu(Event event) {
        if (MenuTools.isPopMenu("ColorExport", false)) {
            showExportMenu(event);
        }
    }

    public void exportCSV(String type) {
        if (task != null && !task.isQuit()) {
            return;
        }
        final List<ColorData> rows;
        boolean isAll = palettesController.isAllColors();
        String filename = palettesController.currentPaletteName();
        if ("selected".equals(type)) {
            rows = selectedItems();
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
                filename += "_" + message("Page") + (pagination.currentPage + 1);
            } else {
                filename += "_" + message("All");
            }
        }
        final File file = saveCurrentFile(FileType.CSV, filename + ".csv");
        if (file == null) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                if ("all".equals(type)) {
                    if (isAll) {
                        ColorDataTools.exportCSV(tableColor, file);
                    } else {
                        ColorDataTools.exportCSV(tableColorPalette, file, palettesController.currentPalette());
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
                    Data2DManufactureController.openCSVFile(file);
                }
            }
        };
        start(task);
    }

    public void exportHtml(String type) {
        try {
            List<ColorData> rows;
            boolean isAll = palettesController.isAllColors();
            String title = palettesController.currentPaletteName();
            if ("selected".equals(type)) {
                rows = selectedItems();
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
                    title += "_" + message("Page") + (pagination.currentPage + 1);
                    displayHtml(title, rows);
                } else {
                    String atitle = title;
                    if (task != null && !task.isQuit()) {
                        return;
                    }
                    task = new FxSingletonTask<Void>(this) {

                        private List<ColorData> data;

                        @Override
                        protected boolean handle() {
                            if (isAll) {
                                data = tableColor.readAll();
                            } else {
                                data = tableColorPalette.colors(palettesController.currentPaletteId());
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

        } catch (Exception e) {
            MyBoxLog.error(e);
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
                if (!column.equals(rowsSelectionColumn) && !column.equals(orderColumn)) {
                    names.add(column.getText());
                }
            }
            StringTable table = new StringTable(names, title);
            for (ColorData data : rows) {
                if (data.needConvert()) {
                    data.convert();
                }
                List<String> row = new ArrayList<>();
                for (TableColumn column : tableView.getColumns()) {
                    if (column.equals(colorValueColumn)) {
                        row.add(data.getColorValue() + "");
                    } else if (column.equals(colorColumn)) {
                        row.add("<DIV style=\"width: 50px;  "
                                + "background-color:" + color2css(data.getColor()) + "; \">"
                                + "&nbsp;&nbsp;&nbsp;</DIV>");
                    } else if (column.equals(colorNameColumn)) {
                        row.add(data.getColorName());
                    } else if (column.equals(rgbaColumn)) {
                        row.add(data.getRgba());
                    } else if (column.equals(rgbColumn)) {
                        row.add(data.getRgb());
                    } else if (column.equals(sRGBColumn)) {
                        row.add(data.getSrgb());
                    } else if (column.equals(HSBColumn)) {
                        row.add(data.getHsb());
                    } else if (column.equals(hueColumn)) {
                        row.add(data.getHue());
                    } else if (column.equals(saturationColumn)) {
                        row.add(data.getSaturation());
                    } else if (column.equals(brightnessColumn)) {
                        row.add(data.getBrightness());
                    } else if (column.equals(rybColumn)) {
                        row.add(data.getRybAngle());
                    } else if (column.equals(opacityColumn)) {
                        row.add(data.getOpacity());
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
                        if (allMergedRadio.isSelected()) {
                            row.add(ColorData.htmlValue(data));
                        } else {
                            row.add(ColorData.htmlSimpleValue(data));
                        }
                    } else if (column.equals(invertColumn)) {
                        row.add("<DIV style=\"width: 50px;  "
                                + "background-color:" + color2css(data.getInvertColor()) + "; \">"
                                + "&nbsp;&nbsp;&nbsp;</DIV>");
                    } else if (column.equals(invertRGBColumn)) {
                        row.add(data.getInvertRGB());
                    } else if (column.equals(complementaryColumn)) {
                        row.add("<DIV style=\"width: 50px; "
                                + " background-color:" + color2css(data.getComplementaryColor()) + "; \">"
                                + "&nbsp;&nbsp;&nbsp;</DIV>");
                    } else if (column.equals(complementaryRGBColumn)) {
                        row.add(data.getComplementaryRGB());
                    }
                }
                table.add(row);
            }
            String html = HtmlWriteTools.html(title, HtmlStyles.styleValue("Table"), table.body());
            WebBrowserController.openHtml(html, HtmlStyles.styleValue("Table"), true);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        PaletteTools.importFile(this, file, palettesController.currentPaletteName(), false);
    }

    @FXML
    @Override
    public void deleteAction() {
        if (paletteTabPane.getSelectionModel().getSelectedItem() == dataTab) {
            super.deleteAction();
            return;
        }
        if (colorsController.clickedRect == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            private int deletedCount = 0;

            @Override
            protected boolean handle() {
                deletedCount = tableColorPalette.delete(colorsController.clickedColor());
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

    @Override
    protected long clearData(FxTask currentTask) {
        if (palettesController.isAllColors()) {
            return tableColor.clearData();
        } else {
            return tableColorPalette.clear(palettesController.currentPaletteId());
        }
    }

    @Override
    public void resetView(boolean changed) {
        super.resetView(changed);
        colorsController.colorsPane.getChildren().clear();
        infoController.clear();
    }

    @FXML
    protected void trimAction() {
        if (palettesController.isAllColors()) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return tableColorPalette.trim(palettesController.currentPaletteId());
            }

            @Override
            protected void whenSucceeded() {
                refreshPalette();
            }

        };
        start(task);
    }

    @FXML
    public void queryAction() {
        openStage(Fxmls.ColorQueryFxml);
    }

    @FXML
    public void customizePalette() {
        ColorsCustomizeController.open(this);
    }

    /*
       Data
     */
    @Override
    public long readDataSize(FxTask currentTask, Connection conn) {
        long size;
        if (palettesController.isAllColors()) {
            size = tableColor.size(conn);
        } else {
            size = tableColorPalette.size(conn, palettesController.currentPaletteId());
        }
        dataSizeLoaded = true;
        return size;

    }

    @Override
    public List<ColorData> readPageData(FxTask currentTask, Connection conn) {
        if (palettesController.isAllColors()) {
            return tableColor.queryConditions(conn, null, null,
                    pagination.startRowOfCurrentPage, pagination.pageSize);
        } else {
            return tableColorPalette.colors(conn, palettesController.currentPaletteId(),
                    pagination.startRowOfCurrentPage, pagination.pageSize);
        }
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        colorsController.loadColors(palettesController.currentPalette(), tableData);
    }

    @Override
    protected int deleteData(FxTask currentTask, List<ColorData> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        if (palettesController.isAllColors()) {
            return tableColor.deleteData(data);
        } else {
            return tableColorPalette.delete(data);
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
        super.checkSelected();

        ColorData color = selectedItem();
        copyButton.setDisable(color == null);
        displayColorInfo(color);
    }

    protected void displayColorInfo(ColorData color) {
        if (color == null) {
            return;
        }
        infoController.displayHtml(color.html());
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        popButton.setDisable(isNoneSelected());
    }

    @FXML
    @Override
    public void copyAction() {
        ColorCopyController controller = (ColorCopyController) openStage(Fxmls.ColorCopyFxml);
        controller.setParameters(this);
        controller.requestMouse();
    }

    @Override
    public void doubleClicked(Event event) {
        popAction();
    }

    @FXML
    @Override
    public boolean popAction() {
        ColorData selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return false;
        }
        HtmlPopController.showHtml(this, selected.html());
        return true;
    }

    @FXML
    protected void popHelps(Event event) {
        if (UserConfig.getBoolean("ColorHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

    @FXML
    protected void showHelps(Event event) {
        popEventMenu(event, HelpTools.colorHelps(true));
    }


    /*
        static methods
     */
    public static ColorsManageController oneOpen() {
        ColorsManageController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof ColorsManageController) {
                try {
                    controller = (ColorsManageController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (ColorsManageController) WindowTools.openStage(Fxmls.ColorsManageFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static ColorsManageController addColors(List<Color> colors) {
        if (colors == null || colors.isEmpty()) {
            return null;
        }
        ColorsManageController manageController = oneOpen();
        if (manageController == null) {
            return null;
        }
        ColorCopyController addController = (ColorCopyController) WindowTools.childStage(manageController, Fxmls.ColorCopyFxml);
        addController.setParameters(manageController, colors);
        addController.requestMouse();
        return manageController;
    }

    public static ColorsManageController addOneColor(Color color) {
        if (color == null) {
            return null;
        }
        List<Color> colors = new ArrayList<>();
        colors.add(color);
        return addColors(colors);
    }

}
