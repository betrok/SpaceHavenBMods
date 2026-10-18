package spacehavenbmods.MoreRefugeeActions;

import fi.bugbyte.framework.screen.StageButton;
import fi.bugbyte.spacehaven.SpaceHaven;
import fi.bugbyte.spacehaven.GameTimeLineStats;
import fi.bugbyte.spacehaven.gui.GUI;
import fi.bugbyte.spacehaven.gui.GameLog;
import fi.bugbyte.spacehaven.stuff.Character;
import fi.bugbyte.spacehaven.stuff.Entity;
import fi.bugbyte.spacehaven.stuff.FactionUtils;
import fi.bugbyte.spacehaven.world.ShipHelper;

import java.util.Arrays;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

//Adds ability to recruit refugee to crew
@Aspect
public class RecruitRefugeeAspect {
	// love me some magic numbers
	static final int playerFactionID = 461;

	// add convert button when refugee is selected
	@After("execution(private void fi.bugbyte.spacehaven.gui.GUI.SelectedElements.finishSelection()) && this(selected)")
	public void addRecruitButton(GUI.SelectedElements selected) throws Throwable {
		for (Entity e : selected.getSelectedEntities()) {
			if (!(e instanceof Character)) {
				continue;
			}
			Character c = (Character) e;
			if (RefugeeUtils.isHeldRefugee(c) && c.getAi().getBelongsToMissionId() == 0) {
				// convert button click handler interacts with enslave button, so we need to make
				// sure both exist, even if only one is shown
				for (String name : Arrays.asList("createEnslaveButton", "createConvertButton")) {
					Method createButtonMethod = selected.getClass()
							.getDeclaredMethod(name);
					createButtonMethod.setAccessible(true);
					createButtonMethod.invoke(selected);
				}

				Field buttonField = selected.getClass().getDeclaredField("convertButton");
				buttonField.setAccessible(true);
				final StageButton button = (StageButton) buttonField.get(selected);
				// default text says "prisoner"
				button.setToolTip("Try to recruit this character");

				Method addButtonMethod = selected.getClass()
						.getDeclaredMethod("addSelectionButton", StageButton.class);
				addButtonMethod.setAccessible(true);
				addButtonMethod.invoke(selected, button);

				return;
			}
		}
	}

	// add recruit logic for refugee ai
	@After("execution(void fi.bugbyte.spacehaven.ai.CharacterAI.setBeingConverted(..)) && this(ai) && args(convert)")
	public void onRecruit(fi.bugbyte.spacehaven.ai.CharacterAI ai, final boolean convert) {
		if (!convert || ai.getBelongsToMissionId() != 0) {
			return;
		}
		Character c = ai.getMyChar();
		if (!RefugeeUtils.isHeldRefugee(c)) {
			return;
		}
		ai.setBeingConverted(false);
		c.setSide(FactionUtils.FactionSide.Player,
				SpaceHaven.library.getFactionById(playerFactionID));
		c.clearPermission(ShipHelper.AreaPermission.Refugee);
		c.getWorld().getTimelineStats().increment(GameTimeLineStats.StatType.NewCrewMember,
				c.getFullName());
		GameLog.addLog(c.getFullName() + " recruited", GameLog.LogType.Good, c);

	}
}
