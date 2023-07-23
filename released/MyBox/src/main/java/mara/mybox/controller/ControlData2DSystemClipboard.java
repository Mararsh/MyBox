package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.Clipboard;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileText;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.FileTmpTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-11-27
 * @License Apache License Version 2.0
 */
public class ControlData2DSystemClipboard extends BaseController {

    protected String delimiterName;
    protected DataFileText textData;
    protected SimpleBooleanProperty loadNotify;

    @FXML
    protected TextArea textArea;
    @FXML
    protected ToggleGroup formatGroup;
    @FXML
    protected RadioButton csvRadio, textsRadio;
    @FXML
    protected CheckBox nameCheck;
    @FXML
    protected Label delimiterLabel, commentsLabel;
    @FXML
    protected Button refreshButton;

    @Override
    public void initControls() {
        try {
            super.initControls();

            loadNotify = new SimpleBooleanProperty();

            delimiterName = UserConfig.getString(baseName + "InputDelimiter", ",");
            labelDelimiter();

            formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkFormat();
                    refreshAction();
                }
            });
            checkFormat();

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
            MyBoxLog.error(e);
        }
    }

    public void checkFormat() {
        if (csvRadio.isSelected()) {
            commentsLabel.setText(message("CSVComments"));
            if (TextTools.BlanksName.equals(delimiterName)) {
                delimiterName = TextTools.BlankName;
            }
        } else {
            commentsLabel.setText(message("TextDataComments"));
        }
    }

    public void labelDelimiter() {
        delimiterLabel.setText(message("Delimiter") + ": "
                + TextTools.delimiterMessage(delimiterName));
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
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void delimiterActon() {
        TextDelimiterController controller = TextDelimiterController.open(this, delimiterName, true, textsRadio.isSelected());
        controller.okNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                delimiterName = controller.delimiterName;
                UserConfig.setString(baseName + "InputDelimiter", delimiterName);
                refreshAction();
                controller.close();
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
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void refreshAction() {
        labelDelimiter();
        String text = textArea.getText();
        if (text == null || text.isBlank()) {
            popError(message("InputOrPasteText"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    File tmpFile = FileTmpTools.getTempFile();
                    TextFileTools.writeFile(tmpFile, text, Charset.forName("UTF-8"));
                    if (csvRadio.isSelected()) {
                        textData = new DataFileCSV();
                    } else {
                        textData = new DataFileText();
                    }
                    textData.setFile(tmpFile).setCharset(Charset.forName("UTF-8"));
                    if (delimiterName == null) {
                        delimiterName = textData.guessDelimiter();
                    }
                    textData.setHasHeader(nameCheck.isSelected())
                            .setDelimiter(delimiterName);
                    return textData != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                labelDelimiter();
                loadNotify.set(!loadNotify.get());
            }

        };
        start(task);
    }

    public boolean hasData() {
        return textData != null && textData.isValid();
    }

    public void editAction() {
        if (textData == null || !textData.isValid()) {
            return;
        }
        if (textData.isCSV()) {
            if (textData.getFile() != null) {
                DataFileCSVController.open(textData);
            } else {
                DataFileCSVController.open(textData.dataName(), textData.getColumns(), textData.tableRows(false, false));
            }
        } else {
            Data2D.open(textData);
        }
    }
}
