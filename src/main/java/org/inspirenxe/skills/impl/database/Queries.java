package org.inspirenxe.skills.impl.database;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.inspirenxe.skills.impl.database.generated.tables.TblSkillExperience.TBL_SKILL_EXPERIENCE;
import static org.inspirenxe.skills.impl.database.generated.tables.TblSkillType.TBL_SKILL_TYPE;

import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.database.generated.tables.TblSkillType;
import org.inspirenxe.skills.impl.database.generated.tables.records.TblSkillExperienceRecord;
import org.inspirenxe.skills.impl.database.generated.tables.records.TblSkillTypeRecord;
import org.jooq.InsertValuesStep4;
import org.jooq.Query;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.UpdateConditionStep;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

public final class Queries {

    public static DatabaseQuery<SelectConditionStep<TblSkillTypeRecord>> createFetchSkillTypeQuery(String catalogId) {
        checkNotNull(catalogId);

        return context -> context.selectFrom(TBL_SKILL_TYPE).where(TBL_SKILL_TYPE.NAME.eq(catalogId));
    }

    public static DatabaseQuery<Query> createInsertSkillTypeQuery(SkillType skillType) {
        return context -> context.insertInto(TblSkillType.TBL_SKILL_TYPE, TblSkillType.TBL_SKILL_TYPE.NAME).values(skillType.getId());
    }

    public static DatabaseQuery<SelectConditionStep<TblSkillExperienceRecord>> createFetchExperienceQuery(UUID containerUniqueId, UUID holderUniqueId,
            SkillType skillType) {
        checkNotNull(containerUniqueId);
        checkNotNull(holderUniqueId);
        checkNotNull(skillType);

        return context -> context.selectFrom(TBL_SKILL_EXPERIENCE)
                .where(TBL_SKILL_EXPERIENCE.CONTAINER_UNIQUE_ID.eq(containerUniqueId)
                        .and(TBL_SKILL_EXPERIENCE.HOLDER_UNIQUE_ID.eq(holderUniqueId)
                                .and(TBL_SKILL_EXPERIENCE.FK_SKILL_TYPE.eq(skillType.getId()))));
    }

    public static DatabaseQuery<InsertValuesStep4<TblSkillExperienceRecord, UUID, UUID, String, BigDecimal>> createInsertSkillExperienceQuery(UUID
            containerUniqueId, UUID holderUniqueId, SkillType skillType, double experience) {
        checkNotNull(containerUniqueId);
        checkNotNull(holderUniqueId);
        checkNotNull(skillType);

        final BigDecimal dbXp = BigDecimal.valueOf(experience);

        return context -> context.insertInto(TBL_SKILL_EXPERIENCE, TBL_SKILL_EXPERIENCE.CONTAINER_UNIQUE_ID,
                TBL_SKILL_EXPERIENCE.HOLDER_UNIQUE_ID, TBL_SKILL_EXPERIENCE.FK_SKILL_TYPE, TBL_SKILL_EXPERIENCE.EXPERIENCE)
                .values(containerUniqueId, holderUniqueId, skillType.getId(), dbXp);
    }

    public static DatabaseQuery<UpdateConditionStep<TblSkillExperienceRecord>> createUpdateSkillExperienceQuery(UUID containerUniqueId, UUID
            holderUniqueId, SkillType skillType, double experience, Timestamp lastGainedExperience) {
        checkNotNull(lastGainedExperience);

        final BigDecimal dbXp = BigDecimal.valueOf(experience);

        return context -> context.update(TBL_SKILL_EXPERIENCE)
                .set(TBL_SKILL_EXPERIENCE.EXPERIENCE, dbXp)
                .set(TBL_SKILL_EXPERIENCE.LAST_GAINED_EXPERIENCE, lastGainedExperience)
                .where(TBL_SKILL_EXPERIENCE.CONTAINER_UNIQUE_ID.eq(containerUniqueId)
                        .and(TBL_SKILL_EXPERIENCE.HOLDER_UNIQUE_ID.eq(holderUniqueId)
                                .and(TBL_SKILL_EXPERIENCE.FK_SKILL_TYPE.eq(skillType.getId()))));
    }

    public static DatabaseQuery<SelectConditionStep<Record1<Integer>>> createHasExperienceInSkillQuery(UUID containerUniqueId, UUID holderUniqueId,
            SkillType skillType) {
        checkNotNull(containerUniqueId);
        checkNotNull(holderUniqueId);
        checkNotNull(skillType);

        return context -> context.selectOne().from(TBL_SKILL_EXPERIENCE).where(TBL_SKILL_EXPERIENCE.CONTAINER_UNIQUE_ID.eq
                (containerUniqueId).and(TBL_SKILL_EXPERIENCE.HOLDER_UNIQUE_ID.eq(holderUniqueId).and(TBL_SKILL_EXPERIENCE.FK_SKILL_TYPE.eq
                (skillType.getId()))));
    }
}
