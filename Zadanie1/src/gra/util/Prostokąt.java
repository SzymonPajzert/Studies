package gra.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Prostokąt implements Iterable<Współrzędna> {
    public final Współrzędna lewyGórny, prawyDolny;

    public Prostokąt(Współrzędna lewyGórny, Współrzędna prawyDolny) {
        this.lewyGórny = lewyGórny;
        this.prawyDolny = prawyDolny;
    }

    @Override
    public Iterator<Współrzędna> iterator() {
        return new ProstokątIterator(lewyGórny, prawyDolny);
    }

    /** Iteruje po prostokącie w taki sposób, że dla dowolnych dwóch
     * prostokątów ich wspólne elementy będą iterowane w tej samej kolejności.
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
    }
}
