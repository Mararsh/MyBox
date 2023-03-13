package mara.mybox.controller;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import mara.mybox.MainApp;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.ConfigTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-11-7
 * @License Apache License Version 2.0
 */
public class MyBoxSetupController implements Initializable {

    protected Stage myStage;
    protected Scene myScene;
    protected String lang;
    protected File configPath;
    protected final String badStyle = "-fx-text-box-border: blue;   -fx-text-fill:blue; ";

    @FXML
    protected Pane thisPane;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected Label titleLabel, fileLabel;
    @FXML
    protected ListView<String> listView;
    @FXML
    protected TextField dataDirInput;
    @FXML
    protected Button openPathButton, okButton;

    public MyBoxSetupController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            lang = Locale.getDefault().getLanguage().toLowerCase();
            if (AppVariables.MyboxConfigFile == null) {
                AppVariables.MyboxConfigFile = ConfigTools.defaultConfigFile();
            }

            makeListView();

            makeEditBox();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void makeListView() {
        try {
            configPath = new File(System.getProperty("user.home") + File.separator + "mybox");
            if (!configPath.isDirectory()) {
                FileDeleteTools.delete(configPath);
            }
            if (!configPath.exists()) {
                configPath.mkdirs();
            }
            listView.getItems().add(0, Languages.message(lang, "Default"));
            for (File file : configPath.listFiles()) {
                String fname = file.getName().toLowerCase();
                if (!file.isFile() || !fname.startsWith("mybox") || !fname.endsWith(".ini")) {
                    continue;
                }
                listView.getItems().add(file.getName());
            }
            listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String name) {
                    try {
                        if (name == null || name.isBlank()) {
                            return;
                        }
                        if (Languages.message(lang, "Default").equals(name)) {
                            dataDirInput.setText(configPath.getAbsolutePath() + File.separator + "data_v" + AppValues.AppVersion);
                        } else {
                            File file = new File(configPath + File.separator + name);
                            String MyBoxDataPath = ConfigTools.readValue(file, "MyBoxDataPath");
                            if (MyBoxDataPath != null) {
                                dataDirInput.setText(MyBoxDataPath);
                            }
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                    }
                }
            });
            listView.getSelectionModel().select(0);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void makeEditBox() {
        try {
            titleLabel.setText("MyBox v" + AppValues.AppVersion);
            fileLabel.setText(AppVariables.MyboxConfigFile.getAbsolutePath());

            okButton.disableProperty().bind(
                    dataDirInput.textProperty().isEmpty()
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public Stage getMyStage() {
        if (myStage == null) {
            if (thisPane != null) {
                myScene = thisPane.getScene();
                if (myScene != null) {
                    myStage = (Stage) myScene.getWindow();
                    myStage.setUserData(this);
                }
            }
        }
        return myStage;
    }

    @FXML
    protected void selectDataPath(ActionEvent event) {
        try {
            String defaultPath = AppVariables.MyboxDataPath != null
                    ? AppVariables.MyboxDataPath : ConfigTools.defaultDataPath();
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setInitialDirectory(new File(defaultPath));
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            dataDirInput.setText(directory.getPath());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void openDataPath(ActionEvent event) {
        PopTools.browseURI(null, configPath.toURI());
    }

    @FXML
    protected void okAction(ActionEvent event) {
        try {
            File dataPath = new File(dataDirInput.getText());
            if (!dataPath.exists()) {
                dataPath.mkdirs();
            } else if (!dataPath.isDirectory()) {
                FileDeleteTools.delete(dataPath);
                dataPath.mkdirs();
            }
            if (dataPath.exists() && dataPath.isDirectory()) {
                AppVariables.MyboxDataPath = dataPath.getAbsolutePath();
                ConfigTools.writeConfigValue("MyBoxDataPath", AppVariables.MyboxDataPath);
            } else {
                PopTools.alertError(null, MessageFormat.format(Languages.message(lang, "UserPathFail"), dataPath));
                return;
            }
            MainApp.MyBoxLoading(getMyStage());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
