package com.applozic.mobicomkit.uiwidgets.stego;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by kadyr on 10.03.2018.
 */

public class StegoValidator {
    public static final int MAX_COUNT = 375;
    public static final int BLOCK_CONT_DATA =9;
    public static final int REPEAT_COUNT = 3;


    public boolean isValidToCompute(String string){
        if(StringUtils.isNotEmpty(string) && string.length() <= MAX_COUNT){
            return true;
        }
        return false;
    }
}
