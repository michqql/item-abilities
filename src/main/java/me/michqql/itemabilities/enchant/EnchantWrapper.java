package me.michqql.itemabilities.enchant;

import com.github.fracpete.romannumerals4j.RomanNumeralFormat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.michqql.core.io.JsonFile;
import me.michqql.itemabilities.IReload;
import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.item.ItemGenerator;
import me.michqql.itemabilities.item.ItemModifier;
import me.michqql.itemabilities.item.NKeyCache;
import me.michqql.itemabilities.util.ChatColorUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public abstract class EnchantWrapper extends Enchantment implements Listener, IReload {

    private final static RomanNumeralFormat ROMAN_NUMERAL_FORMAT = new RomanNumeralFormat();

    protected final ItemAbilityPlugin plugin;
    protected final String enchantId;

    protected String enchantmentName;
    protected String displayColour;
    protected int maxLevel;
    protected JsonObject settings;

    public EnchantWrapper(ItemAbilityPlugin plugin, String id) {
        super(NKeyCache.getNKey(ItemGenerator.ENCHANTMENT_ID_KEY + id));
        this.plugin = plugin;
        this.enchantId = id;

        // Load enchantment settings
        if(!loadSettings()) {
            // Error while loading settings, so cannot register enchantment
            plugin.getLogger().severe("Error while loading enchantment settings for " + enchantId);
            return;
        }

        EnchantHandler.registerEnchantment(this);
    }

    public String getDisplayInfo(int level) {
        return ChatColorUtil.of(displayColour) + enchantmentName + " " + ROMAN_NUMERAL_FORMAT.format(level);
    }

    @Override
    public void reload(ItemAbilityPlugin plugin) {
        loadSettings();
    }

    boolean loadSettings() {
        JsonFile file = new JsonFile(plugin, "enchants", enchantId);
        if(!file.getJsonElement().isJsonObject())
            return false;

        JsonObject object = file.getJsonElement().getAsJsonObject();
        try {
            this.enchantmentName = object.get("enchantment_name").getAsString();
            this.displayColour = object.get("display_colour").getAsString();
            this.maxLevel = object.get("max_level").getAsInt();

            JsonElement settingsElement = object.get(("settings"));
            if(settingsElement != null && settingsElement.isJsonObject()) {
                settings = settingsElement.getAsJsonObject();
            } else {
                settings = new JsonObject();
            }
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String getEnchantId() {
        return enchantId;
    }

    @NotNull
    @Override
    public String getName() {
        return enchantmentName;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return null;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment other) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return ItemModifier.getItemData(item).isEnchantable();
    }

    protected final boolean isPlayerHoldingCorrectItem(Player player, boolean mainHandOnly) {
        EntityEquipment equipment = player.getEquipment();
        if(equipment == null)
            return false;

        ItemMeta mainHandMeta = equipment.getItemInMainHand().getItemMeta();
        ItemMeta offHandMeta = equipment.getItemInOffHand().getItemMeta();
        return (mainHandMeta != null && mainHandMeta.hasEnchant(this)) ||
                (!mainHandOnly && offHandMeta != null && offHandMeta.hasEnchant(this));
    }

    protected final int getEnchantmentLevel(Player player, EquipmentSlot hand) {
        EntityEquipment equipment = player.getEquipment();
        if(equipment == null)
            return 0;

        ItemMeta meta = equipment.getItem(hand).getItemMeta();
        return meta != null ? meta.getEnchantLevel(this) : 0;
    }

    protected int getIntSetting(String key, int def) {
        return settings.has(key) ? settings.get(key).getAsInt() : def;
    }

    protected double getDoubleSetting(String key, double def) {
        return settings.has(key) ? settings.get(key).getAsDouble() : def;
    }
}
