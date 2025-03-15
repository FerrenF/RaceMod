package patches.server;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import core.network.CustomPacketPlayerAppearance;
import core.race.CustomHumanLook;
import core.race.RaceLook;
import core.race.factory.RaceDataFactory;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.network.client.loading.ClientLoadingSelectCharacter;
import necesse.engine.network.packet.PacketDownloadCharacter;
import necesse.engine.network.packet.PacketSelectedCharacter;
import necesse.engine.network.server.ServerClient;
import necesse.engine.save.CharacterSave;
import necesse.engine.window.GameWindow;
import necesse.engine.window.WindowManager;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.FormResizeWrapper;
import necesse.gfx.forms.FormSwitcher;
import necesse.gfx.forms.presets.CharacterSelectForm;
import necesse.gfx.forms.presets.ConfirmationForm;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.This;
import overrides.NewCharacterForm;

public class ClientLoadingSelectCharacterStartPatch {
	
	public static FormResizeWrapper start(@This ClientLoadingSelectCharacter th) {
	    try {
	        // Access private fields using reflection
	        Field needAppearanceField = ClientLoadingSelectCharacter.class.getDeclaredField("needAppearance");
	        needAppearanceField.setAccessible(true);
	        boolean needAppearance = (boolean) needAppearanceField.get(th);

	        Field allowCharacterSelectField = ClientLoadingSelectCharacter.class.getDeclaredField("allowCharacterSelect");
	        allowCharacterSelectField.setAccessible(true);
	        boolean allowCharacterSelect = (boolean) allowCharacterSelectField.get(th);

	        if (!needAppearance && !allowCharacterSelect) {
	            Method markDoneMethod = ClientLoadingSelectCharacter.class.getDeclaredMethod("markDone");
	            markDoneMethod.setAccessible(true);
	            markDoneMethod.invoke(th);  // Call private method
	            return null;
	        } else {
	            // Access and set 'switcher'
	            Field switcherField = ClientLoadingSelectCharacter.class.getDeclaredField("switcher");
	            switcherField.setAccessible(true);
	            FormSwitcher switcher = new FormSwitcher();
	            switcherField.set(th, switcher);

	            // Access and set 'characterSelectForm'
	            Field characterSelectFormField = ClientLoadingSelectCharacter.class.getDeclaredField("characterSelectForm");
	            characterSelectFormField.setAccessible(true);
	            CharacterSelectForm characterSelectForm = new CharacterSelectForm(
	                    new LocalMessage("ui", "connectcancel"),
	                    th.client.worldSettings.allowCheats,
	                    th.client.getPermissionLevel().getLevel() >= PermissionLevel.OWNER.getLevel()) {

	                public void onSelected(File filePath, CharacterSave character) {
	                    if (!th.client.hasDisconnected()) {
	                        th.client.characterFilePath = filePath;
	                        th.client.network.sendPacket(new PacketSelectedCharacter(
	                                character.characterUniqueID, filePath == null ? null : character));
	                    }
	                }

	                public void onBackPressed() {
	                    th.cancelConnection();
	                }

	                public void onDownloadPressed(int characterUniqueID) {
	                    if (!th.client.hasDisconnected()) {
	                        th.client.network.sendPacket(new PacketDownloadCharacter(characterUniqueID));
	                    }
	                }
	            };
	            characterSelectFormField.set(th, characterSelectForm);

	            // Access and set 'createCharacterForm'
	            Field createCharacterFormField = ClientLoadingSelectCharacter.class.getDeclaredField("createCharacterForm");
	            createCharacterFormField.setAccessible(true);
	            NewCharacterForm createCharacterForm = new NewCharacterForm("newCharacter") {
	                public void onCreatePressed(PlayerMob player) {
	                    if (!th.client.hasDisconnected()) {
	                        th.client.network.sendPacket(
	                                new CustomPacketPlayerAppearance(th.client.getSlot(),
	                                        CharacterSave.getNewUniqueCharacterID((uniqueID) -> {
	                                            return !characterSelectForm.isCharacterUniqueIDOccupied(uniqueID);
	                                        }), player));
	                    }
	                }

	                public void onCancelPressed() {
	                    th.cancelConnection();
	                }
	            };
	            createCharacterFormField.set(th, createCharacterForm);

	            // Access 'serverCharacterPlayer' field
	            Field serverCharacterPlayerField = ClientLoadingSelectCharacter.class.getDeclaredField("serverCharacterPlayer");
	            serverCharacterPlayerField.setAccessible(true);
	            PlayerMob serverCharacterPlayer = (PlayerMob) serverCharacterPlayerField.get(th);

	            if (serverCharacterPlayer != null) {
	            	RaceLook apply = RaceDataFactory.getOrRegisterRaceData(serverCharacterPlayer).raceDataInitialized 
	            	? RaceDataFactory.getRaceLook(serverCharacterPlayer, RaceLook.fromHumanLook(serverCharacterPlayer.look, CustomHumanLook.class)) 
	            			:  RaceLook.fromHumanLook(serverCharacterPlayer.look, CustomHumanLook.class);
	                createCharacterForm.setLook(apply);
	            }

	            // Access and set 'lookErrorForm'
	            Field lookErrorFormField = ClientLoadingSelectCharacter.class.getDeclaredField("lookErrorForm");
	            lookErrorFormField.setAccessible(true);
	            ConfirmationForm lookErrorForm = new ConfirmationForm("lookError", 400, 120);
	            lookErrorFormField.set(th, lookErrorForm);

	            if (allowCharacterSelect) {
	                // Access 'serverCharacterUniqueID' and 'serverCharacterTimePlayed'
	                Field serverCharacterUniqueIDField = ClientLoadingSelectCharacter.class.getDeclaredField("serverCharacterUniqueID");
	                serverCharacterUniqueIDField.setAccessible(true);
	                int serverCharacterUniqueID = (int) serverCharacterUniqueIDField.get(th);

	                Field serverCharacterTimePlayedField = ClientLoadingSelectCharacter.class.getDeclaredField("serverCharacterTimePlayed");
	                serverCharacterTimePlayedField.setAccessible(true);
	                long serverCharacterTimePlayed = (long) serverCharacterTimePlayedField.get(th);

	                if (serverCharacterPlayer != null) {
	                    characterSelectForm.addExtraCharacter(serverCharacterUniqueID, serverCharacterPlayer, serverCharacterTimePlayed);
	                }

	                characterSelectForm.loadCharacters();
	                switcher.makeCurrent(characterSelectForm);
	            } else {
	                switcher.makeCurrent(createCharacterForm);
	            }

	            return new FormResizeWrapper(switcher, () -> {
	                GameWindow window = WindowManager.getWindow();
	                characterSelectForm.onWindowResized(window);
	                createCharacterForm.setPosMiddle(window.getHudWidth() / 2, window.getHudHeight() / 2);
	                lookErrorForm.setPosMiddle(window.getHudWidth() / 2, window.getHudHeight() / 2);
	            });

	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
}
