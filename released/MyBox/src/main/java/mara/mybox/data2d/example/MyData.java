package mara.mybox.data2d.example;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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
public class MyData {

    public static Menu menu(String lang, BaseData2DLoadController controller) {
        try {
            Menu myMenu = new Menu(message(lang, "MyData"), StyleTools.getIconImageView("iconCat.png"));

            MenuItem menu = new MenuItem(message(lang, "Notes"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Notes(lang);
                if (makeExampleFile("MyData_notes_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "Contacts"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Contacts(lang);
                if (makeExampleFile("MyData_contacts_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "CashFlow"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = CashFlow(lang);
                if (makeExampleFile("MyData_cashflow_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "PrivateProperty"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = PrivateProperty(lang);
                if (makeExampleFile("MyData_property_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            myMenu.getItems().add(menu);

            myMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message(lang, "Eyesight"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Eyesight(lang);
                if (makeExampleFile("MyData_eyesight", data)) {
                    controller.loadDef(data);
                }
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "Weight"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Weight(lang);
                if (makeExampleFile("MyData_weight", data)) {
                    controller.loadDef(data);
                }
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "Height"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Height(lang);
                if (makeExampleFile("MyData_height", data)) {
                    controller.loadDef(data);
                }
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "Menstruation"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Menstruation(lang);
                if (makeExampleFile("MyData_menstruation", data)) {
                    controller.loadDef(data);
                }
            });
            myMenu.getItems().add(menu);

            return myMenu;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        exmaples of data 2D definition
     */
    public static DataFileCSV Notes(String lang) {
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "Time"), ColumnDefinition.ColumnType.Datetime, 180));
        columns.add(new Data2DColumn(message(lang, "Title"), ColumnDefinition.ColumnType.String, 180));
        columns.add(new Data2DColumn(message(lang, "InvolvedObjects"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Location"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String, 300));
        data.setColumns(columns).setDataName(message(lang, "Notes"));
        return data;
    }

    public static DataFileCSV Contacts(String lang) {
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "Name"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Relationship"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "PhoneNumber") + "1", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "PhoneNumber") + "2", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Email"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Address"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Birthday"), ColumnDefinition.ColumnType.Datetime));
        columns.add(new Data2DColumn(message(lang, "Hobbies"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String, 300));
        data.setColumns(columns).setDataName(message(lang, "Contacts"));
        return data;
    }

    public static DataFileCSV CashFlow(String lang) {
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "Time"), ColumnDefinition.ColumnType.Datetime, 180));
        columns.add(new Data2DColumn(message(lang, "Amount"), ColumnDefinition.ColumnType.Float));
        columns.add(new Data2DColumn(message(lang, "Type"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Account"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "InvolvedObjects"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String, 300));
        data.setColumns(columns).setDataName(message(lang, "CashFlow"));
        return data;
    }

    public static DataFileCSV PrivateProperty(String lang) {
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "Time"), ColumnDefinition.ColumnType.Datetime, 180));
        columns.add(new Data2DColumn(message(lang, "Amount"), ColumnDefinition.ColumnType.Float));
        columns.add(new Data2DColumn(message(lang, "Type"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Account"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String, 300));
        data.setColumns(columns).setDataName(message(lang, "PrivateProperty"));
        return data;
    }

    public static DataFileCSV Eyesight(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "Time"), ColumnDefinition.ColumnType.Datetime, true, true).setWidth(180));
        columns.add(new Data2DColumn(isChinese ? "左眼" : "left eye", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "右眼" : "right eye", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "基弧" : "Radian", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String, 300));
        data.setColumns(columns).setDataName(message(lang, "Eyesight"));
        return data;
    }

    public static DataFileCSV Weight(String lang) {
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "Time"), ColumnDefinition.ColumnType.Datetime, true, true).setWidth(180));
        columns.add(new Data2DColumn(message(lang, "Weight") + "(kg)", ColumnDefinition.ColumnType.Float).setScale(2));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String, 300));
        data.setColumns(columns).setDataName(message(lang, "Weight"));
        return data;
    }

    public static DataFileCSV Height(String lang) {
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "Time"), ColumnDefinition.ColumnType.Datetime, true, true).setWidth(180));
        columns.add(new Data2DColumn(message(lang, "Height") + "(cm)", ColumnDefinition.ColumnType.Float).setScale(2));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String, 300));
        data.setColumns(columns).setDataName(message(lang, "Height"));
        return data;
    }

    public static DataFileCSV Menstruation(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "StartTime"), ColumnDefinition.ColumnType.Datetime, true).setWidth(180));
        columns.add(new Data2DColumn(message(lang, "EndTime"), ColumnDefinition.ColumnType.Datetime, true).setWidth(180));
        columns.add(new Data2DColumn(isChinese ? "疼痛" : "Pain", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "卫生巾" : "Pads", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String, 300));
        data.setColumns(columns).setDataName(message(lang, "Menstruation"));
        return data;
    }

}
