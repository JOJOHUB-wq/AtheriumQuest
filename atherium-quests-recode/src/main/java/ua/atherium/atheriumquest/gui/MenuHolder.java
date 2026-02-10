package ua.atherium.atheriumquest.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder {

    private final String type;
    private final int level;

    public MenuHolder(String type) {
        this(type, 0);
    }

    public MenuHolder(String type, int level) {
        this.type = type;
        this.level = level;
    }

    public String getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
