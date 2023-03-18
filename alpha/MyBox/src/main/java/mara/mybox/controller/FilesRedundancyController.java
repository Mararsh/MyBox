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
import mara.mybox.data.FileNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.MessageDigestTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-11-13
 * @License Apache License Version 2.0
 */
public class FilesRedundancyController extends BaseBatchFileController {

    protected ObservableList<FileNode> filesList;
    protected Map<String, List<FileNode>> redundancy;
    protected long totalChecked;
    protected LongProperty totalRedundancy;
    protected String done;

    @FXML
    protected HBox currentBox;
    @FXML
    protected Label currentLabel;
    @FXML
    protected Button goButton;

    public FilesRedundancyController() {
        baseTitle = message("FilesRedundancy");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            openTargetButton.setVisible(false);
            openCheck.setVisible(false);

            filesList = FXCollections.observableArrayList();
            redundancy = new ConcurrentHashMap();

            currentBox.setVisible(false);
            done = message("Done");

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    @Override
    public boolean makeMoreParameters() {
        filesList.clear();
        redundancy.clear();
        totalChecked = 0;
        totalRedundancy = new SimpleLongProperty(0);
        currentBox.setVisible(true);
        goButton.disableProperty().bind(totalRedundancy.isEqualTo(0));
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(File file) {
        try {
            if (!match(file)) {
                return done;
            }
            totalChecked++;
            FileNode d = new FileNode(file);
            filesList.add(d);
            return done;
        } catch (Exception e) {
            return done;
        }
    }

    @Override
    public String handleDirectory(File directory) {
        try {
            if (directory == null || !directory.isDirectory()) {
                return done;
            }
            File[] files = directory.listFiles();
            if (files == null) {
                return done;
            }
            for (File srcFile : files) {
                if (task == null || task.isCancelled()) {
                    return done;
                }
                if (srcFile.isFile()) {
                    handleFile(srcFile);
                } else if (srcFile.isDirectory()) {
                    handleDirectory(srcFile);
                }
            }
            return done;
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
            return done;
        }
    }

    @Override
    public void afterHandleFiles() {
        try {
            if (filesList == null || filesList.isEmpty()) {
                return;
            }
            showStatus(message("SortingFilesSize"));
            Collections.sort(filesList, new Comparator<FileNode>() {
                @Override
                public int compare(FileNode f1, FileNode f2) {
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
            FileNode f = filesList.get(0);
            long size = f.getFileSize(), big = 50 * 1024 * 1024L;
            List<FileNode> sameSize = new ArrayList();
            sameSize.add(f);
            updateTaskProgress(1, filesList.size());
            for (int i = 1; i < filesList.size(); ++i) {
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
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkDigest(List<FileNode> files) {
        long big = 500 * 1024 * 1024L;
        for (FileNode f : files) {
            if (task == null || task.isCancelled()) {
                return;
            }
            if (f.getFileSize() > big) {
                showStatus(MessageFormat.format(message("CalculatingDigest"), f.getFile().getAbsolutePath()), f);
            }
            f.setData(ByteTools.bytesToHex(MessageDigestTools.MD5(f.getFile())));
        }
        Collections.sort(files, new Comparator<FileNode>() {
            @Override
            public int compare(FileNode f1, FileNode f2) {
                return f1.getData().compareTo(f2.getData());
            }
        });
        FileNode f = files.get(0);
        String digest = f.getData();
        List<FileNode> sameFiles = new ArrayList();
        sameFiles.add(f);
        for (int i = 1; i < files.size(); ++i) {
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

    public void showStatus(String info, FileNode file) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                currentLabel.setText(info);
                String s = message("FindingFilesRedundancy") + "   "
                        + message("Cost") + ": " + DateTools.datetimeMsDuration(new Date(), processStartTime) + ".   "
                        + MessageFormat.format(message("HandlingObject"),
                                file.getFile().getAbsolutePath() + "   " + FileTools.showFileSize(file.getFileSize()));
                statusLabel.setText(s);
            }
        });
    }

    @FXML
    @Override
    public void goAction() {
        if (redundancy.size() > 0) {
            FilesRedundancyResultsController controller
                    = (FilesRedundancyResultsController) WindowTools.openStage(Fxmls.FilesRedundancyResultsFxml);
            if (controller != null) {
                controller.loadRedundancy(redundancy);
            }

        } else {
            popInformation(message("NoRedundancy"));
        }

    }

    @Override
    public void donePost() {
        goAction();

        showCost();
        if (operationBarController.miaoCheck.isSelected()) {
            SoundTools.miao3();
        }
    }

    @Override
    public void showCost() {
        String s;
        if (paused) {
            s = message("Paused");
        } else {
            s = message(currentParameters.status);
        }
        s += ".  "
                + message("TotalCheckedFiles") + ": " + totalChecked + "   "
                + message("TotalRedundancyFiles") + ": " + totalRedundancy.get() + "   "
                + message("Cost") + ": " + DateTools.datetimeMsDuration(new Date(), processStartTime) + ". "
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
