package ua.atherium.atheriumquest;

import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import ua.atherium.atheriumquest.database.StorageManager;
import ua.atherium.atheriumquest.quest.QuestManager;
import ua.atherium.atheriumquest.gui.MenuManager;



public class AtheriumQuests extends JavaPlugin {

    private ConfigManager configManager;
    private StorageManager storageManager;
    private QuestManager questManager;
    private MenuManager menuManager;
    private ua.atherium.atheriumquest.shop.ShopManager shopManager;
    private ua.atherium.atheriumquest.command.ShopEditorListener shopEditorListener;
    private Economy economy;

    @Override
    public void onEnable() {

        configManager = new ConfigManager(this);


        storageManager = new StorageManager(this, configManager);


        setupEconomy();


        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ua.atherium.atheriumquest.integration.AtheriumQuestsPlaceholderExpansion(this).register();
        }


        // questManager = new QuestManager(this); // Duplicate removed




        questManager = new QuestManager(this);
        menuManager = new ua.atherium.atheriumquest.gui.MenuManager(this);
        shopManager = new ua.atherium.atheriumquest.shop.ShopManager(this);


        shopEditorListener = new ua.atherium.atheriumquest.command.ShopEditorListener(this);
        getServer().getPluginManager().registerEvents(new ua.atherium.atheriumquest.quest.QuestListener(this), this);
        getServer().getPluginManager().registerEvents(new ua.atherium.atheriumquest.gui.MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new ua.atherium.atheriumquest.shop.ShopListener(this), this);
        getServer().getPluginManager().registerEvents(shopEditorListener, this);


        getCommand("atq").setExecutor(new ua.atherium.atheriumquest.command.AdminCommand(this));
        getCommand("atq").setTabCompleter(new ua.atherium.atheriumquest.command.AdminTabCompleter());
        getCommand("fermer").setExecutor(new ua.atherium.atheriumquest.command.MenuCommand(this, ua.atherium.atheriumquest.quest.NpcType.FERMER));
        getCommand("alximik").setExecutor(new ua.atherium.atheriumquest.command.MenuCommand(this, ua.atherium.atheriumquest.quest.NpcType.ALXIMIK));
        getCommand("weapons").setExecutor(new ua.atherium.atheriumquest.command.MenuCommand(this, ua.atherium.atheriumquest.quest.NpcType.WEAPONS));
        getCommand("quests").setExecutor(new ua.atherium.atheriumquest.command.MenuCommand(this, null));

        getLogger().info("AtheriumQuests has been enabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    @Override
    public void onDisable() {
        if (storageManager != null) {
            storageManager.close();
        }
        getLogger().info("AtheriumQuests has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public ua.atherium.atheriumquest.shop.ShopManager getShopManager() {
        return shopManager;
    }

    public ua.atherium.atheriumquest.command.ShopEditorListener getShopEditorListener() {
        return shopEditorListener;
    }
}
