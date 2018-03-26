/*
 * This file is part of Skills, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE <https://github.com/InspireNXE/>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.inspirenxe.skills.impl.database;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.inspirenxe.skills.impl.database.generated.tables.TblSkillExperience.TBL_SKILL_EXPERIENCE;
import static org.inspirenxe.skills.impl.database.generated.tables.TblSkillType.TBL_SKILL_TYPE;

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

  public static DatabaseQuery<Query> createInsertSkillTypeQuery(String skillType) {
    return context -> context.insertInto(TblSkillType.TBL_SKILL_TYPE, TblSkillType.TBL_SKILL_TYPE.NAME).values(skillType);
  }

  public static DatabaseQuery<SelectConditionStep<TblSkillExperienceRecord>> createFetchExperienceQuery(UUID containerUniqueId, UUID holderUniqueId,
      String skillType) {
    checkNotNull(containerUniqueId);
    checkNotNull(holderUniqueId);
    checkNotNull(skillType);

    return context -> context.selectFrom(TBL_SKILL_EXPERIENCE)
        .where(TBL_SKILL_EXPERIENCE.CONTAINER_UNIQUE_ID.eq(containerUniqueId)
            .and(TBL_SKILL_EXPERIENCE.HOLDER_UNIQUE_ID.eq(holderUniqueId)
                .and(TBL_SKILL_EXPERIENCE.FK_SKILL_TYPE.eq(skillType))));
  }

  public static DatabaseQuery<InsertValuesStep4<TblSkillExperienceRecord, UUID, UUID, String, BigDecimal>> createInsertSkillExperienceQuery(UUID
      containerUniqueId, UUID holderUniqueId, String skillType, double experience) {
    checkNotNull(containerUniqueId);
    checkNotNull(holderUniqueId);
    checkNotNull(skillType);

    final BigDecimal dbXp = BigDecimal.valueOf(experience);

    return context -> context.insertInto(TBL_SKILL_EXPERIENCE, TBL_SKILL_EXPERIENCE.CONTAINER_UNIQUE_ID,
        TBL_SKILL_EXPERIENCE.HOLDER_UNIQUE_ID, TBL_SKILL_EXPERIENCE.FK_SKILL_TYPE, TBL_SKILL_EXPERIENCE.EXPERIENCE)
        .values(containerUniqueId, holderUniqueId, skillType, dbXp);
  }

  public static DatabaseQuery<UpdateConditionStep<TblSkillExperienceRecord>> createUpdateSkillExperienceQuery(UUID containerUniqueId, UUID
      holderUniqueId, String skillType, double experience, Timestamp lastGainedExperience) {
    checkNotNull(containerUniqueId);
    checkNotNull(holderUniqueId);
    checkNotNull(skillType);
    checkNotNull(lastGainedExperience);

    final BigDecimal dbXp = BigDecimal.valueOf(experience);

    return context -> context.update(TBL_SKILL_EXPERIENCE)
        .set(TBL_SKILL_EXPERIENCE.EXPERIENCE, dbXp)
        .set(TBL_SKILL_EXPERIENCE.LAST_GAINED_EXPERIENCE, lastGainedExperience)
        .where(TBL_SKILL_EXPERIENCE.CONTAINER_UNIQUE_ID.eq(containerUniqueId)
            .and(TBL_SKILL_EXPERIENCE.HOLDER_UNIQUE_ID.eq(holderUniqueId)
                .and(TBL_SKILL_EXPERIENCE.FK_SKILL_TYPE.eq(skillType))));
  }

  public static DatabaseQuery<SelectConditionStep<Record1<Integer>>> createHasExperienceInSkillQuery(UUID containerUniqueId, UUID holderUniqueId,
      String skillType) {
    checkNotNull(containerUniqueId);
    checkNotNull(holderUniqueId);
    checkNotNull(skillType);

    return context -> context.selectOne().from(TBL_SKILL_EXPERIENCE).where(TBL_SKILL_EXPERIENCE.CONTAINER_UNIQUE_ID.eq
        (containerUniqueId).and(TBL_SKILL_EXPERIENCE.HOLDER_UNIQUE_ID.eq(holderUniqueId).and(TBL_SKILL_EXPERIENCE.FK_SKILL_TYPE.eq
        (skillType))));
  }
}
