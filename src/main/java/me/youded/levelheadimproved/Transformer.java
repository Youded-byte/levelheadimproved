package me.youded.levelheadimproved;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class Transformer implements ClassFileTransformer {
    private Boolean foundLevelHeadImprovedFunction = false;
    private Boolean foundhttpfunction = false;
    private Boolean foundlevelstring = false;
    private String levelString;
    private int nickLevel;
    
    Transformer(String args){
        String[] parts = args.split("@");
        if(parts.length > 1) {
        try {this.nickLevel = Integer.parseInt(parts[1]);}
            catch(Exception e){ 
                e.printStackTrace();
                this.nickLevel = -1;
            }
        } else {
            this.nickLevel = -1;
        }
        this.levelString = parts[0];
    }
    private AbstractInsnNode nicklevelobject() {
        if(this.nickLevel == -1){
            return new InsnNode(Opcodes.ICONST_M1);
        } else {
            return new LdcInsnNode(this.nickLevel);
        }
    }
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (classfileBuffer == null || classfileBuffer.length == 0) {
            return new byte[0];
        }

        if(!className.startsWith("lunar/")){
            return classfileBuffer;
        }

        ClassReader cr = new ClassReader(classfileBuffer);
        if(cr.getSuperName().startsWith("lunar/") && cr.getInterfaces().length == 1) {
            ClassNode cn = new ClassNode();

            cr.accept(cn, 0);
            
            
            for (MethodNode method : cn.methods) {
                Boolean hasString = Arrays.stream(method.instructions.toArray())
                        .filter(LdcInsnNode.class::isInstance)
                        .map(LdcInsnNode.class::cast)
                        .map(inst -> inst.cst)
                        .anyMatch("Level: "::equals);
                
                if (hasString) {
                    if(!(this.foundLevelHeadImprovedFunction && this.foundlevelstring)){
                        for(AbstractInsnNode insn : method.instructions) {
                            if(insn.getOpcode() == Opcodes.BIPUSH && insn.getPrevious().getOpcode() == Opcodes.INVOKESTATIC && insn.getNext().getOpcode() == Opcodes.INVOKEVIRTUAL && insn.getNext().getNext().getOpcode() == Opcodes.BIPUSH && insn.getNext().getNext().getNext().getOpcode() == Opcodes.IADD) {
                                method.instructions.set(insn.getNext().getNext().getNext(), nicklevelobject());
                                method.instructions.remove(insn.getNext().getNext());
                                method.instructions.remove(insn.getNext());
                                method.instructions.remove(insn.getPrevious());
                                method.instructions.remove(insn);
                                this.foundLevelHeadImprovedFunction = true;
                            }
                        }
                        for(AbstractInsnNode insn : method.instructions) {
                            if (insn.getOpcode() == Opcodes.ILOAD && insn.getPrevious().getOpcode() == Opcodes.DUP && insn.getPrevious().getPrevious().getOpcode() == Opcodes.NEW && insn.getPrevious().getPrevious().getPrevious().getOpcode() == Opcodes.DUP && insn.getPrevious().getPrevious().getPrevious().getPrevious().getOpcode() == Opcodes.NEW){
                                method.instructions.set(insn.getNext(), new LdcInsnNode(this.levelString));
                                this.foundlevelstring = true;
                            }
                        }
                        
                    }
                }
            }
            if(this.foundLevelHeadImprovedFunction && !this.foundhttpfunction) {
                for (MethodNode method : cn.methods) {
                    for(AbstractInsnNode insn : method.instructions){
                        if(insn.getOpcode() == Opcodes.BIPUSH && insn.getPrevious().getOpcode() == Opcodes.INVOKESTATIC && insn.getNext().getOpcode() == Opcodes.INVOKEVIRTUAL && insn.getNext().getNext().getOpcode() == Opcodes.BIPUSH && insn.getNext().getNext().getNext().getOpcode() == Opcodes.IADD) {
                            method.instructions.set(insn.getNext().getNext().getNext(), nicklevelobject());
                            method.instructions.remove(insn.getNext().getNext());
                            method.instructions.remove(insn.getNext());
                            method.instructions.remove(insn.getPrevious());
                            method.instructions.remove(insn);
                            this.foundhttpfunction = true;
                        }
                    }
                }
            }

            ClassWriter cw = new ClassWriter(cr, 0);
            cn.accept(cw);
            return cw.toByteArray();
        }
        return classfileBuffer;
    }
}