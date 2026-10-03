package com.iafenvoy.origins.content;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RegenerationWandItem extends Item {

    public RegenerationWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            RegenCloudEntity cloud = new RegenCloudEntity(level, player.getX(), player.getY(), player.getZ());

            cloud.setOwner(player);
            cloud.setRadius(5.0F);
            cloud.setDuration(200);
            cloud.setWaitTime(0);
            cloud.setRadiusPerTick(0.0F);
            cloud.setParticle(ParticleTypes.TOTEM_OF_UNDYING);
            level.addFreshEntity(cloud);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.2F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 0.5F);
        }

        return InteractionResultHolder.success(itemStack);
    }
}
