package io.piotrjastrzebski.playground.ecs.assettest;

import com.artemis.*;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.systems.IteratingSystem;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 11/08/15.
 */
public class ECSAssetTest extends BaseScreen {
	World world;
	public ECSAssetTest (GameReset game) {
		super(game);
		WorldConfiguration config = new WorldConfiguration();

		config.setSystem(new SpriteRenderableInitSystem());
		config.setSystem(new SpriteRenderer());

		world = new World(config);
	}

	@Override public void render (float delta) {
		super.render(delta);
		world.process();
	}

	public static class SpriteRenderableInitSystem extends IteratingSystem {
		protected ComponentMapper<SpriteRenderableDef> mSpriteRenderableDef;
		public SpriteRenderableInitSystem () {
			super(Aspect.all(SpriteRenderableDef.class));
		}

		@Override protected void inserted (int e) {
			SpriteRenderableDef spriteRenderableDef = mSpriteRenderableDef.get(e);
			EntityEdit edit = world.getEntity(e).edit();
			SpriteRenderable renderable = edit.create(SpriteRenderable.class);
			// get sprite from some where
			//renderable.sprite =
		}

		@Override protected void process (int e) {
			// passive we dont care
		}
	}

	public static class SpriteRenderer extends EntityProcessingSystem {
		protected ComponentMapper<SpriteRenderable> mSpriteRenderable;
		public SpriteRenderer () {
			super(Aspect.all(SpriteRenderable.class));
		}

		@Override protected void process (Entity e) {
			SpriteRenderable spriteRenderable = mSpriteRenderable.get(e);
			// render stuff
		}
	}
}
