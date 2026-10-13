package spacehavenbmods.MoreRefugeeActions;

import com.badlogic.gdx.utils.Array;
import fi.bugbyte.spacehaven.stuff.Character;
import fi.bugbyte.spacehaven.stuff.Entity;
import fi.bugbyte.spacehaven.stuff.FactionUtils;
import fi.bugbyte.spacehaven.stuff.crafts.Craft;
import fi.bugbyte.spacehaven.world.Ship;
import fi.bugbyte.spacehaven.world.ShipHelper;
import fi.bugbyte.spacehaven.world.World;

public class RefugeeUtils {
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
}
