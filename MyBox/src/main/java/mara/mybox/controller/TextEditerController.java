package mara.mybox.controller;

import java.nio.charset.Charset;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.IndexRange;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.data.FileEditInformation;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.TextTools;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class TextEditerController extends FileEditerController {

    public TextEditerController() {
        baseTitle = AppVaribles.message("TextEditer");

        TipsLabelKey = "TextEditerTips";

        setTextType();
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();
            initCharsetTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initCharsetTab() {

        Tooltip tips = new Tooltip(AppVaribles.message("EncodeComments"));
        tips.setFont(new Font(16));
        FxmlControl.setTooltip(currentBox, tips);

        List<String> setNames = TextTools.getCharsetNames();
        currentBox.getItems().addAll(setNames);
        currentBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                changeCurrentCharset();
            }
        });

        if (targetBox != null) {
            targetBox.getItems().addAll(setNames);
            targetBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    targetInformation.setCharset(Charset.forName(newValue));
                    if ("UTF-8".equals(newValue) || "UTF-16BE".equals(newValue)
                            || "UTF-16LE".equals(newValue) || "UTF-32BE".equals(newValue)
                            || "UTF-32LE".equals(newValue)) {
                        targetBomCheck.setDisable(false);
                    } else {
                        targetBomCheck.setDisable(true);
                        if ("UTF-16".equals(newValue) || "UTF-32".equals(newValue)) {
                            targetBomCheck.setSelected(true);
                        } else {
                            targetBomCheck.setSelected(false);
                        }
                    }
                }
            });

            tips = new Tooltip(AppVaribles.message("BOMcomments"));
            tips.setFont(new Font(16));
            FxmlControl.setTooltip(targetBomCheck, tips);
        }
    }

    @Override
    protected void initLineBreakTab() {
        try {
            lineBreakGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkLineBreakGroup();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkLineBreakGroup() {
        try {
            RadioButton selected = (RadioButton) lineBreakGroup.getSelectedToggle();
            if (AppVaribles.message("LF").equals(selected.getText())) {
                targetInformation.setLineBreak(FileEditInformation.Line_Break.LF);
            } else if (AppVaribles.message("CR").equals(selected.getText())) {
                targetInformation.setLineBreak(FileEditInformation.Line_Break.CR);
            } else if (AppVaribles.message("CRLF").equals(selected.getText())) {
                targetInformation.setLineBreak(FileEditInformation.Line_Break.CRLF);
            }
            targetInformation.setLineBreakValue(TextTools.lineBreakValue(targetInformation.getLineBreak()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected boolean validateFindString(String string) {
        if (string.length() >= pageSize) {
            popError(AppVaribles.message("FindStringLimitation"));
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void changeCurrentCharset() {
        sourceInformation.setCharset(Charset.forName(currentBox.getSelectionModel().getSelectedItem()));
        charsetByUser = !isSettingValues;
        if (!isSettingValues && sourceFile != null) {
            openFile(sourceFile);
        }
    }

    @Override
    protected void countCurrentFound() {
        if (!findWhole || sourceInformation.getPagesNumber() <= 1) {
            return;
        }
        currentFound = -1;
        if (sourceInformation.getCurrentFound() >= 0) {
            int pos = (int) (sourceInformation.getCurrentFound() % sourceInformation.getPageSize());
            int p = (int) (sourceInformation.getCurrentFound() / sourceInformation.getPageSize() + 1);
            if (p == currentPage) {
                if (pos > 0 && sourceInformation.getLineBreak() == FileEditInformation.Line_Break.CRLF) {
                    pos -= TextTools.countNumber(mainArea.getText().substring(0, pos), "\n");
                }
                currentFound = pos;
            }
        }
    }

    @Override
    protected void setSecondArea(String text) {
        if (isSettingValues || displayArea == null || !splitPane.getItems().contains(displayArea)) {
            return;
        }
        isSettingValues = true;
        if (!text.isEmpty()) {
            String hex = ByteTools.bytesToHexFormat(text.getBytes(sourceInformation.getCharset()));
            String hexLF = ByteTools.bytesToHexFormat("\n".getBytes(sourceInformation.getCharset())).trim();
            String hexLB = ByteTools.bytesToHexFormat(sourceInformation.getLineBreakValue().getBytes(sourceInformation.getCharset())).trim();
            hex = hex.replaceAll(hexLF, hexLB + "\n");
            if (sourceInformation.isWithBom()) {
                hex = TextTools.bomHex(sourceInformation.getCharset().name()) + " " + hex;
            }
            displayArea.setText(hex);
        } else {
            displayArea.clear();
        }
        isSettingValues = false;
    }

    @Override
    protected void setSecondAreaSelection() {
        if (isSettingValues
                || displayArea == null || !splitPane.getItems().contains(displayArea)) {
            return;
        }
        isSettingValues = true;
        displayArea.deselect();
        final String text = mainArea.getText();
        if (!text.isEmpty()) {
            IndexRange hexRange = TextTools.hexIndex(text, sourceInformation.getCharset(),
                    sourceInformation.getLineBreakValue(), mainArea.getSelection());
            int bomLen = 0;
            if (sourceInformation.isWithBom()) {
                bomLen = TextTools.bomHex(sourceInformation.getCharset().name()).length() + 1;
            }
            displayArea.selectRange(hexRange.getStart() + bomLen, hexRange.getEnd() + bomLen);
            displayArea.setScrollTop(mainArea.getScrollTop());
        }
        isSettingValues = false;
    }

}
