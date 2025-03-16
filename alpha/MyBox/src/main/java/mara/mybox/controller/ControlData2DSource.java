package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DSource extends BaseData2DRowsColumnsController {

    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        try {
            if (data2D == null || !data2D.isDataFile()) {
                return null;
            }
            sourceFile = data2D.getFile();
            if (sourceFile == null) {
                return null;
            }
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (data2D.isExcel()) {
                menu = new MenuItem(message("Sheet"), StyleTools.getIconImageView("iconFrame.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    DataFileExcelSheetsController.open(this);
                });
                items.add(menu);
            }

            menu = new MenuItem(message("Format"), StyleTools.getIconImageView("iconFormat.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                if (data2D.isCSV()) {
                    DataFileCSVFormatController.open(this);
                } else if (data2D.isTexts() || data2D.isMatrix()) {
                    DataFileTextFormatController.open(this);
                } else if (data2D.isExcel()) {
                    DataFileExcelFormatController.open(this);
                }
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            if (data2D.isTexts() || data2D.isCSV() || data2D.isMatrix()) {
                menu = new MenuItem(message("Texts"), StyleTools.getIconImageView("iconTxt.png"));
                menu.setOnAction((ActionEvent event) -> {
                    editTextFile();
                });
                items.add(menu);
            }

            menu = new MenuItem(message("OpenDirectory"), StyleTools.getIconImageView("iconOpenPath.png"));
            menu.setOnAction((ActionEvent event) -> {
                openSourcePath();
            });
            items.add(menu);

            menu = new MenuItem(message("BrowseFiles"), StyleTools.getIconImageView("iconList.png"));
            menu.setOnAction((ActionEvent event) -> {
                FileBrowseController.open(this);
            });
            items.add(menu);

            menu = new MenuItem(message("SystemMethod"), StyleTools.getIconImageView("iconSystemOpen.png"));
            menu.setOnAction((ActionEvent event) -> {
                systemMethod();
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
