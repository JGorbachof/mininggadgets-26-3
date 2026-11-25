package com.direwolf20.mininggadgets.client.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class ModScreens {
    public static void openGadgetSettingsScreen(ItemStack itemstack) {
        Minecraft.getInstance().gui.setScreen(new MiningSettingScreen(itemstack));
    }

    public static void openVisualSettingsScreen(ItemStack itemstack) {
        Minecraft.getInstance().gui.setScreen(new MiningVisualsScreen(itemstack));
    }
}
