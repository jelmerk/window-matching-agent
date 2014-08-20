/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.jteam.agent;

import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * @author Jelmer Kuperus
 */
public class MyTransformer implements ClassFileTransformer {

    private final String awtAppClassName;

    public MyTransformer(String awtAppClassName) {
        this.awtAppClassName = awtAppClassName;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {

        byte[] result;
        if ("sun/awt/X11/XToolkit".equals(className)) {

            ClassReader reader = new ClassReader(classfileBuffer);
            ClassWriter writer = new ClassWriter(0);

            reader.accept(new MyClassVisitor(writer, awtAppClassName), 0);

            result = writer.toByteArray();

        } else {
            result = null; 
        }

        return result;
    }

    private static class MyClassVisitor extends ClassVisitor {

        private final String awtAppClassName;

        private MyClassVisitor(ClassVisitor cv, String awtAppClassName) {
            super(Opcodes.ASM5, cv);
            this.awtAppClassName= awtAppClassName;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            return new MyMethodVisitor(mv, awtAppClassName);
        }

    }

    private static class MyMethodVisitor extends MethodVisitor {

        private final String awtAppClassName;

        private MyMethodVisitor(MethodVisitor methodVisitor, String awtAppClassName) {
            super(Opcodes.ASM5, methodVisitor);
            this.awtAppClassName = awtAppClassName;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if ("<init>".equals(name)) {
                visitLdcInsn(awtAppClassName);
                visitFieldInsn(Opcodes.PUTSTATIC, "sun/awt/X11/XToolkit", "awtAppClassName", "Ljava/lang/String;");
            }
        }
    }

    public static void premain(String agentArguments, Instrumentation instrumentation) {
        instrumentation.addTransformer(new MyTransformer(agentArguments));
    }

}
