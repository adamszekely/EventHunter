package com.eventhunters.eventhunter.PriorityQueue;

import com.eventhunters.eventhunter.model.Event;
import java.util.Vector;

/**
 * Created by Adam on 09/12/2017.
 * Partially implemented from the internet
 */


//Class for implementing a Priority Queue
public class PriorityQueueEvent {
    //Vector to store heap elements
    private Vector<Event> A;
    //Constructor: use default initial capacity of vector
    public PriorityQueueEvent() {
        A = new Vector<Event>();
    }

    // return parent of A.get(i)
    private int parent(int i) {
        // if i is already a root node
        if (i == 0)
            return 0;
        return (i - 1) / 2;
    }

    // return left child of A.get(i)
    private int LEFT(int i) {
        return (2 * i + 1);
    }

    // return right child of A.get(i)
    private int RIGHT(int i) {
        return (2 * i + 2);
    }

    // swap values at two indexes
    private void swap(int x, int y) {
        // swap with child having greater value
        Event temp = A.get(x);
        A.setElementAt(A.get(y), x);
        A.setElementAt(temp, y);
    }

    // Recursive Heapify-down procedure. Here the node at index i
    // and its two direct children violates the heap property
    private void heapify_down(int i) {
        // get left and right child of the node at index i
        int left = LEFT(i);
        int right = RIGHT(i);
        int smallest = i;
        // compare A.get(i) with its left and right child
        // and find smallest value
        if (left < size() && A.get(left).getStartDate().before(A.get(i).getStartDate()))
            smallest = left;
        if (right < size() && A.get(right).getStartDate().before(A.get(smallest).getStartDate()))
            smallest = right;
        if (smallest != i) {
            //swap with child having lesser value
            swap(i, smallest);
            //call heapify-down on the child
            heapify_down(smallest);
        }
    }
    //Recursive Heapify-up procedure
    private void heapify_up(int i) {
        // check if node at index i and its parent violate the heap property
        if (i > 0 && A.get(parent(i)).getStartDate().after(A.get(i).getStartDate())) {
            // swap the two if heap property is violated
            swap(i, parent(i));
            // call Heapify-up on the parent
            heapify_up(parent(i));
        }
    }

    // return size of the heap
    public int size() {
        return A.size();
    }

    // check if heap is empty or not
    public Boolean isEmpty() {
        return A.isEmpty();
    }

    // insert specified key into the heap
    public void add(Event key) {
        // insert the new element to the end of the vector
        A.addElement(key);
        // get element index and call Heapify-up procedure
        int index = size() - 1;
        heapify_up(index);
    }

    // function to remove and return element with the highest priority
    // (present at root). It returns null if queue is empty
    public Event poll() {
        try {
            // if heap is empty, throw an exception
            if (size() == 0)
                throw new Exception("Index out of range");
            // element with highest priority
            Event root = A.firstElement();
            // replace root of the heap with the last element of the vector
            A.setElementAt(A.lastElement(), 0);

            A.remove(size() - 1);
            // call heapify-down on root node
            heapify_down(0);
            // return root element
            return root;
        }
        // catch and print the exception
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // function to return, but does not remove element with highest priority present at root
    public Event peek() {
        try {
            // if heap has no elements, throw an exception
            if (size() == 0)
                throw new Exception("Index out of range");
            // else return the top first element
            return A.firstElement();
        }
        // catch the exception
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // returns an array containing all elements in the queue
    public Event[] toArray() {
        return A.toArray(new Event[size()]);
    }
}
