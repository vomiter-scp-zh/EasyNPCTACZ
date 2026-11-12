package com.vomiter.easynpc_tacz.config;

import com.vomiter.easynpc_tacz.EasyNPCTacz;
import de.markusbordihn.easynpc.entity.easynpc.EasyNPC;
import de.markusbordihn.easynpc.access.AccessManager;
import de.markusbordihn.easynpc.security.NpcFeature;
import de.markusbordihn.easynpc.security.SecurityManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import de.markusbordihn.easynpc.tabs.ModTabs;

public final class NPCConfigRegistry {
    public static final String CONFIG_MOD = "easy_npc_config_ui";
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EasyNPCTacz.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, EasyNPCTacz.MOD_ID);
    public static final RegistryObject<Item> CONFIG_STICK = ITEMS.register("tacz_config_stick",
            () -> new NPCConfigStickItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<MenuType<NPCGunConfigMenu>> GUN_CONFIG = MENUS.register("npc_gun_config",
            () -> IForgeMenuType.create(NPCGunConfigMenu::new));

    private NPCConfigRegistry() {}

    public static void register(IEventBus bus) {
        // No reference to Config UI classes: this remains safe when the optional mod is absent.
        if (ModList.get().isLoaded(CONFIG_MOD)) {
            ITEMS.register(bus);
            MENUS.register(bus);
            bus.addListener(NPCConfigRegistry::creativeTab);
        }
    }

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == ModTabs.TAB_CONFIG_ITEMS.get()) {
            event.accept(CONFIG_STICK.get());
        }
    }

    public static boolean isHoldingStick(Player player) {
        return CONFIG_STICK.isPresent() && (player.getMainHandItem().is(CONFIG_STICK.get())
                || player.getOffhandItem().is(CONFIG_STICK.get()));
    }

    public static boolean canEdit(ServerPlayer player, Mob mob) {
        return CONFIG_STICK.isPresent() && player.isAlive() && mob.isAlive() && mob.level() == player.level()
                && player.distanceToSqr(mob) <= 64.0 && isHoldingStick(player)
                && mob instanceof EasyNPC<?> npc
                && AccessManager.hasAccess(player, npc)
                && SecurityManager.checkFeatureAccess(player, npc, NpcFeature.OBJECTIVE).allowed();
    }
}
