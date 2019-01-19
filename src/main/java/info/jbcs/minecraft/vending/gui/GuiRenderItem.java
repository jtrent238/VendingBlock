package info.jbcs.minecraft.vending.gui;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.concurrent.Callable;

@SideOnly(Side.CLIENT)
public class GuiRenderItem implements IResourceManagerReloadListener {
    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private boolean notRenderingEffectsInGUI = true;
    public float zLevel;
    private final ItemModelMesher itemModelMesher;
    private final TextureManager textureManager;
    private final ItemColors itemColors;

    public GuiRenderItem(TextureManager p_i46552_1_, ItemModelMesher itemModelMesher, ItemColors p_i46552_3_) {
        this.textureManager = p_i46552_1_;
        this.itemModelMesher = itemModelMesher;
        this.itemColors = p_i46552_3_;
    }

    public void isNotRenderingEffectsInGUI(boolean isNot) {
        this.notRenderingEffectsInGUI = isNot;
    }

    public ItemModelMesher getItemModelMesher() {
        return this.itemModelMesher;
    }


    private void renderModel(IBakedModel model, ItemStack stack) {
        this.renderModel(model, -1, stack);
    }

    private void renderModel(IBakedModel model, int color) {
        this.renderModel(model, color, (ItemStack)null);
    }

