package com.example.obezkedi;

import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod(ObezKedi.MODID)
public class ObezKedi {
    public static final String MODID = "obezkedi";
    public static final String TAG_COUNT = "ObezKediBalikSayisi";
    /** Bu kadar balıktan sonra kedi obez olur. */
    public static final int ESIK = 8;

    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public ObezKedi() {
        CHANNEL.registerMessage(0, ObezPaketi.class, ObezPaketi::encode, ObezPaketi::decode, ObezPaketi::handle);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static boolean obezMi(CatEntity cat) {
        return cat.getPersistentData().getInt(TAG_COUNT) >= ESIK;
    }

    @SubscribeEvent
    public void kediyiBeslediginde(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof CatEntity)) return;
        if (event.getHand() != Hand.MAIN_HAND) return;
        CatEntity cat = (CatEntity) event.getTarget();
        ItemStack stack = event.getItemStack();
        if (stack.getItem() != Items.COD && stack.getItem() != Items.SALMON) return;
        if (!cat.isTamed() || !cat.isOwner(event.getPlayer())) return;

        // Hem istemcide hem sunucuda iptal et ki oturma/kalkma tetiklenmesin
        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.SUCCESS);
        if (event.getWorld().isRemote) return;

        if (!event.getPlayer().abilities.isCreativeMode) stack.shrink(1);
        cat.heal(2.0F);

        CompoundNBT data = cat.getPersistentData();
        int sayi = data.getInt(TAG_COUNT) + 1;
        data.putInt(TAG_COUNT, sayi);

        ((ServerWorld) event.getWorld()).spawnParticle(ParticleTypes.HEART,
                cat.getPosXRandom(0.5D), cat.getPosYRandom(), cat.getPosZRandom(0.5D), 3, 0.3, 0.3, 0.3, 0.02);

        if (sayi == ESIK) {
            event.getPlayer().sendStatusMessage(new StringTextComponent("Kedin çok şişmanladı!"), true);
            gonder(cat);
        }
    }

    private static void gonder(CatEntity cat) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> cat),
                new ObezPaketi(cat.getEntityId(), true));
    }

    /** Kediyi yeni gören oyuncuya durumunu bildir. */
    @SubscribeEvent
    public void takipBasladi(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof CatEntity && obezMi((CatEntity) event.getTarget())
                && event.getPlayer() instanceof ServerPlayerEntity) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                    new ObezPaketi(event.getTarget().getEntityId(), true));
        }
    }
}
