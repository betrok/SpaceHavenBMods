package spacehavenbmods.MoreRefugeeActions;

import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.lang.reflect.Field;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

import fi.bugbyte.framework.Game;
import fi.bugbyte.framework.Settings;
import fi.bugbyte.framework.animation.Animation;
import fi.bugbyte.framework.screen.GuiSkin;
import fi.bugbyte.spacehaven.gui.MenuSystemItems;
import fi.bugbyte.spacehaven.stuff.Character;
import fi.bugbyte.spacehaven.stuff.Entity;

// Refugee status (discontent/neutral/ready to hire) in selection card
@Aspect
public class EntityCardAspect {
	// It is somewhat safe to assume that there is only one SingleEntitySelected active at time,
	// so we can cache refugee status icon here.
	Animation statusIcon;

	static Field timerField;
	static Field skinField;
	static Field iconXField;
	static {
		try {
			timerField = MenuSystemItems.SingleEntitySelected.class.getDeclaredField("timer");
			timerField.setAccessible(true);
			skinField = MenuSystemItems.AbstractSelectedBoxItem.class.getDeclaredField("skin");
			skinField.setAccessible(true);
			iconXField = MenuSystemItems.SingleEntitySelected.class.getDeclaredField("iconX");
			iconXField.setAccessible(true);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	// update cached refugee status along with other info
	@After("execution(void fi.bugbyte.spacehaven.gui.MenuSystemItems.SingleEntitySelected.update(..)) && this(selected)")
	public void selectedEntryDraw(MenuSystemItems.SingleEntitySelected selected) throws Throwable {
		// timer is set to zero after actual update step, skip any other ticks
		if (timerField.getFloat(selected) != 0) {
			return;
		}
		Entity e = selected.getEntity();
		if (!(e instanceof Character)) {
			statusIcon = null;
			return;
		}
		Character c = (Character) e;
		if (!RefugeeUtils.isHeldRefugee(c)) {
			statusIcon = null;
			return;
		}

		IntSet crewIds = RefugeeUtils.collectCrewIds(c.getWorld());
		int crewMood = RefugeeUtils.averageCrewMood(c.getWorld());
		if (!RefugeeUtils.fineWithStaying(c, crewMood, crewIds)) {
			statusIcon = Game.library.getAnimation("ssNoWorkerAvailable");
		} else if (RefugeeUtils.acceptsHire(c, crewMood, crewIds)) {
			statusIcon = Game.library.getAnimation("ssCrewForHire");
		} else {
			statusIcon = Game.library.getAnimation("moodNeutralIcon");
		}
	}

	// draw our status icon when applicable
	@After("execution(void fi.bugbyte.spacehaven.gui.MenuSystemItems.SingleEntitySelected.draw(..)) && this(selected) && args(batch)")
	public void selectedEntryDraw(MenuSystemItems.SingleEntitySelected selected,
			SpriteBatch batch) throws Throwable {
		if (statusIcon == null) {
			return;
		}

		final float scale = Settings.uiScale;
		GuiSkin skin = (GuiSkin) skinField.get(selected);
		float y = skin.getPosY() - 44.0f * scale;
		statusIcon.draw(0, iconXField.getFloat(selected), y, scale, scale, batch);
	}
}