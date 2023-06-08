package mara.mybox.controller;

import com.vladsch.flexmark.util.data.MutableDataHolder;
import java.util.Arrays;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.MarkdownTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-6-8
 * @License Apache License Version 2.0
 */
public class ControlMarkdownOptions extends BaseController {

    protected final SimpleBooleanProperty changedNotify;
    protected int indentSize = 4;

    @FXML
    protected ComboBox<String> emulationSelector, indentSelector;
    @FXML
    protected CheckBox trimCheck, appendCheck, discardCheck, linesCheck;

    public ControlMarkdownOptions() {
        changedNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            emulationSelector.getItems().addAll(Arrays.asList(
                    "GITHUB", "MARKDOWN", "GITHUB_DOC", "COMMONMARK", "KRAMDOWN", "PEGDOWN",
                    "FIXED_INDENT", "MULTI_MARKDOWN", "PEGDOWN_STRICT"
            ));
            emulationSelector.getSelectionModel().select(UserConfig.getString(baseName + "Emulation", "GITHUB"));
            emulationSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    UserConfig.setString(baseName + "Emulation", newValue);
                    changedNotify.set(!changedNotify.get());
                }
            });

            indentSize = UserConfig.getInt(baseName + "Indent", 4);
            indentSelector.getItems().addAll(Arrays.asList(
                    "4", "2", "0", "6", "8"
            ));
            indentSelector.getSelectionModel().select(indentSize + "");
            indentSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            indentSize = v;
                            UserConfig.setInt(baseName + "Indent", v);
                            changedNotify.set(!changedNotify.get());
                        }
                    } catch (Exception e) {
                    }
                }
            });

            trimCheck.setSelected(UserConfig.getBoolean(baseName + "Trim", false));
            trimCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Trim", trimCheck.isSelected());
                    changedNotify.set(!changedNotify.get());
                }
            });

            appendCheck.setSelected(UserConfig.getBoolean(baseName + "Append", false));
            appendCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Append", appendCheck.isSelected());
                    changedNotify.set(!changedNotify.get());
                }
            });

            discardCheck.setSelected(UserConfig.getBoolean(baseName + "Discard", false));
            discardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Discard", discardCheck.isSelected());
                    changedNotify.set(!changedNotify.get());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public MutableDataHolder options() {
        return MarkdownTools.htmlOptions(
                emulationSelector.getValue(),
                indentSize,
                trimCheck.isSelected(),
                discardCheck.isSelected(),
                appendCheck.isSelected());
    }

}
