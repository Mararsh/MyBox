package mara.mybox.fxml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javafx.stage.FileChooser;
import mara.mybox.controller.BaseController_Files;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class FxFileTools {

    public static File getInternalFile(String resourceFile, String subPath, String userFile) {
        return getInternalFile(resourceFile, subPath, userFile, false);
    }

    // Solution from https://stackoverflow.com/questions/941754/how-to-get-a-path-to-a-resource-in-a-java-jar-file
    public static File getInternalFile(String resourceFile, String subPath, String userFile, boolean deleteExisted) {
        if (resourceFile == null || userFile == null) {
            return null;
        }
        try {
            File path = new File(AppVariables.MyboxDataPath + File.separator + subPath + File.separator);
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(AppVariables.MyboxDataPath + File.separator + subPath + File.separator + userFile);
            if (file.exists() && !deleteExisted) {
                return file;
            }
            File tmpFile = getInternalFile(resourceFile);
            if (tmpFile == null) {
                return null;
            }
            if (file.exists()) {
                file.delete();
            }
            mara.mybox.tools.FileTools.override(tmpFile, file);
            return file;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static File getInternalFile(String resourceFile) {
        if (resourceFile == null) {
            return null;
        }
        File file = FileTmpTools.getTempFile();
        try (final InputStream input = NodeTools.class.getResourceAsStream(resourceFile);
                final OutputStream out = new FileOutputStream(file)) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = input.read(bytes)) > 0) {
                out.write(bytes, 0, read);
            }
            file.deleteOnExit();
            return file;
        } catch (Exception e) {
            //            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static File selectFile(BaseController_Files controller) {
        return selectFile(controller,
                UserConfig.getPath(controller.getBaseName() + "SourcePath"),
                controller.getSourceExtensionFilter());
    }

    public static File selectFile(BaseController_Files controller, int fileType) {
        return selectFile(controller,
                UserConfig.getPath(VisitHistoryTools.getPathKey(fileType)),
                VisitHistoryTools.getExtensionFilter(fileType));
    }

    public static File selectFile(BaseController_Files controller, File path, List<FileChooser.ExtensionFilter> filter) {
        try {
            FileChooser fileChooser = new FileChooser();
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(filter);
            File file = fileChooser.showOpenDialog(controller.getMyStage());
            if (file == null || !file.exists()) {
                return null;
            }
            controller.recordFileOpened(file);
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<String> getResourceFiles(String path) {
        List<String> files = new ArrayList<>();
        try {
            if (!path.endsWith("/")) {
                path += "/";
            }
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL dirURL = classLoader.getResource(path);
            if (dirURL == null) {
                File filePath = new File(path);
                if (!filePath.exists()) {
                    MyBoxLog.error(path);
                    return files;
                }
                File[] list = filePath.listFiles();
                if (list != null) {
                    for (File file : list) {
                        if (file.isFile()) {
                            files.add(file.getName());
                        }
                    }
                }
                return files;
            }
            if (dirURL.getProtocol().equals("jar")) {
                String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
                try (final JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (entry.isDirectory() || !name.startsWith(path)) {
                            continue;
                        }
                        name = name.substring(path.length());
                        if (!name.contains("/")) {
                            files.add(name);
                        }
                    }
                }
            } else if (dirURL.getProtocol().equals("file")) {
                File[] list = new File(dirURL.getPath()).listFiles();
                if (list != null) {
                    for (File file : list) {
                        if (file.isFile()) {
                            files.add(file.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
        return files;
    }

}
