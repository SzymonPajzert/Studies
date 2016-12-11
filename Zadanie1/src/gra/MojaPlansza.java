package gra;

import gra.util.AwaitingInstance;
import gra.util.BinaryCondition;
import gra.util.Prostokąt;
import gra.util.Współrzędna;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MojaPlansza implements Plansza {
    public final int wysokość, szerokość;
    private final Graph dependencies;
    private final Map<Postać, Współrzędna> umiejscowienie; // Zwraca lewy górny róg postaci.
    private final Postać[][] zajęte;
    private final BinaryCondition[][] nieruszalne;
    private final LinkedHashMap<Postać, AwaitingInstance> awaitingInstances;
    private final Lock protection;
    private final Semaphore awaitingWakerSemaphore;

    MojaPlansza(int wysokość, int szerokość) {
        assert (wysokość > 0 && szerokość > 0);
        this.wysokość = wysokość;
        this.szerokość = szerokość;
        this.dependencies = new Graph();
        this.umiejscowienie = new HashMap<>();

        this.zajęte = new Postać[wysokość][szerokość];
        this.nieruszalne = new BinaryCondition[wysokość][szerokość];

        this.awaitingInstances = new LinkedHashMap<>();
        this.protection = new ReentrantLock();
        this.awaitingWakerSemaphore = new Semaphore(0);
    }

    private boolean wolne(Prostokąt obszar) {
        boolean result = true;
        for(Współrzędna współrzędna : obszar) {
            result = result && (zajęte[współrzędna.wysokość][współrzędna.szerokość] == null);
        }
        return result;
    }

    private List<BinaryCondition> nieruszalne(Prostokąt obszar) {
        List<BinaryCondition> result = new LinkedList<>();
        for(Współrzędna współrzędna : obszar) {
            if(nieruszalne[współrzędna.wysokość][współrzędna.szerokość] != null) {
                result.add(nieruszalne[współrzędna.wysokość][współrzędna.szerokość]);
            }
        }
        return result;
    }

    private void zajmij(Prostokąt obszar, Postać postać) throws InterruptedException {
        for(Współrzędna współrzędna : obszar) {
            zajęte[współrzędna.wysokość][współrzędna.szerokość] = postać;
        }
    }

    private void zwolnij(Prostokąt obszar) {
        for(Współrzędna współrzędna : obszar) {
            zajęte[współrzędna.wysokość][współrzędna.szerokość] = null;
        }
    }

    private void checkAwaiting() {
        Iterable<AwaitingInstance> instances = awaitingInstances.values();
       for(AwaitingInstance current : instances) {
            if (wolne(current.obszar)) {
                current.wake();
                try {
                    awaitingWakerSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                awaitingInstances.remove(current.postać);
            }
        }
    }

    private void addToAwaiting(Postać postać, Prostokąt obszar) throws InterruptedException {
        AwaitingInstance instance = new AwaitingInstance(protection, postać, obszar);
        awaitingInstances.put(postać, instance);
        instance.hang();
    }

    private Prostokąt obszar(Postać postać, int wiersz, int kolumna) {
        if(wiersz < 0 || kolumna < 0 || wiersz + postać.dajWysokość() >= wysokość || kolumna + postać.dajSzerokość() >= szerokość) {
            throw new IllegalArgumentException();
        }
        Współrzędna lewyGórny = new Współrzędna(wiersz, kolumna);
        Współrzędna prawyDolny = new Współrzędna(wiersz + postać.dajWysokość() - 1, kolumna + postać.dajSzerokość() - 1);
        return new Prostokąt(lewyGórny, prawyDolny);
    }

    private Kierunek odwrotnyKierunek(Kierunek kierunek) {
        switch (kierunek) {
            case GÓRA: return Kierunek.DÓŁ;
            case DÓŁ: return Kierunek.GÓRA;
            case LEWO: return Kierunek.PRAWO;
            case PRAWO: return Kierunek.LEWO;
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public void postaw(Postać postać, int wiersz, int kolumna) throws InterruptedException {
        Prostokąt obszar = obszar(postać, wiersz, kolumna);
        boolean unlockProtection = true;

        protection.lock();
        if(!wolne(obszar)) {
            addToAwaiting(postać, obszar);
            unlockProtection = false;
        }

        zajmij(obszar, postać);
        umiejscowienie.put(postać, new Współrzędna(wiersz, kolumna));

        if(unlockProtection)
            protection.unlock();
        else
            awaitingWakerSemaphore.release();
    }

    @Override
    synchronized public void przesuń(Postać postać, Kierunek kierunek) throws InterruptedException, DeadlockException {
        protection.lock();

        Współrzędna lewyGórny = umiejscowienie.get(postać);
        if(lewyGórny == null) {
            throw new IllegalArgumentException();
        }

        Współrzędna nowyLewyGórny = lewyGórny.przesuń(kierunek);

        Prostokąt zajętyObszar = obszar(postać, lewyGórny.wysokość, lewyGórny.szerokość);

        // Oblicza prostokąt by w razie co wyrzucić wyjątkiem, jeśli pole nie może leżeć na planszy.
        obszar(postać, nowyLewyGórny.wysokość, nowyLewyGórny.szerokość);

        Prostokąt brzeg = zajętyObszar.brzeg(kierunek);
        Prostokąt noweMiejsce = brzeg.przesuń(kierunek);
        Prostokąt stareMiejsce = zajętyObszar.brzeg(odwrotnyKierunek(kierunek));

        List<BinaryCondition> nieruszalne = nieruszalne(zajętyObszar);
        do {
            for(BinaryCondition condition : nieruszalne) {
                condition.hang();
            }

            boolean unlockProtection = true;
            if(!wolne(noweMiejsce)) {
                addToAwaiting(postać, noweMiejsce);
                unlockProtection = false;
            }

            zwolnij(stareMiejsce);
            zajmij(noweMiejsce, postać);
            umiejscowienie.put(postać, nowyLewyGórny);

            if(unlockProtection)
                protection.unlock();
            else
                awaitingWakerSemaphore.release();


        } while (!nieruszalne.isEmpty() || !wolne(noweMiejsce));

        if(wolne(noweMiejsce)) {

        }

        if(wolne(noweMiejsce) && nieruszalne.isEmpty()) {
            zwolnij(stareMiejsce);
            zajmij(noweMiejsce, postać);
            umiejscowienie.put(postać, nowyLewyGórny);
        } else {
            for(BinaryCondition condition : nieruszalne) {
                condition.hang();
            }
        }

        checkAwaiting();
        protection.unlock();
    }

    @Override
    synchronized public void usuń(Postać postać) {
        protection.lock();
        try {
            Współrzędna lewyGórny = umiejscowienie.get(postać);
            Współrzędna prawyDolny = new Współrzędna(lewyGórny.wysokość + postać.dajWysokość(), lewyGórny.szerokość + postać.dajSzerokość());
            zwolnij(new Prostokąt(lewyGórny, prawyDolny));
            umiejscowienie.remove(postać);
            checkAwaiting();
        } finally {
            protection.unlock();
        }

    }

    @Override
    synchronized public void sprawdź(int wiersz, int kolumna, final Akcja jeśliZajęte, Runnable jeśliWolne) {
        protection.lock();
        final Postać zajmująca = zajęte[wiersz][kolumna];
        nieruszalne[wiersz][kolumna] = new BinaryCondition(protection);
        protection.unlock();

        Runnable operation = zajmująca == null ? jeśliWolne : new Runnable() {
            @Override
            public void run() {
                jeśliZajęte.wykonaj(zajmująca);
            }
        };

        Thread executor = new Thread(operation);
        executor.start();
        try {
            executor.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        protection.lock();
        nieruszalne[wiersz][kolumna].wake();
        nieruszalne[wiersz][kolumna] = null;
        checkAwaiting();
        protection.unlock();

    }
}
