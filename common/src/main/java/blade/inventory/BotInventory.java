package blade.inventory;

import blade.Bot;
import blade.event.InventoryEvents;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class BotInventory {
    protected final Bot bot;
    protected final Inventory inventory;
    protected final ObjectList<NonNullList<ItemStack>> compartments;
    protected boolean inventoryOpen = false;

    public BotInventory(Bot bot) {
        this.bot = bot;
        this.inventory = bot.getVanillaPlayer().getInventory();
        this.compartments = (ObjectList<NonNullList<ItemStack>>) Collections.unmodifiableList(
                new ObjectArrayList<>(Arrays.asList(inventory.items, inventory.armor, inventory.offhand))
        );
    }


    public Bot getBot() {
        return bot;
    }

    public void move(Slot from, Slot to) {
        move(from, to, false);
    }

    public void move(Slot from, Slot to, boolean force) {
        if (from.equals(to)) return;
        InventoryEvents.MOVE_ITEM.call(bot).onMoveItem(bot, from, to);
        moveInternally(from, to);
    }

    public ItemStack getItem(Slot slot) {
        return inventory.getItem(slot.getVanillaIndex());
    }

    public Slot findFirst(Predicate<ItemStack> tester, SlotFlag... order) {
        if (order.length == 0) order = new SlotFlag[] { SlotFlag.MAIN, SlotFlag.HOT_BAR, SlotFlag.ARMOR, SlotFlag.OFF_HAND };
        for (SlotFlag flag : order) {
            for (int i = 0; i < Slot.MAX_INDEX; i++) {
                Slot slot = new Slot(i);
                if (!flag.matchesSlot(slot)) continue;
                if (tester.test(getItem(slot))) return slot;
            }
        }
        return null;
    }

    public Slot findBest(Predicate<ItemStack> tester, Comparator<ItemStack> sorter, SlotFlag... slots) {
        if (slots.length == 0) slots = new SlotFlag[] { SlotFlag.MAIN, SlotFlag.HOT_BAR, SlotFlag.ARMOR, SlotFlag.OFF_HAND };
        ItemStack bestStack = null;
        Integer bestVanillaIndex = null;
        int index = -1;
        for (NonNullList<ItemStack> itemList : compartments) {
            for (ItemStack stack : itemList) {
                index++;
                if (!tester.test(stack)) continue;
                if (bestStack != null && sorter.compare(stack, bestStack) <= 0) continue;
                bestStack = stack;
                bestVanillaIndex = index;
            }
        }
        return bestVanillaIndex == null ? null : Slot.ofVanilla(bestVanillaIndex);
    }

    public List<ItemStack> getHotBar() {
        return inventory.items.subList(0, 9);
    }

    public List<ItemStack> getMain() {
        return inventory.items.subList(9, 36);
    }

    public ItemStack getOffHand() {
        return inventory.offhand.getFirst();
    }

    public List<ItemStack> getArmor() {
        return inventory.armor;
    }

    public int getSelectedSlot() {
        return inventory.selected;
    }

    public void setSelectedSlot(int slot) {
        inventory.selected = slot;
    }

    public void moveInternally(Slot from, Slot to) {
        if (bot.isClient) throw new UnsupportedOperationException("use BotClientInventory");
        int fromVanillaIndex = from.getVanillaIndex();
        int toVanillaIndex = to.getVanillaIndex();
        ItemStack tmp = inventory.getItem(fromVanillaIndex);
        inventory.setItem(fromVanillaIndex, inventory.getItem(toVanillaIndex));
        inventory.setItem(toVanillaIndex, tmp);
        if (getBot().getVanillaPlayer() instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(serverPlayer.containerMenu.containerId, serverPlayer.containerMenu.incrementStateId(), from.index(), inventory.getItem(from.index())));
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(serverPlayer.containerMenu.containerId, serverPlayer.containerMenu.incrementStateId(), to.index(), inventory.getItem(to.index())));
        }
    }

    public void drop(boolean entireStack) {
        ((ServerPlayer) bot.getVanillaPlayer()).drop(entireStack);
    }

    public void openInventory() {
        inventoryOpen = true;
    }

    public void closeInventory() {
        inventoryOpen = false;
    }

    public boolean hasInventoryOpen() {
        return inventoryOpen;
    }

    public Inventory getVanilla() {
        return inventory;
    }

    public Slot getBestFood(Predicate<FoodProperties> tester, SlotFlag... slots) {
        Slot bestSlot = null;
        float bestNum = Float.MIN_NORMAL;
        for (int i = 0; i < Slot.MAX_INDEX; i++) {
            Slot slot = new Slot(i);
            inner: {
                for (SlotFlag flag : slots) {
                    if (flag.matchesSlot(slot)) break inner;
                }
                continue;
            }
            ItemStack stack = getItem(slot);
            FoodProperties foodProperties = stack.get(DataComponents.FOOD);
            if (foodProperties == null) continue;
            float num = foodProperties.nutrition() + foodProperties.saturation();
            if (num > bestNum && tester.test(foodProperties)) {
                bestNum = num;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }
}
