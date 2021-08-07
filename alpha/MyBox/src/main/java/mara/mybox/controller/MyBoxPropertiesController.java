/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import com.sun.management.OperatingSystemMXBean;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.lang.management.ManagementFactory;
import java.text.MessageFormat;
import javafx.geometry.Rectangle2D;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import mara.mybox.db.DerbyBase;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBoxPropertiesController extends HtmlTableController {

    public MyBoxPropertiesController() {
        baseTitle = Languages.message("JvmProperties");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initTable(null);

            addData(Languages.message("UserName"), System.getProperty("user.name"));
            addData(Languages.message("UserHome"), System.getProperty("user.home"));
            addData(Languages.message("UserDir"), System.getProperty("user.dir"));
            addData(Languages.message("MyBoxDataPath"), AppVariables.MyboxDataPath);
            addData(Languages.message("MyBoxDatabase"),
                    DerbyBase.protocol + "<BR>" + DerbyBase.dbHome() + "<BR>" + DerbyBase.login);
            addData(Languages.message("JvmName"), System.getProperty("java.version"));
            addData(Languages.message("JavaVendor"), System.getProperty("java.vendor"));
            addData(Languages.message("JvmName"), System.getProperty("java.vm.name"));
            addData(Languages.message("JvmInfo"), System.getProperty("java.vm.info"));
            addData(Languages.message("JavaHome"), System.getProperty("java.home"));
            addData(Languages.message("JavaIOTmpdir"), System.getProperty("java.io.tmpdir"));
            addData(Languages.message("JavafxRuntimeVersion"), System.getProperty("javafx.runtime.version"));

            int mb = 1024 * 1024;
            OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            addData(Languages.message("PhysicalMemory"), osmxb.getTotalPhysicalMemorySize() / mb + "MB");
            Runtime r = Runtime.getRuntime();
            addData(Languages.message("JvmXmx"), r.maxMemory() / mb + "MB");

            addData(Languages.message("SunJnuEncoding"), System.getProperty("sun.jnu.encoding"));
            addData(Languages.message("IOUnicodeEncoding"), System.getProperty("sun.io.unicode.encoding"));
            addData(Languages.message("CPUEndian"), System.getProperty("sun.cpu.endian"));
            addData(Languages.message("SunDesktop"), System.getProperty("sun.desktop"));

            // https://stackoverflow.com/questions/48915229/how-to-make-javafx-tell-if-im-on-a-4k-or-1080p-screen?r=SearchResults
            DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
            addData(Languages.message("PhysicalScreen"),
                    MessageFormat.format(Languages.message("PhysicalScreenValue"), Toolkit.getDefaultToolkit().getScreenResolution(),
                            dm.getWidth(), dm.getHeight(), dm.getRefreshRate(), dm.getBitDepth()));

            // https://stackoverflow.com/questions/28817460/how-do-i-find-out-whether-my-program-is-running-on-a-retina-screen?r=SearchResults
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getBounds();
            addData(Languages.message("JavaFxScreen"),
                    MessageFormat.format(Languages.message("JavaFxScreenValue"), screen.getDpi(),
                            bounds.getWidth(), bounds.getHeight(),
                            screen.getOutputScaleX(), screen.getOutputScaleY()));

            addData("WebView", new WebView().getEngine().getUserAgent());

            addData(Languages.message("FileEncoding"), System.getProperty("file.encoding"));
            addData(Languages.message("FileSeparator"), System.getProperty("file.separator"));
            addData(Languages.message("UserCountry"), System.getProperty("user.country"));
            addData(Languages.message("UserLanguage"), System.getProperty("user.language"));
            addData(Languages.message("OSName"), System.getProperty("os.name"));
            addData(Languages.message("OSVersion"), System.getProperty("os.version"));
            addData(Languages.message("OSArch"), System.getProperty("os.arch"));

            displayHtml();

        } catch (Exception e) {

        }
    }

}
