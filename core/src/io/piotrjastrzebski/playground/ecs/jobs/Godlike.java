package io.piotrjastrzebski.playground.ecs.jobs;

import com.artemis.PooledComponent;
import com.badlogic.gdx.graphics.Color;

/**
 * All we need besides jobs cus we are lazy
 * Created by EvilEntity on 17/08/2015.
 */
public class Godlike extends PooledComponent {
	public String name;
	public float x, y, width, height;
	public float vx, vy;
	public Color color = new Color();
	boolean selected;

	@Override protected void reset () {
		name = null;
		x = y = width = height = vx = vy = 0;
		color.set(Color.WHITE);
		selected = false;
	}

	@Override public String toString () {
		return "Godlike<"+name+">";
	}
}
