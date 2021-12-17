package jcog.agent;

import jcog.data.list.Lst;
import jcog.math.FloatSupplier;
import jcog.signal.Tensor;
import jcog.signal.tensor.ScalarTensor;

import java.util.List;

public class SensorBuilder {
	final List<Tensor> sensors = new Lst();

	public int size() {
		return sensors.stream().mapToInt(Tensor::volume).sum(); //TODO dimensionality
	}

	/** allow history individually for each input using Rolling tensor */
	@Deprecated int history = 0;

	@Deprecated public <S extends SensorBuilder> S history(int h) { this.history = h; return (S)this;}

	public <S extends SensorBuilder> S in(FloatSupplier f) {
		sensors.add(new ScalarTensor(f));
		return (S) this;
	}

	public <S extends SensorBuilder> S in(Tensor t) {
		sensors.add(t);
		return (S) this;
	}

	public SensorTensor sensor() {
		return new SensorTensor(sensors, history);
	}
}