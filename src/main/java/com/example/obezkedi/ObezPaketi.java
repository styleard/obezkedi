package com.example.obezkedi;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ObezPaketi {
    private final int entityId;
    private final boolean obez;

    public ObezPaketi(int entityId, boolean obez) {
        this.entityId = entityId;
        this.obez = obez;
    }

    public static void encode(ObezPaketi p, PacketBuffer buf) {
        buf.writeInt(p.entityId);
        buf.writeBoolean(p.obez);
    }

    public static ObezPaketi decode(PacketBuffer buf) {
        return new ObezPaketi(buf.readInt(), buf.readBoolean());
    }

    public static void handle(ObezPaketi p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (p.obez) {
                ObezDurum.OBEZ.add(p.entityId);
            } else {
                ObezDurum.OBEZ.remove(p.entityId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
