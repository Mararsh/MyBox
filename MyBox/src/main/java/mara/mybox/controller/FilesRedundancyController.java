package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import mara.mybox.data.FileInformation;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-11-13
 * @License Apache License Version 2.0
 */
public class FilesRedundancyController extends FilesBatchController {

    protected ObservableList<FileInformation> filesList;
    protected Map<String, List<FileInformation>> redundancy;
    protected long totalChecked;
    protected LongProperty totalRedundancy;

    @FXML
    protected HBox currentBox;
    @FXML
    protected Label currentLabel;
    @FXML
    protected Button handleButton;

    public FilesRedundancyController() {
        baseTitle = AppVariables.message("FilesRedundancy");
        allowPaused = false;
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            openTargetButton.setVisible(false);
            openCheck.setVisible(false);

            filesList = FXCollections.observableArrayList();
            redundancy = new ConcurrentHashMap();

            currentBox.setVisible(false);

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @Override
    public boolean makeBatchParameters() {
        filesList.clear();
        redundancy.clear();
        totalChecked = 0;
        totalRedundancy = new SimpleLongProperty(0);
        currentBox.setVisible(true);
        handleButton.disableProperty().bind(totalRedundancy.isEqualTo(0));
        return super.makeBatchParameters();
    }

    @Override
    public String handleFile(File file) {
        try {
            showHandling(file);
            if (!match(file)) {
                return AppVariables.message("Done");
            }
            totalChecked++;
            FileInformation d = new FileInformation(file);
            filesList.add(d);
            return AppVariables.message("Done");
        } catch (Exception e) {
            return AppVariables.message("Done");
        }
    }

    @Override
    public String handleDirectory(File directory) {
        try {
            if (directory == null || !directory.isDirectory()) {
                return AppVariables.message("Done");
            }
            showHandling(directory);
            File[] files = directory.listFiles();
            for (File srcFile : files) {
                if (task == null || task.isCancelled()) {
                    return AppVariables.message("Done");
                }
                if (srcFile.isFile()) {
                    handleFile(srcFile);
                } else if (srcFile.isDirectory()) {
                    handleDirectory(srcFile);
                }
            }
            return AppVariables.message("Done");
        } catch (Exception e) {
//            logger.error(e.toString());
            return AppVariables.message("Done");
        }
    }

    @Override
    public void afterHandleFiles() {
        try {
            if (filesList == null || filesList.isEmpty()) {
                return;
            }
            showStatus(message("SortingFilesSize"));
            Collections.sort(filesList, new Comparator<FileInformation>() {
                @Override
                public int compare(FileInformation f1, FileInformation f2) {
                    long sizeDiff = f1.getFileSize() - f2.getFileSize();
                    if (sizeDiff > 0) {
                        return 1;
                    } else if (sizeDiff < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            showStatus(message("FindingFilesRedundancy"));
            updateTaskProgress(0, filesList.size());
            FileInformation f = filesList.get(0);
            long size = f.getFileSize(), big = 50 * 1024 * 1024;
            List<FileInformation> sameSize = new ArrayList();
            sameSize.add(f);
            updateTaskProgress(1, filesList.size());
            for (int i = 1; i < filesList.size(); i++) {
                if (task == null || task.isCancelled()) {
                    return;
                }
                f = filesList.get(i);
                if (i % 200 == 0 || size > big) {
                    showStatus(MessageFormat.format(message("RedundancyCurrentValues"),
                            redundancy.size(), totalRedundancy.get()), f);
                    updateTaskProgress(i, filesList.size());
                }

                if (f.getFileSize() == size) {
                    sameSize.add(f);
                } else {
                    if (sameSize.size() > 1) {
                        checkDigest(sameSize);
                    }
                    sameSize = new ArrayList();
                    sameSize.add(f);
                    size = f.getFileSize();
                }
            }
            if (sameSize.size() > 1) {
                checkDigest(sameSize);
            }
            filesList.clear();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void checkDigest(List<FileInformation> files) {
        long big = 500 * 1024 * 1024;
        for (FileInformation f : files) {
            if (task == null || task.isCancelled()) {
                return;
            }
            if (f.getFileSize() > big) {
                showStatus(MessageFormat.format(message("CalculatingDigest"),
                        f.getFileName()), f);
            }
            f.setData(ByteTools.bytesToHex(SystemTools.MD5(f.getFile())));
        }
        Collections.sort(files, new Comparator<FileInformation>() {
            @Override
            public int compare(FileInformation f1, FileInformation f2) {
                return f1.getData().compareTo(f2.getData());
            }
        });
        FileInformation f = files.get(0);
        String digest = f.getData();
        List<FileInformation> sameFiles = new ArrayList();
        sameFiles.add(f);
        for (int i = 1; i < files.size(); i++) {
            if (task == null || task.isCancelled()) {
                return;
            }
            f = files.get(i);
            if (f.getData().equals(digest)) {
                sameFiles.add(f);
            } else {
                if (sameFiles.size() > 1) {
                    redundancy.put(digest, sameFiles);
                    totalRedundancy.set(totalRedundancy.get() + sameFiles.size() - 1);
                    showStatus(MessageFormat.format(message("RedundancyCurrentValues"),
                            redundancy.size(), totalRedundancy.get()), f);
                }
                sameFiles = new ArrayList();
                sameFiles.add(f);
                digest = f.getData();
            }
        }
        if (sameFiles.size() > 1) {
            redundancy.put(digest, sameFiles);
            totalRedundancy.set(totalRedundancy.get() + sameFiles.size() - 1);
        }
    }

    public void showStatus(String info, FileInformation file) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                currentLabel.setText(info);
                long cost = new Date().getTime() - processStartTime.getTime();
                String s = message("FindingFilesRedundancy") + "   "
                        + message("Cost") + ": " + DateTools.showTime(cost) + ".   "
                        + MessageFormat.format(message("HandlingObject"),
                                file.getFileName() + "   " + FileTools.showFileSize(file.getFileSize()));
                statusLabel.setText(s);
            }
        });
    }

    @FXML
    public void handleAction() {
        if (redundancy.size() > 0) {
            FilesRedundancyResultsController controller
                    = (FilesRedundancyResultsController) FxmlStage.openStage(CommonValues.FilesRedundancyResultsFxml);
            if (controller != null) {
                controller.loadRedundancy(redundancy);
            }

        } else {
            popInformation(message("NoRedundancy"));
        }

    }

    @Override
    public void donePost() {
        handleAction();

        showCost();
        if (operationBarController.miaoCheck.isSelected()) {
            FxmlControl.miao3();
        }
    }

    @Override
    public void showCost() {
        if (operationBarController.getStatusLabel() == null) {
            return;
        }
        long cost = new Date().getTime() - processStartTime.getTime();
        String s;
        if (paused) {
            s = message("Paused");
        } else {
            s = message(currentParameters.status);
        }
        s += ".  "
                + message("TotalCheckedFiles") + ": " + totalChecked + "   "
                + message("TotalRedundancyFiles") + ": " + totalRedundancy.get() + "   "
                + message("Cost") + ": " + DateTools.showTime(cost) + ". "
                + message("StartTime") + ": " + DateTools.datetimeToString(processStartTime) + ", "
                + message("EndTime") + ": " + DateTools.datetimeToString(new Date());
        statusLabel.setText(s);
        currentLabel.setText("");
    }

    @FXML
    @Override
    public void openTarget(ActionEvent event) {

    }

}
