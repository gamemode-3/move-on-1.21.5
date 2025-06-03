package net.gamemode3.moveon.block;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.gamemode3.moveon.MoveOn;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block LIGHTLY_POWERED_RAIL = registerBlock("lightly_powered_rail", new LightlyPoweredRailBlock(newBlockSettings("lightly_powered_rail").noCollision().strength(0.7F).sounds(BlockSoundGroup.METAL)));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(MoveOn.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(MoveOn.MOD_ID, name), new BlockItem(block, newItemSettings(name)));
    }

    public static Item.Settings newItemSettings(String name) {
        return new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MoveOn.MOD_ID, name)));
    }

    public static AbstractBlock.Settings newBlockSettings(String name) {
        return AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MoveOn.MOD_ID, name)));
    }

    public static void registerModBlocks() {
        MoveOn.LOGGER.info("Registering Mod Blocks for " + MoveOn.MOD_ID);

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.LIGHTLY_POWERED_RAIL, RenderLayer.getCutout());

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.add(LIGHTLY_POWERED_RAIL);
        });
    }
}
