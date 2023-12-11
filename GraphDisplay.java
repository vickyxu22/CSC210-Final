package demo;

import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import com.google.common.graph.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to display a graph on screen
 *
 * @author N. Howe
 * @version November 2023
 */
public class GraphDisplay extends JComponent implements ActionListener {
  
  // only one of the following three should be active
  /** The Graph to display */
  Graph<Object> graph;
  /** The ValueGraph to display */
  ValueGraph<Object,Object> vgraph;
  /** The Network to display */
  Network<Object,Object> net;

  /** Map graph objects to locations */
  HashMap<Object,Point> locMap;
  
  /** Map graph objects to colors */
  HashMap<Object,Color> colorMap;

  /** Map graph objects to labels */
  HashMap<Object,String> labelMap;

  /** Map graph objects to notes */
  HashMap<Object,String> noteMap;
  
  /** Window the graph will appear in */
  private JFrame frame;

  /** TImer for callbacks */
  private Timer timer;
  
  /** Location of current drag */
  Point dragPoint = null;

  /** Remembers node where last mousedown event occurred */
  Object activeNode;

  /** Size of canvas */
  public static final Dimension CANVAS_SIZE = new Dimension(1000, 800);

  /** Radius of nodes */
  public static final int NODE_RADIUS = 16;

  /** Radius to draw arrows */
  public static final int ARROW_RADIUS = NODE_RADIUS+4;
  
  /** default color of nodes */
  public static final Color DEFAULT_NODE_COLOR = new Color(192, 192, 255);

  /** default color of edges */
  public static final Color DEFAULT_EDGE_COLOR = Color.BLUE;

  /** default color of edges */
  public Point labelOffset = new Point(0,24);

  /** default color of edges */
  public Point noteOffset = new Point(NODE_RADIUS,-NODE_RADIUS);
  
  /** used to draw arrows */
  private static final AffineTransform tx = new AffineTransform();

  /** used to draw arrows */
  private static final Line2D.Double line = new Line2D.Double(0,0,100,100);

  /** used to draw arrows */
  private static final Polygon arrowHead = new Polygon();  

  static {
    arrowHead.addPoint( 0,4);
    arrowHead.addPoint( -4, -4);
    arrowHead.addPoint( 4,-4);
  }

  /** Constructor starts with empty graph */
  public GraphDisplay(Object g) {
    super();
    if (g instanceof Graph) {
      this.graph = (Graph)g;
    } else if (g instanceof ValueGraph) {
       this.vgraph = (ValueGraph)g;
    } else if (g instanceof Network) {
      this.net = (Network)g;
    } else {
      throw new RuntimeException("Attempt to display non-graph object: "+g);
    }
    locMap = new HashMap<Object,Point>();
    assignLocations();
    colorMap = new HashMap<Object,Color>();
    labelMap = new HashMap<Object,String>();
    noteMap = new HashMap<Object,String>();

    highlightNodeWithMaxDegree();
    colorBasedOnDegree();
    setMinimumSize(CANVAS_SIZE);
    setPreferredSize(CANVAS_SIZE);
    openWindow();
  }

  public void highlightNodeWithMaxDegree() {
    int maxDegree = 0;
    Set<Object> nodesWithMaxDegree = new HashSet<>();

    // Find the maximum degree among nodes
    for (Object node : getNodeSet()) {
        int degree = getAdjacentNodes(node).size();
        if (degree > maxDegree) {
            maxDegree = degree;
            nodesWithMaxDegree.clear();
            nodesWithMaxDegree.add(node);
        } else if (degree == maxDegree) {
            nodesWithMaxDegree.add(node);
        }
    }

    // Set color of node(s) with maximum degree to green
    for (Object node : nodesWithMaxDegree) {
        setColor(node, Color.GREEN);
    }
  }

  public void colorBasedOnDegree() {
    int maxDegree = 0;
    int minDegree = Integer.MAX_VALUE;

    // Find the maximum and minimum degrees among nodes
    for (Object node : getNodeSet()) {
        int degree = getAdjacentNodes(node).size();
        if (degree > maxDegree) {
            maxDegree = degree;
        }
        if (degree < minDegree) {
            minDegree = degree;
        }
    }

    // Set colors based on degree (using a gradient from lighter to darker)
    for (Object node : getNodeSet()) {
        int degree = getAdjacentNodes(node).size();

        // Calculate a color intensity value based on the node's degree
        // Here, darker colors represent higher degrees using a gradient
        int colorIntensity = 255 - (int) (255 * ((double) (degree - minDegree) / (maxDegree - minDegree)));
        Color nodeColor = new Color(colorIntensity, colorIntensity, colorIntensity);

        setColor(node, nodeColor);
    }
}

