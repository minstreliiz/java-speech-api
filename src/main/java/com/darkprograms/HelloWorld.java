package com.darkprograms;

import javax.sound.sampled.*;

import java.io.File;
import java.util.Date;

import com.darkprograms.speech.microphone.Microphone;
import net.sourceforge.javaflacencoder.FLACFileWriter;

/**
 * Jarvis Speech API Tutorial
 * 
 * @author Aaron Gokaslan (Skylion)
 *
 */
public class HelloWorld {

  public static void main(String[] args) throws Exception {

    // Mixer.Info[] infoArray = AudioSystem.getMixerInfo();
    // for(Mixer.Info info : infoArray) {
    // System.out.println("info: " + info.toString());
    // }
    AudioFileFormat.Type[] typeArray = AudioSystem.getAudioFileTypes();
    for (AudioFileFormat.Type type : typeArray) {
      System.out.println("type: " + type.toString());
    }
    Microphone mic = new Microphone(FLACFileWriter.FLAC);
    // MicrophoneAnalyzer mic = new MicrophoneAnalyzer(AudioFileFormat.Type.WAVE);
    String tmpFileName = "D:/testfile_" + new Date().getTime() + ".flac";
    File file = new File(tmpFileName); // Name your file whatever you want
    // int recStatus = 50;
    // try {
    // mic.captureAudioToFile(file);
    // } catch (Exception ex) {
    // //Microphone not available or some other error.
    // System.out.println ("ERROR: Microphone is not availible.");
    // ex.printStackTrace ();
    // }

    /*
     * User records the voice here. Microphone starts a separate thread so do
     * whatever you want in the mean time. Show a recording icon or whatever.
     */
    try {
      mic.checkMicrophoneAvailability();

      mic.captureAudioToFile(file);
      System.out.println("Recording...");

      // while (recStatus != 1) {
      // recStatus = mic.getAudioVolume(1000);
      // System.out.println("recStatus : " + recStatus);
      // Thread.sleep(500L);
      // }

      int level = 0;
      int frequency = 0;
      byte tempBuffer[] = new byte[4096];
      boolean stopCapture = false;
      while (!stopCapture) {
        if (mic.getTargetDataLine().read(tempBuffer, 0, tempBuffer.length) > 0) {
          level = mic.getAudioVolume(1000);
          frequency = mic.getFrequency();
          Thread.sleep(3000L); // In our case, we'll just wait 5 seconds.
          // System.out.println("getNumOfBytes :" + mic.getNumOfBytes(1));
          System.out.println("Level : " + level);
          System.out.println("frequency : " + frequency);

          if (level <= 40) {
            mic.close();

            // Start - Convert to flac.
            // FlacEncoder flacEncoder = new FlacEncoder();
            // File flacFile = new File(tmpFileName + ".flac");
            // flacEncoder.convertWaveToFlac(file, flacFile);
            // End - Convert to flac.

            Thread.sleep(500L); // In our case, we'll just wait 5 seconds.

            System.out.println("newfile.");
            file.exists();
            tmpFileName = "D:/testfile_" + new Date().getTime() + ".flac";
            ;
            file = new File(tmpFileName);
            mic.captureAudioToFile(file);
          }
        }
      }
      //

    } catch (InterruptedException itre) {
      itre.printStackTrace();
    } catch (LineUnavailableException lunae) {
      lunae.printStackTrace();
    }

    mic.close(); // Ends recording and frees the resources
    System.out.println("Recording stopped.");
    /*
     * Recognizer recognizer = new Recognizer (Recognizer.Languages.ENGLISH_US,
     * System.getProperty("google-api-key")); //Although auto-detect is available,
     * it is recommended you select your region for added accuracy. try { int
     * maxNumOfResponses = 4; System.out.println("Sample rate is: " + (int)
     * mic.getAudioFormat().getSampleRate()); GoogleResponse response =
     * recognizer.getRecognizedDataForFlac (file, maxNumOfResponses, (int)
     * mic.getAudioFormat().getSampleRate ()); System.out.println
     * ("Google Response: " + response.getResponse ()); System.out.println
     * ("Google is " + Double.parseDouble (response.getConfidence ()) * 100 +
     * "% confident in" + " the reply"); System.out.println
     * ("Other Possible responses are: "); for (String
     * s:response.getOtherPossibleResponses ()) { System.out.println ("\t" + s); } }
     * catch (Exception ex) { // TODO Handle how to respond if Google cannot be
     * contacted System.out.println ("ERROR: Google cannot be contacted");
     * ex.printStackTrace (); }
     */
    // file.deleteOnExit (); //Deletes the file as it is no longer necessary.
  }
}