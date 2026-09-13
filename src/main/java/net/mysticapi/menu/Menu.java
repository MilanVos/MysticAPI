package net.mysticapi.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Menu {

    private final String title;
    private final int size;
    private final Map<Integer, MenuItem> items;
    private final boolean cancelClicksByDefault;
    private final Consumer<InventoryClickEvent> defaultClickHandler;
    private final Consumer<InventoryCloseEvent> closeHandler;
    private final Consumer<Player> openHandler;

    private Menu(Builder builder) {
        this.title = builder.title;
        this.size = builder.rows * 9;
        this.items = builder.items;
        this.cancelClicksByDefault = builder.cancelClicksByDefault;
        this.defaultClickHandler = builder.defaultClickHandler;
        this.closeHandler = builder.closeHandler;
        this.openHandler = builder.openHandler;
    }

    public void open(Player player) {
        MenuHolder holder = new MenuHolder(this);
        Inventory inventory = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inventory);

        for (Map.Entry<Integer, MenuItem> entry : items.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
        }

        player.openInventory(inventory);

        if (openHandler != null) {
            openHandler.accept(player);
        }
    }

    void handleClick(InventoryClickEvent event) {
        if (cancelClicksByDefault) {
            event.setCancelled(true);
        }

        if (event.getClickedInventory() == null
                || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        int slot = event.getSlot();
        MenuItem menuItem = items.get(slot);
        if (menuItem != null && menuItem.getClickHandler() != null) {
            menuItem.getClickHandler().accept(event);
        } else if (defaultClickHandler != null) {
            defaultClickHandler.accept(event);
        }
    }

    void handleClose(InventoryCloseEvent event) {
        if (closeHandler != null) {
            closeHandler.accept(event);
        }
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String title = "";
        private int rows = 3;
        private final Map<Integer, MenuItem> items = new HashMap<>();
        private boolean cancelClicksByDefault = true;
        private Consumer<InventoryClickEvent> defaultClickHandler;
        private Consumer<InventoryCloseEvent> closeHandler;
        private Consumer<Player> openHandler;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder rows(int rows) {
            this.rows = rows;
            return this;
        }

        public Builder item(int slot, MenuItem item) {
            items.put(slot, item);
            return this;
        }

        public Builder item(int slot, ItemStack itemStack, Consumer<InventoryClickEvent> clickHandler) {
            items.put(slot, new MenuItem(itemStack, clickHandler));
            return this;
        }

        public Builder item(int slot, ItemStack itemStack) {
            items.put(slot, new MenuItem(itemStack, null));
            return this;
        }

        public Builder fill(ItemStack itemStack) {
            for (int i = 0; i < rows * 9; i++) {
                items.putIfAbsent(i, new MenuItem(itemStack, null));
            }
            return this;
        }

        public Builder cancelClicksByDefault(boolean cancel) {
            this.cancelClicksByDefault = cancel;
            return this;
        }

        public Builder onClick(Consumer<InventoryClickEvent> defaultClickHandler) {
            this.defaultClickHandler = defaultClickHandler;
            return this;
        }

        public Builder onClose(Consumer<InventoryCloseEvent> closeHandler) {
            this.closeHandler = closeHandler;
            return this;
        }

        public Builder onOpen(Consumer<Player> openHandler) {
            this.openHandler = openHandler;
            return this;
        }

        public Menu build() {
            return new Menu(this);
        }
    }
}
