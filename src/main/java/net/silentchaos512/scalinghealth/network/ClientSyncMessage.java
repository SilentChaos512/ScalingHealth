package net.silentchaos512.scalinghealth.network;

import net.minecraft.network.PacketBuffer;

public class ClientSyncMessage {
    public float playerDifficulty;
    public float worldDifficulty;
    public float areaDifficulty;
    public int regenTimer;

    public ClientSyncMessage() {}

    public ClientSyncMessage(float playerDifficulty, float worldDifficulty, float areaDifficulty, int regenTimer) {
        this.playerDifficulty = playerDifficulty;
        this.worldDifficulty = worldDifficulty;
        this.areaDifficulty = areaDifficulty;
        this.regenTimer = regenTimer;
    }

    public static ClientSyncMessage fromBytes(PacketBuffer buf) {
        ClientSyncMessage msg = new ClientSyncMessage();
        msg.playerDifficulty = buf.readFloat();
        msg.worldDifficulty = buf.readFloat();
        msg.areaDifficulty = buf.readFloat();
        msg.regenTimer = buf.readVarInt();
        return msg;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeFloat(playerDifficulty);
        buf.writeFloat(worldDifficulty);
        buf.writeFloat(areaDifficulty);
        buf.writeVarInt(regenTimer);
    }
}
