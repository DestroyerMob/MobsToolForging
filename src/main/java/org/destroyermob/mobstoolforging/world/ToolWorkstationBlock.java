package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.item.ToolTemplateItem;
import org.destroyermob.mobstoolforging.network.ModNetworking;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public abstract class ToolWorkstationBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private final WorkstationKind kind;

    protected ToolWorkstationBlock(BlockBehaviour.Properties properties, WorkstationKind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected abstract MapCodec<? extends BaseEntityBlock> codec();

    public WorkstationKind kind() {
        return kind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ToolForgeBlockEntity(pos, state);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && level.getBlockEntity(pos) instanceof ToolForgeBlockEntity forge) {
            forge.reset();
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof ToolForgeBlockEntity forge)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (SmithingHammerLevel.isHammer(stack) && PatternRackSelection.hasPendingRackLink(player, level)) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            if (player instanceof ServerPlayer serverPlayer
                    && PatternRackSelection.linkPendingRackToStation(serverPlayer, level, pos, kind, forge)) {
                return ItemInteractionResult.CONSUME;
            }
        }
        if (player.isShiftKeyDown()) {
            return itemResult(handleSneakUse(forge, level, pos, player, kind), level);
        }
        if (tryCollectOutput(forge, player)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!(stack.getItem() instanceof ToolTemplateItem) && !isRepairStack(stack, forge, kind) && handleMissingPattern(forge, level, player)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (EmptyMainHandInteractions.shouldFallbackToEmptyHand(player, hand) && !canHandleItem(stack, forge, level)) {
            return EmptyMainHandInteractions.itemResult(useWithoutItem(state, level, pos, player, hitResult), level);
        }
        if (kind == WorkstationKind.TOOLMAKERS_BENCH) {
            return useToolmakersBench(stack, forge, level, pos, player);
        }
        if (isRepairStation(kind)) {
            ItemInteractionResult repairResult = useSmithingAnvilRepair(stack, forge, level, pos, player);
            if (repairResult != ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION) {
                return repairResult;
            }
        }
        if (stack.getItem() instanceof ToolTemplateItem templateItem) {
            if (kind == WorkstationKind.LAPIDARY_TABLE) {
                if (!level.isClientSide) {
                    DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.lapidary_use_heated_part"));
                }
                return ItemInteractionResult.CONSUME;
            }
            if (!MobsToolForgingConfig.ALLOW_DIRECT_PATTERN_STATION_SELECTION.get()) {
                if (!level.isClientSide) {
                    DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.pattern_rack_select"));
                }
                return ItemInteractionResult.CONSUME;
            }
            return applyTemplateItem(stack, templateItem, forge, level, pos, player);
        }
        if (kind == WorkstationKind.LAPIDARY_TABLE && ToolForgeBlockEntity.isLapidaryCoatablePart(stack)) {
            return placeLapidaryBasePart(stack, forge, level, pos, player);
        }
        if (ArmorForgeAttachment.isAttachmentStation(kind) && ArmorForgeAttachment.isAttachmentTemplate(forge.templateId())) {
            if (forge.canPlaceArmorAttachmentTarget(stack)) {
                return placeArmorAttachmentTarget(stack, forge, level, pos, player);
            }
            if (ArmorForgeAttachment.isArmorStack(stack)) {
                if (!level.isClientSide) {
                    DebugFeedback.actionBar(player, Component.translatable(forge.hasPlacedWork() ? "message.mobstoolforging.forge_busy" : "message.mobstoolforging.armor_attachment_wrong_target"));
                }
                return ItemInteractionResult.CONSUME;
            }
        }
        if (kind == WorkstationKind.LAPIDARY_TABLE && LapidaryAbrasives.isAbrasive(stack)) {
            return placeAbrasive(stack, forge, level, pos, player);
        }
        if (forge.canPlaceLooseWorkInput(stack)) {
            return placeAdditionalStationWorkInput(stack, forge, level, pos, player);
        }
        if (forge.canPlaceLooseWorkSecondary(stack)) {
            return placeStationWorkSecondary(stack, forge, level, pos, player);
        }
        StationWorkRecipe stationWorkRecipe = StationWorkRecipeRegistry.findStartRecipe(kind, forge.templateId(), stack).orElse(null);
        if (stationWorkRecipe != null && forge.canPlaceLooseWork(stationWorkRecipe, stack)) {
            return placeStationWork(stack, stationWorkRecipe, forge, level, pos, player);
        }
        if (MaterialCatalog.isMaterial(stack)) {
            return placeMaterial(stack, forge, level, pos, player);
        }
        if (kind == WorkstationKind.LAPIDARY_TABLE && stack.is(ModItems.GEM_CUTTERS_KNIFE.get())) {
            return work(stack, forge, level, pos, player);
        }
        if (kind == WorkstationKind.LAPIDARY_TABLE && SmithingHammerLevel.isHammer(stack)) {
            if (MobsToolForgingConfig.GEMCUTTERS_FILE_REQUIRED.get()) {
                if (!level.isClientSide) {
                    DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.lapidary_needs_knife"));
                }
                return ItemInteractionResult.CONSUME;
            }
            return work(stack, forge, level, pos, player);
        }
        if (kind != WorkstationKind.LAPIDARY_TABLE && SmithingHammerLevel.isHammer(stack)) {
            return work(stack, forge, level, pos, player);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ToolForgeBlockEntity forge) {
            if (!player.isShiftKeyDown() && EmptyMainHandInteractions.shouldDeferToOffhand(
                    player,
                    stack -> canHandleItem(stack, forge, level)
            )) {
                return InteractionResult.PASS;
            }
            if (player.isShiftKeyDown()) {
                return handleSneakUse(forge, level, pos, player, kind);
            }
            if (tryCollectOutput(forge, player)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (handleMissingPattern(forge, level, player)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (kind == WorkstationKind.TOOLMAKERS_BENCH) {
                if (tryCollectToolmakerBenchTool(forge, player, level, pos)) {
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
                if (forge.hasBenchStacks() && forge.benchStacks().stream().noneMatch(ToolmakerBenchAssembly::isFinishedTool)) {
                    assembleTool(ItemStack.EMPTY, forge, level, pos, player);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
                if (!level.isClientSide) {
                    if (forge.hasBenchStacks()) {
                        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.toolmaker_status", forge.benchStacks().size()));
                    } else {
                        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.use_toolmakers_bench"));
                    }
                }
                return InteractionResult.CONSUME;
            }
            if (kind == WorkstationKind.LAPIDARY_TABLE && forge.canHammer()) {
                if (MobsToolForgingConfig.GEMCUTTERS_FILE_REQUIRED.get()) {
                    if (!level.isClientSide) {
                        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.lapidary_needs_knife"));
                    }
                    return InteractionResult.CONSUME;
                }
                ItemInteractionResult result = work(ItemStack.EMPTY, forge, level, pos, player);
                return result.consumesAction() ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
            }
            if (ArmorForgeAttachment.isAttachmentStation(kind) && forge.hasArmorAttachmentTarget()) {
                if (!level.isClientSide) {
                    DebugFeedback.actionBar(player, ArmorForgeAttachment.statusMessage(forge));
                }
                return InteractionResult.CONSUME;
            }
            if (isRepairStation(kind) && forge.hasBenchStacks()) {
                if (!level.isClientSide) {
                    DebugFeedback.actionBar(player, Component.translatable(forge.hasRepairWork() ? "message.mobstoolforging.tool_repair_ready" : "message.mobstoolforging.tool_repair_needs_material"));
                }
                return InteractionResult.CONSUME;
            }
            if (isPatternRackSelectable(kind) && forge.canChangeTemplate()) {
                if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                    PatternRackSelection.begin(serverPlayer, pos, kind);
                }
                return InteractionResult.CONSUME;
            }
            if (!level.isClientSide) {
                StationWorkRecipe recipe = forge.looseWorkRecipe();
                if (recipe != null) {
                    DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.station_status", forge.materialCount(), recipe.input().count(), forge.hitCount(), recipe.requiredHits()));
                } else if (kind == WorkstationKind.LAPIDARY_TABLE && forge.hasLapidaryCoatingBase()) {
                    DebugFeedback.actionBar(player, Component.translatable(
                            "message.mobstoolforging.lapidary_coating_status",
                            forge.materialCount(),
                            forge.lapidaryCoatingRequiredMaterials(),
                            forge.hitCount(),
                            forge.lapidaryCoatingRequiredHits()
                    ));
                } else if (kind == WorkstationKind.LAPIDARY_TABLE && forge.template() == null) {
                    DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.lapidary_use_heated_part"));
                } else if (forge.template() == null) {
                    DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.select_template"));
                } else if (forge.hasMaterialHeat()) {
                    DebugFeedback.actionBar(player, Component.translatable(
                            "message.mobstoolforging.station_heat_status",
                            forge.materialCount(),
                            forge.template().requiredMaterials(),
                            forge.hitCount(),
                            forge.template().requiredHits(),
                            Math.round(forge.materialHeatTemperature() * 100.0F),
                            Component.translatable("tooltip.mobstoolforging.workpiece_status." + forge.materialHeatStatusKey())
                    ));
                } else {
                    DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.station_status", forge.materialCount(), forge.template().requiredMaterials(), forge.hitCount(), forge.template().requiredHits()));
                }
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private boolean canHandleItem(ItemStack stack, ToolForgeBlockEntity forge, Level level) {
        if (kind == WorkstationKind.TOOLMAKERS_BENCH) {
            return SmithingHammerLevel.isHammer(stack)
                    || stack.is(Items.NAME_TAG)
                    || forge.canPlaceToolmakerStack(stack);
        }
        if (isRepairStack(stack, forge, kind)) {
            return true;
        }
        if (ArmorForgeAttachment.isAttachmentStation(kind)
                && ArmorForgeAttachment.isAttachmentTemplate(forge.templateId())
                && (forge.canPlaceArmorAttachmentTarget(stack) || ArmorForgeAttachment.isArmorStack(stack))) {
            return true;
        }
        if (stack.getItem() instanceof ToolTemplateItem templateItem) {
            if (kind == WorkstationKind.LAPIDARY_TABLE
                    || !MobsToolForgingConfig.ALLOW_DIRECT_PATTERN_STATION_SELECTION.get()) {
                return false;
            }
            return canApplyTemplate(stack, templateItem, forge);
        }
        if (kind == WorkstationKind.LAPIDARY_TABLE && ToolForgeBlockEntity.isLapidaryCoatablePart(stack)) {
            return forge.canPlaceLapidaryBasePart(stack);
        }
        if (kind == WorkstationKind.LAPIDARY_TABLE && LapidaryAbrasives.isAbrasive(stack)) {
            return forge.canPlaceAbrasive(stack);
        }
        if (forge.canPlaceLooseWorkInput(stack)) {
            return true;
        }
        if (forge.canPlaceLooseWorkSecondary(stack)) {
            return true;
        }
        StationWorkRecipe stationWorkRecipe = StationWorkRecipeRegistry.findStartRecipe(kind, forge.templateId(), stack).orElse(null);
        if (stationWorkRecipe != null) {
            return forge.canPlaceLooseWork(stationWorkRecipe, stack);
        }
        if (MaterialCatalog.isMaterial(stack)) {
            return canPlaceMaterial(stack, forge, level);
        }
        if (kind == WorkstationKind.LAPIDARY_TABLE) {
            return stack.is(ModItems.GEM_CUTTERS_KNIFE.get()) || !MobsToolForgingConfig.GEMCUTTERS_FILE_REQUIRED.get() && SmithingHammerLevel.isHammer(stack);
        }
        return canUseHammer(stack, forge);
    }

    private boolean canUseHammer(ItemStack stack, ToolForgeBlockEntity forge) {
        if (!SmithingHammerLevel.isHammer(stack)) {
            return false;
        }
        return forge.hasRepairWork() || forge.hasLooseWork() || forge.canHammer();
    }

    private boolean canApplyTemplate(ItemStack stack, ToolTemplateItem templateItem, ToolForgeBlockEntity forge) {
        if (!templateItem.canUseOn(kind)) {
            return false;
        }
        ForgeTemplateDefinition template = templateItem.template(stack).orElse(null);
        if (template == null) {
            return false;
        }
        return PatternRackSelection.canAssign(stack, template, kind)
                && (template.id().equals(forge.templateId()) || forge.canChangeTemplate());
    }

    private boolean canPlaceMaterial(ItemStack stack, ToolForgeBlockEntity forge, Level level) {
        ForgeTemplateDefinition template = forge.template();
        ToolMaterialDefinition material = MaterialCatalog.resolve(stack).orElse(null);
        if (kind == WorkstationKind.LAPIDARY_TABLE && template == null) {
            return material != null && forge.canPlaceLapidaryCoatingMaterial(stack, material);
        }
        if (template == null || forge.remainingMaterials() <= 0) {
            return false;
        }
        if (material == null || material.category() != kind.materialCategory()) {
            return false;
        }
        if (!template.allowsMaterial(material.id())) {
            return false;
        }
        if (kind == WorkstationKind.LAPIDARY_TABLE && !forge.hasRequiredLapidaryAbrasive(material)) {
            return false;
        }
        if (kind.isSmithingAnvilLike()
                && material.category() == MaterialCategory.METAL
                && MobsToolForgingConfig.REQUIRE_HEATED_METAL.get()
                && !forge.canStartMetalWork(stack, material)) {
            return false;
        }
        return forge.materialId() == null || forge.materialId().equals(material.id());
    }

    private static boolean handleMissingPattern(ToolForgeBlockEntity forge, Level level, Player player) {
        if (!forge.selectedPatternMissing()) {
            return false;
        }
        if (!level.isClientSide) {
            forge.clearTemplate();
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.pattern_missing"));
        }
        return true;
    }

    private static boolean isRepairStation(WorkstationKind kind) {
        return kind.isSmithingAnvilLike() || kind == WorkstationKind.LEATHER_STATION;
    }

    private static boolean isRepairStack(ItemStack stack, ToolForgeBlockEntity forge, WorkstationKind kind) {
        return isRepairStation(kind)
                && (forge.canPlaceRepairTool(stack) || forge.canPlaceRepairMaterial(stack));
    }

    private static boolean isPatternRackSelectable(WorkstationKind kind) {
        return kind == WorkstationKind.CRUDE_ANVIL
                || kind == WorkstationKind.TOOL_FORGE
                || kind == WorkstationKind.SAWMILL
                || kind == WorkstationKind.LEATHER_STATION;
    }

    private ItemInteractionResult applyTemplateItem(ItemStack stack, ToolTemplateItem templateItem, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (!templateItem.canUseOn(kind)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        ForgeTemplateDefinition template = templateItem.template(stack).orElse(null);
        if (template == null) {
            if (!level.isClientSide) {
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.invalid_template_pattern"));
            }
            return ItemInteractionResult.CONSUME;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!PatternRackSelection.canAssign(stack, template, kind)) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.pattern_wrong_station"));
            return ItemInteractionResult.CONSUME;
        }
        if (!forge.setTemplateFromItem(template)) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.forge_busy"));
            return ItemInteractionResult.CONSUME;
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 1.1F + level.random.nextFloat() * 0.1F);
        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.template_selected", template.displayName()));
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult placeMaterial(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        ForgeTemplateDefinition template = forge.template();
        ToolMaterialDefinition material = MaterialCatalog.resolve(stack).orElse(null);
        if (material == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (kind == WorkstationKind.LAPIDARY_TABLE && template == null) {
            return placeLapidaryCoatingMaterial(stack, material, forge, level, pos, player);
        }
        if (template == null) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.select_template"));
            return ItemInteractionResult.CONSUME;
        }
        if (material.category() != kind.materialCategory()) {
            DebugFeedback.actionBar(player, Component.translatable(kind.wrongStationMessage()));
            return ItemInteractionResult.CONSUME;
        }
        if (!template.allowsMaterial(material.id())) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.material_not_allowed"));
            return ItemInteractionResult.CONSUME;
        }
        ResourceLocation missingAbrasiveTier = kind == WorkstationKind.LAPIDARY_TABLE
                ? forge.missingRequiredLapidaryAbrasive(material).orElse(null)
                : null;
        if (missingAbrasiveTier != null) {
            DebugFeedback.actionBar(player, lapidaryNeedsAbrasiveMessage(missingAbrasiveTier));
            return ItemInteractionResult.CONSUME;
        }
        if (kind.isSmithingAnvilLike()
                && material.category() == MaterialCategory.METAL
                && MobsToolForgingConfig.REQUIRE_HEATED_METAL.get()
                && !forge.canStartMetalWork(stack, material)) {
            DebugFeedback.actionBar(player, metalNeedsHeatMessage(template, stack, level));
            return ItemInteractionResult.CONSUME;
        }
        if (forge.materialId() != null && !forge.materialId().equals(material.id())) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.mixed_materials"));
            return ItemInteractionResult.CONSUME;
        }
        int taken = forge.acceptMaterials(stationInputStack(stack, player, forge.remainingMaterials()), material);
        if (taken > 0) {
            level.playSound(null, pos, kind.placeSound(), SoundSource.BLOCKS, 0.8F, 0.9F + level.random.nextFloat() * 0.15F);
            player.awardStat(Stats.ITEM_USED.get(material.displayItem()));
            if (ArmorForgeAttachment.isAttachmentTemplate(template) && forge.hasArmorAttachmentTarget()) {
                DebugFeedback.actionBar(player, ArmorForgeAttachment.statusMessage(forge));
            }
            return ItemInteractionResult.CONSUME;
        }
        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.materials_full"));
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult placeLapidaryBasePart(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!forge.canPlaceLapidaryBasePart(stack)) {
            boolean needsHeat = !WorkpieceHeat.isHot(stack, level);
            DebugFeedback.actionBar(player, Component.translatable(
                    needsHeat ? "message.mobstoolforging.lapidary_part_needs_heat" : "message.mobstoolforging.forge_busy"
            ));
            return ItemInteractionResult.CONSUME;
        }
        var item = stack.getItem();
        if (forge.placeLapidaryBasePart(stationInputStack(stack, player, 1))) {
            level.playSound(null, pos, kind.placeSound(), SoundSource.BLOCKS, 0.8F, 1.0F + level.random.nextFloat() * 0.15F);
            player.awardStat(Stats.ITEM_USED.get(item));
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.lapidary_part_placed"));
        }
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult placeLapidaryCoatingMaterial(ItemStack stack, ToolMaterialDefinition material, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (material.category() != MaterialCategory.GEM) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.lapidary_needs_gem"));
            return ItemInteractionResult.CONSUME;
        }
        if (!forge.hasLapidaryCoatingBase()) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.lapidary_use_heated_part"));
            return ItemInteractionResult.CONSUME;
        }
        ResourceLocation missingAbrasiveTier = forge.missingRequiredLapidaryAbrasive(material).orElse(null);
        if (missingAbrasiveTier != null) {
            DebugFeedback.actionBar(player, lapidaryNeedsAbrasiveMessage(missingAbrasiveTier));
            return ItemInteractionResult.CONSUME;
        }
        if (forge.materialId() != null && !forge.materialId().equals(material.id())) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.mixed_materials"));
            return ItemInteractionResult.CONSUME;
        }
        int taken = forge.acceptLapidaryCoatingMaterial(stationInputStack(stack, player, forge.remainingMaterials()), material);
        if (taken > 0) {
            level.playSound(null, pos, kind.placeSound(), SoundSource.BLOCKS, 0.8F, 1.15F + level.random.nextFloat() * 0.15F);
            player.awardStat(Stats.ITEM_USED.get(material.displayItem()));
            DebugFeedback.actionBar(player, Component.translatable(
                    "message.mobstoolforging.lapidary_shell_material_placed",
                    forge.materialCount(),
                    forge.lapidaryCoatingRequiredMaterials()
            ));
            return ItemInteractionResult.CONSUME;
        }
        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.materials_full"));
        return ItemInteractionResult.CONSUME;
    }

    private static InteractionResult handleSneakUse(ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player, WorkstationKind kind) {
        if (tryCollectOutput(forge, player)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (tryClearPlacedWork(forge, player, level, pos)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (handleMissingPattern(forge, level, player)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (forge.template() != null) {
            if (forge.canChangeTemplate()) {
                if (!level.isClientSide && forge.clearTemplate()) {
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.6F, 1.15F);
                    DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.template_cleared"));
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!level.isClientSide) {
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.forge_busy"));
            }
            return InteractionResult.CONSUME;
        }
        if (kind != WorkstationKind.TOOLMAKERS_BENCH && kind != WorkstationKind.LAPIDARY_TABLE && debugTemplateSelectorEnabled()) {
            openTemplateSelector(level, pos, player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (isPatternRackSelectable(kind)) {
            if (!level.isClientSide) {
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.no_pattern_selected"));
            }
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide) {
            DebugFeedback.actionBar(player, Component.translatable(kind == WorkstationKind.TOOLMAKERS_BENCH ? "message.mobstoolforging.use_toolmakers_bench" : "message.mobstoolforging.no_pattern_selected"));
        }
        return InteractionResult.CONSUME;
    }

    private static ItemInteractionResult itemResult(InteractionResult result, Level level) {
        return result.consumesAction() ? ItemInteractionResult.sidedSuccess(level.isClientSide) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private ItemInteractionResult placeAbrasive(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        var item = stack.getItem();
        if (!forge.placeAbrasive(stationInputStack(stack, player, 1))) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.lapidary_abrasive_present"));
            return ItemInteractionResult.CONSUME;
        }
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.65F, 1.35F);
        player.awardStat(Stats.ITEM_USED.get(item));
        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.lapidary_abrasive_placed"));
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult placeArmorAttachmentTarget(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        var item = stack.getItem();
        if (!forge.placeArmorAttachmentTarget(stationInputStack(stack, player, 1))) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.armor_attachment_wrong_target"));
            return ItemInteractionResult.CONSUME;
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 1.05F);
        player.awardStat(Stats.ITEM_USED.get(item));
        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.armor_attachment_target_placed"));
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult useSmithingAnvilRepair(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (stack.isEmpty() || SmithingHammerLevel.isHammer(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (forge.canPlaceRepairTool(stack)) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            var item = stack.getItem();
            if (!forge.placeRepairTool(stationInputStack(stack, player, 1))) {
                return ItemInteractionResult.CONSUME;
            }
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.7F, 1.0F + level.random.nextFloat() * 0.1F);
            player.awardStat(Stats.ITEM_USED.get(item));
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.tool_repair_tool_placed"));
            return ItemInteractionResult.CONSUME;
        }
        if (forge.canPlaceRepairMaterial(stack)) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            var item = stack.getItem();
            if (!forge.placeRepairMaterial(stationInputStack(stack, player, 1))) {
                return ItemInteractionResult.CONSUME;
            }
            level.playSound(null, pos, kind.placeSound(), SoundSource.BLOCKS, 0.7F, 1.0F + level.random.nextFloat() * 0.1F);
            player.awardStat(Stats.ITEM_USED.get(item));
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.tool_repair_material_placed"));
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private ItemInteractionResult placeStationWork(ItemStack stack, StationWorkRecipe recipe, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        var item = stack.getItem();
        if (forge.placeLooseWork(recipe, stationInputStack(stack, player, recipe.input().count()))) {
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 1.05F);
            player.awardStat(Stats.ITEM_USED.get(item));
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.station_work_placed"));
        } else {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.need_materials"));
        }
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult placeAdditionalStationWorkInput(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        var item = stack.getItem();
        if (forge.placeLooseWorkInput(stationInputStack(stack, player, forge.remainingLooseWorkInput()))) {
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 1.05F);
            player.awardStat(Stats.ITEM_USED.get(item));
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.station_work_placed"));
        }
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult placeStationWorkSecondary(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        var item = stack.getItem();
        if (forge.placeLooseWorkSecondary(stack)) {
            level.playSound(null, pos, kind.placeSound(), SoundSource.BLOCKS, 0.7F, 1.1F);
            player.awardStat(Stats.ITEM_USED.get(item));
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.station_work_secondary_placed"));
        }
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult useToolmakersBench(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (stack.isEmpty()) {
            if (tryCollectToolmakerBenchTool(forge, player, level, pos)) {
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            if (forge.hasBenchStacks() && forge.benchStacks().stream().noneMatch(ToolmakerBenchAssembly::isFinishedTool)) {
                return assembleTool(ItemStack.EMPTY, forge, level, pos, player);
            }
            if (!level.isClientSide) {
                if (forge.hasBenchStacks()) {
                    DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.toolmaker_status", forge.benchStacks().size()));
                } else {
                    DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.use_toolmakers_bench"));
                }
            }
            return ItemInteractionResult.CONSUME;
        }
        if (SmithingHammerLevel.isHammer(stack)) {
            List<ItemStack> benchStacks = forge.benchStacks();
            if (benchStacks.size() == 1 && ToolmakerBenchAssembly.isFinishedTool(benchStacks.get(0))) {
                return disassembleTool(stack, forge, level, pos, player);
            }
            return assembleTool(stack, forge, level, pos, player);
        }
        if (stack.is(Items.NAME_TAG)) {
            return applyNameTagToToolmakerItem(stack, forge, level, pos, player);
        }
        if (forge.canPlaceToolmakerStack(stack)) {
            return placeToolmakerStack(stack, forge, level, pos, player);
        }
        if (!level.isClientSide) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.toolmaker_invalid"));
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private ItemInteractionResult placeToolmakerStack(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        var item = stack.getItem();
        if (!forge.placeToolmakerStack(stationInputStack(stack, player, 1))) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.toolmaker_invalid"));
            return ItemInteractionResult.CONSUME;
        }
        level.playSound(null, pos, kind.placeSound(), SoundSource.BLOCKS, 0.7F, 1.0F + level.random.nextFloat() * 0.1F);
        player.awardStat(Stats.ITEM_USED.get(item));
        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.toolmaker_part_placed"));
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult applyNameTagToToolmakerItem(ItemStack nameTag, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        Component name = nameTag.get(DataComponents.CUSTOM_NAME);
        List<ItemStack> benchStacks = forge.benchStacks();
        if (name == null || benchStacks.size() != 1 || !ToolmakerBenchAssembly.isFinishedTool(benchStacks.get(0))) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.toolmaker_name_tag_invalid"));
            return ItemInteractionResult.CONSUME;
        }
        ItemStack renamed = benchStacks.get(0).copyWithCount(1);
        renamed.set(DataComponents.CUSTOM_NAME, name);
        forge.setToolmakerStacks(List.of(renamed));
        if (!player.getAbilities().instabuild) {
            nameTag.shrink(1);
        }
        player.awardStat(Stats.ITEM_USED.get(Items.NAME_TAG));
        level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.45F, 1.35F);
        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.toolmaker_name_tag_applied", renamed.getHoverName()));
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult assembleTool(ItemStack tool, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        ItemStack output = ToolmakerBenchAssembly.assemble(forge.benchStacks(), level.registryAccess());
        if (output.isEmpty()) {
            DebugFeedback.actionBar(player, Component.translatable(forge.hasBenchStacks() ? "message.mobstoolforging.toolmaker_invalid" : "message.mobstoolforging.toolmaker_needs_parts"));
            return ItemInteractionResult.CONSUME;
        }
        forge.setToolmakerStacks(List.of(output));
        playWorkEffects(tool, level, pos, player);
        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.toolmaker_assembled"));
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult disassembleTool(ItemStack tool, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        List<ItemStack> benchStacks = forge.benchStacks();
        if (benchStacks.size() != 1 || !ToolmakerBenchAssembly.isFinishedTool(benchStacks.get(0))) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.toolmaker_no_tool"));
            return ItemInteractionResult.CONSUME;
        }
        ItemStack benchTool = benchStacks.get(0);
        if (Boolean.TRUE.equals(benchTool.get(ModDataComponents.TOOL_BROKEN.get()))
                || Boolean.TRUE.equals(benchTool.get(ModDataComponents.ARMOR_BROKEN.get()))) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.toolmaker_broken_tool"));
            return ItemInteractionResult.CONSUME;
        }
        List<ItemStack> parts = ToolmakerBenchAssembly.disassemble(benchTool).orElse(List.of());
        if (parts.isEmpty()) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.toolmaker_invalid"));
            return ItemInteractionResult.CONSUME;
        }
        forge.setToolmakerStacks(parts);
        if (!player.getAbilities().instabuild) {
            for (int index = ToolForgeBlockEntity.maxBenchStacks(); index < parts.size(); index++) {
                ItemStack overflow = parts.get(index);
                if (!overflow.isEmpty() && !player.getInventory().add(overflow)) {
                    player.drop(overflow, false);
                }
            }
        }
        playWorkEffects(tool, level, pos, player);
        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.toolmaker_disassembled"));
        return ItemInteractionResult.CONSUME;
    }

    private static boolean tryCollectToolmakerBenchTool(ToolForgeBlockEntity forge, Player player, Level level, BlockPos pos) {
        List<ItemStack> benchStacks = forge.benchStacks();
        if (benchStacks.size() != 1 || !ToolmakerBenchAssembly.isFinishedTool(benchStacks.get(0))) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }

        ItemStack tool = benchStacks.get(0).copyWithCount(1);
        if (player.getInventory().add(tool)) {
            forge.clearToolmakerStacks();
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
        } else {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.inventory_full"));
        }
        return true;
    }

    private ItemInteractionResult work(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        int hammerLevel = (stack.isEmpty() || stack.is(ModItems.GEM_CUTTERS_KNIFE.get())) && kind == WorkstationKind.LAPIDARY_TABLE
                ? SmithingHammerLevel.IRON.level()
                : SmithingHammerLevel.levelOf(stack);
        if (isRepairStation(kind) && forge.hasRepairWork()) {
            return repairTool(stack, forge, level, pos, player);
        }
        if (forge.hasLooseWork()) {
            return workStationRecipe(stack, hammerLevel, forge, level, pos, player);
        }
        if (!forge.canHammer()) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.need_materials"));
            return ItemInteractionResult.CONSUME;
        }
        ForgeTemplateDefinition template = forge.template();
        int requiredHammerLevel = template == null || forge.materialId() == null ? SmithingHammerLevel.STONE.level() : template.minimumHammerLevel(forge.materialId());
        if (hammerLevel < requiredHammerLevel) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.hammer_too_weak", SmithingHammerLevel.displayName(requiredHammerLevel)));
            return ItemInteractionResult.CONSUME;
        }
        if (kind.isSmithingAnvilLike() && MobsToolForgingConfig.REQUIRE_HEATED_METAL.get() && !forge.materialIsForgeReady()) {
            if (template == null || !forge.hasMaterialHeat()) {
                DebugFeedback.actionBar(player, template == null ? Component.translatable("message.mobstoolforging.metal_needs_heat") : metalNeedsHeatMessage(template));
            } else {
                DebugFeedback.actionBar(player, Component.translatable(
                        "message.mobstoolforging.metal_needs_heat_current",
                        Math.round(forge.materialHeatTemperature() * 100.0F),
                        Math.round(template.minimumTemperature() * 100.0F),
                        Component.translatable("tooltip.mobstoolforging.workpiece_status." + forge.materialHeatStatusKey())
                ));
            }
            return ItemInteractionResult.CONSUME;
        }
        boolean armorAttachment = forge.hasArmorAttachmentTarget();
        boolean precisionTool = kind == WorkstationKind.LAPIDARY_TABLE && stack.is(ModItems.GEM_CUTTERS_KNIFE.get());
        if (forge.hammer(precisionTool)) {
            playWorkEffects(stack, level, pos, player);
            if (forge.isComplete()) {
                String completeKey = armorAttachment
                        ? "message.mobstoolforging.armor_attachment_complete"
                        : kind == WorkstationKind.LAPIDARY_TABLE && forge.hasLapidaryCoatingBase()
                        ? "message.mobstoolforging.lapidary_coating_complete"
                        : "message.mobstoolforging.complete";
                DebugFeedback.actionBar(player, Component.translatable(completeKey));
            }
        }
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult repairTool(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        ItemStack output = forge.repairToolWithPlacedMaterial();
        if (output.isEmpty()) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.need_materials"));
            return ItemInteractionResult.CONSUME;
        }
        playWorkEffects(stack, level, pos, player);
        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.tool_repaired"));
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult workStationRecipe(ItemStack stack, int hammerLevel, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        StationWorkRecipe recipe = forge.looseWorkRecipe();
        if (recipe == null) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.station_work_missing"));
            return ItemInteractionResult.CONSUME;
        }
        if (forge.needsLooseWorkInput()) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.need_materials"));
            return ItemInteractionResult.CONSUME;
        }
        if (hammerLevel < recipe.minimumHammerLevel()) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.hammer_too_weak", SmithingHammerLevel.displayName(recipe.minimumHammerLevel())));
            return ItemInteractionResult.CONSUME;
        }
        if (forge.needsLooseWorkSecondary()) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.station_work_needs_secondary"));
            return ItemInteractionResult.CONSUME;
        }
        if (forge.hammerLooseWork(recipe)) {
            playWorkEffects(stack, level, pos, player);
            if (forge.isComplete()) {
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.station_work_ready"));
            }
        }
        return ItemInteractionResult.CONSUME;
    }

    private void playWorkEffects(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (!stack.isEmpty() && !player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
        level.playSound(null, pos, kind.workSound(), SoundSource.BLOCKS, 0.45F, 1.1F + level.random.nextFloat() * 0.2F);
        if (level instanceof ServerLevel serverLevel) {
            int count = kind.isSmithingAnvilLike() ? 14 : kind == WorkstationKind.LAPIDARY_TABLE ? 10 : 8;
            double speed = kind.isSmithingAnvilLike() ? 0.09D : kind == WorkstationKind.LAPIDARY_TABLE ? 0.055D : 0.02D;
            serverLevel.sendParticles(kind.workParticle(), pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5,
                    count, 0.18, 0.05, 0.18, speed);
            if (kind.isSmithingAnvilLike()) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.LAVA,
                        pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5,
                        2, 0.12, 0.025, 0.12, 0.015);
            } else if (kind == WorkstationKind.LAPIDARY_TABLE) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                        pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5,
                        3, 0.12, 0.035, 0.12, 0.015);
            }
        }
        if (kind.isSmithingAnvilLike() && level.getBlockEntity(pos) instanceof ToolForgeBlockEntity forge) {
            HeatingInteractionEffects.hammerStrike(level, pos, forge.materialHeatTemperature());
        }
    }

    private static boolean tryClearPlacedWork(ToolForgeBlockEntity forge, Player player, Level level, BlockPos pos) {
        if (!forge.hasPlacedWork()) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        List<ItemStack> removedStacks = forge.removePlacedWorkStacks();
        if (!player.getAbilities().instabuild) {
            for (ItemStack removed : removedStacks) {
                if (!removed.isEmpty() && !player.getInventory().add(removed)) {
                    player.drop(removed, false);
                }
            }
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.55F, 1.05F);
        DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.station_cleared"));
        return true;
    }

    private static boolean tryCollectOutput(ToolForgeBlockEntity forge, Player player) {
        if (!forge.isComplete()) {
            return false;
        }
        ItemStack output = forge.outputStack();
        if (output.isEmpty()) {
            return false;
        }
        if (player.level().isClientSide) {
            return true;
        }
        if (player.getInventory().add(output)) {
            forge.removeOutput();
            player.level().playSound(null, forge.getBlockPos(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
        } else {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.inventory_full"));
        }
        return true;
    }

    private static void openTemplateSelector(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ModNetworking.openTemplateSelector(serverPlayer, pos);
        }
    }

    private static boolean debugTemplateSelectorEnabled() {
        return MobsToolForgingConfig.DEBUG_TEMPLATE_SELECTOR.get();
    }

    private static ItemStack stationInputStack(ItemStack stack, Player player, int count) {
        return player.getAbilities().instabuild ? stack.copyWithCount(Math.max(1, count)) : stack;
    }

    private static Component lapidaryNeedsAbrasiveMessage(ResourceLocation tier) {
        return Component.translatable("message.mobstoolforging.lapidary_needs_abrasive_tier", LapidaryAbrasives.displayName(tier));
    }

    private static Component metalNeedsHeatMessage(ForgeTemplateDefinition template) {
        return Component.translatable("message.mobstoolforging.metal_needs_workshop_heat");
    }

    private static Component metalNeedsHeatMessage(ForgeTemplateDefinition template, ItemStack stack, Level level) {
        float temperature = WorkpieceHeat.temperature(stack, level);
        if (temperature <= 0.0F) {
            return metalNeedsHeatMessage(template);
        }
        ToolMaterialDefinition material = MaterialCatalog.resolve(stack).orElse(null);
        float minimumTemperature = material == null ? template.minimumTemperature() : WorkshopHeat.minimumForgeTemperature(material, template);
        return Component.translatable(
                "message.mobstoolforging.metal_needs_heat_current",
                WorkpieceHeat.displayPercent(temperature),
                WorkpieceHeat.displayPercent(minimumTemperature),
                Component.translatable("tooltip.mobstoolforging.workpiece_status." + WorkpieceHeat.statusKey(temperature, WorkpieceHeat.isWorkable(stack), minimumTemperature))
        );
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ToolForgeBlockEntity forge) {
            if (forge.isComplete()) {
                Block.popResource(level, pos, forge.outputStack());
            } else {
                ItemStack materialDrop = forge.materialDropStack();
                if (!materialDrop.isEmpty()) {
                    Block.popResource(level, pos, materialDrop);
                }
            }
            ItemStack abrasiveDrop = forge.abrasiveStack();
            if (!abrasiveDrop.isEmpty()) {
                Block.popResource(level, pos, abrasiveDrop);
            }
            for (ItemStack benchStack : forge.benchStacks()) {
                if (!benchStack.isEmpty()) {
                    Block.popResource(level, pos, benchStack);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    protected abstract VoxelShape shapeForState(BlockState state);

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
