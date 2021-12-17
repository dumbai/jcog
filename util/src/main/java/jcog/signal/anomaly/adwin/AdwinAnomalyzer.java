package jcog.signal.anomaly.adwin;

public class AdwinAnomalyzer {

    private final AdwinHisto histo;
    private final AbstractAdwinAnomalyzer model;

    public AdwinAnomalyzer(int cap, double delta) {
        this(new AdwinHisto(cap), new AbstractAdwinAnomalyzer.SingleThreadAdwinModel(delta));
    }

    public AdwinAnomalyzer(AdwinHisto histo, AbstractAdwinAnomalyzer model) {
        this.histo = histo;
        this.model = model;
    }

    public boolean add(double x) {
        histo.add(x);
        return model.execute(histo);
    }


    public double mean() {
        return histo.mean();
    }

    public double variance() {
        return histo.variance();
    }

}