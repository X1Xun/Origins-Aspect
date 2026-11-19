package com.iafenvoy.origins.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

public class WitchSaltBlock extends Block {
    // 1. Создаем редстоуновые свойства сторон, чтобы blockstate их видел!
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
    private static final DustParticleOptions SALT_PARTICLE = new DustParticleOptions(new Vector3f(0.95F, 0.95F, 0.95F), 1.0F);

    public WitchSaltBlock(Properties properties) {
        super(properties);
        // Выставляем дефолтные соединения в false (одиночная точка)
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(EAST, false)
                .setValue(SOUTH, false).setValue(WEST, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos blockBelow = pos.below();
        BlockState stateBelow = level.getBlockState(blockBelow);
        return stateBelow.isFaceSturdy(level, blockBelow, Direction.UP) && !stateBelow.is(this);
    }

    // Рассчитываем соединения при установке блока в мир
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (level.getBlockState(pos).is(this)) return null;

        return this.defaultBlockState()
                .setValue(NORTH, level.getBlockState(pos.north()).is(this))
                .setValue(EAST, level.getBlockState(pos.east()).is(this))
                .setValue(SOUTH, level.getBlockState(pos.south()).is(this))
                .setValue(WEST, level.getBlockState(pos.west()).is(this));
    }

    // Динамически обновляем линии соли, если рядом поставили или сломали другую соль
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }

        // Если обновился горизонтальный сосед — проверяем, соль ли это, и обновляем усы!
        if (direction.getAxis().isHorizontal()) {
            boolean isSalt = neighborState.is(this);
            if (direction == Direction.NORTH) return state.setValue(NORTH, isSalt);
            if (direction == Direction.EAST) return state.setValue(EAST, isSalt);
            if (direction == Direction.SOUTH) return state.setValue(SOUTH, isSalt);
            if (direction == Direction.WEST) return state.setValue(WEST, isSalt);
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (random.nextInt(10) == 0) {
            double x = pos.getX() + 0.5D + (random.nextFloat() - 0.5D) * 0.4D;
            double y = pos.getY() + 0.15D;
            double z = pos.getZ() + 0.5D + (random.nextFloat() - 0.5D) * 0.4D;
            level.addParticle(SALT_PARTICLE, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    // Регистрируем свойства сторон в движок игры
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST);
    }
}
