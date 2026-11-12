package com.vomiter.easynpc_tacz.config;

import de.markusbordihn.easynpc.entity.easynpc.EasyNPC;
import de.markusbordihn.easynpc.network.NetworkHandlerManager;
import de.markusbordihn.easynpc.network.message.client.HighlightEasyNPCMessage;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public final class NPCConfigStickItem extends Item {
    public NPCConfigStickItem(Properties properties) {
        super(properties);
    }

    public static void open(ServerPlayer player, Mob mob) {
        if (!NPCConfigRegistry.canEdit(player, mob)) {
            player.displayClientMessage(Component.translatable("message.easynpc_tacz.cannot_edit"), true);
            return;
        }
        NetworkHooks.openScreen(player, new SimpleMenuProvider(
                (id, inventory, owner) -> new NPCGunConfigMenu(id, inventory, mob),
                Component.translatable("menu.easynpc_tacz.gun_config")),
                buffer -> buffer.writeVarInt(mob.getId()));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)
                || (!selected && player.getOffhandItem() != stack) || level.getGameTime() % 30 != 0) return;
        List<UUID> npcs = serverLevel.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(32),
                mob -> mob.isAlive() && mob instanceof EasyNPC<?>).stream().map(Entity::getUUID).toList();
        // Easy NPC's native packet accepts at most 256 entries per message.
        for (int start = 0; start < npcs.size(); start += 256) {
            NetworkHandlerManager.sendMessageToPlayer(
                    new HighlightEasyNPCMessage(npcs.subList(start, Math.min(start + 256, npcs.size())), 80), player);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.easynpc_tacz.tacz_config_stick.tooltip"));
    }
}
