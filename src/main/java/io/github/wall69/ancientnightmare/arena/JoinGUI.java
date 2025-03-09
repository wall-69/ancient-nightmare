package io.github.wall69.ancientnightmare.arena;

import io.github.wall69.ancientnightmare.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class JoinGUI {

    /*
     * EVERYTHING IS FROM Stephen King's UDEMY COURSE (IT'S LECTURE 72., IF ANYONE IS INTERESTED)
     */

    public JoinGUI(Main main, UUID player, int page){
        Player p = Bukkit.getPlayer(player);
        Inventory gui = Bukkit.createInventory(null, 54, main.getFileUtils().getString("join-gui.title"));

        List<ItemStack> items = new ArrayList<>();

        for (Arena arena : main.getArenaManager().getArenas()) {
            items.add(main.getItemUtils().getGUIJoinItem(arena));
        }

        ItemStack left = new ItemStack(Material.BARRIER), right = new ItemStack(Material.BARRIER);

        if(isPageValid(items, page - 1, 52)){
            left = main.getItemUtils().getGUINextItem("left", page - 1);
        } else {
            ItemMeta leftMeta = left.getItemMeta();
            leftMeta.setDisplayName("");
            leftMeta.setLore(Arrays.asList(""));
            left.setItemMeta(leftMeta);
        }

        if(isPageValid(items, page + 1, 52)){
            right = main.getItemUtils().getGUINextItem("right", page + 1);
        } else {
            ItemMeta rightMeta = right.getItemMeta();
            rightMeta.setDisplayName("");
            rightMeta.setLore(Arrays.asList(""));
            right.setItemMeta(rightMeta);
        }

        gui.setItem(0, left);
        gui.setItem(8, right);

        for(ItemStack itemStack : getPageItems(items, page, 52)){
            gui.setItem(gui.firstEmpty(), itemStack);
        }

        p.openInventory(gui);
    }

    private List<ItemStack> getPageItems(List<ItemStack> items, int page, int spaces){
        int upperBound = page * spaces;
        int lowerBound = upperBound - spaces;

        List<ItemStack> newItems = new ArrayList<>();
        for(int i = lowerBound; i < upperBound; i++){
            try{
                newItems.add(items.get(i));
            } catch(IndexOutOfBoundsException e){
                break;
            }
        }

        return newItems;
    }

    private boolean isPageValid(List<ItemStack> items, int page, int spaces){
        if(page <= 0)
            return false;

        int upperBound = page * spaces;
        int lowerBound = upperBound - spaces;

        return items.size() > lowerBound;
    }


}
