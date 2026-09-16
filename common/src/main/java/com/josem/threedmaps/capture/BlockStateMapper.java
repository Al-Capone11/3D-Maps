package com.josem.threedmaps.capture;

import net.minecraft.world.level.block.state.BlockState;

/** Traduce BlockState a material (argb+flags). Aqui se retiene la inflacion de color; no hay estado. */
public final class BlockStateMapper {
    private BlockStateMapper() {}

    public record Mapping(int argb, int flags) {}
}
