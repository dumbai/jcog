/*
 * The MIT License
 *
 * Copyright 2015 Kamnev Georgiy (nt.gocha@gmail.com).
 *
 * Данная лицензия разрешает, безвозмездно, лицам, получившим копию данного программного
 * обеспечения и сопутствующей документации (в дальнейшем именуемыми "Программное Обеспечение"),
 * использовать Программное Обеспечение без ограничений, включая неограниченное право на
 * использование, копирование, изменение, объединение, публикацию, распространение, сублицензирование
 * и/или продажу копий Программного Обеспечения, также как и лицам, которым предоставляется
 * данное Программное Обеспечение, при соблюдении следующих условий:
 *
 * Вышеупомянутый копирайт и данные условия должны быть включены во все копии
 * или значимые части данного Программного Обеспечения.
 *
 * ДАННОЕ ПРОГРАММНОЕ ОБЕСПЕЧЕНИЕ ПРЕДОСТАВЛЯЕТСЯ «КАК ЕСТЬ», БЕЗ ЛЮБОГО ВИДА ГАРАНТИЙ,
 * ЯВНО ВЫРАЖЕННЫХ ИЛИ ПОДРАЗУМЕВАЕМЫХ, ВКЛЮЧАЯ, НО НЕ ОГРАНИЧИВАЯСЬ ГАРАНТИЯМИ ТОВАРНОЙ ПРИГОДНОСТИ,
 * СООТВЕТСТВИЯ ПО ЕГО КОНКРЕТНОМУ НАЗНАЧЕНИЮ И НЕНАРУШЕНИЯ ПРАВ. НИ В КАКОМ СЛУЧАЕ АВТОРЫ
 * ИЛИ ПРАВООБЛАДАТЕЛИ НЕ НЕСУТ ОТВЕТСТВЕННОСТИ ПО ИСКАМ О ВОЗМЕЩЕНИИ УЩЕРБА, УБЫТКОВ
 * ИЛИ ДРУГИХ ТРЕБОВАНИЙ ПО ДЕЙСТВУЮЩИМ КОНТРАКТАМ, ДЕЛИКТАМ ИЛИ ИНОМУ, ВОЗНИКШИМ ИЗ, ИМЕЮЩИМ
 * ПРИЧИНОЙ ИЛИ СВЯЗАННЫМ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ ИЛИ ИСПОЛЬЗОВАНИЕМ ПРОГРАММНОГО ОБЕСПЕЧЕНИЯ
 * ИЛИ ИНЫМИ ДЕЙСТВИЯМИ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ.
 */

package jcog.reflect;


import jcog.WTF;
import jcog.data.graph.Node;
import jcog.data.graph.path.FromTo;
import jcog.data.graph.path.Path;
import jcog.data.list.Lst;

import java.util.function.Function;

/**
 * Последовательность caster-ов
 *
 * @author Kamnev Georgiy (nt.gocha@gmail.com)
 */
public class Converter<X,Y> extends MutableWeightedCaster<X,Y> {
    
    protected static final double defaultItemWeight = 1;

    private final Function[] functionsArray;

    protected Function F;

    public static <X,Y> Converter<X,Y> the(Path<Class, Function> path) {
        int steps = path.nodeCount();
        Lst<Function> functions = new Lst<>(steps);
        for (FromTo<Node<Class, Function>, Function> ed : path.fetch(0, steps)) {
            if (ed != null) {
                functions.add(ed.id());
            }
        }
        return new Converter<>(functions);
    }

    public Converter(Lst<Function> functions) {

        switch (functions.size()) {
            case 0 -> throw new WTF();
            case 1 -> this.functionsArray = new Function[]{this.F = functions.get(0)};
            default -> {
                Function[] ff = functions.toArrayRecycled(Function[]::new);
                this.functionsArray = ff;
                this.F = (x) -> {
                    Object y = x;
                    for (Function f : ff)
                        y = f.apply(y);

                    return y;
                };
            }
        }



//        path.stream().filter((ed) -> ( ed!=null )).forEach((ed) -> {
//            this.convertors.addAt(ed.getEdge());
//        });

        for (Function c : functions) {
            if (c instanceof WeightChangeSender) {
                WeightChangeListener listener = event -> {
                    Double oldw = Converter.this.weight;
                    Converter.this.weight = Double.NaN;
                    fireEvent(oldw, null);
                };
                ((WeightChangeSender) c).addWeightChangeListener(listener, true);
            }
        }
    }




    public Function[] getConvertors() {
        return functionsArray;
    }

    @Override
    public Y apply(X from) {
        return (Y) F.apply(from);
    }

    @Override
    public double weight() {
        double weight = this.weight;
        if (weight==weight) return weight;

        double w = 0;
        for (Function conv : functionsArray) {
            if (conv instanceof PrioritizedDouble) {
                double wc = ((PrioritizedDouble) conv).weight();
                w += wc == wc ? wc : defaultItemWeight;
            } else {
                w += defaultItemWeight;
            }
        }

        this.weight = w;
        fireEvent(null, w);
        return w;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Object w = weight();
        sb.append("Sequence");
        sb.append(" w=").append(w);
        sb.append(" {");
        int i = -1;
        for (Object conv : getConvertors()) {
            i++;
            Object wc = defaultItemWeight;
            if (conv instanceof PrioritizedDouble) {
                wc = ((PrioritizedDouble) conv).weight();
            }
            if (i > 0) sb.append(", ");
            sb.append(conv).append(" w=").append(wc);
        }
        sb.append('}');
        return sb.toString();
    }
}