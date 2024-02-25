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
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class Data2DCSV extends BaseChildController {

    protected BaseData2DLoadController dataController;
    protected Data2D data2D;
    protected String delimiterName;
    protected ChangeListener<Boolean> delimiterListener;

    @FXML
    protected TextArea textArea;
    @FXML
    protected Label columnsLabel;
    @FXML
    protected CheckBox wrapCheck;
    @FXML
    protected Label nameLabel;

    public Data2DCSV() {
        baseTitle = message("EditPageDataInCSVFormat");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            delimiterName = UserConfig.getString(baseName + "EditDelimiter", ",");

            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "EditTextWrap", true));
            textArea.setWrapText(wrapCheck.isSelected());
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "EditTextWrap", newValue);
                    textArea.setWrapText(newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setParameters(BaseData2DLoadController controller) {
        try {
            dataController = controller;

            loadData();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadData() {
        if (dataController == null || !dataController.isShowing()) {
            close();
            return;
        }
        data2D = dataController.data2D;
        if (data2D == null || !data2D.isColumnsValid()) {
            popError(message("InvalidData"));
            close();
        }
        nameLabel.setText(message("Data") + ": " + data2D.displayName());
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private String text;

            @Override
            protected boolean handle() {
                text = data2D.encodeCSV(this, delimiterName, false, false, false);
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

        };
        start(task, thisPane);
    }

    @FXML
    @Override
    public void okAction() {
        if (delimiterName == null) {
            delimiterName = ",";
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private List<List<String>> rows;

            @Override
            protected boolean handle() {
                try {
                    rows = new ArrayList<>();
                    String text = textArea.getText();
                    if (text != null && !text.isEmpty()) {
                        int colsNumber = data2D.columnsNumber();
                        List<List<String>> data = data2D.decodeCSV(this, text, delimiterName, false);
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
                dataController.updatePage(rows, false);
                isSettingValues = false;
                if (closeAfterCheck.isSelected()) {
                    close();
                }
                dataController.popInformation(message("UpdateSuccessfully"));
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void recoverAction() {
        loadData();
    }

    @FXML
    public void delimiterActon() {
        TextDelimiterController controller = TextDelimiterController.open(this, delimiterName, false, false);
        controller.okNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                delimiterName = controller.delimiterName;
                UserConfig.setString(baseName + "EditDelimiter", delimiterName);
                loadData();
                popDone();
            }
        });
        if (data2D.isCSV() || data2D.isTexts()) {
            controller.label.setText(message("DelimiterNotAffectSource"));
        }
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
        MenuTextEditController.textMenu(myController, textArea, localToScreen.getX(), localToScreen.getY());
        return true;
    }

    @Override
    public void cleanPane() {
        try {
            delimiterListener = null;
            data2D = null;
            delimiterName = null;
            dataController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static Data2DCSV open(BaseData2DLoadController tableController) {
        try {
            Data2DCSV controller = (Data2DCSV) WindowTools.branchStage(
                    tableController, Fxmls.Data2DCSVFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
