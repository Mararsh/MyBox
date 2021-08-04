package mara.mybox.controller;

import java.nio.charset.Charset;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.NodeTools.badStyle;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
        baseTitle = Languages.message("BytesEditer");
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
            lbBytesInput.setText(UserConfig.getUserConfigString(baseName + "LineBreakValue", "0D 0A "));
            lbWidthInput.setText(UserConfig.getUserConfigString(baseName + "LineBreakWidth", "30"));
            String savedLB = UserConfig.getUserConfigString(baseName + "LineBreak", "Width");
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
            if (Languages.message("BytesNumber").equals(selected.getText())) {
                lineBreak = Line_Break.Width;
            } else if (Languages.message("BytesHex").equals(selected.getText())) {
                lineBreak = Line_Break.Value;
            } else if (Languages.message("LFHex").equals(selected.getText())) {
                lineBreak = Line_Break.LF;
                lineBreakValue = "0A ";
                sourceInformation.setLineBreakValue("0A ");
            } else if (Languages.message("CRHex").equals(selected.getText())) {
                lineBreak = Line_Break.CR;
                lineBreakValue = "0D ";
                sourceInformation.setLineBreakValue("0D ");
            } else if (Languages.message("CRLFHex").equals(selected.getText())) {
                lineBreak = Line_Break.CRLF;
                lineBreakValue = "0D 0A ";
                sourceInformation.setLineBreakValue("0D 0A ");
            }
            UserConfig.setUserConfigString(baseName + "LineBreak", lineBreak.toString());
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
                UserConfig.setUserConfigString(baseName + "LineBreakValue", lineBreakValue);
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
                UserConfig.setUserConfigInt(baseName + "LineBreakWidth", v);
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
                UserConfig.setUserConfigString(baseName + "Charset", newValue);
                charsetByUser = true;
                refreshPairAction();
                updateNumbers(fileChanged.get());

            }
        });
        encodeSelector.getSelectionModel().select(UserConfig.getUserConfigString(baseName + "Charset", "UTF-8"));
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
            popError(Languages.message("InvalidData"));
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
                                error = Languages.message("InvalidData");
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

    @FXML
    @Override
    public void popButtons(MouseEvent mouseEvent) {
        MenuBytesEditController.open(myController, mainArea, mouseEvent);
    }

    @Override
    public void makeEditContextMenu(Node node) {
        try {
            if (node == mainArea) {
                node.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                    @Override
                    public void handle(ContextMenuEvent event) {
                        MenuBytesEditController.open(myController, node, event);
                    }
                });
            } else {
                super.makeEditContextMenu(node);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
