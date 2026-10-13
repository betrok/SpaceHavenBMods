package spacehavenbmods.MoreRefugeeActions;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import fi.bugbyte.framework.Settings;
import fi.bugbyte.framework.graphics.Renderer2D;
import fi.bugbyte.framework.input.GameInput;
import fi.bugbyte.framework.screen.GuiSkin;
import fi.bugbyte.framework.screen.ListItem;
import fi.bugbyte.framework.screen.ScrollList;
import fi.bugbyte.framework.screen.StageButton;
import fi.bugbyte.gen.compiled.CheckBoxes1;
import fi.bugbyte.gen.compiled.GuiSkins3;
import fi.bugbyte.gen.compiled.TextButtons2;
import fi.bugbyte.gen.compiled.TextIconButton1;
import fi.bugbyte.spacehaven.gui.EntityAndCraftInfo;
import fi.bugbyte.spacehaven.gui.GUI;
import fi.bugbyte.spacehaven.gui.Notes;
import fi.bugbyte.spacehaven.gui.popups.DialogBoxScreen;
import fi.bugbyte.spacehaven.stuff.Character;
import fi.bugbyte.spacehaven.stuff.Entity;

public class CharacterSelectPopup extends DialogBoxScreen implements EntityAndCraftInfo.CrewSelectCallback {
	static final float popupHeight = 400;
	static final float popupWidht = 500;
	final Array<Character> characters;
	ScrollList scroll;
	OnClose onClose;
	CustomizeCard customizeCard;

	// called on dialog close
	public interface OnClose {
		void callback(boolean confirmed, Array<Character> selected);
	}

	// called on creation of each item in list
	public interface CustomizeCard {
		void callback(EntityAndCraftInfo.CrewSelectItem card);
	}

	// customizeCard can be null
	public CharacterSelectPopup(final String title, final Array<Character> characters, OnClose onClose,
			CustomizeCard customizeCard) {
		super(title);
		this.characters = characters;
		this.onClose = onClose;
		this.customizeCard = customizeCard;
		// needed for doOnClose to be called
		onCloseDo = Notes.ObjectivePopupType.None;
	}

	@Override
	public void open(final String text, final int id) {
		sizeH = (int) (popupHeight * Settings.uiScale);
		sizeW = (int) (popupWidht * Settings.uiScale);
		super.open(text, id);

		// scroll builder stiff is mostly copypasted from game code,
		// little-to-no-idea what specific values do
		GuiSkin scrollBox = GuiSkins3.getGeneralTextBox1();
		scrollBox.setSize((popupWidht - 10) * Settings.uiScale, (popupHeight - 150) * Settings.uiScale);
		scrollBox.setPos(this.skin.getPosX() + this.skin.getWidth() / 2.0f - scrollBox.getWidth() / 2.0f
				- 10.0f * Settings.uiScale, this.skin.getPosY() + 10.0f * Settings.uiScale);
		GuiSkin.ListBuilder builder = scrollBox.getListBuilder(false);
		builder.setEmptySizeOnEnds(0.0f);
		builder.setListHOffset(6.0f * Settings.uiScale);
		builder.setListYOffset(2.0f * Settings.uiScale);
		builder.setItemW(scrollBox.getWidth());
		builder.setItemHeight(60.0f * Settings.uiScale);
		builder.setReducedScrollbarItemW(true, 10.0f * Settings.uiScale);
		builder.setScrollBarOffset(6.0f * Settings.uiScale, 0.0f);
		this.scroll = builder.build();

		for (final Character c : characters) {
			EntityAndCraftInfo.CrewSelectItem l = new EntityAndCraftInfo.CrewSelectItem(c, this);
			String name = c.getFullName();
			if (RefugeeUtils.isHeldRefugee(c)) {
				name += "\n[" + c.getOrginalSide() + "]";
			}
			l.setName(name);
			l.setNameOff(70.0f * Settings.uiScale, 26.0f * Settings.uiScale);
			l.setCharOff(0.0f * Settings.uiScale, -6.0f * Settings.uiScale);
			l.setCheckBoxOff(190.0f * Settings.uiScale, 0.0f);
			l.setExtraIconOff(150 * Settings.uiScale, 0);
			l.setCheckBoxValue(false, this, CheckBoxes1.getAllowCheckBox());
			if (customizeCard != null) {
				customizeCard.callback(l);
			}
			scroll.addItem(l);
		}

		TextIconButton1 selectAll = TextButtons2.getAllowAll();
		selectAll.setClickHandler(() -> {
			for (final ListItem l : scroll.getAllListItems()) {
				if (l instanceof EntityAndCraftInfo.CrewSelectItem) {
					final EntityAndCraftInfo.CrewSelectItem c = (EntityAndCraftInfo.CrewSelectItem) l;
					final StageButton b = c.getCheckBox();
					if (c.getCheckBoxValue()) {
						continue;
					}
					b.getClickHandler().clicked();
				}
			}
		});
		TextIconButton1 selectNone = TextButtons2.getAllowNone();
		selectNone.setClickHandler(() -> {
			for (final ListItem l : scroll.getAllListItems()) {
				if (l instanceof EntityAndCraftInfo.CrewSelectItem) {
					final EntityAndCraftInfo.CrewSelectItem c = (EntityAndCraftInfo.CrewSelectItem) l;
					final StageButton b = c.getCheckBox();
					if (!c.getCheckBoxValue()) {
						continue;
					}
					b.getClickHandler().clicked();
				}
			}
		});
		buttonManager.addButton(selectAll);
		buttonManager.addButton(selectNone);

		// selectAll/selectNone buttonts on left side
		float bx = skin.box.getPosX() - skin.box.getWidth() / 2;
		float by = yes.getY();
		final float spacingX = 4.0f * Settings.uiScale;
		bx += 2 * spacingX + selectAll.get9PWidth() / 2;
		selectAll.moveTo(bx, by);
		bx += selectAll.get9PWidth() / 2 + spacingX + selectNone.get9PWidth() / 2;
		selectNone.moveTo(bx, by);

		// default accept/close buttons on right side
		bx = skin.box.getPosX() + skin.box.getWidth() / 2;
		bx -= no.getWidth() / 2 + 2 * spacingX;
		no.moveTo(bx, by);
		bx -= no.getWidth() / 2 + spacingX + yes.getWidth() / 2;
		yes.moveTo(bx, by);
	}

