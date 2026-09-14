package net.mysticapi.security;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class IpGuard {

    private static final String[] LOOKUP_SERVICES = {
            "https://api.ipify.org",
            "https://checkip.amazonaws.com"
    };

    private IpGuard() {
    }

    public static void protect(JavaPlugin plugin, String... allowedIps) {
        if (!verify(plugin, allowedIps)) {
            throw new IllegalStateException("IpGuard: unauthorized server, plugin disabled.");
        }
    }

    public static boolean verify(JavaPlugin plugin, String... allowedIps) {
        String currentIp = fetchPublicIp();

        if (currentIp == null) {
            plugin.getLogger().severe("Kon het publieke IP-adres van de server niet ophalen. Plugin wordt uitgeschakeld.");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return false;
        }

        List<String> allowed = Arrays.asList(allowedIps);
        if (!allowed.contains(currentIp)) {
            plugin.getLogger().severe("Dit IP-adres (" + currentIp + ") is niet geautoriseerd om deze plugin te gebruiken.");
            plugin.getLogger().severe("Plugin wordt automatisch uitgeschakeld.");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return false;
        }

        return true;
    }

    private static String fetchPublicIp() {
        for (String service : LOOKUP_SERVICES) {
            String ip = request(service);
            if (ip != null && !ip.isEmpty()) {
                return ip;
            }
        }
        return null;
    }

    private static String request(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.readLine().trim();
            }
        } catch (IOException exception) {
            return null;
        }
    }
}
