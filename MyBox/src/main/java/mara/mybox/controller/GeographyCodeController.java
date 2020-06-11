package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import mara.mybox.data.GeographyCode;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.TableBooleanCell;
import mara.mybox.fxml.TableCoordinateCell;
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
    protected TableColumn<GeographyCode, String> levelColumn, chinesenameColumn, englishnameColumn,
            code1Column, code2Column, code3Column, code4Column, code5Column,
            alias1Column, alias2Column, alias3Column, alias4Column, alias5Column;
    @FXML
    protected TableColumn<GeographyCode, Double> longitudeColumn, latitudeColumn;
    @FXML
    protected TableColumn<GeographyCode, Boolean> predefinedColumn;
    @FXML
    protected Button locationButton, examplesButton,
            palettePredefinedButton, paletteInputtedButton;
    @FXML
    protected Rectangle predefinedRect, inputtedRect;
    @FXML
    protected LocationsMapController mapController;
    @FXML
    protected ToggleGroup orderGroup;
    @FXML
    protected CheckBox descendCheck;

    public GeographyCodeController() {
        baseTitle = message("GeographyCode");
        baseName = "GeographyCode";
        dataName = "Geography_Code";
    }

    @Override
    protected void initColumns() {
        try {
            levelColumn.setCellValueFactory(new PropertyValueFactory<>("levelName"));
            chinesenameColumn.setCellValueFactory(new PropertyValueFactory<>("chineseName"));
            englishnameColumn.setCellValueFactory(new PropertyValueFactory<>("englishName"));
            longitudeColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
            longitudeColumn.setCellFactory(new TableCoordinateCell());
            latitudeColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
            latitudeColumn.setCellFactory(new TableCoordinateCell());
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
    public void initSQL() {
        queryPrefix = "SELECT * FROM " + dataName;
        sizePrefix = "SELECT count(gcid) FROM " + dataName;
        clearPrefix = "DELETE FROM " + dataName;
    }

    @Override
    protected DerbyBase dataTable() {
        return new TableGeographyCode();
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            mapController.initSplitPanes();
            mapController.controlRightPane();

//            GeographyCode.exportPredefined();
            String backFile = AppVariables.getSystemConfigValue("GeographyCode621Exported", "");
            if (!backFile.isBlank()) {
                browseURI(new File(backFile).getParentFile().toURI());
                alertInformation(message("DataExportedComments") + "\n\n" + backFile);
                AppVariables.deleteSystemConfigValue("GeographyCode621Exported");
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void checkOrderBy() {
        try {
            queryOrder = null;
            orderTitle = null;
            String order = descendCheck.isSelected() ? "DESC" : "ASC";
            String selected = ((RadioButton) orderGroup.getSelectedToggle()).getText();
            if (selected == null || selected.isBlank()) {
                return;
            }
            orderTitle = selected + " "
                    + (descendCheck.isSelected() ? message("Descending") : message("Ascending"));
            if (message("Dataid").equals(selected)) {
                queryOrder = "gcid " + order;
            } else if (message("Level").equals(selected)) {
                queryOrder = "level " + order;
            } else if (message("ChineseName").equals(selected)) {
                queryOrder = "chinese_name " + order;
            } else if (message("EnglishName").equals(selected)) {
                queryOrder = "english_name " + order;
            } else if (message("Longitude").equals(selected)) {
                queryOrder = "longitude " + order;
            } else if (message("Latitude").equals(selected)) {
                queryOrder = "latitude " + order;
            } else if (message("Area").equals(selected)) {
                queryOrder = "latitude " + order;
            } else if (message("Population").equals(selected)) {
                queryOrder = "population " + order;
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public int readDataSize() {
//        logger.debug(sizeQuerySQL);
        return TableGeographyCode.size(sizeQuerySQL);
    }

    @Override
    public List<GeographyCode> readPageData() {
        setPageSQL();
//        logger.debug(dataQuerySQL);
        return TableGeographyCode.queryCodes(pageQuerySQL, true);
    }

    @Override
    public boolean preLoadingTableData() {
        if (super.preLoadingTableData()) {
            mapController.clearAction();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void postLoadedTableData() {
        if (queryCondition == null) {
            return;
        }
        super.postLoadedTableData();
        mapController.drawGeographyCodes(3, tableData, finalTitle);

    }

    @Override
    protected String loadMoreInfo() {
        if (tableData.isEmpty()) {
            return "";
        } else {
            return "<b>" + message("MapQuery") + ": </b></br>"
                    + "<font color=\"#2e598a\">" + message("CurrentPage") + "</font></br>"
                    + "<b>" + message("DataNumber") + ": </b>" + tableData.size() + "</br></br>";
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
            controller.loadCode(selected);
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
            int mapZoom = 4;
//            if (code.getLevel() != null && message("Country").equals(code.getLevel())) {
//                mapZoom = 3;
//            }
            LocationInMapController controller = (LocationInMapController) openStage(CommonValues.LocationInMapFxml);
            controller.load(code.getLongitude(), code.getLatitude(), mapZoom);
            controller.getMyStage().setAlwaysOnTop(true);
            controller.getMyStage().toFront();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected boolean deleteSelectedData() {
        List<GeographyCode> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return false;
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
                    GeographyCode.predefined(null, loading);
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
                            "data", "Geography_Code_china_towns_internal.csv", false);
                    GeographyCode.importInternalCSV(loading, file, true);
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

    @Override
    protected DataExportController dataExporter() {
        return (GeographyCodeExportController) openStage(CommonValues.GeographyCodeExportFxml);
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
