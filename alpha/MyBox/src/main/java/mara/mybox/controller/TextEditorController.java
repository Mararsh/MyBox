package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.menu.MenuTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class TextEditorController extends BaseTextController {

    public TextEditorController() {
        baseTitle = Languages.message("TextEditer");
        TipsLabelKey = "TextEditerTips";
    }

    @Override
    public void setFileType() {
        setTextType();
    }

    @Override
    protected void initPairBox() {
        try {
            super.initPairBox();
            if (pairArea == null) {
                return;
            }
            pairArea.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    MenuBytesEditController.openBytes(myController, pairArea, event);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void refreshPairAction() {
        if (isSettingValues || pairArea == null) {
            return;
        }
        isSettingValues = true;
        String text = mainArea.getText();
        if (!text.isEmpty()) {
            String hex = ByteTools.bytesToHexFormat(text.getBytes(sourceInformation.getCharset()));
            String hexLF = ByteTools.bytesToHexFormat("\n".getBytes(sourceInformation.getCharset())).trim();
            String hexLB = ByteTools.bytesToHexFormat(sourceInformation.getLineBreakValue().getBytes(sourceInformation.getCharset())).trim();
            hex = hex.replaceAll(hexLF, hexLB + "\n");
            if (sourceInformation.isWithBom()) {
                hex = TextTools.bomHex(sourceInformation.getCharset().name()) + " " + hex;
            }
            pairArea.setText(hex);
            setPairAreaSelection();
        } else {
            pairArea.clear();
        }
        isSettingValues = false;
    }

    @Override
    protected void setPairAreaSelection() {
        if (isSettingValues || pairArea == null
                || !splitPane.getItems().contains(rightPane)) {
            return;
        }
        if (pairTask != null) {
            pairTask.cancel();
        }
        pairTask = new FxTask<Void>(this) {
            private IndexRange hexRange;
            private int bomLen;

            @Override
            protected boolean handle() {
                try {
                    hexRange = null;
                    final String text = mainArea.getText();
                    if (!text.isEmpty()) {
                        hexRange = TextTools.hexIndex(this, text, sourceInformation.getCharset(),
                                sourceInformation.getLineBreakValue(), mainArea.getSelection());
                        bomLen = 0;
                        if (sourceInformation.isWithBom()) {
                            bomLen = TextTools.bomHex(sourceInformation.getCharset().name()).length() + 1;
                        }
                    }
                    return hexRange != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                isSettingValues = true;
                pairArea.deselect();
                pairArea.selectRange(hexRange.getStart() + bomLen, hexRange.getEnd() + bomLen);
                pairArea.setScrollTop(mainArea.getScrollTop());
                isSettingValues = false;
            }

            @Override
            protected void whenCanceled() {
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(pairTask, rightPane);
    }

    @FXML
    @Override
    public void saveAsAction() {
        TextEditorSaveAsController.open(this);
    }

    @FXML
    @Override
    public boolean popAction() {
        TextPopController.openInput(this, mainArea);
        return true;
    }

    @FXML
    public void popBytesAction() {
        BytesPopController.open(this, pairArea);
    }

    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        List<MenuItem> items = MenuTools.initMenu(message("File"));
        MenuItem menu;

        if (sourceFile != null) {
            menu = new MenuItem(message("Format"), StyleTools.getIconImageView("iconFormat.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                TextEditorFormatController.open(this);
            });
            items.add(menu);
        }

        items.addAll(super.fileMenuItems(fevent));
        return items;
    }

    /*
        static
     */
    public static TextEditorController open() {
        try {
            TextEditorController controller = (TextEditorController) WindowTools.openStage(Fxmls.TextEditorFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static TextEditorController open(File file) {
        TextEditorController controller = open();
        if (controller != null) {
            controller.sourceFileChanged(file);
        }
        return controller;
    }

    public static TextEditorController edit(String texts) {
        TextEditorController controller = open();
        if (controller != null) {
            controller.loadContents(texts);
        }
        return controller;
    }

}
