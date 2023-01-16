import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class Main extends JavaPlugin implements Listener {
    private static HashSet<Material> axes = new HashSet<Material>(Arrays.asList(
        Material.WOOD_AXE,
        Material.STONE_AXE,
        Material.IRON_AXE,
        Material.GOLD_AXE,
        Material.DIAMOND_AXE
    ));

    private static int treeMaxBlocks = 50; // Should be exclusive (tree has to be <= 25 blocks here)

    private static double[][] searchPos = {
        {1.0D, 0.0D, 0.0D},
        {0.0D, 0.0D, 1.0D},
        {-1.0D, 0.0D, 0.0D},
        {0.0D, 0.0D, -1.0D},
        {1.0D, 0.0D, 1.0D},
        {-1.0D, 0.0D, -1.0D},
        {1.0D, 0.0D, -1.0D},
        {-1.0D, 0.0D, 1.0D},
        {1.0D, 1.0D, 0.0D},
        {0.0D, 1.0D, 1.0D},
        {-1.0D, 1.0D, 0.0D},
        {0.0D, 1.0D, -1.0D},
        {1.0D, 1.0D, 1.0D},
        {-1.0D, 1.0D, -1.0D},
        {1.0D, 1.0D, -1.0D},
        {-1.0D, 1.0D, 1.0D},
        {0.0D, 1.0D, 0.0D},
    };

    @Override
    public void onEnable() {
        Logger.getLogger(Main.class.getName()).log(Level.INFO, "[Timber] Timber started !");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        
    }

    @EventHandler(priority = Priority.Monitor)
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        ItemStack tool = player.getInventory().getItemInHand();
        Block block = e.getBlock();
        Material material = block.getType();

        if (player.isSneaking() && axes.contains(tool.getType()) && material == Material.LOG) {
            HashSet<Block> blocks = this.getTreeBlocks(block.getLocation());

            if (blocks != null && !blocks.isEmpty() && this.applyDurability(tool, blocks.size())) {
                ItemStack dropped = new ItemStack(material, blocks.size(), (short)0, block.getData());
                
                this.cutDownLog(blocks);
                this.dropLog(block.getLocation(), dropped);
            }
        }
    }

    private HashSet<Block> getTreeBlocks(Location location) {
        HashSet<Block> blocks = new HashSet<>();
        Byte metaByte = location.getBlock().getData(); // For metadata matching (Oak != Spruce != Birch...)
        int[] count = new int[1]; // Using array as a wrapper... IDK not a java expert

        this.recursiveLogSearch(blocks, location, metaByte, count);

        return count[0] == treeMaxBlocks ? null : blocks;
    }

    private void recursiveLogSearch(HashSet<Block> blocks, Location location, Byte metaByte, int[] n) {
        Block block = location.getBlock();
        blocks.add(block);
        n[0]++;

        for (double[] position : searchPos) {
            Location clone = location.clone();
            clone.add(position[0], position[1], position[2]);
            Block nextBlock = clone.getBlock();

            if (!blocks.contains(nextBlock) && nextBlock.getType() == Material.LOG && metaByte == nextBlock.getData() && n[0] < treeMaxBlocks) {
                this.recursiveLogSearch(blocks, clone, metaByte, n);
            }
        }
    }

    private void cutDownLog(HashSet<Block> blocks) {
		for (Block block : blocks) {
            block.setType(Material.AIR);
        }
	}

    private boolean applyDurability(ItemStack item, int minus) {
        short maxDurability = item.getType().getMaxDurability();
        short durability = item.getDurability();

        if ((short)(durability + minus - 1) > maxDurability)
            return false;

        item.setDurability((short)(durability + minus - 1));

        return true;
    }

    private void dropLog(Location location, ItemStack item) {
        World world = location.getWorld();
        world.dropItem(location, item);
    }
}