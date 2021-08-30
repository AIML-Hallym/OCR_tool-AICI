package hallym.luias.graphics;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import hallym.luias.Main;
import hallym.luias.data.Judge_Images;
import hallym.luias.funtions.utils;

public class Simple_Window extends Canvas {
	private static final long serialVersionUID = 1L;

	JFrame frame;
	Thread thread;
	boolean _runner = false;

	String version = "0.1";
	File prevOpenFolder = null;
	File currentDir = null;
	ArrayList<File> task_files = new ArrayList<File>();
	int[] isDone = null;
	int done_count = 0;

	BufferedImage bi;
	int scroll_position = 0;
	int max_scroll = 0;

	API_keyDialog keyDialog;

	BufferStrategy bs;
	Graphics g;
	
	DecimalFormat format = new DecimalFormat("#.##");
	
	Judge_Images jImages = null;

	public Simple_Window() {
		loadConfig();

		// set up end
		frame = new JFrame("CLOVA automation Tool - v" + version);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				_runner = false;

				frame.setVisible(false);
				frame.dispose();

				saveConfig();

				System.exit(0);
			}
		});

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds((d.width - 400) / 2, (d.height - 300) / 2, 400, 300);

		addMenu(frame);
		
		frame.add(this);
		addEvent();
		
		frame.setVisible(true);

		start();

		keyDialog = new API_keyDialog();
		// load directory
		// end

	}

	public void start() {
		_runner = true;

		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (_runner) {
					update();
					render();
				}
			}
		});

		thread.start();
	}

	public void stop() {
		_runner = false;
	}

	public void update() {

	}

	public void render() {

		if (this.getBufferStrategy() == null) {
			this.createBufferStrategy(3);
			return;
		}

		bs = this.getBufferStrategy();
		g = bs.getDrawGraphics();
		g.clearRect(0, 0, 400, 250);
		
		g.setColor(Color.white);
		g.fillRect(0, 0, 400, 250);

		g.setColor(Color.black);
		g.drawString("파일 명", 20, 20);
		g.drawLine(15, 5, 370, 5);
		g.drawLine(15, 5, 15, 180);
		g.drawLine(370, 5, 370, 180);
		g.drawLine(15, 180, 370, 180);
		g.drawLine(15, 30, 370, 30);

		if (bi != null) {
			Graphics g2 = bi.getGraphics();

			g2.clearRect(0, 0, bi.getWidth(), bi.getHeight());
			g2.setColor(Color.white);
			g2.fillRect(0, 0, bi.getWidth(), bi.getHeight());
			
			for (int i = 0; i < task_files.size(); i++) {
				g2.setColor(Color.DARK_GRAY);
				g2.drawString(task_files.get(i).getName(), 0, (i * 30) + 10);
				switch (isDone[i]) {
				case 1:
					g2.setColor(Color.green);
					g2.drawString("완료", 350, (i * 30)+10);
					break;
				case -1:
					g2.setColor(Color.red);
					g2.drawString("에러", 350, (i * 30)+10);
					break;
				case 2:
					g2.setColor(Color.red);
					g2.drawString("진행중", 350, (i * 30)+10);
					break;
				default:
					g2.setColor(Color.black);
					g2.drawString("대기중", 350, (i * 30)+10);

				}
			}

			g.drawImage(bi, 20, 40, 365, 170, 0, scroll_position, bi.getWidth(), (scroll_position + 165), this);
		}
		
		String text = "전체 진행 상황(%)";
		g.drawString(text, (400 - (text.length() * 12)) / 2, 200);
		g.drawLine(15, 210, 370, 210);
		g.drawLine(15, 230, 370, 230);
		g.drawLine(15, 210, 15, 230);
		g.drawLine(370, 210, 370, 230);
		
		g.setColor(Color.lightGray);
		g.fillRect(15, 210, (int)(355), 20);
		
		if(bi != null) {
			float percent = 0.0f;
			
			g.setColor(Color.blue);
			if(done_count > 0) {
				percent = ((float)done_count / (float)task_files.size());
				g.fillRect(15, 210, (int)(355f * percent), 20);
				
			}
			
			g.setColor(Color.white);
			String s = "("+done_count+"/"+task_files.size()+") "+format.format(percent * 100)+"%";
			g.drawString(s, (370 - (s.length() * 6)) / 2, 223);
			
		}

		bs.show();

	}
	
	private void addEvent() {
		
		this.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				// TODO Auto-generated method stub
				if(bi != null) {
					scroll_position += (e.getWheelRotation() * 3);
					
					if(scroll_position < 0)
						scroll_position = 0;
					
					if(scroll_position >= max_scroll)
						scroll_position = max_scroll;
				}
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
		JMenuItem ocr_cur = new JMenuItem("OCR 진행");

		file.add(open);
		file.add(save);
		file.addSeparator();
		file.add(exit);

		mb.add(file);
		mb.add(ocr);
		ocr.add(ocr_cur);

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
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setCurrentDirectory(
						prevOpenFolder == null ? new File(System.getProperty("user.home") + "/Desktop")
								: prevOpenFolder);
				chooser.showOpenDialog(frame);

				if (chooser.getSelectedFile() == null)
					return;

				new Thread(new Runnable() {
					@Override
					public void run() {
						currentDir = chooser.getSelectedFile();
						File[] files = currentDir.listFiles();

						for (int i = 0; i < files.length; i++) {
							if (files[i].isDirectory())
								continue;

							String fName = files[i].getName();

							if (fName.contains("pdf")) {
								task_files.add(files[i]);
							}

						}

						max_scroll = (task_files.size() * 30) - 165;
						bi = new BufferedImage(400, task_files.size() * 30, BufferedImage.TYPE_INT_ARGB);
						isDone = new int[task_files.size()];
					}
				}).start();

				prevOpenFolder = new File(chooser.getSelectedFile().getParent());

			}
		});

		ocr_cur.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {

				if (Main.CLOVA_OCR_INVOKE.length() < 0 || Main.CLOVA_OCR_SECRET.length() < 0)
					inputSetting();
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						
						for(int i = 0; i < task_files.size(); i++, done_count++) {
							isDone[i] = 2;
							
							try {
								
								jImages = utils.getPDF(task_files.get(i));
								
								File dir = null;
								for(int page = 0; page < jImages.getTotalPage(); page++) {
									String name = jImages.getName().substring(0, jImages.getName().indexOf("."));
									BufferedImage cvtedImage = utils.convertImage(Window.WIDTH, Window.HEIGHT, jImages.getCurrentPage());
									BufferedImage[] images = utils.imageCrop(cvtedImage, utils.lineDetect(cvtedImage));
									
									if(images != null) {
										File[] files = new File[2];
										for(int k = 0; k < images.length; k++) {
											files[k] = utils.saveImage(images[k], name, (page+1), (k == 0 ? false : true));
										}
										
										dir = files[0].getParentFile();
									}else {
										File f = utils.saveImages(Window.WIDTH, Window.HEIGHT, jImages.getCurrentPage(), name, (page+1));
										dir = f.getParentFile();
									}
									
									jImages.getNextPage();
								}
								
								utils.postProcessing_dir(dir);
								isDone[i] = 1;
								
							}catch(Exception e) {
								isDone[i] = -1;
							}
							
							scroll_position = (done_count * 30);
						}
						
					}
				}).start();

			}
		});

	}

	public void loadConfig() {
		try {
			File tDir = new File("./OCR_temp");
			if (!tDir.exists())
				tDir.mkdir();

			File f = new File("./config.conf");
			if (f.exists()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				Main.CLOVA_OCR_INVOKE = br.readLine();
				Main.CLOVA_OCR_SECRET = br.readLine();
				br.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void inputSetting() {
		do {
			String invoke = JOptionPane.showInputDialog("Invoke URL을 입력해 주십시오. 기존 이용시 공란");
			if (Main.CLOVA_OCR_INVOKE.length() <= 0 && invoke.length() <= 0) {
				JOptionPane.showMessageDialog(null, "Invoke URL이 없습니다. 다시 입력해 주세요");
			}

			if (Main.CLOVA_OCR_INVOKE.length() > 0 && invoke.length() <= 0) {
				break;
			}

			if (invoke.length() > 0)
				Main.CLOVA_OCR_INVOKE = invoke;
		} while (true);

		do {
			String scretkey = JOptionPane.showInputDialog("Key를 입력해 주십시오. 기존 이용시 공란");
			if (Main.CLOVA_OCR_SECRET.length() <= 0 && scretkey.length() <= 0) {
				JOptionPane.showMessageDialog(null, "Key가 없습니다. 다시 입력해 주세요");
			}

			if (Main.CLOVA_OCR_SECRET.length() > 0 && scretkey.length() <= 0) {
				break;
			}

			if (scretkey.length() > 0)
				Main.CLOVA_OCR_SECRET = scretkey;
		} while (true);
	}

	public void saveConfig() {
		try {
			File f = new File("./config.conf");
			PrintWriter pw = new PrintWriter(new FileOutputStream(f));
			pw.println(Main.CLOVA_OCR_INVOKE);
			pw.println(Main.CLOVA_OCR_SECRET);
			pw.flush();
			pw.close();
		} catch (Exception e) {
		}
	}

}
