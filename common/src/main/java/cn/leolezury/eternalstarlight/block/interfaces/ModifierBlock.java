package cn.leolezury.eternalstarlight.block.interfaces;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface ModifierBlock<T extends Block> {
    T modifiers(Consumer... modifiers);

    @Nullable
    ModifierContainer<Block> getModifierContainer();

    default void initModifiers() {
        if(hasModifiers()) {
            getModifierContainer().initModifiers();
        }
    }

    default boolean hasModifiers() {
        return getModifierContainer() != null;
    }
}
