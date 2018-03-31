package com.applozic.mobicomkit.stego;

/**
 * Created by Slavik on 24.05.2016.
 */
public class DcpUtils {

    private double valueCoefficient(int value){
        if (value == 0)
            return 1.0/Math.sqrt(2);
        else return 1;
    }

    public double[][] dcp(double mas[][]){
        int size = mas.length;
        double[][] res = new double[size][size];
        double U, V, temp;
        for (int v =0; v<size; v++) {
            for (int u = 0; u < size; u++) {
                V = valueCoefficient(v);
                U = valueCoefficient(u);
                temp = 0;
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        temp += mas[i][j] * Math.cos(Math.PI * v * (2 * i + 1) / (2 * size)) *
                                Math.cos(Math.PI * u * (2 * j + 1) / (2 * size));
                    }
                }
                res[v][u] = U * V * temp / (Math.sqrt(2 * size));
            }
        }
        return res;
    }
    public static int n = 8,m = 8;
    public static double pi = 3.142857;

    public double[][] idcp(double mas[][]){
        int size = mas.length;
        double[][] res = new double[size][size];
        double U, V, temp;
        for (int v=0; v<size; v++) {
            for (int u = 0; u < size; u++) {
                temp = 0;
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        V = valueCoefficient(i);
                        U = valueCoefficient(j);
                        temp += U * V * mas[i][j] * Math.cos(Math.PI * i * (2 * v + 1) / (2 * size)) *
                                Math.cos(Math.PI * j * (2 * u + 1) / (2 * size));
                    }
                }
                res[v][u] = temp / (Math.sqrt(2 * size));
            }
        }
        return res;
    }











}
