package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import mara.mybox.data.Data2D;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
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
    protected ControlData2DEditTable tableController;
    protected Data2D data2D;
    protected String delimiterName;
    protected Status status;

    public enum Status {
        Loaded, Modified, Applied
    }

    @FXML
    protected TextArea textArea;
    @FXML
    protected Label columnsLabel;
    @FXML
    protected CheckBox wrapCheck;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    protected void setParameters(ControlData2DEdit editController) {
        try {
            this.editController = editController;
            dataController = editController.dataController;
            tableController = editController.tableController;
            data2D = dataController.data2D;

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

            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "EditTextWrap", true));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "EditTextWrap", newValue);
                    textArea.setWrapText(newValue);
                }
            });
            textArea.setWrapText(wrapCheck.isSelected());

            checkData();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadData() {
        try {
            if (isSettingValues) {
                return;
            }
            status = null;
            loadText();
            status(data2D.isTableChanged() ? Status.Applied : Status.Loaded);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void loadText() {
        try {
            if (!checkData()) {
                return;
            }
            String delimiter = TextTools.delimiterValue(delimiterName);
            String label = "";
            for (String name : data2D.columnNames()) {
                if (!label.isEmpty()) {
                    label += delimiter;
                }
                label += name;
            }
            columnsLabel.setText(label);
            String text = TextTools.dataPage(data2D, delimiterName, false, false);
            isSettingValues = true;
            textArea.setText(text);
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void status(Status newStatus) {
        checkData();
        if (status == newStatus) {
            return;
        }
        status = newStatus;
        dataController.checkStatus();
    }

    public boolean checkData() {
        boolean invalid = data2D == null || !data2D.isColumnsValid();
        if (invalid) {
            columnsLabel.setText("");
            isSettingValues = true;
            textArea.setText("");
            isSettingValues = false;
        }
        thisPane.setDisable(invalid);
        return !invalid;
    }

    public boolean isChanged() {
        return status == Status.Modified || status == Status.Applied;
    }

    @FXML
    @Override
    public void okAction() {
        if (status != Status.Modified || !checkData() || delimiterName == null) {
            popError(message("InvalidData"));
            return;
        }
        try {
            List<List<String>> rows = new ArrayList<>();
            String text = textArea.getText();
            if (text != null && !text.isEmpty()) {
                int colsNumber = data2D.columnsNumber();
                String[] lines = text.split("\n");
                int rowIndex = 0;
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    List<String> row = TextTools.parseLine(line, delimiterName);
                    if (row == null || row.isEmpty()) {
                        continue;
                    }
                    int len = row.size();
                    if (len > colsNumber) {
                        row = row.subList(0, colsNumber);
                    } else {
                        for (int c = len; c < colsNumber; c++) {
                            row.add(null);
                        }
                    }
                    row.add(0, (data2D.getStartRowOfCurrentPage() + rowIndex++) + "");
                    rows.add(row);
                }
            }
            isSettingValues = true;
            tableController.loadData(rows, false);
            isSettingValues = false;
            status(Status.Applied);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        if (status == Status.Modified) {
            loadText();
            status(Status.Applied);
        }
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
    public void delimiterActon() {
        TextDelimiterController controller = TextDelimiterController.open(this, delimiterName, false);
        controller.okNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                delimiterName = controller.delimiterName;
                UserConfig.setString(baseName + "EditDelimiter", delimiterName);
                loadData();
                popDone();
            }
        });
    }

    @FXML
    @Override
    public boolean popAction() {
        TextPopController.openInput(this, textArea);
        return true;
    }

    @FXML
    @Override
    public boolean menuAction() {
        Point2D localToScreen = textArea.localToScreen(textArea.getWidth() - 80, 80);
        MenuTextEditController.open(myController, textArea, localToScreen.getX(), localToScreen.getY());
        return true;
    }

}
