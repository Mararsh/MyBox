package mara.mybox.controller;

import java.nio.charset.Charset;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.IndexRange;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import mara.mybox.data.FileEditInformation;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @Description
 * @License Apache License Version 2.0
 */
public class TextEditerController extends FileEditerController {

    public TextEditerController() {
        baseTitle = AppVariables.message("TextEditer");

        TipsLabelKey = "TextEditerTips";

        setTextType();
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initCharsetTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initCharsetTab() {
        if (encodePane == null) {
            return;
        }
        encodePane.expandedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    AppVariables.setUserConfigValue(baseName + "EncodePane", encodePane.isExpanded());
                });
        encodePane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "EncodePane", true));

        Tooltip tips = new Tooltip(AppVariables.message("EncodeComments"));
        tips.setFont(new Font(16));
        FxmlControl.setTooltip(encodeBox, tips);

        List<String> setNames = TextTools.getCharsetNames();
        encodeBox.getItems().addAll(setNames);
        encodeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
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

            tips = new Tooltip(AppVariables.message("BOMcomments"));
            tips.setFont(new Font(16));
            FxmlControl.setTooltip(targetBomCheck, tips);
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
            logger.error(e.toString());
        }
    }

    protected void checkLineBreakGroup() {
        try {
            RadioButton selected = (RadioButton) lineBreakGroup.getSelectedToggle();
            if (AppVariables.message("LF").equals(selected.getText())) {
                targetInformation.setLineBreak(FileEditInformation.Line_Break.LF);
            } else if (AppVariables.message("CR").equals(selected.getText())) {
                targetInformation.setLineBreak(FileEditInformation.Line_Break.CR);
            } else if (AppVariables.message("CRLF").equals(selected.getText())) {
                targetInformation.setLineBreak(FileEditInformation.Line_Break.CRLF);
            }
            targetInformation.setLineBreakValue(TextTools.lineBreakValue(targetInformation.getLineBreak()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected boolean validateFindString(String string) {
        if (string.length() >= sourceInformation.getPageSize()) {
            popError(AppVariables.message("FindStringLimitation"));
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void changeCurrentCharset() {
        sourceInformation.setCharset(Charset.forName(encodeBox.getSelectionModel().getSelectedItem()));
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
        currentFound = null;
        if (sourceInformation.getCurrentFound() != null) {
            long start = sourceInformation.getCurrentFound().getStart();
            long end = sourceInformation.getCurrentFound().getEnd();
            int pos = (int) (start % sourceInformation.getPageSize());
            int p = (int) (start / sourceInformation.getPageSize() + 1);
            if (p == currentPage) {
                if (pos > 0 && sourceInformation.getLineBreak() == FileEditInformation.Line_Break.CRLF) {
                    pos -= StringTools.countNumber(mainArea.getText().substring(0, pos), "\n");
                }
                int pstart = pos;
                int pend = pstart + (int) (end - start);
                currentFound = new IndexRange(pstart, pend);
            }
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
