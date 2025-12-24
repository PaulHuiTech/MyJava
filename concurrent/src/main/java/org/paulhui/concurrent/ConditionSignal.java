package org.paulhui.concurrent;

import org.paulhui.pub.Utils;

import java.math.BigDecimal;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Condition signal signalAll
 * 类似于wait notify notifyAll
 */
public class ConditionSignal {
    public static void main(String[] args) throws InterruptedException {
        BankAccountSynWithReentrantLockCondition bank1 = new BankAccountSynWithReentrantLockCondition("bank1", new BigDecimal(0));
        BankAccountSynWithReentrantLockCondition bank2 = new BankAccountSynWithReentrantLockCondition("bank2", new BigDecimal(0));
        BankAccountSynWithReentrantLockCondition bank3 = new BankAccountSynWithReentrantLockCondition("bank3", new BigDecimal(1000));
        Thread t1 = new Thread(new BankTransferSyn2(bank1, bank2, 1, new BigDecimal(50)), "t1");
        Thread t2 = new Thread(new BankTransferSyn2(bank1, bank3, 50, new BigDecimal(-10)), "t2");
        t1.start();
        Thread.sleep(500); // 确保t1先启动
        t2.start();
        t1.join();
        t2.join();
        Utils.print("bank1余额："+bank1.getBalance());
        Utils.print("bank2余额："+bank2.getBalance());
        Utils.print("bank3余额："+bank3.getBalance());
    }
}
class BankTransferSyn2 extends BankTransferSyn {

    public BankTransferSyn2(BankAccount from, BankAccount to, int times, BigDecimal money) {
        super(from, to, times, money);
    }

    @Override
    public void run() {
        for (int i=0;i<this.times;i++) {
            ((BankAccountSynWithReentrantLockCondition) bankFrom).transferWaitForSufficientBalance(bankTo, this.money, i);
        }
    }
}
class BankAccountSynWithReentrantLockCondition extends BankAccount {

    private Condition sufficientBalance; //余额充足条件
    private ReentrantLock lock2 = new ReentrantLock(true);
    //为什么要设置公平锁，参照https://chatgpt.com/share/694bf634-907c-8011-a7e2-38e03a84be05
    // 简单来说，如果不设置公平锁，上面t2线程就会一直执行，t1在刚开始进入waiting后，一直到t2全部执行完才能拿到锁执行，只会打印一次账户bank1余额不足，转账进入等待...

    public BankAccountSynWithReentrantLockCondition(String accountNo, BigDecimal balance) {
        super(accountNo, balance);
        sufficientBalance = lock2.newCondition(); //条件需要依赖于锁，一个锁可以有多个条件
    }

    /**
     * 若余额不足，不直接结束，而是等待余额足后转账，中间线程一直挂着
     */
    public void transferWaitForSufficientBalance(BankAccount to, BigDecimal money, int i) {
        lock2.lock();
        try {
            while (this.balance.compareTo(money)<0) {
                Utils.print("账户"+this.accountNo+"余额不足，转账进入等待...");
                sufficientBalance.await();
            }
            transfer(to, money, i);
            Utils.print("账户"+this.accountNo+"完成一次转账，通知所有等待中的转账");
            sufficientBalance.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock2.unlock();
        }
    }
}
