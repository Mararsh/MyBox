package mara.mybox.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class TTFTools {

    public static List<String> ttfList() {
        List<String> names = new ArrayList<>();
        try {
            String os = SystemTools.os();
            File ttfPath;
            switch (os) {
                case "win":
                    ttfPath = new File("C:/Windows/Fonts/");
                    names.addAll(ttfList(ttfPath));
                    break;
                case "linux":
                    ttfPath = new File("/usr/share/fonts/");
                    names.addAll(ttfList(ttfPath));
                    ttfPath = new File("/usr/lib/kbd/consolefonts/");
                    names.addAll(ttfList(ttfPath));
                    break;
                case "mac":
                    ttfPath = new File("/Library/Fonts/");
                    names.addAll(ttfList(ttfPath));
                    ttfPath = new File("/System/Library/Fonts/");
                    names.addAll(ttfList(ttfPath));
                    break;
            }
            // http://wenq.org/wqy2/
            File wqy_microhei = FxFileTools.getInternalFile("/data/wqy-microhei.ttf", "data", "wqy-microhei.ttf");
            String wqy_microhei_name = wqy_microhei.getAbsolutePath() + "      " + Languages.message("wqy_microhei");
            if (!names.isEmpty() && names.get(0).contains("    ")) {
                names.add(1, wqy_microhei_name);
            } else {
                names.add(0, wqy_microhei_name);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return names;
    }

    public static List<String> ttfList(File path) {
        List<String> names = new ArrayList<>();
        try {
            if (path == null || !path.exists() || !path.isDirectory()) {
                return names;
            }
            File[] fontFiles = path.listFiles();
            if (fontFiles == null || fontFiles.length == 0) {
                return names;
            }
            for (File file : fontFiles) {
                String filename = file.getAbsolutePath();
                if (!filename.toLowerCase().endsWith(".ttf")) {
                    continue;
                }
                names.add(filename);
            }
            String pathname = path.getAbsolutePath() + File.separator;
            List<String> cnames = new ArrayList<>();
            if (names.contains(pathname + "STSONG.TTF")) {
                cnames.add(pathname + "STSONG.TTF" + "      \u534e\u6587\u5b8b\u4f53");
            }
            if (names.contains(pathname + "simfang.ttf")) {
                cnames.add(pathname + "simfang.ttf" + "      \u4eff\u5b8b");
            }
            if (names.contains(pathname + "simkai.ttf")) {
                cnames.add(pathname + "simkai.ttf" + "      \u6977\u4f53");
            }
            if (names.contains(pathname + "STKAITI.TTF")) {
                cnames.add(pathname + "STKAITI.TTF" + "      \u534e\u6587\u6977\u4f53");
            }
            if (names.contains(pathname + "SIMLI.TTF")) {
                cnames.add(pathname + "SIMLI.TTF" + "      \u96b6\u4e66");
            }
            if (names.contains(pathname + "STXINWEI.TTF")) {
                cnames.add(pathname + "STXINWEI.TTF" + "      \u534e\u6587\u65b0\u9b4f");
            }
            if (names.contains(pathname + "SIMYOU.TTF")) {
                cnames.add(pathname + "SIMYOU.TTF" + "      \u5e7c\u5706");
            }
            if (names.contains(pathname + "FZSTK.TTF")) {
                cnames.add(pathname + "FZSTK.TTF" + "      \u65b9\u6b63\u8212\u4f53");
            }
            if (names.contains(pathname + "STXIHEI.TTF")) {
                cnames.add(pathname + "STXIHEI.TTF" + "      \u534e\u6587\u7ec6\u9ed1");
            }
            if (names.contains(pathname + "simhei.ttf")) {
                cnames.add(pathname + "simhei.ttf" + "      \u9ed1\u4f53");
            }
            for (String name : names) {
                if (name.contains(pathname + "\u534e\u6587")) {
                    cnames.add(name);
                }
            }
            names.addAll(0, cnames);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return names;
    }

    public static String ttf(String item) {
        if (item == null) {
            return null;
        }
        int pos = item.indexOf("    ");
        if (pos > 0) {
            return item.substring(0, pos);
        }
        return item;
    }

}
