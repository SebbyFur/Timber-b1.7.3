package event;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.stream.IntStream;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import timber.Timber;

import org.bukkit.Location;
import org.bukkit.Material;


public class TimberEvent implements Listener {
    private Timber plugin;

    private static EnumSet<Material> axes = EnumSet.of(
        Material.WOOD_AXE,
        Material.STONE_AXE,
        Material.IRON_AXE,
        Material.GOLD_AXE,
        Material.DIAMOND_AXE
    );

    private static EnumSet<Material> validReplantBlocks = EnumSet.of(
        Material.DIRT,
        Material.GRASS
    );

    private static double[][] searchLocation = {
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

    public TimberEvent(Timber plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = Priority.Monitor)
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        ItemStack tool = player.getInventory().getItemInHand();
        Block block = e.getBlock();
        Material material = block.getType();
        Byte metaByte = block.getData();

        if (player.isSneaking() && axes.contains(tool.getType()) && material == Material.LOG) {
            Location location = block.getLocation();
            HashSet<Block> blocks = this.getTreeBlocks(location);

            if (blocks != null && this.removeDurability(tool, blocks.size())) {
                ItemStack dropped = new ItemStack(material, blocks.size(), (short)0, metaByte);
                
                blocks.forEach((b) -> b.setType(Material.AIR)); // remove tree
                location.getWorld().dropItem(location, dropped); // drop items

                e.setCancelled(true); // cancel current event

                if (plugin.getConfig().getAutoReplant()) plantSapling(player, location, metaByte); // replant if enabled
            }
        }
    }

    private HashSet<Block> getTreeBlocks(Location location) {
        HashSet<Block> treeBlocks = new HashSet<Block>();
        Byte dataByte = location.getBlock().getData(); // For metadata matching (Oak != Spruce != Birch...)

        this.recursiveLogSearch(treeBlocks, location, dataByte);

        return treeBlocks.size() > plugin.getConfig().getMaxLogBlocks() ? null : treeBlocks;
    }

    private void recursiveLogSearch(HashSet<Block> blocks, Location location, Byte metaByte) {
        Block block = location.getBlock();
        blocks.add(block);

        for (double[] position : searchLocation) {
            Location nextLocation = location.clone().add(position[0], position[1], position[2]);
            Block nextBlock = nextLocation.getBlock();

            if (!blocks.contains(nextBlock) && nextBlock.getType() == Material.LOG && metaByte == nextBlock.getData() && blocks.size() <= plugin.getConfig().getMaxLogBlocks()) {
                this.recursiveLogSearch(blocks, nextLocation, metaByte);
            }
        }
    }

    private boolean removeDurability(ItemStack item, int minus) {
        short maxDurability = item.getType().getMaxDurability();
        short durability = item.getDurability();

        if ((short)(durability + minus - 1) > maxDurability)
            return false;

        item.setDurability((short)(durability + minus - 1));

        return true;
    }

    private void plantSapling(Player player, Location location, Byte metaByte) {
        ItemStack[] inv = player.getInventory().getContents();

        if (validReplantBlocks.contains(location.clone().add(0.0D, -1.0D, 0.0D).getBlock().getType())) {
            int foundIndex = IntStream.range(0, inv.length)
            .filter(i -> inv[i] != null && inv[i].getType() == Material.SAPLING && inv[i].getData().getData() == metaByte)
            .findFirst()
            .orElse(-1);

            if (foundIndex != -1) {
                if (inv[foundIndex].getAmount() == 1) {
                    player.getInventory().setItem(foundIndex, null);
                } else {
                    inv[foundIndex].setAmount(inv[foundIndex].getAmount() - 1);
                }
                location.getBlock().setType(Material.SAPLING);
                location.getBlock().setData(metaByte);
            }
        }
    }
}
