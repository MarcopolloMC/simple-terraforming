package com.marcopolo.terraformmod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.HoeItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class TerraformHandler {
    private static boolean terraformingActive = false;
    private static int rangeX = 5;
    private static int rangeY = 5;
    private static int rangeZ = 5;
    private static final int MAX_RANGE = 6;
    private static final Set<Block> blocksToMine = new HashSet<>();
    private static int tickCounter = 0;
    private static final int TICK_DELAY = 1; // Jeden Tick
    private static final List<BlockPos> blocksToBreak = new ArrayList<>();

    static {
        addDefaultBlocks();
    }

    private static void addDefaultBlocks() {
        blocksToMine.clear();
        blocksToMine.add(Blocks.DIRT);
        blocksToMine.add(Blocks.GRASS_BLOCK);
        blocksToMine.add(Blocks.COARSE_DIRT);
        blocksToMine.add(Blocks.ROOTED_DIRT);
        blocksToMine.add(Blocks.STONE);
        blocksToMine.add(Blocks.GRANITE);
        blocksToMine.add(Blocks.DIORITE);
        blocksToMine.add(Blocks.ANDESITE);
        blocksToMine.add(Blocks.DEEPSLATE);
        blocksToMine.add(Blocks.SAND);
        blocksToMine.add(Blocks.RED_SAND);
        blocksToMine.add(Blocks.GRAVEL);
    }

    public static void mineTerrain(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        BlockPos playerPos = player.getBlockPos();
        World world = client.world;

        // Sammle alle Blöcke zum Abbau
        List<BlockPos> blocksToBreakThisTick = new ArrayList<>();

        for (int x = -rangeX; x <= rangeX; x++) {
            for (int y = -rangeY; y <= rangeY; y++) {
                for (int z = -rangeZ; z <= rangeZ; z++) {
                    BlockPos blockPos = playerPos.add(x, y, z);
                    Block block = world.getBlockState(blockPos).getBlock();

                    // Überspringe wenn Block nicht in Whitelist
                    if (!blocksToMine.contains(block)) continue;

                    // Überspringe wenn kein geeignetes Werkzeug
                    if (!hasProperTool(player, block)) continue;

                    blocksToBreakThisTick.add(blockPos);
                }
            }
        }

        // Baue max 5 Blöcke pro Tick ab (Performance)
        for (int i = 0; i < Math.min(5, blocksToBreakThisTick.size()); i++) {
            mineBlock(client, blocksToBreakThisTick.get(i), player);
        }
    }

    private static boolean hasProperTool(PlayerEntity player, Block block) {
        ItemStack mainHand = player.getMainHandStack();

        // Wenn kein Item im MainHand, ignoriere Block
        if (mainHand.isEmpty()) {
            return false;
        }

        // Prüfe auf Haltbarkeit (nicht 5 oder weniger)
        if (mainHand.isDamaged()) {
            int maxDurability = mainHand.getMaxDamage();
            int currentDurability = maxDurability - mainHand.getDamage();
            if (currentDurability <= 5) {
                return false;
            }
        }

        // Prüfe ob Block Silk Touch braucht
        if (needsSilkTouch(block)) {
            return mainHand.enchantments.getLevel(Enchantments.SILK_TOUCH) > 0;
        }

        // Prüfe ob Werkzeug zum Block passt
        return isCorrectTool(mainHand, block);
    }

    private static boolean isCorrectTool(ItemStack tool, Block block) {
        // Stein/Erz braucht Spitzhacke
        if (block == Blocks.STONE || block == Blocks.GRANITE || 
            block == Blocks.DIORITE || block == Blocks.ANDESITE ||
            block == Blocks.DEEPSLATE) {
            return tool.getItem() instanceof PickaxeItem;
        }

        // Erde/Gras braucht Schaufel
        if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || 
            block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT) {
            return tool.getItem() instanceof ShovelItem;
        }

        // Sand/Kies braucht Schaufel
        if (block == Blocks.SAND || block == Blocks.RED_SAND || block == Blocks.GRAVEL) {
            return tool.getItem() instanceof ShovelItem;
        }

        return false;
    }

    private static boolean needsSilkTouch(Block block) {
        return block == Blocks.GLASS || block == Blocks.TINTED_GLASS ||
               block == Blocks.MELON || block == Blocks.PUMPKIN ||
               block == Blocks.BOOKSHELF;
    }

    private static void mineBlock(MinecraftClient client, BlockPos blockPos, ClientPlayerEntity player) {
        if (client.interactionManager != null) {
            client.interactionManager.attackBlock(blockPos, Direction.UP);
        }
    }

    public static void toggleTerraforming() {
        terraformingActive = !terraformingActive;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            String message = terraformingActive ? 
                "§aTerraforming aktiviert (T zum Ausschalten)" : 
                "§cTerraforming deaktiviert";
            client.player.sendMessage(client.player.getTextStream().toText(message), true);
        }
    }

    public static void setRange(int x, int y, int z) {
        rangeX = Math.min(Math.max(x, 1), MAX_RANGE);
        rangeY = Math.min(Math.max(y, 1), MAX_RANGE);
        rangeZ = Math.min(Math.max(z, 1), MAX_RANGE);
    }

    public static int getRangeX() { return rangeX; }
    public static int getRangeY() { return rangeY; }
    public static int getRangeZ() { return rangeZ; }
    public static int getMaxRange() { return MAX_RANGE; }
    public static boolean isTerraformingActive() { return terraformingActive; }
    public static Set<Block> getBlockWhitelist() { return new HashSet<>(blocksToMine); }

    public static void addBlockToWhitelist(Block block) {
        blocksToMine.add(block);
    }

    public static void removeBlockFromWhitelist(Block block) {
        blocksToMine.remove(block);
    }

    public static void clearWhitelist() {
        blocksToMine.clear();
    }

    public static void resetToDefault() {
        addDefaultBlocks();
    }
}
