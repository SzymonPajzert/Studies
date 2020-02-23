package terasort.util;

import org.apache.hadoop.io.WritableComparable;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PairInt implements WritableComparable<PairInt> {
    private int x;
    private int y;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public PairInt() {}

    public PairInt(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(x);
        dataOutput.writeInt(y);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        x = dataInput.readInt();
        y = dataInput.readInt();
    }

    @Override
    public int compareTo(@NotNull PairInt pairInt) {
        return new Integer(x).compareTo(pairInt.x);
    }

    @Override
    public String toString() {
        return "PairInt (" + x + ", " + y + ")";
    }
}
