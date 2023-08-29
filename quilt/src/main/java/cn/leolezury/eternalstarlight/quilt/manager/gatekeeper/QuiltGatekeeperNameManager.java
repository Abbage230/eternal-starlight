package cn.leolezury.eternalstarlight.quilt.manager.gatekeeper;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.manager.gatekeeper.TheGatekeeperNameManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class QuiltGatekeeperNameManager extends TheGatekeeperNameManager implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation(EternalStarlight.MOD_ID, "gatekeeper_names");
    }
}
