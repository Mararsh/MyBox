package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Data extends MyBoxController_Network {

    @FXML
    public void popDataMenu(Event event) {
        if (UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true)) {
            showDataMenu(event);
        }
    }

    @FXML
    protected void showDataMenu(Event event) {
        MenuItem DataManufacture = new MenuItem(message("DataManufacture"));
        DataManufacture.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.Data2DManufactureFxml);
        });

        MenuItem ManageData = new MenuItem(message("ManageData"));
        ManageData.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.Data2DManageFxml);
        });

        MenuItem SpliceData = new MenuItem(message("SpliceData"));
        SpliceData.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.Data2DSpliceFxml);
        });

        MenuItem RowFilter = new MenuItem(message("RowFilter"));
        RowFilter.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.RowFilterFxml);
        });

        MenuItem Data2DDefinition = new MenuItem(message("Data2DDefinition"));
        Data2DDefinition.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.Data2DDefinitionFxml);
        });

        MenuItem DataInSystemClipboard = new MenuItem(message("DataInSystemClipboard"));
        DataInSystemClipboard.setOnAction((ActionEvent event1) -> {
            DataInSystemClipboardController.oneOpen();
        });

        MenuItem DataInMyBoxClipboard = new MenuItem(message("DataInMyBoxClipboard"));
        DataInMyBoxClipboard.setOnAction((ActionEvent event1) -> {
            DataInMyBoxClipboardController c = DataInMyBoxClipboardController.oneOpen();
            c.createAction();
        });

        MenuItem EditExcel = new MenuItem(message("EditExcel"));
        EditExcel.setOnAction((ActionEvent event1) -> {
            DataFileExcelController c = (DataFileExcelController) loadScene(Fxmls.DataFileExcelFxml);
            c.createAction();
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

        Menu dataFile = new Menu(message("DataFile"));
        dataFile.getItems().addAll(CsvConvert, CsvMerge, new SeparatorMenuItem(),
                ExcelConvert, ExcelMerge, new SeparatorMenuItem(),
                TextDataConvert, TextDataMerge);

        MenuItem GeographyCode = new MenuItem(message("GeographyCode"));
        GeographyCode.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.GeographyCodeFxml);
        });

        MenuItem LocationInMap = new MenuItem(message("LocationInMap"));
        LocationInMap.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.LocationInMapFxml);
        });

        MenuItem ConvertCoordinate = new MenuItem(message("ConvertCoordinate"));
        ConvertCoordinate.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ConvertCoordinateFxml);
        });

        Menu Location = new Menu(message("Location"));
        Location.getItems().addAll(
                GeographyCode, LocationInMap, ConvertCoordinate
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

        MenuItem databaseTableDefinition = new MenuItem(message("TableDefinition"));
        databaseTableDefinition.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DatabaseTableDefinitionFxml);
        });

        Menu database = new Menu(message("Database"));
        database.getItems().addAll(
                DatabaseTable, DatabaseSQL, databaseTableDefinition
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
                JavaScript, jshell, jexl
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

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(
                DataManufacture, dataFile, matrix, database, new SeparatorMenuItem(),
                ManageData, SpliceData, Data2DDefinition, RowFilter,
                DataInSystemClipboard, DataInMyBoxClipboard, new SeparatorMenuItem(),
                calculation, MathFunction, new SeparatorMenuItem(),
                Location, miscellaneousMenu));

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("MyBoxHomeMenuPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        popCenterMenu(dataBox, items);

    }

}
