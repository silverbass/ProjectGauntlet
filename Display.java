import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.event.MouseInputAdapter;

public class Display extends JApplet {
	private DisplayFrame frame;
	public void init() {
		frame = new DisplayFrame();
		setUp(frame);
		frame.init();
	}
	
	public void setUp(JFrame frame) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setMinimumSize(new Dimension(800, 600));
		frame.setVisible(true);
	}
}

class DisplayFrame extends JFrame {
	private Graphics2D g2;
	private BufferedImage image;
	private Graphics2D imageg2;
	private Dimension dim;
	
	private final int FPS = 24;
	
	//Background
	private final Color BACKGROUND_COLOR = Color.gray;	
	private File backgroundFile;
	private BufferedImage backgroundImage = null;
	
	private AudioThread audio;
	
	public DisplayFrame() {
		super();
	}
	
	public void init() {
		g2 = (Graphics2D) this.getGraphics();
		dim = this.getSize();
		image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
		imageg2 = image.createGraphics();
		addKeyboard();
		addMouse();
		
		run();
	}
	
	public void run() {		
		while(true) {
			try {
				Thread.sleep(1000/FPS);
			} catch (InterruptedException e) {}
			action();
			draw();
		g2.drawImage(image, 0, 0, null);
	}}
	
	public void action() {
		
	}
	
	public void draw() {
		imageg2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
		drawBackground(imageg2, dim.width, dim.height, BACKGROUND_COLOR, backgroundFile);
	}
	
	public void addKeyboard() {
		this.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				act(e.getKeyChar());
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
	}
	
	public void addMouse() {
		MouseInputAdapter mouseListener = new MouseInputAdapter() {};
		this.addMouseListener(mouseListener);
		this.addMouseMotionListener(mouseListener);
	}
	
	public void act(char key) {
		if (key == 27){ //27 is the Java ASCII for esc
			WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
	        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
		}
	}
	
	public void drawBackground(Graphics2D g2, int width, int height, Color color, File file) {
		if (backgroundFile == null) {
			g2.setColor(color);
			g2.fillRect(0, 0, width, height);
		} else {
			try { //For Image backgrounds
			    backgroundImage = ImageIO.read(file);
			} catch (IOException e) {
				g2.setColor(color);
				g2.fillRect(0, 0, width, height);
			}
			g2.drawImage(backgroundImage, 0, 0, width, height, 0, 0, width, height, null);
		}
	}
	
	public void playAudio(File file, int time) { //Plays an audio file for a specified time in milliseconds
		audio = new AudioThread(file, time);
		audio.start();
		audio = null;
	}
}

class AudioThread extends Thread {
	private File soundFile;
	private AudioInputStream audioInputStream;
	private AudioFormat	audioFormat;
	private SourceDataLine line;
	private DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
	private int time = 1000;
	
	public AudioThread(File file, int time) {
		super();
		this.soundFile = file;
		this.time = time;
	}
	
	public AudioThread(File file) {
		super();
		this.soundFile = file;
	}
	
	public void run() {
		try {
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		audioFormat = audioInputStream.getFormat();
		try {
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(audioFormat);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		line.start();
		int	nBytesRead = 0;
		byte[] abData = new byte[(int) (time * audioFormat.getFrameRate())/1000];
		try	{
			nBytesRead = audioInputStream.read(abData, 0, abData.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (nBytesRead >= 0) {
			int	nBytesWritten = line.write(abData, 0, nBytesRead);
		}
		line.drain();
		line.close();
	}
}