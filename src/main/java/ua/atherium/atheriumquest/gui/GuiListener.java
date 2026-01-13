package ua.atherium.atheriumquest.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Menu) {
            event.setCancelled(true);
            ((Menu) event.getInventory().getHolder()).handleMenu(event);
        }
    }
}
