package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.data.ShortCut;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public class ShortcutsController extends BaseTablePagesController<ShortCut> {

    @FXML
    protected TableColumn<ShortCut, String> keyColumn, actionColumn, altColumn;
    @FXML
    protected TableColumn<ShortCut, ImageView> iconColumn;
    @FXML
    protected CheckBox omitCheck;

    public ShortcutsController() {
        baseTitle = message("Shortcuts");
        TipsLabelKey = "ShortcutsTips";
    }

    @Override
    protected void initColumns() {
        try {
            super.initColumns();

            keyColumn.setCellValueFactory(new PropertyValueFactory<>("functionKey"));
            actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
            altColumn.setCellValueFactory(new PropertyValueFactory<>("possibleAlternative"));
            iconColumn.setCellValueFactory(new PropertyValueFactory<>("icon"));
            iconColumn.setCellFactory(new Callback<TableColumn<ShortCut, ImageView>, TableCell<ShortCut, ImageView>>() {
                @Override
                public TableCell<ShortCut, ImageView> call(TableColumn<ShortCut, ImageView> param) {

                    TableCell<ShortCut, ImageView> cell = new TableCell<ShortCut, ImageView>() {

                        @Override
                        public void updateItem(ImageView item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(null);
                            if (empty || item == null) {
                                setGraphic(null);
                                return;
                            }
                            setGraphic(item);
                        }
                    };
                    return cell;
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            omitCheck.setSelected(AppVariables.ShortcutsCanNotOmitCtrlAlt);

            makeList(Languages.getLangName());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeList(String lang) {
        try {
            tableData.clear();

            actionColumn.setText(message(lang, "Action"));
            iconColumn.setText(message(lang, "Icon"));
            keyColumn.setText(message(lang, "FunctionKey"));
            altColumn.setText(message(lang, "PossibleAlternative"));

            tableData.add(new ShortCut("F1", "", message(lang, "Start") + " / " + message(lang, "OK"), "CTRL+E / ALT+E", "iconStart.png"));
            tableData.add(new ShortCut("F2", "", message(lang, "Go"), "CTRL+G / ALT+G", "iconGo.png"));
            tableData.add(new ShortCut("F3", "", message(lang, "Preview"), "CTRL+U / ALT+U", "iconPreview.png"));
            tableData.add(new ShortCut("F4", "", message(lang, "Pop"), "CTRL+P / ALT+P", "iconPop.png"));
            tableData.add(new ShortCut("F5", "", message(lang, "SaveAs"), "CTRL+B / ALT+B", "iconSaveAs.png"));
            tableData.add(new ShortCut("F6", "", message(lang, "ContextMenu"), "", "iconMenu.png"));
            tableData.add(new ShortCut("F7", "", message(lang, "Operations"), "", "iconOperation.png"));
            tableData.add(new ShortCut("F8", "", message(lang, "MainPage"), "", "iconMyBox.png"));
            tableData.add(new ShortCut("F9", "", message(lang, "Tips"), "", "iconTips.png"));
            tableData.add(new ShortCut("F10", "", message(lang, "Synchronize"), "", "iconSynchronize.png"));
            tableData.add(new ShortCut("F11", "", message(lang, "ControlLeftPane"), "", "iconDoubleLeft.png"));
            tableData.add(new ShortCut("F12", "", message(lang, "ControlRightPane"), "", "iconDoubleRight.png"));
            tableData.add(new ShortCut("DELETE", "", message(lang, "Delete"), "CTRL+D / ALT+D", "iconDelete.png"));
            tableData.add(new ShortCut("PAGE_UP", "", message(lang, "Previous"), "ALT+PAGE_UP", "iconPrevious.png"));
            tableData.add(new ShortCut("PAGE_DOWN", "", message(lang, "Next"), "ALT+PAGE_DOWN", "iconNext.png"));
            tableData.add(new ShortCut("HOME", "", message(lang, "First"), "ALT+HOME", "iconFirst.png"));
            tableData.add(new ShortCut("END", "", message(lang, "Last"), "ALT+END", "iconLast.png"));
            tableData.add(new ShortCut("ESCAPE", "", message(lang, "Cancel") + " / " + message(lang, "ClosePopup"), "", "iconCancel.png"));

            tableData.add(new ShortCut("CTRL", "E", message(lang, "Start") + " /" + message(lang, "OK"), "F1 / ALT+E", "iconOK.png"));
            tableData.add(new ShortCut("CTRL", "C", message(lang, "Copy"), "ALT+C", "iconCopy.png"));
            tableData.add(new ShortCut("CTRL", "V", message(lang, "Paste"), "ALT+V", "iconPaste.png"));
            tableData.add(new ShortCut("CTRL", "Z", message(lang, "Undo"), "ALT+Z", "iconUndo.png"));
            tableData.add(new ShortCut("CTRL", "Y", message(lang, "Redo"), "ALT+Y", "iconRedo.png"));
            tableData.add(new ShortCut("CTRL", "D", message(lang, "Delete"), "DELETE / ALT+D", "iconDelete.png"));
            tableData.add(new ShortCut("CTRL", "X", message(lang, "Crop"), "ALT+X", "iconCrop.png"));
            tableData.add(new ShortCut("CTRL", "S", message(lang, "Save"), "ALT+S", "iconSave.png"));
            tableData.add(new ShortCut("CTRL", "B", message(lang, "SaveAs"), "F5 / ALT+B", "iconSaveAs.png"));
            tableData.add(new ShortCut("CTRL", "F", message(lang, "Find"), "ALT+F", "iconFind.png"));
            tableData.add(new ShortCut("CTRL", "H", message(lang, "Replace"), "ALT+H", "iconReplace.png"));
            tableData.add(new ShortCut("CTRL", "H", message(lang, "Histories"), "ALT+H", "iconHistory.png"));
            tableData.add(new ShortCut("CTRL", "R", message(lang, "Recover"), "ALT+R", "iconRecover.png"));
            tableData.add(new ShortCut("CTRL", "G", message(lang, "Go"), "F2 / ALT+G", "iconGo.png"));
            tableData.add(new ShortCut("CTRL", "N", message(lang, "Create"), "ALT+N", "iconAdd.png"));
            tableData.add(new ShortCut("CTRL", "A", message(lang, "SelectAll"), "ALT+A", "iconSelectAll.png"));
            tableData.add(new ShortCut("CTRL", "O", message(lang, "SelectNone"), "ALT+O", "iconSelectNone.png"));
            tableData.add(new ShortCut("CTRL", "U", message(lang, "Preview"), "F3 / ALT+U", "iconPreview.png"));
            tableData.add(new ShortCut("CTRL", "L", message(lang, "Clear"), "ALT+L", "iconClear.png"));
            tableData.add(new ShortCut("CTRL", "W", message(lang, "WithdrawLastItem"), "ALT+W", "iconWithdraw.png"));
            tableData.add(new ShortCut("CTRL", "P", message(lang, "Pop"), "F4 / ALT+P", "iconPop.png"));
            tableData.add(new ShortCut("CTRL", "Q", message(lang, "Query"), "ALT+Q", "iconQuery.png"));
            tableData.add(new ShortCut("CTRL", "K", message(lang, "PickColor"), "ALT+K", "iconPickColor.png"));
            tableData.add(new ShortCut("CTRL", "M", message(lang, "MyBoxClipboard"), "ALT+M", "iconClipboard.png"));
            tableData.add(new ShortCut("CTRL", "J", message(lang, "SystemClipboard"), "ALT+J", "iconSystemClipboard.png"));
            tableData.add(new ShortCut("CTRL", "1", message(lang, "OriginalSize"), "ALT+1", "iconOriginalSize.png"));
            tableData.add(new ShortCut("CTRL", "2", message(lang, "PaneSize"), "ALT+2", "iconPaneSize.png"));
            tableData.add(new ShortCut("CTRL", "3", message(lang, "ZoomIn"), "ALT+3", "iconZoomIn.png"));
            tableData.add(new ShortCut("CTRL", "4", message(lang, "ZoomOut"), "ALT+4", "iconZoomOut.png"));
            tableData.add(new ShortCut("CTRL", "9", message(lang, "SnapshotWindow"), "ALT+9", "iconSnapshot.png"));
            tableData.add(new ShortCut("CTRL", "0", message(lang, "AlwayOnTop"), "ALT+0", "iconTop.png"));
            tableData.add(new ShortCut("CTRL", "-", message(lang, "DecreaseFontSize"), "", "iconMinus.png"));
            tableData.add(new ShortCut("CTRL", "=", message(lang, "IncreaseFontSize"), "", "iconPlus.png"));

            tableData.add(new ShortCut("ALT", "PAGE_UP", message(lang, "Previous"), "PAGE_UP", "iconPrevious.png"));
            tableData.add(new ShortCut("ALT", "PAGE_DOWN", message(lang, "Next"), "PAGE_DOWN", "iconNext.png"));
            tableData.add(new ShortCut("ALT", "HOME", message(lang, "First"), "HOME", "iconFirst.png"));
            tableData.add(new ShortCut("ALT", "END", message(lang, "Last"), "END", "iconLast.png"));

            tableData.add(new ShortCut("S", "", message(lang, "Play") + " / " + message(lang, "Pause"), "", "iconPlay.png"));
            tableData.add(new ShortCut("Q", "", message(lang, "Stop"), "", "iconStop.png"));
            tableData.add(new ShortCut("M", "", message(lang, "Mute") + " / " + message(lang, "Sound"), "", "iconMute.png"));
            tableData.add(new ShortCut("F", "", message(lang, "FullScreen"), "", "iconExpand.png"));

            tableData.add(new ShortCut("", "", message(lang, "Tips"), "", "iconTips.png"));
            tableData.add(new ShortCut("", "", message(lang, "Function"), "", "iconFunction.png"));
            tableData.add(new ShortCut("", "", message(lang, "Options"), "", "iconOptions.png"));
            tableData.add(new ShortCut("", "", message(lang, "Manage"), "", "iconManage.png"));
            tableData.add(new ShortCut("", "", message(lang, "Data"), "", "iconData.png"));
            tableData.add(new ShortCut("", "", message(lang, "Edit"), "", "iconEdit.png"));
            tableData.add(new ShortCut("", "", message(lang, "View"), "", "iconView.png"));
            tableData.add(new ShortCut("", "", message(lang, "Export"), "", "iconExport.png"));
            tableData.add(new ShortCut("", "", message(lang, "Import"), "", "iconImport.png"));
            tableData.add(new ShortCut("", "", message(lang, "Examples"), "", "iconExamples.png"));
            tableData.add(new ShortCut("", "", message(lang, "Demo"), "", "iconDemo.png"));
            tableData.add(new ShortCut("", "", message(lang, "Random"), "", "iconRandom.png"));
            tableData.add(new ShortCut("", "", message(lang, "Default"), "", "iconDefault.png"));
            tableData.add(new ShortCut("", "", message(lang, "Information"), "", "iconInfo.png"));
            tableData.add(new ShortCut("", "", message(lang, "MetaData"), "", "iconMeta.png"));
            tableData.add(new ShortCut("", "", message(lang, "File"), "", "iconFile.png"));
            tableData.add(new ShortCut("", "", message(lang, "Frames"), "", "iconFrame.png"));
            tableData.add(new ShortCut("", "", message(lang, "SelectFile"), "", "iconSelectFile.png"));
            tableData.add(new ShortCut("", "", message(lang, "SelectPath"), "", "iconSelectPath.png"));
            tableData.add(new ShortCut("", "", message(lang, "OpenDirectory"), "", "iconOpenPath.png"));
            tableData.add(new ShortCut("", "", message(lang, "Insert"), "", "iconInsert.png"));
            tableData.add(new ShortCut("", "", message(lang, "InsertFiles"), "", "iconInsertFile.png"));
            tableData.add(new ShortCut("", "", message(lang, "InsertDirectory"), "", "iconInsertPath.png"));
            tableData.add(new ShortCut("", "", message(lang, "FileBackups"), "", "iconBackup.png"));
            tableData.add(new ShortCut("", "", message(lang, "Pagination"), "", "iconPages.png"));
            tableData.add(new ShortCut("", "", message(lang, "Anchor"), "", "iconAnchor.png"));
            tableData.add(new ShortCut("", "", message(lang, "Calculator"), "", "iconCalculator.png"));
            tableData.add(new ShortCut("", "", message(lang, "Delimiter"), "", "iconDelimiter.png"));
            tableData.add(new ShortCut("", "", message(lang, "LoadedSize"), "", "iconLoadSize.png"));
            tableData.add(new ShortCut("", "", message(lang, "Location"), "", "iconLocation.png"));
            tableData.add(new ShortCut("", "", message(lang, "Matrix"), "", "iconMatrix.png"));
            tableData.add(new ShortCut("", "", message(lang, "Panes"), "", "iconPanes.png"));
            tableData.add(new ShortCut("", "", message(lang, "More"), "", "iconMore.png"));
            tableData.add(new ShortCut("", "", message(lang, "Analyse"), "", "iconAnalyse.png"));
            tableData.add(new ShortCut("", "", message(lang, "Move"), "", "iconMove.png"));
            tableData.add(new ShortCut("", "", message(lang, "Add"), "", "iconNewItem.png"));
            tableData.add(new ShortCut("", "", message(lang, "Pause"), "", "iconPause.png"));
            tableData.add(new ShortCut("", "", message(lang, "Permissions"), "", "iconPermission.png"));
            tableData.add(new ShortCut("", "", message(lang, "Query"), "", "iconQuery.png"));
            tableData.add(new ShortCut("", "", message(lang, "Repeat"), "", "iconRepeat.png"));
            tableData.add(new ShortCut("", "", message(lang, "Sample"), "", "iconSample.png"));
            tableData.add(new ShortCut("", "", message(lang, "Shear"), "", "iconShear.png"));
            tableData.add(new ShortCut("", "", message(lang, "SnapshotWindow"), "", "iconSnapshot.png"));
            tableData.add(new ShortCut("", "", message(lang, "Sort"), "", "iconSort.png"));
            tableData.add(new ShortCut("", "", message(lang, "Statistic"), "", "iconStatistic.png"));
            tableData.add(new ShortCut("", "", message(lang, "Style"), "", "iconStyle.png"));
            tableData.add(new ShortCut("", "", message(lang, "SVG"), "", "iconSVG.png"));
            tableData.add(new ShortCut("", "", message(lang, "Tag"), "", "iconTag.png"));
            tableData.add(new ShortCut("", "", message(lang, "Input"), "", "iconInput.png"));
            tableData.add(new ShortCut("", "", message(lang, "Validate"), "", "iconVerify.png"));
            tableData.add(new ShortCut("", "", message(lang, "Transparent"), "", "iconOpacity.png"));
            tableData.add(new ShortCut("", "", message(lang, "Format"), "", "iconFormat.png"));
            tableData.add(new ShortCut("", "", message(lang, "Wrap"), "", "iconWrap.png"));
            tableData.add(new ShortCut("", "", message(lang, "Rulers"), "", "iconXRuler.png"));
            tableData.add(new ShortCut("", "", message(lang, "Yes"), "", "iconYes.png"));
            tableData.add(new ShortCut("", "", message(lang, "CopyToSystemClipboard"), "", "iconCopySystem.png"));
            tableData.add(new ShortCut("", "", message(lang, "LoadContentInSystemClipboard"), "", "iconImageSystem.png"));
            tableData.add(new ShortCut("", "", message(lang, "PasteContentInSystemClipboard"), "", "iconPasteSystem.png"));
            tableData.add(new ShortCut("", "", message(lang, "SystemMethod"), "", "iconSystemOpen.png"));
            tableData.add(new ShortCut("", "", message(lang, "CustomizeColors"), "", "iconColorWheel.png"));
            tableData.add(new ShortCut("", "", message(lang, "ColorPalette"), "", "iconColor.png"));
            tableData.add(new ShortCut("", "", message(lang, "Help"), "", "iconClaw.png"));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void setOmit() {
        AppVariables.ShortcutsCanNotOmitCtrlAlt = omitCheck.isSelected();
        UserConfig.setBoolean("ShortcutsCanNotOmitCtrlAlt", AppVariables.ShortcutsCanNotOmitCtrlAlt);
    }

    @FXML
    @Override
    public void snapAction() {
        ImageEditorController.openImage(NodeTools.snap(tableView));
    }

    public void makeDocuments(MyBoxDocumentsController maker, File path) {
        makeDocuments(maker, path, "zh");
        makeDocuments(maker, path, "en");
        close();
    }

    public void makeDocuments(MyBoxDocumentsController maker, File path, String lang) {
        try {
            makeList(lang);
            baseTitle = message(lang, "Shortcuts");
            StringTable table = makeStringTable(null);
            String html = HtmlWriteTools.html(baseTitle, HtmlStyles.DefaultStyle, table.body());
            File file = new File(path, "mybox_shortcuts_" + lang + ".html");
            file = TextFileTools.writeFile(file, html);
            maker.showLogs(file.getAbsolutePath());

        } catch (Exception e) {
            error = e.toString();
        }
    }

    /*
        static
     */
    public static void documents(MyBoxDocumentsController maker, File path) {
        try {
            ShortcutsController controller = (ShortcutsController) WindowTools.openStage(Fxmls.ShortcutsFxml);
            controller.makeDocuments(maker, path);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
