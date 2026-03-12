package me.myogoo.myotus.api.config;

public interface ConfigFileProvider {
    void updateState();
    void save();
}
