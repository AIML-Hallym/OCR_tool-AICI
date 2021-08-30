package hallym.luias.funtions;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.JOptionPane;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import hallym.luias.data.Coordinates;
import hallym.luias.data.Judge_Images;
import hallym.luias.data.Resolution;
import hallym.luias.data.TextLine;

public class utils {

	public static BufferedImage arrow_icon, arrow_icon2;

	public static JSONObject getJSonObject() {

		JSONObject json = new JSONObject();

		json.put("version", "V1");
		json.put("requestId", UUID.randomUUID().toString());
		json.put("lang", "ko");
		json.put("timestamp", System.currentTimeMillis());

		JSONObject image = new JSONObject();
		image.put("format", "png");
		image.put("name", "test");

		JSONArray images = new JSONArray();
		images.add(image);

		json.put("images", images);

		return json;

	}

	public static BufferedImage[] imageCrop(BufferedImage src, Point[] p) {
		BufferedImage[] images = new BufferedImage[2];
		Graphics g;

		if (p == null)
			return null;

		// content part
		images[0] = new BufferedImage(src.getWidth(), (int) (p[0].y - 10), BufferedImage.TYPE_BYTE_GRAY);
		g = images[0].getGraphics();
		g.drawImage(src, 0, 0, images[0].getWidth(), images[0].getHeight(), 0, 0, src.getWidth(), images[0].getHeight(),
				null);

		// annotation part
		images[1] = new BufferedImage(src.getWidth(), src.getHeight() - (int) (p[0].y + 10),
				BufferedImage.TYPE_BYTE_GRAY);
		g = images[1].getGraphics();
		g.drawImage(src, 0, 0, images[1].getWidth(), images[1].getHeight(), 0, (int) (p[0].y + 10), src.getWidth(),
				src.getHeight(), null);

		return images;
	}

	public static Point[] lineDetect(BufferedImage bi) {
		Mat dst = new Mat(), cdstP = new Mat();
		Mat src = imageToMat(bi);

		Imgproc.Canny(src, dst, 50, 200, 3, false);
		Imgproc.cvtColor(dst, cdstP, Imgproc.COLOR_GRAY2BGR);

		Mat linesP = new Mat();
		Imgproc.HoughLinesP(dst, linesP, 1, Math.PI / 180, 50, 50, 10);

		Point[] p = new Point[2];

		for (int x = 0; x < linesP.rows(); x++) {
			double[] l = linesP.get(x, 0);

			Point pt1 = new Point(l[0], l[1]);
			Point pt2 = new Point(l[2], l[3]);

			double mag = magnitude(pt1, pt2);
			
			if (mag > 600 && mag < 730) {
				System.out.println("mag"+mag);
				p[0] = new Point(l[0], l[1]);
				p[1] = new Point(l[2], l[3]);
				break;
			}
		}

		if (p[0] == null)
			return null;

		return p;
	}

	public static double magnitude(Point p1, Point p2) {

		double x = Math.pow((p1.x - p2.x), 2);
		double y = Math.pow((p1.y - p2.y), 2);

		return Math.sqrt(x + y);

	}

	public static Mat imageToMat(BufferedImage bi) {
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC1);
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();

