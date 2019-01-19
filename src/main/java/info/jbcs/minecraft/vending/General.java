package info.jbcs.minecraft.vending;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.*;

public class General {
	public static Random rand = new Random();

	public static void propelTowards(Entity what, Entity whereTo, double force) {
		double dx = whereTo.posX - what.posX;
		double dy = whereTo.posY - what.posY;
		double dz = whereTo.posZ - what.posZ;
		double total = Math.sqrt(dx * dx + dy * dy + dz * dz);

		if (total == 0) {
			what.motionX = 0;
			what.motionY = 0;
			what.motionZ = 0;
		} else {
			what.motionX = dx / total * force;
			what.motionY = dy / total * force;
			what.motionZ = dz / total * force;
		}
	}
	
	public static boolean isInRange(double distance, double x1, double y1, double z1, double x2, double y2, double z2) {
		double x = x1 - x2;
		double y = y1 - y2;
		double z = z1 - z2;

		return x * x + y * y + z * z < distance * distance;
	}

	public static Item getItem(ItemStack stack) {
		if (stack == null)
			return null;

		return stack.getItem();
	}
/*
	public static Block getBlock(int blockId) {
		if (blockId < 0)
			return null;
		
		return Block.blocksList[blockId];
	}
*/
	public static Item getItem(int itemId) {
		return GameData.getItemRegistry().getObjectById(itemId);
	}

	public static Integer getItemId(Item item){
		return GameData.getItemRegistry().getId(item);
	}

	public static String getUnlocalizedName(Block block) {
		String name=block.getUnlocalizedName();
			if(name.startsWith("tile.")) name=name.substring(5);
			
			return name;
	}
	
	static HashMap<String,Block> blockMapping;
	public static Block getBlock(String s,Block fallback){
		Set blockReg = GameData.getBlockRegistry().getKeys();
		List<String> blockList = new ArrayList<String>();
		blockList.addAll(blockReg);
		String[] blockNames = blockList.toArray(new String[0]);

		if(blockMapping==null){
			blockMapping=new HashMap<String,Block>();

			for(int i = 0;i<blockList.size();i++){
				Block block = Block.getBlockFromName(blockNames[i]);
				if(block==null) continue;
				String name=block.getUnlocalizedName();
				if(name.startsWith("tile.")) name=name.substring(5);

				blockMapping.put(name.toLowerCase(), block);
			}
		}
		
		Block res=blockMapping.get(s.toLowerCase());
		if(res==null) return fallback;
		return res;
		
	}
	
	
	public static Block getBlock(String s){
		return getBlock(s, Blocks.stone);
	}

	public static String getName(Block block){
		String res=block.getUnlocalizedName();
		return res.substring(5);
	}
	

	public static RayTraceResult getMovingObjectPositionFromPlayer(World par1World, EntityPlayer par2EntityPlayer, boolean par3) {
		float var4 = 1.0F;
		float var5 = par2EntityPlayer.prevRotationPitch + (par2EntityPlayer.rotationPitch - par2EntityPlayer.prevRotationPitch) * var4;
		float var6 = par2EntityPlayer.prevRotationYaw + (par2EntityPlayer.rotationYaw - par2EntityPlayer.prevRotationYaw) * var4;
		double var7 = par2EntityPlayer.prevPosX + (par2EntityPlayer.posX - par2EntityPlayer.prevPosX) * var4;
		double var9 = par2EntityPlayer.prevPosY + (par2EntityPlayer.posY - par2EntityPlayer.prevPosY) * var4 + 1.62D - par2EntityPlayer.getYOffset();
		double var11 = par2EntityPlayer.prevPosZ + (par2EntityPlayer.posZ - par2EntityPlayer.prevPosZ) * var4;
		Vec3d var13 = new Vec3d(var7, var9, var11);
		float var14 = MathHelper.cos(-var6 * 0.017453292F - (float) Math.PI);
		float var15 = MathHelper.sin(-var6 * 0.017453292F - (float) Math.PI);
		float var16 = -MathHelper.cos(-var5 * 0.017453292F);
		float var17 = MathHelper.sin(-var5 * 0.017453292F);
		float var18 = var15 * var16;
		float var20 = var14 * var16;
		double var21 = 5.0D;

		if (par2EntityPlayer instanceof EntityPlayerMP) {
			var21 = ((EntityPlayerMP) par2EntityPlayer).interactionManager.getBlockReachDistance();
		}

		Vec3d var23 = var13.addVector(var18 * var21, var17 * var21, var20 * var21);
		return par1World.rayTraceBlocks(var13, var23, par3);
		//return par1World.rayTraceBlocks_do_do(var13, var23, par3, !par3);
	}
	public static int countNotNull(ItemStack[] itemStacks){
		int counter=0;
		for (ItemStack itemStack: itemStacks) {
			if(itemStack!=null) counter++;
		}
		return counter;
	}
	public static ItemStack getNotNull(ItemStack[] itemStacks, int num){
		int counter=-1;
		for (ItemStack itemStack: itemStacks) {
			if(itemStack!=null) counter++;
			if(counter==num) return itemStack;
		}
		return null;
	}
}
