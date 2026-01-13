package ua.atherium.atheriumquest.gui;

import java.util.List;
import org.bukkit.Material;

public record QuestMenuItem(String id, int slot, Material material, String name, List<String> lore) {}
