package com.iafenvoy.origins.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ChalkSignBlock extends Block {
    // 1. ВАЖНО: Свойство должно быть объявлено в самом верху, ДО конструктора!
    // Это свойство отвечает за 5 видов случайных рун (от 0 до 4)
    public static final IntegerProperty RUNE_TYPE = IntegerProperty.create("rune_type", 0, 4);

    // Плоская форма блока высотой в 1 пиксель, как у ванильного ковра или редстоуна
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public ChalkSignBlock(Properties properties) {
        super(properties);
        // Безопасно регистрируем дефолтное состояние, так как RUNE_TYPE уже создан в памяти
        this.registerDefaultState(this.stateDefinition.any().setValue(RUNE_TYPE, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // МЕХАНИКА: Проверяем, может ли мел существовать в этой точке.
    // Он выживет только НА твердом блоке, и его нельзя поставить на другой мел.
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos blockBelow = pos.below();
        BlockState stateBelow = level.getBlockState(blockBelow);
        return stateBelow.isFaceSturdy(level, blockBelow, Direction.UP) && !stateBelow.is(this);
    }

    // МЕХАНИКА: Запрещаем ставить мел друг на друга, если игрок кликает по уже готовой руне.
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState currentTargetState = context.getLevel().getBlockState(pos);

        if (currentTargetState.is(this)) {
            return null;
        }

        return super.getStateForPlacement(context);
    }

    // МЕХАНИКА: Если блок под мелом сломали, мел мгновенно ломается и превращается в воздух.
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    // Регистрируем свойство блока в движок Майнкрафта
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RUNE_TYPE);
    }
}
