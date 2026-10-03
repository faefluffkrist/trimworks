package com.faefluffkrist.trimworks.gameplay;

import com.faefluffkrist.trimworks.config.MaterialBonusesConfig;
import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public final class MaterialTrimBonuses {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final Identifier IRON_KB_ID = Identifier.parse("trimworks:iron_trim_knockback_resistance");
    private static final Identifier REDSTONE_SPEED_ID = Identifier.parse("trimworks:redstone_trim_movement_speed");
    private static final Identifier DIAMOND_TOUGHNESS_ID = Identifier.parse("trimworks:diamond_trim_armor_toughness");
    private MaterialTrimBonuses() {}

    public static MaterialBonusesConfig config() {
        var root = TrimEffectsConfigManager.getServerConfig();
        if (root.materialBonuses == null) root.materialBonuses = new MaterialBonusesConfig();
        return root.materialBonuses;
    }

    public static boolean enabled() { return config().enabled; }

    public static int pieces(net.minecraft.world.entity.LivingEntity player, String materialId) {
        if (!MobTrimCompatibility.allows(player, materialId, 2)) return 0;
        int count = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) if (hasMaterial(player.getItemBySlot(slot), materialId)) count++;
        return count;
    }

    public static boolean hasAtLeast(net.minecraft.world.entity.LivingEntity player, String materialId, int count) {
        return enabled() && pieces(player, materialId) >= count;
    }

    public static boolean hasMaterial(ItemStack stack, String materialId) {
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        if (trim == null) return false;
        Identifier wanted = Identifier.tryParse(materialId);
        return wanted != null && trim.material().is(wanted);
    }

    /** The smithing ingredient is resin_brick; the resulting trim material registry id is minecraft:resin. */
    public static boolean hasAmberAtLeast(net.minecraft.world.entity.LivingEntity player, int count) {
        return enabled() && (pieces(player, "minecraft:resin") >= count || pieces(player, "minecraft:amber") >= count);
    }

    public static void tick(net.minecraft.world.entity.LivingEntity player) {
        var cfg = config();
        applyAttribute(player, Attributes.KNOCKBACK_RESISTANCE, IRON_KB_ID,
                cfg.enabled && cfg.ironKnockbackResistance && hasAtLeast(player, "minecraft:iron", 4) ? cfg.ironResistance : 0.0D,
                AttributeModifier.Operation.ADD_VALUE);
        applyAttribute(player, Attributes.MOVEMENT_SPEED, REDSTONE_SPEED_ID,
                cfg.enabled && cfg.redstoneMovementSpeed && hasAtLeast(player, "minecraft:redstone", 4) ? cfg.redstoneSpeedPercent / 100.0D : 0.0D,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyAttribute(player, Attributes.ARMOR_TOUGHNESS, DIAMOND_TOUGHNESS_ID,
                cfg.enabled && cfg.diamondArmorToughness && hasAtLeast(player, "minecraft:diamond", 4) ? cfg.diamondToughness : 0.0D,
                AttributeModifier.Operation.ADD_VALUE);

        // Copper is intentionally a little silly: a full copper-trim set resists lightning,
        // but during thunderstorms it also acts like a wearable lightning rod.  The chance
        // is deliberately low (about one attracted strike per five minutes of thunder on average).
        if (cfg.enabled && cfg.copperLightningResistance
                && hasAtLeast(player, "minecraft:copper", 4)
                && player.level().isThundering()
                && player.getRandom().nextInt(Math.max(1, cfg.copperLightningInterval)) == 0) {
            var type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse("minecraft:lightning_bolt"));
            if (type != null) {
                var entity = type.create(player.level(), EntitySpawnReason.TRIGGERED);
                if (entity instanceof LightningBolt lightning) {
                    lightning.setPos(player.getX(), player.getY(), player.getZ());
                    player.level().addFreshEntity(lightning);
                }
            }
        }
    }

    private static void applyAttribute(net.minecraft.world.entity.LivingEntity player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                       Identifier id, double amount, AttributeModifier.Operation operation) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (amount != 0.0D) instance.addTransientModifier(new AttributeModifier(id, amount, operation));
    }
}
