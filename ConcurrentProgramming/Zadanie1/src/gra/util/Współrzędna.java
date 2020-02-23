package gra.util;

import gra.Kierunek;

public class Współrzędna {
    public final int wysokość, szerokość;

    public Współrzędna(int wysokość, int szerokość) {
        this.wysokość = wysokość;
        this.szerokość = szerokość;
    }

    public Współrzędna przesuń(Kierunek kierunek) {
        Współrzędna result;
        switch (kierunek) {
            case GÓRA: result = new Współrzędna(this.wysokość - 1, this.szerokość); break;
            case DÓŁ: result = new Współrzędna(this.wysokość + 1, this.szerokość); break;
            case LEWO: result = new Współrzędna(this.wysokość, this.szerokość - 1); break;
            case PRAWO: result = new Współrzędna(this.wysokość, this.szerokość + 1); break;
            default: throw new IllegalArgumentException();
        }
        return result;
    }
}
