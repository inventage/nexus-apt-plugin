package com.inventage.nexusaptplugin.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.inventage.nexusaptplugin.cache.generators.PackagesGenerator;
import com.inventage.nexusaptplugin.cache.generators.PackagesGzGenerator;
import com.inventage.nexusaptplugin.cache.generators.ReleaseGPGGenerator;
import com.inventage.nexusaptplugin.cache.generators.ReleaseGenerator;
import com.inventage.nexusaptplugin.cache.generators.SignKeyGenerator;
import com.inventage.nexusaptplugin.sign.AptSigningConfiguration;

@Singleton
public class DebianFileManager {

    private Cache<String, byte[]> cache;

    private final Map<String, FileGenerator> generators;

    @Inject
    public DebianFileManager(AptSigningConfiguration aptSigningConfiguration) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(Integer.parseInt(System.getProperty("DebianFileManager.cacheTimeoutSeconds", "5")), TimeUnit.SECONDS)
                .build();

        this.generators = new HashMap<String, FileGenerator>();

        registerGenerator("Packages", new PackagesGenerator());
        registerGenerator("Packages.gz", new PackagesGzGenerator(this));
        registerGenerator("Release", new ReleaseGenerator(this));
        registerGenerator("Release.gpg", new ReleaseGPGGenerator(this, aptSigningConfiguration));
        registerGenerator("apt-key.gpg.key", new SignKeyGenerator(aptSigningConfiguration));
    }

    public void registerGenerator(String name, FileGenerator generator) {
        generators.put(name, generator);
    }

    public void setTimeout(int seconds) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(seconds, TimeUnit.SECONDS)
                .build();
    }

    public byte[] getFile(final String name, final RepositoryData data) throws ExecutionException {
        String key = data.getRepositoryId() + "#" + name;
        if (!generators.containsKey(name)) {
            throw new IllegalArgumentException("Don't know how to generate " + name);
        }

        return cache.get(key, new Callable<byte[]>() {
            @Override
            public byte[] call()
                    throws Exception {
                return generators.get(name).generateFile(data);
            }
        });
    }

}
