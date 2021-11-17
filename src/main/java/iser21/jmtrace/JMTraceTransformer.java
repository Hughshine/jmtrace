package iser21.jmtrace;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class JMTraceTransformer implements ClassFileTransformer {
    private static class JMTracePrinter {
        void print() {
            System.out.println("printer is here!!!");
        }
    }
    public byte[]
    transform(ClassLoader loader,
              String className,
              Class<?> classBeingRedefined,
              ProtectionDomain protectionDomain,
              byte[] classfileBuffer)
            throws IllegalClassFormatException {
        if (className.startsWith("java") || className.startsWith("sun") 
            || className.startsWith("jdk")) {
                return classfileBuffer;
            }
        System.out.println("I'm here! " + className);
        return classfileBuffer;
    }
}


