package com.devfat.mini_ecommerce;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyTestHelper {
    public record ConcurrencyResult(int successCount, List<Exception> exceptions) {}

    public static ConcurrencyResult runConcurrently(int threadCount, Runnable action) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // Khởi tạo 3 chốt chặn theo chuẩn
        CountDownLatch readyLatch = new CountDownLatch(threadCount); // Đợi mọi người vào vị trí
        CountDownLatch startLatch = new CountDownLatch(1);           // Súng lệnh
        CountDownLatch doneLatch = new CountDownLatch(threadCount);  // Đợi mọi người về đích

        AtomicInteger successCount = new AtomicInteger(0);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();   // List thread-safe

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown(); // Báo cáo: "Tôi đã sẵn sàng"
                    startLatch.await();     // Đứng chờ tín hiệu xuất phát

                    action.run();           // Hành động thật (VD: decreaseStock)
                    successCount.incrementAndGet(); // Lưu kết quả thành công
                } catch (Exception e) {
                    exceptions.add(e);      // Lưu lỗi ném ra vào list
                } finally {
                    doneLatch.countDown();  // Báo cáo: "Tôi xong việc"
                }
            });
        }

        readyLatch.await();      // Main thread chờ TẤT CẢ thread đứng ở vạch xuất phát
        startLatch.countDown();  // Bắn súng lệnh! Cả N thread cùng lao vào GẦN NHƯ đồng thời
        doneLatch.await();       // Chờ tất cả xong việc
        executor.shutdown();     // Dọn dẹp luồng

        return new ConcurrencyResult(successCount.get(), exceptions);
    }
}
