package com.josem.threedmaps.core.mesh;
/** Oclusion ambiental por vertice (0..3 oclusores) horneada en color. */
public final class AoCalculator {
    private AoCalculator() {}
    public static float factor(int o) { return switch (o) { case 0->1f; case 1->0.8f; case 2->0.65f; default->0.5f; }; }
}
