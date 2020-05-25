package mara.mybox.controller;

import java.sql.Connection;
import java.sql.DriverManager;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import mara.mybox.data.ColorData;
import mara.mybox.data.StringTable;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.TableColorData;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.TableColorCell;
import mara.mybox.image.ImageColor;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
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
    protected Button htmlButton, inButton, outButton, commonColorsButton;
    @FXML
    protected ColorPicker colorPicker;
    @FXML
    protected CheckBox mergeCheck, allColumnsCheck;

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
                cell.setSelectedStateCallback((Integer index) -> tableData.get(index).getInPaletteProperty());
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
            logger.error(e.toString());
        }
    }

    @Override
    protected void initButtons() {
        try {
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
            logger.error(e.toString());
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
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        FxmlControl.setTooltip(inButton, message("PutInColorPalette"));
        FxmlControl.setTooltip(outButton, message("RemoveFromColorPalette"));
        FxmlControl.removeTooltip(commonColorsButton);

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
        return TableColorData.read(currentPageStart, currentPageSize);
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
    protected void popCommonColorsMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu = new MenuItem(message("ImportWebCommonColors"));
            menu.setOnAction((ActionEvent event) -> {
                commonColors("web");
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("ImportChineseTraditionalColors"));
            menu.setOnAction((ActionEvent event) -> {
                commonColors("chinese");
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ImportJapaneseTraditionalColors"));
            menu.setOnAction((ActionEvent event) -> {
                commonColors("japanese");
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("MenuClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void commonColors(String type) {
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                        conn.setAutoCommit(false);
                        switch (type) {
                            case "chinese":
                                for (Color color
                                        : FxmlColor.chineseColorValues()) {
                                    String rgba = color.toString();
                                    String name = FxmlColor.chineseColorNames().get(color);
                                    TableColorData.write(conn, rgba, name, true);
                                }
                                break;
                            case "japanese":
                                for (Color color
                                        : FxmlColor.japaneseColorValues()) {
                                    String rgba = color.toString();
                                    String name = FxmlColor.japaneseColorNames().get(color);
                                    TableColorData.write(conn, rgba, name, true);
                                }
                                break;
                            default:
                                for (Color color : FxmlColor.webColorValues()) {
                                    String rgba = color.toString();
                                    String name = FxmlColor.webColorNames().get(color);
                                    TableColorData.write(conn, rgba, name, true);
                                }
                                break;
                        }
                        conn.commit();
                    } catch (Exception e) {
                        error = e.toString();
                        logger.debug(e.toString());
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    loadTableData();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void inPaletteAction() {
        List<ColorData> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    TableColorData.addInPalette(selected);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    loadTableData();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
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
            if (task != null) {
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
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    protected boolean deleteSelectedData() {
        List<ColorData> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return false;
        }
        return TableColorData.deleteData(selected);
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

    @FXML
    public void htmlAction() {
        try {
            List<ColorData> rows = tableView.getSelectionModel().getSelectedItems();
            if (rows == null || rows.isEmpty()) {
                rows = tableData;
            }
            if (rows == null || rows.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            List<String> names = new ArrayList<>();
            for (TableColumn column : tableView.getColumns()) {
                if (!column.equals(rgbaColumn) && !column.equals(rgbColumn)) {
                    names.add(column.getText());

                }
            }
            StringTable table = new StringTable(names, message("ManageColors"), 0);
            for (ColorData data : rows) {
                List<String> row = new ArrayList<>();
                for (TableColumn column : tableView.getColumns()) {
                    if (column.equals(colorValueColumn)) {
                        row.add(data.getColorValue() + "");
                    } else if (column.equals(colorColumn)) {
                        row.add(data.getRgba());
                    } else if (column.equals(inPaletteColumn)) {
                        row.add(message(data.getInPalette() + ""));
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
            logger.error(e.toString());
        }
    }
}
