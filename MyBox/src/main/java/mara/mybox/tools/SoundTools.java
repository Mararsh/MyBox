package mara.mybox.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
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
import static mara.mybox.objects.AppVaribles.logger;


/**
 * @Author Mara
 * @CreateDate 2018-7-12
 * @Description
 * @License Apache License Version 2.0
 */
public class SoundTools {

   

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
            try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
                return playback(in, addVolume);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static synchronized Clip playback(URL url, float addVolume) {
        try {
            try (AudioInputStream in = AudioSystem.getAudioInputStream(url)) {
                return playback(in, addVolume);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
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
            logger.debug(e.toString());
            return null;
        }
    }

    public static FloatControl getControl(File file) {
        try {
            try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
                return getControl(in);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
            return null;
        }
    }

    public static FloatControl getControl(URL url) {
        try {
            try (AudioInputStream in = AudioSystem.getAudioInputStream(url)) {
                return getControl(in);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
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
            try (Clip clip = (Clip) AudioSystem.getLine(info)) {
                clip.open(ain);
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                return gainControl;
            }
        } catch (Exception e) {
            logger.debug(e.toString());
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
            //System.out.println(aif);
            final SourceDataLine sdl;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, aif);
            sdl = (SourceDataLine) AudioSystem.getLine(info);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.print("no support for " + aif.toString());
            }

            FloatControl fc = (FloatControl) sdl.getControl(FloatControl.Type.MASTER_GAIN);
            //value可以用来设置音量，从0-2.0
            double value = 2;
            float dB = (float) (Math.log(value == 0.0 ? 0.0001 : value) / Math.log(10.0) * 20.0);
            fc.setValue(dB);
            int nByte = 0;
            int writeByte = 0;
            final int SIZE = 1024 * 64;
            byte[] buffer = new byte[SIZE];
            while (nByte != -1) {
                nByte = audioInputStream.read(buffer, 0, SIZE);
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
            //System.out.println(aif);
            DataLine.Info info = new DataLine.Info(Clip.class, aif);
            try (Clip clip = (Clip) AudioSystem.getLine(info)) {
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

    public static void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException, LineUnavailableException {
        byte[] data = new byte[4096];
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(targetFormat);

        if (line != null) {
            // Start
            FloatControl vol = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
            logger.debug(vol.getValue() + vol.getUnits());
            line.start();
            int nBytesRead = 0, nBytesWritten = 0;
            while (nBytesRead != -1) {
                nBytesRead = din.read(data, 0, data.length);
                if (nBytesRead != -1) {
                    nBytesWritten = line.write(data, 0, nBytesRead);
                }
            }
            // Stop
            line.drain();
            line.stop();
            line.close();
            din.close();
        }
    }

    public static SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        SourceDataLine res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    }

    public void setGain(float ctrl) {
        try {
            Mixer.Info[] infos = AudioSystem.getMixerInfo();
            for (Mixer.Info info : infos) {
                Mixer mixer = AudioSystem.getMixer(info);
                if (mixer.isLineSupported(Port.Info.SPEAKER)) {
                    try (Port port = (Port) mixer.getLine(Port.Info.SPEAKER)) {
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

}
