import java.util.Random;
import java.util.ArrayList;

class Point{
	double x,y;
	// Set of edges attached to this point
	public ArrayList<Edge> edges;
	public Point(double bx,double by){
		x = bx; y = by;
	}
	// Returns true if the given point is the same as this one.
	public boolean same(Point b){
		return (x == b.x && y == b.y);
	}
	// Add an edge connection if not present; lazily creates the edge array/
	public void addEdge(Edge e){
		if (edges==null) edges = new ArrayList<Edge>();
		if (!edges.contains(e))
			edges.add(e);
	}
	public String toString(){
		return "("+x+","+y+")";
	}
}

class Edge{
	Point p,q;
	double l;
	public volatile ArrayList<Triangle> triangles = new ArrayList<Triangle>();
	public Edge(Point p1,Point p2){ 
		p = p1;
		q = p2;
		l = Math.sqrt((p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y));
	}
	// Utility routine -- 2d cross-product (signed area of a triangle) test for orientation.
	public int sat(double p0x,double p0y,double p1x,double p1y,double p2x,double p2y){
		double d = (p1x-p0x)*(p2y-p0y)-(p2x-p0x)*(p1y-p0y);
		if (d<0) return -1;
		if (d>0) return 1;
		return 0;
	}
	public boolean same(Edge e){
		if (p.same(e.p) && q.same(e.q) || p.same(e.q) && q.same(e.p)){
			return true;
		}
		return false;
	}
	// Returns true if the given edge intersects this edge.
	public boolean intersects(Edge e){
		int s1 = sat(p.x,p.y, q.x,q.y, e.p.x,e.p.y);
		int s2 = sat(p.x,p.y, q.x,q.y, e.q.x,e.q.y);
		if (s1==s2 || (s1==0 && s2!=0) || (s2==0 && s1!=0)) return false;
		s1 = sat(e.p.x,e.p.y, e.q.x,e.q.y, p.x,p.y);
		s2 = sat(e.p.x,e.p.y, e.q.x,e.q.y, q.x,q.y);
		if (s1==s2 || (s1==0 && s2!=0) || (s2==0 && s1!=0)) return false;
		return true;
	}
	public double getLength(){
		return l;
	}
	public Point sharePoint(Edge e){
		if (e.p.same(p) || e.q.same(p)){
			return p;
		}
		if (e.p.same(q) || e.q.same(q)){
			return q;
		}
		return null;
	}
	public void addTriangle(Triangle t){
		if (!triangles.contains(t)){
			triangles.add(t);
		}
	}
	public String toString(){
		return "<"+p+","+q+">";
	}
}

class Triangle{
	Edge ex,ey,ez;
	Point px,py,pz;
	public Triangle(Edge exi, Edge eyi, Edge ezi){
		ex=exi;
		ey=eyi;
		ez = ezi;
		px = ex.p;
		py = ex.q;
		if (!ey.p.same(px) && !ey.p.same(py)){
			pz = ey.p;
		}
		else {
			pz = ey.q;
		}
	}
	public Edge[] otherEdge(Edge e){
		if (ex.same(e)){
			Edge[] e1 = {ey,ez};
			return e1;
		}
		if (ey.same(e)){
			Edge[] e1 = {ex,ez};
			return e1;
		}
		if (ez.same(e)){
			Edge[] e1 = {ex,ey};
			return e1;
		}
		return null;
	}
	public double area(Point a, Point b, Point c){
		return Math.abs((a.x*(b.y-c.y)+b.x*(c.y-a.y)+c.x*(a.y-b.y))/2.0);
	}
	public boolean containPoint(Point p){
		double a = area(px,py,pz);
		double a1 = area(px,py,p);
		double a2 = area(px,pz,p);
		double a3 = area(py,pz,p);
		if (a1!=0 && a2!=0 && a3!=0 && (a1+a2+a3)==a){
			return true;
		}
		return false;
	}
	public String toString(){
		return "<"+px.toString()+","+py.toString()+","+pz.toString()+">";
	}
}

