package mara.mybox.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.CompressTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

/**
 * @Author Mara
 * @CreateDate 2019-11-2
 * @License Apache License Version 2.0
 */
// http://commons.apache.org/proper/commons-compress/examples.html
public class FilesDecompressUnarchiveBatchController extends BaseBatchFileController {

    protected CompressorStreamFactory cFactory;
    protected ArchiveStreamFactory aFactory;
    protected String fileName, archiver, compressor, encoding;
    protected int archiveSuccess, archiveFail;
    protected boolean charsetIncorrect;

    @FXML
    protected CheckBox deleteCheck;
    @FXML
    protected ComboBox<String> encodeBox;

    public FilesDecompressUnarchiveBatchController() {
        baseTitle = Languages.message("FilesDecompressUnarchiveBatch");
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
                    UserConfig.setUserConfigString("FilesUnarchiveEncoding", encoding);
                }
            });
            encoding = UserConfig.getUserConfigString("FilesUnarchiveEncoding", Charset.defaultCharset().name());
            encodeBox.getSelectionModel().select(encoding);

            deleteCheck.setSelected(UserConfig.getUserConfigBoolean("FilesDecompressUnarchiveDelete", false));
            deleteCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    UserConfig.setUserConfigBoolean("FilesDecompressUnarchiveDelete", newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean beforeHandleFiles() {
        try {
            cFactory = new CompressorStreamFactory();
            aFactory = new ArchiveStreamFactory();
            archiveSuccess = archiveFail = 0;
            charsetIncorrect = false;
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            Date startTime = new Date();
            if (verboseCheck == null || verboseCheck.isSelected()) {
                updateLogs(MessageFormat.format(Languages.message("HandlingObject"), srcFile), true, true);
            }
            File decompressedFile = null, archiveSource = srcFile;
            compressor = CompressTools.detectCompressor(srcFile);
            if (compressor != null) {
                decompressedFile = makeTargetFile(CompressTools.decompressedName(srcFile, compressor), targetPath);
                Map<String, Object> uncompressed = CompressTools.decompress(srcFile, compressor, decompressedFile);
                if (uncompressed != null) {
                    compressor = (String) uncompressed.get("compressor");
                    decompressedFile = (File) uncompressed.get("decompressedFile");
                    updateLogs(MessageFormat.format(Languages.message("FileDecompressedSuccessfully"),
                            srcFile, DateTools.datetimeMsDuration(new Date(), startTime), true, true
                    ));
                    archiveSource = decompressedFile;
                }
            }
            archiver = CompressTools.detectArchiver(archiveSource);
            if (archiver != null) {
                startTime = new Date();
                archiveFail = archiveSuccess = 0;
                unarchive(archiveSource, archiver);
                if (archiveSuccess > 0 || archiveFail > 0) {
                    updateLogs(MessageFormat.format(Languages.message("FileUnarchived"),
                            archiveSource, archiveSuccess, archiveFail,
                            DateTools.datetimeMsDuration(new Date(), startTime), true, true
                    ));
                    if (archiveFail > 0) {
                        if (ArchiveStreamFactory.SEVEN_Z.equals(archiver)) {
                            return archiveSource + " " + Languages.message("Failed") + ". " + Languages.message("7zNotFullSupported");
                        }
                        return Languages.message("Failed");
                    } else {
                        if (deleteCheck.isSelected()) {
                            FileDeleteTools.delete(srcFile);
                        }
                        return Languages.message("Successful");
                    }
                }
            }
            if (decompressedFile == null) {
                return Languages.message("Failed");
            }
            targetFileGenerated(decompressedFile);
            if (deleteCheck.isSelected()) {
                FileDeleteTools.delete(srcFile);
            }
            return Languages.message("Successful");
        } catch (Exception e) {
            updateLogs(e.toString());
            return Languages.message("Failed");
        }
    }

    protected void unarchive(File srcFile, String archiveExt) {
        if (archiveExt != null) {
            if (archiveExt.equalsIgnoreCase(ArchiveStreamFactory.SEVEN_Z)) {
                unarchive7z(srcFile);
                return;
            } else if (archiver.equalsIgnoreCase(ArchiveStreamFactory.ZIP)) {
                unarchiveZip(srcFile);
                return;
            }
        }
        try ( BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(srcFile))) {
            if (archiveExt != null && aFactory.getInputStreamArchiveNames().contains(archiveExt)) {
                try ( ArchiveInputStream in = aFactory.createArchiveInputStream(archiveExt, fileIn, encoding)) {
                    unarchive(in);
                } catch (Exception e) {
                    unarchive(fileIn);
                }
            } else {
                unarchive(fileIn);
            }
        } catch (Exception e) {
            updateLogs(e.toString());
            MyBoxLog.error(e);
        }
    }

    public void unarchive(BufferedInputStream fileIn) {
        try {
            archiver = ArchiveStreamFactory.detect(fileIn);
            if (archiver == null) {
                return;
            }
            try ( ArchiveInputStream in = aFactory.createArchiveInputStream(archiver, fileIn, encoding)) {
                unarchive(in);
            } catch (Exception ex) {
                updateLogs(ex.toString());
            }
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void unarchive(ArchiveInputStream archiveInputStream) {
        try {
            if (archiveInputStream == null) {
                return;
            }
            ArchiveEntry entry;
            File file;
            while ((entry = archiveInputStream.getNextEntry()) != null) {
                if (verboseCheck == null || verboseCheck.isSelected()) {
                    updateLogs(Languages.message("Handling...") + ":   " + entry.getName());
                }
                if (!archiveInputStream.canReadEntryData(entry)) {
                    archiveFail++;
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(Languages.message("CanNotReadEntryData" + ":" + entry.getName()));
                    }
                    continue;
                }
                try {
                    file = makeTargetFile(entry.getName(), targetPath);
                    file.getParentFile().mkdirs();
                } catch (Exception e) {
                    recordError(e.toString());
                    continue;
                }
                if (entry.isDirectory()) {
                    archiveSuccess++;
                    continue;
                }
                try ( OutputStream o = Files.newOutputStream(file.toPath())) {
                    IOUtils.copy(archiveInputStream, o);
                } catch (Exception e) {
                    recordError(e.toString());
                    continue;
                }
                archiveSuccess++;
                if (verboseCheck == null || verboseCheck.isSelected()) {
                    targetFileGenerated(file);
                }
            }
        } catch (Exception e) {
            recordError(e.toString());
        }
    }

    protected void unarchiveZip(File srcFile) {
        try ( ZipFile zipFile = new ZipFile(srcFile, encoding)) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            File file;
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                if (verboseCheck == null || verboseCheck.isSelected()) {
                    updateLogs(Languages.message("Handling...") + ":   " + entry.getName());
                }
                try {
                    file = makeTargetFile(entry.getName(), targetPath);
                    file.getParentFile().mkdirs();
                } catch (Exception e) {
                    recordError(e.toString());
                    continue;
                }
                if (entry.isDirectory()) {
                    archiveSuccess++;
                    continue;
                }
                try ( FileOutputStream out = new FileOutputStream(file);
                         InputStream in = zipFile.getInputStream(entry)) {
                    if (in != null) {
                        IOUtils.copy(in, out);
                    }
                } catch (Exception e) {
                    recordError(e.toString());
                    continue;
                }
                archiveSuccess++;
                if (verboseCheck == null || verboseCheck.isSelected()) {
                    targetFileGenerated(file);
                }
            }
        } catch (Exception e) {
            recordError(e.toString());
        }
    }

    protected void unarchive7z(File srcFile) {
        try ( SevenZFile sevenZFile = new SevenZFile(srcFile)) {
            SevenZArchiveEntry entry;
            File file;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (verboseCheck == null || verboseCheck.isSelected()) {
                    updateLogs(Languages.message("Handling...") + ":   " + entry.getName());
                }
                try {
                    file = makeTargetFile(entry.getName(), targetPath);
                    file.getParentFile().mkdirs();
                } catch (Exception e) {
                    recordError(e.toString());
                    continue;
                }
                if (entry.isDirectory()) {
                    archiveSuccess++;
                    continue;
                }
                try ( FileOutputStream out = new FileOutputStream(file)) {
                    byte[] content = new byte[(int) entry.getSize()];
                    sevenZFile.read(content, 0, content.length);
                    out.write(content);
                } catch (Exception e) {
                    recordError(e.toString());
                    continue;
                }
                archiveSuccess++;
                if (verboseCheck == null || verboseCheck.isSelected()) {
                    targetFileGenerated(file);
                }
            }
        } catch (Exception e) {
            recordError(e.toString());
        }
    }

    protected void recordError(String error) {
        archiveFail++;
        updateLogs(error);
        if (error.contains("java.nio.charset.MalformedInputException")
                || error.contains("Illegal char")) {
            charsetIncorrect = true;
        }
    }

    @Override
    public void donePost() {
        super.donePost();
        if (charsetIncorrect) {
            alertError(Languages.message("CharsetIncorrect"));
            statusLabel.setText(Languages.message("CharsetIncorrect") + " " + statusLabel.getText());
        }

    }

}
