package iser21.jmtrace;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class JMTraceTransformer implements ClassFileTransformer {
    private static class JMTracePrinter {
        void print() {
            System.out.println("printer is here!!!");
        }
    }
    private static class JMTraceVisitor extends MethodVisitor {

        public JMTraceVisitor(int api, ClassVisitor classVisitor) {
            // super(api, classVisitor);
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
        ClassReader clzReader = new ClassReader(classfileBuffer);
        ClassWriter clzWriter = new ClassWriter(clzReader, ClassWriter.COMPUTE_FRAMES);
        // ClassVisitor clzVisitor = new JMTraceVisitor(clzWriter);
        return classfileBuffer;
    }
}


