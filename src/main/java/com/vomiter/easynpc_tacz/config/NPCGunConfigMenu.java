package com.vomiter.easynpc_tacz.config;

import com.vomiter.mobstacz.Config;
import com.vomiter.mobstacz.common.entity.MobGunUtils;
import com.vomiter.mobstacz.common.entity.ai.IMobGunState;
import com.vomiter.mobstacz.common.registry.ModEntityTypeTags;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;

public final class NPCGunConfigMenu extends AbstractContainerMenu {
    private final Mob npc;
    private final int[] values = new int[2];

    public NPCGunConfigMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, findMob(inventory, buffer.readVarInt()));
    }

    public NPCGunConfigMenu(int id, Inventory inventory, Mob npc) {
        super(NPCConfigRegistry.GUN_CONFIG.get(), id);
        this.npc = npc;
        for (int i = 0; i < values.length; i++) {
            final int index = i;
            addDataSlot(new DataSlot() {
                @Override public int get() {
                    return inventory.player.level().isClientSide ? values[index] : readSetting(index);
                }
                @Override public void set(int value) { values[index] = value; }
            });
        }
    }

    private static Mob findMob(Inventory inventory, int id) {
        Entity entity = inventory.player.level().getEntity(id);
        return entity instanceof Mob mob ? mob : null;
    }

    private int readSetting(int index) {
        if (npc == null) return 0;
        if (index == 0) return MobGunUtils.shouldConsumeAmmo(npc) ? 1 : 0;
        Boolean override = ((IMobGunState) npc).mtacz$getAdditionalShootCooldownOverride();
        boolean enabled = override != null ? override
                : !npc.getType().is(ModEntityTypeTags.ADDITIONAL_SHOOT_COOLDOWN_BLACKLIST)
                && (npc.getType().is(ModEntityTypeTags.ADDITIONAL_SHOOT_COOLDOWN_WHITELIST)
                    || Config.ADDITIONAL_SHOOT_COOLDOWN);
        return enabled ? 1 : 0;
    }

    public boolean consumeAmmo() { return values[0] != 0; }
    public boolean additionalCooldown() { return values[1] != 0; }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer) || !stillValid(player)) return false;
        NPCGunSettings settings = (NPCGunSettings) npc;
        // Explicit values rather than toggles keep concurrent editors deterministic.
        switch (button) {
            case 0, 1 -> settings.enpcTacz$setAmmoSetting((byte) button);
            case 2, 3 -> settings.enpcTacz$setCooldownSetting((byte) (button - 2));
            case 4 -> {
                settings.enpcTacz$setAmmoSetting((byte) -1);
                settings.enpcTacz$setCooldownSetting((byte) -1);
            }
            default -> { return false; }
        }
        broadcastChanges();
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return npc != null && (player.level().isClientSide
                || player instanceof ServerPlayer serverPlayer && NPCConfigRegistry.canEdit(serverPlayer, npc));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
}
