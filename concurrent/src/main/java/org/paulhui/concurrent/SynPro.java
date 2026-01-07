package org.paulhui.concurrent;

import org.paulhui.pub.Utils;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 延伸Syn.java 存在两个问题
 * 1. 没有考虑被转入对象的同步问题，如果两个账户分两个线程一起向第三个账户transfer
 * 2. 余额判断没有加在锁里，会导致出现并发时余额判断失效，出现负余额
 * 3. 由问题1改法引发的死锁问题
 */
public class SynPro {
    public static void main(String[] args) throws InterruptedException {
        // testQ1();
        // testQ2();
        // testQ1Fix();
        // testQ2Fix();
        testQ3();
    }

    /**
     * 没有考虑被转入对象的同步问题，如果两个账户分两个线程一起向第三个账户transfer
     * @throws InterruptedException
     */
    public static void testQ1() throws InterruptedException {
        int i = 1;
        while (true) {
            Utils.print("第"+i+"次运行---------->");
            BankAccountSynWithReentrantLock b1 = new BankAccountSynWithReentrantLock("b1", new BigDecimal(2000));
            BankAccountSynWithReentrantLock b2 = new BankAccountSynWithReentrantLock("b2", new BigDecimal(2000));
            BankAccountSynWithReentrantLock b3 = new BankAccountSynWithReentrantLock("b3", new BigDecimal(0));
            // b1 b2同时并发往b3转钱
            BankTransferSyn bankTransferSyn1 = new BankTransferSyn(b1, b3, 10, new BigDecimal(10));
            BankTransferSyn bankTransferSyn2 = new BankTransferSyn(b2, b3, 10, new BigDecimal(10));
            Thread t1 = new Thread(bankTransferSyn1, "t1");
            Thread t2 = new Thread(bankTransferSyn2, "t2");
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            Utils.print("b1余额："+b1.getBalance());
            Utils.print("b2余额："+b2.getBalance());
            Utils.print("b3余额："+b3.getBalance());
            Utils.print("第"+i+"次运行结束---------->");
            if (b3.getBalance().compareTo(new BigDecimal(200))!=0) {
                break;
            }
            i += 1;
            System.out.println("");
            System.out.println("");
        }
    }

