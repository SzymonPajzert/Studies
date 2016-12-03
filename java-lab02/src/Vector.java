import java.util.concurrent.atomic.AtomicInteger;

class VectorAdder implements Runnable {
    private final int[] arg1;
    private final int[] arg2;
    private final int[] result;
    private final int n;

    VectorAdder(int[] arg1, int[] arg2, int[] result, int n) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
        this.n = n;
    }

    @Override
    public void run() {
        System.out.println("VectorAdder " + n + " adding " + arg1[n] + " " + arg2[n]);
        result[n] = arg1[n] + arg2[n];
    }
}

class VectorMultiplicationIncrementer implements Runnable {
    private final int[] arg1;
    private final int[] arg2;
    private AtomicInteger result;
    private final int n;

    VectorMultiplicationIncrementer(int[] arg1, int[] arg2, AtomicInteger result, int n) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
        this.n = n;
    }

    public void run() {
        System.out.println("VectorMultiplicationIncrementer " + n + " multiplicates " + arg1[n] + " " + arg2[n]);
        result.addAndGet(arg1[n] * arg2[n]);
    }
}

public class Vector {
    private int[] list;

    public Vector(int n) {
        this.list = new int[n];
    }

    public Vector(int n, int arg[]) {
        assert(n == arg.length);
        this.list = arg;
    }

    public Vector add(Vector that) {
        assert (this.list.length == that.list.length);
        int n = this.list.length;

        int res[] = new int[n];
        Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            threads[i] = new Thread(new VectorAdder(list, that.list, res, i));
            threads[i].start();
        }

        // Czekamy na zakończenie procesów.
        for (int i = 0; i < n; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return new Vector(n, res);
    }


    public int dotProduct(Vector that) {
        assert(this.list.length == that.list.length);
        int n = this.list.length;

        AtomicInteger res = new AtomicInteger(0);
        Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            threads[i] = new Thread(new VectorMultiplicationIncrementer(list, that.list, res, i));
            threads[i].start();
        }

        // Czekamy na zakończenie procesów.
        for (int i = 0; i < n; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return res.get();
    }

    @Override
    public String toString() {
        String result = "[";
        for (int value : this.list) {
            result = result.concat(Integer.toString(value) + "; ");
        }
        result = result.substring(0, result.length() - 2);
        result = result.concat("]");

        return result;

    }

    public static void main(String[] args) {

        Vector a = new Vector(5, new int[] {11, 12, 13, 14, 15});
        System.out.println("Created vector: a = " + a.toString());

        Vector b = new Vector(5, new int[] {21, 22, 23, 24, 25});
        System.out.println("Created vector: b = " + b.toString());

        Vector c = new Vector(5);
        System.out.println("Created vector: c = " + c.toString());

        System.out.println("d = a + c");
        Vector d = a.add(c);

        System.out.println("e = b + d");
        Vector e = b.add(d);

        System.out.println("Created vector: e = " + e.toString());

        System.out.println("f = <d, e>");
        int f = d.dotProduct(e);
        System.out.println("f = " + f);
    }
}