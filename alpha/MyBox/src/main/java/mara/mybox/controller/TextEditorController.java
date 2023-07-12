package mara.mybox.controller;

import java.io.File;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.IndexRange;
import javafx.scene.input.ContextMenuEvent;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class TextEditorController extends BaseFileEditorController {

    public TextEditorController() {
        baseTitle = Languages.message("TextEditer");
        TipsLabelKey = "TextEditerTips";
    }

    @Override
    public void setFileType() {
        setTextType();
    }

    @Override
    protected void initLineBreakGroup() {
        try {
            String savedLB = UserConfig.getString(baseName + "LineBreak", Line_Break.LF.toString());
            if (savedLB.equals(Line_Break.CR.toString())) {
                crRadio.setSelected(true);
            } else if (savedLB.equals(Line_Break.CRLF.toString())) {
                crlfRadio.setSelected(true);
            } else {
                lfRadio.setSelected(true);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void checkLineBreakGroup() {
        try {
            if (crRadio.isSelected()) {
                lineBreak = Line_Break.CR;
            } else if (crlfRadio.isSelected()) {
                lineBreak = Line_Break.CRLF;
            } else {
                lineBreak = Line_Break.LF;
            }
            UserConfig.setString(baseName + "LineBreak", lineBreak.toString());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
                    MenuBytesEditController.open(myController, pairArea, event);
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
        isSettingValues = true;
        pairArea.deselect();
        final String text = mainArea.getText();
        if (!text.isEmpty()) {
            IndexRange hexRange = TextTools.hexIndex(text, sourceInformation.getCharset(),
                    sourceInformation.getLineBreakValue(), mainArea.getSelection());
            int bomLen = 0;
            if (sourceInformation.isWithBom()) {
                bomLen = TextTools.bomHex(sourceInformation.getCharset().name()).length() + 1;
            }
            pairArea.selectRange(hexRange.getStart() + bomLen, hexRange.getEnd() + bomLen);
            pairArea.setScrollTop(mainArea.getScrollTop());
        }
        isSettingValues = false;
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
            controller.openFile(file);
        }
        return controller;
    }

}
