package ua.atherium.atheriumquest.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class Menu implements InventoryHolder {
    public abstract void handleMenu(org.bukkit.event.inventory.InventoryClickEvent event);
}
