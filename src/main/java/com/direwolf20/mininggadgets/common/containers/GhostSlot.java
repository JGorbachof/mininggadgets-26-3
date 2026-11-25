package com.direwolf20.mininggadgets.common.containers;

import com.direwolf20.mininggadgets.common.items.MiningGadget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import com.direwolf20.mininggadgets.common.items.UpgradeCard;

import javax.annotation.Nonnull;

public class GhostSlot extends Slot {
    public GhostSlot(Container itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void set(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || mayPlace(stack)) super.set(stack.copyWithCount(1));
    }

    @Override public boolean isFake() { return true; }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }


    @Override
    public void onTake(Player thePlayer, ItemStack stack) {
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return !(stack.getItem() instanceof MiningGadget || stack.getItem() instanceof UpgradeCard) && stack.getItem().canFitInsideContainerItems();
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        return 1;
    }
}
