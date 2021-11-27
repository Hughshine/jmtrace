package iser21.jmtrace;

import java.lang.instrument.Instrumentation;

public class JMTraceAgent {
    public static void premain(String args, Instrumentation inst) throws Throwable {
        try {
            inst.addTransformer(new JMTraceTransformer());
        } catch (Exception e) {
            throw e.getCause();
        }
    }
    public static void agentmain(String args, Instrumentation inst) {
        throw new UnsupportedOperationException();
    }
    public static void main(String[] args) {
        System.out.println("Hello world");
    }
}
