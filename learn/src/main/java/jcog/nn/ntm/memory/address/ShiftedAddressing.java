package jcog.nn.ntm.memory.address;

import jcog.Util;
import jcog.activation.DiffableFunction;
import jcog.nn.ntm.control.Unit;
import jcog.nn.ntm.control.UnitFactory;
import jcog.nn.ntm.memory.address.content.BetaSimilarity;
import jcog.nn.ntm.memory.address.content.ContentAddressing;

public class ShiftedAddressing {
    private final GatedAddressing gatedAddressing;
    public final Unit[] shifted;
    private final Unit _shift;
    private final Unit[] gated;
    private final int conv;
    private final int cells;
    private final double simj;
    private final double shiftWeight;


    public ShiftedAddressing(Unit shift, GatedAddressing gatedAddressing, DiffableFunction act) {
        _shift = shift;
        this.gatedAddressing = gatedAddressing;
        gated = this.gatedAddressing.GatedVector;
        cells = gated.length;
        shifted = UnitFactory.vector(cells);

        shiftWeight = act.valueOf(_shift.value);
        double maxShift = ((2 * shiftWeight) - 1);
        double cellCountDbl = cells;
        double convolutionDbl = (maxShift + cellCountDbl) % cellCountDbl;
        simj = 1 - (convolutionDbl - Math.floor(convolutionDbl));

        int conv = this.conv = convToInt(convolutionDbl);

        int cells = this.cells;

        Unit[] shifted = this.shifted;
        Unit[] gated = this.gated;
        double simj = this.simj;

        for (int i = 0; i < cells; i++) {

            /*
            int imj = (i + _convolution) % _cellCount;

            vectorItem.Value = (_gatedVector[imj].Value * _simj) +
                   (_gatedVector[(imj + 1) % _cellCount].Value * oneMinusSimj);
            */

            Unit si = shifted[i];

            double v =
                Util.lerpSafe(simj,
                        gated[(i + conv + 1) % cells].value,
                        gated[(i + conv)     % cells].value);
//                (gated[imj].value * simj)
//                +
//                (gated[(imj + 1) % cells].value * oneMinusSimj);

            if (v < 0 || Double.isNaN(v))
                throw new RuntimeException("Error - weight should not be smaller than zero or nan");

            si.value = v;

        }
    }

    public static int convToInt(double c) {

        return (int) c;


    }

    public void backward() {

        double oneMinusSimj = 1 - simj;
        double gradient = 0;

        for (int i = 0; i < cells; i++) {

            /*
             Unit vectorItem = ShiftedVector[i];
                int imj = (i + (_convolution)) % _cellCount;
                gradient += ((-_gatedVector[imj].Value) + _gatedVector[(imj + 1) % _cellCount].Value) * vectorItem.Gradient;
                int j = (i - (_convolution) + _cellCount) % _cellCount;
                _gatedVector[i].Gradient += (vectorItem.Gradient * _simj) + (ShiftedVector[(j - 1 + _cellCount) % _cellCount].Gradient * _oneMinusSimj);

             */

            Unit vectorItem = shifted[i];
            int imj = (i + conv) % cells;
            gradient += ((-gated[imj].value) + gated[(imj + 1) % cells].value) * vectorItem.grad;
            int j = (i - conv + cells) % cells;
            gated[i].grad += (vectorItem.grad * simj) + (shifted[(j - 1 + cells) % cells].grad * oneMinusSimj);
        }
        _shift.grad += gradient * 2 * shiftWeight * (1 - shiftWeight);



        gatedAddressing.backward();

        ContentAddressing c = gatedAddressing.content;

        c.backward();

        for (BetaSimilarity s : c.similarities)
            s.backward();
    }

}