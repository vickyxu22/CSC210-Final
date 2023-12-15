## Phase Zero:  Choosing a Dataset
12/1
I chose the EU email communication network dataset from the Large Network Dataset Collection. 
It's interesting to use a graph to investigate a real social and technological communication network. 
This is a single clean text file. Each line consists of a start node (email sender) and an end node (email receiver).

## Phase One:  Reading In
12/2 - 12/3
Since it's a large dataset, reading the entire file results in numerous nodes, 
causing the graph to lose important details such as the number of nodes and edges, and significantly increases response time.
Initially, I attempted to read in the first 200 lines. 
However, this yielded only 2 email senders (No.0 and No.1), providing insufficient information for analysis.
Thus, I implemented a filter on the entire file, 
selecting data where both sender and receiver serial numbers are greater than 0 and smaller than 200 (modifiable). 
This reduced the dataset size while retaining the same amount of information for analysis.

## Phase Two: Computation
12/4 - 12/6
Beginning with basic statistics, I calculated the number of nodes and the number of edges. 
Additionally, I'm curious about which sender sent the most emails and the total count of those emails. 
To achieve this, I utilized an ArrayList named 'nodesWithMaxDegree' to store nodes with the maximum degree. 
This list was continuously updated through iterations, leveraging the efficiency of ArrayList for insertion and retrieval.

The original dataset is accompanied by a paper titled 'Graph Evolution: Densification and Shrinking Diameters.' 
It mentions that the most recent real-world networks capture two conventional assumptions about the growth process: 
the constant average degree assumption and the slowly growing diameter assumption.
Therefore, I computed the average node degree under various dataset size settings.

For both senders and receivers' serial number is bigger than 0 and smaller than 200, the average node degree is 6.05.
For both senders and receivers' serial number is bigger than 0 and smaller than 30000, the average node degree is 6.60.
Though the order of magnitutde is different, the average node degree remains approximately the same.

Moreover, I used Breadth First Search algorithm to find a path between two randomly assigned nodes.  
I chose LinkedList as it implements the Queue interface, offering efficiency in adding and removing elements from both ends, which is advantageous for maintaining paths.
For basic operations such as add() and contains() during traversal, HashSet was chosen for its constant-time complexity when checking visited nodes.
For storing paths, I utilized ArrayList due to its efficiency in random access and ease of adding successor nodes to explore different paths from a given node.

## Phase Three: Output
12/9 - 12/12
In phase two, I printed the text in the terminal: 
the number of nodes, number of edges, maximum node degree, node with the maximum degree, average node degree, and the path found between two random nodes.

To visualize it, I used a static Mutable graph. 
First, to represent the relative average node degree among nodes, I created a `colorBasedOnDegree` method in the GraphDisplay.  
This method calculates the color intensity value based on the node's degree in relation to the min/max degree. 
Nodes with higher average degrees appear darker in color.
Second, I visualize the path between two randomly picked nodes.
Initially, I wish I can color the edges of the path as yellow, but due to time restriction, I can't finish this part. 
Instead, I marked connected nodes as yellow: the start node (sender), the end node (receiver), and any nodes (people) between them.
If there is no path between nodes, no node will be colored yellow.

## Phase Four: Reflection
To conclude, I practiced my data structurea (Queue, ArrayList, Hashset, MutableGraph) and BFS algorithms through the final project. 
The outcome includes text printed in the terminal and a graph visualizing the EU email communication network dataset.
It's interesting to see how dense the social network is, particularly when the graph displays the entire dataset.
