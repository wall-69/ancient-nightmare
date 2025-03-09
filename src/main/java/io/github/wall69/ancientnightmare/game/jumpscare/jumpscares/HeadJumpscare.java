package io.github.wall69.ancientnightmare.game.jumpscare.jumpscares;

import io.github.wall69.ancientnightmare.Main;
import io.github.wall69.ancientnightmare.game.jumpscare.Jumpscare;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class HeadJumpscare extends Jumpscare {

    private final List<String> headTextures;
    private final List<Color> darkColors;

    public HeadJumpscare() {
        this.headTextures = new ArrayList<>();
        this.darkColors = new ArrayList<>();

        headTextures.add("3f6e859435bbcc146c4e217d389cc3ce4063d615ab0417b337216f1137a24494"); // Warden herobrine
        headTextures.add("8a036ce937faacb1720d81810d00ca2b8f331790967f334114f01bd45a9d943b"); // Warden priestess
        headTextures.add("fdea1115a531748880ec22af7dde01cb87415a37b28948f8d6979c5ada83b56e"); // Warden steve
        headTextures.add("3959ae02e35cbc4743e6e702fda6980bad2e98049403c16996286eb412f94689"); // Warden

        darkColors.add(Color.BLACK);
        darkColors.add(Color.fromRGB(3, 52, 46));
        darkColors.add(Color.fromRGB(3, 33, 28));
        darkColors.add(Color.fromRGB(17, 68, 58));
        darkColors.add(Color.fromRGB(39, 1, 0));
        darkColors.add(Color.fromRGB(17, 1, 0));
    }

    // TODO: particle za hlavou
    @Override
    protected void jumpscare(Main main, Player player, BukkitRunnable callback) {
        final int distance = 3;

        Location front = player.getLocation().add(player.getLocation().getDirection().multiply(distance));

        ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(front, EntityType.ARMOR_STAND);
        armorStand.setInvisible(true);
        armorStand.setBasePlate(false);
        armorStand.setGravity(false);
        armorStand.setVisible(true);
        armorStand.setInvulnerable(true);
        spawnParticles(armorStand);

        // Head
        ItemStack head = getHead(headTextures.get(new Random().nextInt(headTextures.size())));
        armorStand.getEquipment().setHelmet(head, true);

        // Rotation
        armorStand.setHeadPose(new EulerAngle(-Math.toRadians(player.getEyeLocation().getPitch()),
                -Math.toRadians(player.getEyeLocation().getYaw() + player.getEyeLocation().getYaw() > 0 ? -180 : 180), 0));

        // Add effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2 * 20, 255));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2 * 20, 0));

        // "Scare" sound
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_HURT, 1.0f, 0.4f);

        // Move towards player
        Vector dirTowardsPlayer = player.getLocation().toVector().subtract(armorStand.getLocation().toVector()).normalize();
        final int maxTicks = distance * 4;

        new BukkitRunnable() {

            int i = 0;

            @Override
            public void run() {
                if (i++ >= maxTicks) {
                    callback.runTaskLater(main, 20L);
                    armorStand.remove();
                    cancel();
                    return;
                } else if (i == 1) {
                    armorStand.setVisible(true);
                }

                armorStand.teleport(armorStand.getLocation().add(dirTowardsPlayer));
                spawnParticles(armorStand);
            }
        }.runTaskTimer(main, 10L, 1L);
    }

    private void spawnParticles(ArmorStand armorStand) {
        armorStand.getWorld().spawnParticle(
                Particle.REDSTONE,
                armorStand.getEyeLocation(),
                25 + random.nextInt(25),
                random.nextFloat() - random.nextFloat(),
                random.nextFloat() - random.nextFloat(),
                random.nextFloat() - random.nextFloat(),
                1,
                new Particle.DustOptions(darkColors.get(random.nextInt(darkColors.size())), 1f)
        );
        armorStand.getWorld().spawnParticle(
                Particle.ASH,
                armorStand.getEyeLocation(),
                25 + random.nextInt(15),
                random.nextFloat() - random.nextFloat(),
                random.nextFloat() - random.nextFloat(),
                random.nextFloat() - random.nextFloat()
        );
    }

    private ItemStack getHead(String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        PlayerProfile profile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(new URL("http://textures.minecraft.net/texture/" + texture));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        profile.setTextures(textures);
        meta.setOwnerProfile(profile);
        head.setItemMeta(meta);
        return head;
    }
}