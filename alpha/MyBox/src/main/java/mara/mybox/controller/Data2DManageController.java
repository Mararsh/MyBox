package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import mara.mybox.data.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-16
 * @License Apache License Version 2.0
 */
public class Data2DManageController extends Data2DListController {

    public Data2DManageController() {
        baseTitle = message("ManageData");
        TipsLabelKey = "DataManageTips";
    }

    @Override
    public void setQueryConditions() {
        queryConditions = " data_type != " + Data2D.type(Data2DDefinition.Type.InternalTable);
    }

    @FXML
    public void queryAction() {
        Data2DManageQueryController.open(this);
    }

    @FXML
    public void popOpen(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem("CSV", StyleTools.getIconImage("iconCSV.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDefinition.openType(Data2DDefinition.Type.CSV);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem("Excel", StyleTools.getIconImage("iconExcel.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDefinition.openType(Data2DDefinition.Type.Excel);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Texts"), StyleTools.getIconImage("iconTxt.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDefinition.openType(Data2DDefinition.Type.Texts);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Matrix"), StyleTools.getIconImage("iconMatrix.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDefinition.openType(Data2DDefinition.Type.Matrix);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("DatabaseTable"), StyleTools.getIconImage("iconDatabase.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDefinition.openType(Data2DDefinition.Type.DatabaseTable);
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("MyBoxClipboard"), StyleTools.getIconImage("iconClipboard.png"));
            menu.setOnAction((ActionEvent event) -> {
                Data2DDefinition.openType(Data2DDefinition.Type.MyBoxClipboard);
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        static
     */
    public static Data2DManageController oneOpen() {
        Data2DManageController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof Data2DManageController) {
                try {
                    controller = (Data2DManageController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (Data2DManageController) WindowTools.openStage(Fxmls.Data2DManageFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static Data2DManageController open(Data2DDefinition def) {
        Data2DManageController controller = oneOpen();
        controller.load(def);
        return controller;
    }

}
