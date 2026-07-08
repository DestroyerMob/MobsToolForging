package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
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
        if (EmptyMainHandInteractions.shouldFallbackToEmptyHand(player, hand) && !canUseItem(stack, forge, level)) {
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
                    player.displayClientMessage(Component.translatable("message.mobstoolforging.lapidary_use_heated_part"), true);
                }
                return ItemInteractionResult.CONSUME;
            }
            if (!MobsToolForgingConfig.ALLOW_DIRECT_PATTERN_STATION_SELECTION.get()) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_rack_select"), true);
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
                    player.displayClientMessage(Component.translatable(forge.hasPlacedWork() ? "message.mobstoolforging.forge_busy" : "message.mobstoolforging.armor_attachment_wrong_target"), true);
                }
                return ItemInteractionResult.CONSUME;
            }
        }
        if (kind == WorkstationKind.LAPIDARY_TABLE && LapidaryAbrasives.isAbrasive(stack)) {
            return placeAbrasive(stack, forge, level, pos, player);
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
                    player.displayClientMessage(Component.translatable("message.mobstoolforging.lapidary_needs_knife"), true);
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
                        player.displayClientMessage(Component.translatable("message.mobstoolforging.toolmaker_status", forge.benchStacks().size()), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.mobstoolforging.use_toolmakers_bench"), true);
                    }
                }
                return InteractionResult.CONSUME;
            }
            if (kind == WorkstationKind.LAPIDARY_TABLE && forge.canHammer()) {
                if (MobsToolForgingConfig.GEMCUTTERS_FILE_REQUIRED.get()) {
                    if (!level.isClientSide) {
                        player.displayClientMessage(Component.translatable("message.mobstoolforging.lapidary_needs_knife"), true);
                    }
                    return InteractionResult.CONSUME;
                }
                ItemInteractionResult result = work(ItemStack.EMPTY, forge, level, pos, player);
                return result.consumesAction() ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
            }
            if (ArmorForgeAttachment.isAttachmentStation(kind) && forge.hasArmorAttachmentTarget()) {
                if (!level.isClientSide) {
                    player.displayClientMessage(ArmorForgeAttachment.statusMessage(forge), true);
                }
                return InteractionResult.CONSUME;
            }
            if (isRepairStation(kind) && forge.hasBenchStacks()) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable(forge.hasRepairWork() ? "message.mobstoolforging.tool_repair_ready" : "message.mobstoolforging.tool_repair_needs_material"), true);
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
                    player.displayClientMessage(Component.translatable("message.mobstoolforging.station_status", forge.materialCount(), recipe.input().count(), forge.hitCount(), recipe.requiredHits()), true);
                } else if (kind == WorkstationKind.LAPIDARY_TABLE && forge.hasLapidaryCoatingBase()) {
                    player.displayClientMessage(Component.translatable(
                            "message.mobstoolforging.lapidary_coating_status",
                            forge.materialCount(),
                            forge.lapidaryCoatingRequiredMaterials(),
                            forge.hitCount(),
                            forge.lapidaryCoatingRequiredHits()
                    ), true);
                } else if (kind == WorkstationKind.LAPIDARY_TABLE && forge.template() == null) {
                    player.displayClientMessage(Component.translatable("message.mobstoolforging.lapidary_use_heated_part"), true);
                } else if (forge.template() == null) {
                    player.displayClientMessage(Component.translatable("message.mobstoolforging.select_template"), true);
                } else if (forge.hasMaterialHeat()) {
                    player.displayClientMessage(Component.translatable(
                            "message.mobstoolforging.station_heat_status",
                            forge.materialCount(),
                            forge.template().requiredMaterials(),
                            forge.hitCount(),
                            forge.template().requiredHits(),
                            Math.round(forge.materialHeatTemperature() * 100.0F),
                            Component.translatable("tooltip.mobstoolforging.workpiece_status." + forge.materialHeatStatusKey())
                    ), true);
                } else {
                    player.displayClientMessage(Component.translatable("message.mobstoolforging.station_status", forge.materialCount(), forge.template().requiredMaterials(), forge.hitCount(), forge.template().requiredHits()), true);
                }
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private boolean canUseItem(ItemStack stack, ToolForgeBlockEntity forge, Level level) {
        if (kind == WorkstationKind.TOOLMAKERS_BENCH) {
            return SmithingHammerLevel.isHammer(stack)
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
            if (kind == WorkstationKind.LAPIDARY_TABLE) {
                return false;
            }
            if (!MobsToolForgingConfig.ALLOW_DIRECT_PATTERN_STATION_SELECTION.get()) {
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
        return SmithingHammerLevel.isHammer(stack);
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

    private static boolean handleMissingPattern(ToolForgeBlockEntity forge, Level level, Player player) {
        if (!forge.selectedPatternMissing()) {
            return false;
        }
        if (!level.isClientSide) {
            forge.clearTemplate();
            player.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_missing"), true);
        }
        return true;
    }

    private static boolean isRepairStation(WorkstationKind kind) {
        return kind.isSmithingAnvilLike();
    }

    private static boolean isRepairStack(ItemStack stack, ToolForgeBlockEntity forge, WorkstationKind kind) {
        return isRepairStation(kind)
                && (forge.canPlaceRepairTool(stack) || forge.canPlaceRepairMaterial(stack));
    }

    private static boolean isPatternRackSelectable(WorkstationKind kind) {
        return kind == WorkstationKind.CRUDE_ANVIL
                || kind == WorkstationKind.TOOL_FORGE
                || kind == WorkstationKind.LEATHER_STATION;
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

    private ItemInteractionResult applyTemplateItem(ItemStack stack, ToolTemplateItem templateItem, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (!templateItem.canUseOn(kind)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        ForgeTemplateDefinition template = templateItem.template(stack).orElse(null);
        if (template == null) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.mobstoolforging.invalid_template_pattern"), true);
            }
            return ItemInteractionResult.CONSUME;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!PatternRackSelection.canAssign(stack, template, kind)) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.pattern_wrong_station"), true);
            return ItemInteractionResult.CONSUME;
        }
        if (!forge.setTemplateFromItem(template)) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.forge_busy"), true);
            return ItemInteractionResult.CONSUME;
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 1.1F + level.random.nextFloat() * 0.1F);
        player.displayClientMessage(Component.translatable("message.mobstoolforging.template_selected", template.displayName()), true);
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
            player.displayClientMessage(Component.translatable("message.mobstoolforging.select_template"), true);
            return ItemInteractionResult.CONSUME;
        }
        if (material.category() != kind.materialCategory()) {
            player.displayClientMessage(Component.translatable(kind.wrongStationMessage()), true);
            return ItemInteractionResult.CONSUME;
        }
        if (!template.allowsMaterial(material.id())) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.material_not_allowed"), true);
            return ItemInteractionResult.CONSUME;
        }
        ResourceLocation missingAbrasiveTier = kind == WorkstationKind.LAPIDARY_TABLE
                ? forge.missingRequiredLapidaryAbrasive(material).orElse(null)
                : null;
        if (missingAbrasiveTier != null) {
            player.displayClientMessage(lapidaryNeedsAbrasiveMessage(missingAbrasiveTier), true);
            return ItemInteractionResult.CONSUME;
        }
        if (kind.isSmithingAnvilLike()
                && material.category() == MaterialCategory.METAL
                && MobsToolForgingConfig.REQUIRE_HEATED_METAL.get()
                && !forge.canStartMetalWork(stack, material)) {
            player.displayClientMessage(metalNeedsHeatMessage(template, stack, level), true);
            return ItemInteractionResult.CONSUME;
        }
        if (forge.materialId() != null && !forge.materialId().equals(material.id())) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.mixed_materials"), true);
            return ItemInteractionResult.CONSUME;
        }
        int taken = forge.acceptMaterials(stationInputStack(stack, player, forge.remainingMaterials()), material);
        if (taken > 0) {
            level.playSound(null, pos, kind.placeSound(), SoundSource.BLOCKS, 0.8F, 0.9F + level.random.nextFloat() * 0.15F);
            player.awardStat(Stats.ITEM_USED.get(material.displayItem()));
            if (ArmorForgeAttachment.isAttachmentTemplate(template) && forge.hasArmorAttachmentTarget()) {
                player.displayClientMessage(ArmorForgeAttachment.statusMessage(forge), true);
            }
            return ItemInteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.translatable("message.mobstoolforging.materials_full"), true);
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult placeLapidaryBasePart(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!forge.canPlaceLapidaryBasePart(stack)) {
            boolean needsHeat = !WorkpieceHeat.isHot(stack, level);
            player.displayClientMessage(Component.translatable(
                    needsHeat ? "message.mobstoolforging.lapidary_part_needs_heat" : "message.mobstoolforging.forge_busy"
            ), true);
            return ItemInteractionResult.CONSUME;
        }
        var item = stack.getItem();
        if (forge.placeLapidaryBasePart(stationInputStack(stack, player, 1))) {
            level.playSound(null, pos, kind.placeSound(), SoundSource.BLOCKS, 0.8F, 1.0F + level.random.nextFloat() * 0.15F);
            player.awardStat(Stats.ITEM_USED.get(item));
            player.displayClientMessage(Component.translatable("message.mobstoolforging.lapidary_part_placed"), true);
        }
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult placeLapidaryCoatingMaterial(ItemStack stack, ToolMaterialDefinition material, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (material.category() != MaterialCategory.GEM) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.lapidary_needs_gem"), true);
            return ItemInteractionResult.CONSUME;
        }
        if (!forge.hasLapidaryCoatingBase()) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.lapidary_use_heated_part"), true);
            return ItemInteractionResult.CONSUME;
        }
        ResourceLocation missingAbrasiveTier = forge.missingRequiredLapidaryAbrasive(material).orElse(null);
        if (missingAbrasiveTier != null) {
            player.displayClientMessage(lapidaryNeedsAbrasiveMessage(missingAbrasiveTier), true);
            return ItemInteractionResult.CONSUME;
        }
        if (forge.materialId() != null && !forge.materialId().equals(material.id())) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.mixed_materials"), true);
            return ItemInteractionResult.CONSUME;
        }
        int taken = forge.acceptLapidaryCoatingMaterial(stationInputStack(stack, player, forge.remainingMaterials()), material);
        if (taken > 0) {
            level.playSound(null, pos, kind.placeSound(), SoundSource.BLOCKS, 0.8F, 1.15F + level.random.nextFloat() * 0.15F);
            player.awardStat(Stats.ITEM_USED.get(material.displayItem()));
            player.displayClientMessage(Component.translatable(
                    "message.mobstoolforging.lapidary_shell_material_placed",
                    forge.materialCount(),
                    forge.lapidaryCoatingRequiredMaterials()
            ), true);
            return ItemInteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.translatable("message.mobstoolforging.materials_full"), true);
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
                    player.displayClientMessage(Component.translatable("message.mobstoolforging.template_cleared"), true);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.mobstoolforging.forge_busy"), true);
            }
            return InteractionResult.CONSUME;
        }
        if (kind != WorkstationKind.TOOLMAKERS_BENCH && kind != WorkstationKind.LAPIDARY_TABLE && debugTemplateSelectorEnabled()) {
            openTemplateSelector(level, pos, player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (isPatternRackSelectable(kind)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.mobstoolforging.no_pattern_selected"), true);
            }
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable(kind == WorkstationKind.TOOLMAKERS_BENCH ? "message.mobstoolforging.use_toolmakers_bench" : "message.mobstoolforging.sneak_hint"), true);
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
            player.displayClientMessage(Component.translatable("message.mobstoolforging.lapidary_abrasive_present"), true);
            return ItemInteractionResult.CONSUME;
        }
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.65F, 1.35F);
        player.awardStat(Stats.ITEM_USED.get(item));
        player.displayClientMessage(Component.translatable("message.mobstoolforging.lapidary_abrasive_placed"), true);
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult placeArmorAttachmentTarget(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        var item = stack.getItem();
        if (!forge.placeArmorAttachmentTarget(stationInputStack(stack, player, 1))) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.armor_attachment_wrong_target"), true);
            return ItemInteractionResult.CONSUME;
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 1.05F);
        player.awardStat(Stats.ITEM_USED.get(item));
        player.displayClientMessage(Component.translatable("message.mobstoolforging.armor_attachment_target_placed"), true);
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
            player.displayClientMessage(Component.translatable("message.mobstoolforging.tool_repair_tool_placed"), true);
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
            player.displayClientMessage(Component.translatable("message.mobstoolforging.tool_repair_material_placed"), true);
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
            player.displayClientMessage(Component.translatable("message.mobstoolforging.station_work_placed"), true);
        } else {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.need_materials"), true);
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
                    player.displayClientMessage(Component.translatable("message.mobstoolforging.toolmaker_status", forge.benchStacks().size()), true);
                } else {
                    player.displayClientMessage(Component.translatable("message.mobstoolforging.use_toolmakers_bench"), true);
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
        if (forge.canPlaceToolmakerStack(stack)) {
            return placeToolmakerStack(stack, forge, level, pos, player);
        }
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.toolmaker_invalid"), true);
        }
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult placeToolmakerStack(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        var item = stack.getItem();
        if (!forge.placeToolmakerStack(stationInputStack(stack, player, 1))) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.toolmaker_invalid"), true);
            return ItemInteractionResult.CONSUME;
        }
        level.playSound(null, pos, kind.placeSound(), SoundSource.BLOCKS, 0.7F, 1.0F + level.random.nextFloat() * 0.1F);
        player.awardStat(Stats.ITEM_USED.get(item));
        player.displayClientMessage(Component.translatable("message.mobstoolforging.toolmaker_part_placed"), true);
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult assembleTool(ItemStack tool, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        ItemStack output = ToolmakerBenchAssembly.assemble(forge.benchStacks(), level.registryAccess());
        if (output.isEmpty()) {
            player.displayClientMessage(Component.translatable(forge.hasBenchStacks() ? "message.mobstoolforging.toolmaker_invalid" : "message.mobstoolforging.toolmaker_needs_parts"), true);
            return ItemInteractionResult.CONSUME;
        }
        forge.setToolmakerStacks(List.of(output));
        playWorkEffects(tool, level, pos, player);
        player.displayClientMessage(Component.translatable("message.mobstoolforging.toolmaker_assembled"), true);
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult disassembleTool(ItemStack tool, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        List<ItemStack> benchStacks = forge.benchStacks();
        if (benchStacks.size() != 1 || !ToolmakerBenchAssembly.isFinishedTool(benchStacks.get(0))) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.toolmaker_no_tool"), true);
            return ItemInteractionResult.CONSUME;
        }
        ItemStack benchTool = benchStacks.get(0);
        if (Boolean.TRUE.equals(benchTool.get(ModDataComponents.TOOL_BROKEN.get()))) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.toolmaker_broken_tool"), true);
            return ItemInteractionResult.CONSUME;
        }
        List<ItemStack> parts = ToolmakerBenchAssembly.disassemble(benchTool).orElse(List.of());
        if (parts.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.toolmaker_invalid"), true);
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
        player.displayClientMessage(Component.translatable("message.mobstoolforging.toolmaker_disassembled"), true);
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
            player.displayClientMessage(Component.translatable("message.mobstoolforging.inventory_full"), true);
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
            player.displayClientMessage(Component.translatable("message.mobstoolforging.need_materials"), true);
            return ItemInteractionResult.CONSUME;
        }
        ForgeTemplateDefinition template = forge.template();
        int requiredHammerLevel = template == null || forge.materialId() == null ? SmithingHammerLevel.STONE.level() : template.minimumHammerLevel(forge.materialId());
        if (hammerLevel < requiredHammerLevel) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.hammer_too_weak", SmithingHammerLevel.displayName(requiredHammerLevel)), true);
            return ItemInteractionResult.CONSUME;
        }
        if (kind.isSmithingAnvilLike() && MobsToolForgingConfig.REQUIRE_HEATED_METAL.get() && !forge.materialIsForgeReady()) {
            if (template == null || !forge.hasMaterialHeat()) {
                player.displayClientMessage(template == null ? Component.translatable("message.mobstoolforging.metal_needs_heat") : metalNeedsHeatMessage(template), true);
            } else {
                player.displayClientMessage(Component.translatable(
                        "message.mobstoolforging.metal_needs_heat_current",
                        Math.round(forge.materialHeatTemperature() * 100.0F),
                        Math.round(template.minimumTemperature() * 100.0F),
                        Component.translatable("tooltip.mobstoolforging.workpiece_status." + forge.materialHeatStatusKey())
                ), true);
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
                player.displayClientMessage(Component.translatable(completeKey), true);
            }
        }
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult repairTool(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        ItemStack output = forge.repairToolWithPlacedMaterial();
        if (output.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.need_materials"), true);
            return ItemInteractionResult.CONSUME;
        }
        playWorkEffects(stack, level, pos, player);
        player.displayClientMessage(Component.translatable("message.mobstoolforging.tool_repaired"), true);
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult workStationRecipe(ItemStack stack, int hammerLevel, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        StationWorkRecipe recipe = forge.looseWorkRecipe();
        if (recipe == null) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.station_work_missing"), true);
            return ItemInteractionResult.CONSUME;
        }
        if (hammerLevel < recipe.minimumHammerLevel()) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.hammer_too_weak", SmithingHammerLevel.displayName(recipe.minimumHammerLevel())), true);
            return ItemInteractionResult.CONSUME;
        }
        if (forge.hammerLooseWork(recipe)) {
            playWorkEffects(stack, level, pos, player);
            if (forge.isComplete()) {
                player.displayClientMessage(Component.translatable("message.mobstoolforging.station_work_ready"), true);
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
            serverLevel.sendParticles(kind.workParticle(), pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 8, 0.18, 0.05, 0.18, 0.02);
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
        player.displayClientMessage(Component.translatable("message.mobstoolforging.station_cleared"), true);
        return true;
    }

    private static boolean tryCollectOutput(ToolForgeBlockEntity forge, Player player) {
        if (!forge.isComplete()) {
            return false;
        }
        if (player.level().isClientSide) {
            return true;
        }
        ItemStack output = forge.outputStack();
        if (player.getInventory().add(output)) {
            forge.removeOutput();
            player.level().playSound(null, forge.getBlockPos(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
        } else {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.inventory_full"), true);
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
                Math.round(temperature * 100.0F),
                Math.round(minimumTemperature * 100.0F),
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
