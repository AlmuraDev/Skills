package org.inspirenxe.skills.impl.content;

import com.almuradev.droplet.content.loader.finder.ContentFinder;
import com.almuradev.droplet.content.type.ContentTypeModule;
import com.almuradev.droplet.inject.DropletBinder;
import net.kyori.violet.AbstractModule;
import org.inspirenxe.skills.impl.content.loader.configuration.ContentConfigurationModule;
import org.inspirenxe.skills.impl.content.loader.finder.ContentFinderImpl;

public final class ContentModule extends AbstractModule implements DropletBinder {
  @Override
  protected void configure() {
    this.bind(ContentFinder.class).to(ContentFinderImpl.class);
    this.install(new ContentConfigurationModule());
    this.install(new ContentTypeModule());
  }
}
