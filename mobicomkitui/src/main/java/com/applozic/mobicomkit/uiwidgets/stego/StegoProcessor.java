package com.applozic.mobicomkit.uiwidgets.stego;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComAttachmentSelectorActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MultimediaOptionFragment;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by kadyr on 10.03.2018.
 */

public class StegoProcessor {
    private String message;

    public StegoProcessor(String message) {
        this.message = message;
    }

    public void preprocess(Context context){
        Intent intentPick = new Intent(context, MobiComAttachmentSelectorActivity.class);
        ((Activity)context).startActivityForResult(intentPick, MultimediaOptionFragment.REQUEST_MULTI_ATTCAHMENT);
    }

    public String prepareMessage(String message){
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
}
