package jcog.math;

import org.eclipse.collections.api.block.procedure.primitive.IntIntProcedure;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

/**
 *
 * Generalized Hilbert ('gilbert') space-filling curve for arbitrary-sized
 * 2D rectangular grids.
 *
 * translated from: https://github.com/jakubcerveny/gilbert/blob/master/gilbert2d.py
 */
public enum GilbertCurve { ;


    public static void gilbertCurve(int width, int height, IntIntProcedure next) {
        if (width >= height)
            gilbertCurve2D(0, 0, width, 0, 0, height, next);
        else
            gilbertCurve2D(0, 0, 0, height, width, 0, next);
    }

    private static void gilbertCurve2D(int x, int y, int ax, int ay, int bx, int by, IntIntProcedure next) {

        int w = abs(ax + ay), h = abs(bx + by);

        int dax = (int) signum(ax), day = (int) signum(ay); //unit major direction
        int dbx = (int) signum(bx), dby = (int) signum(by); //unit orthogonal direction

        if (h == 1) {
            //trivial row fill
            for (int i = 0; i < w; i++) {
                next.value(x, y);
                x += dax;
                y += day;
            }
        } else if (w == 1) {
            //trivial column fill
            for (int i = 0; i < h; i++) {
                next.value(x, y);
                x += dbx;
                y += dby;
            }
        } else {

            int ax2 = ax / 2, ay2 = ay / 2;
            int bx2 = bx / 2, by2 = by / 2;

            int w2 = abs(ax2 + ay2);
            int h2 = abs(bx2 + by2);

            if (2 * w > 3 * h) {
                if ((w2 % 2!=0) &&(w > 2)) {
                    //prefer even steps
                    ax2 += dax;
                    ay2 += day;
                }

                //long case: split in two parts only
                gilbertCurve2D(x, y, ax2, ay2, bx, by, next);

                gilbertCurve2D(x + ax2, y + ay2, ax - ax2, ay - ay2, bx, by, next);

            } else {
                if ((h2 % 2!=0) && (h > 2)) {
                    //prefer even steps
                    bx2 += dbx;
                    by2 += dby;
                }

                //standard case: one step up, one long horizontal, one step down
                gilbertCurve2D(x, y, bx2, by2, ax2, ay2, next);

                gilbertCurve2D(x + bx2, y + by2, ax, ay, bx - bx2, by - by2, next);

                gilbertCurve2D(x + (ax - dax) + (bx2 - dbx), y + (ay - day) + (by2 - dby),
                        -bx2, -by2, -(ax - ax2), -(ay - ay2), next);
            }
        }
    }

    public static void main(String[] args) {
        gilbertCurve(16, 33, (x, y) -> System.out.println(x + " " + y));
    }
}