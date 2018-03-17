package com.applozic.mobicomkit.uiwidgets.stego;

import android.content.Context;
import android.content.Intent;

import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComAttachmentSelectorActivity;

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
        Intent intent = new Intent(context, MobiComAttachmentSelectorActivity.class);
        context.startActivity(intent);
    }

    public void process(){

    }
}
