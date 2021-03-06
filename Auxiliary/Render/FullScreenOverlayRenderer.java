/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Auxiliary.Render;

import java.util.EnumMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

import Reika.ChromatiCraft.ChromatiCraft;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.ChromatiCraft.World.PylonGenerator;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.IO.ReikaColorAPI;
import Reika.DragonAPI.Libraries.IO.ReikaRenderHelper;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;


public class FullScreenOverlayRenderer {

	static final FullScreenOverlayRenderer instance = new FullScreenOverlayRenderer();

	private static final int WASHOUT_LENGTH = 312;
	private static final int WASHOUT_FACTOR = 2;
	private static final int FLASH_FADE = 4;
	private boolean rehideGui;
	private int washout;
	private CrystalElement washoutColor;

	private final EnumMap<CrystalElement, Float> factors = new EnumMap(CrystalElement.class);

	private FullScreenOverlayRenderer() {

	}

	void triggerWashout(CrystalElement e) {
		rehideGui = Minecraft.getMinecraft().gameSettings.hideGUI;
		Minecraft.getMinecraft().gameSettings.hideGUI = false;
		washout = Math.max(washout, WASHOUT_LENGTH-FLASH_FADE);
		washoutColor = e;
	}

	boolean isWashoutActive() {
		return washout > 0;
	}

	void renderWashout(RenderGameOverlayEvent.Pre evt, int tick) {
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		BlendMode.DEFAULT.apply();

		int mx = Minecraft.getMinecraft().displayWidth/evt.resolution.getScaleFactor();
		int my = Minecraft.getMinecraft().displayHeight/evt.resolution.getScaleFactor();

		Tessellator v5 = Tessellator.instance;
		v5.startDrawingQuads();
		v5.setBrightness(240);
		int a = (int)(washout > WASHOUT_LENGTH-FLASH_FADE ? (255F*(WASHOUT_LENGTH-washout)/FLASH_FADE) : 255F*Math.min(1, washout/255F));
		int c1 = ReikaColorAPI.mixColors(washoutColor.getColor(), 0xffffff, 0.5F);
		int c = ReikaColorAPI.mixColors(0xffffff, c1, Math.min(0.95F, a/255F));
		//ReikaJavaLibrary.pConsole(washout+" > A="+a+", cfrac = "+(a/255F)+", C="+Integer.toHexString(c));
		v5.setColorRGBA_I(c, Math.min(255, a));
		v5.addVertex(0, 0, 0);
		v5.addVertex(mx, 0, 0);
		v5.addVertex(mx, my, 0);
		v5.addVertex(0, my, 0);
		v5.draw();

		if (!Minecraft.getMinecraft().isGamePaused()) {
			if (washout >= WASHOUT_LENGTH-FLASH_FADE || tick%WASHOUT_FACTOR == 0)
				washout -= Math.max(1, 90/Math.max(1, ReikaRenderHelper.getFPS()));
			if (washout == 0) {
				if (rehideGui) {
					Minecraft.getMinecraft().gameSettings.hideGUI = true;
				}
			}
		}

		GL11.glPopAttrib();
	}

	void triggerPylonEffect(CrystalElement e) {
		factors.put(e, 2F);
	}

	void renderPylonAura(EntityPlayer ep, int gsc) {
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		String tex = "Textures/aura-bar-half-grid.png";//ChromaOptions.SMALLAURA.getState() ? "Textures/aura-bar-quarter.png" : "Textures/aura-bar-half.png";
		ReikaTextureHelper.bindTexture(ChromatiCraft.class, tex);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		BlendMode.ADDITIVEDARK.apply();
		GL11.glAlphaFunc(GL11.GL_GREATER, 1/255F);
		Tessellator v5 = Tessellator.instance;
		double w = Minecraft.getMinecraft().displayWidth/gsc;
		double h = Minecraft.getMinecraft().displayHeight/gsc;
		double z = -1000;
		for (int i = 0; i < 16; i++) {
			CrystalElement e = CrystalElement.elements[i];
			Coordinate c = PylonGenerator.instance.getNearestPylonSpawn(ep.worldObj, ep.posX, ep.posY, ep.posZ, e);
			double dd = c != null ? c.getDistanceTo(ep.posX, ep.posY, ep.posZ) : Double.POSITIVE_INFINITY;
			if (dd < 32) {
				int step = 40;
				int frame = (int)((System.currentTimeMillis()/step)%20+e.ordinal()*1.25F)%20;
				int imgw = 4;//20;
				int imgh = 5;//1;
				double u = frame%imgw/(double)imgw;
				double du = u+1D/imgw;
				double v = frame/imgw/(double)imgh;
				double dv = v+1D/imgh;
				int alpha = 255;
				float cache = factors.containsKey(e) ? factors.get(e) : 0;
				float bright = Math.min(1, (float)(1.5-dd/24));
				float res = Math.max(cache, bright);
				factors.put(e, cache*0.9975F);
				if (res > 0) {
					int color = ReikaColorAPI.getColorWithBrightnessMultiplier(e.getColor(), Math.min(1, res));
					v5.startDrawingQuads();
					v5.setBrightness(240);
					v5.setColorRGBA_I(color, alpha);
					v5.addVertexWithUV(0, h, z, u, dv);
					v5.addVertexWithUV(w, h, z, du, dv);
					v5.addVertexWithUV(w, 0, z, du, v);
					v5.addVertexWithUV(0, 0, z, u, v);
					v5.draw();
				}
			}
		}
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		BlendMode.DEFAULT.apply();
		//GL11.glDisable(GL11.GL_DEPTH_TEST); //turn off depth testing to avoid this occluding other elements
		GL11.glPopAttrib();
	}

}
