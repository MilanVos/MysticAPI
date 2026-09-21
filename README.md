# MysticAPI

Utility API library voor Paper/Spigot plugin development. Bevat kant-en-klare modules voor commands, GUI's, database access, cooldowns, scheduling en item building.

## Installatie

Voeg JitPack toe als repository en de dependency aan je `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.MilanVos</groupId>
        <artifactId>MysticAPI</artifactId>
        <version>v1.2</version>
    </dependency>
</dependencies>
```

> **Let op:** shade/relocate MysticAPI (en de database-drivers HikariCP, mysql-connector-j, sqlite-jdbc als je die gebruikt) in je eigen plugin via `maven-shade-plugin`, anders krijg je een `ClassNotFoundException` op de server.

---

## IpGuard

Beperk het gebruik van je plugin tot specifieke server-IP's. Als het publieke IP van de server niet in de toegestane lijst staat, wordt de plugin automatisch uitgeschakeld.

```java
@Override
public void onEnable() {
    IpGuard.protect(this, "123.45.67.89", "98.76.54.32");

    // rest van je onEnable — wordt niet uitgevoerd als het IP niet klopt
}
```

`protect(...)` gooit een `IllegalStateException` als het IP niet is toegestaan (of niet opgehaald kon worden). Bukkit vangt dit automatisch op tijdens `onEnable()`, logt de fout en schakelt de plugin uit.

Wil je zelf controle over de flow in plaats van een exception? Gebruik dan `IpGuard.verify(...)`, die een `boolean` teruggeeft:

```java
if (!IpGuard.verify(this, "123.45.67.89")) {
    return;
}
```

Het publieke IP wordt opgehaald via `api.ipify.org` (met `checkip.amazonaws.com` als fallback).

---

## Command Framework

Annotatie-based commando's zonder `plugin.yml` registratie.

```java
@Command(name = "mystic", aliases = {"myst"}, permission = "mystic.use")
public class MysticCommand implements CommandBase {

    @Default
    public void onDefault(CommandContext ctx) {
        ctx.sendMessage("§7Gebruik: /mystic <reload|info>");
    }

    @SubCommand(name = "reload", permission = "mystic.reload")
    public void onReload(CommandContext ctx) {
        ctx.sendMessage("§aConfig herladen!");
    }

    @SubCommand(name = "info", playerOnly = true)
    public void onInfo(CommandContext ctx) {
        ctx.getPlayer().sendMessage("Je speelt op " + ctx.getPlayer().getWorld().getName());
    }
}
```

```java
// in onEnable()
new CommandManager(this).register(new MysticCommand());
```

Method-signatures mogen `(CommandContext ctx)` of `(CommandSender sender, String[] args)` zijn.

---

## GUI / Menu Builder

```java
// in onEnable()
new MenuManager(this);
```

```java
Menu menu = Menu.builder()
        .title("§8Mijn Menu")
        .rows(3)
        .item(13, new ItemStack(Material.DIAMOND), event -> {
            event.getWhoClicked().sendMessage("§aJe klikte op de diamant!");
        })
        .fill(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
        .onClose(event -> event.getPlayer().sendMessage("§7Menu gesloten."))
        .build();

menu.open(player);
```

Klikken worden standaard automatisch gecancelled. Zet dit uit met `.cancelClicksByDefault(false)`.

---

## Database Utilities (HikariCP)

```java
Database database = new MySQLDatabase("localhost", 3306, "mystic", "root", "wachtwoord");
database.connect();
```

Of SQLite:

```java
Database database = new SQLiteDatabase(this, "data.db");
database.connect();
```

```java
database.executeUpdate("CREATE TABLE IF NOT EXISTS players (uuid VARCHAR(36), coins INT)");

database.executeUpdate("INSERT INTO players (uuid, coins) VALUES (?, ?)",
        player.getUniqueId().toString(), 100);

database.executeQuery("SELECT coins FROM players WHERE uuid = ?", resultSet -> {
    if (resultSet.next()) {
        int coins = resultSet.getInt("coins");
        player.sendMessage("§7Je hebt §e" + coins + " §7coins.");
    }
}, player.getUniqueId().toString());
```

Async varianten: `executeUpdateAsync(...)` / `executeQueryAsync(...)` retourneren een `CompletableFuture`. Gebruik `Scheduler`/`TaskChain` om terug naar de main thread te springen voor Bukkit API calls.

Vergeet niet `database.disconnect()` aan te roepen in `onDisable()`.

---

## Cooldown Manager

```java
CooldownManager cooldowns = new CooldownManager();

if (cooldowns.isOnCooldown(player, "kit-warrior")) {
    long remaining = cooldowns.getRemaining(player, "kit-warrior", TimeUnit.SECONDS);
    player.sendMessage("§cWacht nog " + remaining + " seconden.");
    return;
}
cooldowns.setCooldown(player, "kit-warrior", 30, TimeUnit.SECONDS);
```

Werkt op basis van `UUID`/`Player` + een vrije key-string, dus bruikbaar voor commands, abilities, kits, etc.

---

## Scheduler Utilities

```java
Scheduler scheduler = new Scheduler(plugin);

scheduler.runLater(() -> player.sendMessage("§aKlaar!"), 20L);
scheduler.runTimer(() -> broadcastTimer(), 0L, 20L);
```

`TaskChain` combineert sync/async stappen zonder geneste schedulers:

```java
scheduler.chain()
        .async(() -> database.executeQuery("SELECT coins FROM players WHERE uuid = ?", rs -> {
            if (rs.next()) coins.set(rs.getInt("coins"));
        }, player.getUniqueId().toString()))
        .sync(() -> player.sendMessage("§7Je hebt §e" + coins.get() + " §7coins."))
        .execute();
```

---

## ItemBuilder

```java
ItemStack sword = new ItemBuilder(Material.DIAMOND_SWORD)
        .name("&b&lVlammenzwaard")
        .lore("&7Een zwaard omgeven door vlammen.", "", "&e+10 Schade")
        .enchant(Enchantment.FIRE_ASPECT, 2)
        .unbreakable(true)
        .hideAttributes()
        .customModelData(1001)
        .build();
```

Ondersteunt ook: `.glow(true)` (enchant-glow zonder zichtbare enchant), `.skullOwner(naam)` en `.skullTexture(base64)` voor custom heads.

---

## Vereisten

- Java 17+
- Paper/Spigot 1.20+
