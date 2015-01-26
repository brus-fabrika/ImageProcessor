package com.fabrika.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.fabrika.ImageProcessor.IPTask.PROCESS_TYPE;

public class IPFrame extends JFrame {

	private static final long serialVersionUID = 1L; // just to suppress warning

	IPFrame(){
		super();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1024, 768);
		setTitle("Threaded image processing");

		ImagePanel panel = new ImagePanel();
		this.add(panel, BorderLayout.CENTER);

		JButton button = new JButton("Gray it out!");
		this.add(button, BorderLayout.NORTH);
		button.addActionListener(panel);
	}
	
}

class ImagePanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L; // just to suppress warning

	private BufferedImage image;

	public ImagePanel() {
		super();
		try {
			image = ImageIO.read(new File("e:\\tmp\\abrus1.jpg"));
		} catch (IOException ignore) { /* NOP */ }
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		processWith1Threads(PROCESS_TYPE.CONVOLUTION);
		//processWith1Threads(PROCESS_TYPE.SATURATION_CORRECTION);
		//processWith4Threads(PROCESS_TYPE.TINT_SATURATION_CORRECTION);

		repaint();
	}

	@SuppressWarnings("unused")
	private void processWith1Threads(PROCESS_TYPE ptype) {
		
		int partialWidth = image.getWidth();
		int partialHeight = image.getHeight();
		
		int [] ip = image.getRGB(0, 0, partialWidth, partialHeight, null, 0, partialWidth);
		
		Thread t1 = new Thread(new IPTask(ip, partialWidth, partialHeight, ptype));

		long startTimestamp = System.currentTimeMillis();
		
		t1.start();
		try {
			t1.join();
		} catch (InterruptedException ignore) { /* NOP */ }

		long endTimestamp = System.currentTimeMillis();

		image.setRGB(0, 0, partialWidth, partialHeight, ip, 0, partialWidth);

		System.out.println("Threads (1) processing time: " + (endTimestamp - startTimestamp));
	}
	
	@SuppressWarnings("unused")
	private void processWith2Threads(PROCESS_TYPE ptype) {
		
		int partialWidth = image.getWidth()/2;
		int partialHeight = image.getHeight();
		
		int [][] ip = { image.getRGB(           0, 0, partialWidth, partialHeight, null, 0, partialWidth),
						image.getRGB(partialWidth, 0, partialWidth, partialHeight, null, 0, partialWidth) };
		
		Thread t1 = new Thread(new IPTask(ip[0], partialWidth, partialHeight, ptype));
		Thread t2 = new Thread(new IPTask(ip[1], partialWidth, partialHeight, ptype));

		long startTimestamp = System.currentTimeMillis();

		t1.start(); t2.start();
		try {
			t1.join(); t2.join();
		} catch (InterruptedException ignore) { /* NOP */ }

		long endTimestamp = System.currentTimeMillis();
		
		image.setRGB(           0, 0, partialWidth, partialHeight, ip[0], 0, partialWidth);
		image.setRGB(partialWidth, 0, partialWidth, partialHeight, ip[1], 0, partialWidth);

		System.out.println("Threads (2) processing time: " + (endTimestamp - startTimestamp));
	}
	
	@SuppressWarnings("unused")
	private void processWith4Threads(PROCESS_TYPE ptype) {
		
		int partialWidth = image.getWidth()/2;
		int partialHeight = image.getHeight()/2;
		
		int [][] ip = { image.getRGB(           0,             0, partialWidth, partialHeight, null, 0, partialWidth),
						image.getRGB(partialWidth,             0, partialWidth, partialHeight, null, 0, partialWidth),
						image.getRGB(           0, partialHeight, partialWidth, partialHeight, null, 0, partialWidth),
						image.getRGB(partialWidth, partialHeight, partialWidth, partialHeight, null, 0, partialWidth) };
		
		Thread t1 = new Thread(new IPTask(ip[0], partialWidth, partialHeight, ptype));
		Thread t2 = new Thread(new IPTask(ip[1], partialWidth, partialHeight, ptype));
		Thread t3 = new Thread(new IPTask(ip[2], partialWidth, partialHeight, ptype));
		Thread t4 = new Thread(new IPTask(ip[3], partialWidth, partialHeight, ptype));

		long startTimestamp = System.currentTimeMillis();
		
		t1.start(); t2.start(); t3.start(); t4.start();
		try {
			t1.join(); t2.join(); t3.join(); t4.join();
		} catch (InterruptedException ignore) { /* NOP */ }
		
		long endTimestamp = System.currentTimeMillis();

		image.setRGB(           0,             0, partialWidth, partialHeight, ip[0], 0, partialWidth);
		image.setRGB(partialWidth,             0, partialWidth, partialHeight, ip[1], 0, partialWidth);
		image.setRGB(           0, partialHeight, partialWidth, partialHeight, ip[2], 0, partialWidth);
		image.setRGB(partialWidth, partialHeight, partialWidth, partialHeight, ip[3], 0, partialWidth);
		
		System.out.println("Threads (4) processing time: " + (endTimestamp - startTimestamp));
	}
}