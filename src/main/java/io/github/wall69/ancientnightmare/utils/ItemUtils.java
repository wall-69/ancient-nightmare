package io.github.wall69.ancientnightmare.utils;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.arena.ArenaLocation;
import io.github.wall69.ancientnightmare.game.SpecialAbilityItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemUtils {

    private final Main main;

    public ItemUtils(Main main) {
        this.main = main;
    }

    public ItemStack getSetupItem(ArenaLocation arenaLocation, String arenaName) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = item.getItemMeta();

        switch (arenaLocation) {
            case LOBBY:
                item = new ItemStack(Material.REDSTONE_BLOCK);
                itemMeta.setDisplayName("§cLOBBY location");
                itemMeta.setLocalizedName("an_setupLobby");
                break;

            case WARDEN_SPAWN:
                item = new ItemStack(Material.SCULK);
                itemMeta.setDisplayName("§0§lWARDEN spawn location");
                itemMeta.setLocalizedName("an_setupWardenSpawn");
                break;

            case SECURITY_SPAWN:
                item = new ItemStack(Material.IRON_BLOCK);
                itemMeta.setDisplayName("§9§lSECURITY spawn location");
                itemMeta.setLocalizedName("an_setupSecuritySpawn");
                break;

            case BATTERY_SUPPLY_BLOCK:
                item = new ItemStack(Material.ECHO_SHARD);
                itemMeta.setDisplayName("§b§lBATTERY barrel block location");
                itemMeta.setLocalizedName("an_setupBarrelBlock");
                break;

            case GENERATOR_BLOCK:
                item = new ItemStack(Material.REDSTONE);
                itemMeta.setDisplayName("§e§lGENERATOR location");
                itemMeta.setLocalizedName("an_setupGeneratorBlock");
                break;

            case EXIT_REGION_POS_1:
            case EXIT_REGION_POS_2:
                item = new ItemStack(Material.LIME_STAINED_GLASS);
                itemMeta.setDisplayName("§a§lEXIT region");
                itemMeta.setLocalizedName("an_setupExitRegion");
                break;

            case ARENA_REGION_POS_1:
            case ARENA_REGION_POS_2:
                item = new ItemStack(Material.RED_STAINED_GLASS);
                itemMeta.setDisplayName("§a§lArena region");
                itemMeta.setLocalizedName("an_setupArenaRegion");
                break;
        }

        itemMeta.setLore(Arrays.asList(arenaName, arenaLocation.toString()));

        item.setItemMeta(itemMeta);

        return item;
    }

    public ItemStack getLobbySelectionItem(String which) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = item.getItemMeta();

        switch (which) {
            case "security":
                item = new ItemStack(Material.DIAMOND_SWORD);
                itemMeta.setDisplayName(main.getFileUtils().getString("items.lobby.play-security"));
                itemMeta.setLocalizedName("an_selectSecurity");
                break;
            case "warden":
                item = new ItemStack(Material.NETHERITE_SWORD);
                itemMeta.setDisplayName(main.getFileUtils().getString("items.lobby.play-warden"));
                itemMeta.setLocalizedName("an_selectWarden");
                break;
        }

        item.setItemMeta(itemMeta);

        return item;
    }

    public ItemStack getLobbyLeaveItem(){
        ItemStack item = new ItemStack(Material.ECHO_SHARD);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(main.getFileUtils().getString("items.lobby.leave"));
        itemMeta.setLocalizedName("an_leave");

        item.setItemMeta(itemMeta);

        return item;
    }

    public ItemStack getHelpBook() {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta itemMeta = item.getItemMeta();
        BookMeta bookMeta = (BookMeta) itemMeta;

        bookMeta.setDisplayName(main.getFileUtils().getString("items.lobby.help-book.name"));
        bookMeta.setTitle(main.getFileUtils().getString("items.lobby.help-book.name"));
        List<String> pages = new ArrayList<>();
        for (String page : main.getFileUtils().getStringList("items.lobby.help-book.pages")) {
            pages.add(page.replace("{nl}", "\n").replace("{apostrophe}", "'"));
        }
        bookMeta.setPages(pages);

        bookMeta.setAuthor("AncientNightmare");
        bookMeta.setLocalizedName("an_helpBook");

        item.setItemMeta(bookMeta);

        return item;
    }

    public ItemStack getWardenAbilityItem(SpecialAbilityItem specialAbilityItem) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = item.getItemMeta();

        switch (specialAbilityItem) {
            case WARDEN_SONIC_ATTACK:
                item = new ItemStack(Material.WARDEN_SPAWN_EGG);
                itemMeta.setDisplayName(main.getFileUtils().getString("items.game.warden.sonic-attack.name"));
                itemMeta.setLore(main.getFileUtils().getStringList("items.game.warden.sonic-attack.lore"));
                break;

            case WARDEN_STEALTH:
                item = new ItemStack(Material.FEATHER);
                itemMeta.setDisplayName(main.getFileUtils().getString("items.game.warden.stealth.name"));
                itemMeta.setLore(main.getFileUtils().getStringList("items.game.warden.stealth.lore"));
                break;

            case WARDEN_RAGE:
                item = new ItemStack(Material.FIRE_CHARGE);
                itemMeta.setDisplayName(main.getFileUtils().getString("items.game.warden.rage.name"));
                itemMeta.setLore(main.getFileUtils().getStringList("items.game.warden.rage.lore"));
                break;

            case WARDEN_COMPASS:
                item = new ItemStack(Material.COMPASS);
                itemMeta.setDisplayName(main.getFileUtils().getString("items.game.warden.compass.name"));
                itemMeta.setLore(main.getFileUtils().getStringList("items.game.warden.compass.lore"));
                break;
        }

        itemMeta.setLocalizedName(specialAbilityItem.getLocalizedName());

        item.setItemMeta(itemMeta);

        return item;
    }

    public ItemStack getSecurityAbilityItem(SpecialAbilityItem specialAbilityItem) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = item.getItemMeta();

        if(itemMeta == null)
            return null;

        switch (specialAbilityItem) {
            case SECURITY_APPLE:
                item = new ItemStack(Material.APPLE, main.getFileUtils().getSecurityAppleAmount());
                itemMeta.setDisplayName(main.getFileUtils().getString("items.game.security.apple.name"));
                itemMeta.setLore(main.getFileUtils().getStringList("items.game.security.apple.lore"));
                break;

            case SECURITY_FAKE_SOUND:
                item = new ItemStack(Material.EGG);
                itemMeta.setDisplayName(main.getFileUtils().getString("items.game.security.fake-sound.name"));
                itemMeta.setLore(main.getFileUtils().getStringList("items.game.security.fake-sound.lore"));
                break;

            case SECURITY_BATTERY:
                item = new ItemStack(Material.ECHO_SHARD);
                itemMeta.setDisplayName(main.getFileUtils().getString("items.game.security.battery.name"));
                itemMeta.setLore(main.getFileUtils().getStringList("items.game.security.battery.lore"));
                break;

            case SECURITY_BATON:
                item = new ItemStack(Material.STICK);
                itemMeta.setDisplayName(main.getFileUtils().getString("items.game.security.baton.name"));
                itemMeta.setLore(main.getFileUtils().getStringList("items.game.security.baton.lore"));
                break;
        }

        itemMeta.setLocalizedName(specialAbilityItem.getLocalizedName());

        item.setItemMeta(itemMeta);

        return item;
    }

    public ItemStack getGUIJoinItem(Arena arena) {
        Material material = Material.BLACK_DYE;

        switch(arena.getState()){
            case WAITING:
                material = Material.LIME_STAINED_GLASS;
                break;
            case COUNTDOWN:
                material = Material.BLUE_STAINED_GLASS;
                break;
            case PLAYING:
                material = Material.ORANGE_STAINED_GLASS;
                break;
            case ENDING:
                material = Material.RED_STAINED_GLASS;
                break;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(main.getFileUtils().getString("join-gui.join-item.name")
                .replace("{arena}", arena.getName())
                .replace("{arena_state}",
                        main.getFileUtils().getString("arena-state." + arena.getState().getPathName()))
                .replace("{players}", String.valueOf(arena.getPlayers().size())));

        List<String> lore = new ArrayList<>();
        for(String s : main.getFileUtils().getStringList("join-gui.join-item.lore")){
            lore.add(s.replace("{arena}", arena.getName())
                    .replace("{arena_state}",
                            main.getFileUtils().getString("arena-state." + arena.getState().getPathName()))
                    .replace("{players}", String.valueOf(arena.getPlayers().size())));
        }

        itemMeta.setLore(lore);
        itemMeta.setLocalizedName("an_guiJoin" + arena.getName());

        item.setItemMeta(itemMeta);

        return item;
    }

    public ItemStack getGUINextItem(String which, int page) {
        ItemStack item = new ItemStack(Material.ECHO_SHARD);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(main.getFileUtils().getString("join-gui.go-" + which + "-item.name"));

        if(which.equals("left")) {
            itemMeta.setLocalizedName("an_guiGoLeft" + page);
        } else if(which.equals("right")){
            itemMeta.setLocalizedName("an_guiGoRight" + page);
        }

        item.setItemMeta(itemMeta);

        return item;
    }

    public void giveLobbyItems(Player player){
        player.getInventory().setItem(0, getHelpBook());
        if(main.getFileUtils().allowRoleSelect()) {
            player.getInventory().setItem(1, getLobbySelectionItem("warden"));
            player.getInventory().setItem(2, getLobbySelectionItem("security"));
        }
        player.getInventory().setItem(8, getLobbyLeaveItem());
    }

    public void giveWardenItems(Player warden){
        if (main.getFileUtils().isWardenAbilityEnabled("sonic-attack")) {
            warden.getInventory().setItem(4, getWardenAbilityItem(SpecialAbilityItem.WARDEN_SONIC_ATTACK));
        }
        if (main.getFileUtils().isWardenAbilityEnabled("stealth")) {
            warden.getInventory().setItem(3, getWardenAbilityItem(SpecialAbilityItem.WARDEN_STEALTH));
        }
        if (main.getFileUtils().isWardenAbilityEnabled("rage")) {
            warden.getInventory().setItem(5, getWardenAbilityItem(SpecialAbilityItem.WARDEN_RAGE));
        }
        if (main.getFileUtils().isWardenAbilityEnabled("compass")) {
            warden.getInventory().setItem(2, getWardenAbilityItem(SpecialAbilityItem.WARDEN_COMPASS));
        }
    }

    public void giveSecurityItems(Player security){
        if(main.getFileUtils().isSecurityAbilityEnabled("apple")) {
            security.getInventory().addItem(getSecurityAbilityItem(SpecialAbilityItem.SECURITY_APPLE));
        }
        if (main.getFileUtils().isSecurityAbilityEnabled("fake-sound")) {
            security.getInventory().addItem(getSecurityAbilityItem(SpecialAbilityItem.SECURITY_FAKE_SOUND));
        }
        if (main.getFileUtils().isSecurityAbilityEnabled("baton")) {
            security.getInventory().addItem(getSecurityAbilityItem(SpecialAbilityItem.SECURITY_BATON));
        }
    }

}
