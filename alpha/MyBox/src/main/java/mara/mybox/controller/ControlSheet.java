package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.db.table.TableDataColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-25
 * @License Apache License Version 2.0
 *
 * ControlSheet < ControlSheet_Calculation < ControlSheet_TextsDisplay <
 * ControlSheet_Html < ControlSheet_ColMenu < ControlSheet_RowMenu <
 * ControlSheet_Buttons < ControlSheet_Edit < ControlSheet_Pages <
 * ControlSheet_Sheet < ControlSheet_Columns < ControlSheet_Base
 *
 */
public abstract class ControlSheet extends ControlSheet_Calculation {

    @Override
    public void initValues() {
        try {
            super.initValues();
            widthChange = 10;

            columns = new ArrayList<>();
            tableDataDefinition = new TableDataDefinition();
            tableDataColumn = new TableDataColumn();
            sheetChangedNotify = new SimpleBooleanProperty(false);
            dataChangedNotify = new SimpleBooleanProperty(false);
            noDataLabel = new Label(Languages.message("NoData"));
            noDataLabel.setStyle("-fx-text-fill: gray;");
            inputStyle = "-fx-border-radius: 10; -fx-background-radius: 0;";
            pagesNumber = 1;
            pageSize = 50;
            currentRow = currentCol = 0;

            parentController = this;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(trimColumnsButton, message("RenameAllColumns"));
            NodeStyleTools.setTooltip(equalSheetButton, message("SetValues"));
            NodeStyleTools.setTooltip(synchronizeEditButton, message("SynchronizeChangesToOtherPanes"));
            NodeStyleTools.setTooltip(analyseSheetButton, message("Validate"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    // parent should call this
    public void setParent(BaseController parent) {
        this.parentController = parent;
        this.baseName = parent.baseName;
        setControls();
    }

    // Window should call this when start
    public void setControls() {
        try {
            initEdit();
            initPagination();
            initHtmlControls();
            initTextControls();
            initOptions();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initOptions() {
        try {
            scale = (short) UserConfig.getInt(baseName + "Scale", 2);
            maxRandom = UserConfig.getInt(baseName + "MaxRandom", 100000);

            scaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            scaleSelector.setValue(scale + "");
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkScale();
                    });

            randomSelector.getItems().addAll(
                    Arrays.asList("1", "100", "10", "1000", "10000", "1000000", "10000000")
            );
            randomSelector.setValue(maxRandom + "");
            randomSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        if (isSettingValues) {
                            return;
                        }
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                maxRandom = v;
                                UserConfig.setInt(baseName + "MaxRandom", v);
                                randomSelector.getEditor().setStyle(null);
                            } else {
                                randomSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                            }
                        } catch (Exception e) {
                            randomSelector.getEditor().setStyle(NodeStyleTools.badStyle);
                        }
                    });

            overPopMenuCheck.setSelected(UserConfig.getBoolean(baseName + "OverPop", false));
            overPopMenuCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        UserConfig.setBoolean(baseName + "OverPop", newValue);
                    });

            rightClickPopMenuCheck.setSelected(UserConfig.getBoolean(baseName + "RightClickPop", true));
            rightClickPopMenuCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                        UserConfig.setBoolean(baseName + "RightClickPop", newValue);
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkScale() {
        if (isSettingValues) {
            return;
        }
        try {
            int v = Integer.parseInt(scaleSelector.getValue());
            if (v >= 0 && v <= 15) {
                scale = (short) v;
                UserConfig.setInt(baseName + "Scale", v);
                scaleSelector.getEditor().setStyle(null);
            } else {
                scaleSelector.getEditor().setStyle(NodeStyleTools.badStyle);
            }
        } catch (Exception e) {
            scaleSelector.getEditor().setStyle(NodeStyleTools.badStyle);
        }
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            TableSizeController controller = (TableSizeController) openChildStage(Fxmls.TableSizeFxml, true);
            controller.setParameters(this, message("Table"));
            controller.notify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    newSheet(controller.rowsNumber, controller.colsNumber);
                    controller.closeStage();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void afterDataChanged() {
        try {
            updateEdit();
            makeDefintionPane();
            validateData(false);
            updateHtml();
            updateText();

            boolean noRows = sheetInputs == null || sheetInputs.length == 0;
            boolean noCols = columns == null || columns.isEmpty();
            rowsAddButton.setDisable(noCols);
            rowsDeleteButton.setDisable(noRows);
            columnsDeleteButton.setDisable(noCols);
            widthSheetButton.setDisable(noCols);
            copyToSystemClipboardButton.setDisable(noRows);
            copyToMyBoxClipboardButton.setDisable(noRows);
            equalSheetButton.setDisable(noRows);
            sortSheetButton.setDisable(noRows);
            calculateSheetButton.setDisable(noRows);

//            BaseDataOperationController.update(this);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        parentController.saveAction();
    }

    @FXML
    @Override
    public boolean popAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == htmlTab) {
                HtmlPopController.openWebView(this, htmlViewController.webView);
                return true;

            } else if (tab == textsDisplayTab) {
                TextPopController.openInput(this, textsDisplayArea);
                return true;

            } else if (tab == editTab) {
                TextPopController.openInput(this, textsEditArea);
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return true;
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            closePopup();

            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == htmlTab) {
                Point2D localToScreen = htmlViewController.webView.localToScreen(htmlViewController.webView.getWidth() - 80, 80);
                MenuWebviewController.pop(htmlViewController, null, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (tab == textsDisplayTab) {
                Point2D localToScreen = textsDisplayArea.localToScreen(textsDisplayArea.getWidth() - 80, 80);
                MenuTextEditController.open(this, textsDisplayArea, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (tab == editTab) {
                Point2D localToScreen = textsEditArea.localToScreen(textsEditArea.getWidth() - 80, 80);
                MenuTextEditController.open(this, textsEditArea, localToScreen.getX(), localToScreen.getY());
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @Override
    public boolean controlAltM() {
        myBoxClipBoard();
        return true;
    }

    @Override
    public boolean checkBeforeNextAction() {
        boolean goOn;
        if (!dataChangedNotify.get()) {
            goOn = true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setHeaderText(getMyStage().getTitle());
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
            dataChangedNotify.set(false);
        }
        return goOn;
    }

    @Override
    public void cleanPane() {
        try {
            tableDataDefinition = null;
            tableDataColumn = null;
            dataDefinition = null;
            sheetInputs = null;
            colsCheck = null;
            rowsCheck = null;
            pageData = null;
            columns = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
