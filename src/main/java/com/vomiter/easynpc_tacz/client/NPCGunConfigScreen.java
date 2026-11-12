package com.vomiter.easynpc_tacz.client;

import com.vomiter.easynpc_tacz.config.NPCGunConfigMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class NPCGunConfigScreen
        extends AbstractContainerScreen<NPCGunConfigMenu> {

    private static final int BORDER_COLOR = 0xFF373737;
    private static final int HIGHLIGHT_COLOR = 0xFFFFFFFF;
    private static final int SHADOW_COLOR = 0xFF555555;
    private static final int BACKGROUND_COLOR = 0xFFC6C6C6;
    private static final int TEXT_COLOR = 0x404040;
    private static final int DESCRIPTION_COLOR = 0x606060;

    private SettingCheckbox ammo;
    private SettingCheckbox cooldown;

    public NPCGunConfigScreen(
            NPCGunConfigMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
        imageWidth = 276;
        imageHeight = 156;
        titleLabelX = 12;
        titleLabelY = 12;
    }

    @Override
    protected void init() {
        super.init();

        ammo = addRenderableWidget(new SettingCheckbox(
                leftPos + 12,
                topPos + 38,
                Component.translatable("menu.easynpc_tacz.consume_ammo"),
                menu.consumeAmmo(),
                0
        ));

        cooldown = addRenderableWidget(new SettingCheckbox(
                leftPos + 12,
                topPos + 66,
                Component.translatable("menu.easynpc_tacz.additional_cooldown"),
                menu.additionalCooldown(),
                2
        ));

        cooldown.setTooltip(Tooltip.create(
                Component.translatable("menu.easynpc_tacz.cooldown_hint")
        ));

        addRenderableWidget(Button.builder(
                Component.translatable("menu.easynpc_tacz.reset"),
                button -> send(4)
        ).bounds(leftPos + 12, topPos + 118, 150, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> onClose()
        ).bounds(leftPos + 174, topPos + 118, 90, 20).build());
    }

    private void send(int button) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(
                    menu.containerId,
                    button
            );
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ammo.sync(menu.consumeAmmo());
        cooldown.sync(menu.additionalCooldown());
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int x = leftPos;
        int y = topPos;
        int w = imageWidth;
        int h = imageHeight;

        graphics.fill(x, y, x + w, y + h, BORDER_COLOR);
        graphics.fill(
                x + 1, y + 1,
                x + w - 1, y + h - 1,
                HIGHLIGHT_COLOR
        );
        graphics.fill(
                x + 2, y + 2,
                x + w - 1, y + h - 1,
                SHADOW_COLOR
        );
        graphics.fill(
                x + 2, y + 2,
                x + w - 2, y + h - 2,
                BACKGROUND_COLOR
        );
    }

    @Override
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.drawString(
                font, title,
                titleLabelX, titleLabelY,
                TEXT_COLOR, false
        );

        graphics.drawString(
                font, ammo.getMessage(),
                36, 44,
                TEXT_COLOR, false
        );

        graphics.drawString(
                font, cooldown.getMessage(),
                36, 72,
                TEXT_COLOR, false
        );

        graphics.drawString(
                font,
                Component.translatable("menu.easynpc_tacz.saved_immediately"),
                12, 98,
                DESCRIPTION_COLOR, false
        );
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private final class SettingCheckbox extends Checkbox {
        private final int buttonBase;

        private SettingCheckbox(
                int x,
                int y,
                Component label,
                boolean checked,
                int buttonBase
        ) {
            // 標籤改由 renderLabels() 以深色繪製。
            super(x, y, 252, 20, label, checked, false);
            this.buttonBase = buttonBase;
        }

        @Override
        public void onPress() {
            super.onPress();
            send(buttonBase + (selected() ? 1 : 0));
        }

        private void sync(boolean checked) {
            if (selected() != checked) {
                // 同步伺服器狀態，不再次送出修改。
                super.onPress();
            }
        }
    }
}