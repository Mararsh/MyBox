package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.data.DownloadItem;
import mara.mybox.data.DownloadTask;
import mara.mybox.data.StringTable;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.TableDurationCell;
import mara.mybox.fxml.TableFileSizeCell;
import mara.mybox.fxml.TableTimeCell;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-13 8:14:06
 * @Description
 * @License Apache License Version 2.0
 */
public class DownloadController extends BaseController {

    public final static ObservableList<DownloadItem> downloadData = FXCollections.observableArrayList();

    @FXML
    protected Button downloadButton;
    @FXML
    protected TableView<DownloadItem> tableView;
    @FXML
    protected TableColumn<DownloadItem, String> addressColumn, statusColumn, progressColumn,
            speedColumn;
    @FXML
    protected TableColumn<DownloadItem, Long> currentColumn, totalColumn,
            startColumn, endColumn, costColumn;
    @FXML
    protected Label tableLabel;

    public DownloadController() {
        baseTitle = AppVariables.message("DownloadManage");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.setItems(downloadData);

            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    if (!isSettingValues) {
                        tableSelected();
                    }
                }
            });

            addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
            totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalSize"));
            totalColumn.setCellFactory(new TableFileSizeCell());
            currentColumn.setCellValueFactory(new PropertyValueFactory<>("currentSize"));
            currentColumn.setCellFactory(new TableFileSizeCell());
            startColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
            startColumn.setCellFactory(new TableTimeCell());
            endColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
            endColumn.setCellFactory(new TableTimeCell());
            costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
            costColumn.setCellFactory(new TableDurationCell());
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
            speedColumn.setCellValueFactory(new PropertyValueFactory<>("speed"));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void tableSelected() {
        DownloadItem selected = tableView.getSelectionModel().getSelectedItem();
        boolean none = (selected == null);
        infoButton.setDisable(none);
        cancelButton.setDisable(none);
        downloadButton.setDisable(none);
        deleteButton.setDisable(none);
    }

    public void download(String address) {
        try {
            if (address == null || address.trim().isBlank()) {
                return;
            }
            DownloadItem item = DownloadItem.create()
                    .setAddress(address);
            synchronized (downloadData) {
                downloadData.add(item);
            }
            DownloadTask dtask = download(item);
            item.setTask(dtask);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void addAction(ActionEvent event) {
        try {
            TextInputDialog dialog = new TextInputDialog("https://");
            dialog.setTitle(message("DownloadManage"));
            dialog.setHeaderText(message("InputAddress"));
            dialog.setContentText("");
            dialog.getEditor().setPrefWidth(500);
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();
            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) {
                return;
            }
            String address = result.get();
            download(address);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void infoAction() {
        List<Integer> selected = tableView.getSelectionModel().getSelectedIndices();
        for (Integer i : selected) {
            info(downloadData.get(i).getAddress());
        }
    }

    @FXML
    public void downloadAction() {
        List<DownloadItem> selected = tableView.getSelectionModel().getSelectedItems();
        for (DownloadItem item : selected) {
            DownloadTask dtask = item.getTask();
            if (dtask != null) {
                logger.debug(dtask.getState());
            }
            if (dtask != null && dtask.isRunning()) {
                continue;
            }
            dtask = download(item);
            item.setTask(dtask);
        }
    }

    @FXML
    @Override
    public void cancelAction() {
        List<DownloadItem> selected = tableView.getSelectionModel().getSelectedItems();
        for (DownloadItem item : selected) {
            DownloadTask dtask = item.getTask();
            if (dtask != null) {
                logger.debug(dtask.getState());
                dtask.cancel();
            }
        }
    }

    @FXML
    @Override
    public void deleteAction() {
        List<DownloadItem> selected = tableView.getSelectionModel().getSelectedItems();
        List<DownloadItem> items = new ArrayList();
        items.addAll(selected);
        for (DownloadItem item : items) {
            DownloadTask dtask = item.getTask();
            if (dtask != null) {
                logger.debug(dtask.getState());
                dtask.cancel();
            }
        }
        synchronized (downloadData) {
            downloadData.removeAll(items);
        }
    }

    public void info(String inAddress) {
        if (inAddress == null
                || (!inAddress.startsWith("http://") && !inAddress.startsWith("https://"))) {
            popError(message("InvalidParameter"));
            return;
        }
        Task infoTask = new DownloadTask() {

            @Override
            protected boolean initValues() {
                readHead = true;
                address = inAddress;
                return super.initValues();
            }

            @Override
            protected void whenSucceeded() {
                if (head == null) {
                    popError(AppVariables.message("InvalidData"));
                    return;
                }
                StringBuilder s = new StringBuilder();
                s.append("<h1  class=\"center\">").append(address).append("</h1>\n");
                s.append("<hr>\n");
                List<String> names = new ArrayList<>();
                names.addAll(Arrays.asList(message("Name"), message("Value")));
                StringTable table = new StringTable(names);
                for (Object key : head.keySet()) {
                    String name = (String) key;
                    if (name.startsWith("HeaderField_") || name.startsWith("RequestProperty_")) {
                        continue;
                    }
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(name, (String) head.get(key)));
                    table.add(row);
                }
                s.append(StringTable.tableDiv(table));
                s.append("<h2  class=\"center\">").append("Header Fields").append("</h2>\n");
                int hlen = "HeaderField_".length();
                for (Object key : head.keySet()) {
                    String name = (String) key;
                    if (!name.startsWith("HeaderField_")) {
                        continue;
                    }
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(name.substring(hlen), (String) head.get(key)));
                    table.add(row);
                }
                s.append(StringTable.tableDiv(table));
                s.append("<h2  class=\"center\">").append("Request Property").append("</h2>\n");
                int rlen = "RequestProperty_".length();
                for (Object key : head.keySet()) {
                    String name = (String) key;
                    if (!name.startsWith("RequestProperty_")) {
                        continue;
                    }
                    List<String> row = new ArrayList<>();
                    row.addAll(Arrays.asList(name.substring(rlen), (String) head.get(key)));
                    table.add(row);
                }
                s.append(StringTable.tableDiv(table));
                FxmlStage.openHtmlViewer(null, s.toString());
            }

            @Override
            protected void whenFailed() {
                if (error != null) {
                    popError(error);
                }
            }

        };
        Thread thread = new Thread(infoTask);
        openHandlingStage(infoTask, Modality.WINDOW_MODAL);
        thread.setDaemon(true);
        thread.start();
    }

    protected DownloadTask download(DownloadItem inItem) {
        if (inItem == null) {
            popError(message("InvalidParameter"));
            return null;
        }
        String inAddress = inItem.getAddress();
        if ((!inAddress.startsWith("http://") && !inAddress.startsWith("https://"))) {
            popError(message("InvalidParameter"));
            return null;
        }
        DownloadTask downloadTask = new DownloadTask() {

            protected DownloadItem item;

            @Override
            protected boolean initValues() {
                this.item = inItem;
                readHead = false;
                address = inAddress;
                targetPath = AppVariables.MyBoxDownloadsPath;
                if (!super.initValues()) {
                    return false;
                }
                item.setStartTime(startTime.getTime())
                        .setCurrentSize(currentSize);
                return true;
            }

            @Override
            protected void progress() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        item.setTotalSize(totalSize);
                        item.setCurrentSize(currentSize);
                        if (error != null) {
                            item.setStatus(error);
                        } else {
                            item.setStatus(getState().name());
                        }
                        tableView.refresh();
                    }
                });
            }

            @Override
            protected void whenSucceeded() {
                if (targetFile != null && targetFile.exists()) {
                    item.setStatus(message("Finished"));
                } else if (error != null) {
                    item.setStatus(error);
                } else {
                    item.setStatus("");
                }
            }

            @Override
            protected void whenFailed() {
                item.setStatus(message("Failed"));
            }

            @Override
            protected void whenCanceled() {
                item.setStatus(message("Canceled"));
            }

            @Override
            protected void finalAction() {
                if (error != null) {
                    popError(error);
                }
                endTime = new Date();
                item.setEndTime(endTime.getTime());
                tableView.refresh();
            }

        };
        Thread thread = new Thread(downloadTask);
        thread.setDaemon(true);
        thread.start();
        return downloadTask;
    }

    @FXML
    public void openAction() {
        browseURI(AppVariables.MyBoxDownloadsPath.toURI());
    }

}
