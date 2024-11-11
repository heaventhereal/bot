package blade.scheduler;

import blade.Bot;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotScheduler {
    private final Long2ObjectOpenHashMap<ObjectArrayList<BotTask>> scheduled = new Long2ObjectOpenHashMap<>();
    private final ExecutorService asyncExecutor;
    private final Bot bot;
    private long tick = 0L;

    public BotScheduler(Bot bot, ScheduledExecutorService asyncExecutor) {
        this.bot = bot;
        this.asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void schedule(long tickDelay, BotTask task) {
        scheduled.computeIfAbsent(tick + Math.max(1, tickDelay), point -> new ObjectArrayList<>()).add(task);
    }

    public void scheduleAtRate(long tickRate, BotTask task) {
        final BotTask initialTask = task;
        task = new BotTask() {
            @Override
            public void run(Bot bot) {
                initialTask.run(bot);
                schedule(tickRate, this);
            }
        };
        schedule(tickRate, task);
    }

    public void scheduleAsync(long tickDelay, BotTask task) {
        asyncExecutor.execute(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(tickDelay * 50);
                task.run(bot);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void scheduleAtRateAsync(long tickRate, BotTask task) {
        asyncExecutor.execute(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    task.run(bot);
                    TimeUnit.MILLISECONDS.sleep(tickRate * 50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void runAsync(Runnable runnable) {
        asyncExecutor.execute(runnable);
    }

    public void tick() {
        tick++;
        ObjectArrayList<BotTask> tasks = scheduled.get(tick);
        if (tasks != null) {
            for (BotTask task : tasks) {
                task.run(bot);
            }
        }
    }
}
