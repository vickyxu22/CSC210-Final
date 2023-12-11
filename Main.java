package demo;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
// import com.google.common.graph.Traverser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.awt.Color;


public class Main {
    public static MutableGraph<String> createGraphFromData(String filePath) {
        MutableGraph<String> graph = GraphBuilder.directed().build();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // int lineCount = 0;
            // while ((line = br.readLine()) != null & lineCount < 200) {
                while ((line = br.readLine()) != null) {
                    if (!line.startsWith("#")) {
                        String[] data = line.trim().split("\\s+"); // Split by any whitespace
                        String sourceNode = data[0];
                        String targetNode = data[1];
                
                        boolean isValidNodes = isNumberInRange(sourceNode) && isNumberInRange(targetNode);
                
                        if (isValidNodes && !sourceNode.equals(targetNode)) {
                            int sourceNumber = Integer.parseInt(sourceNode);
                            int targetNumber = Integer.parseInt(targetNode);
                
                            // Check if both sourceNumber and targetNumber fall within the range of 1-100
                            if (sourceNumber >= 0 && sourceNumber <= 100 && targetNumber >= 0 && targetNumber <= 100) {
                                // Add nodes and edges to the graph if both nodes are in the desired range
                                graph.addNode(sourceNode);
                                graph.addNode(targetNode);
                                graph.putEdge(sourceNode, targetNode);
                            } else {
                                // Handle cases where one or both nodes are not in the desired range
                                // System.out.println("Nodes out of range: " + sourceNode + " -> " + targetNode);
                                // Skip adding this edge to avoid issues or add appropriate handling
                            }
                        } else {
                            // Handle cases where one or both nodes are not valid or if the sourceNode is equal to targetNode
                            // System.out.println("Invalid nodes or self-loop edge detected: " + sourceNode + " -> " + targetNode);
                            // // Skip adding this edge to avoid issues or add appropriate handling
                        }
                    }
                }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return graph;
    }
    private static boolean isNumberInRange(String node) {
        try {
            int number = Integer.parseInt(node);
            return number >= 0 && number <= 40;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Iterable<String> findPathUsingBFS(MutableGraph<String> graph, String startNode, String targetNode) {
        Queue<List<String>> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        
        List<String> initialPath = new ArrayList<>();
        initialPath.add(startNode);
        queue.add(initialPath);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            List<String> path = queue.poll();
            String lastNode = path.get(path.size() - 1);

            if (lastNode.equals(targetNode)) {
                return path; // Path found
            }

            for (String successor : graph.successors(lastNode)) {
                if (!visited.contains(successor)) {
                    visited.add(successor);
                    List<String> newPath = new ArrayList<>(path);
                    newPath.add(successor);
                    queue.add(newPath);
                }
            }
        }
        return null; // No path found
    }


    public static int findMaxNodeNumber(MutableGraph<String> graph) {
        int maxNodeNumber = 0;
        for (String node : graph.nodes()) {
            int nodeNumber = Integer.parseInt(node);
            if (nodeNumber > maxNodeNumber) {
                maxNodeNumber = nodeNumber;
            }
        }
        return maxNodeNumber;
    }

    public static void main(String[] args) {
        String filePath = "email-EuAll.txt"; // Replace with your file path
        MutableGraph<String> emailGraph = createGraphFromData(filePath);
    
        // Calculating graph properties
        int numberOfNodes = emailGraph.nodes().size();
        int numberOfEdges = emailGraph.edges().size();
    
        int maxDegree = 0;
        double totalDegree = 0;
    
        List<String> nodesWithMaxDegree = new ArrayList<>();
    
        for (String node : emailGraph.nodes()) {
            int degree = emailGraph.degree(node);
            totalDegree += degree;
            if (degree > maxDegree) {
                maxDegree = degree;
                nodesWithMaxDegree.clear();
                nodesWithMaxDegree.add(node);
            } else if (degree == maxDegree) {
                nodesWithMaxDegree.add(node);
            }
        }
    
        double averageDegree = totalDegree / numberOfNodes;
    
        // Display or use the computed values as needed
        System.out.println("Number of Nodes: " + numberOfNodes);
        System.out.println("Number of Edges: " + numberOfEdges);
        System.out.println("Maximum Node Degree: " + maxDegree);
        System.out.println("Node(s) with Maximum Degree: " + nodesWithMaxDegree);
        System.out.println("Average Node Degree: " + averageDegree);
    
        // Create an instance of GraphDisplay and use it to display the graph
        GraphDisplay display = new GraphDisplay(emailGraph);
    
        // Example: Find a path between two arbitrary nodes (replace with actual node names)
        // String startNode = "1";
        // String targetNode = "179170";
    
        // Define the range of numbers
        int minNumber = 0;
        int maxNumber = 40;
    
        // Create an instance of the Random class
        Random random = new Random();
    
        // Generate a random number within the specified range
        int randomNumber1 = random.nextInt(maxNumber - minNumber + 1) + minNumber;
        int randomNumber2 = random.nextInt(maxNumber - minNumber + 1) + minNumber;
    
        // Convert the random number to a string
        String startNode = String.valueOf(randomNumber1);
        String targetNode = String.valueOf(randomNumber2);
    
        // Perform BFS to find the path
        Iterable<String> path = findPathUsingBFS(emailGraph, startNode, targetNode);
    
        // Display the path found or indicate no path found
        if (path != null) {
            for (String node : path) {
                display.setColor(node, Color.YELLOW);
            }
            System.out.println("Path from " + startNode + " to " + targetNode + ": " + path);
        } else {
            System.out.println("No path found from " + startNode + " to " + targetNode);
        }
    }
}    
