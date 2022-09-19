package com.ericdebouwer.zombieapocalypse.config;

import com.ericdebouwer.zombieapocalypse.ZombieApocalypse;
import com.ericdebouwer.zombieapocalypse.zombie.ZombieType;
import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZombieWrapper {

    private ItemStack head;
    @Getter
    private String customName;
    @Getter
    private final ZombieType type;
    private final Map<Attribute, Double> attributes = new HashMap<>();

    public ZombieWrapper(ZombieType type){
        this(type, null);
    }

    public ZombieWrapper(ZombieType type, ConfigurationSection section){
        this.type = type;
        if (section == null) return;

        String textureUrl = section.getString("head", "");
        head = getHead(textureUrl);

        customName = Strings.emptyToNull(section.getString("name"));
        if (customName != null){
            customName = ChatColor.translateAlternateColorCodes('§', customName);
        }

        ConfigurationSection attributeSection = section.getConfigurationSection("attributes");
        if (attributeSection == null) return;
        for (String attribute: attributeSection.getKeys(false)){
            try {
                attributes.put(Attribute.valueOf(attribute.toUpperCase()), attributeSection.getDouble(attribute));
            } catch (IllegalArgumentException e){
                JavaPlugin.getPlugin(ZombieApocalypse.class).getLogger().info("Attribute " + attribute + " does not exist!");
            }
        }
    }

    public Zombie apply(Zombie zombie){
        if (head != null)
            zombie.getEquipment().setHelmet(head);

        if (customName != null){
            zombie.setCustomName(customName);
            zombie.setCustomNameVisible(true);
        }

        for (Map.Entry<Attribute, Double> attribute: attributes.entrySet()){
            zombie.getAttribute(attribute.getKey()).setBaseValue(attribute.getValue());

            if (attribute.getKey() == Attribute.GENERIC_MAX_HEALTH){
                zombie.setHealth(attribute.getValue());
            }
        }
        return zombie;
    }

    private ItemStack getHead(String textureUrl){
        if (textureUrl == null || textureUrl.isEmpty()) return null;

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", textureUrl).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            JavaPlugin.getPlugin(ZombieApocalypse.class).getLogger().info("Zombie head could not be set, is the minecraft version correct?");
        }
        head.setItemMeta(headMeta);
        return head;
    }
}
