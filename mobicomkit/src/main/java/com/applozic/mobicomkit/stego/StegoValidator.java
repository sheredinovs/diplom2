package com.applozic.mobicomkit.stego;

import android.graphics.Bitmap;


import java.util.List;

/**
 * Created by kadyr on 10.03.2018.
 */

public class StegoValidator {
    public static final int MAX_COUNT = 375;
    public static final int BLOCK_CONT_DATA =9;
    public static final int REPEAT_COUNT = 3;


    public boolean isValidToSelect(Bitmap bitmap){
        List<double[][]> blocks = new ImageHelper().convertToArray(bitmap);
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for(int i = 0; i< 2; i++){
            if(new Cipher().compute(blocks.get(i)) == 1){
                count++;
            }
        }
        return count > 1;
    }
}
