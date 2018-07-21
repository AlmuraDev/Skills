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
package org.inspirenxe.skills.impl.content.type.skill.processor;

import com.almuradev.droplet.content.processor.Processor;
import com.almuradev.droplet.registry.Registry;
import com.google.common.collect.MoreCollectors;
import com.google.inject.Inject;
import net.kyori.fragment.filter.Filter;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.filter.NodeFilters;
import net.kyori.xml.node.flattener.PathNodeFlattener;
import net.kyori.xml.node.parser.Parser;
import org.inspirenxe.skills.impl.content.component.apply.TransactionValidityApplicator;
import org.inspirenxe.skills.impl.content.type.skill.ContentSkillTypeBuilder;
import org.inspirenxe.skills.impl.content.type.skill.component.event.Branch;
import org.inspirenxe.skills.impl.content.type.skill.component.event.EventScript;
import org.inspirenxe.skills.impl.content.type.skill.component.event.EventType;
import org.inspirenxe.skills.impl.content.type.skill.component.event.branch.ResultBranch;
import org.inspirenxe.skills.impl.content.type.skill.component.event.branch.iflogic.ElseBranch;
import org.inspirenxe.skills.impl.content.type.skill.component.event.branch.iflogic.IfBranch;
import org.inspirenxe.skills.impl.content.type.skill.component.event.branch.iflogic.ThenBranch;

import java.util.stream.Collectors;

public final class EventProcessor implements Processor<ContentSkillTypeBuilder> {

  private final Registry<EventType> eventTypeRegistry;
  private final Parser<Filter> filterParser;

  @Inject
  public EventProcessor(final Registry<EventType> eventTypeRegistry, final Parser<Filter> filterParser) {
    this.eventTypeRegistry = eventTypeRegistry;
    this.filterParser = filterParser;
  }

  @Override
  public void process(final Node node, final ContentSkillTypeBuilder builder) {

    for (final EventType eventType : this.eventTypeRegistry.all()) {
      final PathNodeFlattener flattener = new PathNodeFlattener(NodeFilters.onlyElements(), eventType.getPath());

      flattener
        .flatten(node)
        .forEach(eventTypeNode -> {
          final EventScript.Builder eventScriptBuilder = builder.eventScript(eventType);

          eventTypeNode
            .nodes("apply")
            .collect(Collectors.toList())
            .forEach(applyNode -> {
              Branch.Builder branchBuilder = null;
              final Node ifNode = applyNode.nodes("if").collect(MoreCollectors.toOptional()).orElse(null);
              if (ifNode != null) {
                branchBuilder = IfBranch.builder();
                this.traverseIf(applyNode, (IfBranch.Builder) branchBuilder);
              }

              if (branchBuilder != null) {
                eventScriptBuilder.branch(branchBuilder.build());
              }
            });
        });
    }
  }

  private void traverseIf(final Node root, final IfBranch.Builder rootBuilder) {
    final Node ifNode = root.nodes("if").collect(MoreCollectors.onlyElement());
    if (ifNode.elements().stream().count() == 0) {
      rootBuilder.statement(this.filterParser.parse(ifNode));
    } else {
      rootBuilder.statement(this.filterParser.parse(ifNode.nodes().collect(MoreCollectors.onlyElement())));
    }

    final ThenBranch.Builder thenBranchBuilder = ThenBranch.builder();
    final Node thenNode = root.nodes("then").collect(MoreCollectors.onlyElement());
    thenNode
      .nodes("apply")
      .collect(Collectors.toList())
      .forEach(innerApplyNode -> {
        final Node innerIfNode = innerApplyNode
          .nodes("if")
          .collect(MoreCollectors.toOptional())
          .orElse(null);

        if (innerIfNode != null) {
          final IfBranch.Builder ifBranchBuilder = IfBranch.builder();
          this.traverseIf(innerApplyNode, ifBranchBuilder);
          thenBranchBuilder.branch(ifBranchBuilder.build());
        } else {
          // Safe to assume this is a result branch
          ResultBranch.Builder resultBranchBuilder = ResultBranch.builder();
          // TODO Apply Parsing Test Code
          resultBranchBuilder.apply(new TransactionValidityApplicator(false));
          thenBranchBuilder.branch(resultBranchBuilder.build());
        }
      });

    rootBuilder.branch(thenBranchBuilder.build());

    root
      .nodes("else")
      .collect(MoreCollectors.toOptional())
      .ifPresent(elseNode -> {
        final ElseBranch.Builder elseBranchBuilder = ElseBranch.builder();
        elseNode
          .nodes("apply")
          .collect(Collectors.toList())
          .forEach(innerApplyNode -> {
            final Node innerIfNode = innerApplyNode
              .nodes("if")
              .collect(MoreCollectors.toOptional())
              .orElse(null);

            if (innerIfNode != null) {
              final IfBranch.Builder ifBranchBuilder = IfBranch.builder();
              this.traverseIf(innerApplyNode, ifBranchBuilder);
              elseBranchBuilder.branch(ifBranchBuilder.build());
            } else {
              // Safe to assume this is a result branch
              ResultBranch.Builder resultBranchBuilder = ResultBranch.builder();
              // TODO Apply Parsing Test Code
              resultBranchBuilder.apply(new TransactionValidityApplicator(false));
              elseBranchBuilder.branch(resultBranchBuilder.build());
            }
          });

        rootBuilder.branch(elseBranchBuilder.build());
      });
  }
}
