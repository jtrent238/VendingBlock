package info.jbcs.minecraft.vending.gui;

import info.jbcs.minecraft.vending.General;
import info.jbcs.minecraft.vending.GeneralClient;
import info.jbcs.minecraft.vending.tileentity.TileEntityVendingMachine;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;
import java.util.List;

import static info.jbcs.minecraft.vending.General.countNotNull;
import static info.jbcs.minecraft.vending.General.getNotNull;
import static java.lang.Math.max;


public class HintGui extends Gui {
    private Minecraft mc;
    public HintGui(Minecraft mc){
        super();
        this.mc = mc;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderInfo(RenderGameOverlayEvent.Post  event){
        if (event.isCancelable() || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            //event.setCanceled(true);
            return;
        } else {
            if (mc == null || mc.thePlayer == null || mc.theWorld == null) {
                return;
            }
            EntityPlayer player = mc.thePlayer;
            World world = mc.theWorld;
            RayTraceResult mop = General.getMovingObjectPositionFromPlayer(world, player, false);

            if (mop == null) {
                return;
            }

            // if (mop.typeOfHit != EnumMovingObjectType.TILE) {
            //    return;
            //}

            TileEntity te = world.getTileEntity(mop.getBlockPos());

            if (te == null) {
                return;
            }

            if (!(te instanceof TileEntityVendingMachine)) {
                return;
            }

            TileEntityVendingMachine tileEntity = (TileEntityVendingMachine) te;
            draw(tileEntity, tileEntity.getOwnerName(), tileEntity.getSoldItems(), tileEntity.getBoughtItems());
            GeneralClient.bind("textures/gui/icons.png");
        }
    }

    void drawNumberForItem(FontRenderer fontRenderer, ItemStack stack, int ux, int uy) {
        if (stack == null || stack.stackSize < 2) {
            return;
        }

        String line = "" + stack.stackSize;
        int x = ux + 19 - 2 - fontRenderer.getStringWidth(line);
        int y = uy + 6 + 3;
        GL11.glTranslatef(0.0f, 0.0f, 500.0f);
        drawString(fontRenderer, line, x + 1, y + 1, 0x888888);
        drawString(fontRenderer, line, x, y, 0xffffff);
        GL11.glTranslatef(0.0f, 0.0f, -500.0f);
    }
    void drawItemsWithLabel(FontRenderer fontRenderer, String label, int x, int y, int colour, ItemStack[] itemStacks, boolean drawDescription, int descWidth){
        int w = fontRenderer.getStringWidth(I18n.translateToLocal(label))+2;
        int numOfItems = countNotNull(itemStacks);
        int witdth = (drawDescription? max(w+18*numOfItems, descWidth):w+18*numOfItems);
        x-=witdth/2;
        drawString(fontRenderer, I18n.translateToLocal(label), x, y, colour);
        for (ItemStack itemStack: itemStacks) {
            if(itemStack==null) continue;
            this.renderItemIntoGUI(itemStack, x + w, y - 4);
            drawNumberForItem(fontRenderer, itemStack, x + w, y - 4);
            w+=18;
        }
        y+=20;
        if(drawDescription){
            ItemStack itemStack=getNotNull(itemStacks, ((int) mc.thePlayer.worldObj.getWorldTime() / 50) % numOfItems);
            if(itemStack!=null) {
                String line;
                for(Object object: itemStack.getTooltip(mc.thePlayer, false).toArray()){
                    line = object.toString();
                    if(!line.isEmpty()) {
                        drawString(fontRenderer, line, x, y, 0xa0a0a0);
                        y+=16;
                    }
                }
            }
        }
    }
    public void renderItemIntoGUI(ItemStack stack, int x, int y)
    {
        Minecraft mc = Minecraft.getMinecraft();
        ItemModelMesher itemModelMesher = mc.getRenderItem().getItemModelMesher();
        GuiRenderItem guiRenderItem = new GuiRenderItem(mc.getTextureManager(), itemModelMesher, new ItemColors());
        guiRenderItem.renderItemAndEffectIntoGUI(stack, x, y);
    }

    void draw(TileEntityVendingMachine tileEntity, String seller, ItemStack[] soldItems, ItemStack[] boughtItems) {
        boolean isSoldEmpty = countNotNull(soldItems)==0;
        boolean isBoughtEmpty = countNotNull(boughtItems)==0;

        if (isBoughtEmpty && isSoldEmpty && tileEntity.isOpen()) return;
        ScaledResolution resolution = new ScaledResolution(mc);
        FontRenderer fontRenderer = Minecraft.getMinecraft().getRenderManager().getFontRenderer();
        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();
        String tooltip;
        int c;
        int linesBought=0;
        int lengthBought=0;
        int linesSold=0;
        int lengthSold=0;
        for(ItemStack bought: boughtItems) {
            if(bought ==null) continue;
            c=0;
            for (int i = 0; i < bought.getTooltip(mc.thePlayer, false).size(); i++) {
                tooltip = bought.getTooltip(mc.thePlayer, false).get(i).toString();
                if (!tooltip.isEmpty()) c++;
                if (tooltip.length() > lengthBought) lengthBought = fontRenderer.getStringWidth(tooltip);
            }
            linesBought=max(linesBought,c);
        }
        for(ItemStack sold: soldItems) {
            if(sold ==null) continue;
            c=0;
            for (int i = 0; i < sold.getTooltip(mc.thePlayer, false).size(); i++) {
                tooltip = sold.getTooltip(mc.thePlayer, false).get(i).toString();
                if (!tooltip.isEmpty()) c++;
                if (tooltip.length() > lengthSold) lengthSold = fontRenderer.getStringWidth(tooltip);
            }
            linesSold=max(linesBought,c);linesBought=max(linesBought,c);
        }
        boolean drawDesc = mc.thePlayer.isSneaking();
        int descHeight = max(linesBought, linesSold)*16;
        int w = 104+countNotNull(soldItems)*16;
        if(drawDesc) w = max((!isBoughtEmpty && !isSoldEmpty)? 340:140, w);
        int h = 44 + (drawDesc? descHeight:0) + ((!isBoughtEmpty && !isSoldEmpty)?16:0);
        int centerYOff = -80 + (drawDesc? (descHeight)/2:0) + ((!isBoughtEmpty && !isSoldEmpty)?16/2:0);
        if(drawDesc && !isBoughtEmpty && !isSoldEmpty){h-=16; centerYOff-=16/2;}

        if(!tileEntity.isOpen()) {w = 104; h = 44; centerYOff=-80; }
        int cx = width / 2;
        int x = cx - w / 2;
        int y = height / 2 - h / 2 + centerYOff;

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0f, 0.0f, -100.0f);
        GL11.glDisable(GL11.GL_LIGHTING);


        drawGradientRect(x, y, x + w, y + h, 0xc0101010, 0xd0101010);
        drawCenteredString(fontRenderer, seller, cx, y + 8, 0xffffff);
        if(!tileEntity.isOpen()) drawCenteredString(fontRenderer, "Shop is closed", cx, y + 26, 0xa0a0a0);
        else {
            if (!isBoughtEmpty && !isSoldEmpty) {
                drawItemsWithLabel(fontRenderer, "gui.vendingBlock.isSelling", cx - (drawDesc ? 100 : 0), y + 26, 0xa0a0a0, soldItems, drawDesc, lengthSold);
                drawItemsWithLabel(fontRenderer, "gui.vendingBlock.for", cx + (drawDesc ? 100 : 0), y + (drawDesc ? 26 : 46), 0xa0a0a0, boughtItems, drawDesc, lengthBought);
            } else if (!isBoughtEmpty) {
                drawItemsWithLabel(fontRenderer, "gui.vendingBlock.isAccepting", cx, y + 26, 0xa0a0a0, boughtItems, drawDesc, lengthBought);
            } else {
                drawItemsWithLabel(fontRenderer, "gui.vendingBlock.isGivingAway", cx, y + 26, 0xa0a0a0, soldItems, drawDesc, lengthSold);
            }
        }
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
