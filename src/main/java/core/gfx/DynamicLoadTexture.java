package core.gfx;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawOptions.texture.TextureDrawOptionsStart;
import necesse.gfx.gameTexture.GameTexture;
import necesse.gfx.ui.GameTextureData;

public class DynamicLoadTexture{

	private static GameTexture BLANK_TEXTURE;
	private GameTexture texture;
	
	public DynamicLoadTexture(Integer textureId) {

	}
	
	public GameTexture get() {
	
		return BLANK_TEXTURE;
		
	}

}
