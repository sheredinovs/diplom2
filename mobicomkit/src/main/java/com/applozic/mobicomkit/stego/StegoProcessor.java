package com.applozic.mobicomkit.stego;


import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StegoProcessor {
    private String message;

    public StegoProcessor(String message) {
        this.message = message;
    }

    public String formatInt(int val) {
        return String.format("%16s", Integer.toBinaryString(val)).replace(' ', '0');
    }

    public int decodeInt(String val) {
        return Integer.parseInt(val, 2);
    }

    public String prepareMessage(){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < StegoValidator.REPEAT_COUNT; i++){
            builder.append("1");
        }

        StringBuilder lengthPart = new StringBuilder(formatInt(message.length()));

        //while (lengthPart.length() != StegoValidator.BLOCK_CONT_DATA){
        //    lengthPart.insert(0, "0");
        //}

        //for (int i = 0; i < StegoValidator.REPEAT_COUNT; i++){
            builder.append(lengthPart);
        //}
        char[] messageArray = message.toCharArray();
        StringBuilder messageBuilder = new StringBuilder();

        for (char aMessageArray : messageArray) {
            messageBuilder.append(formatInt(aMessageArray));
        }

        //for (int i = 0; i < StegoValidator.REPEAT_COUNT; i++){
            builder.append(messageBuilder);
        //}
        return builder.toString();
    }

    public String extractSecureMessage(Bitmap bitmap){
        double[][] blueMatrix = new ImageHelper().convertToArray(bitmap).get(2);
        List<double[][]> blosks = new ImageHelper().getBlocks(blueMatrix);
        for (int i = 0; i < StegoValidator.REPEAT_COUNT; i++) {
            blosks.remove(0);
        }
        Cipher cipher = new Cipher();
        StringBuilder lenStr = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            lenStr.append(cipher.compute(blosks.remove(0)));
        }
        int messageLength = decodeInt(lenStr.toString());
        StringBuilder messageBuilder = new StringBuilder(messageLength);
        for (double[][] re : blosks) {
            messageBuilder.append(cipher.compute(re));
        }
        char[] messageBits = messageBuilder.toString().toCharArray();
        List<String> bla = new ArrayList<>();
        for (int i = 0; i < messageBits.length; i += 64) {
            bla.add(new String(Arrays.copyOfRange(messageBits, i, i + 64)));
        }
        StringBuilder message = new StringBuilder();
        for(String string : bla){
            int charCode = decodeInt(string);
            message.append(Character.toString((char)charCode));
        }

        // Debug
        Log.d("IDebug", "StegoProcessor/extractSecureMessage: result message = '" + message.toString() + "'");

        return message.toString();
    }

    public int coungOfText(List<double[][]> blosks){
        StringBuilder builder = new StringBuilder();
        Cipher cipher = new Cipher();
        for (int i = 0; i < 27; i++) {
            builder.append(cipher.compute(blosks.get(i)));
        }
        return 0;
    }

}
