package jcog.tree.rtree.rect;

import jcog.TODO;
import jcog.Util;
import jcog.math.v2;
import jcog.tree.rtree.HyperRegion;
import org.jetbrains.annotations.Nullable;

import static jcog.Str.n4;
import static jcog.Util.lerpSafe;
import static jcog.Util.maybeEqual;
import static jcog.tree.rtree.Spatialization.EPSILON;
import static jcog.tree.rtree.Spatialization.EPSILONf;


/** 32-bit float Rectangle */
public class RectF implements HyperRegion, Comparable<RectF> {

    public static final RectF Unit = XYXY(0, 0, 1, 1);
    public static final RectF Zero = XYXY(0, 0, 0, 0);

    public volatile float x, y, w, h;


    protected RectF(RectF r) {
        this(r.x, r.y, r.w, r.h);
    }

    private RectF(float left, float bottom, float w, float h) {
        //assert(w >= 0 && h >= 0);
        this.x = left; this.y = bottom; this.w = w; this.h = h;
    }

    protected RectF() {

    }


    /**
     * specified as a pair of X,Y coordinate pairs defining the diagonal extent
     */
    public static RectF XYXY(float x1 /* left */, float y1 /* bottom */, float x2, float y2) {
        if (x2 < x1) {
            float t = x2;
            x2 = x1;
            x1 = t;
        }
        if (y2 < y1) {
            float t = y2;
            y2 = y1;
            y1 = t;
        }

        return RectF._X0Y0WH(x1, y1, (x2 - x1), (y2 - y1));
    }

    /**
     * specified as a center point (cx,cy) and width,height extent (w,h)
     */
    public static RectF XYWH(float cx, float cy, float w, float h) {
        w = Math.abs(w); h = Math.abs(h);
        return X0Y0WH(cx - w / 2, cy - h / 2, w, h);
    }

    public static RectF XYWH(v2 center, float w, float h) {
        return XYWH(center.x, center.y, w, h);
    }

    /** x,y corresponds to "lower left" corner rather than XYWH's center */
    public static RectF X0Y0WH(float x0, float y0, float w, float h) {
        return _X0Y0WH(x0, y0, Math.abs(w), Math.abs(h));
    }

    
    private static RectF _X0Y0WH(float x0, float y0, float w, float h) {
        return new RectF(x0, y0, w, h);
    }


    public static RectF XYWH(double cx, double cy, double w, double h) {
        return XYWH((float)cx, ((float)cy), (float)w, (float)h);
    }
    /**
     * interpolates the coordinates, and the scale is proportional to the mean dimensions of each
     */
    public static RectF mid(RectF source, RectF target, float relScale) {
        float cx = (source.cx() + target.cx()) / 2;
        float cy = (source.cy() + target.cy()) / 2;
        float wh = relScale * Util.max((source.w + target.w) / 2f, (source.h + target.h) / 2);
        return target.orThisIfEqual(source.orThisIfEqual(RectF.XYWH(cx, cy, wh, wh)));
    }

    public static RectF WH(float w, float h) {
        return X0Y0WH(0, 0, w, h);
    }

    public static RectF XYXY(v2 ul, v2 br) {
        return RectF.XYXY(ul.x, ul.y, br.x, br.y);
    }

    public RectF move(double dx, double dy) {
        return move((float) dx, (float) dy);
    }

    public RectF move(float dx, float dy) {
        return move(dx, dy, EPSILONf);
    }

    public RectF move(float dx, float dy, float epsilon) {
        return Math.abs(dx) < epsilon && Math.abs(dy) < epsilon ? this :
                X0Y0WH(x + dx, y + dy, w, h);
    }
    public RectF pos(float x, float y, float epsilon) {
        return Util.equals(this.x, x, epsilon) && Util.equals(this.y, y, epsilon) ? this :
                X0Y0WH(x , y, w, h);
    }


    public RectF size(float ww, float hh) {
        return orThisIfEqual(X0Y0WH(x, y, ww, hh));
    }

    @Override
    public RectF mbr(HyperRegion _b) {
        if (_b == this) return this;

        RectF b = (RectF) _b;

        //TODO better merge of these conditions
//        if (contains(b))
//            return this;
//        else if (b.contains(this))
//            return this;
//        else if (b.equals(this))
//            return this;

        float ax = this.x, bx = b.x,
                minX = Util.min(ax, bx),
                maxX = Util.max(ax + w, bx + b.w);
        float ay = this.y, by = b.y,
                minY = Util.min(ay, by),
                maxY = Util.max(ay + h, by + b.h);

        return orThisIfEqual(XYXY(minX, minY, maxX, maxY));
    }

