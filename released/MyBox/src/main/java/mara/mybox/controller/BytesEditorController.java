package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.Popup;
import mara.mybox.data.FileEditInformation.Line_Break;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextTools;
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
    protected ComboBox<String> charsetSelector;

    public BytesEditorController() {
        baseTitle = message("BytesEditer");
        TipsLabelKey = "BytesEditerTips";
    }

    @Override
    public void setFileType() {
        setBytesType();
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            lineBreak = Line_Break.Width;
            lineBreakValue = UserConfig.getString(baseName + "LineBreakValue", "0A");
            lineBreakWidth = UserConfig.getInt(baseName + "LineBreakWidth", 30);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            charsetSelector.getItems().addAll(TextTools.getCharsetNames());
            charsetSelector.setValue(UserConfig.getString(baseName + "PairCharset", "utf-8"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
        try {
            String c = charsetSelector.getValue();
            if (c == null) {
                popError(message("SelectToHandle"));
                return;
            }
            sourceInformation.setCharset(Charset.forName(c));
            UserConfig.setString(baseName + "PairCharset", c);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return;
        }
        if (pairTask != null) {
            pairTask.cancel();
        }
        pairTask = new FxTask<Void>(this) {

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
    public void saveAsAction() {
        try {
            targetFile = saveAsFile();
            if (targetFile == null) {
                return;
            }
            saveAs(targetFile);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public boolean menuAction(Event event) {
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

    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        if (sourceFile != null) {
            menu = new MenuItem(message("Format"), StyleTools.getIconImageView("iconFormat.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                BytesEditorFormatController.open(this);
            });
            items.add(menu);
        }

        items.addAll(super.fileMenuItems(fevent));
        return items;
    }

    @Override
    public List<MenuItem> operationsMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (sourceFile != null) {
                menu = new MenuItem(message("FormattedHexadecimal"), StyleTools.getIconImageView("iconHex.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    formatMainArea();
                });
                items.add(menu);
            }

            items.addAll(super.operationsMenuItems(fevent));

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
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
