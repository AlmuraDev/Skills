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
package org.inspirenxe.skills.api.event;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillType;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;

import java.util.UUID;

public interface ExperienceEvent extends Event {

  /**
   * Gets the {@link UUID} of the container.
   *
   * @return The container unique id
   */
  UUID getContainerUniqueId();

  /**
   * Gets the {@link UUID} of the holder.
   *
   * @return The holder unique id
   */
  UUID getHolderUniqueId();

  /**
   * Gets the {@link SkillType}.
   *
   * @return The skill type
   */
  SkillType getSkillType();

  /**
   * Gets the original experience change that would occur barring no other changes.
   *
   * @return The original experience change
   */
  double getOriginalExperience();

  /**
   * Gets the experience that will be changed on the {@link Skill}. Barring no other changes in a sub event,
   * this will equal the result of {@link ExperienceEvent#getOriginalExperience()}.
   *
   * @return The experience change
   */
  double getExperience();

  /**
   * Helper method that returns the difference in experience from the original amount passed to the event
   * from what it'll be when the event is resolved.
   *
   * @return The difference in experience
   */
  default double getExperienceDifference() {
    return getExperience() - getOriginalExperience();
  }

  interface Change extends ExperienceEvent {

    /**
     * Gets the {@link Skill}.
     *
     * @return The skill
     */
    Skill getSkill();

    /**
     * Called before the change in experience occurs.
     *
     * <Note>
     *   May be called asynchronously.
     * </Note>
     */
    interface Pre extends Change, Cancellable {

      /**
       * Sets the experience that will be changed on the {@link Skill}.
       *
       * @param experience The new experience change
       */
      void setExperience(final double experience);
    }

    /**
     * Called after the change in experience occurs.
     *
     * <Note>
     *   This will always be called on the main thread.
     * </Note>
     */
    interface Post extends Change {

      /**
       * Called after the change in experience occurs and the level changed.
       *
       * <Note>
       *   This will always be called on the main thread.
       * </Note>
       */
      interface Level extends Post {

        /**
         * Gets the original level.
         * @return The original level
         */
        int getOriginalLevel();

        /**
         * Gets the new level.
         * @return The new level
         */
        int getLevel();
      }
    }
  }

  interface Load extends ExperienceEvent {

    /**
     * Returns true if experience has been gained before.
     *
     * @return True if experience has been gained before, false if not
     */
    boolean hasGainedExperienceBefore();

    /**
     * Called before loading experience.
     *
     * <Note>
     *   May be called asynchronously.
     * </Note>
     */
    interface Pre extends Load {

      /**
       * Sets the experience that will be changed.
       *
       * @param experience The new experience change
       */
      void setExperience(final double experience);
    }

    /**
     * Called after experience is loaded.
     *
     * <Note>
     *   This will always be called on the main thread.
     * </Note>
     */
    interface Post extends Load {

      /**
       * Gets the {@link Skill}.
       *
       * @return The skill
       */
      Skill getSkill();
    }
  }

  interface Save extends ExperienceEvent {

    /**
     * Called before saving experience.
     *
     * <Note>
     *   May be called asynchronously.
     * </Note>
     */
    interface Pre extends Save {

      /**
       * Sets the experience that will be changed.
       *
       * @param experience The new experience change
       */
      void setExperience(final double experience);
    }

    /**
     * Called after experience is saved.
     *
     * <Note>
     *   This will always be called on the main thread.
     * </Note>
     */
    interface Post extends Save {

      /**
       * Gets the {@link Skill}.
       *
       * @return The skill
       */
      Skill getSkill();
    }
  }
}
