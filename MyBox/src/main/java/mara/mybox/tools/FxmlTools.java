package mara.mybox.tools;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import mara.mybox.objects.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 11:19:42
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlTools {

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

    public static void setNonnegativeValidation(final TextField input,
            final String goodStyle, final String badStyle) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v >= 0) {
                        input.setStyle(goodStyle);
                    } else {
                        input.setStyle(badStyle);
                    }
                } catch (Exception e) {
                    input.setStyle(badStyle);
                }
            }
        });
    }

    public static void setFileValidation(final TextField input,
            final String goodStyle, final String badStyle) {
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
                input.setStyle(goodStyle);
            }
        });
    }

}
