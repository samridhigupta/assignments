package com.uc;

public class Temp {
    double power(int x, int p)
    { double r = 1;
        while (p > 0)
        { if (p % 2 == 0)
            { x = x * x;
                p = p/2;
            }
            else
            { r = r * x;
                p = p - 1;
            }
        } //end while
        return r;
    }
    public static void main(String[] args){
        Temp t = new Temp();
        System.out.print(t.power(2, 4));
    }
}
