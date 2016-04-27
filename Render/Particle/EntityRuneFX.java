/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Render.Particle;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;
import Reika.ChromatiCraft.Auxiliary.Interfaces.CustomRenderFX;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.ChromatiCraft.Render.ParticleEngine;
import Reika.ChromatiCraft.Render.ParticleEngine.RenderMode;
import Reika.ChromatiCraft.Render.ParticleEngine.RenderModeFlags;
import Reika.ChromatiCraft.Render.ParticleEngine.TextureMode;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityRuneFX extends EntityFX implements CustomRenderFX {

	private boolean fade = false;

	private static final RenderMode render = new RenderMode().setFlag(RenderModeFlags.ADDITIVE, true).setFlag(RenderModeFlags.ALPHACLIP, false).setFlag(RenderModeFlags.LIGHT, false);

	public EntityRuneFX(World world, double x, double y, double z, CrystalElement e) {
		this(world, x, y, z, 0, 0, 0, e);
		particleGravity = 0.05F;
	}

	public EntityRuneFX(World world, double x, double y, double z, double vx, double vy, double vz, CrystalElement e) {
		super(world, x, y, z);
		particleMaxAge = 30;
		noClip = true;
		motionX = vx;
		motionY = vy;
		motionZ = vz;
		particleIcon = e.getGlowRune();
		particleScale = 1F;
		particleGravity = 0;
	}

	public EntityRuneFX setScale(float sc) {
		particleScale = sc;
		return this;
	}

	public EntityRuneFX setLife(int life) {
		particleMaxAge = life;
		return this;
	}

	public EntityRuneFX setFading() {
		fade = true;
		return this;
	}

	public EntityRuneFX setGravity(float f) {
		particleGravity = f;
		return this;
	}

	/*
	@Override
	public void renderParticle(Tessellator v5, float par2, float par3, float par4, float par5, float par6, float par7)
	{
		v5.draw();
		ReikaTextureHelper.bindTerrainTexture();
		BlendMode.ADDITIVEDARK.apply();
		GL11.glColor4f(1, 1, 1, 1);
		v5.startDrawingQuads();
		v5.setBrightness(this.getBrightnessForRender(0));
		super.renderParticle(v5, par2, par3, par4, par5, par6, par7);
		v5.draw();
		BlendMode.DEFAULT.apply();
		v5.startDrawingQuads();
	}
	 */

	@Override
	public void onUpdate()
	{
		if (particleGravity == 0) {
			double mx = motionX;
			double my = motionY;
			double mz = motionZ;
			super.onUpdate();
			motionX = mx;
			motionY = my;
			motionZ = mz;
		}
		else {
			super.onUpdate();
		}
	}

	@Override
	public int getBrightnessForRender(float par1)
	{
		return fade ? (int)(250-10*(0.5+0.5*Math.sin(Math.toRadians(180D*particleAge/particleMaxAge)))) : 240;
	}

	@Override
	public int getFXLayer()
	{
		return 2;
	}

	@Override
	public RenderMode getRenderMode() {
		return render;
	}

	@Override
	public TextureMode getTexture() {
		return ParticleEngine.instance.blockTex;
	}

}
