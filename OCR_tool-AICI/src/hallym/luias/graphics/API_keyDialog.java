package hallym.luias.graphics;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import hallym.luias.Main;

public class API_keyDialog extends JDialog{
	private static final long serialVersionUID = 1L;
	
	
	static final int width = 380;
	static final int height = 175;
	
	public API_keyDialog() {
		super();
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds( (d.width - width) /2 , (d.height - height)/2, width, height);
		this.setTitle("Naver OCR - API Å° º¯°æ");
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				API_keyDialog.this.setVisible(false);
			}
		});
		
		this.setLayout(null);
		
		setUI();
	}
	
	private void setUI() {
		
		Container con = this.getContentPane();
		
		
		JLabel invoke = new JLabel("API_Invoke_URL");
		JLabel secret = new JLabel("SECRET_KEY");
		
		con.add(invoke);
		con.add(secret);
		
		invoke.setBounds(10, 10, 150, 30);
		secret.setBounds(10, 50, 150, 30);
		
		JTextField invoke_tf = new JTextField();
		JTextField secret_tf = new JTextField();
		
		
		con.add(invoke_tf);
		con.add(secret_tf);
		
		invoke_tf.setText(Main.CLOVA_OCR_INVOKE);
		secret_tf.setText(Main.CLOVA_OCR_SECRET);
		
		invoke_tf.setBounds(150, 10, 200, 30);
		secret_tf.setBounds(150, 50, 200, 30);
		
		
		JButton save = new JButton("Save");
		con.add(save);
		
		save.setBounds(268, 90, 80, 25);
		
		
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				Main.CLOVA_OCR_INVOKE = invoke_tf.getText();
				Main.CLOVA_OCR_SECRET = secret_tf.getText();
				API_keyDialog.this.setVisible(false);
			}
		});
		
	}
	
	
	
	
}
