package com.direwolf20.mininggadgets.client.renderer;

import com.direwolf20.mininggadgets.common.items.gadget.MiningCollect;
import com.direwolf20.mininggadgets.common.items.gadget.MiningProperties;
import com.direwolf20.mininggadgets.common.util.VectorHelper;
import com.direwolf20.mininggadgets.setup.Registration;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.List;

public class BlockOverlayRender {

    public static void extract(java.util.List<Consumer<SubmitCustomGeometryEvent>> draws, ItemStack item) {
        final Minecraft mc = Minecraft.getInstance();


        int range = MiningProperties.getBeamRange(item);
        BlockHitResult lookingAt = VectorHelper.getLookingAt(mc.player, ClipContext.Fluid.NONE, range);
        if (mc.level.getBlockState(VectorHelper.getLookingAt(mc.player, item, range).getBlockPos()) == Blocks.AIR.defaultBlockState()) {
            return;
        }

        List<BlockPos> coords = MiningCollect.collect(mc.player, lookingAt, mc.level, MiningProperties.getRange(item), MiningProperties.getSizeMode(item));
        var positions = coords.stream().filter(pos -> mc.level.getBlockState(pos).getBlock() != Registration.RENDER_BLOCK.get()).map(BlockPos::immutable).toList();
        draws.add(event -> {
            Vec3 view = event.getLevelRenderState().cameraRenderState.pos;
            PoseStack matrix = event.getPoseStack();
            for (BlockPos pos : positions) {
                matrix.pushPose();
                matrix.translate(pos.getX()-view.x-.005, pos.getY()-view.y-.005, pos.getZ()-view.z-.005);
                matrix.scale(1.01f,1.01f,1.01f);
                matrix.rotate(Axis.YP.rotationDegrees(-90));
                event.getSubmitNodeCollector().submitCustomGeometry(matrix, MyRenderType.BLOCK_OVERLAY,
                    (pose, buffer) -> render(pose.pose(),buffer,pos,Color.GREEN));
                matrix.popPose();
            }
        });
    }

    public static void render(Matrix4f matrix, VertexConsumer builder, BlockPos pos, Color color) {
        float red = color.getRed() / 255f, green = color.getGreen() / 255f, blue = color.getBlue() / 255f, alpha = .125f;

        float startX = 0, startY = 0, startZ = -1, endX = 1, endY = 1, endZ = 0;

        //down
        builder.addVertex(matrix, startX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, startY, endZ).setColor(red, green, blue, alpha);

        //up
        builder.addVertex(matrix, startX, endY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, startZ).setColor(red, green, blue, alpha);

        //east
        builder.addVertex(matrix, startX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, startZ).setColor(red, green, blue, alpha);

        //west
        builder.addVertex(matrix, startX, startY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, endZ).setColor(red, green, blue, alpha);

        //south
        builder.addVertex(matrix, endX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, endZ).setColor(red, green, blue, alpha);

        //north
        builder.addVertex(matrix, startX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, startY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, startZ).setColor(red, green, blue, alpha);
    }
}
