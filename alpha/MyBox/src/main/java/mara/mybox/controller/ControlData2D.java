package mara.mybox.controller;

import java.sql.Connection;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.Data2D;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2D extends BaseController {

    protected Data2D data2D;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected ControlData2DEditTable tableController;
    protected ControlData2DEditText textController;
    protected boolean changed;
    protected final SimpleBooleanProperty statusNotify;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab editTab, viewTab, attributesTab, columnsTab;
    @FXML
    protected ControlData2DEdit editController;
    @FXML
    protected ControlData2DView viewController;
    @FXML
    protected ControlData2DAttributes attributesController;
    @FXML
    protected ControlData2DColumns columnsController;
    @FXML
    protected HBox paginationBox;
    @FXML
    protected ComboBox<String> pageSizeSelector, pageSelector;
    @FXML
    protected Label pageLabel, dataSizeLabel;

    public ControlData2D() {
        statusNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableController = editController.tableController;
            textController = editController.textController;

            tableData2DDefinition = new TableData2DDefinition();
            tableData2DColumn = new TableData2DColumn();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // parent should call this before initControls()
    public void setDataType(BaseController parent, Data2D.Type type) {
        try {
            parentController = parent;

            data2D = Data2D.create(type);
            data2D.setTableData2DDefinition(tableData2DDefinition);
            data2D.setTableData2DColumn(tableData2DColumn);
            data2D.setTableView(tableController.tableView);

            editController.setParameters(this);
            viewController.setParameters(this);
            attributesController.setParameters(this);
            columnsController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public synchronized void loadData() {
        tableController.loadData();
        attributesController.loadData();
        columnsController.loadData();
    }

    public synchronized void checkStatus() {
        changed = false;

        editController.changed = false;
        String title = message("Table");
        if (data2D.isTableChanged()) {
            title += "*";
            editController.changed = true;
        }
        editController.tableTab.setText(title);

        title = message("Text");
        if (textController.status == ControlData2DEditText.Status.Applied) {
            title += "*";
            editController.changed = true;
        } else if (textController.status == ControlData2DEditText.Status.Modified) {
            title += "**";
            editController.changed = true;
        }
        editController.textTab.setText(title);

        title = message("Edit");
        if (editController.changed) {
            title += "*";
            changed = true;
        }
        editTab.setText(title);

        title = message("Attributes");
        if (attributesController.status == ControlData2DAttributes.Status.Applied) {
            title += "*";
            changed = true;
        } else if (attributesController.status == ControlData2DAttributes.Status.Modified) {
            title += "**";
            changed = true;
        }
        attributesTab.setText(title);

        title = message("Columns");
        if (columnsController.status == ControlData2DColumns.Status.Applied) {
            title += "*";
            changed = true;
        } else if (columnsController.status == ControlData2DColumns.Status.Modified) {
            title += "**";
            changed = true;
        }
        columnsTab.setText(title);

        statusNotify.set(!statusNotify.get());
    }

    public synchronized void resetStatus() {
        if (tableController.task != null) {
            tableController.task.cancel();
        }
        if (tableController.backgroundTask != null) {
            tableController.backgroundTask.cancel();
        }
        data2D.setTableChanged(false);

        if (textController.task != null) {
            textController.task.cancel();
        }
        if (textController.backgroundTask != null) {
            textController.backgroundTask.cancel();
        }
        textController.status = null;

        if (attributesController.task != null) {
            attributesController.task.cancel();
        }
        if (attributesController.backgroundTask != null) {
            attributesController.backgroundTask.cancel();
        }
        attributesController.status = null;

        if (columnsController.task != null) {
            columnsController.task.cancel();
        }
        if (columnsController.backgroundTask != null) {
            columnsController.backgroundTask.cancel();
        }
        columnsController.status = null;
    }

    public int checkBeforeSave() {
        if (attributesController.status == ControlData2DAttributes.Status.Modified
                || columnsController.status == ControlData2DColumns.Status.Modified
                || textController.status == ControlData2DEditText.Status.Modified) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setHeaderText(getMyStage().getTitle());
            alert.setContentText(Languages.message("DataModifiedNotApplied"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonApply = new ButtonType(Languages.message("ApplyModificationAndSave"));
            ButtonType buttonDiscard = new ButtonType(Languages.message("DiscardModificationAndSave"));
            ButtonType buttonCancel = new ButtonType(Languages.message("Cancel"));
            alert.getButtonTypes().setAll(buttonApply, buttonDiscard, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonApply) {
                if (textController.status == ControlData2DEditText.Status.Modified) {
                    textController.okAction();
                    if (textController.status != ControlData2DEditText.Status.Applied) {
                        return -1;
                    }
                }
                if (attributesController.status == ControlData2DAttributes.Status.Modified) {
                    attributesController.okAction();
                    if (attributesController.status != ControlData2DAttributes.Status.Applied) {
                        return -1;
                    }
                }
                if (columnsController.status == ControlData2DColumns.Status.Modified) {
                    columnsController.okAction();
                    if (columnsController.status != ControlData2DColumns.Status.Applied) {
                        return -1;
                    }
                }
                return 1;
            } else if (result.get() == buttonDiscard) {
                return 2;
            } else {
                return -1;
            }
        } else {
            return 0;
        }
    }

    public void saveDefinition() {
        if (data2D == null) {
            return;
        }
        if (data2D.getD2did() >= 0
                && attributesController.status != ControlData2DAttributes.Status.Applied
                && columnsController.status != ControlData2DColumns.Status.Applied) {
            return;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            if (data2D.getD2did() <= 0 || attributesController.status == ControlData2DAttributes.Status.Applied) {
                Data2DDefinition def;
                if (data2D.getD2did() >= 0) {
                    def = tableData2DDefinition.updateData(conn, data2D);
                } else {
                    def = tableData2DDefinition.insertData(conn, data2D);
                }
                conn.commit();
                data2D.load(def);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        attributesController.status(ControlData2DAttributes.Status.Loaded);
                    }
                });
            }
            long d2did = data2D.getD2did();
            if (d2did < 0) {
                return;
            }
            if (columnsController.status == ControlData2DColumns.Status.Applied) {
                tableData2DColumn.save(conn, d2did, data2D.getColumns());
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        columnsController.status(ControlData2DColumns.Status.Loaded);
                    }
                });
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean needSave() {
        boolean goOn;
        if (!editController.changed
                && !attributesController.isChanged()
                && !columnsController.isChanged()) {
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
                parentController.saveAction();
                goOn = false;
            } else {
                goOn = result.get() == buttonNotSave;
            }
        }
        if (goOn) {
            resetStatus();
        }
        return goOn;
    }

    /*
        paigination
     */
    @FXML
    public void goPage() {
        tableController.goPage();
    }

    @FXML
    @Override
    public void pageNextAction() {
        tableController.pageNextAction();
    }

    @FXML
    @Override
    public void pagePreviousAction() {
        tableController.pagePreviousAction();
    }

    @FXML
    @Override
    public void pageFirstAction() {
        tableController.pageFirstAction();
    }

    @FXML
    @Override
    public void pageLastAction() {
        tableController.pageLastAction();
    }


    /*
        interface
     */
    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            try {
                Tab tab = tabPane.getSelectionModel().getSelectedItem();
                if (tab == editTab) {
                    return editController.keyEventsFilter(event);

                } else if (tab == viewTab) {
                    return viewController.keyEventsFilter(event);

                } else if (tab == attributesTab) {
                    return attributesController.keyEventsFilter(event);

                } else if (tab == columnsTab) {
                    return columnsController.keyEventsFilter(event);

                }
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
            return false;
        } else {
            return true;
        }
    }

    @FXML
    @Override
    public boolean popAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == viewTab) {
//                HtmlPopController.openWebView(this, webView);
                return true;

            } else if (tab == editTab) {
//                MarkdownPopController.open(this, markdownArea);
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @FXML
    @Override
    public boolean synchronizeAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == viewTab) {

                return true;

            } else if (tab == editTab) {

                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            closePopup();

            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == editTab) {
//                Point2D localToScreen = webView.localToScreen(webView.getWidth() - 80, 80);
//                MenuWebviewController.pop(webViewController, null, localToScreen.getX(), localToScreen.getY());
                return true;

            } else if (tab == viewTab) {
//                Point2D localToScreen = codesArea.localToScreen(codesArea.getWidth() - 80, 80);
//                MenuHtmlCodesController.open(this, codesArea, localToScreen.getX(), localToScreen.getY());
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @Override
    public void cleanPane() {
        try {
            tableController = null;
            data2D = null;
            tableData2DDefinition = null;
            tableData2DColumn = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
