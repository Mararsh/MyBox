package mara.mybox.data2d.example;

import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import mara.mybox.controller.BaseData2DLoadController;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-15
 * @License Apache License Version 2.0
 */
public class Matrix {

    public static Menu menu(String lang, BaseData2DLoadController controller) {
        try {
            Menu locationMenu = new Menu(message(lang, "Matrix"),
                    StyleTools.getIconImageView("iconMatrix.png"));

            short scale1 = 4, scale2 = 8;
            int max = 100000;
            MenuItem menu = new MenuItem(message(lang, "DoubleMatrix") + " 10*10");
            menu.setOnAction((ActionEvent event) -> {
                DataMatrix matrix = DataMatrix.makeMatrix("Double", 10, 10, scale1, max);
                controller.loadDef(matrix);
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "DoubleMatrix") + " "
                    + message(lang, "PureDecimal") + " 10*10");
            menu.setOnAction((ActionEvent event) -> {
                DataMatrix matrix = DataMatrix.makeMatrix("Double", 10, 10, scale2, 1);
                controller.loadDef(matrix);
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "DoubleMatrix") + " 50*50");
            menu.setOnAction((ActionEvent event) -> {
                DataMatrix matrix = DataMatrix.makeMatrix("Double", 50, 50, scale1, max);
                controller.loadDef(matrix);
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "DoubleMatrix") + " "
                    + message(lang, "PureDecimal") + " 50*50");
            menu.setOnAction((ActionEvent event) -> {
                DataMatrix matrix = DataMatrix.makeMatrix("Double", 50, 50, scale2, 1);
                controller.loadDef(matrix);
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "DoubleMatrix") + " 500*500");
            menu.setOnAction((ActionEvent event) -> {
                DataMatrix matrix = DataMatrix.makeMatrix("Double", 500, 500, scale1, max);
                controller.loadDef(matrix);
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "DoubleMatrix") + " 1000*1000");
            menu.setOnAction((ActionEvent event) -> {
                DataMatrix matrix = DataMatrix.makeMatrix("Double", 1000, 1000, scale1, max);
                controller.loadDef(matrix);
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "FloatMatrix") + " 50*50");
            menu.setOnAction((ActionEvent event) -> {
                DataMatrix matrix = DataMatrix.makeMatrix("Float", 50, 50, scale1, max);
                controller.loadDef(matrix);
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "FloatMatrix") + " "
                    + message(lang, "PureDecimal") + " 50*50");
            menu.setOnAction((ActionEvent event) -> {
                DataMatrix matrix = DataMatrix.makeMatrix("Float", 50, 50, scale2, 1);
                controller.loadDef(matrix);
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "IntegerMatrix") + " 50*50");
            menu.setOnAction((ActionEvent event) -> {
                DataMatrix matrix = DataMatrix.makeMatrix("Integer", 50, 50, scale1, max);
                controller.loadDef(matrix);
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "BooleanMatrix") + " 50*50");
            menu.setOnAction((ActionEvent event) -> {
                DataMatrix matrix = DataMatrix.makeMatrix("NumberBoolean", 50, 50, scale1, max);
                controller.loadDef(matrix);
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
}
