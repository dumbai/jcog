package jcog.agent;

import jcog.signal.Tensor;
import jcog.signal.tensor.TensorRing;
import jcog.signal.tensor.TensorSerial;

import java.util.List;

public class SensorTensor {

	protected final Tensor sensors;

	/** TODO array */
	protected final List<Tensor> _sensors;

	protected final TensorSerial sensorsNow;

	public int volume() {
		return sensors.volume();
	}

	public SensorTensor(List<Tensor> _sensors, int history) {
		this._sensors = _sensors;
		sensorsNow = new TensorSerial(_sensors);
		this.sensors = history > 0 ? new TensorRing(sensorsNow.volume(), history) : sensorsNow;
	}

	public Tensor update() {
		/* @Deprecated  */
		for (Tensor _sensor : _sensors) _sensor.snapshot();

		sensorsNow.update();

		if (sensors instanceof TensorRing)
			((TensorRing)sensors).setSpin(sensorsNow.snapshot()); //HACK bad


		return sensors;
	}


}