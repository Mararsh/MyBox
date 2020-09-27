package mara.mybox.controller;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import mara.mybox.db.DerbyBase;
import mara.mybox.tools.ConfigTools;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-11 8:14:06
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBoxLoadingController implements Initializable {

    private Stage stage;
    private String lang;
    private boolean isSettingValues;

    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label infoLabel;
    @FXML
    private ImageView imageView;
    @FXML
    protected ToggleGroup derbyGroup;
    @FXML
    protected RadioButton embeddedRadio, networkRadio;
    @FXML
    protected HBox derbyBox;

    public MyBoxLoadingController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {

            derbyBox.setVisible(false);

            infoLabel.requestFocus();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void pathReady() {
        try {
            lang = Locale.getDefault().getLanguage().toLowerCase();
            infoLabel.setText(message(lang, "Initializing..."));
            networkRadio.setText(message(lang, "NetworkModeOnlyLocal"));
            embeddedRadio.setText(message(lang, "EmbeddedMode"));
            derbyBox.setVisible(true);

            isSettingValues = true;
            DerbyBase.mode = DerbyBase.readMode();
            if (DerbyBase.mode != null && "client".equals(DerbyBase.mode.toLowerCase())) {
                networkRadio.setSelected(true);
            } else {
                embeddedRadio.setSelected(true);
            }
            isSettingValues = false;

            derbyGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle old_val, Toggle new_val) {
                    checkDerbyMode();
                }
            });

            infoLabel.requestFocus();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkDerbyMode() {
        if (isSettingValues) {
            return;
        }
        DerbyBase.mode = networkRadio.isSelected() ? "client" : "embedded";
        ConfigTools.writeConfigValue("DerbyMode", DerbyBase.mode);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                derbyBox.setDisable(true);
                try {
                    String ret = DerbyBase.startDerby();
                    if (ret != null) {
                        isSettingValues = true;
                        if (DerbyBase.mode != null && "client".equals(DerbyBase.mode.toLowerCase())) {
                            networkRadio.setSelected(true);
                        } else {
                            embeddedRadio.setSelected(true);
                        }
                        isSettingValues = false;
                    }
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                derbyBox.setDisable(false);
            }
        });
    }

    public void setInfo(String info) {
        infoLabel.setText(info);
    }

    public void setImage(Image image) {
        imageView.setImage(image);
    }

    public void close() {
        stage.close();
    }

    public void setProgress(float value) {
        progressIndicator.setProgress(value);
    }

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public void setProgressIndicator(ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    public Label getInfoLabel() {
        return infoLabel;
    }

    public void setInfoLabel(Label infoLabel) {
        this.infoLabel = infoLabel;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

}
