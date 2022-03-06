/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jcog.signal.meter;

import jcog.data.list.Lst;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.LongColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * TODO use TableSaw Table
 */
public class TemporalMetrics  {

    public final Table data;
    final List<Consumer<Row>> update = new Lst();
    private final LongColumn time;

    public TemporalMetrics() {
        data = Table.create();
        data.addColumns(time = LongColumn.create("t"));
    }
    public TemporalMetrics on(String name, DoubleSupplier d) {
        synchronized (data) {
            DoubleColumn c = DoubleColumn.create(name);
            data.addColumns(c);
            int n = data.columnCount()-1;
            update.add(r -> r.setDouble(n, d.getAsDouble()));
        }
        return this;
    }

    public void commit(long time) {
        synchronized (data) {
            Row r = data.appendRow();
            r.setLong(0, time);
            for (Consumer<Row> c : update)
                c.accept(r);
//            data.addRow(r);
        }
    }


//
//
//
//
//
//
//    /** allows updating with an integer/long time, because it will be converted
//     * to double internally
//     */
//    public void update(long integerTime) {
//        update((double)integerTime);
//    }
//
//    public void add(String id, DoubleSupplier x) {
//        add(DoubleMeter.get(id, x));
//    }

}