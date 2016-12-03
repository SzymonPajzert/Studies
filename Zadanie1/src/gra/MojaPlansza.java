package gra;

import gra.util.Prostokąt;
import gra.util.Współrzędna;

import java.util.HashMap;
import java.util.Map;

public class MojaPlansza implements Plansza {
    public final int wysokość, szerokość;
    private final Graph dependencies;
    private final Map<Postać, Współrzędna> umiejscowienie; // Zwraca lewy górny róg postaci.
    private final Postać[][] plansza;

    MojaPlansza(int wysokość, int szerokość) {
        assert (wysokość > 0 && szerokość > 0);
        this.wysokość = wysokość;
        this.szerokość = szerokość;
        this.dependencies = new Graph(wysokość, szerokość);
        this.umiejscowienie = new HashMap<>();
        this.plansza = new Postać[wysokość][szerokość];
    }

    synchronized private void zajmij(Prostokąt obszar) throws InterruptedException {
        for(Współrzędna współrzędna : obszar) {
            
        }
    }

    @Override
    public void postaw(Postać postać, int wiersz, int kolumna) throws InterruptedException {

    }

    @Override
    public void przesuń(Postać postać, Kierunek kierunek) throws InterruptedException, DeadlockException {

    }

    @Override
    public void usuń(Postać postać) {

    }

    @Override
    public void sprawdź(int wiersz, int kolumna, Akcja jeśliZajęte, Runnable jeśliWolne) {

    }
}
