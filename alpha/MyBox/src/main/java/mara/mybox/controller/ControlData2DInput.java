package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TmpFileTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-11-27
 * @License Apache License Version 2.0
 */
public class ControlData2DInput extends BaseController {

    protected String delimiterName;

    @FXML
    protected TextArea textArea;
    @FXML
    protected CheckBox nameCheck;
    @FXML
    protected Button refreshButton;
    @FXML
    protected ControlData2DResults dataController;

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            delimiterName = UserConfig.getString(baseName + "InputDelimiter", ",");

            nameCheck.setSelected(UserConfig.getBoolean(baseName + "WithNames", false));
            nameCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WithNames", nameCheck.isSelected());
                    refreshAction();
                }
            });

            refreshButton.disableProperty().bind(textArea.textProperty().isNull()
                    .or(textArea.textProperty().isEmpty()));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(String text) {
        try {
            if (text == null || text.isBlank()) {
                popError(message("InputOrPasteText"));
                return;
            }
            textArea.setText(text);
            delimiterName = null;  // guess at first 
            refreshAction();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void delimiterActon() {
        TextDelimiterController controller = TextDelimiterController.open(this, delimiterName, false, false);
        controller.okNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                delimiterName = controller.delimiterName;
                UserConfig.setString(baseName + "InputDelimiter", delimiterName);
                refreshAction();
                popDone();
            }
        });
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        try {
            String text = Clipboard.getSystemClipboard().getString();
            if (text == null || text.isBlank()) {
                popError(message("NoTextInClipboard"));
            }
            load(text);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void refreshAction() {
        dataController.loadNull();
        String text = textArea.getText();
        if (text == null || text.isBlank()) {
            popError(message("InputOrPasteText"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            protected DataFileCSV data2D;

            @Override
            protected boolean handle() {
                try {
                    File tmpFile = TmpFileTools.getTempFile();
                    TextFileTools.writeFile(tmpFile, text, Charset.forName("UTF-8"));
                    data2D = new DataFileCSV(tmpFile);
                    data2D.setCharset(Charset.forName("UTF-8"));
                    if (delimiterName == null) {
                        delimiterName = data2D.guessDelimiter();
                    }
                    data2D.setHasHeader(nameCheck.isSelected())
                            .setDelimiter(delimiterName);
                    return data2D != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                dataController.loadData(data2D);
            }

        };
        start(task);
    }

    public boolean hasData() {
        Data2D data2D = dataController.data2D;
        if (data2D == null || !data2D.isValid()) {
            return false;
        }
        return !dataController.tableData.isEmpty();
    }

    public Data2D data2D() {
        return dataController.data2D;
    }

    public List<List<String>> data() {
        Data2D data2D = dataController.data2D;
        if (data2D == null || !data2D.isValid()) {
            return null;
        }
        return data2D.allRows(false);
    }

}
