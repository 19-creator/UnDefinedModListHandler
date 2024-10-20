package com.undefined;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Mod implements Serializable {
    private String name;
    private String modid;
    private String description;
    private LogicalSide side;

    private String originalFileName;

    private List<String> dependents;
    private List<String> dependencies;
    private final boolean isApi;

    // Constructor
    public Mod(String fileName, String name, String modid, LogicalSide side, String description, boolean isApi, List<String> dependents, List<String> dependencies, ModList modlist) {
        this.originalFileName = fileName;
        this.name = name;
        this.side = side;
        this.modid = modid;
        this.description = description;
        this.dependents = (dependents != null && !dependents.isEmpty()) ? new ArrayList<>(dependents) : new ArrayList<>();
        this.dependencies = (dependencies != null && !dependencies.isEmpty()) ? new ArrayList<>(dependencies) : new ArrayList<>();
        this.isApi = isApi;
    }

    public void initializeDependenciesAndDependents(ModList modlist) {
        if(!dependents.isEmpty()) {
            List<String> dependentsCopy = new ArrayList<>(dependents);
            dependents = new ArrayList<>();

            for (String dependent : dependentsCopy) {
                this.addDependent(dependent, modlist);
            }
        }

        if(!dependencies.isEmpty()) {
            List<String> dependenciesCopy = new ArrayList<>(dependencies);
            dependencies = new ArrayList<>();

            for (String dependency : dependenciesCopy) {
                this.addDependency(dependency, modlist);
            }
        }
    }

    public LogicalSide getSide() {
        return this.side;
    }

    public String fileName() {
        return this.originalFileName;
    }


    public String name() {
        return name;
    }

    public String modid() {
        return modid;
    }

    public String nameWithId() {
        return "'" + name() + "' : '" + modid() + "'";
    }

    public String description() {
        return description;
    }

    public List<String> dependents() {
        return new ArrayList<>(dependents); // Return a copy to maintain encapsulation
    }

    public List<String> dependencies() {
        return new ArrayList<>(dependencies); // Return a copy to maintain encapsulation
    }

    public boolean isApi() {
        return isApi;
    }

    public void addDependent(String modId, ModList modList) {
        if(!modList.containsMod(modId)) {
            System.out.println("Failed to add dependent '" + modId  +"' Dependent not found in mod list.");
            return;
        }

        if (dependents.contains(modId)) return;
        dependents.add(modId);
        Optional<Mod> modThatIsDependingOnUs = modList.getMod(modId);

        if (modThatIsDependingOnUs.isPresent()) {
            if(!modThatIsDependingOnUs.get().dependencies.contains(this.modid)) {
                modThatIsDependingOnUs.get().addDependency(this.modid, modList);
            }
        }
    }

    public void addDependency(String modId, ModList modList) {
        if(!modList.containsMod(modId)) {
            System.out.println("Failed to add dependency '" + modId  +"' Dependency not found in mod list.");
            return;
        }

        if (dependencies.contains(modId)) return;
        dependencies.add(modId);
        Optional<Mod> modWeAreDependingOn = modList.getMod(modId);
        if (modWeAreDependingOn.isPresent()) {
            if(!modWeAreDependingOn.get().dependents.contains(this.modid)) {
                modWeAreDependingOn.get().addDependent(this.modid, modList);
            }
        }
    }

    public void removeDependent(String modId, ModList modList) {
        if(!dependents.contains(modId)) {
            System.out.println("Failed to remove dependent '" + modId  +"' Dependent not found in mod list.");
            return;
        }
        dependents.remove(modId);

        Optional<Mod> modThatIsDependingOnUs = modList.getMod(modId);
        modThatIsDependingOnUs.ifPresent(mod -> mod.dependencies.remove(this.modid));
    }

    public void removeDependency(String modId, ModList modList) {
        if(!dependencies.contains(modId)) {
            System.out.println("Failed to remove dependency '" + modId  +"' Dependency not found in mod list.");
            return;
        }
        dependencies.remove(modId);

        Optional<Mod> modWeAreDependingOn = modList.getMod(modId);
        modWeAreDependingOn.ifPresent(mod -> mod.dependents.remove(this.modid));
    }


    @Override
    public String toString() {
        return "Mod{" +
                "name='" + name + '\'' +
                ", modid='" + modid + '\'' +
                ", description='" + description + '\'' +
                ", dependents=" + dependents.size() +
                ", dependencies=" + dependencies.size() +
                ", isApi=" + isApi +
                '}';
    }


}
