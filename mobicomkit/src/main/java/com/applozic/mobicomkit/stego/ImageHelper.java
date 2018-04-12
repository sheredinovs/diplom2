package com.applozic.mobicomkit.stego;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kadyr on 04.02.2018.
 */
public class ImageHelper {

    public List<double[][]> getBlocks(double[][] blueMAtrix){
        int rows = blueMAtrix.length / 8;
        int cols = blueMAtrix[0].length / 8;

        ArrayList<double[][]> list = new ArrayList<>(rows * cols);

        for (int indRow = 0; indRow < rows; indRow++)
            for (int indCol = 0; indCol < cols; indCol++) {
                double[][] tempBlock = new double[8][8];
                for (int i = 0; i < 8; i++)
                    System.arraycopy(blueMAtrix[indRow * 8 + i], indCol * 8, tempBlock[i], 0, 8);
                list.add(tempBlock);
            }

        return list;
    }

    public List<double[][]> convertToArray(Bitmap image){
        int h = image.getHeight();
        int w = image.getWidth();
        double[][] resultBlue = new double[h][w];
        double[][] resultRed = new double[h][w];
        double[][] resultGreen = new double[h][w];
        System.out.println(resultBlue.length + "   " + resultBlue[0].length);
        List<double[][]> res = new ArrayList<>();
        for(int i = 0; i < h; i++)
            for(int j = 0; j < w; j++) {
                try {
                    int pixel;
                    pixel =  image.getPixel(j, i);
                    resultBlue[i][j] = Color.blue(pixel);
                    resultRed[i][j] = Color.red(pixel);
                    resultGreen[i][j] = Color.green(pixel);
                }catch (Exception e){
                    System.out.println(e.getMessage());;
                }
            }
        res.add(resultRed);
        res.add(resultGreen);
        res.add(resultBlue);

        return res;
    }

    public void showImageMatrix(double[][] arr){
        for(int i = 0; i < arr.length; i++){
            System.out.println(Arrays.toString(arr[i]));
        }
    }

    public void showAllBlocks(List<double[][]> blosks){
        for (double[][] matrix:blosks) {
            showImageMatrix(matrix);
            System.out.println();
        }
    }

    public Bitmap createImage(double[][] redPart, double[][] greenPart, double[][] bluePart, Bitmap originalImage) throws Exception {
        Bitmap image = Bitmap.createBitmap(originalImage.getWidth(), originalImage.getHeight(),originalImage.getConfig());
        int[][] newRedPart = arrayToByte(redPart);
        int[][] newGreenPart = arrayToByte(greenPart);
        int[][] newBluePart = arrayToByte(bluePart);
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, 1, 1);

        for (int y = 0; y < newRedPart.length; y++) {
            for (int x = 0; x < newRedPart[0].length; x++) {
                int color = Color.rgb(newRedPart[y][x], newGreenPart[y][x], newBluePart[y][x]);
                paint.setColor(color);
                //canvas.drawPaint(paint);
                canvas.drawPoint(x, y, paint);
            }
        }

        return image;
    }
    private static int[][] arrayToByte(double[][] array) {
        int[][] res = new int[array.length][array[0].length];

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                if (array[i][j] < 0)
                    res[i][j] = 0;
                else if (array[i][j] > 255)
                    res[i][j] = 255;
                else
                    res[i][j] = (int) array[i][j];
            }
        }
        return res;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }


}
