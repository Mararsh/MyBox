package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.data.Data2D;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 *
 */
public class ControlData2DEditText extends BaseController {

    protected ControlData2D dataController;
    protected ControlData2DEdit editController;
    protected Data2D data2D;
    protected String delimiterName;
    protected Status lastStatus, status;

    public enum Status {
        Loaded, Modified, Applied
    }

    @FXML
    protected TextArea textArea;
    @FXML
    protected Label columnsLabel;

    protected void setParameters(ControlData2DEdit editController) {
        try {
            this.editController = editController;
            dataController = editController.dataController;
            this.data2D = dataController.data2D;
            this.baseName = dataController.baseName;

            delimiterName = UserConfig.getString(baseName + "EditDelimiter", ",");

            textArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    status(Status.Modified);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadData() {
        try {
            status = null;
            loadText();
            status(Status.Loaded);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void loadText() {
        try {
            String text = TextTools.dataPage(data2D, delimiterName, false, false);
            isSettingValues = true;
            textArea.setText(text);
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void status(Status newStatus) {
        okButton.setDisable(newStatus != Status.Modified);
        cancelButton.setDisable(newStatus != Status.Modified);
        if (status == newStatus) {
            return;
        }
        lastStatus = status;
        this.status = newStatus;
        dataController.checkStatus();
    }

    public boolean isChanged() {
        return status == Status.Modified && status == Status.Applied;
    }

    @FXML
    @Override
    public void okAction() {
        if (!isChanged()) {
            return;
        }
        if (pickValues()) {
            status(Status.Applied);
        } else {
            popError(message("InvalidParameter") + ": " + message("DataName"));
        }
    }

    public boolean pickValues() {
        try {
            String s = textArea.getText();
            String[] lines = s.split("\n");
            int colsSize = 0;
            List<List<String>> rows = new ArrayList<>();
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                List<String> row = TextTools.parseLine(line, delimiterName);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                int size = row.size();
                if (size > colsSize) {
                    colsSize = size;
                }
                rows.add(row);
            }
            int rowsSize = rows.size();
            if (data2D.isMutiplePages()) {
                colsSize = data2D.columnsNumber();
            }
            if (rowsSize == 0 || colsSize == 0) {
//                makeSheet(null);
                return true;
            }
            String[][] data = new String[rowsSize][colsSize];
            for (int r = 0; r < rowsSize; r++) {
                List<String> row = rows.get(r);
                for (int c = 0; c < Math.min(colsSize, row.size()); c++) {
                    data[r][c] = row.get(c);
                }
            }
//            makeSheet(data, true, true);
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
            return false;
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        if (!isChanged()) {
            return;
        }
        status(lastStatus);
        loadText();
    }

    public void loadText(String text) {
        try {
            editController.tabPane.getSelectionModel().select(editController.textTab);
            isSettingValues = true;
            textArea.setText(text);
            isSettingValues = false;
            okAction();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("Copy"));
            menu.setOnAction((ActionEvent event) -> {
                copyAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Pop"));
            menu.setOnAction((ActionEvent event) -> {
                popAction();
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
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

}
