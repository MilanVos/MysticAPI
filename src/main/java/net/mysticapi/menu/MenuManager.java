package net.mysticapi.menu;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MenuManager {

    public MenuManager(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new MenuListener(), plugin);
    }
}
