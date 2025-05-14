package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.tools.Data2DConvertTools;
import static mara.mybox.data2d.tools.Data2DExampleTools.CompatibilityTesting;
import static mara.mybox.data2d.tools.Data2DExampleTools.DetailedTesting;
import static mara.mybox.data2d.tools.Data2DExampleTools.TestEnvironment;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorDataTools;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.BaseNodeTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
import static mara.mybox.fxml.HelpTools.imageStories;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.FxColorTools;
import mara.mybox.fxml.image.PaletteTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleData;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.CurrentBundle;
import static mara.mybox.value.AppVariables.CurrentLangName;
import static mara.mybox.value.Colors.color;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2024-12-12
 * @License Apache License Version 2.0
 */
public class MyBoxDocumentsController extends BaseTaskController {

    protected File path;
    protected final SimpleBooleanProperty finishNotify;
    protected int index;
    protected String realLang;
    protected ResourceBundle realBoundle;

    @FXML
    protected CheckBox readmeCheck, functionsCheck, tipsCheck, shortcutsCheck,
            testingCheck, treeCheck, paletteCheck, linksCheck, imagesCheck;

    public MyBoxDocumentsController() {
        baseTitle = message("MakeDocuments");
        finishNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            path = new File(AppVariables.MyboxDataPath + "/doc/");

            readmeCheck.setSelected(UserConfig.getBoolean(baseName + "Readme", true));
            readmeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "Readme", nv);
                }
            });

            functionsCheck.setSelected(UserConfig.getBoolean(baseName + "Functions", true));
            functionsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "Functions", nv);
                }
            });

            tipsCheck.setSelected(UserConfig.getBoolean(baseName + "Tips", true));
            tipsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "Tips", nv);
                }
            });

            treeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    if (treeCheck.isSelected()) {
                        treeCheck.setStyle(NodeStyleTools.darkRedTextStyle());
                    } else {
                        treeCheck.setStyle(null);
                    }
                    UserConfig.setBoolean(baseName + "Tree", nv);
                }
            });
            treeCheck.setSelected(UserConfig.getBoolean(baseName + "Tree", true));

            shortcutsCheck.setSelected(UserConfig.getBoolean(baseName + "Shortcuts", true));
            shortcutsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "Shortcuts", nv);
                }
            });

            testingCheck.setSelected(UserConfig.getBoolean(baseName + "Testing", true));
            testingCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "Testing", nv);
                }
            });

            paletteCheck.setSelected(UserConfig.getBoolean(baseName + "Palette", true));
            paletteCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "Palette", nv);
                }
            });

            linksCheck.setSelected(UserConfig.getBoolean(baseName + "Links", true));
            linksCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "Links", nv);
                }
            });

            imagesCheck.setSelected(UserConfig.getBoolean(baseName + "Images", true));
            imagesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "Images", nv);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void startTask() {
        defaultStartTask();

        if (shortcutsCheck.isSelected()) {
            showLogs(message("Shortcuts") + "...");
            ShortcutsController.documents(this, path);
        }

        if (functionsCheck.isSelected()) {
            showLogs(message("FunctionsList") + "...");
            FunctionsListController.makeFunctionsList(this, path);
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        realLang = CurrentLangName;
        realBoundle = CurrentBundle;
        if (readmeCheck.isSelected()) {
            readme("zh");
            readme("en");
        }
        if (task == null || !task.isWorking()) {
            return false;
        }
        if (tipsCheck.isSelected()) {
            tips("zh");
            tips("en");
        }
        if (task == null || !task.isWorking()) {
            return false;
        }
        if (testingCheck.isSelected()) {
            testing("zh");
            testing("en");
        }
        if (task == null || !task.isWorking()) {
            return false;
        }
        if (linksCheck.isSelected()) {
            links("zh");
            links("en");
        }
        if (task == null || !task.isWorking()) {
            return false;
        }
        if (treeCheck.isSelected()) {
            trees();
        }
        if (task == null || !task.isWorking()) {
            return false;
        }
        if (imagesCheck.isSelected()) {
            images("zh");
            images("en");
        }
        if (task == null || !task.isWorking()) {
            return false;
        }
        if (paletteCheck.isSelected()) {
            palettes("zh");
            palettes("en");
        }
        return true;
    }

    protected boolean readme(String lang) {
        try {
            File file = HelpTools.makeReadMe(lang);
            if ("zh".equals(lang)) {
                FileCopyTools.copyFile(file, new File(path, "index.html"), true, true);
            }
            showLogs(file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    protected boolean tips(String lang) {
        try {
            File file = HelpTools.makeInterfaceTips(lang);
            FileTools.override(file, new File(path, file.getName()), true);
            showLogs(file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    protected boolean links(String lang) {
        try {
            File file = HelpTools.usefulLinks(lang);
            FileTools.override(file, new File(path, file.getName()), true);
            showLogs(file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    protected boolean images(String lang) {
        try {
            File file = imageStories(task, true, lang);
            FileTools.override(file, new File(path, file.getName()), true);
            showLogs(file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    protected boolean testing(String lang) {
        try {
            DataFileCSV data = TestEnvironment(lang);
            data.setFile(FxFileTools.getInternalFile("/data/examples/ST_TestEnvironment_" + lang + ".csv"))
                    .setCharset(Charset.forName("UTF-8")).setDelimiter(",").setHasHeader(true);
            File htmlFile = new File(path, "mybox_TestEnvironment_" + lang + ".html");
            Data2DConvertTools.toHtmlFile(task, data, htmlFile);
            showLogs(htmlFile.getAbsolutePath());

            data = CompatibilityTesting(lang);
            data.setFile(FxFileTools.getInternalFile("/data/examples/ST_CompatibilityTesting_" + lang + ".csv"))
                    .setCharset(Charset.forName("UTF-8")).setDelimiter(",").setHasHeader(true);
            htmlFile = new File(path, "mybox_CompatibilityTesting_" + lang + ".html");
            Data2DConvertTools.toHtmlFile(task, data, htmlFile);
            showLogs(htmlFile.getAbsolutePath());

            data = DetailedTesting(lang);
            data.setFile(FxFileTools.getInternalFile("/data/examples/ST_DetailedTesting_" + lang + ".csv"))
                    .setCharset(Charset.forName("UTF-8")).setDelimiter(",").setHasHeader(true);
            htmlFile = new File(path, "mybox_DetailedTesting_" + lang + ".html");
            Data2DConvertTools.toHtmlFile(task, data, htmlFile);
            showLogs(htmlFile.getAbsolutePath());

            Platform.runLater(() -> {
                FunctionsListController.makeVerificationList(this, path, lang);
            });
            Platform.requestNextPulse();

            return true;
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    public class TreeCase {

        public String tableName, lang;

        public TreeCase(String tableName, String lang) {
            this.tableName = tableName;
            this.lang = lang;
        }
    }

    protected boolean trees() {
        try {
            List<TreeCase> cases = new ArrayList<>();
            cases.add(new TreeCase("Text", "zh"));
            cases.add(new TreeCase("Text", "en"));
            cases.add(new TreeCase("Html", "zh"));
            cases.add(new TreeCase("Html", "en"));
            cases.add(new TreeCase("MathFunction", "zh"));
            cases.add(new TreeCase("MathFunction", "en"));
            cases.add(new TreeCase("WebFavorite", "zh"));
            cases.add(new TreeCase("WebFavorite", "en"));
            cases.add(new TreeCase("SQL", "zh"));
            cases.add(new TreeCase("SQL", "en"));
            cases.add(new TreeCase("ImageScope", "zh"));
            cases.add(new TreeCase("ImageScope", "en"));
            cases.add(new TreeCase("JShell", "zh"));
            cases.add(new TreeCase("JShell", "en"));
            cases.add(new TreeCase("JEXL", "zh"));
            cases.add(new TreeCase("JEXL", "en"));
            cases.add(new TreeCase("JavaScript", "zh"));
            cases.add(new TreeCase("JavaScript", "en"));
            cases.add(new TreeCase("RowExpression", "zh"));
            cases.add(new TreeCase("RowExpression", "en"));
            cases.add(new TreeCase("DataColumn", "zh"));
            cases.add(new TreeCase("DataColumn", "en"));
            cases.add(new TreeCase("GeographyCode", "zh"));
            cases.add(new TreeCase("GeographyCode", "en"));

            index = 0;
            finishNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (index < cases.size()) {
                        Platform.runLater(() -> {
                            TreeCase theCase = cases.get(index);
                            treeHtml(theCase.tableName, theCase.lang);
                            index++;
                        });
                        Platform.requestNextPulse();
                    }
                }
            });
            finishNotify();
            return true;
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    protected void finishNotify() {
        AppVariables.isTesting = false;
        finishNotify.set(!finishNotify.get());
    }

    protected boolean treeHtml(String tableName, String lang) {
        try {
            AppVariables.isTesting = true;
            CurrentLangName = lang;
            CurrentBundle = "zh".equals(lang) ? Languages.BundleZhCN : Languages.BundleEn;
            BaseNodeTable nodeTable = BaseNodeTable.create(tableName);
            nodeTable.truncate();
            DataTreeController dataController;
            if (tableName.equals("GeographyCode")) {
                dataController = (GeographyCodeController) WindowTools.openStage(Fxmls.GeographyCodeFxml);
            } else {
                dataController = (DataTreeController) WindowTools.openStage(Fxmls.DataTreeFxml);
                dataController.initDataTree(nodeTable, null);
            }
            if (dataController == null) {
                finishNotify();
                return false;
            }
//            treeController.setIconified(true);
            DataNode rootNode = nodeTable.getRoot();
            if (rootNode == null) {
                finishNotify();
                return false;
            }
            TreeItem<DataNode> rootItem = new TreeItem(rootNode);
            dataController.treeController.treeView.setRoot(rootItem);
//            popInformation(message("Handling") + ": " + tableName);
            DataTreeImportController importController = (DataTreeImportController) WindowTools
                    .openStage(Fxmls.DataTreeImportFxml);
//            importController.setIconified(true);
            importController.miaoCheck.setSelected(false);
            importController.importExamples(dataController, rootNode, nodeTable.exampleFileLang(lang));
            importController.taskClosedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    Platform.runLater(() -> {
                        importController.close();
                        DataTreeExportController exportController = (DataTreeExportController) WindowTools
                                .openStage(Fxmls.DataTreeExportFxml);
//                        exportController.setIconified(true);
                        exportController.setParameters(dataController, rootNode);
                        exportController.selectAllFormat(false);
                        exportController.treeHtmlCheck.setSelected(true);
                        exportController.openCheck.setSelected(false);
                        exportController.startAction();
                        exportController.taskClosedNotify.addListener(new ChangeListener<Boolean>() {
                            @Override
                            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                                Platform.runLater(() -> {
                                    File file = exportController.treeHtmlFile;
                                    if (file != null && file.exists()) {
                                        File htmlFile = new File(path,
                                                "mybox_examples_" + nodeTable.getTableName() + "_" + lang + ".html");
                                        FileTools.override(file, htmlFile, true);
                                        showLogs(htmlFile.getAbsolutePath());
                                    } else {
                                        showLogs(message("Failed"));
                                    }
                                    exportController.close();
                                    dataController.close();
                                    nodeTable.truncate();
                                    CurrentLangName = realLang;
                                    CurrentBundle = realBoundle;
                                    finishNotify();
                                });
                                Platform.requestNextPulse();
                            }
                        });
                    });
                    Platform.requestNextPulse();
                }
            });
            return true;
        } catch (Exception e) {
            error = e.toString();
            finishNotify();
            return false;
        }
    }

    protected boolean palettes(String lang) {
        try {
            File file = FxFileTools.getInternalFile("/data/examples/ColorsArtPaints.csv",
                    "data", "ColorsArtPaints_" + lang + ".csv", true);
            List<ColorData> colors = ColorDataTools.readCSV(task, file, true);
            palettes(lang, colors, message(lang, "ArtPaints"), "art_paints");

            file = FxFileTools.getInternalFile("/data/examples/ColorsWeb.csv",
                    "data", "ColorsWeb_" + lang + ".csv", true);
            colors = ColorDataTools.readCSV(task, file, true);
            palettes(lang, colors, message(lang, "WebCommonColors"), "web");

            file = FxFileTools.getInternalFile("/data/examples/ColorsChinese.csv",
                    "data", "ColorsChinese_" + lang + ".csv", true);
            colors = ColorDataTools.readCSV(task, file, true);
            palettes(lang, colors, message(lang, "ChineseTraditionalColors"), "chinese");

            file = FxFileTools.getInternalFile("/data/examples/ColorsJapanese.csv",
                    "data", "ColorsJapanese_" + lang + ".csv", true);
            colors = ColorDataTools.readCSV(task, file, true);
            palettes(lang, colors, message(lang, "JapaneseTraditionalColors"), "japanese");

            file = FxFileTools.getInternalFile("/data/examples/ColorsColorhexa.csv",
                    "data", "ColorsColorhexa_" + lang + ".csv", true);
            colors = ColorDataTools.readCSV(task, file, true);
            palettes(lang, colors, message(lang, "HexaColors"), "colorhexa");

            colors = new ArrayList<>();
            for (StyleData.StyleColor style : StyleData.StyleColor.values()) {
                colors.add(new ColorData(color(style, true).getRGB(), message(lang, "MyBoxColor" + style.name() + "Dark")));
                colors.add(new ColorData(color(style, false).getRGB(), message(lang, "MyBoxColor" + style.name() + "Light")));
            }
            palettes(lang, colors, message(lang, "MyBoxColors"), "mybox");

            colors = PaletteTools.defaultColors(lang);
            palettes(lang, colors, message(lang, "DefaultPalette"), "default");

            file = FxFileTools.getInternalFile("/data/examples/ColorsRYB12_" + lang + ".csv",
                    "data", "ColorsRYB12_" + lang + ".csv", true);
            colors = ColorDataTools.readCSV(task, file, true);
            palettes(lang, colors, message(lang, "ArtHuesWheel") + "-" + message(lang, "Colors12"), "ryb12");

            file = FxFileTools.getInternalFile("/data/examples/ColorsRYB24_" + lang + ".csv",
                    "data", "ColorsRYB24_" + lang + ".csv", true);
            colors = ColorDataTools.readCSV(task, file, true);
            palettes(lang, colors, message(lang, "ArtHuesWheel") + "-" + message(" + lang + ", "Colors24"), "ryb24");

            colors = PaletteTools.artHuesWheel(lang, 1);
            palettes(lang, colors, message(lang, "ArtHuesWheel") + "-" + message(lang, "Colors360"), "ryb360");

            colors = PaletteTools.opticalHuesWheel(lang, 30);
            palettes(lang, colors, message(lang, "OpticalHuesWheel") + "-" + message(lang, "Colors12"), "rgb12");

            colors = PaletteTools.opticalHuesWheel(lang, 15);
            palettes(lang, colors, message(lang, "OpticalHuesWheel") + "-" + message(lang, "Colors24"), "rgb24");

            colors = PaletteTools.opticalHuesWheel(lang, 1);
            palettes(lang, colors, message(lang, "OpticalHuesWheel") + "-" + message(lang, "Colors360"), "rgb360");

            colors = PaletteTools.greyScales(lang);
            palettes(lang, colors, message(lang, "GrayScale"), "gray");
            return true;
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    protected void palettes(String lang, List<ColorData> colors, String title, String name) {
        try {
            if (colors == null || colors.isEmpty()) {
                return;
            }
            List<String> columns = new ArrayList<>();
            columns.addAll(Arrays.asList(message(lang, "Name"), message(lang, "Color"),
                    "HSBA", message(lang, "RGBInvertColor"), message(lang, "RGBInvertColor") + "-" + message(lang, "Value"),
                    message(lang, "RYBComplementaryColor"), message(lang, "RYBComplementaryColor") + "-" + message(lang, "Value"), "RGBA", "RGB",
                    message(lang, "RYBAngle"), message(lang, "Hue"), message(lang, "Saturation"), message(lang, "Brightness"), message(lang, "Opacity")));
            makeColorDoc(lang, colors, columns, title + " - RGBA", name + "_rgba_" + lang);

            columns = new ArrayList<>();
            columns.addAll(Arrays.asList(message(lang, "Name"), message(lang, "Color"), "RGBA", "RGB",
                    message(lang, "RYBAngle"), message(lang, "Hue"), message(lang, "Saturation"), message(lang, "Brightness"), message(lang, "Opacity"),
                    "HSBA", "sRGB", message(lang, "CalculatedCMYK"),
                    message(lang, "RGBInvertColor"), message(lang, "RGBInvertColor") + "-" + message(lang, "Value"),
                    message(lang, "RYBComplementaryColor"), message(lang, "RYBComplementaryColor") + "-" + message(lang, "Value"),
                    "Adobe RGB", "Apple RGB", "ECI RGB", "sRGB Linear", "Adobe RGB Linear", "Apple RGB Linear",
                    "ECI CMYK", "Adobe CMYK", "XYZ", "CIE-L*ab", "LCH(ab)", "CIE-L*uv", "LCH(uv)", message(lang, "Value")
            ));
            makeColorDoc(lang, colors, columns, title + " - " + message(lang, "All"), name + "_all_" + lang);

        } catch (Exception e) {
            error = e.toString();
        }
    }

    protected void makeColorDoc(String lang, List<ColorData> colors, List<String> columns, String title, String name) {
        try {
            if (colors == null || colors.isEmpty()) {
                return;
            }
            colors.addAll(PaletteTools.speicalColors(lang));
            StringTable table = new StringTable(columns, title);
            for (ColorData c : colors) {
                if (!task.isWorking()) {
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
            showLogs(file.getAbsolutePath());
        } catch (Exception e) {
            error = e.toString();
        }
    }

    @Override
    public void handleTargetFiles() {
        recordTargetFiles();
        openTarget();
    }

    @FXML
    @Override
    public void openTarget() {
        browse(path);
    }

    @FXML
    @Override
    public void selectAllAction() {
        selectAll(true);
    }

    @FXML
    @Override
    public void selectNoneAction() {
        selectAll(false);
    }

    public void selectAll(boolean select) {
        readmeCheck.setSelected(select);
        functionsCheck.setSelected(select);
        tipsCheck.setSelected(select);
        shortcutsCheck.setSelected(select);
        testingCheck.setSelected(select);
        paletteCheck.setSelected(select);
        linksCheck.setSelected(select);
        imagesCheck.setSelected(select);
        treeCheck.setSelected(select);
    }

    /*
        static methods
     */
    public static MyBoxDocumentsController open() {
        try {
            MyBoxDocumentsController controller
                    = (MyBoxDocumentsController) WindowTools.openStage(Fxmls.MyBoxDocumentsFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