    /**
     * 修复后的Q1问题测试
     * @throws InterruptedException
     */
    public static void testQ1Fix() throws InterruptedException {
        BankAccountSynWithReentrantLockPro b1 = new BankAccountSynWithReentrantLockPro("b1", new BigDecimal(2000));
        BankAccountSynWithReentrantLockPro b2 = new BankAccountSynWithReentrantLockPro("b2", new BigDecimal(2000));
        BankAccountSynWithReentrantLockPro b3 = new BankAccountSynWithReentrantLockPro("b3", new BigDecimal(0));
        // b1 b2同时并发往b3转钱
        BankTransferSyn bankTransferSyn1 = new BankTransferSyn(b1, b3, 10, new BigDecimal(10));
        BankTransferSyn bankTransferSyn2 = new BankTransferSyn(b2, b3, 10, new BigDecimal(10));
        Thread t1 = new Thread(bankTransferSyn1, "t1");
        Thread t2 = new Thread(bankTransferSyn2, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        Utils.print("b1余额："+b1.getBalance());
        Utils.print("b2余额："+b2.getBalance());
        Utils.print("b3余额："+b3.getBalance());
    }

    /**
     * 余额判断没有加在锁里，会导致出现并发时余额判断失效，出现负余额
     */
    public static void testQ2() throws InterruptedException {
        BankAccountSynWithReentrantLock b1 = new BankAccountSynWithReentrantLock("b1", new BigDecimal(100));
        BankAccountSynWithReentrantLock b2 = new BankAccountSynWithReentrantLock("b2", new BigDecimal(2000));
        BankTransferSyn bankTransferSyn = new BankTransferSyn(b1, b2, 1, new BigDecimal(80));
        Thread t1 = new Thread(bankTransferSyn, "t1");
        Thread t2 = new Thread(bankTransferSyn, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        Utils.print("b1余额："+b1.getBalance());
        Utils.print("b2余额："+b2.getBalance());
        /**
         * [2025-12-25 22:21:39 Thread-t2]账户b1向账户b2转账开始第0次
         * [2025-12-25 22:21:39 Thread-t2]账户b1向账户b2第0次转账成功，账户b1余额：20，账户b2余额：2080
         * [2025-12-25 22:21:39 Thread-t1]账户b1向账户b2转账开始第0次
         * [2025-12-25 22:21:39 Thread-t1]账户b1向账户b2第0次转账成功，账户b1余额：-60，账户b2余额：2160
         * [2025-12-25 22:21:39 Thread-main]b1余额：-60
         * [2025-12-25 22:21:39 Thread-main]b2余额：2160
         */
    }

    /**
     * 修复后的Q2问题测试
     * @throws InterruptedException
     */
    public static void testQ2Fix() throws InterruptedException {
        BankAccountSynWithReentrantLockPro b1 = new BankAccountSynWithReentrantLockPro("b1", new BigDecimal(100));
        BankAccountSynWithReentrantLockPro b2 = new BankAccountSynWithReentrantLockPro("b2", new BigDecimal(2000));
        BankTransferSyn bankTransferSyn = new BankTransferSyn(b1, b2, 1, new BigDecimal(80));
        Thread t1 = new Thread(bankTransferSyn, "t1");
        Thread t2 = new Thread(bankTransferSyn, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        Utils.print("b1余额："+b1.getBalance());
        Utils.print("b2余额："+b2.getBalance());
        /**
         * [2025-12-25 22:27:48 Thread-t1]账户b1向账户b2转账开始第0次
         * [2025-12-25 22:27:48 Thread-t1]账户b1向账户b2第0次转账成功，账户b1余额：20，账户b2余额：2080
         * [2025-12-25 22:27:48 Thread-t2]账户b1余额不足，无法转账
         * [2025-12-25 22:27:48 Thread-main]b1余额：20
         * [2025-12-25 22:27:48 Thread-main]b2余额：2080
         */
    }

    public static void testQ3() throws InterruptedException {
        BankAccountSynWithReentrantLockPro b1 = new BankAccountSynWithReentrantLockPro("b1", new BigDecimal(1000));
        BankAccountSynWithReentrantLockPro b2 = new BankAccountSynWithReentrantLockPro("b2", new BigDecimal(2000));
        // b1往b2转账，同时b2往b1转账
        BankTransferSyn bankTransferSyn1 = new BankTransferSyn(b1, b2, 5, new BigDecimal(80));
        BankTransferSyn bankTransferSyn2 = new BankTransferSyn(b2, b1, 5, new BigDecimal(80));
        Thread t1 = new Thread(bankTransferSyn1, "t1");
        Thread t2 = new Thread(bankTransferSyn2, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}

/**
 * 终极线程安全，其实并不是，这个写法会有死锁问题
 */
class BankAccountSynWithReentrantLockPro extends BankAccount {

    private ReentrantLock lock = new ReentrantLock();

    public BankAccountSynWithReentrantLockPro(String accountNo, BigDecimal balance) {
        super(accountNo, balance);
    }

    @Override
    public void transfer(BankAccount toAccount, BigDecimal money, int i) {
        lock.lock();
        ((BankAccountSynWithReentrantLockPro) toAccount).lock.lock();
        try {
            if (this.balance.compareTo(money) >= 0) {
                try {
                    Thread.sleep(20); // 设置延时，更容易触发竟态
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Utils.print("账户" + this.accountNo + "向账户" + toAccount.getAccountNo() + "转账开始第" + i + "次");
                this.balance = this.balance.subtract(money);
                try {
                    Thread.sleep(100); // 设置延时，更容易触发竟态
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                toAccount.setBalance(toAccount.getBalance().add(money));
                Utils.print("账户" + this.accountNo + "向账户" + toAccount.getAccountNo() + "第" + i + "次转账成功，账户" + this.accountNo + "余额："
                        + this.balance + "，账户" + toAccount.getAccountNo() + "余额：" + toAccount.getBalance());
            } else {
                Utils.print("账户" + this.accountNo + "余额不足，无法转账");
            }
        } finally {
            ((BankAccountSynWithReentrantLockPro) toAccount).lock.unlock();
            lock.unlock();
        }
    }
}

/**
 * 终极线程安全，不存在死锁问题
 * BankAccountSynWithReentrantLockPro类的transfer方法，如果是A->B，同时并发B->A，就会引发死锁
 * A.transfer执行时，this.lock锁住，toAccount.lock被B占用，A等待
 * B.transfer执行时，this.lock锁住，toAccount.lock被A占用，B等待
 * 产生死锁
 * BankAccountSynWithReentrantLockProMax通过限定加锁顺序，解决问题
 */
class BankAccountSynWithReentrantLockProMax extends BankAccount {

    private ReentrantLock lock = new ReentrantLock();

    public BankAccountSynWithReentrantLockProMax(String accountNo, BigDecimal balance) {
        super(accountNo, balance);
    }

    @Override
    public void transfer(BankAccount toAccount, BigDecimal money, int i) {
        // 对锁进行排序
        BankAccountSynWithReentrantLockProMax from = this;
        BankAccountSynWithReentrantLockProMax to = (BankAccountSynWithReentrantLockProMax) toAccount;
        BankAccountSynWithReentrantLockProMax first;
        BankAccountSynWithReentrantLockProMax second;
        int compare = from.getAccountNo().compareTo(toAccount.accountNo);
        if (compare>0) {
            first = from;
            second = to;
        } else if (compare<0) {
            first = to;
            second = from;
        } else {
            compare = System.identityHashCode(from)-System.identityHashCode(to);
            if (compare>0) {
                first = from;
                second = to;
            } else {
                first = to;
                second = from;
            }
        }
        first.lock.lock();
        second.lock.lock();
        try {
            if (this.balance.compareTo(money) >= 0) {
                try {
                    Thread.sleep(20); // 设置延时，更容易触发竟态
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Utils.print("账户" + this.accountNo + "向账户" + toAccount.getAccountNo() + "转账开始第" + i + "次");
                this.balance = this.balance.subtract(money);
                try {
                    Thread.sleep(10); // 设置延时，更容易触发竟态
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                toAccount.setBalance(toAccount.getBalance().add(money));
                Utils.print("账户" + this.accountNo + "向账户" + toAccount.getAccountNo() + "第" + i + "次转账成功，账户" + this.accountNo + "余额："
                        + this.balance + "，账户" + toAccount.getAccountNo() + "余额：" + toAccount.getBalance());
            } else {
                Utils.print("账户" + this.accountNo + "余额不足，无法转账");
            }
        } finally {
            second.lock.unlock();
            first.lock.unlock();
        }
    }
}
