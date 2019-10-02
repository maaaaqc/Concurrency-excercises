import java.util.ArrayList;
import java.util.concurrent.*;
import java.awt.geom.*;

public class game_map{
	static int p;
	static int n;
	static int b;
	static volatile int taskcount;
	static double r;
	static Map gameMap;
	public static void main(String[] args){
		if (args.length != 4){
			System.out.println("Invalid number of arguments.");
			System.exit(0);
		}
		try{
			p = Integer.parseInt(args[0]);
			assert p > 0;
			n = Integer.parseInt(args[1]);
			assert n > 0;
			b = Integer.parseInt(args[2]);
			assert b > 1;
			r = Double.parseDouble(args[3]);
			assert r > 0;
		} catch (AssertionError e){
			e.printStackTrace();
		}
		gameMap = new Map(n, b, r);
		long start = System.currentTimeMillis();
		ExecutorService executorService = Executors.newFixedThreadPool(p);
		while (gameMap.nodes.size() < n){
			try{
				Callable<Node> task = new Task(gameMap);
				Future<Node> future = executorService.submit(task);
				while (future.get() == null){
					task = new Task(gameMap);
					executorService.submit(task);
				}
				taskcount++;
			} catch (InterruptedException e){
				e.printStackTrace();
			} catch (ExecutionException e){
				e.printStackTrace();
			}
		}
		executorService.shutdown();
		while (!executorService.isTerminated());
		long end = System.currentTimeMillis();
		System.out.println("Tasks done: " + taskcount + " tasks");
		System.out.println("Time used: " + (end-start) + " milliseconds");
	}
}

class Map{
	final int width = 1;
	final int length = 1;
	final int numObstacles = 20;
	final double obstacleSize = 0.05;
	int n;
	int b;
	double r;
	Node root;
	ArrayList<Obstacle> obstacles;
	ArrayList<Node> nodes;
	Task task;
	public Map(int mn, int mb, double mr){
		// initializes the map
		n = mn;
		b = mb;
		r = mr;
		obstacles = new ArrayList<Obstacle>();
		for (int i = 0; i < numObstacles; i++){
			Obstacle temp = generateRandomObstacle();
			obstacles.add(temp);
		}
		nodes = new ArrayList<Node>();
		root = generateRandomNode();
		nodes.add(root);
	}
	// checks if an object is in map
	public boolean inBound(Node a){
		if (a.x >= 0 && a.x <= width){
			if (a.y >= 0 && a.y <= length){
				return true;
			}
		}
		return false;
	}
	public boolean inBound(Obstacle a){
		if (a.getMinX() >= 0 && a.getMaxX() <= width){
			if (a.getMinY() >= 0 && a.getMaxY() <= length){
				return true;
			}
		}
		return false;
	}
	// checks if inside an obstacle
	public boolean insideObstacles(Node a){
		for (Obstacle o: obstacles){
			if (a.x >= o.getMinX() && a.x <= o.getMaxX()){
				if (a.y >= o.getMinY() && a.y <= o.getMaxY()){
					return true;
				}
			}
		}
		return false;
	}
	// checks if an edge can grow
	public boolean intersectObstacles(Edge a){
		for (Obstacle o: obstacles){
			if (o.intersectsLine(a)){
				return true;
			}
		}
		return false;
	}
	// generate random feasible solutions
	public Node generateRandomNode(){
		Node temp = new Node(Math.random(), Math.random());
		while (this.insideObstacles(temp) || !this.inBound(temp)){
			temp = new Node(Math.random(), Math.random());
		}
		return temp;
	}
	public Node generateRandomNode(Node parent){
		Node temp = new Node(((Math.random()*r-0.5*r)/Math.sqrt(2))+parent.x, 
							((Math.random()*r-0.5*r)/Math.sqrt(2))+parent.y);
		while (this.insideObstacles(temp) || !this.inBound(temp)){
			temp = new Node(((Math.random()*r-0.5*r)/Math.sqrt(2))+parent.x, 
							((Math.random()*r-0.5*r)/Math.sqrt(2))+parent.y);
		}
		return temp;
	}
	public Obstacle generateRandomObstacle(){
		Obstacle temp = new Obstacle(Math.random(), Math.random(), obstacleSize);
		while (!this.inBound(temp)){
			temp = new Obstacle(Math.random(), Math.random(), obstacleSize);
		}
		return temp;
	}
	public Node generateChildNode(Node parent){
		if (!parent.isFull()){
			Node child = generateRandomNode(parent);
			Edge temp = new Edge(parent, child);
			while (temp.l > r || this.intersectObstacles(temp)){
				child = generateRandomNode(parent);
				temp = new Edge(parent, child);
			}
			parent.children.add(child);
			this.nodes.add(child);
			return child;
		}
		return null;
	}
}

class Node extends Point2D.Double{
	double x,y;
	volatile ArrayList<Node> children;
	public Node(double px, double py){
		x = px;
		y = py;
		children = new ArrayList<Node>();
	}
	public boolean isFull(){
		if (children.size() < q1.b){
			return false;
		}
		return true;
	}
}

class Obstacle extends Rectangle2D.Double{
	double x,y;
	double height, width;
	public Obstacle(double px, double py, double size){
		x = px;
		y = py;
		height = size;
		width = size;
	}
}

class Edge extends Line2D.Double{
	double x1, x2, y1, y2;
	double l;
	public Edge(Node a, Node b){
		x1 = a.x;
		x2 = b.x;
		y1 = a.y;
		y2 = b.y;
		l = Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
	}
}

class Task implements Callable<Node>{
	Map map;
	public Task(Map m) {
		map = m;
	}
	@Override
	public Node call() throws Exception{
		// breadth first search
		Node cur = map.root;
		int i = 0;
		while (cur.isFull() && i < map.nodes.size()-1){
			i++;
			cur = map.nodes.get(i);
		}
		Node node = map.generateChildNode(cur);
		return node;
	}
}