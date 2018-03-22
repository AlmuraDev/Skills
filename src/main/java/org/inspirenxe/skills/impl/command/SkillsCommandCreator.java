package org.inspirenxe.skills.impl.command;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillManager;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.Constants;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.SkillsPermissions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public final class SkillsCommandCreator {

    public static CommandSpec createRootCommand() {
        return CommandSpec.builder()
                .permission(SkillsPermissions.INFO_COMMAND)
                .description(Text.of("Displays user skills when logged in or commands in console"))
                .executor((source, args) -> {

                    if (source instanceof Player) {
                        final Player player = (Player) source;
                        final SkillManager manager = SkillsImpl.instance.skillManager;

                        final SkillHolder holder = manager.getHolder(player.getWorld().getUniqueId(), player.getUniqueId()).orElse(null);

                        if (holder == null) {
                            return CommandResult.success();
                        }

                        final Collection<Text> skillPrintouts = new LinkedList<>();

                        int totalLevel = 0;

                        for (Map.Entry<SkillType, Skill> skillEntry : holder.getSkills().entrySet()) {
                            final SkillType skillType = skillEntry.getKey();
                            final Skill skill = skillEntry.getValue();

                            final int currentLevel = skill.getCurrentLevel();
                            final int maxLevel = skillType.getMaxLevel();

                            final double currentExperience = skill.getCurrentExperience();
                            final double maxLevelExperience = skillType.getLevelFunction().getXPFor(maxLevel);
                            double toNextLevelExperience = 0;

                            if (currentLevel != maxLevel) {
                                toNextLevelExperience = skillType.getLevelFunction().getXPFor(currentLevel + 1) - currentExperience;
                            }

                            skillPrintouts.add(Text.of(TextColors.AQUA, skillType.getName(), TextColors.RESET, " :: Lv. ", currentLevel, "/",
                                    maxLevel));

                            skillPrintouts.add(Text.of("  Current XP: ", Constants.Format.PRETTY_EXP.format(currentExperience), "/", Constants.Format
                                    .PRETTY_EXP.format(maxLevelExperience)));

                            skillPrintouts.add(Text.of("  Next Level: ", Constants.Format.PRETTY_EXP.format(toNextLevelExperience)));

                            totalLevel += currentLevel;
                        }

                        final PaginationList list = Sponge.getServiceManager().provide(PaginationService.class).get().builder()
                                .title(Text.of(TextColors.RED, "My Skills"))
                                .contents(skillPrintouts)
                                .footer(Text.of(TextColors.YELLOW, "Total Level: ", TextColors.RESET, totalLevel))
                                .build();
                        list.sendTo(player);
                    }

                    return CommandResult.success();
                })
                .build();
    }
}
