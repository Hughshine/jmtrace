package iser21.jmtrace;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;

public class JMTraceTransformer implements ClassFileTransformer {

    static boolean inUserPkg(String pkg) {
        return !(pkg.startsWith("java") || pkg.startsWith("sun") || pkg.startsWith("jdk"));
    }

    public byte[]
    transform(ClassLoader loader,
              String className,
              Class<?> classBeingRedefined,
              ProtectionDomain protectionDomain,
              byte[] classfileBuffer)
            throws IllegalClassFormatException {
        if (!inUserPkg(className)) {
            return null;  // return null is OK
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
                    if (!inUserPkg(owner)) {
                        super.visitFieldInsn(opcode, owner, name, descriptor);
                        return;
                    }
                    if (opcode == Opcodes.GETFIELD) {
                        this.mv.visitInsn(Opcodes.DUP);
                        super.visitFieldInsn(opcode, owner, name, descriptor);
                        this.mv.visitInsn(Opcodes.SWAP);
                        JMTracePrinterGen.instrument(this.mv, opcode, owner, name, descriptor);
                    }
                    if (opcode == Opcodes.GETSTATIC) {
                        super.visitFieldInsn(opcode, owner, name, descriptor);  // T.g 这种情况，reference是什么？
                        this.mv.visitInsn(Opcodes.ACONST_NULL);
//                        System.out.println(descriptor);
                        JMTracePrinterGen.instrument(this.mv, opcode, owner, name, descriptor);
                    }
                    if (opcode == Opcodes.PUTSTATIC || opcode == Opcodes.PUTFIELD) {
                        super.visitFieldInsn(opcode, owner, name, descriptor);
//                        this.mv.visitInsn(Opcodes.DUP);
                        this.mv.visitInsn(Opcodes.ACONST_NULL);
                        JMTracePrinterGen.instrument(this.mv, opcode, owner, name, descriptor);
                    }
                }
                // TODO: *ASTORE, *ALOAD
            };
        }
    }

    /**
     * format: R/W thrd-id object-hash access
     */
    public static class JMTracePrinterGen {
        final static String className =  "iser21/jmtrace/JMTraceTransformer$JMTracePrinterGen";
        final static String desc = "(Ljava/lang/Object;ZLjava/lang/String;Ljava/lang/String;I)V";
        final static String bool_desc = "(Z)V";
        // 有问题，这里其实不关注discriptor，因为它只是field. 这里进来的array访问，都是从object中取arr得到的.
        private static void instrument(MethodVisitor methodVisitor, int opcode, String owner, String name, String descriptor) {
            // add param into stack
            boolean isRead = opcode==Opcodes.GETFIELD||opcode==Opcodes.GETSTATIC;
            boolean isStatic = opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC;
            boolean isArray = descriptor.startsWith("[");

//            if (isStatic) {
//                methodVisitor.visitInsn(Opcodes.ACONST_NULL);
//            } else {
//                methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
//                methodVisitor.visitInsn(Opcodes.ACONST_NULL);  // TODO: changed to visited object
//            }

            if (isRead) {
                methodVisitor.visitInsn(Opcodes.ICONST_1);
            } else {
                methodVisitor.visitInsn(Opcodes.ICONST_0);
            }

            methodVisitor.visitLdcInsn(owner);
            methodVisitor.visitLdcInsn(name);
            if (isArray) {
                methodVisitor.visitInsn(Opcodes.ICONST_M1);  // TODO: pass array index
            } else {
                methodVisitor.visitInsn(Opcodes.ICONST_M1);
            }

            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, JMTracePrinterGen.className, "printMemoryTrace"
                    , desc,false);
        }
        public static void printBool(boolean readFlag) {
            System.out.println("Here! " + readFlag);
        }
        // will be instrumented with invokestatic to print thrd-id and object id
        public static void printMemoryTrace(Object obj, boolean readFlag,
                                      String className, String fieldName,
                                      int arrayIndex
                                      ) {
            String longFieldName = className + '.' + fieldName;
            if (obj != null && arrayIndex < 0) {
                System.out.printf("%s %d %08x%08x %s\n", readFlag ? 'R' : 'W',
                        Thread.currentThread().getId(), System.identityHashCode(obj),
                        System.identityHashCode(longFieldName), longFieldName);
            } else if (obj != null && arrayIndex >= 0) {
                System.out.printf("%s %d %08x%08x %s[%d]\n", readFlag ? 'R' : 'W',
                        Thread.currentThread().getId(), System.identityHashCode(obj),
                        System.identityHashCode(longFieldName+arrayIndex), longFieldName, arrayIndex);
            } else if (obj == null && arrayIndex < 0) {
                System.out.printf("%s %d 00000000%08x %s\n", readFlag ? 'R' : 'W',
                        Thread.currentThread().getId(), System.identityHashCode(longFieldName), longFieldName);
            } else {
                System.out.printf("%s %d 00000000%08x %s[%d]\n", readFlag ? 'R' : 'W',
                        Thread.currentThread().getId(), System.identityHashCode(longFieldName+arrayIndex), longFieldName, arrayIndex);
            }
        }

        public static void main(String[] args) {
            Object o = new Object();
//            printMemoryTrace(true, o, JMTracePrinterGen.className, "className", 0);
        }
    }

}


