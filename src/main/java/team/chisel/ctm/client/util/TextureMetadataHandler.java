package team.chisel.ctm.client.util;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.resources.model.ModelResourceLocation;
import org.apache.logging.log4j.message.ParameterizedMessage;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import lombok.SneakyThrows;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.CTM;
import team.chisel.ctm.client.model.AbstractCTMBakedModel;
import team.chisel.ctm.client.model.ModelBakedCTM;
import team.chisel.ctm.client.model.ModelCTM;

public enum TextureMetadataHandler {

    INSTANCE;

	private final Object2BooleanMap<ModelResourceLocation> wrappedModels = new Object2BooleanLinkedOpenHashMap<>();
    private final Multimap<ResourceLocation, Material> scrapedTextures = HashMultimap.create();

    public void textureScraped(ResourceLocation modelLocation, Material material) {
        scrapedTextures.put(modelLocation, material);
    }
    
    /*
     * Handle stitching metadata additional textures
     */
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public void onTextureStitch(TextureStitchEvent.Pre event) {
//    	Set<ResourceLocation> sprites = new HashSet<>(ObfuscationReflectionHelper.getPrivateValue(TextureStitchEvent.Pre.class, event, "sprites"));
//        for (ResourceLocation rel : sprites) {
//            try {
//                rel = new ResourceLocation(rel.getNamespace(), "textures/" + rel.getPath() + ".png");
//                Optional<IMetadataSectionCTM> metadata = ResourceUtil.getMetadata(rel);
//                var proxy = metadata.map(IMetadataSectionCTM::getProxy);
//                if (proxy.isPresent()) {
//                    ResourceLocation proxysprite = new ResourceLocation(proxy.get());
//                    Optional<IMetadataSectionCTM> proxymeta = ResourceUtil.getMetadata(ResourceUtil.spriteToAbsolute(proxysprite));
//                    // Load proxy's base sprite
//                    event.addSprite(proxysprite);
//                    proxymeta.ifPresent(m -> {
//                        // Load proxy's additional textures
//                        for (ResourceLocation r : m.getAdditionalTextures()) {
//                        	if (registeredTextures.add(r)) {
//                        		event.addSprite(r);
//                        	}
//                        }
//                    });
//                }
//                
//                metadata.map(IMetadataSectionCTM::getAdditionalTextures)
//                    .ifPresent(textures -> {
//                    // Load additional textures
//                        for (ResourceLocation r : textures) {
//                            if (registeredTextures.add(r)) {
//                                event.addSprite(r);
//                            }
//                        }
//                    });
//            }
//            catch (FileNotFoundException e) {} // Ignore these, they are reported by vanilla
//            catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    /*
     * Handle wrapping models that use CTM textures 
     */

//    private static final Class<?> multipartModelClass;
//    private static final Class<?> vanillaModelWrapperClass;
//    private static final Field multipartPartModels;
//    private static final Field modelWrapperModel;
//    static {
//        try {
//            multipartModelClass = Class.forName("net.minecraftforge.client.model.ModelLoader$MultipartModel");
//            multipartPartModels = multipartModelClass.getDeclaredField("partModels");
//            multipartPartModels.setAccessible(true);
//            vanillaModelWrapperClass = Class.forName("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper");
//            modelWrapperModel = vanillaModelWrapperClass.getDeclaredField("model");
//            modelWrapperModel.setAccessible(true);
//        } catch (ClassNotFoundException | NoSuchFieldException | SecurityException e) {
//            throw Throwables.propagate(e);
//        }
//    }

    @SubscribeEvent(priority = EventPriority.LOWEST) // low priority to capture all event-registered models
    @SneakyThrows
    public void onModelBake(ModelEvent.ModifyBakingResult event) {
        ModelBakery modelBakery = event.getModelBakery();
        Map<ModelResourceLocation, UnbakedModel> topLevelModels = ObfuscationReflectionHelper.getPrivateValue(ModelBakery.class, modelBakery, "topLevelModels");
        Map<ResourceLocation, UnbakedModel> unbakedCache = ObfuscationReflectionHelper.getPrivateValue(ModelBakery.class, modelBakery, "unbakedCache");
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        for (Map.Entry<ModelResourceLocation, BakedModel> entry : models.entrySet()) {
            ModelResourceLocation mrl = entry.getKey();
            ResourceLocation rl = mrl.id();
            UnbakedModel rootModel = topLevelModels.get(mrl);
            if (rootModel == null) {
                rootModel = unbakedCache.get(rl);
                if (rootModel != null) {
                    //TODO - 1.21: Remove this after testing against more complex models to validate if we need to do this or not
                    CTM.logger.info("Modify baking unbaked cache has an element top level doesn't: {}, {}", rl, mrl);
                }
            }
            if (rootModel != null) {
            	BakedModel baked = entry.getValue();
            	if (baked instanceof AbstractCTMBakedModel) {
            		continue;
            	}
            	if (baked.isCustomRenderer()) { // Nothing we can add to builtin models
            	    continue;
            	}
                Deque<ResourceLocation> dependencies = new ArrayDeque<>();
                Set<ResourceLocation> seenModels = new HashSet<>();
                dependencies.push(rl);
                seenModels.add(rl);
                boolean shouldWrap = wrappedModels.getOrDefault(mrl, false);
                // Breadth-first loop through dependencies, exiting as soon as a CTM texture is found, and skipping duplicates/cycles
                while (!shouldWrap && !dependencies.isEmpty()) {
                    ResourceLocation dep = dependencies.pop();
                    UnbakedModel model;
                    try {
                        //TODO - 1.21: Evaluate if this should be using getModel or something that will get the dep as a root model?
                        model = dep == rl ? rootModel : modelBakery.getModel(dep);
                    } catch (Exception e) {
                        continue;
                    }

                    try {
                        Set<Material> textures = new HashSet<>(scrapedTextures.get(dep));
                        for (Material tex : textures) {
                            // Cache all dependent texture metadata
                            try {
                                // At least one texture has CTM metadata, so we should wrap this model
                                if (ResourceUtil.getMetadata(ResourceUtil.spriteToAbsolute(tex.texture())).isPresent()) { // TODO lazy
                                    shouldWrap = true;
                                    break;
                                }
                            } catch (IOException e) {} // Fallthrough
                        }
                        if (!shouldWrap) {
                            for (ResourceLocation newDep : model.getDependencies()) {
                                if (seenModels.add(newDep)) {
                                    dependencies.push(newDep);
                                }
                            }
                        }
                    } catch (Exception e) {
                        CTM.logger.error(new ParameterizedMessage("Error loading model dependency {} for model {}. Skipping...", dep, rl), e);
                    }
                }
                wrappedModels.put(mrl, shouldWrap);
                if (shouldWrap) {
                    try {
                        entry.setValue(wrap(rootModel, baked));
                    } catch (IOException e) {
                        CTM.logger.error("Could not wrap model {}. Aborting...", rl, e);
                    }
                }
            }
        }
    }

    private @NotNull BakedModel wrap(UnbakedModel model, BakedModel object) throws IOException {
        ModelCTM modelchisel = new ModelCTM(model);
        return new ModelBakedCTM(modelchisel, object, null); 	
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @SubscribeEvent
    public void onModelBake(ModelEvent.BakingCompleted event) {
        var cache = ObfuscationReflectionHelper.<Map, ModelBakery>getPrivateValue(ModelBakery.class, event.getModelBakery(), "bakedCache");
        var cacheCopy = Map.copyOf(cache);
        cache.clear();
        for (var e : event.getModels().entrySet()) {
            if (e.getValue() instanceof AbstractCTMBakedModel baked && 
                    baked.getModel() instanceof ModelCTM ctmModel && 
                    !ctmModel.isInitialized()) {
                var baker = event.getModelBakery().new ModelBakerImpl((rl, m) -> m.sprite(), e.getKey());
                ctmModel.bake(baker, Material::sprite, BlockModelRotation.X0_Y0);
                //Note: We have to clear the cache after baking each model to ensure that we can initialize and capture any textures
                // that might be done by parent models
                cache.clear();
            }
        }
        cache.putAll(cacheCopy);
    }

    public void invalidateCaches() {
        wrappedModels.clear();
        scrapedTextures.clear();
    }
}
