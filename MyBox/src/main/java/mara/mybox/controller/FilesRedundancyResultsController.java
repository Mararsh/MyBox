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
import javafx.stage.Modality;
import mara.mybox.data.FileInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-11-13
 * @License Apache License Version 2.0
 */
public class FilesRedundancyResultsController extends FilesTreeController {

    protected Map<String, List<FileInformation>> redundancy;

    @FXML
    protected RadioButton deleteRadio, trashRadio;

    public FilesRedundancyResultsController() {
        baseTitle = AppVariables.message("HandleFilesRedundancy");
    }

    public void checkSelection() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private int filesSelected = 0, filesTotal = 0, filesRundancy = 0;
                private long sizeSelected = 0, sizeTotal = 0, sizeRedundant = 0, fileSize = 0;

                @Override
                protected boolean handle() {
                    try {
                        TreeItem rootItem = filesTreeView.getRoot();
                        List<TreeItem> digests = new ArrayList();
                        digests.addAll(rootItem.getChildren());
                        for (TreeItem digest : digests) {
                            List<TreeItem<FileInformation>> files = new ArrayList();
                            files.addAll(digest.getChildren());
                            filesTotal += files.size();
                            filesRundancy += files.size() - 1;
                            for (TreeItem<FileInformation> item : files) {
                                FileInformation info = item.getValue();
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
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    // https://stackoverflow.com/questions/29989892/checkboxtreetablecell-select-all-children-under-parent-event
    public void loadRedundancy(Map<String, List<FileInformation>> data) {
        filesTreeView.setRoot(null);
        if (data == null || data.isEmpty()) {
            popInformation(message("NoRedundancy"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private TreeItem<FileInformation> rootItem;

                @Override
                protected boolean handle() {
                    try {
                        redundancy = data;
                        FileInformation rootInfo = new FileInformation();
                        rootInfo.setFileName(message("HandleFilesRedundancy"));
                        rootInfo.setFileType("root");
                        rootItem = new TreeItem(rootInfo);
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

                        for (String digest : redundancy.keySet()) {
                            FileInformation digestInfo = new FileInformation();
                            digestInfo.setFileName(digest);
                            digestInfo.setFileType("digest");
                            TreeItem<FileInformation> digestItem = new TreeItem(digestInfo);
                            digestItem.setExpanded(true);

                            List<FileInformation> files = redundancy.get(digest);
                            digestInfo.setFileSize(files.get(0).getFileSize());
                            digestInfo.getSelectedProperty().addListener(new ChangeListener<Boolean>() {
                                @Override
                                public void changed(ObservableValue<? extends Boolean> ov,
                                        Boolean oldItem, Boolean newItem) {
                                    if (!isSettingValues) {
                                        treeItemSelected(digestItem, newItem);
                                    }
                                }
                            });

                            for (FileInformation file : files) {
                                File f = file.getFile();
                                if (f == null || !f.exists() || !f.isFile()) {
                                    continue;
                                }
                                digestItem.getChildren().add(new TreeItem(file));
                                file.getSelectedProperty().addListener(new ChangeListener<Boolean>() {
                                    @Override
                                    public void changed(ObservableValue<? extends Boolean> ov,
                                            Boolean oldItem, Boolean newItem) {
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
                        logger.debug(error);
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
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @FXML
    public void exceptFirstAction() {
        isSettingValues = true;
        TreeItem<FileInformation> rootItem = filesTreeView.getRoot();
        List<TreeItem<FileInformation>> digests = rootItem.getChildren();
        if (digests == null || digests.isEmpty()) {
            filesTreeView.setRoot(null);
            return;
        }
        rootItem.getValue().setSelected(false);
        for (TreeItem<FileInformation> digest : digests) {
            digest.getValue().setSelected(false);
            List<TreeItem<FileInformation>> files = digest.getChildren();
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
        TreeItem<FileInformation> rootItem = filesTreeView.getRoot();
        List<TreeItem<FileInformation>> digests = rootItem.getChildren();
        if (digests == null || digests.isEmpty()) {
            filesTreeView.setRoot(null);
            return;
        }
        rootItem.getValue().setSelected(false);
        for (TreeItem<FileInformation> digest : digests) {
            digest.getValue().setSelected(false);
            List<TreeItem<FileInformation>> files = digest.getChildren();
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
            task = new SingletonTask<Void>() {
                private int deleted;

                @Override
                protected boolean handle() {
                    try {
                        deleted = 0;
                        TreeItem rootItem = filesTreeView.getRoot();
                        List<TreeItem> digests = new ArrayList();
                        digests.addAll(rootItem.getChildren());
                        for (TreeItem digest : digests) {
                            List<TreeItem<FileInformation>> files = new ArrayList();
                            files.addAll(digest.getChildren());
                            for (TreeItem<FileInformation> item : files) {
                                if (!item.getValue().isSelected()) {
                                    continue;
                                }
                                File file = item.getValue().getFile();
                                if (file == null || !file.exists() || !file.isFile()) {
                                    continue;
                                }
                                if (deleteRadio.isSelected()) {
                                    file.delete();
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
                        List<TreeItem<FileInformation>> files = new ArrayList();
                        files.addAll(digest.getChildren());
                        for (TreeItem<FileInformation> item : files) {
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
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

}
