package net.mysticapi.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;
import java.util.Queue;

public class TaskChain {

    private final JavaPlugin plugin;
    private final Queue<ChainStep> steps = new LinkedList<>();

    TaskChain(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public TaskChain sync(Runnable task) {
        steps.add(new ChainStep(task, false));
        return this;
    }

    public TaskChain async(Runnable task) {
        steps.add(new ChainStep(task, true));
        return this;
    }

    public void execute() {
        runNext();
    }

    private void runNext() {
        ChainStep step = steps.poll();
        if (step == null) {
            return;
        }

        Runnable wrapped = () -> {
            step.task.run();
            runNext();
        };

        if (step.async) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, wrapped);
        } else {
            Bukkit.getScheduler().runTask(plugin, wrapped);
        }
    }

    private record ChainStep(Runnable task, boolean async) {
    }
}
