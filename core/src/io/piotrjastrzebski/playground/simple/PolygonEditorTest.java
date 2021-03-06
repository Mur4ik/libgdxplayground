package io.piotrjastrzebski.playground.simple;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PlaygroundGame;
import io.piotrjastrzebski.playground.bttests.dog.MarkTask;

import java.util.Iterator;

/**
 * Created by EvilEntity on 25/01/2016.
 */
public class PolygonEditorTest extends BaseScreen {
	private static final String TAG = PolygonEditorTest.class.getSimpleName();
	private Texture image;
	private PolygonSpriteBatch polyBatch;
	private PolygonEditor editor;

	public PolygonEditorTest (GameReset game) {
		super(game);
		clear.set(Color.GRAY);
		polyBatch = new PolygonSpriteBatch();
		image = new Texture("badlogic.jpg");
		editor = new PolygonEditor();
		editor.grid(.5f, .5f);
		editor.init(-8, -8, 16, 16);
	}

	@Override public void render (float delta) {
		super.render(delta);
		polyBatch.enableBlending();
		polyBatch.setProjectionMatrix(gameCamera.combined);
		polyBatch.begin();
		polyBatch.setColor(Color.DARK_GRAY);
		polyBatch.draw(image, -8, -8, 16, 16);
		polyBatch.setColor(Color.WHITE);

		polyBatch.end();

		editor.update(delta);

		renderer.setProjectionMatrix(gameCamera.combined);
		enableBlending();
		renderer.begin(ShapeRenderer.ShapeType.Line);
		editor.drawLine(renderer);
		renderer.end();
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		editor.drawFilled(renderer);
		renderer.end();
	}

	protected static class PolygonEditor {
		private Array<Vertex> vertices = new Array<>();
		private Array<VertexConnection> connections = new Array<>();
		private float gridX = 1, gridY = 1;
		private Rectangle bounds = new Rectangle();
		private Rectangle marginBounds = new Rectangle();
		private float margin = 3;
		private boolean drawGrid = true;
		private float VERTEX_OVERLAP_EPSILON = .2f;

		public void init (float x, float y, float width, float height){
			bounds.set(x, y, width, height);
			marginBounds.set(x - margin, y - margin, width + margin * 2, height + margin * 2);
			// 0,1,2, 0,3,2
			// build a basic rect
			vertex(x, y);
			vertex(x + width, y);
			vertex(x + width, y + height);
			vertex(x, y + height);

			connect(0, 1);
			connect(0, 2);
			connect(1, 2);
			connect(0, 3);
			connect(3, 2);
		}

		public void grid (float gridX, float gridY) {
			this.gridX = gridX;
			this.gridY = gridY;
		}

		private void vertex(float x, float y) {
			vertices.add(new Vertex(x, y, vertices.size));
		}

		private void connect (int v1, int v2) {
			connect(vertices.get(v1), vertices.get(v2));
		}

		private void connect (Vertex vertex1, Vertex vertex2) {
			for (VertexConnection vc : vertex1.connections) {
				if (vc.vertex1 == vertex2 || vc.vertex2 == vertex2) {
					return;
				}
			}
			VertexConnection connection = new VertexConnection(vertex1, vertex2);
			vertex1.connections.add(connection);
			vertex2.connections.add(connection);
			connections.add(connection);
		}

		private void disconnect (Vertex vertex) {
			for (VertexConnection vc : vertex.connections) {
				if (vc.vertex1 == vertex) {
					vc.vertex2.connections.removeValue(vc, true);
				} else {
					vc.vertex1.connections.removeValue(vc, true);
				}
				connections.removeValue(vc, true);
			}
		}

		private void disconnect (Vertex vertex1, Vertex vertex2) {
			Iterator<VertexConnection> it1 = vertex1.connections.iterator();
			while (it1.hasNext()) {
				VertexConnection vc = it1.next();
				if (vc.vertex1 == vertex2 || vc.vertex2 == vertex2) {
					it1.remove();
					connections.removeValue(vc, true);
					break;
				}
			}
			Iterator<VertexConnection> it2 = vertex2.connections.iterator();
			while (it2.hasNext()) {
				VertexConnection vc = it2.next();
				if (vc.vertex1 == vertex1 || vc.vertex2 == vertex1) {
					it2.remove();
					connections.removeValue(vc, true);
					break;
				}
			}
		}

		private void remove(Vertex vertex) {
			vertices.removeValue(vertex, true);
			disconnect(vertex);
			for (int i = 0; i < vertices.size; i++) {
				vertices.get(i).id = i;
			}
		}

		public void drawFilled(ShapeRenderer renderer) {
			if (selected != null && dragged == null) {
				renderer.setColor(Color.ORANGE);
				renderer.circle(selected.pos.x, selected.pos.y, .25f, 16);
			}
			for (Vertex vertex : vertices) {
				renderer.setColor(vertex.color);
				renderer.circle(vertex.pos.x, vertex.pos.y, .15f, 8);
			}
		}

