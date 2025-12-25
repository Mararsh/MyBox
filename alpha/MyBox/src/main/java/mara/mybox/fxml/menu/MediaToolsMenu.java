package mara.mybox.fxml.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2025-12-9
 * @License Apache License Version 2.0
 */
public class MediaToolsMenu {

    public static List<MenuItem> menusList(BaseController controller) {
        MenuItem mediaPlayer = new MenuItem(Languages.message("MediaPlayer"));
        mediaPlayer.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.MediaPlayerFxml);
        });

        MenuItem mediaLists = new MenuItem(Languages.message("ManageMediaLists"));
        mediaLists.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.MediaListFxml);
        });

        MenuItem FFmpegInformation = new MenuItem(Languages.message("FFmpegInformation"));
        FFmpegInformation.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FFmpegInformationFxml);
        });

        MenuItem FFprobe = new MenuItem(Languages.message("FFmpegProbeMediaInformation"));
        FFprobe.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FFmpegProbeMediaInformationFxml);
        });

        MenuItem FFmpegConversionFiles = new MenuItem(Languages.message("FFmpegConvertMediaFiles"));
        FFmpegConversionFiles.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FFmpegConvertMediaFilesFxml);
        });

        MenuItem FFmpegConversionStreams = new MenuItem(Languages.message("FFmpegConvertMediaStreams"));
        FFmpegConversionStreams.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FFmpegConvertMediaStreamsFxml);
        });

        Menu FFmpegConversionMenu = new Menu(Languages.message("FFmpegConvertMedias"));
        FFmpegConversionMenu.getItems().addAll(
                FFmpegConversionFiles, FFmpegConversionStreams);

        MenuItem FFmpegMergeImages = new MenuItem(Languages.message("FFmpegMergeImagesInformation"));
        FFmpegMergeImages.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FFmpegMergeImagesFxml);
        });

        MenuItem FFmpegMergeImageFiles = new MenuItem(Languages.message("FFmpegMergeImagesFiles"));
        FFmpegMergeImageFiles.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FFmpegMergeImageFilesFxml);
        });

        MenuItem screenRecorder = new MenuItem(Languages.message("FFmpegScreenRecorder"));
        screenRecorder.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.FFmpegScreenRecorderFxml);
        });

        Menu FFmpegMergeMenu = new Menu(Languages.message("FFmpegMergeImages"));
        FFmpegMergeMenu.getItems().addAll(
                FFmpegMergeImageFiles, FFmpegMergeImages);

        MenuItem alarmClock = new MenuItem(Languages.message("AlarmClock"));
        alarmClock.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.AlarmClockFxml);
        });

        MenuItem GameElimniation = new MenuItem(Languages.message("GameElimniation"));
        GameElimniation.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.GameElimniationFxml);
        });

        MenuItem GameMine = new MenuItem(Languages.message("GameMine"));
        GameMine.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.GameMineFxml);
        });

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(mediaPlayer, mediaLists, new SeparatorMenuItem(),
                screenRecorder,
                FFmpegConversionMenu, FFmpegMergeMenu,
                FFprobe, FFmpegInformation, new SeparatorMenuItem(),
                //                alarmClock, new SeparatorMenuItem(),
                GameElimniation, GameMine));

        return items;
    }

}
