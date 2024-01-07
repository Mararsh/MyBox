package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.Popup;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-12-09
 * @License Apache License Version 2.0
 */
public class BytesEditorController extends BaseTextController {

    protected Popup valuePop;

    @FXML
    protected TextField lbWidthInput, lbBytesInput;
    @FXML
    protected RadioButton lbWidthRadio, bytesRadio, lbLFRadio, lbCRRadio, lbCRLFRsadio;

    public BytesEditorController() {
        baseTitle = message("BytesEditer");
        TipsLabelKey = "BytesEditerTips";
    }

    @Override
    public void setFileType() {
        setBytesType();
    }

    @FXML
    @Override
    public void refreshAction() {
        try {
            if (sourceFile == null) {
                formatMainArea();
                updateInterface(false);
            } else {
                sourceInformation.setTotalNumberRead(false);
                openFile(sourceFile);
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    @Override
    protected void initLineBreakGroup() {
        try {
            lbBytesInput.setText(UserConfig.getString(baseName + "LineBreakValue", "0D 0A "));
            lbBytesInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    if (!isSettingValues) {
                        checkBytesHex();
                    }
                }
            });

            lbWidthInput.setText(UserConfig.getString(baseName + "LineBreakWidth", "30"));
            lbWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    if (!isSettingValues) {
                        checkBytesNumber();
                    }
                }
            });

            String savedLB = UserConfig.getString(baseName + "LineBreak", "Width");
            if (savedLB.equals(Line_Break.Value.toString())) {
                bytesRadio.setSelected(true);
            } else if (savedLB.equals(Line_Break.LF.toString())) {
                lbLFRadio.setSelected(true);
            } else if (savedLB.equals(Line_Break.CR.toString())) {
                lbCRRadio.setSelected(true);
            } else if (savedLB.equals(Line_Break.CRLF.toString())) {
                lbCRLFRsadio.setSelected(true);
            } else {
                lbWidthRadio.setSelected(true);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void checkLineBreakGroup() {
        try {
            if (lbWidthRadio.isSelected()) {
                lineBreak = Line_Break.Width;

            } else if (bytesRadio.isSelected()) {
                lineBreak = Line_Break.Value;

            } else if (lbLFRadio.isSelected()) {
                lineBreak = Line_Break.LF;
                lineBreakValue = "0A ";

            } else if (lbCRRadio.isSelected()) {
                lineBreak = Line_Break.CR;
                lineBreakValue = "0D ";

            } else if (lbCRLFRsadio.isSelected()) {
                lineBreak = Line_Break.CRLF;
                lineBreakValue = "0D 0A ";
            }
            UserConfig.setString(baseName + "LineBreak", lineBreak.toString());
            checkBytesHex();
            checkBytesNumber();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void checkBytesHex() {
        try {
            if (lineBreak != Line_Break.Value) {
                lbBytesInput.setStyle(null);
                return;
            }
            final String v = ByteTools.formatTextHex(lbBytesInput.getText());
            if (v == null || v.isEmpty()) {
                lbBytesInput.setStyle(UserConfig.badStyle());
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
                UserConfig.setString(baseName + "LineBreakValue", lineBreakValue);
            }

        } catch (Exception e) {
            lbBytesInput.setStyle(UserConfig.badStyle());
        }

    }

    private void checkBytesNumber() {
        try {
            if (lineBreak != Line_Break.Width) {
                lbWidthInput.setStyle(null);
                return;
            }
            int v = Integer.parseInt(lbWidthInput.getText());
            if (v > 0) {
                lineBreakWidth = v;
                lbWidthInput.setStyle(null);
                UserConfig.setInt(baseName + "LineBreakWidth", v);
            } else {
                lbWidthInput.setStyle(UserConfig.badStyle());
            }

        } catch (Exception e) {
            lbWidthInput.setStyle(UserConfig.badStyle());
        }
    }

    @Override
    protected boolean validateMainArea() {
        return ByteTools.isBytesHex(mainArea.getText());
    }

    @FXML
    @Override
    protected boolean formatMainArea() {
        String text = mainArea.getText();
        text = ByteTools.formatTextHex(text);
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

    @Override
    public void makeEditContextMenu(Node node) {
        try {
            if (node == mainArea) {
                node.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                    @Override
                    public void handle(ContextMenuEvent event) {
                        MenuBytesEditController.openBytes(myController, node, event);
                    }
                });
            } else {
                super.makeEditContextMenu(node);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void refreshPairAction() {
        if (pairArea.isDisable() || !splitPane.getItems().contains(rightPane)) {
            return;
        }
        String c = charsetSelector.getSelectionModel().getSelectedItem();
        if (c == null) {
            return;
        }
        sourceInformation.setCharset(Charset.forName(c));
        FxTask pairTask = new FxTask<Void>(this) {

            private String pairText;

            @Override
            protected boolean handle() {
                try {
                    String text = mainArea.getText();
                    if (!text.isEmpty()) {
                        String[] lines = text.split("\n");
                        StringBuilder bytes = new StringBuilder();
                        String lineText;
                        for (String line
                                : lines) {
                            byte[] hex = ByteTools.hexFormatToBytes(line);
                            if (hex == null) {
                                error = message("InvalidData");
                                return false;
                            }
                            lineText = new String(hex, sourceInformation.getCharset());
                            lineText = StringTools.replaceLineBreak(lineText) + "\n";
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
                updateNumbers(fileChanged.get());
            }

        };
        start(pairTask, pairArea);
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
    public boolean menuAction() {
        Point2D localToScreen = mainArea.localToScreen(mainArea.getWidth() - 80, 80);
        MenuBytesEditController.openBytes(myController, mainArea, localToScreen.getX(), localToScreen.getY());
        return true;
    }

    @FXML
    @Override
    public boolean popAction() {
        BytesPopController.open(this, mainArea);
        return true;
    }

    @FXML
    public void popTextAction() {
        TextPopController.openInput(this, pairArea);
    }

    /*
        static
     */
    public static BytesEditorController open() {
        try {
            BytesEditorController controller = (BytesEditorController) WindowTools.openStage(Fxmls.BytesEditorFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static BytesEditorController open(File file) {
        BytesEditorController controller = open();
        if (controller != null) {
            controller.sourceFileChanged(file);
        }
        return controller;
    }

    public static BytesEditorController edit(String texts) {
        BytesEditorController controller = open();
        if (controller != null) {
            controller.loadContents(texts);
        }
        return controller;
    }

}
