/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.World;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.common.util.ForgeDirection;
import Reika.ChromatiCraft.ChromatiCraft;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Instantiable.LobulatedCurve;
import Reika.DragonAPI.Instantiable.SimplexNoiseGenerator;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.Immutable.BlockKey;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Instantiable.Worldgen.ControllableOreVein.ExposedOreVein;
import Reika.DragonAPI.Interfaces.RetroactiveGenerator;
import Reika.DragonAPI.Interfaces.Registry.OreType;
import Reika.DragonAPI.Interfaces.Registry.TreeType;
import Reika.DragonAPI.Libraries.ReikaDirectionHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaOreHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaTreeHelper;
import Reika.DragonAPI.Libraries.World.ReikaChunkHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.MystCraftHandler;
import Reika.DragonAPI.ModRegistry.ModOreList;
import Reika.DragonAPI.ModRegistry.ModWoodList;
import cpw.mods.fml.common.Loader;

public class GlowingCliffsAuxGenerator implements RetroactiveGenerator {

	public static final GlowingCliffsAuxGenerator instance = new GlowingCliffsAuxGenerator();

	private static final int MAX_CONTOUR_HEIGHT = 6;
	private static final int SCALE_RANGE = 2;

	private static final BlockKey STONE_BLOCK = Loader.isModLoaded("HarderUnderground") ? new BlockKey(Blocks.stone, 1) : new BlockKey(Blocks.stone);

	private SimplexNoiseGenerator contours;
	private SimplexNoiseGenerator islandFrequency;
	private long noiseSeed;

	private final WeightedRandom<TreeType> treeRand = new WeightedRandom();
	private final WeightedRandom<OreType> oreRand = new WeightedRandom();

	private GlowingCliffsAuxGenerator() {

	}

