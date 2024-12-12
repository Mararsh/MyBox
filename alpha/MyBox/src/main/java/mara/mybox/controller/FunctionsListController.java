package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebEvent;
import mara.mybox.data.FunctionsList;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

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
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            FunctionsList list = new FunctionsList(getMainMenu(), true, Languages.getLangName());
            table = list.make();
            map = list.getMap();
            if (table != null) {
                webView.getEngine().loadContent(table.html());
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void makeDocuments(MyBoxDocumentsController maker, File path, String lang) {
        task = new FxTask<Void>(this) {

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
        start(task);
    }

    /*
        static
     */
    public static void documents(MyBoxDocumentsController maker, File path) {
        try {
            FunctionsListController zh = (FunctionsListController) WindowTools
                    .openStage(Fxmls.FunctionsListFxml, Languages.BundleZhCN);
            zh.makeDocuments(maker, path, "zh");

            FunctionsListController en = (FunctionsListController) WindowTools
                    .openStage(Fxmls.FunctionsListFxml, Languages.BundleEn);
            en.makeDocuments(maker, path, "en");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
