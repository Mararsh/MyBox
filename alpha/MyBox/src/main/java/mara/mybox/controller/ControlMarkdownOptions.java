package mara.mybox.controller;

import com.vladsch.flexmark.util.data.MutableDataHolder;
import java.sql.Connection;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.MarkdownTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-6-8
 * @License Apache License Version 2.0
 */
public class ControlMarkdownOptions extends BaseController {

    protected int indentSize = 4;

    @FXML
    protected ComboBox<String> emulationSelector, indentSelector;
    @FXML
    protected CheckBox trimCheck, appendCheck, discardCheck, linesCheck;

    @Override
    public void initControls() {
        try (Connection conn = DerbyBase.getConnection()) {
            super.initControls();

            emulationSelector.getItems().addAll(Arrays.asList(
                    "GITHUB", "MARKDOWN", "GITHUB_DOC", "COMMONMARK", "KRAMDOWN", "PEGDOWN",
                    "FIXED_INDENT", "MULTI_MARKDOWN", "PEGDOWN_STRICT"
            ));
            emulationSelector.setValue(UserConfig.getString(conn, "MarkdownEmulation", "GITHUB"));

            indentSize = UserConfig.getInt(conn, "MarkdownIndent", 4);
            if (indentSize < 0) {
                indentSize = 4;
            }
            indentSelector.getItems().addAll(Arrays.asList("4", "2", "0", "6", "8"));
            indentSelector.setValue(indentSize + "");
            indentSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            indentSize = v;
                        }
                    } catch (Exception e) {
                    }
                }
            });

            trimCheck.setSelected(UserConfig.getBoolean(conn, "MarkdownTrim", false));
            appendCheck.setSelected(UserConfig.getBoolean(conn, "MarkdownAppend", false));
            discardCheck.setSelected(UserConfig.getBoolean(conn, "MarkdownDiscard", false));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    public boolean pickValues() {
        try (Connection conn = DerbyBase.getConnection()) {
            UserConfig.setString(conn, "MarkdownEmulation", emulationSelector.getValue());
            UserConfig.setInt(conn, "MarkdownIndent", indentSize);
            UserConfig.setBoolean(conn, "MarkdownTrim", trimCheck.isSelected());
            UserConfig.setBoolean(conn, "MarkdownAppend", appendCheck.isSelected());
            UserConfig.setBoolean(conn, "MarkdownDiscard", discardCheck.isSelected());
        } catch (Exception e) {
//            MyBoxLog.error(e);
            return false;
        }
        return true;
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
