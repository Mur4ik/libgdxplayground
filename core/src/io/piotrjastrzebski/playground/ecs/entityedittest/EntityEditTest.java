package io.piotrjastrzebski.playground.ecs.entityedittest;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;

/**
 * Created by PiotrJ on 20/06/15.
 */
public class EntityEditTest extends BaseScreen {
	World world;
	public EntityEditTest (GameReset game) {
		super(game);
		WorldConfiguration cfg = new WorldConfiguration();
		cfg.setSystem(new TestSystem());

		world = new World(cfg);
		Entity entity = world.createEntity();
		Gdx.app.log("", "e: "+ world.getEntity(entity.getId()));
		world.process();
		Gdx.app.log("", "e: "+ world.getEntity(entity.getId()));
		entity.edit().create(TestComponentA.class).data = "first A";
		entity.edit().create(TestComponentB.class).data = "first B";
		world.process(); // TestSystem: inserted, process
		// entity.edit().remove(TestComponentB.class);
		// world.process(); // TestSystem: removed
		// replace/edit component
		entity.edit().create(TestComponentB.class).data = "second B";
		world.process(); // TestSystem: process, TestComponentB changed
		entity.deleteFromWorld();
		world.process(); // TestSystem: removed

		Json json = new Json();
		Gdx.app.log("",""+Gdx.files.external("welp"));
		Gdx.files.external("whatever.json").writeString(json.toJson("some string crap"), false);
	}

	@Override
	public void dispose() {
		super.dispose();
		world.dispose();
	}

	static Bag<Component> cFill = new Bag<>();
	public static String entityToStr(World world, int e) {
		cFill.clear();
		world.getEntity(e).getComponents(cFill);
		String str =  "Entity{id="+e;
		for(Component c: cFill) {
			// can be null when it was removed
			if (c == null) continue;
			str+=", " + c.toString();
		}
		return str + "}";
	}
}
