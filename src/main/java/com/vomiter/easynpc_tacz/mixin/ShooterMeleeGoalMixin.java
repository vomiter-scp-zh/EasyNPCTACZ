package com.vomiter.easynpc_tacz.mixin;

import com.vomiter.easynpc_tacz.NPCGunBridge;
import com.vomiter.mobstacz.common.entity.ai.GunMode;
import com.vomiter.mobstacz.common.entity.ai.IMobGunState;
import com.vomiter.mobstacz.common.entity.ai.IShootingGoal;
import com.vomiter.mobstacz.common.entity.ai.ShooterMeleeGoal;
import com.vomiter.neurolib.common.entity.generic.MutatedMeleeGoal;
import com.vomiter.neurolib.mixin.MeleeAttackGoalAccessor;
import de.markusbordihn.easynpc.entity.easynpc.EasyNPC;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShooterMeleeGoal.class, remap = false)
public abstract class ShooterMeleeGoalMixin extends MutatedMeleeGoal {
    protected ShooterMeleeGoalMixin(MeleeAttackGoal basicGoal) {
        super(basicGoal);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void easynpcTacz$gateNativeWrapper(MeleeAttackGoal basicGoal, IShootingGoal shootingGoal,
                                              CallbackInfo ci) {
        var mob = ((MeleeAttackGoalAccessor) basicGoal).getMob();
        if (!(mob instanceof EasyNPC<?>)) {
            return;
        }
        // Keep the native wrapper and its MOVE/LOOK flags, priority and delegated behavior.
        setExtraUseCheck(goal -> NPCGunBridge.isManaged(mob)
                ? NPCGunBridge.canUseMelee(mob)
                : ((IMobGunState) mob).mtacz$getMode() == GunMode.MELEE);
        setExtraContinueCheck(goal -> !NPCGunBridge.isManaged(mob) || NPCGunBridge.canUseMelee(mob));
    }
}
