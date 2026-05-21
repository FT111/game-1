package resources.levels.level_1;

import resources.LevelContext;
import resources.LevelSection;

import java.util.List;

public class Level1 {
    private final List<LevelSection> sections = List.of(
            new Level1Layers(),
            new Level1Player(),
            new Level1Guards(),
            new Level1DoorsAndItems(),
            new Level1SystemSection(),
            new Level1Objective()
    );

    public void run(LevelContext context) {
        for (LevelSection section : sections) {
            section.run(context);
        }
    }
}
