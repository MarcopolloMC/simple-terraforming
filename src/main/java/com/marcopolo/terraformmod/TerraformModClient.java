package com.marcopolo.terraformmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class TerraformModClient implements ClientModInitializer {
    public static KeyBinding OPEN_MENU_KEY;
    public static KeyBinding TOGGLE_TERRAFORM_KEY;

    @Override
    public void onInitializeClient() {
        // Keybindings registrieren
        OPEN_MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.terraform-mod.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_APOSTROPHE,
                "category.terraform-mod"
        ));

        TOGGLE_TERRAFORM_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.terraform-mod.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_T,
                "category.terraform-mod"
        ));

        // World Render Event für Würfel-Rendering
        WorldRenderEvents.END.register(context -> {
            TerraformWorldRender.renderCube(context);
        });

        // Client Tick Event registrieren
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Menü öffnen
            if (OPEN_MENU_KEY.wasPressed()) {
                client.setScreen(new TerraformScreen());
            }

            // Terraforming toggle
            if (TOGGLE_TERRAFORM_KEY.wasPressed()) {
                TerraformHandler.toggleTerraforming();
            }
        });

        // Mining Tick Event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (TerraformHandler.isTerraformingActive() && client.player != null) {
                TerraformHandler.mineTerrain(client);
            }
        });
    }
}
