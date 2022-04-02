package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-9-8
 * @License Apache License Version 2.0
 */
public class ControlTimeLength extends BaseController {

    protected String name;
    protected long value, defaultValue;
    protected boolean isSeconds, permitNotSet, permitInvalid, permitZero;
    protected SimpleBooleanProperty notify;

    @FXML
    protected ComboBox<String> lengthSelector;

    public ControlTimeLength() {
        value = -1;
        defaultValue = 15;
        isSeconds = true;
        permitZero = false;
        permitNotSet = false;
        permitInvalid = false;
        notify = new SimpleBooleanProperty(false);
    }

    public static ControlTimeLength create() {
        return new ControlTimeLength();
    }

    public ControlTimeLength isSeconds(boolean isSeconds) {
        this.isSeconds = isSeconds;
        return this;
    }

    public ControlTimeLength permitZero(boolean permitZero) {
        this.permitZero = permitZero;
        return this;
    }

    public ControlTimeLength permitInvalid(boolean permitInvalid) {
        this.permitInvalid = permitInvalid;
        return this;
    }

    public ControlTimeLength permitNotSet(boolean permitUnlimit) {
        this.permitNotSet = permitUnlimit;
        return this;
    }

    public ControlTimeLength init(String name, long defaultValue) {
        this.name = name;
//        MyBoxLog.debug(name + " " + defaultValue + " " + value);
        lengthSelector.getItems().clear();
        if (permitNotSet) {
            lengthSelector.getItems().add(Languages.message("NotSet"));
        }
        if (permitZero) {
            lengthSelector.getItems().add("0");
        }
        if (isSeconds) {
            lengthSelector.getItems().addAll(Arrays.asList(
                    "10", "5", "15", "20", "30", "45",
                    "60   1 " + Languages.message("Minutes"), "180   3 " + Languages.message("Minutes"),
                    "300   5 " + Languages.message("Minutes"), "600   10 " + Languages.message("Minutes"),
                    "900   15 " + Languages.message("Minutes"), "1800   30 " + Languages.message("Minutes"),
                    "3600   1 " + Languages.message("Hours"), "5400   1.5 " + Languages.message("Hours"),
                    "7200   2 " + Languages.message("Hours")
            ));
        } else {  // milliseconds
            lengthSelector.getItems().addAll(Arrays.asList(
                    "200", "500", "1000", "50", "100", "300", "800",
                    "1000   1 " + Languages.message("Seconds"), "1500   1.5 " + Languages.message("Seconds"),
                    "2000   2 " + Languages.message("Seconds"), "3000   3 " + Languages.message("Seconds"),
                    "5000   5 " + Languages.message("Seconds"), "10000   10 " + Languages.message("Seconds"),
                    "15000   15 " + Languages.message("Seconds"), "30000   30 " + Languages.message("Seconds")
            ));
        }
        if (defaultValue > 0 || (permitZero && defaultValue == 0)) {
            this.defaultValue = defaultValue;
            if (!lengthSelector.getItems().contains(defaultValue + "")) {
                lengthSelector.getItems().add(0, defaultValue + "");
            }
        }

        lengthSelector.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                    if (isSettingValues) {
                        return;
                    }
                    value = -1;
                    try {
                        int pos = newValue.indexOf(' ');
                        String s = newValue;
                        if (pos >= 0) {
                            s = newValue.substring(0, pos);
                        }
                        long v = Long.parseLong(s);
                        if (v > 0 || (permitZero && v == 0)) {
                            value = v;
                            UserConfig.setString(name, v + "");
                        }
                    } catch (Exception e) {
                    }
                    if (value < 0) {
                        if (permitNotSet || permitInvalid) {
                            lengthSelector.getEditor().setStyle(null);
                            UserConfig.setString(name, "-1");
                        } else {
                            lengthSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } else {
                        lengthSelector.getEditor().setStyle(null);
                    }
//                    MyBoxLog.debug(name + " " + this.defaultValue + " " + value);
                    notify.set(!notify.get());
                });
        isSettingValues = true;
        value = this.defaultValue;
//        MyBoxLog.debug(name + " " + this.defaultValue + " " + value);
        if (name != null) {
            String saved = UserConfig.getString(name, this.defaultValue + "");
            if ("-1".equals(saved) || Languages.message("NotSet").equals(saved)) {
                value = -1;
                if (permitNotSet) {
                    lengthSelector.getSelectionModel().select(Languages.message("NotSet"));
                }
            } else {
                try {
                    long v = Long.parseLong(saved);
                    if (v > 0 || (permitZero && v == 0)) {
                        value = v;
                        lengthSelector.getSelectionModel().select(value + "");
                    }
                } catch (Exception e) {
                }
            }
        }
//        MyBoxLog.debug(name + " " + this.defaultValue + " " + value);
        isSettingValues = false;
        return this;
    }

    public boolean select(long inValue) {
        if (inValue < 0) {
            if (permitNotSet) {
                lengthSelector.getSelectionModel().select(Languages.message("NotSet"));
                return true;
            } else {
                return false;
            }
        } else if (inValue == 0) {
            if (permitZero) {
                lengthSelector.getSelectionModel().select("0");
                return true;
            } else {
                return false;
            }
        } else {
            lengthSelector.setValue(inValue + "");
            return true;
        }
    }

}
