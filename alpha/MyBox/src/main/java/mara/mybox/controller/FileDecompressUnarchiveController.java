package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.data.FileInformation.FileType;
import mara.mybox.data.FileNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.CompressTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;

/**
 * @Author Mara
 * @CreateDate 2019-11-14
 * @License Apache License Version 2.0
 */
// http://commons.apache.org/proper/commons-compress/examples.html
public class FileDecompressUnarchiveController extends FilesTreeController {

    protected String compressor, archiver, archiverChoice, compressorChoice, error;
    protected SevenZMethod sevenCompress;
    protected List<FileNode> entries;
    protected List<String> selected;
    protected long totalFiles, totalSize;
    protected FileUnarchive fileUnarchive;
    protected File decompressedFile, unarchiveFile;

    @FXML
    protected Tab sourceTab, selectionTab, targetTab;
    @FXML
    protected ToggleGroup archiverGroup, compressGroup, sevenCompressGroup;
    @FXML
    protected FlowPane sevenZCompressPane, commonCompressPane;
    @FXML
    protected ComboBox<String> charsetSelector;
    @FXML
    protected Label sourceLabel;
    @FXML
    protected HBox encodingHBox, selectionBar, sourceBar;
    @FXML
    protected VBox sourceVBox, selectionVBox, targetVBox;

    public FileDecompressUnarchiveController() {
        baseTitle = message("FileDecompressUnarchive");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initSourceTab();
            initSelectionTab();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initSourceTab() {
        try {
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
            MyBoxLog.error(e);
        }
    }

    public void initSelectionTab() {
        try {
            List<String> setNames = TextTools.getCharsetNames();
            charsetSelector.getItems().addAll(setNames);
            charsetSelector.getSelectionModel().select(UserConfig.getString("FilesUnarchiveEncoding", Charset.defaultCharset().name()));
            charsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldItem, String newItem) {
                    UserConfig.setString("FilesUnarchiveEncoding", newItem);
                    readEntries();
                }
            });

            startButton.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void checkCompressor() {
        compressorChoice = ((RadioButton) compressGroup.getSelectedToggle()).getText();
        if (compressorChoice.equals(message("DetectAutomatically"))) {
            compressorChoice = "auto";
        } else if (compressorChoice.equals(message("None"))) {
            compressorChoice = "none";
        }
    }

    protected void checkArchiver() {
        archiverChoice = ((RadioButton) archiverGroup.getSelectedToggle()).getText();
        sevenZCompressPane.setVisible(archiverChoice.equalsIgnoreCase(ArchiveStreamFactory.SEVEN_Z));
        if (archiverChoice.equals(message("DetectAutomatically"))) {
            archiverChoice = "auto";
        } else if (archiverChoice.equals(message("None"))) {
            archiverChoice = "none";
        }
    }

    protected void checkSevenCompress() {
        String sc = ((RadioButton) sevenCompressGroup.getSelectedToggle()).getText();
        switch (sc) {
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
    public void handleFile() {
        if (sourceFile == null || UserConfig.badStyle().equals(sourceFileInput.getStyle())) {
            popError(Languages.message("InvalidData"));
            return;
        }
        if (targetPath == null || !targetPathController.valid.get()) {
            popError(message("InvalidTargetPath"));
            tabPane.getSelectionModel().select(targetTab);
            return;
        }
        if (task != null) {
            task.cancel();
        }
        compressor = null;
        archiver = null;
        startButton.setDisable(true);
        FilesTreeController thisController = this;
        tabPane.getSelectionModel().select(logsTab);
        String info = message("Reading") + ": " + sourceFile.getAbsolutePath()
                + "   " + FileTools.showFileSize(sourceFile.length());
        updateLogs(info);
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    switch (compressorChoice) {
                        case "none":
                            compressor = null;
                            break;
                        case "auto":
                            compressor = CompressTools.detectCompressor(thisController, sourceFile);
                            break;
                        default:
                            compressor = CompressTools.detectCompressor(thisController, sourceFile, compressorChoice);
                    }
                    if (compressor == null) {
                        switch (archiverChoice) {
                            case "none":
                                archiver = null;
                                break;
                            case "auto":
                                archiver = CompressTools.detectArchiver(thisController, sourceFile);
                                break;
                            default:
                                archiver = CompressTools.detectArchiver(thisController, sourceFile, archiverChoice);
                        }
                    }
                    return true;
                } catch (Exception e) {
                    updateLogs(e.toString());
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                try {
                    if (archiver != null) {
                        unarchiveFile = sourceFile;
                        readEntries();

                    } else if (compressor != null) {
                        decompress();

                    } else {
                        popError(message("InvalidFormatTryOther"));
                    }
                } catch (Exception e) {
                    updateLogs(e.toString());
                }
            }

        };
        start(task, info);
    }

