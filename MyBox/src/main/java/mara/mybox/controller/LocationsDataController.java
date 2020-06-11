package mara.mybox.controller;

import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import mara.mybox.data.BaseTask;
import mara.mybox.data.Location;
import mara.mybox.db.TableLocation;
import static mara.mybox.fxml.FxmlStage.openScene;
import mara.mybox.fxml.TableBooleanCell;
import mara.mybox.fxml.TableCoordinateCell;
import mara.mybox.fxml.TableDateCell;
import mara.mybox.fxml.TableDoubleCell;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationsDataController extends TableManageController<Location> {

    protected ObservableList<String> dataSets;
    protected String currentDataSet;

    @FXML
    protected ListView<String> listView;
    @FXML
    protected TableColumn<Location, Long> dataidColumn;
    @FXML
    protected TableColumn<Location, String> datasetColumn, labelColumn, addressColumn, commentsColumn, imageColumn;
    @FXML
    protected TableColumn<Location, Double> longitudeColumn, latitudeColumn, altitudeColumn,
            valueColumn, sizeColumn, precisionColumn, speedColumn;
    @FXML
    protected TableColumn<Location, Date> timeColumn;
    @FXML
    protected TableColumn<Location, Integer> directionColumn;
    @FXML
    protected TableColumn<Location, Boolean> timeBCColumn;

    public LocationsDataController() {
        baseTitle = AppVariables.message("LocationsData");

        dataName = "Location";
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            dataSets = FXCollections.observableArrayList();
            listView.setItems(dataSets);
            listView.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String ot, String selected) -> {
                        if (isSettingValues || selected == null) {
                            return;
                        }
                        currentDataSet = selected;
                        loadTableData();
                    });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    protected void initColumns() {
        try {

            dataidColumn.setCellValueFactory(new PropertyValueFactory<>("dataid"));
            datasetColumn.setCellValueFactory(new PropertyValueFactory<>("dataSet"));
            labelColumn.setCellValueFactory(new PropertyValueFactory<>("dataLabel"));
            addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            commentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));
            imageColumn.setCellValueFactory(new PropertyValueFactory<>("imageLocation"));
            longitudeColumn.setCellValueFactory(new PropertyValueFactory<>("longitude"));
            longitudeColumn.setCellFactory(new TableCoordinateCell());
            latitudeColumn.setCellValueFactory(new PropertyValueFactory<>("latitude"));
            latitudeColumn.setCellFactory(new TableCoordinateCell());
            altitudeColumn.setCellValueFactory(new PropertyValueFactory<>("altitude"));
            altitudeColumn.setCellFactory(new TableDoubleCell());
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("dataValue"));
            valueColumn.setCellFactory(new TableDoubleCell());
            sizeColumn.setCellValueFactory(new PropertyValueFactory<>("dataSize"));
            sizeColumn.setCellFactory(new TableDoubleCell());
            precisionColumn.setCellValueFactory(new PropertyValueFactory<>("precision"));
            precisionColumn.setCellFactory(new TableDoubleCell());
            speedColumn.setCellValueFactory(new PropertyValueFactory<>("speed"));
            speedColumn.setCellFactory(new TableDoubleCell());
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("dataTime"));
            timeColumn.setCellFactory(new TableDateCell());
            timeBCColumn.setCellValueFactory(new PropertyValueFactory<>("timeBC"));
            timeBCColumn.setCellFactory(new TableBooleanCell());
            directionColumn.setCellValueFactory(new PropertyValueFactory<>("direction"));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        loadDataSets();
        loadTableData();
    }

    protected void loadDataSets() {
        synchronized (this) {

            BaseTask namesTask = new BaseTask<Void>() {
                private List<String> datasets;

                @Override
                protected boolean handle() {
                    datasets = TableLocation.datasets();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    dataSets.clear();
                    dataSets.add(message("All"));
                    dataSets.addAll(datasets);
                    isSettingValues = false;
                    checkSelected();
                    if (currentDataSet != null) {
                        listView.getSelectionModel().select(currentDataSet);
                    } else {
                        listView.getSelectionModel().select(message("All"));
                    }
                }
            };
            openHandlingStage(namesTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(namesTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public int readDataSize() {
        return TableLocation.size();
    }

    @Override
    public List<Location> readPageData() {
        if (currentDataSet == null || message("All").equals(currentDataSet)) {
            return TableLocation.read(currentPageStart, currentPageSize); //Current limitation due to non-pagenation
        } else {
            return TableLocation.read(currentDataSet, currentPageStart, currentPageSize);
        }
    }

    @Override
    public void itemDoubleClicked() {
        editAction();
    }

    @FXML
    @Override
    public void addAction() {
        try {
            LocationEditController controller
                    = (LocationEditController) openScene(null, CommonValues.LocationEditFxml);
            if (controller != null) {
                controller.parent = this;
                controller.loadDataSets(currentDataSet);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void editAction() {
        Location selected = (Location) tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            LocationEditController controller
                    = (LocationEditController) openScene(null, CommonValues.LocationEditFxml);
            if (controller != null) {
                controller.parent = this;
                controller.loadLocation(selected);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected boolean deleteSelectedData() {
        List<Location> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return false;
        }
        return TableLocation.deleteData(selected);
    }

    @Override
    protected boolean clearData() {
        if (currentDataSet == null || message("All").equals(currentDataSet)) {
            return new TableLocation().clear();
        } else {
            return TableLocation.delete(currentDataSet);
        }
    }

    @FXML
    public void locationAction() {
        try {
            LocationsDataInMapController controller
                    = (LocationsDataInMapController) openScene(null, CommonValues.LocationsDataInMapFxml);
            if (currentDataSet != null) {
                controller.display(currentDataSet);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        loadDataSets();
    }

    @Override
    public void loadExamples() {
        Location.ChinaEarlyCultures();
    }

}
