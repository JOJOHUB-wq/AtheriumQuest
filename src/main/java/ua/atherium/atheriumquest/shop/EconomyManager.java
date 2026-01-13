package ua.atherium.atheriumquest.shop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import ua.atherium.atheriumquest.AtheriumQuest;

public class EconomyManager {

    private final AtheriumQuest plugin;
    private final Map<UUID, Double> balances = new HashMap<>();

    public EconomyManager(AtheriumQuest plugin) {
        this.plugin = plugin;
    }

    private net.milkbowl.vault.economy.Economy vaultEco = null;

    public void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            org.bukkit.plugin.RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp =
                plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (rsp != null) {
                vaultEco = rsp.getProvider();
                plugin.getLogger().info("Vault hooked!");
            }
        }
    }

    public double getBalance(UUID uuid) {
        if (vaultEco != null) {
            return vaultEco.getBalance(plugin.getServer().getOfflinePlayer(uuid));
        }
        return plugin.getDatabaseManager().getStorage().getBalance(uuid);
    }

    public void withdraw(UUID uuid, double amount) {
        if (vaultEco != null) {
            vaultEco.withdrawPlayer(plugin.getServer().getOfflinePlayer(uuid), amount);
        } else {
            double current = getBalance(uuid);
            plugin.getDatabaseManager().getStorage().setBalance(uuid, current - amount);
        }
    }

    public void deposit(UUID uuid, double amount) {
        if (vaultEco != null) {
            vaultEco.depositPlayer(plugin.getServer().getOfflinePlayer(uuid), amount);
        } else {
             double current = getBalance(uuid);
             plugin.getDatabaseManager().getStorage().setBalance(uuid, current + amount);
        }
    }
}
