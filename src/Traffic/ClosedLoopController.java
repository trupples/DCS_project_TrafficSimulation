package Traffic;

import Components.*;
import DataObjects.DataInteger;
import DataObjects.DataTransfer;
import DataOnly.TransferOperation;
import Enumerations.LogicConnector;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

public class ClosedLoopController {

    protected static void makeSimpleTransition(PetriNet pn, String s1, String s2, String name, Object[][] lights) {
        PetriTransition T = new PetriTransition(pn);
        T.Delay = 2;
        T.TransitionName = name;
        T.InputPlaceName.add(s1);
        GuardMapping grd = new GuardMapping();
        grd.condition = new Condition(T, s1, TransitionCondition.Equal, "one");

        grd.Activations.add(new Activation(T, s1, TransitionOperation.Move, s2));

        for(Object[] light : lights) {
            String lightname = (String) light[0];
            int value = (Integer) light[1];
            grd.Activations.add(new Activation(T, value == 1 ? "one" : "zero", TransitionOperation.SendOverNetwork, lightname));
        }

            T.GuardMappingList.add(grd);
        pn.Transitions.add(T);
    }

    protected static void makeDoubleTransition(PetriNet pn, String s1, String s2, String name, String[] sensors, Object[][] lights) {
        // Long transition(s)
        for(int i = 0; i < sensors.length; i++) {
            PetriTransition Tl = new PetriTransition(pn);
            Tl.Delay = 15;
            Tl.TransitionName = name + "l" + i;
            Tl.InputPlaceName.add(s1);
            GuardMapping grd = new GuardMapping();
            grd.condition = new Condition(Tl, s1, TransitionCondition.Equal, "one");
            grd.condition.SetNextCondition(LogicConnector.AND, new Condition(Tl, sensors[i], TransitionCondition.Equal, "five"));

            grd.Activations.add(new Activation(Tl, s1, TransitionOperation.Move, s2));
            for(Object[] light : lights) {
                String lightname = (String) light[0];
                int value = (Integer) light[1];
                grd.Activations.add(new Activation(Tl, value == 1 ? "one" : "zero", TransitionOperation.SendOverNetwork, lightname));

            }

            Tl.GuardMappingList.add(grd);
            pn.Transitions.add(Tl);
        }

        // Transition short
        {
            PetriTransition Ts = new PetriTransition(pn);
            Ts.Delay = 10;
            Ts.TransitionName = name + "s";
            Ts.InputPlaceName.add(s1);
            GuardMapping grd = new GuardMapping();
            grd.condition = new Condition(Ts, s1, TransitionCondition.Equal, "one");

            Condition shortcond = new Condition(Ts, sensors[0], TransitionCondition.NotEqual, "five");
            Condition temp = shortcond;
            for (int i = 1; i < sensors.length; i++) {
                Condition next = new Condition(Ts, sensors[i], TransitionCondition.NotEqual, "five");
                temp.SetNextCondition(LogicConnector.AND, next);
                temp = next;
            }
            grd.condition.SetNextCondition(LogicConnector.AND, shortcond);

            grd.Activations.add(new Activation(Ts, s1, TransitionOperation.Move, s2));

            for(Object[] light : lights) {
                String lightname = (String) light[0];
                int value = (Integer) light[1];
                grd.Activations.add(new Activation(Ts, value == 1 ? "one" : "zero", TransitionOperation.SendOverNetwork, lightname));

            }


            Ts.GuardMappingList.add(grd);
            pn.Transitions.add(Ts);
        }
    }

    protected static DataInteger makeLightState(PetriNet pn, String name) {
        DataInteger s = new DataInteger();
        s.SetName(name);
        s.SetValue(0);
        pn.PlaceList.add(s);
        return s;
    }

    protected static void makeLight(PetriNet pn, String host, int port, String name) {
        DataTransfer p = new DataTransfer();
        p.SetName(name);
        p.Value = new TransferOperation(host, ""+port, name);
        pn.PlaceList.add(p);
    }

    protected static void makeSensorPlace(PetriNet pn, String name) {
        DataInteger p = new DataInteger();
        p.SetName(name);
        p.SetValue(0);
        pn.PlaceList.add(p);
    }
}
