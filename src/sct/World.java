package sct;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Font;
import java.awt.Graphics2D;


public class World extends JPanel{
	ArrayList<Bot> objects;
	int size = 25;
	Timer timer;
	int delay = 10;
	Random rand = new Random();
	double[][] Map = new double[1620][1080];//0 - none, 1 - bot, 2 - organics
	Color gray = new Color(100, 100, 100);
	Color green = new Color(0, 255, 0);
	Color red = new Color(255, 0, 0);
	Color black = new Color(0, 0, 0);
	int steps = 0;
	int W = 1920;
	int H = 1080;
	JButton stop_button = new JButton("Stop");
	boolean pause = false;
	boolean render = true;
	boolean render_feromon = true;
	JButton render_button = new JButton("Render: on");
	JButton renderf_button = new JButton("Render feromon: on");
	int[] world_scale = {1620, 1080};
	double params[] = {
		rand.nextDouble(),//феромон нижняя граница
		rand.nextDouble(),//феромон верхняя граница
		rand.nextInt(10, 100) * 1.0,//спрарение (1/n)
		rand.nextDouble(0.5, 1),//коефф. диффузии
		0.01,//минимальное кол-во феромона для диффузии
		rand.nextDouble(0, 5),//скорость нижняя граница
		rand.nextDouble(0, 5),//скорость верхняя граница
		rand.nextDouble(0, 6.28),//угол поворота нижняя граница
		rand.nextDouble(0, 6.28),//угол поворота верхняя граница
		rand.nextDouble(0, 5),//длина датчиков нижняя граница
		rand.nextDouble(0, 5),//длина датчиков верхняя граница
		rand.nextDouble(0, 6.28),//угол датчиков нижняя граница
		rand.nextDouble(0, 6.28),//угол датчиков верхняя граница
		rand.nextDouble(),//случайное виляние
		rand.nextInt(10, 100),//время "обдолбанности" нижняя граница
		rand.nextInt(10, 100),//время "обдолбанности" верхняя граница
		rand.nextDouble(0.4, 1),//феромона для "обдолбанности" нижняя граница
		rand.nextDouble(0.4, 1),//феромона для "обдолбанности" верхняя граница
	};
	public int[][] movelist = {
		{0, -1},
		{1, -1},
		{1, 0},
		{1, 1},
		{0, 1},
		{-1, 1},
		{-1, 0},
		{-1, -1}
	};
	public World() {
		setLayout(null);
		timer = new Timer(delay, new BotListener());
		objects = new ArrayList<Bot>();
		setBackground(new Color(255, 255, 255));
		addMouseListener(new BotListener());
		addMouseMotionListener(new BotListener());
		stop_button.addActionListener(new start_stop());
		stop_button.setBounds(W - 300, 125, 250, 35);
        add(stop_button);
        //
        JButton new_population_button = new JButton("New population");
        new_population_button.addActionListener(new nwp());
        new_population_button.setBounds(W - 300, 590, 125, 20);
        add(new_population_button);
        //
        render_button.addActionListener(new rndr());
        render_button.setBounds(W - 300, 615, 125, 20);
        add(render_button);
        //
        renderf_button.addActionListener(new rndrf());
        renderf_button.setBounds(W - 170, 615, 125, 20);
        add(renderf_button);
        //
		newPopulation();
		timer.start();
	}
	public void paintComponent(Graphics canvas) {
		super.paintComponent(canvas);
		if (render_feromon) {
			for (int x = 0; x < 1620; x++) {
				for (int y = 0; y < 1080; y++) {
					canvas.setColor(new Color((int)(Map[x][y] * 255), (int)(Map[x][y] * 255), (int)(Map[x][y] * 255)));
					canvas.drawRect(x, y, 1, 1);
				}
			}
		}
		if (render) {
			for(Bot b: objects) {
				b.Draw(canvas);
			}
		}
		canvas.setColor(gray);
		canvas.fillRect(W - 300, 0, 300, 1080);
		canvas.setColor(black);
		canvas.setFont(new Font("arial", Font.BOLD, 18));
		canvas.drawString("Main: ", W - 300, 20);
		canvas.drawString("version 1.9.1", W - 300, 40);
		canvas.drawString("steps: " + String.valueOf(steps), W - 300, 60);
		//
		BufferedImage buff = new BufferedImage(world_scale[0], world_scale[1], BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = buff.createGraphics();
		g2d.setColor(Color.WHITE);
		for (int drx = 0; drx < world_scale[0]; drx++) {
			for (int dry = 0; dry < world_scale[1]; dry++) {
				int gr = (int)(Map[drx][dry] * 255);
				g2d.setColor(new Color(gr, gr, gr));
				g2d.fillRect(drx, dry, 1, 1);
			}
		}
		g2d.dispose();
		try {
			ImageIO.write(buff, "png", new File("record/screen" + String.valueOf(steps)+ ".png"));
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void newPopulation() {
		for (int x = 0; x < world_scale[0]; x++) {
			for (int y = 0; y < world_scale[1]; y++) {
				Map[x][y] = 0;
			}
		}
		double params[] = {
			rand.nextDouble(),//феромон нижняя граница
			rand.nextDouble(),//феромон верхняя граница
			rand.nextInt(10, 100) * 1.0,//спрарение (1/n)
			rand.nextDouble(0.5, 1),//коефф. диффузии
			0.01,//минимальное кол-во феромона для диффузии
			rand.nextDouble(0, 5),//скорость нижняя граница
			rand.nextDouble(0, 5),//скорость верхняя граница
			rand.nextDouble(0, 6.28),//угол поворота нижняя граница
			rand.nextDouble(0, 6.28),//угол поворота верхняя граница
			rand.nextDouble(0, 5),//длина датчиков нижняя граница
			rand.nextDouble(0, 5),//длина датчиков верхняя граница
			rand.nextDouble(0, 6.28),//угол датчиков нижняя граница
			rand.nextDouble(0, 6.28),//угол датчиков верхняя граница
			rand.nextDouble(),//случайное виляние
			rand.nextInt(10, 100),//время "обдолбанности" нижняя граница
			rand.nextInt(10, 100),//время "обдолбанности" верхняя граница
			rand.nextDouble(0.4, 1),//феромона для "обдолбанности" нижняя граница
			rand.nextDouble(0.4, 1),//феромона для "обдолбанности" верхняя граница
		};
		double[] speed = {params[5], params[6]};
		double[] fer = {params[0], params[1]};
		double[] sens_length = {params[9], params[10]};
		double[] sens_angle = {params[11], params[12]};
		double[] turn_angle = {params[7], params[8]};
		int[] crazy_time = {(int)(params[14]), (int)(params[15])};
		double[] crazy_fero = {params[16], params[7]};
		steps = 0;
		objects = new ArrayList<Bot>();
		for (int i = 0; i < 80000; i++) {
			int x = rand.nextInt(1620);
			int y = rand.nextInt(1080);
			Bot new_bot = new Bot(
				x,
				y,
				rand.nextDouble(Math.min(speed[0], speed[1]), Math.max(speed[0], speed[1])),
				rand.nextDouble(Math.min(fer[0], fer[1]), Math.max(fer[0], fer[1])),
				rand.nextDouble(Math.min(sens_length[0], sens_length[1]), Math.max(sens_length[0], sens_length[1])),
				rand.nextDouble(Math.min(sens_angle[0], sens_angle[1]), Math.max(sens_angle[0], sens_angle[1])),
				rand.nextDouble(Math.min(turn_angle[0], turn_angle[1]), Math.max(turn_angle[0], turn_angle[1])),
				params[13],
				rand.nextInt(Math.min(crazy_time[0], crazy_time[1]), Math.max(crazy_time[0], crazy_time[1])),
				rand.nextDouble(Math.min(crazy_fero[0], crazy_fero[1]), Math.max(crazy_fero[0], crazy_fero[1])),
				objects
			);
			objects.add(new_bot);
		}
		repaint();
	}
	private class BotListener extends MouseAdapter implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if (!pause) {
				steps++;
				ListIterator<Bot> bot_iterator = objects.listIterator();
				while (bot_iterator.hasNext()) {
					Bot next_bot = bot_iterator.next();
					next_bot.Update(bot_iterator, params, Map);
				}
				feromon();
			}
			repaint();
		}
	}
	public void feromon() {//распространение кислорода
		double[][] new_map = new double[world_scale[0]][world_scale[1]];
		for (int x = 0; x < world_scale[0]; x++) {
			for (int y = 0; y < world_scale[1]; y++) {
				if (Map[x][y] >= params[4]) {
					Map[x][y] *= (params[2] - 1) / params[2];//испарение
					double fer = Map[x][y] * params[3];
					new_map[x][y] += Map[x][y] - fer;
					Map[x][y] = fer;
					int count = 1;
					for (int i = 0; i < 8; i++) {
						int[] f = {x, y};
						int[] pos = get_rotate_position(i, f);
						if (pos[1] >= 0 && pos[1] < world_scale[1]) {
							count++;
						}
					}
					double ox = Map[x][y] / count;
					new_map[x][y] += ox;
					for (int i = 0; i < 8; i++) {
						int[] f = {x, y};
						int[] pos = get_rotate_position(i, f);
						if (pos[1] >= 0 && pos[1] < world_scale[1]) {
							new_map[pos[0]][pos[1]] += ox;
							if (new_map[pos[0]][pos[1]] > 1) {
								new_map[pos[0]][pos[1]] = 1;
							}
						}
					}
				}else {
					new_map[x][y] += Map[x][y];
				}
				if (new_map[x][y] > 1) {
					new_map[x][y] = 1;
				}
			}
		}
		Map = new_map;
	}
	public int[] get_rotate_position(int rot, int[] sp){
		int[] pos = new int[2];
		pos[0] = (sp[0] + movelist[rot][0]) % world_scale[0];
		pos[1] = sp[1] + movelist[rot][1];
		if (pos[0] < 0) {
			pos[0] = world_scale[0] - 1;
		}else if(pos[0] >= world_scale[0]) {
			pos[0] = 0;
		}
		return(pos);
	}
	private class start_stop implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			pause = !pause;
			if (pause) {
				stop_button.setText("Start");
			}else {
				stop_button.setText("Stop");
			}
		}
	}
	private class nwp implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			newPopulation();
		}
	}
	private class rndr implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			render = !render;
			if (render) {
				render_button.setText("Render: on");
			}else {
				render_button.setText("Render: off");
			}
		}
	}
	private class rndrf implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			render_feromon = !render_feromon;
			if (render_feromon) {
				renderf_button.setText("Render feromon: on");
			}else {
				renderf_button.setText("Render feromon: off");
			}
		}
	}
}
