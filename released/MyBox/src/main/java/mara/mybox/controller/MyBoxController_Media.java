package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Media extends MyBoxController_Data {

    @FXML
    public void popMediaMenu(Event event) {
        if (UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true)) {
            showMediaMenu(event);
        }
    }

    @FXML
    protected void showMediaMenu(Event event) {
        MenuItem mediaPlayer = new MenuItem(Languages.message("MediaPlayer"));
        mediaPlayer.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MediaPlayerFxml);
        });

        MenuItem mediaLists = new MenuItem(Languages.message("ManageMediaLists"));
        mediaLists.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.MediaListFxml);
        });

        MenuItem FFmpegInformation = new MenuItem(Languages.message("FFmpegInformation"));
        FFmpegInformation.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FFmpegInformationFxml);
        });

        MenuItem FFprobe = new MenuItem(Languages.message("FFmpegProbeMediaInformation"));
        FFprobe.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FFmpegProbeMediaInformationFxml);
        });

        MenuItem FFmpegConversionFiles = new MenuItem(Languages.message("FFmpegConvertMediaFiles"));
        FFmpegConversionFiles.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FFmpegConvertMediaFilesFxml);
        });

        MenuItem FFmpegConversionStreams = new MenuItem(Languages.message("FFmpegConvertMediaStreams"));
        FFmpegConversionStreams.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FFmpegConvertMediaStreamsFxml);
        });

        Menu FFmpegConversionMenu = new Menu(Languages.message("FFmpegConvertMedias"));
        FFmpegConversionMenu.getItems().addAll(
                FFmpegConversionFiles, FFmpegConversionStreams);

        MenuItem FFmpegMergeImages = new MenuItem(Languages.message("FFmpegMergeImagesInformation"));
        FFmpegMergeImages.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FFmpegMergeImagesFxml);
        });

        MenuItem FFmpegMergeImageFiles = new MenuItem(Languages.message("FFmpegMergeImagesFiles"));
        FFmpegMergeImageFiles.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FFmpegMergeImageFilesFxml);
        });

        MenuItem screenRecorder = new MenuItem(Languages.message("FFmpegScreenRecorder"));
        screenRecorder.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.FFmpegScreenRecorderFxml);
        });

        Menu FFmpegMergeMenu = new Menu(Languages.message("FFmpegMergeImages"));
        FFmpegMergeMenu.getItems().addAll(
                FFmpegMergeImageFiles, FFmpegMergeImages);

        MenuItem alarmClock = new MenuItem(Languages.message("AlarmClock"));
        alarmClock.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.AlarmClockFxml);
        });

        MenuItem GameElimniation = new MenuItem(Languages.message("GameElimniation"));
        GameElimniation.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.GameElimniationFxml);
        });

        MenuItem GameMine = new MenuItem(Languages.message("GameMine"));
        GameMine.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.GameMineFxml);
        });

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(mediaPlayer, mediaLists, new SeparatorMenuItem(),
                screenRecorder,
                FFmpegConversionMenu, FFmpegMergeMenu,
                FFprobe, FFmpegInformation, new SeparatorMenuItem(),
                //                alarmClock, new SeparatorMenuItem(),
                GameElimniation, GameMine));

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("MyBoxHomeMenuPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        popCenterMenu(mediaBox, items);
    }

}
