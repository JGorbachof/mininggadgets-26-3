package com.direwolf20.mininggadgets.client.renderer;

import com.direwolf20.mininggadgets.common.MiningGadgets;
import com.direwolf20.mininggadgets.common.items.MiningGadget;
import com.direwolf20.mininggadgets.common.items.gadget.MiningProperties;
import com.direwolf20.mininggadgets.common.items.upgrade.Upgrade;
import com.direwolf20.mininggadgets.common.items.upgrade.UpgradeTools;
import com.direwolf20.mininggadgets.common.util.CodecHelpers;
import com.direwolf20.mininggadgets.setup.Registration;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import java.util.function.Consumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class RenderMiningLaser {

    private final static Identifier laserBeam = Identifier.fromNamespaceAndPath(MiningGadgets.MOD_ID, "textures/misc/laser.png");
    private final static Identifier laserBeam2 = Identifier.fromNamespaceAndPath(MiningGadgets.MOD_ID, "textures/misc/laser2.png");
    private final static Identifier laserBeamGlow = Identifier.fromNamespaceAndPath(MiningGadgets.MOD_ID, "textures/misc/laser_glow.png");

    public static void extractLaser(java.util.List<Consumer<SubmitCustomGeometryEvent>> draws, Player player, float ticks) {
        ItemStack stack = MiningGadget.getGadget(player);

        if (!MiningProperties.getCanMine(stack))
            return;

        int range = MiningProperties.getBeamRange(stack);

        Vec3 playerPos = player.getEyePosition(ticks);
        HitResult trace = player.pick(range, ticks, false);

        // parse data from item
        float speedModifier = getSpeedModifier(stack);

        CodecHelpers.LaserColor laserColor = MiningProperties.getColors(stack);

        float red = laserColor.red() / 255f;
        float green = laserColor.green() / 255f;
        float blue = laserColor.blue() / 255f;

        drawLasers(stack, draws, playerPos, trace, 0, 0, 0, red, green, blue, 0.02f, player, ticks, speedModifier);
    }

    private static float getSpeedModifier(ItemStack stack) {
        if (UpgradeTools.containsUpgrade(stack, Upgrade.EFFICIENCY_1)) {
            double efficiency = UpgradeTools.getUpgradeFromGadget(stack, Upgrade.EFFICIENCY_1).get().getTier() / 5f;
            double speedModifier = Mth.lerp(efficiency, 0.02, 0.05);
            return (float) -speedModifier;
        } else {
            return -0.02f;
        }
    }

    private static void drawLasers(ItemStack stack, java.util.List<Consumer<SubmitCustomGeometryEvent>> draws, Vec3 from, HitResult trace, double xOffset, double yOffset, double zOffset, float r, float g, float b, float thickness, Player player, float ticks, float speedModifier) {
        InteractionHand activeHand;
        if (player.getMainHandItem().getItem() instanceof MiningGadget) {
            activeHand = InteractionHand.MAIN_HAND;
        } else if (player.getOffhandItem().getItem() instanceof MiningGadget) {
            activeHand = InteractionHand.OFF_HAND;
        } else {
            return;
        }

        double distance = Math.max(1, from.subtract(trace.getLocation()).length());
        long gameTime = player.level().getGameTime();
        double v = gameTime * speedModifier;
        float additiveThickness = (thickness * 3.5f) * calculateLaserFlickerModifier(gameTime);

        CodecHelpers.LaserColor laserColor = MiningProperties.getColors(stack);

        float beam2r = laserColor.innerRed() / 255f;
        float beam2g = laserColor.innerGreen() / 255f;
        float beam2b = laserColor.innerBlue() / 255f;

        // Capture the complete beam pose during extraction; submission must not read the live world.
        float yaw = Mth.lerp(ticks, -player.yRotO, -player.getYRot());
        float pitch = Mth.lerp(ticks, player.xRotO, player.getXRot());
        boolean fancy = stack.is(Registration.MININGGADGET_FANCY.get());
        boolean simple = stack.is(Registration.MININGGADGET_SIMPLE.get());
        InteractionHand hand = player.getMainArm() == HumanoidArm.RIGHT ? activeHand :
                (activeHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        float sx = -0.20f, sy = -.108f, sz = .60f;
        if (fancy) sy += .02f;
        if (simple) { sx -= .02f; sz += .05f; sy -= .005f; }
        LocalPlayer local = Minecraft.getInstance().player;
        if (local == player) {
            float fov = local.getFieldOfViewModifier(false, 0);
            float setting = Minecraft.getInstance().options.fov().get();
            float adjustment = setting == 70 ? 0 : setting < 70 ? (setting / 70f) / .5f : -(setting / 70f) / 6f;
            sz += (1 - fov) * (fov >= 1 ? .5f : 1.5f) + adjustment;
        }
        if (hand == InteractionHand.OFF_HAND) { sy = -.120f; sx = .25f; }
        if (local == player) {
            sx += (Mth.lerp(ticks, local.yRotO, local.getYRot()) - Mth.lerp(ticks, local.yBobO, local.yBob)) / 750;
            sy += (Mth.lerp(ticks, local.xRotO, local.getXRot()) - Mth.lerp(ticks, local.xBobO, local.xBob)) / 750;
        }
        final float startX = sx, startY = sy, startZ = sz;
        draws.add(event -> {
            Vec3 view = event.getLevelRenderState().cameraRenderState.pos;
            PoseStack matrix = event.getPoseStack();
            matrix.pushPose();
            matrix.translate(from.x-view.x, from.y-view.y, from.z-view.z);
            matrix.rotate(Axis.YP.rotationDegrees(yaw));
            matrix.rotate(Axis.XP.rotationDegrees(pitch));
            var collector = event.getSubmitNodeCollector();
            collector.submitCustomGeometry(matrix, MyRenderType.LASER_MAIN_ADDITIVE, (pose, buffer) ->
                drawBeam(startX,startY,startZ,xOffset,yOffset,zOffset,buffer,pose.pose(),pose.normal(),additiveThickness,hand,distance,.5,1,r,g,b,.7f));
            collector.submitCustomGeometry(matrix, MyRenderType.LASER_MAIN_BEAM, (pose, buffer) ->
                drawBeam(startX,startY,startZ,xOffset,yOffset,zOffset,buffer,pose.pose(),pose.normal(),thickness,hand,distance,v,v+distance*1.5,r,g,b,1));
            collector.submitCustomGeometry(matrix, MyRenderType.LASER_MAIN_CORE, (pose, buffer) ->
                drawBeam(startX,startY,startZ,xOffset,yOffset,zOffset,buffer,pose.pose(),pose.normal(),thickness/2,hand,distance,v,v+distance*1.5,beam2r,beam2g,beam2b,1));
            matrix.popPose();
        });
    }

    private static float calculateLaserFlickerModifier(long gameTime) {
        return 0.9f + 0.1f * Mth.sin(gameTime * 0.99f) * Mth.sin(gameTime * 0.3f) * Mth.sin(gameTime * 0.1f);
    }

    private static void drawBeam(float startXOffset,float startYOffset,float startZOffset,double xOffset,double yOffset,double zOffset,VertexConsumer builder,Matrix4f positionMatrix,Matrix3f matrixNormalIn,float thickness,InteractionHand hand,double distance,double v1,double v2,float r,float g,float b,float alpha) {
        Vector3f vector3f = new Vector3f(0,1,0).mul(matrixNormalIn);
        Vector4f vec1 = new Vector4f(startXOffset, -thickness + startYOffset, startZOffset, 1.0F);
        vec1.mul(positionMatrix);
        Vector4f vec2 = new Vector4f((float) xOffset, -thickness + (float) yOffset, (float) distance + (float) zOffset, 1.0F);
        vec2.mul(positionMatrix);
        Vector4f vec3 = new Vector4f((float) xOffset, thickness + (float) yOffset, (float) distance + (float) zOffset, 1.0F);
        vec3.mul(positionMatrix);
        Vector4f vec4 = new Vector4f(startXOffset, thickness + startYOffset, startZOffset, 1.0F);
        vec4.mul(positionMatrix);

        if (hand == InteractionHand.MAIN_HAND) {
            builder.addVertex(vec4.x(), vec4.y(), vec4.z()).setColor(r, g, b, alpha).setUv(0, (float) v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
            builder.addVertex(vec3.x(), vec3.y(), vec3.z()).setColor(r, g, b, alpha).setUv(0, (float) v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
            builder.addVertex(vec2.x(), vec2.y(), vec2.z()).setColor(r, g, b, alpha).setUv(1, (float) v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
            builder.addVertex(vec1.x(), vec1.y(), vec1.z()).setColor(r, g, b, alpha).setUv(1, (float) v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
            //Rendering a 2nd time to allow you to see both sides in multiplayer, shouldn't be necessary with culling disabled but here we are....
            builder.addVertex(vec1.x(), vec1.y(), vec1.z()).setColor(r, g, b, alpha).setUv(1, (float) v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
            builder.addVertex(vec2.x(), vec2.y(), vec2.z()).setColor(r, g, b, alpha).setUv(1, (float) v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
            builder.addVertex(vec3.x(), vec3.y(), vec3.z()).setColor(r, g, b, alpha).setUv(0, (float) v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
            builder.addVertex(vec4.x(), vec4.y(), vec4.z()).setColor(r, g, b, alpha).setUv(0, (float) v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
        } else {
            builder.addVertex(vec1.x(), vec1.y(), vec1.z()).setColor(r, g, b, alpha).setUv(1, (float) v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
            builder.addVertex(vec2.x(), vec2.y(), vec2.z()).setColor(r, g, b, alpha).setUv(1, (float) v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
            builder.addVertex(vec3.x(), vec3.y(), vec3.z()).setColor(r, g, b, alpha).setUv(0, (float) v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
            builder.addVertex(vec4.x(), vec4.y(), vec4.z()).setColor(r, g, b, alpha).setUv(0, (float) v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
            //Rendering a 2nd time to allow you to see both sides in multiplayer, shouldn't be necessary with culling disabled but here we are....
            builder.addVertex(vec4.x(), vec4.y(), vec4.z()).setColor(r, g, b, alpha).setUv(0, (float) v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
            builder.addVertex(vec3.x(), vec3.y(), vec3.z()).setColor(r, g, b, alpha).setUv(0, (float) v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
            builder.addVertex(vec2.x(), vec2.y(), vec2.z()).setColor(r, g, b, alpha).setUv(1, (float) v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
            builder.addVertex(vec1.x(), vec1.y(), vec1.z()).setColor(r, g, b, alpha).setUv(1, (float) v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(vector3f.x(), vector3f.y(), vector3f.z());
        }
    }

}
