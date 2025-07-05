package mara.mybox.data;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.tools.AlphaTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.AppVariables.MyboxDataPath;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;

/**
 * @Author Mara
 * @CreateDate 2025-6-30
 * @License Apache License Version 2.0
 */
public class TesseractOptions {

    // https://github.com/nguyenq/tess4j/blob/master/src/test/java/net/sourceforge/tess4j/Tesseract1Test.java#L177
    public static final double MINIMUM_DESKEW_THRESHOLD = 0.05d;

    protected File tesseract, dataPath;
    protected String os, selectedLanguages, regionLevel, wordLevel, more,
            texts, html;
    protected List<Rectangle> rectangles;
    protected List<Word> words;
    protected int psm, tesseractVersion;
    protected boolean embed, setFormats, setLevels, isVersion3, outHtml, outPdf;
    protected File configFile;
    protected Tesseract tessInstance;

    public TesseractOptions() {
        initValues();
    }

    final public void initValues() {
        os = SystemTools.os();
        readValues();
        configFile = null;
        tessInstance = null;
        clearResults();
    }

    public void readValues() {
        try (Connection conn = DerbyBase.getConnection()) {
            tesseract = new File(UserConfig.getString(conn, "TesseractPath",
                    "win".equals(os) ? "D:\\Programs\\Tesseract-OCR\\tesseract.exe" : "/bin/tesseract"));
            dataPath = new File(UserConfig.getString(conn, "TesseractData",
                    "win".equals(os) ? "D:\\Programs\\Tesseract-OCR\\tessdata" : "/usr/local/share/tessdata/"));

            embed = UserConfig.getBoolean(conn, "TesseracEmbed", true);
            selectedLanguages = UserConfig.getString(conn, "ImageOCRLanguages", null);
            psm = UserConfig.getInt(conn, "TesseractPSM", 6);
            regionLevel = UserConfig.getString(conn, "TesseractRegionLevel", message("Symbol"));
            wordLevel = UserConfig.getString(conn, "TesseractWordLevel", message("Symbol"));
            outHtml = UserConfig.getBoolean(conn, "TesseractOutHtml", false);
            outPdf = UserConfig.getBoolean(conn, "TesseractOutPdf", false);
            more = UserConfig.getString(conn, "TesseractMore", null);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void writeValues() {
        try (Connection conn = DerbyBase.getConnection()) {
            UserConfig.setString(conn, "TesseractPath", tesseract.getAbsolutePath());
            UserConfig.setString(conn, "TesseractData", dataPath.getAbsolutePath());
            UserConfig.setBoolean(conn, "ImageOCREmbed", embed);
            UserConfig.setString(conn, "ImageOCRLanguages", selectedLanguages);
            UserConfig.setInt(conn, "TesseractPSM", psm);
            UserConfig.setString(conn, "TesseractRegionLevel", regionLevel);
            UserConfig.setString(conn, "TesseractWordLevel", wordLevel);
            UserConfig.setBoolean(conn, "TesseractOutHtml", outHtml);
            UserConfig.setBoolean(conn, "TesseractOutPdf", outPdf);
            UserConfig.setString(conn, "TesseractMore", more);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void clearResults() {
        texts = null;
        html = null;
        rectangles = null;
        words = null;
    }

    public boolean isWin() {
        return "win".equals(os);
    }

    public int level(String name) {
        if (name == null || name.isBlank()) {
            return -1;
        } else if (Languages.matchIgnoreCase("Block", name)) {
            return ITessAPI.TessPageIteratorLevel.RIL_BLOCK;

        } else if (Languages.matchIgnoreCase("Paragraph", name)) {
            return ITessAPI.TessPageIteratorLevel.RIL_PARA;

        } else if (Languages.matchIgnoreCase("Textline", name)) {
            return ITessAPI.TessPageIteratorLevel.RIL_TEXTLINE;

        } else if (Languages.matchIgnoreCase("Word", name)) {
            return ITessAPI.TessPageIteratorLevel.RIL_WORD;

        } else if (Languages.matchIgnoreCase("Symbol", name)) {
            return ITessAPI.TessPageIteratorLevel.RIL_SYMBOL;

        } else {
            return -1;
        }
    }

    public int wordLevel() {
        return level(wordLevel);
    }

    public int regionLevel() {
        return level(regionLevel);
    }

    public Map<String, String> moreOptions() {
        if (more == null || more.isBlank()) {
            return null;
        }
        Map<String, String> p = new HashMap<>();
        String[] lines = more.split("\n");
        for (String line : lines) {
            String[] fields = line.split("\t");
            if (fields.length < 2) {
                continue;
            }
            p.put(fields[0].trim(), fields[1].trim());
        }
        return p;
    }

    // Make sure supported language files are under defined data path
    public boolean initDataFiles() {
        try {
            if (dataPath == null || !dataPath.exists() || !dataPath.isDirectory()) {
                dataPath = new File(MyboxDataPath + File.separator + "tessdata");
            }
            dataPath.mkdirs();
            File chi_sim = new File(dataPath.getAbsolutePath() + File.separator + "chi_sim.traineddata");
            if (!chi_sim.exists()) {
                File tmp = mara.mybox.fxml.FxFileTools.getInternalFile("/data/tessdata/chi_sim.traineddata");
                FileCopyTools.copyFile(tmp, chi_sim);
            }
            File chi_sim_vert = new File(dataPath.getAbsolutePath() + File.separator + "chi_sim_vert.traineddata");
            if (!chi_sim_vert.exists()) {
                File tmp = mara.mybox.fxml.FxFileTools.getInternalFile("/data/tessdata/chi_sim_vert.traineddata");
                FileCopyTools.copyFile(tmp, chi_sim_vert);
            }
            File chi_tra = new File(dataPath.getAbsolutePath() + File.separator + "chi_tra.traineddata");
            if (!chi_tra.exists()) {
                File tmp = mara.mybox.fxml.FxFileTools.getInternalFile("/data/tessdata/chi_tra.traineddata");
                FileCopyTools.copyFile(tmp, chi_tra);
            }
            File chi_tra_vert = new File(dataPath.getAbsolutePath() + File.separator + "chi_tra_vert.traineddata");
            if (!chi_tra_vert.exists()) {
                File tmp = mara.mybox.fxml.FxFileTools.getInternalFile("/data/tessdata/chi_tra_vert.traineddata");
                FileCopyTools.copyFile(tmp, chi_tra_vert);
            }
            File equ = new File(dataPath.getAbsolutePath() + File.separator + "equ.traineddata");
            if (!equ.exists()) {
                File tmp = mara.mybox.fxml.FxFileTools.getInternalFile("/data/tessdata/equ.traineddata");
                FileCopyTools.copyFile(tmp, equ);
            }
            File eng = new File(dataPath.getAbsolutePath() + File.separator + "eng.traineddata");
            if (!eng.exists()) {
                File tmp = mara.mybox.fxml.FxFileTools.getInternalFile("/data/tessdata/eng.traineddata");
                FileCopyTools.copyFile(tmp, eng);
            }
            File osd = new File(dataPath.getAbsolutePath() + File.separator + "osd.traineddata");
            if (!osd.exists()) {
                File tmp = mara.mybox.fxml.FxFileTools.getInternalFile("/tessdata/osd.traineddata");
                FileCopyTools.copyFile(tmp, osd);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public List<String> namesList() {
        return namesList(tesseractVersion > 3);
    }

    public List<String> namesList(boolean copyFiles) {
        List<String> data = new ArrayList<>();
        try {
            if (copyFiles) {
                initDataFiles();
            }
            data.addAll(names());
            List<String> codes = codes();
            File[] files = dataPath.listFiles();
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

    public Tesseract makeInstance() {
        try {
            tessInstance = new Tesseract();
            // https://stackoverflow.com/questions/58286373/tess4j-pdf-to-tiff-to-tesseract-warning-invalid-resolution-0-dpi-using-70/58296472#58296472
            tessInstance.setVariable("user_defined_dpi", "96");
            tessInstance.setVariable("debug_file", "/dev/null");
            tessInstance.setPageSegMode(psm);

            Map<String, String> moreOptions = moreOptions();
            if (moreOptions != null && !moreOptions.isEmpty()) {
                for (String key : moreOptions.keySet()) {
                    tessInstance.setVariable(key, moreOptions.get(key));
                }
            }
            tessInstance.setDatapath(dataPath.getAbsolutePath());
            if (selectedLanguages != null) {
                tessInstance.setLanguage(selectedLanguages);
            }
            return tessInstance;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    public int tesseractVersion() {
        try {
            if (tesseract == null || !tesseract.exists()) {
                return -1;
            }
            List<String> parameters = new ArrayList<>();
            parameters.addAll(Arrays.asList(tesseract.getAbsolutePath(), "--version"));
            ProcessBuilder pb = new ProcessBuilder(parameters).redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader inReader = process.inputReader(Charset.defaultCharset())) {
                String line;
                while ((line = inReader.readLine()) != null) {
                    if (line.contains("tesseract v4.") || line.contains("tesseract 4.")) {
                        return 4;
                    }
                    if (line.contains("tesseract v5.") || line.contains("tesseract 5.")) {
                        return 5;
                    }
                    if (line.contains("tesseract v3.") || line.contains("tesseract 3.")) {
                        return 3;
                    }
                    if (line.contains("tesseract v2.") || line.contains("tesseract 2.")) {
                        return 2;
                    }
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
            }
            process.waitFor();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return -1;
    }

    public void makeConfigFile() {
        try {
            configFile = FileTmpTools.getTempFile();
            String s = "tessedit_create_txt 1\n";
            if (setFormats) {
                if (outHtml) {
                    s += "tessedit_create_hocr 1\n";
                }
                if (outPdf) {
                    s += "tessedit_create_pdf 1\n";
                }
            }
            Map<String, String> moreOptions = moreOptions();
            if (moreOptions != null) {
                for (String key : moreOptions.keySet()) {
                    s += key + "\t" + moreOptions.get(key) + "\n";
                }
            }
            TextFileTools.writeFile(configFile, s, Charset.forName("utf-8"));
        } catch (Exception e) {
            MyBoxLog.error(e);
            configFile = null;
        }
    }

    public boolean imageOCR(FxTask currentTask, Image image, boolean allData) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        bufferedImage = AlphaTools.removeAlpha(currentTask, bufferedImage);
        return bufferedImageOCR(currentTask, bufferedImage, allData);
    }

    public boolean bufferedImageOCR(FxTask currentTask, BufferedImage bufferedImage, boolean allData) {
        try {
            clearResults();
            if (bufferedImage == null || (currentTask != null && !currentTask.isWorking())) {
                return false;
            }
            if (tessInstance == null) {
                tessInstance = makeInstance();
            }
            List<ITesseract.RenderedFormat> formats = new ArrayList<>();
            formats.add(ITesseract.RenderedFormat.TEXT);
            if (allData) {
                formats.add(ITesseract.RenderedFormat.HOCR);
            }

            File tmpFile = File.createTempFile("MyboxOCR", "");
            String tmp = tmpFile.getAbsolutePath();
            FileDeleteTools.delete(tmpFile);

            tessInstance.createDocumentsWithResults​(bufferedImage, tmp,
                    tmp, formats, ITessAPI.TessPageIteratorLevel.RIL_SYMBOL);
            File txtFile = new File(tmp + ".txt");
            texts = TextFileTools.readTexts(currentTask, txtFile);
            FileDeleteTools.delete(txtFile);
            if (texts == null || (currentTask != null && !currentTask.isWorking())) {
                return false;
            }
            if (allData) {
                File htmlFile = new File(tmp + ".hocr");
                html = TextFileTools.readTexts(currentTask, htmlFile);
                FileDeleteTools.delete(htmlFile);
                if (html == null || (currentTask != null && !currentTask.isWorking())) {
                    return false;
                }

                int wl = wordLevel();
                if (wl >= 0) {
                    words = tessInstance.getWords(bufferedImage, wl);
                }
                int rl = regionLevel();
                if (rl >= 0) {
                    rectangles = tessInstance.getSegmentedRegions(bufferedImage, rl);
                }
            }

            return texts != null;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public Process process(File file, String prefix) {
        try {
            if (file == null || configFile == null || dataPath == null) {
                return null;
            }
            List<String> parameters = new ArrayList<>();
            parameters.addAll(Arrays.asList(
                    tesseract.getAbsolutePath(),
                    file.getAbsolutePath(),
                    prefix,
                    "--tessdata-dir", dataPath.getAbsolutePath(),
                    tesseractVersion > 3 ? "--psm" : "-psm", psm + ""
            ));
            if (selectedLanguages != null) {
                parameters.addAll(Arrays.asList("-l", selectedLanguages));
            }
            parameters.add(configFile.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(parameters).redirectErrorStream(true);
            return pb.start();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        static
     */
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

    public static String name(String code) {
        Map<String, String> codes = codeName();
        return codes.get(code);
    }

    public static String code(String name) {
        Map<String, String> names = nameCode();
        return names.get(name);
    }


    /*
        get/set
     */
    public boolean isEmbed() {
        return embed;
    }

    public void setEmbed(boolean embed) {
        this.embed = embed;
    }

    public File getTesseract() {
        return tesseract;
    }

    public void setTesseract(File tesseract) {
        this.tesseract = tesseract;
    }

    public File getDataPath() {
        return dataPath;
    }

    public void setDataPath(File dataPath) {
        this.dataPath = dataPath;
    }

    public String getSelectedLanguages() {
        return selectedLanguages;
    }

    public void setSelectedLanguages(String selectedLanguages) {
        this.selectedLanguages = selectedLanguages;
    }

    public String getTexts() {
        return texts;
    }

    public TesseractOptions setTexts(String texts) {
        this.texts = texts;
        return this;
    }

    public String getHtml() {
        return html;
    }

    public TesseractOptions setHtml(String html) {
        this.html = html;
        return this;
    }

    public List<Rectangle> getRectangles() {
        return rectangles;
    }

    public TesseractOptions setRectangles(List<Rectangle> rectangles) {
        this.rectangles = rectangles;
        return this;
    }

    public List<Word> getWords() {
        return words;
    }

    public TesseractOptions setWords(List<Word> words) {
        this.words = words;
        return this;
    }

    public int getPsm() {
        return psm;
    }

    public TesseractOptions setPsm(int psm) {
        this.psm = psm;
        return this;
    }

    public String getRegionLevel() {
        return regionLevel;
    }

    public TesseractOptions setRegionLevel(String regionLevel) {
        this.regionLevel = regionLevel;
        return this;
    }

    public String getWordLevel() {
        return wordLevel;
    }

    public TesseractOptions setWordLevel(String wordLevel) {
        this.wordLevel = wordLevel;
        return this;
    }

    public int getTesseractVersion() {
        return tesseractVersion;
    }

    public TesseractOptions setTesseractVersion(int tesseractVersion) {
        this.tesseractVersion = tesseractVersion;
        return this;
    }

    public boolean isSetFormats() {
        return setFormats;
    }

    public TesseractOptions setSetFormats(boolean setFormats) {
        this.setFormats = setFormats;
        return this;
    }

    public boolean isSetLevels() {
        return setLevels;
    }

    public TesseractOptions setSetLevels(boolean setLevels) {
        this.setLevels = setLevels;
        return this;
    }

    public boolean isIsVersion3() {
        return isVersion3;
    }

    public TesseractOptions setIsVersion3(boolean isVersion3) {
        this.isVersion3 = isVersion3;
        return this;
    }

    public File getConfigFile() {
        return configFile;
    }

    public TesseractOptions setConfigFile(File configFile) {
        this.configFile = configFile;
        return this;
    }

    public String getOs() {
        return os;
    }

    public TesseractOptions setOs(String os) {
        this.os = os;
        return this;
    }

    public boolean isOutHtml() {
        return outHtml;
    }

    public TesseractOptions setOutHtml(boolean outHtml) {
        this.outHtml = outHtml;
        return this;
    }

    public boolean isOutPdf() {
        return outPdf;
    }

    public TesseractOptions setOutPdf(boolean outPdf) {
        this.outPdf = outPdf;
        return this;
    }

    public String getMore() {
        return more;
    }

    public TesseractOptions setMore(String more) {
        this.more = more;
        return this;
    }

    public Tesseract getTessInstance() {
        return tessInstance;
    }

    public void setTessInstance(Tesseract tessInstance) {
        this.tessInstance = tessInstance;
    }

}
