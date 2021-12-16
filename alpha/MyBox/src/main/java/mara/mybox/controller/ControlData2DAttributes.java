package mara.mybox.controller;

import java.util.Arrays;
import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import mara.mybox.data.Data2D;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public class ControlData2DAttributes extends BaseController {

    protected ControlData2D dataController;
    protected TableData2DDefinition tableData2DDefinition;
    protected Data2D data2D;
    protected String dataName;
    protected short scale;
    protected int maxRandom;
    protected Status status;
    protected final SimpleBooleanProperty invalid;

    public enum Status {
        Loaded, Modified, Applied
    }

    @FXML
    protected TextField idInput, timeInput, dataTypeInput, dataNameInput;
    @FXML
    protected ComboBox<String> scaleSelector, randomSelector;

    public ControlData2DAttributes() {
        invalid = new SimpleBooleanProperty(false);
    }

    protected void setParameters(ControlData2D dataController) {
        try {
            this.dataController = dataController;

            dataNameInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if (!isSettingValues) {
                        if (newValue == null && oldValue != null
                                || newValue != null && !newValue.equals(oldValue)) {
                            status(Status.Modified);
                        }
                    }
                }
            });

            scale = (short) UserConfig.getInt(baseName + "Scale", 2);
            if (scale < 0) {
                scale = 2;
            }
            scaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            scaleSelector.setValue(scale + "");
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(scaleSelector.getValue());
                            if (v >= 0 && v <= 15) {
                                scale = (short) v;
                                UserConfig.setInt(baseName + "Scale", v);
                                scaleSelector.getEditor().setStyle(null);
                                if (!isSettingValues) {
                                    status(Status.Modified);
                                }
                            } else {
                                scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            maxRandom = UserConfig.getInt(baseName + "MaxRandom", 100000);
            if (maxRandom < 0) {
                maxRandom = 100000;
            }
            randomSelector.getItems().addAll(Arrays.asList("1", "100", "10", "1000", "10000", "1000000", "10000000"));
            randomSelector.setValue(maxRandom + "");
            randomSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                maxRandom = v;
                                UserConfig.setInt(baseName + "MaxRandom", v);
                                randomSelector.getEditor().setStyle(null);
                                if (!isSettingValues) {
                                    status(Status.Modified);
                                }
                            } else {
                                randomSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            randomSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            invalid.bind(Bindings.isEmpty(dataNameInput.textProperty())
                    .or(Bindings.isEmpty(randomSelector.getEditor().textProperty()))
                    .or(randomSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(Bindings.isEmpty(randomSelector.getEditor().textProperty()))
                    .or(randomSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setData(Data2D data) {
        try {
            data2D = data;
            tableData2DDefinition = dataController.tableData2DDefinition;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public synchronized void loadData() {
        status = null;
        loadAttributes();
        status(Status.Loaded);
    }

    public void loadAttributes() {
        try {
            if (data2D == null) {
                return;
            }
            idInput.setText(data2D.getD2did() >= 0 ? data2D.getD2did() + "" : message("NewData"));
            timeInput.setText(DateTools.datetimeToString(data2D.getModifyTime()));
            dataTypeInput.setText(message(data2D.getType().name()));
            isSettingValues = true;
            dataNameInput.setText(data2D.getDataName());
            scaleSelector.setValue(data2D.getScale() + "");
            randomSelector.setValue(data2D.getMaxRandom() + "");
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void status(Status newStatus) {
        if (status == newStatus) {
            return;
        }
        status = newStatus;
        dataController.checkStatus();

    }

    public boolean isChanged() {
        return status == Status.Modified || status == Status.Applied;
    }

    public void updateDataName() {
        isSettingValues = true;
        dataNameInput.setText(data2D.getDataName());
        isSettingValues = false;
    }

    @FXML
    @Override
    public void okAction() {
        if (status != Status.Modified) {
            return;
        }
        if (pickValues()) {
            status(Status.Applied);
        } else {
            popError(message("InvalidParameter") + ": " + message("DataName"));
        }
    }

    public boolean pickValues() {
        String name = dataNameInput.getText();
        if (name == null || name.isBlank()) {
            return false;
        }
        data2D.setDataName(name);
        data2D.setScale(scale);
        data2D.setMaxRandom(maxRandom);
        data2D.setModifyTime(new Date());
        return true;
    }

    @FXML
    @Override
    public void cancelAction() {
        if (status == Status.Modified) {
            loadAttributes();
            status(Status.Applied);
        }
    }

}
