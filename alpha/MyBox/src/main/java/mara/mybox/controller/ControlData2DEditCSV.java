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
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class ControlData2DEditCSV extends BaseController {

    protected ControlData2D dataController;
    protected ControlData2DEdit editController;
    protected ControlData2DEditTable tableController;
    protected Data2D data2D;
    protected String delimiterName;
    protected Status status;
    protected ChangeListener<Boolean> delimiterListener;

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

    @Override
    public void initControls() {
        try {
            super.initControls();

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

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setParameters(ControlData2DEdit editController) {
        try {
            this.editController = editController;
            dataController = editController.dataController;
            tableController = editController.tableController;
            baseTitle = dataController.baseTitle;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setData(Data2D data) {
        try {
            data2D = data;
            checkData();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadData() {
        if (isSettingValues) {
            return;
        }
        status = null;
        loadText();
        status(data2D.isTableChanged() ? Status.Applied : Status.Loaded);
    }

    public void loadText() {
        if (!checkData()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {
            private String text;

            @Override
            protected boolean handle() {
                text = data2D.encodeCSV(task, delimiterName, false, false);
                return text != null;
            }

            @Override
            protected void whenSucceeded() {
                String delimiter = TextTools.delimiterValue(delimiterName);
                String label = "";
                for (String name : data2D.columnNames()) {
                    if (!label.isEmpty()) {
                        label += delimiter;
                    }
                    label += name;
                }
                columnsLabel.setText(label);
                isSettingValues = true;
                textArea.setText(text);
                isSettingValues = false;
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                task = null;
            }

        };
        start(task);
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
        if (status != Status.Modified) {
            popInformation(message("Unchanged"));
            return;
        }
        if (!checkData() || delimiterName == null) {
            popError(message("InvalidData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {
            private List<List<String>> rows;

            @Override
            protected boolean handle() {
                try {
                    rows = new ArrayList<>();
                    String text = textArea.getText();
                    if (text != null && !text.isEmpty()) {
                        int colsNumber = data2D.columnsNumber();
                        List<List<String>> data = data2D.decodeCSV(task, text, delimiterName, false);
                        if (data == null) {
                            return false;
                        }
                        long startindex = data2D.getStartRowOfCurrentPage();
                        for (int i = 0; i < data.size(); i++) {
                            List<String> drow = data.get(i);
                            List<String> nrow = new ArrayList<>();
                            nrow.add((startindex + i) + "");
                            int len = drow.size();
                            if (len > colsNumber) {
                                nrow.addAll(drow.subList(0, colsNumber));
                            } else {
                                nrow.addAll(drow);
                                for (int c = len; c < colsNumber; c++) {
                                    nrow.add(null);
                                }
                            }
                            rows.add(nrow);
                        }
                    }
                    return rows != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                isSettingValues = true;
                tableController.updateData(rows, false);
                isSettingValues = false;
                status(Status.Applied);
                popInformation(message("UpdateSuccessfully"));
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                task = null;
            }

        };
        start(task);
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

    @Override
    public void cleanPane() {
        try {
            delimiterListener = null;
            data2D = null;
            delimiterName = null;
            tableController = null;
            editController = null;
            dataController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
