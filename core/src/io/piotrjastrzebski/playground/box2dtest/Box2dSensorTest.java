package io.piotrjastrzebski.playground.box2dtest;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 31/07/15.
 */
public class Box2dSensorTest extends BaseScreen {
	public final static float VP_WIDTH = 40;
	public final static float VP_HEIGHT = 22.5f;
	public final static float SCALE = 32f;
	public final static float INV_SCALE = 1.f/32f;
	private final Texture box;

	World world;
	Array<Box> boxes = new Array<>();
	Array<Sensor> sensors = new Array<>();
	Box2DDebugRenderer debugRenderer;
	boolean debugDraw = true;

	public Box2dSensorTest (GameReset game) {
		super(game);
		gameCamera = new OrthographicCamera();
		gameViewport = new ExtendViewport(VP_WIDTH, VP_HEIGHT, gameCamera);
		debugRenderer = new Box2DDebugRenderer();
		world = new World(new Vector2(0, -10), true);
		world.setContactListener(new ContactListener() {
			@Override public void beginContact (Contact contact) {
				Object udA = contact.getFixtureA().getBody().getUserData();
				Object udB = contact.getFixtureB().getBody().getUserData();
				// we only care if both objects are no null
				// note this can be cleaner if all bodies have same type of user data and that handles the checks
				if (udA == null || udB == null) return;
				if (udA instanceof Sensor) {
					if (udB instanceof Box) {
						((Sensor)udA).startTouching((Box)udB);
					}
				}
				if (udB instanceof Sensor) {
					if (udA instanceof Box) {
						((Sensor)udB).startTouching((Box)udA);
					}
				}
			}

			@Override public void endContact (Contact contact) {
				Object udA = contact.getFixtureA().getBody().getUserData();
				Object udB = contact.getFixtureB().getBody().getUserData();
				// we only care if both objects are no null
				// note this can be cleaner if all bodies have same type of user data and that handles the checks
				if (udA == null || udB == null) return;
				if (udA instanceof Sensor) {
					if (udB instanceof Box) {
						((Sensor)udA).endTouching((Box)udB);
					}
				}
				if (udB instanceof Sensor) {
					if (udA instanceof Box) {
						((Sensor)udB).endTouching((Box)udA);
					}
				}
			}

			@Override public void preSolve (Contact contact, Manifold oldManifold) {}
			@Override public void postSolve (Contact contact, ContactImpulse impulse) {}
		});
		box = new Texture("badlogic.jpg");

		createBounds();
		reset();
	}

	Body groundBody;

	private void createBounds () {
		float halfWidth = VP_WIDTH / 2f - 0.5f;
		float halfHeight = VP_HEIGHT / 2f - 0.5f;
		ChainShape chainShape = new ChainShape();
		chainShape.createLoop(new float[] {-halfWidth, -halfHeight, halfWidth, -halfHeight,
			halfWidth, halfHeight, -halfWidth, halfHeight});
		BodyDef chainBodyDef = new BodyDef();
		chainBodyDef.type = BodyDef.BodyType.StaticBody;
		groundBody = world.createBody(chainBodyDef);
		groundBody.createFixture(chainShape, 0);
		chainShape.dispose();
	}

