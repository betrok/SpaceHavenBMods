package spacehavenbmods.MoreRefugeeActions;

import fi.bugbyte.spacehaven.ai.EncounterAI;
import fi.bugbyte.spacehaven.stuff.Character;
import fi.bugbyte.spacehaven.stuff.FactionUtils;
import fi.bugbyte.spacehaven.world.World;

import com.badlogic.gdx.utils.IntSet;
import java.lang.System;
import java.lang.reflect.Field;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

// Disables communication call from AI about refugees if all of them are fine with staying on player fleet
@Aspect
public class CallPlayerAspect {
	// Misleading method name, it also checks whether AI can take refugess.
	// With true as argument it is only called on call reason pick, exactly what we need to adjust.
	@Around("execution(public boolean fi.bugbyte.spacehaven.ai.EncounterAI.playerHasMyRefugees(..)) && this(ai) && args(checkCall)")
	public boolean playerHasMyRefugees(ProceedingJoinPoint jp, EncounterAI ai,
			final boolean checkCall) throws Throwable {

		System.out.printf("playerHasMyRefugees: checkCall: %b\n", checkCall);
		if (!checkCall) {
			return (boolean) jp.proceed();
		}
		if (!ai.canTakeRefugeesBack()) {
			return false;
		}

		Field worldField = ai.getClass().getDeclaredField("world");
		worldField.setAccessible(true);
		final World world = (World) worldField.get(ai);
		Field sideField = ai.getClass().getDeclaredField("side");
		sideField.setAccessible(true);
		final FactionUtils.FactionSide side = (FactionUtils.FactionSide) sideField.get(ai);

		IntSet crewIds = RefugeeUtils.collectCrewIds(world);
		int crewMood = RefugeeUtils.averageCrewMood(world);

		System.out.printf("playerHasMyRefugees: side %s, crew mood: %d, crew ids: %s (%d)\n",
				side.toString(), crewMood, crewIds.toString(), crewIds.size);

		for (final Character c : RefugeeUtils.getAllHeldRefugges(world)) {
			if (c.getOrginalSide() != side) {
				continue;
			}
			if (!RefugeeUtils.fineWithStaying(c, crewMood, crewIds)) {
				return true;
			}
		}

		return false;
	}
}
