package net.mysticapi.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class MenuItem {

    private final ItemStack itemStack;
    private final Consumer<InventoryClickEvent> clickHandler;

    public MenuItem(ItemStack itemStack, Consumer<InventoryClickEvent> clickHandler) {
        this.itemStack = itemStack;
        this.clickHandler = clickHandler;
    }

    public static MenuItem of(ItemStack itemStack, Consumer<InventoryClickEvent> clickHandler) {
        return new MenuItem(itemStack, clickHandler);
    }

    public static MenuItem of(ItemStack itemStack) {
        return new MenuItem(itemStack, null);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Consumer<InventoryClickEvent> getClickHandler() {
        return clickHandler;
    }
}
