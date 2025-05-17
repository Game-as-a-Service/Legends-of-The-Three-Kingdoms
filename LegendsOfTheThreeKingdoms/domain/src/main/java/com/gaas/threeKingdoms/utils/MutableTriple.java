package com.gaas.threeKingdoms.utils;

import java.util.Objects;

public class MutableTriple<L, M, R> {
    private L left;
    private M middle;
    private R right;

    public MutableTriple(L left, M middle, R right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    public static <L, M, R> MutableTriple<L, M, R> of(L left, M middle, R right) {
        return new MutableTriple<>(left, middle, right);
    }

    public L getLeft() { return left; }
    public M getMiddle() { return middle; }
    public R getRight() { return right; }

    public void setLeft(L left) { this.left = left; }
    public void setMiddle(M middle) { this.middle = middle; }
    public void setRight(R right) { this.right = right; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MutableTriple)) return false;
        MutableTriple<?, ?, ?> that = (MutableTriple<?, ?, ?>) o;
        return Objects.equals(left, that.left) &&
                Objects.equals(middle, that.middle) &&
                Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, middle, right);
    }

    @Override
    public String toString() {
        return "(" + left + ", " + middle + ", " + right + ")";
    }
}