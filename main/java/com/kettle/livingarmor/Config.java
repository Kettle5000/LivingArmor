package com.kettle.livingarmor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import com.mojang.datafixers.util.Pair;

@Mod.EventBusSubscriber(modid = LivingArmorMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_ARMOR_SETS;
    private static final ForgeConfigSpec.BooleanValue AUTO_DETECT_ARMOR_SETS;
    private static final ForgeConfigSpec.IntValue AUTO_DETECT_WEIGHT;
    private static final ForgeConfigSpec.ConfigValue<String> AUTO_DETECT_MAINHAND;
    private static final ForgeConfigSpec.ConfigValue<String> AUTO_DETECT_OFFHAND;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> AUTO_DETECT_BLACKLIST;
    private static final ForgeConfigSpec.ConfigValue<String> CURSED_KING_MAINHAND;
    private static final ForgeConfigSpec.ConfigValue<String> CURSED_KING_OFFHAND;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_EFFECTS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> POSITIVE_EFFECTS;
    public static List<MobEffect> allowed_effects = new ArrayList<>();
    public static List<MobEffect> deny_effects = new ArrayList<>();
    public static List<Pair<List<ItemStack>, Integer>> allowedArmorSets = new ArrayList<>();
    public static ItemStack CursedMainHand;
    public static ItemStack CursedOffHand;
    static {
        ALLOWED_ARMOR_SETS = BUILDER.comment("List of allowed armor sets with weights. Format: item1;item2;item3;item4;mainHand;offHand-weight")
                .defineList("allowedArmorSets",
                        Arrays.asList(
                                "minecraft:iron_helmet;minecraft:iron_chestplate;minecraft:iron_leggings;minecraft:iron_boots;minecraft:iron_sword;minecraft:shield-5",
                                "minecraft:golden_helmet;minecraft:golden_chestplate;minecraft:golden_leggings;minecraft:golden_boots;minecraft:golden_sword;minecraft:shield-20",
                                "minecraft:diamond_helmet;minecraft:diamond_chestplate;minecraft:diamond_leggings;minecraft:diamond_boots;minecraft:diamond_sword;minecraft:shield-1"
                        ),
                        entry -> entry instanceof String && validateArmorSet((String) entry));
        AUTO_DETECT_ARMOR_SETS = BUILDER.comment("Also use every full 4 piece armor set in the game (helmet, chestplate, leggings and boots of the same material and mod). Sets that are missing a piece are never used")
                .define("autoDetectArmorSets", true);
        AUTO_DETECT_WEIGHT = BUILDER.comment("Weight of each detected armor set. Sets listed in allowedArmorSets keep their own weight")
                .defineInRange("autoDetectWeight", 5, 1, 1000);
        AUTO_DETECT_MAINHAND = BUILDER.comment("Main Hand item for detected sets that have no matching sword (a set uses its own sword when the mod has one, like minecraft:golden_sword for gold armor)")
                .define("autoDetectMainHand", "minecraft:iron_sword");
        AUTO_DETECT_OFFHAND = BUILDER.comment("Off Hand item for detected sets, or empty")
                .define("autoDetectOffHand", "minecraft:shield");
        AUTO_DETECT_BLACKLIST = BUILDER.comment("Armor that is never detected: an item id (any piece blocks its whole set) or a whole mod as modid:*")
                .defineList("autoDetectBlacklist", Arrays.asList("living_armor:*"), entry -> entry instanceof String);
        CURSED_KING_MAINHAND = BUILDER.comment("Main Hand item that the cursed king will always hold")
                .define("Cursed Main Hand", "minecraft:netherite_sword");
        CURSED_KING_OFFHAND = BUILDER.comment("Off Hand item that the cursed king will always hold")
                .define("Cursed Off Hand", "minecraft:shield");
        ALLOWED_EFFECTS = BUILDER.comment("List of mob effects that bypass debuff inmunity by the cursed kings armor")
                .defineList("allowed_effects",
                        Arrays.asList(
                                "minecraft:hunger", "minecraft:poison", "minecraft:instant_damage"
                        ),
                        entry -> entry instanceof String);
        POSITIVE_EFFECTS = BUILDER.comment("List of positive or neutral mob effects that are denied by the cursed kings armor")
                .defineList("positive_effects",
                        Arrays.asList(
                                ""),
                        entry -> entry instanceof String);
        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            rebuild();
        }
    }

    // Run again once every mod has registered its items, so armor from mods that load after this one is found too
    @SubscribeEvent
    static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(Config::rebuild);
    }

    private static void rebuild() {
        List<Pair<List<ItemStack>, Integer>> sets = new ArrayList<>(ALLOWED_ARMOR_SETS.get().stream()
                .map(entry -> {
                    String[] parts = entry.split("-");
                    if (parts.length != 2) return null; // Skip invalid entries

                    String[] items = parts[0].split(";");
                    if (items.length != 6) return null; // Skip incomplete sets

                    int weight;
                    try {
                        weight = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        return null; // Skip entries with invalid weights
                    }

                    List<ItemStack> armorSet = Arrays.stream(items)
                            .map(itemName -> {
                                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
                                return item != null ? new ItemStack(item) : null;
                            })
                            .filter(Objects::nonNull)
                            .toList();

                    if (armorSet.size() != 6 || armorSet.get(4).isEmpty()) return null; // Skip sets with missing main hand
                    if (!isFullArmorSet(armorSet)) {
                        LivingArmorMod.LOGGER.warn("Skipping allowedArmorSets entry {}: the first 4 items must be a helmet, chestplate, leggings and boots", entry);
                        return null;
                    }

                    return Pair.of(armorSet, weight);
                })
                .filter(Objects::nonNull)
                .toList());

        if (AUTO_DETECT_ARMOR_SETS.get()) {
            // Sets already in allowedArmorSets keep their configured weight
            Set<List<Item>> listed = new HashSet<>();
            for (Pair<List<ItemStack>, Integer> set : sets) {
                listed.add(armorItems(set.getFirst()));
            }
            ItemStack defaultMainHand = stackOf(AUTO_DETECT_MAINHAND.get());
            ItemStack offHand = stackOf(AUTO_DETECT_OFFHAND.get());
            int detected = 0;
            for (List<ArmorItem> pieces : detectArmorSets()) {
                List<Item> items = new ArrayList<>(pieces);
                if (listed.contains(items)) continue;
                ItemStack mainHand = matchingSword(pieces.get(1), defaultMainHand);
                sets.add(Pair.of(Arrays.asList(new ItemStack(pieces.get(0)), new ItemStack(pieces.get(1)),
                        new ItemStack(pieces.get(2)), new ItemStack(pieces.get(3)), mainHand, offHand.copy()), AUTO_DETECT_WEIGHT.get()));
                detected++;
            }
            LivingArmorMod.LOGGER.info("Living Armor can use {} armor sets ({} detected)", sets.size(), detected);
        }

        // Fallback to default if all entries are invalid
        if (sets.isEmpty()) {
            sets = Arrays.asList(
                    Pair.of(createDefaultArmorSet("iron", 25), 25),
                    Pair.of(createDefaultArmorSet("golden", 10), 10),
                    Pair.of(createDefaultArmorSet("diamond", 1), 1)
            );
        }
        allowedArmorSets = sets;
        Item temp = ForgeRegistries.ITEMS.getValue(new ResourceLocation(CURSED_KING_MAINHAND.get()));
        if (temp != null) {
        	CursedMainHand = new ItemStack(temp);
        } else {
        	CursedMainHand = new ItemStack(Items.NETHERITE_SWORD);
        }
        temp = ForgeRegistries.ITEMS.getValue(new ResourceLocation(CURSED_KING_OFFHAND.get()));
        if (temp != null) {
        	CursedOffHand = new ItemStack(temp);
        } else {
        	CursedOffHand = new ItemStack(Items.SHIELD);
        }
        // New lists swapped in whole: a config reload runs on Forge's file watcher thread while the server reads these
        List<MobEffect> allowed = new ArrayList<>();
        List<MobEffect> denied = new ArrayList<>();
        for (String string: ALLOWED_EFFECTS.get()) {
        	MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(string));
        	if (effect != null) {
        		allowed.add(effect);
        	}
        }
        for (String string: POSITIVE_EFFECTS.get()) {
        	MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(string));
        	if (effect != null) {
        		denied.add(effect);
        	}
        }
        allowed_effects = allowed;
        deny_effects = denied;
    }

    // Every armor material (per mod) that has exactly one helmet, chestplate, leggings and boots, in that order
    public static List<List<ArmorItem>> detectArmorSets() {
        Set<String> blacklist = new HashSet<>(AUTO_DETECT_BLACKLIST.get());
        // group by mod + material, then by the item name with the piece word taken out (copper_helmet -> copper)
        Map<String, Map<ArmorItem.Type, List<ArmorItem>>> groups = new LinkedHashMap<>();
        for (Item item : ForgeRegistries.ITEMS) {
            if (!(item instanceof ArmorItem armor)) continue;
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id == null) continue;
            String key = id.getNamespace() + "|" + armor.getMaterial().getName();
            groups.computeIfAbsent(key, k -> new EnumMap<>(ArmorItem.Type.class))
                    .computeIfAbsent(armor.getType(), t -> new ArrayList<>()).add(armor);
        }
        List<List<ArmorItem>> sets = new ArrayList<>();
        for (Map<ArmorItem.Type, List<ArmorItem>> group : groups.values()) {
            if (group.size() != 4) continue; // a piece is missing
            boolean onePerSlot = group.values().stream().allMatch(list -> list.size() == 1);
            if (onePerSlot) {
                addSet(sets, group.get(ArmorItem.Type.HELMET).get(0), group.get(ArmorItem.Type.CHESTPLATE).get(0),
                        group.get(ArmorItem.Type.LEGGINGS).get(0), group.get(ArmorItem.Type.BOOTS).get(0), blacklist);
                continue;
            }
            // several sets share a material: pair the pieces by name
            Map<String, Map<ArmorItem.Type, List<ArmorItem>>> byName = new LinkedHashMap<>();
            for (List<ArmorItem> list : group.values()) {
                for (ArmorItem armor : list) {
                    byName.computeIfAbsent(setName(armor), k -> new EnumMap<>(ArmorItem.Type.class))
                            .computeIfAbsent(armor.getType(), t -> new ArrayList<>()).add(armor);
                }
            }
            for (Map<ArmorItem.Type, List<ArmorItem>> named : byName.values()) {
                if (named.size() == 4 && named.values().stream().allMatch(list -> list.size() == 1)) {
                    addSet(sets, named.get(ArmorItem.Type.HELMET).get(0), named.get(ArmorItem.Type.CHESTPLATE).get(0),
                            named.get(ArmorItem.Type.LEGGINGS).get(0), named.get(ArmorItem.Type.BOOTS).get(0), blacklist);
                }
            }
        }
        return sets;
    }

    private static void addSet(List<List<ArmorItem>> sets, ArmorItem head, ArmorItem chest, ArmorItem legs, ArmorItem feet, Set<String> blacklist) {
        List<ArmorItem> set = Arrays.asList(head, chest, legs, feet);
        for (ArmorItem armor : set) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(armor);
            if (blacklist.contains(id.toString()) || blacklist.contains(id.getNamespace() + ":*")) return;
        }
        sets.add(set);
    }

    private static final String[] PIECE_WORDS = {"helmet", "helm", "hat", "cap", "hood", "mask", "crown", "chestplate",
            "chest", "tunic", "robe", "body", "leggings", "legs", "pants", "greaves", "boots", "shoes", "feet", "sabatons"};

    // The item name with the piece word removed: "copper_helmet" and "copper_boots" both give "copper"
    private static String setName(ArmorItem armor) {
        String path = ForgeRegistries.ITEMS.getKey(armor).getPath();
        for (String word : PIECE_WORDS) {
            path = path.replaceAll("(^|_)" + word + "($|_)", "$1$2");
        }
        return path.replaceAll("__+", "_").replaceAll("^_|_$", "");
    }

    // The set's own sword if its mod has one (minecraft:golden_chestplate -> minecraft:golden_sword), otherwise the default
    private static ItemStack matchingSword(ArmorItem chestplate, ItemStack fallback) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(chestplate);
        Item sword = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id.getNamespace(), setName(chestplate) + "_sword"));
        if (sword != null && sword != Items.AIR) {
            return new ItemStack(sword);
        }
        return fallback.copy();
    }

    // True when the first 4 stacks are a helmet, chestplate, leggings and boots
    private static boolean isFullArmorSet(List<ItemStack> set) {
        ArmorItem.Type[] order = {ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS};
        for (int i = 0; i < 4; i++) {
            if (!(set.get(i).getItem() instanceof ArmorItem armor) || armor.getType() != order[i]) return false;
        }
        return true;
    }

    private static List<Item> armorItems(List<ItemStack> set) {
        return Arrays.asList(set.get(0).getItem(), set.get(1).getItem(), set.get(2).getItem(), set.get(3).getItem());
    }

    private static ItemStack stackOf(String id) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return item != null && item != Items.AIR ? new ItemStack(item) : ItemStack.EMPTY;
    }

    private static List<ItemStack> createDefaultArmorSet(String material, int weight) {
        return Arrays.asList(
                new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:" + material + "_helmet"))),
                new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:" + material + "_chestplate"))),
                new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:" + material + "_leggings"))),
                new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:" + material + "_boots"))),
                new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:" + material + "_sword"))),
                new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:shield")))
        );
    }

    private static boolean validateArmorSet(String entry) {
        try {
            String[] parts = entry.split("-");
            if (parts.length != 2) return false;

            String[] items = parts[0].split(";");
            if (items.length != 6) return false;

            for (String itemName : items) {
                if (!itemName.equals("empty") && ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)) == null) {
                    return false;
                }
            }

            Integer.parseInt(parts[1]); // Ensure the weight is a valid number
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
}