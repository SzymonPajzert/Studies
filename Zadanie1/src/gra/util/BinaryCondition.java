package gra.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class BinaryCondition {
    private final Condition semaphore;
    private boolean waiting;

    public BinaryCondition(Lock lock) {
        this.semaphore = lock.newCondition();
        this.waiting = false;
    }

    public void hang() throws InterruptedException {
        waiting = true;
        while(waiting) {    // Case against spurious wakeup
            semaphore.await();
        }
    }

    public void wake() {
        waiting = false;
        semaphore.signalAll();
    }
}
