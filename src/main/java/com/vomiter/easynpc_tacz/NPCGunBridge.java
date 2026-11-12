package com.vomiter.easynpc_tacz;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.util.AttachmentDataUtils;
import com.vomiter.mobstacz.common.entity.ai.CrazyShooterShootingGoal;
import com.vomiter.mobstacz.common.entity.MobGunUtils;
import com.vomiter.mobstacz.common.entity.ai.GunMode;
import com.vomiter.mobstacz.common.entity.ai.IMobGunState;
import com.vomiter.mobstacz.common.entity.ammo.ModCapabilities;
import com.vomiter.mobstacz.common.registry.ModEntityTypeTags;
import de.markusbordihn.easynpc.data.objective.ObjectiveType;
import de.markusbordihn.easynpc.entity.easynpc.EasyNPC;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.ToIntFunction;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

public final class NPCGunBridge {
    private static final Map<Mob, InventoryWatch> INVENTORIES = new WeakHashMap<>();

    private NPCGunBridge() {}

    public static boolean isGunAttackEnabled(Mob mob) {
        return !(mob instanceof EasyNPC<?>) || hasObjective(mob, ObjectiveType.GUN_ATTACK);
    }

    public static boolean isMeleeAttackEnabled(Mob mob) {
        return hasObjective(mob, ObjectiveType.MELEE_ATTACK) || hasObjective(mob, ObjectiveType.ZOMBIE_ATTACK);
    }

    private static boolean hasObjective(Mob mob, ObjectiveType type) {
        if (!(mob instanceof EasyNPC<?> npc)) {
            return false;
        }
        var objectives = npc.getEasyNPCObjectiveData();
        return objectives != null && objectives.getObjectiveDataSet() != null
                && objectives.getObjectiveDataSet().getObjectives().stream()
                .anyMatch(entry -> entry != null && entry.getType() == type);
    }

    /** Keep the native fallback for other weapons and blacklisted NPC types. */
    public static boolean isManaged(LivingEntity entity) {
        if (!(entity instanceof Mob mob) || !(mob instanceof EasyNPC<?> npc)
                || !(mob instanceof IMobGunState)
                || mob.getType().is(ModEntityTypeTags.GUN_EQUIP_BLACKLIST)
                || !IGun.mainHandHoldGun(mob)) {
            return false;
        }
        return npc.getEntityGoalSelector().getAvailableGoals().stream()
                .anyMatch(goal -> goal.getGoal() instanceof CrazyShooterShootingGoal);
    }

    /** Only gate native goals; Mobs TACZ owns ammo-driven mode transitions. */
    public static boolean canUseMelee(Mob mob) {
        return isMeleeAttackEnabled(mob) && (!isGunAttackEnabled(mob)
                || ((IMobGunState) mob).mtacz$getMode() == GunMode.MELEE);
    }

    public static void tick(LivingEntity entity) {
        if (!(entity instanceof Mob mob) || mob.level().isClientSide || !(mob instanceof EasyNPC<?>)) {
            return;
        }
        // Return legacy reserve and watch resupply; never classify ammo into combat modes here.
        returnReserveToInventory(mob);
        SimpleContainer inventory = inventory(mob);
        if (inventory == null) {
            return;
        }
        InventoryWatch watch = INVENTORIES.get(mob);
        if (watch == null || watch.inventory.get() != inventory) {
            if (watch != null && watch.inventory.get() != null) {
                watch.inventory.get().removeListener(watch);
            }
            watch = new InventoryWatch(mob, inventory);
            INVENTORIES.put(mob, watch);
            inventory.addListener(watch);
        }
        boolean enabled = isGunAttackEnabled(mob);
        if (enabled && !watch.gunEnabled) {
            retryGunAfterChange(mob);
        }
        watch.gunEnabled = enabled;
    }

    public static void onAmmoSettingsChanged(Mob mob) {
        if (!MobGunUtils.shouldConsumeAmmo(mob)) retryGunAfterChange(mob);
    }

    /** A resupply/enable edge permits one native attempt, not another ammo state machine. */
    private static void retryGunAfterChange(Mob mob) {
        if (!mob.level().isClientSide && isManaged(mob) && isGunAttackEnabled(mob)
                && ((IMobGunState) mob).mtacz$getMode() == GunMode.MELEE) {
            ((IMobGunState) mob).mtacz$setMode(GunMode.RANGED);
        }
    }

    private static final class InventoryWatch implements ContainerListener {
        private final WeakReference<Mob> owner;
        private final WeakReference<SimpleContainer> inventory;
        private boolean gunEnabled;

        private InventoryWatch(Mob mob, SimpleContainer inventory) {
            this.owner = new WeakReference<>(mob);
            this.inventory = new WeakReference<>(inventory);
        }

        @Override
        public void containerChanged(Container container) {
            Mob mob = owner.get();
            if (mob != null) {
                retryGunAfterChange(mob);
            }
        }
    }

