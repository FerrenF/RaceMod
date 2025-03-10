package overrides;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import core.RaceMod;
import core.race.CustomHumanLook;
import core.registries.RaceRegistry;
import extensions.FormNewPlayerRaceCustomizer;
import extensions.HumanNewPlayerRaceCustomizer;
import extensions.RaceLook;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.localization.message.GameMessage;
import necesse.gfx.HumanLook;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormFlow;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.forms.events.FormInputEvent;
import necesse.gfx.gameFont.FontOptions;


public class FormNewPlayerPreset extends Form {	
	
	public static int RACE_SWITCH_FORM_WIDTH = 100;
	public FormNewPlayerRaceCustomizer newPlayerFormContents;
	public FormLocalLabel headerForm;
	public FormLocalTextButton nextRaceButton;
	public FormLocalTextButton prevRaceButton;
	
	public final List<String> raceIDs; 
	public int currentRaceIndex = 0;
			
	public FormNewPlayerPreset(int x, int y, int width, boolean allowSupernaturalChanges, boolean allowClothesChance) {
		super(width, 0);		
		
		this.raceIDs = RaceRegistry.getRaces().stream().map((r)->r.getRaceID()).toList();
		this.newPlayerFormContents = new HumanNewPlayerRaceCustomizer(RACE_SWITCH_FORM_WIDTH, y, width-(RACE_SWITCH_FORM_WIDTH*2), allowSupernaturalChanges, allowClothesChance);
		this.setLook(new CustomHumanLook(true));
		this.addComponent(newPlayerFormContents);	
		this.setHeight(newPlayerFormContents.getHeight());
		this.setupRaceButtons();

	}
	private void setupRaceButtons() {
		
		int modifiedRaceButtonSize = (RACE_SWITCH_FORM_WIDTH + 50);
		this.prevRaceButton = (FormLocalTextButton) this.addComponent(new FormLocalTextButton("racemodui", "prevrace", 5 , 25, modifiedRaceButtonSize - 10));		
		this.prevRaceButton.onClicked((e) -> {
			
			this.onPrevRaceButtonClicked(e);
		});
		
		this.nextRaceButton = (FormLocalTextButton) this.addComponent(new FormLocalTextButton("racemodui", "nextrace",  this.getWidth() - (modifiedRaceButtonSize + 10) , 25, (modifiedRaceButtonSize - 10)));		
		this.nextRaceButton.onClicked((e) -> {
			
			this.onNextRaceButtonClicked(e);
		});
		
		this.updateRaceButtons();
	}
	
	private void updateRaceButtons() {
		
		int prevRaceInd = (this.currentRaceIndex - 1) >= 0 ? this.currentRaceIndex - 1 : this.raceIDs.size()-1;
		int nextRaceInd = (this.currentRaceIndex + 1) % this.raceIDs.size();
		
		RaceLook prevRace = RaceRegistry.getRace(this.raceIDs.get(prevRaceInd));
		RaceLook nextRace = RaceRegistry.getRace(this.raceIDs.get(nextRaceInd));

		
		this.prevRaceButton.setText(prevRace.getRaceDisplayName().translate());
		this.nextRaceButton.setText(nextRace.getRaceDisplayName().translate());
	}
	private void onPrevRaceButtonClicked(FormInputEvent<FormButton> e) {
		this.alternateRace(-1);
	}
	
	private void onNextRaceButtonClicked(FormInputEvent<FormButton> e) {
		this.alternateRace(1);
	}
	
	private void alternateRace(int direction) {
		
		if(currentRaceIndex + direction < 0) this.currentRaceIndex = this.raceIDs.size() - 1;
		else this.currentRaceIndex = (currentRaceIndex + direction) % this.raceIDs.size();	
		
		String newRaceID = this.raceIDs.get(this.currentRaceIndex);
		this.changeRace(newRaceID);
		this.updateRaceButtons();
	}
	
	private void changeRace(String newRaceID) {		
		if(this.newPlayerFormContents.getRaceID()!=newRaceID) {
			RaceMod.handleDebugMessage("Player switching to new race " + newRaceID + " from source: New Player Customizer Form" ,40);
			try {

				Constructor<? extends RaceLook> constructor = RaceRegistry.getRace(newRaceID).getClass().getConstructor(boolean.class);	
				RaceLook newRaceLook = (RaceLook)constructor.newInstance(true);
						
				Constructor<? extends FormNewPlayerRaceCustomizer> newPresetForm = newRaceLook.associatedCustomizerForm.getConstructor(int.class, int.class, int.class, boolean.class, boolean.class);
				this.newPlayerFormContents.dispose();
				this.newPlayerFormContents = (FormNewPlayerRaceCustomizer)newPresetForm.newInstance(RACE_SWITCH_FORM_WIDTH, this.getY(), this.getWidth()-(RACE_SWITCH_FORM_WIDTH*2), true, true);
				this.addComponent(newPlayerFormContents);	
				
				this.setLook(newRaceLook);				
				
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				RaceMod.handleDebugMessage("Failed to switch to new race " + newRaceID + " from source: New Player Customizer Form with error: "+e.getMessage() ,5);
				e.printStackTrace();
			}
		}
	}
	
	public void drawEdge(TickManager tickManager) {
		// no edge
	}
	
	public Form addHeader(GameMessage message, int size, Form form) {
		Form out = new Form(form.getWidth(), 0);
		out.drawBase = false;
		FormFlow flow = new FormFlow();
		out.addComponent((FormLocalLabel) flow.nextY(
				new FormLocalLabel(message, new FontOptions(size), 0, form.getWidth() / 2, 0, form.getWidth() - 20)));
		out.addComponent((Form) flow.nextY(form));
		out.setHeight(flow.next() + 10);
		return out;
	}
	
	public void setLook(RaceLook look) {
		this.newPlayerFormContents.setRaceLook(look);
	}
	
	public void setLook(HumanLook look) {
		this.newPlayerFormContents.setLook(RaceLook.fromHumanLook(look));
	}
	
	public RaceLook getLook() {
		return this.newPlayerFormContents.getRaceLook();
	}

}