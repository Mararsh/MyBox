package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.controller.base.BatchController;
import mara.mybox.value.AppVaribles;
import mara.mybox.data.FileInformation;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesBatchController extends BatchController<FileInformation> {

    /**
     * Methods to be implemented
     */
    // SubClass should use either "makeSingleParameters()" or "makeBatchParameters()"
    @Override
    public void makeMoreParameters() {

    }
    // "targetFiles" and "actualParameters.finalTargetName" should be written by this method

    public String handleCurrentFile(FileInformation file) {
        return AppVaribles.getMessage("Successful");
    }

    public String handleCurrentDirectory(FileInformation file) {
        return AppVaribles.getMessage("Successful");
    }

    public FilesBatchController() {
    }

    /* ----Method may need updated ------------------------------------------------- */
    @Override
    public void initializeNext2() {

    }

    @Override
    public void initOptionsSection() {

    }

    /* ------Method need not updated commonly ----------------------------------------------- */
    @Override
    public void addFiles(int index, List<File> files) {
        try {
            if (files == null || files.isEmpty()) {
                return;
            }
            recordFileAdded(files.get(0));

            List<FileInformation> infos = new ArrayList<>();
            for (File file : files) {
                FileInformation info = new FileInformation(file);
                infos.add(info);
            }
            if (infos.isEmpty()) {
                return;
            }
            if (index < 0 || index >= tableData.size()) {
                tableData.addAll(infos);
            } else {
                tableData.addAll(index, infos);
            }
            tableView.refresh();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void viewFileAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(tableView.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index < 0 || index > tableData.size() - 1) {
                continue;
            }
            FileInformation info = tableData.get(index);
            if (info.getNewName() != null && !info.getNewName().isEmpty()) {
                FxmlStage.openTarget(getClass(), myStage, info.getNewName());
            } else {
                FxmlStage.openTarget(getClass(), myStage, info.getFile().getAbsolutePath());
            }
        }
    }

    public FileInformation getCurrentData() {
        return getData(currentParameters.currentIndex);
    }

    public FileInformation getData(int index) {
        try {
            return tableData.get(sourcesIndice.get(index));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void handleCurrentFile() {
        FileInformation d = getCurrentData();
        if (d == null) {
            return;
        }
        File file = d.getFile();
        currentParameters.sourceFile = file;
        String result;
        if (!file.exists()) {
            result = AppVaribles.getMessage("NotFound");
        } else if (file.isFile()) {
            result = handleCurrentFile(d);
        } else {
            result = handleCurrentDirectory(d);
        }
        d.setHandled(result);
        tableView.refresh();
        currentParameters.currentTotalHandled++;
    }

    @Override
    public FileInformation getData(File directory) {
        return new FileInformation(directory);
    }

    @Override
    public File getTableFile(int index) {
        return tableData.get(index).getFile();
    }

    public void markFileHandled(int index) {
        if (tableView == null) {
            return;
        }
        FileInformation d = getData(index);
        if (d == null) {
            return;
        }
        d.setHandled(getMessage("Yes"));
        tableView.refresh();
    }

    public void markFileHandled(int index, String message) {
        if (tableView == null) {
            return;
        }
        FileInformation d = getData(index);
        if (d == null) {
            return;
        }
        d.setHandled(message);
        tableView.refresh();
    }

}
