package blade.impl.action.attack.crystal;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.CrystalPosition;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.util.BotMath;
import blade.util.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static blade.impl.action.attack.Attack.isPvPSatisfied;
import static blade.impl.action.attack.crystal.Crystal.getCrystalScore;

public class PlaceObsidian extends BladeAction implements Crystal {
    private CrystalPosition crystalPos = null;

    public Slot getObsidianSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(Items.OBSIDIAN), SlotFlag.OFF_HAND, SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot obsidianSlot = getObsidianSlot();
        if (obsidianSlot == null) return;
        if (obsidianSlot.isHotbar()) {
            bot.getInventory().setSelectedSlot(obsidianSlot.getHotbarIndex());
        }

        float time = ConfigKeys.getDifficultyReversedCubic(bot) * 0.3f;
        Vec3 lookAt = crystalPos.placeAgainst();
        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        Vec3 direction = lookAt.subtract(eyePos);
        float yaw = BotMath.getYaw(direction);
        float pitch = BotMath.getPitch(direction);
        bot.lookRealistic(yaw, pitch, (tick % time) / time);
        if (tick >= time) {
            bot.interact();
        }
    }

    @Override
    public boolean isSatisfied() {
        return isPvPSatisfied(bot) && (crystalPos = CrystalPosition.get(bot, crystalPos)) != null;
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Player player = bot.getVanillaPlayer();
        Level world = bot.getVanillaPlayer().level();
        Vec3 eyePos = player.getEyePosition();
        Vec3 closestPoint = BotMath.getClosestPoint(eyePos, target.getBoundingBox());
        double distSq = closestPoint.distanceToSqr(eyePos);
        BlockState obsidian = world.getBlockState(crystalPos.obsidian());

        return getCrystalScore(bot) +
                (distSq > 3 * 3 ? -8 : (Math.min(distSq, 3))) +
                (Math.max(Math.min(crystalPos.confidence() / 3, 3), 0)) +
                (getObsidianSlot() == null ? -8 : 0) +
                (obsidian.isAir() ? 0 : -12);
    }

    @Override
    public String toString() {
        return String.format("place_obsidian[pos=%s]", crystalPos);
    }
}
