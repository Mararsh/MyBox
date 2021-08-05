package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.IndexRange;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.input.ContextMenuEvent;
import mara.mybox.data.FileEditInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Languages;

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
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initLineBreakTab() {
        try {
            super.initLineBreakTab();
            if (lineBreakGroup == null) {
                return;
            }
            lineBreakGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkLineBreakGroup();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkLineBreakGroup() {
        try {
            RadioButton selected = (RadioButton) lineBreakGroup.getSelectedToggle();
            if (Languages.message("LF").equals(selected.getText())) {
                targetInformation.setLineBreak(FileEditInformation.Line_Break.LF);
            } else if (Languages.message("CR").equals(selected.getText())) {
                targetInformation.setLineBreak(FileEditInformation.Line_Break.CR);
            } else if (Languages.message("CRLF").equals(selected.getText())) {
                targetInformation.setLineBreak(FileEditInformation.Line_Break.CRLF);
            }
            targetInformation.setLineBreakValue(TextTools.lineBreakValue(targetInformation.getLineBreak()));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

}
