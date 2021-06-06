package com.ericdebouwer.zombieapocalypse.zombie;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;

public class ZombieItems {

    private final NamespacedKey zombieTypeKey;

    public ZombieItems(ZombieApocalypse plugin){
        zombieTypeKey = new NamespacedKey(plugin, "ZombieType");
    }

    public NamespacedKey getKey() {
        return this.zombieTypeKey;
    }

    protected ItemStack createZombieItem(String name, Material mat, ZombieType type){
        ItemStack zombieItem = new ItemStack(mat);
        ItemMeta meta = zombieItem.getItemMeta();
        meta.getPersistentDataContainer().set(zombieTypeKey, PersistentDataType.STRING, type.toString());
        String itemName = WordUtils.capitalizeFully(type.toString()) + name;
        meta.setDisplayName(ChatColor.RESET + itemName);
        zombieItem.setItemMeta(meta);
        return zombieItem;
    }

    public ItemStack getSpawnEgg(ZombieType type){
        return this.createZombieItem(" Zombie Egg", Material.ZOMBIE_SPAWN_EGG, type);
    }

    public ItemStack getSpawner(ZombieType type){
        return this.createZombieItem(" Zombie Spawner", Material.SPAWNER, type);
    }

    public @Nullable ZombieType getZombieType(PersistentDataHolder container){
        if (container == null) return null;
        try {
            return ZombieType.valueOf(container.getPersistentDataContainer().get(zombieTypeKey, PersistentDataType.STRING));
        } catch (IllegalArgumentException ila){
            return null;
        }
    }

}
