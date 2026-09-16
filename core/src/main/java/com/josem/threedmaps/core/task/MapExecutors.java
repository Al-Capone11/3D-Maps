package com.josem.threedmaps.core.task;
import java.util.concurrent.*;
public final class MapExecutors {
    private MapExecutors() {}
    public static ExecutorService newBuildPool() {
        int n = Math.min(Math.max(Runtime.getRuntime().availableProcessors()/2, 1), 6);
        return Executors.newFixedThreadPool(n, r -> { Thread t=new Thread(r,"3DMaps-Build"); t.setDaemon(true); return t; });
    }
}
