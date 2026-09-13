package net.mysticapi.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class SQLiteDatabase extends Database {

    private final File file;

    public SQLiteDatabase(File file) {
        this.file = file;
    }

    public SQLiteDatabase(JavaPlugin plugin, String fileName) {
        this(new File(plugin.getDataFolder(), fileName));
    }

    @Override
    public void connect() {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(1);
        config.setPoolName("MysticAPI-SQLite");
        this.dataSource = new HikariDataSource(config);
    }
}
