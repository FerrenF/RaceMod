package core.forms;

import java.awt.Color;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Predicate;

import core.containers.CustomRaceStylistContainer;
import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import helpers.DebugHelper;
import helpers.DebugHelper.MESSAGE_TYPE;
import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.localization.Language;
import necesse.engine.localization.Localization;
import necesse.engine.localization.LocalizationChangeListener;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.Packet;
import necesse.engine.network.PacketWriter;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.HumanLook;
import necesse.gfx.fairType.FairGlyph;
import necesse.gfx.fairType.FairItemGlyph;
import necesse.gfx.fairType.FairType;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.FormSwitcher;
import necesse.gfx.forms.components.FormContentVarToggleButton;
import necesse.gfx.forms.components.FormFairTypeLabel;
import necesse.gfx.forms.components.FormFlow;
import overrides.FormNewPlayerPreset;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonIcon;
import necesse.inventory.InventoryItem;

public abstract class CustomRaceStyleForm extends Form {
	public final core.containers.CustomRaceStylistContainer container;
	public FormNewPlayerPreset newPlayerPreset;
	public FormFairTypeLabel costText;
	public FormLocalTextButton styleButton;

	public CustomRaceStyleForm(final core.containers.CustomRaceStylistContainer container) {
      super("playerStyle", 500, 10);
      this.container = container;
      FormFlow flow = new FormFlow(5);

      RaceLook ra = RaceDataFactory.getRaceLook(this.container.client.playerMob, RaceLook.fromHumanLook(this.container.client.playerMob.look, CustomHumanLook.class));
      DebugHelper.handleFormattedDebugMessage("Style form opened for player %s with race %s", 50, MESSAGE_TYPE.DEBUG, new Object[] {this.container.client.playerMob.playerName, ra.getRaceID()});
      this.addComponent(new FormLocalLabel("ui", "stylistchange", new FontOptions(20), 0, this.getWidth() / 2, flow.next(25)));
      
      this.newPlayerPreset = (overrides.FormNewPlayerPreset)this.addComponent(    		  
    		(overrides.FormNewPlayerPreset)flow.nextY(new StylistCustomNewPlayerForm(5, 0, this.getWidth() - 10, true, true, RaceLook.fromRaceLook(ra)), 20)
    		  );
      
      int labelY = flow.next(28);
      this.costText = (FormFairTypeLabel)this.addComponent(new FormFairTypeLabel("", 40, labelY - 8));
      this.costText.setMaxWidth(this.getWidth() - 80);
      int buttonsY = flow.next(40);
      
      ((FormLocalTextButton)this.addComponent(new FormLocalTextButton("ui", "backbutton", this.getWidth() / 2 + 2, buttonsY, this.getWidth() / 2 - 6))).onClicked((e) -> {
         this.onBackPressed();
      });
      
      this.styleButton = (FormLocalTextButton)this.addComponent(new FormLocalTextButton("ui", "stylistbuy", 4, buttonsY, this.getWidth() / 2 - 6));
      this.styleButton.onClicked((e) -> {
    	  
         if (container.canStyle(container.getTotalStyleCost(ra, this.newPlayerPreset.getRaceLook() ))) {
            Packet content = new Packet();
            this.newPlayerPreset.getRaceLook().setupContentPacket(new PacketWriter(content), true);
            container.playerStyleButton.runAndSend(content);
         }

      });
      this.setHeight(flow.next());
      this.updateCostAndCanStyle();
   }

	protected void init() {
		super.init();
		Localization.addListener(new LocalizationChangeListener() {
			public void onChange(Language language) {
				CustomRaceStyleForm.this.updateCostAndCanStyle();
			}

			public boolean isDisposed() {
				return CustomRaceStyleForm.this.isDisposed();
			}
		});
	}

	public void updateCostAndCanStyle() {
		
		RaceLook newLook = this.newPlayerPreset.getRaceLook();
		RaceLook oldLook = RaceDataFactory.getRaceLook(this.container.client.playerMob, RaceLook.fromHumanLook(this.container.client.playerMob.look, newLook.getClass()));
		
		ArrayList<InventoryItem> cost = this.container.getTotalStyleCost(oldLook, newLook);
		this.styleButton.setActive(this.container.canStyle(cost));
		if (cost == null) {
			cost = new ArrayList<InventoryItem>(Collections.singletonList(new InventoryItem("coin", 0)));
		}

		this.costText.setCustomFairType(this.getTotalCostFairType(cost));
	}

