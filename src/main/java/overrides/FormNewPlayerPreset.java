package overrides;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import core.forms.FormNewPlayerRaceCustomizer;
import core.forms.events.ComponentSizeChanged;
import core.forms.events.ComponentSizeChangedListener;
import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import core.registries.RaceRegistry;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.gfx.GameBackground;
import necesse.gfx.HumanLook;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormButton;
import necesse.gfx.forms.components.FormContentBox;
import necesse.gfx.forms.components.FormFlow;
import necesse.gfx.forms.components.FormIconButton;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.forms.events.FormEventListener;
import necesse.gfx.forms.events.FormInputEvent;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonStateTextures;
import necesse.gfx.ui.GameInterfaceStyle;


public class FormNewPlayerPreset extends Form {	
	
	public static int RACE_SWITCH_FORM_WIDTH = 100;
	public FormNewPlayerRaceCustomizer newPlayerFormContents;
	public FormLocalLabel headerForm;
	public FormLocalTextButton nextRaceButton;
	public FormLocalTextButton prevRaceButton;
	
	public final List<String> raceIDs; 
	public int currentRaceIndex = 0;
	public boolean allowRaceChange;
	public Class<? extends RaceLook> startingRaceClass;
	
	private final List<ComponentSizeChangedListener> componentSizeChanged = new ArrayList<>();
	
	public FormNewPlayerPreset(int x, int y, int width, boolean allowSupernaturalChanges, boolean allowClothesChance) {
		this(x, y, width, allowSupernaturalChanges, allowClothesChance, true, new CustomHumanLook(true));
	}
	
	public FormNewPlayerPreset(int x, int y, int width, boolean allowSupernaturalChanges, boolean allowClothesChance, boolean allowRaceChange, RaceLook startingRaceLook) {
		super(width, 0);			
		this.allowRaceChange = allowRaceChange;
		this.raceIDs = RaceRegistry.getRaces().stream().map((r)->r.getRaceID()).toList();
		this.setupCustomizer(x, y, width, allowSupernaturalChanges, allowClothesChance, startingRaceLook);
		this.setupRaceButtons();
	}
	
	protected void setupCustomizer(int x, int y, int width, boolean allowSupernaturalChanges, boolean allowClothesChance, RaceLook startingRace) {
		startingRaceClass = startingRace.getClass();
		changeRace(startingRace.getRaceID());
	}
	
	protected void setupRaceButtons() {
		
		if(!allowRaceChange) return;
		int modifiedRaceButtonSize = (RACE_SWITCH_FORM_WIDTH + 50);
		//int x, int y, int width, FormInputSize size, ButtonColor color, ButtonIcon icon,	GameMessage... tooltips
		
		List<RaceLook> rlist = RaceRegistry.getRaces();
		
		int raceButtonSize = 64;
		int padding = 10;
		int x = 5;
		int yi = 0;
		int increment = 64+padding;
		for(RaceLook r : rlist) {
			int y = (this.getHeight() / 2) - (increment * rlist.size())/2;
			int sy = y+(yi*increment); 
			FormContentBox formWrapper = new FormContentBox(x, sy, raceButtonSize, raceButtonSize, GameBackground.form);
			FormIconButton testButton = new FormIconButton(
					0,
					0,
					new ButtonStateTextures(GameInterfaceStyle.getStyle("primal"), r.getCustomizerIconPath()),
					raceButtonSize, raceButtonSize,
					new LocalMessage("racemod.race", r.getRaceID())					
					);
			
			testButton.onClicked((FormEventListener<FormInputEvent<FormButton>>)(event)->{
				FormNewPlayerPreset.this.changeRace(r.getRaceID());
			});
			formWrapper.addComponent(testButton);
			this.addComponent(formWrapper);
			yi+=1;
		}

		//this.updateRaceButtons();
	}
		
	public void onComponentSizeChanged(ComponentSizeChangedListener listener) {
	        this.componentSizeChanged.add(listener);
	}
	
	private void triggerComponentResized() {
        ComponentSizeChanged event = new ComponentSizeChanged(this);
        for (ComponentSizeChangedListener listener : this.componentSizeChanged) {
            listener.handleEvent(event);
        }
	 }
	
	public List<String> getLoadedRaceList(){
		return this.raceIDs;
	}
	
	public void changeRace(String newRaceID) {	
		DebugHelper.handleDebugMessage("Change race requested with new race ID "+newRaceID, 50, MESSAGE_TYPE.DEBUG);
		if(RaceRegistry.getRace(newRaceID) == null) {
			newRaceID = this.raceIDs.get(0);
		}
		try {
			Constructor<? extends RaceLook> constructor = RaceRegistry.getRace(newRaceID).getClass().getConstructor(boolean.class);	
			RaceLook newRaceLook = constructor.newInstance(true);
			if(this.newPlayerFormContents!=null)this.removeComponent(newPlayerFormContents);
			this.newPlayerFormContents = newRaceLook.associatedCustomizerForm
					.getConstructor(int.class, int.class, int.class, boolean.class, boolean.class)
					.newInstance(RACE_SWITCH_FORM_WIDTH, this.getY(), this.getWidth()-(RACE_SWITCH_FORM_WIDTH*2), true, true);
			this.newPlayerFormContents.setRaceLook(newRaceLook);		
			this.addComponent(newPlayerFormContents);		
			this.setHeight(newPlayerFormContents.getHeight());
			this.onChanged();
			DebugHelper.handleFormattedDebugMessage("Player switching to new race %s with new form %s" ,40, MESSAGE_TYPE.DEBUG, new Object[] {newRaceLook.getClass().getName(), newPlayerFormContents.getClass().getName()});
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			DebugHelper.handleDebugMessage("Failed to switch to new race " + newRaceID + " from source: New Player Customizer Form with error: "+e.getMessage() ,5);
			e.printStackTrace();
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
		
		if(this.newPlayerFormContents.getRaceLook()!=null) {
			RaceLook ra = this.newPlayerFormContents.getRaceLook();
			ra.copy(look);
			RaceDataFactory.getOrRegisterRaceData(this.newPlayerFormContents.getPlayerHelper(),ra);
		}
		else {				
			Constructor<? extends RaceLook> constructor;
			try {
				constructor = this.startingRaceClass.getConstructor(boolean.class);
				RaceLook newRaceLook = constructor.newInstance(true);
				newRaceLook.copy(look);
				this.setLook(newRaceLook);
				return;
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
			this.newPlayerFormContents.setLook(look);
		}
	}
	
	public RaceLook getRaceLook() {
		return this.newPlayerFormContents.getRaceLook();
	}
	
	public HumanLook getLook() {
		return this.newPlayerFormContents.getPlayerHelper().look;
	}
	
	public void onChanged() {
		this.triggerComponentResized();
		this.newPlayerFormContents.onChanged();
	}

	public void reset() {
		// TODO Auto-generated method stub
		
	}

}