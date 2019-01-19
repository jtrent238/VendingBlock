package info.jbcs.minecraft.vending.gui;


import info.jbcs.minecraft.vending.GeneralClient;
import info.jbcs.minecraft.vending.inventory.ContainerPickBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import java.io.IOException;

public class GuiPickBlock extends GuiContainer
{
    Scrollbar scrollbar;
    ContainerPickBlock container;
    GuiScreen parent;

    public GuiPickBlock(EntityPlayer player, ItemStack stack, GuiScreen screen)
    {
        super(new ContainerPickBlock(player));
        ySize = 185;
        xSize = 195;
        container = (ContainerPickBlock) inventorySlots;
        container.gui = this;
        parent = screen;
        container.resultSlot.putStack(stack);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
    {
        GeneralClient.bind("vending:textures/list_items.png");
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        scrollbar.drawButton(mc, x, y);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        scrollbar.handleMouseInput();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.add(scrollbar = new Scrollbar(200, guiLeft + 175, guiTop + 18, 12, 124, "")
        {
            @Override
            public void onScrolled(float off)
            {
                int columnsNotFitting = container.items.size() / container.width - container.height + 1;

                if (columnsNotFitting < 0)
                {
                    columnsNotFitting = 0;
                }

                if (columnsNotFitting == 0)
                {
                    scrollbar.active = false;
                    scrollbar.offset = 0;
                }
                else
                {
                    scrollbar.active = true;
                    scrollbar.step = 1.0f / columnsNotFitting;
                }

                container.scrollTo(off);
            }
        });
        buttonList.add(new GuiButton(100, guiLeft + 44, guiTop + 151, 70, 20, I18n.translateToLocal("gui.vendingBlock.select")));
    }

    public void picked(ItemStack stack)
    {
        if (parent == null)
        {
            return;
        }

        if (parent instanceof IPickBlockHandler)
        {
            ((IPickBlockHandler) parent).blockPicked(stack);
        }
    }

    /**
     * Fired when a control is clicked. This is the equivalent of
     * ActionListener.actionPerformed(ActionEvent e).
     */
    @Override
    protected void actionPerformed(GuiButton button)
    {
        switch (button.id)
        {
            case 100:
                ItemStack stack = container.resultSlot.getStack();

                if (parent instanceof IPickBlockHandler)
                {
                    ((IPickBlockHandler) parent).blockPicked(stack);
                }

                Minecraft.getMinecraft().displayGuiScreen(parent);
                break;
        }
    }
}
