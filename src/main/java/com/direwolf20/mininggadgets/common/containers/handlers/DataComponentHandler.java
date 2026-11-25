package com.direwolf20.mininggadgets.common.containers.handlers;

import com.direwolf20.mininggadgets.setup.MGDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public class DataComponentHandler implements Container {
    private final ItemStack stack;
    private final int size;

    public DataComponentHandler(ItemStack stack, int size) {
        this.stack = stack;
        this.size = size;
    }

    public int getSlots() {
        return size;
    }

    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        if (getItemList().size() < slot + 1)
            return ItemStack.EMPTY;
        return getItemList().get(slot);
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        if (!isItemValid(slot, stack))
            return stack;

        validateSlotIndex(slot);

        NonNullList<ItemStack> itemStacks = getItemList();

        ItemStack existing = itemStacks.get(slot);

        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                itemStacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            setItemList(itemStacks);
        }

        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        NonNullList<ItemStack> itemStacks = getItemList();
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = itemStacks.get(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                itemStacks.set(slot, ItemStack.EMPTY);
                setItemList(itemStacks);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                itemStacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
                setItemList(itemStacks);
            }

            return existing.copyWithCount(toExtract);
        }
    }

    @Override public int getContainerSize() { return size; }
    @Override public boolean isEmpty() { return getItemList().stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return getStackInSlot(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { return extractItem(slot, amount, false); }
    @Override public ItemStack removeItemNoUpdate(int slot) { return extractItem(slot, Integer.MAX_VALUE, false); }
    @Override public void setItem(int slot, ItemStack value) { setStackInSlot(slot, value.copy()); }
    @Override public void setChanged() {}
    @Override public boolean stillValid(Player player) { return true; }
    @Override public void clearContent() { setItemList(NonNullList.withSize(size, ItemStack.EMPTY)); }

    private void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= getSlots())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
    }

    public int getSlotLimit(int slot) {
        return 64;
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return stack.getItem().canFitInsideContainerItems();
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        if (!stack.isEmpty() && !isItemValid(slot, stack))
            throw new RuntimeException("Invalid stack " + stack + " for slot " + slot + ")");
        NonNullList<ItemStack> itemStacks = getItemList();
        itemStacks.set(slot, stack);
        setItemList(itemStacks);
    }

    private NonNullList<ItemStack> getItemList() {
        DireItemContainerContents contents = this.stack.getOrDefault(MGDataComponents.ITEMSTACK_HANDLER, DireItemContainerContents.fromItems(NonNullList.withSize(size, ItemStack.EMPTY)));
        NonNullList<ItemStack> list = NonNullList.withSize(size, ItemStack.EMPTY);
        contents.copyInto(list);
        return list;
    }

    private void setItemList(NonNullList<ItemStack> itemStacks) {
        this.stack.set(MGDataComponents.ITEMSTACK_HANDLER, DireItemContainerContents.fromItems(itemStacks));
    }
}
