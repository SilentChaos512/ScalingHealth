package net.silentchaos512.scalinghealth.network;

import io.netty.buffer.ByteBuf;

public class ClientSyncMessage {
    public float playerDifficulty;
    public float worldDifficulty;
    public float areaDifficulty;

    public ClientSyncMessage() {}

    public ClientSyncMessage(float playerDifficulty, float worldDifficulty, float areaDifficulty) {
        this.playerDifficulty = playerDifficulty;
        this.worldDifficulty = worldDifficulty;
        this.areaDifficulty = areaDifficulty;
    }

    public static ClientSyncMessage fromBytes(ByteBuf buf) {
        ClientSyncMessage msg = new ClientSyncMessage();
        msg.playerDifficulty = buf.readFloat();
        msg.worldDifficulty = buf.readFloat();
        msg.areaDifficulty = buf.readFloat();
        return msg;
    }

    public void toBytes(ByteBuf buf) {
        buf.writeFloat(playerDifficulty);
        buf.writeFloat(worldDifficulty);
        buf.writeFloat(areaDifficulty);
    }
}
