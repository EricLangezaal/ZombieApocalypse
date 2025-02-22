package com.ericdebouwer.zombieapocalypse.integration.silkspawners;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieItems;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieType;
import de.dustplanet.util.SilkUtil;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SilkZombieItems extends ZombieItems {

    public SilkZombieItems(ZombieApocalypse plugin) {
        super(plugin);
    }

    @Override
    public ItemStack getSpawner(ZombieType type){
        SilkUtil silkUtil = SilkUtil.hookIntoSilkSpanwers();
        ItemStack spawner = silkUtil.newSpawnerItem("zombie",
                ChatColor.RESET + "" + ChatColor.WHITE + WordUtils.capitalizeFully(type.toString()) + " Zombie Spawner",
                1, false);

        ItemMeta meta = spawner.getItemMeta();
        meta.getPersistentDataContainer().set(super.getKey(), PersistentDataType.STRING, type.toString());
        spawner.setItemMeta(meta);
        return spawner;
    }
}
