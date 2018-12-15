package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.FileEncoding;
import mara.mybox.objects.FileInformation;
import mara.mybox.tools.FileEncodingTools;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class TextEncodingBatchController extends FileBatchController {

    private boolean autoDetermine;
    private FileEncoding sourceEncoding, targetEncoding;
    private int dirTotal, dirOk;

    @FXML
    protected ToggleGroup sourceGroup;
    @FXML
    private ComboBox<String> sourceBox, targetBox;
    @FXML
    protected CheckBox targetBomCheck;
    @FXML
    private CheckBox subCheck, nameCheck;
    @FXML
    private TextField nameInput;

    public TextEncodingBatchController() {
        sourcePathKey = "TextFilePathKey";
        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("*", "*.*"));
                add(new FileChooser.ExtensionFilter("txt", "*.txt"));
                add(new FileChooser.ExtensionFilter("html", "*.html", "*.htm"));
            }
        };
    }

    @Override
    protected void initSourceSection() {
        try {
            sourceFilesInformation = FXCollections.observableArrayList();

            handledColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("handled"));
            fileColumn.setCellValueFactory(new PropertyValueFactory<FileInformation, String>("fileName"));

            sourceTable.setItems(sourceFilesInformation);
            sourceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            sourceTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        openAction();
                    }
                }
            });
            sourceTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkTableSelected();
                }
            });
            checkTableSelected();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initOptionsSection() {

        sourceEncoding = new FileEncoding();
        targetEncoding = new FileEncoding();

        sourceGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkSource();
            }
        });

        List<String> setNames = FileEncodingTools.getCharsetNames();
        sourceBox.getItems().addAll(setNames);
        sourceBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                sourceEncoding.setCharset(Charset.forName(newValue));
            }
        });
        sourceBox.getSelectionModel().select(Charset.defaultCharset().name());

        targetBox.getItems().addAll(setNames);
        targetBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                targetEncoding.setCharset(Charset.forName(newValue));
                if ("UTF-8".equals(newValue) || "UTF-16BE".equals(newValue)
                        || "UTF-16LE".equals(newValue) || "UTF-32BE".equals(newValue)
                        || "UTF-32LE".equals(newValue)) {
                    targetBomCheck.setDisable(false);
                } else {
                    targetBomCheck.setDisable(true);
                    if ("UTF-16".equals(newValue) || "UTF-32".equals(newValue)) {
                        targetBomCheck.setSelected(true);
                    } else {
                        targetBomCheck.setSelected(false);
                    }
                }
            }
        });
        targetBox.getSelectionModel().select(Charset.defaultCharset().name());

        checkSource();
    }

    protected void checkSource() {
        RadioButton selected = (RadioButton) sourceGroup.getSelectedToggle();
        if (getMessage("DetermainAutomatically").equals(selected.getText())) {
            autoDetermine = true;
            sourceBox.setDisable(true);
        } else {
            autoDetermine = false;
            sourceEncoding.setCharset(Charset.forName(sourceBox.getSelectionModel().getSelectedItem()));
            sourceBox.setDisable(false);
        }
    }

    @Override
    protected void handleCurrentIndex() {
        File currentDir = sourceFiles.get(currentParameters.currentIndex);
        dirTotal = dirOk = 0;
        File rootPath = new File(currentParameters.targetPath + File.separator + currentDir.getName());
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        convertDirectory(currentDir, rootPath);
        markFileHandled(currentParameters.currentIndex,
                MessageFormat.format(AppVaribles.getMessage("DirHandledSummary"), dirTotal, dirOk));
        currentParameters.currentTotalHandled++;
    }

    protected void convertDirectory(File sourcePath, File targetPath) {
        if (sourcePath == null || !sourcePath.exists() || !sourcePath.isDirectory()) {
            return;
        }
        try {
            List<File> files = new ArrayList<>();
            files.addAll(Arrays.asList(sourcePath.listFiles()));

            String[] names = nameInput.getText().trim().split("\\s+");
            for (File srcFile : files) {
                if (task.isCancelled()) {
                    return;
                }
                File targetFile = new File(targetPath + File.separator + srcFile.getName());
                if (srcFile.isFile()) {
                    dirTotal++;
                    String originalName = srcFile.getAbsolutePath();
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
                    if (convertFile(srcFile, targetFile)) {
                        dirOk++;
                    }
                } else if (subCheck.isSelected()) {
                    targetFile.mkdirs();
                    convertDirectory(srcFile, targetFile);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected boolean convertFile(File srcFile, File targetFile) {
        try {
            sourceEncoding.setFile(srcFile);
            if (autoDetermine) {
                boolean ok = FileEncodingTools.checkCharset(sourceEncoding);
                if (!ok || sourceEncoding == null) {
                    return false;
                }
            }
            String filename = targetFile.getAbsolutePath();
            String suffix = FileTools.getFileSuffix(filename);
            currentParameters.finalTargetName = filename;

            boolean skip = false;
            if (targetExistType == TargetExistType.Rename) {
                while (new File(currentParameters.finalTargetName).exists()) {
                    currentParameters.finalTargetName = FileTools.getFilePrefix(currentParameters.finalTargetName)
                            + targetSuffixInput.getText().trim() + "." + suffix;
                }
            } else if (targetExistType == TargetExistType.Skip) {
                if (new File(currentParameters.finalTargetName).exists()) {
                    skip = true;
                }
            }
            if (!skip) {
                targetEncoding.setFile(new File(currentParameters.finalTargetName));
                targetEncoding.setWithBom(targetBomCheck.isSelected());
                return FileEncodingTools.convertCharset(sourceEncoding, targetEncoding);
            } else {
                return true;
            }
        } catch (Exception e) {
            logger.error(e.toString());
            return false;
        }
    }

    @Override
    protected void viewFile(String file) {
//        TextEncodingController controller = (TextEncodingController) openStage(CommonValues.TextEncodingFxml,
//                AppVaribles.getMessage("TextEncoding"), false, true);
//        controller.openFile(new File(file));
    }

    @Override
    protected void addAction(int index) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File defaultPath = new File(AppVaribles.getUserConfigValue(sourcePathKey, CommonValues.UserFilePath));
            if (!defaultPath.isDirectory()) {
                defaultPath = new File(CommonValues.UserFilePath);
            }
            chooser.setInitialDirectory(defaultPath);
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            AppVaribles.setUserConfigValue(LastPathKey, directory.getPath());
            AppVaribles.setUserConfigValue(sourcePathKey, directory.getPath());

            FileInformation d = new FileInformation(directory);
            if (index < 0 || index >= sourceFilesInformation.size()) {
                sourceFilesInformation.add(d);
            } else {
                sourceFilesInformation.add(index, d);
            }
            sourceTable.refresh();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

}