    @Override
    public final int dim() {
        return 2;
    }


    @Override
    public double center(int d) {
        if (d == 0) {
            return cx();
        } else {
            assert (d == 1);
            return cy();
        }
    }


    @Override
    public double coord(int dimension, boolean maxOrMin) {
        return switch (dimension) {
            case 0 -> maxOrMin ? (x + w) : x;
            case 1 -> maxOrMin ? (y + h) : h;
            default -> throw new UnsupportedOperationException();
        };
    }


    @Override
    public double range(int dim) {
        return switch (dim) {
            case 0 -> w;
            case 1 -> h;
            default -> throw new IllegalArgumentException("Invalid dimension");
        };
    }


    @Override
    public final boolean contains(HyperRegion r) {
        if (this == r) return true;
        RectF R = (RectF) r;
        return contains(R.x, R.y, R.w, R.h);
    }
    @Override
    public final boolean intersects(HyperRegion r) {
        if (this == r) return true;
        RectF R = (RectF) r;
        return intersects(R.x, R.y, R.w, R.h);
    }

    public final boolean contains(float rx, float ry, float rw, float rh) {
        return (x <= rx) && (x + w >= rx + rw) && (y <= ry) && (y + h >= (ry + rh));
    }
    public final boolean intersects(float rx, float ry, float rw, float rh) {
        return (Util.max(rx, x) <= Util.min(rx + rw, x + w)) && (Util.max(ry, y) <= Util.min(ry + rh, y + h));
    }
    public final boolean intersectsX1Y1X2Y2(float x1, float y1, float x2, float y2) {
        //Longerval.intersects() = return max(x1, x2) <= min(y1, y2);
        return (Util.max(x1, x) <= Util.min(x2, x + w)) && (Util.max(y1, y) <= Util.min(y2, y + h));
    }

    @Override
    public double cost() {
        return Math.abs(w * h);
    }

    @Override
    public final int hashCode() {
        return Util.hashCombine(Float.hashCode(x), Float.hashCode(y), Util.hashCombine(Float.hashCode(w), Float.hashCode(h)));
    }

    @Override
    public final boolean equals(Object o) {
        return this == o || ((o instanceof RectF) && equals((RectF)o, EPSILONf));

    }

    public final boolean equals(RectF o) {
        return this == o || equals(o, EPSILONf);
    }

    protected final boolean equals(Object o, float epsilon) {

        return this == o || ((o instanceof RectF) && equals((RectF)o, epsilon));
    }

    public final boolean equals(RectF o, float epsilon) {
        return o!=null && equals(o.x, o.y, o.w, o.h, epsilon);
    }

    public boolean equals(float xx, float yy, float ww, float hh, float epsilon) {
        return Util.equals(x, xx, epsilon) &&
                Util.equals(y, yy, epsilon) &&
                Util.equals(w, ww, epsilon) &&
                Util.equals(h, hh, epsilon);
    }

    public String toString() {
        return /* estimate */ '(' + n4(cx()) + ',' + n4(cy()) + ')' +
                'x' +
                '(' + n4(w) + ',' + n4(h) + ')';
    }

    @Override
    public int compareTo(RectF o) {
        throw new TODO();
    }


    /** max dimensional extent */
    public final float mag() {
        return Util.max(w, h);
    }

    public final boolean contains(v2 v) {
        return contains(v.x, v.y);
    }

    public final boolean contains(float px, float py) {
        return (px >= x && px <= x + w && py >= y && py <= y + h);
    }

    public final float bottom() { return y; }
    public final float left() {
        return x;
    }

    public final float right() {
        return x + w;
    }

    public final float top() {
        return y + h;
    }

    public final float cx() {
        return x + w / 2;
    }
    public final float cy() {
        return y + h / 2;
    }

    public RectF transform(float s, float ox, float oy) {
        ////        if (Util.equals(scale, 1f, ScalarValue.EPSILON) && offset.equalsZero())
            return orThisIfEqual(RectF.X0Y0WH(left()+ox, bottom()+oy, w * s, h * s));
    }

    public RectF scale(float s) {
        return Util.equals(s, 1, EPSILON) ? this : RectF.XYWH(cx(), cy(), w * s, h * s);
    }

    public RectF scale(float sw, float sh) {
        return !Util.equals(sw, 1, EPSILON) || !Util.equals(sh, 1, EPSILON) ?
                RectF.XYWH(cx(), cy(), w * sw, h * sh) : this;
    }

    public float radius() {
        return ((float) Math.sqrt(radiusSquare()));
    }

