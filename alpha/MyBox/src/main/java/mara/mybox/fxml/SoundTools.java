package mara.mybox.fxml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javazoom.jl.player.Player;
import mara.mybox.data.BaseTask;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.value.AppValues;


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
        BaseTask miaoTask = new BaseTask<Void>() {
            @Override
            protected boolean handle() {
                try {
                    File sound = FxFileTools.getInternalFile(file, "sound", userFile);
                    FloatControl control = SoundTools.getControl(sound);
                    Clip player = SoundTools.playback(sound, control.getMaximum() * 0.6F);
                    player.start();
                } catch (Exception e) {
                }
                return true;
            }
        };
        Thread thread = new Thread(miaoTask);
        thread.setDaemon(false);
        thread.start();
    }

    public static void playClip(final File file) {
        BaseTask miaoTask = new BaseTask<Void>() {
            @Override
            protected boolean handle() {
                try {
                    FloatControl control = SoundTools.getControl(file);
                    Clip player = SoundTools.playback(file, control.getMaximum() * 0.6F);
                    player.start();
                } catch (Exception e) {
                }
                return true;
            }
        };
        Thread thread = new Thread(miaoTask);
        thread.setDaemon(false);
        thread.start();
    }

    public static void miao7() {
        playClip("/sound/guaiMiao7.mp3", "guaiMiao7.mp3");
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

    public void setGain(float ctrl) {
        try {
            Mixer.Info[] infos = AudioSystem.getMixerInfo();
            for (Mixer.Info info : infos) {
                Mixer mixer = AudioSystem.getMixer(info);
                if (mixer.isLineSupported(Port.Info.SPEAKER)) {
                    try ( Port port = (Port) mixer.getLine(Port.Info.SPEAKER)) {
                        port.open();
                        if (port.isControlSupported(FloatControl.Type.VOLUME)) {
                            FloatControl volume = (FloatControl) port.getControl(FloatControl.Type.VOLUME);
                            volume.setValue(ctrl);
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    public static synchronized Clip playback(String name, float addVolume) {
        File file = new File(name);
        if (file.isFile() && file.exists()) {
            return playback(file, addVolume);
        }
        try {
            URL url = new URL(name);
            return playback(url, addVolume);
        } catch (Exception e) {
            return null;
        }

    }

    public static synchronized Clip playback(File file, float addVolume) {
        try {
            try ( AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
                return playback(in, addVolume);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static synchronized Clip playback(URL url, float addVolume) {
        try {
            try ( AudioInputStream in = AudioSystem.getAudioInputStream(url)) {
                return playback(in, addVolume);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    // This can work for both mp3 and wav
    public static synchronized Clip playback(AudioInputStream in, float addVolume) {
        try {
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
            AudioInputStream ain = AudioSystem.getAudioInputStream(decodedFormat, in);
            DataLine.Info info = new DataLine.Info(Clip.class, decodedFormat);

            // https://stackoverflow.com/questions/25564980/java-use-a-clip-and-a-try-with-resources-block-which-results-with-no-sound
            final Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(ain);
            // Input stream must be closed, or else some thread is still running when application is exited.
            in.close();
            ain.close();
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(addVolume); // Add volume by decibels.
            return clip;

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static FloatControl getControl(File file) {
        try {
            try ( AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
                return getControl(in);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static FloatControl getControl(URL url) {
        try {
            try ( AudioInputStream in = AudioSystem.getAudioInputStream(url)) {
                return getControl(in);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static FloatControl getControl(AudioInputStream in) {
        try {

            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
            AudioInputStream ain = AudioSystem.getAudioInputStream(decodedFormat, in);
            DataLine.Info info = new DataLine.Info(Clip.class, decodedFormat);
            try ( Clip clip = (Clip) AudioSystem.getLine(info)) {
                clip.open(ain);
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                return gainControl;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static void playBigAudio(File file) {

        try {
            if (file.getName().endsWith(".mp3")) {
                getMp3Player(file);
                return;
            }

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            AudioFormat aif = audioInputStream.getFormat();
            final SourceDataLine sdl;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, aif);
            sdl = (SourceDataLine) AudioSystem.getLine(info);
            if (!AudioSystem.isLineSupported(info)) {
                MyBoxLog.error("no support for " + aif.toString());
            }

            FloatControl fc = (FloatControl) sdl.getControl(FloatControl.Type.MASTER_GAIN);
            //value可以用来设置音量，从0-2.0
            double value = 2;
            float dB = (float) (Math.log(value == 0.0 ? 0.0001 : value) / Math.log(10.0) * 20.0);
            fc.setValue(dB);
            int nByte = 0;
            byte[] buffer = new byte[AppValues.IOBufferLength];
            while ((nByte = audioInputStream.read(buffer, 0, AppValues.IOBufferLength)) > 0) {
                sdl.write(buffer, 0, nByte);
            }
            sdl.stop();
        } catch (Exception e) {

        }
    }

    public static Clip playSmallAudio(File file) {

        try {

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            AudioFormat aif = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, aif);
            try ( Clip clip = (Clip) AudioSystem.getLine(info)) {
                clip.addLineListener(new LineListener() {
                    @Override
                    public void update(LineEvent e) {
                        if (e.getType() == LineEvent.Type.STOP) {
                            synchronized (clip) {
                                clip.notify();
                            }
                        }
                    }
                });
                clip.open(audioInputStream);

                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.getValue();
                double value = 2;
                float dB = (float) (Math.log(value == 0.0 ? 0.0001 : value) / Math.log(10.0) * 20.0);
                gainControl.setValue(dB);
                gainControl.setValue(-10.0f); // Reduce volume by 10 decibels.
                clip.start();
                synchronized (clip) {
                    clip.wait();
                }
                return clip;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static Player getMp3Player(File file) {
        try {
            BufferedInputStream buffer = new BufferedInputStream(new FileInputStream(file));
            Player player = new Player(buffer);
//            player.play();
            return player;
        } catch (Exception e) {
            return null;
        }
    }

    // http://www.javazoom.net/mp3spi/documents.html
    public static void testPlay(String filename) {
        try {
            File file = new File(filename);
            AudioInputStream in = AudioSystem.getAudioInputStream(file);
            AudioInputStream din = null;
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
            din = AudioSystem.getAudioInputStream(decodedFormat, in);
            // Play now.
            rawplay(decodedFormat, din);
            in.close();
        } catch (Exception e) {
            //Handle exception.
        }
    }

    public static void rawplay(AudioFormat targetFormat, AudioInputStream din)
            throws IOException, LineUnavailableException {
        byte[] data = new byte[AppValues.IOBufferLength];
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(targetFormat);

        if (line != null) {
            // Start
            FloatControl vol = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
            MyBoxLog.debug(vol.getValue() + vol.getUnits());
            line.start();
            int nBytesRead = 0, nBytesWritten;
            while ((nBytesRead = din.read(data, 0, data.length)) > 0) {
                nBytesWritten = line.write(data, 0, nBytesRead);
            }
            // Stop
            line.drain();
            line.stop();
            line.close();
            din.close();
        }
    }

    public static SourceDataLine getLine(AudioFormat audioFormat) throws
            LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        SourceDataLine res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }

}
