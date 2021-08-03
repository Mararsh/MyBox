package mara.mybox.controller;

import java.text.MessageFormat;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.scheduledTasks;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:48:15
 * @License Apache License Version 2.0
 */
public class MyBoxController extends MyBoxController_About {

    public MyBoxController() {
        baseTitle = Languages.message("AppTitle");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            makeImagePopup();

            if (scheduledTasks != null && scheduledTasks.size() > 0) {
                bottomLabel.setText(MessageFormat.format(Languages.message("AlarmClocksRunning"), scheduledTasks.size()));
            }

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
