package mara.mybox.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.data.FileInformation;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.TreeTableFileSizeCell;
import mara.mybox.fxml.TreeTableEraCell;
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
import org.apache.commons.compress.utils.IOUtils;

/**
 * @Author Mara
 * @CreateDate 2019-11-14
 * @License Apache License Version 2.0
 */
// http://commons.apache.org/proper/commons-compress/examples.html
public class FileUnarchiveController extends FilesTreeController {

    protected String archiver, error;
    protected List<FileInformation> entries;
    protected List<String> selected;
    protected int archiveSuccess, archiveFail;
    protected boolean charsetIncorrect;
    long totalFiles, totalSize;

    @FXML
    protected ComboBox<String> encodeSelector;
    @FXML
    protected Label sourceLabel;
    @FXML
    protected HBox encodingHBox;

    public FileUnarchiveController() {
        baseTitle = AppVariables.message("FileUnarchive");
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            initUnarchiveBox();

            startButton.disableProperty().unbind();
//            startButton.disableProperty().bind(
//                    Bindings.isEmpty(targetPathInput.textProperty())
//                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
//            );
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void initUnarchiveBox() {
        try {

            fileColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("fileName"));
            fileColumn.setPrefWidth(400);

            selectedColumn.setCellValueFactory(
                    new Callback<TreeTableColumn.CellDataFeatures<FileInformation, Boolean>, ObservableValue<Boolean>>() {
                @Override
                public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<FileInformation, Boolean> param) {
                    if (param.getValue() != null) {
                        return param.getValue().getValue().getSelectedProperty();
                    }
                    return null;
                }
            });
            selectedColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(selectedColumn));

            typeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("fileSuffix"));

            sizeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("fileSize"));
            sizeColumn.setCellFactory(new TreeTableFileSizeCell());

            modifyTimeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("modifyTime"));
            modifyTimeColumn.setCellFactory(new TreeTableEraCell());

            createTimeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("createTime"));
            createTimeColumn.setCellFactory(new TreeTableEraCell());

            filesTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            List<String> setNames = TextTools.getCharsetNames();
            encodeSelector.getItems().addAll(setNames);
            encodeSelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue("FilesUnarchiveEncoding", Charset.defaultCharset().name()));
            encodeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldItem, String newItem) {
                    AppVariables.setUserConfigValue("FilesUnarchiveEncoding", newItem);
                    readEntries();
                }
            });

        } catch (Exception e) {

        }

    }

    public void loadFile(File file, String archiver) {
        try {
            sourceFile = file;
            this.archiver = archiver;
            totalFiles = totalSize = 0;
            if (sourceFile == null || !sourceFile.exists()) {
                closeStage();
                return;
            }
            sourceLabel.setText(message("ArchiverFormat") + ": " + archiver + "    "
                    + sourceFile.getAbsolutePath() + "    "
                    + FileTools.showFileSize(sourceFile.length()));

            readEntries();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void readEntries() {
        filesTreeView.setRoot(null);
        entries = null;
        startButton.setDisable(true);
        filesTreeView.setRoot(null);
        if (sourceFile == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private TreeItem<FileInformation> root;

                @Override
                protected boolean handle() {
                    try {
                        Map<String, Object> archive
                                = CompressTools.readEntries(sourceFile, archiver, encodeSelector.getValue());
                        if (archive == null) {
                            return true;
                        }
                        archiver = (String) archive.get("archiver");
                        entries = (List<FileInformation>) archive.get("entries");
                        root = makeFilesTree();
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    try {
                        if (entries.isEmpty()) {
                            bottomLabel.setText("");
                            startButton.setDisable(true);
                            popError(AppVariables.message("InvalidFormatTryOther"));
                        } else {
                            filesTreeView.setRoot(root);
                            startButton.setDisable(false);
                            startButton.setText(AppVariables.message("Extract"));
                            sourceLabel.setText(message("ArchiverFormat") + ": " + archiver + "    "
                                    + sourceFile.getAbsolutePath() + "    "
                                    + FileTools.showFileSize(sourceFile.length()));
                            encodingHBox.setVisible(!ArchiveStreamFactory.SEVEN_Z.equals(archiver));
                            bottomLabel.setText(MessageFormat.format(message("FilesValues"),
                                    totalFiles, FileTools.showFileSize(totalSize)));
                        }

                    } catch (Exception e) {
                        error = e.toString();
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    protected TreeItem<FileInformation> makeFilesTree() {
        try {
            isSettingValues = true;
            FileInformation rootInfo = new FileInformation();
            rootInfo.setFileName("");
            TreeItem<FileInformation> rootItem = new TreeItem(rootInfo);
            rootItem.setExpanded(true);
            rootInfo.getSelectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean oldItem, Boolean newItem) {
                    if (!isSettingValues) {
                        treeItemSelected(rootItem, newItem);
                    }
                }
            });

            TreeItem<FileInformation> parent;

            for (FileInformation entry : entries) {
                String[] nodes = entry.getFileName().split("/");
                parent = rootItem;
                TreeItem<FileInformation> nodeItem = null;
                for (String node : nodes) {
                    String parentName = parent.getValue().getFileName();
                    String name = parentName.isEmpty() ? node : parentName + "/" + node;
                    nodeItem = getChild(parent, name);
                    parent = nodeItem;
                }
                if (nodeItem == null) {
                    continue;
                }
                FileInformation nodeInfo = nodeItem.getValue();
                nodeInfo.setFileType(entry.getFileType());
                if ("file".equals(entry.getFileType())) {
                    totalFiles++;
                    nodeInfo.setFileName(entry.getFileName());
                    nodeInfo.setFileSuffix(FileTools.getFileSuffix(entry.getFileName()));
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
            logger.debug(e.toString());
            return null;
        }
    }

    public void checkSelection(TreeItem<FileInformation> item) {
        try {
            if (item == null || item.getValue() == null) {
                return;
            }
            FileInformation info = item.getValue();
            if (info.isSelected() && "file".equals(info.getFileType())) {
                selected.add(info.getFileName());
            }
            List<TreeItem<FileInformation>> children = item.getChildren();
            if (children == null || children.isEmpty()) {
                return;
            }
            for (TreeItem<FileInformation> child : children) {
                checkSelection(child);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        if (targetPath == null || badStyle.equals(targetPathInput.getStyle())) {
            popError(message("InvalidTargetPath"));
            return;
        }
        selected = new ArrayList();
        checkSelection(filesTreeView.getRoot());
        if (selected.isEmpty()) {
            popError(message("NoSelection"));
            return;
        }
        charsetIncorrect = false;
        error = null;
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        // archiver should have been determinied at this moment
                        if (archiver.equalsIgnoreCase(ArchiveStreamFactory.SEVEN_Z)) {
                            unarchive7z();
                        } else {
                            unarchive();
                        }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    bottomLabel.setText(MessageFormat.format(message("FileUnarchived"),
                            sourceFile, archiveSuccess, archiveFail, DateTools.datetimeMsDuration(cost)));
                    if (archiveSuccess > 0) {
                        browseURI(targetPath.toURI());
                        recordFileOpened(targetPath);
                    }
                    if (archiveFail > 0) {
                        if (charsetIncorrect) {
                            alertError(message("CharsetIncorrect"));
                        } else if (error != null) {
                            alertError(error);
                        }
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    protected void unarchive() {
        try {
            if (selected == null || selected.isEmpty()) {
                return;
            }
            ArchiveStreamFactory aFactory = new ArchiveStreamFactory();
            if (archiver == null || !aFactory.getInputStreamArchiveNames().contains(archiver)) {
                return;
            }
            try ( BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(sourceFile));
                     ArchiveInputStream in = aFactory.createArchiveInputStream(archiver, fileIn, encodeSelector.getValue())) {
                ArchiveEntry entry;
                while ((entry = in.getNextEntry()) != null) {
                    if (!in.canReadEntryData(entry)) {
                        archiveFail++;
//                        logger.debug(message("CanNotReadEntryData" + ":" + entry.getName()));
                        continue;
                    }
                    if (entry.isDirectory() || !selected.contains(entry.getName())) {
                        continue;
                    }
                    File file = makeTargetFile(entry.getName(), targetPath);
                    if (file == null) {
                        continue;
                    }
                    File parent = file.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        archiveFail++;
//                        logger.debug(message("FailOpenFile" + ":" + file));
                        continue;
                    }
                    try ( OutputStream o = Files.newOutputStream(file.toPath())) {
                        IOUtils.copy(in, o);
                    }
                    archiveSuccess++;
                }
            }
        } catch (Exception e) {
            archiveFail++;
            error = e.toString();
            if (error.contains("java.nio.charset.MalformedInputException")
                    || error.contains("Illegal char")) {
                charsetIncorrect = true;
            }
        }
    }

    protected void unarchive7z() {
        try {
            if (selected == null || selected.isEmpty()) {
                return;
            }
            try ( SevenZFile sevenZFile = new SevenZFile(sourceFile)) {
                SevenZArchiveEntry entry;
                while ((entry = sevenZFile.getNextEntry()) != null) {
                    if (entry.isDirectory() || !selected.contains(entry.getName())) {
                        continue;
                    }
                    File file = makeTargetFile(entry.getName(), targetPath);
                    if (file == null) {
                        continue;
                    }
                    File parent = file.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        archiveFail++;
//                        logger.debug(message("FailOpenFile" + ":" + file));
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
                }
            }
        } catch (Exception e) {
            archiveFail++;
            archiveFail++;
            error = e.toString();
            if (error.contains("java.nio.charset.MalformedInputException")
                    || error.contains("Illegal char")) {
                charsetIncorrect = true;
            }
        }
    }

}
