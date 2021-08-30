package hallym.luias.graphics;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.imageio.spi.IIORegistry;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.levigo.jbig2.JBIG2ImageReaderSpi;

import hallym.luias.Main;
import hallym.luias.data.Judge_Images;
import hallym.luias.funtions.Network;
import hallym.luias.funtions.utils;

public class Window extends Canvas{
	private static final long serialVersionUID = 6312013211559379989L;

	
	boolean _runner = false;
	Thread thread;
	JFrame frame;
	
	BufferStrategy bs;
	Graphics g;
	
	boolean isMouseEntered = false;
	Judge_Images jImages;
	
	static int WIDTH = 720, HEIGHT = (int)((float)WIDTH * 1.41428f); 
	String loadText = "문서 불러오는 중....";
	boolean nowLoading = false;
	boolean nowImageSaving = false;
	
	File prevOpenFolder = null;
	API_keyDialog keyDialog;
	
	public Window() {
		
		utils.loadResources();
		
		try {
			
			File f = new File("./config.conf");
			if(f.exists()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				Main.CLOVA_OCR_INVOKE = br.readLine();
				Main.CLOVA_OCR_SECRET = br.readLine();
			}
		}catch(Exception e) {}
		
		frame = new JFrame("PDF tools - P2I");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				_runner = false;
				
				frame.setVisible(false);
				frame.dispose();
				
				try {
					File f = new File("./config.conf");
					PrintWriter pw = new PrintWriter(new FileOutputStream(f));
					pw.println(Main.CLOVA_OCR_INVOKE);
					pw.println(Main.CLOVA_OCR_SECRET);
					pw.flush();
					pw.close();
				}catch(Exception e) {}
				
				System.exit(0);
			}
		});
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds((d.width - WIDTH) / 2, (d.height - HEIGHT) / 2,  WIDTH, HEIGHT);
		addMenu(frame);
		frame.add(this);
		setEvents();
		frame.setVisible(true);
		start();
		
		IIORegistry.getDefaultInstance().registerServiceProvider(new JBIG2ImageReaderSpi());
		
		//temp image save dir create
		try {
			File tDir = new File("./OCR_temp");
			if(!tDir.exists())
				tDir.mkdir();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		keyDialog = new API_keyDialog();
	}
	
	
	public void render() {
		if(this.getBufferStrategy() == null) {
			this.createBufferStrategy(3);
			return;
		}
		
		bs = this.getBufferStrategy();
		g = bs.getDrawGraphics();
		
		g.setColor(Color.white);
		g.clearRect(0, 0, WIDTH, HEIGHT);
		
		if(nowLoading) {
			g.setColor(Color.black);
			g.drawString(loadText, (WIDTH - (loadText.length() * 18)) / 2, (HEIGHT - 50) / 2);
		}
		
		if(jImages != null) {
			BufferedImage img = jImages.getCurrentPage();
			g.drawImage(img,0, 0, WIDTH, HEIGHT-50, 20, 200, img.getWidth()-20, img.getHeight()-100, this);
			
			Graphics2D g2d = (Graphics2D)g;
			g2d.setStroke(new BasicStroke(0.8f));
			g2d.setColor(Color.black);
			g2d.drawRect(10, 10, WIDTH-30, HEIGHT-80);
		}
		
		
		if(isMouseEntered && jImages != null && !nowImageSaving) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.drawImage(utils.arrow_icon, (WIDTH - 50) - 22, (HEIGHT - 50) / 2, 55, 55, this);
			AffineTransform original = g2d.getTransform();
			AffineTransform newTransform = new AffineTransform();
			g2d.setTransform(newTransform);
			g2d.rotate(Math.toRadians(180), WIDTH/2, HEIGHT/2);
			g2d.drawImage(utils.arrow_icon2, 640, 210, 75, 75, this);
			g2d.setTransform(original);
		}
		
		
		
		bs.show();
	}
	
	public void update() {
		
	}
	
	public void start() {
		
		_runner = true;
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(_runner) {
					render();
					update();
					
					try {thread.sleep(100);}catch(Exception e) {}
				}
			}
		});
		
		thread.start();
		
	}
	
	private void setEvents() {
		
//		this.addMouseMotionListener(new MouseMotionAdapter() {
//			
//			@Override
//			public void mouseMoved(MouseEvent mme) {
//				int x = mme.getX(), y = mme.getY();
//				System.out.println(x + ", " + y);
//			}
//			
//		});
		
		this.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent me) {
				
				int x = me.getX(), y = me.getY();
				
				if( (x >= 650) && (x <= 690) && (y >= 485) && (y <= 535)) {
					//next
					jImages.getNextPage();
				}
				
				if( (x >= 5) && (x <= 54) && (y >= 485) && (y <= 535)) {
					//prev
					jImages.getPrevPage();
				}
				
			}
			
			@Override
			public void mouseEntered(MouseEvent me) {
				isMouseEntered = true;
				
				
			}
			
			@Override
			public void mouseExited(MouseEvent me) {
				isMouseEntered = false;
			}
		});
		
	}
	
	
	private void addMenu(JFrame frame) {
		
		JMenuBar mb = new JMenuBar();
		frame.setJMenuBar(mb);
		
		JMenu file = new JMenu("파일");
		JMenuItem open = new JMenuItem("열기");
		JMenuItem save = new JMenuItem("API 키 변경");
		JMenuItem exit = new JMenuItem("종료");
		
		JMenu ocr = new JMenu("OCR");
		JMenuItem ocr_cur = new JMenuItem("현재 페이지 진행");
		JMenuItem ocr_all = new JMenuItem("모든 페이지 진행");
		
		file.add(open);
		file.add(save);
		file.addSeparator();
		file.add(exit);
		
		mb.add(file);
		mb.add(ocr);
		ocr.add(ocr_cur);
		ocr.add(ocr_all);
		
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				
				keyDialog.setVisible(true);
				
			}
		});
		
		open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setCurrentDirectory(prevOpenFolder == null ? new File(System.getProperty("user.home") + "/Desktop") : prevOpenFolder);
				chooser.showOpenDialog(frame);
				
				if(chooser.getSelectedFile()==null)
					return;
				
				Window.this.nowLoading = true;
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						jImages = utils.getPDF(chooser.getSelectedFile());
						Window.this.nowLoading = false;
					}
				}).start();
				
				prevOpenFolder = new File(chooser.getSelectedFile().getParent());
				
			}
		});
		
		ocr_cur.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
			
				if(Main.CLOVA_OCR_INVOKE.length() <= 0 || Main.CLOVA_OCR_SECRET.length() <= 0) {
					JOptionPane.showMessageDialog(null, "Error! Check out the API keys");
					return;
				}
				
				//ImageSave
				nowImageSaving = true;
				new Thread(new Runnable() {
					@Override
					public void run() {
						File f = utils.saveImages(WIDTH, HEIGHT, jImages.getCurrentPage());
						nowImageSaving = false;
						Network.ClovaOCR(f);
					}
				}).start();
				
				
			}
		});
		
		ocr_all.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent ae) {
				//Image Save
				if(Main.CLOVA_OCR_INVOKE.length() <= 0 || Main.CLOVA_OCR_SECRET.length() <= 0) {
					JOptionPane.showMessageDialog(null, "Error! Check out the API keys");
					return;
				}
				
				if(jImages.getCurrentImageIndex() != 0)
					jImages.firstPage();
				
				nowImageSaving = true;
				new Thread(new Runnable() {
					@Override
					public void run() {
						File dir = null;
						for(int i = 0; i < jImages.getTotalPage(); i++) {
							System.out.println("cur Page" + (i+1));
							String name = jImages.getName().substring(0, jImages.getName().indexOf("."));
							BufferedImage cvtedImage = utils.convertImage(WIDTH, HEIGHT, jImages.getCurrentPage());
							BufferedImage[] images = utils.imageCrop(cvtedImage, utils.lineDetect(cvtedImage));
							
							
							if(images != null) {
								File[] files = new File[2];
								for(int k = 0; k < images.length; k++) {
									files[k] = utils.saveImage(images[k], name, (i+1), (k == 0 ? false : true));
								}
								
								
								dir = files[0].getParentFile();
								
							}else {
								
								File f = utils.saveImages(WIDTH, HEIGHT, jImages.getCurrentPage(), name, (i+1));
								dir = f.getParentFile();
							}
							
							
							
							jImages.getNextPage();
							
							
						}
						
						utils.postProcessing_dir(dir);
						
						nowImageSaving = false;
						JOptionPane.showMessageDialog(null, "작업이 끝낫습니다!");
						
					}
				}).start();
			}
			/*
			@Override
			public void actionPerformed(ActionEvent ae) {
				//ImageSave
				if(Main.CLOVA_OCR_INVOKE.length() <= 0 || Main.CLOVA_OCR_SECRET.length() <= 0) {
					JOptionPane.showMessageDialog(null, "Error! Check out the API keys");
					return;
				}
				
				if(jImages.getCurrentImageIndex() != 0)
					jImages.firstPage();
				
				nowImageSaving = true;
				new Thread(new Runnable() {
					@Override
					public void run() {
						for(int i = 0; i < jImages.getTotalPage(); i++) {
							String name = jImages.getName().substring(0, jImages.getName().indexOf("."));
							File f = utils.saveImages(WIDTH, HEIGHT, jImages.getCurrentPage(), name,(i+1));
							jImages.getNextPage();
							//utils.postProcessing(Network.ClovaOCR(f), i+1);
						}
						nowImageSaving = false;
						
						JOptionPane.showMessageDialog(null, "작업이 끝낫습니다!");
					}
				}).start();
			}
			*/
		});
		
	}
	
}
