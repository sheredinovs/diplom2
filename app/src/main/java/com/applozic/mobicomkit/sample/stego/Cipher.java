package com.applozic.mobicomkit.sample.stego;

/**
 * Created by kadyr on 04.02.2018.
 */
public class Cipher {
    public static final int THRESHOLD = 150;

    public static final int FIRST_COEFF = 4;
    public static final int SECOND_COEFF = 5;

    public byte compute(double[][] mass){
        return extractBite(new DcpUtils().dcp(mass));
    }

    public double[][] chipher(double[][] mass, byte l){
        double[][] dct = new DcpUtils().dcp(mass);
        if(l == 1){
            if(Math.abs(mass[FIRST_COEFF][SECOND_COEFF]) - Math.abs(mass[SECOND_COEFF][FIRST_COEFF]) < (- THRESHOLD)){
                System.out.println("погружение  для 1 " + mass[FIRST_COEFF][SECOND_COEFF] + "   "
                        + mass[FIRST_COEFF][SECOND_COEFF] +  "   " + mass[SECOND_COEFF][FIRST_COEFF]);
                return mass;
            }
            else {
                System.out.println("погружение  для 1 " + mass[FIRST_COEFF][SECOND_COEFF] + "   "
                        + mass[FIRST_COEFF][SECOND_COEFF] +  "   " + mass[SECOND_COEFF][FIRST_COEFF]);



                mass[SECOND_COEFF][FIRST_COEFF] = Math.signum(mass[SECOND_COEFF][FIRST_COEFF]) * THRESHOLD;



                System.out.println("погружение  для 1 " + mass[FIRST_COEFF][SECOND_COEFF] + "   ");
            }
        }
        else if(l == 0){
            if(Math.abs(mass[FIRST_COEFF][SECOND_COEFF]) - Math.abs(mass[SECOND_COEFF][FIRST_COEFF]) > THRESHOLD){
                System.out.println("погружение  для 0 " + mass[FIRST_COEFF][SECOND_COEFF] + "   "
                        + mass[FIRST_COEFF][SECOND_COEFF] +  "   " + mass[SECOND_COEFF][FIRST_COEFF]);
                return mass;
            }
            else {
                System.out.println("погружение  для 0 " + mass[FIRST_COEFF][SECOND_COEFF] + "   "
                        + mass[FIRST_COEFF][SECOND_COEFF] +  "   " + mass[SECOND_COEFF][FIRST_COEFF]);



                mass[FIRST_COEFF][SECOND_COEFF] = Math.signum(mass[FIRST_COEFF][SECOND_COEFF]) * THRESHOLD;




                System.out.println("погружение  для 0 " + mass[FIRST_COEFF][SECOND_COEFF] + "   ");
            }
        }
        return mass;
    }



    public double[][] smartInsert(double[][] mass, int l) {
        double[][] dct = new DcpUtils().dcp(mass);
        double[][]temp = mass;
        int k =1;
        while (!isValid(dct, l) || extractBite(dct) != l){
            dct[FIRST_COEFF][SECOND_COEFF] = dct[FIRST_COEFF][SECOND_COEFF] - THRESHOLD * (l * 2 - 1) * Math.signum(dct[FIRST_COEFF][SECOND_COEFF]);
            dct[SECOND_COEFF][FIRST_COEFF] = dct[SECOND_COEFF][FIRST_COEFF] + THRESHOLD * (l * 2 - 1) * Math.signum(dct[SECOND_COEFF][FIRST_COEFF]);

            temp = new DcpUtils().idcp(dct);
            dct = new DcpUtils().dcp(temp);

            k++;
            if(k == 5){
                return temp;
            }
        }
        return temp;
    }


    public boolean isValid(double[][] mass, int l){
        double first = mass[FIRST_COEFF][SECOND_COEFF];
        double second = mass[SECOND_COEFF][FIRST_COEFF];

        double d = Math.abs(first) - Math.abs(second);

        return (l == 0 && d > THRESHOLD) || (l == 1 && d < - THRESHOLD);
    }

    public byte extractBite(double[][] mass){
        double first = mass[FIRST_COEFF][SECOND_COEFF];
        double second = mass[SECOND_COEFF][FIRST_COEFF];
        double d = Math.abs(first) - Math.abs(second);

        if(d > 0){
            return 0;
        }
        else
            return 1;
    }
}
