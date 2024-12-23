package mara.mybox.controller;

import mara.mybox.data.FileUnarchive;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.CompressTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

/**
 * @Author Mara
 * @CreateDate 2019-11-2
 * @License Apache License Version 2.0
 */
// http://commons.apache.org/proper/commons-compress/examples.html
public class FilesDecompressUnarchiveBatchController extends BaseBatchFileController {

    protected ArchiveStreamFactory aFactory;
    protected String fileName, archiver, compressor, encoding;
    protected FileUnarchive fileUnarchive;

    @FXML
    protected CheckBox deleteCheck;
    @FXML
    protected ComboBox<String> encodeBox;

    public FilesDecompressUnarchiveBatchController() {
        baseTitle = message("FilesDecompressUnarchiveBatch");
        viewTargetPath = true;
    }

    @Override
    public void initOptionsSection() {
        try {
            List<String> setNames = TextTools.getCharsetNames();
            encodeBox.getItems().addAll(setNames);
            encodeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldItem, String newItem) {
                    encoding = newItem;
                    UserConfig.setString("FilesUnarchiveEncoding", encoding);
                }
            });
            encoding = UserConfig.getString("FilesUnarchiveEncoding", Charset.defaultCharset().name());
            encodeBox.getSelectionModel().select(encoding);

            deleteCheck.setSelected(UserConfig.getBoolean("FilesDecompressUnarchiveDelete", false));
            deleteCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("FilesDecompressUnarchiveDelete", newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean beforeHandleFiles(FxTask currentTask) {
        try {
            aFactory = new ArchiveStreamFactory();
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            Date fStartTime = new Date();
            if (verboseCheck == null || verboseCheck.isSelected()) {
                updateLogs(MessageFormat.format(message("HandlingObject"), srcFile), true, true);
            }
            File decompressedFile = null, unarchiveFile = srcFile;
            compressor = CompressTools.detectCompressor(this, srcFile);
            if (compressor != null) {
                decompressedFile = makeTargetFile(
                        new File(targetPath + File.separator + CompressTools.decompressedName(this, srcFile, compressor)),
                        targetPath);
                Map<String, Object> decompressedResults = CompressTools.decompress(this, srcFile, compressor, decompressedFile);
                if (decompressedResults != null) {
                    decompressedFile = (File) decompressedResults.get("decompressedFile");
                    if (decompressedFile.exists()) {
                        compressor = (String) decompressedResults.get("compressor");
                        updateLogs(MessageFormat.format(message("FileDecompressedSuccessfully"),
                                srcFile, DateTools.datetimeMsDuration(new Date(), fStartTime), true, true
                        ));
                        unarchiveFile = decompressedFile;
                    } else {
                        decompressedFile = null;
                    }
                } else {
                    decompressedFile = null;
                }
            }
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            archiver = CompressTools.detectArchiver(this, unarchiveFile);
            if (archiver != null && unarchiveFile != null) {
                fileUnarchive = new FileUnarchive()
                        .setTaskController(this)
                        .setSourceFile(unarchiveFile)
                        .setTargetPath(targetPath)
                        .setCharset(encoding)
                        .setArchiver(archiver)
                        .setSelected(null)
                        .setaFactory(aFactory)
                        .setVerbose(verboseCheck != null && verboseCheck.isSelected());
                fileUnarchive.start();
                if (fileUnarchive.getArchiveSuccess() > 0 || fileUnarchive.getArchiveFail() > 0) {
                    updateLogs(MessageFormat.format(message("FileUnarchived"),
                            unarchiveFile, fileUnarchive.getArchiveSuccess(), fileUnarchive.getArchiveFail()));
                    if (fileUnarchive.getArchiveFail() > 0) {
                        if (ArchiveStreamFactory.SEVEN_Z.equals(archiver)) {
                            return unarchiveFile + " " + message("Failed") + ". " + message("7zNotFullSupported");
                        } else {
                            return message("Failed");
                        }
                    } else {
                        if (decompressedFile != null) {
                            FileDeleteTools.delete(decompressedFile);
                        } else if (deleteCheck.isSelected()) {
                            FileDeleteTools.delete(srcFile);
                        }
                        return message("Successful");
                    }
                }
            }
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            if (decompressedFile == null) {
                return message("Failed");
            }
            targetFileGenerated(decompressedFile);
            if (deleteCheck.isSelected()) {
                FileDeleteTools.delete(srcFile);
            }
            return message("Successful");
        } catch (Exception e) {
            updateLogs(e.toString());
            return message("Failed");
        }
    }

}
