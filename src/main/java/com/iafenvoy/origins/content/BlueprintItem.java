package com.iafenvoy.origins.content;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BlueprintItem extends Item {
    private final String targetItemPath;

    public BlueprintItem(Properties properties, String targetItemPath) {
        super(properties);
        this.targetItemPath = targetItemPath;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            String tagToId = "craft_" + this.targetItemPath;
            if (serverPlayer.getTags().contains(tagToId)) {
                serverPlayer.sendSystemMessage(Component.literal("§eВы уже изучили эту схему ранее!"));
                return InteractionResultHolder.fail(itemStack);
            }
            serverPlayer.addTag(tagToId);
            ResourceLocation recipeLocation = ResourceLocation.fromNamespaceAndPath("origins", this.targetItemPath);
            java.util.Optional<net.minecraft.world.item.crafting.RecipeHolder<?>> recipeHolder =
                    serverPlayer.server.getRecipeManager().byKey(recipeLocation);

            if (recipeHolder.isPresent()) {
                serverPlayer.awardRecipes(java.util.Collections.singleton(recipeHolder.get()));
            } else {
                System.out.println("Рецепт не найден в файлах игры: " + recipeLocation);
            }

            serverPlayer.sendSystemMessage(Component.literal("§aВы успешно изучили схему предмета: §6" + this.targetItemPath));
            level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.0F);
            if (!serverPlayer.isCreative()) {
                itemStack.shrink(1);
            }
            return InteractionResultHolder.consume(itemStack);
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}
