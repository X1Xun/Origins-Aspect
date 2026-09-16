package com.iafenvoy.origins.content;

import com.iafenvoy.origins.attachment.PowerHelper;
import com.iafenvoy.origins.data.power.builtin.prevent.PreventBlockTriggerPower;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GrapplingHook extends Item {
    private final double maxDistance = 40.0D;
    private double pullSpeed = 1.4D;
    private final int cooldownTicks = 60;


    public GrapplingHook(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(
                    player,
                    entity -> !entity.isSpectator() && entity.isPickable(),
                    this.maxDistance
            );

            var list = PowerHelper.get(player).listActive(PreventBlockTriggerPower.class);
            if (!list.isEmpty()) {
            } else {
                pullSpeed = 1.1D;
            }
            if (hitResult.getType() != HitResult.Type.MISS) {
                player.getCooldowns().addCooldown(this, this.cooldownTicks);

                Vec3 strikePoint = hitResult.getLocation();
                Vec3 playerPos = player.position();
                Vec3 pullVector = strikePoint.subtract(playerPos).normalize();
                double motionX = pullVector.x * this.pullSpeed;
                double motionY = (pullVector.y * this.pullSpeed) + 0.45D;
                double motionZ = pullVector.z * this.pullSpeed;
                player.setDeltaMovement(motionX, motionY, motionZ);
                player.hurtMarked = true;
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        strikePoint.x, strikePoint.y, strikePoint.z,
                        12, 0.1D, 0.1D, 0.1D, 0.1D);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.8F, 1.4F);

                return InteractionResultHolder.success(itemStack);
            }
        }
        return InteractionResultHolder.pass(itemStack);
    }
}
