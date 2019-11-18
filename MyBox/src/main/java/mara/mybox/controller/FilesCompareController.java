package mara.mybox.controller;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import mara.mybox.data.VisitHistory;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-10-31
 * @License Apache License Version 2.0
 */
public class FilesCompareController extends BaseController {

    protected File file1, file2;
    protected String algorithm;
    protected byte[] digest;

    protected enum Algorithm {
        MD5, SHA1, SHA256
    }

    @FXML
    protected ToggleGroup algorithmGroup;
    @FXML
    protected VBox inputBox;
    @FXML
    protected HBox fileBox;
    @FXML
    protected TextField file1Input, file2Input;
    @FXML
    protected TextArea resultArea;
    @FXML
    protected Button selectFile1Button, selectFile2Button;

    public FilesCompareController() {
        baseTitle = AppVariables.message("FilesCompare");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            file1Input.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    String v = file1Input.getText();
                    if (v == null || v.isEmpty()) {
                        file1Input.setStyle(badStyle);
                        return;
                    }
                    final File file = new File(v);
                    if (!file.exists()) {
                        file1Input.setStyle(badStyle);
                        return;
                    }
                    file1 = file;
                    file1Input.setStyle(null);
                    recordFileOpened(file);
                }
            });

            file2Input.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    String v = file2Input.getText();
                    if (v == null || v.isEmpty()) {
                        file2Input.setStyle(badStyle);
                        return;
                    }
                    final File file = new File(v);
                    if (!file.exists()) {
                        file2Input.setStyle(badStyle);
                        return;
                    }
                    file2 = file;
                    file2Input.setStyle(null);
                    recordFileOpened(file);
                }
            });

            algorithmGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkAlgorithm();
                }
            });
            checkAlgorithm();

            startButton.disableProperty().bind(
                    Bindings.isEmpty(file1Input.textProperty())
                            .or(file1Input.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(file2Input.textProperty()))
                            .or(file2Input.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void checkAlgorithm() {
        try {
            clear();
            String selected = ((RadioButton) algorithmGroup.getSelectedToggle()).getText();
            switch (selected) {
                case "SHA1":
                    algorithm = "SHA-1";
                    break;
                case "SHA256":
                    algorithm = "SHA-256";
                    break;
                default:
                    algorithm = "MD5";
                    break;
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void selectFile1() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVariables.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(sourceExtensionFilter);
            File file = fileChooser.showOpenDialog(myStage);
            if (file == null || !file.exists()) {
                return;
            }
            file1Input.setText(file.getAbsolutePath());
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    public void selectFile2() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVariables.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(sourceExtensionFilter);
            File file = fileChooser.showOpenDialog(myStage);
            if (file == null || !file.exists()) {
                return;
            }
            file2Input.setText(file.getAbsolutePath());
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    private void clear() {
        resultArea.clear();
        bottomLabel.setText("");
        digest = null;
    }

    @FXML
    public void dmHelp() {
        try {
            String link;
            switch (AppVariables.getLanguage()) {
                case "zh":
                    link = "https://baike.baidu.com/item/%E6%95%B0%E5%AD%97%E6%91%98%E8%A6%81/4069118";
                    break;
                default:
                    link = "https://en.wikipedia.org/wiki/Message_digest";
            }
            browseURI(new URI(link));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void popFile1(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return recentSourceFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePathsBesidesFiles();
            }

            @Override
            public void handleSelect() {
                selectFile1();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    selectFile1();
                    return;
                }
                file1Input.setText(file.getAbsolutePath());
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

        }.pop();
    }

    @FXML
    public void popFile2(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return recentSourceFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePathsBesidesFiles();
            }

            @Override
            public void handleSelect() {
                selectFile2();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    selectFile2();
                    return;
                }
                file2Input.setText(file.getAbsolutePath());
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

        }.pop();
    }

    @FXML
    @Override
    public void startAction() {
        if (file1 == null || file2 == null) {
            clear();
            popError(message("InvalidData"));
            return;
        }
        if (file1.length() != file2.length()) {
            String s = message("Different") + "\n\n"
                    + message("File") + " 1: " + message("Length") + " " + file1.length() + "\n"
                    + message("File") + " 2: " + message("Length") + " " + file2.length();
            resultArea.setText(s);
            return;
        }
        try {
            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private byte[] digest1, digest2;
                    private boolean same;

                    @Override
                    protected boolean handle() {
                        try {
                            digest1 = SystemTools.messageDigest(file1, algorithm);
                            digest2 = SystemTools.messageDigest(file2, algorithm);
                            same = Arrays.equals(digest1, digest2);
                            return true;
                        } catch (Exception e) {
                            error = e.toString();
                            return false;
                        }
                    }

                    @Override
                    protected void whenSucceeded() {
                        String s = (same ? message("Same") : message("Different")) + "\n"
                                + message("Cost") + ":" + DateTools.showTime(cost) + "\n\n"
                                + message("File") + " 1: \n"
                                + MessageFormat.format(message("DigestResult"),
                                        file1.length(), digest1.length) + "\n"
                                + ByteTools.bytesToHexFormat(digest1) + "\n\n"
                                + message("File") + " 2: \n"
                                + MessageFormat.format(message("DigestResult"),
                                        file2.length(), digest2.length) + "\n"
                                + ByteTools.bytesToHexFormat(digest2);
                        resultArea.setText(s);
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

}
