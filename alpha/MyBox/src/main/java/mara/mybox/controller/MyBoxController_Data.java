package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Data extends MyBoxController_Network {

    @FXML
    protected void showDataMenu(MouseEvent event) {
        hideMenu(event);

        MenuItem EditExcel = new MenuItem(Languages.message("EditExcel"));
        EditExcel.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DataFileExcelFxml);
        });

        MenuItem ExcelConvert = new MenuItem(Languages.message("ExcelConvert"));
        ExcelConvert.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DataFileExcelConvertFxml);
        });

        MenuItem ExtractTextsFromMS = new MenuItem(Languages.message("ExtractTextsFromMS"));
        ExtractTextsFromMS.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ExtractTextsFromMSFxml);
        });

        MenuItem ExcelMerge = new MenuItem(Languages.message("ExcelMerge"));
        ExcelMerge.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DataFileExcelMergeFxml);
        });

        MenuItem EditCSV = new MenuItem(Languages.message("EditCSV"));
        EditCSV.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DataFileCSVFxml);
        });

        MenuItem CsvConvert = new MenuItem(Languages.message("CsvConvert"));
        CsvConvert.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DataFileCSVConvertFxml);
        });

        MenuItem CsvMerge = new MenuItem(Languages.message("CsvMerge"));
        CsvMerge.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DataFileCSVMergeFxml);
        });

        Menu DataFile = new Menu(Languages.message("DataFile"));
        DataFile.getItems().addAll(
                EditCSV, CsvConvert, CsvMerge, new SeparatorMenuItem(),
                EditExcel, ExcelConvert, ExcelMerge, ExtractTextsFromMS
        );

        MenuItem DataClipboard = new MenuItem(Languages.message("DataClipboard"));
        DataClipboard.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DataClipboardFxml);
        });

        MenuItem Dataset = new MenuItem(Languages.message("Dataset"));
        Dataset.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.DatasetFxml);
        });

        MenuItem GeographyCode = new MenuItem(Languages.message("GeographyCode"));
        GeographyCode.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.GeographyCodeFxml);
        });

        MenuItem LocationInMap = new MenuItem(Languages.message("LocationInMap"));
        LocationInMap.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.LocationInMapFxml);
        });

        MenuItem LocationData = new MenuItem(Languages.message("LocationData"));
        LocationData.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.LocationDataFxml);
        });

        MenuItem ConvertCoordinate = new MenuItem(Languages.message("ConvertCoordinate"));
        ConvertCoordinate.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ConvertCoordinateFxml);
        });

        MenuItem LocationsDataInMap = new MenuItem(Languages.message("LocationsDataInMap"));
        LocationsDataInMap.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.LocationsDataInMapFxml);
        });

        Menu locationApplicationsMenu = new Menu(Languages.message("LocationApplications"));
        locationApplicationsMenu.getItems().addAll(
                LocationData, LocationsDataInMap
        );

        MenuItem EpidemicReport = new MenuItem(Languages.message("EpidemicReport"));
        EpidemicReport.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.EpidemicReportsFxml);
        });

        MenuItem MatricesManage = new MenuItem(Languages.message("MatricesManage"));
        MatricesManage.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MatricesManageFxml);
        });

        MenuItem MatrixUnaryCalculation = new MenuItem(Languages.message("MatrixUnaryCalculation"));
        MatrixUnaryCalculation.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MatrixUnaryCalculationFxml);
        });

        MenuItem MatricesBinaryCalculation = new MenuItem(Languages.message("MatricesBinaryCalculation"));
        MatricesBinaryCalculation.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MatricesBinaryCalculationFxml);
        });

        MenuItem barcodeCreator = new MenuItem(Languages.message("BarcodeCreator"));
        barcodeCreator.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.BarcodeCreatorFxml);
        });

        MenuItem barcodeDecoder = new MenuItem(Languages.message("BarcodeDecoder"));
        barcodeDecoder.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.BarcodeDecoderFxml);
        });

        MenuItem messageDigest = new MenuItem(Languages.message("MessageDigest"));
        messageDigest.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MessageDigestFxml);
        });

        MenuItem Base64Conversion = new MenuItem(Languages.message("Base64Conversion"));
        Base64Conversion.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.Base64Fxml);
        });

        MenuItem TTC2TTF = new MenuItem(Languages.message("TTC2TTF"));
        TTC2TTF.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FileTTC2TTFFxml);
        });

        Menu miscellaneousMenu = new Menu(Languages.message("Miscellaneous"));
        miscellaneousMenu.getItems().addAll(
                barcodeCreator, barcodeDecoder, new SeparatorMenuItem(),
                messageDigest, Base64Conversion, new SeparatorMenuItem(),
                TTC2TTF
        );

        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.getItems().addAll(
                DataFile, DataClipboard, new SeparatorMenuItem(),
                MatricesManage, MatrixUnaryCalculation, MatricesBinaryCalculation, new SeparatorMenuItem(),
                GeographyCode, LocationInMap, LocationData, ConvertCoordinate, new SeparatorMenuItem(),
                EpidemicReport, new SeparatorMenuItem(),
                miscellaneousMenu
        );

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem closeMenu = new MenuItem(Languages.message("PopupClose"));
        closeMenu.setStyle("-fx-text-fill: #2e598a;");
        closeMenu.setOnAction((ActionEvent cevent) -> {
            popMenu.hide();
            popMenu = null;
        });
        popMenu.getItems().add(closeMenu);

        showMenu(dataBox, event);

        view.setImage(new Image("img/DataTools.png"));
        text.setText(Languages.message("DataToolsImageTips"));
        locateImage(dataBox, true);
    }

}
