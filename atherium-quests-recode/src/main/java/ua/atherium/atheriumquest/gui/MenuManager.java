package ua.atherium.atheriumquest.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ua.atherium.atheriumquest.AtheriumQuests;
import ua.atherium.atheriumquest.quest.NpcType;
import ua.atherium.atheriumquest.quest.Quest;
import ua.atherium.atheriumquest.quest.QuestLevel;
import me.clip.placeholderapi.PlaceholderAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MenuManager {

    private final AtheriumQuests plugin;

    public MenuManager(AtheriumQuests plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::updateMenus, 20L, 20L);
    }

    public void openMainMenu(Player player) {
        ConfigurationSection config = plugin.getConfigManager().getMainMenuConfig();
        if (config == null) return;

        String title = config.getString("menu_title", "Menu");
        int size = config.getInt("size", 27);

        Inventory inv = Bukkit.createInventory(new MenuHolder("main_menu"), size, plugin.getConfigManager().parse(replacePlaceholders(player, title)));

        fillInventory(inv, player, config.getConfigurationSection("items"), null, 0);

        player.openInventory(inv);
    }

    public void openNpcMenu(Player player, NpcType npc) {
        int level = plugin.getStorageManager().getStorage().getLevel(player.getUniqueId(), npc.getId());

        File file = new File(plugin.getDataFolder(), npc.getId() + "/menues/level_" + level + ".yml");
        if (!file.exists()) {
            player.sendMessage(plugin.getConfigManager().parse("&cMenu file not found for level " + level));
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String title = config.getString("menu_title", npc.getName() + " - Level " + level);
        int size = config.getInt("size", 54);

        Inventory inv = Bukkit.createInventory(new MenuHolder("npc_" + npc.getId(), level), size, plugin.getConfigManager().parse(replacePlaceholders(player, title)));

        fillInventory(inv, player, config.getConfigurationSection("items"), npc, level);

        player.openInventory(inv);
    }

    private void fillInventory(Inventory inv, Player player, ConfigurationSection itemsSec, NpcType npc, int level) {
        if (itemsSec == null) return;

        for (String key : itemsSec.getKeys(false)) {
            ConfigurationSection itemSec = itemsSec.getConfigurationSection(key);
            if (itemSec == null) continue;

            ItemStack item;
            if (itemSec.contains("quest")) {
                int questNum = itemSec.getInt("quest");
                String questId = "quest_" + questNum;
                Quest quest = plugin.getQuestManager().getQuest(npc, level, questId);

                if (quest == null) {
                    item = new ItemStack(Material.BARRIER);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName("Quest Not Found: " + questId);
                    item.setItemMeta(meta);
                } else {
                    item = buildQuestItem(player, npc, level, quest);
                }
            } else {
                item = buildItem(player, itemSec);
            }

            List<Integer> slots = itemSec.getIntegerList("slots");
            if (slots.isEmpty()) {
                int slot = itemSec.getInt("slot", -1);
                if (slot >= 0 && slot < inv.getSize()) {
                    inv.setItem(slot, item);
                }
            } else {
                for (int slot : slots) {
                    if (slot >= 0 && slot < inv.getSize()) {
                        inv.setItem(slot, item);
                    }
                }
            }
        }
    }

    private ItemStack buildItem(Player player, ConfigurationSection section) {
        String matName = section.getString("material", "STONE");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.STONE;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        if (section.contains("display_name")) {
            String name = section.getString("display_name");
            name = replacePlaceholders(player, name);
            meta.displayName(plugin.getConfigManager().parse(name));
        }

        if (section.contains("lore")) {
            List<String> lore = section.getStringList("lore");
            List<net.kyori.adventure.text.Component> loreComp = new ArrayList<>();
            for (String l : lore) {
                l = replacePlaceholders(player, l);
                loreComp.add(plugin.getConfigManager().parse(l));
            }
            meta.lore(loreComp);
        }

        if (section.getBoolean("enchanted", false)) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack buildQuestItem(Player player, NpcType npc, int level, Quest quest) {
        boolean completed = plugin.getStorageManager().getStorage().isQuestCompleted(player.getUniqueId(), npc.getId(), level, quest.getId());

        boolean locked = false;




        QuestLevel qLevel = plugin.getQuestManager().getQuestLevel(npc, level);
        if (qLevel != null) {
            boolean prevCompleted = true;
            for (Quest q : qLevel.getQuests().values()) {
                if (q.getId().equals(quest.getId())) {
                    if (!prevCompleted) locked = true;
                    break;
                }
                if (!plugin.getStorageManager().getStorage().isQuestCompleted(player.getUniqueId(), npc.getId(), level, q.getId())) {
                    prevCompleted = false;
                }
            }
        }

        Material mat;
        String name;
        List<String> lore;
        boolean enchanted;

        if (completed) {
            mat = quest.getCompletedMaterial();
            name = quest.getCompletedDisplayName();
            lore = quest.getCompletedLore();
            enchanted = quest.isCompletedEnchanted();
        } else if (locked) {
            mat = quest.getLockedMaterial();
            name = quest.getLockedDisplayName();
            lore = quest.getLockedLore();
            enchanted = quest.isLockedEnchanted();
        } else {
            mat = quest.getActiveMaterial();
            name = quest.getActiveDisplayName();
            lore = quest.getActiveLore();
            enchanted = quest.isActiveEnchanted();
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (name != null) {
            name = replacePlaceholders(player, name);

            name = replaceQuestPlaceholders(name, player, npc, level, quest);
            meta.displayName(plugin.getConfigManager().parse(name));
        }
        if (lore != null) {
            List<net.kyori.adventure.text.Component> loreComp = new ArrayList<>();
            for (String l : lore) {
                l = replacePlaceholders(player, l);
                l = replaceQuestPlaceholders(l, player, npc, level, quest);
                loreComp.add(plugin.getConfigManager().parse(l));
            }
            meta.lore(loreComp);
        }
        if (enchanted) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    private String replacePlaceholders(Player player, String text) {
        if (text == null) return "";
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }





        text = text.replace("%player%", player.getName());
        return text;
    }

    private String replaceQuestPlaceholders(String text, Player player, NpcType npc, int level, Quest quest) {


        int progress = plugin.getStorageManager().getStorage().getQuestProgress(player.getUniqueId(), npc.getId(), level, quest.getId());
        text = text.replace("%current%", String.valueOf(progress));
        text = text.replace("%required%", String.valueOf(quest.getAmount()));







        String questNum = quest.getId().replace("quest_", "");
        text = text.replace("%atherium_quest_progress_" + questNum + "%", String.valueOf(progress));

        return text;
    }

    private void updateMenus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory top = player.getOpenInventory().getTopInventory();
            if (top.getHolder() instanceof MenuHolder) {
                MenuHolder holder = (MenuHolder) top.getHolder();
                if (holder.getType().startsWith("npc_")) {
                    String[] parts = holder.getType().split("_");
                    if (parts.length >= 2) {
                        NpcType npc = NpcType.fromId(parts[1]);
                        if (npc != null) {
                            ConfigurationSection config;
                            File file = new File(plugin.getDataFolder(), npc.getId() + "/menues/level_" + holder.getLevel() + ".yml");
                            if (file.exists()) {
                                config = YamlConfiguration.loadConfiguration(file).getConfigurationSection("items");
                                fillInventory(top, player, config, npc, holder.getLevel());
                            }
                        }
                    }
                } else if (holder.getType().equals("main_menu")) {
                    ConfigurationSection config = plugin.getConfigManager().getMainMenuConfig().getConfigurationSection("items");
                    fillInventory(top, player, config, null, 0);
                }
            }
        }
    }
}
