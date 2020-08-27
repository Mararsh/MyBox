package mara.mybox.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import mara.mybox.tools.CompressTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
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
public class FilesDecompressUnarchiveBatchController extends FilesBatchController {

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
        baseTitle = AppVariables.message("FilesDecompressUnarchiveBatch");
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
                    AppVariables.setUserConfigValue("FilesUnarchiveEncoding", encoding);
                }
            });
            encoding = AppVariables.getUserConfigValue("FilesUnarchiveEncoding", Charset.defaultCharset().name());
            encodeBox.getSelectionModel().select(encoding);

            deleteCheck.setSelected(
                    AppVariables.getUserConfigBoolean("FilesDecompressUnarchiveDelete", false));
            deleteCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue("FilesDecompressUnarchiveDelete", newValue);
                }
            });

        } catch (Exception e) {
            logger.debug(e.toString());
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
            logger.debug(e.toString());
            return false;
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            Date startTime = new Date();
            fileName = srcFile.getName();

            if (verboseCheck == null || verboseCheck.isSelected()) {
                updateLogs(MessageFormat.format(message("HandlingObject"), srcFile), true, true);
            }

            File decompressedFile = null, archiveSource;
            Map<String, Object> uncompress = CompressTools.decompress(srcFile, null);
            String archiveExt = FileTools.getFileSuffix(fileName);
            if (uncompress != null) {
                compressor = (String) uncompress.get("compressor");
                decompressedFile = (File) uncompress.get("decompressedFile");
                if (verboseCheck == null || verboseCheck.isSelected()) {
                    updateLogs(MessageFormat.format(message("FileDecompressedSuccessfully"),
                            srcFile, DateTools.datetimeMsDuration(new Date(), startTime), true, true
                    ));
                }
                archiveSource = decompressedFile;
                String suffix = "." + CompressTools.extensionByCompressor(compressor);
                if (fileName.toLowerCase().endsWith(suffix)) {
                    archiveExt = fileName.substring(0, fileName.length() - suffix.length());
                }
            } else {
                archiveSource = srcFile;
            }

            startTime = new Date();
            archiveFail = archiveSuccess = 0;
            unarchive(archiveSource, archiveExt);
            if (archiveSuccess > 0 || archiveFail > 0) {
                if (verboseCheck == null || verboseCheck.isSelected()) {
                    updateLogs(MessageFormat.format(message("FileUnarchived"),
                            srcFile, archiveSuccess, archiveFail,
                            DateTools.datetimeMsDuration(new Date(), startTime), true, true
                    ));
                }
                if (archiveFail > 0) {
                    return AppVariables.message("Failed");
                } else {
                    if (deleteCheck.isSelected()) {
                        srcFile.delete();
                    }
                    return AppVariables.message("Successful");
                }
            }

            if (decompressedFile == null) {
                return AppVariables.message("Failed");
            }

            targetFile = makeTargetFile(decompressedFile, targetPath);
            if (targetFile == null) {
                return AppVariables.message("Skip");
            }

            if (targetFile.exists()) {
                targetFile.delete();
            }
            if (!decompressedFile.renameTo(targetFile)) {
                Files.copy(Paths.get(decompressedFile.getAbsolutePath()),
                        Paths.get(targetFile.getAbsolutePath()));
                decompressedFile.delete();
            }
            targetFileGenerated(targetFile);
            if (deleteCheck.isSelected()) {
                srcFile.delete();
            }
            return AppVariables.message("Successful");
        } catch (Exception e) {
            logger.debug(e.toString());
            return AppVariables.message("Failed");
        }
    }

    protected void unarchive(File srcFile, String archiveExt) {
        try {
            try ( BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(srcFile))) {
                if (archiveExt != null && aFactory.getInputStreamArchiveNames().contains(archiveExt)) {
                    try ( ArchiveInputStream in = aFactory.createArchiveInputStream(archiveExt, fileIn, encoding)) {
                        if (archiveExt.equalsIgnoreCase(ArchiveStreamFactory.SEVEN_Z)) {
                            unarchive7z(srcFile);
                        } else if (archiver.equalsIgnoreCase(ArchiveStreamFactory.ZIP)) {
                            unarchiveZip(srcFile);
                        } else {
                            unarchive(in);
                        }
                    } catch (Exception e) {
                        unarchive(srcFile, fileIn);
                    }
                } else {
                    unarchive(srcFile, fileIn);
                }
            }

        } catch (Exception e) {
//            logger.debug(e.toString());
        }
    }

    public void unarchive(File srcFile, BufferedInputStream fileIn) {
        try {
            archiver = ArchiveStreamFactory.detect(fileIn);
            if (archiver == null) {
                return;
            }
            if (archiver.equalsIgnoreCase(ArchiveStreamFactory.SEVEN_Z)) {
                unarchive7z(srcFile);
            } else if (archiver.equalsIgnoreCase(ArchiveStreamFactory.ZIP)) {
                unarchiveZip(srcFile);
            } else {
                try ( ArchiveInputStream in
                        = aFactory.createArchiveInputStream(archiver, fileIn, encoding)) {
                    unarchive(in);
                } catch (Exception ex) {
//                            logger.debug(ex.toString());
                }
            }
        } catch (Exception e) {
//            logger.debug(e.toString());
        }
    }

    protected void unarchive(ArchiveInputStream archiveInputStream) {
        try {
            if (archiveInputStream == null) {
                return;
            }
            ArchiveEntry entry;
            while ((entry = archiveInputStream.getNextEntry()) != null) {
                if (verboseCheck == null || verboseCheck.isSelected()) {
                    updateLogs(message("Handling...") + ":   " + entry.getName());
                }
                if (!archiveInputStream.canReadEntryData(entry)) {
                    archiveFail++;
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(message("CanNotReadEntryData" + ":" + entry.getName()));
                    }
                    continue;
                }
                if (!entry.isDirectory()) {
                    File file = makeTargetFile(entry.getName(), targetPath);
                    if (file == null) {
                        continue;
                    }
                    File parent = file.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        archiveFail++;
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(message("FailOpenFile" + ":" + file));
                        }
                    }
                    try ( OutputStream o = Files.newOutputStream(file.toPath())) {
                        IOUtils.copy(archiveInputStream, o);
                    }
                    archiveSuccess++;
                    targetFileGenerated(file);
                }
            }
        } catch (Exception e) {
            archiveFail++;
            String s = e.toString();
            updateLogs(s);
            if (s.contains("java.nio.charset.MalformedInputException")
                    || s.contains("Illegal char")) {
                updateLogs(message("CharsetIncorrect"));
                charsetIncorrect = true;
            }
        }
    }

    protected void unarchiveZip(File srcFile) {
        try {
            try ( ZipFile zipFile = new ZipFile(srcFile)) {
                Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
                while (entries.hasMoreElements()) {
                    ZipArchiveEntry entry = entries.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(message("Handling...") + ":   " + entry.getName());
                    }
                    File file = makeTargetFile(entry.getName(), targetPath);
                    if (file == null) {
                        continue;
                    }
                    File parent = file.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        archiveFail++;
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(message("FailOpenFile" + ":" + file));
                        }
                        continue;
                    }
                    try ( FileOutputStream out = new FileOutputStream(file);
                             InputStream in = zipFile.getInputStream(entry)) {
                        IOUtils.copy(in, out);
                    }
                    archiveSuccess++;
                    targetFileGenerated(file);
                }
            }
        } catch (Exception e) {
            archiveFail++;
            String s = e.toString();
            updateLogs(s);
            if (s.contains("java.nio.charset.MalformedInputException")
                    || s.contains("Illegal char")) {
                updateLogs(message("CharsetIncorrect"));
                charsetIncorrect = true;
            }
        }
    }

    protected void unarchive7z(File srcFile) {
        try {
            try ( SevenZFile sevenZFile = new SevenZFile(srcFile)) {
                SevenZArchiveEntry entry;
                while ((entry = sevenZFile.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    if (verboseCheck == null || verboseCheck.isSelected()) {
                        updateLogs(message("Handling...") + ":   " + entry.getName());
                    }
                    File file = makeTargetFile(entry.getName(), targetPath);
                    if (file == null) {
                        continue;
                    }
                    File parent = file.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        archiveFail++;
                        if (verboseCheck == null || verboseCheck.isSelected()) {
                            updateLogs(message("FailOpenFile" + ":" + file));
                        }
                        continue;
                    }
                    try ( FileOutputStream out = new FileOutputStream(file)) {
                        int length;
                        byte[] buf = new byte[CommonValues.IOBufferLength];
                        while ((length = sevenZFile.read(buf)) != -1) {
                            out.write(buf, 0, length);
                        }
                    }
                    archiveSuccess++;
                    targetFileGenerated(file);
                }
            }
        } catch (Exception e) {
            archiveFail++;
            String s = e.toString();
            updateLogs(s);
            if (s.contains("java.nio.charset.MalformedInputException")
                    || s.contains("Illegal char")) {
                updateLogs(message("CharsetIncorrect"));
                charsetIncorrect = true;
            }
        }
    }

    @Override
    public void donePost() {
        super.donePost();
        if (charsetIncorrect) {
            alertError(message("CharsetIncorrect"));
            statusLabel.setText(message("CharsetIncorrect") + " " + statusLabel.getText());
        }

    }

}
