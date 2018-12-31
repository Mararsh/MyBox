package mara.mybox.controller;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import mara.mybox.fxml.FxmlTools;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.FileEditInformationFactory.Edit_Type;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.TextTools;

/**
 * @Author Mara
 * @CreateDate 2018-12-09
 * @Description
 * @License Apache License Version 2.0
 */
public class BytesEditerController extends FileEditerController {

    @FXML
    protected ComboBox<String> symbolBox, capitalBox, smallBox, numberBox;

    public BytesEditerController() {
        editType = Edit_Type.Bytes;
        FilePathKey = "BytesFilePathKey";
        DisplayKey = "BytesEditerDisplayText";

        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("*", "*.*"));
            }
        };
    }

    @Override
    protected void initializeNext() {
        try {
            super.initializeNext();
            initInputTab();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initInputTab() {
        Tooltip tips = new Tooltip(getMessage("BytesEditComments"));
        tips.setFont(new Font(16));
        FxmlTools.quickTooltip(mainArea, tips);

        List<String> symbolList = Arrays.asList(
                "LF", "CR", AppVaribles.getMessage("Space"), "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", "-",
                ",", ".", "/", ":", ";", "<", "=", ">", "?", "@", "[", "]", "\\", "^", "_", "`",
                "{", "}", "|", "~");
        symbolBox.getItems().addAll(symbolList);
        symbolBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                String s = newValue;
                if (newValue.equals(AppVaribles.getMessage("Space"))) {
                    s = " ";
                } else if (newValue.equals("LF")) {
                    s = "\n";
                } else if (newValue.equals("CR")) {
                    s = "\r";
                }
                mainArea.insertText(mainArea.getSelection().getStart(), ByteTools.stringToHexFormat(s));
            }
        });

        List<String> capitalList = new ArrayList<>();
        for (char i = 'A'; i <= 'Z'; i++) {
            capitalList.add(i + "");
        }
        capitalBox.getItems().addAll(capitalList);
        capitalBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                mainArea.insertText(mainArea.getSelection().getStart(), ByteTools.stringToHexFormat(newValue));
            }
        });

        List<String> smallList = new ArrayList<>();
        for (char i = 'a'; i <= 'z'; i++) {
            smallList.add(i + "");
        }
        smallBox.getItems().addAll(smallList);
        smallBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                mainArea.insertText(mainArea.getSelection().getStart(), ByteTools.stringToHexFormat(newValue));
            }
        });

        List<String> numberList = new ArrayList<>();
        for (char i = '0'; i <= '9'; i++) {
            numberList.add(i + "");
        }
        numberBox.getItems().addAll(numberList);
        numberBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                mainArea.insertText(mainArea.getSelection().getStart(), ByteTools.stringToHexFormat(newValue));
            }
        });

    }

    @Override
    protected void initDisplayTab() {
        super.initDisplayTab();
        List<String> setNames = TextTools.getCharsetNames();
        currentBox.getItems().addAll(setNames);
        currentBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                sourceInformation.setCharset(Charset.forName(newValue));
                charsetByUser = !isSettingValues;
                if (!isSettingValues && displayArea != null) {
                    setSecondArea(mainArea.getText());
                }
            }
        });
    }

    @Override
    protected void checkDisplay() {
        super.checkDisplay();
        charsetLabel.setDisable(!displayCheck.isSelected());
        currentBox.setDisable(!displayCheck.isSelected());
    }

    @Override
    protected void setSecondArea(String hexFormat) {
        if (isSettingValues || displayArea == null || !splitPane.getItems().contains(displayArea)) {
            return;
        }
        isSettingValues = true;
        if (!hexFormat.isEmpty()) {
            String hex = hexFormat;
            if (sourceInformation.isWithBom()) {
                hex = hexFormat.substring(TextTools.bomHex(sourceInformation.getCharset().name()).length() + 1);
            }
            String text = new String(ByteTools.hexFormatToBytes(hex), sourceInformation.getCharset());
            displayArea.setText(text);
        } else {
            displayArea.clear();
        }
        isSettingValues = false;
    }

    @Override
    protected void setSecondAreaSelection() {

    }

}