    private void renderModel(IBakedModel model, int color, ItemStack stack) {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.ITEM);
        EnumFacing[] var6 = EnumFacing.values();
        int var7 = var6.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            EnumFacing enumfacing = var6[var8];
            this.renderQuads(vertexbuffer, model.getQuads((IBlockState)null, enumfacing, 0L), color, stack);
        }

        this.renderQuads(vertexbuffer, model.getQuads((IBlockState)null, (EnumFacing)null, 0L), color, stack);
        tessellator.draw();
    }

    public void renderItem(ItemStack stack, IBakedModel model) {
        if(stack != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            if(model.isBuiltInRenderer()) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                TileEntityItemStackRenderer.instance.renderByItem(stack);
            } else {
                this.renderModel(model, stack);
                if(stack.hasEffect()) {
                    this.renderEffect(model);
                }
            }

            GlStateManager.popMatrix();
        }

    }

    private void renderEffect(IBakedModel model) {
        GlStateManager.depthMask(false);
        GlStateManager.depthFunc(514);
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
        this.textureManager.bindTexture(RES_ITEM_GLINT);
        GlStateManager.matrixMode(5890);
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
        GlStateManager.translate(f, 0.0F, 0.0F);
        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
        this.renderModel(model, -8372020);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f1 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
        GlStateManager.translate(-f1, 0.0F, 0.0F);
        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
        this.renderModel(model, -8372020);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableLighting();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        this.textureManager.bindTexture(TextureMap.locationBlocksTexture);
    }

    private void putQuadNormal(VertexBuffer renderer, BakedQuad quad) {
        Vec3i vec3i = quad.getFace().getDirectionVec();
        renderer.putNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
    }

    private void renderQuad(VertexBuffer renderer, BakedQuad quad, int color) {
        renderer.addVertexData(quad.getVertexData());
        renderer.putColor4(color);
        this.putQuadNormal(renderer, quad);
    }

    private void renderQuads(VertexBuffer renderer, List<BakedQuad> quads, int color, ItemStack stack) {
        boolean flag = color == -1 && stack != null;
        int i = 0;

        for(int j = quads.size(); i < j; ++i) {
            BakedQuad bakedquad = (BakedQuad)quads.get(i);
            int k = color;
            if(flag && bakedquad.hasTintIndex()) {
                k = this.itemColors.getColorFromItemstack(stack, bakedquad.getTintIndex());
                if(EntityRenderer.anaglyphEnable) {
                    k = TextureUtil.anaglyphColor(k);
                }

                k |= -16777216;
            }

            LightUtil.renderQuadColor(renderer, bakedquad, k);
        }

    }

    public boolean shouldRenderItemIn3D(ItemStack stack) {
        IBakedModel ibakedmodel = this.itemModelMesher.getItemModel(stack);
        return ibakedmodel == null?false:ibakedmodel.isGui3d();
    }

    public void renderItem(ItemStack stack, ItemCameraTransforms.TransformType cameraTransformType) {
        if(stack != null) {
            IBakedModel ibakedmodel = this.getItemModelWithOverrides(stack, (World)null, (EntityLivingBase)null);
            this.renderItemModel(stack, ibakedmodel, cameraTransformType, false);
        }

    }

    public IBakedModel getItemModelWithOverrides(ItemStack p_184393_1_, World p_184393_2_, EntityLivingBase p_184393_3_) {
        IBakedModel ibakedmodel = this.itemModelMesher.getItemModel(p_184393_1_);
        return ibakedmodel.getOverrides().handleItemState(ibakedmodel, p_184393_1_, p_184393_2_, p_184393_3_);
    }

    public void renderItem(ItemStack p_184392_1_, EntityLivingBase p_184392_2_, ItemCameraTransforms.TransformType p_184392_3_, boolean p_184392_4_) {
        if(p_184392_1_ != null && p_184392_2_ != null && p_184392_1_.getItem() != null) {
            IBakedModel ibakedmodel = this.getItemModelWithOverrides(p_184392_1_, p_184392_2_.worldObj, p_184392_2_);
            this.renderItemModel(p_184392_1_, ibakedmodel, p_184392_3_, p_184392_4_);
        }

    }

    protected void renderItemModel(ItemStack p_184394_1_, IBakedModel p_184394_2_, ItemCameraTransforms.TransformType p_184394_3_, boolean p_184394_4_) {
        if(p_184394_1_.getItem() != null) {
            this.textureManager.bindTexture(TextureMap.locationBlocksTexture);
            this.textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();
            p_184394_2_ = ForgeHooksClient.handleCameraTransforms(p_184394_2_, p_184394_3_, p_184394_4_);
            this.renderItem(p_184394_1_, p_184394_2_);
            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            this.textureManager.bindTexture(TextureMap.locationBlocksTexture);
            this.textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
        }

    }

    private boolean isThereOneNegativeScale(ItemTransformVec3f itemTranformVec) {
        return itemTranformVec.scale.x < 0.0F ^ itemTranformVec.scale.y < 0.0F ^ itemTranformVec.scale.z < 0.0F;
    }

    public void renderItemIntoGUI(ItemStack stack, int x, int y) {
        this.renderItemModelIntoGUI(stack, x, y, this.getItemModelWithOverrides(stack, (World)null, (EntityLivingBase)null));
    }

    protected void renderItemModelIntoGUI(ItemStack p_184390_1_, int p_184390_2_, int p_184390_3_, IBakedModel p_184390_4_) {
        GlStateManager.pushMatrix();
        this.textureManager.bindTexture(TextureMap.locationBlocksTexture);
        this.textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.setupGuiTransform(p_184390_2_, p_184390_3_, p_184390_4_.isGui3d());
        p_184390_4_ = ForgeHooksClient.handleCameraTransforms(p_184390_4_, ItemCameraTransforms.TransformType.GUI, false);
        this.renderItem(p_184390_1_, p_184390_4_);
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
        this.textureManager.bindTexture(TextureMap.locationBlocksTexture);
        this.textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
    }

    private void setupGuiTransform(int xPosition, int yPosition, boolean isGui3d) {
        GlStateManager.translate((float)xPosition, (float)yPosition, 100.0F + this.zLevel);
        GlStateManager.translate(8.0F, 8.0F, 0.0F);
        GlStateManager.scale(1.0F, -1.0F, 1.0F);
        GlStateManager.scale(16.0F, 16.0F, 16.0F);
        if(isGui3d) {
            //GlStateManager.enableLighting();
            GlStateManager.disableLighting();
        } else {
            GlStateManager.disableLighting();
        }

    }

    public void renderItemAndEffectIntoGUI(ItemStack stack, int xPosition, int yPosition) {
        this.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().thePlayer, stack, xPosition, yPosition);
    }

    public void renderItemAndEffectIntoGUI(EntityLivingBase p_184391_1_, final ItemStack p_184391_2_, int p_184391_3_, int p_184391_4_) {
        if(p_184391_2_ != null && p_184391_2_.getItem() != null) {
            this.zLevel += 50.0F;

            try {
                this.renderItemModelIntoGUI(p_184391_2_, p_184391_3_, p_184391_4_, this.getItemModelWithOverrides(p_184391_2_, (World)null, p_184391_1_));
            } catch (Throwable var8) {
                CrashReport crashreport = CrashReport.makeCrashReport(var8, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being rendered");
                crashreportcategory.addCrashSectionCallable("Item Type", new Callable() {
                    public String call() throws Exception {
                        return String.valueOf(p_184391_2_.getItem());
                    }
                });
                crashreportcategory.addCrashSectionCallable("Item Aux", new Callable() {
                    public String call() throws Exception {
                        return String.valueOf(p_184391_2_.getMetadata());
                    }
                });
                crashreportcategory.addCrashSectionCallable("Item NBT", new Callable() {
                    public String call() throws Exception {
                        return String.valueOf(p_184391_2_.getTagCompound());
                    }
                });
                crashreportcategory.addCrashSectionCallable("Item Foil", new Callable() {
                    public String call() throws Exception {
                        return String.valueOf(p_184391_2_.hasEffect());
                    }
                });
                throw new ReportedException(crashreport);
            }

            this.zLevel -= 50.0F;
        }

    }

    public void renderItemOverlays(FontRenderer fr, ItemStack stack, int xPosition, int yPosition) {
        this.renderItemOverlayIntoGUI(fr, stack, xPosition, yPosition, (String)null);
    }

    public void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text) {
        if(stack != null) {
            if(stack.stackSize != 1 || text != null) {
                String entityplayersp = text == null?String.valueOf(stack.stackSize):text;
                if(text == null && stack.stackSize < 1) {
                    entityplayersp = TextFormatting.RED + String.valueOf(stack.stackSize);
                }

                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                fr.drawStringWithShadow(entityplayersp, (float)(xPosition + 19 - 2 - fr.getStringWidth(entityplayersp)), (float)(yPosition + 6 + 3), 16777215);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }

            if(stack.getItem().showDurabilityBar(stack)) {
                double entityplayersp1 = stack.getItem().getDurabilityForDisplay(stack);
                int tessellator1 = (int)Math.round(13.0D - entityplayersp1 * 13.0D);
                int vertexbuffer1 = (int)Math.round(255.0D - entityplayersp1 * 255.0D);
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                Tessellator tessellator = Tessellator.getInstance();
                VertexBuffer vertexbuffer = tessellator.getBuffer();
                this.draw(vertexbuffer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
                this.draw(vertexbuffer, xPosition + 2, yPosition + 13, 12, 1, (255 - vertexbuffer1) / 4, 64, 0, 255);
                this.draw(vertexbuffer, xPosition + 2, yPosition + 13, tessellator1, 1, 255 - vertexbuffer1, vertexbuffer1, 0, 255);
                GlStateManager.enableAlpha();
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }

            EntityPlayerSP entityplayersp2 = Minecraft.getMinecraft().thePlayer;
            float f = entityplayersp2 == null?0.0F:entityplayersp2.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getMinecraft().getRenderPartialTicks());
            if(f > 0.0F) {
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                Tessellator tessellator11 = Tessellator.getInstance();
                VertexBuffer vertexbuffer11 = tessellator11.getBuffer();
                this.draw(vertexbuffer11, xPosition, yPosition + MathHelper.floor_float(16.0F * (1.0F - f)), 16, MathHelper.ceiling_float_int(16.0F * f), 255, 255, 255, 127);
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }

    }

    private void draw(VertexBuffer renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos((double)(x + 0), (double)(y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + 0), (double)(y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + width), (double)(y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + width), (double)(y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }

    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.itemModelMesher.rebuildCache();
    }
}
