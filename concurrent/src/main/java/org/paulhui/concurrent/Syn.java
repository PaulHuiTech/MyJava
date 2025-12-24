package org.paulhui.concurrent;

import org.paulhui.pub.Utils;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 多线程同步的几种方法
 * ReentrantLock
 * synchronized(lock) 同步块
 * synchronized 修饰方法
 */
public class Syn {
    public static void main(String[] args) throws InterruptedException {
        // ReentrantLock方式
        BankAccountSynWithReentrantLock bank1 = new BankAccountSynWithReentrantLock("bank1", new BigDecimal(2000));
        BankAccountSynWithReentrantLock bank2 = new BankAccountSynWithReentrantLock("bank2", new BigDecimal(2000));
        BankTransferSyn bankTransferSyn = new BankTransferSyn(bank1, bank2, 10, new BigDecimal(100));
        Thread t1 = new Thread(bankTransferSyn, "t1");
        Thread t2 = new Thread(bankTransferSyn, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        Utils.print("全部转账结束，账户"+bank1.getAccountNo()+"余额："+bank1.getBalance()
                +"，账户"+bank2.getAccountNo()+"余额："+bank2.getBalance());

        // synchronized(lock) 同步块方式
        BankAccountSynWithSynchronizedLock bank3 = new BankAccountSynWithSynchronizedLock("bank3", new BigDecimal(2000));
        BankAccountSynWithSynchronizedLock bank4 = new BankAccountSynWithSynchronizedLock("bank4", new BigDecimal(2000));
        BankTransferSyn bankTransferSyn2 = new BankTransferSyn(bank3, bank4, 10, new BigDecimal(100));
        Thread t3 = new Thread(bankTransferSyn2, "t3");
        Thread t4 = new Thread(bankTransferSyn2, "t4");
        t3.start();
        t4.start();
        t3.join();
        t4.join();
        Utils.print("全部转账结束，账户"+bank3.getAccountNo()+"余额："+bank3.getBalance()
                +"，账户"+bank4.getAccountNo()+"余额："+bank4.getBalance());

        // synchronized 同步方式
        BankAccountSynWithSynchronized bank5 = new BankAccountSynWithSynchronized("bank5", new BigDecimal(2000));
        BankAccountSynWithSynchronized bank6 = new BankAccountSynWithSynchronized("bank6", new BigDecimal(2000));
        BankTransferSyn bankTransferSyn3 = new BankTransferSyn(bank5, bank6, 10, new BigDecimal(100));
        Thread t5 = new Thread(bankTransferSyn3, "t5");
        Thread t6 = new Thread(bankTransferSyn3, "t6");
        t5.start();
        t6.start();
        t5.join();
        t6.join();
        Utils.print("全部转账结束，账户"+bank5.getAccountNo()+"余额："+bank5.getBalance()
                +"，账户"+bank5.getAccountNo()+"余额："+bank6.getBalance());
    }
}

class BankTransferSyn implements Runnable {
    protected BankAccount bankFrom;
    protected BankAccount bankTo;
    protected int times;
    protected BigDecimal money;

    public BankTransferSyn(BankAccount from, BankAccount to, int times, BigDecimal money) {
        this.bankFrom = from;
        this.bankTo = to;
        this.times = times;
        this.money = money;
    }

    @Override
    public void run() {
        for (int i=0;i<this.times;i++) {
            bankFrom.transfer(bankTo, this.money, i);
        }
    }
}


class BankAccountSynWithReentrantLock extends BankAccount {

    private ReentrantLock lock = new ReentrantLock();

    public BankAccountSynWithReentrantLock(String accountNo, BigDecimal balance) {
        super(accountNo, balance);
    }

    @Override
    public void transfer(BankAccount toAccount, BigDecimal money, int i) {
        if (this.balance.compareTo(money)>=0) {
            try {
                Thread.sleep(20); // 设置延时，更容易触发竟态
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.lock();
            try {
                Utils.print("账户"+this.accountNo+"向账户"+toAccount.getAccountNo()+"转账开始第"+i+"次");
                this.balance = this.balance.subtract(money);
                toAccount.setBalance(toAccount.getBalance().add(money));
                Utils.print("账户"+this.accountNo+"向账户"+toAccount.getAccountNo()+"第"+i+"次转账成功，账户"+this.accountNo+"余额："
                        +this.balance+"，账户"+toAccount.getAccountNo()+"余额："+toAccount.getBalance());
            } finally {
                lock.unlock();
            }
        } else {
            Utils.print("账户"+this.accountNo+"余额不足，无法转账");
        }
    }
}

class BankAccountSynWithSynchronizedLock extends BankAccount {

    private final Object lock = new Object(); // 注意定义为final

    public BankAccountSynWithSynchronizedLock(String accountNo, BigDecimal balance) {
        super(accountNo, balance);
    }

    @Override
    public void transfer(BankAccount toAccount, BigDecimal money, int i) {
        if (this.balance.compareTo(money)>=0) {
            try {
                Thread.sleep(20); // 设置延时，更容易触发竟态
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (lock) {
                Utils.print("账户" + this.accountNo + "向账户" + toAccount.getAccountNo() + "转账开始第" + i + "次");
                this.balance = this.balance.subtract(money);
                toAccount.setBalance(toAccount.getBalance().add(money));
                Utils.print("账户" + this.accountNo + "向账户" + toAccount.getAccountNo() + "第" + i + "次转账成功，账户" + this.accountNo + "余额："
                        + this.balance + "，账户" + toAccount.getAccountNo() + "余额：" + toAccount.getBalance());
            }
        } else {
            Utils.print("账户"+this.accountNo+"余额不足，无法转账");
        }
    }
}

class BankAccountSynWithSynchronized extends BankAccount {

    public BankAccountSynWithSynchronized(String accountNo, BigDecimal balance) {
        super(accountNo, balance);
    }

    @Override
    public synchronized void transfer(BankAccount toAccount, BigDecimal money, int i) {
        if (this.balance.compareTo(money)>=0) {
            try {
                Thread.sleep(20); // 设置延时，更容易触发竟态
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Utils.print("账户" + this.accountNo + "向账户" + toAccount.getAccountNo() + "转账开始第" + i + "次");
            this.balance = this.balance.subtract(money);
            toAccount.setBalance(toAccount.getBalance().add(money));
            Utils.print("账户" + this.accountNo + "向账户" + toAccount.getAccountNo() + "第" + i + "次转账成功，账户" + this.accountNo + "余额："
                    + this.balance + "，账户" + toAccount.getAccountNo() + "余额：" + toAccount.getBalance());
        } else {
            Utils.print("账户"+this.accountNo+"余额不足，无法转账");
        }
    }
}