    public static boolean canReloadFromInventory(Mob mob) {
        if (!isManaged(mob) || !isGunAttackEnabled(mob)) {
            return false;
        }
        ItemStack stack = mob.getMainHandItem();
        IGun gun = IGun.getIGunOrNull(stack);
        if (gun == null || gun.useInventoryAmmo(stack) || gun.useDummyAmmo(stack)) {
            return false;
        }
        CommonGunIndex index = TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).orElse(null);
        return index != null
                && gun.getCurrentAmmoCount(stack) < AttachmentDataUtils.getAmmoCountWithAttachment(stack, index.getGunData())
                && hasInventoryAmmo(mob, stack)
                && mob.getCapability(ModCapabilities.MOB_AMMO).map(reserve -> {
                    ItemStack round = AmmoItemBuilder.create().setId(index.getGunData().getAmmoId()).setCount(1).build();
                    return ItemHandlerHelper.insertItemStacked(reserve, round, true).isEmpty();
                }).orElse(false);
    }

    /** Read-only: usable for repeated AI and TACZ availability checks. */
    public static boolean hasInventoryAmmo(Mob mob, ItemStack gun) {
        if (!isManaged(mob) || !isGunAttackEnabled(mob)) {
            return false;
        }
        SimpleContainer inventory = inventory(mob);
        if (inventory == null) {
            return false;
        }
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (ammoCount(gun, inventory.getItem(slot)) > 0) {
                return true;
            }
        }
        return false;
    }

    /** Called only when TACZ's reload script actually requests rounds. */
    public static void stockAmmoForReload(Mob mob, ItemStack gun, int requested) {
        if (requested <= 0 || mob.level().isClientSide || !isManaged(mob) || !isGunAttackEnabled(mob)
                || !MobGunUtils.shouldConsumeAmmo(mob)) {
            return;
        }
        mob.getCapability(ModCapabilities.MOB_AMMO).ifPresent(reserve -> {
            int missing = requested;
            for (int slot = 0; slot < reserve.getSlots() && missing > 0; slot++) {
                missing -= Math.min(missing, ammoCount(gun, reserve.getStackInSlot(slot)));
            }
            supplyFromInventory(mob, gun, missing, offered -> {
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(reserve, offered, false);
                return offered.getCount() - remainder.getCount();
            });
        });
    }

    /** Inventory-fed guns consume directly; they never need an artificial reload or cap staging. */
    public static int consumeInventoryAmmo(Mob mob, ItemStack gun, int requested) {
        if (mob.level().isClientSide || !isManaged(mob) || !isGunAttackEnabled(mob)
                || !MobGunUtils.shouldConsumeAmmo(mob)) {
            return 0;
        }
        return supplyFromInventory(mob, gun, requested, ItemStack::getCount);
    }

    private static int supplyFromInventory(Mob mob, ItemStack gun, int requested, ToIntFunction<ItemStack> receive) {
        SimpleContainer inventory = inventory(mob);
        IGun gunItem = IGun.getIGunOrNull(gun);
        if (inventory == null || requested <= 0 || gunItem == null) {
            return 0;
        }
        CommonGunIndex index = TimelessAPI.getCommonGunIndex(gunItem.getGunId(gun)).orElse(null);
        if (index == null) {
            return 0;
        }
        int supplied = 0;
        for (int slot = 0; slot < inventory.getContainerSize() && supplied < requested; slot++) {
            ItemStack stack = inventory.getItem(slot);
            int available = ammoCount(gun, stack);
            int amount = Math.min(requested - supplied, available);
            if (amount <= 0) {
                continue;
            }
            if (stack.getItem() instanceof IAmmo ammo && ammo.isAmmoOfGun(gun, stack)) {
                int accepted = receive.applyAsInt(stack.copyWithCount(amount));
                if (accepted > 0) {
                    stack.shrink(accepted);
                    inventory.setChanged();
                    supplied += accepted;
                }
            } else if (stack.getItem() instanceof IAmmoBox box) {
                // Keep the box, its tier and its remaining rounds in the visible NPC inventory.
                ItemStack rounds = AmmoItemBuilder.create().setId(index.getGunData().getAmmoId()).setCount(amount).build();
                int accepted = receive.applyAsInt(rounds);
                if (accepted > 0) {
                    if (!box.isCreative(stack)) {
                        box.setAmmoCount(stack, box.getAmmoCount(stack) - accepted);
                    }
                    inventory.setChanged();
                    supplied += accepted;
                }
            }
        }
        return supplied;
    }

    private static void returnReserveToInventory(Mob mob) {
        SimpleContainer inventory = inventory(mob);
        if (inventory == null) {
            return;
        }
        InvWrapper destination = new InvWrapper(inventory);
        mob.getCapability(ModCapabilities.MOB_AMMO).ifPresent(reserve -> {
            for (int slot = 0; slot < reserve.getSlots(); slot++) {
                ItemStack stack = reserve.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(destination, stack.copy(), false);
                if (remainder.getCount() != stack.getCount()) {
                    reserve.setStackInSlot(slot, remainder);
                }
            }
        });
    }

    private static SimpleContainer inventory(Mob mob) {
        if (!(mob instanceof EasyNPC<?> npc)) {
            return null;
        }
        var data = npc.getEasyNPCInventoryData();
        return data == null ? null : data.getEasyNPCInventory();
    }

    private static int ammoCount(ItemStack gun, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.getItem() instanceof IAmmo ammo && ammo.isAmmoOfGun(gun, stack)) {
            return stack.getCount();
        }
        if (stack.getItem() instanceof IAmmoBox box && box.isAmmoBoxOfGun(gun, stack)) {
            return box.isCreative(stack) ? Integer.MAX_VALUE : Math.max(0, box.getAmmoCount(stack));
        }
        return 0;
    }
}
