package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import mara.mybox.data.GeoCoordinateSystem;
import mara.mybox.db.data.BaseDataAdaptor;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TableCoordinateSystemCell;
import mara.mybox.fxml.cell.TableLatitudeCell;
import mara.mybox.fxml.cell.TableLongitudeCell;
import mara.mybox.fxml.cell.TableMessageCell;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-2-3
 * @License Apache License Version 2.0
 */
public class GeographyCodeController extends BaseDataManageController<GeographyCode> {

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
    protected TableColumn<GeographyCode, String> sourceColumn;
    @FXML
    protected TableColumn<GeographyCode, GeoCoordinateSystem> coordinateSystemColumn;
    @FXML
    protected ColorSetController predefinedColorSetController, inputtedColorSetController;

    public GeographyCodeController() {
        baseTitle = Languages.message("GeographyCode");
    }

    @Override
    public void setTableDefinition() {
        tableDefinition = new TableGeographyCode();
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

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
            sourceColumn.setCellValueFactory(new PropertyValueFactory<>("sourceName"));
            sourceColumn.setCellFactory(new TableMessageCell());

            tableView.setRowFactory((TableView<GeographyCode> param) -> {
                return new SourceRow();
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            } else if (GeographyCode.isPredefined(item)) {
                setStyle("-fx-background-color: " + predefinedColorSetController.rgb());
            } else {
                setStyle("-fx-background-color: " + inputtedColorSetController.rgb());
            }
        }
    };

