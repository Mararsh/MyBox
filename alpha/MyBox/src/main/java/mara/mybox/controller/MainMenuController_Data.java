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
    protected void manufactureData(ActionEvent event) {
        loadScene(Fxmls.Data2DManufactureFxml);
    }

    @FXML
    protected void openMatricesManage(ActionEvent event) {
        MatricesManageController c = (MatricesManageController) loadScene(Fxmls.MatricesManageFxml);
        c.createAction();
    }

    @FXML
    protected void openMatrixUnaryCalculation(ActionEvent event) {
        MatrixUnaryCalculationController c = (MatrixUnaryCalculationController) loadScene(Fxmls.MatrixUnaryCalculationFxml);
        c.createAction();
    }

    @FXML
    protected void openMatricesBinaryCalculation(ActionEvent event) {
        MatricesBinaryCalculationController c = (MatricesBinaryCalculationController) loadScene(Fxmls.MatricesBinaryCalculationFxml);
        c.createAction();
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
        DataClipboardController c = (DataClipboardController) loadScene(Fxmls.DataClipboardFxml);
        c.createAction();
    }

    @FXML
    protected void openDataCsv(ActionEvent event) {
        DataFileCSVController c = (DataFileCSVController) loadScene(Fxmls.DataFileCSVFxml);
        c.createAction();
    }

    @FXML
    protected void openDataExcel(ActionEvent event) {
        DataFileExcelController c = (DataFileExcelController) loadScene(Fxmls.DataFileExcelFxml);
        c.createAction();
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

    @FXML
    protected void openDataText(ActionEvent event) {
        DataFileTextController c = (DataFileTextController) loadScene(Fxmls.DataFileTextFxml);
        c.createAction();
    }

    @FXML
    protected void openTextDataConvert(ActionEvent event) {
        loadScene(Fxmls.DataFileTextConvertFxml);
    }

    @FXML
    protected void openTextDataMerge(ActionEvent event) {
        loadScene(Fxmls.DataFileTextMergeFxml);
    }

}
