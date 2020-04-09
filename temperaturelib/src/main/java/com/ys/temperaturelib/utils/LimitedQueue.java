package com.ys.temperaturelib.utils;

import java.util.LinkedList;


/**
 * java大小固定的队列——保存最后N个元素
 */
public class  LimitedQueue<E> extends LinkedList<E> {
    private static final long serialVersionUID = 1L;
    private int limit;

    public LimitedQueue(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean add(E o) {
        super.add(o);
        while (size() > limit) { super.remove(); }
        return true;
    }
}

//blog.csdn.net/liuxiao723846/article/details/81782507