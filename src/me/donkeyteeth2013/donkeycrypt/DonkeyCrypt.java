package me.donkeyteeth2013.donkeycrypt;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

//8395->1234 | "test" -> �55�
public class DonkeyCrypt extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	public static final String version = "0.1";
	private static final long p = 9234L;
	private static final int g = 13;
	private final JTextField to;
	private final JTextArea input;
	private final JTextArea output;
	private final JButton totext;
	private final JButton toenc;
	private final String ip;
	private final long pub;
	private final long priv;
	
	public static void main(String[] args) {
		if ((args.length == 0) || (!args[0].equalsIgnoreCase("--nogui")))
			new DonkeyCrypt(true);
		else if (args.length > 0) {
			String s = "";
			for(int i = 1; i < args.length; i++)
				s = s + args[i];
			new DonkeyCrypt(false).autoEncrypt(s);
		} else
			new DonkeyCrypt(false);
	}
	
	public DonkeyCrypt(boolean gui) {
		String ip;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			URL url = new URL("http://checkip.amazonaws.com");
			BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream()));
			ip = InetAddress.getLocalHost().getHostName() + "/" + r.readLine();
		} catch(Throwable t) {
			ip = "127.0.0.1";
		}
		System.out.println("DonkeyCrypt v0.1 initialized.");
		this.ip = ip;
		this.priv = (new Random(ip.hashCode()).nextInt(11) + 1);
		long temppub = -1L;
		try {
			temppub = (long) (((Double) Class.forName("java.lang.Math").getMethod("pow", new Class[]{Double.TYPE, Double.TYPE}).invoke(null, new Object[]{g, Long.valueOf(this.priv)})).doubleValue() % p);
		} catch(Throwable localThrowable1) {}
		this.pub = temppub;
		setName("DonkeyCrypt");
		if (gui) {
			setTitle("DonkeyCrypt");
			setSize(500, 400);
			setLocation((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - getWidth() / 2, (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - getHeight() / 2);
			setResizable(false);
			setDefaultCloseOperation(3);
			setLayout(new FlowLayout());
			JPanel ptop = new JPanel();
			JPanel pto = new JPanel();
			JPanel pconverttop = new JPanel();
			JPanel pconvertmid = new JPanel();
			JPanel pconvertbot = new JPanel();
			JTextArea publickey = new JTextArea("" + getPublicKey());
			final JTextField to = new JTextField("");
			this.input = new JTextArea("", 6, 30);
			this.output = new JTextArea("", 6, 30);
			output.setEditable(false);
			publickey.setEditable(false);
			publickey.setForeground(Color.BLUE);
			to.setForeground(Color.BLUE);
			to.setColumns(20);
			to.setToolTipText("Enter the other person's Public Key here!");
			to.addCaretListener(new CaretListener() {
				
				public void caretUpdate(CaretEvent e) {
					if (to.getText().matches(".*\\D+.*")) {
						new Thread(new Runnable() {
							
							public void run() {
								int i = to.getCaretPosition();
								to.setText(to.getText().replaceAll("\\D", ""));
								to.setCaretPosition(i - 1);
							}
						}).start();
					} else if (to.getText().length() > 4) {
						new Thread(new Runnable() {
							
							public void run() {
								to.setText(to.getText().substring(0, 4));
								to.setCaretPosition(to.getText().length());
							}
						}).start();
					}
				}
			});
			add(ptop);
			add(pto);
			add(pconverttop);
			add(pconvertmid);
			add(pconvertbot);
			add(new JLabel("DonkeyCrypt © 2014 Eli Blaney"));
			ptop.add(new JLabel("Your unique public key: "));
			ptop.add(publickey);
			pto.add(new JLabel("Public Key: "));
			pto.add(this.to = to);
			pconverttop.add(this.input);
			pconverttop.add(new JScrollPane(this.input));
			pconvertmid.add(this.totext = new JButton("Decrypt"));
			pconvertmid.add(this.toenc = new JButton("Encrypt"));
			pconvertbot.add(this.output);
			pconvertbot.add(new JScrollPane(this.output));
			this.totext.addActionListener(this);
			this.toenc.addActionListener(this);
			setVisible(true);
			to.requestFocus();
		} else {
			this.to = null;
			this.input = null;
			this.output = null;
			this.totext = null;
			this.toenc = null;
			System.out.println("Public Key: " + getPublicKey());
			System.out.println("Private Key: " + this.priv + " (Do NOT share this with anyone!)");
		}
	}
	
	public String getIP() {
		return this.ip;
	}
	
	public long getPublicKey() {
		return this.pub;
	}
	
	public long getPrivateKey() {
		return this.priv;
	}
	
	public String autoEncrypt(String s) {
		if (isEncrypted(s))
			return decrypt(s);
		return encrypt(s);
	}
	
	public String encrypt(String s) {
		s = this.pub + s + this.to.getText();
		String f = "�" + new StringBuilder(String.valueOf(this.pub)).append(" ").toString().length();
		for(char c : s.toCharArray())
			f = f + (char) (int) (c - Math.pow(Long.parseLong(this.to.getText()), this.priv) % p);
		return f + (this.to.getText().length() + 1) + "�";
	}
	
	public String decrypt(String s) {
		try {
			if (!isEncrypted(s))
				throw new IllegalArgumentException();
			String f = "";
			for(char c : s.substring(Integer.parseInt("" + s.charAt(1)) + 1, s.length() - Integer.parseInt("" + s.charAt(s.length() - 2)) - 1).toCharArray())
				f = f + (char) (int) (c + Math.pow(Long.parseLong(this.to.getText()), this.priv) % p);
			return f;
		} catch(Throwable t) {}
		return "Error: Invalid message.";
	}
	
	public boolean isEncrypted(String s) {
		return (s.startsWith("�")) && (s.endsWith("�"));
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e == null)
			return;
		Object o = e.getSource();
		if (this.to.getText().length() > 0)
			if ((o == this.totext) && (this.input.getText().length() > 0))
				this.output.setText(decrypt(this.input.getText()));
			else if ((o == this.toenc) && (this.input.getText().length() > 0))
				this.output.setText(encrypt(this.input.getText()));
	}
}