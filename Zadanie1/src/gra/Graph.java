package gra;

import gra.util.Współrzędna;

import java.util.*;

public class Graph {
    private final Map<Współrzędna, Set<Współrzędna>> graph;

    public Graph() {
        graph = new HashMap<>();
    }

    Set<Współrzędna> getNeighbours(Współrzędna from) {
        Set<Współrzędna> neighbours = graph.get(from);
        if(neighbours == null) {
            neighbours = new HashSet<>();
        }
        return neighbours;
    }

    synchronized public void insert(Współrzędna from, Współrzędna to) throws DeadlockException {
        Set<Współrzędna> neighbours = getNeighbours(from);
        neighbours.add(to);
        graph.put(from, neighbours);

        Współrzędna current;
        Queue<Współrzędna> toCheck = new LinkedList<>();
        Set<Współrzędna> checked = new HashSet<>();
        toCheck.addAll(neighbours);
        while (!toCheck.isEmpty()) {
            current = toCheck.poll();
            if(current == from) {
                throw new DeadlockException();
            }
            checked.add(current);
            Set<Współrzędna> curNeighbours = getNeighbours(current);
            curNeighbours.removeAll(checked);
            toCheck.addAll(curNeighbours);
        }
    }

    synchronized public void remove(Współrzędna from, Współrzędna to) {
        Set<Współrzędna> neighbours = getNeighbours(from);
        neighbours.remove(to);
        graph.put(from, neighbours);
    }
}
