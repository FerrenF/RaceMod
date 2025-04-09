package overrides;

import java.lang.reflect.Field;

import core.network.CustomPacketPlayerAppearance;
import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import necesse.engine.network.client.Client;
import necesse.engine.network.client.ClientClient;
import necesse.engine.network.packet.PacketMobHealth;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.PlayerMob;

import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormComponent;
import necesse.gfx.forms.components.FormFlow;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.FormSlider;
import necesse.gfx.forms.components.FormTextButton;
import necesse.gfx.forms.position.FormPositionContainer;
import overrides.FormNewPlayerPreset;
import necesse.gfx.forms.presets.debug.DebugForm;
import necesse.gfx.gameFont.FontOptions;

public class DebugPlayerForm extends Form {
	public FormNewPlayerPreset newPlayer;
	public FormSlider healthSlider;
	public DebugForm parent;
	
	 @SuppressWarnings("unchecked")
	public RaceLook getRaceLook(PlayerMob target) {
	        try {
	            Field lookField = PlayerMob.class.getDeclaredField("look");
	            lookField.setAccessible(true); // Make private field accessible
	            return (RaceLook) lookField.get(target); // Get the 'look' field
	        } catch (NoSuchFieldException | IllegalAccessException e) {
	            e.printStackTrace(); // Handle error
	            return null; // Or return a default look, as necessary
	        }
	    }
	 public void setRaceLook(PlayerMob target, RaceLook raceLook) {
	        try {
	            Field lookField = PlayerMob.class.getDeclaredField("look");
	            lookField.setAccessible(true); // Make private field accessible
	            lookField.set(target, raceLook); // Set the 'look' field
	        } catch (NoSuchFieldException | IllegalAccessException e) {
	            e.printStackTrace(); // Handle error
	        }
	  }
	public DebugPlayerForm(String name, DebugForm parent) {
		super(name, 500, 10);
		this.parent = parent;
		Client client = parent.client;
		PlayerMob clientPlayer = client.getPlayer();
		FormFlow flow = new FormFlow(10);
		this.addComponent(new FormLabel("Player", new FontOptions(20), 0, this.getWidth() / 2, flow.next(25)));
		this.newPlayer = (FormNewPlayerPreset) this.addComponent(
				(FormNewPlayerPreset) flow.nextY(new FormNewPlayerPreset(0, 35, this.getWidth() - 10, true, true, false,
						clientPlayer.look instanceof RaceLook 
						? (RaceLook)clientPlayer.look 
						: RaceDataFactory.getRaceLook(clientPlayer, new CustomHumanLook(true))), 10));
		
		this.newPlayer.onComponentSizeChanged((event)->{
			
			  int totalHeight = 0;
			    for (FormComponent c : this.getComponents()) {
			        if (c == this.newPlayer) continue; // Skip the preset

			        if (c instanceof Form) { 
			            totalHeight += ((Form) c).getHeight();
			        } else {
			            totalHeight += c.getBoundingBox().height;
			        }
			    }
			    
			    totalHeight += this.newPlayer.getHeight();
			    
			    int currentHeight = this.getHeight();
			    int componentOffset = totalHeight - currentHeight;
			    for (FormComponent c : this.getComponents()) {
			    	if (c instanceof FormPositionContainer) {
			    		FormPositionContainer pc = ((FormPositionContainer)c);
			    		if(pc.getY()>newPlayer.getY()) {
			    			pc.setY(pc.getY()+componentOffset);
			    		}
			    	}
			    }
			    
			    this.setHeight(totalHeight);
		});
		this.healthSlider = (FormSlider) this
				.addComponent((FormSlider) flow.nextY(new FormSlider("Health", 8, 305, clientPlayer.getHealth(), 0,
						clientPlayer.getMaxHealth(), this.getWidth() - 16, new FontOptions(12)), 5));
		this.healthSlider.onGrab((e) -> {
			if (!e.grabbed) {
				int healthValue = ((FormSlider) e.from).getValue();
				ClientClient me = client.getClient();
				me.playerMob.setHealthHidden(healthValue, 0.0F, 0.0F, (Attacker) null, true);
				client.network.sendPacket(new PacketMobHealth(me.playerMob, true));
			}

		});
		this.healthSlider.drawValueInPercent = false;
		int buttonsY = flow.next(40);
		((FormTextButton) this.addComponent(new FormTextButton("Save", 4, buttonsY, this.getWidth() / 2 - 6)))
				.onClicked((e) -> {
					PlayerMob player = this.newPlayer.newPlayerFormContents.getNewPlayer();
					ClientClient clientClient = client.getClient();
					player.playerName = clientClient.getName();
					clientClient.playerMob.look = player.look;
					//setRaceLook(clientClient.playerMob, new HumanLook(player.look));
					client.network.sendPacket(
							new CustomPacketPlayerAppearance(client.getSlot(), client.getCharacterUniqueID(), player));
				});
		((FormTextButton) this
				.addComponent(new FormTextButton("Back", this.getWidth() / 2 + 2, buttonsY, this.getWidth() / 2 - 6)))
				.onClicked((e) -> {
					parent.makeCurrent(parent.mainMenu);
				});
		
		this.setHeight(flow.next());
		newPlayer.triggerComponentResized();
	}

	public void refreshPlayer() {
		PlayerMob clientPlayer = this.parent.client.getPlayer();
		setRaceLook(clientPlayer,getRaceLook(clientPlayer));
		this.healthSlider.setRange(0, this.parent.client.getPlayer().getMaxHealth());
		this.healthSlider.setValue(this.parent.client.getPlayer().getHealth());
	}
}