    @Override
    public void initControls() {
        try {
            super.initControls();

            predefinedColorSetController.init(this, baseName + "PredefinedColor", Color.LAVENDERBLUSH);
            inputtedColorSetController.init(this, baseName + "InputtedColor", Color.WHITE);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            mapController.dataController = this;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean mapCurrentPage() {
        return true;
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
    public List<GeographyCode> readPageData(Connection conn) {
        setPageSQL();
//        MyBoxLog.debug(dataQuerySQL);
        return TableGeographyCode.queryCodes(conn, pageQuerySQL, true);
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        if (mapCurrentPage()) {
            mapController.drawGeographyCodes(tableData, finalTitle);
        }
    }

    @Override
    protected String loadMoreInfo() {
        String s = "<SPAN class=\"boldText\">" + Languages.message("MapQuery") + ": </SPAN></br>";
        if (mapCurrentPage()) {
            s += "<SPAN class=\"valueText\">" + Languages.message("CurrentPage") + "</SPAN></br>"
                    + "<SPAN class=\"boldText\">" + Languages.message("DataNumber") + ": </SPAN>"
                    + "<SPAN class=\"valueText\">" + (tableData != null ? tableData.size() : "") + "</SPAN></br></br>";
        } else {
            s += "<SPAN class=\"valueText\">" + Languages.message("CurrentQuery") + "</SPAN></br>"
                    + "<SPAN class=\"boldText\">" + Languages.message("DataNumber") + ": </SPAN>"
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
        if (backgroundTask != null) {
            backgroundTask.cancel();
        }
        backgroundTask = new SingletonTask<Void>(this) {
            private List<GeographyCode> mapData;

            @Override
            protected boolean handle() {
                mapData = TableGeographyCode.queryCodes(dataQuerySQL, -1, true);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                mapController.drawGeographyCodes(mapData, queryCondition.getTitle());
                loadInfo();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                backgroundTask = null;
            }
        };
        start(backgroundTask);
    }

    @Override
    protected boolean checkClearCondition() {
        if (!super.checkClearCondition()) {
            return false;
        }
        String where = clearCondition.getWhere() == null || clearCondition.getWhere().isBlank()
                ? "gcsource<>2" : " ( " + clearCondition.getWhere() + " ) AND gcsource<>2";
        clearCondition.setWhere(where);
        return true;
    }

    @FXML
    @Override
    public void addAction() {
        try {
            GeographyCodeEditController controller = (GeographyCodeEditController) openStage(Fxmls.GeographyCodeEditFxml);
            controller.load(this, null);
            controller.getMyStage().requestFocus();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void editAction() {
        GeographyCode selected = (GeographyCode) selectedItem();
        if (selected == null) {
            return;
        }
        try {
            GeographyCodeEditController controller = (GeographyCodeEditController) openStage(Fxmls.GeographyCodeEditFxml);
            controller.load(this, selected);
            controller.getMyStage().requestFocus();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void viewAction() {
        GeographyCode selected = (GeographyCode) selectedItem();
        if (selected == null) {
            return;
        }
        HtmlTableController.open(message("GeographyCode"),
                BaseDataAdaptor.displayData(tableDefinition, selected, null, true));
    }

    @FXML
    public void locationAction(ActionEvent event) {
        try {
            GeographyCode code = (GeographyCode) selectedItem();
            if (code == null) {
                return;
            }
            LocationInMapController.load(code.getLongitude(), code.getLatitude());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

//    @Override
//    protected int deleteData(List<GeographyCode> data) {
//        if (data == null || data.isEmpty()) {
//            return 0;
//        }
//        return TableGeographyCode.delete(data);
//    }
    @FXML
    public void defaultColors() {
        predefinedColorSetController.setColor(Color.LAVENDERBLUSH);
        inputtedColorSetController.setColor(Color.WHITE);
    }

    @FXML
    public void randomColors() {
        List<String> colors = FxColorTools.randomRGB(2);

        predefinedColorSetController.setColor(Color.web(colors.get(0)));
        inputtedColorSetController.setColor(Color.web(colors.get(1)));
    }

    @FXML
    public void applyColors() {
        tableView.refresh();
        popSuccessful();
    }

    @FXML
    @Override
    protected void popImportMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(Languages.message("ImportGeographyCodeExternalCSVFormat"));
            menu.setOnAction((ActionEvent event) -> {
                GeographyCodeImportExternalCSVController controller
                        = (GeographyCodeImportExternalCSVController) openStage(Fxmls.GeographyCodeImportExternalCSVFxml);
                controller.parent = this;
            });
            items.add(menu);

            menu = new MenuItem(Languages.message("ImportGeographyCodeGeonamesFormat"));
            menu.setOnAction((ActionEvent event) -> {
                GeographyCodeImportGeonamesFileController controller
                        = (GeographyCodeImportGeonamesFileController) openStage(Fxmls.GeographyCodeImportGeonamesFileFxml);
                controller.parent = this;
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
            menu = new MenuItem(Languages.message("RecoverGeographyCodePredefined"));
            menu.setOnAction((ActionEvent event) -> {
                predefined();
            });
            items.add(menu);

//            items.add(new SeparatorMenuItem());
//            menu = new MenuItem(message("ImportChinaTowns"));
//            menu.setOnAction((ActionEvent event) -> {
//                importChinaTowns();
//            });
//            items.add(menu);
            items.add(new SeparatorMenuItem());
            popEventMenu(mouseEvent, items);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void predefined() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

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
        loading = start(task);
    }

    public void importChinaTowns() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                File file = FxFileTools.getInternalFile("/data/examples/Geography_Code_china_towns_internal.csv",
                        "data", "Geography_Code_china_towns_internal.csv");
                GeographyCodeTools.importInternalCSV(loading, file, true);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                refreshAction();
            }
        };
        loading = start(task);
    }

    @FXML
    @Override
    protected void popSetMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(Languages.message("SetAsPredefinedData"));
            menu.setOnAction((ActionEvent event) -> {
                setSelectedData(true);
            });
            items.add(menu);
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("SetAsInputtedData"));
            menu.setOnAction((ActionEvent event) -> {
                setSelectedData(false);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
            popEventMenu(mouseEvent, items);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setSelectedData(boolean predefined) {
        final List<GeographyCode> selected = selectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                for (GeographyCode code : selected) {
                    if (predefined) {
                        code.setSource(GeographyCode.AddressSource.PredefinedData);
                    } else {
                        code.setSource(GeographyCode.AddressSource.InputtedData);
                    }
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
        start(task);
    }

    @Override
    public void cleanPane() {
        try {
            mapController.cleanPane();
            if (loading != null) {
                loading.cancelAction();
                loading = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
