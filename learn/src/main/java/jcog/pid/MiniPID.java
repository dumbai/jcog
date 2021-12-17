package jcog.pid;

import jcog.Util;
import jcog.signal.FloatRange;

/**
 * Small, easy to use PID implementation with advanced controller capability.<br>
 * Minimal usage:<br>
 * MiniPID pid = new MiniPID(p,i,d); <br>
 * ...looping code...{ <br>
 * output= pid.getOutput(sensorvalue,target); <br>
 * }
 *
 * @see http:
 */
public class MiniPID {


    public final FloatRange P = new FloatRange(0.5f, 0, +100);
    public final FloatRange I = new FloatRange(0.5f, 0, +100);
    public final FloatRange D = new FloatRange(0.5f, 0, +100);
    public final FloatRange F = new FloatRange(0, 0, 100);

    private double maxIOutput = 0;
    private double errMax = Double.POSITIVE_INFINITY;
    private double errSum = 0;

    private double outMax = Double.POSITIVE_INFINITY;
    private double outMin = Double.NEGATIVE_INFINITY;

    private double setpoint = 0;
//    private double setpointMin = Double.NaN, setpointMax = Double.NaN;

    private double actual = Double.NaN;

    private boolean reversed = false;

    private double outRampRate = 0;
    private double out = 0;

    private double outMomentumPlus = 0, outMomentumNeg = 0;


    /**
     * Create a MiniPID class object.
     * See setP, setI, setD methods for more detailed parameters.
     *
     * @param p Proportional gain. Large if large difference between setpoint and target.
     * @param i Integral gain.  Becomes large if setpoint cannot reach target quickly.
     * @param d Derivative gain. Responds quickly to large changes in error. Small values prevents P and I terms from causing overshoot.
     */
    public MiniPID(double p, double i, double d) {
        setPID(p,i,d);
        checkSigns();
    }

    /**
     * Create a MiniPID class object.
     * See setP, setI, setD, setF methods for more detailed parameters.
     *
     * @param p Proportional gain. Large if large difference between setpoint and target.
     * @param i Integral gain.  Becomes large if setpoint cannot reach target quickly.
     * @param d Derivative gain. Responds quickly to large changes in error. Small values prevents P and I terms from causing overshoot.
     * @param f Feed-forward gain. Open loop "best guess" for the output should be. Only useful if setpoint represents a rate.
     *          <p>
     *          <p>
     *          <p>
     *          https://www.crossco.com/blog/basics-tuning-pid-loops
     *          Start with a low proportional and no integral or derivative.
     *          Double the proportional until it begins to oscillate, then halve it.
     *          Implement a small integral.
     *          Double the integral until it starts oscillating, then halve it.
     */
    public MiniPID(double p, double i, double d, double f) {
        p(p);
        i(i);
        d(d);
        f(f);
        checkSigns();
    }

    /**
     * Test if the value is within the min and max, inclusive
     *
     * @param value to test
     * @param min   Minimum value of range
     * @param max   Maximum value of range
     * @return true if value is within range, false otherwise
     */
    private static boolean includes(double value, double min, double max) {
        return (min <= value) && (value <= max);
    }

    /**
     * Configure the Proportional gain parameter. <br>
     * This responds quickly to changes in setpoint, and provides most of the initial driving force
     * to make corrections. <br>
     * Some systems can be used with only a P gain, and many can be operated with only PI.<br>
     * For position based controllers, this is the first parameter to tune, with I second. <br>
     * For rate controlled systems, this is often the second after F.
     *
     * @param p Proportional gain. Affects output according to <b>output+=P*(setpoint-current_value)</b>
     */
    public void p(double p) {
        if (P.setIfChanged((float)p))
            checkSigns();
    }

    /**
     * Changes the I parameter <br>
     * This is used for overcoming disturbances, and ensuring that the controller always gets to the control mode.
     * Typically tuned second for "Position" based modes, and third for "Rate" or continuous based modes. <br>
     * Affects output through <b>output+=previous_errors*Igain ;previous_errors+=current_error</b>
     *
     * @param i New gain value for the Integral term
     * @see {@link #setMaxIOutput(double) setMaxIOutput} for how to restrict
     */
    private void i(double i) {
        float I = this.I.floatValue();

        if (I != 0) {
            errSum = errSum * I / i;
        }
        if (maxIOutput != 0) {
            errMax = maxIOutput / i;
        }
        this.I.set(i);
        checkSigns();

    }

