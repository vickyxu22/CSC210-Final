# CSC 210 Final Project

For your final project in this course, you will identify a dataset of interest to you that can be represented using a graph. Then, you will write a program to read in your dataset and perform a computation on it to generate some sort of result --  ideally, this result will show or analyze something that you find interesting.  

We will provide you with general guidelines for the project, but many choices will be left to your discretion.  If you would prefer more specific guidance, we will provide a more specific example: an application that seeks the fastest route between two points in a road network. However, you don't have to go with this choice.

You may complete this assignment alone or as part of a pair-programming unit.

## Documentation Requirements

For this project you are required to keep a journal documenting your entire development process.  This will eventually be included in your `readme.md` file after your reflections.

Your journal should included dated entries, minimum one per phase, describing your progress.  This will include any decisions made and your reasons for making them.  Focus in particular on data structure issues and choices -- if faced with a particular task, what data structure did you select in order to solve it and why?  What other data structures did you consider and reject, if any?  Beyond the requirement to use a graph, there are many opportunities to apply some of the other data stuctures we have studied this semester, and we are interested in your thinking on this subject.

## Phase Zero:  Choosing a Dataset

Spend some time thinking about what sort of dataset you would like to work with.  Creating your own synthetic data is always an available option.  However, you may find it more inspiring to work with more realistic data collected from some application in the world.  

To document your work on this phase, you should create a file named `data_description.txt` that contains the following information:
* Bibliographic information about the dataset (i.e., source url, authors/owners)
* Number of nodes and edges
* Qualities of the graph:  directed or undirected; self-edges ok?  duplicate edges ok? other choices made?

There are several collections of graph data that you can browse if you need inspiration.  One [Large Network Dataset Collection](https://snap.stanford.edu/data/) is maintained at Stanford University and consists of cleaned data of various kinds.  Another option is [Kaggle](https://www.kaggle.com/), which includes a number of datatsets used for competitions.  You can search through the site -- just keep in mind that the data here may not be as clean as those in the previous collection.

Another alternative is to infer a graph from some other data object.  For example, a maze can be represented as a graph where each intersection or dead end is a node, and edge weights are the length of the connecting path.

If you are creating your own data, you should write documentation for it in your `data_description.txt` file that answers the above questions.

## Phase One:  Reading In

Once you have identified the dataset you plan to work with, the next step should be to write a program to read it in and store the data in a graph.  In most cases, you will want to write a program that reads a file one line at a time and parses it to extract the necessary information -- just as we have done for many assignments in this class.  However, the specific way this is implemented will likely vary depending on the dataset you choose.

Therefore, you should check the documentation for your dataset and consider:
* Is everything in a single text file, or are there multiple files?  
* What information is included in one line of the file?  
* What information is the most relevant or interesting? It is ok to pick and choose the information you want to work with.
  * If your data file has 78 pieces of information about each node, you can ignore most of them and just keep a handful that are the most relevant or interesting.  
  * You may also decide to prune the nodes themselves as you read them in -- for example, if nodes are cities you could keep only those above a certain population, or perhaps those within a certain range of latitudes and longitudes.
  
### Example Problem Guidance for Phase I

If you are creating your own data file, here is a very simple tab-delimited (tsv) format you could possibly use:
 
    p 4 6 
    n 1 New_York 
    n 2 Los_Angeles 
    n 3 Chicago 
    n 4 Houston 
    e 1 2 2800 
    e 1 3 795 
    e 2 3 2020 
    e 3 4 1085 
    e 1 4 1635 
    e 2 4 1550

This file breaks down into 3 sections:
1. The first line states the "problem" by giving the number of nodes (4) and edges (6). This indicates that the next four lines will describe the four nodes, and the six lines after that will describe edges.
2. In the next four lines, we provide a temporary id and a data value (city name) for each node. You are of course welcome to add extra data, such as position coordinates, depending on the specifics of your program. 
3. The last six lines describe the edges, using the temporary ids of the nodes to identify the endpoints of each edge. The last column provides a data value for each edge, which is the edge weight. 

You may augment this file format with additional information, if you wish. For example, including position coordinates for each node (as mentioned above) would probably be important if your file will work within a GUI environment, so that you can know where to draw the node. If you are generating your own data, please ensure that is sufficiently complex to be interesting, and submit it with the rest of your work.

## Phase Two: Computation

In this phase you will perform computation on your graph.  Start with simple statistics -- the number of nodes, number of edges, maximum node degree, average node degree, etc.

In the next step, think about what interests you about this dataset and compute something you find interesting using one of the graph algorithms we have studied.  For example, you could try to find a path between two arbitrary nodes using either breadth-first or depth-first traversal.  If the nodes have costs associated, you could search for the shortest path using Dijkstra's algorithm.  If you are feeling ambitious, you could compute network flow capacity and/or chokepoints in the network.  Depending on what you choose to do, there may be a choice between writing your own implementation or using an existing method included with Guava.  Either is fine.

## Phase Three: Output

For this phase you will find some way to communicate the results of your main computation in the previous phase to show your project's findings. Since this phase is closely linked to phase two, you may find it easiest to work on them simultaneously, but we separate them here for clarity.

One way you could do this is by producing a report as text output, either to the terminal or to a separate file.  

Another possibility is to display it visually.  (This may work best for graphs without too many nodes.)  You can use the `GraphDisplay` class shared with the Guava demo for this purpose.  Your visualization can work either as an animation, like we did with the maze, or as a static result.

## Final Documentation and Deliverables

When you turn in your work on this project, be sure to include each of the following:
* Your reflection on the assignment, including your development journal.  Both of these should be inside `readme.md`, in clearly marked sections.  Also include here the list of those you consulted with and a bibliography of any web sites consulted beyond the standard javadoc pages.
* All the source files needed to compile your program
* All the data files needed to run your program, plus the `data_description.txt` file
* In a file named `demo.txt`, an example showing a sample run of your program.  This should be something we can reproduce while testing your program ourselves.  If the output is graphical, you should also include a screenshot of the results called `screenshot.png`.
* a completed `checklist.txt`