    public float radiusSquare() {
        float W = w / 2, H = h / 2;
        return (W * W + H * H);
    }

    public final float area() {
        return w * h;
    }

    /** note: this is sloppy lerp (non-cartesian) on dimensions independently */
    @Deprecated public RectF posLerp(float x, float y, float p) {
        return RectF.XYWH(lerpSafe(p, cx(), x),lerpSafe(p, cy(), y) ,w , h);
    }

//    public boolean nonZero(float epsilon) {
//        return w > epsilon && h > epsilon;
//    }

    public RectF rel(float cx, float cy, float pctW, float pctH) {
        return orThisIfEqual(X0Y0WH( x + cx , y + cy , this.w * pctW, this.h * pctH));
    }

    public v2 midPoint(RectF o) {
        return new v2((cx()+o.cx())/2 , (cy()+o.cy())/2);
    }

    public v2 center() {
        return new v2(cx(), cy());
    }

    /** relative unit-scale position of the global point */
    public v2 unitize(v2 v) {
        return new v2((v.x - left())/w, (v.y - bottom()/h));
    }
    /** opposite of unitize */
    public v2 normalize(v2 v) {
        return new v2(jcog.Util.fma(v.x, w, left()), jcog.Util.fma(v.y, h, bottom()));
    }

    /** computes the average of both position and scale parameters */
    public RectF mean(RectF o) {
        if (this == o) return this;
        return orThisIfEqual(X0Y0WH((x + o.x)/2, (y+o.y)/2, (w + o.w)/2, (h + o.h)/2));
    }

    /** clamp inside */
    public RectF clamp(RectF outer) {
        if (outer.contains(this))
            return this;
        else {
            float w = Util.min(this.w, outer.w), h = Util.min(this.h, outer.h);
//            if (outer.w < w || outer.h < h)
//                throw new WTF(this +  " is too large to fit inside " + outer);

            //if ((cx != cx) || (cy != cy)) randomize(bounds);
            float x = cx(); if (!Float.isFinite(x)) x = 0;
            float y = cy(); if (!Float.isFinite(y)) y = 0;

            return orThisIfEqual(XYWH(
                    Util.clampSafe(x, outer.left() + w / 2, outer.right() - w / 2),
                    Util.clampSafe(y, outer.bottom() + h / 2, outer.top() - h / 2),
                    w,h));
        }

    }

    private RectF orThisIfEqual(RectF r) {
        return r.equals(this) ? this : r;
    }

    public double distanceTo(double x, double y) {
        //  Calculate a distance between a point and a rectangle.
        //  The area around/in the rectangle is defined in terms of
        //  several regions:
        //
        //  O--x
        //  |
        //  y
        //
        //
        //        I   |    II    |  III
        //      ======+==========+======   --yMin
        //       VIII |  IX (in) |  IV
        //      ======+==========+======   --yMax
        //       VII  |    VI    |   V
        //
        //
        //  Note that the +y direction is down because of Unity's GUI coordinates.

        if (x < left()) { // Region I, VIII, or VII
            if (y < bottom()) { // I
                return Math.sqrt(Util.sqr(x - left())+Util.sqr(y - bottom()));
            } else if (y > top()) { // VII
                return Math.sqrt(Util.sqr(x - left())+Util.sqr(y - top()));
            } else { // VIII
                return left() - x;
            }
        }
        else if (x > right()) { // Region III, IV, or V
            if (y < bottom()) { // III
                return Math.sqrt(Util.sqr(x - right())+Util.sqr(y - bottom()));
            } else if (y > top()) { // V
                return Math.sqrt(Util.sqr(x - right())+Util.sqr(y - top()));
            } else { // IV
                return x - right();
            }
        }
        else { // Region II, IX, or VI
            if (y < bottom()) { // II
                return bottom() - y;
            } else if (y > top()) { // VI
                return y - top();
            } else { // IX
                return 0;
            }
        }

    }

    @Nullable
    public RectF intersection(RectF x) {
        if (this == x) return this;
        float L = Util.max(left(), x.left());
        float R = Util.min(right(), x.right());
        if (L <= R) {
            float B = Util.max(bottom(), x.bottom());
            float T = Util.min(top(), x.top());
            if (B <= T) {
              return maybeEqual(RectF.XYXY(L,B,R,T), x /* or this */);
            }
        }
        return null;
    }

    public void set(RectF r) {
        this.x = r.x; this.y = r.y; this.w= r.w; this.h = r.h;
    }

    public float aspectExtreme() {
        float a = aspect();
        return a < 1 ? 1/a : a;
    }

    public float aspect() {
        return h / w;
    }
}