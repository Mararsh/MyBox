package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.XmlTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-4-30
 * @License Apache License Version 2.0
 */
public class XmlEditorController extends BaseFileController {

    protected boolean domChanged, textsChanged, fileChanged;
    protected String title;
    protected final SimpleBooleanProperty loadNotify;

    @FXML
    protected Tab domTab, textsTab;
    @FXML
    protected TextArea textsArea;
    @FXML
    protected Label textsLabel;
    @FXML
    protected ControlXmlTree domController;
    @FXML
    protected ControlXmlOptions optionsController;
    @FXML
    protected ControlFileBackup backupController;
    @FXML
    protected CheckBox wrapTextsCheck, typesettingCheck;

    public XmlEditorController() {
        baseTitle = message("XmlEditor");
        TipsLabelKey = "XmlEditorTips";
        loadNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.XML);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                    tabChanged();
                }
            });

            initTextsTab();

            recoverButton.setDisable(true);

            domController.xmlEditor = this;

            backupController.setParameters(this, baseName);

            tabChanged();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        source
     */
    @Override
    public void sourceFileChanged(File file) {
        if (!checkBeforeNextAction()) {
            return;
        }
        if (file == null || !file.exists()) {
            popError(message("InvalidData"));
            return;
        }
        if (file.length() > 10 * 1024 * 1024) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("FileSize") + ": " + FileTools.showFileSize(file.length()));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonXml = new ButtonType(message("XmlEditor"));
            ButtonType buttonSystem = new ButtonType(message("SystemMethod"));
            ButtonType buttontext = new ButtonType(message("TextEditor"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
            alert.getButtonTypes().setAll(buttonXml, buttonSystem, buttontext, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || !result.isPresent() || result.get() == buttonCancel) {
                return;
            }
            if (result.get() == buttonSystem) {
                browse(file);
                return;

            } else if (result.get() == buttontext) {
                TextEditorController.open(file);
                return;

            }
        }
        super.sourceFileChanged(file);
        writePanes(TextFileTools.readTexts(file));
    }

    public boolean writePanes(String xml) {
        fileChanged = false;
        isSettingValues = true;
        loadDom(xml, false);
        loadText(xml, false);
        updateTitle();
        isSettingValues = false;
        recordFileOpened(sourceFile);
        recoverButton.setDisable(true);
        backupController.loadBackups(sourceFile);
        browseController.setCurrentFile(sourceFile);
        fileInfoLabel.setText(FileTools.fileInformation(sourceFile));
        loadNotify.set(!loadNotify.get());
        return true;
    }

    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            String blank = makeBlank();
            if (blank == null || blank.isBlank()) {
                return;
            }
            sourceFile = null;
            writePanes(blank);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String makeBlank() {
        String name = PopTools.askValue(getBaseTitle(), message("Create"), message("Root"), "data");
        if (name == null || name.isBlank()) {
            return null;
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<" + name + "></" + name + ">";
    }

    @FXML
    @Override
    public void recoverAction() {
        if (sourceFile != null && sourceFile.exists()) {
            fileChanged = false;
            sourceFileChanged(sourceFile);
        }
    }

    /*
        file
     */
    @FXML
    @Override
    public void saveAction() {
        if (task != null && !task.isQuit()) {
            return;
        }
        if (sourceFile == null) {
            targetFile = chooseSaveFile();
        } else {
            targetFile = sourceFile;
        }
        if (targetFile == null) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            private String xml;

            @Override
            protected boolean handle() {
                try {
                    xml = null;
                    Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
                    if (currentTab == domTab) {
                        xml = xmlByDom();
                    } else if (currentTab == textsTab) {
                        xml = xmlByText();
                    }
                    if (xml == null || xml.isBlank()) {
                        error = message("NoData");
                        return false;
                    }
                    String encoding = domController.doc.getXmlEncoding();
                    if (encoding == null) {
                        encoding = "utf-8";
                    }
                    File tmpFile = TextFileTools.writeFile(FileTmpTools.getTempFile(),
                            xml, Charset.forName(encoding));
                    if (tmpFile == null || !tmpFile.exists()) {
                        return false;
                    }
                    if (sourceFile != null && backupController.needBackup()) {
                        backupController.addBackup(task, sourceFile);
                    }
                    return FileTools.rename(tmpFile, targetFile);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popSaved();
                recordFileWritten(targetFile);
                fileChanged = false;
                sourceFileChanged(targetFile);
            }

        };
        start(task);
    }

    public void updateTitle() {
        if (getMyStage() == null) {
            return;
        }
        title = baseTitle;
        if (sourceFile != null) {
            title += " - " + sourceFile.getAbsolutePath();
        }
        if (fileChanged) {
            title += " *";
        }
        myStage.setTitle(title);
    }

    protected void fileChanged() {
        fileChanged = true;
        updateTitle();
        recoverButton.setDisable(sourceFile == null);
    }

    protected String current() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab == domTab) {
            return xmlByDom();
        } else if (currentTab == textsTab) {
            return xmlByText();
        }
        return null;
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (task != null && !task.isQuit()) {
            return;
        }
        File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            @Override
            protected boolean handle() {
                String xml = current();
                if (xml == null || xml.isBlank()) {
                    error = message("NoData");
                    return false;
                }
                String encoding = domController.doc.getXmlEncoding();
                if (encoding == null) {
                    encoding = "utf-8";
                }
                File tmpFile = TextFileTools.writeFile(FileTmpTools.getTempFile(),
                        xml, Charset.forName(encoding));
                if (tmpFile == null || !tmpFile.exists()) {
                    return false;
                }
                return FileTools.rename(tmpFile, file);
            }

            @Override
            protected void whenSucceeded() {
                popSaved();
                recordFileWritten(file);
                openSavedFile(file);
            }

        };
        start(task);
    }

    public void openSavedFile(File file) {
        XmlEditorController.open(file);
    }


    /*
        dom
     */
    public void loadDom(String xml, boolean updated) {
        domController.makeTree(xml);
        domChanged(updated);
    }

    public String xmlByDom() {
        return XmlTools.transform(domController.doc);
    }

    public void domChanged(boolean changed) {
        domChanged = changed;
        domTab.setText(message("Tree") + (changed ? " *" : ""));
        if (changed) {
            fileChanged();
        }
    }

    public void updateNode(TreeItem<XmlTreeNode> item) {
        domChanged(true);
    }

    public void clearDom() {
        domController.clearTree();
        domChanged(true);
    }

    @FXML
    @Override
    public void refreshAction() {
        fileChanged = false;

    }

    /*
        texts
     */
    protected void initTextsTab() {
        try {
            textsArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    textsChanged(true);
                }
            });

            wrapTextsCheck.setSelected(UserConfig.getBoolean(baseName + "WrapText", true));
            textsArea.setWrapText(wrapTextsCheck.isSelected());
            wrapTextsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WrapText", wrapTextsCheck.isSelected());
                    textsArea.setWrapText(wrapTextsCheck.isSelected());
                }
            });

            typesettingCheck.selectedProperty().bindBidirectional(optionsController.indentCheck.selectedProperty());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadText(String xml, boolean updated) {
        try {
            textsArea.setText(xml);
            textsChanged(updated);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public String xmlByText() {
        return textsArea.getText();
    }

    protected void textsChanged(boolean changed) {
        textsChanged = changed;
        textsTab.setText("XML" + (changed ? " *" : ""));
        textsLabel.setText(message("CharactersNumber") + ": " + StringTools.format(textsArea.getLength()));
        if (changed) {
            fileChanged();
        }
    }

    @FXML
    protected void clearTexts() {
        textsArea.clear();
        textsChanged(true);
    }


    /*
        buttons
     */
    @FXML
    @Override
    public boolean popAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == textsTab) {
                TextPopController.openInput(this, textsArea);
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @FXML
    @Override
    public boolean synchronizeAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();

            if (tab == domTab) {
                synchronizeDom();
                return true;

            } else if (tab == textsTab) {
                synchronizeTexts();
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    public void synchronizeDom() {
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            String xml;

            @Override
            protected boolean handle() {
                try {
                    xml = xmlByDom();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                synchronizeDomXML(xml);
            }

        };
        start(task);
    }

    public void synchronizeDomXML(String xml) {
        loadText(xml, true);
    }

    public void synchronizeTexts() {
        loadDom(xmlByText(), true);
    }

    @FXML
    @Override
    public boolean menuAction() {
        try {
            closePopup();

            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == domTab) {
                domController.popFunctionsMenu(null);
                return true;

            } else if (tab == textsTab) {
                MenuTextEditController.textMenu(this, textsArea);
                return true;

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return false;
    }

    @FXML
    @Override
    public void myBoxClipBoard() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == textsTab) {
                TextClipboardPopController.open(this, textsArea);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public void clearAction() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            if (tab == domTab) {
                clearDom();
            } else if (tab == textsTab) {
                clearTexts();

            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    protected void editTexts() {
        String xml = current();
        if (xml == null || xml.isBlank()) {
            error = message("NoData");
            return;
        }
        TextEditorController.edit(xml);
    }

    @FXML
    @Override
    public void systemMethod() {
        String xml = current();
        if (xml == null || xml.isBlank()) {
            error = message("NoData");
            return;
        }
        File tmpFile = FileTmpTools.getTempFile(".xml");
        TextFileTools.writeFile(tmpFile, xml);
        if (tmpFile != null && tmpFile.exists()) {
            browse(tmpFile);
        } else {
            popError(message("Failed"));
        }
    }

    /*
        panes
     */
    public void tabChanged() {
        try {
            TextClipboardPopController.closeAll();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (isPop || !fileChanged) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("FileChanged"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(message("Save"));
            ButtonType buttonNotSave = new ButtonType(message("NotSave"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || !result.isPresent()) {
                return false;
            }
            if (result.get() == buttonSave) {
                saveAction();
                return false;
            } else if (result.get() == buttonNotSave) {
                fileChanged = false;
                return true;
            } else {
                return false;
            }
        }
    }

    @FXML
    protected void popHelps(Event event) {
        if (UserConfig.getBoolean("XmlHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

    @FXML
    protected void showHelps(Event event) {
        popEventMenu(event, HelpTools.xmlHelps(true));
    }

    /*
        static
     */
    public static XmlEditorController load(String xml) {
        try {
            XmlEditorController controller = (XmlEditorController) WindowTools.openStage(Fxmls.XmlEditorFxml);
            controller.writePanes(xml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static XmlEditorController open(File file) {
        try {
            XmlEditorController controller = (XmlEditorController) WindowTools.openStage(Fxmls.XmlEditorFxml);
            controller.sourceFileChanged(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
