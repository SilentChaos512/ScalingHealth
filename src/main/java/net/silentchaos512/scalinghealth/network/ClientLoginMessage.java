package net.silentchaos512.scalinghealth.network;

import net.minecraft.network.PacketBuffer;
import net.silentchaos512.scalinghealth.utils.mode.AreaDifficultyMode;

public class ClientLoginMessage {
    public AreaDifficultyMode areaMode;
    public float maxDifficultyValue;

    public ClientLoginMessage() {}

    public ClientLoginMessage(AreaDifficultyMode areaMode, float maxDifficultyValue) {
        this.areaMode = areaMode;
        this.maxDifficultyValue = maxDifficultyValue;
    }

    public static ClientLoginMessage fromBytes(PacketBuffer buf) {
        ClientLoginMessage msg = new ClientLoginMessage();
        try {
            msg.areaMode = buf.readWithCodec(AreaDifficultyMode.CODEC);
        } catch (Exception e) {
            throw new RuntimeException("Failed to receive difficulty mode packet!", e);
        }
        msg.maxDifficultyValue = buf.readFloat();
        return msg;
    }

    public void toBytes(PacketBuffer buf) {
        try {
            buf.writeWithCodec(AreaDifficultyMode.CODEC, areaMode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send difficulty mode packet!", e);
        }
        buf.writeFloat(maxDifficultyValue);
    }
}
