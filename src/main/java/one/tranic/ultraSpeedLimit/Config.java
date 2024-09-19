package one.tranic.ultraSpeedLimit;

import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Config {
    private final File configFile;
    private final YamlConfiguration configuration;

    public int connections = 2;
    public int expiredConnections = 30;
    public String connectFailedMessage = "The connection speed limit has been reached! Please come back later!";

    public Config(Path dataDirectory) {
        configFile = dataDirectory.getParent().resolve("UltraSpeedLimit").resolve("config.yml").toFile();
        try {
            if (!configFile.exists()) {
                if (!configFile.getParentFile().exists()) {
                    configFile.getParentFile().mkdir();
                }
                configFile.createNewFile();
            }
            configuration = YamlConfiguration.loadConfiguration(configFile);
            save();
            read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void read() {
        connections = configuration.getInt("connections");
        expiredConnections = configuration.getInt("expired-connections");
        if (expiredConnections < 1) {
            expiredConnections = 1;
        }
        connectFailedMessage = configuration.getString("connect-failed-message");
    }

    private void save() throws IOException {
        configuration.addDefault("connections", 2);
        configuration.addDefault("expired-connections", 30);
        configuration.addDefault("connect-failed-message", "The connection speed limit has been reached! Please come back later!");

        configuration.options().copyDefaults(true);
        configuration.save(configFile);
    }
}
