package org.wenyan.wenyan_addon.qi.spell;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

import java.util.List;
import java.util.Set;

/**
 * 无倾向符咒方法的路由器：按输入灵气属性组合选择最匹配的分支。
 */
public final class QiSpellRouter {
    private QiSpellRouter() {
    }

    public record Branch(Set<ElementAttribute> forPrimary, QiSpellMethod method) {
    }

    public static QiSpellMethod route(List<Branch> branches, QiSpellMethod fallback) {
        return (ctx, request, context) -> {
            Branch best = null;
            int bestScore = -1;
            for (Branch branch : branches) {
                int score = score(branch.forPrimary(), context.match().input());
                if (score >= bestScore) {
                    bestScore = score;
                    best = branch;
                }
            }
            return (best != null ? best.method() : fallback).invoke(ctx, request, context);
        };
    }

    private static int score(Set<ElementAttribute> branchTags, QiComposition input) {
        if (branchTags.isEmpty()) {
            return 0;
        }
        Set<ElementAttribute> inputs = input.present();
        if (branchTags.equals(inputs)) {
            return 100;
        }
        if (inputs.containsAll(branchTags)) {
            return 70;
        }
        long common = branchTags.stream().filter(inputs::contains).count();
        return (int) (common * 20);
    }
}
