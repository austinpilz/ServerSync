package com.AustinPilz.ServerSync.Components;

public class Queue<T> {
    private java.util.LinkedList<T> list = new java.util.LinkedList<T>();
    public Queue() {
    }
    public void clear() {
        list.clear();
    }
    public boolean isEmpty() {
        return list.isEmpty();
    }
    public T firstEl() {
        return list.getFirst();
    }
    public T dequeue() {
        return list.removeFirst();
    }
    public void enqueue(T el) {
        list.addLast(el);
    }
    public String toString() {
        return list.toString();
    }
    public int size() {
    	return list.size();
    }
    public boolean inQueue(T el)
    {
    	return list.contains(el);
    }
}