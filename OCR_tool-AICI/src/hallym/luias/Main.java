package hallym.luias;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.opencv.core.Core;

import hallym.luias.graphics.Simple_Window;
import hallym.luias.graphics.Window;

public class Main {

	public static String CLOVA_OCR_INVOKE = "";
	public static String CLOVA_OCR_SECRET = "";

	private static boolean isSimple = true;

	public static void main(String...strings) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		if(!isSimple) {
			new Window();
		}else {
			new Simple_Window();
		}
	}

}
