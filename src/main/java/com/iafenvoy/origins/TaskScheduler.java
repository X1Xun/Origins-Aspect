package com.iafenvoy.origins;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class TaskScheduler {
    private static final List<DelayedTask> TASKS = new ArrayList<>();

    // Метод для планирования задачи (20 тиков = 1 секунда)
    public static void schedule(int delayInTicks, Runnable action) {
        synchronized (TASKS) {
            TASKS.add(new DelayedTask(delayInTicks, action));
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel) {
            synchronized (TASKS) {
                TASKS.removeIf(task -> {
                    task.delay--;
                    if (task.delay <= 0) {
                        task.action.run();
                        return true; // Удаляем выполненную задачу
                    }
                    return false;
                });
            }
        }
    }

    private static class DelayedTask {
        int delay;
        Runnable action;

        DelayedTask(int delay, Runnable action) {
            this.delay = delay;
            this.action = action;
        }
    }
}
