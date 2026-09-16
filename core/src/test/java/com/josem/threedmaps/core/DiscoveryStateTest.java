package com.josem.threedmaps.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscoveryStateTest {
    @Test void initiallyUndiscovered() { assertFalse(new com.josem.threedmaps.core.discovery.DiscoveryState().isDiscovered(0,0,0)); }
    @Test void discoverTwice() { var d=new com.josem.threedmaps.core.discovery.DiscoveryState(); d.discover(1,2,5); assertTrue(d.isDiscovered(1,2,5)); }
}
