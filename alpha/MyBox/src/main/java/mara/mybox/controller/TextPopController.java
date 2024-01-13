package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-7
 * @License Apache License Version 2.0
 */
public class TextPopController extends BaseChildController {

    protected TextInputControl sourceInput;
    protected ChangeListener listener;

    @FXML
    protected TextArea textArea;
    @FXML
    protected CheckBox wrapCheck, refreshChangeCheck;
    @FXML
    protected Button refreshButton;

    public TextPopController() {
        baseTitle = message("Texts");
    }

    @Override
    public void setFileType() {
        setFileType(FileType.Text);
    }

    public void setControls() {
        try {
            if (parentController != null) {
                baseName = parentController.baseName + "_" + baseName;
            }

            editButton.disableProperty().bind(Bindings.isEmpty(textArea.textProperty()));
            saveAsButton.disableProperty().bind(Bindings.isEmpty(textArea.textProperty()));

            refreshChangeCheck.setSelected(UserConfig.getBoolean(baseName + "Sychronized", true));
            checkSychronize();
            refreshChangeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldState, Boolean newState) {
                    checkSychronize();
                }
            });

            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "Wrap", true));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Wrap", newValue);
                    textArea.setWrapText(newValue);
                }
            });
            textArea.setWrapText(wrapCheck.isSelected());

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setSourceInput(BaseController parent, TextInputControl sourceInput) {
        try {
            this.parentController = parent;
            this.sourceInput = sourceInput;
            setControls();

            refreshAction();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setFile(BaseController parent, String filename) {
        this.parentController = parent;
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private String texts;

            @Override
            protected boolean handle() {
                try {
                    texts = TextFileTools.readTexts(this, new File(filename));
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                setText(texts);
            }

        };
        start(task);
    }

    public void setText(String text) {
        try {
            this.sourceInput = null;
            setControls();
            refreshAction();
            textArea.setText(text);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void checkSychronize() {
        if (sourceInput == null) {
            refreshChangeCheck.setVisible(false);
            refreshButton.setVisible(false);
            return;
        }
        if (listener == null) {
            listener = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldv, String newv) {
                    if (refreshChangeCheck.isVisible() && refreshChangeCheck.isSelected()) {
                        refreshAction();
                    }
                }
            };
        }
        if (refreshChangeCheck.isVisible() && refreshChangeCheck.isSelected()) {
            sourceInput.textProperty().addListener(listener);
        } else {
            sourceInput.textProperty().removeListener(listener);
        }
    }

    @FXML
    @Override
    public void refreshAction() {
        if (sourceInput == null) {
            refreshChangeCheck.setVisible(false);
            refreshButton.setVisible(false);
            return;
        }
        textArea.setText(sourceInput.getText());
    }

    @FXML
    public void editAction() {
        TextEditorController.edit(textArea.getText());
    }

    @FXML
    @Override
    public void saveAsAction() {
        File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    File tmpFile = TextFileTools.writeFile(textArea.getText());
                    return FileTools.override(tmpFile, file);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popSaved();
                recordFileWritten(file);
                TextEditorController.open(file);
            }
        };
        start(task);
    }

    @Override
    public void cleanPane() {
        try {
            if (sourceInput != null) {
                sourceInput.textProperty().removeListener(listener);
            }
            listener = null;
            sourceInput = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }


    /*
        static methods
     */
    public static TextPopController openInput(BaseController parent, TextInputControl textInput) {
        try {
            if (textInput == null) {
                return null;
            }
            TextPopController controller = (TextPopController) WindowTools.popStage(parent, Fxmls.TextPopFxml);
            controller.setSourceInput(parent, textInput);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static TextPopController openFile(BaseController parent, String filename) {
        try {
            if (filename == null) {
                return null;
            }
            TextPopController controller = (TextPopController) WindowTools.popStage(parent, Fxmls.TextPopFxml);
            controller.setFile(parent, filename);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static TextPopController loadText(String text) {
        try {
            TextPopController controller = (TextPopController) WindowTools.popStage(null, Fxmls.TextPopFxml);
            controller.setText(text);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
