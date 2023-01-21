package mara.mybox.controller;

import java.text.MessageFormat;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.AppVariables.scheduledTasks;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-12
 * @License Apache License Version 2.0
 */
public class MyBoxController extends MyBoxController_About {

    @FXML
    protected Label titleLabel;

    public MyBoxController() {
        baseTitle = message("AppTitle") + " v" + AppValues.AppVersion;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            titleLabel.setText(baseTitle);

            makeImagePopup();
            if (scheduledTasks != null && !scheduledTasks.isEmpty()) {
                bottomLabel.setText(MessageFormat.format(message("AlarmClocksRunning"), scheduledTasks.size()));
            }

            Platform.runLater(() -> {
                WindowTools.clearInvalidData();
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    private void makeImagePopup() {
        try {
            imagePop = new Popup();
            imagePop.setWidth(650);
            imagePop.setHeight(650);

            VBox vbox = new VBox();
            VBox.setVgrow(vbox, Priority.ALWAYS);
            HBox.setHgrow(vbox, Priority.ALWAYS);
            vbox.setMaxWidth(Double.MAX_VALUE);
            vbox.setMaxHeight(Double.MAX_VALUE);
            vbox.setStyle("-fx-background-color: white;");
            imagePop.getContent().add(vbox);

            view = new ImageView();
            view.setFitWidth(500);
            view.setFitHeight(500);
            vbox.getChildren().add(view);

            text = new Text();
            text.setStyle("-fx-font-size: 1.2em;");

            vbox.getChildren().add(text);
            vbox.setPadding(new Insets(15, 15, 15, 15));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

}
