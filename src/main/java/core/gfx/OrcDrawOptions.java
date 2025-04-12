package core.gfx;

import java.awt.Point;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import core.race.OrcRaceLook;
import core.race.RaceLook;
import core.race.TestFurryRaceLook;
import core.race.factory.RaceDataFactory;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;


public class OrcDrawOptions implements DrawOptions {
	
	private PlayerMob player;
	private final Level level;
	private OrcRaceLook look;
	
	private GameTexture facialFeaturesTexture;	
	private boolean drawFacialFeatures;
	
	private int dir;
	private boolean invis;
	private GameLight light;
	private int rotationY;
	private int rotationX;
	private float rotation;
	private float allAlpha;
	private float alpha;
	private boolean mirrorY;
	private boolean mirrorX;
	private int drawOffsetY;
	private int drawOffsetX;
	private int width;
	private int height;
	private int spriteRes;
	private Point sprite;
	private int drawX;
	private int drawY;
	
	private MaskShaderOptions mask;
	
	public OrcDrawOptions(Level level) {
		this.drawFacialFeatures = false;	
		this.mirrorX = false;
		this.mirrorY = false;
		this.invis = false;
		this.level = level;
		this.drawOffsetX=0;
		this.drawOffsetY=0;
		this.light = new GameLight(150.0F);
		this.alpha = 1.0F;
		this.allAlpha = 1.0F;
		this.width = 64;
		this.height = 64;
		this.spriteRes = 64;		
	}	
	
	public OrcDrawOptions size(Point p) {
		this.width = p.x;
		this.height = p.y;
		return this;
	}
	
	public OrcDrawOptions(Level level, OrcRaceLook look) {
		this(level);
		this.look = look;		
	}
	
	public OrcDrawOptions(Level level, PlayerMob player) {
		this(level);
		this.player = player;
	}
			
	public OrcDrawOptions(Level level, PlayerMob player, int dir, int spriteX, int spriteY, int spriteRes, int drawX,
			int drawY, int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha,
			MaskShaderOptions mask) {		
			this(level, player);
	}

	public OrcDrawOptions facialFeaturesTexture(OrcRaceLook _look, int spriteX, int spriteY, boolean resize) {
		this.facialFeaturesTexture = resize 
				? _look.getFacialFeaturesTexture(spriteX, spriteY, width, height) 
				: _look.getFacialFeaturesTexture(spriteX, spriteY);
		return this;
	}
	
	public OrcDrawOptions drawFacialFeatures(boolean drawFacialFeatures) {
		this.drawFacialFeatures = drawFacialFeatures;
		return this;
	}

	public OrcDrawOptions sprite(Point sprite, int spriteRes) {
		return this.sprite(sprite.x, sprite.y, spriteRes);
	}

	private OrcDrawOptions sprite(int x, int y, int spriteRes) {
		this.spriteRes = spriteRes;
		this.sprite = new Point(x,y);
		return this;
	}

	public OrcDrawOptions sprite(Point sprite) {
		return this.sprite(sprite.x, sprite.y);
	}

	private OrcDrawOptions sprite(int x, int y) {
		return this.sprite(x, y);
	}

	public OrcDrawOptions dir(int dir) {
		this.dir = dir;
		return this;
	}
	
	public static boolean isSpriteXOffset(int spriteX) {
		return spriteX == 1 || spriteX == 3;
	}
	
	public OrcDrawOptions invis(boolean invis) {
		this.invis = invis;
		return this;
	}
	
	public OrcDrawOptions player(PlayerMob player) {
		this.player = player;
		return this;
	}
	
	public OrcDrawOptions addDrawOffset(int x, int y) {
		this.drawOffsetX = x;
		this.drawOffsetY = y;
		return this;
	}

	public OrcDrawOptions drawOffset(int x, int y) {
		this.drawOffsetX += x;
		this.drawOffsetY += y;
		return this;
	}
	public OrcDrawOptions mirrorX(boolean mirror) {
		this.mirrorX = mirror;
		return this;
	}

	public OrcDrawOptions mirrorY(boolean mirror) {
		this.mirrorY = mirror;
		return this;
	}

	public OrcDrawOptions alpha(float alpha) {
		this.alpha = alpha;
		return this;
	}

	public OrcDrawOptions allAlpha(float alpha) {
		this.allAlpha = alpha;
		return this;
	}

	public OrcDrawOptions rotate(float angle, int midX, int midY) {
		this.rotation = angle;
		this.rotationX = midX;
		this.rotationY = midY;
		return this;
	}

	public OrcDrawOptions light(GameLight light) {
		this.light = light;
		return this;
	}
	

	@Override
	public void draw() {		
	    
	    if (this.drawFacialFeatures) {
	        this.facialFeaturesTexture.initDraw()
	            .pos(drawX + drawOffsetX, drawY + drawOffsetY)
	            .size(width, height)
	            .mirror(this.mirrorX, this.mirrorY)
	            .alpha(this.invis ? 0 : this.allAlpha)
	            .light(this.light)
	            .addMaskShader(mask)
	            .draw();
	    }
	}
	
	@FunctionalInterface
	public interface OrcDrawOptionsGetter extends HumanDrawOptions.HumanDrawOptionsGetter {
		DrawOptions getDrawOptions(PlayerMob var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8,
				int var9, boolean var10, boolean var11, GameLight var12, float var13, MaskShaderOptions var14);
	}

	public OrcDrawOptions pos(int drawX, int drawY) {
		this.drawX = drawX;
		this.drawY = drawY;
		return this;
	}

	public OrcDrawOptions size(int i, int j) {
		this.width = i;
		this.height = j;
		return this;
	}

	public OrcDrawOptions mask(MaskShaderOptions mask) {
		this.mask = mask;
		return this;
	}

	public OrcDrawOptions mask(GameTexture mask, int xOffset, int yOffset) {
		this.mask = new MaskShaderOptions(mask, 0, 0, xOffset, yOffset);
		return this;
	}

	public OrcDrawOptions mask(GameTexture mask) {
		return this.mask(mask, 0, 0);
	}

	public OrcDrawOptions spriteRes(int spriteRes) {
		this.spriteRes = spriteRes;
		return this;
	}

}