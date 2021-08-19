package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-25
 * @License Apache License Version 2.0
 */
public abstract class BaseSheetController extends BaseSheetController_Size {

    @Override
    public void initValues() {
        try {
            super.initValues();
            widthChange = 10;
            dataChanged = false;
            columns = new ArrayList<>();
            tableDataDefinition = new TableDataDefinition();
            tableDataColumn = new TableDataColumn();
            notify = new SimpleBooleanProperty(false);
            noDataLabel = new Label(Languages.message("NoData"));
            noDataLabel.setStyle("-fx-text-fill: gray;");
            inputStyle = "-fx-border-radius: 10; -fx-background-radius: 0;";

            sheetController = this;
            if (sheetDisplayController == null) {
                sheetDisplayController = this;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.removeTooltip(sizeSheetButton);
            NodeStyleTools.removeTooltip(copySheetButton);
            NodeStyleTools.removeTooltip(equalSheetButton);
            NodeStyleTools.removeTooltip(deleteSheetButton);
            NodeStyleTools.setTooltip(editSheetButton, message("EditPageRows"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        sheetDisplayController.initDisplay(this);
    }

    @FXML
    @Override
    public boolean popAction() {
        if (sheetDisplayController == this) {
            return super.popAction();
        } else {
            return sheetDisplayController.popAction();
        }
    }

    @FXML
    @Override
    public boolean menuAction() {
        if (sheetDisplayController == this) {
            return super.menuAction();
        } else {
            return sheetDisplayController.menuAction();
        }
    }

    @FXML
    protected void editPageAllRows() {
        DataClipboardController controller = (DataClipboardController) WindowTools.openStage(Fxmls.DataClipboardFxml);
        controller.setSourceController(this);
        controller.toFront();
    }

    @Override
    public boolean checkBeforeNextAction() {
        boolean goOn;
        if (!dataChanged) {
            goOn = true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(Languages.message("NeedSaveBeforeAction"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(Languages.message("Save"));
            ButtonType buttonNotSave = new ButtonType(Languages.message("NotSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                goOn = false;
            } else {
                goOn = result.get() == buttonNotSave;
            }
        }
        if (goOn) {
            if (task != null) {
                task.cancel();
            }
            if (backgroundTask != null) {
                backgroundTask.cancel();
            }
            dataChanged = false;
        }
        return goOn;
    }

    @Override
    public void cleanPane() {
        try {
            tableDataDefinition = null;
            tableDataColumn = null;
            dataDefinition = null;
            inputs = null;
            colsCheck = null;
            rowsCheck = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        get/set
     */
    public SimpleBooleanProperty getNotify() {
        return notify;
    }

    public void setNotify(SimpleBooleanProperty notify) {
        this.notify = notify;
    }

}
