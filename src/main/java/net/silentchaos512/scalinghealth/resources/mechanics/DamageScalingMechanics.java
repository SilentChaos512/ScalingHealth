package net.silentchaos512.scalinghealth.resources.mechanics;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.silentchaos512.scalinghealth.event.DamageScaling;
import net.silentchaos512.scalinghealth.utils.serialization.SerializationUtils;

import java.util.Collections;
import java.util.List;

public class DamageScalingMechanics {
    public static final String FILE = "damage_scaling";

    public static final Codec<DamageScalingMechanics> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    SerializationUtils.positiveDouble().fieldOf("difficultyWeight").forGetter(d -> d.difficultyWeight),
                    SerializationUtils.positiveDouble().fieldOf("genericScale").forGetter(d -> d.genericScale),
                    Codec.BOOL.fieldOf("affectHostiles").forGetter(d -> d.affectHostiles),
                    Codec.BOOL.fieldOf("affectPeacefuls").forGetter(d -> d.affectPeaceful),
                    Codec.STRING.listOf().fieldOf("modBlacklist").forGetter(d -> d.modBlackList),
                    DamageScaling.Mode.CODEC.fieldOf("mode").forGetter(d -> d.mode),
                    Codec.mapPair(
                            Codec.STRING.listOf().fieldOf("damageTypes"),
                            Codec.DOUBLE.fieldOf("scale")
                    ).codec().listOf().fieldOf("damageScales").forGetter(d -> d.scales)
            ).apply(inst, DamageScalingMechanics::new)
    );

    public final double difficultyWeight;
    public final double genericScale;
    public final boolean affectHostiles;
    public final boolean affectPeaceful;
    public final List<String> modBlackList;
    public final DamageScaling.Mode mode;
    public final List<Pair<List<String>, Double>> scales;

    public DamageScalingMechanics(double difficultyWeight, double genericScale, boolean affectHostiles, boolean affectPeaceful, List<String> modBlackList, DamageScaling.Mode mode, List<Pair<List<String>, Double>> scales) {
        this.difficultyWeight = difficultyWeight;
        this.genericScale = genericScale;
        this.affectHostiles = affectHostiles;
        this.affectPeaceful = affectPeaceful;
        this.modBlackList = modBlackList;
        this.mode = mode;
        this.scales = scales;
    }

    public static final DamageScalingMechanics DEFAULT = new DamageScalingMechanics(
            0.04,
            0,
            true,
            false,
            Collections.emptyList(),
            DamageScaling.Mode.MAX_HEALTH,
            Collections.emptyList()
    );
}
