package mara.mybox.controller;

import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-08
 * @License Apache License Version 2.0
 */
public class FileRenameController extends BaseController {

    protected File file, newFile;

    @FXML
    protected ToggleGroup targetExistGroup;
    @FXML
    protected RadioButton targetReplaceRadio, targetRenameRadio, targetSkipRadio;
    @FXML
    protected TextField targetAppendInput;
    @FXML
    protected Label fileLabel, pathLabel, suffixLabel;
    @FXML
    protected TextField nameInput;

    public FileRenameController() {
        baseTitle = Languages.message("FileRename");
    }

    public void set(File file) {
        try {
            newFile = null;
            if (file == null || !file.exists() || !file.isFile()) {
                popError("InvalidParameters");
                closeStage();
                return;
            }
            this.file = file;
            fileLabel.setText(file.getAbsolutePath());
            pathLabel.setText(file.getParent() + File.separator);
            suffixLabel.setText("." + FileNameTools.suffix(file.getName()));
            nameInput.setText(FileNameTools.prefix(file.getName()));
            nameInput.requestFocus();
            nameInput.selectAll();

            getMyStage().setWidth(file.getAbsolutePath().length() * AppVariables.sceneFontSize + 40);
            myStage.setHeight(AppVariables.sceneFontSize * 14 + 80);
            myStage.setAlwaysOnTop(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (file == null || !file.exists() || !file.isFile()) {
                popError(message("InvalidParameters"));
                closeStage();
                return;
            }
            File theFile = new File(pathLabel.getText() + FileNameTools.filter(nameInput.getText() + suffixLabel.getText()));
            if (theFile.equals(file)) {
                popError(message("Unchanged"));
                return;
            }
            if (theFile.exists()) {
                if (!PopTools.askSure(getTitle(), Languages.message("SureReplaceExistedFile"))) {
                    return;
                }
            }
            if (FileTools.rename(file, theFile)) {
                newFile = theFile;
                closeStage();
            } else {
                popFailed();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    /*
        get/set
     */
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getNewFile() {
        return newFile;
    }

    public void setNewFile(File newFile) {
        this.newFile = newFile;
    }

    public Label getFileLabel() {
        return fileLabel;
    }

    public void setFileLabel(Label fileLabel) {
        this.fileLabel = fileLabel;
    }

    public Label getPathLabel() {
        return pathLabel;
    }

    public void setPathLabel(Label pathLabel) {
        this.pathLabel = pathLabel;
    }

    public Label getSuffixLabel() {
        return suffixLabel;
    }

    public void setSuffixLabel(Label suffixLabel) {
        this.suffixLabel = suffixLabel;
    }

    public TextField getNameInput() {
        return nameInput;
    }

    public void setNameInput(TextField nameInput) {
        this.nameInput = nameInput;
    }

}
