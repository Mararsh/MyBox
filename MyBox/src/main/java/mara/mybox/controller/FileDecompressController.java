package mara.mybox.controller;

import java.io.File;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.CompressTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-11-14
 * @License Apache License Version 2.0
 */
// http://commons.apache.org/proper/commons-compress/examples.html
public class FileDecompressController extends BaseController {

    protected String compressor, archiverChoice;

    @FXML
    protected Label sourceLabel;

    public FileDecompressController() {
        baseTitle = AppVariables.message("FileDecompress");
    }

    public void loadFile(File file, String compressor, String archiverChoice) {
        sourceFile = file;
        this.compressor = compressor;
        this.archiverChoice = archiverChoice;
        if (sourceFile == null || !sourceFile.exists() || compressor == null) {
            closeStage();
            return;
        }
        sourceLabel.setText(message("CompressionFormat") + ": " + compressor + "    "
                + sourceFile.getAbsolutePath() + "    "
                + FileTools.showFileSize(sourceFile.length()));
        startButton.disableProperty().unbind();
        startButton.setDisable(false);
    }

    @FXML
    @Override
    public void startAction() {
        if (targetPath == null || badStyle.equals(targetPathInput.getStyle())) {
            popError(message("InvalidTargetPath"));
            return;
        }
        if (sourceFile == null || !sourceFile.exists() || compressor == null) {
            closeStage();
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                File decompressFile;
                String archiver;

                @Override
                protected boolean handle() {
                    try {
                        Map<String, Object> decompressedResults = null;
                        String filename = sourceFile.getName();
                        String suffix = CompressTools.extensionByCompressor(compressor);
                        if (suffix != null && filename.toLowerCase().endsWith("." + suffix)) {
                            File dFile = makeTargetFile(filename.substring(0, filename.length() - suffix.length() - 1), targetPath);
                            if (dFile == null) {
                                return true;
                            }
                            decompressedResults = CompressTools.decompress(sourceFile, compressor, dFile);
                        }
                        if (decompressedResults == null) {
                            return false;
                        }
                        decompressFile = (File) decompressedResults.get("decompressedFile");
                        if (decompressFile == null || !decompressFile.exists()) {
                            return false;
                        }
                        if (archiverChoice == null || "auto".equals(archiverChoice)) {
                            archiver = CompressTools.detectArchiver(decompressFile);
                        } else {
                            archiver = CompressTools.detectArchiver(decompressFile, archiverChoice);
                        }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (decompressFile == null || !decompressFile.exists()) {
                        popFailed();
                        return;
                    }
                    if (archiver != null) {
                        FileUnarchiveController controller
                                = (FileUnarchiveController) openStage(CommonValues.FileUnarchiveFxml);
                        controller.loadFile(decompressFile, archiver);
                    } else {
                        File path = decompressFile.getParentFile();
                        browseURI(path.toURI());
                        recordFileOpened(path);
                    }
                    closeStage();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

}
