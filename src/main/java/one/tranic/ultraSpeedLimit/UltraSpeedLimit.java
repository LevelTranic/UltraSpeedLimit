package one.tranic.ultraSpeedLimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.GameProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

@Plugin(id = "ultraspeedlimit", name = "UltraSpeedLimit", version = BuildConstants.VERSION, url = "https://tranic.one", authors = {"404"}, dependencies = {@Dependency(id = "maven-loader")})
public class UltraSpeedLimit {
    private final ProxyServer server;
    private final Logger logger;
    private final Metrics.Factory metricsFactory;
    private final Path dataDirectory;
    private Metrics metrics;

    @Inject
    public UltraSpeedLimit(ProxyServer server, Logger logger, Metrics.Factory metricsFactory, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing UltraSpeedLimit");
        this.metrics = metricsFactory.make(this, 23413);
        try {
            Config config = new Config(dataDirectory);
            Cache<UUID, Integer> playerCache = Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofSeconds(config.expiredConnections))
                    .build();
            server.getEventManager().register(this, new Event(config, playerCache));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (metrics != null) {
            metrics.shutdown();
        }
    }

    public static class Event {
        private final Config config;
        private final Cache<UUID, Integer> playerCache;

        public Event(Config config, Cache<UUID, Integer> playerCache) {
            this.config = config;
            this.playerCache = playerCache;
        }

        @Subscribe
        public void onServerPreConnect(ServerPreConnectEvent e) {
            if (config.connections < 1) return;
            Player player = e.getPlayer();
            GameProfile profile = player.getGameProfile();
            Integer i = playerCache.getIfPresent(profile.getId());
            if (i == null) {
                playerCache.put(profile.getId(), 0);
                return;
            }
            if (i >= config.connections) {
                player.sendMessage(Component.text(config.connectFailedMessage, NamedTextColor.RED));
                e.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }
            i++;
            playerCache.put(profile.getId(), i);
        }
    }
}
