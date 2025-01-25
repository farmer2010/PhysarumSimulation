package sct;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;
import java.util.ListIterator;

public class Bot{
	ArrayList<Bot> objects;
	Random rand = new Random();
	private double x;
	private double y;
	private double rotate = rand.nextInt(360) / 57.32;
	private int[] world_scale = {1620, 1080};
	private double speed;
	private double feromon;
	private double sens_length;
	private double sens_angle;
	private double turn_angle;
	private double wobbling;
	private int crazy_time = 0;
	private int crazy_time_max;
	private double crazy_fero;
	public Bot(int new_xpos, int new_ypos, double new_speed, double new_feromon, double new_sens_length, double new_sens_angle, double new_turn_angle, double new_wobbling, int new_crazy_time_max, double new_crazy_fero, ArrayList<Bot> new_objects) {
		x = new_xpos;
		y = new_ypos;
		objects = new_objects;
		speed = new_speed;
		feromon = new_feromon;
		sens_length = new_sens_length;
		sens_angle = new_sens_angle;
		turn_angle = new_turn_angle;
		wobbling = new_wobbling;
		crazy_time_max = new_crazy_time_max;
		crazy_fero = new_crazy_fero;
	}
	public void Draw(Graphics canvas) {
		if (crazy_time == 0) {
			canvas.setColor(new Color(0, 255, 0));
		}else {
			canvas.setColor(new Color(255, 0, 0));
		}
		canvas.fillOval((int)(x), (int)(y), 3, 3);
	}
	public int Update(ListIterator<Bot> iterator, double[] params, double[][] map) {
		map[(int)(x)][(int)(y)] += feromon;
		if (map[(int)(x)][(int)(y)] > 1) {
			map[(int)(x)][(int)(y)] = 1;
		}
		x += speed * Math.cos(rotate);
		x = div(x, world_scale[0]);
		y += speed * Math.sin(rotate);
		y = div(y, world_scale[1]);
		rotate += rand.nextDouble(-wobbling, wobbling);
		//
		double s1;//левый
		double s2;//правый
		double s3;//средний
		//
		int sx = (int)(div(x + (sens_length * Math.cos(rotate - sens_angle)), world_scale[0]));//левый сенсор
		int sy = (int)(div(y + (sens_length * Math.sin(rotate - sens_angle)), world_scale[1]));
		s1 = map[sx][sy];
		//
		sx = (int)(div(x + (sens_length * Math.cos(rotate + sens_angle)), world_scale[0]));//правый сенсор
		sy = (int)(div(y + (sens_length * Math.sin(rotate + sens_angle)), world_scale[1]));
		s2 = map[sx][sy];
		//
		sx = (int)(div(x + (sens_length * Math.cos(rotate)), world_scale[0]));//центральный сенсор
		sy = (int)(div(y + (sens_length * Math.sin(rotate)), world_scale[1]));
		s3 = map[sx][sy];
		//
		if (crazy_time == 0) {
			double max = 0;
			if (s1 > s2 && s1 > s3) {//повернуть налево
				rotate -= turn_angle;
				max = s1;
			}else if (s2 > s3 && s2 > s1) {//повернуть направо
				rotate += turn_angle;
				max = s2;
			}else {
				max = s3;
			}
			if (max >= crazy_fero) {
				crazy_time = crazy_time_max;
			}
		}else {
			if (s1 < s2 && s1 < s3) {//повернуть налево
				rotate -= turn_angle;
			}else if (s2 < s3 && s2 < s1) {//повернуть направо
				rotate += turn_angle;
			}
			crazy_time--;
		}
		return(0);
	}
	public double div(double num, double d) {
		if (num < 0) {
			num += d;
		}else if (num >= d) {
			num -= d;
		}
		return(num);
	}
}