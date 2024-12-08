package cn.leolezury.eternalstarlight.common.registry;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.item.menu.CrateMenu;
import cn.leolezury.eternalstarlight.common.platform.registry.RegistrationProvider;
import cn.leolezury.eternalstarlight.common.platform.registry.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ESMenuTypes {
	public static final RegistrationProvider<MenuType<?>> MENU_TYPES = RegistrationProvider.get(Registries.MENU, EternalStarlight.ID);
	public static final RegistryObject<MenuType<?>, MenuType<CrateMenu>> CRATE = MENU_TYPES.register("crate", () -> new MenuType<>(CrateMenu::new, FeatureFlags.VANILLA_SET));

	public static void loadClass() {
	}
}
