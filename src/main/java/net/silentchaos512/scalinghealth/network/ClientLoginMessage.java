package net.silentchaos512.scalinghealth.network;

import io.netty.buffer.ByteBuf;
import net.silentchaos512.scalinghealth.lib.AreaDifficultyMode;

public class ClientLoginMessage {
    public AreaDifficultyMode areaMode;
    public float maxDifficultyValue;

    public ClientLoginMessage() {}

    public ClientLoginMessage(AreaDifficultyMode areaMode, float maxDifficultyValue) {
        this.areaMode = areaMode;
        this.maxDifficultyValue = maxDifficultyValue;
    }

    public static ClientLoginMessage fromBytes(ByteBuf buf) {
        ClientLoginMessage msg = new ClientLoginMessage();
        msg.areaMode = AreaDifficultyMode.fromOrdinal((int) buf.readByte());
        msg.maxDifficultyValue = buf.readFloat();
        return msg;
    }

    public void toBytes(ByteBuf buf) {
        buf.writeByte(areaMode.ordinal());
        buf.writeFloat(maxDifficultyValue);
    }
}
