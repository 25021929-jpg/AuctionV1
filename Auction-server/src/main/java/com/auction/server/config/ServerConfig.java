package com.auction.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class ServerConfig {

    private static final Logger log = Logger.getLogger(ServerConfig.class.getName());
    private static volatile ServerConfig instance;
    private final Properties props = new Properties();

    private ServerConfig() {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in == null)
                throw new RuntimeException("Không tìm thấy application.properties!");
            props.load(in);
            log.info("✅ Đã load application.properties");
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc config: " + e.getMessage(), e);
        }
    }

    public static ServerConfig getInstance() {
        if (instance == null) {
            synchronized (ServerConfig.class) {
                if (instance == null) instance = new ServerConfig();
            }
        }
        return instance;
    }

    public int getPort()           { return Integer.parseInt(props.getProperty("server.port", "8080")); }
    public String getDbUrl()       { return props.getProperty("db.url"); }
    public String getDbUser()      { return props.getProperty("db.user"); }
    public String getDbPassword()  { return props.getProperty("db.password"); }
    public int getDbPoolSize()     { return Integer.parseInt(props.getProperty("db.pool.size", "10")); }
    public int getDbPoolMax()      { return Integer.parseInt(props.getProperty("db.pool.max", "20")); }
    public int getSnipeWindowSec() { return Integer.parseInt(props.getProperty("antisniping.window.seconds", "30")); }
    public int getSnipeExtendSec() { return Integer.parseInt(props.getProperty("antisniping.extend.seconds", "60")); }
    public int getMaxExtensions()  { return Integer.parseInt(props.getProperty("antisniping.max.extensions", "5")); }
    public boolean isDevMode()     { return Boolean.parseBoolean(props.getProperty("dev.mode", "false")); }
}