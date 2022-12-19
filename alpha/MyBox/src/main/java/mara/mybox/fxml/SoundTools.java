package mara.mybox.fxml;

import java.io.File;
import java.util.Arrays;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-7-12
 * @License Apache License Version 2.0
 */
public class SoundTools {

    public static void audioSystem() {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : infos) {
            MyBoxLog.debug(info.getName() + " " + info.getVendor() + " " + info.getVersion() + " " + info.getDescription());
        }
        AudioFileFormat.Type[] formats = AudioSystem.getAudioFileTypes();
        MyBoxLog.debug(Arrays.asList(formats).toString());
    }

    public static void miao6() {
        playClip("/sound/guaiMiao6.mp3", "guaiMiao6.mp3");
    }

    public static void GuaiAO() {
        playClip("/sound/GuaiAO.mp3", "GuaiAO.mp3");
    }

    public static void mp3(File file) {
        playClip(file);
    }

    public static void miao5() {
        playClip("/sound/guaiMiao5.mp3", "guaiMiao5.mp3");
    }

    public static void miao3() {
        playClip("/sound/guaiMiao3.mp3", "guaiMiao3.mp3");
    }

    public static void miao2() {
        playClip("/sound/guaiMiao2.mp3", "guaiMiao2.mp3");
    }

    public static void miao7() {
        playClip("/sound/guaiMiao7.mp3", "guaiMiao7.mp3");
    }

    public static void BenWu() {
        playClip("/sound/BenWu.mp3", "BenWu.mp3");
    }

    public static void BenWu2() {
        playClip("/sound/BenWu2.mp3", "BenWu2.mp3");
    }

    public static void playSound(final String file, final String userFile) {
        File miao = FxFileTools.getInternalFile(file, "sound", userFile);
        play(miao, 1, 1);
    }

    public static void playClip(final String file, final String userFile) {
        File sound = FxFileTools.getInternalFile(file, "sound", userFile);
        playClip(sound);
    }

    public static void playClip(File file) {
        BaseTask miaoTask = new BaseTask<Void>() {
            @Override
            protected boolean handle() {
                try {
                    AudioClip plonkSound = new AudioClip(file.toURI().toString());
                    plonkSound.play();
                } catch (Exception e) {
                }
                return true;
            }
        };
        Thread thread = new Thread(miaoTask);
        thread.setDaemon(false);
        thread.start();
    }

    public static MediaPlayer play(File file, double volumn, int cycle) {
        return play(file.toURI().toString(), volumn, cycle);
    }

    public static MediaPlayer play(String address, double volumn, int cycle) {
        MediaPlayer mp = new MediaPlayer(new Media(address));
        mp.setVolume(volumn);
        mp.setCycleCount(cycle);
        mp.setAutoPlay(true);
        return mp;
    }

    public static void audio(File file) {
        audio(file, 1, 1);
    }

    public static void audio(File file, double volumn, int cycle) {
        AudioClip clip = new AudioClip(file.toURI().toString());
        clip.setVolume(volumn);
        clip.setCycleCount(cycle);
        clip.play();
    }

    public static AudioClip clip(File file, double volumn) {
        AudioClip clip = new AudioClip(file.toURI().toString());
        clip.setVolume(volumn);
        return clip;
    }

}
