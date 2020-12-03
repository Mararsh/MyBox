package mara.mybox.controller;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-12-09
 * @License Apache License Version 2.0
 */
public class BytesEditerController extends FileEditerController {

    @FXML
    protected TextField lbWidthInput, lbBytesInput;
    @FXML
    private RadioButton lbWidthRadio, bytesRadio, lbLFRadio, lbCRRadio, lbCRLFRsadio;

    public BytesEditerController() {
        baseTitle = AppVariables.message("BytesEditer");
        TipsLabelKey = "BytesEditerTips";

        setBytesType();
    }

    @Override
    protected void initLineBreakTab() {
        try {
            super.initLineBreakTab();

            lineBreakGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    if (!isSettingValues) {
                        checkLineBreakGroup();
                    }
                }
            });

            lbBytesInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    if (!isSettingValues) {
                        checkBytesHex();
                    }
                }
            });

            lbWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    if (!isSettingValues) {
                        checkBytesNumber();
                    }
                }
            });

            isSettingValues = true;
            lbBytesInput.setText(AppVariables.getUserConfigValue(baseName + "LineBreakValue", "0D 0A "));
            lbWidthInput.setText(AppVariables.getUserConfigValue(baseName + "LineBreakWidth", "30"));
            String savedLB = AppVariables.getUserConfigValue(baseName + "LineBreak", "Width");
            if (savedLB.equals(Line_Break.Value.toString())) {
                bytesRadio.fire();
            } else if (savedLB.equals(Line_Break.LF.toString())) {
                lbLFRadio.fire();
            } else if (savedLB.equals(Line_Break.CR.toString())) {
                lbCRRadio.fire();
            } else if (savedLB.equals(Line_Break.CRLF.toString())) {
                lbCRLFRsadio.fire();
            } else {
                lbWidthRadio.fire();
            }
            isSettingValues = false;
            checkLineBreakGroup();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            } else if (AppVariables.message("CRLFHex").equals(selected.getText())) {
                lineBreak = Line_Break.CRLF;
                lineBreakValue = "0D 0A ";
                sourceInformation.setLineBreakValue("0D 0A ");
            }
            AppVariables.setUserConfigValue(baseName + "LineBreak", lineBreak.toString());
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
            MyBoxLog.error(e.toString());
        }
    }

    private void checkBytesHex() {
        try {
            if (lineBreak != Line_Break.Value) {
                lbBytesInput.setStyle(null);
                return;
            }
            final String v = ByteTools.validateTextHex(lbBytesInput.getText());
            if (v == null || v.isEmpty()) {
                lbBytesInput.setStyle(badStyle);
            } else {
                lineBreakValue = v;
                lbBytesInput.setStyle(null);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        isSettingValues = true;
                        lbBytesInput.setText(v);
                        lbBytesInput.end();
                        isSettingValues = false;
                    }
                });
                AppVariables.setUserConfigValue(baseName + "LineBreakValue", lineBreakValue);
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
            lbBytesInput.setStyle(badStyle);
        }

    }

    private void checkBytesNumber() {
        try {
            if (lineBreak != Line_Break.Width) {
                lbWidthInput.setStyle(null);
                return;
            }
            int v = Integer.valueOf(lbWidthInput.getText());
            if (v > 0) {
                lineBreakWidth = v;
                lbWidthInput.setStyle(null);
                AppVariables.setUserConfigInt(baseName + "LineBreakWidth", v);
                sourceInformation.setLineBreakWidth(lineBreakWidth);
                if (!isSettingValues) {
                    if (sourceFile == null) {
                        updateInterface(false);
                    } else {
                        sourceInformation.setTotalNumberRead(false);
                        openFile(sourceFile);
                    }
                }
            } else {
                lbWidthInput.setStyle(badStyle);
            }

        } catch (Exception e) {
            lbWidthInput.setStyle(badStyle);
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
                AppVariables.setUserConfigValue(baseName + "Charset", newValue);
                charsetByUser = !isSettingValues;
                if (!isSettingValues) {
                    updatePairArea();
                }
            }
        });
        encodeBox.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "Charset", "UTF-8"));
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
            MyBoxLog.console(lines.length);
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

    @Override
    protected void checkFilterStrings() {
        String f = filterInput.getText();
        boolean invalid = f.isEmpty() || sourceFile == null || mainArea.getText().isEmpty();
        if (!invalid) {
            if (filterType == FileEditInformation.StringFilterType.MatchRegularExpression
                    || filterType == FileEditInformation.StringFilterType.NotMatchRegularExpression
                    || filterType == FileEditInformation.StringFilterType.IncludeRegularExpression
                    || filterType == FileEditInformation.StringFilterType.NotIncludeRegularExpression) {
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

    @FXML
    public void popNumber(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menuItem;
            for (int i = 0; i <= 9; ++i) {
                final String name = i + "";
                menuItem = new MenuItem(name);
                menuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        mainArea.insertText(mainArea.getSelection().getStart(), ByteTools.stringToHexFormat(name));
                    }
                });
                popMenu.getItems().add(menuItem);
            }
            popMenu.getItems().add(new SeparatorMenuItem());

            menuItem = new MenuItem(message("AsciiTable"));
            menuItem.setOnAction((ActionEvent event) -> {
                try {
                    browseURI(new URI("https://www.ascii-code.com/"));
//           browseURI(new URI("https://en.wikipedia.org/wiki/ASCII"));  // this address is unavaliable to someones
                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                }
            });
            popMenu.getItems().add(menuItem);

            popMenu.getItems().add(new SeparatorMenuItem());
            MenuItem menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popLowerLetter(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menuItem;
            for (char i = 'a'; i <= 'z'; ++i) {
                final String name = i + "";
                menuItem = new MenuItem(name);
                menuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        mainArea.insertText(mainArea.getSelection().getStart(), ByteTools.stringToHexFormat(name));
                    }
                });
                popMenu.getItems().add(menuItem);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            MenuItem menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popUpperLetter(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menuItem;
            for (char i = 'A'; i <= 'Z'; ++i) {
                final String name = i + "";
                menuItem = new MenuItem(name);
                menuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        mainArea.insertText(mainArea.getSelection().getStart(), ByteTools.stringToHexFormat(name));
                    }
                });
                popMenu.getItems().add(menuItem);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            MenuItem menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popSpecial(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menuItem;

            List<String> symbolList = Arrays.asList("LF", "CR", AppVariables.message("Space"),
                    "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", "-",
                    ",", ".", "/", ":", ";", "<", "=", ">", "?", "@", "[", "]", "\\", "^", "_", "`",
                    "{", "}", "|", "~");
            for (String symbol : symbolList) {
                menuItem = new MenuItem(symbol);
                menuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        String value = symbol;
                        if (value.equals(AppVariables.message("Space"))) {
                            value = " ";
                        } else if (value.equals("LF")) {
                            value = "\n";
                        } else if (value.equals("CR")) {
                            value = "\r";
                        }
                        mainArea.insertText(mainArea.getSelection().getStart(), ByteTools.stringToHexFormat(value));
                    }
                });
                popMenu.getItems().add(menuItem);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            MenuItem menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