public class triangulation{
	public static int n,t;
	public volatile static Point[] points; 
	public volatile static ArrayList<Edge> edges = new ArrayList<Edge>();
	public volatile static boolean[] changed;
	public static volatile int num;
	// Returns true if any existing edge intersects this one
	public static boolean intersection(Edge f){
		for (Edge e : edges){
			if (f.intersects(e)){
				return true;
			}
		}
		return false;
	}
	public static boolean checkchange(){
		for (int i = 0; i<t; i++){
			if (changed[i] == true){
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args){
		try {
			Random r;
			n = Integer.parseInt(args[0]);
			t = Integer.parseInt(args[1]);
			if (args.length>2){
				r = new Random(Integer.parseInt(args[2]));
			}
			else{
				r = new Random();
			}
			points = new Point[n];
			// First, create a set of unique points
			points[0] = new Point(0.0,0.0);
			points[1] = new Point(0.0,1.0);
			points[2] = new Point(1.0,1.0);
			points[3] = new Point(1.0,0.0);
			for (int i=4;i<n;i++){
				boolean repeat;
				Point np = null;
				do{
					repeat = false;
					np = new Point(r.nextDouble(),r.nextDouble());
					// Verify it is a distinct point.
					for (int j=0;j<i;j++){
						if (np.same(points[j])){
							repeat = true;
							break;
						}
					}
				}
				while(repeat);
				points[i] = np;
			}
			System.out.println("Generated points");
			// Triangulate
			for (int i=0;i<n;i++){
				for (int j=i+1;j<n;j++){
					Edge e = new Edge(points[i],points[j]);
					if (!intersection(e)){
						edges.add(e);
						e.p.addEdge(e);
						e.q.addEdge(e);
					}
				}
			}
			System.out.println("Triangulated: "+n+" points, "+edges.size()+" edges");
			boolean inside;
			/*System.out.println("Old triangles: ");*/
			for (int i = 0; i < edges.size()-2; i++){
				for (int j = i+1; j < edges.size()-1; j++){
					for (int k = j+1; k < edges.size(); k++){
						inside = false;
						Point p1 = edges.get(i).sharePoint(edges.get(j));
						Point p2 = edges.get(i).sharePoint(edges.get(k));
						Point p3 = edges.get(j).sharePoint(edges.get(k));
						if (p1 != null && p2 != null && p3 != null &&
							p1 != p2 && p1 != p3 && p2 != p3){
							Triangle t = new Triangle(edges.get(i),edges.get(j),edges.get(k));
							for (int l = 0; l < points.length; l++){
								if (t.containPoint(points[l])){
									inside = true;
									break;
								}
							}
							if (!inside){
								edges.get(i).addTriangle(t);
								edges.get(j).addTriangle(t);
								edges.get(k).addTriangle(t);
								/*System.out.println(t.toString());*/
							}
						}
					}
				}
			}
			changed = new boolean[t];
			for (int i = 0; i<t; i++){
				changed[i] = true;
			}
			/*System.out.println("New triangles: ");*/
			ArrayList<Thread> thread_list = new ArrayList<Thread>();
			long start = System.currentTimeMillis();
			for (int i = 0; i < t; i++){
				MyThread temp = new MyThread(i, points, edges);
				temp.start();
				thread_list.add(temp);
			}
			for (int i = 0; i < t; i++){
				thread_list.get(i).join();
			}
			long end = System.currentTimeMillis();
			System.out.println("Number of flips: " + num);
			System.out.println("The time spent: " + (end - start));
		}
		catch (Exception e){
			System.out.println("ERROR " +e);
			e.printStackTrace();
		}
	}

	public static class MyThread extends Thread{
		private int id;
		private static Point[] points; 
		private static ArrayList<Edge> edges = new ArrayList<Edge>();
		MyThread(int id, Point[] points, ArrayList<Edge> edges){
			this.id = id;
			this.points = points;
			this.edges = edges;
		}
		synchronized public void run(){
			int length = (edges.size()/t)+1;
			int start = id*length;
			int end = (id+1)*length;
			if (id == t-1){
				end = edges.size();
			}
			while (checkchange()){
				changed[id] = false;
				// iterating through all the edges
				for (int i = start; i < end; i++){
					if (i < edges.size()){
						if (edges.get(i)!=null){
						// iterating through all pairs of triangles related to that edge
							for (int j = 0; j < edges.get(i).triangles.size()-1; j++){
								for (int k = j+1; k < edges.get(i).triangles.size(); k++){
									if (j < edges.get(i).triangles.size() && k < edges.get(i).triangles.size()){
										if (edges.get(i).triangles.get(j)!=null && edges.get(i).triangles.get(k)!=null){
											Triangle o1 = edges.get(i).triangles.get(j);
											Triangle o2 = edges.get(i).triangles.get(k);
											Edge[] temp1 = o1.otherEdge(edges.get(i));
											Edge[] temp2 = o2.otherEdge(edges.get(i));
											double cos1 = (temp1[0].l*temp1[0].l+temp1[1].l*temp1[1].l
															-edges.get(i).l*edges.get(i).l)/(2*temp1[0].l*temp1[1].l);
											double cos2 = (temp2[0].l*temp2[0].l+temp2[1].l*temp2[1].l
															-edges.get(i).l*edges.get(i).l)/(2*temp2[0].l*temp2[1].l);
											double acos1 = Math.acos(cos1);
											if (acos1 > Math.PI){
												acos1 = 2*Math.PI - acos1;
											}
											double acos2 = Math.acos(cos2);
											if (acos2 > Math.PI){
												acos2 = 2*Math.PI - acos2;
											}
											if (acos1+acos2 > Math.PI){
												changed[id] = true;
												num++;
												Edge e1 = new Edge(temp1[0].sharePoint(temp1[1]),temp2[0].sharePoint(temp2[1]));
												if (temp1[0].sharePoint(temp2[0]) != null){
													Triangle t1 = new Triangle(e1,temp1[0],temp2[0]);
													Triangle t2 = new Triangle(e1,temp1[1],temp2[1]);
													e1.addTriangle(t1);
													e1.addTriangle(t2);
													temp1[0].addTriangle(t1);
													temp2[0].addTriangle(t1);
													temp1[1].addTriangle(t2);
													temp2[1].addTriangle(t2);
													temp1[0].triangles.remove(o1);
													temp1[0].triangles.remove(o2);
													temp1[1].triangles.remove(o1);
													temp1[1].triangles.remove(o2);
													temp2[0].triangles.remove(o1);
													temp2[0].triangles.remove(o2);
													temp2[1].triangles.remove(o1);
													temp2[1].triangles.remove(o2);
													/*System.out.println(t1.toString());
													System.out.println(t2.toString());*/
												}
												else {
													Triangle t1 = new Triangle(e1,temp1[0],temp2[1]);
													Triangle t2 = new Triangle(e1,temp1[1],temp2[0]);
													e1.addTriangle(t1);
													e1.addTriangle(t2);
													temp1[0].addTriangle(t1);
													temp2[1].addTriangle(t1);
													temp1[1].addTriangle(t2);
													temp2[0].addTriangle(t2);
													temp1[0].triangles.remove(o1);
													temp1[0].triangles.remove(o2);
													temp1[1].triangles.remove(o1);
													temp1[1].triangles.remove(o2);
													temp2[0].triangles.remove(o1);
													temp2[0].triangles.remove(o2);
													temp2[1].triangles.remove(o1);
													temp2[1].triangles.remove(o2);
													/*System.out.println(t1.toString());
													System.out.println(t2.toString());*/
												}
												edges.set(i,e1);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}