package spacehavenbmods.MoreRefugeeActions;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Field;
import java.lang.System;
import java.util.Comparator;

import com.badlogic.gdx.utils.Array;

import fi.bugbyte.spacehaven.gui.popups.ReachedEdenPopup;
import fi.bugbyte.spacehaven.gui.popups.DialogBoxScreen;
import fi.bugbyte.spacehaven.gui.StarMapScreen;
import fi.bugbyte.spacehaven.gui.GameLog;
import fi.bugbyte.spacehaven.stuff.Character;
import fi.bugbyte.spacehaven.world.Ship;
import fi.bugbyte.gen.compiled.*;
import fi.bugbyte.framework.screen.StageButton;

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
		StarMapScreen sm = (StarMapScreen) field.get(popup);

		Array<Character> refugees = RefugeeUtils.getAllHeldRefugges(sm.getWorld());
		System.out.println("Number of refugees: " + refugees.size);
		if (refugees.isEmpty()) {
			return;
		}
		refugees.sort((final Character c1, final Character c2) -> {
			int cmp = c1.getOrginalSide().compareTo(c2.getOrginalSide());
			if (cmp != 0) {
				return cmp;
			}
			return c1.getFullName().compareTo(c2.getFullName());
		});

		TextIconButton1 button = TextButtons2.getBase();
		button.setText("Drop off refugges");
		button.setClickHandler(() -> {
			final CharacterSelectPopup p = new CharacterSelectPopup("Select refugees to drop off", refugees,
					(Array<Character> selected) -> {
						refugeeSelected(selected);
					});
			popup.openCustomPopup("", 0, p);
		});
		// layout managment does not exist, yet I want shortcut to calculate position,
		// let it just be between two original buttons
		final Array<StageButton> originalButtons = popup.getButtonManager().getButtons();
		float x = 0, y = 0;
		for (final StageButton b : originalButtons) {
			y = b.getY();
			x += b.getX();
		}
		if (originalButtons.size != 2) {
			GameLog.addLog("Unexpected buttons number in ReachedEdenPopup", GameLog.LogType.Error, null);
			x = 0;
		} else {
			x /= originalButtons.size;
		}
		button.moveTo(x, y);

		popup.getButtonManager().addButton(button);
	}

	public boolean refugeeSelected(Array<Character> refugees) {
		System.out.println("Number of selected refugees: " + refugees.size);
		return false;
	}
}
