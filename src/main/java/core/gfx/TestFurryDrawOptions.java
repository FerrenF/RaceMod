package core.gfx;

import java.awt.Point;
import java.util.LinkedList;

import core.RaceMod;
import core.race.TestFurryRaceLook;
import extensions.RaceLook;
import necesse.entity.mobs.MaskShaderOptions;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.HumanLook;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.gameTexture.GameTexture;
import necesse.inventory.item.armorItem.ArmorItem;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import overrides.CustomPlayerMob;

public class TestFurryDrawOptions implements DrawOptions {
	
	private CustomPlayerMob player;
	private final Level level;
	private RaceLook look;
	
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
	
	public static RaceLook getRaceLook(CustomPlayerMob _player) {	
		
	    if ("CUSTOM".equals(_player.secondType)) {  // Safe string comparison
	        if (_player.look instanceof RaceLook) {
	            return (RaceLook) _player.look;
	        } else {
	            System.err.println("Error: newPlayer.look is not an instance of RaceLook!");
	            return null;  // Handle invalid state gracefully
	        }
	    }
	    
	    RaceLook converted = racelookFromBase(_player.look);
	    if (converted == null) {
	        System.err.println("Error: Failed to convert HumanLook to RaceLook!");
	    }
	    return converted;
	}

	public static TestFurryRaceLook getCustomRaceLook(RaceLook _look) {	
		if (_look.getRaceID() != TestFurryRaceLook.TEST_FURRY_RACE_ID) {
			RaceMod.handleDebugMessage(String.format("Draw options for raceID %s requested for non-raceID %s from %.", TestFurryRaceLook.TEST_FURRY_RACE_ID, _look.getRaceID(), _look.getClass().getName()), 25);
			return null;
		}
		return (TestFurryRaceLook)_look;
	}
	public static RaceLook racelookFromBase(HumanLook look) {	return new TestFurryRaceLook(look);}

	public TestFurryDrawOptions(Level level) {
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
	public TestFurryDrawOptions(Level level, RaceLook look) {
		this(level);
		this.look = look;
		
	}
	public TestFurryDrawOptions(Level level, CustomPlayerMob player) {
		this(level);
		this.player = player;
		this.look = getCustomRaceLook(getRaceLook(player));
	}
			
	public TestFurryDrawOptions(Level level, CustomPlayerMob player, int dir, int spriteX, int spriteY, int spriteRes, int drawX,
			int drawY, int width, int height, boolean mirrorX, boolean mirrorY, GameLight light, float alpha,
			MaskShaderOptions mask) {		
			this(level, player);
	}

	public TestFurryDrawOptions(Level level2, PlayerMob player2) {
		this(level2, (CustomPlayerMob)player2);
	}

	public TestFurryDrawOptions tailTexture(GameTexture tailTexture) {
		this.tailTexture = tailTexture;
		return this;
	}
	
	public TestFurryDrawOptions earsTexture(GameTexture earsTexture) {
		this.earsTexture = earsTexture;
		return this;
	}
	
	public TestFurryDrawOptions muzzleTexture(GameTexture muzzleTexture) {
		this.muzzleTexture = muzzleTexture;
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
		// TODO Auto-generated method stub
		return null;
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
	
	public TestFurryDrawOptions player(CustomPlayerMob player) {
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
		
		if(this.drawEars) {
			this.earsTexture.initDraw().pos(drawX+drawOffsetX, drawY+drawOffsetY).size(width, height).alpha(this.allAlpha).light(this.light).draw();
		}
		if(this.drawMuzzle) {
			this.muzzleTexture.initDraw().pos(drawX+drawOffsetX, drawY+drawOffsetY).size(width, height).alpha(this.allAlpha).light(this.light).draw();
		}
		if(this.drawTail) {
			this.tailTexture.initDraw().pos(drawX+drawOffsetX, drawY+drawOffsetY).size(width, height).alpha(this.allAlpha).light(this.light).draw();
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
	
}