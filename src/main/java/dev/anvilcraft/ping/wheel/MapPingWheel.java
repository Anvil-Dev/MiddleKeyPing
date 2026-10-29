package dev.anvilcraft.ping.wheel;

import dev.anvilcraft.lib.v2.wheel.api.WheelOpenMode;
import dev.anvilcraft.lib.v2.wheel.client.gui.screen.WheelScreen;
import dev.anvilcraft.ping.util.PingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/// 在地图上使用 MiddleKeyPing 轮盘，选项发送到地图坐标，关闭后恢复原地图。
public final class MapPingWheel extends WheelScreen {
    private final Screen parent;
    private final Runnable onClose;
    private boolean closing;

    public MapPingWheel(Screen parent, Vec3 position, Runnable onClose) {
        super(WheelMenu.create(type -> PingUtil.send(position, type)), WheelOpenMode.HOLD);
        this.parent = parent;
        this.onClose = onClose;
    }

    @Override
    protected void init() {
        if (this.parent.width != this.width || this.parent.height != this.height) {
            this.parent.resize(this.width, this.height);
        }
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.parent.render(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            if (!this.closing) {
                this.triggerFromHoldRelease();
            }
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        this.onClose();
        return true;
    }

    @Override
    public void onClose() {
        if (!this.closing) {
            Minecraft.getInstance().setScreen(this.parent);
        }
    }

    @Override
    public void removed() {
        if (!this.closing) {
            this.closing = true;
            this.onClose.run();
        }
        super.removed();
    }
}
