package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import mara.mybox.data.StringTable;
import mara.mybox.tools.HtmlTools;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ShortcutsController extends StringTableController {

    @FXML
    protected HBox iccBox;
    @FXML
    protected ComboBox<String> indexSelector;

    public ShortcutsController() {
        baseTitle = message("Shortcuts");

    }

    @Override
    public void initializeNext() {
        try {
            loadInformation();
        } catch (Exception e) {

        }
    }

    public void key(String key1, String key2, String action, String alt) {
        List<String> row = new ArrayList<>();
        row.addAll(Arrays.asList(key1, key2, action, alt));
        table.add(row);
    }

    @Override
    public void loadInformation() {
        try {
            if (table == null) {
                table();
            }
            html = HtmlTools.html(message("Shortcuts"), style, StringTable.tableDiv(table));
            webView.getEngine().loadContent​(html);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void table() {
        try {
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("FunctionKey"), message("KeyCombination"),
                    message("Action"), message("PossibleAlternative"))
            );
            table = new StringTable(names, message("Shortcuts"));

            key("F1", "", message("Start") + " / " + message("OK") + " / " + message("Set"), "CTRL+e / SHIFT+e");
            key("F2", "", message("Save"), "CTRL+s / SHIFT+s");
            key("F3", "", message("SaveAs"), "CTRL+f / SHIFT+f");
            key("F4", "", message("CloseStage"), "");
            key("F5", "", message("RefreshStage"), "");
            key("F11", "", message("Recover"), "CTRL+r / SHIFT+r");
            key("F12", "", message("MoreControls"), "CTRL+m / SHIFT+m");
            key("DELETE", "", message("Delete"), "CTRL+d / SHIFT+d");
            key("PAGE_UP", "", message("Previous"), "");
            key("PAGE_DOWN", "", message("Next"), "");
            key("ESCAPE", "", message("Cancel"), "");

            key("CTRL", "e", message("Start") + " / " + message("OK") + " / " + message("Set"), "F1 / SHIFT+e");
            key("CTRL", "c", message("Copy"), "SHIFT+c");
            key("CTRL", "v", message("Paste"), "SHIFT+v");
            key("CTRL", "z", message("Undo"), "SHIFT+z");
            key("CTRL", "y", message("Redo"), "SHIFT+y");
            key("CTRL", "d", message("Delete"), "DELETE / SHIFT+d");
            key("CTRL", "x", message("Crop"), "SHIFT+x");
            key("CTRL", "s", message("Save"), "F2 / SHIFT+s");
            key("CTRL", "f", message("SaveAs"), "F3 / SHIFT+f");
            key("CTRL", "r", message("Recover"), "F11 / SHIFT+r");
            key("CTRL", "m", message("MoreControls"), "F12 / SHIFT+m");
            key("CTRL", "n", message("Create"), "");
            key("CTRL", "a", message("SelectAll"), "SHIFT+a");
            key("CTRL", "p", message("Pop"), "SHIFT+p");
            key("CTRL", "1", message("OriginalSize"), "");
            key("CTRL", "2", message("PaneSize"), "");
            key("CTRL", "3", message("ZoomIn"), "");
            key("CTRL", "4", message("ZoomOut"), "");
            key("CTRL", "-", message("DecreaseFontSize"), "");
            key("CTRL", "=", message("IncreaseFontSize"), "");
            key("CTRL", "HOME", message("First"), "");
            key("CTRL", "END", message("Last"), "");

            key("SHIFT", "1", message("Set"), "");
            key("SHIFT", "2", message("Increase"), "");
            key("SHIFT", "3", message("Decrease"), "");
            key("SHIFT", "4", message("Filter"), "");
            key("SHIFT", "5", message("Invert"), "");

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
