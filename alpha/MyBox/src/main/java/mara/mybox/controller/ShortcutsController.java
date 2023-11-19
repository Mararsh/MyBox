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
import mara.mybox.data.FunctionsList;
import mara.mybox.data.ShortCut;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorDataTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fximage.PaletteTools;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.HelpTools;
import static mara.mybox.fxml.HelpTools.imageStories;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.SingletonTask;
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

            tableData.add(new ShortCut("F1", "", message("Start") + " / " + message("OK"), "CTRL+E / ALT+E", "iconStart.png"));
            tableData.add(new ShortCut("F2", "", message("Go"), "CTRL+G / ALT+G", "iconGo.png"));
            tableData.add(new ShortCut("F3", "", message("Preview"), "CTRL+U / ALT+U", "iconPreview.png"));
            tableData.add(new ShortCut("F4", "", message("Pop"), "CTRL+P / ALT+P", "iconPop.png"));
            tableData.add(new ShortCut("F5", "", message("SaveAs"), "CTRL+B / ALT+B", "iconSaveAs.png"));
            tableData.add(new ShortCut("F6", "", message("ContextMenu"), "", "iconMenu.png"));
            tableData.add(new ShortCut("F7", "", message("Operations"), "", "iconOperation.png"));
            tableData.add(new ShortCut("F8", "", message("Home"), "", "iconMyBox.png"));
            tableData.add(new ShortCut("F9", "", message("Tips"), "", "iconTips.png"));
            tableData.add(new ShortCut("F10", "", message("Synchronize"), "", "iconSynchronize.png"));
            tableData.add(new ShortCut("F11", "", message("ControlLeftPane"), "", "iconDoubleLeft.png"));
            tableData.add(new ShortCut("F12", "", message("ControlRightPane"), "", "iconDoubleRight.png"));
            tableData.add(new ShortCut("DELETE", "", message("Delete"), "CTRL+D / ALT+D", "iconDelete.png"));
            tableData.add(new ShortCut("PAGE_UP", "", message("Previous"), "ALT+PAGE_UP", "iconPrevious.png"));
            tableData.add(new ShortCut("PAGE_DOWN", "", message("Next"), "ALT+PAGE_DOWN", "iconNext.png"));
            tableData.add(new ShortCut("HOME", "", message("First"), "ALT+HOME", "iconFirst.png"));
            tableData.add(new ShortCut("END", "", message("Last"), "ALT+END", "iconLast.png"));
            tableData.add(new ShortCut("ESCAPE", "", message("Cancel") + " / " + message("ClosePopup"), "", "iconCancel.png"));

            tableData.add(new ShortCut("CTRL", "E", message("Start") + " /" + message("OK"), "F1 / ALT+E", "iconOK.png"));
            tableData.add(new ShortCut("CTRL", "C", message("Copy"), "ALT+C", "iconCopy.png"));
            tableData.add(new ShortCut("CTRL", "V", message("Paste"), "ALT+V", "iconPaste.png"));
            tableData.add(new ShortCut("CTRL", "Z", message("Undo"), "ALT+Z", "iconUndo.png"));
            tableData.add(new ShortCut("CTRL", "Y", message("Redo"), "ALT+Y", "iconRedo.png"));
            tableData.add(new ShortCut("CTRL", "D", message("Delete"), "DELETE / ALT+D", "iconDelete.png"));
            tableData.add(new ShortCut("CTRL", "X", message("Crop"), "ALT+X", "iconCrop.png"));
            tableData.add(new ShortCut("CTRL", "S", message("Save"), "ALT+S", "iconSave.png"));
            tableData.add(new ShortCut("CTRL", "B", message("SaveAs"), "F5 / ALT+B", "iconSaveAs.png"));
            tableData.add(new ShortCut("CTRL", "F", message("Find"), "ALT+F", "iconFind.png"));
            tableData.add(new ShortCut("CTRL", "H", message("Replace"), "ALT+H", "iconReplace.png"));
            tableData.add(new ShortCut("CTRL", "H", message("Histories"), "ALT+H", "iconHistory.png"));
            tableData.add(new ShortCut("CTRL", "R", message("Recover"), "ALT+R", "iconRecover.png"));
            tableData.add(new ShortCut("CTRL", "G", message("Go"), "F2 / ALT+G", "iconGo.png"));
            tableData.add(new ShortCut("CTRL", "N", message("Create"), "ALT+N", "iconAdd.png"));
            tableData.add(new ShortCut("CTRL", "A", message("SelectAll"), "ALT+A", "iconSelectAll.png"));
            tableData.add(new ShortCut("CTRL", "O", message("SelectNone"), "ALT+O", "iconSelectNone.png"));
            tableData.add(new ShortCut("CTRL", "U", message("Preview"), "F3 / ALT+U", "iconPreview.png"));
            tableData.add(new ShortCut("CTRL", "L", message("Clear"), "ALT+L", "iconClear.png"));
            tableData.add(new ShortCut("CTRL", "W", message("Withdraw"), "ALT+W", "iconUndo.png"));
            tableData.add(new ShortCut("CTRL", "P", message("Pop"), "F4 / ALT+P", "iconPop.png"));
            tableData.add(new ShortCut("CTRL", "Q", message("Query"), "ALT+Q", "iconQuery.png"));
            tableData.add(new ShortCut("CTRL", "K", message("PickColor"), "ALT+K", "iconPickColor.png"));
            tableData.add(new ShortCut("CTRL", "M", message("MyBoxClipboard"), "ALT+M", "iconClipboard.png"));
            tableData.add(new ShortCut("CTRL", "J", message("SystemClipboard"), "ALT+J", "iconSystemClipboard.png"));
            tableData.add(new ShortCut("CTRL", "1", message("OriginalSize"), "ALT+1", "iconOriginalSize.png"));
            tableData.add(new ShortCut("CTRL", "2", message("PaneSize"), "ALT+2", "iconPaneSize.png"));
            tableData.add(new ShortCut("CTRL", "3", message("ZoomIn"), "ALT+3", "iconZoomIn.png"));
            tableData.add(new ShortCut("CTRL", "4", message("ZoomOut"), "ALT+4", "iconZoomOut.png"));
            tableData.add(new ShortCut("CTRL", "-", message("DecreaseFontSize"), "", "iconMinus.png"));
            tableData.add(new ShortCut("CTRL", "=", message("IncreaseFontSize"), "", "iconPlus.png"));

            tableData.add(new ShortCut("ALT", "PAGE_UP", message("Previous"), "PAGE_UP", "iconPrevious.png"));
            tableData.add(new ShortCut("ALT", "PAGE_DOWN", message("Next"), "PAGE_DOWN", "iconNext.png"));
            tableData.add(new ShortCut("ALT", "HOME", message("First"), "HOME", "iconFirst.png"));
            tableData.add(new ShortCut("ALT", "END", message("Last"), "END", "iconLast.png"));

            tableData.add(new ShortCut("S", "", message("Play") + " / " + message("Pause"), "", "iconPlay.png"));
            tableData.add(new ShortCut("Q", "", message("Stop"), "", "iconStop.png"));
            tableData.add(new ShortCut("M", "", message("Mute") + " / " + message("Sound"), "", "iconMute.png"));
            tableData.add(new ShortCut("F", "", message("FullScreen"), "", "iconExpand.png"));

            tableData.add(new ShortCut("", "", message("Tips"), "", "iconTips.png"));
            tableData.add(new ShortCut("", "", message("Function"), "", "iconFunction.png"));
            tableData.add(new ShortCut("", "", message("Options"), "", "iconOptions.png"));
            tableData.add(new ShortCut("", "", message("Manage"), "", "iconManage.png"));
            tableData.add(new ShortCut("", "", message("Data"), "", "iconData.png"));
            tableData.add(new ShortCut("", "", message("Edit"), "", "iconEdit.png"));
            tableData.add(new ShortCut("", "", message("View"), "", "iconView.png"));
            tableData.add(new ShortCut("", "", message("Export"), "", "iconExport.png"));
            tableData.add(new ShortCut("", "", message("Import"), "", "iconImport.png"));
            tableData.add(new ShortCut("", "", message("Examples"), "", "iconExamples.png"));
            tableData.add(new ShortCut("", "", message("Demo"), "", "iconDemo.png"));
            tableData.add(new ShortCut("", "", message("Random"), "", "iconRandom.png"));
            tableData.add(new ShortCut("", "", message("Default"), "", "iconDefault.png"));
            tableData.add(new ShortCut("", "", message("Information"), "", "iconInfo.png"));
            tableData.add(new ShortCut("", "", message("MetaData"), "", "iconMeta.png"));
            tableData.add(new ShortCut("", "", message("File"), "", "iconFile.png"));
            tableData.add(new ShortCut("", "", message("Frames"), "", "iconFrame.png"));
            tableData.add(new ShortCut("", "", message("SelectFile"), "", "iconSelectFile.png"));
            tableData.add(new ShortCut("", "", message("SelectPath"), "", "iconSelectPath.png"));
            tableData.add(new ShortCut("", "", message("OpenDirectory"), "", "iconOpenPath.png"));
            tableData.add(new ShortCut("", "", message("Insert"), "", "iconInsert.png"));
            tableData.add(new ShortCut("", "", message("InsertFiles"), "", "iconInsertFile.png"));
            tableData.add(new ShortCut("", "", message("InsertDirectory"), "", "iconInsertPath.png"));
            tableData.add(new ShortCut("", "", message("FileBackups"), "", "iconBackup.png"));
            tableData.add(new ShortCut("", "", message("Anchor"), "", "iconAnchor.png"));
            tableData.add(new ShortCut("", "", message("Calculator"), "", "iconCalculator.png"));
            tableData.add(new ShortCut("", "", message("Delimiter"), "", "iconDelimiter.png"));
            tableData.add(new ShortCut("", "", message("LoadedSize"), "", "iconLoadSize.png"));
            tableData.add(new ShortCut("", "", message("Location"), "", "iconLocation.png"));
            tableData.add(new ShortCut("", "", message("Matrix"), "", "iconMatrix.png"));
            tableData.add(new ShortCut("", "", message("Panes"), "", "iconPanes.png"));
            tableData.add(new ShortCut("", "", message("More"), "", "iconMore.png"));
            tableData.add(new ShortCut("", "", message("Analyse"), "", "iconAnalyse.png"));
            tableData.add(new ShortCut("", "", message("Move"), "", "iconMove.png"));
            tableData.add(new ShortCut("", "", message("Add"), "", "iconNewItem.png"));
            tableData.add(new ShortCut("", "", message("Pause"), "", "iconPause.png"));
            tableData.add(new ShortCut("", "", message("Permissions"), "", "iconPermission.png"));
            tableData.add(new ShortCut("", "", message("Query"), "", "iconQuery.png"));
            tableData.add(new ShortCut("", "", message("Repeat"), "", "iconRepeat.png"));
            tableData.add(new ShortCut("", "", message("Sample"), "", "iconSample.png"));
            tableData.add(new ShortCut("", "", message("Shear"), "", "iconShear.png"));
            tableData.add(new ShortCut("", "", message("Snapshot"), "", "iconSnapshot.png"));
            tableData.add(new ShortCut("", "", message("Sort"), "", "iconSort.png"));
            tableData.add(new ShortCut("", "", message("Statistic"), "", "iconStatistic.png"));
            tableData.add(new ShortCut("", "", message("Style"), "", "iconStyle.png"));
            tableData.add(new ShortCut("", "", message("SVG"), "", "iconSVG.png"));
            tableData.add(new ShortCut("", "", message("Input"), "", "iconInput.png"));
            tableData.add(new ShortCut("", "", message("Validate"), "", "iconVerify.png"));
            tableData.add(new ShortCut("", "", message("Transparent"), "", "iconOpacity.png"));
            tableData.add(new ShortCut("", "", message("Typesetting"), "", "iconTypesetting.png"));
            tableData.add(new ShortCut("", "", message("Wrap"), "", "iconWrap.png"));
            tableData.add(new ShortCut("", "", message("Rulers"), "", "iconXRuler.png"));
            tableData.add(new ShortCut("", "", message("Yes"), "", "iconYes.png"));
            tableData.add(new ShortCut("", "", message("CopyToSystemClipboard"), "", "iconCopySystem.png"));
            tableData.add(new ShortCut("", "", message("LoadContentInSystemClipboard"), "", "iconImageSystem.png"));
            tableData.add(new ShortCut("", "", message("PasteContentInSystemClipboard"), "", "iconPasteSystem.png"));
            tableData.add(new ShortCut("", "", message("SystemMethod"), "", "iconSystemOpen.png"));
            tableData.add(new ShortCut("", "", message("CustomizeColors"), "", "iconColorWheel.png"));
            tableData.add(new ShortCut("", "", message("ColorPalette"), "", "iconColor.png"));
            tableData.add(new ShortCut("", "", message("Help"), "", "iconClaw.png"));

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

    public void makeDocuments(MainMenuController menu, String lang) {
        task = new SingletonTask<Void>(this) {
            private File path;

            @Override
            protected boolean handle() {
                try {
                    path = new File(AppVariables.MyboxDataPath + "/doc/");
                    {
                        File file = HelpTools.makeReadMe(lang);
                        if ("zh".equals(lang)) {
                            FileCopyTools.copyFile(file, new File(path, "index.html"), true, true);
                        }
                        task.setInfo(file.getAbsolutePath());
                    }

                    {
                        StringTable table = makeStringTable();
                        String html = HtmlWriteTools.html(message("Shortcuts"), HtmlStyles.DefaultStyle, table.body());
                        File file = new File(path, "mybox_shortcuts_" + lang + ".html");
                        file = TextFileTools.writeFile(file, html);
                        task.setInfo(file.getAbsolutePath());
                    }

                    {
                        File file = HelpTools.makeInterfaceTips(lang);
                        FileCopyTools.copyFile(file, new File(path, file.getName()), true, true);
                        task.setInfo(file.getAbsolutePath());
                    }

                    {
                        FunctionsList list = new FunctionsList(menu.menuBar, false);
                        StringTable table = list.make();
                        File file = new File(path, "mybox_functions_" + lang + ".html");
                        TextFileTools.writeFile(file, table.html());
                        task.setInfo(file.getAbsolutePath());
                    }

                    {
                        File file = HelpTools.usefulLinks(lang);
                        FileCopyTools.copyFile(file, new File(path, file.getName()), true, true);
                        task.setInfo(file.getAbsolutePath());
                    }

                    if ("zh".equals(lang)) {
                        File file = imageStories(this, true, "zh");
                        FileCopyTools.copyFile(file, new File(path, file.getName()), true, true);
                        task.setInfo(file.getAbsolutePath());

                        file = imageStories(this, true, "en");
                        FileCopyTools.copyFile(file, new File(path, file.getName()), true, true);
                        task.setInfo(file.getAbsolutePath());

                        file = FxFileTools.getInternalFile("/data/examples/ColorsArtPaints.csv",
                                "data", "ColorsArtPaints.csv", true);
                        List<ColorData> colors = ColorDataTools.readCSV(file, true);
                        colorsDoc("zh", colors, message("zh", "ArtPaints"), "art_paints");
                        colorsDoc("en", colors, message("en", "ArtPaints"), "art_paints");

                        file = FxFileTools.getInternalFile("/data/examples/ColorsWeb.csv",
                                "data", "ColorsWeb.csv", true);
                        colors = ColorDataTools.readCSV(file, true);
                        colorsDoc("zh", colors, message("zh", "WebCommonColors"), "web");
                        colorsDoc("en", colors, message("en", "WebCommonColors"), "web");

                        file = FxFileTools.getInternalFile("/data/examples/ColorsChinese.csv",
                                "data", "ColorsChinese.csv", true);
                        colors = ColorDataTools.readCSV(file, true);
                        colorsDoc("zh", colors, message("zh", "ChineseTraditionalColors"), "chinese");
                        colorsDoc("en", colors, message("en", "ChineseTraditionalColors"), "chinese");

                        file = FxFileTools.getInternalFile("/data/examples/ColorsJapanese.csv",
                                "data", "ColorsJapanese.csv", true);
                        colors = ColorDataTools.readCSV(file, true);
                        colorsDoc("zh", colors, message("zh", "JapaneseTraditionalColors"), "japanese");
                        colorsDoc("en", colors, message("en", "JapaneseTraditionalColors"), "japanese");

                        file = FxFileTools.getInternalFile("/data/examples/ColorsColorhexa.csv",
                                "data", "ColorsColorhexa.csv", true);
                        colors = ColorDataTools.readCSV(file, true);
                        colorsDoc("zh", colors, message("zh", "HexaColors"), "colorhexa");
                        colorsDoc("en", colors, message("en", "HexaColors"), "colorhexa");

                        colors = new ArrayList<>();
                        for (StyleData.StyleColor style : StyleData.StyleColor.values()) {
                            colors.add(new ColorData(color(style, true).getRGB(), message("zh", "MyBoxColor" + style.name() + "Dark")));
                            colors.add(new ColorData(color(style, false).getRGB(), message("zh", "MyBoxColor" + style.name() + "Light")));
                        }
                        colorsDoc("zh", colors, message("zh", "MyBoxColors"), "mybox");
                        colors = new ArrayList<>();
                        for (StyleData.StyleColor style : StyleData.StyleColor.values()) {
                            colors.add(new ColorData(color(style, true).getRGB(), message("en", "MyBoxColor" + style.name() + "Dark")));
                            colors.add(new ColorData(color(style, false).getRGB(), message("en", "MyBoxColor" + style.name() + "Light")));
                        }
                        colorsDoc("en", colors, message("en", "MyBoxColors"), "mybox");

                        colors = PaletteTools.defaultColors("zh");
                        colorsDoc("zh", colors, message("zh", "DefaultPalette"), "default");
                        colors = PaletteTools.defaultColors("en");
                        colorsDoc("en", colors, message("en", "DefaultPalette"), "default");

                        file = FxFileTools.getInternalFile("/data/examples/ColorsRYB12_zh.csv",
                                "data", "ColorsRYB12_zh.csv", true);
                        colors = ColorDataTools.readCSV(file, true);
                        colorsDoc("zh", colors, message("zh", "ArtHuesWheel") + "-" + message("zh", "Colors12"), "ryb12");
                        file = FxFileTools.getInternalFile("/data/examples/ColorsRYB12_en.csv",
                                "data", "ColorsRYB12_en.csv", true);
                        colors = ColorDataTools.readCSV(file, true);
                        colorsDoc("en", colors, message("en", "ArtHuesWheel") + "-" + message("en", "Colors12"), "ryb12");

                        file = FxFileTools.getInternalFile("/data/examples/ColorsRYB24_zh.csv",
                                "data", "ColorsRYB24_zh.csv", true);
                        colors = ColorDataTools.readCSV(file, true);
                        colorsDoc("zh", colors, message("zh", "ArtHuesWheel") + "-" + message("zh", "Colors24"), "ryb24");
                        file = FxFileTools.getInternalFile("/data/examples/ColorsRYB24_en.csv",
                                "data", "ColorsRYB24_en.csv", true);
                        colors = ColorDataTools.readCSV(file, true);
                        colorsDoc("en", colors, message("en", "ArtHuesWheel") + "-" + message("en", "Colors24"), "ryb24");

                        colors = PaletteTools.artHuesWheel("zh", 1);
                        colorsDoc("zh", colors, message("zh", "ArtHuesWheel") + "-" + message("zh", "Colors360"), "ryb360");
                        colors = PaletteTools.artHuesWheel("en", 1);
                        colorsDoc("en", colors, message("en", "ArtHuesWheel") + "-" + message("en", "Colors360"), "ryb360");

                        colors = PaletteTools.opticalHuesWheel("zh", 30);
                        colorsDoc("zh", colors, message("zh", "OpticalHuesWheel") + "-" + message("zh", "Colors12"), "rgb12");
                        colors = PaletteTools.opticalHuesWheel("en", 30);
                        colorsDoc("en", colors, message("en", "OpticalHuesWheel") + "-" + message("en", "Colors12"), "rgb12");

                        colors = PaletteTools.opticalHuesWheel("zh", 15);
                        colorsDoc("zh", colors, message("zh", "OpticalHuesWheel") + "-" + message("zh", "Colors24"), "rgb24");
                        colors = PaletteTools.opticalHuesWheel("en", 15);
                        colorsDoc("en", colors, message("en", "OpticalHuesWheel") + "-" + message("en", "Colors24"), "rgb24");

                        colors = PaletteTools.opticalHuesWheel("zh", 1);
                        colorsDoc("zh", colors, message("zh", "OpticalHuesWheel") + "-" + message("zh", "Colors360"), "rgb360");
                        colors = PaletteTools.opticalHuesWheel("en", 1);
                        colorsDoc("en", colors, message("en", "OpticalHuesWheel") + "-" + message("en", "Colors360"), "rgb360");

                        colors = PaletteTools.greyScales("zh");
                        colorsDoc("zh", colors, message("zh", "GrayScale"), "gray");
                        colors = PaletteTools.greyScales("en");
                        colorsDoc("en", colors, message("en", "GrayScale"), "gray");

                    }

                    return true;

                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            protected void colorsDoc(String lang, List<ColorData> colors, String title, String name) {
                try {
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
                    colors.addAll(PaletteTools.speicalColors(lang));
                    StringTable table = new StringTable(columns, title);
                    for (ColorData c : colors) {
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
                if ("zh".equals(lang)) {
                    browse(path);
                }
                close();
            }

        };
        start(task);
    }

    /*
        static
     */
    public static void documents(MainMenuController menu) {
        try {
            ShortcutsController zh = (ShortcutsController) WindowTools.openStage(Fxmls.ShortcutsFxml, Languages.BundleZhCN);
            if (zh != null) {
                zh.makeDocuments(menu, "zh");
            }
            ShortcutsController en = (ShortcutsController) WindowTools.openStage(Fxmls.ShortcutsFxml, Languages.BundleEn);
            if (en != null) {
                en.makeDocuments(menu, "en");
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
