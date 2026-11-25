package com.direwolf20.mininggadgets.client.renderer;

import com.direwolf20.mininggadgets.common.items.MiningGadget;
import com.direwolf20.mininggadgets.common.items.upgrade.Upgrade;
import com.direwolf20.mininggadgets.common.items.upgrade.UpgradeTools;
import com.direwolf20.mininggadgets.common.tiles.ModificationTableTileEntity;
import com.direwolf20.mininggadgets.setup.Registration;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import java.util.function.Consumer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

public class ModificationShiftOverlay {

    public static void extract(java.util.List<Consumer<SubmitCustomGeometryEvent>> draws, Player player) {
        HitResult pick = player.pick(5, 0, false);
        if (pick.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult trace = (BlockHitResult) pick;
        if (player.level().getBlockState(trace.getBlockPos()).getBlock() != Registration.MODIFICATION_TABLE.get()) {
            return;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(trace.getBlockPos());
        if (!(blockEntity instanceof ModificationTableTileEntity)) {
            return;
        }

        // Finally, lets try and render something if we have a gadget in the main slot
        ItemStack stack = ((ModificationTableTileEntity) blockEntity).handler.getResource(0).toStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof MiningGadget)) {
            return;
        }

        List<Upgrade> upgrades = UpgradeTools.getUpgrades(stack);
        if (upgrades.isEmpty()) {
            return;
        }

        BlockPos blockPos = trace.getBlockPos().immutable();
        double distance = player.position().distanceTo(Vec3.atCenterOf(blockPos));
        float scaleFactor = Math.max(.2f, ((float) distance / 10) + .1f);
        var states = upgrades.stream().map(upgrade -> {
            ItemStackRenderState state = new ItemStackRenderState();
            Minecraft.getInstance().getItemModelResolver().updateForLiving(state, new ItemStack(upgrade.getCardItem().get()), ItemDisplayContext.GUI, player);
            return state;
        }).toList();
        draws.add(evt -> {
            var camera = evt.getLevelRenderState().cameraRenderState;
            Vec3 view = camera.pos;
            PoseStack matrix = evt.getPoseStack();
            matrix.pushPose();
            matrix.translate(blockPos.getX()+.5-view.x, blockPos.getY()+1-view.y, blockPos.getZ()+.5-view.z);
            matrix.scale(scaleFactor, scaleFactor, scaleFactor);
            matrix.rotate(camera.orientation);
            float offset = states.size()/3 > 0 ? -1.15f : states.size()>1 ? -(states.size()/3f) : -.2f;
            for(int i=0;i<states.size();i++){
                matrix.pushPose();
                matrix.translate(offset+i%3,i/3,0);
                matrix.rotate(Axis.YP.rotationDegrees(90));
                matrix.rotate(Axis.XP.rotationDegrees(26));
                states.get(i).submit(matrix,evt.getSubmitNodeCollector(),15728880,OverlayTexture.NO_OVERLAY,0);
                matrix.popPose();
            }
            matrix.popPose();
        });
    }
}
