package org.wenyan.wenyan_addon.qi.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 灵气异步计算执行器：专用线程池 + 统一降级。
 * 异步线程只做纯计算（读快照）；结果应用必须回主线程（由调用方执行）。
 * 计算任务失败时降级为同步执行（优雅降级）。
 * 服务器重启（回到标题再进世界）时线程池自动重建。
 */
public final class QiAsyncExecutor {
    private static final Logger log = LoggerFactory.getLogger(QiAsyncExecutor.class);
    private static volatile ExecutorService POOL = createPool();

    private QiAsyncExecutor() {
    }

    private static ExecutorService createPool() {
        int threadCount = Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() / 2));
        return Executors.newFixedThreadPool(threadCount, runnable -> {
            Thread thread = new Thread(runnable, "Qi-Calculator-" + runnable.hashCode());
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            return thread;
        });
    }

    /**
     * 提交异步计算任务（线程池执行，快照只读）。
     * 若线程池已被 {@link #shutdown()} 终止（服务器停止），自动重建后再提交。
     */
    public static <T> void submit(Supplier<T> task, Consumer<T> onResult, Supplier<T> fallback) {
        ExecutorService pool = ensurePool();
        pool.submit(() -> {
            T result;
            try {
                result = task.get();
            } catch (Exception e) {
                log.warn("灵气异步计算失败，降级为同步", e);
                result = fallback != null ? fallback.get() : null;
            }
            T finalResult = result;
            onResult.accept(finalResult);
        });
    }

    private static ExecutorService ensurePool() {
        ExecutorService pool = POOL;
        if (pool.isShutdown()) {
            synchronized (QiAsyncExecutor.class) {
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
