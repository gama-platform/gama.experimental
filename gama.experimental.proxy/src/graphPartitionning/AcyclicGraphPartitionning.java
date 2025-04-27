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

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class AcyclicGraphPartitionning 
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
	
    private static Map<Integer, List<Integer>> buildAdjacencyListFromTree(TreeNode root) {
        Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
        buildAdjacencyListRecursive(root, adjacencyList);
        return adjacencyList;
    }

    private static void buildAdjacencyListRecursive(TreeNode node, Map<Integer, List<Integer>> adjacencyList) {
        for (TreeNode child : node.children) {
            adjacencyList.computeIfAbsent(node.value, k -> new ArrayList<>()).add(child.value);
            buildAdjacencyListRecursive(child, adjacencyList);
        }
    }

    private static TreeNode buildTreeFromEdges(int[][] edges, int rootValue) {

        Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
        for (int[] edge : edges) {
            int parent = edge[1];
            int child = edge[0];
            adjacencyList.computeIfAbsent(parent, k -> new ArrayList<>()).add(child);
        }
    
        TreeNode root = new TreeNode(rootValue);
    
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
    
        while (!queue.isEmpty()) {
            TreeNode currentNode = queue.poll();
            if (adjacencyList.containsKey(currentNode.value)) {
                for (int childValue : adjacencyList.get(currentNode.value)) {
                    TreeNode childNode = new TreeNode(childValue);
                    currentNode.children.add(childNode);
                    queue.add(childNode); 
                }
            }
        }
    
        return root;
    }

    public static TreeNode buildTreeFromEdges(int[][] edges) {
        Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
        Set<Integer> allNodes = new HashSet<>();
        Set<Integer> childNodes = new HashSet<>();

        for (int[] edge : edges) {
            int parent = edge[1];
            int child = edge[0];

            adjacencyList.computeIfAbsent(parent, k -> new ArrayList<>()).add(child);
            allNodes.add(parent);
            allNodes.add(child);
            childNodes.add(child);
        }
        int rootValue = -1;
        for (int node : allNodes) {
            if (!childNodes.contains(node)) {
                rootValue = node;
                break;
            }
        }

        if (rootValue == -1) {
            throw new IllegalStateException("No root found! Input edges may not form a valid tree.");
        }

        TreeNode root = new TreeNode(rootValue);
        Queue<TreeNode> queue = new LinkedList<>();
        Map<Integer, TreeNode> nodeMap = new HashMap<>();
        nodeMap.put(rootValue, root);
        queue.add(root);

        while (!queue.isEmpty()) {
            TreeNode currentNode = queue.poll();
            int currentValue = currentNode.value;

            if (adjacencyList.containsKey(currentValue)) {
                for (int childValue : adjacencyList.get(currentValue)) {
                    TreeNode childNode = new TreeNode(childValue);
                    currentNode.children.add(childNode);
                    queue.add(childNode);
                    nodeMap.put(childValue, childNode);
                }
            }
        }

        return root;
    }

    public static List<Integer> findRandomLongestPath(TreeNode root) {
        List<List<Integer>> longestPaths = findAllLongestPaths(root);
        if (longestPaths.isEmpty()) {
            return new ArrayList<>();
        }
        Random random = new Random();
        return longestPaths.get(random.nextInt(longestPaths.size()));
    }

    private static List<List<Integer>> findAllLongestPaths(TreeNode root) {
        List<List<Integer>> allLongestPaths = new ArrayList<>();
        int[] maxLength = {0};
        findAllLongestPathsRecursive(root, new ArrayList<>(), allLongestPaths, maxLength);
        return allLongestPaths;
    }

    private static void findAllLongestPathsRecursive(TreeNode node, List<Integer> currentPath, List<List<Integer>> allLongestPaths, int[] maxLength) {
        currentPath.add(node.value);
        if (node.children.isEmpty()) {
            if (currentPath.size() > maxLength[0]) {
                maxLength[0] = currentPath.size();
                allLongestPaths.clear();
                allLongestPaths.add(new ArrayList<>(currentPath));
            } else if (currentPath.size() == maxLength[0]) {
                allLongestPaths.add(new ArrayList<>(currentPath));
            }
        } else {
            for (TreeNode child : node.children) {
                findAllLongestPathsRecursive(child, new ArrayList<>(currentPath), allLongestPaths, maxLength);
            }
        }
    }

    public static List<List<Integer>> getSubtreesFromLongestPath(List<Integer> longestPath, Map<Integer, List<Integer>> adjacencyList) {
        List<List<Integer>> subtrees = new ArrayList<>();
        Set<Integer> longestPathSet = new HashSet<>(longestPath);
        for (int node : longestPath) {
            if (adjacencyList.containsKey(node)) {
                for (int child : adjacencyList.get(node)) {
                    if (!longestPathSet.contains(child)) {
                        List<Integer> subtree = new ArrayList<>();
                        getSubtreeNodes(child, adjacencyList, subtree, longestPathSet);
                        if (!subtree.isEmpty()) {
                            subtrees.add(subtree);
                        }
                    }
                }
            }
        }
        return subtrees;
    }

    private static void getSubtreeNodes(int node, Map<Integer, List<Integer>> adjacencyList, List<Integer> subtree, Set<Integer> longestPathSet) {
        if (!adjacencyList.containsKey(node)) { // If it's a leaf node
            subtree.add(node); // Add it to the subtree
            return;
        }
        subtree.add(node); // Add the current node
        for (int child : adjacencyList.get(node)) {
            if (!longestPathSet.contains(child)) {
                getSubtreeNodes(child, adjacencyList, subtree, longestPathSet);
            }
        }
    }

    private static TreeNode buildTree(Map<Integer, List<Integer>> adjacencyList, int rootValue) {
        TreeNode root = new TreeNode(rootValue);
        buildTreeRecursive(root, adjacencyList);
        return root;
    }

    private static void buildTreeRecursive(TreeNode node, Map<Integer, List<Integer>> adjacencyList) {
        int nodeValue = node.value;
        if (adjacencyList.containsKey(nodeValue)) {
            for (int childValue : adjacencyList.get(nodeValue)) {
                TreeNode child = new TreeNode(childValue);
                node.children.add(child);
                buildTreeRecursive(child, adjacencyList);
            }
        }
    }

    public static List<List<Integer>> clusterSubtrees(List<List<Integer>> subtrees, int cluster_number) {
        subtrees.sort((list1, list2) -> Integer.compare(list2.size(), list1.size()));
        List<List<Integer>> clusters = new ArrayList<>();
        int[] clusterSizes = new int[cluster_number];

        for (int i = 0; i < cluster_number; i++) {
            clusters.add(new ArrayList<>());
        }

        for (List<Integer> subtree : subtrees) {
            int minClusterIndex = 0;
            for (int i = 1; i < cluster_number; i++) {
                if (clusterSizes[i] < clusterSizes[minClusterIndex]) {
                    minClusterIndex = i;
                }
            }
            
            clusters.get(minClusterIndex).addAll(subtree);
            clusterSizes[minClusterIndex] += subtree.size();
            
        }
        
        boolean removed = clusters.removeIf(p -> p.isEmpty());
        if(removed)
        {
            DEBUG.OUT("CAN't DO " + cluster_number + " clusters, can do " + clusters.size());
        }

        return clusters;
    }

    public static int[][] generateRandomReverseTree(int numNodes) {
        Random random = new Random();
        List<int[]> edges = new ArrayList<>();
        for (int i = 1; i < numNodes; i++) {
            int parent = random.nextInt(i);
            edges.add(new int[]{i, parent});
        }
        return edges.toArray(new int[0][]);
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

    public static List<List<Integer>> optimizeNodeOnPath(List<List<Integer>> longestPaths, Map<Integer, List<Integer>> adjacencyList, List<List<Integer>> clusters) {
        var newClusters = new ArrayList<>(clusters);
        
        for (var longestPath : longestPaths) {
            Set<Integer> subtreeRootsMet = new HashSet<>();
            int firstSubtreeRoot = -1;
            int firstSubtreeClusterIndex = -1;
            List<Integer> nodesToAdd = new ArrayList<>();
    
            for (int i = longestPath.size() - 1; i >= 0; i--) {
                int currentNode = longestPath.get(i);
                
                if (adjacencyList.containsKey(currentNode)) {
                    List<Integer> children = adjacencyList.get(currentNode);
                    List<Integer> subtreeChildren = new ArrayList<>();
                    
                    for (int child : children) {
                        if (!longestPath.contains(child)) {
                            subtreeChildren.add(child);
                        }
                    }
    
                    if (!subtreeChildren.isEmpty()) { 
                        if (firstSubtreeRoot == -1) { 
                            firstSubtreeRoot = subtreeChildren.get(0);
    
                            // Find the first valid cluster
                            for (int j = 0; j < newClusters.size(); j++) {
                                if (newClusters.get(j).contains(firstSubtreeRoot)) {
                                    firstSubtreeClusterIndex = j;
                                    break; // Stop at the first match
                                }
                            }
    
                            if (firstSubtreeClusterIndex != -1) {
                                if (subtreeChildren.size() > 1) {
                                    nodesToAdd.addAll(longestPath.subList(i + 1, longestPath.size()));
                                } else {
                                    nodesToAdd.addAll(longestPath.subList(i, longestPath.size()));
                                }
    
                                // Prevent duplicate nodes before adding
                                for (int node : nodesToAdd) {
                                    newClusters.get(firstSubtreeClusterIndex).add(node);
                                }
    
                                // Remove nodes from other clusters
                                for (int node : nodesToAdd) {
                                    for (int j = 0; j < newClusters.size(); j++) {
                                        if (j != firstSubtreeClusterIndex) {
                                            newClusters.get(j).remove(Integer.valueOf(node));
                                        }
                                    }
                                }
    
                                nodesToAdd.clear();
                                subtreeRootsMet.add(firstSubtreeRoot);
    
                                if (subtreeChildren.size() > 1) {
                                    for (var auto : subtreeChildren) {
                                        if (!newClusters.get(firstSubtreeClusterIndex).contains(auto)) {
                                            longestPath.subList(i + 1, longestPath.size()).clear();
                                            break;
                                        }
                                    }
    
                                    // Prevent duplicate `currentNode`
                                    boolean isAlreadyAssigned = false;
                                    for (List<Integer> cluster : newClusters) {
                                        if (cluster.contains(currentNode)) {
                                            isAlreadyAssigned = true;
                                            break;
                                        }
                                    }
    
                                    if (!isAlreadyAssigned) {
                                        newClusters.get(firstSubtreeClusterIndex).add(currentNode);
                                    }
                                }
                            }
                        } else {
                            longestPath.subList(i + 1, longestPath.size()).clear();
                            break;
                        }
                    } else {
                        // Prevent duplicate `currentNode`
                        boolean isAlreadyAssigned = false;
                        for (List<Integer> cluster : newClusters) {
                            if (cluster.contains(currentNode)) {
                                isAlreadyAssigned = true;
                                break;
                            }
                        }
    
                        if (firstSubtreeClusterIndex != -1 && !isAlreadyAssigned) {
                            newClusters.get(firstSubtreeClusterIndex).add(currentNode);
                        }
                    }
                }
            }
        }
        return newClusters;
    }
    
    public static Map<Integer, Integer> computeClusterLevels(List<List<Integer>> clusters, List<Integer> longestPath, Map<Integer, List<Integer>> adjacencyList) {
        if (clusters == null || clusters.isEmpty() || longestPath == null || longestPath.isEmpty() || adjacencyList == null || adjacencyList.isEmpty()) {
            DEBUG.OUT("Invalid input data.");
            return null;
        }

        Set<Integer> longestPathNodes = new HashSet<>(longestPath);
        Map<Integer, Integer> clusterLevels = new HashMap<>();
        Map<Integer, Set<Integer>> nodeClusterMap = new HashMap<>(); // Maps nodes to their clusters

        // Initialize nodeClusterMap
        for (int i = 0; i < clusters.size(); i++) {
            for (int node : clusters.get(i)) {
                if (!nodeClusterMap.containsKey(node)) {
                    nodeClusterMap.put(node, new HashSet<>());
                }
                nodeClusterMap.get(node).add(i);
            }
        }

        // Assign level 0 to the cluster containing the longest path
        int longestPathCluster = -1;
        for (int i = 0; i < clusters.size(); i++) {
            for (int node : clusters.get(i)) {
                if (longestPathNodes.contains(node)) {
                    longestPathCluster = i;
                    break;
                }
            }
            if (longestPathCluster != -1) {
                clusterLevels.put(longestPathCluster, 0);
                break;
            }
        }

        // Calculate levels for other clusters
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(longestPathCluster);

        while (!queue.isEmpty()) {
            int currentCluster = queue.poll();
            int currentLevel = clusterLevels.get(currentCluster);

            for (int node : clusters.get(currentCluster)) {
                if (adjacencyList.containsKey(node)) {
                    for (int neighbor : adjacencyList.get(node)) {
                        if (nodeClusterMap.containsKey(neighbor)) {
                            for (int neighborCluster : nodeClusterMap.get(neighbor)) {
                                if (!clusterLevels.containsKey(neighborCluster)) {
                                    clusterLevels.put(neighborCluster, currentLevel + 1);
                                    queue.offer(neighborCluster);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Print cluster levels
        for (int i = 0; i < clusters.size(); i++) {
            DEBUG.OUT("Cluster " + i + ": Level " + (clusterLevels.containsKey(i) ? clusterLevels.get(i) : -1));
        }

        return clusterLevels;
    }

    public static List<List<Integer>> divideCluster(List<Integer> clusterToDivide, Map<Integer, List<Integer>> adjacencyList, int threshold, int maxClusters) {
        List<List<Integer>> dividedClusters = new ArrayList<>();
        
        if (clusterToDivide.size() <= threshold) {
            dividedClusters.add(clusterToDivide); // Cluster is small enough
            return dividedClusters;
        }
    
        // Find the root of the cluster
        int root = clusterToDivide.get(0);
        if (root != -1) {
            // Find the longest path in the cluster
            TreeNode rootNode = buildTree(adjacencyList, root);
            List<Integer> longestPath = findRandomLongestPath(rootNode);
    
            // Divide the cluster based on the longest path
            List<List<Integer>> subClusters = getSubtreesFromLongestPath(longestPath, adjacencyList);
    
            // Check if dividing the cluster will exceed maxClusters
            if (dividedClusters.size() + subClusters.size() <= maxClusters) {
                for (List<Integer> subCluster : subClusters) {
                    if (!subCluster.isEmpty()) {
                        dividedClusters.addAll(divideCluster(subCluster, adjacencyList, threshold, maxClusters)); // Recursively divide sub-clusters
                    }
                }
            } else {
                dividedClusters.add(clusterToDivide); // Cannot divide further without exceeding maxClusters
            }
        } else {
            dividedClusters.add(clusterToDivide); // No root found, return original cluster
        }
    
        return dividedClusters;
    }

    public static List<List<Integer>> getSubtreeSizeDifferences(List<List<Integer>> subtrees) {
        if (subtrees == null || subtrees.isEmpty()) {
            DEBUG.OUT("No subtrees to analyze.");
            return new ArrayList<>();
        }
    
        List<Integer> subtreeSizes = new ArrayList<>();
        for (List<Integer> subtree : subtrees) {
            subtreeSizes.add(subtree.size());
        }
    
        Collections.sort(subtreeSizes);
    
        // Compute average size
        double averageSize = subtreeSizes.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    
        // Compute median size
        double medianSize;
        int n = subtreeSizes.size();
        if (n % 2 == 0) {
            medianSize = (subtreeSizes.get(n / 2 - 1) + subtreeSizes.get(n / 2)) / 2.0;
        } else {
            medianSize = subtreeSizes.get(n / 2);
        }
    
        // List to store subtrees exceeding 150% difference
        List<List<Integer>> exceedingSubtrees = new ArrayList<>();
    
        // Print statistics
        DEBUG.OUT("==================================");
        System.out.printf("Average subtree size: %.2f\n", averageSize);
        System.out.printf("Median subtree size: %.2f\n", medianSize);
        DEBUG.OUT("==================================");
    
        // Print size differences for each subtree
        DEBUG.OUT("Subtree Size Differences (relative to median):");
        for (int i = 0; i < subtrees.size(); i++) {
            int size = subtrees.get(i).size();
            double difference = size - medianSize;
            double percentageDifference = (medianSize != 0) ? (difference / medianSize) * 100 : 0;
    
            System.out.printf("Subtree %d: Size = %d, Difference = %.2f (%.2f%% from median)\n",
                    i, size, difference, percentageDifference);
    
            // Check if the subtree exceeds 150% of the median
            if (Math.abs(percentageDifference) > 150.0) {
                exceedingSubtrees.add(subtrees.get(i));
            }
        }
    
        DEBUG.OUT("==================================");
        DEBUG.OUT("Subtrees exceeding 150% size difference from median: " + exceedingSubtrees.size());
        DEBUG.OUT("==================================");
    
        return exceedingSubtrees;
    }
    
    public static List<List<Integer>> splitOversizedSubtrees(List<List<Integer>> subtrees, List<List<Integer>> oversizedSubtrees, 
    int[][] edges, Map<Integer, List<Integer>> adjacencyList, List<List<Integer>> longuestPaths) {
    List<List<Integer>> updatedSubtrees = new ArrayList<>(subtrees);

        // Process each oversized subtree
        for (List<Integer> oversizedSubtree : oversizedSubtrees) {
            DEBUG.OUT("Splitting large subtree...");

            // Build a tree for the oversized subtree
            TreeNode rootBig = buildTreeFromEdges(edges, oversizedSubtree.get(0));

            // Compute the longest path on this subtree
            List<Integer> longestPath = findRandomLongestPath(rootBig);
            longuestPaths.add(longestPath);

            // Extract new subtrees from the longest path
            List<List<Integer>> extractedSubtrees = getSubtreesFromLongestPath(longestPath, adjacencyList);

            // Remove the original large subtree
            updatedSubtrees.remove(oversizedSubtree);

            // Add the new smaller subtrees instead
            updatedSubtrees.addAll(extractedSubtrees);
            //updatedSubtrees.add(longestPath);
        }

        DEBUG.OUT("==================================");
        DEBUG.OUT("Final number of subtrees after splitting: " + updatedSubtrees.size());
        DEBUG.OUT("==================================");

        return updatedSubtrees;
    } 

    public static List<List<Integer>> mergeClusters(List<List<Integer>> clusters, int cluster_number, Map<Integer, List<Integer>> adjacencyList, List<Integer> longuestPath) {
        if (clusters.size() <= cluster_number) {
            return clusters; // Already at or below the required number
        }
        // Get cluster levels
        Map<Integer, Integer> clusterLevels = computeClusterLevels(clusters, longuestPath, adjacencyList);
        // Group clusters by level
        Map<Integer, List<List<Integer>>> levelClusters = new HashMap<>();
        for (int i = 0; i < clusters.size(); i++) {
            int level = clusterLevels.getOrDefault(i, -1);
            levelClusters.computeIfAbsent(level, k -> new ArrayList<>()).add(clusters.get(i));
        }
    
        // Sort levels
        List<Integer> sortedLevels = new ArrayList<>(levelClusters.keySet());
        Collections.sort(sortedLevels);

        clusters = mergeListsInMapKeepLevelsNoLast(levelClusters, cluster_number);

        return clusters;
    }

    public static List<List<Integer>> mergeListsInMapKeepLevelsNoLast(Map<Integer, List<List<Integer>>> dataMap,int targetTotalLists) {

        List<List<Integer>> allLists = new ArrayList<>();
        Map<Integer, Integer> levelLastIndices = new HashMap<>();
        int currentIndex = 0;

        for (Map.Entry<Integer, List<List<Integer>>> entry : dataMap.entrySet()) {
            int level = entry.getKey();
            List<List<Integer>> levelLists = entry.getValue();
            currentIndex += levelLists.size();
            levelLastIndices.put(level, currentIndex - 1);
            allLists.addAll(levelLists);
        }

        if (allLists.size() <= targetTotalLists) {
            if (allLists.size() < targetTotalLists){
                return allLists;
            }
            return allLists;
        }

        while (allLists.size() > targetTotalLists) {
            List<List<Integer>> mergeableLists = new ArrayList<>();
            List<Integer> mergeableIndices = new ArrayList<>();

            for (int i = 0; i < allLists.size(); i++) {
                boolean isLastList = false;
                for (int level : levelLastIndices.keySet()) {
                    if (levelLastIndices.get(level) == i) {
                        isLastList = true;
                        break;
                    }
                }
                if (!isLastList) {
                    mergeableLists.add(allLists.get(i));
                    mergeableIndices.add(i);
                }
            }

            if (mergeableLists.size() < 2) {
                return allLists;
            }

            // Find the two lists with the closest sizes
            int minSizeDiff = Integer.MAX_VALUE;
            int index1 = -1;
            int index2 = -1;

            for (int i = 0; i < mergeableLists.size(); i++) {
                for (int j = i + 1; j < mergeableLists.size(); j++) {
                    int sizeDiff = Math.abs(mergeableLists.get(i).size() - mergeableLists.get(j).size());
                    if (sizeDiff < minSizeDiff) {
                        minSizeDiff = sizeDiff;
                        index1 = allLists.indexOf(mergeableLists.get(i));
                        index2 = allLists.indexOf(mergeableLists.get(j));
                    }
                }
            }

            List<Integer> mergedList = new ArrayList<>(allLists.get(index1));
            mergedList.addAll(allLists.get(index2));

            if (index1 > index2) {
                int temp = index1;
                index1 = index2;
                index2 = temp;
            }

            allLists.remove(index2);
            allLists.remove(index1);
            allLists.add(index1, mergedList);

            // Update levelLastIndices if necessary
            for (int level : levelLastIndices.keySet()) {
                if (levelLastIndices.get(level) >= index2) {
                    levelLastIndices.put(level, levelLastIndices.get(level) - 1);
                }
            }
        }

        return allLists;
    }

    public static List<List<Integer>> partitionTreeIntoClusters(int[][] edges, TreeNode root, int cluster_number, String outputFileName) {
        // Construire la liste d'adjacence
        Map<Integer, List<Integer>> adjacencyList = buildAdjacencyListFromTree(root);
        List<List<Integer>> longestPaths = new ArrayList<>();
        
        // Trouver le plus long chemin
        List<Integer> longestPath = findRandomLongestPath(root);
        longestPaths.add(longestPath);
        
        // Extraire les sous-arbres à partir du plus long chemin
        List<List<Integer>> subtrees = getSubtreesFromLongestPath(longestPath, adjacencyList);

        List<List<Integer>> biggerSubTree = getSubtreeSizeDifferences(subtrees);

        subtrees = splitOversizedSubtrees(subtrees, biggerSubTree, edges, adjacencyList, longestPaths);

        List<Integer> flat =  longestPaths.stream().flatMap(List::stream).collect(Collectors.toList());
        
        // Regrouper les sous-arbres en clusters
        List<List<Integer>> clusters = clusterSubtrees(subtrees, cluster_number-1);
        clusters.add(flat);
        
        clusters = optimizeNodeOnPath(longestPaths, adjacencyList, clusters);
        // Optimiser l'affectation des nœuds sur le chemin
        

        DEBUG.OUT("generatePngFromGraph ");
        if(clusters.size() != cluster_number)
        {
            clusters = mergeClusters(clusters, cluster_number, adjacencyList, flat);
        }

        DEBUG.OUT("NEED CLUSTER OF CLUSTER : " + cluster_number);
        DEBUG.OUT("NUMBER OF CLUSTER : " + clusters.size());
        return clusters;
    }
    

    /**
     * 
     * Acyclic Tree partitionning into cluster_number cluster
     * 
     * 
     * @param edges : edges of the graph to clsuter
     * @param cluster_number : number of cluster requireds
     * 
     * @return List<List<Integer>> clustered graph
     */
    @SuppressWarnings("rawtypes")
	@operator (
			value = "cluster",
			type = IType.LIST,
			category = { IOperatorCategory.GRAPH },
			concept = { IConcept.GRAPH})
	@doc (value = "Acyclic Tree partitionning into cluster_number cluster")
    public static List<List<Integer>> cluster(IGraph graph, int cluster_number) {
    	
        /*int node_number = 20;
        int cluster_number = 4;
        int[][] edges = generateRandomReverseTree(node_number); // Générer des arêtes aléatoires*/
    	
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
    	
        TreeNode root = buildTreeFromEdges(edges); // Construire l'arbre à partir des arêtes
        List<List<Integer>> clusters = partitionTreeIntoClusters(edges, root, cluster_number, "graph_output"); // Partitionner l'arbre en clusters et générer l'image

        DEBUG.OUT("FINAL CLUSTER ");
        int node = 0;
        for(var auto : clusters)
        {   
            node += auto.size();
            DEBUG.OUT(auto);
        }

        DEBUG.OUT("TOTAL SIZE " + node);
        getClusterSizeDifferences(clusters);
        return clusters;
    }
}
	        
	/*int[][] edges = {
	    {45,24}, {39,24}, 
	    {44,45}, {46,45}, {50,39}, {38,39},
	    {57,50}, {51,50}, {55,38}, {40,38},
	    {47,55}, {56,55}, {52,40}, {42,40},
	    {49,47}, {48,47}, {53,52}, {54,52},
	    {63,42}, {62,42}, {41,42},
	    {58,41},{59,58},{61,58},
	    {37,41}, {64,37}, {60,64}, {65,64},
	    {66,36}, {35,36}, {68,35}, {34,35},
	    {69,68}, {17,34}, {32,34},
	    {22,32}, {9,32}, {31,32},
	    {23,22}, {21,22}, {11,9}, {15,9},
	    {13,11}, {10,11}, {14,13}, {12,13},
	    {36,37},
	    {18,31}, {30,31}, {19,18}, {20,18},
	    {16,30}, {29,30}, {2,29}, {4,29},
	    {1,2}, {3,2}, {28,4},
	    {5,28}, {26,28}, {7,5}, {6,5},
	    {8,26}, {25,26}
	};*/
