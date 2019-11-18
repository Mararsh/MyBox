package mara.mybox.controller;

import java.awt.Desktop;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.data.FileInformation;
import mara.mybox.fxml.TreeTableFileSizeCell;
import mara.mybox.fxml.TreeTableTimeCell;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-11-13
 * @License Apache License Version 2.0
 */
public class FilesRedundancyResultsController extends BaseController {

    protected Map<String, List<FileInformation>> redundancy;

    @FXML
    protected TreeTableView<FileInformation> resultsView;
    @FXML
    protected TreeTableColumn<FileInformation, String> digestColumn, fileColumn, typeColumn;
    @FXML
    protected TreeTableColumn<FileInformation, Long> sizeColumn, modifyTimeColumn, createTimeColumn;
    @FXML
    protected TreeTableColumn<FileInformation, Boolean> selectedColumn;
    @FXML
    protected RadioButton deleteRadio, trashRadio;

    public FilesRedundancyResultsController() {
        baseTitle = AppVariables.message("HandleFilesRedundancy");
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();
            initFilesTab();
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void initFilesTab() {
        try {

            fileColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("fileName"));
            fileColumn.setPrefWidth(400);

            selectedColumn.setCellValueFactory(
                    new Callback<CellDataFeatures<FileInformation, Boolean>, ObservableValue<Boolean>>() {
                @Override
                public ObservableValue<Boolean> call(CellDataFeatures<FileInformation, Boolean> param) {
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
            modifyTimeColumn.setCellFactory(new TreeTableTimeCell());

            createTimeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("createTime"));
            createTimeColumn.setCellFactory(new TreeTableTimeCell());

            resultsView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            resultsView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        TreeItem<FileInformation> item = resultsView.getSelectionModel().getSelectedItem();
                        if (item == null) {
                            return;
                        }
                        File file = item.getValue().getFile();
                        if (file == null || !file.exists() || !file.isFile()) {
                            return;
                        }
                        view(file);
                    }
                }
            });
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void selectChildren(TreeItem<FileInformation> item) {
        try {
            if (item == null) {
                return;
            }
            for (TreeItem<FileInformation> child : item.getChildren()) {
                resultsView.getSelectionModel().select(child);
                selectChildren(child);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkSelection() {
        try {

            int filesSelected = 0, filesTotal = 0, filesRundancy = 0;
            long sizeSelected = 0, sizeTotal = 0, sizeRedundant = 0, fileSize = 0;
            TreeItem rootItem = resultsView.getRoot();
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
                    sizeTotal += fileSize;
                    if (info.isSelected()) {
                        filesSelected++;
                        sizeSelected += fileSize;
                    }
                }
                sizeRedundant += (files.size() - 1) * fileSize;
            }
            bottomLabel.setText(MessageFormat.format(
                    message("RedundancyCheckValues"), filesTotal, FileTools.showFileSize(sizeTotal),
                    filesRundancy, FileTools.showFileSize(sizeRedundant),
                    filesSelected, FileTools.showFileSize(sizeSelected)));
            deleteButton.setDisable(filesSelected == 0);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    // https://stackoverflow.com/questions/29989892/checkboxtreetablecell-select-all-children-under-parent-event
    public void loadRedundancy(Map<String, List<FileInformation>> redundancy) {
        this.redundancy = redundancy;
        if (redundancy == null || redundancy.isEmpty()) {
            popInformation(message("NoRedundancy"));
        } else {
            FileInformation rootInfo = new FileInformation();
            rootInfo.setFileName(message("HandleFilesRedundancy"));
            rootInfo.setFileType("root");
            TreeItem<FileInformation> rootItem = new TreeItem(rootInfo);
            rootItem.setExpanded(true);
            rootInfo.getSelectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean oldItem, Boolean newItem) {
                    if (!isSettingValues) {
                        selectChildren(rootItem, newItem);
                        checkSelection();
                    }
                }
            });
            resultsView.setRoot(rootItem);

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
                            selectChildren(digestItem, newItem);
                            checkSelection();
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

            checkSelection();
        }

    }

    protected void selectChildren(TreeItem<FileInformation> item, boolean select) {
        if (item == null || item.getChildren() == null) {
            return;
        }
        for (TreeItem<FileInformation> child : item.getChildren()) {
            child.getValue().setSelected(select);
            selectChildren(child, select);
        }
    }

    public void exceptFirstAction() {
        isSettingValues = true;
        TreeItem<FileInformation> rootItem = resultsView.getRoot();
        List<TreeItem<FileInformation>> digests = rootItem.getChildren();
        if (digests == null || digests.isEmpty()) {
            resultsView.setRoot(null);
            return;
        }
        rootItem.getValue().setSelected(false);
        for (TreeItem<FileInformation> digest : digests) {
            digest.getValue().setSelected(false);
            List<TreeItem<FileInformation>> files = digest.getChildren();
            for (int i = 0; i < files.size(); i++) {
                files.get(i).getValue().setSelected(i > 0);
            }
        }
        isSettingValues = false;
        checkSelection();

    }

    @Override
    public void deleteAction() {
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private int deleted;

                @Override
                protected boolean handle() {
                    try {
                        deleted = 0;
                        TreeItem rootItem = resultsView.getRoot();
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
                    super.taskQuit();
                    bottomLabel.setText(message("TotalDeletedFiles") + ": " + deleted);
                    TreeItem rootItem = resultsView.getRoot();
                    List<TreeItem> digests = new ArrayList();
                    digests.addAll(rootItem.getChildren());
                    if (digests.isEmpty()) {
                        resultsView.setRoot(null);
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
                        resultsView.setRoot(null);
                    }

                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

}
