package net.silentchaos512.scalinghealth.asm;

import java.util.Arrays;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.silentchaos512.scalinghealth.ScalingHealth;

public class SHClassTransformer implements IClassTransformer {

  static final String[] classesToTransform = { "net.minecraft.entity.SharedMonsterAttributes" };

  @Override
  public byte[] transform(String name, String transformedName, byte[] basicClass) {

    int index = Arrays.asList(classesToTransform).indexOf(transformedName);
    return index != -1 ? transform(index, basicClass, SHLoadingPlugin.isObf)
        : basicClass;
  }

  private byte[] transform(int index, byte[] basicClass, boolean isObf) {

    ScalingHealth.logHelper.info("Transforming class " + classesToTransform[index]);

    try {
      ClassNode node = new ClassNode();
      ClassReader reader = new ClassReader(basicClass);
      reader.accept(node, 0);

      switch (index) {
        case 0:
          transformSharedMonsterAttributes(node, isObf);
          break;
      }

      ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
      node.accept(writer);

      return writer.toByteArray();

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return basicClass;
  }

  private void transformSharedMonsterAttributes(ClassNode node, boolean isObf) {

    for (MethodNode method : node.methods) {
      //ScalingHealth.logHelper.info("  method: " + method.name + " " + method.desc);
      if (method.name.equals("<clinit>")) {
        for (int i = 0; i < method.instructions.size(); ++i) {
          AbstractInsnNode a = method.instructions.get(i);
          //ScalingHealth.logHelper.info("    " + a.getOpcode() + ", " + a.getType() + ", " + a.toString());

          // Replace the maximum value for MAX_HEALTH.
          if (a.getOpcode() == Opcodes.LDC && a.getNext().getOpcode() == Opcodes.INVOKESPECIAL) {
            if (a instanceof LdcInsnNode) {
              LdcInsnNode ldc = (LdcInsnNode) a;
              if (!(ldc.cst instanceof Double) || (double) ldc.cst != 1024D) {
                continue; // wrong value?
              }
            }
            method.instructions.insertBefore(a, new LdcInsnNode(SHAsmConfig.getValue(SHAsmConfig.MAX_HEALTH_MAX)));
            method.instructions.remove(a);
            ScalingHealth.logHelper.info("  Successfully changed max health maximum!");
            break;
          }
        }
      }
    }
  }
}
