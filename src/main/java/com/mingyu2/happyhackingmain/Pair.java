package com.mingyu2.happyhackingmain;

public final class Pair<T,M> {
    private T d1;
    private M d2;
    public Pair(T data1, M data2){
        this.d1 = data1;
        this.d2 = data2;
    }
    public T getD1() {
        return d1;
    }
    public M getD2() {
        return d2;
    }
    @Override
    public String toString() {
        return d1+" : "+d2;
    }
}
