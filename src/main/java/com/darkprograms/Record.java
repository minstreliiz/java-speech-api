package com.darkprograms;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import com.darkprograms.speech.microphone.Microphone;

import net.sourceforge.javaflacencoder.FLACFileWriter;

public class Record {

    public static void main(String[] args) throws Exception {

        AudioFileFormat.Type[] typeArray = AudioSystem.getAudioFileTypes();
        for (AudioFileFormat.Type type : typeArray) {
            System.out.println("type: " + type.toString());
        }
        try {
            Microphone mic = new Microphone(FLACFileWriter.FLAC);
            mic.enumerateMicrophones();
            System.out.println("Recording...");
            mic.recordAudio();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}