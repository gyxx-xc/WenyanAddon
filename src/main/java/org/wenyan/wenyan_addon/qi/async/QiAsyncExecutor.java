package org.wenyan.wenyan_addon.qi.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 灵气异步计算执行器：专用线程池 + 统一降级。
 * 异步线程只做纯计算（读快照）；结果应用必须回主线程（由调用方执行）。
 * 计算任务失败时降级为同步执行（优雅降级）。
 */
public final class QiAsyncExecutor {
    private static final Logger log = LoggerFactory.getLogger(QiAsyncExecutor.class);
    private static final int THREAD_COUNT = Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() / 2));
    private static final ExecutorService POOL = Executors.newFixedThreadPool(THREAD_COUNT, runnable -> {
        Thread thread = new Thread(runnable, "Qi-Calculator-" + runnable.hashCode());
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        return thread;
    });

    private QiAsyncExecutor() {
    }

    /**
     * 提交异步计算任务（线程池执行，快照只读）。
     */
    public static <T> void submit(Supplier<T> task, java.util.function.Consumer<T> onResult,
                                  Supplier<T> fallback) {
        POOL.submit(() -> {
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

    /**
     * 关闭线程池（服务器停止时调用）。
     */
    public static void shutdown() {
        POOL.shutdown();
        try {
            if (!POOL.awaitTermination(5, TimeUnit.SECONDS)) {
                POOL.shutdownNow();
            }
        } catch (InterruptedException e) {
            POOL.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