    public void decompress() {
        if (task != null) {
            task.cancel();
        }
        startButton.setDisable(true);
        filesTreeView.setRoot(null);
        sourceLabel.setText("");
        decompressedFile = null;
        FilesTreeController thisController = this;
        tabPane.getSelectionModel().select(logsTab);
        String info = message("CompressionFormat") + ": " + compressor + "    "
                + sourceFile.getAbsolutePath() + "    "
                + FileTools.showFileSize(sourceFile.length());
        updateLogs(info);
        task = new SingletonCurrentTask<Void>(this) {
            File decompressedFile;
            String archiver;

            @Override
            protected boolean handle() {
                try {
                    decompressedFile = makeTargetFile(
                            new File(targetPath + File.separator
                                    + CompressTools.decompressedName(thisController, sourceFile, compressor)),
                            targetPath);
                    if (decompressedFile == null) {
                        return false;
                    }
                    Map<String, Object> decompressedResults = CompressTools.decompress(thisController,
                            sourceFile, compressor, decompressedFile);
                    if (decompressedResults == null) {
                        return false;
                    }
                    decompressedFile = (File) decompressedResults.get("decompressedFile");
                    if (!decompressedFile.exists()) {
                        return false;
                    }

                    if (archiverChoice == null || "auto".equals(archiverChoice)) {
                        archiver = CompressTools.detectArchiver(thisController, decompressedFile);
                    } else {
                        archiver = CompressTools.detectArchiver(thisController, decompressedFile, archiverChoice);
                    }
                    return true;
                } catch (Exception e) {
                    updateLogs(e.toString());
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                updateLogs(MessageFormat.format(message("FileDecompressedSuccessfully"),
                        decompressedFile, DateTools.datetimeMsDuration(new Date(), startTime), true, true
                ));
                targetFileGenerated(decompressedFile);
                recordFileWritten(decompressedFile);
                if (archiver != null) {
                    unarchiveFile = decompressedFile;
                    readEntries();
                } else {
                    File path = decompressedFile.getParentFile();
                    browseURI(path.toURI());
                    recordFileOpened(path);
                }
            }
        };
        start(task, info);

    }

    public void readEntries() {
        if (task != null) {
            task.cancel();
        }
        entries = null;
        totalFiles = 0;
        totalSize = 0;
        startButton.setDisable(true);
        filesTreeView.setRoot(null);
        sourceLabel.setText("");
        if (unarchiveFile == null) {
            return;
        }
        FilesTreeController thisController = this;
        tabPane.getSelectionModel().select(logsTab);
        updateLogs(message("Reading") + ": " + unarchiveFile.getAbsolutePath());
        task = new SingletonCurrentTask<Void>(this) {

            private TreeItem<FileNode> root;

            @Override
            protected boolean handle() {
                try {
                    Map<String, Object> archive = CompressTools.readEntries(thisController,
                            unarchiveFile, archiver, charsetSelector.getValue());
                    if (archive == null) {
                        return true;
                    }
                    if (archive.containsKey("entries")) {
                        archiver = (String) archive.get("archiver");
                        entries = (List<FileNode>) archive.get("entries");
                        root = makeFilesTree();
                    }
                    if (archive.containsKey("error")) {
                        error = (String) archive.get("error");
                    }
                    return true;
                } catch (Exception e) {
                    updateLogs(e.toString());
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (entries == null || entries.isEmpty()) {
                    startButton.setDisable(true);
                    String info;
                    if (error != null) {
                        info = error;
                    } else {
                        info = Languages.message("InvalidFormatTryOther");
                    }
                    // https://commons.apache.org/proper/commons-compress/examples.html
                    if (ArchiveStreamFactory.SEVEN_Z.equals(archiver)) {
                        info += "\n" + Languages.message("7zNotFullSupported");
                    }
                    alertError(info);
                    updateLogs(info);
                } else {
                    tabPane.getSelectionModel().select(selectionTab);
                    encodingHBox.setVisible(!ArchiveStreamFactory.SEVEN_Z.equals(archiver));
                    filesTreeView.setRoot(root);
                    startButton.setDisable(false);
                    String info = message("ArchiverFormat") + ": " + archiver + "    "
                            + unarchiveFile.getAbsolutePath() + "    "
                            + MessageFormat.format(message("FilesValues"), totalFiles, FileTools.showFileSize(totalSize));
                    updateLogs(info);
                    sourceLabel.setText(info);
                }
            }

        };
        start(task);
    }

    protected TreeItem<FileNode> makeFilesTree() {
        try {
            totalFiles = 0;
            totalSize = 0;
            if (entries == null) {
                return null;
            }
            isSettingValues = true;
            FileNode rootInfo = new FileNode();
            rootInfo.setData("");
            TreeItem<FileNode> rootItem = new TreeItem(rootInfo);
            rootItem.setExpanded(true);
            addSelectedListener(rootItem);

            TreeItem<FileNode> parent;
            for (FileNode entry : entries) {
                String[] nodes = entry.getData().split("/");
                parent = rootItem;
                TreeItem<FileNode> nodeItem = null;
                for (String node : nodes) {
                    String parentName = parent.getValue().getData();
                    String name = parentName.isEmpty() ? node : parentName + "/" + node;
                    nodeItem = getChild(parent, name);
                    parent = nodeItem;
                }
                if (nodeItem == null) {
                    continue;
                }
                FileNode nodeInfo = nodeItem.getValue();
                nodeInfo.setFileType(entry.getFileType());
                if (entry.getFileType() == FileType.File) {
                    totalFiles++;
                    nodeInfo.setData(entry.getData());
                    nodeInfo.setModifyTime(entry.getModifyTime());
                    long size = entry.getFileSize();
                    if (size < 0) {
                        size = 0;
                    }
                    totalSize += size;
                    nodeInfo.setFileSize(size);
                }

            }
            isSettingValues = false;
            return rootItem;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            isSettingValues = false;
            return null;
        }
    }

    public void checkSelection(TreeItem<FileNode> item) {
        try {
            if (item == null || item.getValue() == null) {
                return;
            }
            FileNode info = item.getValue();
            if (info.isSelected() && info.getFileType() == FileType.File) {
                selected.add(info.getData());
            }
            List<TreeItem<FileNode>> children = item.getChildren();
            if (children == null || children.isEmpty()) {
                return;
            }
            for (TreeItem<FileNode> child : children) {
                checkSelection(child);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        if (unarchiveFile == null) {
            popError(message("InvalidData"));
            tabPane.getSelectionModel().select(sourceTab);
            return false;
        }
        if (targetPath == null || !targetPathController.valid.get()) {
            popError(message("InvalidTargetPath"));
            tabPane.getSelectionModel().select(targetTab);
            return false;
        }
        selected = new ArrayList();
        checkSelection(filesTreeView.getRoot());
        if (selected.isEmpty()) {
            popError(message("SelectToHandle"));
            tabPane.getSelectionModel().select(selectionTab);
            return false;
        }
        return true;
    }

    @Override
    public void beforeTask() {
        super.beforeTask();
        sourceVBox.setDisable(true);
        selectionVBox.setDisable(true);
        targetVBox.setDisable(true);
    }

    @Override
    public boolean doTask() {
        try {
            fileUnarchive = new FileUnarchive()
                    .setTaskController(this)
                    .setSourceFile(unarchiveFile)
                    .setTargetPath(targetPath)
                    .setCharset(charsetSelector.getValue())
                    .setArchiver(archiver)
                    .setSelected(selected);
            return fileUnarchive.start();
        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }
    }

    @Override
    public void afterTask() {
        sourceVBox.setDisable(false);
        selectionVBox.setDisable(false);
        targetVBox.setDisable(false);
        if (fileUnarchive.getArchiveSuccess() > 0) {
            openTarget();
        }
        if (fileUnarchive.getArchiveFail() > 0) {
            if (fileUnarchive.isCharsetIncorrect()) {
                alertError(message("CharsetIncorrect"));
            }
        }
        showLogs(MessageFormat.format(message("FileUnarchived"),
                message("Selected") + ":" + selected.size(),
                fileUnarchive.getArchiveSuccess(), fileUnarchive.getArchiveFail()));
        super.afterTask();
    }

    /*
        static
     */
    public static FileDecompressUnarchiveController open() {
        try {
            FileDecompressUnarchiveController controller
                    = (FileDecompressUnarchiveController) WindowTools.openStage(Fxmls.FileDecompressUnarchiveFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static FileDecompressUnarchiveController open(File file) {
        FileDecompressUnarchiveController controller = open();
        if (controller != null) {
            controller.sourceFileChanged(file);
        }
        return controller;
    }

}
