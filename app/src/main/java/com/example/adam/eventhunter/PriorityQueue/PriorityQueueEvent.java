package com.example.adam.eventhunter.PriorityQueue;

import com.example.adam.eventhunter.Event;

import java.util.Vector;

/**
 * Created by Adam on 09/12/2017.
 */

public class PriorityQueueEvent {
    private Vector<Event> A;

    public PriorityQueueEvent() {
        A = new Vector<Event>();
    }

    // return parent of A.get
    private int parent(int i) {
        // if i is already a
        if (i == 0)
            return 0;
        return (i - 1) / 2;
    }

    // return left child of A
    private int LEFT(int i) {
        return (2 * i + 1);
    }

    // return right child of
    private int RIGHT(int i) {
        return (2 * i + 2);
    }

    // swap values at two ind
    private void swap(int x, int y) {
        // swap with child ha
        Event temp = A.get(x);
        A.setElementAt(A.get(y), x);
        A.setElementAt(temp, y);
    }

    // Recursive Heapify-down
    // and its two direct chi
    private void heapify_down(int i) {
        // get left and right
        int left = LEFT(i);
        int right = RIGHT(i);
        int smallest = i;
        // compare A.get(i) w
        // and find smallest
        if (left < size() && A.get(left).getStartDate().before(A.get(i).getStartDate()))
            smallest = left;
        if (right < size() && A.get(right).getStartDate().before(A.get(smallest).getStartDate()))
            smallest = right;
        if (smallest != i) {
            swap(i, smallest);
            heapify_down(smallest);
        }
    }

    private void heapify_up(int i) {
// check if node at i
// the heap property
        if (i > 0 && A.get(parent(i)).getStartDate().after(A.get(i).getStartDate())) {
// swap the two i
            swap(i, parent(i));
// call Heapify-u
            heapify_up(parent(i));
        }
    }

    // return size of the hea
    public int size() {
        return A.size();
    }

    // check if heap is empty
    public Boolean isEmpty() {
        return A.isEmpty();
    }

    // insert specified key i
    public void add(Event key) {
// insert the new ele
        A.addElement(key);
// get element index
        int index = size() - 1;
        heapify_up(index);
    }

    // function to remove and
// (present at root). It
    public Event poll() {
        try {
// if heap is emp
            if (size() == 0)
                throw new Exception("Index out of range");
// element with h
            Event root = A.firstElement();
// replace root o
            A.setElementAt(A.lastElement(), 0);

            A.remove(size() - 1);
// call heapify-d
            heapify_down(0);
// return root el
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
// catch and print th

    }

    // function to return, bu
// priority present at ro
    public Event peek() {
        try {
// if heap has no
            if (size() == 0)
                throw new Exception("Index out of range");
// else return th
            return A.firstElement();
        }
// catch the exceptio
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // function to remove all
    public void clear() {

        while (!A.isEmpty()) {
        }

    }

    public Event lastElement()
    {
        return A.lastElement();
    }

    // returns true if queue
    public Boolean contains(String text) {
        return A.contains(text);
    }

    // returns an array conta
    public Event[] toArray() {
        return A.toArray(new Event[size()]);
    }
}
