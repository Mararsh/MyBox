package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.TextClipboardTools;
import static mara.mybox.fxml.NodeTools.badStyle;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.MessageDigestTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-10-31
 * @License Apache License Version 2.0
 */
public class MessageDigestController extends BaseController {

    protected InputType inputType;
    protected String algorithm;
    protected Charset charset;
    protected byte[] digest;

    protected enum InputType {
        File, Input
    }

    @FXML
    protected ToggleGroup inputGroup, algorithmGroup, formatGroup;
    @FXML
    protected VBox handleBox;
    @FXML
    protected HBox fileBox, charsetBox;
    @FXML
    protected TextArea inputArea, resultArea;
    @FXML
    protected RadioButton base64Radio, hexRadio, fhexRadio;
    @FXML
    protected ComboBox<String> charsetSelector;

    public MessageDigestController() {
        baseTitle = Languages.message("MessageDigest");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            inputGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkInputType();
                }
            });
            checkInputType();

            algorithmGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkAlgorithm();
                }
            });
            checkAlgorithm();

            formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    display();
                }
            });

            List<String> setNames = TextTools.getCharsetNames();
            charsetSelector.getItems().addAll(setNames);
            try {
                charset = Charset.forName(UserConfig.getUserConfigString(baseName + "Charset", Charset.defaultCharset().name()));
            } catch (Exception e) {
                charset = Charset.defaultCharset();
            }
            charsetSelector.setValue(charset.name());
            charsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    charset = Charset.forName(charsetSelector.getSelectionModel().getSelectedItem());
                    UserConfig.setUserConfigString(baseName + "Charset", charset.name());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void checkInputType() {
        try {
            clear();
            String selected = ((RadioButton) inputGroup.getSelectedToggle()).getText();
            handleBox.getChildren().clear();
            if (Languages.message("File").equals(selected)) {
                handleBox.getChildren().addAll(fileBox);
                inputType = InputType.File;

            } else {
                handleBox.getChildren().addAll(charsetBox, inputArea);
                inputType = InputType.Input;
                sourceFileInput.setStyle(null);
            }

            refreshStyle(handleBox);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // https://docs.oracle.com/javase/10/docs/specs/security/standard-names.html#messagedigest-algorithms
    protected void checkAlgorithm() {
        try {
            clear();
            algorithm = ((RadioButton) algorithmGroup.getSelectedToggle()).getText();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void clear() {
        resultArea.clear();
        bottomLabel.setText("");
        digest = null;
    }

    protected void display() {
        if (digest == null) {
            resultArea.clear();
            return;
        }
        String result;
        if (base64Radio.isSelected()) {
            Base64.Encoder encoder = Base64.getEncoder();
            result = encoder.encodeToString(digest);
        } else if (fhexRadio.isSelected()) {
            result = ByteTools.bytesToHexFormat(digest);
        } else {
            result = ByteTools.bytesToHex(digest);
        }
        resultArea.setText(result);
    }

    @Override
    public void sourceFileChanged(final File file) {
        sourceFile = file;
        clear();
    }

    @FXML
    public void dmHelp() {
        try {
            String link;
            if (Languages.isChinese()) {
                link = "https://baike.baidu.com/item/%E6%95%B0%E5%AD%97%E6%91%98%E8%A6%81/4069118";
            } else {
                link = "https://en.wikipedia.org/wiki/Message_digest";
            }
            openLink(link);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        if (inputType == InputType.File) {
            if (sourceFile == null || badStyle.equals(sourceFileInput.getStyle())) {
                resultArea.clear();
                popError(Languages.message("InvalidData"));
                return;
            }
        } else {
            if (inputArea.getText().isEmpty()) {
                resultArea.clear();
                popError(Languages.message("InvalidData"));
                return;
            }
        }
        try {
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private long datalen;

                    @Override
                    protected boolean handle() {
                        try {
                            if (inputType == InputType.File) {
                                digest = MessageDigestTools.messageDigest(sourceFile, algorithm);
                                datalen = sourceFile.length();
                            } else {
                                byte[] data = inputArea.getText().getBytes(charset);
                                digest = MessageDigestTools.messageDigest(data, algorithm);
                                datalen = data.length;
                            }
                            return true;
                        } catch (Exception e) {
                            error = e.toString();
                            return false;
                        }
                    }

                    @Override
                    protected void whenSucceeded() {
                        display();
                        String s = MessageFormat.format(Languages.message("DigestResult"),
                                datalen, digest.length);
                        s += "  " + Languages.message("Cost") + ":" + DateTools.datetimeMsDuration(cost);
                        bottomLabel.setText(s);
                    }

                };
                handling(task);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    @Override
    public void copyAction() {
        TextClipboardTools.copyToSystemClipboard(myController, resultArea.getText());
    }

    @FXML
    @Override
    public void pasteAction() {
        String string = TextClipboardTools.getSystemClipboardString();
        if (string != null && !string.isBlank()) {
            inputArea.setText(string);
        }
    }

    @FXML
    @Override
    public void clearAction() {
        inputArea.clear();
    }

}
