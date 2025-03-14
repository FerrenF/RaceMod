package overrides;

import java.lang.reflect.Field;

import core.forms.FormNewPlayerRaceCustomizer;
import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import helpers.DebugHelper;
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
import necesse.gfx.forms.components.FormComponent;
import necesse.gfx.forms.components.FormFlow;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormTextInput;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.forms.position.FormPosition;
import necesse.gfx.forms.position.FormPositionContainer;
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
			
			PlayerMob player = this.getPlayer();									
			this.onCreatePressed(player);
		});
		
		this.cancelButton = (FormLocalTextButton) this.addComponent(new FormLocalTextButton("ui", "connectcancel",
				this.getWidth() / 2 + 2, buttonsY, this.getWidth() / 2 - 6));
		
		this.cancelButton.onClicked((e) -> {
			this.onCancelPressed();
		});
		
		this.updateCreateButton();
		this.setHeight(flow.next());	
		this.onWindowResized(WindowManager.getWindow());
		
		this.newPlayerFormPreset.onComponentSizeChanged((event) -> {
			
		    int totalHeight = 0;
		    for (FormComponent c : this.getComponents()) {
		        if (c == this.newPlayerFormPreset) continue; // Skip the preset

		        if (c instanceof Form) { 
		            totalHeight += ((Form) c).getHeight();
		        } else {
		            totalHeight += c.getBoundingBox().height;
		        }
		    }
		    
		    totalHeight += this.newPlayerFormPreset.getHeight();
		    
		    int currentHeight = this.getHeight();
		    int componentOffset = totalHeight - currentHeight;
		    for (FormComponent c : this.getComponents()) {
		    	if (c instanceof FormPositionContainer) {
		    		FormPositionContainer pc = ((FormPositionContainer)c);
		    		if(pc.getY()>newPlayerFormPreset.getY()) {
		    			pc.setY(pc.getY()+componentOffset);
		    		}
		    	}
		    }
		    
		    this.setHeight(totalHeight);
		});
	
	}
	
	public void setLook(HumanLook look) {
		this.setLook(new CustomHumanLook(look));
		//do NUFFIN
	}
	
	public void setLook(RaceLook look) {
		this.newPlayerFormPreset.setLook(look);
	}
	
	public RaceLook getLook() {		
		return this.newPlayerFormPreset.getRaceLook();
	}
	
	public abstract void onCreatePressed(PlayerMob var1);

	public abstract void onCancelPressed();

	public void reset() {
		
		this.newPlayerFormPreset.reset();
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
		
		RaceLook getMeFirst = this.newPlayerFormPreset.newPlayerFormContents.getRaceLook();
		PlayerMob player = this.newPlayerFormPreset.newPlayerFormContents.getNewPlayer();
		player.playerName = this.nameInput.getText().trim();		
		
		DebugHelper.handleDebugMessage("Attempting to save player " + player.playerName + " with race "+this.newPlayerFormPreset.newPlayerFormContents.getRaceID() ,45);
		RaceDataFactory.getOrRegisterRaceData(player,getMeFirst);
		DebugHelper.handleDebugMessage("Check factory exists: " + (RaceDataFactory.hasRaceData(player) ? "true" : "false") ,45);
		DebugHelper.handleDebugMessage("Check factory race exists: " + (RaceDataFactory.getRaceData(player).raceDataInitialized ? "true" : "false") ,45);
		
		return player;
	}

	public void initSavePlayer() {
		RaceDataFactory.getOrRegisterRaceData(getPlayer(), this.newPlayerFormPreset.newPlayerFormContents.getRaceLook());
	}
}