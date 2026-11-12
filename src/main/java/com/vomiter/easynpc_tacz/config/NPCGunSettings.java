package com.vomiter.easynpc_tacz.config;

import com.vomiter.mobstacz.common.entity.ai.IMobGunState;

/** -1 inherits Mobs TACZ settings; 0/1 is an explicit per-NPC override. */
public interface NPCGunSettings {
    byte enpcTacz$getAmmoSetting();
    byte enpcTacz$getCooldownSetting();
    void enpcTacz$setAmmoSetting(byte value);
    void enpcTacz$setCooldownSetting(byte value);

    /** Mirror synchronized flags into Mobs TACZ's runtime-only fields on the client. */
    default void enpcTacz$applySyncedOverrides() {
        IMobGunState state = (IMobGunState) this;
        byte ammo = enpcTacz$getAmmoSetting();
        byte cooldown = enpcTacz$getCooldownSetting();
        state.mtacz$setConsumeAmmoOverride(ammo < 0 ? null : ammo == 1);
        state.mtacz$setAdditionalShootCooldownOverride(cooldown < 0 ? null : cooldown == 1);
    }
}
