package iser21.jmtrace;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;

public class JMTraceTransformer implements ClassFileTransformer {

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
        ClassReader clzReader = new ClassReader(classfileBuffer);
        // first param `clzReader` is for optimization: do not parse when need no transformation
        ClassWriter clzWriter = new ClassWriter(clzReader, ClassWriter.COMPUTE_FRAMES);
        clzReader.accept(new JMTraceAdaptor(Opcodes.ASM8, clzWriter), 0);
        return clzWriter.toByteArray();
    }


    /**
     * focus on `getstatic/putstatic/getfield/putfield/*aload/*astore`
     */
    private static class JMTraceAdaptor extends ClassVisitor {
        public JMTraceAdaptor(int api, ClassVisitor clzVisitor) {
            super(api, clzVisitor);  // this.cv = clzVisitor
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return new MethodVisitor(this.api,
                    super.visitMethod(access, name, descriptor, signature, exceptions)) {
                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                    super.visitFieldInsn(opcode, owner, name, descriptor);
                    JMTracePrinterGen.instrument(this.mv, opcode, owner, name, descriptor);
                }
                // TODO: *ASTORE, *ALOAD
            };
        }
    }

    /**
     * format: R/W thrd-id object-hash access
     */
    public static class JMTracePrinterGen {
        final static String className =  JMTracePrinterGen.class.getCanonicalName().replace(".", "/");

        final static String desc = "(L)V";
        private static void instrument(MethodVisitor methodVisitor, int opcode, String owner, String name, String descriptor) {
            // add param into stack
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, JMTracePrinterGen.className, "printMemoryTrace"
                    , desc,false);
        }
        // will be instrumented with invokestatic to print thrd-id and object id
        public static void printMemoryTrace(boolean readFlag,
                                      Object obj, String className, String fieldName,
                                      int arrayIndex
                                      ) {
            String longFieldName = className + '.' + fieldName;
            if (obj != null && arrayIndex < 0) {
                System.out.printf("%s %d %x%x %s\n", readFlag ? 'R' : 'W',
                        Thread.currentThread().getId(), System.identityHashCode(obj),
                        System.identityHashCode(longFieldName), longFieldName);
            } else if (obj != null && arrayIndex >= 0) {
                System.out.printf("%s %d %x%x %s[%d]\n", readFlag ? 'R' : 'W',
                        Thread.currentThread().getId(), System.identityHashCode(obj),
                        System.identityHashCode(longFieldName+arrayIndex), longFieldName, arrayIndex);
            } else if (obj == null && arrayIndex < 0) {
                System.out.printf("%s %d 00000000%x %s\n", readFlag ? 'R' : 'W',
                        Thread.currentThread().getId(), System.identityHashCode(longFieldName), longFieldName);
            } else {
                System.out.printf("%s %d 00000000%x %s[%d]\n", readFlag ? 'R' : 'W',
                        Thread.currentThread().getId(), System.identityHashCode(longFieldName+arrayIndex), longFieldName, arrayIndex);
            }
        }

        public static void main(String[] args) {
            Object o = new Object();
            printMemoryTrace(true, o, JMTracePrinterGen.className, "className", 0);
        }
    }

}


