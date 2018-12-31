package mara.mybox.controller;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.IndexRange;
import javafx.stage.FileChooser;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.FileEditInformationFactory.Edit_Type;
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
        editType = Edit_Type.Text;
        FilePathKey = "TextFilePathKey";
        DisplayKey = "TextEditerDisplayHex";

        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("*", "*.*"));
                add(new FileChooser.ExtensionFilter("txt", "*.txt"));
                add(new FileChooser.ExtensionFilter("html", "*.html", "*.htm"));
            }
        };
    }

    @Override
    protected void initializeNext() {
        try {
            super.initializeNext();
            initCharsetTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    protected void initCharsetTab() {
        List<String> setNames = TextTools.getCharsetNames();
        currentBox.getItems().addAll(setNames);
        currentBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                changeCurrentCharset();
            }
        });

        if (targetBox != null) {
            targetBox.getItems().addAll(setNames);
            targetBox.valueProperty().addListener(new ChangeListener<String>() {
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
    protected void setSecondArea(String text) {
        if (isSettingValues || displayArea == null || !splitPane.getItems().contains(displayArea)) {
            return;
        }
        isSettingValues = true;
        if (!text.isEmpty()) {
            String hex = ByteTools.bytesToHexFormat(text.getBytes(sourceInformation.getCharset()),
                    TextTools.lineBreakHexFormat(sourceInformation.getLineBreak()));
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
                    sourceInformation.getLineBreak(), mainArea.getSelection());
            if (sourceInformation.isWithBom()) {
                String bom = TextTools.bomHex(sourceInformation.getCharset().name());
                displayArea.selectRange(hexRange.getStart() + bom.length() + 1, hexRange.getEnd() + bom.length() + 1);
            } else {
                displayArea.selectRange(hexRange.getStart(), hexRange.getEnd());
            }
            displayArea.setScrollTop(mainArea.getScrollTop());
        }
        isSettingValues = false;
    }

}
