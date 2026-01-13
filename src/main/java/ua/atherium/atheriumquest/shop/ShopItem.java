package ua.atherium.atheriumquest.shop;

import org.bukkit.Material;

public class ShopItem {
    private Material material;
    private double price;
    private int maxStock;
    private int currentStock;
    private long refillTime;
    private long lastRefill;

    public ShopItem(Material material, double price, int maxStock, long refillTime) {
        this.material = material;
        this.price = price;
        this.maxStock = maxStock;
        this.currentStock = maxStock;
        this.refillTime = refillTime;
        this.lastRefill = System.currentTimeMillis();
    }

    public Material getMaterial() { return material; }
    public double getPrice() { return price; }
    public int getMaxStock() { return maxStock; }
    public int getCurrentStock() { return currentStock; }
    public long getRefillTime() { return refillTime; }

    public void setMaterial(Material material) { this.material = material; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.currentStock = stock; }
    public void setMaxStock(int stock) { this.maxStock = stock; }

    public void checkRefill() {
        if (System.currentTimeMillis() - lastRefill > refillTime * 1000) {
            currentStock = maxStock;
            lastRefill = System.currentTimeMillis();
        }
    }

    public long getNextRefillTime() {
        return lastRefill + (refillTime * 1000);
    }
}
