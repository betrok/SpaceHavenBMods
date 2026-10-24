package spacehavenbmods.MoreRefugeeActions;

import fi.bugbyte.framework.Game;
import fi.bugbyte.framework.screen.StageButton;
import fi.bugbyte.gen.compiled.TextIconButton1;
import fi.bugbyte.gen.compiled.TextButtons2;
import fi.bugbyte.spacehaven.ai.Job;
import fi.bugbyte.spacehaven.gui.GUI;
import fi.bugbyte.spacehaven.stuff.Character;
import fi.bugbyte.spacehaven.stuff.Entity;
import fi.bugbyte.spacehaven.stuff.personality.Personality;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

// Splits refugee schedules into 3 shifts offset by 8 hours
@Aspect
public class ScheduleAspect {
	// surely there will be no issues with global counter...
	static private int nextShift = 0;

	// Select one of 3 schedule offsets
	@After("execution(void fi.bugbyte.spacehaven.stuff.personality.Personality.setRefugeeSchedule()) && this(personality)")
	public void setRefugeeSchedule(Personality personality) throws Throwable {
		offsetShedule(personality, 8 * nextShift);
		nextShift = (nextShift + 1) % 3;
	}

	@After("execution(void fi.bugbyte.spacehaven.stuff.personality.Personality.setPrisonerSchedule()) && this(personality)")
	public void setPrisonerSchedule(Personality personality) throws Throwable {
		offsetShedule(personality, 8 * nextShift);
		nextShift = (nextShift + 1) % 3;
	}

	// add schedule shift switch button when refugee is selected
	@After("execution(private void fi.bugbyte.spacehaven.gui.GUI.SelectedElements.finishSelection()) && this(selected)")
	public void addButton(GUI.SelectedElements selected) throws Throwable {
		for (Entity e : selected.getSelectedEntities()) {
			if (!(e instanceof Character)) {
				continue;
			}
			Character c = (Character) e;
			if (!RefugeeUtils.isHeldRefugee(c) && !RefugeeUtils.isHeldPrisoner(c)) {
				continue;
			}

			TextIconButton1 button = TextButtons2.getIconBase2();
			button.text = "OFFSET SCHEDULE";
			button.toolTipText = "Offset character schedule by 8 hours";
			button.icon = Game.library.getAnimation("smallScheduleIcon1");
			// I naively thought that this class will calculate button width based on content.
			// Silly me, values for valilla buttons seems to be generated externally and hardcoded
			// regardless of locale.
			button.width = 67;
			button.onGet();

			button.setClickHandler(() -> {
				for (Entity s : selected.getSelectedEntities()) {
					if (!(s instanceof Character)) {
						continue;
					}
					Character ch = (Character) s;
					if (!RefugeeUtils.isHeldRefugee(ch) && !RefugeeUtils.isHeldPrisoner(c)) {
						continue;
					}
					try {
						offsetShedule(ch.getPersonality(), 8);
					} catch (final Throwable t) {
					}
				}
			});

			Method addButtonMethod = selected.getClass()
					.getDeclaredMethod("addSelectionButton", StageButton.class);
			addButtonMethod.setAccessible(true);
			addButtonMethod.invoke(selected, button);

			return;
		}
	}

	// Rotates schedule by requested hours
	static void offsetShedule(Personality personality, int hours) throws Throwable {
		hours %= 24;
		if (hours == 0) {
			return;
		}
		Field scheduleField = personality.getClass().getDeclaredField("schedule");
		scheduleField.setAccessible(true);
		Job.ScheduleTask[] schedule = (Job.ScheduleTask[]) scheduleField.get(personality);
		rotate(schedule, hours);

		Field secondaryField = personality.getClass().getDeclaredField("secondary");
		secondaryField.setAccessible(true);
		Job.SecondaryScheduleTask[] secondary = (Job.SecondaryScheduleTask[]) secondaryField
				.get(personality);
		rotate(secondary, hours);
	}

	static <T> void rotate(T[] data, int offset) {
		T[] tmp = data.clone();
		System.arraycopy(data, data.length - offset, tmp, 0, offset);
		System.arraycopy(data, 0, data, offset, data.length - offset);
		System.arraycopy(tmp, 0, data, 0, offset);
	}
}