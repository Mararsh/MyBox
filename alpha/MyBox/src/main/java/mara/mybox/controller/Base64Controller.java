package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.ByteFileTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-14
 * @License Apache License Version 2.0
 */
public class Base64Controller extends BaseController {

    protected Charset charset;

    @FXML
    protected ToggleGroup convertGroup;
    @FXML
    protected VBox inputBox, textBox;
    @FXML
    protected HBox fileBox, charsetBox;
    @FXML
    protected TextArea inputArea, resultArea;
    @FXML
    protected RadioButton fileRadio, textRadio, base64FileRadio, base64TextRadio;
    @FXML
    protected Label resultLabel;
    @FXML
    protected Button txtButton;
    @FXML
    protected ComboBox<String> charsetSelector;

    public Base64Controller() {
        baseTitle = message("Base64Conversion");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            convertGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkConvert();
                }
            });

            List<String> setNames = TextTools.getCharsetNames();
            charsetSelector.getItems().addAll(setNames);
            try {
                charset = Charset.forName(UserConfig.getString(baseName + "Charset", Charset.defaultCharset().name()));
            } catch (Exception e) {
                charset = Charset.defaultCharset();
            }
            charsetSelector.setValue(charset.name());
            charsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    charset = Charset.forName(charsetSelector.getSelectionModel().getSelectedItem());
                    UserConfig.setString(baseName + "Charset", charset.name());
                }
            });

            checkConvert();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    protected void checkConvert() {
        try {
            clear();
            inputBox.getChildren().clear();
            resultArea.setVisible(false);
            copyButton.setVisible(false);
            sourceFileInput.clear();
            sourceFileInput.setStyle(null);
            sourceFile = null;
            if (base64FileRadio.isSelected() || fileRadio.isSelected()) {
                inputBox.getChildren().addAll(fileBox);

            } else {
                inputBox.getChildren().addAll(textBox);

            }
            refreshStyle(inputBox);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void clear() {
        resultArea.clear();
        bottomLabel.setText("");
    }

    @Override
    public void sourceFileChanged(final File file) {
        super.sourceFileChanged(file);
        clear();
    }

    @FXML
    public void txtAction() {
        if (textRadio.isSelected() || base64TextRadio.isSelected()) {
            if (inputArea.getText().isEmpty()) {
                popError(message("NoData"));
                return;
            }
        } else if (sourceFile == null || UserConfig.badStyle().equals(sourceFileInput.getStyle())) {
            popError(message("NoData"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private long bytesLen;
            private String results;

            @Override
            protected boolean handle() {
                try {
                    if (textRadio.isSelected()) {
                        Base64.Encoder encoder = Base64.getEncoder();
                        byte[] bytes = inputArea.getText().getBytes(charset);
                        bytesLen = bytes.length;
                        results = encoder.encodeToString(bytes);

                    } else if (fileRadio.isSelected()) {
                        Base64.Encoder encoder = Base64.getEncoder();
                        byte[] bytes = ByteFileTools.readBytes(sourceFile);
                        bytesLen = bytes.length;
                        results = encoder.encodeToString(bytes);

                    } else if (base64TextRadio.isSelected()) {
                        Base64.Decoder decoder = Base64.getDecoder();
                        byte[] bytes = decoder.decode(inputArea.getText());
                        bytesLen = bytes.length;
                        results = new String(bytes, charset);

                    } else if (base64FileRadio.isSelected()) {
                        Base64.Decoder decoder = Base64.getDecoder();
                        String s = TextFileTools.readTexts(this, sourceFile);
                        if (s == null || !task.isWorking()) {
                            return false;
                        }
                        byte[] bytes = decoder.decode(s);
                        bytesLen = bytes.length;
                        results = new String(bytes, charset);
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                resultArea.setVisible(true);
                resultArea.setText(results);
                copyButton.setVisible(true);
                String s = message("Bytes") + ": " + StringTools.format(bytesLen)
                        + "  " + message("Cost") + ":" + DateTools.datetimeMsDuration(cost);
                bottomLabel.setText(s);
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void saveAction() {
        saveAsAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (task != null && !task.isQuit()) {
            return;
        }
        if (textRadio.isSelected() || base64TextRadio.isSelected()) {
            if (inputArea.getText().isEmpty()) {
                popError(message("NoData"));
                return;
            }
        } else if (sourceFile == null || UserConfig.badStyle().equals(sourceFileInput.getStyle())) {
            popError(message("NoData"));
            return;
        }
        File file;
        if (textRadio.isSelected()) {
            file = chooseSaveFile(VisitHistory.FileType.Text, "encodeBase64.txt");
        } else if (fileRadio.isSelected()) {
            file = chooseSaveFile(VisitHistory.FileType.Text, sourceFile.getName() + "-encodeBase64.txt");
        } else if (base64FileRadio.isSelected()) {
            file = chooseSaveFile(VisitHistory.FileType.All, sourceFile.getName() + "-decodeBase64");
        } else {
            file = chooseSaveFile(VisitHistory.FileType.All, "decodeBase64");
        }
        if (file == null) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            private long bytesLen;

            @Override
            protected boolean handle() {
                try {
                    if (textRadio.isSelected()) {
                        Base64.Encoder encoder = Base64.getEncoder();
                        byte[] bytes = inputArea.getText().getBytes(charset);
                        bytesLen = bytes.length;
                        String results = encoder.encodeToString(bytes);
                        if (TextFileTools.writeFile(file, results) != null) {
                            recordFileWritten(file);
                            return true;
                        } else {
                            return false;
                        }
                    } else if (fileRadio.isSelected()) {
                        Base64.Encoder encoder = Base64.getEncoder();
                        byte[] bytes = ByteFileTools.readBytes(sourceFile);
                        bytesLen = bytes.length;
                        String results = encoder.encodeToString(bytes);
                        if (TextFileTools.writeFile(file, results) != null) {
                            recordFileWritten(file);
                            return true;
                        } else {
                            return false;
                        }

                    } else if (base64TextRadio.isSelected()) {
                        Base64.Decoder decoder = Base64.getDecoder();
                        byte[] bytes = decoder.decode(inputArea.getText());
                        bytesLen = bytes.length;
                        if (ByteFileTools.writeFile(file, bytes) != null) {
                            recordFileWritten(file);
                            return true;
                        } else {
                            return false;
                        }

                    } else if (base64FileRadio.isSelected()) {
                        Base64.Decoder decoder = Base64.getDecoder();
                        String s = TextFileTools.readTexts(this, sourceFile);
                        if (s == null || !task.isWorking()) {
                            return false;
                        }
                        byte[] bytes = decoder.decode(s);
                        bytesLen = bytes.length;
                        if (ByteFileTools.writeFile(file, bytes) != null) {
                            recordFileWritten(file);
                            return true;
                        } else {
                            return false;
                        }
                    }
                    return false;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                resultArea.setVisible(false);
                resultArea.clear();
                copyButton.setVisible(false);
                String s = message("Bytes") + ": " + StringTools.format(bytesLen)
                        + "  " + message("Cost") + ":" + DateTools.datetimeMsDuration(cost);
                bottomLabel.setText(s);
                popSuccessful();
                if (textRadio.isSelected() || fileRadio.isSelected()) {
                    TextEditorController.open(file);
                } else {
                    browseURI(file.getParentFile().toURI());
                }
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void clearAction() {
        inputArea.clear();
    }

    @FXML
    @Override
    public void copyAction() {
        TextClipboardTools.copyToSystemClipboard(this, resultArea.getText());
    }

    @FXML
    @Override
    public void pasteAction() {
        String string = TextClipboardTools.getSystemClipboardString();
        if (string != null && !string.isBlank()) {
            inputArea.setText(string);
        }
    }

}
