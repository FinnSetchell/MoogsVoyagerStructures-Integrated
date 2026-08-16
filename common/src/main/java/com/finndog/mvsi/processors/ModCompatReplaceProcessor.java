package com.finndog.mvsi.processors;

import com.finndog.moogs_structures.utils.GeneralUtils;
import com.finndog.mvsi.modinit.MVSIProcessors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Swaps a block to a modded counterpart only if that counterpart is present in the
 * live block registry; otherwise the original (vanilla fallback) block is kept.
 *
 * <p>Each entry is {@code {from, to, if_registered?}}: when a placed block matches
 * {@code from} AND the probed id ({@code if_registered}, defaulting to {@code to}) is
 * registered, the block becomes {@code to}, carrying over any matching blockstate
 * properties from the original.
 *
 * <p>{@code to} is stored as a plain block id and resolved only at placement time, so a
 * processor list that references an absent mod's block still loads cleanly and simply
 * leaves the original block in place — structures degrade gracefully without the
 * integrated mod. {@code from} is a normal (always-present) block, so it decodes directly.
 */
public class ModCompatReplaceProcessor extends StructureProcessor {

    public record Entry(Block from, ResourceLocation to, Optional<ResourceLocation> ifRegistered) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("from").forGetter(Entry::from),
                ResourceLocation.CODEC.fieldOf("to").forGetter(Entry::to),
                ResourceLocation.CODEC.optionalFieldOf("if_registered").forGetter(Entry::ifRegistered)
        ).apply(instance, Entry::new));

        /** The id whose presence in the block registry gates the swap (defaults to {@code to}). */
        public ResourceLocation probe() {
            return ifRegistered.orElse(to);
        }
    }

    public static final Codec<ModCompatReplaceProcessor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().fieldOf("replacements").forGetter(config -> config.replacements)
    ).apply(instance, ModCompatReplaceProcessor::new));

    private final List<Entry> replacements;

    public ModCompatReplaceProcessor(List<Entry> replacements) {
        this.replacements = replacements;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldReader, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo infoIn1, StructureTemplate.StructureBlockInfo infoIn2, StructurePlaceSettings settings) {
        for (Entry entry : replacements) {
            if (infoIn2.state().is(entry.from()) && BuiltInRegistries.BLOCK.containsKey(entry.probe())) {
                BlockState replacement = BuiltInRegistries.BLOCK.get(entry.to()).defaultBlockState();
                BlockState newBlockState = GeneralUtils.copyBlockProperties(infoIn2.state(), replacement);
                return new StructureTemplate.StructureBlockInfo(infoIn2.pos(), newBlockState, infoIn2.nbt());
            }
        }
        return infoIn2;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return MVSIProcessors.MOD_COMPAT_REPLACE;
    }
}
