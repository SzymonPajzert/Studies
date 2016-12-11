package gra.util;

import gra.Postać;

import java.util.concurrent.locks.Lock;

public class AwaitingInstance {
    public final Postać postać;
    public final Prostokąt obszar;

    private final BinaryCondition semaphore;

    public AwaitingInstance(Lock lock, Postać postać, Prostokąt obszar) {
        this.postać = postać;
        this.obszar = obszar;
        this.semaphore = new BinaryCondition(lock);

    }

    public void hang() throws InterruptedException {
        semaphore.hang();
    }

    public void wake() {
        semaphore.wake();
    }
}
