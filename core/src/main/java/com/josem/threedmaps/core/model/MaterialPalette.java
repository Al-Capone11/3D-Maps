package com.josem.threedmaps.core.model;

import java.util.ArrayList;
import java.util.List;

/** Paleta de materiales de una seccion (id pequeño por voxel). */
public final class MaterialPalette {
    private final List<VoxelMaterial> materials = new ArrayList<>();

    public MaterialPalette() {
        // id 0 = aire reservado
        if (materials.isEmpty()) materials.add(null);
    }

    /** Registra el material y devuelve su id local. */
    public int register(VoxelMaterial m) {
        for (int i = 1; i < materials.size(); i++) {
            if (materials.get(i).equals(m)) return i;
        }
        materials.add(m);
        return materials.size() - 1;
    }

    public int size() { return materials.size() - 1; } // sin contar aire
    public VoxelMaterial get(int id) { return id == 0 ? null : materials.get(id); }
}
