/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import Reika.ChromatiCraft.Base.TileEntity.TileEntityChromaticBase;
import Reika.ChromatiCraft.Container.ContainerAutoEnchanter;
import Reika.ChromatiCraft.Container.ContainerCastingTable;
import Reika.ChromatiCraft.Container.ContainerCollector;
import Reika.ChromatiCraft.Container.ContainerCrystalBrewer;
import Reika.ChromatiCraft.Container.ContainerCrystalCharger;
import Reika.ChromatiCraft.Container.ContainerCrystalFurnace;
import Reika.ChromatiCraft.Container.ContainerInventoryLinker;
import Reika.ChromatiCraft.Container.ContainerSpawnerProgrammer;
import Reika.ChromatiCraft.GUI.GuiAbilitySelect;
import Reika.ChromatiCraft.GUI.GuiAutoEnchanter;
import Reika.ChromatiCraft.GUI.GuiCastingTable;
import Reika.ChromatiCraft.GUI.GuiCollector;
import Reika.ChromatiCraft.GUI.GuiCrystalBrewer;
import Reika.ChromatiCraft.GUI.GuiCrystalCharger;
import Reika.ChromatiCraft.GUI.GuiCrystalFurnace;
import Reika.ChromatiCraft.GUI.GuiInventoryLinker;
import Reika.ChromatiCraft.GUI.GuiOneSlot;
import Reika.ChromatiCraft.GUI.GuiRitualTable;
import Reika.ChromatiCraft.GUI.GuiSpawnerProgrammer;
import Reika.ChromatiCraft.Registry.ChromaGuis;
import Reika.ChromatiCraft.TileEntity.TileEntityAutoEnchanter;
import Reika.ChromatiCraft.TileEntity.TileEntityCastingTable;
import Reika.ChromatiCraft.TileEntity.TileEntityCollector;
import Reika.ChromatiCraft.TileEntity.TileEntityCrystalBrewer;
import Reika.ChromatiCraft.TileEntity.TileEntityCrystalCharger;
import Reika.ChromatiCraft.TileEntity.TileEntityCrystalFurnace;
import Reika.ChromatiCraft.TileEntity.TileEntityRitualTable;
import Reika.ChromatiCraft.TileEntity.TileEntitySpawnerReprogrammer;
import Reika.DragonAPI.Base.ContainerBasicStorage;
import Reika.DragonAPI.Base.CoreContainer;
import Reika.DragonAPI.Base.OneSlotContainer;
import Reika.DragonAPI.Base.OneSlotMachine;
import Reika.DragonAPI.Interfaces.GuiController;
import Reika.DragonAPI.Interfaces.InertIInv;
import cpw.mods.fml.common.network.IGuiHandler;

public class ChromaGuiHandler implements IGuiHandler {

	public static final ChromaGuiHandler instance = new ChromaGuiHandler();
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		ChromaGuis gui = ChromaGuis.guiList[id];
		switch(gui) {
		case LINK:
			return new ContainerInventoryLinker(player, world);
		case TILE:
			TileEntity te = world.getTileEntity(x, y, z);

			if (te instanceof TileEntityAutoEnchanter)
				return new ContainerAutoEnchanter(player, (TileEntityAutoEnchanter)te);
			if (te instanceof TileEntityCollector)
				return new ContainerCollector(player, (TileEntityCollector)te);
			if (te instanceof TileEntitySpawnerReprogrammer)
				return new ContainerSpawnerProgrammer(player, (TileEntitySpawnerReprogrammer)te);
			if (te instanceof TileEntityCrystalBrewer)
				return new ContainerCrystalBrewer(player, (TileEntityCrystalBrewer) te);
			if (te instanceof TileEntityCastingTable)
				return new ContainerCastingTable(player, te);
			if (te instanceof TileEntityCrystalCharger)
				return new ContainerCrystalCharger(player, (TileEntityCrystalCharger) te);
			if (te instanceof TileEntityCrystalFurnace)
				return new ContainerCrystalFurnace(player, (TileEntityCrystalFurnace) te);

			if (te instanceof OneSlotMachine)
				return new OneSlotContainer(player, te);
			if (te instanceof GuiController)
				return new CoreContainer(player, te);
			if (te instanceof IInventory && !(te instanceof InertIInv))
				return new ContainerBasicStorage(player, te);
			break;
		case ABILITY:
			return null;
		}
		return null;
	}

	//returns an instance of the Gui you made earlier
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		ChromaGuis gui = ChromaGuis.guiList[id];
		switch(gui) {
		case LINK:
			return new GuiInventoryLinker(player, world);
		case TILE:
			TileEntity te = world.getTileEntity(x, y, z);

			if (te instanceof TileEntityAutoEnchanter)
				return new GuiAutoEnchanter(player, (TileEntityAutoEnchanter)te);
			if (te instanceof TileEntityCollector)
				return new GuiCollector(player, (TileEntityCollector)te);
			if (te instanceof TileEntitySpawnerReprogrammer)
				return new GuiSpawnerProgrammer(player, (TileEntitySpawnerReprogrammer)te);
			if (te instanceof TileEntityCrystalBrewer)
				return new GuiCrystalBrewer(player, (TileEntityCrystalBrewer) te);
			if (te instanceof TileEntityCastingTable)
				return new GuiCastingTable(player, (TileEntityCastingTable) te);
			if (te instanceof TileEntityCrystalCharger)
				return new GuiCrystalCharger(player, (TileEntityCrystalCharger) te);
			if (te instanceof TileEntityRitualTable)
				return new GuiRitualTable(player, (TileEntityRitualTable) te);
			if (te instanceof TileEntityCrystalFurnace)
				return new GuiCrystalFurnace(player, (TileEntityCrystalFurnace) te);


			if (te instanceof OneSlotMachine) {
				return new GuiOneSlot(player, (TileEntityChromaticBase)te);
			}/*
			if (te instanceof IInventory && !(te instanceof InertIInv))
				return new GuiBasicStorage(player, (RotaryCraftTileEntity)te);
			 */
			break;
		case ABILITY:
			return new GuiAbilitySelect(player);
		}
		return null;
	}
}
