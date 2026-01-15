package ua.atherium.atheriumquest;

import org.bukkit.plugin.java.JavaPlugin;

public class AtheriumQuest extends JavaPlugin {

    private ConfigManager configManager;
    private ua.atherium.atheriumquest.database.DatabaseManager databaseManager;
    private ua.atherium.atheriumquest.quest.QuestManager questManager;
    private ua.atherium.atheriumquest.user.UserManager userManager;
    private ua.atherium.atheriumquest.shop.ShopManager shopManager;
    private ua.atherium.atheriumquest.shop.EconomyManager economyManager;
    private ua.atherium.atheriumquest.shop.ShopEditorListener shopEditorListener;
    private ua.atherium.atheriumquest.gui.MenuManager menuManager;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        databaseManager = new ua.atherium.atheriumquest.database.DatabaseManager(this, configManager);
        databaseManager.init();

        questManager = new ua.atherium.atheriumquest.quest.QuestManager(this);
        questManager.loadQuests();

        menuManager = new ua.atherium.atheriumquest.gui.MenuManager(this);

        userManager = new ua.atherium.atheriumquest.user.UserManager(this);

        shopManager = new ua.atherium.atheriumquest.shop.ShopManager(this);
        economyManager = new ua.atherium.atheriumquest.shop.EconomyManager(this);
        economyManager.setupEconomy();

        getServer().getPluginManager().registerEvents(new ua.atherium.atheriumquest.gui.GuiListener(), this);
        getServer().getPluginManager().registerEvents(new ua.atherium.atheriumquest.npc.NpcListener(this), this);
        getServer().getPluginManager().registerEvents(new ua.atherium.atheriumquest.quest.QuestListener(this), this);

        shopEditorListener = new ua.atherium.atheriumquest.shop.ShopEditorListener(this);
        getServer().getPluginManager().registerEvents(shopEditorListener, this);

        getCommand("atq").setExecutor(new ua.atherium.atheriumquest.command.AdminCommand(this));
        getCommand("atq").setTabCompleter(new ua.atherium.atheriumquest.command.AdminTabCompleter());
        getCommand("fermer").setExecutor(new ua.atherium.atheriumquest.command.MenuCommand(this, ua.atherium.atheriumquest.quest.NpcType.FARMER));
        getCommand("alximik").setExecutor(new ua.atherium.atheriumquest.command.MenuCommand(this, ua.atherium.atheriumquest.quest.NpcType.ALCHEMIST));
        getCommand("weapons").setExecutor(new ua.atherium.atheriumquest.command.MenuCommand(this, ua.atherium.atheriumquest.quest.NpcType.WEAPONS));

        getLogger().info("AtheriumQuest has been enabled!");
    }

    @Override
    public void onDisable() {
        if (shopManager != null) {
            shopManager.saveShops();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("AtheriumQuest has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ua.atherium.atheriumquest.database.DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ua.atherium.atheriumquest.quest.QuestManager getQuestManager() {
        return questManager;
    }

    public ua.atherium.atheriumquest.user.UserManager getUserManager() {
        return userManager;
    }

    public ua.atherium.atheriumquest.shop.ShopManager getShopManager() {
        return shopManager;
    }

    public ua.atherium.atheriumquest.shop.EconomyManager getEconomyManager() {
        return economyManager;
    }

    public ua.atherium.atheriumquest.shop.ShopEditorListener getShopEditorListener() {
        return shopEditorListener;
    }

    public ua.atherium.atheriumquest.gui.MenuManager getMenuManager() {
        return menuManager;
    }
}
