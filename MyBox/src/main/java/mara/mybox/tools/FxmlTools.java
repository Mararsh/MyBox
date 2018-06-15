package mara.mybox.tools;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.objects.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 11:19:42
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlTools {

    public static String badStyle = "-fx-text-box-border: red;";

    public static boolean setRadioFirstSelected(ToggleGroup group) {
        if (group == null) {
            return false;
        }
        ObservableList<Toggle> buttons = group.getToggles();
        for (Toggle button : buttons) {
            RadioButton radioButton = (RadioButton) button;
            radioButton.setSelected(true);
            return true;
        }
        return false;
    }

    public static boolean setRadioSelected(ToggleGroup group, String text) {
        if (group == null || text == null) {
            return false;
        }
        ObservableList<Toggle> buttons = group.getToggles();
        for (Toggle button : buttons) {
            RadioButton radioButton = (RadioButton) button;
            if (text.equals(radioButton.getText())) {
                button.setSelected(true);
                return true;
            }
        }
        return false;
    }

    public static String getFxmlPath(String fullPath) {
        if (fullPath == null) {
            return null;
        }
        int pos = fullPath.lastIndexOf("jar!");
        if (pos < 0) {
            return fullPath;
        }
        return fullPath.substring(pos + "jar!".length());
    }

    public static int getInputInt(TextField input) {
        try {
            return Integer.parseInt(input.getText());
        } catch (Exception e) {
            return CommonValues.InvalidValue;
        }
    }

    public static void setNonnegativeValidation(final TextField input) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v >= 0) {
                        input.setStyle(null);
                    } else {
                        input.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    input.setStyle(badStyle);
                }
            }
        });
    }

    public static void setFileValidation(final TextField input) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                if (newValue == null || newValue.isEmpty()) {
                    input.setStyle(badStyle);
                    return;
                }
                final File file = new File(newValue);
                if (!file.exists()) {
                    input.setStyle(badStyle);
                    return;
                }
                input.setStyle(null);
            }
        });
    }

}
