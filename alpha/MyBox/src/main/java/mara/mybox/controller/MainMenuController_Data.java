package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MainMenuController_Data extends MainMenuController_Network {

    @FXML
    protected void openMatricesManage(ActionEvent event) {
        loadScene(Fxmls.MatricesManageFxml);
    }

    @FXML
    protected void openMatrixUnaryCalculation(ActionEvent event) {
        loadScene(Fxmls.MatrixUnaryCalculationFxml);
    }

    @FXML
    protected void openMatricesBinaryCalculation(ActionEvent event) {
        loadScene(Fxmls.MatricesBinaryCalculationFxml);
    }

    @FXML
    protected void openDataset(ActionEvent event) {
        loadScene(Fxmls.DatasetFxml);
    }

    @FXML
    protected void openLocationData(ActionEvent event) {
        loadScene(Fxmls.LocationDataFxml);
    }

    @FXML
    protected void openGeographyCode(ActionEvent event) {
        loadScene(Fxmls.GeographyCodeFxml);
    }

    @FXML
    protected void openLocationsDataInMap(ActionEvent event) {
        loadScene(Fxmls.LocationsDataInMapFxml);
    }

    @FXML
    protected void openLocationInMap(ActionEvent event) {
        loadScene(Fxmls.LocationInMapFxml);
    }

    @FXML
    protected void ConvertCoordinate(ActionEvent event) {
        loadScene(Fxmls.ConvertCoordinateFxml);
    }

    @FXML
    protected void openEpidemicReports(ActionEvent event) {
        loadScene(Fxmls.EpidemicReportsFxml);
    }

    @FXML
    protected void openDataClipboard(ActionEvent event) {
        loadScene(Fxmls.DataClipboardFxml);
    }

    @FXML
    protected void openDataCsv(ActionEvent event) {
        DataFileCSVController c = (DataFileCSVController) loadScene(Fxmls.DataFileCSVFxml);
        c.newSheet(3, 3);
    }

    @FXML
    protected void openDataExcel(ActionEvent event) {
        DataFileExcelController c = (DataFileExcelController) loadScene(Fxmls.DataFileExcelFxml);
        c.newSheet(3, 3);
    }

    @FXML
    protected void openExcelConvert(ActionEvent event) {
        loadScene(Fxmls.DataFileExcelConvertFxml);
    }

    @FXML
    protected void openExcelMerge(ActionEvent event) {
        loadScene(Fxmls.DataFileExcelMergeFxml);
    }

    @FXML
    protected void openCsvConvert(ActionEvent event) {
        loadScene(Fxmls.DataFileCSVConvertFxml);
    }

    @FXML
    protected void openCsvMerge(ActionEvent event) {
        loadScene(Fxmls.DataFileCSVMergeFxml);
    }

}
