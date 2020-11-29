package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.CompressTools;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
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
        baseTitle = AppVariables.message("FileDecompressUnarchive");

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
        if (compressorChoice.equals(message("DetectAutomatically"))) {
            compressorChoice = "auto";
        } else if (compressorChoice.equals(message("None"))) {
            compressorChoice = "none";
        }
        readFile();
    }

    protected void checkArchiver() {
        archiverChoice = ((RadioButton) archiverGroup.getSelectedToggle()).getText();
        sevenZCompressPane.setVisible(archiverChoice.equalsIgnoreCase(ArchiveStreamFactory.SEVEN_Z));
        if (archiverChoice.equals(message("DetectAutomatically"))) {
            archiverChoice = "auto";
        } else if (archiverChoice.equals(message("None"))) {
            archiverChoice = "none";
        }
        readFile();
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

    @Override
    public void sourceFileChanged(final File file) {
        try {
            sourceFile = file;
            readFile();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void readFile() {
        compressor = null;
        archiver = null;
        if (sourceFile == null || badStyle.equals(sourceFileInput.getStyle())) {
            popError(message("InvalidData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit() ) {
                return;
            }
            task = new SingletonTask<Void>() {

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
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    try {
                        if (archiver != null) {
                            FileUnarchiveController controller
                                    = (FileUnarchiveController) openStage(CommonValues.FileUnarchiveFxml);
                            controller.loadFile(sourceFile, archiver);

                        } else if (compressor != null) {
                            FileDecompressController controller
                                    = (FileDecompressController) openStage(CommonValues.FileDecompressFxml);
                            controller.loadFile(sourceFile, compressor, archiverChoice);

                        } else {
                            popError(AppVariables.message("InvalidFormatTryOther"));
                        }

                    } catch (Exception e) {
                        error = e.toString();
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

}