    /**
     * Changes the D parameter <br>
     * This has two primary effects:
     * <list>
     * <li> Adds a "startup kick" and speeds up system response during setpoint changes
     * <li> Adds "drag" and slows the system when moving toward the target
     * </list>
     * A small D value can be useful for both improving response times, and preventing overshoot.
     * However, in many systems a large D value will cause significant instability, particularly
     * for large setpoint changes.
     * <br>
     * Affects output through <b>output += -D*(current_input_value - last_input_value)</b>
     *
     * @param d New gain value for the Derivative term
     */
    public void d(double d) {
        if (this.D.setIfChanged((float)d))
            checkSigns();
    }

    /**
     * Configure the FeedForward parameter. <br>
     * This is excellent for velocity, rate, and other  continuous control modes where you can
     * expect a rough output value based solely on the setpoint.<br>
     * Should not be used in "position" based control modes.<br>
     * Affects output according to <b>output+=F*Setpoint</b>. Note, that a F-only system is actually open loop.
     *
     * @param f Feed forward gain.
     */
    public void f(double f) {
        if (this.F.setIfChanged((float)f))
            checkSigns();
    }

    /**
     * Configure the PID object.
     * See setP, setI, setD methods for more detailed parameters.
     *
     * @param p Proportional gain. Large if large difference between setpoint and target.
     * @param i Integral gain.  Becomes large if setpoint cannot reach target quickly.
     * @param d Derivative gain. Responds quickly to large changes in error. Small values prevents P and I terms from causing overshoot.
     */
    public void setPID(double p, double i, double d) {
        p(p);
        i(i);
        d(d);
    }

    /**
     * Configure the PID object.
     * See setP, setI, setD, setF methods for more detailed parameters.
     *
     * @param p Proportional gain. Large if large difference between setpoint and target.
     * @param i Integral gain.  Becomes large if setpoint cannot reach target quickly.
     * @param d Derivative gain. Responds quickly to large changes in error. Small values prevents P and I terms from causing overshoot.
     * @param f Feed-forward gain. Open loop "best guess" for the output should be. Only useful if setpoint represents a rate.
     */
    public void setPID(double p, double i, double d, double f) {
        p(p);
        d(d);
        f(f);
        i(i);
        checkSigns();
    }

    /**
     * Set the maximum output value contributed by the I component of the system
     * This can be used to prevent large windup issues and make tuning simpler
     *
     * @param maximum. Units are the same as the expected output value
     */
    private void setMaxIOutput(double maximum) {
        maxIOutput = maximum;

        double i = i();
        if (i != 0)
            errMax = maxIOutput / i;
    }

    /**
     * Specify a  maximum output.
     * When two inputs specified, output range is configured to
     * <b>[minimum, maximum]</b>
     *
     * @param minimum possible output value
     * @param maximum possible output value
     */
    public MiniPID outRange(double minimum, double maximum) {
        if (maximum < minimum) throw new UnsupportedOperationException();
        if (minimum!=outMin || maximum!=outMax) {
            outMin = minimum;
            outMax = maximum;

            if (Double.isFinite(minimum) && Double.isFinite(maximum)) {
                //if (maxIOutput == 0 || maxIOutput > (maximum - minimum)) {
                setMaxIOutput(maximum - minimum);
                //}
            }
        }
        return this;
    }

    /**
     * Set the operating direction of the PID controller
     *
     * @param reversed Set true to reverse PID output
     * @return
     */
    public MiniPID reversed() {
        this.reversed = true;
        return this;
    }

    /**
     * Configure setpoint for the PID calculations<br>
     * This represents the target for the PID system's, such as a
     * position, velocity, or angle. <br>
     *
     * @param setpoint
     * @see MiniPID#getOutput(actual) <br>
     */
    public MiniPID setpoint(double setpoint) {
        this.setpoint = setpoint;
        return this;
    }

    /**
     * Calculate the output value for the current PID cycle.<br>
     *
     * @param actual observed value, typically as a sensor input.
     * @param target target value
     * @return calculated output value for driving the system
     */
    public final double out(double actual, double target) {
        return setpoint(target).out(actual);
    }

    @Override
    public String toString() {
        return "MiniPID{" +
                "P=" + P +
                ", I=" + I +
                ", D=" + D +
                ", setpoint=" + setpoint +
                ", actual=" + actual +
                ", out=" + out +
                '}';
    }