		private Vector2 tmp = new Vector2();
		public void drawLine(ShapeRenderer renderer) {
			if (drawGrid) {
				renderer.setColor(1, 1, 1, .5f);
				renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
				renderer.rect(marginBounds.x, marginBounds.y, marginBounds.width, marginBounds.height);
				renderer.setColor(1, 1, 1, .25f);
				float sx = toGrid(marginBounds.x, gridX);
				float sy = toGrid(marginBounds.y, gridY);
				int width = MathUtils.ceil(marginBounds.width / gridX);
				int height = MathUtils.ceil(marginBounds.height / gridY);
				// major grid lines
				for (int x = 0; x <= width; x++) {
					renderer.line(sx + x * gridX, sy, sx + x * gridX, sy + height * gridY);
				}
				for (int y = 0; y <= height; y++) {
					renderer.line(sx, sy + y * gridY, sx + width * gridX, sy + y * gridY);
				}
			}
			for (VertexConnection vc : connections) {
				renderer.setColor(0, 1, 1, .75f);
				for (int i = 0; i < connections.size; i++) {
					VertexConnection ovc = connections.get(i);
					if (vc != ovc) {
						// skip connections with shared vertices
						if (vc.vertex1 == ovc.vertex1 || vc.vertex1 == ovc.vertex2 || vc.vertex2 == ovc.vertex1 || vc.vertex2 == ovc.vertex2) continue;
						if (Intersector.intersectSegments(vc.vertex1.pos, vc.vertex2.pos, ovc.vertex1.pos, ovc.vertex2.pos, tmp)) {
							renderer.setColor(1, 0, 0, .75f);
							break;
						}
					}
				}
				renderer.line(vc.vertex1.pos, vc.vertex2.pos);
			}
		}

		protected Vertex selected;
		protected Vertex dragged;
		public void touchDown (float x, float y) {
			if (doubleClick > 0) {
				Vertex at = null;
				for (Vertex vertex : vertices) {
					if (vertex.pos.epsilonEquals(x, y, .25f)) {
						at = vertex;
						break;
					}
				}
				if (at != null) {
					remove(at);
				} else {
					vertex(x, y);
				}
			} else {
				doubleClick = .25f;
				boolean link = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && selected != null;
				if (!link) selected = null;
				for (Vertex vertex : vertices) {
					if (vertex.pos.epsilonEquals(x, y, .25f)) {
						if (link) {
							boolean disconnected = false;
							for (VertexConnection vc : selected.connections) {
								if (vc.vertex1 == vertex || vc.vertex2 == vertex) {
									disconnected = true;
									disconnect(selected, vertex);
									break;
								}
							}
							if (!disconnected) {
								connect(selected, vertex);
							}
						} else {
							dragged = vertex;
							selected = vertex;
						}
						break;
					}
				}
			}
		}

		public void touchDragged (float x, float y) {
			if (dragged != null) {
				boolean snapToGrid = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
				if (snapToGrid) {
					x += (x > 0)? gridX/2: -gridX/2;
					y += (y > 0)? gridY/2: -gridY/2;
					dragged.pos.set(toGrid(x, gridX), toGrid(y, gridY));
				} else {
					dragged.pos.set(x, y);
				}
			}
		}

		protected float toGrid(float value, float grid) {
			return value - value % grid;
		}

		public void touchUp (float x, float y) {
			if (dragged != null) {
				bounds.set(0, 0, 0, 0);
				for(Vertex vertex : vertices) {
					vertex.color.set(Color.GREEN);
					extendBounds(vertex.pos.x, vertex.pos.y);
				}
				// check for vertex collisions
				for(Vertex vertex : vertices) {
					for (int i = 0; i < vertices.size; i++) {
						Vertex other = vertices.get(i);
						if (vertex != other) {
							if (vertex.pos.epsilonEquals(other.pos, VERTEX_OVERLAP_EPSILON)) {
								vertex.color.set(Color.RED);
								other.color.set(Color.RED);
							}
						}
					}
				}

				updateMarginBounds();
				dragged = null;
			}
		}

		private void extendBounds (float x, float y) {
			if (!bounds.contains(x, y)) {
				if (x < bounds.x) {
					float ox = bounds.x + bounds.width;
					bounds.x = x;
					bounds.width = ox - x;
				} else if (x > bounds.x + bounds.width) {
					bounds.width = x - bounds.x;
				}
				if (y < bounds.y) {
					float oy = bounds.y + bounds.height;
					bounds.y = y;
					bounds.height = oy - y;
				} else if (y > bounds.y + bounds.height) {
					bounds.height = y - bounds.y;
				}
			}
		}

		private void updateMarginBounds() {
			marginBounds.set(bounds.x - margin, bounds.y - margin, bounds.width + margin * 2, bounds.height + margin * 2);
		}

		private float doubleClick;
		public void update (float delta) {
			if (doubleClick > 0) doubleClick -= delta;
		}

		protected static class Vertex {
			public int id;
			public Vector2 pos = new Vector2();
			public Array<VertexConnection> connections = new Array<>();
			public Color color = new Color(Color.GREEN);

			public Vertex (float x, float y, int id) {
				pos.set(x, y);
				this.id = id;
			}
		}

		protected static class VertexConnection {
			public Color color = new Color(Color.GREEN);
			public Vertex vertex1;
			public Vertex vertex2;

			public VertexConnection (Vertex vertex1, Vertex vertex2) {
				this.vertex1 = vertex1;
				this.vertex2 = vertex2;
			}
		}
	}


	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		super.touchDown(screenX, screenY, pointer, button);
		editor.touchDown(cs.x, cs.y);
		return true;
	}

	@Override public boolean touchDragged (int screenX, int screenY, int pointer) {
		super.touchDragged(screenX, screenY, pointer);
		editor.touchDragged(cs.x, cs.y);
		return true;
	}

	@Override public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		super.touchUp(screenX, screenY, pointer, button);
		editor.touchUp(cs.x, cs.y);
		return true;
	}

	@Override public void dispose () {
		super.dispose();
		polyBatch.dispose();
	}

	// allow us to start this test directly
	public static void main (String[] args) {
		PlaygroundGame.start(args, PolygonEditorTest.class);
	}
}
