package spacehavenbmods.MoreRefugeeActions;

import java.util.HashMap;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;

import fi.bugbyte.spacehaven.stuff.Character;
import fi.bugbyte.spacehaven.stuff.Entity;
import fi.bugbyte.spacehaven.stuff.FactionUtils;
import fi.bugbyte.spacehaven.stuff.crafts.Craft;
import fi.bugbyte.spacehaven.stuff.personality.Social;
import fi.bugbyte.spacehaven.world.Ship;
import fi.bugbyte.spacehaven.world.ShipHelper;
import fi.bugbyte.spacehaven.world.World;

public class RefugeeUtils {
	final static HashMap<Social.RelationshipStatus, MoodPair> stayRequirements;
	final static HashMap<Social.RelationshipStatus, MoodPair> hireRequirements;

	static {
		stayRequirements = new HashMap<Social.RelationshipStatus, MoodPair>();
		stayRequirements.put(Social.RelationshipStatus.Enemies, new MoodPair(70, 70));
		stayRequirements.put(Social.RelationshipStatus.Dislike, new MoodPair(65, 65));
		stayRequirements.put(Social.RelationshipStatus.Acquaintance, new MoodPair(60, 60));
		stayRequirements.put(Social.RelationshipStatus.Friends, new MoodPair(50, 50));
		stayRequirements.put(Social.RelationshipStatus.GoodFriends, new MoodPair(40, 40));
		// lovers and best friends want to stay unless personally misreable
		stayRequirements.put(Social.RelationshipStatus.BestFriends, new MoodPair(30, 0));
		stayRequirements.put(Social.RelationshipStatus.Lovers, new MoodPair(30, 0));

		hireRequirements = new HashMap<Social.RelationshipStatus, MoodPair>();
		hireRequirements.put(Social.RelationshipStatus.Enemies, new MoodPair(75, 75));
		hireRequirements.put(Social.RelationshipStatus.Dislike, new MoodPair(70, 70));
		hireRequirements.put(Social.RelationshipStatus.Acquaintance, new MoodPair(65, 65));
		hireRequirements.put(Social.RelationshipStatus.Friends, new MoodPair(55, 55));
		hireRequirements.put(Social.RelationshipStatus.GoodFriends, new MoodPair(40, 40));
		hireRequirements.put(Social.RelationshipStatus.BestFriends, new MoodPair(30, 30));
		hireRequirements.put(Social.RelationshipStatus.Lovers, new MoodPair(30, 30));
	}

	public static class MoodPair {
		public int personal;
		public int crewAverage;

		public MoodPair(int personal, int crewAverage) {
			this.personal = personal;
			this.crewAverage = crewAverage;
		}
	}

	// Returns all refugee characters held by player
	static Array<Character> getAllHeldRefugges(World world) {
		Array<Character> ret = new Array<Character>(false, 32);

		for (final Ship ship : world.getShips()) {
			for (final Character c : ship.getCharacters()) {
				if (isHeldRefugee(c)) {
					ret.add(c);
				}
			}
		}
		for (final Craft craft : world.getCrafts()) {
			for (final Entity e : craft.getEntitysOnBoard()) {
				if (e instanceof Character) {
					final Character c = (Character) e;
					if (isHeldRefugee(c)) {
						ret.add(c);
					}
				}
			}
		}

		for (Entity e : world.getSpaceWalkers()) {
			if (e instanceof Character) {
				final Character c = (Character) e;
				if (isHeldRefugee(c)) {
					ret.add(c);
				}
			}
		}

		return ret;
	}

	// returns true if character is refugee under players custody
	static boolean isHeldRefugee(final Character c) {
		if (c.isPlayerChar() || c.getOwnerSide() != FactionUtils.FactionSide.Player) {
			return false;
		}
		return (c.getAi().getPermissionFlags() & ShipHelper.AreaPermission.Refugee.bitTest) > 0;
	}

	// average longterm mood of player crew
	static int averageCrewMood(final World world) {
		int total = 0;
		int count = 0;
		for (final Ship ship : world.getShips()) {
			for (final Character c : ship.getCharacters()) {
				if (c.isPlayerChar()) {
					total += c.getMoodProp().getLongTerm();
					count++;
				}
			}
		}
		for (final Craft craft : world.getCrafts()) {
			for (final Entity e : craft.getEntitysOnBoard()) {
				if (e instanceof Character) {
					final Character c = (Character) e;
					if (c.isPlayerChar()) {
						total += c.getMoodProp().getLongTerm();
						count++;
					}
				}
			}
		}

		for (Entity e : world.getSpaceWalkers()) {
			if (e instanceof Character) {
				final Character c = (Character) e;
				if (c.isPlayerChar()) {
					total += c.getMoodProp().getLongTerm();
					count++;
				}
			}
		}

		if (count > 1) {
			return total / count;
		}
		return total;
	}

	// returns set of ids of all player crew
	static IntSet collectCrewIds(final World world) {
		IntSet ret = new IntSet(32);
		for (final Ship ship : world.getShips()) {
			for (final Character c : ship.getCharacters()) {
				if (c.isPlayerChar()) {
					ret.add(c.getEntityId());
				}
			}
		}
		for (final Craft craft : world.getCrafts()) {
			for (final Entity e : craft.getEntitysOnBoard()) {
				if (e instanceof Character) {
					final Character c = (Character) e;
					if (c.isPlayerChar()) {
						ret.add(c.getEntityId());
					}
				}
			}
		}

		for (Entity e : world.getSpaceWalkers()) {
			if (e instanceof Character) {
				final Character c = (Character) e;
				if (c.isPlayerChar()) {
					ret.add(c.getEntityId());
				}
			}
		}

		return ret;
	}

	// Returns highest relationships status between character c and all of characters with ids.
	// If norelationships were found, Acquaintance is returned.
	static Social.RelationshipStatus bestRelationships(Character c, IntSet ids) {
		Social.RelationshipStatus ret = Social.RelationshipStatus.Enemies;
		boolean found = false;
		for (Social.Relationship r : c.getPersonality().getSociality().getRelationships()) {
			if (!ids.contains(r.targetId)) {
				continue;
			}
			Social.RelationshipStatus t = r.getStatus();
			if (t.compareTo(ret) > 0) {
				ret = t;
				found = true;
			}
		}
		if (!found) {
			return Social.RelationshipStatus.Acquaintance;
		}
		return ret;
	}

	static boolean fineWithStaying(Character c, int avgMood, IntSet crewIds) {
		Social.RelationshipStatus best = bestRelationships(c, crewIds);
		MoodPair requirements = stayRequirements.get(best);
		if (avgMood < requirements.crewAverage) {
			return false;
		}
		return c.getMoodProp().getLongTerm() >= requirements.personal;
	}

	static boolean acceptsHire(Character c, int avgMood, IntSet crewIds) {
		Social.RelationshipStatus best = bestRelationships(c, crewIds);
		MoodPair requirements = hireRequirements.get(best);
		if (avgMood < requirements.crewAverage) {
			return false;
		}
		return c.getMoodProp().getLongTerm() >= requirements.personal;
	}
}
