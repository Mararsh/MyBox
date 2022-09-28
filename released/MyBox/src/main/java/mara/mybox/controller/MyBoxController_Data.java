package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Data extends MyBoxController_Network {

    @FXML
    protected void showDataMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem DataManufacture = new MenuItem(message("DataManufacture"));
        DataManufacture.setOnAction((ActionEvent event1) -> {
            DataManufactureController c = (DataManufactureController) loadScene(Fxmls.DataManufactureFxml);
            c.createAction();
        });

        MenuItem ManageData = new MenuItem(message("ManageData"));
        ManageData.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.Data2DManageFxml);
        });

        MenuItem SpliceData = new MenuItem(message("SpliceData"));
        SpliceData.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.Data2DSpliceFxml);
        });

        MenuItem DataInSystemClipboard = new MenuItem(message("DataInSystemClipboard"));
        DataInSystemClipboard.setOnAction((ActionEvent event1) -> {
            DataInMyBoxClipboardController.oneOpen();
        });

        MenuItem DataInMyBoxClipboard = new MenuItem(message("DataInMyBoxClipboard"));
        DataInMyBoxClipboard.setOnAction((ActionEvent event1) -> {
            DataInMyBoxClipboardController c = DataInMyBoxClipboardController.oneOpen();
            c.createAction();
        });

        MenuItem EditExcel = new MenuItem(message("EditExcel"));
        EditExcel.setOnAction((ActionEvent event1) -> {
            DataFileExcelController c = (DataFileExcelController) loadScene(Fxmls.DataFileExcelFxml);
            c.dataController.create();
        });

        MenuItem ExcelConvert = new MenuItem(message("ExcelConvert"));
        ExcelConvert.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DataFileExcelConvertFxml);
        });

        MenuItem ExcelMerge = new MenuItem(message("ExcelMerge"));
        ExcelMerge.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DataFileExcelMergeFxml);
        });

        MenuItem EditCSV = new MenuItem(message("EditCSV"));
        EditCSV.setOnAction((ActionEvent event1) -> {
            DataFileCSVController c = (DataFileCSVController) loadScene(Fxmls.DataFileCSVFxml);
            c.createAction();
        });

        MenuItem CsvConvert = new MenuItem(message("CsvConvert"));
        CsvConvert.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DataFileCSVConvertFxml);
        });

        MenuItem CsvMerge = new MenuItem(message("CsvMerge"));
        CsvMerge.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DataFileCSVMergeFxml);
        });

        MenuItem TextData = new MenuItem(message("EditTextDataFile"));
        TextData.setOnAction((ActionEvent event1) -> {
            DataFileTextController c = (DataFileTextController) loadScene(Fxmls.DataFileTextFxml);
            c.createAction();
        });

        MenuItem TextDataConvert = new MenuItem(message("TextDataConvert"));
        TextDataConvert.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DataFileTextConvertFxml);
        });

        MenuItem TextDataMerge = new MenuItem(message("TextDataMerge"));
        TextDataMerge.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DataFileTextMergeFxml);
        });

        Menu DataFile = new Menu(message("DataFile"));
        DataFile.getItems().addAll(
                EditCSV, CsvConvert, CsvMerge, new SeparatorMenuItem(),
                EditExcel, ExcelConvert, ExcelMerge, new SeparatorMenuItem(),
                TextData, TextDataConvert, TextDataMerge
        );

        MenuItem Dataset = new MenuItem(message("Dataset"));
        Dataset.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DatasetFxml);
        });

        MenuItem GeographyCode = new MenuItem(message("GeographyCode"));
        GeographyCode.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.GeographyCodeFxml);
        });

        MenuItem LocationInMap = new MenuItem(message("LocationInMap"));
        LocationInMap.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.LocationInMapFxml);
        });

        MenuItem LocationData = new MenuItem(message("LocationData"));
        LocationData.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.LocationDataFxml);
        });

        MenuItem ConvertCoordinate = new MenuItem(message("ConvertCoordinate"));
        ConvertCoordinate.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ConvertCoordinateFxml);
        });

        MenuItem EpidemicReport = new MenuItem(message("EpidemicReport"));
        EpidemicReport.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.EpidemicReportsFxml);
        });

        Menu Location = new Menu(message("Location"));
        Location.getItems().addAll(
                GeographyCode, LocationInMap, ConvertCoordinate, new SeparatorMenuItem(),
                LocationData, EpidemicReport
        );

        MenuItem MatricesManage = new MenuItem(message("MatricesManage"));
        MatricesManage.setOnAction((ActionEvent event1) -> {
            MatricesManageController c = (MatricesManageController) loadScene(Fxmls.MatricesManageFxml);
            c.createAction();
        });

        MenuItem MatrixUnaryCalculation = new MenuItem(message("MatrixUnaryCalculation"));
        MatrixUnaryCalculation.setOnAction((ActionEvent event1) -> {
            MatrixUnaryCalculationController c = (MatrixUnaryCalculationController) loadScene(Fxmls.MatrixUnaryCalculationFxml);
            c.createAction();
        });

        MenuItem MatricesBinaryCalculation = new MenuItem(message("MatricesBinaryCalculation"));
        MatricesBinaryCalculation.setOnAction((ActionEvent event1) -> {
            MatricesBinaryCalculationController c = (MatricesBinaryCalculationController) loadScene(Fxmls.MatricesBinaryCalculationFxml);
            c.createAction();
        });

        Menu matrix = new Menu(message("Matrix"));
        matrix.getItems().addAll(
                MatricesManage, MatrixUnaryCalculation, MatricesBinaryCalculation
        );

        MenuItem DatabaseSQL = new MenuItem(message("DatabaseSQL"));
        DatabaseSQL.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DatabaseSqlFxml);
        });

        MenuItem DatabaseTable = new MenuItem(message("DatabaseTable"));
        DatabaseTable.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DataTablesFxml);
        });

        Menu database = new Menu(message("Database"));
        database.getItems().addAll(
                DatabaseTable, DatabaseSQL
        );

        MenuItem jshell = new MenuItem(message("JShell"));
        jshell.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.JShellFxml);
        });

        MenuItem jexl = new MenuItem(message("JEXL"));
        jexl.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.JexlFxml);
        });

        MenuItem JavaScript = new MenuItem("JavaScript");
        JavaScript.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.JavaScriptFxml);
        });

        Menu calculation = new Menu(message("ScriptAndExperssion"));
        calculation.getItems().addAll(
                jshell, jexl, JavaScript
        );

        MenuItem MathFunction = new MenuItem(message("MathFunction"));
        MathFunction.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MathFunctionFxml);
        });

        MenuItem barcodeCreator = new MenuItem(message("BarcodeCreator"));
        barcodeCreator.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.BarcodeCreatorFxml);
        });

        MenuItem barcodeDecoder = new MenuItem(message("BarcodeDecoder"));
        barcodeDecoder.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.BarcodeDecoderFxml);
        });

        MenuItem messageDigest = new MenuItem(message("MessageDigest"));
        messageDigest.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MessageDigestFxml);
        });

        MenuItem Base64Conversion = new MenuItem(message("Base64Conversion"));
        Base64Conversion.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.Base64Fxml);
        });

        MenuItem TTC2TTF = new MenuItem(message("TTC2TTF"));
        TTC2TTF.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FileTTC2TTFFxml);
        });

        Menu miscellaneousMenu = new Menu(message("Miscellaneous"));
        miscellaneousMenu.getItems().addAll(
                barcodeCreator, barcodeDecoder, new SeparatorMenuItem(),
                messageDigest, Base64Conversion, new SeparatorMenuItem(),
                TTC2TTF
        );

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                DataManufacture, ManageData, SpliceData, new SeparatorMenuItem(),
                DataFile, matrix, database, DataInSystemClipboard, DataInMyBoxClipboard, new SeparatorMenuItem(),
                calculation, MathFunction, new SeparatorMenuItem(),
                Location, new SeparatorMenuItem(),
                miscellaneousMenu
        );

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(dataBox, event);

        view.setImage(new Image("img/DataTools.png"));
        text.setText(message("DataToolsImageTips"));
        locateImage(dataBox, true);
    }

}
