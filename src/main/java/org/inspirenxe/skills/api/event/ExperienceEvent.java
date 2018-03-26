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
package org.inspirenxe.skills.api.event;

import org.inspirenxe.skills.api.Skill;
import org.spongepowered.api.event.Cancellable;

import java.util.UUID;

public interface ExperienceEvent extends TargetSkillTypeEvent {

  /**
   * Gets the {@link UUID} of the container this experience event is occurring in.
   *
   * @return The container unique id
   */
  UUID getContainerUniqueId();

  /**
   * Gets the {@link UUID} of the holder this experience event is occurring for.
   *
   * @return The holder unique id
   */
  UUID getHolderUniqueId();

  /**
   * Gets the original experience change that would occur barring no plugin changes.
   *
   * @return The original experience change
   */
  double getOriginalExperience();

  /**
   * Gets the experience that will be changed on the {@link Skill}. Barring no plugin changes in a sub event,
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

  interface Change extends ExperienceEvent, TargetSkillEvent {

    @Override
    default UUID getContainerUniqueId() {
      return getTargetSkill().getHolder().getContainerUniqueId();
    }

    @Override
    default UUID getHolderUniqueId() {
      return getTargetSkill().getHolder().getHolderUniqueId();
    }

    /**
     * Called before the change in experience occurs.
     *
     * Note: This will always be called on the main thread
     */
    interface Pre extends Change, Cancellable {

      /**
       * Sets the experience that will be changed on the {@link Skill}.
       *
       * @param experience The new experience change
       */
      void setExperience(double experience);
    }

    /**
     * Called after the change in experience occurs.
     *
     * Note: This will always be called on the main thread
     */
    interface Post extends Change {

    }
  }

  interface Load extends ExperienceEvent, TargetSkillTypeEvent {

    /**
     * Returns true if the {@link Skill} has ever had experience gained before.
     *
     * @return True if experience has been gained before, false if not
     */
    boolean hasGainedExperienceBefore();

    /**
     * Called before loading the experience for the {@link TargetSkillTypeEvent#getTargetSkillType()}.
     *
     * Note: Be warned that this event could be called asynchronously by the implementation.
     */
    interface Pre extends Load {

      /**
       * Sets the experience that will be changed on the {@link Skill}.
       *
       * @param experience The new experience change
       */
      void setExperience(double experience);
    }

    /**
     * Called after experience is loaded for the {@link TargetSkillEvent#getTargetSkill()}.
     *
     * Note: This will always be called on the main thread
     */
    interface Post extends Load, TargetSkillEvent {

      @Override
      default UUID getContainerUniqueId() {
        return getTargetSkill().getHolder().getContainerUniqueId();
      }

      @Override
      default UUID getHolderUniqueId() {
        return getTargetSkill().getHolder().getHolderUniqueId();
      }
    }
  }

  interface Save extends ExperienceEvent, TargetSkillTypeEvent {

    /**
     * Called before saving the experience for the {@link TargetSkillTypeEvent#getTargetSkillType()}.
     *
     * Note: Be warned that this event could be called asynchronously by the implementation.
     */
    interface Pre extends Save {

      /**
       * Sets the experience that will be changed on the {@link Skill}.
       *
       * @param experience The new experience change
       */
      void setExperience(double experience);
    }

    /**
     * Called after experience is saved for the {@link TargetSkillEvent#getTargetSkill()}.
     *
     * Note: This will always be called on the main thread
     */
    interface Post extends Save, TargetSkillEvent {

      @Override
      default UUID getContainerUniqueId() {
        return getTargetSkill().getHolder().getContainerUniqueId();
      }

      @Override
      default UUID getHolderUniqueId() {
        return getTargetSkill().getHolder().getHolderUniqueId();
      }
    }
  }
}
