package io.github.blaezdev.rwbym.capability.aura;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class Aura implements IAura {
    private float amount;
    private float max = 100.0F;
    private float recharge = 1.0F;
    private int rate = 10;
    private int delay;

    @Override
    public void tick(Player player) {
        if (this.delay > 0) {
            this.delay--;
            return;
        }
        if (this.amount >= this.max || player.tickCount % this.rate != 0 || player.getFoodData().getFoodLevel() <= 6) {
            return;
        }
        float restored = player.getFoodData().getFoodLevel() > 16 ? this.recharge : this.recharge / 4.0F;
        if (player.getFoodData().getFoodLevel() > 16) {
            player.causeFoodExhaustion(restored);
        }
        addAmount(restored);
    }

    @Override
    public float useAura(float usage, boolean overflow) {
        float remainder = this.amount - usage;
        if (remainder >= 0.0F || overflow) {
            this.amount = Math.max(remainder, 0.0F);
        }
        return remainder < 0.0F ? -remainder : 0.0F;
    }

    @Override
    public void delayRecharge(int ticks) {
        this.delay = Math.max(this.delay, ticks);
    }

    @Override
    public float getPercentage() {
        return this.max <= 0.0F ? 0.0F : this.amount / this.max;
    }

    @Override
    public void addToMax(float amount) {
        this.max = Math.max(0.0F, this.max + amount);
        this.amount = Math.min(this.amount, this.max);
    }

    @Override
    public int getExpToLevel() {
        return this.max < 200.0F ? 25 : Integer.MAX_VALUE;
    }

    @Override
    public float getMaxAura() {
        return this.max;
    }

    @Override
    public void setMaxAura(float amount) {
        this.max = Math.max(0.0F, amount);
        this.amount = Math.min(this.amount, this.max);
    }

    @Override
    public float getAmount() {
        return this.amount;
    }

    @Override
    public int getDelay() {
        return this.delay;
    }

    @Override
    public void setAmount(float amount) {
        this.amount = Math.max(0.0F, Math.min(amount, this.max));
    }

    @Override
    public void addAmount(float amount) {
        setAmount(this.amount + amount);
    }

    @Override
    public void copyFrom(IAura other) {
        setMaxAura(other.getMaxAura());
        setAmount(other.getAmount());
        delayRecharge(other.getDelay());
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("amount", this.amount);
        tag.putFloat("max", this.max);
        tag.putFloat("recharge", this.recharge);
        tag.putInt("rate", this.rate);
        tag.putInt("delay", this.delay);
        return tag;
    }

    @Override
    public void deserialize(CompoundTag tag) {
        this.amount = tag.getFloat("amount");
        this.max = tag.contains("max") ? tag.getFloat("max") : 100.0F;
        this.recharge = tag.contains("recharge") ? tag.getFloat("recharge") : 1.0F;
        this.rate = tag.contains("rate") ? Math.max(1, tag.getInt("rate")) : 10;
        this.delay = Math.max(0, tag.getInt("delay"));
        setAmount(this.amount);
    }
}
