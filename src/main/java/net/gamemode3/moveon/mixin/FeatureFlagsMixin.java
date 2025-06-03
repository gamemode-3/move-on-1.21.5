package net.gamemode3.moveon.mixin;

import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FeatureFlags.class)
public class FeatureFlagsMixin {
    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resource/featuretoggle/FeatureSet;of(Lnet/minecraft/resource/featuretoggle/FeatureFlag;)Lnet/minecraft/resource/featuretoggle/FeatureSet;"
            )
    )
    private static FeatureSet redirectVanillaFeatureSetOf(FeatureFlag flag) {
        // Return a FeatureSet containing both VANILLA and MINECART_IMPROVEMENTS
        return FeatureSet.of(flag, FeatureFlags.MINECART_IMPROVEMENTS);
    }
}
