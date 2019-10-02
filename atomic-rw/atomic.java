import java.util.Timer;
import java.util.TimerTask;

public class atomic{

	private static volatile Node first;
	private static volatile Node last;

	public static Timer timer = new Timer();
	public static Helper task = new Helper();

	public static void main(String[] args) throws InterruptedException{
		Node a = new Node('A');
		Node b = new Node('B');
		Node c = new Node('C');
		a.set_next(b);
		b.set_next(c);
		first = a;
		last = c;
		last.set_next(a);
		Thread t0 = new Thread0(first);
		Thread t1 = new Thread1(first);
		Thread t2 = new Thread2(first);
		initialize();
		t0.start();
		t1.start();
		t2.start();
		t0.join();
		t1.join();
		t2.join();
		Node iterator = first;
		System.out.print("\nFinal list: ");
		while (iterator.get_next() != first){
			System.out.print(iterator.get_char() + " ");
			iterator = iterator.get_next();
		}
		System.out.println(iterator.get_char());
	}

	public static void initialize(){
		timer.schedule(task, 0, 1000);
	}

	public static class Thread0 extends Thread{
		Node current;
		Thread0 (Node a){
			this.current = a;
		}
		public void run(){
			while (task.get_time() <= 5){
				System.out.print(current.get_char() + " ");
				current = current.get_next();
				try {
					this.sleep(100);
				}
				catch (InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}

	public static class Thread1 extends Thread{
		Node current;
		Thread1 (Node a){
			this.current = a;
		}
		public void run(){
			double rand;
			while (task.get_time() <= 5){
				rand = Math.random();
				if (rand >= 0 && rand < 0.1){
					Node temp = current.get_next();
					if (temp.get_char() != 'A' && temp.get_char() != 'B' && temp.get_char() != 'C'){
						current.set_next(temp.get_next());
					}
				}
				current = current.get_next();
				try {
					this.sleep(20);
				}
				catch (InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}

	public static class Thread2 extends Thread{
		Node current;
		Thread2 (Node a){
			this.current = a;
		}
		public void run(){
			double rand;
			double rand_char;
			while (task.get_time() <= 5){
				rand = Math.random();
				rand_char = Math.random();
				if (rand >= 0 && rand < 0.1){
					char a = (char) Math.round(68+(90-68)*rand_char);
					Node temp = new Node(a);
					temp.set_next(current.get_next());
					current.set_next(temp);
					if (current == last){
						last = temp;
					}
				}
				current = current.get_next();
				try {
					this.sleep(20);
				}
				catch (InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}

	public static class Node{
		private volatile char self;
		private volatile Node next;
		Node(char a){
			this.self = a;
			this.next = null;
		}
		public void set_next(Node a){
			this.next = a;
		}
		public char get_char(){
			return this.self;
		}
		public Node get_next(){
			return this.next;
		}
	}

	public static class Helper extends TimerTask{
		private static int time_passed = 0;
		public void run(){
			time_passed++;
			if (time_passed > 5){
				timer.cancel();
				timer.purge();
				return;
			}
		}
		public int get_time(){
			return this.time_passed;
		}
	}
}