	@Override
	public void draw() {
		super.draw();
		final SpriteBatch gamebatch = gui.beginGuiBatching();
		scroll.draw(gamebatch);
		final Renderer2D renderer = GUI.instance.getWorld().getRenderer();
		gamebatch.end();
		scroll.setScissors(true);
		for (final ListItem i : scroll.getCurrentItems()) {
			if (scroll.isItemVisible(i)) {
				((EntityAndCraftInfo.CrewSelectItem) i).drawChar(gamebatch, renderer);
			}
		}
		scroll.setScissors(false);
	}

	@Override
	protected void update(final float deltaTime) {
		super.update(deltaTime);
		scroll.update(deltaTime);
	}

	@Override
	public void mouseMoved(final int screenX, final int screenY) {
		super.mouseMoved(screenX, screenY);
		scroll.mouseMoved(screenX, screenY);
	}

	@Override
	public void scrolled(final int amount) {
		super.scrolled(amount);
		final int mouseX = GameInput.mouseX;
		final int mouseY = GameInput.mouseY;
		if (scroll.touchOverlaps((float) mouseX, (float) mouseY)) {
			scroll.scrolled(amount);
		}
	}

	@Override
	public void touchDown(final float x, final float y, final int pointer, final int button) {
		super.touchDown(x, y, pointer, button);
		scroll.touchDown(x, y, pointer, button);
	}

	@Override
	public void touchDragged(final float x, final float y, final int pointer) {
		super.touchDragged(x, y, pointer);
		scroll.touchDragged(x, y, pointer);
	}

	@Override
	public void touchUp(final float x, final float y, final int pointer, final int button) {
		super.touchUp(x, y, pointer, button);
		scroll.touchUp(x, y, pointer, button);
	}

	@Override
	protected void doOnClose() {
		Array<Character> selected = new Array<Character>(false, scroll.getSize());
		for (final ListItem i : scroll.getCurrentItems()) {
			EntityAndCraftInfo.CrewSelectItem c = (EntityAndCraftInfo.CrewSelectItem) i;
			if (c.getCheckBoxValue()) {
				selected.add(c.getCharacter());
			}
		}

		onClose.callback(lastAnswer, selected);
	}

	@Override
	public void selected(final EntityAndCraftInfo.CrewSelectItem item, final Entity selected) {
		item.setSelected(false);
	}

	@Override
	public void selectedCheckbox(final EntityAndCraftInfo.CrewSelectItem item, final Entity selected,
			final boolean value) {
	}

}
