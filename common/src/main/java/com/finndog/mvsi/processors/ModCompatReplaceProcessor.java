package com.finndog.mvsi.processors;

import com.finndog.moogs_structures.utils.GeneralUtils;
import com.finndog.mvsi.modinit.MVSIProcessors;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

/**
 * Swaps a block to a modded counterpart only if that counterpart is present in the
 * live block registry; otherwise the original (vanilla fallback) block is kept.
 *
 * <p>Each replacement entry is {@code {from, to, if_registered}}: when a placed block
 * matches {@code from}'s block AND {@code if_registered} resolves in the block registry,
 * the block becomes {@code to} (copying over any matching blockstate properties from the
 * original). Entries are evaluated in order; the first match wins. When the probed block
 * is absent the original block info is returned untouched, so structures degrade
 * gracefully on installs without the integrated mod.
 */
public class ModCompatReplaceProcessor extends StructureProcessor {

    public record Entry(BlockState from, BlockState to, ResourceLocation ifRegistered) {
        public static final MapCodec<Entry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockState.CODEC.fieldOf("from").forGetter(Entry::from),
                BlockState.CODEC.fieldOf("to").forGetter(Entry::to),
                ResourceLocation.CODEC.fieldOf("if_registered").forGetter(Entry::ifRegistered)
        ).apply(instance, Entry::new));
    }

    public static final MapCodec<ModCompatReplaceProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Entry.CODEC.codec().listOf().fieldOf("replacements").forGetter(config -> config.replacements)
    ).apply(instance, ModCompatReplaceProcessor::new));

    private final List<Entry> replacements;

    public ModCompatReplaceProcessor(List<Entry> replacements) {
        this.replacements = replacements;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader worldReader, BlockPos pos, BlockPos pos2, StructureTemplate.StructureBlockInfo infoIn1, StructureTemplate.StructureBlockInfo infoIn2, StructurePlaceSettings settings) {
        for (Entry entry : replacements) {
            if (infoIn2.state().is(entry.from().getBlock()) && BuiltInRegistries.BLOCK.containsKey(entry.ifRegistered())) {
                BlockState newBlockState = GeneralUtils.copyBlockProperties(infoIn2.state(), entry.to());
                return new StructureTemplate.StructureBlockInfo(infoIn2.pos(), newBlockState, infoIn2.nbt());
            }
        }
        return infoIn2;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return MVSIProcessors.MOD_COMPAT_REPLACE.get();
    }
}
