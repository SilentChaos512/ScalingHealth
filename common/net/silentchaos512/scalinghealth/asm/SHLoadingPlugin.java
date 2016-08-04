package net.silentchaos512.scalinghealth.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.silentchaos512.scalinghealth.ScalingHealth;

//@IFMLLoadingPlugin.MCVersion("1.9.4")
@IFMLLoadingPlugin.TransformerExclusions({"net.silentchaos512.scalinghealth.asm"})
public class SHLoadingPlugin implements IFMLLoadingPlugin {

  public static boolean isObf;

  @Override
  public String[] getASMTransformerClass() {

    return new String[] { "net.silentchaos512.scalinghealth.asm.SHClassTransformer" };
  }

  @Override
  public String getModContainerClass() {

    return null;
  }

  @Override
  public String getSetupClass() {

    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void injectData(Map<String, Object> data) {

    isObf = (Boolean) data.get("runtimeDeobfuscationEnabled");
    SHAsmConfig.init();
    SHAsmConfig.load();
    SHAsmConfig.save();
  }

  @Override
  public String getAccessTransformerClass() {

    // TODO Auto-generated method stub
    return null;
  }

}
