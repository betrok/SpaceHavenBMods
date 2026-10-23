package spacehavenbmods.MoreRefugeeActions;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;
import fi.bugbyte.framework.Game;
import fi.bugbyte.framework.screen.GuiSkin;
import fi.bugbyte.gen.compiled.TextIconButton1;
import fi.bugbyte.gen.compiled.TextButtons2;
import fi.bugbyte.spacehaven.gui.EntityAndCraftInfo;
import fi.bugbyte.spacehaven.gui.GameLog;
import fi.bugbyte.spacehaven.gui.StarMapScreen;
import fi.bugbyte.spacehaven.gui.popups.DialogBoxScreen;
import fi.bugbyte.spacehaven.gui.popups.ReachedEdenPopup;
import fi.bugbyte.spacehaven.stuff.Character;
import fi.bugbyte.spacehaven.stuff.Entity;
import fi.bugbyte.spacehaven.stuff.personality.AbsWorkPersonality;
import fi.bugbyte.spacehaven.world.Ship;
import fi.bugbyte.spacehaven.world.World;

import java.lang.reflect.Field;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

// Adds option to drop off refugges to popup which is triggered by reaching Eden system after end of Exodus mission
@Aspect
public class ReachedEdenPopupAspect {
	@Pointcut("execution(void fi.bugbyte.spacehaven.gui.popups.ReachedEdenPopup.open(..))")
	public void reachedEdenPopupOpen() {
	}

	@Before("(reachedEdenPopupOpen() || execution(void fi.bugbyte.spacehaven.gui.popups.ReachedEdenPopup.setStayOnFleetInfo(..))) && this(popup)")
	public void cleanButtons(ReachedEdenPopup popup) {
		popup.getButtonManager().removeAllButtons();
	}

	@After("reachedEdenPopupOpen() && this(popup)")
	public void addRefuggesButton(ReachedEdenPopup popup) throws Throwable {
		Field field = popup.getClass().getDeclaredField("sm");
		field.setAccessible(true);
		World world = ((StarMapScreen) field.get(popup)).getWorld();

		if (RefugeeUtils.getAllHeldRefugges(world).isEmpty()) {
			return;
		}

		TextIconButton1 button = TextButtons2.getBase();
		button.setText("Drop off refugges");
		CharacterSelectPopup.OnClose onClose = (boolean confirmed, Array<Character> selected) -> {
			if (!confirmed || selected.isEmpty()) {
				return;
			}
			int reward = 0;
			for (Character c : selected) {
				reward += dropOffCharacter(c);
			}
			world.getPlayerBank().addCredits(reward);
			GameLog.addLog(
					"Refugee drop-off award: " + String.valueOf(reward) + " "
							+ Game.library.getTextById("1564").getText(),
					GameLog.LogType.Good, null);

			if (RefugeeUtils.getAllHeldRefugges(world).isEmpty()) {
				popup.getButtonManager().removeButton(button);
			}
		};
		button.setClickHandler(() -> {
			// A bit akward to recollect this array every time,
			// but it is easiest way to guarantee consistency for repetitive action.
			Array<Character> refugees = RefugeeUtils.getAllHeldRefugges(world);
			refugees.sort((final Character c1, final Character c2) -> {
				int cmp = c1.getOrginalSide().compareTo(c2.getOrginalSide());
				if (cmp != 0) {
					return cmp;
				}
				return c1.getFullName().compareTo(c2.getFullName());
			});
			IntSet crewIds = RefugeeUtils.collectCrewIds(world);
			int crewMood = RefugeeUtils.averageCrewMood(world);
			final CharacterSelectPopup p = new CharacterSelectPopup("Select refugees to drop off",
					refugees, onClose, (EntityAndCraftInfo.CrewSelectItem card) -> {
						Character c = card.getCharacter();
						if (RefugeeUtils.fineWithStaying(c, crewMood, crewIds)) {
							card.setExtraIcon(Game.library.getAnimation("goodRestTopicIcon"));
						}
					});
			popup.openCustomPopup("", 0, p);
		});
		// middle of popup, same level as other buttons
		Field skinField = DialogBoxScreen.class.getDeclaredField("skin");
		skinField.setAccessible(true);
		float x = ((GuiSkin) skinField.get(popup)).getPosX();
		float y = popup.getButtonManager().getButtons().first().getY();
		button.moveTo(x, y);

		popup.getButtonManager().addButton(button);
	}

	// removes character and return amount of reward credits
	static int dropOffCharacter(Character c) {
		int mood = c.getMoodProp().getLongTerm();
		int totalSkill = 0;
		for (final AbsWorkPersonality.Skill s : c.getPersonality().getSkills()) {
			totalSkill += s.getLevel();
		}
		// (100 + longterm mood) scalead with sum of all skill levels
		int reward = 100 + mood;
		reward = (int) ((float) reward * (1 + (float) totalSkill / 40));
		Entity.EntityWorldElement wa = c.getWa();
		Ship ship = wa.getShip();
		if (ship != null) {

			c.dropAllCarryItems(ship, true);
			ship.entityLeftShip(c, wa.getGridX(), wa.getGridY());
		}
		c.dispose();
		return reward;
	}
}
