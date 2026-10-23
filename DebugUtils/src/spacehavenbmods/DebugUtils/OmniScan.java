package spacehavenbmods.DebugUtils;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Gdx;
import java.lang.reflect.Field;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

import fi.bugbyte.spacehaven.gui.StarMapScreen;
import fi.bugbyte.spacehaven.starmap.Bodies;
import fi.bugbyte.spacehaven.starmap.StarMap;

@Aspect
public class OmniScan {
	@After("execution(* fi.bugbyte.spacehaven.gui.StarMapScreen.doScanPulse(..)) && this(screen)")
	public void doScanPulse(StarMapScreen screen) throws Throwable {
		if(!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
			return;
		}
		
		Field mapField = screen.getClass().getDeclaredField("map");
		mapField.setAccessible(true);
		final StarMap map = (StarMap) mapField.get(screen);
		
		if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
			for (final StarMap.StarMapSystem system : map.getSystems()) {
				if(!system.isVisible()) {
					system.setVisible();
				}
				for (final Bodies.AbsBody b : system.bodies) {
					b.setVisible(true);
					final StarMap.SectorBase sec = b.getSectorBase();
					if (sec == null) {
						continue;
					}
					sec.calcCenter();
				}
				for (final StarMap.Sector e : system.getEmptySectors()) {
					e.setVisible(true);
					final StarMap.SectorBase sec = e.getSectorBase();
					if (sec != null) {
						sec.calcCenter();
					}
				}
			}
		}
		
		for (final StarMap.StarMapSystem system : map.getSystems()) {
			if(!system.isVisible()) {
				continue;
			}
			for (final Bodies.AbsBody b : system.bodies) {
				if(!b.isVisible()) {
					continue;
				}
				if (b.getFleet() == null) {
					continue;
				}
				for (final StarMap.Fleet f : b.getFleet()) {
					f.setScannedByPlayer(true);
				}
			}
			for (final StarMap.Sector e : system.getEmptySectors()) {
				if(!e.isVisible()) {
					continue;
				}
				if (e.getFleet() == null) {
					continue;
				}
				for (final StarMap.Fleet f : e.getFleet()) {
					f.setScannedByPlayer(true);
				}
			}
		}
		for (final StarMap.JumpingFleet f : map.getJumpingFleet()) {
			if((f.from != null && f.from.isVisible()) || (f.to != null && f.to.isVisible())) {
				f.fleet.setScannedByPlayer(true);
			}
		}
	}
}