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
import java.util.List;
import java.util.Objects;

import com.mojang.datafixers.util.Pair;

@Mod.EventBusSubscriber(modid = LivingArmorMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_ARMOR_SETS;
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
        allowedArmorSets = ALLOWED_ARMOR_SETS.get().stream()
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

                    return Pair.of(armorSet, weight);
                })
                .filter(Objects::nonNull)
                .toList();

        // Fallback to default if all entries are invalid
        if (allowedArmorSets.isEmpty()) {
            allowedArmorSets = Arrays.asList(
                    Pair.of(createDefaultArmorSet("iron", 25), 25),
                    Pair.of(createDefaultArmorSet("golden", 10), 10),
                    Pair.of(createDefaultArmorSet("diamond", 1), 1)
            );
        }
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
        for (String string: ALLOWED_EFFECTS.get()) {
        	MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(string));
        	if (effect != null) {
        		allowed_effects.add(effect);
        	}
        }
        for (String string: POSITIVE_EFFECTS.get()) {
        	MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(string));
        	if (effect != null) {
        		deny_effects.add(effect);
        	}
        }
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