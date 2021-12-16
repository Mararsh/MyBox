package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Window;
import mara.mybox.data.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-16
 * @License Apache License Version 2.0
 */
public class Data2DManufactureController extends BaseController {

    protected Data2D data2D;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;

    @FXML
    protected ControlData2DTable tableController;
    @FXML
    protected ControlData2D dataController;
    @FXML
    protected Label dataNameLabel;

    public Data2DManufactureController() {
        baseTitle = Languages.message("ManufactureData");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            dataController.setDataType(this, Data2DDefinition.Type.Table);
            data2D = dataController.data2D;
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;

            tableController.setParameters(this);

            dataController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    checkStatus();
                }
            });

            dataController.savedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    checkStatus();
                }
            });

            checkStatus();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkStatus() {
        try {
            boolean changed = dataController.isChanged();
            if (myStage != null) {
                String title = baseTitle;
                if (!data2D.isTmpFile()) {
                    title += " " + data2D.getFile().getAbsolutePath();
                }
                if (changed) {
                    title += " *";
                }
                myStage.setTitle(title);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void load(Data2DDefinition source) {
        try {
            if (source == null) {
                return;
            }
            Data2D data = Data2D.create(source.getType());
            data.cloneAll(source);
            String name;
            if (data.getFile() != null) {
                name = data.getFile().getAbsolutePath();
            } else {
                name = data.getDataName();
            }
            if (name == null) {
                name = message("NewData");
            }
            dataNameLabel.setText(message(data.getType().name()) + " - "
                    + (data.getD2did() >= 0 ? data.getD2did() + " - " : "")
                    + name);
            dataController.setData(data);
            dataController.readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void createAction() {
        dataController.create();
    }

    @FXML
    @Override
    public void recoverAction() {
        dataController.recover();
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        dataController.loadContentInSystemClipboard();
    }

    @FXML
    @Override
    public void saveAction() {
        dataController.save();
    }

    @Override
    public boolean checkBeforeNextAction() {
        return dataController.checkBeforeNextAction();
    }

    /*
        static
     */
    public static Data2DManufactureController oneOpen() {
        Data2DManufactureController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof Data2DManufactureController) {
                try {
                    controller = (Data2DManufactureController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (Data2DManufactureController) WindowTools.openStage(Fxmls.Data2DManufactureFxml);
        }
        return controller;
    }

    public static Data2DManufactureController open(Data2DDefinition def) {
        Data2DManufactureController controller = oneOpen();
        controller.load(def);
        return controller;
    }

}
