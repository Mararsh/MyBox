package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeItem;
import mara.mybox.data.FileInformation.FileType;
import mara.mybox.data.FileNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-11-13
 * @License Apache License Version 2.0
 */
public class FilesRedundancyResultsController extends FilesTreeController {

    protected Map<String, List<FileNode>> redundancy;

    @FXML
    protected RadioButton deleteRadio, trashRadio;

    public FilesRedundancyResultsController() {
        baseTitle = message("HandleFilesRedundancy");
    }

    public void checkSelection() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private int filesSelected = 0, filesTotal = 0, filesRundancy = 0;
                private long sizeSelected = 0, sizeTotal = 0, sizeRedundant = 0, fileSize = 0;

                @Override
                protected boolean handle() {
                    try {
                        TreeItem rootItem = filesTreeView.getRoot();
                        List<TreeItem> digests = new ArrayList();
                        digests.addAll(rootItem.getChildren());
                        for (TreeItem digest : digests) {
                            List<TreeItem<FileNode>> files = new ArrayList();
                            files.addAll(digest.getChildren());
                            filesTotal += files.size();
                            filesRundancy += files.size() - 1;
                            for (TreeItem<FileNode> item : files) {
                                FileNode info = item.getValue();
                                fileSize = info.getFileSize();
                                if (fileSize > 0) {
                                    sizeTotal += fileSize;
                                }
                                if (info.isSelected()) {
                                    filesSelected++;
                                    if (fileSize > 0) {
                                        sizeSelected += fileSize;
                                    }
                                }
                            }
                            sizeRedundant += (files.size() - 1) * fileSize;
                        }

                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    bottomLabel.setText(MessageFormat.format(message("RedundancyCheckValues"),
                            filesTotal, FileTools.showFileSize(sizeTotal),
                            filesRundancy, FileTools.showFileSize(sizeRedundant),
                            filesSelected, FileTools.showFileSize(sizeSelected)));
                    deleteButton.setDisable(filesSelected == 0);
                }
            };
            start(task);
        }
    }

    // https://stackoverflow.com/questions/29989892/checkboxtreetablecell-select-all-children-under-parent-event
    public void loadRedundancy(Map<String, List<FileNode>> data) {
        filesTreeView.setRoot(null);
        if (data == null || data.isEmpty()) {
            popInformation(message("NoRedundancy"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private TreeItem<FileNode> rootItem;

                @Override
                protected boolean handle() {
                    try {
                        redundancy = data;
                        FileNode rootInfo = new FileNode();
                        rootInfo.setData(message("HandleFilesRedundancy"));
                        rootInfo.setFileType(FileType.Root);
                        rootItem = new TreeItem(rootInfo);
                        addSelectedListener(rootItem);
                        rootItem.setExpanded(true);

                        for (String digest : redundancy.keySet()) {
                            FileNode digestInfo = new FileNode();
                            digestInfo.setData(digest);
                            digestInfo.setFileType(FileType.Digest);
                            TreeItem<FileNode> digestItem = new TreeItem(digestInfo);
                            digestItem.setExpanded(true);

                            List<FileNode> files = redundancy.get(digest);
                            digestInfo.setFileSize(files.get(0).getFileSize());
                            addSelectedListener(digestItem);

                            for (FileNode file : files) {
                                File f = file.getFile();
                                if (f == null || !f.exists() || !f.isFile()) {
                                    continue;
                                }
                                digestItem.getChildren().add(new TreeItem(file));
                                file.getSelected().addListener(new ChangeListener<Boolean>() {
                                    @Override
                                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                                        if (!isSettingValues) {
                                            checkSelection();
                                        }
                                    }
                                });
                            }

                            if (digestItem.getChildren().size() > 1) {
                                rootItem.getChildren().add(digestItem);
                            }
                        }

                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.debug(error);
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    try {
                        filesTreeView.setRoot(rootItem);
                        checkSelection();
                    } catch (Exception e) {
                        error = e.toString();
                    }
                }

            };
            start(task);
        }

    }

    @FXML
    public void exceptFirstAction() {
        isSettingValues = true;
        TreeItem<FileNode> rootItem = filesTreeView.getRoot();
        List<TreeItem<FileNode>> digests = rootItem.getChildren();
        if (digests == null || digests.isEmpty()) {
            filesTreeView.setRoot(null);
            return;
        }
        rootItem.getValue().setSelected(false);
        for (TreeItem<FileNode> digest : digests) {
            digest.getValue().setSelected(false);
            List<TreeItem<FileNode>> files = digest.getChildren();
            if (files == null || files.isEmpty()) {
                continue;
            }
            files.get(0).getValue().setSelected(false);
            for (int i = 1; i < files.size(); ++i) {
                files.get(i).getValue().setSelected(true);
            }
        }
        filesTreeView.refresh();
        isSettingValues = false;
        checkSelection();

    }

    @FXML
    public void exceptLastAction() {
        isSettingValues = true;
        TreeItem<FileNode> rootItem = filesTreeView.getRoot();
        List<TreeItem<FileNode>> digests = rootItem.getChildren();
        if (digests == null || digests.isEmpty()) {
            filesTreeView.setRoot(null);
            return;
        }
        rootItem.getValue().setSelected(false);
        for (TreeItem<FileNode> digest : digests) {
            digest.getValue().setSelected(false);
            List<TreeItem<FileNode>> files = digest.getChildren();
            if (files == null || files.isEmpty()) {
                continue;
            }
            for (int i = 0; i < files.size() - 1; ++i) {
                files.get(i).getValue().setSelected(true);
            }
            files.get(files.size() - 1).getValue().setSelected(false);
        }
        filesTreeView.refresh();
        isSettingValues = false;
        checkSelection();

    }

    @Override
    public void deleteAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {
                private int deleted;

                @Override
                protected boolean handle() {
                    try {
                        deleted = 0;
                        TreeItem rootItem = filesTreeView.getRoot();
                        List<TreeItem> digests = new ArrayList();
                        digests.addAll(rootItem.getChildren());
                        for (TreeItem digest : digests) {
                            List<TreeItem<FileNode>> files = new ArrayList();
                            files.addAll(digest.getChildren());
                            for (TreeItem<FileNode> item : files) {
                                if (!item.getValue().isSelected()) {
                                    continue;
                                }
                                File file = item.getValue().getFile();
                                if (file == null || !file.exists() || !file.isFile()) {
                                    continue;
                                }
                                if (deleteRadio.isSelected()) {
                                    FileDeleteTools.delete(file);
                                } else {
                                    Desktop.getDesktop().moveToTrash(file);
                                }
                                deleted++;
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }

                @Override
                protected void taskQuit() {
                    bottomLabel.setText(message("TotalDeletedFiles") + ": " + deleted);
                    TreeItem rootItem = filesTreeView.getRoot();
                    List<TreeItem> digests = new ArrayList();
                    digests.addAll(rootItem.getChildren());
                    if (digests.isEmpty()) {
                        filesTreeView.setRoot(null);
                        return;
                    }
                    for (TreeItem digest : digests) {
                        List<TreeItem<FileNode>> files = new ArrayList();
                        files.addAll(digest.getChildren());
                        for (TreeItem<FileNode> item : files) {
                            File file = item.getValue().getFile();
                            if (file == null || !file.exists() || !file.isFile()) {
                                digest.getChildren().remove(item);
                            }
                        }
                        if (digest.getChildren().size() < 2) {
                            rootItem.getChildren().remove(digest);
                        }
                    }
                    if (digests.isEmpty()) {
                        filesTreeView.setRoot(null);
                    }
                    super.taskQuit();
                    task = null;
                }

            };
            start(task);
        }

    }

}
