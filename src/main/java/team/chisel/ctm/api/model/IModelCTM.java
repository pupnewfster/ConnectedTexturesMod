package team.chisel.ctm.api.model;

import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import team.chisel.ctm.api.texture.ICTMTexture;

public interface IModelCTM extends IModelGeometry<IModelCTM> {

    void load();
    
    Collection<ICTMTexture<?>> getCTMTextures();
    
    ICTMTexture<?> getTexture(ResourceLocation loc);

    boolean canRenderInLayer(BlockState state, RenderType layer);

    @Nullable
    TextureAtlasSprite getOverrideSprite(int tintIndex);

    @Nullable
    ICTMTexture<?> getOverrideTexture(int tintIndex, ResourceLocation loc);
}
