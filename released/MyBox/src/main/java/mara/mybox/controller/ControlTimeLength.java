package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import static mara.mybox.value.Languages.message;
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
            lengthSelector.getItems().add(message("NotSet"));
        }
        if (permitZero) {
            lengthSelector.getItems().add("0");
        }
        if (isSeconds) {
            lengthSelector.getItems().addAll(Arrays.asList(
                    "10", "5", "15", "20", "30", "45",
                    "60   1 " + message("Minutes"), "180   3 " + message("Minutes"),
                    "300   5 " + message("Minutes"), "600   10 " + message("Minutes"),
                    "900   15 " + message("Minutes"), "1800   30 " + message("Minutes"),
                    "3600   1 " + message("Hours"), "5400   1.5 " + message("Hours"),
                    "7200   2 " + message("Hours")
            ));
        } else {  // milliseconds
            lengthSelector.getItems().addAll(Arrays.asList(
                    "200", "500", "1000", "50", "100", "300", "800",
                    "1000   1 " + message("Seconds"), "1500   1.5 " + message("Seconds"),
                    "2000   2 " + message("Seconds"), "3000   3 " + message("Seconds"),
                    "5000   5 " + message("Seconds"), "10000   10 " + message("Seconds"),
                    "15000   15 " + message("Seconds"), "30000   30 " + message("Seconds")
            ));
        }
        if (defaultValue > 0 || (permitZero && defaultValue == 0)) {
            this.defaultValue = defaultValue;
            if (!lengthSelector.getItems().contains(defaultValue + "")) {
                lengthSelector.getItems().add(0, defaultValue + "");
            }
        }

        lengthSelector.valueProperty().addListener(
                (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                    if (isSettingValues) {
                        return;
                    }
                    pickValue();
                });

        isSettingValues = true;
        value = this.defaultValue;
//        MyBoxLog.debug(name + " " + this.defaultValue + " " + value);
        if (name != null) {
            String saved = UserConfig.getString(name, this.defaultValue + "");
            if ("-1".equals(saved) || message("NotSet").equals(saved)) {
                value = -1;
                if (permitNotSet) {
                    lengthSelector.setValue(message("NotSet"));
                }
            } else {
                try {
                    long v = Long.parseLong(saved);
                    if (v > 0 || (permitZero && v == 0)) {
                        value = v;
                        lengthSelector.setValue(value + "");
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
                lengthSelector.getSelectionModel().select(message("NotSet"));
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

    public long pickValue() {
        value = -1;
        String vs = lengthSelector.getValue();
        try {
            int pos = vs.indexOf(' ');
            String s = vs;
            if (pos >= 0) {
                s = vs.substring(0, pos);
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
                popError(message("InvalidParameter"));
                return -1;
            }
        } else {
            lengthSelector.getEditor().setStyle(null);
        }
//                    MyBoxLog.debug(name + " " + this.defaultValue + " " + value);
        notify.set(!notify.get());
        return value;
    }
}
