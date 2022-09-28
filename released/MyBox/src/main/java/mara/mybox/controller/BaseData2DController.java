package mara.mybox.controller;

import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2DTools;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-2-21
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DController extends BaseController {

    protected Data2D.Type type;
    protected TableData2DDefinition tableData2DDefinition;
    protected Data2D data2D;

    @FXML
    protected ControlData2DList listController;
    @FXML
    protected ControlData2D dataController;
    @FXML
    protected ControlData2DLoad loadController;
    @FXML
    protected Label nameLabel;

    public BaseData2DController() {
        type = Data2DDefinition.Type.Texts;
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            initData();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void initData() {
        try {
            setDataType(type);

            if (listController != null) {
                listController.setParameters(this);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // subclass should call this
    public void setDataType(Data2D.Type type) {
        try {
            this.type = type;
            if (dataController != null) {
                dataController.setDataType(this, type);
                loadController = dataController.editController.tableController;

            } else if (loadController != null) {
                loadController.setData(Data2D.create(type));

            }

            tableData2DDefinition = loadController.tableData2DDefinition;
            data2D = loadController.data2D;

            loadController.dataLabel = nameLabel;
            loadController.baseTitle = baseTitle;
            checkButtons();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (dataController != null) {
                dataController.setParameters(this);
            }

            if (listController != null) {
                rightPaneControl = listController.rightPaneControl;
                initRightPaneControl();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkButtons() {
        if (saveButton != null) {
            saveButton.setDisable(loadController.data2D == null || !loadController.data2D.isValid());
        }
        if (recoverButton != null) {
            recoverButton.setDisable(loadController.data2D == null || loadController.data2D.isTmpData());
        }
    }

    public void loadDef(Data2DDefinition def) {
        if (loadController == null || !checkBeforeNextAction()) {
            return;
        }
        loadController.loadDef(def);
        checkButtons();
    }

    public void loadCSVData(DataFileCSV csvData) {
        if (csvData == null || loadController == null || !checkBeforeNextAction()) {
            return;
        }
        loadController.loadCSVData(csvData);
    }

    public void loadTableData(DataTable dataTable) {
        if (dataTable == null || loadController == null || !checkBeforeNextAction()) {
            return;
        }
        loadController.loadTableData(dataTable);
    }

    public void loadData(List<Data2DColumn> cols, List<List<String>> data) {
        if (loadController == null || !checkBeforeNextAction()) {
            return;
        }
        loadController.loadTmpData(null, cols, data);
        checkButtons();
    }

    @FXML
    @Override
    public void createAction() {
        if (dataController == null) {
            return;
        }
        dataController.create();
    }

    @FXML
    @Override
    public void recoverAction() {
        if (dataController == null) {
            return;
        }
        dataController.recover();
    }

    @FXML
    public void refreshAction() {
        if (listController == null) {
            return;
        }
        listController.refreshAction();
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        if (dataController == null) {
            return;
        }
        dataController.loadContentInSystemClipboard();
    }

    @FXML
    public void editAction() {
        Data2DDefinition.open(loadController.data2D);
    }

    @FXML
    @Override
    public void saveAction() {
        if (dataController == null) {
            return;
        }
        dataController.save();
    }

    @FXML
    protected void popExamplesMenu(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean("Data2DExamplesPopWhenMouseHovering", true)) {
            examplesMenu(mouseEvent);
        }
    }

    @FXML
    protected void showExamplesMenu(ActionEvent event) {
        examplesMenu(event);
    }

    @FXML
    protected void examplesMenu(Event event) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            popMenu.getItems().add(new SeparatorMenuItem());

            popMenu.getItems().addAll(Data2DTools.examplesMenu(dataController));

            popMenu.getItems().add(new SeparatorMenuItem());

            CheckMenuItem pMenu = new CheckMenuItem(message("PopWhenMouseHovering"), StyleTools.getIconImage("iconPop.png"));
            pMenu.setSelected(UserConfig.getBoolean("Data2DExamplesPopWhenMouseHovering", true));
            pMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("Data2DExamplesPopWhenMouseHovering", pMenu.isSelected());
                }
            });
            popMenu.getItems().add(pMenu);

            MenuItem menu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) event.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (dataController != null) {
            return dataController.checkBeforeNextAction();
        } else {
            return true;
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (dataController != null) {
                return dataController.keyEventsFilter(event);
            } else if (loadController != null) {
                return loadController.keyEventsFilter(event);
            }
        }
        return true;
    }

    @Override
    public void myBoxClipBoard() {
        if (dataController != null) {
            dataController.myBoxClipBoard();
        } else if (loadController != null) {
            loadController.myBoxClipBoard();
        }

    }

    @Override
    public void cleanPane() {
        try {
            dataController = null;
            loadController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
