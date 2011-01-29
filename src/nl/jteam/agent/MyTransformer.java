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

    private String awtAppClassName;

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

            reader.accept(new MyClassAdapter(writer, awtAppClassName), 0);

            result = writer.toByteArray();

        } else {
            result = null; 
        }

        return result;
    }

    private static class MyClassAdapter extends ClassAdapter {

        private String awtAppClassName;

        private MyClassAdapter(ClassVisitor cv, String awtAppClassName) {
            super(cv);
            this.awtAppClassName= awtAppClassName;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            return new MyMethodVisitor(mv, awtAppClassName);
        }

    }

    private static class MyMethodVisitor extends MethodAdapter {

        private String awtAppClassName;

        private MyMethodVisitor(MethodVisitor methodVisitor, String awtAppClassName) {
            super(methodVisitor);
            this.awtAppClassName = awtAppClassName;
        }


        @Override
        public void visitLdcInsn(Object o) {
            super.visitLdcInsn(o);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            super.visitMethodInsn(opcode, owner, name, desc);
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
