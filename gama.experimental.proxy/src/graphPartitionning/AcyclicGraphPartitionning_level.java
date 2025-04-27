package graphPartitionning;

import gama.annotations.precompiler.IConcept;
import gama.annotations.precompiler.IOperatorCategory;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.operator;
import gama.core.util.GamaPair;
import gama.core.util.IList;
import gama.core.util.graph.*;
import gama.dev.DEBUG;
import gama.gaml.types.IType;
import java.util.*;

public class AcyclicGraphPartitionning_level 
{
	static
	{
		DEBUG.ON();
	}
	public static class TreeNode { // custom TreeNode
	    int value;
	    List<TreeNode> children;
	
	    public TreeNode(int value) {
	        this.value = value;
	        this.children = new ArrayList<>();
	    }
	}
	
	public static List<List<Integer>> partitionGraphWithDependencies(int[][] edges, int numClusters) {
        // Step 1: Build adjacency list
        Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
        Map<Integer, Integer> inDegree = new HashMap<>();
        Set<Integer> allNodes = new HashSet<>();
    
        for (int[] edge : edges) {
            int parent = edge[1];
            int child = edge[0];
            adjacencyList.computeIfAbsent(parent, k -> new ArrayList<>()).add(child);
            inDegree.put(child, inDegree.getOrDefault(child, 0) + 1);
            inDegree.putIfAbsent(parent, 0);
            allNodes.add(parent);
            allNodes.add(child);
        }

        if (allNodes.size() < numClusters) {
            numClusters = allNodes.size();
            System.out.println("Number of clusters cannot exceed the number of nodes in the graph. Updated number of cluster " + numClusters);
        }
    
        // Step 2: Topological Sorting (Ensures no cycles in dependencies)
        Queue<Integer> queue = new LinkedList<>();
        for (int node : allNodes) {
            if (inDegree.get(node) == 0) {
                queue.add(node);
            }
        }
    
        List<Integer> topoOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            int node = queue.poll();
            topoOrder.add(node);
            if (adjacencyList.containsKey(node)) {
                for (int neighbor : adjacencyList.get(node)) {
                    inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                    if (inDegree.get(neighbor) == 0) {
                        queue.add(neighbor);
                    }
                }
            }
        }
    
        // Step 3: Partitioning (Balanced Clustering)
        //double clusterSize = (double) Math.round( allNodes.size() / numClusters);
        //double remainder = allNodes.size() - (clusterSize * numClusters);

        int clusterSize = allNodes.size() / numClusters;
        int remainder = allNodes.size() % numClusters;
        


        System.out.println("allNodes.size() "+ allNodes.size());
        System.out.println("remainder "+ remainder);
        System.out.println("numClusters "+ numClusters);
        System.out.println("clusterSize " + clusterSize);
       List<List<Integer>> clusters = new ArrayList<>();
        Map<Integer, Integer> nodeToCluster = new HashMap<>();
    
        for (int i = 0; i < numClusters; i++) {
            clusters.add(new ArrayList<>());
        }
    
        int clusterIndex = 0, count = 0;
        for (int node : topoOrder) {
            clusters.get(clusterIndex).add(node);
            nodeToCluster.put(node, clusterIndex);

            // Determine the expected size for this cluster
            int expectedSize = (clusterIndex < remainder) ? (clusterSize + 1) : clusterSize;
            
            count++;
            if (count == expectedSize) {
                clusterIndex++;
                count = 0;
            }
        }
            
        // Ensure every cluster has at least one node
        while (clusters.stream().anyMatch(List::isEmpty)) {
            List<Integer> largestCluster = clusters.stream().max(Comparator.comparing(List::size)).orElse(null);
            List<Integer> smallestCluster = clusters.stream().filter(List::isEmpty).findFirst().orElse(null);
            if (largestCluster != null && smallestCluster != null && !largestCluster.isEmpty()) {
                smallestCluster.add(largestCluster.remove(largestCluster.size() - 1));
            }
        }
    
        // Step 4: Detect Cyclic Dependencies Between Clusters
        Set<Integer> visitedClusters = new HashSet<>();
        Set<Integer> stack = new HashSet<>();
        boolean hasCycle = false;
    
        for (int i = 0; i < numClusters; i++) {
            if (!visitedClusters.contains(i)) {
                hasCycle = hasCycle || detectCycleInClusters(i, clusters, adjacencyList, nodeToCluster, visitedClusters, stack);
            }
        }
    
        if (hasCycle) {
            System.out.println("Dependency Violation: Cyclic dependency detected between clusters!");
        }

        int node = 0;
        for(var auto : clusters)
        {   
            node += auto.size();
            System.out.println(auto);
        }

        System.out.println("TOTAL SIZE " + node);
        
