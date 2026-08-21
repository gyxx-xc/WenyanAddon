package org.wenyan.wenyan_addon.spell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 法术异步执行器：专用守护线程池，专供法术编译与背包扫描等纯计算任务。
 * 异步线程只做只读快照/编译；结果必须回主线程应用（由调用方通过 server.execute 完成）。
 * 服务器停止时关闭线程池，下次提交自动重建。
 */
public final class SpellAsyncExecutor {
    private static final Logger log = LoggerFactory.getLogger(SpellAsyncExecutor.class);
    private static volatile ExecutorService POOL = createPool();

    private SpellAsyncExecutor() {
    }

    private static ExecutorService createPool() {
        int threadCount = Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() / 2));
        return Executors.newFixedThreadPool(threadCount, runnable -> {
            Thread thread = new Thread(runnable, "Spell-Calculator-" + runnable.hashCode());
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            return thread;
        });
    }

    /**
     * 提交异步任务；任务失败时降级为同步执行（优雅降级）。
     */
    public static <T> void submit(Supplier<T> task, Consumer<T> onResult, Supplier<T> fallback) {
        ExecutorService pool = ensurePool();
        pool.submit(() -> {
            T result;
            try {
                result = task.get();
            } catch (Exception e) {
                log.warn("法术异步任务失败，降级为同步执行", e);
                result = fallback != null ? fallback.get() : null;
            }
            T finalResult = result;
            onResult.accept(finalResult);
        });
    }

    private static ExecutorService ensurePool() {
        ExecutorService pool = POOL;
        if (pool.isShutdown()) {
            synchronized (SpellAsyncExecutor.class) {
                if (POOL.isShutdown()) {
                    POOL = createPool();
                }
                pool = POOL;
            }
        }
        return pool;
    }

    /**
     * 关闭线程池（服务器停止时调用；下次提交自动重建）。
     */
    public static void shutdown() {
        ExecutorService pool = POOL;
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}