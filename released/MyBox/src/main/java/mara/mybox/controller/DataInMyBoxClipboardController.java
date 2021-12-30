package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataClipboard;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-11
 * @License Apache License Version 2.0
 */
public class DataInMyBoxClipboardController extends BaseController {

    protected DataClipboard dataClipboard;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;

    @FXML
    protected ControlDataClipboardTable listController;
    @FXML
    protected ControlData2D dataController;
    @FXML
    protected Label nameLabel;

    public DataInMyBoxClipboardController() {
        baseTitle = message("DataInMyBoxClipboard");
        TipsLabelKey = "Data2DTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            dataController.setDataType(this, Data2D.Type.MyBoxClipboard);
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;
            dataClipboard = (DataClipboard) dataController.data2D;
            dataController.tableController.dataLabel = nameLabel;
            dataController.tableController.baseTitle = baseTitle;

            listController.setParameters(dataController.tableController);

            dataController.savedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    listController.refreshAction();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void createAction() {
        dataController.createAction();
    }

    @FXML
    @Override
    public void recoverAction() {
        dataController.recoverFile();
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

    @FXML
    public void refreshAction() {
        listController.refreshAction();
    }

    public void load(Data2DDefinition clip) {
        dataController.loadDef(clip);
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return dataController.keyEventsFilter(event);
        }
        return true;
    }

    @Override
    public void myBoxClipBoard() {
        dataController.myBoxClipBoard();
    }

    @Override
    public boolean checkBeforeNextAction() {
        return dataController.checkBeforeNextAction();
    }

    @Override
    public void cleanPane() {
        try {
            dataClipboard = null;
            tableData2DDefinition = null;
            tableData2DColumn = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        static
     */
    public static DataInMyBoxClipboardController oneOpen() {
        DataInMyBoxClipboardController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof DataInMyBoxClipboardController) {
                try {
                    controller = (DataInMyBoxClipboardController) object;
                    controller.toFront();
                    controller.refreshAction();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (DataInMyBoxClipboardController) WindowTools.openStage(Fxmls.DataInMyBoxClipboardFxml);
        }
        return controller;
    }

    public static void update() {
        Platform.runLater(() -> {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                Object object = window.getUserData();
                if (object == null) {
                    continue;
                }
                if (object instanceof DataInMyBoxClipboardController) {
                    ((DataInMyBoxClipboardController) object).refreshAction();
                }
                if (object instanceof Data2DPasteContentInMyBoxClipboardController) {
                    ((Data2DPasteContentInMyBoxClipboardController) object).refreshAction();
                }
            }
        });
    }

    public static DataInMyBoxClipboardController open(Data2DDefinition clip) {
        DataInMyBoxClipboardController c = oneOpen();
        c.load(clip);
        return c;
    }

}
