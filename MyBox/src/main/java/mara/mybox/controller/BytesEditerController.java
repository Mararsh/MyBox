package mara.mybox.controller;

import java.awt.Desktop;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.stage.Modality;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.FileEditInformation.Line_Break;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.StringTools;
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
    @FXML
    protected TextField bytesNumberInput, hexInput;
    @FXML
    private RadioButton bytesNumberRadio, byteRadio;

    public BytesEditerController() {
        setBytesType();
    }

    @Override
    protected void initializeNext() {
        try {
            super.initializeNext();
            initCharInputTab();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initCharInputTab() {
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
    protected void initLineBreakTab() {
        try {
            lineBreakGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    if (!isSettingValues) {
                        checkLineBreakGroup();
                    }
                }
            });

            hexInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    if (!isSettingValues) {
                        checkBytesHex();
                    }
                }
            });

            bytesNumberInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    if (!isSettingValues) {
                        checkBytesNumber();
                    }
                }
            });

            isSettingValues = true;
            hexInput.setText(AppVaribles.getUserConfigValue(LineBreakValueKey, "0A 0D"));
            bytesNumberInput.setText(AppVaribles.getUserConfigValue(LineBreakWidthKey, "30"));
            if (AppVaribles.getUserConfigValue(BytesLineBreakKey, "Width").equals("Width")) {
                bytesNumberRadio.fire();
            } else {
                byteRadio.fire();
            }
            isSettingValues = false;
            checkLineBreakGroup();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkLineBreakGroup() {
        try {
            isSettingValues = true;
            RadioButton selected = (RadioButton) lineBreakGroup.getSelectedToggle();
            if (AppVaribles.getMessage("BytesNumber").equals(selected.getText())) {
                lineBreak = Line_Break.Width;
            } else if (AppVaribles.getMessage("BytesHex").equals(selected.getText())) {
                lineBreak = Line_Break.Value;
            } else if (AppVaribles.getMessage("LFHex").equals(selected.getText())) {
                lineBreak = Line_Break.LF;
                lineBreakValue = "0A ";
                sourceInformation.setLineBreakValue("0A ");
            } else if (AppVaribles.getMessage("CRHex").equals(selected.getText())) {
                lineBreak = Line_Break.CR;
                lineBreakValue = "0D ";
                sourceInformation.setLineBreakValue("0D ");
            }
            AppVaribles.setUserConfigValue(BytesLineBreakKey, lineBreak.toString());
            sourceInformation.setLineBreak(lineBreak);
            checkBytesHex();
            checkBytesNumber();
            isSettingValues = false;

            if (sourceFile == null) {
                formatMainArea();
                updateInterface(false);
            } else {
                sourceInformation.setTotalNumberRead(false);
                openFile(sourceFile);

            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkBytesHex() {
        try {
            if (lineBreak != Line_Break.Value) {
                hexInput.setStyle(null);
                return;
            }
            final String v = ByteTools.validateTextHex(hexInput.getText());
            if (v == null || v.isEmpty()) {
                hexInput.setStyle(badStyle);
            } else {
                lineBreakValue = v;
                hexInput.setStyle(null);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        isSettingValues = true;
                        hexInput.setText(v);
                        hexInput.end();
                        isSettingValues = false;
                    }
                });
                AppVaribles.setUserConfigValue(LineBreakValueKey, lineBreakValue);
                sourceInformation.setLineBreakValue(lineBreakValue);
                if (!isSettingValues) {
                    if (sourceFile == null) {
                        updateInterface(false);
                    } else {
                        sourceInformation.setObjectsNumber(-1);
                        openFile(sourceFile);
                    }
                }
            }

        } catch (Exception e) {
            hexInput.setStyle(badStyle);
        }

    }

    private void checkBytesNumber() {
        try {
            if (lineBreak != Line_Break.Width) {
                bytesNumberInput.setStyle(null);
                return;
            }
            int v = Integer.valueOf(bytesNumberInput.getText());
            if (v > 0) {
                lineBreakWidth = v;
                bytesNumberInput.setStyle(null);
                AppVaribles.setUserConfigInt(LineBreakWidthKey, v);
                sourceInformation.setLineBreakWidth(lineBreakWidth);
                if (!isSettingValues) {
                    if (sourceFile == null) {
                        logger.debug(lineBreakWidth);
                        updateInterface(false);
                    } else {
                        sourceInformation.setTotalNumberRead(false);
                        openFile(sourceFile);
                    }
                }
            } else {
                bytesNumberInput.setStyle(badStyle);
            }

        } catch (Exception e) {
            bytesNumberInput.setStyle(badStyle);
        }
    }

    @Override
    protected boolean validateFindString(String string) {
        if (isSettingValues) {
            return true;
        }
        final String v = ByteTools.validateTextHex(string);
        if (v == null) {
            findInput.setStyle(badStyle);
            return false;
        } else {
            if (v.length() >= pageSize * 3) {
                popError(AppVaribles.getMessage("FindStringLimitation"));
                findInput.setStyle(badStyle);
                return false;
            }
            findInput.setStyle(null);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    isSettingValues = true;
                    findInput.setText(v);
                    findInput.end();
                    isSettingValues = false;
                }
            });
            return true;
        }
    }

    @Override
    protected boolean checkReplaceString(String string) {
        if (isSettingValues || string.trim().isEmpty()) {
            return true;
        }
        final String v = ByteTools.validateTextHex(string);
        if (v == null) {
            replaceInput.setStyle(badStyle);
            return false;
        } else {
            if (v.length() >= pageSize * 3) {
                popError(AppVaribles.getMessage("FindStringLimitation"));
                replaceInput.setStyle(badStyle);
                return false;
            }
            replaceInput.setStyle(null);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    isSettingValues = true;
                    replaceInput.setText(v);
                    replaceInput.end();
                    isSettingValues = false;
                }
            });
            return true;
        }
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
                AppVaribles.setUserConfigValue(BytesCharsetKey, newValue);
                charsetByUser = !isSettingValues;
                if (!isSettingValues && displayArea != null) {
                    setSecondArea(mainArea.getText());
                }
            }
        });
        currentBox.getSelectionModel().select(AppVaribles.getUserConfigValue(BytesCharsetKey, "UTF-8"));
    }

    @Override
    protected boolean formatMainArea() {
        if (isSettingValues) {
            return true;
        }
        String text = mainArea.getText();
        text = ByteTools.validateTextHex(text);
        if (text != null) {
            if (text.isEmpty()) {
                return true;
            }
            final String hex = ByteTools.formatHex(text, lineBreak, lineBreakWidth, lineBreakValue);
            isSettingValues = true;
            mainArea.setText(hex);
            isSettingValues = false;
            return true;
        } else {
            return false;
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
                currentFound = pos * 3;
            }
        }
    }

    @Override
    protected void setSecondArea(String hexFormat) {
        if (isSettingValues || displayArea == null
                || !splitPane.getItems().contains(displayArea)) {
            return;
        }
        isSettingValues = true;
        LoadingController loadingController = openHandlingStage(Modality.WINDOW_MODAL);
        if (!hexFormat.isEmpty()) {
            String[] lines = hexFormat.split("\n");
            StringBuilder text = new StringBuilder();
            String lineText;
            for (String line : lines) {
                lineText = new String(ByteTools.hexFormatToBytes(line), sourceInformation.getCharset());
                lineText = lineText.replaceAll("\n", " ").replaceAll("\r", " ") + "\n";
                text.append(lineText);
            }
            displayArea.setText(text.toString());
        } else {
            displayArea.clear();
        }
        if (loadingController != null && loadingController.getMyStage() != null) {
            loadingController.getMyStage().close();
        }
        isSettingValues = false;
    }

    @Override
    protected void setSecondAreaSelection() {
        if (isSettingValues || displayArea == null || !splitPane.getItems().contains(displayArea)) {
            return;
        }
        displayArea.deselect();
        IndexRange hexRange = mainArea.getSelection();
        if (hexRange.getLength() == 0) {
            return;
        }
        isSettingValues = true;
        final String text = displayArea.getText();
        if (!text.isEmpty()) {
            IndexRange textRange = ByteTools.textIndex(mainArea.getText(), sourceInformation.getCharset(), hexRange);
            displayArea.selectRange(textRange.getStart(), textRange.getEnd());
            displayArea.setScrollTop(mainArea.getScrollTop());
        }
        isSettingValues = false;
    }

    @FXML
    @Override
    protected void openAction() {
        super.openAction();
        tabPane.getSelectionModel().select(inputTab);
    }

    @FXML
    private void openAscii() {
        try {
            Desktop.getDesktop().browse(new URI("https://en.wikipedia.org/wiki/ASCII"));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void checkFilterStrings() {
        String f = filterInput.getText();
        boolean invalid = f.isEmpty() || !validateFilterStrings()
                || sourceFile == null || mainArea.getText().isEmpty();
        filterButton.setDisable(invalid);
    }

    public boolean validateFilterStrings() {
        if (filterInput.getText().trim().endsWith(",")) {
            filterInput.setStyle(badStyle);
            return false;
        }
        String[] strings = StringTools.splitByComma(filterInput.getText());
        List<String> vs = new ArrayList<>();
        for (String s : strings) {
            String v = ByteTools.validateTextHex(s);
            if (v == null) {
                filterInput.setStyle(badStyle);
                return false;
            }
            if (v.length() >= pageSize * 3) {
                popError(AppVaribles.getMessage("FindStringLimitation"));
                filterInput.setStyle(badStyle);
                return false;
            }
            vs.add(v);
        }
        if (vs.isEmpty()) {
            return false;
        }
        filterStrings = new String[vs.size()];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vs.size(); i++) {
            filterStrings[i] = vs.get(i);
            if (i == 0) {
                sb.append(filterStrings[i]);
            } else {
                sb.append(",").append(filterStrings[i]);
            }
        }
        filterInput.setStyle(null);
        final String fixed = sb.toString();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                isSettingValues = true;
                filterInput.setText(fixed);
                filterInput.end();
                isSettingValues = false;
            }
        });
        return true;

    }

}
