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
import java.util.ArrayList;
import java.util.Enumeration;
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
import javafx.util.Callback;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.TreeTableEraCell;
import mara.mybox.fxml.cell.TreeTableFileSizeCell;
import mara.mybox.tools.CompressTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
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
    protected ComboBox<String> charsetSelector;
    @FXML
    protected Label sourceLabel;
    @FXML
    protected HBox encodingHBox;

    public FileUnarchiveController() {
        baseTitle = Languages.message("FileUnarchive");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initUnarchiveBox();

            startButton.disableProperty().unbind();
//            startButton.disableProperty().bind(
//                    Bindings.isEmpty(targetPathInput.textProperty())
//                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
//            );
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
            sourceLabel.setText(Languages.message("ArchiverFormat") + ": " + archiver + "    "
                    + sourceFile.getAbsolutePath() + "    "
                    + FileTools.showFileSize(sourceFile.length()));

            readEntries();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void readEntries() {
        filesTreeView.setRoot(null);
        entries = null;
        totalFiles = 0;
        totalSize = 0;
        startButton.setDisable(true);
        filesTreeView.setRoot(null);
        if (sourceFile == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private TreeItem<FileInformation> root;

                @Override
                protected boolean handle() {
                    try {
                        Map<String, Object> archive
                                = CompressTools.readEntries(sourceFile, archiver, charsetSelector.getValue());
                        if (archive == null) {
                            return true;
                        }
                        if (archive.containsKey("entries")) {
                            archiver = (String) archive.get("archiver");
                            entries = (List<FileInformation>) archive.get("entries");
                            root = makeFilesTree();
                        }
                        if (archive.containsKey("error")) {
                            error = (String) archive.get("error");
                        }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    encodingHBox.setVisible(!ArchiveStreamFactory.SEVEN_Z.equals(archiver));
                    if (entries == null || entries.isEmpty()) {
                        bottomLabel.setText("");
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
                        bottomLabel.setText(info);
                    } else {
                        filesTreeView.setRoot(root);
                        startButton.setDisable(false);
                        sourceLabel.setText(Languages.message("ArchiverFormat") + ": " + archiver + "    "
                                + sourceFile.getAbsolutePath() + "    "
                                + FileTools.showFileSize(sourceFile.length()));
                        bottomLabel.setText(MessageFormat.format(Languages.message("FilesValues"),
                                totalFiles, FileTools.showFileSize(totalSize)));
                    }
                }

            };
            start(task);
        }

    }

    protected TreeItem<FileInformation> makeFilesTree() {
        try {
            totalFiles = 0;
            totalSize = 0;
            if (entries == null) {
                return null;
            }
            isSettingValues = true;
            FileInformation rootInfo = new FileInformation();
            rootInfo.setFileName("");
            TreeItem<FileInformation> rootItem = new TreeItem(rootInfo);
            rootItem.setExpanded(true);
            rootInfo.getSelectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldItem, Boolean newItem) {
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
                    nodeInfo.setFileSuffix(FileNameTools.suffix(entry.getFileName()));
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
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        if (targetPath == null || !targetPathController.valid.get()) {
            popError(Languages.message("InvalidTargetPath"));
            return;
        }
        selected = new ArrayList();
        checkSelection(filesTreeView.getRoot());
        if (selected.isEmpty()) {
            popError(Languages.message("SelectToHandle"));
            return;
        }
        charsetIncorrect = false;
        error = null;
        archiveFail = 0;
        archiveSuccess = 0;
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try {
                        // archiver should have been determinied at this moment
                        if (archiver.equalsIgnoreCase(ArchiveStreamFactory.SEVEN_Z)) {
                            unarchive7z();
                        } else if (archiver.equalsIgnoreCase(ArchiveStreamFactory.ZIP)) {
                            unarchiveZip();
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
                    bottomLabel.setText(MessageFormat.format(Languages.message("FileUnarchived"),
                            Languages.message("Selected") + ":" + selected.size(),
                            archiveSuccess, archiveFail, DateTools.datetimeMsDuration(cost)));
                    if (archiveSuccess > 0) {
                        browseURI(targetPath.toURI());
                        recordFileOpened(targetPath);
                    }
                    if (archiveFail > 0) {
                        if (charsetIncorrect) {
                            alertError(Languages.message("CharsetIncorrect"));
                        } else if (error != null) {
                            alertError(error);
                        }
                    }
                }
            };
            start(task);
        }

    }

    protected void unarchive() {
        try {
            ArchiveStreamFactory aFactory = new ArchiveStreamFactory();
            if (archiver == null || !aFactory.getInputStreamArchiveNames().contains(archiver)) {
                return;
            }
            try ( BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(sourceFile));
                     ArchiveInputStream in = aFactory.createArchiveInputStream(archiver, fileIn, charsetSelector.getValue())) {
                ArchiveEntry entry;
                File file;
                while ((entry = in.getNextEntry()) != null) {
                    if (!in.canReadEntryData(entry)) {
                        archiveFail++;
                        MyBoxLog.debug(Languages.message("CanNotReadEntryData" + ":" + entry.getName()));
                        continue;
                    }
                    if (!selected.contains(entry.getName())) {
                        continue;
                    }
                    try {
                        file = new File(targetPath + File.separator + entry.getName());
                        file = makeTargetFile(file, file.getParentFile());
                    } catch (Exception e) {
                        recordError(e.toString());
                        continue;
                    }
                    if (entry.isDirectory()) {
                        archiveSuccess++;
                        continue;
                    }
                    try ( OutputStream o = Files.newOutputStream(file.toPath())) {
                        IOUtils.copy(in, o);
                    } catch (Exception e) {
                        recordError(e.toString());
                        continue;
                    }
                    archiveSuccess++;
                }
            }
        } catch (Exception e) {
            recordError(e.toString());
        }
    }

    protected void unarchive7z() {
        try ( SevenZFile sevenZFile = new SevenZFile(sourceFile)) {
            SevenZArchiveEntry entry;
            File file;
            while ((entry = sevenZFile.getNextEntry()) != null) {
                if (!selected.contains(entry.getName())) {
                    continue;
                }
                try {
                    file = new File(targetPath + File.separator + entry.getName());
                    file = makeTargetFile(file, file.getParentFile());
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
            }
        } catch (Exception e) {
            recordError(e.toString());
        }
    }

    protected void unarchiveZip() {
        try ( ZipFile zipFile = new ZipFile(sourceFile, charsetSelector.getValue())) {
            Enumeration<ZipArchiveEntry> zEntries = zipFile.getEntries();
            File file;
            while (zEntries.hasMoreElements()) {
                ZipArchiveEntry entry = zEntries.nextElement();
                try {
                    file = new File(targetPath + File.separator + entry.getName());
                    file = makeTargetFile(file, file.getParentFile());
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
            }
        } catch (Exception e) {
            recordError(e.toString());
        }
    }

    protected void recordError(String error) {
        archiveFail++;
        this.error = error;
        if (error.contains("java.nio.charset.MalformedInputException")
                || error.contains("Illegal char")) {
            charsetIncorrect = true;
        }
    }

}
