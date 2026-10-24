package spacehavenbmods.MoreRefugeeActions;

import fi.bugbyte.spacehaven.ai.EncounterAI;
import fi.bugbyte.spacehaven.ai.EncounterAIHelper;
import fi.bugbyte.spacehaven.stuff.Character;
import fi.bugbyte.spacehaven.stuff.Entity;
import fi.bugbyte.spacehaven.stuff.FactionUtils;
import fi.bugbyte.spacehaven.world.ShipHelper;
import fi.bugbyte.spacehaven.world.World;

import com.badlogic.gdx.utils.IntSet;
import java.lang.reflect.Field;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

// Disables communication call from AI about refugees if all of them are fine with staying on player fleet.
// Handles suspicious too.
@Aspect
public class CallPlayerAspect {
	// Misleading method name, it also checks whether AI can take refugess.
	// With true as argument it is only called on call reason pick, exactly what we need to adjust.
	@Around("execution(public boolean fi.bugbyte.spacehaven.ai.EncounterAI.playerHasMyRefugees(..)) && this(ai) && args(checkCall)")
	public boolean playerHasMyRefugees(ProceedingJoinPoint jp, EncounterAI ai,
			final boolean checkCall) throws Throwable {
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

	// Nothing to be suspicious about if everyone is happy
	@Around("execution(public static boolean fi.bugbyte.spacehaven.world.ShipHelper.factionReactsNegatively(..)) && args(entity, permission, targetSide, reactorSide, type)")
	public boolean factionReactsNegatively(ProceedingJoinPoint jp, Entity entity,
			final ShipHelper.AreaPermission permission, final FactionUtils.FactionSide targetSide,
			final FactionUtils.FactionSide reactorSide,
			final EncounterAIHelper.SuspiciousType type) throws Throwable {
		if (permission != ShipHelper.AreaPermission.Refugee) {
			return (boolean) jp.proceed();
		}
		if (type == EncounterAIHelper.SuspiciousType.EnemyRefugees) {
			if (reactorSide.isEnemy(entity.getOrginalSide())) {
				return true;
			}
		} else if (reactorSide == entity.getOrginalSide()) {
			if (entity.getOwnerSide() != null) {
				final FactionUtils.StancePair pair = entity.getWorld().getHostilityMap()
						.getPair(reactorSide, entity.getOwnerSide());
				if (pair.awareOfCrew) {
					return false;
				}
			}
			if (entity.getOwnerSide() == FactionUtils.FactionSide.Player
					&& entity instanceof Character) {
				Character c = (Character) entity;
				// Somewhat inconvonient to recollect crew ids and mood
				// for each refugee and for each faction in sector.
				// I thought about caching some stuff but it seems like
				// suspicious update only happenes every 10s, perhaps not a big deal.
				IntSet crewIds = RefugeeUtils.collectCrewIds(c.getWorld());
				int crewMood = RefugeeUtils.averageCrewMood(c.getWorld());
				if (RefugeeUtils.fineWithStaying(c, crewMood, crewIds)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
