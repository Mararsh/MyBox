package mara.mybox.controller;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Paste extends ControlSheet_Buttons {

    @FXML
    public void sheetPasteMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            List<MenuItem> items = makeSheetPasteMenu();
            if (items == null || items.isEmpty()) {
                return;
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            popMenu.getItems().addAll(items);
            popMenu.getItems().add(new SeparatorMenuItem());

            MenuItem menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateCenter((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public List<MenuItem> makeSheetPasteMenu() {
        List<MenuItem> items = new ArrayList<>();
        try {
            MenuItem menu = new MenuItem(Languages.message("PasteContentInSystemClipboard"));
            menu.setOnAction((ActionEvent event) -> {
                pasteContentInSystemClipboard();
            });
            items.add(menu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return items;
    }

    @Override
    public void pasteContentInSystemClipboard() {
        try {
            if (sheetInputs == null) {
                popError(message("NoData"));
                return;
            }
            String s = TextClipboardTools.getSystemClipboardString();
            if (s == null || s.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            String[] values = s.split("\n");
            if (values.length > sheetInputs.length) {
                if (!PopTools.askSure(message("PastePageColFromSystemClipboard"),
                        MessageFormat.format(message("DataInClipboardMoreThanPage"), values.length, sheetInputs.length))) {
                    return;
                }
            } else if (values.length < sheetInputs.length) {
                if (!PopTools.askSure(message("PastePageColFromSystemClipboard"),
                        MessageFormat.format(message("DataInClipboardLessThanPage"), values.length, sheetInputs.length))) {
                    return;
                }
            }
//            isSettingValues = true;
//            for (int r = 0; r < Math.min(sheetInputs.length, values.length); ++r) {
//                sheetInputs[r][col].setText(values[r]);
//            }
//            isSettingValues = false;
//            sheetChanged();
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

}
