package cn.leolezury.eternalstarlight.neoforge.block.fluid;

import cn.leolezury.eternalstarlight.common.block.fluid.EtherFluid;
import cn.leolezury.eternalstarlight.neoforge.registry.ESFluidTypes;
import net.neoforged.neoforge.fluids.FluidType;

public abstract class ForgeEtherFluid extends EtherFluid {
	public static class Flowing extends EtherFluid.Flowing {
		@Override
		public FluidType getFluidType() {
			return ESFluidTypes.ETHER.get();
		}
	}

	public static class Still extends EtherFluid.Still {
		@Override
		public FluidType getFluidType() {
			return ESFluidTypes.ETHER.get();
		}
	}
}
