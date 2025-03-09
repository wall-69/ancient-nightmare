package io.github.wall69.ancientnightmare.listeners;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.arena.Arena;
import io.github.wall69.ancientnightmare.arena.ArenaState;
import io.github.wall69.ancientnightmare.game.Game;
import io.github.wall69.ancientnightmare.game.GameBlock;
import io.github.wall69.ancientnightmare.game.SpecialAbilityItem;
import io.github.wall69.ancientnightmare.game.jumpscare.jumpscares.HeadJumpscare;
import io.github.wall69.ancientnightmare.game.runnables.FakeSound;
import io.github.wall69.ancientnightmare.game.runnables.SecuritySprint;
import org.bukkit.*;
import org.bukkit.Vibration.Destination.BlockDestination;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GameListener implements Listener {

    private final Main main;
    private final Random random;


    public GameListener(Main main) {
        this.main = main;
        this.random = new Random();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        // Jumpscare
        /*if(p.hasMetadata("an_jumpscared")){
            e.setCancelled(true);
        }*/

        if (main.getArenaManager().getArena(p) == null)
            return;

        Arena arena = main.getArenaManager().getArena(p);
        Game game = arena.getGame();
        UUID uuid = p.getUniqueId();

        if (p.getWalkSpeed() == 0.0 && p.getFlySpeed() == 0.0) {
            if (e.getTo().getY() != e.getFrom().getY()) {
                if (p.getGameMode().equals(GameMode.SPECTATOR)) {
                    e.setTo(e.getFrom());
                } else if (p.getGameMode().equals(GameMode.ADVENTURE)) {
                    if (e.getTo().getY() > e.getFrom().getY()) {
                        e.setTo(e.getFrom());
                    }
                }
            }

            return;
        }

        if (arena.getState() != ArenaState.PLAYING)
            return;

        if ((e.getFrom().getX() == e.getTo().getX() && e.getFrom().getZ() == e.getTo().getZ()))
            return;

        if (uuid.equals(game.getSecurity())) {
            if (p.hasPotionEffect(PotionEffectType.UNLUCK)) {
                if (e.getTo().getY() > e.getFrom().getY()) {
                    e.setTo(e.getFrom());
                }
            }

            double wardenDistance = Bukkit.getPlayer(game.getWarden()).getLocation().distance(p.getLocation());
            double increment = wardenDistance > 15 ? wardenDistance / 75 : 0;

            // Movement Sound
            if (System.currentTimeMillis() - game.getLastSound(uuid) > 4000
                    && random.nextDouble() * 100 < (e.getPlayer().isSneaking() ? 0.3 + increment : 1.4 + increment)) {
                Bukkit.getPlayer(game.getWarden()).spawnParticle(Particle.VIBRATION, p.getLocation(), 1, new Vibration(
                        p.getLocation(), new BlockDestination(p.getLocation().clone().add(0, 1.8, 0)), 50));
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_SCULK_SENSOR_STEP, 5.0f, 1.0f);
                p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 2 * 20, 0, false, false));
                game.setLastSound(uuid);
            }

            game.setLastMovement(game.getSecurity());
        } else if (uuid.equals(game.getWarden())) {
            if (random.nextDouble() * 100 < 0.3 && !game.isOnItemCooldown(uuid, "wardenStealthAbility")) {
                List<Sound> possibleSounds = new ArrayList<>();
                possibleSounds.add(Sound.ENTITY_WARDEN_AGITATED);
                possibleSounds.add(Sound.ENTITY_WARDEN_HEARTBEAT);
                possibleSounds.add(Sound.ENTITY_WARDEN_SNIFF);
                possibleSounds.add(Sound.ENTITY_WARDEN_AMBIENT);
                p.getWorld().playSound(p.getLocation(), possibleSounds.get(random.nextInt(possibleSounds.size())), 0.3f,
                        1.0f);
            }
        }
    }

    @EventHandler
    public void onToggleSprint(PlayerToggleSprintEvent e) {
        Player p = e.getPlayer();

        if (main.getArenaManager().getArena(p) == null)
            return;

        Arena arena = main.getArenaManager().getArena(p);
        Game game = arena.getGame();

        if (arena.getState() != ArenaState.PLAYING)
            return;

        if (p.getUniqueId().equals(game.getSecurity()) && e.isSprinting()) {
            new SecuritySprint(main, arena, p.getUniqueId()).start();
            Bukkit.getPlayer(game.getWarden()).spawnParticle(Particle.VIBRATION, p.getLocation(), 1,
                    new Vibration(p.getLocation(), new BlockDestination(p.getLocation().clone().add(0, 1.8, 0)), 50));
            p.getWorld().playSound(p.getLocation(), Sound.BLOCK_SCULK_SENSOR_STEP, 5.0f, 1.0f);
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 4 * 20, 0, false, false));
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            Player p = (Player) e.getDamager();

            if (main.getArenaManager().getArena(p) == null)
                return;

            Arena arena = main.getArenaManager().getArena(p);
            Game game = arena.getGame();

            if (arena.getState() != ArenaState.PLAYING) {
                e.setCancelled(true);
                return;
            }

            if (p.getUniqueId().equals(game.getWarden())
                    && e.getEntity().getUniqueId().equals(game.getSecurity())) {
                if (p.hasPotionEffect(PotionEffectType.WEAKNESS)) {
                    e.setCancelled(true);
                    return;
                }

                e.setCancelled(true);

                Player other = (Player) e.getEntity();
                other.setHealth(20.0d);
                other.setGameMode(GameMode.SPECTATOR);

                for (int i = 0; i < 8; i++) {
                    double randomX = random.nextBoolean() ? random.nextDouble() : -random.nextDouble();
                    double randomY = random.nextDouble();
                    double randomZ = random.nextBoolean() ? random.nextDouble() : -random.nextDouble();

                    p.getWorld().spawnParticle(Particle.CLOUD,
                            other.getLocation().clone().add(randomX, randomY, randomZ),
                            1, 0, 0, 0, 0);
                }

                other.getWorld().spawnParticle(Particle.BLOCK_DUST, other.getLocation(),
                        35, Material.REDSTONE_BLOCK.createBlockData());

                other.getWorld().playSound(other.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
                other.getWorld().playSound(other.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);

                arena.sendMessage(main.getFileUtils().getString("arena.game.security-death"));
                game.gameWin("warden");
            } else if (p.getUniqueId().equals(game.getSecurity())
                    && e.getEntity().getUniqueId().equals(game.getWarden())) {
                ItemStack itemInHand = p.getEquipment().getItemInMainHand();
                if (itemInHand != null && itemInHand.getItemMeta() != null && itemInHand.getItemMeta().getLocalizedName().
                        equals("an_securityBaton") && !p.hasPotionEffect(PotionEffectType.UNLUCK)) {
                    if (game.isOnItemCooldown(p.getUniqueId(), itemInHand.getItemMeta().getLocalizedName())) {
                        p.sendMessage(main.prefix + main.getFileUtils().getString("items.game.item-on-cooldown"));
                        e.setCancelled(true);
                        return;
                    }

                    Player warden = (Player) e.getEntity();
                    warden.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 4 * 20, 1, false, false));
                    warden.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 10 * 20, 0, false, false));
                    warden.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 8 * 20, 0, false, false));
                    warden.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 4 * 20, 0, false, false));

                    p.getWorld().playSound(warden.getLocation(), Sound.ENTITY_WARDEN_HURT, 1.0f, 1.0f);

                    game.setItemCooldown(p.getUniqueId(), itemInHand.getItemMeta().getLocalizedName(),
                            main.getFileUtils().getSecurityAbilityCooldown("baton"));
                }

                e.setDamage(0);
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;

        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)
            return;

        Player p = (Player) e.getEntity();

        if (main.getArenaManager().getArena(p) == null)
            return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();

            if (main.getArenaManager().getArena(p) == null)
                return;

            if (e.getItem() == null) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack itemInHand = p.getEquipment().getItemInMainHand();

        if (main.getArenaManager().getArena(p) == null)
            return;

        Arena arena = main.getArenaManager().getArena(p);
        Game game = arena.getGame();
        UUID uuid = p.getUniqueId();

        if (arena.getState() != ArenaState.PLAYING)
            return;

        if (!e.getHand().equals(EquipmentSlot.HAND) || p.isDead())
            return;

        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            e.setCancelled(true);
            return;
        }

        boolean hasItemInHand = itemInHand != null && itemInHand.getItemMeta() != null;

        if (uuid.equals(game.getWarden())) {
            if ((e.hasBlock() && e.getClickedBlock().getBlockData() instanceof Bed) ||
                    p.hasPotionEffect(PotionEffectType.WEAKNESS)) {
                e.setCancelled(true);
                return;
            }

            if (!hasItemInHand)
                return;

            switch (itemInHand.getItemMeta().getLocalizedName()) {
                case "an_wardenSonicAttack":
                    if (game.isOnItemCooldown(uuid, itemInHand.getItemMeta().getLocalizedName())) {
                        p.sendMessage(main.prefix + main.getFileUtils().getString("items.game.item-on-cooldown"));
                        e.setCancelled(true);
                        return;
                    }

                    game.setItemCooldown(uuid, itemInHand.getItemMeta().getLocalizedName(),
                            main.getFileUtils().getWardenAbilityCooldown(
                                    "sonic-attack"));

                    for (int i = 1; i < main.getFileUtils().getWardenSonicAttackMaxRange(); i++) {
                        Vector direction = p.getLocation().getDirection().normalize();
                        direction = direction.multiply(i);
                        Location particleLocation = p.getLocation().clone().add(direction.getX(), 1, direction.getZ());

                        // Block hit
                        if (isCube(p.getWorld().getBlockAt(particleLocation))) {
                            p.getWorld().playSound(p.getWorld().getBlockAt(particleLocation).getLocation(),
                                    Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);
                            i = main.getFileUtils().getWardenSonicAttackMaxRange();
                        }

                        // Entity hit
                        for (Entity en : p.getWorld().getNearbyEntities(particleLocation, 0.3, 1, 0.3)) {
                            if (en instanceof Player) {
                                if (en.getUniqueId().equals(game.getSecurity())) {
                                    Player security = (Player) en;
                                    security.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 7 * 20, 0,
                                            false, false));
                                    security.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 7 * 20, 4, false
                                            , false));
                                    security.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, 7 * 20, 4,
                                            false, false));
                                    security.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 7 * 20, 4,
                                            false, false));
                                    security.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 7 * 20, 0,
                                            false, false));

                                    p.getWorld().playSound(particleLocation, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f,
                                            1.0f);
                                    i = main.getFileUtils().getWardenSonicAttackMaxRange();
                                }

                            } else if (en instanceof LivingEntity) {
                                p.getWorld().playSound(particleLocation, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);
                                i = main.getFileUtils().getWardenSonicAttackMaxRange();
                            }
                        }

                        if (i + 1 == main.getFileUtils().getWardenSonicAttackMaxRange()) {
                            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);
                        }

                        p.getWorld().spawnParticle(Particle.SONIC_BOOM, particleLocation, 1);
                    }
                    break;

                case "an_wardenStealthAbility":
                    if (game.isOnItemCooldown(uuid, itemInHand.getItemMeta().getLocalizedName())) {
                        p.sendMessage(main.prefix + main.getFileUtils().getString("items.game.item-on-cooldown"));
                        e.setCancelled(true);
                        return;
                    }

                    game.setItemCooldown(uuid, itemInHand.getItemMeta().getLocalizedName(),
                            main.getFileUtils().getWardenAbilityCooldown(
                                    "stealth"));

                    List<Sound> possibleSounds = new ArrayList<>();
                    possibleSounds.add(Sound.ENTITY_WARDEN_AGITATED);
                    possibleSounds.add(Sound.ENTITY_WARDEN_HEARTBEAT);
                    possibleSounds.add(Sound.ENTITY_WARDEN_SNIFF);
                    possibleSounds.add(Sound.ENTITY_WARDEN_AMBIENT);

                    p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 8 * 20, 0,
                            false, false));
                    p.playSound(p, Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);

                    new BukkitRunnable() {

                        int seconds = 8;

                        @Override
                        public void run() {
                            if (arena.getState() != ArenaState.PLAYING) {
                                p.removePotionEffect(PotionEffectType.INVISIBILITY);
                                cancel();
                                return;
                            }

                            if (seconds == 0) {
                                cancel();
                                return;
                            }

                            double randomX = random.nextBoolean() ? random.nextInt(3) : -random.nextInt(3);
                            double randomY = random.nextBoolean() ? random.nextInt(3) : -random.nextInt(3);
                            double randomZ = random.nextBoolean() ? random.nextInt(3) : -random.nextInt(3);

                            Location randomLocation = Bukkit.getPlayer(game.getSecurity()).getLocation()
                                    .add(randomX, randomY, randomZ);

                            Bukkit.getPlayer(game.getSecurity()).playSound(randomLocation,
                                    possibleSounds.get(random.nextInt(possibleSounds.size())), 10.0f, 1.0f);

                            seconds--;
                        }

                    }.runTaskTimer(main, 0L, 20L);
                    break;

                case "an_wardenRageAbility":
                    if (game.isOnItemCooldown(uuid, itemInHand.getItemMeta().getLocalizedName())) {
                        p.sendMessage(main.prefix + main.getFileUtils().getString("items.game.item-on-cooldown"));
                        e.setCancelled(true);
                        return;
                    }

                    game.setItemCooldown(uuid, itemInHand.getItemMeta().getLocalizedName(),
                            main.getFileUtils().getWardenAbilityCooldown("rage"));

                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6 * 20, 1, false, false));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 3 * 20, 0, false, false));
                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_ANGRY, 10.0f, 1.0f);
                    break;

                case "an_wardenCompass":
                    p.setCompassTarget(arena.getSecuritySpawn());
                    p.sendMessage(main.prefix + main.getFileUtils().getString("items.game.warden.compass.use"));
                    e.setCancelled(true);
                    break;
            }

        } else if (uuid.equals(game.getSecurity())) {
            if (!e.hasBlock())
                return;

            Block block = e.getClickedBlock();
            Material type = block.getType();

            if (type == Material.IRON_DOOR && game.getBattery() > 0) {
                Door door = (Door) block.getBlockData();
                if (door.isOpen()) {
                    game.setClosedDoors(game.getClosedDoors() + 1);
                    door.setOpen(false);
                    p.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_DOOR_CLOSE, 1.0f,
                            1.0f);
                } else {
                    game.setClosedDoors(game.getClosedDoors() - 1);
                    door.setOpen(true);
                    p.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_DOOR_OPEN, 1.0f,
                            1.0f);
                }

                block.setBlockData(door);
            } else if (type == Material.IRON_TRAPDOOR && game.getBattery() > 0) {
                TrapDoor trapdoor = (TrapDoor) block.getBlockData();
                if (trapdoor.isOpen()) {
                    game.setClosedDoors(game.getClosedDoors() + 1);
                    trapdoor.setOpen(false);
                    p.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 1.0f,
                            1.0f);
                } else {
                    game.setClosedDoors(game.getClosedDoors() - 1);
                    trapdoor.setOpen(true);
                    p.getWorld().playSound(block.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1.0f,
                            1.0f);
                }

                block.setBlockData(trapdoor);
            } else if (type == GameBlock.BATTERY_SUPPLY.getType()) {
                if (block.getLocation().equals(arena.getBatterySupplyLocation())) {
                    e.setCancelled(true);

                    ItemStack batteryItem =
                            main.getItemUtils().getSecurityAbilityItem(SpecialAbilityItem.SECURITY_BATTERY);

                    if (p.getInventory().contains(batteryItem))
                        return;

                    p.getInventory().addItem(batteryItem);

                    Bukkit.getPlayer(game.getWarden()).spawnParticle(Particle.VIBRATION, p.getLocation(), 1,
                            new Vibration(p.getLocation(),
                                    new BlockDestination(p.getLocation().clone().add(0, 1.8, 0)), 50));
                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 5.0f, 1.0f);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 5 * 20, 0, false, false));
                }
            } else if (type == GameBlock.GENERATOR.getType()) {
                if (block.getLocation().equals(arena.getGeneratorLocation())) {
                    e.setCancelled(true);

                    if (!hasItemInHand
                            || !itemInHand.getItemMeta().getLocalizedName().equals("an_securityBattery")) {
                        p.sendMessage(main.prefix + main.getFileUtils().getString("arena.game.generator-no-battery"));
                        return;
                    }

                    if (game.getBattery() == 100) {
                        p.sendMessage(main.prefix + main.getFileUtils().getString("arena.game.generator-full"));
                        return;
                    }

                    Bukkit.getPlayer(game.getWarden()).spawnParticle(Particle.VIBRATION, p.getLocation(), 1,
                            new Vibration(p.getLocation(),
                                    new BlockDestination(p.getLocation().clone().add(0, 1.8, 0)), 50));
                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 5.0f, 1.0f);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 5 * 20, 0, false, false));

                    p.getInventory().remove(itemInHand);

                    int batteryRecharge = arena.getDoors().size()
                            * main.getFileUtils().getGeneratorRechargePerDoor();

                    if (game.getBattery() + batteryRecharge > 100) {
                        game.setNextBattery(100);
                    } else {
                        game.setNextBattery(game.getBattery() + batteryRecharge);
                    }
                }
            } else if (!(block.getBlockData() instanceof Door)) {
                if (!(block.getBlockData() instanceof Bed)) {
                    if (itemInHand != null && itemInHand.getType().isEdible()) {
                        return;
                    } else if (itemInHand != null && itemInHand.getItemMeta() != null
                            && itemInHand.getItemMeta().getLocalizedName().equals("an_securityFakeSound")) {
                        return;
                    }
                }

                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player))
            return;

        Player p = (Player) e.getEntity().getShooter();

        if (main.getArenaManager().getArena(p) == null)
            return;

        Arena arena = main.getArenaManager().getArena(p);
        Game game = arena.getGame();

        if (arena.getState() != ArenaState.PLAYING)
            return;

        if (p.getUniqueId().equals(game.getSecurity())) {
            if (e.getEntity() instanceof Egg) {
                Egg egg = (Egg) e.getEntity();
                ItemStack item = egg.getItem();

                if (item.getItemMeta().getLocalizedName().equals("an_securityFakeSound")) {
                    if (game.isOnItemCooldown(p.getUniqueId(), item.getItemMeta().getLocalizedName())) {
                        p.sendMessage(main.prefix +
                                main.getFileUtils().getString("items.game.item-on-cooldown"));
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEggThrow(PlayerEggThrowEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getEgg().getItem();

        if (main.getArenaManager().getArena(p) == null)
            return;

        Arena arena = main.getArenaManager().getArena(p);
        Game game = arena.getGame();

        if (arena.getState() != ArenaState.PLAYING)
            return;

        if (p.getUniqueId().equals(game.getSecurity())) {
            if (item.getItemMeta().getLocalizedName().equals("an_securityFakeSound")) {
                e.setHatching(false);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        ProjectileSource shooter = e.getEntity().getShooter();

        if (!(shooter instanceof Player) || !e.getEntityType().equals(EntityType.EGG))
            return;

        Player p = (Player) shooter;

        if (main.getArenaManager().getArena(p) == null)
            return;

        Arena arena = main.getArenaManager().getArena(p);
        Game game = arena.getGame();

        if (arena.getState() != ArenaState.PLAYING)
            return;

        if (!p.getUniqueId().equals(game.getSecurity()))
            return;

        e.getEntity().remove();
        e.setCancelled(true);

        p.getInventory().addItem(main.getItemUtils().getSecurityAbilityItem(SpecialAbilityItem.SECURITY_FAKE_SOUND));
        game.setItemCooldown(p.getUniqueId(),
                main.getItemUtils().getSecurityAbilityItem(SpecialAbilityItem.SECURITY_FAKE_SOUND)
                        .getItemMeta().getLocalizedName(),
                main.getFileUtils().getSecurityAbilityCooldown("fake-sound"));

        Villager villager = (Villager) e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation(),
                EntityType.VILLAGER);
        villager.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10, 0, false, false));

        Bukkit.getPlayer(game.getWarden()).spawnParticle(Particle.VIBRATION, villager.getLocation(), 1,
                new Vibration(villager.getLocation(),
                        new BlockDestination(villager.getLocation().clone().add(0, 1.8, 0)), 50));
        p.getWorld().playSound(villager.getLocation(), Sound.BLOCK_SCULK_SENSOR_STEP, 5.0f, 1.0f);

        new FakeSound(main, arena, villager.getUniqueId()).start();
    }

    @EventHandler
    public void onPoseChange(EntityPoseChangeEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;

        Player p = (Player) e.getEntity();

        if (main.getArenaManager().getArena(p) == null)
            return;

        Arena arena = main.getArenaManager().getArena(p);
        Game game = arena.getGame();

        if (arena.getState() != ArenaState.PLAYING)
            return;

        if (!p.getUniqueId().equals(game.getWarden()))
            return;

        if (e.getPose() == Pose.SWIMMING) {
            p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(main.getFileUtils().getWardenVentSpeed());
        } else if (e.getPose() == Pose.STANDING) {
            p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1);
        }

    }

    @EventHandler
    public void onArmorStandManipulateEvent(PlayerArmorStandManipulateEvent e) {
        Player p = e.getPlayer();

        if (main.getArenaManager().getArena(p) == null)
            return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player) {
            Player p = (Player) e.getRemover();

            if (main.getArenaManager().getArena(p) == null)
                return;

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();

        if (main.getArenaManager().getArena(p) == null)
            return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!main.getFileUtils().disableMobSpawn())
            return;

        if (!e.getLocation().getWorld().getGameRuleValue(GameRule.DO_MOB_SPAWNING))
            return;

        if ((e.getEntityType() == EntityType.VILLAGER && e.getEntityType() != EntityType.WANDERING_TRADER)
                || e.getEntityType() == EntityType.EGG || e.getEntityType() == EntityType.PAINTING
                || e.getEntityType() == EntityType.ARMOR_STAND)
            return;

        for (Arena arena : main.getArenaManager().getArenas()) {
            if (!arena.getArenaRegion().isIn(e.getLocation())) {
                continue;
            }

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();

        if (main.getArenaManager().getArena(p) == null)
            return;

        Arena arena = main.getArenaManager().getArena(p);

        if (arena.getState() != ArenaState.PLAYING)
            return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();

        if (main.getArenaManager().getArena(p) == null)
            return;

        if (p.getGameMode() != GameMode.SPECTATOR)
            return;

        if (e.getCause() != PlayerTeleportEvent.TeleportCause.SPECTATE)
            return;

        e.setCancelled(true);
    }

    /*
     * UTILS
     */

    // By: eccentric
    public boolean isCube(Block block) {
        VoxelShape voxelShape = block.getCollisionShape();
        BoundingBox boundingBox = block.getBoundingBox();
        return (voxelShape.getBoundingBoxes().size() == 1 && boundingBox.getWidthX() == 1.0
                && boundingBox.getHeight() == 1.0 && boundingBox.getWidthZ() == 1.0);
    }

}