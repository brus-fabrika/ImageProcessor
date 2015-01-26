package com.fabrika.ImageProcessor;

import javax.swing.JFrame;

public class ImageProcessor {

	public static void main(String[] args) {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				makeGui();
			}
		});

	}

	public static void makeGui() {
		JFrame mainFrame = new IPFrame();
		mainFrame.setVisible(true);
	}
}