	public void initialize() {
		treeRand.addEntry(ReikaTreeHelper.OAK, 100);
		treeRand.addEntry(ReikaTreeHelper.BIRCH, 20);

		if (ModWoodList.GREATWOOD.exists())
			treeRand.addEntry(ModWoodList.GREATWOOD, 10);
		if (ModWoodList.SILVERWOOD.exists())
			treeRand.addEntry(ModWoodList.SILVERWOOD, 4);

		if (ModWoodList.LOFTWOOD.exists())
			treeRand.addEntry(ModWoodList.LOFTWOOD, 8);

		if (ModWoodList.SAKURA.exists())
			treeRand.addEntry(ModWoodList.SAKURA, 10);

		oreRand.addEntry(ReikaOreHelper.DIAMOND, 20);
		oreRand.addEntry(ReikaOreHelper.EMERALD, 10);
		oreRand.addEntry(ReikaOreHelper.REDSTONE, 40);
		oreRand.addEntry(ReikaOreHelper.GOLD, 30);

		if (ModOreList.AMBER.existsInGame())
			oreRand.addEntry(ModOreList.AMBER, 30);
		if (ModOreList.AMETHYST.existsInGame())
			oreRand.addEntry(ModOreList.AMETHYST, 10);
		if (ModOreList.IRIDIUM.existsInGame())
			oreRand.addEntry(ModOreList.IRIDIUM, 5);
		if (ModOreList.MOONSTONE.existsInGame())
			oreRand.addEntry(ModOreList.MOONSTONE, 5);
		if (ModOreList.PLATINUM.existsInGame())
			oreRand.addEntry(ModOreList.PLATINUM, 10);
		if (ModOreList.RUBY.existsInGame())
			oreRand.addEntry(ModOreList.RUBY, 20);
		if (ModOreList.SAPPHIRE.existsInGame())
			oreRand.addEntry(ModOreList.SAPPHIRE, 20);
		if (ModOreList.PERIDOT.existsInGame())
			oreRand.addEntry(ModOreList.PERIDOT, 20);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {

		long seed = world.getSeed();
		if (contours == null || noiseSeed != seed) {
			contours = new SimplexNoiseGenerator(seed >> 1).setFrequency(1/16D).addOctave(2, 0.5, 1000);
			islandFrequency = new SimplexNoiseGenerator(seed << 1).setFrequency(1/32D);
			noiseSeed = seed;
			treeRand.setSeed(seed);
		}

		chunkX *= 16;
		chunkZ *= 16;

		this.generateMaterialVeins(world, chunkX, chunkZ, random);
		this.generateIslands(world, chunkX, chunkZ, random);
	}

	private void generateMaterialVeins(World world, int chunkX, int chunkZ, Random random) {
		if (ReikaChunkHelper.chunkContainsBiomeTypeBlockCoords(world, chunkX, chunkZ, BiomeGlowingCliffs.class)) {
			int n = 1+random.nextInt(6);
			for (int i = 0; i < n; i++) {
				int x = chunkX+random.nextInt(16)+8;
				int z = chunkZ+random.nextInt(16)+8;
				if (BiomeGlowingCliffs.isGlowingCliffs(world.getBiomeGenForCoords(x, z))) {
					int y = 64+random.nextInt(192);
					if (ModList.MYSTCRAFT.isLoaded()) {
						Block b = MystCraftHandler.getInstance().crystalID;
						if (b != null) {
							int s = 32+random.nextInt(32+1);
							new ExposedOreVein(b, s).generate(world, random, x, y, z);
						}
					}
				}
			}
		}
	}

	private void generateIslands(World world, int chunkX, int chunkZ, Random random) {
		double s = ReikaMathLibrary.normalizeToBounds(islandFrequency.getValue(chunkX, chunkZ), 1D/SCALE_RANGE, SCALE_RANGE);
		if (ReikaRandomHelper.doWithChance(33/s)) {
			int x = chunkX+random.nextInt(16)+8;
			int z = chunkZ+random.nextInt(16)+8;
			int c = world.getBlock(x, world.getTopSolidOrLiquidBlock(x, z)+1, z) == Blocks.water ? 3 : 6;
			if (random.nextInt(c) == 0) {
				if (BiomeGlowingCliffs.isGlowingCliffs(world.getBiomeGenForCoords(x, z))) {
					Island is = this.initializeIsland(world, x, z, random, s);
					this.generateIsland(world, x, z, random, is);
				}
			}
		}
	}

	private Island initializeIsland(World world, int x, int z, Random rand, double sizeScale) {
		Island is = new Island(x, z);
		is.degree = 3+rand.nextInt(4);
		is.scale = sizeScale;
		is.innerRadius = (8+4*rand.nextDouble())*sizeScale;
		is.outerRadius = (12+6*rand.nextDouble())*sizeScale;
		is.maxThickness = (18+12*rand.nextDouble())*sizeScale;
		is.originX = x;
		is.originZ = z;
		is.originY = Math.max(world.getTopSolidOrLiquidBlock(x, z)+30, 90)+rand.nextInt(80);
		is.hasLake = rand.nextInt(2) == 0;
		is.hasRiver = rand.nextInt(5) == 0;
		if (rand.nextInt(20) == 0) {
			is.hasLake = is.hasRiver = true;
		}
		if (is.hasLake) {
			is.lakeDepth = 2+rand.nextInt(5);
			is.lakeScale = 0.25+rand.nextDouble()*0.625;
		}
		if (is.hasRiver) {
			is.allowableChildren = rand.nextInt(4) > 0 ? 0 : rand.nextInt(8) == 0 ? 2 : 1;
		}
		/*
		int nt = 0;//rand.nextInt(8) > 0 ? 0 : ReikarandHelper.getInverseLinearrand(4);
		for (int i = 0; i < nt; i++) {
			TreeType tree = treeRand.getRandomEntry();
			int dx = x-(int)is.outerRadius+rand.nextInt((int)(is.outerRadius*2+1));
			int dz = z-(int)is.outerRadius+rand.nextInt((int)(is.outerRadius*2+1));
			is.trees.put(new Coordinate(dx, 0, dz), tree);
		}
		 */
		return is;
	}

	private boolean generateIsland(World world, int x, int z, Random rand, Island is) {
		is.calculate(world, x, z, rand);
		if (is.canGenerate(world)) {
			is.generate(world);
			BiomeGenBase b = ChromatiCraft.glowingcliffs;
			((GlowingCliffsDecorator)b.theBiomeDecorator).genIslandDecorations(world, b, is);
			int n = 2+rand.nextInt(7);
			for (int i = 0; i < n; i++) {
				OreType ore = oreRand.getRandomEntry();
				BlockKey bk = new BlockKey(ore.getFirstOreBlock());
				int dx = is.getRandomX(rand);
				int dy = is.getRandomY(rand);
				int dz = is.getRandomZ(rand);
				int size = 12+rand.nextInt(20);
				new WorldGenMinable(bk.blockID, bk.metadata, size, Blocks.stone).generate(world, rand, dx, dy, dz);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canGenerateAt(Random rand, World world, int chunkX, int chunkZ) {
		return true;
	}

	@Override
	public String getIDString() {
		return "Glowing Cliffs Floating Islands";
	}

	static class Island {

		private final HashMap<Coordinate, BlockKey> blocks = new HashMap();
		private final HashMap<Coordinate, Integer> topMap = new HashMap();

		private int originX;
		private int originY;
		private int originZ;

		private double scale = 1;

		private double innerRadius = 10;
		private double outerRadius = 18;

		private double minThickness = 2;
		private double maxThickness = 24;

		private int degree = 6;

		private boolean hasLake = false;
		private boolean hasRiver = false;

		private double lakeDepth = 5;
		private double lakeScale = 0.5;

		private int allowableChildren = 0;

		private Island(int x, int z) {
			originX = x;
			originZ = z;
		}

		private void calculate(World world, int x, int z, Random rand) {
			double da = 1;
			LobulatedCurve lb = LobulatedCurve.fromMinMaxRadii(innerRadius, outerRadius, degree);
			lb.generate(rand);
			for (double d = 0; d < 360; d += da) {
				double r = lb.getRadius(d);
				double cos = Math.cos(Math.toRadians(d));
				double sin = Math.sin(Math.toRadians(d));
				for (double dr = 0; dr <= r; dr += 0.5) {
					double ax = x+dr*cos;
					double az = z+dr*sin;
					int dx = MathHelper.floor_double(ax);
					int dz = MathHelper.floor_double(az);
					double t = ReikaMathLibrary.linterpolate(r-dr, 0, outerRadius, minThickness, maxThickness);
					double cont = Math.abs(instance.contours.getValue(dx, dz));
					int oy = (int)(cont*MAX_CONTOUR_HEIGHT);
					double lr = r*lakeScale;
					if (hasLake && dr <= lr) {
						double ld = /*Math.min(t-4, */2*lakeDepth*((lr-dr)/r/lakeScale)/*)*/;
						oy = (int)Math.max(0, oy-ld);
					}
					int ty = originY+oy;
					for (int i = 0; i <= t; i++) {
						Coordinate c = new Coordinate(dx, ty-i, dz);
						BlockKey bk = STONE_BLOCK;
						double dt = ReikaMathLibrary.linterpolate(dr, 0, outerRadius, 0.5, 3);
						if (i == 0) {
							bk = new BlockKey(ChromatiCraft.glowingcliffs.topBlock);
						}
						else if (i < dt) {
							bk = new BlockKey(ChromatiCraft.glowingcliffs.fillerBlock);
						}
						if (bk.blockID != Blocks.stone || !blocks.containsKey(c))
							blocks.put(c, bk);
					}
					if ((hasRiver || (hasLake && dr < lr)) && cont < 0.125) {
						BlockKey bk = new BlockKey(Blocks.flowing_water);
						blocks.put(new Coordinate(dx, ty, dz), bk);
						blocks.put(new Coordinate(dx, ty-1, dz), new BlockKey(Blocks.grass));
						if (cont < 0.0625) {
							blocks.put(new Coordinate(dx, ty-1, dz), bk);
							blocks.put(new Coordinate(dx, ty-2, dz), new BlockKey(Blocks.grass));
						}
						if (dr >= r-1) {
							if (allowableChildren > 0) {
								Island is = instance.initializeIsland(world, dx, dz, rand, scale);
								is.originY = originY-(int)maxThickness-10-rand.nextInt(30);
								is.hasLake = true;
								is.lakeDepth = 2+rand.nextInt(5);
								is.lakeScale = 0.375+rand.nextDouble()*0.5;
								is.hasRiver = false;
								is.innerRadius = 6+2*rand.nextDouble();
								is.outerRadius = 10+2*rand.nextDouble();
								is.maxThickness = 12+6*rand.nextDouble();
								if (instance.generateIsland(world, dx, dz, rand, is))
									allowableChildren--;
							}
						}
					}
					topMap.put(new Coordinate(dx, 0, dz), ty);
				}
			}
		}

		private boolean canGenerate(World world) {
			for (Coordinate c : blocks.keySet())
				if (!c.getBlock(world).isAir(world, c.xCoord, c.yCoord, c.zCoord))
					return false;
			return true;
		}

		private void generate(World world) {
			for (Coordinate c : blocks.keySet()) {
				BlockKey bk = blocks.get(c);
				boolean water = bk.blockID == Blocks.flowing_water;
				if (water) {
					ForgeDirection dir = this.getEdgeSide(c);
					if (dir != null) {
						dir = ReikaDirectionHelper.getLeftBy90(dir);
						Coordinate c2 = c.offset(dir, 1);
						Coordinate c3 = c.offset(dir.getOpposite(), 1);
						BlockKey e1 = blocks.get(c2);
						BlockKey e2 = blocks.get(c3);
						if (e1 == null || e2 == null || (e1.blockID == Blocks.flowing_water && this.getEdgeSide(c2) != null) || (e2.blockID == Blocks.flowing_water && this.getEdgeSide(c3) != null)) {
							bk = new BlockKey(blocks.containsKey(c.offset(0, 1, 0)) ? Blocks.dirt : Blocks.grass);
						}
					}
				}
				bk.place(world, c.xCoord, c.yCoord, c.zCoord);
				if (water)
					c.triggerBlockUpdate(world, false);
			}
			/*
			for (Coordinate c : trees.keySet()) {
				TreeType tree = trees.get(c);
				BlockKey log = new BlockKey(tree.getLogID(), tree.getLogMetadatas().get(0));
				BlockKey leaf = new BlockKey(tree.getBasicLeaf());
				Integer yt = topMap.get(new Coordinate(c.xCoord, 0, c.zCoord));
				if (yt != null) {
					for (int i = 1; i < 5; i++) {
						log.place(world, c.xCoord, yt+i, c.zCoord);
					}
					leaf.place(world, c.xCoord, yt+5, c.zCoord);
				}
			}
			 */
		}

		private ForgeDirection getEdgeSide(Coordinate c) {
			for (int i = 2; i < 6; i++) {
				ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
				Coordinate c2 = c.offset(dir, 1);
				if (!blocks.containsKey(c2))
					return dir;
			}
			return null;
		}

		public int getRandomX(Random rand) {
			int minX = MathHelper.floor_double(originX+0.5-outerRadius);
			int maxX = MathHelper.floor_double(originX+0.5+outerRadius);
			return minX+rand.nextInt(maxX-minX+1);
		}

		public int getRandomY(Random rand) {
			int maxY = originY+MAX_CONTOUR_HEIGHT;
			int minY = (int)(originY-MAX_CONTOUR_HEIGHT-maxThickness);
			return minY+rand.nextInt(maxY-minY+1);
		}

		public int getRandomZ(Random rand) {
			int minZ = MathHelper.floor_double(originZ+0.5-outerRadius);
			int maxZ = MathHelper.floor_double(originZ+0.5+outerRadius);
			return minZ+rand.nextInt(maxZ-minZ+1);
		}

		public int getTopY(World world, int x, int z) {
			return 1+Math.max(originY, world.getTopSolidOrLiquidBlock(x, z));
		}

	}

}
