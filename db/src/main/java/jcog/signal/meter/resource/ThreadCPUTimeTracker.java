/* Copyright 2009 - 2010 The Stajistics Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http:
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcog.signal.meter.resource;

import jcog.signal.meter.event.DoubleMeter;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

/**
 * CPU Time, in milliseconds
 *
 * @author The Stajistics Project
 */
public class ThreadCPUTimeTracker extends DoubleMeter {

    public ThreadCPUTimeTracker(String id) {
        super(id);
    }

    public double get() {
        ThreadMXBean threadMXBean1 = getThreadMXBean();
        double sum = Arrays.stream(getThreadMXBean().getAllThreadIds()).mapToDouble(threadMXBean1::getThreadUserTime).sum();
        return sum;
                //.getCurrentThreadCpuTime();
    }


    
   private static volatile boolean hasSetContentionMonitoringEnabled;
    private static volatile boolean hasSetCPUTimeMonitoringEnabled;

    private static boolean contentionMonitoringEnabled;
    private static boolean cpuTimeMonitoringEnabled = true;

    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();    

    protected static ThreadMXBean getThreadMXBean() {
        return threadMXBean;
    }

    protected static void ensureContentionMonitoringEnabled() {
        if (!hasSetContentionMonitoringEnabled) {
            hasSetContentionMonitoringEnabled = true;

            if (threadMXBean.isThreadContentionMonitoringSupported()) {
                threadMXBean.setThreadContentionMonitoringEnabled(true);
                contentionMonitoringEnabled = true;

                

            } else {
                System.err.println("Thread contention monitoring is not supported in this JVM; "
                        + "Thread contention related trackers will be silent");
            }
        }
    }

    protected static boolean isContentionMonitoringEnabled() {
        return contentionMonitoringEnabled;
    }

    protected static void ensureCPUTimeMonitoringEnabled() {
        if (!hasSetCPUTimeMonitoringEnabled) {
            hasSetCPUTimeMonitoringEnabled = true;

            if (threadMXBean.isCurrentThreadCpuTimeSupported()) {
                threadMXBean.setThreadCpuTimeEnabled(true);
                cpuTimeMonitoringEnabled = true;

                
            } else {
                System.err.println("Thread CPU time monitoring is not supported in this JVM; "
                        + "Thread CPU time related trackers will be silent");
            }
        }
    }

    protected static boolean isCPUTimeMonitoringEnabled() {
        return cpuTimeMonitoringEnabled;
    }

    protected static ThreadInfo getCurrentThreadInfo() {
        if (contentionMonitoringEnabled) {
            return threadMXBean.getThreadInfo(Thread.currentThread().getId(), 0);
        }

        return null;
    }
}
