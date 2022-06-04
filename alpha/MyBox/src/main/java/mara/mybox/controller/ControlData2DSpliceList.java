package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-5-22
 * @License Apache License Version 2.0
 */
public class ControlData2DSpliceList extends ControlData2DList {

    protected Data2DSpliceController spliceController;

    @FXML
    protected Button editDataButton, dataAButton, dataBButton;

    public ControlData2DSpliceList() {
    }

    @Override
    public void setParameters(BaseData2DController manageController) {
        try {
            this.spliceController = (Data2DSpliceController) manageController;
            super.setParameters(manageController);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void itemClicked() {
    }

    @Override
    public void itemDoubleClicked() {
        editAction();
    }

    @FXML
    @Override
    public void editAction() {
        try {
            Data2DDefinition selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            Data2DDefinition.open(selected);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        boolean isEmpty = tableData == null || tableData.isEmpty();
        boolean none = isEmpty || tableView.getSelectionModel().getSelectedItem() == null;
        editDataButton.setDisable(none);
        dataAButton.setDisable(none);
        dataBButton.setDisable(none);
    }

    @FXML
    public void dataAAction() {
        try {
            Data2DDefinition selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            spliceController.dataAController.loadDef(selected);
            spliceController.dataAController.setLabel(selected.displayName());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void dataBAction() {
        try {
            Data2DDefinition selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            spliceController.dataBController.loadDef(selected);
            spliceController.dataBController.setLabel(selected.displayName());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("SetAsDataA"), StyleTools.getIconImage("iconA.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                dataAAction();
            });
            menu.setDisable(dataAButton.isDisable());
            items.add(menu);

            menu = new MenuItem(message("SetAsDataB"), StyleTools.getIconImage("iconB.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                dataBAction();
            });
            menu.setDisable(dataBButton.isDisable());
            items.add(menu);

            items.add(new SeparatorMenuItem());

            items.addAll(super.makeTableContextMenu());
            return items;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public void cleanPane() {
        try {
            manageController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
