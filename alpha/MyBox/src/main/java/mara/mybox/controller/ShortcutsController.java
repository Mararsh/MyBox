package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import mara.mybox.data.ShortCut;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorDataTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fximage.PaletteTools;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
import static mara.mybox.fxml.HelpTools.imageStories;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.StyleData;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Colors.color;
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

            tableData.add(new ShortCut("F1", "", message(lang, "Start") + " / " + message(lang, "OK"), "CTRL+E / ALT+E", "iconStart.png"));
            tableData.add(new ShortCut("F2", "", message(lang, "Go"), "CTRL+G / ALT+G", "iconGo.png"));
            tableData.add(new ShortCut("F3", "", message(lang, "Preview"), "CTRL+U / ALT+U", "iconPreview.png"));
            tableData.add(new ShortCut("F4", "", message(lang, "Pop"), "CTRL+P / ALT+P", "iconPop.png"));
            tableData.add(new ShortCut("F5", "", message(lang, "SaveAs"), "CTRL+B / ALT+B", "iconSaveAs.png"));
            tableData.add(new ShortCut("F6", "", message(lang, "ContextMenu"), "", "iconMenu.png"));
            tableData.add(new ShortCut("F7", "", message(lang, "Operations"), "", "iconOperation.png"));
            tableData.add(new ShortCut("F8", "", message(lang, "Home"), "", "iconMyBox.png"));
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
            tableData.add(new ShortCut("", "", message(lang, "Snapshot"), "", "iconSnapshot.png"));
            tableData.add(new ShortCut("", "", message(lang, "Sort"), "", "iconSort.png"));
            tableData.add(new ShortCut("", "", message(lang, "Statistic"), "", "iconStatistic.png"));
            tableData.add(new ShortCut("", "", message(lang, "Style"), "", "iconStyle.png"));
            tableData.add(new ShortCut("", "", message(lang, "SVG"), "", "iconSVG.png"));
            tableData.add(new ShortCut("", "", message(lang, "Input"), "", "iconInput.png"));
            tableData.add(new ShortCut("", "", message(lang, "Validate"), "", "iconVerify.png"));
            tableData.add(new ShortCut("", "", message(lang, "Transparent"), "", "iconOpacity.png"));
            tableData.add(new ShortCut("", "", message(lang, "Typesetting"), "", "iconTypesetting.png"));
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

    public void makeDocuments(String lang) {
        task = new FxTask<Void>(this) {
            private File path;

            @Override
            protected boolean handle() {
                try {
                    path = new File(AppVariables.MyboxDataPath + "/doc/");

                    File file = HelpTools.makeReadMe(lang);
                    if ("zh".equals(lang)) {
                        FileCopyTools.copyFile(file, new File(path, "index.html"), true, true);
                    }
                    task.setInfo(file.getAbsolutePath());

                    baseTitle = message(lang, "Shortcuts");
                    makeList(lang);
                    StringTable table = makeStringTable(this);
                    String html = HtmlWriteTools.html(baseTitle, HtmlStyles.DefaultStyle, table.body());
                    file = new File(path, "mybox_shortcuts_" + lang + ".html");
                    file = TextFileTools.writeFile(file, html);
                    task.setInfo(file.getAbsolutePath());

                    file = HelpTools.makeInterfaceTips(lang);
                    FileCopyTools.copyFile(file, new File(path, file.getName()), true, true);
                    task.setInfo(file.getAbsolutePath());

                    file = HelpTools.usefulLinks(lang);
                    FileCopyTools.copyFile(file, new File(path, file.getName()), true, true);
                    task.setInfo(file.getAbsolutePath());

                    file = imageStories(this, true, lang);
                    FileCopyTools.copyFile(file, new File(path, file.getName()), true, true);
                    task.setInfo(file.getAbsolutePath());

                    boolean isZh = "zh".equals(lang);
                    file = FxFileTools.getInternalFile("/data/examples/ColorsArtPaints.csv",
                            "data", "ColorsArtPaints.csv", isZh);
                    List<ColorData> colors = ColorDataTools.readCSV(this, file, true);
                    colorsDoc(lang, colors, message(lang, "ArtPaints"), "art_paints");

                    file = FxFileTools.getInternalFile("/data/examples/ColorsWeb.csv",
                            "data", "ColorsWeb.csv", isZh);
                    colors = ColorDataTools.readCSV(this, file, true);
                    colorsDoc(lang, colors, message(lang, "WebCommonColors"), "web");

                    file = FxFileTools.getInternalFile("/data/examples/ColorsChinese.csv",
                            "data", "ColorsChinese.csv", isZh);
                    colors = ColorDataTools.readCSV(this, file, true);
                    colorsDoc(lang, colors, message(lang, "ChineseTraditionalColors"), "chinese");

                    file = FxFileTools.getInternalFile("/data/examples/ColorsJapanese.csv",
                            "data", "ColorsJapanese.csv", isZh);
                    colors = ColorDataTools.readCSV(this, file, true);
                    colorsDoc(lang, colors, message(lang, "JapaneseTraditionalColors"), "japanese");

                    file = FxFileTools.getInternalFile("/data/examples/ColorsColorhexa.csv",
                            "data", "ColorsColorhexa.csv", isZh);
                    colors = ColorDataTools.readCSV(this, file, true);
                    colorsDoc(lang, colors, message(lang, "HexaColors"), "colorhexa");

                    colors = new ArrayList<>();
                    for (StyleData.StyleColor style : StyleData.StyleColor.values()) {
                        colors.add(new ColorData(color(style, true).getRGB(), message(lang, "MyBoxColor" + style.name() + "Dark")));
                        colors.add(new ColorData(color(style, false).getRGB(), message(lang, "MyBoxColor" + style.name() + "Light")));
                    }
                    colorsDoc(lang, colors, message(lang, "MyBoxColors"), "mybox");

                    colors = PaletteTools.defaultColors(lang);
                    colorsDoc(lang, colors, message(lang, "DefaultPalette"), "default");

                    file = FxFileTools.getInternalFile("/data/examples/ColorsRYB12_" + lang + ".csv",
                            "data", "ColorsRYB12_" + lang + ".csv", isZh);
                    colors = ColorDataTools.readCSV(this, file, true);
                    colorsDoc(lang, colors, message(lang, "ArtHuesWheel") + "-" + message(lang, "Colors12"), "ryb12");

                    file = FxFileTools.getInternalFile("/data/examples/ColorsRYB24_" + lang + ".csv",
                            "data", "ColorsRYB24_" + lang + ".csv", isZh);
                    colors = ColorDataTools.readCSV(this, file, true);
                    colorsDoc(lang, colors, message(lang, "ArtHuesWheel") + "-" + message(" + lang + ", "Colors24"), "ryb24");

                    colors = PaletteTools.artHuesWheel(lang, 1);
                    colorsDoc(lang, colors, message(lang, "ArtHuesWheel") + "-" + message(lang, "Colors360"), "ryb360");

                    colors = PaletteTools.opticalHuesWheel(lang, 30);
                    colorsDoc(lang, colors, message(lang, "OpticalHuesWheel") + "-" + message(lang, "Colors12"), "rgb12");

                    colors = PaletteTools.opticalHuesWheel(lang, 15);
                    colorsDoc(lang, colors, message(lang, "OpticalHuesWheel") + "-" + message(lang, "Colors24"), "rgb24");

                    colors = PaletteTools.opticalHuesWheel(lang, 1);
                    colorsDoc(lang, colors, message(lang, "OpticalHuesWheel") + "-" + message(lang, "Colors360"), "rgb360");

                    colors = PaletteTools.greyScales(lang);
                    colorsDoc(lang, colors, message(lang, "GrayScale"), "gray");

                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            protected void colorsDoc(String lang, List<ColorData> colors, String title, String name) {
                try {
                    if (colors == null || colors.isEmpty()) {
                        return;
                    }
                    List<String> columns = new ArrayList<>();
                    columns.addAll(Arrays.asList(message(lang, "Name"), message(lang, "Color"),
                            "HSBA", message(lang, "RGBInvertColor"), message(lang, "RGBInvertColor") + "-" + message(lang, "Value"),
                            message(lang, "RYBComplementaryColor"), message(lang, "RYBComplementaryColor") + "-" + message(lang, "Value"), "RGBA", "RGB",
                            message(lang, "RYBAngle"), message(lang, "Hue"), message(lang, "Saturation"), message(lang, "Brightness"), message(lang, "Opacity")));
                    makeDoc(lang, colors, columns, title + " - RGBA", name + "_rgba_" + lang);

                    columns = new ArrayList<>();
                    columns.addAll(Arrays.asList(message(lang, "Name"), message(lang, "Color"), "RGBA", "RGB",
                            message(lang, "RYBAngle"), message(lang, "Hue"), message(lang, "Saturation"), message(lang, "Brightness"), message(lang, "Opacity"),
                            "HSBA", "sRGB", message(lang, "CalculatedCMYK"),
                            message(lang, "RGBInvertColor"), message(lang, "RGBInvertColor") + "-" + message(lang, "Value"),
                            message(lang, "RYBComplementaryColor"), message(lang, "RYBComplementaryColor") + "-" + message(lang, "Value"),
                            "Adobe RGB", "Apple RGB", "ECI RGB", "sRGB Linear", "Adobe RGB Linear", "Apple RGB Linear",
                            "ECI CMYK", "Adobe CMYK", "XYZ", "CIE-L*ab", "LCH(ab)", "CIE-L*uv", "LCH(uv)", message(lang, "Value")
                    ));
                    makeDoc(lang, colors, columns, title + " - " + message(lang, "All"), name + "_all_" + lang);

                } catch (Exception e) {
                    error = e.toString();
                }
            }

            protected void makeDoc(String lang, List<ColorData> colors, List<String> columns, String title, String name) {
                try {
                    if (colors == null || colors.isEmpty()) {
                        return;
                    }
                    colors.addAll(PaletteTools.speicalColors(lang));
                    StringTable table = new StringTable(columns, title);
                    for (ColorData c : colors) {
                        if (!isWorking()) {
                            return;
                        }
                        if (c.needConvert()) {
                            c.convert();
                        }
                        List<String> row = new ArrayList<>();
                        for (String column : columns) {
                            if (message(lang, "Name").equals(column)) {
                                row.add(c.getColorName());
                            } else if (message(lang, "Color").equals(column)) {
                                row.add("<DIV style=\"width: 50px;  background-color:"
                                        + c.getRgb() + "; \">&nbsp;&nbsp;&nbsp;</DIV>");
                            } else if ("RGBA".equals(column)) {
                                row.add(c.getRgba());
                            } else if ("RGB".equals(column)) {
                                row.add(c.getRgb());
                            } else if (message(lang, "RYBAngle").equals(column)) {
                                row.add(c.getRybAngle());
                            } else if (message(lang, "Hue").equals(column)) {
                                row.add(c.getHue());
                            } else if (message(lang, "Saturation").equals(column)) {
                                row.add(c.getSaturation());
                            } else if (message(lang, "Brightness").equals(column)) {
                                row.add(c.getBrightness());
                            } else if (message(lang, "Opacity").equals(column)) {
                                row.add(c.getOpacity());
                            } else if ("HSBA".equals(column)) {
                                row.add(c.getHsb());
                            } else if ("sRGB".equals(column)) {
                                row.add(c.getSrgb());
                            } else if (message(lang, "CalculatedCMYK").equals(column)) {
                                row.add(c.getCalculatedCMYK());
                            } else if (message(lang, "RGBInvertColor").equals(column)) {
                                row.add("<DIV style=\"width: 50px;  background-color:"
                                        + FxColorTools.color2rgb(c.getInvertColor()) + "; \">&nbsp;&nbsp;&nbsp;</DIV>");
                            } else if ((message(lang, "RGBInvertColor") + "-" + message(lang, "Value")).equals(column)) {
                                row.add(c.getInvertRGB());
                            } else if (message(lang, "RYBComplementaryColor").equals(column)) {
                                row.add("<DIV style=\"width: 50px;  background-color:"
                                        + FxColorTools.color2rgb(c.getComplementaryColor()) + "; \">&nbsp;&nbsp;&nbsp;</DIV>");
                            } else if ((message(lang, "RYBComplementaryColor") + "-" + message(lang, "Value")).equals(column)) {
                                row.add(c.getComplementaryRGB());
                            } else if ("Adobe RGB".equals(column)) {
                                row.add(c.getAdobeRGB());
                            } else if ("Apple RGB".equals(column)) {
                                row.add(c.getAppleRGB());
                            } else if ("ECI RGB".equals(column)) {
                                row.add(c.getEciRGB());
                            } else if ("sRGB Linear".equals(column)) {
                                row.add(c.getSRGBLinear());
                            } else if ("Adobe RGB Linear".equals(column)) {
                                row.add(c.getAdobeRGBLinear());
                            } else if ("Apple RGB Linear".equals(column)) {
                                row.add(c.getAppleRGBLinear());
                            } else if ("ECI CMYK".equals(column)) {
                                row.add(c.getEciCMYK());
                            } else if ("Adobe CMYK".equals(column)) {
                                row.add(c.getAdobeCMYK());
                            } else if ("XYZ".equals(column)) {
                                row.add(c.getXyz());
                            } else if ("CIE-L*ab".equals(column)) {
                                row.add(c.getCieLab());
                            } else if ("LCH(ab)".equals(column)) {
                                row.add(c.getLchab());
                            } else if ("CIE-L*uv".equals(column)) {
                                row.add(c.getCieLuv());
                            } else if ("LCH(uv)".equals(column)) {
                                row.add(c.getLchuv());
                            } else if (message(lang, "Value").equals(column)) {
                                row.add(c.getColorValue() + "");
                            }
                        }
                        table.add(row);
                    }

                    String html = HtmlWriteTools.html(title, HtmlStyles.TableStyle, table.body());
                    File file = new File(path, "mybox_palette_" + name + ".html");
                    file.delete();
                    TextFileTools.writeFile(file, html);
                    task.setInfo(file.getAbsolutePath());
                } catch (Exception e) {
                    error = e.toString();
                }
            }

            @Override
            protected void whenSucceeded() {
                browse(path);
                close();
            }

        };
        start(task);
    }

    /*
        static
     */
    public static void documents() {
        try {

            ShortcutsController zh = (ShortcutsController) WindowTools.openStage(Fxmls.ShortcutsFxml, Languages.BundleZhCN);
            zh.makeDocuments("zh");

            ShortcutsController en = (ShortcutsController) WindowTools.openStage(Fxmls.ShortcutsFxml, Languages.BundleEn);
            en.makeDocuments("en");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
