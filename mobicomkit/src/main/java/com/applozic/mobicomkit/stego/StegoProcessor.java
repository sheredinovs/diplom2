package com.applozic.mobicomkit.stego;


import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StegoProcessor {
    private String message;

    public StegoProcessor(String message) {
        this.message = message;
    }

    public String prepareMessage(){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < StegoValidator.REPEAT_COUNT; i++){
            builder.append("1");
        }

        StringBuilder lengthPart = new StringBuilder(Integer.toBinaryString(message.length()));

        while (lengthPart.length() != StegoValidator.BLOCK_CONT_DATA){
            lengthPart.insert(0, "0");
        }

        for (int i= 0; i< StegoValidator.REPEAT_COUNT; i++){
            builder.append(lengthPart);
        }
        char[] messageArray = message.toCharArray();
        StringBuilder messageBuilder = new StringBuilder();

        for (char aMessageArray : messageArray) {
            messageBuilder.append(Integer.toBinaryString(aMessageArray));
        }

        for (int i= 0; i< StegoValidator.REPEAT_COUNT; i++){
            builder.append(messageBuilder);
        }
        return builder.toString();
    }

    public String extractSecureMessage(Bitmap bitmap){
        double[][] blueMatrix = new ImageHelper().convertToArray(bitmap).get(2);
        List<double[][]> blosks = new ImageHelper().getBlocks(blueMatrix);
        blosks.remove(0);
        blosks.remove(1);
        blosks.remove(2);
        Cipher cipher = new Cipher();
        List<Byte> out = new ArrayList<>();
        for (double[][] re : blosks) {
            out.add(cipher.compute(re));
        }
        byte[] res = new byte[out.size()];
        for(int i = 0; i < out.size(); i++) {
            res[i] = out.get(i);
        }

        List<String> bla = new ArrayList<>();
        for (int i = 0; i < res.length; i+=8) {
            bla.add(Arrays.toString(Arrays.copyOfRange(res, i, i + 8)));
        }
        StringBuilder message = new StringBuilder();
        for(String string : bla){
            int charCode = Integer.parseInt(string, 2);
            message.append(Character.toString((char) charCode));
        }
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
