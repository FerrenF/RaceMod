package core.gfx;

import java.awt.Point;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

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


public class TestFurryDrawOptions implements DrawOptions {
	
	private PlayerMob player;
	private final Level level;
	private TestFurryRaceLook look;
	
	private GameTexture tailTexture;
	private GameTexture earsTexture;
	private GameTexture muzzleTexture;
	
	private boolean drawMuzzle;
	private boolean drawTail;
	private boolean drawEars;
	
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
	
	public TestFurryDrawOptions(Level level) {
		this.drawEars = false;
		this.drawMuzzle = false;
		this.drawTail = false;
		
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
	
	public TestFurryDrawOptions size(Point p) {
		this.width = p.x;
		this.height = p.y;
		return this;
	}
	
	public TestFurryDrawOptions(Level level, TestFurryRaceLook look) {
		this(level);
		this.look = look;		
	}
	
	public TestFurryDrawOptions(Level level, PlayerMob player) {
		this(level);
		this.player = player;
	}
			
	public TestFurryDrawOptions(Level level, PlayerMob player, int dir, int spriteX, int spriteY, int spriteRes, int drawX,
			int drawY, int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha,
			MaskShaderOptions mask) {		
			this(level, player);
	}

	public TestFurryDrawOptions tailTexture(TestFurryRaceLook _look, int spriteX, int spriteY, boolean resize) {
		this.tailTexture = resize ? _look.getTailTexture(spriteX, spriteY, width, height) : _look.getTailTexture(spriteX, spriteY);
		return this;
	}
	
	public TestFurryDrawOptions earsTexture(TestFurryRaceLook _look, int spriteX, int spriteY, boolean resize) {
		this.earsTexture = resize ? _look.getEarsTexture(spriteX, spriteY, width, height) : _look.getEarsTexture(spriteX, spriteY);
		return this;
	}
	
	public TestFurryDrawOptions muzzleTexture(TestFurryRaceLook _look, int spriteX, int spriteY, boolean resize) {
		this.muzzleTexture = resize ? _look.getMuzzleTexture(spriteX, spriteY, width, height) : _look.getMuzzleTexture(spriteX, spriteY);
		return this;
	}
	
	public TestFurryDrawOptions drawMuzzle(boolean drawMuzzle) {
		this.drawMuzzle = drawMuzzle;
		return this;
	}
	public TestFurryDrawOptions drawEars(boolean drawEars) {
		this.drawEars = drawEars;
		return this;
	}
	public TestFurryDrawOptions drawTail(boolean drawTail) {
		this.drawTail = drawTail;
		return this;
	}

	public TestFurryDrawOptions sprite(Point sprite, int spriteRes) {
		return this.sprite(sprite.x, sprite.y, spriteRes);
	}

	private TestFurryDrawOptions sprite(int x, int y, int spriteRes) {
		this.spriteRes = spriteRes;
		this.sprite = new Point(x,y);
		return this;
	}

	public TestFurryDrawOptions sprite(Point sprite) {
		return this.sprite(sprite.x, sprite.y);
	}

	private TestFurryDrawOptions sprite(int x, int y) {

		return this.sprite(x, y);
	}

	public TestFurryDrawOptions dir(int dir) {
		this.dir = dir;
		return this;
	}
	
	public static boolean isSpriteXOffset(int spriteX) {
		return spriteX == 1 || spriteX == 3;
	}
	
	public TestFurryDrawOptions invis(boolean invis) {
		this.invis = invis;
		return this;
	}
	
	public TestFurryDrawOptions player(PlayerMob player) {
		this.player = player;
		return this;
	}
	
	public TestFurryDrawOptions addDrawOffset(int x, int y) {
		this.drawOffsetX = x;
		this.drawOffsetY = y;
		return this;
	}

	public TestFurryDrawOptions drawOffset(int x, int y) {
		this.drawOffsetX += x;
		this.drawOffsetY += y;
		return this;
	}
	public TestFurryDrawOptions mirrorX(boolean mirror) {
		this.mirrorX = mirror;
		return this;
	}

	public TestFurryDrawOptions mirrorY(boolean mirror) {
		this.mirrorY = mirror;
		return this;
	}

	public TestFurryDrawOptions alpha(float alpha) {
		this.alpha = alpha;
		return this;
	}

	public TestFurryDrawOptions allAlpha(float alpha) {
		this.allAlpha = alpha;
		return this;
	}

	public TestFurryDrawOptions rotate(float angle, int midX, int midY) {
		this.rotation = angle;
		this.rotationX = midX;
		this.rotationY = midY;
		return this;
	}

	public TestFurryDrawOptions light(GameLight light) {
		this.light = light;
		return this;
	}
	

	@Override
	public void draw() {
	    if (this.drawEars) {
	        this.earsTexture.initDraw()
	            .pos(drawX + drawOffsetX, drawY + drawOffsetY)
	            .size(width, height)
	            .alpha(this.allAlpha)
	            .light(this.light)
	            .addMaskShader(mask)
	            .draw();
	    }

	    if (this.drawMuzzle) {
	        this.muzzleTexture.initDraw()
	            .pos(drawX + drawOffsetX, drawY + drawOffsetY)
	            .size(width, height)
	            .alpha(this.allAlpha)
	            .light(this.light)
	            .addMaskShader(mask)
	            .draw();
	    }

	    if (this.drawTail) {
	        this.tailTexture.initDraw()
	            .pos(drawX + drawOffsetX, drawY + drawOffsetY)
	            .size(width, height)
	            .alpha(this.allAlpha)
	            .light(this.light)
	            .addMaskShader(mask)
	            .draw();
	    }
	}
	

	@FunctionalInterface
	public interface FurryDrawOptionsGetter extends HumanDrawOptions.HumanDrawOptionsGetter {
		DrawOptions getDrawOptions(PlayerMob var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8,
				int var9, boolean var10, boolean var11, GameLight var12, float var13, MaskShaderOptions var14);
	}


	public TestFurryDrawOptions pos(int drawX, int drawY) {
		this.drawX = drawX;
		this.drawY = drawY;
		return this;
	}

	public TestFurryDrawOptions size(int i, int j) {
		this.width = i;
		this.height = j;
		return this;
	}

	public DrawOptions getTopDrawOptions() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public TestFurryDrawOptions mask(MaskShaderOptions mask) {
		this.mask = mask;
		return this;
	}

	public TestFurryDrawOptions mask(GameTexture mask, int xOffset, int yOffset) {
		this.mask = new MaskShaderOptions(mask, 0, 0, xOffset, yOffset);
		return this;
	}

	public TestFurryDrawOptions mask(GameTexture mask) {
		return this.mask(mask, 0, 0);
	}

	public TestFurryDrawOptions spriteRes(int spriteRes) {
		this.spriteRes = spriteRes;
		return this;
	}

}