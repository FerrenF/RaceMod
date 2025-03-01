package overrides;

import java.lang.reflect.Field;

import core.race.CustomHumanLook;
import extensions.RaceLook;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.platforms.PlatformManager;
import necesse.engine.util.GameRandom;
import necesse.engine.util.GameUtils;
import necesse.engine.window.GameWindow;
import necesse.engine.window.WindowManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.HumanLook;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormFlow;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormTextInput;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.forms.position.FormPosition;
import necesse.gfx.gameFont.FontOptions;

public abstract class NewCharacterForm extends Form {
	protected overrides.FormNewPlayerPreset newPlayerFormPreset;
	protected FormTextInput nameInput;
	protected FormLocalTextButton createButton;
	protected FormLocalTextButton cancelButton;
	
	protected String currentRace = CustomHumanLook.HUMAN_RACE_ID;
 
	private static int CONTAINING_FORM_WIDTH = 600;	
	private static int PRESET_FORM_WIDTH = 600;
	public NewCharacterForm(String name) {
		super(name, CONTAINING_FORM_WIDTH, 10);
		FormFlow flow = new FormFlow(5);
		
		this.addComponent(new FormLocalLabel("racemodui", "createnewcharacter", new FontOptions(20), 0, this.getWidth() / 2,
				flow.next(25)));		

		overrides.FormNewPlayerPreset np = new overrides.FormNewPlayerPreset(0, 0, PRESET_FORM_WIDTH, true, true);
		this.newPlayerFormPreset = this.addComponent((FormNewPlayerPreset)flow.nextY(np, 20));
		
		this.setLook(new CustomHumanLook(true));
		
		this.addComponent(new FormLocalLabel("racemodui", "playername", new FontOptions(16), -1, 5, flow.next(18)));
		
		this.nameInput = (FormTextInput) this.addComponent(new FormTextInput(4, flow.next(40),
				FormInputSize.SIZE_32_TO_40, this.getWidth() - 8, GameUtils.getPlayerNameLength().height));
		
		this.nameInput.placeHolder = new LocalMessage("racemodui", "playername");
		this.nameInput.setRegexMatchFull(GameUtils.playerNameSymbolsPattern);
		
		String userName = PlatformManager.getPlatform().getUserName();
		if (userName != null && GameUtils.isValidPlayerName(userName) == null) {
			this.nameInput.setText(userName.trim());
		}

		System.out.println("Entered modified character form...");
		this.nameInput.onChange((e) -> {
			this.updateCreateButton();
		});
		
		int buttonsY = flow.next(40);
		this.createButton = (FormLocalTextButton) this
				.addComponent(new FormLocalTextButton("racemodui", "charcreate", 4, buttonsY, this.getWidth() / 2 - 6));
		
		this.createButton.onClicked((e) -> {
			this.onCreatePressed(this.getPlayer());
		});
		
		this.cancelButton = (FormLocalTextButton) this.addComponent(new FormLocalTextButton("ui", "connectcancel",
				this.getWidth() / 2 + 2, buttonsY, this.getWidth() / 2 - 6));
		
		this.cancelButton.onClicked((e) -> {
			this.onCancelPressed();
		});
		
		this.updateCreateButton();
		this.setHeight(flow.next());
		this.onWindowResized(WindowManager.getWindow());
	}
	
	public void setLook(HumanLook look) {
		this.setLook(new CustomHumanLook(look));
	}
	
	public void setLook(RaceLook look) {
		this.newPlayerFormPreset.setLook(look);
	}
	
	public RaceLook getLook() {		
		return this.newPlayerFormPreset.getLook();
	}
	
	public abstract void onCreatePressed(PlayerMob var1);

	public abstract void onCancelPressed();

	public void reset() {
		this.newPlayerFormPreset.newPlayerFormContents.reset();
		this.nameInput.setText("");
		String userName = PlatformManager.getPlatform().getUserName();
		if (userName != null && GameUtils.isValidPlayerName(userName) == null) {
			this.nameInput.setText(userName.trim());
		}

		this.updateCreateButton();
	}
	
	

	public void updateCreateButton() {
		boolean canCreate = true;
		String name = this.nameInput.getText().trim();
		GameMessage valid = GameUtils.isValidPlayerName(name);
		if (valid != null) {
			canCreate = false;
			this.createButton.setLocalTooltip(valid);
		}

		if (canCreate) {
			this.createButton.setLocalTooltip((GameMessage) null);
		}

		this.createButton.setActive(canCreate);
	}

	public void onWindowResized(GameWindow window) {
		super.onWindowResized(window);
		this.setPosMiddle(window.getHudWidth() / 2, window.getHudHeight() / 2);
	}

	public PlayerMob getPlayer() {
		PlayerMob player = this.newPlayerFormPreset.newPlayerFormContents.getNewPlayer();
		player.playerName = this.nameInput.getText().trim();
		return player;
	}
}