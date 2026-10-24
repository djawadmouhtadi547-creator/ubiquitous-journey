package com.novaclient.keybind;

import com.novaclient.NovaClient;
import com.novaclient.config.NovaConfig;
import com.novaclient.module.FeatureModule;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

public final class KeybindManager {
    private final KeyBinding openMenu;
    private final Map<String, KeyBinding> modules = new LinkedHashMap<>();

    public KeybindManager() {
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nova_client.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.category.nova_client"));
        for (FeatureModule module : NovaClient.MODULES.all()) {
            KeyBinding key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.nova_client.module." + module.id(), InputUtil.Type.KEYSYM,
                    module.defaultKeyCode(), "key.category.nova_client"));
            modules.put(module.id(), key);
        }
        KeyBinding.updateKeysByCode();
    }

    public KeyBinding openMenu() { return openMenu; }
    public KeyBinding forModule(String id) { return modules.get(id); }

    public int keyCode(String id) {
        KeyBinding key = id.equals("menu") ? openMenu : modules.get(id);
        return key == null ? GLFW.GLFW_KEY_UNKNOWN : key.getBoundKey().getCode();
    }

    public void setKeyCode(String id, int keyCode, NovaConfig config) {
        KeyBinding key = id.equals("menu") ? openMenu : modules.get(id);
        if (key == null) return;
        key.setBoundKey(InputUtil.fromKeyCode(keyCode, 0));
        KeyBinding.updateKeysByCode();
        config.keybinds.put(id, keyCode);
    }

    public void applyConfig(NovaConfig config) {
        setKeyCode("menu", config.keybinds.getOrDefault("menu", GLFW.GLFW_KEY_RIGHT_SHIFT), config);
        for (FeatureModule module : NovaClient.MODULES.all()) {
            setKeyCode(module.id(), config.keybinds.getOrDefault(module.id(), module.defaultKeyCode()), config);
        }
    }

    public boolean hasConflict(String id) {
        int target = keyCode(id);
        if (target < 0) return false;
        int matches = openMenu.getBoundKey().getCode() == target ? 1 : 0;
        for (KeyBinding binding : modules.values()) {
            if (binding.getBoundKey().getCode() == target) matches++;
        }
        return matches > 1;
    }

    public void writeTo(NovaConfig config) {
        config.keybinds.put("menu", openMenu.getBoundKey().getCode());
        modules.forEach((id, key) -> config.keybinds.put(id, key.getBoundKey().getCode()));
    }

    public void clearPendingPresses() {
        while (openMenu.wasPressed()) { /* Drain input while a screen owns focus. */ }
        for (KeyBinding key : modules.values()) while (key.wasPressed()) { /* Drain input while a screen owns focus. */ }
    }
}
