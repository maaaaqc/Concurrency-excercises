import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.ArrayList;

public class transform {

    // Number of threads to use
    public static int threads = 1;

    public static void main(String[] args) {
        try {
            if (args.length>0) {
                threads = Integer.parseInt(args[0]);
            }

            // read in an image from a file
            BufferedImage img = ImageIO.read(new File("image.jpg"));
            // store the dimensions locally for convenience
            int width  = img.getWidth();
            int height = img.getHeight();

            // create an output image
            BufferedImage outputimage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);

            // ------------------------------------
            // Your code would go here

            ArrayList<Thread> thread_list = new ArrayList<Thread>();
            long start = System.currentTimeMillis();
            for (int i = 0; i < threads; i++){
                MyThread temp = new MyThread(img, outputimage, height, width, i);
                temp.start();
                thread_list.add(temp);
            }
            for (int i = 0; i < threads; i++){
                thread_list.get(i).join();
            }
            long end = System.currentTimeMillis();
            System.out.println("The time spent: " + (end - start));

            // The easiest mechanisms for getting and setting pixels are the
            // BufferedImage.setRGB(x,y,value) and getRGB(x,y) functions.
            // Note that setRGB is synchronized (on the BufferedImage object).
            // Consult the javadocs for other methods.

            // The getRGB/setRGB functions return/expect the pixel value in ARGB format, one byte per channel.  For example,
            //  int p = img.getRGB(x,y);
            // With the 32-bit pixel value you can extract individual colour channels by shifting and masking:
            //  int red = ((p>>16)&0xff);
            //  int green = ((p>>8)&0xff);
            //  int blue = (p&0xff);
            // If you want the alpha channel value it's stored in the uppermost 8 bits of the 32-bit pixel value
            //  int alpha = ((p>>24)&0xff);
            // Note that an alpha of 0 is transparent, and an alpha of 0xff is fully opaque.
            
            // ------------------------------------
            
            // Write out the image
            File outputfile = new File("outputimage.png");
            ImageIO.write(outputimage, "png", outputfile);

        } catch (Exception e) {
            System.out.println("ERROR " +e);
            e.printStackTrace();
        }
    }

    public static class MyThread extends Thread{
        private BufferedImage img;
        private BufferedImage outputimage;
        private int height;
        private int width;
        private int id;
        MyThread(BufferedImage img, BufferedImage outputimage, int height, int width, int id){
            this.img = img;
            this.outputimage = outputimage;
            this.height = height;
            this.width = width;
            this.id = id;
        }
        synchronized public void run(){
            int length = height/threads+1;
            int start = id*length;
            int end = (id+1)*length;
            if (id == threads-1 && height%threads != 0){
                end = id*length + height%threads;
            }
            int[][] sum = new int[width][height];
            int[][] r = new int[width][height];
            int[][] g = new int[width][height];
            int[][] b = new int[width][height];
            for (int i = 0; i < width; i++){
                for (int j = start; j < end; j++){
                    if (j >= height) {
                        continue;
                    }
                    if (outputimage.getRGB(i, j) != 0){
                        continue;
                    }
                    int[] x = {i-1, i, i+1};
                    int[] y = {j-1, j ,j+1};
                    for (int k = 0; k < 3; k++){
                        for (int l = 0; l < 3; l++){
                            if (x[k] >= 0 && x[k] <= width-1 && y[l] >= 0 && y[l] <= height-1){
                                int p = img.getRGB(x[k],y[l]);
                                int red = ((p>>16)&0xff);
                                int green = ((p>>8)&0xff);
                                int blue = (p&0xff);
                                if (k == 1 && l == 1){
                                    r[i][j] += 8*red;
                                    g[i][j] += 8*green;
                                    b[i][j] += 8*blue;
                                }
                                else {
                                    r[i][j] -= red;
                                    g[i][j] -= green;
                                    b[i][j] -= blue;
                                }
                            }
                        }
                    }
                    if (r[i][j] > 255){r[i][j] = 255;}
                    if (r[i][j] < 0){r[i][j] = 0;}
                    if (g[i][j] > 255){g[i][j] = 255;}
                    if (g[i][j] < 0){g[i][j] = 0;}
                    if (b[i][j] > 255){b[i][j] = 255;}
                    if (b[i][j] < 0){b[i][j] = 0;}
                    sum[i][j] = (0xff<<24) + (r[i][j]<<16) + (g[i][j]<<8) + b[i][j];
                    outputimage.setRGB(i, j, sum[i][j]);
                }
            }
        }
    }
}