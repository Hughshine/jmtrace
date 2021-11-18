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
    private static class JMTraceAdaptor extends ClassVisitor {

        public JMTraceAdaptor(int api, ClassVisitor clzVisitor) {
            super(api, clzVisitor);  // this.cv = clzVisitor
        }

//        @Override
//        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
//
//            return;
//        }
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
        // first param `clzReader` is for optimization: do not parse when need no transformation
        ClassWriter clzWriter = new ClassWriter(clzReader, ClassWriter.COMPUTE_FRAMES);
        clzReader.accept(new JMTraceAdaptor(Opcodes.ASM8, clzWriter), 0);
//        clzReader.accept(clzWriter, 0);
        return clzWriter.toByteArray();
    }
}


