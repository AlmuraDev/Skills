package org.inspirenxe.skills.impl.skill.builtin;

import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.inspirenxe.skills.api.level.LevelFunction;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.Constants;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.level.UnknownLevelFunction;
import org.inspirenxe.skills.impl.skill.SkillTypeBuilderImpl;
import org.inspirenxe.skills.impl.skill.SkillTypeImpl;
import org.inspirenxe.skills.impl.skill.config.SkillConfig;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.FireworkEffectData;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.projectile.Firework;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShapes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Color;

public final class MiningSkillType extends SkillTypeImpl {

    public MiningSkillType() {
        super(Constants.Plugin.ID + ":mining", "Mining",
                (SkillTypeBuilderImpl) Sponge.getRegistry().createBuilder(SkillType.Builder.class)
                        .minLevel(1)
                        .maxLevel(99)
                        .levelFunction(Sponge.getRegistry().getType(LevelFunction.class, Constants.Plugin.ID + ":mmo_style").orElse(
                                UnknownLevelFunction.instance)));

        // TODO Need kashike's fancy system
        Sponge.getEventManager().registerListeners(SkillsImpl.instance.container, this);
    }

    @Listener
    public void onInteractItemByPlayer(InteractItemEvent.Primary.MainHand event, @Root Player player) {

        // TODO Configurable?
        if (player.get(Keys.GAME_MODE).get().equals(GameModes.SURVIVAL)) {
            final ItemStackSnapshot snapshot = event.getItemStack();

            final int requiredLevel = this.getRequiredLevel(snapshot.getType());

            final SkillHolder holder = SkillsImpl.instance.skillManager.getHolder(player.getWorld().getUniqueId(), player.getUniqueId()).orElse(null);

            if (holder != null) {
                final Skill skill = holder.getSkill(this).orElse(null);
                final int currentlevel = skill.getCurrentLevel();

                if (currentlevel < requiredLevel) {
                    player.sendMessage(Text.of("This item requires lvl " + requiredLevel + " to use!"));
                    event.setCancelled(true);
                }
            }
        }
    }

