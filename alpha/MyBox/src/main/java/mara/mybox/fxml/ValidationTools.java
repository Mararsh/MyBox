package mara.mybox.fxml;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-5
 * @License Apache License Version 2.0
 */
public class ValidationTools {

    public static void setEditorStyle(final ComboBox box, final String style) {
        box.getEditor().setStyle(style);
        //        Platform.runLater(new Runnable() {
        //            @Override
        //            public void run() {
        //                box.getEditor().setStyle(style);
        //            }
        //        });
    }

    public static void setEditorNormal(final ComboBox box) {
        setEditorStyle(box, null);
    }

    public static void setEditorBadStyle(final ComboBox box) {
        setEditorStyle(box, NodeStyleTools.badStyle);
    }

    public static void setEditorWarnStyle(final ComboBox box) {
        setEditorStyle(box, NodeStyleTools.warnStyle);
    }

    public static int positiveValue(final TextField input) {
        return positiveValue(input, Integer.MAX_VALUE);
    }

    public static int positiveValue(final TextField input, final int max) {
        try {
            int v = Integer.parseInt(input.getText());
            if (v > 0 && v <= max) {
                input.setStyle(null);
                return v;
            } else {
                input.setStyle(NodeStyleTools.badStyle);
                return -1;
            }
        } catch (Exception e) {
            input.setStyle(NodeStyleTools.badStyle);
            return -1;
        }
    }

    public static void setNonnegativeValidation(final TextField input) {
        setNonnegativeValidation(input, Integer.MAX_VALUE);
    }

    public static void setNonnegativeValidation(final TextField input, final int max) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v >= 0 && v <= max) {
                        input.setStyle(null);
                    } else {
                        input.setStyle(NodeStyleTools.badStyle);
                    }
                } catch (Exception e) {
                    input.setStyle(NodeStyleTools.badStyle);
                }
            }
        });
    }

    public static void setFloatValidation(final TextField input) {
        input.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                float v = Float.valueOf(newValue);
                input.setStyle(null);
            } catch (Exception e) {
                input.setStyle(NodeStyleTools.badStyle);
            }
        });
    }

    public static void setPositiveValidation(final TextField input) {
        setPositiveValidation(input, Integer.MAX_VALUE);
    }

    public static void setPositiveValidation(final TextField input, final int max) {
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    int v = Integer.parseInt(newValue);
                    if (v > 0 && v <= max) {
                        input.setStyle(null);
                    } else {
                        input.setStyle(NodeStyleTools.badStyle);
                    }
                } catch (Exception e) {
                    input.setStyle(NodeStyleTools.badStyle);
                }
            }
        });
    }

    public static void setFileValidation(final TextField input, String key) {
        if (input == null) {
            return;
        }
        input.setStyle(NodeStyleTools.badStyle);
        input.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            String v = input.getText();
            if (v == null || v.isEmpty()) {
                input.setStyle(NodeStyleTools.badStyle);
                return;
            }
            final File file = new File(newValue);
            if (!file.exists() || !file.isFile()) {
                input.setStyle(NodeStyleTools.badStyle);
                return;
            }
            input.setStyle(null);
            UserConfig.setUserConfigString(key, file.getParent());
        });
    }

    public static void setPathValidation(final TextField input) {
        if (input == null) {
            return;
        }
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                final File file = new File(newValue);
                if (!file.isDirectory()) {
                    input.setStyle(NodeStyleTools.badStyle);
                    return;
                }
                input.setStyle(null);
            }
        });
    }

    public static void setPathExistedValidation(final TextField input) {
        if (input == null) {
            return;
        }
        input.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                final File file = new File(newValue);
                if (!file.exists() || !file.isDirectory()) {
                    input.setStyle(NodeStyleTools.badStyle);
                    return;
                }
                input.setStyle(null);
            }
        });
    }

}
