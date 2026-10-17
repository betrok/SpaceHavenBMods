package spacehavenbmods.MoreRefugeeActions;

import fi.bugbyte.spacehaven.gui.popups.CommPopup;

import java.lang.System;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class CommPopupAspect {
	@Around("execution(void fi.bugbyte.spacehaven.gui.popups.CommPopup.addRefugees(..)) && this(popup)")
	public void addRefugees(JoinPoint jp, CommPopup popup) {
		// Very akward pincut, method is large, one of arguments is private nested class,
		// some private method calls too...
		System.out.println("comms popup");
	}
}
