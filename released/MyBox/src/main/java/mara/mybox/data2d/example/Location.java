package mara.mybox.data2d.example;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import mara.mybox.controller.BaseData2DLoadController;
import mara.mybox.data2d.DataFileCSV;
import static mara.mybox.data2d.example.Data2DExampleTools.makeExampleFile;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-15
 * @License Apache License Version 2.0
 */
public class Location {

    public static Menu menu(String lang, BaseData2DLoadController controller) {
        try {
            Menu locationMenu = new Menu(message(lang, "LocationData"),
                    StyleTools.getIconImageView("iconLocation.png"));

            MenuItem menu = new MenuItem(message(lang, "ChineseHistoricalCapitals"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseHistoricalCapitals(lang);
                if (makeExampleFile("Location_ChineseHistoricalCapitals_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "AutumnMovementPatternsOfEuropeanGadwalls"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = AutumnMovementPatternsOfEuropeanGadwalls(lang);
                if (makeExampleFile("Location_EuropeanGadwalls", data)) {
                    controller.loadDef(data);
                }
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "SpermWhalesGulfOfMexico"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = SpermWhalesGulfOfMexico(lang);
                if (makeExampleFile("Location_SpermWhales", data)) {
                    controller.loadDef(data);
                }
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "EpidemicReportsCOVID19"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = EpidemicReportsCOVID19(lang);
                if (makeExampleFile("Location_EpidemicReports", data)) {
                    controller.loadDef(data);
                }
            });
            locationMenu.getItems().add(menu);

            return locationMenu;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        exmaples of data 2D definition
     */
    public static DataFileCSV ChineseHistoricalCapitals(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "Country"), ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(message(lang, "Capital"), ColumnDefinition.ColumnType.String, true).setWidth(200));
        columns.add(new Data2DColumn(message(lang, "Longitude"), ColumnDefinition.ColumnType.Longitude));
        columns.add(new Data2DColumn(message(lang, "Latitude"), ColumnDefinition.ColumnType.Latitude));
        columns.add(new Data2DColumn(message(lang, "StartTime"), ColumnDefinition.ColumnType.Era).setFormat(isChinese ? "Gy" : "y G"));
        columns.add(new Data2DColumn(message(lang, "EndTime"), ColumnDefinition.ColumnType.Era).setFormat(isChinese ? "Gy" : "y G"));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(message(lang, "ChineseHistoricalCapitals"));
        return data;
    }

    public static DataFileCSV AutumnMovementPatternsOfEuropeanGadwalls(String lang) {
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "StartTime"), ColumnDefinition.ColumnType.Datetime, 180).setFixTwoDigitYear(true));
        columns.add(new Data2DColumn(message(lang, "EndTime"), ColumnDefinition.ColumnType.Datetime, 180).setFixTwoDigitYear(true));
        columns.add(new Data2DColumn(message(lang, "Longitude"), ColumnDefinition.ColumnType.Longitude));
        columns.add(new Data2DColumn(message(lang, "Latitude"), ColumnDefinition.ColumnType.Latitude));
        columns.add(new Data2DColumn(message(lang, "CoordinateSystem"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(message(lang, "AutumnMovementPatternsOfEuropeanGadwalls"))
                .setComments("https://www.datarepository.movebank.org/handle/10255/move.346");
        return data;
    }

    public static DataFileCSV SpermWhalesGulfOfMexico(String lang) {
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "Time"), ColumnDefinition.ColumnType.Datetime, 180).setFixTwoDigitYear(true));
        columns.add(new Data2DColumn(message(lang, "Longitude"), ColumnDefinition.ColumnType.Longitude));
        columns.add(new Data2DColumn(message(lang, "Latitude"), ColumnDefinition.ColumnType.Latitude));
        columns.add(new Data2DColumn(message(lang, "CoordinateSystem"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(message(lang, "SpermWhalesGulfOfMexico"))
                .setComments("https://www.datarepository.movebank.org/handle/10255/move.1059");
        return data;
    }

    public static DataFileCSV EpidemicReportsCOVID19(String lang) {
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "Date"), ColumnDefinition.ColumnType.Date)
                .setFixTwoDigitYear(false).setFormat("yyyy-MM-dd"));
        columns.add(new Data2DColumn(message(lang, "Country"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Province"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Confirmed"), ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(message(lang, "Healed"), ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(message(lang, "Dead"), ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(message(lang, "Longitude"), ColumnDefinition.ColumnType.Longitude));
        columns.add(new Data2DColumn(message(lang, "Latitude"), ColumnDefinition.ColumnType.Latitude));
        data.setColumns(columns).setDataName(message(lang, "EpidemicReportsCOVID19"))
                .setComments("https://github.com/CSSEGISandData/COVID-19");
        return data;
    }

}
