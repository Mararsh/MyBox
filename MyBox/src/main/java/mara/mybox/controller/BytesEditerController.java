package mara.mybox.controller;

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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.IndexRange;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import mara.mybox.data.FileEditInformation;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlWindow;
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
public class BytesEditerController extends BaseFileEditerController {

    protected Popup valuePop;

    @FXML
    protected TextField lbWidthInput, lbBytesInput;
    @FXML
    protected RadioButton lbWidthRadio, bytesRadio, lbLFRadio, lbCRRadio, lbCRLFRsadio;

    public BytesEditerController() {
        baseTitle = AppVariables.message("BytesEditer");
        TipsLabelKey = "BytesEditerTips";

    }

    @Override
    public void setFileType() {
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
                validMainArea();
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
    protected void initCharsetTab() {
        List<String> setNames = TextTools.getCharsetNames();
        encodeSelector.getItems().addAll(setNames);
        encodeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                sourceInformation.setCharset(Charset.forName(newValue));
                AppVariables.setUserConfigValue(baseName + "Charset", newValue);
                charsetByUser = true;
                refreshPairAction();
                updateNumbers(fileChanged.get());

            }
        });
        encodeSelector.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "Charset", "UTF-8"));
    }

    @Override
    protected boolean validMainArea() {
        return ByteTools.isBytesHex(mainArea.getText());
    }

    @Override
    protected boolean formatMainArea() {
        String text = mainArea.getText();
        text = ByteTools.validateTextHex(text);
        if (text != null) {
            if (text.isEmpty()) {
                return true;
            }
            String hex = ByteTools.formatHex(text, lineBreak, lineBreakWidth, lineBreakValue);
            isSettingValues = true;
            mainArea.setText(hex);
            isSettingValues = false;
            return true;
        } else {
            popError(message("InvalidData"));
            return false;
        }
    }

    @FXML
    @Override
    public void refreshPairAction() {
        if (pairArea.isDisable() || !splitPane.getItems().contains(rightPane)) {
            return;
        }
        pairArea.setDisable(true);
        SingletonTask pairTask = new SingletonTask<Void>() {

            private String pairText;

            @Override
            protected boolean handle() {
                try {
                    String text = mainArea.getText();
                    if (!text.isEmpty()) {
                        String[] lines = text.split("\n");
                        StringBuilder bytes = new StringBuilder();
                        String lineText;
                        for (String line : lines) {
                            byte[] hex = ByteTools.hexFormatToBytes(line);
                            if (hex == null) {
                                error = message("InvalidData");
                                return false;
                            }
                            lineText = new String(hex, sourceInformation.getCharset());
                            lineText = lineText.replaceAll("\n|\r", " ") + "\n";
                            bytes.append(lineText);
                        }
                        pairText = bytes.toString();
                    } else {
                        pairText = "";
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (pairText.isEmpty()) {
                    pairArea.clear();
                } else {
                    isSettingValues = true;
                    pairArea.setText(pairText);
                    pairArea.setScrollLeft(mainArea.getScrollLeft());
                    pairArea.setScrollTop(mainArea.getScrollTop());
                    isSettingValues = false;
                    setPairAreaSelection();
                }
            }

            @Override
            protected void finalAction() {
                pairArea.setDisable(false);
            }
        };
        pairTask.setSelf(pairTask);
        Thread thread = new Thread(pairTask);
        thread.setDaemon(false);
        thread.start();
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
    public void popValues(MouseEvent mouseEvent) {
        try {
            popup = FxmlWindow.popWindow(myController, mouseEvent);
            if (popup == null) {
                return;
            }
            Object object = popup.getUserData();
            if (object != null && object instanceof PopNodesController) {
                PopNodesController controller = (PopNodesController) object;
                controller.setParameters(myController);
                makeMainAreaContextMenu(controller);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void makeMainAreaContextMenu(PopNodesController controller) {
        try {
            super.makeMainAreaContextMenu(controller);
            controller.addNode(new Separator());

            List<Node> number = new ArrayList<>();
            for (int i = 0; i <= 9; ++i) {
                String s = i + "";
                Button button = new Button(s);
                String value = ByteTools.stringToHexFormat(s);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        mainArea.insertText(mainArea.getSelection().getStart(), value);
                    }
                });
                FxmlControl.setTooltip(button, value);
                number.add(button);
            }
            controller.addFlowPane(number);
            controller.addNode(new Separator());

            List<Node> AZ = new ArrayList<>();
            for (char i = 'A'; i <= 'Z'; ++i) {
                String s = i + "";
                String value = ByteTools.stringToHexFormat(s);
                Button button = new Button(s);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        mainArea.insertText(mainArea.getSelection().getStart(), value);
                    }
                });
                FxmlControl.setTooltip(button, value);
                AZ.add(button);
            }
            controller.addFlowPane(AZ);
            controller.addNode(new Separator());

            List<Node> az = new ArrayList<>();
            for (char i = 'a'; i <= 'z'; ++i) {
                String s = i + "";
                String value = ByteTools.stringToHexFormat(s);
                Button button = new Button(s);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        mainArea.insertText(mainArea.getSelection().getStart(), value);
                    }
                });
                FxmlControl.setTooltip(button, value);
                az.add(button);
            }
            controller.addFlowPane(az);
            controller.addNode(new Separator());

            List<String> names = Arrays.asList("LF", "CR", AppVariables.message("Space"),
                    "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", "-",
                    ",", ".", "/", ":", ";", "<", "=", ">", "?", "@", "[", "]", "\\", "^", "_", "`",
                    "{", "}", "|", "~");
            List<Node> special = new ArrayList<>();
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                Button button = new Button(name);
                if (name.equals(AppVariables.message("Space"))) {
                    name = " ";
                } else if (name.equals("LF")) {
                    name = "\n";
                } else if (name.equals("CR")) {
                    name = "\r";
                }
                String value = ByteTools.stringToHexFormat(name);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        mainArea.insertText(mainArea.getSelection().getStart(), value);
                    }
                });
                FxmlControl.setTooltip(button, value);
                special.add(button);
            }
            controller.addFlowPane(special);
            controller.addNode(new Separator());

            Hyperlink link = new Hyperlink(message("AsciiTable"));
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openLink("https://www.ascii-code.com/");
                }
            });
            controller.addNode(link);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
