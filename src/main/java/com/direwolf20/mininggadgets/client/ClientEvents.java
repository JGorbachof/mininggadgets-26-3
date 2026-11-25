package com.direwolf20.mininggadgets.client;

import com.direwolf20.mininggadgets.client.renderer.BlockOverlayRender;
import com.direwolf20.mininggadgets.client.renderer.ModificationShiftOverlay;
import com.direwolf20.mininggadgets.client.renderer.RenderMiningLaser;
import com.direwolf20.mininggadgets.client.screens.ModScreens;
import com.direwolf20.mininggadgets.common.MiningGadgets;
import com.direwolf20.mininggadgets.common.items.MiningGadget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.minecraft.util.context.ContextKey;
import net.minecraft.resources.Identifier;
import java.util.function.Consumer;
import java.util.ArrayList;

import java.util.List;

@EventBusSubscriber(modid = MiningGadgets.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    //TODO look into how to do this now
//    @SubscribeEvent
//    static void drawBlockHighlightEvent(RenderHighlightEvent.Block evt) {
//        if (Minecraft.getInstance().player == null)
//            return;
//
//        if (MiningGadget.isHolding(Minecraft.getInstance().player))
//            evt.setCanceled(true);
//    }

    private static final ContextKey<List<Consumer<SubmitCustomGeometryEvent>>> DRAWS = new ContextKey<>(Identifier.fromNamespaceAndPath(MiningGadgets.MOD_ID,"draws"));
    @SubscribeEvent
    static void extractWorld(ExtractLevelRenderStateEvent evt) {
        List<Consumer<SubmitCustomGeometryEvent>> draws = new ArrayList<>();
        evt.getRenderState().setRenderData(DRAWS,draws);
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) return;
        List<AbstractClientPlayer> players = Minecraft.getInstance().level.players();
        Player myplayer = Minecraft.getInstance().player;

        ItemStack myItem = MiningGadget.getGadget(myplayer);
        if (myItem.getItem() instanceof MiningGadget)
            BlockOverlayRender.extract(draws, myItem);

        if (myplayer.isShiftKeyDown()) {
            ModificationShiftOverlay.extract(draws, myplayer);
        }

        for (Player player : players) {
            if (player.distanceToSqr(myplayer) > 500)
                continue;

            ItemStack heldItem = MiningGadget.getGadget(player);
            if (player.isUsingItem() && heldItem.getItem() instanceof MiningGadget) {
                if (MiningGadget.canMine(heldItem)) {
                    float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
                    RenderMiningLaser.extractLaser(draws, player, partialTick);
                }
            }
        }
    }

    @SubscribeEvent
    static void submitWorld(SubmitCustomGeometryEvent evt) {
        var draws = evt.getLevelRenderState().getRenderData(DRAWS);
        if (draws != null) draws.forEach(draw -> draw.accept(evt));
    }

    @SubscribeEvent
    static void keyPressed(InputEvent.Key event) {
        if (OurKeys.shiftClickGuiBinding.consumeClick() && Minecraft.getInstance().gui.screen() == null) {
            if (Minecraft.getInstance().player == null) {
                return;
            }

            ItemStack gadget = MiningGadget.getGadget(Minecraft.getInstance().player);
            if (gadget.isEmpty()) {
                return;
            }

            ModScreens.openGadgetSettingsScreen(gadget);
        }
    }
}
