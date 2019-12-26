package mara.mybox.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.Date;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

/**
 * @Author Mara
 * @CreateDate 2019-11-2
 * @License Apache License Version 2.0
 */
// http://commons.apache.org/proper/commons-compress/examples.html
public class FilesCompressBatchController extends FilesBatchController {

    protected String compressor, extension;
    protected SevenZMethod sevenCompress;

    @FXML
    protected ToggleGroup compressGroup, sevenCompressGroup;
    @FXML
    protected FlowPane sevenZCompressPane, commonCompressPane;
    @FXML
    protected RadioButton pack200Radio, gzRadio;
    @FXML
    protected Label commentsLabel;

    public FilesCompressBatchController() {
        baseTitle = AppVariables.message("FilesCompressBatch");
        viewTargetPath = true;
    }

    @Override
    public void initOptionsSection() {
        try {
            compressGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldv, Toggle newv) {
                    checkCompressor();
                }
            });
            checkCompressor();

            sevenCompressGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldv, Toggle newv) {
                    checkSevenCompress();
                }
            });
            checkSevenCompress();

        } catch (Exception e) {

        }

    }

    protected void checkSevenCompress() {
        String selected = ((RadioButton) sevenCompressGroup.getSelectedToggle()).getText();
        switch (selected) {
            case "LZMA2":
                sevenCompress = SevenZMethod.LZMA2;
                break;
            case "DEFLATE":
                sevenCompress = SevenZMethod.DEFLATE;
                break;
            case "BZIP2":
                sevenCompress = SevenZMethod.BZIP2;
                break;
        }
    }

    protected void checkCompressor() {
        compressor = ((RadioButton) compressGroup.getSelectedToggle()).getText();
        switch (compressor) {
            case "bzip2":
                extension = ".bz2";
                break;
            case "pack200":
                extension = ".pack";
                break;
            case "lz4-block":
            case "lz4-framed":
                extension = ".lz4";
                break;
            case "snappy-framed":
                extension = ".sz";
                break;
            default:
                extension = "." + compressor;
                break;
        }
        sevenZCompressPane.setVisible(ArchiveStreamFactory.SEVEN_Z.equals(compressor));
        if ("pack200".equals(compressor)) {
            commentsLabel.setText(message("Pack200Comments"));
        } else {
            commentsLabel.setText("");
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            showHandling(srcFile);
            targetFile = makeTargetFile(srcFile.getName(), extension, targetPath);
            if (targetFile == null) {
                return AppVariables.message("Skip");
            }
            long s = new Date().getTime();
            File tmpFile = FileTools.getTempFile();
            if (compressor.equalsIgnoreCase(ArchiveStreamFactory.SEVEN_Z)) {
                try ( SevenZOutputFile sevenZOutput = new SevenZOutputFile(tmpFile)) {
                    sevenZOutput.setContentCompression(sevenCompress);
                    SevenZArchiveEntry entry = sevenZOutput.createArchiveEntry(srcFile, srcFile.getName());
                    sevenZOutput.putArchiveEntry(entry);
                    try ( BufferedInputStream inputStream
                            = new BufferedInputStream(new FileInputStream(srcFile))) {
                        int len;
                        byte[] buf = new byte[CommonValues.IOBufferLength];
                        while ((len = inputStream.read(buf)) >= 0) {
                            sevenZOutput.write(buf, 0, len);
                        }
                    }
                    sevenZOutput.closeArchiveEntry();
                    sevenZOutput.finish();
                }

            } else if ("zip".equals(compressor) || "jar".equals(compressor)
                    || "7z".equals(compressor)) {
                ArchiveStreamFactory f = new ArchiveStreamFactory("UTF-8");
                try ( ArchiveOutputStream archiveOut = new ArchiveStreamFactory("UTF-8").
                        createArchiveOutputStream(compressor, new BufferedOutputStream(new FileOutputStream(tmpFile)))) {
                    ArchiveEntry entry = archiveOut.createArchiveEntry(srcFile, srcFile.getName());
                    archiveOut.putArchiveEntry(entry);
                    try ( BufferedInputStream inputStream
                            = new BufferedInputStream(new FileInputStream(srcFile))) {
                        IOUtils.copy(inputStream, archiveOut);
                    }
                    archiveOut.closeArchiveEntry();
                    archiveOut.finish();
                }

            } else {
                try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(srcFile));
                         BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmpFile));
                         CompressorOutputStream compressOut = new CompressorStreamFactory().
                                createCompressorOutputStream(compressor, out)) {
                    IOUtils.copy(inputStream, compressOut);
                }
            }
            if (targetFile.exists()) {
                targetFile.delete();
            }
            tmpFile.renameTo(targetFile);
            updateLogs(MessageFormat.format(message("FileCompressedSuccessfully"),
                    targetFile, FileTools.showFileSize(srcFile.length()),
                    FileTools.showFileSize(targetFile.length()),
                    (100 - targetFile.length() * 100 / srcFile.length()),
                    DateTools.showTime(new Date().getTime() - s)
            ));
            targetFileGenerated(targetFile);
            return AppVariables.message("Successful");
        } catch (Exception e) {
            logger.debug(e.toString());
            return AppVariables.message("Failed");
        }
    }

}
