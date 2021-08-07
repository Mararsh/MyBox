package mara.mybox.fxml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javafx.stage.FileChooser;
import mara.mybox.controller.BaseController_Files;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TmpFileTools;
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
            mara.mybox.tools.FileTools.rename(tmpFile, file);
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
        File file = TmpFileTools.getTempFile();
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

}
