package mara.mybox.fxml.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.DataInMyBoxClipboardController;
import mara.mybox.controller.DataInSystemClipboardController;
import mara.mybox.controller.DataTreeController;
import mara.mybox.controller.GeographyCodeController;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-12-9
 * @License Apache License Version 2.0
 */
public class DataToolsMenu {

    public static List<MenuItem> menusList(BaseController controller) {
        MenuItem DataManufacture = new MenuItem(message("DataManufacture"));
        DataManufacture.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.Data2DManufactureFxml);
        });

        MenuItem ManageData = new MenuItem(message("ManageData"));
        ManageData.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.Data2DManageFxml);
        });

        MenuItem SpliceData = new MenuItem(message("SpliceData"));
        SpliceData.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.Data2DSpliceFxml);
        });

        MenuItem RowExpression = new MenuItem(message("RowExpression"));
        RowExpression.setOnAction((ActionEvent event) -> {
            DataTreeController.rowExpression(controller, true);
        });

        MenuItem DataColumn = new MenuItem(message("DataColumn"));
        DataColumn.setOnAction((ActionEvent event) -> {
            DataTreeController.dataColumn(controller, true);
        });

        MenuItem DataInSystemClipboard = new MenuItem(message("DataInSystemClipboard"));
        DataInSystemClipboard.setOnAction((ActionEvent event) -> {
            DataInSystemClipboardController.oneOpen();
        });

        MenuItem DataInMyBoxClipboard = new MenuItem(message("DataInMyBoxClipboard"));
        DataInMyBoxClipboard.setOnAction((ActionEvent event) -> {
            DataInMyBoxClipboardController c = DataInMyBoxClipboardController.oneOpen();
        });

        MenuItem ExcelConvert = new MenuItem(message("ExcelConvert"));
        ExcelConvert.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.DataFileExcelConvertFxml);
        });

        MenuItem ExcelMerge = new MenuItem(message("ExcelMerge"));
        ExcelMerge.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.DataFileExcelMergeFxml);
        });

        MenuItem CsvConvert = new MenuItem(message("CsvConvert"));
        CsvConvert.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.DataFileCSVConvertFxml);
        });

        MenuItem CsvMerge = new MenuItem(message("CsvMerge"));
        CsvMerge.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.DataFileCSVMergeFxml);
        });

        MenuItem TextDataConvert = new MenuItem(message("TextDataConvert"));
        TextDataConvert.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.DataFileTextConvertFxml);
        });

        MenuItem TextDataMerge = new MenuItem(message("TextDataMerge"));
        TextDataMerge.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.DataFileTextMergeFxml);
        });

        Menu dataFile = new Menu(message("DataFile"));
        dataFile.getItems().addAll(CsvConvert, CsvMerge, new SeparatorMenuItem(),
                ExcelConvert, ExcelMerge, new SeparatorMenuItem(),
                TextDataConvert, TextDataMerge);

        MenuItem GeographyCode = new MenuItem(message("GeographyCode"));
        GeographyCode.setOnAction((ActionEvent event) -> {
            GeographyCodeController.open(controller, true, true);
        });

        MenuItem LocationInMap = new MenuItem(message("LocationInMap"));
        LocationInMap.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.LocationInMapFxml);
        });

        MenuItem ConvertCoordinate = new MenuItem(message("ConvertCoordinate"));
        ConvertCoordinate.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ConvertCoordinateFxml);
        });

        Menu Location = new Menu(message("Location"));
        Location.getItems().addAll(
                GeographyCode, LocationInMap, ConvertCoordinate
        );

        MenuItem MatricesManage = new MenuItem(message("MatricesManage"));
        MatricesManage.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.MatricesManageFxml);
        });

        MenuItem MatrixUnaryCalculation = new MenuItem(message("MatrixUnaryCalculation"));
        MatrixUnaryCalculation.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.MatrixUnaryCalculationFxml);
        });

        MenuItem MatricesBinaryCalculation = new MenuItem(message("MatricesBinaryCalculation"));
        MatricesBinaryCalculation.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.MatricesBinaryCalculationFxml);
        });

        Menu matrix = new Menu(message("Matrix"));
        matrix.getItems().addAll(
                MatricesManage, MatrixUnaryCalculation, MatricesBinaryCalculation
        );

        MenuItem DatabaseSQL = new MenuItem(message("DatabaseSQL"));
        DatabaseSQL.setOnAction((ActionEvent event) -> {
            DataTreeController.sql(controller, true);
        });

        MenuItem DatabaseTable = new MenuItem(message("DatabaseTable"));
        DatabaseTable.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.DataTablesFxml);
        });

        MenuItem databaseTableDefinition = new MenuItem(message("TableDefinition"));
        databaseTableDefinition.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.DatabaseTableDefinitionFxml);
        });

        Menu database = new Menu(message("Database"));
        database.getItems().addAll(
                DatabaseTable, DatabaseSQL, databaseTableDefinition
        );

        MenuItem jshell = new MenuItem(message("JShell"));
        jshell.setOnAction((ActionEvent event) -> {
            DataTreeController.jShell(controller, true);
        });

        MenuItem jexl = new MenuItem(message("JEXL"));
        jexl.setOnAction((ActionEvent event) -> {
            DataTreeController.jexl(controller, true);
        });

        MenuItem JavaScript = new MenuItem("JavaScript");
        JavaScript.setOnAction((ActionEvent event) -> {
            DataTreeController.javascript(controller, true);
        });

        Menu calculation = new Menu(message("ScriptAndExperssion"));
        calculation.getItems().addAll(
                JavaScript, jshell, jexl
        );

        MenuItem MathFunction = new MenuItem(message("MathFunction"));
        MathFunction.setOnAction((ActionEvent event) -> {
            DataTreeController.mathFunction(controller, true);
        });

        MenuItem barcodeCreator = new MenuItem(message("BarcodeCreator"));
        barcodeCreator.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.BarcodeCreatorFxml);
        });

        MenuItem barcodeDecoder = new MenuItem(message("BarcodeDecoder"));
        barcodeDecoder.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.BarcodeDecoderFxml);
        });

        MenuItem messageDigest = new MenuItem(message("MessageDigest"));
        messageDigest.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.MessageDigestFxml);
        });

        MenuItem Base64Conversion = new MenuItem(message("Base64Conversion"));
        Base64Conversion.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.Base64Fxml);
        });

        MenuItem TTC2TTF = new MenuItem(message("TTC2TTF"));
        TTC2TTF.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FileTTC2TTFFxml);
        });

        Menu miscellaneousMenu = new Menu(message("Miscellaneous"));
        miscellaneousMenu.getItems().addAll(
                barcodeCreator, barcodeDecoder, new SeparatorMenuItem(),
                messageDigest, Base64Conversion, new SeparatorMenuItem(),
                TTC2TTF
        );

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(
                DataManufacture, ManageData, new SeparatorMenuItem(),
                dataFile, matrix, database, new SeparatorMenuItem(),
                SpliceData, DataColumn, RowExpression,
                DataInSystemClipboard, DataInMyBoxClipboard, new SeparatorMenuItem(),
                calculation, MathFunction, new SeparatorMenuItem(),
                Location, miscellaneousMenu));

        return items;

    }

}
