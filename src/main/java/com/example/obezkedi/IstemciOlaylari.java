package com.example.obezkedi;

import net.minecraft.entity.passive.CatEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ObezKedi.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IstemciOlaylari {

    /** Obez kedileri yana doğru genişleterek şişman gösterir. */
    @SubscribeEvent
    public static void kediCizilirken(RenderLivingEvent.Pre<?, ?> event) {
        if (event.getEntity() instanceof CatEntity
                && ObezDurum.OBEZ.contains(event.getEntity().getEntityId())) {
            event.getMatrixStack().scale(1.35F, 1.0F, 1.35F);
        }
    }

    @SubscribeEvent
    public static void dunyaKapanirken(WorldEvent.Unload event) {
        if (event.getWorld().isRemote()) {
            ObezDurum.OBEZ.clear();
        }
    }
}
