package com.danikula.videocache;

import com.danikula.videocache.file.DiskUsage;
import com.danikula.videocache.file.FileNameGenerator;

import java.io.File;

/**
 * Configuration for proxy cache.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
class Config {

    public final File cacheRoot;
    public final FileNameGenerator fileNameGenerator;
    public final DiskUsage diskUsage;
    public final int proxyCacheMemoryTTL;
    public final int networkTimeout;

    Config(File cacheRoot, FileNameGenerator fileNameGenerator, DiskUsage diskUsage, int proxyCacheMemoryTTL, int networkTimeout) {
        this.cacheRoot = cacheRoot;
        this.fileNameGenerator = fileNameGenerator;
        this.diskUsage = diskUsage;
        this.proxyCacheMemoryTTL = proxyCacheMemoryTTL;
        this.networkTimeout = networkTimeout;
    }

    File generateCacheFile(String url) {
        String name = fileNameGenerator.generate(url);
        return new File(cacheRoot, name);
    }

}
