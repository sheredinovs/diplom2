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
        List<double[][]> blueMatrix = new ImageHelper().getBlocks(blocks.get(2));
        int count = 0;
        for(int i = 0; i< 2; i++){
            if(new Cipher().compute(blueMatrix.get(i)) == 1){
                count++;
            }
        }
        return count > 1;
    }
}
