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
                    boolean isLongFormat = descriptor.startsWith("F") || descriptor.startsWith("J");
                    if (!inUserPkg(owner)) {
                        super.visitFieldInsn(opcode, owner, name, descriptor);
                        return;
                    }
                    if (opcode == Opcodes.GETFIELD) {
                        // objref
                        this.mv.visitInsn(Opcodes.DUP);
                        // objref, objref
                        super.visitFieldInsn(opcode, owner, name, descriptor);
                        // objref, value
                        if (!isLongFormat) {
                            this.mv.visitInsn(Opcodes.SWAP);
                        } else {
                            // objref, value
                            this.mv.visitInsn(Opcodes.DUP2_X1);
                            this.mv.visitInsn(Opcodes.POP2);
                            // value, objref
                        }
                        // value, objref
                        this.mv.visitInsn(Opcodes.ICONST_M1);
                        // value, objref, arrIndex
                        JMTracePrinterGen.instrument(this.mv, opcode, owner, name, descriptor);
                    }
                    else if (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC) {
                        super.visitFieldInsn(opcode, owner, name, descriptor);
                        this.mv.visitInsn(Opcodes.ACONST_NULL);
                        // null
                        this.mv.visitInsn(Opcodes.ICONST_M1);
                        // null, arrIndex
                        JMTracePrinterGen.instrument(this.mv, opcode, owner, name, descriptor);
                    }
                    else if (opcode == Opcodes.PUTFIELD) {
                        if (!isLongFormat) {
                            // objref, value
                            this.mv.visitInsn(Opcodes.SWAP);
                            // value, objref
                            this.mv.visitInsn(Opcodes.DUP_X1);
                            // objref, value, objref
                            this.mv.visitInsn(Opcodes.SWAP);
                            // objref, objref, value
                        } else {
                            // objref, value
                            this.mv.visitInsn(Opcodes.DUP2_X1);
                            this.mv.visitInsn(Opcodes.POP2);
                            // value, objref
                            this.mv.visitInsn(Opcodes.DUP_X2);
                            this.mv.visitInsn(Opcodes.DUP_X2);
                            // objref, objref, value, objref
                            this.mv.visitInsn(Opcodes.POP);
                            // objref, objref, value
                        }
                        super.visitFieldInsn(opcode, owner, name, descriptor);
                        // objref
                        this.mv.visitInsn(Opcodes.ICONST_M1);
                        // objref, arrIndex
                        JMTracePrinterGen.instrument(this.mv, opcode, owner, name, descriptor);
                    }
                    else {
                        super.visitFieldInsn(opcode, owner, name, descriptor);
                    }
                }

                // TODO: *ASTORE, *ALOAD
                /** ASTORE & ALOAD are not shared memory access, therefore not taken into consideration */
                @Override
                public void visitInsn(int opcode) {
//                    if (opcode >= Opcodes.IALOAD && opcode <= Opcodes.SALOAD) {
//                        if (opcode == Opcodes.FALOAD || opcode == Opcodes.DALOAD) {  // long format
//
//                        } else {
//                            // arrRef, index
//                            this.mv.visitInsn(Opcodes.DUP2);
//                            // arrRef, index, arrRef, index
//                        }
//                    } else if (opcode >= Opcodes.IASTORE && opcode <= Opcodes.SASTORE) {
//                        if (opcode == Opcodes.FASTORE || opcode == Opcodes.DASTORE) {
//
//                        } else {
//
//                        }
//                    }
                    super.visitInsn(opcode);
                    // arrRef, index, value
//                    JMTracePrinterGen.instrument(this.mv, opcode, "arrOwner", "arr", "[]");
                }
            };
        }
    }

    /**
     * format: R/W thrd-id object-hash access
     */
    public static class JMTracePrinterGen {
        final static String className =  "iser21/jmtrace/JMTraceTransformer$JMTracePrinterGen";
        final static String desc = "(Ljava/lang/Object;IZLjava/lang/String;Ljava/lang/String;)V";

        private static void instrument(MethodVisitor methodVisitor, int opcode, String owner, String name, String descriptor) {
//            methodVisitor.visitInsn(Opcodes.ICONST_0);  // arr index
            // add param into stack: [obj, arrIndex], isRead, owner, name
            boolean isRead = opcode==Opcodes.GETFIELD||opcode==Opcodes.GETSTATIC;

            if (isRead) {
                methodVisitor.visitInsn(Opcodes.ICONST_1);
            } else {
                methodVisitor.visitInsn(Opcodes.ICONST_0);
            }

            methodVisitor.visitLdcInsn(owner);
            methodVisitor.visitLdcInsn(name);

            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, JMTracePrinterGen.className, "printMemoryTrace"
                    , desc,false);
        }
        // will be instrumented with `invokestatic`
        // obj & arrIndex can only be known dynamically, as first two parameters
        public static void printMemoryTrace(Object obj, int arrayIndex,
                                            boolean readFlag, String className,
                                            String fieldName
                                      ) {
            String longFieldName = className + '.' + fieldName;
            if (obj != null && arrayIndex < 0) {
                System.out.printf("%s %d %08x%08x %s\n", readFlag ? 'R' : 'W',
                        Thread.currentThread().getId(), System.identityHashCode(obj),
                        longFieldName.hashCode(), longFieldName);
            } else if (obj != null && arrayIndex >= 0) {
                System.out.printf("%s %d %08x%08x %s[%d]\n", readFlag ? 'R' : 'W',
                        Thread.currentThread().getId(), System.identityHashCode(obj),
                        (longFieldName+arrayIndex).hashCode(), longFieldName, arrayIndex);
            } else if (obj == null && arrayIndex < 0) {
                System.out.printf("%s %d 00000000%08x %s\n", readFlag ? 'R' : 'W',
                        Thread.currentThread().getId(), longFieldName.hashCode(), longFieldName);
            } else {
                System.out.printf("%s %d 00000000%08x %s[%d]\n", readFlag ? 'R' : 'W',
                        Thread.currentThread().getId(), (longFieldName+arrayIndex).hashCode(), longFieldName, arrayIndex);
            }
        }

        public static void main(String[] args) {
            Object o = new Object();
//            printMemoryTrace(true, o, JMTracePrinterGen.className, "className", 0);
        }
    }

}


