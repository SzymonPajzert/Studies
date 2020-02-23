package zadania;

import java.util.concurrent.*;

public class Zadanie1 {

    private static class Macierz {
        static int wartość(final int w, final int k) {
            try {
                Thread.sleep(1000); // It takes really long to compute the values.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            final int a = 2 * k + 1;
            return (w + 1) * (a % 4 - 2) * a;
        }

    }

    private static class RowAdder implements Runnable {
        public void run() {
            for(int row = 0; row< N_WIERSZY; row++) {
                results.putIfAbsent(row, new LinkedBlockingQueue<>());
                int result = 0;
                try {
                    for(int col=0; col<N_KOLUMN; col++) {
                        result += results.get(row).take().get();
                    }
                    results.remove(row);
                    System.out.println("Value in row " + row + " equals: " + result);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     Column worker just uses callable and submits results to the adder.
     */
    private static class ColumnWorker implements Runnable {
        private final int columnNumber;

        ColumnWorker(int columnNumber) {
            this.columnNumber = columnNumber;
        }

        public void run() {
            for(int row=0; row<N_WIERSZY; row++) {
                results.putIfAbsent(row, new LinkedBlockingQueue<>());
                try {
                    results.get(row).put(executors.submit(new MatrixCalculator(row, columnNumber)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class MatrixCalculator implements Callable<Integer> {
        private final int row;
        private final int col;

        private MatrixCalculator(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public Integer call() {
            int value = Macierz.wartość(row, col);
            System.out.println("\tCalculated in (" + row + "," + col + ") = " + value);
            return value;
        }
    }

    private static final int N_WIERSZY = 10;
    private static final int N_KOLUMN = 100;
    private static final int N_LICZACZY = 4; // Number of threads calculating values in matrix.

    private static final ExecutorService executors;

    private static final ConcurrentMap<Integer, BlockingQueue<Future<Integer>>> results;

    static {
        results = new ConcurrentHashMap<>();
        executors = Executors.newFixedThreadPool(N_LICZACZY);
    }


    public static void main(final String[] args) throws InterruptedException {
        Thread adder = new Thread(new RowAdder());
        adder.start();

        Thread[] workers = new Thread[N_KOLUMN];
        for(int i=0; i<N_KOLUMN; i++) {
            workers[i] = new Thread(new ColumnWorker(i));
            workers[i].start();
        }

        for(int i=0; i<N_KOLUMN; i++) {
            workers[i].join();
        }
        adder.join();
    }

}
