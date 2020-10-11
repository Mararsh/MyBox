package mara.mybox.controller;

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
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Line_Break;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

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
        baseTitle = AppVariables.message("BytesEditer");

        TipsLabelKey = "BytesEditerTips";

        setBytesType();
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initCharInputTab();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initCharInputTab() {
        List<String> symbolList = Arrays.asList("LF", "CR", AppVariables.message("Space"), "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", "-",
                ",", ".", "/", ":", ";", "<", "=", ">", "?", "@", "[", "]", "\\", "^", "_", "`",
                "{", "}", "|", "~");
        symbolBox.getItems().addAll(symbolList);
        symbolBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                String s = newValue;
                if (newValue.equals(AppVariables.message("Space"))) {
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
        for (char i = 'A'; i <= 'Z'; ++i) {
            capitalList.add(i + "");
        }
        capitalBox.getItems().addAll(capitalList);
        capitalBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                mainArea.insertText(mainArea.getSelection().getStart(), ByteTools.stringToHexFormat(newValue));
            }
        });

        List<String> smallList = new ArrayList<>();
        for (char i = 'a'; i <= 'z'; ++i) {
            smallList.add(i + "");
        }
        smallBox.getItems().addAll(smallList);
        smallBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                mainArea.insertText(mainArea.getSelection().getStart(), ByteTools.stringToHexFormat(newValue));
            }
        });

        List<String> numberList = new ArrayList<>();
        for (char i = '0'; i <= '9'; ++i) {
            numberList.add(i + "");
        }
        numberBox.getItems().addAll(numberList);
        numberBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                mainArea.insertText(mainArea.getSelection().getStart(), ByteTools.stringToHexFormat(newValue));
            }
        });

        inputPane.expandedProperty().addListener(
                (ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    AppVariables.setUserConfigValue(baseName + "InputPane", inputPane.isExpanded());
                });
        inputPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "InputPane", true));
    }

    @Override
    protected void initLineBreakTab() {
        try {
            super.initLineBreakTab();

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
            hexInput.setText(AppVariables.getUserConfigValue(LineBreakValueKey, "0A 0D"));
            bytesNumberInput.setText(AppVariables.getUserConfigValue(LineBreakWidthKey, "30"));
            if (AppVariables.getUserConfigValue(BytesLineBreakKey, "Width").equals("Width")) {
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
            if (AppVariables.message("BytesNumber").equals(selected.getText())) {
                lineBreak = Line_Break.Width;
            } else if (AppVariables.message("BytesHex").equals(selected.getText())) {
                lineBreak = Line_Break.Value;
            } else if (AppVariables.message("LFHex").equals(selected.getText())) {
                lineBreak = Line_Break.LF;
                lineBreakValue = "0A ";
                sourceInformation.setLineBreakValue("0A ");
            } else if (AppVariables.message("CRHex").equals(selected.getText())) {
                lineBreak = Line_Break.CR;
                lineBreakValue = "0D ";
                sourceInformation.setLineBreakValue("0D ");
            }
            AppVariables.setUserConfigValue(BytesLineBreakKey, lineBreak.toString());
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
                AppVariables.setUserConfigValue(LineBreakValueKey, lineBreakValue);
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
                AppVariables.setUserConfigInt(LineBreakWidthKey, v);
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
            findArea.setStyle(badStyle);
            return false;
        } else {
            if (v.length() >= sourceInformation.getPageSize() * 3) {
                popError(AppVariables.message("FindStringLimitation"));
                findArea.setStyle(badStyle);
                return false;
            }
            findArea.setStyle(null);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    isSettingValues = true;
                    findArea.setText(v);
                    findArea.end();
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
            replaceArea.setStyle(badStyle);
            return false;
        } else {
            if (v.length() >= sourceInformation.getPageSize() * 3) {
                popError(AppVariables.message("FindStringLimitation"));
                replaceArea.setStyle(badStyle);
                return false;
            }
            replaceArea.setStyle(null);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    isSettingValues = true;
                    replaceArea.setText(v);
                    replaceArea.end();
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
        encodeBox.getItems().addAll(setNames);
        encodeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                sourceInformation.setCharset(Charset.forName(newValue));
                AppVariables.setUserConfigValue(BytesCharsetKey, newValue);
                charsetByUser = !isSettingValues;
                if (!isSettingValues) {
                    updatePairArea();
                }
            }
        });
        encodeBox.getSelectionModel().select(AppVariables.getUserConfigValue(BytesCharsetKey, "UTF-8"));
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
        currentFound = null;
        if (sourceInformation.getCurrentFound() != null) {
            long start = sourceInformation.getCurrentFound().getStart();
            long end = sourceInformation.getCurrentFound().getEnd();
            int pos = (int) (start % sourceInformation.getPageSize());
            int p = (int) (start / sourceInformation.getPageSize() + 1);
            if (p == currentPage) {
                int pstart = pos * 3;
                int pend = pstart + (int) (end - start);
                currentFound = new IndexRange(pstart, pend);
            }
        }
    }

    @FXML
    @Override
    public void refreshPairAction() {
        if (isSettingValues || !splitPane.getItems().contains(rightPane)) {
            return;
        }
        isSettingValues = true;
        LoadingController loadingController = openHandlingStage(Modality.WINDOW_MODAL);
        String text = mainArea.getText();
        if (!text.isEmpty()) {
            String[] lines = text.split("\n");
            StringBuilder bytes = new StringBuilder();
            String lineText;
            for (String line : lines) {
                lineText = new String(ByteTools.hexFormatToBytes(line), sourceInformation.getCharset());
                lineText = lineText.replaceAll("\n|\r", " ") + "\n";
                bytes.append(lineText);
            }
            pairArea.setText(bytes.toString());
            setPairAreaSelection();
        } else {
            pairArea.clear();
        }
        if (loadingController != null) {
            loadingController.closeStage();
        }
        isSettingValues = false;
    }

    @Override
    protected void setPairAreaSelection() {
        if (isSettingValues || !splitPane.getItems().contains(rightPane)) {
            return;
        }
        pairArea.deselect();
        IndexRange hexRange = mainArea.getSelection();
        if (hexRange.getLength() == 0) {
            return;
        }
        isSettingValues = true;
        final String text = pairArea.getText();
        if (!text.isEmpty()) {
            IndexRange textRange = ByteTools.textIndex(mainArea.getText(), sourceInformation.getCharset(), hexRange);
            pairArea.selectRange(textRange.getStart(), textRange.getEnd());
            pairArea.setScrollTop(mainArea.getScrollTop());
        }
        isSettingValues = false;
    }

    @FXML
    @Override
    public void selectSourceFile() {
        super.selectSourceFile();
        inputPane.setExpanded(true);
    }

    @FXML
    private void openAscii() {
        try {
            browseURI(new URI("https://www.ascii-code.com/"));
//           browseURI(new URI("https://en.wikipedia.org/wiki/ASCII"));  // this address in unavaliable to someones
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void checkFilterStrings() {
        String f = filterInput.getText();
        boolean invalid = f.isEmpty() || sourceFile == null || mainArea.getText().isEmpty();
        if (!invalid) {
            if (filterType == FileEditInformation.StringFilterType.MatchRegularExpression
                    || filterType == FileEditInformation.StringFilterType.NotMatchRegularExpression) {
                filterStrings = new String[1];
                filterStrings[0] = filterInput.getText();
            } else {
                invalid = !validateFilterStrings();
            }
        }
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
            if (v.length() >= sourceInformation.getPageSize() * 3) {
                popError(AppVariables.message("FindStringLimitation"));
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
        for (int i = 0; i < vs.size(); ++i) {
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
