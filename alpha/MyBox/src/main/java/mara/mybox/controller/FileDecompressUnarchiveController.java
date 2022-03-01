package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.CompressTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;

/**
 * @Author Mara
 * @CreateDate 2019-11-14
 * @License Apache License Version 2.0
 */
// http://commons.apache.org/proper/commons-compress/examples.html
public class FileDecompressUnarchiveController extends BaseController {

    protected String compressor, archiver, archiverChoice, compressorChoice;
    protected SevenZMethod sevenCompress;

    @FXML
    protected ToggleGroup archiverGroup, compressGroup, sevenCompressGroup;
    @FXML
    protected FlowPane sevenZCompressPane, commonCompressPane;

    public FileDecompressUnarchiveController() {
        baseTitle = Languages.message("FileDecompressUnarchive");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            archiverGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldv, Toggle newv) {
                    checkArchiver();
                }
            });
            checkArchiver();

            sevenCompressGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldv, Toggle newv) {
                    checkSevenCompress();
                }
            });
            checkSevenCompress();

            compressGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldv, Toggle newv) {
                    checkCompressor();
                }
            });
            checkCompressor();

        } catch (Exception e) {

        }

    }

    protected void checkCompressor() {
        compressorChoice = ((RadioButton) compressGroup.getSelectedToggle()).getText();
        if (compressorChoice.equals(Languages.message("DetectAutomatically"))) {
            compressorChoice = "auto";
        } else if (compressorChoice.equals(Languages.message("None"))) {
            compressorChoice = "none";
        }
    }

    protected void checkArchiver() {
        archiverChoice = ((RadioButton) archiverGroup.getSelectedToggle()).getText();
        sevenZCompressPane.setVisible(archiverChoice.equalsIgnoreCase(ArchiveStreamFactory.SEVEN_Z));
        if (archiverChoice.equals(Languages.message("DetectAutomatically"))) {
            archiverChoice = "auto";
        } else if (archiverChoice.equals(Languages.message("None"))) {
            archiverChoice = "none";
        }
    }

    protected void checkSevenCompress() {
        String selected = ((RadioButton) sevenCompressGroup.getSelectedToggle()).getText();
        switch (selected) {
            case "LZMA2":
                sevenCompress = SevenZMethod.LZMA2;
                break;
            case "COPY":
                sevenCompress = SevenZMethod.COPY;
                break;
            case "DEFLATE":
                sevenCompress = SevenZMethod.DEFLATE;
                break;
            case "BZIP2":
                sevenCompress = SevenZMethod.BZIP2;
                break;
        }
    }

    @FXML
    @Override
    public void startAction() {
        compressor = null;
        archiver = null;
        if (sourceFile == null || UserConfig.badStyle().equals(sourceFileInput.getStyle())) {
            popError(Languages.message("InvalidData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try {
                        switch (compressorChoice) {
                            case "none":
                                compressor = null;
                                break;
                            case "auto":
                                compressor = CompressTools.detectCompressor(sourceFile);
                                break;
                            default:
                                compressor = CompressTools.detectCompressor(sourceFile, compressorChoice);
                        }
                        if (compressor == null) {
                            switch (archiverChoice) {
                                case "none":
                                    archiver = null;
                                    break;
                                case "auto":
                                    archiver = CompressTools.detectArchiver(sourceFile);
                                    break;
                                default:
                                    archiver = CompressTools.detectArchiver(sourceFile, archiverChoice);
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        MyBoxLog.debug(e, sourceFile.getAbsolutePath());
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    try {
                        if (archiver != null) {
                            FileUnarchiveController controller = (FileUnarchiveController) WindowTools.openChildStage(
                                    myController.getMyWindow(), Fxmls.FileUnarchiveFxml, true);
                            controller.loadFile(sourceFile, archiver);

                        } else if (compressor != null) {
                            FileDecompressController controller
                                    = (FileDecompressController) openStage(Fxmls.FileDecompressFxml);
                            controller.loadFile(sourceFile, compressor, archiverChoice);

                        } else {
                            popError(Languages.message("InvalidFormatTryOther"));
                        }
                    } catch (Exception e) {
                        MyBoxLog.debug(e, sourceFile.getAbsolutePath());
                        popError(e.toString());
                    }
                }

            };
            start(task);
        }

    }

}
