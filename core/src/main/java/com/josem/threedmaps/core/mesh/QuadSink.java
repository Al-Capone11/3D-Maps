package com.josem.threedmaps.core.mesh;
public interface QuadSink {
    void quad(int x0,int y0,int z0, int x1,int y1,int z1, int x2,int y2,int z2, int x3,int y3,int z3,
              int argb, int normalPacked, boolean hidden);
}
