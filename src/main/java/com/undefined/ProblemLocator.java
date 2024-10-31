package com.undefined;

import java.util.*;

public class ProblemLocator {

    private Scanner scanner;
    private ModpackManager modpack;
    private List<Mod> excludedMods;
    private List<Mod> disabledMods;
    private List<Mod> enabledMods;
    private List<Mod> suspects;

    public ProblemLocator(ModpackManager modpackManager, String... excludedModIds) {
        scanner = new Scanner(System.in);
        this.modpack = modpackManager;
        var modlist = this.modpack.getModList();
        var mods = new ArrayList<>(modlist.allMods());

        this.excludedMods = createExclusionList(excludedModIds);
        mods.removeAll(excludedMods);

        this.suspects = new ArrayList<>(mods);
        this.enabledMods = new ArrayList<>(mods);
        this.disabledMods = new ArrayList<>();
    }

    public ProblemLocator(ModpackManager modpackManager) {
        this(modpackManager, new String[0]);
    }

    private List<Mod> createExclusionList(String[] excludedModIds) {
        List<Mod> excludedMods = new ArrayList<>();

        for (String excludedModId : excludedModIds) {
            var foundExclusion = this.modpack.getModList().getMod(excludedModId);
            if (foundExclusion.isPresent()) {
                Mod excludedMod = foundExclusion.get();
                collectDependencies(excludedMod, excludedMods);
            } else {
                System.out.println("Couldn't find mod '" + excludedModId + "' in the mod list.");
            }
        }

        return excludedMods;
    }

    private void collectDependencies(Mod mod, List<Mod> excludedMods) {
        if (!excludedMods.contains(mod)) {
            excludedMods.add(mod);
            for (String dependencyId : mod.dependencies()) {
                var foundMod = this.modpack.getModList().getMod(dependencyId);
                foundMod.ifPresent(depMod -> collectDependencies(depMod, excludedMods));
            }
        }
    }

    public void run() {
        enableAllMods();
        List<Mod> disabledMods = disableHalfOfEnabledMods();
        System.out.println("Disabled half of the mods");
        boolean problemOccurred = askYesOrNo("Are you still experiencing the issue?");

        if (problemOccurred) {
            suspects.removeAll(disabledMods);
        } else {
            suspects = new ArrayList<>(disabledMods);
        }

        int tries = checkSuspects(1, suspects.size());
        System.out.println("Took " + tries + " tries");
    }

    public int checkSuspects(int tries, int lastSuspectSize) {
        enableMods(suspects);

        List<Mod> disabledSuspects = disableHalfOfSuspects();
        System.out.println("Disabled half of the suspects");
        boolean problemOccurredAgain = askYesOrNo("Are you still experiencing the issue?");

        if (problemOccurredAgain) {
            suspects.removeAll(disabledSuspects);
        } else {
            suspects = new ArrayList<>(disabledSuspects);
        }

        if (!suspects.isEmpty() && lastSuspectSize != suspects.size()) {
            return checkSuspects(tries + 1, suspects.size());
        } else {
            System.out.println("Mods causing the problem: ");
            disabledSuspects.forEach(s -> System.out.println("- " + s.fileName()));
            return tries;
        }
    }

    public boolean askYesOrNo(String question) {
        System.out.println(question);
        String answer = scanner.nextLine();
        while (!(answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y") ||
                answer.equalsIgnoreCase("true") || answer.equalsIgnoreCase("no") ||
                answer.equalsIgnoreCase("n") || answer.equalsIgnoreCase("false"))) {
            System.out.println("Please answer 'yes' or 'no'.");
            answer = scanner.nextLine();
        }
        return answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y") ||
                answer.equalsIgnoreCase("true");
    }

    private List<Mod> enableWithDependencies(Mod mod) {
        return enableWithDependencies(mod, new HashSet<>());
    }

    private List<Mod> enableWithDependencies(Mod mod, Set<Mod> visitedMods) {
        if (isEnabled(mod) || visitedMods.contains(mod)) return new ArrayList<>();
        visitedMods.add(mod);

        List<Mod> enabledMods = new ArrayList<>();
        for (String dependencyId : mod.dependencies()) {
            var foundMod = this.modpack.getModList().getMod(dependencyId);
            foundMod.ifPresent(value -> enabledMods.addAll(enableWithDependencies(value, visitedMods)));
        }

        enable(mod);
        enabledMods.add(mod);
        return enabledMods;
    }

    private List<Mod> disableHalfOfSuspects() {
        return disableHalfOfMods(suspects);
    }

    private List<Mod> disableHalfOfEnabledMods() {
        return disableHalfOfMods(enabledMods);
    }

    private List<Mod> disableHalfOfMods(List<Mod> mods) {
        int amount = mods.size();
        if (amount == 0) return new ArrayList<>();

        int target = Math.round(amount / 2f);
        List<Mod> disabledThisRun = new ArrayList<>();

        for (int i = 0; i < target && i < mods.size(); i++) {
            disabledThisRun.addAll(disableWithDependenciesAndDependents(mods.get(i)));
        }

        return disabledThisRun;
    }

    private List<Mod> disableWithDependenciesAndDependents(Mod mod) {
        return disableWithDependenciesAndDependents(mod, new HashSet<>());
    }

    private List<Mod> disableWithDependenciesAndDependents(Mod mod, Set<Mod> visitedMods) {
        if (isDisabled(mod) || visitedMods.contains(mod)) return new ArrayList<>();
        visitedMods.add(mod);

        List<Mod> disabledMods = new ArrayList<>();
        for (String dependentID : mod.dependents()) {
            var foundMod = this.modpack.getModList().getMod(dependentID);
            foundMod.ifPresent(value -> disabledMods.addAll(disableWithDependenciesAndDependents(value, visitedMods)));
        }

        for (String dependencyID : mod.dependencies()) {
            var foundMod = this.modpack.getModList().getMod(dependencyID);
            foundMod.ifPresent(value -> disabledMods.addAll(disableWithDependenciesAndDependents(value, visitedMods)));
        }

        disable(mod);
        disabledMods.add(mod);
        return disabledMods;
    }

    private boolean isDisabled(Mod mod) {
        return this.disabledMods.contains(mod);
    }

    private boolean isEnabled(Mod mod) {
        return this.enabledMods.contains(mod);
    }

    private void enable(Mod mod) {
        if (isDisabled(mod)) {
            this.modpack.enableMod(mod);
            this.enabledMods.add(mod);
            this.disabledMods.remove(mod);
        }
    }

    private void disable(Mod mod) {
        if (isEnabled(mod)) {
            this.modpack.disableMod(mod);
            this.disabledMods.add(mod);
            this.enabledMods.remove(mod);
        }
    }

    private void enableMods(List<Mod> mods) {
        mods.forEach(this::enable);
    }

    private void enableAllMods() {
        this.modpack.getModList().allMods().forEach(this.modpack::enableMod);
    }
}
