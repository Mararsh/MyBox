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
import mara.mybox.tools.FileTmpTools;
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

            tableData.add(new ShortCut("F1", "",
                    message("Start") + " / " + message("OK") + " / " + message("Synchronize") + " / " + message("Set") + " / " + message("Query"),
                    "CTRL+e / ALT+e, CTRL+q / ALT+q", "iconStart.png"));
            tableData.add(new ShortCut("F2", "", message("Save"), "CTRL+s / ALT+s", "iconSave.png"));
            tableData.add(new ShortCut("F3", "", message("Recover"), "CTRL+r / ALT+r", "iconRecover.png"));
            tableData.add(new ShortCut("F4", "", message("ControlLeftPane"), "", "iconDoubleLeft.png"));
            tableData.add(new ShortCut("F5", "", message("ControlRightPane"), "", "iconDoubleRight.png"));
            tableData.add(new ShortCut("F6", "", message("ClosePopup"), "", "iconCancel.png"));
            tableData.add(new ShortCut("F7", "", message("CloseStage"), "", "iconClose.png"));
            tableData.add(new ShortCut("F8", "", message("RefreshStage"), "", "iconRefresh.png"));
            tableData.add(new ShortCut("F9", "", message("Go"), "CTRL+g / ALT+g", "iconGo.png"));
            tableData.add(new ShortCut("F10", "", message("Synchronize"), "", "iconSynchronize.png"));
            tableData.add(new ShortCut("F11", "", message("SaveAs"), "CTRL+b / ALT+b", "iconSaveAs.png"));
            tableData.add(new ShortCut("F12", "", message("Menu"), "", "iconMenu.png"));
            tableData.add(new ShortCut("DELETE", "", message("Delete"), "CTRL+d / ALT+d", "iconDelete.png"));
            tableData.add(new ShortCut("PAGE_UP", "", message("Previous"), "ALT+PAGE_UP", "iconPrevious.png"));
            tableData.add(new ShortCut("PAGE_DOWN", "", message("Next"), "ALT+PAGE_DOWN", "iconNext.png"));
            tableData.add(new ShortCut("HOME", "", message("First"), "ALT+HOME", "iconFirst.png"));
            tableData.add(new ShortCut("END", "", message("Last"), "ALT+END", "iconLast.png"));
            tableData.add(new ShortCut("ESCAPE", "", message("Cancel") + " / " + message("Withdraw"), "CTRL+w / ALT+w", "iconCancel.png"));

            tableData.add(new ShortCut("CTRL", "e",
                    message("Start") + " /" + message("OK") + " / " + message("Set") + " / " + message("Export"),
                    "F1 / ALT+e", "iconOK.png"));
            tableData.add(new ShortCut("CTRL", "c", message("Copy"), "ALT+c", "iconCopy.png"));
            tableData.add(new ShortCut("CTRL", "v", message("Paste"), "ALT+v", "iconPaste.png"));
            tableData.add(new ShortCut("CTRL", "z", message("Undo"), "ALT+z", "iconUndo.png"));
            tableData.add(new ShortCut("CTRL", "y", message("Redo"), "ALT+y", "iconRedo.png"));
            tableData.add(new ShortCut("CTRL", "d", message("Delete"), "DELETE / ALT+d", "iconDelete.png"));
            tableData.add(new ShortCut("CTRL", "x", message("Crop"), "ALT+x", "iconCrop.png"));
            tableData.add(new ShortCut("CTRL", "s", message("Save"), "F2 / ALT+s", "iconSave.png"));
            tableData.add(new ShortCut("CTRL", "b", message("SaveAs"), "F11 / ALT+b", "iconSaveAs.png"));
            tableData.add(new ShortCut("CTRL", "f", message("Find"), "ALT+f", "iconFind.png"));
            tableData.add(new ShortCut("CTRL", "h", message("Replace") + " / " + message("CopyHtml"), "ALT+h", "iconReplace.png"));
            tableData.add(new ShortCut("CTRL", "r", message("Recover"), "F3 / ALT+r", "iconRecover.png"));
            tableData.add(new ShortCut("CTRL", "g", message("Go"), "F9 / ALT+g", "iconGo.png"));
            tableData.add(new ShortCut("CTRL", "n", message("Create"), "", "iconAdd.png"));
            tableData.add(new ShortCut("CTRL", "a", message("SelectAll"), "ALT+a", "iconSelectAll.png"));
            tableData.add(new ShortCut("CTRL", "o", message("SelectNone"), "ALT+o", "iconSelectNone.png"));
            tableData.add(new ShortCut("CTRL", "u", message("Select"), "ALT+u", "iconSelect.png"));
            tableData.add(new ShortCut("CTRL", "l", message("Clear"), "ALT+l(lowercase of 'L')", "iconClear.png"));
            tableData.add(new ShortCut("CTRL", "w", message("Cancel") + " / " + message("Withdraw") + " / " + message("ReplaceAll"), "ESCAPE", "iconCancel.png"));
            tableData.add(new ShortCut("CTRL", "p", message("Pop"), "ALT+p", "iconPop.png"));
            tableData.add(new ShortCut("CTRL", "q", message("Query"), "ALT+q", "iconQuery.png"));
            tableData.add(new ShortCut("CTRL", "k", message("PickColor"), "ALT+k", "iconPickColor.png"));
            tableData.add(new ShortCut("CTRL", "t", message("SelectArea") + " / " + message("CopyText"), "ALT+t", "iconTarget.png"));
            tableData.add(new ShortCut("CTRL", "m", message("MyBoxClipboard"), "ALT+m", "iconClipboard.png"));
            tableData.add(new ShortCut("CTRL", "j", message("SystemClipboard"), "ALT+j", "iconSystemClipboard.png"));
            tableData.add(new ShortCut("CTRL", "1", message("OriginalSize") + " / " + message("Previous"), "ALT+1", "iconOriginalSize.png"));
            tableData.add(new ShortCut("CTRL", "2", message("PaneSize") + " / " + message("Next"), "ALT+2", "iconPaneSize.png"));
            tableData.add(new ShortCut("CTRL", "3", message("ZoomIn"), "ALT+3", "iconZoomIn.png"));
            tableData.add(new ShortCut("CTRL", "4", message("ZoomOut"), "ALT+4", "iconZoomOut.png"));
            tableData.add(new ShortCut("CTRL", "-", message("DecreaseFontSize"), "", "iconMinus.png"));
            tableData.add(new ShortCut("CTRL", "=", message("IncreaseFontSize"), "", "iconPlus.png"));

            tableData.add(new ShortCut("ALT", "1", message("Set") + " / " + message("Previous"), "CTRL+1", "iconEqual.png"));
            tableData.add(new ShortCut("ALT", "2", message("Increase") + " / " + message("Next"), "CTRL+2", "iconPlus.png"));
            tableData.add(new ShortCut("ALT", "3", message("Decrease"), "CTRL+5", "iconMinus.png"));
            tableData.add(new ShortCut("ALT", "4", message("Filter"), "CTRL+5", "iconFilter.png"));
            tableData.add(new ShortCut("ALT", "5", message("Invert"), "CTRL+5", "iconInvert.png"));
            tableData.add(new ShortCut("ALT", "PAGE_UP", message("Previous"), "PAGE_UP", "iconPrevious.png"));
            tableData.add(new ShortCut("ALT", "PAGE_DOWN", message("Next"), "PAGE_DOWN", "iconNext.png"));
            tableData.add(new ShortCut("ALT", "HOME", message("First"), "HOME", "iconFirst.png"));
            tableData.add(new ShortCut("ALT", "END", message("Last"), "END", "iconLast.png"));

            tableData.add(new ShortCut("s / S", "", message("Play") + " / " + message("Pause"), "", "iconPlay.png"));
            tableData.add(new ShortCut("q / Q", "", message("Stop"), "", "iconStop.png"));
            tableData.add(new ShortCut("m / M", "", message("Mute") + " / " + message("Sound"), "", "iconMute.png"));
            tableData.add(new ShortCut("f / F", "", message("FullScreen"), "", "iconExpand.png"));

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
        ImageViewerController.openImage(NodeTools.snap(tableView));
    }

    public void makeHelp() {
        StringTable table = makeStringTable();
        if (table == null) {
            return;
        }
        String html = HtmlWriteTools.html(message("Shortcuts"), HtmlStyles.DefaultStyle, table.body());
        File file = new File(FileTmpTools.generatePath("html")
                + "/mybox_shortcuts_" + Languages.getLangName() + ".html");
        file = TextFileTools.writeFile(file, html);
        openHtml(file);
        browse(file.getParentFile());
        close();
    }

    /*
        static
     */
    public static ShortcutsController html() {
        try {
            ShortcutsController controller = (ShortcutsController) WindowTools.openStage(Fxmls.ShortcutsFxml);
            if (controller != null) {
                controller.makeHelp();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
