package mara.mybox.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.MyboxDataPath;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-21
 * @License Apache License Version 2.0
 */
public class OCRTools {

    // https://github.com/nguyenq/tess4j/blob/master/src/test/java/net/sourceforge/tess4j/Tesseract1Test.java#L177
    public static final double MINIMUM_DESKEW_THRESHOLD = 0.05d;
    public static final String TessDataPath = "TessDataPath";

    // Generate each time because user may change the interface language.
    public static Map<String, String> codeName() {
//        if (CodeName != null) {
//            return CodeName;
//        }
        Map<String, String> named = new HashMap<>();
        named.put("chi_sim", Languages.message("SimplifiedChinese"));
        named.put("chi_sim_vert", Languages.message("SimplifiedChineseVert"));
        named.put("chi_tra", Languages.message("TraditionalChinese"));
        named.put("chi_tra_vert", Languages.message("TraditionalChineseVert"));
        named.put("eng", Languages.message("English"));
        named.put("equ", Languages.message("MathEquation"));
        named.put("osd", Languages.message("OrientationScript"));
        return named;
    }

    public static Map<String, String> nameCode() {
//        if (NameCode != null) {
//            return NameCode;
//        }
        Map<String, String> codes = codeName();
        Map<String, String> names = new HashMap<>();
        for (String code : codes.keySet()) {
            names.put(codes.get(code), code);
        }
        return names;
    }

    public static List<String> codes() {
//        if (Codes != null) {
//            return Codes;
//        }
        List<String> Codes = new ArrayList<>();
        Codes.add("chi_sim");
        Codes.add("chi_sim_vert");
        Codes.add("chi_tra");
        Codes.add("chi_tra_vert");
        Codes.add("eng");
        Codes.add("equ");
        Codes.add("osd");
        return Codes;
    }

    public static List<String> names() {
//        if (Names != null) {
//            return Names;
//        }
        List<String> Names = new ArrayList<>();
        Map<String, String> codes = codeName();
        for (String code : codes()) {
            Names.add(codes.get(code));
        }
        return Names;
    }

    // Make sure supported language files are under defined data path
    public static boolean initDataFiles() {
        try {
            String pathname = UserConfig.getString(TessDataPath, null);
            if (pathname == null) {
                pathname = MyboxDataPath + File.separator + "tessdata";
            }
            File path = new File(pathname);
            if (!path.exists() || !path.isDirectory()) {
                path = new File(MyboxDataPath + File.separator + "tessdata");
                path.mkdirs();
            }
            File chi_sim = new File(path.getAbsolutePath() + File.separator + "chi_sim.traineddata");
            if (!chi_sim.exists()) {
                File tmp = mara.mybox.fxml.FxFileTools.getInternalFile("/data/tessdata/chi_sim.traineddata");
                FileCopyTools.copyFile(tmp, chi_sim);
            }
            File chi_sim_vert = new File(path.getAbsolutePath() + File.separator + "chi_sim_vert.traineddata");
            if (!chi_sim_vert.exists()) {
                File tmp = mara.mybox.fxml.FxFileTools.getInternalFile("/data/tessdata/chi_sim_vert.traineddata");
                FileCopyTools.copyFile(tmp, chi_sim_vert);
            }
            File chi_tra = new File(path.getAbsolutePath() + File.separator + "chi_tra.traineddata");
            if (!chi_tra.exists()) {
                File tmp = mara.mybox.fxml.FxFileTools.getInternalFile("/data/tessdata/chi_tra.traineddata");
                FileCopyTools.copyFile(tmp, chi_tra);
            }
            File chi_tra_vert = new File(path.getAbsolutePath() + File.separator + "chi_tra_vert.traineddata");
            if (!chi_tra_vert.exists()) {
                File tmp = mara.mybox.fxml.FxFileTools.getInternalFile("/data/tessdata/chi_tra_vert.traineddata");
                FileCopyTools.copyFile(tmp, chi_tra_vert);
            }
            File equ = new File(path.getAbsolutePath() + File.separator + "equ.traineddata");
            if (!equ.exists()) {
                File tmp = mara.mybox.fxml.FxFileTools.getInternalFile("/data/tessdata/equ.traineddata");
                FileCopyTools.copyFile(tmp, equ);
            }
            File eng = new File(path.getAbsolutePath() + File.separator + "eng.traineddata");
            if (!eng.exists()) {
                File tmp = mara.mybox.fxml.FxFileTools.getInternalFile("/data/tessdata/eng.traineddata");
                FileCopyTools.copyFile(tmp, eng);
            }
            File osd = new File(path.getAbsolutePath() + File.separator + "osd.traineddata");
            if (!osd.exists()) {
                File tmp = mara.mybox.fxml.FxFileTools.getInternalFile("/tessdata/osd.traineddata");
                FileCopyTools.copyFile(tmp, osd);
            }
            UserConfig.setString(TessDataPath, path.getAbsolutePath());
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public static List<String> namesList(boolean copyFiles) {
        List<String> data = new ArrayList<>();
        try {
            if (copyFiles) {
                initDataFiles();
            }
            String dataPath = UserConfig.getString(TessDataPath, null);
            if (dataPath == null) {
                return data;
            }
            data.addAll(names());
            List<String> codes = codes();
            File[] files = new File(dataPath).listFiles();
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    if (!f.isFile() || !name.endsWith(".traineddata")) {
                        continue;
                    }
                    String code = name.substring(0, name.length() - ".traineddata".length());
                    if (codes.contains(code)) {
                        continue;
                    }
                    data.add(code);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return data;
    }

    public static String name(String code) {
        Map<String, String> codes = codeName();
        return codes.get(code);
    }

    public static String code(String name) {
        Map<String, String> names = nameCode();
        return names.get(name);
    }

}
