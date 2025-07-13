package core.forms.container;

import core.containers.CustomRaceStylistContainer;
import core.forms.CustomRaceSettlerStyleForm;
import core.forms.CustomRaceStyleForm;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.client.Client;
import necesse.engine.window.GameWindow;
import necesse.entity.mobs.friendly.human.humanShop.StylistHumanMob;
import necesse.gfx.forms.ContainerComponent;
import necesse.gfx.forms.presets.containerComponent.mob.SettlerStyleForm;
import necesse.gfx.forms.presets.containerComponent.mob.ShopContainerForm;

public class CustomRaceStylistContainerForm<T extends CustomRaceStylistContainer> extends ShopContainerForm<T> {
	public CustomRaceStyleForm playerStyleForm;
	public CustomRaceSettlerStyleForm settlerStyleForm;

	public CustomRaceStylistContainerForm(Client client, T container, int width, int height, int maxExpeditionsHeight) {
		
		super(client, container, width, height, maxExpeditionsHeight);
		this.playerStyleForm = (CustomRaceStyleForm) this.addComponent(new CustomRaceStyleForm(container) {
			public void onBackPressed() {
				CustomRaceStylistContainerForm.this.makeCurrent(CustomRaceStylistContainerForm.this.dialogueForm);
			}
		});
		
		this.settlerStyleForm = (CustomRaceSettlerStyleForm) this.addComponent(
				
			new CustomRaceSettlerStyleForm(container) {
				public void onBackPressed() {
					CustomRaceStylistContainerForm.this.makeCurrent(CustomRaceStylistContainerForm.this.dialogueForm);
					}
				}
				
				);
		
	}

	public CustomRaceStylistContainerForm(Client client, T container) {
		this(client, container, 408, 170, 240);
	}

	protected void addShopDialogueOptions() {
		
		super.addShopDialogueOptions();
		//if (((CustomRaceStylistContainer) this.container).humanShop instanceof StylistHumanMob
		//		&& ((CustomRaceStylistContainer) this.container).items != null) {
			this.dialogueForm.addDialogueOption(new LocalMessage("ui", "stylistwantchange"), () -> {
				this.makeCurrent(this.playerStyleForm);
			});
			if (((CustomRaceStylistContainer) this.container).availableSettlers != null) {
				this.dialogueForm.addDialogueOption(new LocalMessage("ui", "stylistchangesettler"), () -> {
					this.makeCurrent(this.settlerStyleForm);
				});
			}
		//}

	}

	public void onWindowResized(GameWindow window) {
		super.onWindowResized(window);
		ContainerComponent.setPosFocus(this.playerStyleForm);
		ContainerComponent.setPosFocus(this.settlerStyleForm);
	}
}