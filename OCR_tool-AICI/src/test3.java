import java.awt.Canvas;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

public class test3 extends Canvas{

	
	public static void main(String...strings) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		Mat dst = new Mat(), cdst = new Mat(), cdstP;
		
		String file = "./OCR_temp/test2.png";
		Mat src = null;
		try {
			
			src = bufferedImageToMat(ImageIO.read(new File(file)));
			
		}catch(Exception e) {}
		
		
		if(src.empty()) {
			System.out.println("Error opening image");
			System.exit(-1);
		}
		
		Imgproc.Canny(src, dst, 50, 200, 3, false);
		Imgproc.cvtColor(dst,  cdst,  Imgproc.COLOR_GRAY2BGR);
		cdstP = cdst.clone();
		
		Mat lines = new Mat();
		Imgproc.HoughLines(dst, lines, 1, Math.PI/180, 150);
		
		for(int x= 0; x < lines.rows(); x++) {
			double rho = lines.get(x,  0)[0],
					theta = lines.get(x,  0)[1];
			
			double a = Math.cos(theta), b = Math.sin(theta);
			double x0 = a * rho, y0 = b *rho;
			Point pt1 = new Point(Math.round(x0 + 1000*(-b)), Math.round(y0 + 1000*(a)));
			Point pt2 = new Point(Math.round(x0 - 1000*(-b)), Math.round(y0 - 1000*(a)));
			Imgproc.line(cdst, pt1, pt2, new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
		
			
		}
		
		Mat linesP = new Mat();
		Imgproc.HoughLinesP(dst, linesP,  1, Math.PI/180, 50, 50, 10);
		
		for (int x = 0; x < linesP.rows(); x++) {
            double[] l = linesP.get(x, 0);
            Point pt1 = new Point(l[0], l[1]);
            Point pt2 = new Point(l[2], l[3]);
            double mag = magnitude(pt1, pt2);
            if(mag > 500 && mag <= 750) {
            	Imgproc.line(cdstP, pt1, pt2, new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
            	System.out.println(magnitude(pt1, pt2));
            	System.out.println(pt1);
            }
            
        }
        // Show results
        //HighGui.imshow("Source", src);
        //HighGui.imshow("Detected Lines (in red) - Standard Hough Line Transform", cdst);
        HighGui.imshow("Detected Lines (in red) - Probabilistic Line Transform", cdstP);
        HighGui.resizeWindow("Detected Lines (in red) - Probabilistic Line Transform", 800, 1000);
        // Wait and Exit
        HighGui.waitKey();
        System.exit(0);
		
	}
	
	public static Mat bufferedImageToMat(BufferedImage bi) {
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC1);
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		
		return mat;
	}
	
	public static double magnitude(Point p1, Point p2) {
		
		double x = Math.pow((p1.x - p2.x), 2);
		double y = Math.pow((p1.y - p2.y), 2);
		
		return Math.sqrt(x + y);
		
		
	}
	
	
}
