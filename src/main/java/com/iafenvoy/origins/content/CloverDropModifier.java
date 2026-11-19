package com.iafenvoy.origins.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicInteger;

public class CloverDropModifier extends LootModifier {

    public static final MapCodec<CloverDropModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst).and(
                    Codec.FLOAT.fieldOf("bonus_chance").forGetter(m -> m.bonusChance)
            ).apply(inst, CloverDropModifier::new)
    );

    private final float bonusChance;

    public CloverDropModifier(LootItemCondition[] conditionsIn, float bonusChance) {
        super(conditionsIn);
        this.bonusChance = bonusChance;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // Ищем игрока: либо он сломал блок (THIS_ENTITY), либо убил моба (ATTACKING_ENTITY)
        Player player = null;
        Entity thisEntity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        Entity attacker = context.getParamOrNull(LootContextParams.ATTACKING_ENTITY);

        if (attacker instanceof Player p) {
            player = p;
        } else if (thisEntity instanceof Player p) {
            player = p;
        }

        if (player != null) {
            // Считаем, сколько талисманов надето на игроке (вдруг у него 2 или 3 слота!)
            AtomicInteger charmCount = new AtomicInteger(0);
            CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
                charmCount.set(inv.findCurios(ModItems.CLOVER.get()).size());
            });

            if (charmCount.get() > 0) {
                // Итоговый шанс: базовый шанс * количество надетых талисманов
                float totalChance = this.bonusChance * charmCount.get();

                // Проходим по всему выпавшему луту (кроме не стакающихся предметов типа мечей/брони)
                ObjectArrayList<ItemStack> extraLoot = new ObjectArrayList<>();
                for (ItemStack stack : generatedLoot) {
                    if (stack.isStackable() && context.getRandom().nextFloat() < totalChance) {
                        // Добавляем копию выпавшего предмета (удвоение дропа)
                        extraLoot.add(stack.copy());
                    }
                }
                generatedLoot.addAll(extraLoot);
            }
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
