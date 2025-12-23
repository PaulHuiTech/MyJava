package org.paulhui.concurrent;

import org.paulhui.pub.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 线程基础、线程状态、线程属性
 * wait notify join方法
 */
public class ThreadBasic {
    // 创建锁对象
    private static final Object lock = new Object();
    private static final Object lock2 = new Object();
    private static final Object lockMonitor = new Object();
    private static boolean condition = false;

    public static void main(String[] args) throws InterruptedException {
        // 创建线程
        Runnable runnable =  new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    Utils.PrintWithThread("线程"+Thread.currentThread().getName()+"正在运行");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // 检测到线程中断，中止线程
                    }
                    synchronized (lockMonitor) {
                        lockMonitor.notifyAll(); // 当线程状态可能变化时通知给监控线程
                    }
                }
                Utils.PrintWithThread("线程"+Thread.currentThread().getName()+"已释放");
            }
        };
        Thread t1 = new Thread(runnable, "t1");
        Thread t2 = new Thread(runnable, "t2");

        // 等待线程
        Thread t3 = new Thread(()->{
            synchronized (lock2) {
                Utils.PrintWithThread("进入线程"+Thread.currentThread().getName());
                try {
                    while (!condition) {
                        Utils.PrintWithThread("线程"+Thread.currentThread().getName()+"不满足执行条件，进入等待");
                        synchronized (lockMonitor) {
                            lockMonitor.notifyAll(); // 当线程状态可能变化时通知给监控线程
                        }
                        lock2.wait(); // 释放锁
                    }
                    Utils.PrintWithThread("线程"+Thread.currentThread().getName()+"满足执行条件，继续执行");
                    synchronized (lockMonitor) {
                        lockMonitor.notifyAll(); // 当线程状态可能变化时通知给监控线程
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 检测到线程中断，中止线程
                }
            }
        });
        t3.setName("t3");

        // 通知线程
        Thread t4 = new Thread(()->{
            synchronized (lock2) {
                Utils.PrintWithThread("进入线程"+Thread.currentThread().getName());
                Utils.PrintWithThread("开始通知等待线程继续");
                condition = true;
                lock2.notifyAll(); // 不会释放锁
                Utils.PrintWithThread("线程"+Thread.currentThread().getName()+"通知完毕");
                synchronized (lockMonitor) {
                    lockMonitor.notifyAll(); // 当线程状态可能变化时通知给监控线程
                }
            }
        }, "t4");

        // 监控线程（守护线程，其他线程都执行完毕后，本线程也退出）
        Thread t5 = new Thread(()->{
            synchronized (lockMonitor) {
//                while (!(Thread.State.TERMINATED.equals(t1.getState())
//                        && Thread.State.TERMINATED.equals(t2.getState())
//                        && Thread.State.TERMINATED.equals(t3.getState())
//                        && Thread.State.TERMINATED.equals(t4.getState()))) {
                while (true) {
                    System.out.printf("---监控结果，线程t1状态为%s，线程t2状态为%s，线程t3状态为%s，线程t4状态为%s%n",
                            t1.getState(), t2.getState(), t3.getState(), t4.getState());
                    try {
                        lockMonitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
//                Utils.PrintWithThread("守护监控线程执行完毕，退出------");
            }
        }, "t5");
        t5.setDaemon(true); // 设为守护线程

        class JoinTask implements Runnable {
            private String taskName;
            private long duration;

            public JoinTask(String taskName, long duration) {
                this.taskName = taskName;
                this.duration = duration;
            }
            @Override
            public void run() {
                Utils.PrintWithThread("线程"+Thread.currentThread().getName()+"开始运行"+this.taskName+"任务，运行时间"+this.duration);
                try {
                    Thread.sleep(this.duration);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 检测到线程中断，中止线程
                }
                Utils.PrintWithThread("线程"+Thread.currentThread().getName()+"结束运行"+this.taskName+"任务");
            }
        }

        Thread t6 = new Thread(new JoinTask("t6Join", 2000), "t6");
        Thread t7 = new Thread(new JoinTask("t7Join", 5000), "t7");

        t5.start();
        t1.start();
        t3.start();
        Thread.sleep(500); // 确保t1 t3先启动成功
        t2.start();

        t6.start();
        t7.start();
        // 主线程等待线程t6执行完成，再执行后续
        Utils.PrintWithThread("主线程"+Thread.currentThread().getName()+"等待线程t6执行完毕--");
        t6.join();
        Utils.PrintWithThread("线程t6执行完毕，主线程"+Thread.currentThread().getName()+"继续运行");
        t4.start(); // 这样也可以确保t4在t3之后启动

        // 主线程等待线程t7 2000ms，2000ms未执行完则继续主线程
        Utils.PrintWithThread("主线程"+Thread.currentThread().getName()+"等待线程t7执行2秒");
        t7.join(2000);
        Utils.PrintWithThread("已等待线程t7执行2秒，主线程"+Thread.currentThread().getName()+"继续运行");
        if (t7.isAlive()) {
            Utils.PrintWithThread("线程t7仍在运行中，状态为"+t6.getState());
        } else {
            Utils.PrintWithThread("线程t7已提前完成");
        }
    }
    
    /**
     * main输出结果
     ---监控结果，线程t1状态为RUNNABLE，线程t2状态为NEW，线程t3状态为RUNNABLE，线程t4状态为NEW
     [2025-12-23 22:51:48 Thread-t1]线程t1正在运行
     [2025-12-23 22:51:48 Thread-t3]进入线程t3
     [2025-12-23 22:51:48 Thread-t3]线程t3不满足执行条件，进入等待
     ---监控结果，线程t1状态为TIMED_WAITING，线程t2状态为NEW，线程t3状态为WAITING，线程t4状态为NEW
     [2025-12-23 22:51:48 Thread-main]主线程main等待线程t6执行完毕--
     [2025-12-23 22:51:48 Thread-t7]线程t7开始运行t7Join任务，运行时间5000
     [2025-12-23 22:51:48 Thread-t6]线程t6开始运行t6Join任务，运行时间2000
     [2025-12-23 22:51:50 Thread-t6]线程t6结束运行t6Join任务
     [2025-12-23 22:51:50 Thread-main]线程t6执行完毕，主线程main继续运行
     [2025-12-23 22:51:50 Thread-t4]进入线程t4
     [2025-12-23 22:51:50 Thread-t4]开始通知等待线程继续
     [2025-12-23 22:51:50 Thread-main]主线程main等待线程t7执行2秒
     [2025-12-23 22:51:50 Thread-t4]线程t4通知完毕
     ---监控结果，线程t1状态为TIMED_WAITING，线程t2状态为BLOCKED，线程t3状态为BLOCKED，线程t4状态为TERMINATED
     [2025-12-23 22:51:50 Thread-t3]线程t3满足执行条件，继续执行
     ---监控结果，线程t1状态为TIMED_WAITING，线程t2状态为BLOCKED，线程t3状态为TERMINATED，线程t4状态为TERMINATED
     ---监控结果，线程t1状态为RUNNABLE，线程t2状态为BLOCKED，线程t3状态为TERMINATED，线程t4状态为TERMINATED
     [2025-12-23 22:51:51 Thread-t2]线程t2正在运行
     [2025-12-23 22:51:51 Thread-t1]线程t1已释放
     [2025-12-23 22:51:52 Thread-main]已等待线程t7执行2秒，主线程main继续运行
     [2025-12-23 22:51:52 Thread-main]线程t7仍在运行中，状态为TERMINATED
     [2025-12-23 22:51:53 Thread-t7]线程t7结束运行t7Join任务
     ---监控结果，线程t1状态为TERMINATED，线程t2状态为RUNNABLE，线程t3状态为TERMINATED，线程t4状态为TERMINATED
     [2025-12-23 22:51:54 Thread-t2]线程t2已释放
     */
}
