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
    protected void DataManufacture(ActionEvent event) {
        loadScene(Fxmls.Data2DManufactureFxml);
    }

    @FXML
    protected void manageData(ActionEvent event) {
        loadScene(Fxmls.Data2DManageFxml);
    }

    @FXML
    protected void SpliceData(ActionEvent event) {
        loadScene(Fxmls.Data2DSpliceFxml);
    }

    @FXML
    protected void RowFilter(ActionEvent event) {
        loadScene(Fxmls.RowFilterFxml);
    }

    @FXML
    protected void Data2DDefinition(ActionEvent event) {
        loadScene(Fxmls.Data2DDefinitionFxml);
    }

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
    protected void openGeographyCode(ActionEvent event) {
        loadScene(Fxmls.GeographyCodeFxml);
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
    protected void dataInSystemClipboard(ActionEvent event) {
        DataInSystemClipboardController.oneOpen();
    }

    @FXML
    protected void dataInMyBoxClipboard(ActionEvent event) {
        DataInMyBoxClipboardController.oneOpen();
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
    protected void openTextDataConvert(ActionEvent event) {
        loadScene(Fxmls.DataFileTextConvertFxml);
    }

    @FXML
    protected void openTextDataMerge(ActionEvent event) {
        loadScene(Fxmls.DataFileTextMergeFxml);
    }

    @FXML
    protected void DatabaseSQL(ActionEvent event) {
        DataTreeController.sql(parentController, false);
    }

    @FXML
    protected void DatabaseTable(ActionEvent event) {
        loadScene(Fxmls.DataTablesFxml);
    }

    @FXML
    protected void databaseTableDefinition(ActionEvent event) {
        loadScene(Fxmls.DatabaseTableDefinitionFxml);
    }

    @FXML
    protected void JShell(ActionEvent event) {
        DataTreeController.jShell(parentController, false);
    }

    @FXML
    protected void JEXL(ActionEvent event) {
        DataTreeController.jexl(parentController, false);
    }

    @FXML
    protected void JavaScript(ActionEvent event) {
        loadScene(Fxmls.JavaScriptFxml);
    }

    @FXML
    protected void MathFunction(ActionEvent event) {
        DataTreeController.mathFunction(parentController, false);
    }

}
