package com.darkprograms.speech.microphone;

import javax.sound.sampled.*;

import net.sourceforge.javaflacencoder.FLACFileWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/***************************************************************************
 * Microphone class that contains methods to capture audio from microphone
 *
 * @author Luke Kuza, Aaron Gokaslan
 ***************************************************************************/
public class Microphone extends MicrophoneAnalyzer implements Closeable {

    /**
     * Enum for current Microphone state
     */
    public enum CaptureState {
        PROCESSING_AUDIO, STARTING_CAPTURE, CLOSED;
    }

    /**
     * Variable for enum
     */
    CaptureState state;

    /**
     * Variable for the audios saved file type
     */
    private AudioFileFormat.Type fileType;

    /**
     * Variable that holds the saved audio file
     */
    private File audioFile;

    // Adddition - new params
    protected boolean running;
    private ByteArrayOutputStream out;
    private float level;
    private float frequency;
    // Adddition - new params

    /**
     * Constructor
     *
     * @param fileType File type to save the audio in<br>
     *                 Example, to save as WAVE use AudioFileFormat.Type.WAVE
     */
    public Microphone(AudioFileFormat.Type fileType) {
        setState(CaptureState.CLOSED);
        setFileType(fileType);
        initTargetDataLine();
    }

    /**
     * Gets the current state of Microphone
     *
     * @return PROCESSING_AUDIO is returned when the Thread is recording Audio
     *         and/or saving it to a file<br>
     *         STARTING_CAPTURE is returned if the Thread is setting variables<br>
     *         CLOSED is returned if the Thread is not doing anything/not capturing
     *         audio
     */
    public CaptureState getState() {
        return state;
    }

    /**
     * Sets the current state of Microphone
     *
     * @param state State from enum
     */
    private void setState(CaptureState state) {
        this.state = state;
    }

    public File getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(File audioFile) {
        this.audioFile = audioFile;
    }

    public AudioFileFormat.Type getFileType() {
        return fileType;
    }

    public void setFileType(AudioFileFormat.Type fileType) {
        this.fileType = fileType;
    }

    /**
     * Initializes the target data line.
     */
    private void initTargetDataLine() {
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, getAudioFormat());
        try {
            setTargetDataLine((TargetDataLine) AudioSystem.getLine(dataLineInfo));
        } catch (LineUnavailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

    }

    /**
     * Captures audio from the microphone and saves it a file
     *
     * @param audioFile The File to save the audio to
     * @throws LineUnavailableException
     */
    public void captureAudioToFile(File audioFile) throws LineUnavailableException {
        setState(CaptureState.STARTING_CAPTURE);
        setAudioFile(audioFile);

        if (getTargetDataLine() == null) {
            initTargetDataLine();
        }

        // Get Audio
        new Thread(new CaptureThread()).start();

    }

    /**
     * Captures audio from the microphone and saves it a file
     *
     * @param audioFile The fully path (String) to a file you want to save the audio
     *                  in
     * @throws LineUnavailableException
     */
    public void captureAudioToFile(String audioFile) throws LineUnavailableException {
        File file = new File(audioFile);
        captureAudioToFile(file);
    }

    /**
     * Opens the microphone, starting the targetDataLine. If it's already open, it
     * does nothing.
     */
    public void open() {
        if (getTargetDataLine() == null) {
            initTargetDataLine();
        }
        if (!getTargetDataLine().isOpen() && !getTargetDataLine().isRunning() && !getTargetDataLine().isActive()) {
            try {
                setState(CaptureState.PROCESSING_AUDIO);
                getTargetDataLine().open(getAudioFormat());
                getTargetDataLine().start();
            } catch (LineUnavailableException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
        }

    }

    /**
     * Close the microphone capture, saving all processed audio to the specified
     * file.<br>
     * If already closed, this does nothing
     */
    public void close() {
        if (getState() == CaptureState.CLOSED) {
        } else {
            getTargetDataLine().stop();
            getTargetDataLine().close();
            setState(CaptureState.CLOSED);
        }
    }

    /**
     * Thread to capture the audio from the microphone and save it to a file
     */
    private class CaptureThread implements Runnable {

        /**
         * Run method for thread
         */
        public void run() {
            try {
                AudioFileFormat.Type fileType = getFileType();
                File audioFile = getAudioFile();
                open();
                AudioSystem.write(new AudioInputStream(getTargetDataLine()), fileType, audioFile);
                // Will write to File until it's closed.
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // Addition - New flow for recording.
    private class NewCaptureThread implements Runnable {

        int bufferSize = (int) getAudioFormat().getSampleRate() * getAudioFormat().getFrameSize();
        byte buffer[] = new byte[bufferSize];

        @Override
        public void run() {
            try {
                open();
                out = new ByteArrayOutputStream();
                running = true;
                boolean delay = false;
                int delayCount = 3;
               
                // Temp for 5 file.
                boolean temp = true;
                int countRound = 0;
                System.out.println("buffer.length : " + buffer.length);
               
                while (temp) { // Temp for infite loop.
                    while (running) {
                        // Read the next chunk of data from the TargetDataLine.
                        int count = getTargetDataLine().read(buffer, 0, buffer.length);
                      
                        level = getAudioVolume(1000);
                        frequency = getFrequency();
                        System.out.println("Level : " + level);
                        System.out.println("frequency : " + frequency);
                        System.out.println("count : " + count);
                        
                        if (count > 0 && level > 30 && frequency < 200) {
                            System.out.println("Write.");
                            // Save this chunk of data.
                            out.write(buffer, 0, count);
                            delay = true;
                            delayCount = 2;
                        } else if (delay) {
                            System.out.println("Delay Write.");
                            // Save this chunk of data.
                            out.write(buffer, 0, count);
                            if (delayCount <= 0)
                                delay = false;
                            delayCount--;
                        } else {
                            if (level <= 30 && out.size() > 0) {
                                System.out.println("stop.");
                                stopAudio();
                            } else {
                                System.out.println("Not write, Is it noise ?");
                            }
                        }
                    }
                     // Clear
                    close();

                    if (out.size() != 0) {
                        System.out.println("Record.");
                        getAudioFileByLevel();
                        out.reset();
                        if (countRound == 5)
                            temp = false;
                        countRound++;
                    } else {
                        System.out.println("Not record, Ignore it.");
                    }

                    // Clear
                    running = true;
                    open();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void recordAudio() {
        try {
            // Get Audio
            new Thread(new NewCaptureThread()).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getAudioFileByLevel() {
        try {
            byte[] audio = out.toByteArray();
            InputStream input = new ByteArrayInputStream(audio);
            String tmpFileName = "D:/testfile_" + new Date().getTime() + ".flac";
            File file = new File(tmpFileName); // Name your file whatever you want
            AudioFormat format = getAudioFormat();
            AudioInputStream ais = new AudioInputStream(input, format, audio.length / format.getFrameSize());
            AudioSystem.write(ais, FLACFileWriter.FLAC, file);
            input.close();
            System.out.println("New file created!");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void stopAudio() {
        running = false;
    }
    // Addition - New flow for recording.

}