  /** Assigns nodes to points around an oval */
  public void assignLocations() {
    Set<Object> nodes = getNodeSet();
    int i = 0;
    int num = nodes.size();
    int w = CANVAS_SIZE.width;
    int h = CANVAS_SIZE.height;
    for (Object n : nodes) {
      double angle = Math.PI/2+((2*i+0.5)*Math.PI)/num;
      setLoc(n, new Point((int)(w/2.0+w*Math.cos(angle)/2.5),(int)(h/2.0+h*Math.sin(angle)/2.5)));
      i++;
    }
  }

  /** Sets up the GUI window */
  private void openWindow() {
    // Make sure we have nice window decorations.
    JFrame.setDefaultLookAndFeelDecorated(true);

    // Create and set up the window.
    frame = new JFrame("Graph Display");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Add components
    createComponents(frame);

    // Display the window.
    frame.pack();
    frame.setVisible(true);

    // Add listener for drag events
    DragListener dl = new DragListener();
    this.addMouseListener(dl);
    this.addMouseMotionListener(dl);

    // Begin animation callback events
    timer = new Timer(25, this);
    timer.setInitialDelay(500);
    timer.start(); 
  }

  private void createComponents(JFrame frame) {
    Container pane = frame.getContentPane();
    pane.add(this);
  }


  /** Returns the node under the given location, or null if none */
  public Object getNode(int x, int y) {
    Object result = null;
    for (Object n : getNodeSet()) {
      //System.out.println("Node "+n+": "+getLoc(n).distance(x, y));
      if (getLoc(n).distance(x, y) <= NODE_RADIUS) {
        result = n;
      }
    }      
    return result;
  }

  /** Returns the location of a given graph element */
  public Point getLoc(Object obj) {
    Point loc = locMap.get(obj);
    if (loc == null) {
      loc = new Point(ThreadLocalRandom.current().nextInt(0,CANVAS_SIZE.width),
                        ThreadLocalRandom.current().nextInt(0,CANVAS_SIZE.height));
      locMap.put(obj,loc);
    }
    return loc;
  }

  /** Sets the location of a given graph element */
  public void setLoc(Object obj, Point loc) {
    locMap.put(obj,loc);
  }

  /** Sets multiple node locations at once */
  public void setLocs(HashMap<?,? extends Point> locs) {
    locMap.putAll(locs);
  }
  
  /** Returns the color of a given graph element */
  public Color getColor(Object obj) {
    Color c = colorMap.get(obj);
    if (c == null) {
      if (getNodeSet().contains(obj)) {
        c = DEFAULT_NODE_COLOR;
      } else {
        c = DEFAULT_EDGE_COLOR;
      }
    }
    return c;
  }

  /** Sets the color of a given graph element */
  public void setColor(Object obj, Color c) {
    colorMap.put(obj,c);
  }

  /** Sets multiple colors at once */
  public void setColors(HashMap<?,? extends Color> colors) {
    colorMap.putAll(colors);
  }
  
  /** Returns the label of a given graph element */
  public String getLabel(Object obj) {
    if (obj == null) {
      return null;
    }
    String lbl = labelMap.get(obj);
    if (lbl == null) {
      lbl = obj.toString();
    }
    return lbl;
  }

  /** Sets the label of a given graph element */
  public void setLabel(Object obj, String lbl) {
    labelMap.put(obj,lbl);
  }

  /** Sets multiple labels at once */
  public void setLabels(HashMap<?,? extends String> labels) {
    labelMap.putAll(labels);
  }
  
  /** Returns the note on a given graph element */
  public String getNote(Object obj) {
    String note = noteMap.get(obj);
    if (note == null) {
        note = "";
    }
    return note;
  }

  /** Sets the note on a given graph element */
  public void setNote(Object obj, String note) {
    noteMap.put(obj,note);
  }

  /** Sets multiple labels at once */
  public void setNotes(HashMap<?,? extends String> notes) {
    noteMap.putAll(notes);
  }
  
  /** Reset colors to default */
  public void setNodeColors(Color c) {
    for (Object n : getNodeSet()) {
      colorMap.put(n,c);
    }
  }

  /** Reset colors to default */
  public void setEdgeColors(Color c) {
    for (Object e : getEdgeSet()) {
      colorMap.put(e,c);
    }
  }

  /** returns the node set */
  public Set<Object> getNodeSet() {
    Set<Object> nodes;
    if (graph != null) {
      nodes = graph.nodes();
    }else if (vgraph != null) {
      nodes = vgraph.nodes();
    }else if (net != null) {
      nodes = net.nodes();
    } else {
      nodes = new HashSet<Object>();
    }
    return nodes;
  }

