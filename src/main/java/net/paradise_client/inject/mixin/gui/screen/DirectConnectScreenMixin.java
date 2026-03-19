package net.paradise_client.inject.mixin.gui.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.paradise_client.util.IPUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(DirectConnectScreen.class)
public abstract class DirectConnectScreenMixin extends Screen {
    @Shadow private TextFieldWidget addressField;
    
    @Unique
    private IPUtil.IPInfo ipInfo;
    @Unique
    private boolean loading = false;
    @Unique
    private String lastAddress = "";

    protected DirectConnectScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        lastAddress = addressField.getText();
        if (!lastAddress.isEmpty()) {
            updateIPInfo();
        }
    }

    private void updateIPInfo() {
        if (lastAddress.isEmpty()) {
            ipInfo = null;
            return;
        }
        loading = true;
        CompletableFuture.runAsync(() -> {
            try {
                String host = lastAddress;
                if (host.contains(":")) {
                    host = host.split(":")[0];
                }
                ipInfo = IPUtil.getIPInfo(host);
            } catch (Exception e) {
                ipInfo = null;
            } finally {
                loading = false;
            }
        });
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        String currentAddress = addressField.getText();
        if (!currentAddress.equals(lastAddress)) {
            lastAddress = currentAddress;
            updateIPInfo();
        }
        
        int x = 20;
        int y = this.height / 2 - 60;
        
        drawInfo(context, "Organization", ipInfo == null ? "Unknown" : ipInfo.organisation, x, y);
        drawInfo(context, "Country", ipInfo == null ? "Unknown" : ipInfo.country, x, y + 12);
        drawInfo(context, "City", ipInfo == null ? "Unknown" : ipInfo.city, x, y + 24);
        drawInfo(context, "Region", ipInfo == null ? "Unknown" : ipInfo.region, x, y + 36);
        drawInfo(context, "AS", ipInfo == null ? "Unknown" : ipInfo.as, x, y + 48);
        drawInfo(context, "ISP", ipInfo == null ? "Unknown" : ipInfo.isp, x, y + 60);
        drawInfo(context, "Timezone", ipInfo == null ? "Unknown" : ipInfo.timezone, x, y + 72);
        drawInfo(context, "IP", ipInfo == null ? "Unknown" : ipInfo.ip, x, y + 84);
        drawInfo(context, "Country Code", ipInfo == null ? "Unknown" : ipInfo.countryCode, x, y + 96);
    }

    private void drawInfo(DrawContext context, String label, String value, int x, int y) {
        String displayValue = loading ? "§cLoading..." : "§c" + value;
        context.drawTextWithShadow(this.textRenderer, label + " » " + displayValue, x, y, 0xFFFFFF);
    }
}
