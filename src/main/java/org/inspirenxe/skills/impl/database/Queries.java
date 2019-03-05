/*
 * This file is part of Skills, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE
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
import static com.google.common.base.Preconditions.checkState;
import static org.inspirenxe.skills.generated.Tables.SKILLS_BLOCK_CREATION;
import static org.inspirenxe.skills.generated.Tables.SKILLS_BLOCK_CREATION_PALLETE;
import static org.inspirenxe.skills.generated.Tables.SKILLS_EXPERIENCE;

import org.inspirenxe.skills.generated.tables.records.SkillsBlockCreationPalleteRecord;
import org.inspirenxe.skills.generated.tables.records.SkillsBlockCreationRecord;
import org.inspirenxe.skills.generated.tables.records.SkillsExperienceRecord;
import org.jooq.DeleteConditionStep;
import org.jooq.DeleteWhereStep;
import org.jooq.InsertValuesStep2;
import org.jooq.InsertValuesStep3;
import org.jooq.InsertValuesStep4;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.UpdateConditionStep;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;

public final class Queries {

  public static DatabaseQuery<SelectConditionStep<SkillsExperienceRecord>> createFetchExperienceQuery(final UUID container, final UUID holder,
    final String skillType) {

    checkNotNull(container);
    checkNotNull(holder);
    checkNotNull(skillType);

    final byte[] containerData = DatabaseUtils.toBytes(container);
    final byte[] holderData = DatabaseUtils.toBytes(holder);

    return context -> context
      .selectFrom(SKILLS_EXPERIENCE)
      .where(SKILLS_EXPERIENCE.CONTAINER.eq(containerData).and(SKILLS_EXPERIENCE.HOLDER.eq(holderData).and(SKILLS_EXPERIENCE.SKILL.eq(skillType)
      )));
  }

  public static DatabaseQuery<InsertValuesStep4<SkillsExperienceRecord, byte[], byte[], String, BigDecimal>> createInsertSkillExperienceQuery(final
    UUID container, final UUID holder, final String skillType, final double experience) {

    checkNotNull(container);
    checkNotNull(holder);
    checkNotNull(skillType);

    final BigDecimal dbXp = BigDecimal.valueOf(experience);
    final byte[] containerData = DatabaseUtils.toBytes(container);
    final byte[] holderData = DatabaseUtils.toBytes(holder);
    return context -> context
      .insertInto(SKILLS_EXPERIENCE, SKILLS_EXPERIENCE.CONTAINER, SKILLS_EXPERIENCE.HOLDER, SKILLS_EXPERIENCE.SKILL, SKILLS_EXPERIENCE
          .EXPERIENCE)
      .values(containerData, holderData, skillType, dbXp);
  }

  public static DatabaseQuery<UpdateConditionStep<SkillsExperienceRecord>> createUpdateSkillExperienceQuery(final UUID container, final UUID holder,
    final String skillType, final double experience, final Timestamp modified) {

    checkNotNull(container);
    checkNotNull(holder);
    checkNotNull(skillType);
    checkNotNull(modified);

    final BigDecimal dbXp = BigDecimal.valueOf(experience);
    final byte[] containerData = DatabaseUtils.toBytes(container);
    final byte[] holderData = DatabaseUtils.toBytes(holder);

    return context -> context.update(SKILLS_EXPERIENCE).set(SKILLS_EXPERIENCE.EXPERIENCE, dbXp).set(SKILLS_EXPERIENCE.MODIFIED, modified).where
      (SKILLS_EXPERIENCE.CONTAINER.eq(containerData).and(SKILLS_EXPERIENCE.HOLDER.eq(holderData).and(SKILLS_EXPERIENCE.SKILL.eq(skillType))));
  }

  public static DatabaseQuery<SelectConditionStep<Record1<Integer>>> createHasExperienceInSkillQuery(final UUID container, final UUID holder, final
  String skillType) {

    checkNotNull(container);
    checkNotNull(holder);
    checkNotNull(skillType);

    final byte[] containerData = DatabaseUtils.toBytes(container);
    final byte[] holderData = DatabaseUtils.toBytes(holder);

    return context -> context.selectOne().from(SKILLS_EXPERIENCE).where(SKILLS_EXPERIENCE.CONTAINER.eq(containerData).and(SKILLS_EXPERIENCE.HOLDER.eq
      (holderData).and(SKILLS_EXPERIENCE.SKILL.eq(skillType))));
  }

  public static DatabaseQuery<SelectConditionStep<SkillsBlockCreationPalleteRecord>> createFetchBlockCreationPalleteQuery(final String id) {
    checkNotNull(id);

    return context -> context
        .selectFrom(SKILLS_BLOCK_CREATION_PALLETE)
        .where(SKILLS_BLOCK_CREATION_PALLETE.ID.eq(id));
  }

  public static DatabaseQuery<InsertValuesStep2<SkillsBlockCreationPalleteRecord, String, String>> createInsertBlockCreationPalleteQuery(final String id, final String name) {
    checkNotNull(id);
    checkNotNull(name);

    return context -> context
        .insertInto(SKILLS_BLOCK_CREATION_PALLETE, SKILLS_BLOCK_CREATION_PALLETE.ID, SKILLS_BLOCK_CREATION_PALLETE.NAME)
        .values(id, name);
  }

  public static DatabaseQuery<SelectConditionStep<SkillsBlockCreationRecord>> createFetchBlockCreationQuery(final UUID container) {
    checkNotNull(container);

    final byte[] containerData = DatabaseUtils.toBytes(container);
    return context -> context
        .selectFrom(SKILLS_BLOCK_CREATION)
        .where(SKILLS_BLOCK_CREATION.CONTAINER.eq(containerData));
  }

  public static DatabaseQuery<InsertValuesStep3<SkillsBlockCreationRecord, byte[], Long, Integer>> createInsertBlockCreationQuery(final UUID container
      , final long pos, final int creationTypeIndex) {
    checkNotNull(container);
    checkState(creationTypeIndex >= 0);

    final byte[] containerData = DatabaseUtils.toBytes(container);
    return context -> context
        .insertInto(SKILLS_BLOCK_CREATION, SKILLS_BLOCK_CREATION.CONTAINER, SKILLS_BLOCK_CREATION.POS,
            SKILLS_BLOCK_CREATION.CREATION_TYPE)
        .values(containerData, pos, creationTypeIndex);
  }

  public static DatabaseQuery<DeleteConditionStep<SkillsBlockCreationRecord>> createDeleteBlockCreationQuery(final UUID container,
      final long pos) {
    checkNotNull(container);

    final byte[] containerData = DatabaseUtils.toBytes(container);
    return context -> context
        .deleteFrom(SKILLS_BLOCK_CREATION)
        .where(SKILLS_BLOCK_CREATION.CONTAINER.eq(containerData).and(SKILLS_BLOCK_CREATION.POS.eq(pos)));
  }
}