  /** returns a representation of the edge between two nodes */
  public Object getEdgeBetween(Object n1, Object n2) {
    Object e = null;
    if (((graph != null)&&graph.hasEdgeConnecting(n1,n2))
        ||((vgraph != null)&&vgraph.hasEdgeConnecting(n1,n2))) {
      if (((graph != null)&&graph.isDirected())||vgraph.isDirected()) {
        e = new Pair<Object,Object>(n1,n2);
      } else {
        e = new Diset<Object>(n1,n2);
      }
    } else if (net != null) {
      if (net.hasEdgeConnecting(n1,n2)) {
        e = net.edgeConnecting(n1,n2).get();
      }
    }
    return e;
  }
  
  /** returns the edge set */
  public Set<Object> getEdgeSet() {
    Set<Object> edges = new HashSet<Object>();
    Set<Object> nodes = getNodeSet();
    for (Object n : nodes) {
      if (net != null) {
        edges = net.edges();
      } else {
        Set<Object> succ;
        if (graph != null) {
          succ = graph.successors(n);
        } else if (vgraph != null) {
          succ = graph.successors(n);        
        } else {
          return edges;
        }
        for (Object s : succ) {
          edges.add(getEdgeBetween(n,s));
        }
      }   
    }
    return edges;
  }
  
  /** returns the node set */
  public Set getAdjacentNodes(Object n) {
    Set edges;
    if (graph != null) {
      edges = graph.adjacentNodes(n);
    }else if (vgraph != null) {
      edges = vgraph.adjacentNodes(n);
    }else if (net != null) {
      edges = net.successors(n);
    } else {
      edges = new HashSet<Object>();
    }
    return edges;
  }

  /** for drawing arrows
  * see https://stackoverflow.com/questions/2027613/how-to-draw-a-directed-arrow-line-in-java
  */
  private void drawArrow(Point p1, Point p2, Graphics g) {
    g.drawLine(p1.x,p1.y,p2.x,p2.y);
    tx.setToIdentity();
    double angle = Math.atan2(p2.y-p1.y, p2.x-p1.x);
    tx.translate(p2.x-ARROW_RADIUS*Math.cos(angle),p2.y-ARROW_RADIUS*Math.sin(angle));
    tx.rotate((angle-Math.PI/2d));  

    Graphics2D g2d = (Graphics2D) g.create();
    g2d.setTransform(tx);   
    g2d.fill(arrowHead);
    g2d.dispose();
  }

  /** for drawing rotated text
   *  see https://stackoverflow.com/questions/10083913/how-to-rotate-text-with-graphics2d-in-java
   */
  private void rotateText(String text, Point p1, Point p2, Graphics g) {
    if (text==null) {
      return;
    }
    double angle = Math.atan((p2.y-p1.y)/(double)(p2.x-p1.x));
    if (angle==-Math.PI/2) {
      angle = angle+Math.PI;
    }
    //System.out.println(angle);
    Graphics2D g2d = (Graphics2D) g.create();
    Font font = new Font(null, Font.PLAIN, 12);    

    Rectangle2D sbound = g.getFontMetrics().getStringBounds(text, g);
    int descent = g.getFontMetrics().getDescent();
    int ascent = g.getFontMetrics().getAscent();
    AffineTransform affineTransform = new AffineTransform();
    affineTransform.rotate(angle, 0, 0);
    //affineTransform.rotate(Math.toRadians(angle), 0, 0);    
    affineTransform.translate(-sbound.getWidth()/2,-2);

    Font rotatedFont = font.deriveFont(affineTransform);
    g2d.setFont(rotatedFont);
    g2d.drawString(text,(p1.x+p2.x)/2,(p1.y+p2.y)/2);
    g2d.dispose();
    //System.out.println("Edge: "+text);
  }
  
  /** Draws the graph in a window */
  public void paintComponent(Graphics g) {
    //System.out.println("Entering paintComponent.");
    
    // get node collection (varies depending on graph type)
    Set nodes = getNodeSet();
    //System.out.println("Nodes: "+nodes);
    
    // draw edges
    for (Object n : nodes) {
      Point loc = getLoc(n);
      for (Object e : getAdjacentNodes(n)) {
        Point dloc = getLoc(e);
        drawArrow(loc,dloc,g);

        // add text
        rotateText(getLabel(getEdgeBetween(n,e)),loc,dloc,g);
      }
    }

    // draw nodes
    for (Object n : nodes) {
      Point pos = getLoc(n);
      g.setColor(getColor(n));
      g.fillOval(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS,
              2 * NODE_RADIUS, 2 * NODE_RADIUS);
      g.setColor(Color.black);
      g.drawOval(pos.x - NODE_RADIUS, pos.y - NODE_RADIUS,
              2 * NODE_RADIUS, 2 * NODE_RADIUS);
      String label = getLabel(n);
      Rectangle2D sbound = g.getFontMetrics().getStringBounds(label, g);
      int descent = g.getFontMetrics().getDescent();
      int ascent = g.getFontMetrics().getAscent();
      g.drawString(label, pos.x - (int) sbound.getWidth() / 2 + labelOffset.x,
              pos.y + (int) (sbound.getHeight()) / 2 - descent + labelOffset.y);
      String note = getNote(n);
      Rectangle2D nbound = g.getFontMetrics().getStringBounds(note, g);
      descent = g.getFontMetrics().getDescent();
      ascent = g.getFontMetrics().getAscent();
      g.drawString(note, pos.x - (int) nbound.getWidth() / 2 + noteOffset.x,
              pos.y + NODE_RADIUS + (int) (nbound.getHeight()) - descent + noteOffset.y);
    }
  }

