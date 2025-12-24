package org.paulhui.concurrent;

import org.paulhui.pub.Utils;

import java.math.BigDecimal;

/**
 * 竞态条件 Race Condition
 */
public class RaceCondition {
    public static void main(String[] args) throws InterruptedException {
        // 不涉及多线程并发的转账
        BankAccount bank1 = new BankAccount("bank1", new BigDecimal(2000));
        BankAccount bank2 = new BankAccount("bank2", new BigDecimal(2000));
        for (int i=0;i<10;i++) {
            bank1.transfer(bank2, new BigDecimal(100), i);
        }
        Utils.print("全部转账结束，账户"+bank1.getAccountNo()+"余额："+bank1.getBalance()
                +"，账户"+bank2.getAccountNo()+"余额："+bank2.getBalance());

        // 多线程并发转账
        BankAccount bank3 = new BankAccount("bank3", new BigDecimal(2000));
        BankAccount bank4 = new BankAccount("bank4", new BigDecimal(2000));
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (int i=0;i<10;i++) {
                    bank3.transfer(bank4, new BigDecimal(100), i); // 这里其实隐式的把bank3 bank4作为了final变量 引用对象变量作为final不能再指向另外的变量，但可以修改其本身的属性值
                }
            }
        };
        Thread t1 = new Thread(runnable, "t1");
        Thread t2 = new Thread(runnable, "t2");

        t1.start();
        t2.start();

        t1.join();
        t2.join(); // 等待t1 t2全部执行完毕

        Utils.print("全部转账结束，账户"+bank3.getAccountNo()+"余额："+bank3.getBalance()
                +"，账户"+bank4.getAccountNo()+"余额："+bank4.getBalance());
    }
    /**
     * [2025-12-24 20:18:42 Thread-t1]账户bank3向账户bank4转账开始第0次
     * [2025-12-24 20:18:42 Thread-t2]账户bank3向账户bank4转账开始第0次
     * [2025-12-24 20:18:42 Thread-t2]账户bank3向账户bank4第0次转账成功，账户bank3余额：1900，账户bank4余额：2100
     * [2025-12-24 20:18:42 Thread-t1]账户bank3向账户bank4第0次转账成功，账户bank3余额：1900，账户bank4余额：2100
     * [2025-12-24 20:18:42 Thread-t2]账户bank3向账户bank4转账开始第1次
     * [2025-12-24 20:18:42 Thread-t1]账户bank3向账户bank4转账开始第1次
     * [2025-12-24 20:18:42 Thread-t2]账户bank3向账户bank4第1次转账成功，账户bank3余额：1800，账户bank4余额：2200
     * [2025-12-24 20:18:42 Thread-t1]账户bank3向账户bank4第1次转账成功，账户bank3余额：1700，账户bank4余额：2300
     * [2025-12-24 20:18:42 Thread-t2]账户bank3向账户bank4转账开始第2次
     * [2025-12-24 20:18:42 Thread-t1]账户bank3向账户bank4转账开始第2次
     * [2025-12-24 20:18:42 Thread-t2]账户bank3向账户bank4第2次转账成功，账户bank3余额：1600，账户bank4余额：2400
     * [2025-12-24 20:18:42 Thread-t1]账户bank3向账户bank4第2次转账成功，账户bank3余额：1600，账户bank4余额：2500
     * [2025-12-24 20:18:42 Thread-t2]账户bank3向账户bank4转账开始第3次
     * [2025-12-24 20:18:42 Thread-t1]账户bank3向账户bank4转账开始第3次
     * [2025-12-24 20:18:42 Thread-t1]账户bank3向账户bank4第3次转账成功，账户bank3余额：1500，账户bank4余额：2600
     * [2025-12-24 20:18:42 Thread-t2]账户bank3向账户bank4第3次转账成功，账户bank3余额：1400，账户bank4余额：2700
     * [2025-12-24 20:18:42 Thread-t1]账户bank3向账户bank4转账开始第4次
     * [2025-12-24 20:18:42 Thread-t2]账户bank3向账户bank4转账开始第4次
     * [2025-12-24 20:18:42 Thread-t1]账户bank3向账户bank4第4次转账成功，账户bank3余额：1200，账户bank4余额：2900
     * [2025-12-24 20:18:42 Thread-t2]账户bank3向账户bank4第4次转账成功，账户bank3余额：1300，账户bank4余额：2800
     * [2025-12-24 20:18:42 Thread-t1]账户bank3向账户bank4转账开始第5次
     * [2025-12-24 20:18:42 Thread-t2]账户bank3向账户bank4转账开始第5次
     * [2025-12-24 20:18:43 Thread-t1]账户bank3向账户bank4第5次转账成功，账户bank3余额：1000，账户bank4余额：3100
     * [2025-12-24 20:18:43 Thread-t2]账户bank3向账户bank4第5次转账成功，账户bank3余额：1100，账户bank4余额：3000
     * [2025-12-24 20:18:43 Thread-t1]账户bank3向账户bank4转账开始第6次
     * [2025-12-24 20:18:43 Thread-t2]账户bank3向账户bank4转账开始第6次
     * [2025-12-24 20:18:43 Thread-t2]账户bank3向账户bank4第6次转账成功，账户bank3余额：800，账户bank4余额：3300
     * [2025-12-24 20:18:43 Thread-t1]账户bank3向账户bank4第6次转账成功，账户bank3余额：900，账户bank4余额：3200
     * [2025-12-24 20:18:43 Thread-t2]账户bank3向账户bank4转账开始第7次
     * [2025-12-24 20:18:43 Thread-t1]账户bank3向账户bank4转账开始第7次
     * [2025-12-24 20:18:43 Thread-t2]账户bank3向账户bank4第7次转账成功，账户bank3余额：700，账户bank4余额：3400
     * [2025-12-24 20:18:43 Thread-t2]账户bank3向账户bank4转账开始第8次
     * [2025-12-24 20:18:43 Thread-t1]账户bank3向账户bank4第7次转账成功，账户bank3余额：700，账户bank4余额：3400
     * [2025-12-24 20:18:43 Thread-t1]账户bank3向账户bank4转账开始第8次
     * [2025-12-24 20:18:43 Thread-t2]账户bank3向账户bank4第8次转账成功，账户bank3余额：600，账户bank4余额：3500
     * [2025-12-24 20:18:43 Thread-t2]账户bank3向账户bank4转账开始第9次
     * [2025-12-24 20:18:43 Thread-t1]账户bank3向账户bank4第8次转账成功，账户bank3余额：600，账户bank4余额：3500
     * [2025-12-24 20:18:43 Thread-t1]账户bank3向账户bank4转账开始第9次
     * [2025-12-24 20:18:43 Thread-t2]账户bank3向账户bank4第9次转账成功，账户bank3余额：500，账户bank4余额：3600
     * [2025-12-24 20:18:43 Thread-t1]账户bank3向账户bank4第9次转账成功，账户bank3余额：500，账户bank4余额：3600
     * [2025-12-24 20:18:43 Thread-main]全部转账结束，账户bank3余额：500，账户bank4余额：3600
     */
}

class BankAccount {
    protected String accountNo; //protected用于继承
    protected BigDecimal balance;

    public BankAccount(String accountNo, BigDecimal balance) {
        this.accountNo = accountNo;
        this.balance = balance;
    }

    public String getAccountNo() {
        return this.accountNo;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void transfer(BankAccount toAccount, BigDecimal money, int i) {
        if (this.balance.compareTo(money)>=0) {
            Utils.print("账户"+this.accountNo+"向账户"+toAccount.getAccountNo()+"转账开始第"+i+"次");
            try {
                Thread.sleep(20); // 设置延时，更容易触发竟态
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.balance = this.balance.subtract(money);
            toAccount.setBalance(toAccount.getBalance().add(money));
            Utils.print("账户"+this.accountNo+"向账户"+toAccount.getAccountNo()+"第"+i+"次转账成功，账户"+this.accountNo+"余额："
                    +this.balance+"，账户"+toAccount.getAccountNo()+"余额："+toAccount.getBalance());
        } else {
            Utils.print("账户"+this.accountNo+"余额不足，无法转账");
        }
    }
}