        return clusters;
    }

    private static boolean detectCycleInClusters(int cluster, List<List<Integer>> clusters, Map<Integer, List<Integer>> adjacencyList, Map<Integer, Integer> nodeToCluster, Set<Integer> visited, Set<Integer> stack) {
        if (stack.contains(cluster)) {
            return true; // Cycle detected
        }
        if (visited.contains(cluster)) {
            return false;
        }
        
        visited.add(cluster);
        stack.add(cluster);
        
        for (int node : clusters.get(cluster)) {
            if (adjacencyList.containsKey(node)) {
                for (int neighbor : adjacencyList.get(node)) {
                    int neighborCluster = nodeToCluster.get(neighbor);
                    if (neighborCluster != cluster && detectCycleInClusters(neighborCluster, clusters, adjacencyList, nodeToCluster, visited, stack)) {
                        return true;
                    }
                }
            }
        }
        
        stack.remove(cluster);
        return false;
   }
    
    public static List<List<Integer>> getClusterSizeDifferences(List<List<Integer>> clusters) {
        
        if (clusters == null || clusters.isEmpty()) {
        DEBUG.OUT("No clusters to analyze.");
        return null;
        }

        List<List<Integer>> exceedingClusters = new ArrayList<>(); // List to store exceeding clusters
        List<Integer> toRemove = new ArrayList<>();

        // Calculate median cluster size
        List<Integer> clusterSizes = new ArrayList<>();
        for (List<Integer> cluster : clusters) {
            clusterSizes.add(cluster.size());
        }
        Collections.sort(clusterSizes);
        double medianSize;
        if (clusterSizes.size() % 2 == 0) {
            int mid1 = clusterSizes.get(clusterSizes.size() / 2 - 1);
            int mid2 = clusterSizes.get(clusterSizes.size() / 2);
            medianSize = (mid1 + mid2) / 2.0;
        } else {
            medianSize = clusterSizes.get(clusterSizes.size() / 2);
        }

        // Analyze each cluster and calculate standard deviation.
        double sum = 0;
        double squaredDifferencesSum = 0;

        for (int i = 0; i < clusters.size(); i++) {
            List<Integer> currentCluster = clusters.get(i);
            double clusterSize = currentCluster.size();

            // Calculate sum for average
            sum += clusterSize;

            // Calculate difference and percentage difference
            double difference = clusterSize - medianSize;
            double percentageDifference = (medianSize != 0) ? (difference / medianSize) * 100 : (difference == 0 ? 0 : Double.POSITIVE_INFINITY); // Handle division by zero

            // Calculate squared differences for standard deviation
            double averageSize = (i == clusters.size() - 1 && clusters.size() != 0) ? sum / clusters.size() : 0; // Calculate average only on the last iteration
            if (clusters.size() != 0) {
                double differenceFromAverage = clusterSize - averageSize;
                squaredDifferencesSum += Math.pow(differenceFromAverage, 2);
            }

            System.out.printf("Cluster %d: Size=%.0f, Median=%.2f, Difference=%.2f (%.2f%%)\n", i, clusterSize, medianSize, difference, percentageDifference);

            // Calculate standard deviation if last element.
            if (i == clusters.size() - 1 && clusters.size() != 0) {
                double variance = squaredDifferencesSum / clusters.size();
                double standardDeviation = Math.sqrt(variance);
                DEBUG.OUT("Standard Deviation of Cluster Sizes: " + standardDeviation);
            }

            if(percentageDifference > 150.0)
            {
                exceedingClusters.add(clusters.get(i)); // Store cluster exceeding 50% difference
                toRemove.add(i);
            }
        }

        int tttt = toRemove.size()-1;
        for(int n = tttt; n >= 0; n--){
            clusters.remove(n);
        }

        return exceedingClusters; // Return the list of exceeding clusters
    }

    /**
     * 
     * Dummy Acyclic Tree partitionning into cluster_number cluster
     * 
     * 
     * @param edges : edges of the graph to clsuter
     * @param cluster_number : number of cluster requireds
     * 
     * @return List<List<Integer>> clustered graph
     */
    @SuppressWarnings("rawtypes")
	@operator (
			value = "cluster_level",
			type = IType.LIST,
			category = { IOperatorCategory.GRAPH },
			concept = { IConcept.GRAPH})
	@doc (value = "Dummy acyclic Tree partitionning into cluster_number cluster")
    public static List<List<Integer>> cluster_level(IGraph graph, int cluster_number) {
    	
    	DEBUG.OUT("graph " + graph);
    	
    	IList<GamaPair> gama_edges = graph.getEdges();
    	DEBUG.OUT("gama_edges " + gama_edges);

    	List<int[]> edges_list = new ArrayList<>();

    	for(int i = 0; i < gama_edges.size(); i++)
        {
        	GamaPair pair = gama_edges.get(i);
        	DEBUG.OUT(pair.getKey() + " -> " + pair.getValue());
        	
        	edges_list.add(new int[] {(int)pair.getKey(),(int)pair.getValue()});
        }
    	
    	int[][] edges = edges_list.toArray(new int[0][]);
    	
    	 List<List<Integer>> clusters = partitionGraphWithDependencies(edges, cluster_number);

         getClusterSizeDifferences(clusters);
         
         return clusters;
    }
}
