import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Timer;
import java.util.TimerTask;

abstract class Chess{
	int x,y;
	public Chess(){
		while(true){
			int xtemp = (int)(Math.random()*8);
			int ytemp = (int)(Math.random()*8);
			if(!q1.board[xtemp][ytemp].occupied){
				x = xtemp;
				y = ytemp;
				q1.board[xtemp][ytemp].occupied = true;
				break;
			}
		}
	}
	public synchronized void setPos(int x, int y){
		this.x = x;
		this.y = y;
	}
	abstract int[] dest();
}

class Knight extends Chess{
	public Knight(){
		super();
	}
	@Override
	public int[] dest(){
		int[][] set = {{x-2,y-1},{x-2,y+1},{x-1,y-2},{x-1,y+2},
		{x+1,y-2},{x+1,y+2},{x+2,y-1},{x+2,y+1}};
		while(true){
			int r = (int)(Math.random() * 8);
			if(set[r][0] <= 7 && set[r][0] >= 0 && set[r][1] <= 7 && set[r][1] >= 0){
				return new int[] {set[r][0],set[r][1]};
			}
		}
	}
}

class Queen extends Chess{
	public Queen(){
		super();
	}
	@Override
	public int[] dest(){
		int rx = (int)(Math.random() * 8);
		int ry =  (int)(Math.random() * 8);
		if (rx == x){
			while (ry == y){
				ry = (int)(Math.random() * 8);
			}
			return new int[]{rx, ry};
		}
		else {
			int set[] = new int[3];
			int diff = Math.abs(x-rx);
			int counter = 0;
			set[counter++] = y;
			if (y-diff >= 0){
				set[counter++] = y-diff;
			}
			if (y+diff <= 7){
				set[counter++] = y+diff;
			}
			return new int[] {rx, set[ry%counter]};
		}
	}
}

class Square{
	int x_pos;
	int y_pos;
	Lock lock = new ReentrantLock();
	volatile boolean occupied;
	volatile Queue<Integer> queue = new LinkedList();
	public Square(int x, int y){
		x_pos = x;
		y_pos = y;
		occupied = false;
	}
}

public class chessboard {
	public static Square[][] board = new Square[8][8];
	public static ArrayList<Thread> Threads;
	public static AtomicInteger moves = new AtomicInteger(0);
	public static int t;
	public static int time;
	public static Timer timer = new Timer();
	public static TimeHelper task = new TimeHelper();

	public static void initialize(){
		for(int i = 0; i < 8; i++){
			for(int j = 0; j < 8; j++){
				board[i][j] = new Square(i,j);
			}
		}
	}

	public static class TimeHelper extends TimerTask{
		private static int timePassed = 0;
		@Override
		public void run(){
			timePassed++;
			System.out.println("Number of moves generated: " + moves);
			moves.getAndSet(0);

			if (timePassed > time){
				timer.cancel();
				timer.purge();
				return;
			}
		}
		public int getTimePassed(){
			return this.timePassed;
		}
	}

	public static void main(String[] args){
		t = Integer.parseInt(args[0]);
		time = Integer.parseInt(args[1]);
		Threads = new ArrayList(t);
		initialize();
		for(int i = 0; i < t; i++){
			double r = Math.random();
			if (r >= 0.5){
				ChessThread temp = new ChessThread(new Knight(),i);
				Threads.add(temp);
			}
			else {
				ChessThread temp = new ChessThread(new Queen(),i);
				Threads.add(temp);
			}
		}
		timer.schedule(task,0,1000);
		for (int i = 0; i < t; i++){
            Threads.get(i).start();
        }
	}

	public static class ChessThread extends Thread{
		Chess c;
		Integer id;
		public ChessThread(Chess c,int id){
			this.c = c;
			this.id = id;

		}
		@Override
		public void run(){
			while(task.getTimePassed() <= time){
				int[] pos;
				int x,y;
				do{
					pos = c.dest();
					x = pos[0];
					y = pos[1];
				} while(board[x][y].occupied);
				if(c instanceof Knight){
					board[x][y].queue.add(id);
					if(board[x][y].queue.peek() == id){
						try{
							board[x][y].lock.lock();
							board[c.x][c.y].occupied = false;
							c.setPos(x,y);
							board[x][y].occupied = true;
							moves.getAndIncrement();
						}
						finally{
							board[x][y].lock.unlock();
							board[x][y].queue.clear();
						}
					}
				}
				else{
					int x_dir = 0;
					if (x != c.x){x_dir = (x-c.x)/Math.abs(x-c.x);}
					int y_dir = 0;
					if (y != c.y){y_dir = (y-c.y)/Math.abs(y-c.y);}
					int temp = 0;
					for (int i = 1; i < Math.max(Math.abs(x-c.x), Math.abs(y-c.y))+1; i++){
						if (board[c.x+x_dir*i][c.y+y_dir*i].occupied){
							temp = i;
							break;
						}
						board[c.x+x_dir*i][c.y+y_dir*i].queue.add(id);
						if (board[c.x+x_dir*i][c.y+y_dir*i].queue.peek() != id){
							temp = i;
							break;
						}
						else {
							synchronized (this){
								board[c.x+x_dir*i][c.y+y_dir*i].lock.lock();
							}
						}
					}
					if (temp == 0){
						synchronized (this){
							for (int i = 1; i < Math.max(Math.abs(x-c.x), Math.abs(y-c.y)); i++){
								board[c.x+x_dir*i][c.y+y_dir*i].queue.clear();
								board[c.x+x_dir*i][c.y+y_dir*i].lock.unlock();
							}
							board[x][y].occupied = true;
							board[x][y].queue.clear();
							board[x][y].lock.unlock();
							board[c.x][c.y].occupied = false;
							c.setPos(x,y);
							moves.getAndIncrement();
						}
					}
					else {
						synchronized (this){
							for (int i = 1; i < temp; i++){
								board[c.x+x_dir*i][c.y+y_dir*i].lock.unlock();
								board[c.x+x_dir*i][c.y+y_dir*i].queue.clear();
							}
						}
					}
				}
				try {
					Thread.sleep(30);
				} catch (InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}
}


