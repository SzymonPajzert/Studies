package gra.util;

import gra.Kierunek;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Prostokąt implements Iterable<Współrzędna> {
    public final Współrzędna lewyGórny, prawyDolny;

    public Prostokąt(Współrzędna lewyGórny, Współrzędna prawyDolny) {
        this.lewyGórny = lewyGórny;
        this.prawyDolny = prawyDolny;
    }

    public Prostokąt przesuń(Kierunek kierunek) {
        return new Prostokąt(lewyGórny.przesuń(kierunek), prawyDolny.przesuń(kierunek));
    }

    public Prostokąt brzeg(Kierunek kierunek) {
        Prostokąt result;
        switch (kierunek) {
            case GÓRA: result = new Prostokąt(lewyGórny, new Współrzędna(lewyGórny.wysokość, prawyDolny.szerokość)); break;
            case DÓŁ: result = new Prostokąt(new Współrzędna(prawyDolny.szerokość, lewyGórny.szerokość), prawyDolny); break;
            case LEWO: result = new Prostokąt(lewyGórny, new Współrzędna(prawyDolny.wysokość, lewyGórny.szerokość)); break;
            case PRAWO: result = new Prostokąt(new Współrzędna(lewyGórny.wysokość, prawyDolny.szerokość), prawyDolny); break;
            default: throw new IllegalArgumentException();
        }
        return result;
    }

    @Override
    public Iterator<Współrzędna> iterator() {
        return new ProstokątIterator(lewyGórny, prawyDolny);
    }

    /** Iteruje po prostokącie po kolejnych wierszach kolejnych kolumn
     *
     */
    class ProstokątIterator implements Iterator<Współrzędna> {
        private Współrzędna next;
        private final int wysokośćPoczątkowa;
        private final Współrzędna last;

        public ProstokątIterator(Współrzędna first, Współrzędna last) {
            this.next = first;
            this.last = last;
            this.wysokośćPoczątkowa = first.wysokość;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Współrzędna next() throws NoSuchElementException {
            Współrzędna current = next;
            if(current != null) {
                if(current == last) {
                    next = null;
                } else {
                    int wysokość, szerokość;
                    if(current.wysokość == last.wysokość) {
                        wysokość = wysokośćPoczątkowa;
                        szerokość = current.szerokość + 1;
                    } else {
                        wysokość = current.wysokość + 1;
                        szerokość = current.szerokość;
                    }
                    next = new Współrzędna(wysokość, szerokość);
                }
                return current;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
