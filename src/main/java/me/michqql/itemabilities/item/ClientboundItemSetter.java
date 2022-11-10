package me.michqql.itemabilities.item;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import me.michqql.core.util.MessageHandler;
import me.michqql.itemabilities.ItemAbilityPlugin;
import me.michqql.itemabilities.enchant.EnchantWrapper;
import me.michqql.itemabilities.item.data.NumberDataType;
import me.michqql.itemabilities.item.data.UUIDDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.*;

public class ClientboundItemSetter {

    private static boolean initialised;

    private final ItemAbilityPlugin plugin;
    private final String pluginName;

    public ClientboundItemSetter(ItemAbilityPlugin plugin) {
        this.plugin = plugin;
        this.pluginName = plugin.getName().toLowerCase(Locale.ROOT);
        if(initialised)
            return;

        init();
        initialised = true;
    }

    private void init() {
        ProtocolManager manager = plugin.getProtocolManager();

        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.SET_SLOT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                // Update items in any inventory
                StructureModifier<ItemStack> itemModifier = packet.getItemModifier();
                ItemStack update = updateItem(itemModifier.read(0), event.getPlayer()); // read from struct
                if(update != null)
                    packet.getItemModifier().write(0, update);       // write to struct
            }
        });

        manager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                // Update items in any inventory
                StructureModifier<List<ItemStack>> itemsModifier = packet.getItemListModifier();
                List<ItemStack> items = itemsModifier.read(0);
                for(int i = 0; i < items.size(); i++) {
                    ItemStack update = updateItem(items.get(i), event.getPlayer());
                    if(update != null)
                        items.set(i, update);
                }
            }
        });
    }

    private ItemStack updateItem(ItemStack item, Player player) {
        if (item.getType().isAir())
            return null;

        item = item.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            final List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            assert lore != null;

            PersistentDataContainer data = meta.getPersistentDataContainer();

            // Properties
            HashMap<String, String> propertyPlaceholders = new HashMap<>();
            for (NamespacedKey nkey : data.getKeys()) {
                // Is this key our key
                if(!nkey.getNamespace().equalsIgnoreCase(pluginName))
                    continue;

                // This key is ours!
                String key = nkey.getKey();

                // Is this item an example? No updates to example items
                if(key.equalsIgnoreCase(ItemGenerator.EXAMPLE_KEY))
                    return null;

                // Is this a tracker uuid?
                if(key.equalsIgnoreCase(ItemGenerator.TRACKER_ID_KEY)) {
                    UUID trackerUUID = data.get(nkey, UUIDDataType.UUID);
                    ItemUUIDTracker.Tracker.playerInventory(trackerUUID, player);
                    continue;
                }

                // Check this key is a property
                if (!key.startsWith(ItemGenerator.PROPERTY_ID_KEY) ||
                        key.length() <= ItemGenerator.PROPERTY_ID_KEY.length())
                    continue;

                String tag = key.substring(ItemGenerator.PROPERTY_ID_KEY.length());
                Number value = data.get(nkey, NumberDataType.NUMBER);
                if (value == null)
                    continue;

                propertyPlaceholders.put(tag, String.valueOf(value.intValue()));
            }

            // Enchantments
            for(Map.Entry<Enchantment, Integer> enchantment : meta.getEnchants().entrySet()) {
                if(enchantment.getKey() instanceof EnchantWrapper wrapper) {
                    lore.add(0, wrapper.getDisplayInfo(enchantment.getValue()));
                }
            }

            meta.setDisplayName(MessageHandler.replacePlaceholdersStatic(meta.getDisplayName(), propertyPlaceholders));
            meta.setLore(MessageHandler.replacePlaceholdersStatic(lore, propertyPlaceholders));

            item.setItemMeta(meta);
        }
        return item;
    }
}