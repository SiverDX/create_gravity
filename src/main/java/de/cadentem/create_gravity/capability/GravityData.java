package de.cadentem.create_gravity.capability;

import net.minecraft.nbt.CompoundTag;

public class GravityData {
    private double oxygenDamage;

    public void damageOxygen(double value) {
        oxygenDamage += value;
    }

    public double getOxygenDamage() {
        return oxygenDamage;
    }

    public void resetOxygenDamage() {
        oxygenDamage = 0;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("oxygenDamage", oxygenDamage);

        return tag;
    }

    public void deserializeNBT(final CompoundTag tag) {
        oxygenDamage = tag.getDouble("oxygenDamage");
    }
}
