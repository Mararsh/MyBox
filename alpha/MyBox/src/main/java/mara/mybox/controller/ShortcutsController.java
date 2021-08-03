package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.HtmlWriteTools;

import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ShortcutsController extends HtmlViewerController {

    public ShortcutsController() {
        baseTitle = Languages.message("Shortcuts");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            displayHtml();
        } catch (Exception e) {

        }
    }

    public void key(String key1, String key2, String action, String alt) {
        List<String> row = new ArrayList<>();
        row.addAll(Arrays.asList(key1, key2, action, alt));
        table.add(row);
    }

    @Override
    public void displayHtml() {
        try {
            if (table == null) {
                table();
            }
            String htmlStyle = UserConfig.getUserConfigString(baseName + "HtmlStyle", "Default");
            html = "<P class=\"valueText\">" + Languages.message("ShortcutsTips") + "</P>\n"
                    + StringTable.tableDiv(table);
            html = HtmlWriteTools.html(Languages.message("Shortcuts"), htmlStyle, html);
            webView.getEngine().loadContent​(html);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void table() {
        try {
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(Languages.message("FunctionKey"), Languages.message("KeyCombination"),
                    Languages.message("Action"), Languages.message("PossibleAlternative"))
            );
            table = new StringTable(names, Languages.message("Shortcuts"));

            key("F1", "", Languages.message("Start") + " / " + Languages.message("OK") + " / " + Languages.message("Set") + " / " + Languages.message("Query"), "CTRL+e / ALT+e, CTRL+q / ALT+q");
            key("F2", "", Languages.message("Save"), "CTRL+s / ALT+s");
            key("F3", "", Languages.message("Recover") + " / " + Languages.message("Export"), "CTRL+r / ALT+r, CTRL+e / ALT+e ");
            key("F4", "", Languages.message("ControlLeftPane"), "");
            key("F5", "", Languages.message("ControlRightPane"), "");
            key("F6", "", Languages.message("ClosePopup"), "");
            key("F7", "", Languages.message("ControlScopePane"), "");
            key("F8", "", Languages.message("ControlImagePane"), "");
            key("F9", "", Languages.message("CloseStage"), "");
            key("F10", "", Languages.message("RefreshStage"), "");
            key("F11", "", Languages.message("SaveAs"), "CTRL+b / ALT+b");
            key("DELETE", "", Languages.message("Delete"), "CTRL+d / ALT+d");
            key("PAGE_UP", "", Languages.message("Previous"), "ALT+PAGE_UP");
            key("PAGE_DOWN", "", Languages.message("Next"), "ALT+PAGE_DOWN");
            key("HOME", "", Languages.message("First"), "ALT+HOME");
            key("END", "", Languages.message("Last"), "ALT+END");
            key("ESCAPE", "", Languages.message("Cancel") + " / " + Languages.message("Withdraw"), "CTRL+w / ALT+w");

            key("CTRL", "e", Languages.message("Start") + " /" + Languages.message("OK") + " / " + Languages.message("Set") + " / " + Languages.message("Export"), "F1 / ALT+e");
            key("CTRL", "c", Languages.message("Copy"), "ALT+c");
            key("CTRL", "v", Languages.message("Paste"), "ALT+v");
            key("CTRL", "z", Languages.message("Undo"), "ALT+z");
            key("CTRL", "y", Languages.message("Redo"), "ALT+y");
            key("CTRL", "d", Languages.message("Delete"), "DELETE / ALT+d");
            key("CTRL", "x", Languages.message("Crop"), "ALT+x");
            key("CTRL", "s", Languages.message("Save"), "F2 / ALT+s");
            key("CTRL", "b", Languages.message("SaveAs"), "F11 / ALT+b");
            key("CTRL", "f", Languages.message("Find"), "ALT+f");
            key("CTRL", "h", Languages.message("Replace"), "ALT+h");
            key("CTRL", "r", Languages.message("Recover") + " / " + Languages.message("Clear"), "ALT+r");
            key("CTRL", "n", Languages.message("Create"), "");
            key("CTRL", "a", Languages.message("SelectAll"), "ALT+a");
            key("CTRL", "o", Languages.message("SelectNone"), "ALT+o");
            key("CTRL", "g", Languages.message("Clear"), "ALT+g");
            key("CTRL", "w", Languages.message("Cancel") + " / " + Languages.message("Withdraw") + " / " + Languages.message("ReplaceAll"), "ESCAPE");
            key("CTRL", "p", Languages.message("Pop"), "ALT+p");
            key("CTRL", "q", Languages.message("Query"), "ALT+q");
            key("CTRL", "k", Languages.message("PickColor"), "ALT+k");
            key("CTRL", "t", Languages.message("SelectArea"), "ALT+t");
            key("CTRL", "m", Languages.message("MyBoxClipboard"), "ALT+m");
            key("CTRL", "j", Languages.message("SystemClipboard"), "ALT+j");
            key("CTRL", "1", Languages.message("OriginalSize") + " / " + Languages.message("Previous"), "");
            key("CTRL", "2", Languages.message("PaneSize") + " / " + Languages.message("Next"), "");
            key("CTRL", "3", Languages.message("ZoomIn"), "");
            key("CTRL", "4", Languages.message("ZoomOut"), "");
            key("CTRL", "-", Languages.message("DecreaseFontSize"), "");
            key("CTRL", "=", Languages.message("IncreaseFontSize"), "");

            key("ALT", "1", Languages.message("Set") + " / " + Languages.message("Previous"), "F1");
            key("ALT", "2", Languages.message("Increase") + " / " + Languages.message("Next"), "");
            key("ALT", "3", Languages.message("Decrease"), "");
            key("ALT", "4", Languages.message("Filter"), "");
            key("ALT", "5", Languages.message("Invert"), "");
            key("ALT", "PAGE_UP", Languages.message("Previous"), "PAGE_UP");
            key("ALT", "PAGE_DOWN", Languages.message("Next"), "PAGE_DOWN");
            key("ALT", "HOME", Languages.message("First"), "HOME");
            key("ALT", "END", Languages.message("Last"), "END");

            key("s / S", "", Languages.message("Play") + " / " + Languages.message("Pause"), "");
            key("q / Q", "", Languages.message("Stop"), "");
            key("m / M", "", Languages.message("Mute") + " / " + Languages.message("Sound"), "");
            key("f / F", "", Languages.message("FullScreen"), "");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
