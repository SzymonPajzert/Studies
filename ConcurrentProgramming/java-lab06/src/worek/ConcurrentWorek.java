package worek;

import java.util.HashMap;
import java.util.Map;

interface Worek<T> {
    void włóż(T wartość);
    void wyjmij(T wartość) throws InterruptedException;
}

public class ConcurrentWorek<T> implements Worek<T> {
    private Map<T, Integer> elements;
    private Map<T, Object> queues;  // Used for pausing threads awaiting for given object.

    public ConcurrentWorek() {
        elements = new HashMap<>();
        queues = new HashMap<>();
    }

    @Override
    public synchronized void włóż(T wartość) {
        Integer eltNumber = elements.getOrDefault(wartość, 0);
        elements.put(wartość, eltNumber + 1);

        queues.putIfAbsent(wartość, new Object());
        queues.get(wartość).notifyAll();
    }

    @Override
    public synchronized void wyjmij(T wartość) throws InterruptedException {
        Integer eltNumber = elements.getOrDefault(wartość, 0);
        queues.putIfAbsent(wartość, new Object());
        while (eltNumber == 0) {
            queues.get(wartość).wait();
            eltNumber = elements.getOrDefault(wartość, 0);
        }

        eltNumber--;
        if(eltNumber == 0) {
            elements.remove(wartość);
        } else {
            elements.put(wartość, eltNumber);
        }
    }
}