	public FairType getTotalCostFairType(ArrayList<InventoryItem> cost) {
		FontOptions fontOptions = new FontOptions(16);
		FairType fairType = new FairType();
		fairType.append(fontOptions, Localization.translate("ui", "stylistcost"));
		Iterator<InventoryItem> it = cost.iterator();

		while (it.hasNext()) {
			InventoryItem next = (InventoryItem) it.next();
			fairType.append(new FairGlyph[]{(new FairItemGlyph(24, next)).offsetY(4)});
			fairType.append(fontOptions, "x " + next.getAmount());
			if (it.hasNext()) {
				fairType.append(fontOptions, ",");
			}
		}

		return fairType;
	}

	public void updateCanStyle() {
		
		RaceLook look = this.newPlayerPreset.getRaceLook();
		RaceLook oldLook = RaceDataFactory.getRaceLook(this.container.client.playerMob, RaceLook.fromHumanLook(this.container.client.playerMob.look, look.getClass()));
		ArrayList<InventoryItem> cost = this.container.getTotalStyleCost(oldLook, look);
		this.styleButton.setActive(this.container.canStyle(cost));
		
	}

	public void draw(TickManager tickManager, PlayerMob perspective, Rectangle renderBox) {
		this.updateCanStyle();
		super.draw(tickManager, perspective, renderBox);
	}

	public abstract void onBackPressed();
	
	public class StylistCustomNewPlayerForm extends overrides.FormNewPlayerPreset{

		public StylistCustomNewPlayerForm(int x, int y, int width, boolean allowSupernaturalChanges,
				boolean allowClothesChance, RaceLook startingRace) {
			super(x, y, width, allowSupernaturalChanges, allowClothesChance, false, startingRace);
		}
		
		@Override
		protected void setupCustomizer(int x, int y, int width, boolean allowSupernaturalChanges, boolean allowClothesChance, RaceLook startingRace) {
			
			Class<? extends FormNewPlayerRaceCustomizer> customizerType = startingRace.associatedCustomizerForm;
			
			boolean formSuccess = false;
			try {
				formSuccess=true;
				this.newPlayerFormContents = customizerType.getConstructor(int.class, int.class, int.class, boolean.class, boolean.class)
						.newInstance(RACE_SWITCH_FORM_WIDTH, y, width-(RACE_SWITCH_FORM_WIDTH*2), allowSupernaturalChanges, allowClothesChance);
				
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				
				e.printStackTrace();
				this.newPlayerFormContents = new HumanNewPlayerRaceCustomizer(x, y, width, allowSupernaturalChanges, allowClothesChance);
				
			}
			
				newPlayerFormContents.sectionListModifier = (sections, isCurrent)->{
					
		            sections.add(0, newPlayerFormContents.new Section(new FormNewPlayerRaceCustomizer.DrawButtonFunction() {
		               public void draw(FormContentVarToggleButton button, int drawX, int drawY, int width, int height) {
		                  ButtonIcon icon = Settings.UI.button_reset_20;
		                  Color color = (Color)icon.colorGetter.apply(button.getButtonState());
		                  icon.texture.initDraw().color(color).posMiddle(drawX + width / 2, drawY + height / 2).draw();
		               }
		            }, new LocalMessage("ui", "stylistreset"), (Form)null, isCurrent)  {
		               public void onClicked(FormSwitcher switcher) {
		            	   
		            	   
		                  CustomRaceStyleForm.this.newPlayerPreset.setLook(startingRace);
		                  onChanged();
		               }
		            });

		         };
		         
		         newPlayerFormContents.onChangedEvent = ()->{
		        	 CustomRaceStyleForm.this.updateCostAndCanStyle();
		         };  
		      if(formSuccess) {
		    	  this.newPlayerFormContents.setRaceLook(startingRace);
		      }
	         DebugHelper.handleFormattedDebugMessage("Limited customizer created for player %s with starting race %s",
					   50, MESSAGE_TYPE.DEBUG, new Object[] {this.newPlayerFormContents.getPlayerHelper(), startingRace.getRaceID()});
			this.addComponent(newPlayerFormContents);	
			this.setHeight(newPlayerFormContents.getHeight());
		}
		
	}
}