  /** Timer callback causes the window to be repainted */
  public void actionPerformed(ActionEvent e) {
    repaint();
  }

  /** listener class for drag events */
  private class DragListener extends MouseAdapter {
    /** mouse press event handler */
    public void mousePressed(MouseEvent e) {
      dragPoint = new Point(e.getX(),e.getY());
      //System.out.println("Point set: "+dragPoint);
    }

    /** Release event handler */
    public void mouseReleased(MouseEvent e) {
      //System.out.println("Point cleared. ");
      dragPoint = null;
      activeNode = null;
    }

    /** event handler for drag events */
    public void mouseDragged(MouseEvent e) {
      //System.out.println("p: "+dragPoint+" node: "+activeNode);
      if (activeNode == null) {
        //System.out.println("Getting node");
        activeNode = getNode(dragPoint.x, dragPoint.y);
      } 
      Point loc = getLoc(activeNode);
      loc.x = e.getX();
      loc.y = e.getY();
      //System.out.println("loc: "+loc+" "+activeNode);
      loc = getLoc(activeNode);
      //System.out.println("loc: "+getLoc(activeNode));
      repaint();
    }
  }


  /** class to represent an ordered pair for a directed edge */
  public class Pair<T,U> {
    /** one end of the pair */
    private final T p1;
    /** the other end of the pair */
    private final U p2;

    /** constructor for pair */
    public Pair(T p1, U p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    // from https://www.sitepoint.com/implement-javas-equals-method-correctly/
    /** equal if ends are equal */
    @Override
    public boolean equals(Object o) {
      if (this == o) // self check
          return true;
      if (o == null) // null check
          return false;
      if (getClass() != o.getClass()) // type check and cast
          return false;
      Pair p = (Pair) o;
      return Objects.equals(p1, p.p1) && Objects.equals(p2, p.p2); // field comparison
    }

    // from https://www.sitepoint.com/how-to-implement-javas-hashcode-correctly/
    /** both endpoints contribute to hashcode */
    @Override
    public int hashCode() {
      return Objects.hash(p1, p2);
    }

    /** return an appropriate default string */
    @Override
    public String toString() {
      String result;
      if ((vgraph != null)&&vgraph.hasEdgeConnecting(p1,p2)) {
        //System.out.println(vgraph.edgeValue(p1,p2));
        result = vgraph.edgeValue(p1,p2).get().toString();
      } else {
        result = "";
      }
      return result;
    }
  }

  /** class to represent a diset for an undirected edge */
  public class Diset<T> {
    /** one end of the pair */
    private final T p1;
    /** the other end of the pair */
    private final T p2;

    /** constructor for pair */
    public Diset(T p1, T p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    // from https://www.sitepoint.com/implement-javas-equals-method-correctly/
    /** equal if ends are equal */
    @Override
    public boolean equals(Object o) {
      if (this == o) // self check
          return true;
      if (o == null) // null check
          return false;
      if (getClass() != o.getClass()) // type check and cast
          return false;
      Pair p = (Pair) o;
      return (Objects.equals(p1, p.p1) && Objects.equals(p2, p.p2))
        ||(Objects.equals(p2, p.p1) && Objects.equals(p1, p.p2)); // field comparison
    }

    // from https://www.sitepoint.com/how-to-implement-javas-hashcode-correctly/
    /** both endpoints contribute to hashcode */
    @Override
    public int hashCode() {
      return Objects.hash(p1)+Objects.hash(p2);
    }

    /** return an appropriate default string */
    @Override
    public String toString() {
      String result;
      if ((vgraph != null)&&vgraph.hasEdgeConnecting(p1,p2)) {
        //System.out.println(vgraph.edgeValue(p1,p2));
        result = vgraph.edgeValue(p1,p2).get().toString();
      } else {
        result = "";
      }
      return result;
    }
  }
}
