package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebEvent;
import mara.mybox.data.FunctionsList;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.tools.Data2DExampleTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-5-14
 * @License Apache License Version 2.0
 */
public class FunctionsListController extends ControlWebView {

    protected StringTable table;
    protected String goImageFile;
    protected Map<String, MenuItem> map;

    public FunctionsListController() {
        baseTitle = message("FunctionsList");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            goImageFile = AppVariables.MyboxDataPath + "/icons/iconGo.png";
            BufferedImage srcImage = SwingFXUtils.fromFXImage(StyleTools.getIconImage("iconGo.png"), null);
            ImageFileWriters.writeImageFile(null, srcImage, "png", goImageFile);

            goImageFile = new File(goImageFile).toURI().toString();

            webView.getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    if (map == null) {
                        return;
                    }
                    String name = ev.getData();
                    MenuItem menu = map.get(name);
                    if (menu != null) {
                        menu.fire();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean afterSceneLoaded() {
        try {
            if (!super.afterSceneLoaded()) {
                return false;
            }

            FunctionsList list = new FunctionsList(getMainMenu(), true, Languages.getLangName());
            table = list.make();
            map = list.getMap();
            if (table != null) {
                webView.getEngine().loadContent(table.html());
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public void functionsList(MyBoxDocumentsController maker, File path, String lang) {
        FxTask docTask = new FxTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    FunctionsList list = new FunctionsList(getMainMenu(), true, lang);
                    StringTable table = list.make();
                    File file = new File(path, "mybox_functions_" + lang + ".html");
                    TextFileTools.writeFile(file, table.html());
                    maker.showLogs(file.getAbsolutePath());

                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                close();
            }

        };
        start(docTask);
    }

    public void verificationList(MyBoxDocumentsController maker, File path, String lang) {
        FxTask docTask = new FxTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    boolean isChinese = Languages.isChinese(lang);
                    DataFileCSV data = Data2DExampleTools.MyBoxBaseVerificationList(myController, lang, false);
                    StringTable table = new StringTable(data.getDataName());
                    File file = data.getFile();
                    try (CSVParser parser = CSVParser.parse(file, data.getCharset(),
                            CsvTools.csvFormat(data.getDelimiter(), data.isHasHeader()))) {
                        List<String> names = parser.getHeaderNames();
                        table.setNames(names);
                        int col1 = names.indexOf(isChinese ? "打开界面" : "Open interface");
                        int col2 = names.indexOf(message(lang, "ModifyTime"));
                        Iterator<CSVRecord> iterator = parser.iterator();
                        while (iterator.hasNext() && (task == null || task.isWorking())) {
                            CSVRecord csvRecord = iterator.next();
                            if (csvRecord == null) {
                                continue;
                            }
                            List<String> htmlRow = new ArrayList<>();
                            for (String v : csvRecord) {
                                htmlRow.add(v);
                            }
                            htmlRow.set(col1, message(lang, "Success"));
                            htmlRow.set(col2, AppValues.AppVersionDate);
                            table.add(htmlRow);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    File htmlFile = new File(path, "mybox_BaseVerificationList_" + lang + ".html");
                    TextFileTools.writeFile(htmlFile, table.html());
                    maker.showLogs(file.getAbsolutePath());

                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                close();
            }

        };
        start(docTask);
    }

    /*
        static
     */
    public static void makeFunctionsList(MyBoxDocumentsController maker, File path) {
        try {
            FunctionsListController zh = (FunctionsListController) WindowTools
                    .openStage(Fxmls.FunctionsListFxml, Languages.BundleZhCN);
            zh.functionsList(maker, path, "zh");

            FunctionsListController en = (FunctionsListController) WindowTools
                    .openStage(Fxmls.FunctionsListFxml, Languages.BundleEn);
            en.functionsList(maker, path, "en");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public static void makeVerificationList(MyBoxDocumentsController maker, File path, String lang) {
        try {
            FunctionsListController c = (FunctionsListController) WindowTools.openStage(Fxmls.FunctionsListFxml,
                    Languages.isChinese(lang) ? Languages.BundleZhCN : Languages.BundleEn);
            c.verificationList(maker, path, lang);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
