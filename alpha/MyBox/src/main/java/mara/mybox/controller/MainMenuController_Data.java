package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MainMenuController_Data extends MainMenuController_Network {

    @FXML
    protected void DataManufacture(ActionEvent event) {
        openScene(Fxmls.Data2DManufactureFxml);
    }

    @FXML
    protected void manageData(ActionEvent event) {
        openScene(Fxmls.Data2DManageFxml);
    }

    @FXML
    protected void SpliceData(ActionEvent event) {
        openScene(Fxmls.Data2DSpliceFxml);
    }

    @FXML
    protected void RowExpression(ActionEvent event) {
        DataTreeController.rowExpression(parentController, AppVariables.closeCurrentWhenOpenTool);
    }

    @FXML
    protected void DataColumn(ActionEvent event) {
        DataTreeController.dataColumn(parentController, AppVariables.closeCurrentWhenOpenTool);
    }

    @FXML
    protected void openMatricesManage(ActionEvent event) {
        openScene(Fxmls.MatricesManageFxml);
    }

    @FXML
    protected void openMatrixUnaryCalculation(ActionEvent event) {
        openScene(Fxmls.MatrixUnaryCalculationFxml);
    }

    @FXML
    protected void openMatricesBinaryCalculation(ActionEvent event) {
        openScene(Fxmls.MatricesBinaryCalculationFxml);
    }

    @FXML
    protected void openGeographyCode(ActionEvent event) {
        GeographyCodeController.open(parentController, AppVariables.closeCurrentWhenOpenTool, true);
    }

    @FXML
    protected void openLocationInMap(ActionEvent event) {
        openScene(Fxmls.LocationInMapFxml);
    }

    @FXML
    protected void ConvertCoordinate(ActionEvent event) {
        openScene(Fxmls.ConvertCoordinateFxml);
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
        openScene(Fxmls.DataFileExcelConvertFxml);
    }

    @FXML
    protected void openExcelMerge(ActionEvent event) {
        openScene(Fxmls.DataFileExcelMergeFxml);
    }

    @FXML
    protected void openCsvConvert(ActionEvent event) {
        openScene(Fxmls.DataFileCSVConvertFxml);
    }

    @FXML
    protected void openCsvMerge(ActionEvent event) {
        openScene(Fxmls.DataFileCSVMergeFxml);
    }

    @FXML
    protected void openTextDataConvert(ActionEvent event) {
        openScene(Fxmls.DataFileTextConvertFxml);
    }

    @FXML
    protected void openTextDataMerge(ActionEvent event) {
        openScene(Fxmls.DataFileTextMergeFxml);
    }

    @FXML
    protected void DatabaseSQL(ActionEvent event) {
        DataTreeController.sql(parentController, false);
    }

    @FXML
    protected void DatabaseTable(ActionEvent event) {
        openScene(Fxmls.DataTablesFxml);
    }

    @FXML
    protected void databaseTableDefinition(ActionEvent event) {
        openScene(Fxmls.DatabaseTableDefinitionFxml);
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
        DataTreeController.javascript(parentController, false);
    }

    @FXML
    protected void MathFunction(ActionEvent event) {
        DataTreeController.mathFunction(parentController, false);
    }

}
