package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import mara.mybox.data.CoordinateSystem;
import mara.mybox.data.GeographyCode;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.TableBooleanCell;
import mara.mybox.fxml.TableCoordinateSystemCell;
import mara.mybox.fxml.TableLatitudeCell;
import mara.mybox.fxml.TableLongitudeCell;
import mara.mybox.tools.GeographyCodeTools;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class GeographyCodeController extends DataAnalysisController<GeographyCode> {

    protected String predefinedColor, inputtedColor;
    protected LoadingController loading;

    @FXML
    protected Tab mapTab;
    @FXML
    protected GeographyCodeMapController mapController;
    @FXML
    protected TableColumn<GeographyCode, String> levelColumn, chinesenameColumn, englishnameColumn,
            code1Column, code2Column, code3Column, code4Column, code5Column,
            alias1Column, alias2Column, alias3Column, alias4Column, alias5Column;
    @FXML
    protected TableColumn<GeographyCode, Double> longitudeColumn, latitudeColumn;
    @FXML
    protected TableColumn<GeographyCode, Boolean> predefinedColumn;
    @FXML
    protected TableColumn<GeographyCode, CoordinateSystem> coordinateSystemColumn;
    @FXML
    protected Button locationButton, palettePredefinedButton, paletteInputtedButton;
    @FXML
    protected Rectangle predefinedRect, inputtedRect;

    public GeographyCodeController() {
        baseTitle = message("GeographyCode");
    }

    @Override
    public void setTableDefinition() {
        tableDefinition = new TableGeographyCode();
    }

    @Override
    protected DataExportController dataExporter() {
        return (GeographyCodeExportController) openStage(CommonValues.GeographyCodeExportFxml);
    }

    @Override
    protected void initColumns() {
        try {

            levelColumn.setCellValueFactory(new PropertyValueFactory<>("levelName"));
            chinesenameColumn.setCellValueFactory(new PropertyValueFactory<>("chineseName"));
            englishnameColumn.setCellValueFactory(new PropertyValueFactory<>("englishName"));
            longitudeColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
            longitudeColumn.setCellFactory(new TableLongitudeCell());
            latitudeColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
            latitudeColumn.setCellFactory(new TableLatitudeCell());
            coordinateSystemColumn.setCellValueFactory(new PropertyValueFactory<>("coordinateSystem"));
            coordinateSystemColumn.setCellFactory(new TableCoordinateSystemCell());
            code1Column.setCellValueFactory(new PropertyValueFactory<>("code1"));
            code2Column.setCellValueFactory(new PropertyValueFactory<>("code2"));
            code3Column.setCellValueFactory(new PropertyValueFactory<>("code3"));
            code4Column.setCellValueFactory(new PropertyValueFactory<>("code4"));
            code5Column.setCellValueFactory(new PropertyValueFactory<>("code5"));
            alias1Column.setCellValueFactory(new PropertyValueFactory<>("alias1"));
            alias2Column.setCellValueFactory(new PropertyValueFactory<>("alias2"));
            alias3Column.setCellValueFactory(new PropertyValueFactory<>("alias3"));
            alias4Column.setCellValueFactory(new PropertyValueFactory<>("alias4"));
            alias5Column.setCellValueFactory(new PropertyValueFactory<>("alias5"));
            predefinedColumn.setCellValueFactory(new PropertyValueFactory<>("predefined"));
            predefinedColumn.setCellFactory(new TableBooleanCell());

            tableView.setRowFactory((TableView<GeographyCode> param) -> {
                return new SourceRow();
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected class SourceRow extends TableRow<GeographyCode> {

        @Override
        protected void updateItem(GeographyCode item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                setTextFill(null);
                return;
            }
            if (this.isSelected()) {
                setStyle("-fx-background-color:  #0096C9; -fx-text-background-color: white");
            } else if (item.isPredefined()) {
                setStyle("-fx-background-color: " + predefinedColor);
            } else {
                setStyle("-fx-background-color: " + inputtedColor);
            }
        }
    };

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            predefinedColor = FxmlColor.rgb2Hex(Color.LAVENDERBLUSH);
            try {
                predefinedColor = AppVariables.getUserConfigValue("GeographyCodePredefinedDataColor", predefinedColor);
                predefinedRect.setFill(Color.web(predefinedColor));
            } catch (Exception e) {
                predefinedRect.setFill(Color.LAVENDERBLUSH);
                AppVariables.setUserConfigValue("GeographyCodePredefinedDataColor", predefinedColor);
            }
            FxmlControl.setTooltip(predefinedRect, FxmlColor.colorNameDisplay((Color) predefinedRect.getFill()));

            inputtedColor = FxmlColor.rgb2Hex(Color.WHITE);
            try {
                inputtedColor = AppVariables.getUserConfigValue("GeographyCodeInputtedDataColor", inputtedColor);
                inputtedRect.setFill(Color.web(inputtedColor));
            } catch (Exception e) {
                inputtedRect.setFill(Color.WHITE);
                AppVariables.setUserConfigValue("GeographyCodeInputtedDataColor", inputtedColor);
            }
            FxmlControl.setTooltip(inputtedRect, FxmlColor.colorNameDisplay((Color) inputtedRect.getFill()));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            mapController.initMap(this);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public boolean mapCurrentPage() {
        return !paginate || mapController.mapOptionsController.currentPageRadio.isSelected();
    }

    @FXML
    @Override
    public void queryData() {
        if (isSettingValues) {
            return;
        }
        super.queryData();
        if (!mapCurrentPage()) {
            queryMap();
        }
    }

    @Override
    public List<GeographyCode> readPageData() {
        setPageSQL();
//        logger.debug(dataQuerySQL);
        return TableGeographyCode.queryCodes(pageQuerySQL, true);
    }

    @Override
    public void postLoadedTableData() {
        if (queryCondition == null) {
            return;
        }
        super.postLoadedTableData();
        if (mapCurrentPage()) {
            mapController.drawGeographyCodes(tableData, finalTitle);
        }
    }

    @Override
    protected String loadMoreInfo() {
        String s = "<SPAN class=\"boldText\">" + message("MapQuery") + ": </SPAN></br>";
        if (mapCurrentPage()) {
            s += "<SPAN class=\"valueText\">" + message("CurrentPage") + "</SPAN></br>"
                    + "<SPAN class=\"boldText\">" + message("DataNumber") + ": </SPAN>"
                    + "<SPAN class=\"valueText\">" + (tableData != null ? tableData.size() : "") + "</SPAN></br></br>";
        } else {
            s += "<SPAN class=\"valueText\">" + message("CurrentQuery") + "</SPAN></br>"
                    + "<SPAN class=\"boldText\">" + message("DataNumber") + ": </SPAN>"
                    + "<SPAN class=\"valueText\">"
                    + (mapController.geographyCodes != null ? mapController.geographyCodes.size() : "")
                    + "</SPAN></br></br>";
        }
        return s;
    }

    @Override
    public void reloadChart() {
        mapController.clearAction();
        if (mapCurrentPage()) {
            mapController.drawGeographyCodes(tableData, finalTitle);
            return;
        }
        queryMap();
    }

    public void queryMap() {
        if (isSettingValues || queryCondition == null || dataQuerySQL == null) {
            return;
        }
        synchronized (this) {
            if (backgroundTask != null) {
                return;
            }
            backgroundTask = new SingletonTask<Void>() {
                private List<GeographyCode> mapData;

                @Override
                protected boolean handle() {
                    mapData = TableGeographyCode.queryCodes(dataQuerySQL,
                            mapController.mapOptionsController.dataMax, true);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    mapController.drawGeographyCodes(mapData, queryCondition.getTitle());
                    loadInfo();
                }

                @Override
                protected void taskQuit() {
                    backgroundTask = null;
                }
            };
            openHandlingStage(backgroundTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(backgroundTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    protected boolean checkClearCondition() {
        if (!super.checkClearCondition()) {
            return false;
        }
        String where = clearCondition.getWhere() == null || clearCondition.getWhere().isBlank()
                ? "predefined<>1" : " ( " + clearCondition.getWhere() + " ) AND predefined<>1";
        clearCondition.setWhere(where);
        return true;
    }

    @Override
    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        super.checkSelected();
        int selection = tableView.getSelectionModel().getSelectedIndices().size();
        if (locationButton != null) {
            locationButton.setDisable(selection == 0);
        }
    }

    @FXML
    @Override
    public void addAction() {
        try {
            GeographyCodeEditController controller = (GeographyCodeEditController) openStage(CommonValues.GeographyCodeEditFxml);
            controller.parent = this;
            controller.getMyStage().toFront();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void editAction() {
        GeographyCode selected = (GeographyCode) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            GeographyCodeEditController controller = (GeographyCodeEditController) openStage(CommonValues.GeographyCodeEditFxml);
            controller.parent = this;
            controller.setGeographyCode(selected);
            controller.getMyStage().toFront();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void viewAction() {
        GeographyCode selected = (GeographyCode) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        HtmlTools.viewHtml(message("GeographyCode"), selected.info("</br>"));
    }

    @FXML
    public void locationAction() {
        try {
            GeographyCode code = (GeographyCode) tableView.getSelectionModel().getSelectedItem();
            if (code == null) {
                return;
            }
            LocationInMapController controller = (LocationInMapController) openStage(CommonValues.LocationInMapFxml);
            controller.consumer = this;
            controller.setCoordinate(code.getLongitude(), code.getLatitude());
            controller.getMyStage().toFront();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected int deleteSelectedData() {
        List<GeographyCode> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return 0;
        }
        return TableGeographyCode.delete(selected);
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

            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            popError(e.toString());
            return false;
        }
    }

    @FXML
    public void defaultColors() {
        Color color = Color.LAVENDERBLUSH;
        predefinedRect.setFill(color);
        FxmlControl.setTooltip(predefinedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.WHITE;
        inputtedRect.setFill(color);
        FxmlControl.setTooltip(inputtedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));
    }

    @FXML
    public void randomColors() {
        List<String> colors = FxmlColor.randomColorsHex(2);

        Color color = Color.web(colors.get(0));
        predefinedRect.setFill(color);
        FxmlControl.setTooltip(predefinedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));

        color = Color.web(colors.get(1));
        inputtedRect.setFill(color);
        FxmlControl.setTooltip(inputtedRect, new Tooltip(FxmlColor.colorNameDisplay(color)));
    }

    @FXML
    public void applyColors() {
        Color color = (Color) (predefinedRect.getFill());
        predefinedColor = FxmlColor.rgb2Hex(color);
        AppVariables.setUserConfigValue("EpidemicReportsPredefinedDataColor", predefinedColor);

        color = (Color) (inputtedRect.getFill());
        inputtedColor = FxmlColor.rgb2Hex(color);
        AppVariables.setUserConfigValue("EpidemicReportsInputtedDataColor", inputtedColor);

        tableView.refresh();
        popSuccessful();
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
    @Override
    protected void popImportMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("ImportGeographyCodeExternalCSVFormat"));
            menu.setOnAction((ActionEvent event) -> {
                GeographyCodeImportExternalCSVController controller
                        = (GeographyCodeImportExternalCSVController) openStage(CommonValues.GeographyCodeImportExternalCSVFxml);
                controller.parent = this;
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("ImportGeographyCodeGeonamesFormat"));
            menu.setOnAction((ActionEvent event) -> {
                GeographyCodeImportGeonamesFileController controller
                        = (GeographyCodeImportGeonamesFileController) openStage(CommonValues.GeographyCodeImportGeonamesFileFxml);
                controller.parent = this;
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("RecoverGeographyCodePredefined"));
            menu.setOnAction((ActionEvent event) -> {
                predefined();
            });
            popMenu.getItems().add(menu);

//            popMenu.getItems().add(new SeparatorMenuItem());
//            menu = new MenuItem(message("ImportChinaTowns"));
//            menu.setOnAction((ActionEvent event) -> {
//                importChinaTowns();
//            });
//            popMenu.getItems().add(menu);
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

    public void predefined() {
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    GeographyCodeTools.importPredefined(null, loading);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    refreshAction();
                }
            };
            loading = openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void importChinaTowns() {
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    File file = FxmlControl.getInternalFile("/data/db/Geography_Code_china_towns_internal.csv",
                            "data", "Geography_Code_china_towns_internal.csv", true);
                    GeographyCodeTools.importInternalCSV(loading, file, true);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    refreshAction();
                }
            };
            loading = openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    protected void popSetMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu = new MenuItem(message("SetAsPredefinedData"));
            menu.setOnAction((ActionEvent event) -> {
                setSelectedData(true);
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("SetAsInputtedData"));
            menu.setOnAction((ActionEvent event) -> {
                setSelectedData(false);
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

    protected void setSelectedData(boolean predefined) {
        final List<GeographyCode> selected = tableView.getSelectionModel().getSelectedItems();
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
                    for (GeographyCode code : selected) {
                        code.setPredefined(predefined);
                    }
                    TableGeographyCode.write(selected);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    refreshAction();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public boolean leavingScene() {
        try {
            mapController.leavingScene();
            if (loading != null) {
                loading.cancelAction();
                loading = null;
            }
        } catch (Exception e) {
        }
        return super.leavingScene();
    }

}
