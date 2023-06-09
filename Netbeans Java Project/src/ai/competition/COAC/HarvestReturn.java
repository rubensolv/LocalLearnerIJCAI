/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.competition.COAC;

import ai.abstraction.AbstractAction;
import ai.abstraction.pathfinding.PathFinding;
import rts.GameState;
import rts.PhysicalGameState;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import util.XMLWriter;

/**
 * Modified by @Coac to return the resource despite the target resource is null
 *
 * @author santi
 */
public class HarvestReturn extends AbstractAction {
    Unit target;
    Unit base;
    Unit unit;
    PathFinding pf;

    public HarvestReturn(Unit u, Unit a_target, Unit a_base, PathFinding a_pf) {
        super(u);
        target = a_target;
        unit = u;
        base = a_base;
        pf = a_pf;
    }


    public Unit getTarget() {
        return target;
    }


    public Unit getBase() {
        return base;
    }


    public boolean completed(GameState gs) {
        return !gs.getPhysicalGameState().getUnits().contains(target) && (unit.getResources() == 0);
    }


    public boolean equals(Object o) {
        if (!(o instanceof HarvestReturn)) return false;
        HarvestReturn a = (HarvestReturn) o;
        return target.getID() == a.target.getID() && base.getID() == a.base.getID()
                && pf.getClass() == a.pf.getClass();
    }


    public void toxml(XMLWriter w) {
        w.tagWithAttributes("Harvest", "unitID=\"" + unit.getID() + "\" target=\"" + target.getID() + "\" base=\"" + base.getID() + "\" pathfinding=\"" + pf.getClass().getSimpleName() + "\"");
        w.tag("/Harvest");
    }

    public UnitAction execute(GameState gs, ResourceUsage ru) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        if (unit.getResources() == 0) {
            // go get resources:
//            System.out.println("findPathToAdjacentPosition from Harvest: (" + target.getX() + "," + target.getY() + ")");
            UnitAction move = pf.findPathToAdjacentPosition(unit, target.getX() + target.getY() * gs.getPhysicalGameState().getWidth(), gs, ru);
            if (move != null) {
                if (gs.isUnitActionAllowed(unit, move)) return move;
//                System.out.println(unit + " no path to resource");
                return null;
            }

            // harvest:
            if (target.getX() == unit.getX() &&
                    target.getY() == unit.getY() - 1)
                return new UnitAction(UnitAction.TYPE_HARVEST, UnitAction.DIRECTION_UP);
            if (target.getX() == unit.getX() + 1 &&
                    target.getY() == unit.getY())
                return new UnitAction(UnitAction.TYPE_HARVEST, UnitAction.DIRECTION_RIGHT);
            if (target.getX() == unit.getX() &&
                    target.getY() == unit.getY() + 1)
                return new UnitAction(UnitAction.TYPE_HARVEST, UnitAction.DIRECTION_DOWN);
            if (target.getX() == unit.getX() - 1 &&
                    target.getY() == unit.getY())
                return new UnitAction(UnitAction.TYPE_HARVEST, UnitAction.DIRECTION_LEFT);
        } else {
            // return resources:
//            System.out.println("findPathToAdjacentPosition from Return: (" + target.getX() + "," + target.getY() + ")");
            UnitAction move = pf.findPathToAdjacentPosition(unit, base.getX() + base.getY() * gs.getPhysicalGameState().getWidth(), gs, ru);
            if (move != null) {
                if (gs.isUnitActionAllowed(unit, move)) return move;
                return null;
            }

            // harvest:
            if (base.getX() == unit.getX() &&
                    base.getY() == unit.getY() - 1)
                return new UnitAction(UnitAction.TYPE_RETURN, UnitAction.DIRECTION_UP);
            if (base.getX() == unit.getX() + 1 &&
                    base.getY() == unit.getY())
                return new UnitAction(UnitAction.TYPE_RETURN, UnitAction.DIRECTION_RIGHT);
            if (base.getX() == unit.getX() &&
                    base.getY() == unit.getY() + 1)
                return new UnitAction(UnitAction.TYPE_RETURN, UnitAction.DIRECTION_DOWN);
            if (base.getX() == unit.getX() - 1 &&
                    base.getY() == unit.getY())
                return new UnitAction(UnitAction.TYPE_RETURN, UnitAction.DIRECTION_LEFT);
        }

//        System.out.println(unit + " harvester doing nothing");

        return null;
    }
}
