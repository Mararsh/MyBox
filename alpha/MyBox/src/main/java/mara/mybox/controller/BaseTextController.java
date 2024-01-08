package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-12-09
 * @License Apache License Version 2.0
 *
 * BaseTextController < BaseTextController_Left < BaseTextController_Actions <
 * BaseTextController_File < BaseTextController_Main < BaseTextController_Pair <
 * BaseTextController_Base
 */
public abstract class BaseTextController extends BaseTextController_Left {

    public BaseTextController() {
        baseTitle = message("FileEditer");
    }

    public BaseTextController(Edit_Type editType) {
        baseTitle = message("FileEditer");
        if (null != editType) {
            switch (editType) {
                case Text:
                    setTextType();
                    break;
                case Markdown:
                    setMarkdownType();
                    break;
                case Bytes:
                    setBytesType();
                    break;
                default:
                    break;
            }
        }
    }

    public final void setTextType() {
        editType = Edit_Type.Text;
        defaultPageSize = 200;

        setFileType(VisitHistory.FileType.Text);
    }

    public final void setBytesType() {
        editType = Edit_Type.Bytes;
        defaultPageSize = 50000;

        setFileType(VisitHistory.FileType.All);
    }

    public final void setMarkdownType() {
        editType = Edit_Type.Markdown;
        defaultPageSize = 200;

        setFileType(VisitHistory.FileType.Markdown);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            if (findReplaceController != null) {
                findReplaceController.setEditor(this);
            }
            initPage(null);

            initSaveTab();
            initLocateTab();
            initFilterTab();
            initFindTab();
            initMainBox();
            initPairBox();
            initPageBar();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            if (findReplaceController != null) {
                if (sourceInformation != null && sourceInformation.getEditType() == Edit_Type.Bytes) {
                    NodeStyleTools.setTooltip(findReplaceController.tipsView, new Tooltip(message("FindReplaceBytesTips")));
                } else {
                    NodeStyleTools.setTooltip(findReplaceController.tipsView, new Tooltip(message("FindReplaceTextsTips")));
                }
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @FXML
    @Override
    public boolean infoAction() {
        String info = fileInfo();
        if (info != null && !info.isBlank()) {
            TextPopController.loadText(info);
            return true;
        }
        return false;
    }

    public String fileInfo() {
        try {
            if (!sourceInformation.isTotalNumberRead()) {
                return message("CountingTotalNumber");
            }
            String pageText = mainArea.getText();
            if (pageText == null) {
                pageText = "";
            }
            int pageLinesNumber = pageLinesNumber(pageText);
            int pageObjectsNumber = pageObjectsNumber(pageText);
            long pageObjectStart = 0, pageObjectEnd = pageObjectsNumber;
            long pageLineStart = 0, pageLineEnd = pageLinesNumber, pagesNumber = 1;
            int pageSize = sourceInformation.getPageSize();
            long currentPage = sourceInformation.getCurrentPage();
            StringBuilder s = new StringBuilder();
            if (sourceFile != null) {
                pageLineStart = sourceInformation.getCurrentPageLineStart();
                pageLineEnd = pageLineStart + pageLinesNumber;

                pageObjectStart = sourceInformation.getCurrentPageObjectStart();
                pageObjectEnd = pageObjectStart + pageObjectsNumber;

                pagesNumber = sourceInformation.getPagesNumber();
                s.append(message("File"))
                        .append(": ").append(sourceFile).append("\n");
                s.append(message("FileSize"))
                        .append(": ").append(FileTools.showFileSize(sourceFile.length())).append("\n");
                s.append(message("FileModifyTime"))
                        .append(": ").append(DateTools.datetimeToString(sourceFile.lastModified())).append("\n");
                s.append(editType == Edit_Type.Bytes ? message("BytesNumberInFile") : message("CharactersNumberInFile"))
                        .append(": ").append(StringTools.format(sourceInformation.getObjectsNumber())).append("\n");
                s.append(message("RowsNumberInFile"))
                        .append(": ").append(StringTools.format(sourceInformation.getLinesNumber())).append("\n");
                s.append(editType == Edit_Type.Bytes ? message("BytesPerPage") : message("RowsPerPage"))
                        .append(": ").append(StringTools.format(pageSize)).append("\n");
                s.append(message("CurrentPage"))
                        .append(": ").append(StringTools.format(currentPage + 1)).append(" / ")
                        .append(StringTools.format(pagesNumber)).append("\n");
                if (editType != Edit_Type.Bytes) {
                    s.append(message("WithBom"))
                            .append(": ").append(sourceInformation.isWithBom() ? message("Yes") : message("No"))
                            .append("\n");
                }
            }
            s.append(message("LineBreak"))
                    .append(": ").append(sourceInformation.lineBreakName()).append("\n");
            s.append(message("Charset"))
                    .append(": ").append(sourceInformation.getCharset().name()).append("\n");
            if (pagesNumber > 1) {
                s.append(editType == Edit_Type.Bytes ? message("BytesRangeInPage") : message("CharactersRangeInPage"))
                        .append(": [").append(StringTools.format(pageObjectStart + 1))
                        .append(" - ").append(StringTools.format(pageObjectEnd))
                        .append("] ").append(StringTools.format(pageObjectsNumber)).append("\n");
                s.append(message("RowsRangeInPage"))
                        .append(": [").append(StringTools.format(pageLineStart + 1))
                        .append(" - ").append(StringTools.format(pageLineEnd))
                        .append("] ").append(StringTools.format(pageLinesNumber)).append("\n");
            } else {
                s.append(editType == Edit_Type.Bytes ? message("BytesNumberInPage") : message("CharactersNumberInPage"))
                        .append(": ").append(StringTools.format(pageObjectsNumber)).append("\n");
                s.append(message("RowsNumberInPage"))
                        .append(": ").append(StringTools.format(pageLinesNumber)).append("\n");
            }
            s.append(message("PageModifyTime"))
                    .append(": ").append(DateTools.nowString()).append("\n");
            return s.toString();
        } catch (Exception e) {
            return e.toString();
        }
    }

    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(message("Information") + "    Ctrl+I " + message("Or") + " Alt+I",
                    StyleTools.getIconImageView("iconInfo.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                infoAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Save") + "    Ctrl+S " + message("Or") + " Alt+S",
                    StyleTools.getIconImageView("iconSave.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                saveAction();
            });
            menu.setDisable(saveButton.isDisabled());
            items.add(menu);

            if (sourceFile != null) {
                menu = new MenuItem(message("Recover") + "    Ctrl+R " + message("Or") + " Alt+R",
                        StyleTools.getIconImageView("iconRecover.png"));
                menu.setOnAction((ActionEvent event) -> {
                    recoverAction();
                });
                menu.setDisable(recoverButton.isDisabled());
                items.add(menu);

                CheckMenuItem backItem = new CheckMenuItem(message("BackupWhenSave"));
                backItem.setSelected(UserConfig.getBoolean(baseName + "BackupWhenSave", true));
                backItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        UserConfig.setBoolean(baseName + "BackupWhenSave", backItem.isSelected());
                    }
                });
                items.add(backItem);

