package de.tbodyowski.waros.AntiCheat.checks;

import de.tbodyowski.waros.AntiCheat.checks.movement.Fly;
import de.tbodyowski.waros.AntiCheat.checks.movement.NoFall;

import java.util.ArrayList;
import java.util.List;

public class CheckManager {

    private List<Check> checks = new ArrayList<>();

    public CheckManager(){
        checks.add(new Fly("Fly", CheckType.MOVEMENT, true, true, 50));
        checks.add(new NoFall("NoFall", CheckType.MOVEMENT, true, true, 20));
    }
}
