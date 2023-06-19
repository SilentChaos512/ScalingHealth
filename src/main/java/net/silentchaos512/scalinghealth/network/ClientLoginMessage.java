package net.silentchaos512.scalinghealth.network;

import net.minecraft.network.FriendlyByteBuf;
import net.silentchaos512.scalinghealth.utils.mode.AreaDifficultyMode;

public class ClientLoginMessage {
    public AreaDifficultyMode areaMode;
    public float maxDifficultyValue;

    public ClientLoginMessage() {}

    public ClientLoginMessage(AreaDifficultyMode areaMode, float maxDifficultyValue) {
        this.areaMode = areaMode;
        this.maxDifficultyValue = maxDifficultyValue;
    }

    public static ClientLoginMessage fromBytes(FriendlyByteBuf buf) {
        ClientLoginMessage msg = new ClientLoginMessage();
        try {
            msg.areaMode = buf.readJsonWithCodec(AreaDifficultyMode.CODEC);
        } catch (Exception e) {
            throw new RuntimeException("Failed to receive difficulty mode packet!", e);
        }
        msg.maxDifficultyValue = buf.readFloat();
        return msg;
    }

    public void toBytes(FriendlyByteBuf buf) {
        try {
            buf.writeJsonWithCodec(AreaDifficultyMode.CODEC, areaMode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send difficulty mode packet!", e);
        }
        buf.writeFloat(maxDifficultyValue);
    }
}
