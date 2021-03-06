package algorithms;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.ToDoubleFunction;

public class Sort1D<T> implements FurthestItems<T> {

    private class CombinedList extends AbstractList<T> {

        @NotNull List<T> l1;
        @NotNull List<T> l2;

        CombinedList(@NotNull List<T> l1, @NotNull List<T> l2) {
            this.l1 = l1;
            this.l2 = l2;
        }

        @Override
        public T get(int index) {
            Objects.checkIndex(index, this.size());
            return (index < this.l1.size()) ? this.l1.get(index) : this.l2.get(
                    index - this.l1.size());
        }

        @Override
        public int size() {
            return this.l1.size() + this.l2.size();
        }

    }//end inner class CombinedList

    private @NotNull List<T> universe;
    private @NotNull ToDoubleFunction<T> toDouble;
    private double threshold;

    public Sort1D(@NotNull Collection<T> universe, @NotNull ToDoubleFunction<T>
            toDouble) {
        if (universe.isEmpty()) {
            throw new IllegalArgumentException("Argument Collection universe " +
                    "can't be empty.");
        }//end if

        List<T> items = new ArrayList<>(universe);
        items.sort(Comparator.comparingDouble(toDouble));

        this.universe = items;
        this.toDouble = toDouble;
    }

    public Sort1D(@NotNull Collection<T> universe, @NotNull ToDoubleFunction<T>
            toDouble, final double delta) {
        this(universe, toDouble);

        //delta of 0.0 means, return whatever you want
        if (delta < 0.0 || delta > 1.0) {
            throw new IllegalArgumentException("Argument delta must be " +
                    "in range [0.0, 1.0].");
        }//end if

        this.threshold = 1.0 - delta;
    }

    @Override
    public @NotNull Collection<T> find(@NotNull T query, final int k) {
        if (k < 1 || k > this.universe.size()) {
            throw new IllegalArgumentException("Argument k must be in range " +
                    "[1, universe.size()].");
        }//end if

        if (k == this.universe.size()) {
            return Collections.unmodifiableCollection(this.universe);
        }//end if

        final double QUERY_VALUE = this.toDouble.applyAsDouble(query);
        int ll = 0;
        int lh = k - 1;
        int rl = this.universe.size() - k;
        int rh = this.universe.size() - 1;

        if (this.distance(lh, QUERY_VALUE) > this.distance(rh, QUERY_VALUE)) {
            return Collections.unmodifiableCollection(this.universe.subList(ll,
                    k));
        }//end if

        if (this.distance(ll, QUERY_VALUE) < this.distance(rl, QUERY_VALUE)) {
            return Collections.unmodifiableCollection(this.universe.subList(rl,
                    this.universe.size()));
        }//end if

        int lastSafeL;
        int lastSafeR;
        double value = 1.0;
        do {
            final int LEFT_MID = ll + (lh - ll) / 2;
            final int RIGHT_MID = rh - (rh - rl) / 2;
            if (this.distance(LEFT_MID, QUERY_VALUE) >
                    this.distance(RIGHT_MID, QUERY_VALUE)) {
                ll = LEFT_MID + 1;
                rl = RIGHT_MID + (rh - rl + 1) % 2;
                lastSafeL = LEFT_MID;
                lastSafeR = rl;
            } else {
                lh = LEFT_MID - (lh - ll + 1) % 2;
                rh = RIGHT_MID - 1;
                lastSafeL = lh;
                lastSafeR = RIGHT_MID;
            }//end if
        } while (lh - ll > -1 && (value *= 0.5) > this.threshold);

        return new CombinedList(this.universe.subList(0, lastSafeL + 1),
                this.universe.subList(lastSafeR, this.universe.size()));
    }

    @Override
    public String toString() {
        return "Sort1D";
    }

    private double getAsDouble(final int index) {
        return this.toDouble.applyAsDouble(this.universe.get(index));
    }

    private double distance(final int index, final double queryValue) {
        return Math.abs(this.getAsDouble(index) - queryValue);
    }

}//end class Sort1D