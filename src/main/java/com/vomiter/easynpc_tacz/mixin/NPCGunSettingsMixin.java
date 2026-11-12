package com.vomiter.easynpc_tacz.mixin;

import com.vomiter.easynpc_tacz.NPCGunBridge;
import com.vomiter.easynpc_tacz.config.NPCGunSettings;
import com.vomiter.mobstacz.common.entity.ai.IMobGunState;
import de.markusbordihn.easynpc.entity.easynpc.EasyNPC;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class NPCGunSettingsMixin implements NPCGunSettings {
    @Unique private static final String ENPC_TACZ_KEY = "easynpc_tacz:gun_settings";
    @Unique private static final EntityDataAccessor<Byte> ENPC_TACZ_AMMO =
            SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BYTE);
    @Unique private static final EntityDataAccessor<Byte> ENPC_TACZ_COOLDOWN =
            SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BYTE);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void enpcTacz$defineSettings(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        mob.getEntityData().define(ENPC_TACZ_AMMO, (byte) -1);
        mob.getEntityData().define(ENPC_TACZ_COOLDOWN, (byte) -1);
    }

    @Override
    public byte enpcTacz$getAmmoSetting() {
        return ((Mob) (Object) this).getEntityData().get(ENPC_TACZ_AMMO);
    }

    @Override
    public byte enpcTacz$getCooldownSetting() {
        return ((Mob) (Object) this).getEntityData().get(ENPC_TACZ_COOLDOWN);
    }

    @Override
    public void enpcTacz$setAmmoSetting(byte value) {
        Mob mob = (Mob) (Object) this;
        if (mob.level().isClientSide || !(mob instanceof EasyNPC<?>)) return;
        byte setting = enpcTacz$normalize(value);
        mob.getEntityData().set(ENPC_TACZ_AMMO, setting);
        ((IMobGunState) mob).mtacz$setConsumeAmmoOverride(setting < 0 ? null : setting == 1);
        NPCGunBridge.onAmmoSettingsChanged(mob);
    }

    @Override
    public void enpcTacz$setCooldownSetting(byte value) {
        Mob mob = (Mob) (Object) this;
        if (mob.level().isClientSide || !(mob instanceof EasyNPC<?>)) return;
        byte setting = enpcTacz$normalize(value);
        mob.getEntityData().set(ENPC_TACZ_COOLDOWN, setting);
        ((IMobGunState) mob).mtacz$setAdditionalShootCooldownOverride(setting < 0 ? null : setting == 1);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void enpcTacz$saveSettings(CompoundTag tag, CallbackInfo ci) {
        if (!((Object) this instanceof EasyNPC<?>)) return;
        CompoundTag settings = new CompoundTag();
        if (enpcTacz$getAmmoSetting() >= 0) settings.putBoolean("ConsumeAmmo", enpcTacz$getAmmoSetting() == 1);
        if (enpcTacz$getCooldownSetting() >= 0) settings.putBoolean("AdditionalCooldown", enpcTacz$getCooldownSetting() == 1);
        if (settings.isEmpty()) tag.remove(ENPC_TACZ_KEY);
        else tag.put(ENPC_TACZ_KEY, settings);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void enpcTacz$loadSettings(CompoundTag tag, CallbackInfo ci) {
        if (!((Object) this instanceof EasyNPC<?>)) return;
        CompoundTag settings = tag.getCompound(ENPC_TACZ_KEY);
        enpcTacz$setAmmoSetting(settings.contains("ConsumeAmmo", Tag.TAG_BYTE)
                ? (byte) (settings.getBoolean("ConsumeAmmo") ? 1 : 0) : (byte) -1);
        enpcTacz$setCooldownSetting(settings.contains("AdditionalCooldown", Tag.TAG_BYTE)
                ? (byte) (settings.getBoolean("AdditionalCooldown") ? 1 : 0) : (byte) -1);
    }

    @Unique
    private static byte enpcTacz$normalize(byte value) {
        return value == 0 || value == 1 ? value : (byte) -1;
    }
}