    /**
     * Calculate the output value for the current PID cycle.<br>
     * In one parameter mode, the last configured setpoint will be used.<br>
     *
     * @param actual   The monitored value, typically as a sensor input.
     * @param setpoint The target value for the system
     * @return calculated output value for driving the system
     * @see MiniPID#setSetpoint()
     */
    public double out(double actual) {

//        double spMin = this.setpointMin;
//        if (spMin == spMin) {
//            setpoint = Util.clamp(setpoint, spMin, this.setpointMax);
//        }

        double outPrev = this.out;
        double actualPrev = this.actual;
        double setpoint = this.setpoint;

        double error = setpoint - actual;
        //error = Util.clamp(error, -errMax, errMax);
        //this.errSum += error;
        this.errSum = Util.clamp(errSum + error, -errMax, errMax);

        double Foutput = f() * setpoint;


        double Poutput = p() * error;


        boolean initialIter = (actualPrev != actualPrev);
        if (initialIter) {
            actualPrev = actual;
            outPrev = Poutput + Foutput;
        }

        double Doutput = d() * (actualPrev - actual);
        this.actual = actual;

        double Ioutput = i() * errSum;

        if (maxIOutput != 0)
            Ioutput = Util.clamp(Ioutput, -maxIOutput, maxIOutput);


        double out = Foutput + Poutput + Ioutput + Doutput;

        if (outRampRate != 0)
            out = Util.clamp(out, outPrev - outRampRate, outPrev + outRampRate);

        out = Util.lerp(out >= outPrev ? outMomentumPlus : outMomentumNeg, out, outPrev);

        return (this.out = filterOut(out));
    }

    private double filterOut(double output) {
        return Util.clamp(output, outMin, outMax);
    }

    /**
     * Resets the controller. This erases the I term buildup, and removes
     * D gain on the next loop.<br>
     * This should be used any time the PID is disabled or inactive for extended
     * duration, and the controlled portion of the system may have changed due to
     * external forces.
     */
    public void reset() {
        actual = Double.NaN;
        errSum = 0;
    }

    /**
     * Set the maximum rate the output can increase per cycle.<br>
     * This can prevent sharp jumps in output when changing setpoints or
     * enabling a PID system, which might cause stress on physical or electrical
     * systems.  <br>
     * Can be very useful for fast-reacting control loops, such as ones
     * with large P or D values and feed-forward systems.
     *
     * @param rate, with units being the same as the output
     */
    public void setOutRampRate(double rate) {
        outRampRate = rate;
    }

//    /**
//     * Set a limit on how far the setpoint can be from the current position
//     * <br>Can simplify tuning by helping tuning over a small range applies to a much larger range.
//     * <br>This limits the reactivity of P term, and restricts impact of large D term
//     * during large setpoint adjustments. Increases lag and I term if range is too small.
//     *
//     * @param range, with units being the same as the expected sensor range.
//     */
//    public void setSetpointRange(double min, double max) {
//        setpointMin = min; setpointMax = max;
//    }

    /**
     * Set a filter on the output to reduce sharp oscillations. <br>
     * 0.1 is likely a sane starting value. Larger values use historical data
     * more heavily, with low values weigh newer data. 0 will disable, filtering, and use
     * only the most recent value. <br>
     * Increasing the filter strength will P and D oscillations, but force larger I
     * values and increase I term overshoot.<br>
     * Uses an exponential wieghted rolling sum filter, according to a simple <br>
     * <pre>output*(1-strength)*sum(0..n){output*strength^n}</pre> algorithm.
     *
     * @param output valid between [0..1), meaning [current output only.. historical output only)
     * @return
     */
    public MiniPID setOutMomentum(double momentumPlus, double momentumNeg) {
        outMomentumPlus = momentumPlus;
        outMomentumNeg = momentumNeg;
        return this;
    }

    public final MiniPID setOutMomentum(double momentum) {
        return setOutMomentum(momentum,momentum);
    }

    /**
     * To operate correctly, all PID parameters require the same sign
     * This should align with the {@literal}reversed value
     */
    private void checkSigns() {
        {
            double p = p(); if ((reversed && (p > 0)) || (!reversed && (p < 0))) p(p * -1);
        }
        {
            double i = i(); if ((reversed && (i > 0)) || (!reversed && (i < 0))) i(i * -1);
        }
        {
            double d = i(); if ((reversed && (d > 0)) || (!reversed && (d < 0))) d(d * -1);
        }
        {
            double f = f(); if ((reversed && (f > 0)) || (!reversed && (f < 0))) f(f * -1);
        }
    }

    public final double p() {
        return P.doubleValue();
    }

    public final double i() {
        return I.doubleValue();
    }

    public final double d() {
        return D.doubleValue();
    }

    public final double f() {
        return F.doubleValue();
    }

    public final MiniPID setActual(double a) {
        actual = a;
        return this;
    }

    public MiniPID setOutput(double o) {
        out = o;
        return this;
    }
}