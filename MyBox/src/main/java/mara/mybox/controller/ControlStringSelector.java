package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-21
 * @License Apache License Version 2.0
 */
public class ControlStringSelector extends BaseController {

    protected int max, defaultMax;
    protected String name, defaultValue, setting;

    @FXML
    protected ComboBox<String> selector;

    public void init(BaseController parent, String name, String defaultValue, int defaultMax) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.defaultMax = defaultMax;
        selector.setValue(defaultValue);
        init(parent);
        refreshList();
    }

    public void init(BaseController parent) {
        parentController = parent;
        setting = "... " + message("MaxSaved");
        baseName = parent.baseName;
        baseTitle = parent.baseTitle;
        defaultMax = defaultMax > 0 ? defaultMax : 20;
        if (name == null) {
            name = baseName;
        }
        selector.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                if (isSettingValues || newValue == null || newValue.trim().isEmpty()) {
                    return;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (setting.equals(newValue)) {
                            String value = FxmlControl.askValue(baseTitle, baseTitle, message("MaxSaved"), max + "");
                            if (value == null) {
                                return;
                            }
                            try {
                                max = Integer.parseInt(value);
                                AppVariables.setUserConfigInt(getName() + "MaxSaved", max);
                                refreshList();
                            } catch (Exception e) {
                                popError(message("InvalidData"));
                            }
                        }
                    }
                });
            }
        });

    }

    protected void refreshList(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        selector.setValue(defaultValue);
        refreshList();
    }

    protected void refreshList() {
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>() {
                private List<String> values;
                private String value;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        value = value();
                        if (value != null && !value.isBlank() && !value.equals(setting)) {
                            TableStringValues.add(conn, getName(), value);
                        }
                        max = AppVariables.getUserConfigInt(getName() + "MaxSaved", defaultMax);
                        if (max <= 0) {
                            if (defaultMax <= 0) {
                                defaultMax = 20;
                            }
                            AppVariables.setUserConfigInt(getName() + "MaxSaved", defaultMax);
                            max = 20;
                        }
                        if (isCancelled()) {
                            return true;
                        }
                        values = TableStringValues.max(conn, getName(), max);
                        if (values.isEmpty() && defaultValue != null) {
                            values.add(defaultValue);
                            TableStringValues.add(conn, getName(), defaultValue);
                        }
                        values.add(setting);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (isCancelled() || values == null) {
                        return;
                    }
                    isSettingValues = true;
                    selector.getItems().setAll(values);
                    if (value != null && !value.isBlank() && !value.equals(setting)) {
                        selector.getSelectionModel().select(value);
                    } else if (values.size() > 1) {
                        selector.getSelectionModel().select(0);
                    }
                    isSettingValues = false;
                }

            };
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }

    }

    public String value() {
        String value = selector.getEditor().getText();
        if (value == null || value.isBlank()) {
            return null;
        } else {
            return value;
        }
    }

    public void disable(boolean set) {
        selector.setDisable(set);
    }

    public void set(String value) {
        selector.setValue(value);
    }

    public String getName() {
        return name;
    }

    /*
     get/set
     */
    public void setName(String name) {
        this.name = name;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ComboBox<String> getSelector() {
        return selector;
    }

    public void setSelector(ComboBox<String> selector) {
        this.selector = selector;
    }

}