    @Listener(order = Order.LAST)
    public void onChangeBlockBreakByPlayer(ChangeBlockEvent.Break event, @Root Player player) {
        // TODO Configurable?
        // Only gain xp in survival
        if (player.get(Keys.GAME_MODE).get().equals(GameModes.SURVIVAL)) {
            for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                final BlockSnapshot snapshot = transaction.getOriginal();

                // Player placed blocks don't gain xp
                // TODO Configurable?
                if (!snapshot.getCreator().isPresent()) {
                    final SkillHolder holder = SkillsImpl.instance.skillManager.getHolder(player.getWorld().getUniqueId(), player.getUniqueId())
                            .orElse(null);

                    if (holder != null) {

                        final Skill skill = holder.getSkill(this).orElse(null);

                        if (skill != null) {
                            final boolean canBreak = this.canBreakBlock(skill, snapshot);

                            if (!canBreak) {
                                transaction.setValid(false);
                                return;
                            }

                            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                                frame.pushCause(snapshot);
                                skill.addExperience(getExperience(snapshot));
                            }
                        }
                    }
                }
            }
        }
    }

    @Listener
    public void onExperienceEvent(ExperienceEvent.Change.Post event, @First Player player) {

        final SkillType skillType = event.getTargetSkillType();
        final double expDiff = event.getExperienceDifference();

        if (expDiff > 0) {
            final double originalExperience = event.getOriginalExperience();
            final double experience = event.getExperience();

            final int previousLevel = event.getTargetSkillType().getLevelFunction().getLevelFor(originalExperience);
            final int currentLevel = event.getTargetSkillType().getLevelFunction().getLevelFor(experience);

            player.sendMessage(ChatTypes.ACTION_BAR, Text.of("+", Constants.Format.PRETTY_EXP.format(expDiff), "xp (", TextColors.AQUA, skillType
                    .getName(), TextColors.RESET, ")"));

            if (currentLevel > previousLevel) {
                player.sendMessage(Text.of("Congratulations! You've just advanced a ", TextColors.AQUA, skillType.getName(), TextColors.RESET, " "
                        + "level and are now ", TextColors.GREEN, currentLevel, TextColors.RESET, "."));

                if (!player.get(Keys.IS_SNEAKING).get()) {
                    final FireworkEffect levelUpEffect = FireworkEffect.builder()
                            .colors(Color.GRAY, Color.BLUE)
                            .shape(FireworkShapes.BALL)
                            .build();
                    final Firework firework = (Firework) player.getWorld().createEntity(EntityTypes.FIREWORK, player.getLocation().getPosition().add(0,
                            2.5, 0));
                    final FireworkEffectData data = firework.getOrCreate(FireworkEffectData.class).get().addElement(levelUpEffect);
                    firework.offer(data);

                    player.getWorld().spawnEntity(firework);
                }

            }
        }
    }

    @Listener
    public void onAttackEntity(DamageEntityEvent event, @First DamageSource source) {
        if (source instanceof EntityDamageSource) {
            if (((EntityDamageSource) source).getSource() instanceof Firework) {
                event.setCancelled(true);
            }
        }
    }

    private boolean canBreakBlock(Skill skill, BlockSnapshot block) {
        final int currentLevel = skill.getCurrentLevel();
        final BlockType blockType = block.getState().getType();

        if (blockType.equals(BlockTypes.COAL_ORE) && currentLevel < 5) {
            return false;
        }

        if (blockType.equals(BlockTypes.LAPIS_ORE) && currentLevel < 10) {
            return false;
        }

        if (blockType.equals(BlockTypes.IRON_ORE) && currentLevel < 20) {
            return false;
        }

        if (blockType.equals(BlockTypes.LIT_REDSTONE_ORE) && currentLevel < 25) {
            return false;
        }

        if (blockType.equals(BlockTypes.GOLD_ORE) && currentLevel < 30) {
            return false;
        }

        if (blockType.equals(BlockTypes.OBSIDIAN) && currentLevel < 40) {
            return false;
        }

        if (blockType.equals(BlockTypes.DIAMOND_ORE) && currentLevel < 40) {
            return false;
        }

        if (blockType.equals(BlockTypes.EMERALD_ORE) && currentLevel < 50) {
            return false;
        }

        return true;
    }

    private double getExperience(BlockSnapshot block) {
        final BlockType blockType = block.getState().getType();

        if (blockType.equals(BlockTypes.STONE)) {
            return 5;
        }

        if (blockType.equals(BlockTypes.GRAVEL)) {
            return 6;
        }

        if (blockType.equals(BlockTypes.COAL_ORE)) {
            return 12.5;
        }

        if (blockType.equals(BlockTypes.LAPIS_ORE)) {
            return 17.5;
        }

        if (blockType.equals(BlockTypes.OBSIDIAN)) {
            return 20;
        }

        if (blockType.equals(BlockTypes.IRON_ORE)) {
            return 25;
        }

        if (blockType.equals(BlockTypes.LIT_REDSTONE_ORE)) {
            return 35;
        }

        if (blockType.equals(BlockTypes.GOLD_ORE)) {
            return 50;
        }

        if (blockType.equals(BlockTypes.DIAMOND_ORE)) {
            return 100;
        }

        if (blockType.equals(BlockTypes.EMERALD_ORE)) {
            return 250;
        }

        return 1;
    }

    private int getRequiredLevel(ItemType itemType) {
        if (itemType.equals(ItemTypes.STONE_PICKAXE)) {
            return 10;
        } else if (itemType.equals(ItemTypes.IRON_PICKAXE)) {
            return 20;
        } else if (itemType.equals(ItemTypes.GOLDEN_PICKAXE)) {
            return 30;
        } else if (itemType.equals(ItemTypes.DIAMOND_PICKAXE)) {
            return 40;
        } else {
            return 1;
        }
    }
}
