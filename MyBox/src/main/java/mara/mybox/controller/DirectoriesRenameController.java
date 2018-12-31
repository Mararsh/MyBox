package mara.mybox.controller;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.FileInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;

/**
 * @Author Mara
 * @CreateDate 2018-12-15
 * @Description
 * @License Apache License Version 2.0
 */
public class DirectoriesRenameController extends FilesRenameController {

    protected List<File> includeFiles;
    protected int dirFiles, dirRenamed;
    protected File currentDir;
    protected Map<String, String> currentNewNames;
    protected Map<String, Map<String, String>> newNames;

    @FXML
    private CheckBox subCheck, nameCheck;
    @FXML
    private TextField nameInput;

    @Override
    protected void initializeNext() {

        tableController = dirsTableController;
        dirsTableController.setRenameController(this);

        operationBarController.startButton.disableProperty().bind(
                Bindings.isEmpty(tableController.getFilesTableView().getItems())
                        .or(tableController.addButton.disableProperty())
        );

    }

    @Override
    protected void handleSourceFiles() {
        sourceFiles = new ArrayList();
        for (FileInformation f : sourceFilesInformation) {
            sourceFiles.add(new File(f.getFileName()));
        }
        total = sourceFiles.size();
        newNames = new HashMap();
    }

    @Override
    protected void handleCurrentIndex() {
        currentDir = sourceFiles.get(currentIndex);
        dirFiles = dirRenamed = 0;
        currentNewNames = new HashMap();
        renameDirectory(currentDir);
        newNames.put(currentDir.getAbsolutePath(), currentNewNames);
        if (tableController != null) {
            FileInformation d = tableController.findData(currentDir.getAbsolutePath());
            d.setHandled(MessageFormat.format(AppVaribles.getMessage("DirRenameSummary"),
                    dirFiles, dirRenamed));
        }
    }

    protected void renameDirectory(File dir) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        try {
            try {
                currentAccum = Integer.valueOf(acumFromInput.getText());
            } catch (Exception e) {
                currentAccum = 0;
            }
            List<File> files = new ArrayList<>();
            files.addAll(Arrays.asList(dir.listFiles()));
            sortFiles(files);

            int bdigit = (files.size() + "").length();
            try {
                digit = Integer.valueOf(digitInput.getText());
                if (digit < bdigit) {
                    digit = bdigit;
                }
            } catch (Exception e) {
                digit = bdigit;
            }
            digitInput.setText(digit + "");

            String[] names = StringTools.splitBySpace(nameInput.getText());
            for (File file : files) {
                if (file.isFile()) {
                    dirFiles++;
                    currentTotalHandled++;
                    String originalName = file.getAbsolutePath();
                    if (nameCheck.isSelected() && names.length > 0) {
                        boolean isValid = false;
                        for (String name : names) {
                            if (FileTools.getFileName(originalName).contains(name)) {
                                isValid = true;
                                break;
                            }
                        }
                        if (!isValid) {
                            continue;
                        }
                    }
                    String newName = renameFile(file);
                    if (newName != null) {
                        dirRenamed++;
                        currentNewNames.put(newName, originalName);
                    }
                } else if (subCheck.isSelected()) {
                    renameDirectory(file);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void recoveryAll() {
        if (newNames == null || newNames.isEmpty()) {
            return;
        }
        for (String dir : newNames.keySet()) {
            currentNewNames = newNames.get(dir);
            int recovered = 0;
            for (String newName : currentNewNames.keySet()) {
                String originalName = currentNewNames.get(newName);
                File f = new File(newName);
                if (!f.exists()) {
                    continue;
                }
                if (f.renameTo(new File(originalName))) {
                    recovered++;
                }
            }
            FileInformation item = tableController.findData(dir);
            if (item != null) {
                item.setHandled(MessageFormat.format(AppVaribles.getMessage("DirRecoverSummary"),
                        currentNewNames.size(), recovered));
            }
        }
    }

    protected void recoverySelected(ObservableList<FileInformation> selected) {
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (FileInformation dir : selected) {
            currentNewNames = newNames.get(dir.getFileName());
            int recovered = 0;
            for (String newName : currentNewNames.keySet()) {
                String originalName = currentNewNames.get(newName);
                File f = new File(newName);
                if (!f.exists()) {
                    continue;
                }
                if (f.renameTo(new File(originalName))) {
                    recovered++;
                }
            }
            dir.setHandled(MessageFormat.format(AppVaribles.getMessage("DirRecoverSummary"),
                    currentNewNames.size(), recovered));
        }
    }

}
