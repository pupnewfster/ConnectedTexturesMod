package team.chisel.ctm.api.util;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenCustomHashMap;
import lombok.EqualsAndHashCode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import team.chisel.ctm.api.texture.ICTMTexture;
import team.chisel.ctm.api.texture.ITextureContext;
import team.chisel.ctm.api.texture.ITextureType;
import team.chisel.ctm.client.util.IdentityStrategy;
import team.chisel.ctm.client.util.ProfileUtil;

/**
 * List of IBlockRenderContext's
 */
@EqualsAndHashCode(of = "serialized")
@ParametersAreNonnullByDefault
public class RenderContextList {

    private final Map<ICTMTexture<?>, ITextureContext> contextMap = Maps.newIdentityHashMap();
    private final Object2LongMap<ICTMTexture<?>> serialized = new Object2LongOpenCustomHashMap<>(new IdentityStrategy<>());

    public RenderContextList(BlockState state, Collection<ICTMTexture<?>> textures, final BlockAndTintGetter world, BlockPos pos) {
        ProfileUtil.start("ctm_region_cache_update");
    	
    	ProfileUtil.endAndStart("ctm_context_gather");
        for (ICTMTexture<?> tex : textures) {
            ITextureType type = tex.getType();
            ITextureContext ctx = type.getBlockRenderContext(state, world, pos, tex);
            if (ctx != null) {
                contextMap.put(tex, ctx);
            }
        }
        
        ProfileUtil.endAndStart("ctm_context_serialize");
        for (Entry<ICTMTexture<?>, ITextureContext> e : contextMap.entrySet()) {
            serialized.put(e.getKey(), e.getValue().getCompressedData());
        }
        ProfileUtil.end();
    }

    public @Nullable ITextureContext getRenderContext(ICTMTexture<?> tex) {
        return this.contextMap.get(tex);
    }

    public boolean contains(ICTMTexture<?> tex) {
        return getRenderContext(tex) != null;
    }

    public Object2LongMap<ICTMTexture<?>> serialized() {
        return serialized;
    }
}