                menu = new MenuItem(message("FileBackups"), StyleTools.getIconImageView("iconBackup.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    openBackups();
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("Create") + "    Ctrl+N " + message("Or") + " Alt+N",
                    StyleTools.getIconImageView("iconAdd.png"));
            menu.setOnAction((ActionEvent event) -> {
                createAction();
            });
            items.add(menu);

            menu = new MenuItem(message("LoadContentInSystemClipboard"), StyleTools.getIconImageView("iconImageSystem.png"));
            menu.setOnAction((ActionEvent event) -> {
                loadContentInSystemClipboard();
            });
            items.add(menu);

            menu = new MenuItem(message("SaveAs") + "    Ctrl+B " + message("Or") + " Alt+B",
                    StyleTools.getIconImageView("iconSaveAs.png"));
            menu.setOnAction((ActionEvent event) -> {
                saveAsAction();
            });
            items.add(menu);

            menu = new MenuItem(message("Texts"), StyleTools.getIconImageView("iconTxt.png"));
            menu.setOnAction((ActionEvent event) -> {
                editTexts();
            });
            items.add(menu);

            if (sourceFile == null) {
                return items;
            }
            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("OpenDirectory"), StyleTools.getIconImageView("iconOpenPath.png"));
            menu.setOnAction((ActionEvent event) -> {
                openSourcePath();
            });
            items.add(menu);

            menu = new MenuItem(message("BrowseFiles"), StyleTools.getIconImageView("iconList.png"));
            menu.setOnAction((ActionEvent event) -> {
                FileBrowseController.open(this);
            });
            items.add(menu);

            menu = new MenuItem(message("SystemMethod"), StyleTools.getIconImageView("iconSystemOpen.png"));
            menu.setOnAction((ActionEvent event) -> {
                systemMethod();
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public boolean controlAltFilter(KeyEvent event) {
        if (event.getCode() == null) {
            return false;
        }
        if (findReplaceController != null && findPane != null) {
            switch (event.getCode()) {
                case DIGIT1:
                case DIGIT2:
                case F:
                case H:
                case W:
                    if (leftPaneControl != null) {
                        showLeftPane();
                    }
                    if (findPane.isExpanded()) {
                        findReplaceController.keyEventsFilter(event);
                    } else {
                        findPane.setExpanded(true);
                        findReplaceController.findArea.requestFocus();
                    }
                    return true;
            }
        }
        return super.controlAltFilter(event);
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (!isIndependantStage()
                || fileChanged == null || !fileChanged.getValue()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("NeedSaveBeforeAction"));
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
                if (fileChanged != null) {
                    fileChanged.set(false);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean controlAltN() {
        createAction();
        return true;
    }

    @Override
    public boolean controlAltS() {
        saveAction();
        return true;
    }

    @Override
    public boolean controlAltB() {
        saveAsAction();
        return true;
    }

    @Override
    public void cleanPane() {
        try {
            if (autoSaveTimer != null) {
                autoSaveTimer.cancel();
                autoSaveTimer = null;
            }

        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