		mat.put(0, 0, data);
		return mat;
	}

	public static JSONObject getEndVertice(JSONObject obj) {
		return (JSONObject) ((JSONArray) obj.get("vertices")).get(1);
	}

	public static JSONObject getStartVertice(JSONObject obj) {
		return (JSONObject) ((JSONArray) obj.get("vertices")).get(0);
	}

	public static ArrayList <String> checkSentence(ArrayList<TextLine> texts) {

		TextLine prev = null;
		ArrayList<String> temp = new ArrayList<String>();

		StringBuffer sb = new StringBuffer();

		System.out.println(texts.size());
		for (int i = 0; i < texts.size(); i++) {
			TextLine cur = texts.get(i);

			if (prev == null) {
				prev = cur;
				sb.append(prev.getText());
				continue;
			}

			// 같은 위치에서 시작하는가?
			if (isStartSamePos(prev, cur)) {
				// 이전 문장이 끝까지 다 쓰여졌는가?
				if (isPrevSentenceFullLength(prev)) {
					sb.append(" " + cur.getText());
				} else {
					// 이전 문장이 끝까지 다 쓰여지지 않았다면,
					temp.add(sb.toString());
					sb = new StringBuffer();

					sb.append(cur.getText());
					if (i >= texts.size() - 1) {
						temp.add(sb.toString());
					}
				}
			} else {
				if (isPrevSentenceFullLength(prev)) {
					sb.append(" " + cur.getText());
				} else {
					temp.add(sb.toString());
					sb = new StringBuffer();

					sb.append(cur.getText());

					if (i >= texts.size() - 1) {
						temp.add(sb.toString());
					}
				}
			}

			prev = cur;
		}

		return temp;

	}

	public static boolean isPrevSentenceFullLength(TextLine t1) {
		double gap = Math.abs(t1.geteX() - 2650);
		return (gap / 100d) <= 0.2d ? true : false;
	}

	public static boolean isStartSamePos(TextLine t1, TextLine t2) {
		double gap = Math.abs(t1.getsX() - t2.getsX());
		return (gap / 100d) <= 0.4d ? true : false;
	}

	public static void ConcatArray(JSONArray arr1, JSONArray arr2) {

		for (int i = 0; i < arr2.size(); i++) {
			arr1.add(arr2.get(i));
		}

	}
	
	public static ArrayList <TextLine> readAllJSON(File dir){
		
		ArrayList <TextLine> textLines = new ArrayList <TextLine>();
		
		try {
			
			File[] files = dir.listFiles();
			
			JSONArray lines = new JSONArray();
			for(int i = 0; i < files.length; i++) {
				if(files[i].isFile() && !(files[i].getName()).contains("png")) {
					
					if(files[i].getName().contains("_주석"))
						continue;
					
					System.out.println(files[i].getName());
					
					BufferedReader br = new BufferedReader(new  InputStreamReader(
							new FileInputStream(files[i])));
					
					StringBuffer sb = new StringBuffer();
					
					String s;
					while((s = br.readLine()) != null) {
						sb.append(s);
					}
					
					JSONParser parser = new JSONParser();
					JSONObject obj = (JSONObject) parser.parse(sb.toString());
					JSONArray images = (JSONArray)obj.get("images");
					
					
					JSONArray fields = (JSONArray)((JSONObject)images.get(0)).get("fields");
					
					ConcatArray(lines, SentenceParser(fields));
					
				}
			}
			
			for(int i = 0; i < lines.size(); i++) textLines.add(new TextLine((JSONObject)lines.get(i)));
			
			return textLines;
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void saveAllTextAndJson(ArrayList <String> texts, String name, File dir) {
		try {
			
			JSONObject obj = new JSONObject();
			
			JSONArray tArr = new JSONArray();
			for(int i = 0; i < texts.size(); i++) {
				JSONObject tObj = new JSONObject();
				tObj.put("text", texts.get(i));
				tArr.add(tObj);
			}
			
			obj.put("textLines", tArr);
			
			File concatResult = new File(dir.getPath()+"/"+name+"_문장정리v1.json");
			PrintWriter pw = new PrintWriter(concatResult);
			pw.println(obj.toJSONString());
			pw.flush();
			pw.close();
			
			File allResult = new File(dir.getPath()+"/"+name+"_문장정리v1.txt");
			pw = new PrintWriter(allResult);
			for(int i = 0; i < texts.size(); i++) {
				pw.println(texts.get(i));
			}
			pw.flush();
			pw.close();
			
		}catch(Exception e) {e.printStackTrace();}
	}
	
	public static void postProcessing_dir(File dir) {
		
		try {
			File[] files = dir.listFiles();
			File result_dir = new File(dir.getPath()+"/results");
			
			if(!result_dir.exists())
				result_dir.mkdir();
				
			for(int i = 0; i < files.length; i++) {
				if(files[i].isFile() && !(files[i].getName()).contains("json")) {
					Network.ClovaOCR(files[i]);
				}
			}
			
			ArrayList <String> conResult = checkSentence(readAllJSON(dir));
			saveAllTextAndJson(conResult, dir.getName(), result_dir);
			
			
			files = dir.listFiles();
			for(int i = 0; i < files.length; i++) {
				if(files[i].isFile() && !(files[i].getName()).contains("png")) {
					System.out.println(i);
					String[] name = files[i].getName().split("_");
					
					int page_num;
					if(files[i].getName().contains("주석")) {
						page_num = Integer.parseInt(name[name.length-2].replace("page", ""));
					}else {
						
						String tn = name[name.length-1].replace("page", "");
						System.out.println(tn);
						page_num = Integer.parseInt(tn.replace(".json", ""));
					}
					
					postProcessing(files[i], page_num);
					
				}
			}
			
		}catch(Exception e) {
			JOptionPane.showMessageDialog(null, "문제가 생겼습니다. 개발자에게 문의하세요");
			e.printStackTrace();
			
		}
		
	}
	
	public static JSONArray SentenceParser(JSONArray texts) {
		JSONObject prev = null;
		JSONObject pprev = null;

		StringBuffer sb = new StringBuffer();
		String single_word;
		boolean single_line_sep = false;

		JSONObject nType = new JSONObject();
		JSONArray lines = new JSONArray();
		JSONObject start_v = null, end_v = null;

		for (int i = 0; i < texts.size(); i++) {
			JSONObject obj = (JSONObject) texts.get(i);
			JSONObject temp = new JSONObject();
			
			if (prev == null) {
				prev = obj;
				start_v = getStartVertice((JSONObject)obj.get("boundingPoly"));
				sb.append((String) prev.get("inferText"));
				continue;
			}

			if (isSameLine((JSONObject) prev.get("boundingPoly"), (JSONObject) obj.get("boundingPoly"))) {
				// 같은 라인일 경우
				if (isBothSingle(prev, obj)) {
					// 이전 글자 현재 글자 둘다 외자 일경우,
					if (!isAlphabet(prev) && !isAlphabet(obj)) {
						// 둘다 알파벳이 아닐경우
						if (single_line_sep) {
							end_v = getEndVertice((JSONObject)pprev.get("boundingPoly"));
							temp.put("text", sb.toString());
							temp.put("location", objsTOvertices(start_v, end_v));
							lines.add(temp);
							
							start_v = null;
							end_v = null;
							sb = new StringBuffer();
							single_line_sep = false;
						}
						
						if(start_v == null)
							start_v = getStartVertice((JSONObject)prev.get("boundingPoly"));
						
						single_word = ((String) prev.get("inferText") + ((String) obj.get("inferText")));
						sb.append(single_word);
					} else {

					}
				} else {

					if (single_line_sep) {
						end_v = getEndVertice((JSONObject)pprev.get("boundingPoly"));
						temp.put("text", sb.toString());
						temp.put("location", objsTOvertices(start_v, end_v));
						lines.add(temp);
						
						start_v = null;
						end_v = null;
						sb = new StringBuffer();
						
						if(start_v == null)
							start_v = getStartVertice((JSONObject)prev.get("boundingPoly"));
						
						sb.append((String) prev.get("inferText"));
						single_line_sep = false;
					}

					sb.append(" " + (String) obj.get("inferText"));
				}

			} else {
				if (!isSingle(obj)) {
					end_v = getEndVertice((JSONObject)prev.get("boundingPoly"));
					temp.put("text", sb.toString());
					temp.put("location", objsTOvertices(start_v, end_v));
					lines.add(temp);
					
					start_v = null;
					end_v = null;
					sb = new StringBuffer();
					
					start_v = getStartVertice((JSONObject)obj.get("boundingPoly"));
					sb.append((String) obj.get("inferText"));
				}else {
					single_line_sep = true;
				}
			}
			
			pprev = prev;
			prev = obj;
			
			if(i >= texts.size()-1) {
				if(sb.length() > 0 && start_v != null) {
					end_v = getEndVertice((JSONObject)prev.get("boundingPoly"));
					temp.put("text", sb.toString());
					temp.put("location", objsTOvertices(start_v, end_v));
					lines.add(temp);
				}
			}
		}

		return lines;
	}
	
	public static JSONArray objsTOvertices(JSONObject obj1, JSONObject obj2) {
		JSONArray jarray = new JSONArray();
		jarray.add(obj1);
		jarray.add(obj2);

		return jarray;
	}

	@SuppressWarnings("unchecked")
	public static void postProcessing(File json, int page_num) {

		try {

			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(json)));

			StringBuffer sb = new StringBuffer();
			StringBuffer sbb = new StringBuffer();

			String s;
			while ((s = br.readLine()) != null) {
				sb.append(s);
			}

			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(sb.toString());
			JSONArray images = (JSONArray) obj.get("images");
			JSONArray fields = (JSONArray) ((JSONObject) images.get(0)).get("fields");

			String lines[] = line_sperator(fields).split("\n");

			JSONObject newObject = new JSONObject();
			newObject.put("page_num", page_num);

			JSONArray textsArray = new JSONArray();
			for (int i = 0; i < lines.length; i++) {
				JSONObject text = new JSONObject();
				text.put("text", lines[i]);
				textsArray.add(text);
			}

			newObject.put("textLines", textsArray);

			File f = new File(json.getParent() + "/results");
			if (!f.exists())
				f.mkdir();

			File ff = new File(f.getPath() + "/" + json.getName());
			PrintWriter pw = new PrintWriter(ff);
			pw.println(newObject.toJSONString());
			pw.flush();
			pw.close();
			
			File fff = new File(f.getPath()+"/" + json.getName().substring(0, json.getName().indexOf("."))+".txt");
			pw = new PrintWriter(fff);
			for(int i = 0; i < lines.length; i++) {
				pw.println(lines[i]);
			}
			pw.flush();
			pw.close();

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "문제가 생겼습니다. 개발자에게 문의하세요");
			e.printStackTrace();
		}

	}

	private static String line_sperator(JSONArray texts) {
		JSONObject prev = null;

		StringBuffer sb = new StringBuffer();
		String single_word;
		boolean single_line_sep = false;
		for (int i = 0; i < texts.size(); i++) {
			JSONObject obj = (JSONObject) texts.get(i);
			if (prev == null) {
				prev = obj;
				sb.append((String) prev.get("inferText"));
				continue;
			}

			if (isSameLine((JSONObject) prev.get("boundingPoly"), (JSONObject) obj.get("boundingPoly"))) {
				// 같은 라인일 경우
				if (isBothSingle(prev, obj)) {
					// 이전 글자 현재 글자 둘다 외자 일경우,
					if (!isAlphabet(prev) && !isAlphabet(obj)) {
						// 둘다 알파벳이 아닐경우
						if (single_line_sep) {
							sb.append("\n");
							single_line_sep = false;
						}

						single_word = ((String) prev.get("inferText") + ((String) obj.get("inferText")));
						sb.append(single_word);
						sb.append(" ");
					} else {
						/////
					}
				} else {

					if (single_line_sep) {
						sb.append("\n");
						sb.append((String) prev.get("inferText"));
						single_line_sep = false;
					}

					sb.append(" " + (String) obj.get("inferText"));
				}

			} else {
				if (!isSingle(obj))
					sb.append("\n" + (String) obj.get("inferText"));
				else
					single_line_sep = true;
			}

			prev = obj;
		}

		return sb.toString();
	}

	private static boolean isSingle(JSONObject o) {
		String s = (String) o.get("inferText");
		return s.length() <= 1 ? true : false;
	}

	private static boolean isAlphabet(JSONObject o) {
		String s = (String) o.get("inferText");
		char c = s.charAt(0);
		if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122))
			return true;

		return false;
	}

	private static boolean isBothSingle(JSONObject o1, JSONObject o2) {
		String s1 = (String) o1.get("inferText");
		String s2 = (String) o2.get("inferText");

		return s1.length() <= 1 && s2.length() <= 1 ? true : false;
	}

	private static boolean isSameLine(JSONObject o1, JSONObject o2) {
		JSONArray o1_array = (JSONArray) o1.get("vertices");
		JSONArray o2_array = (JSONArray) o2.get("vertices");

		JSONObject o1_xy = (JSONObject) o1_array.get(0);
		JSONObject o2_xy = (JSONObject) o2_array.get(0);

		double o1_y = ((Double) o1_xy.get("y"));
		double o2_y = ((Double) o2_xy.get("y"));

		double gap = Math.abs(o1_y - o2_y);
		gap /= 100;

		return gap <= 0.4 ? true : false;
	}

	public static void loadResources() {

		try {

			arrow_icon = ImageIO.read(new File("./resources/arrow.png"));
			arrow_icon2 = ImageIO.read(new File("./resources/arrow.png"));

		} catch (Exception e) {
		}

	}

	public static Judge_Images getPDF(File file) {

		Judge_Images jImages = new Judge_Images(file.getName());

		try {
			PDDocument document = PDDocument.load(file);
			PDFRenderer renderer = new PDFRenderer(document);

			for (int i = 0; i < document.getNumberOfPages(); i++)
				jImages.addPage(renderer.renderImageWithDPI(i, 144));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return jImages;

	}

	public static File saveImages(int width, int height, BufferedImage img) {
		int scale_factor = 4;
		try {
			BufferedImage tImg = new BufferedImage(width * scale_factor, height * scale_factor,
					BufferedImage.TYPE_BYTE_GRAY);
			Graphics2D g2d = (Graphics2D) tImg.getGraphics();

			g2d.drawImage(img, 0, 0, width * scale_factor, height * scale_factor, 20, 200, img.getWidth() - 20,
					img.getHeight() - 50, null);
			g2d.setStroke(new BasicStroke(2f));
			g2d.setColor(Color.black);
			g2d.drawRect(10, 10, (width * scale_factor) - 50, (height * scale_factor) - 80);

			Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");
			ImageWriter writer = (ImageWriter) iter.next();
			ImageWriteParam iwp = writer.getDefaultWriteParam();

			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			iwp.setCompressionQuality(1f);

			File file = new File("./OCR_temp/temp.png");
			FileImageOutputStream out = new FileImageOutputStream(file);
			writer.setOutput(out);

			IIOImage image = new IIOImage(tImg, null, null);
			writer.write(null, image, iwp);
			writer.dispose();

			System.out.println("Save complete!");

			return file;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static BufferedImage convertImage(int width, int height, BufferedImage img) {
		int scale_factor = 4;
		BufferedImage tImg = new BufferedImage(width * scale_factor, height * scale_factor,
				BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g2d = (Graphics2D) tImg.getGraphics();

		g2d.drawImage(img, 0, 0, width * scale_factor, height * scale_factor, 20, 200, img.getWidth() - 20,
				img.getHeight() - 100, null);
		g2d.setStroke(new BasicStroke(2f));
		g2d.setColor(Color.black);
		g2d.drawRect(10, 10, (width * scale_factor) - 50, (height * scale_factor) - 80);

		return tImg;
	}

	public static File saveImages(int width, int height, BufferedImage img, String name, int page) {
		int scale_factor = 4;
		try {
			BufferedImage tImg = new BufferedImage(width * scale_factor, height * scale_factor,
					BufferedImage.TYPE_BYTE_GRAY);
			Graphics2D g2d = (Graphics2D) tImg.getGraphics();

			g2d.drawImage(img, 0, 0, width * scale_factor, height * scale_factor, 20, 200, img.getWidth() - 20,
					img.getHeight() - 100, null);
			g2d.setStroke(new BasicStroke(2f));
			g2d.setColor(Color.black);
			g2d.drawRect(10, 10, (width * scale_factor) - 50, (height * scale_factor) - 80);

//			Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");
//			ImageWriter writer = (ImageWriter)iter.next();
//			ImageWriteParam iwp = writer.getDefaultWriteParam();
//			
//			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//			iwp.setCompressionQuality(1f);
//			
//			File file = new File("./OCR_temp/"+name+"_page"+page+".png");
//			FileImageOutputStream out = new FileImageOutputStream(file);
//			writer.setOutput(out);

			IIOImage image = new IIOImage(tImg, null, null);
//			writer.write(null, image, iwp);
//			writer.dispose();
			File dir = new File("./OCR_temp/" + name);
			if (!dir.exists())
				dir.mkdir();

			File f = new File("./OCR_temp/" + name + "/" + name + "_page" + page + ".png");
			ImageIO.write(tImg, "png", f);

			System.out.println("Save complete!");
			return f;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static File saveImage(BufferedImage img, String name, int page, boolean isAnnotation) {

		try {

			File dir = new File("./OCR_temp/" + name);
			if (!dir.exists())
				dir.mkdir();

			File f = new File(dir.getPath() + "/" + name + "_page" + page + (isAnnotation ? "_주석" : "") + ".png");
			ImageIO.write(img, "png", f);

			return f;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