	private void reset () {
		if (mouseJoint != null) {
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
		for (Box box : boxes) {
			world.destroyBody(box.body);
		}
		boxes.clear();

		for (int i = 0; i < 3; i++) {
			float x = MathUtils.random(-15, 15);
			float y = MathUtils.random(-8, 8);
			float rotation = MathUtils.random(90);
			createBox(x, y, rotation, box);
		}
		for (int i = 0; i < 3; i++) {
			float x = MathUtils.random(-15, 15);
			float y = MathUtils.random(-8, 8);
			createSensor(x, y);
		}
	}

	private void createBox (float x, float y, float rotation, Texture texture) {
		Box box = new Box(x, y, rotation, texture);

		BodyDef def = new BodyDef();
		def.position.set(x, y);
		def.angle = rotation * MathUtils.degreesToRadians;
		def.type = BodyDef.BodyType.DynamicBody;
		box.body = world.createBody(def);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(box.width / 2, box.height / 2);
		box.body.createFixture(shape, 1);
		shape.dispose();
		box.body.setUserData(box);
		boxes.add(box);
	}

	private void createSensor (float x, float y) {
		Sensor sensor = new Sensor();
		sensor.x = x;
		sensor.y = y;
		sensor.height = 5;
		sensor.width = 5;

		BodyDef def = new BodyDef();
		def.position.set(x + sensor.width / 2, y + sensor.height / 2);
		Body body = world.createBody(def);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(sensor.width / 2, sensor.height / 2);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1;
		fixtureDef.isSensor = true;
		body.createFixture(fixtureDef);
		shape.dispose();

		body.setUserData(sensor);
		sensor.body = body;
		sensors.add(sensor);
	}

	@Override public void render (float delta) {
		super.render(delta);
		world.step(1f / 60f, 6, 4);

		for (Box box : boxes) {
			box.update();
		}
		draw();
	}

	private void draw () {
		if (debugDraw) {
			debugRenderer.render(world, gameCamera.combined);
		}
		renderer.setProjectionMatrix(gameCamera.combined);
		renderer.begin(ShapeRenderer.ShapeType.Filled);
		for (Sensor s : sensors) {
			s.draw(renderer);
		}
		renderer.end();
		batch.setProjectionMatrix(gameCamera.combined);
		batch.begin();
		for (Box box : boxes) {
			box.draw(batch);
		}
		batch.end();
	}
	private class Sensor {
		public Body body;
		public float x, y, width, height;

		int touching = 0;
		public void startTouching(Box box) {
			touching++;
		}

		public void endTouching(Box box) {
			touching--;
		}


		public void draw (ShapeRenderer renderer) {
			if (touching > 0) {
				renderer.setColor(Color.RED);
			} else {
				renderer.setColor(Color.GREEN);
			}
			renderer.rect(x, y, width, height);
		}
	}

	private class Box {
		public Body body;
		public Texture texture;
		public float x;
		public float y;
		public float rot;
		private float width;
		private float height;
		private int srcWidth;
		private int srcHeight;

		public Box (float x, float y, float rotation, Texture texture) {
			this.x = x;
			this.y = y;
			this.rot = rotation;
			this.texture = texture;
			srcWidth = texture.getWidth()/2;
			width = srcWidth * INV_SCALE;
			srcHeight = texture.getHeight()/2;
			height = srcHeight * INV_SCALE;
		}

		public void update () {
			Vector2 position = body.getPosition();
			x = position.x;
			y = position.y;
			rot = body.getAngle() * MathUtils.radiansToDegrees;
		}

		public void draw (Batch batch) {
			batch.draw(texture, x - width / 2, y - height / 2, width / 2, height / 2, width, height, 1, 1, rot, 0, 0, srcWidth,
				srcHeight, false, false);
		}
	}

	Body hitBody;
	Vector3 testPoint = new Vector3();
	QueryCallback callback = new QueryCallback() {
		@Override
		public boolean reportFixture(Fixture fixture) {
			if (fixture.getBody() == groundBody)
				return true;

			if (fixture.testPoint(testPoint.x, testPoint.y)) {
				hitBody = fixture.getBody();
				return false;
			} else
				return true;
		}
	};

	private MouseJoint mouseJoint;
	@Override public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		gameCamera.unproject(testPoint.set(screenX, screenY, 0));

		// ask the world which bodies are within the given
		// bounding box around the mouse pointer
		hitBody = null;
		world.QueryAABB(callback, testPoint.x - 0.1f, testPoint.y - 0.1f,
			testPoint.x + 0.1f, testPoint.y + 0.1f);

		// if we hit something we create a new mouse joint
		// and attach it to the hit body.
		if (hitBody != null) {
			MouseJointDef def = new MouseJointDef();
			def.bodyA = groundBody;
			def.bodyB = hitBody;
			def.collideConnected = true;
			def.target.set(testPoint.x, testPoint.y);
			def.maxForce = 1000.0f * hitBody.getMass();

			mouseJoint = (MouseJoint) world.createJoint(def);
			hitBody.setAwake(true);
		}

		return super.touchDown(screenX, screenY, pointer, button);
	}
	Vector2 target = new Vector2();

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		gameCamera.unproject(testPoint.set(x, y, 0));
		target.set(testPoint.x, testPoint.y);
		// if a mouse joint exists we simply update
		// the target of the joint based on the new
		// mouse coordinates
		if (mouseJoint != null) {
			mouseJoint.setTarget(target);
		}
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		// if a mouse joint exists we simply destroy it
		if (mouseJoint != null) {
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
		return false;
	}

	@Override public boolean keyDown (int keycode) {
		if (keycode == Input.Keys.F5) {
			reset();
		}
		if (keycode == Input.Keys.Z) {
			debugDraw = !debugDraw;
		}
		return super.keyDown(keycode);
	}

	@Override public void dispose () {
		super.dispose();
		box.dispose();
		world.dispose();
	}
}
