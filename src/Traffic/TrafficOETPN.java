package Traffic;

import Components.*;
import DataObjects.DataCarQueue;
import DataObjects.DataInteger;
import DataObjects.DataTransfer;
import DataOnly.TransferOperation;
import Enumerations.LogicConnector;
import Enumerations.TransitionCondition;
import Enumerations.TransitionOperation;

public class TrafficOETPN {
    public static void main(String[] args) {
        PetriNet pn = new PetriNet();
        pn.PetriNetName = "Traffic";
        pn.NetworkPort = 1234;

        for(Character placeName : ("ABCDEFGHIJKLMNOP".toCharArray())) {
            DataCarQueue place = new DataCarQueue(placeName.toString());
            pn.PlaceList.add(place);
        }

        // Create green / red constants
        DataInteger green = new DataInteger();
        green.SetName("GREEN");
        green.SetValue(1);
        pn.ConstantPlaceList.add(green);
        DataInteger red = new DataInteger();
        red.SetName("RED");
        red.SetValue(0);
        pn.ConstantPlaceList.add(red);

        // Create semaphore places -> won't be shown
        makeSemaphore(pn, "LightA");
        makeSemaphore(pn, "LightC");
        makeSemaphore(pn, "LightE");
        makeSemaphore(pn, "LightK");
        makeSemaphore(pn, "LightL");

        // T0, T11, T14 - simple "always allowed to turn" transitions
        makeSimpleTransition(pn, "A", "T0", "B", "B");
        makeSimpleTransition(pn, "K", "T11", "O", "O");
        makeSimpleTransition(pn, "L", "T14", "P", "P");

        // T4, T7, T8, T9, T12, T13 - simple "go ahead always" transitions
        makeAlwaysTransition(pn, "F", "T4", "D");
        makeAlwaysTransition(pn, "H", "T7", "J");
        makeAlwaysTransition(pn, "G", "T8", "D");
        makeAlwaysTransition(pn, "I", "T9", "K");
        makeAlwaysTransition(pn, "M", "T12", "O");
        makeAlwaysTransition(pn, "N", "T13", "P");

        // T15, T16, T17, T18, T19 - exit transitions
        makeExitTransition(pn, "D", "T15");
        makeExitTransition(pn, "J", "T16");
        makeExitTransition(pn, "O", "T17");
        makeExitTransition(pn, "B", "T18");
        makeExitTransition(pn, "P", "T19");

        // Intersection 1, starting from A
        makeSemaphoreTransition(pn, "A", "T1", "F", "LightA", new String[] { "D" }, new String[] { "G" } );
        makeSemaphoreTransition(pn, "A", "T2", "I", "LightA", new String[] { "O", "P" }, new String[] { "H" } );

        // Intersection 1, starting from E
        makeSemaphoreTransition(pn, "E", "T5", "H", "LightE", new String[] { "J" }, new String[] { "G", "I" } );

        // Intersection 1, starting from C
        makeSemaphoreTransition(pn, "C", "T3", "G", "LightC", new String[] { "O", "P" }, new String[] { "F", "H" } );

        // Intersection 2, starting from L
        makeSemaphoreTransition(pn, "L", "T10", "M", "LightL", new String[] { "O" }, new String[] { "N" });

        // Intersection 2, starting from K
        makeSemaphoreTransition(pn, "K", "T6", "N", "LightK", new String[] { "P" }, new String[] { "M" });

        // Announce car counts. Send A, C, E to controller 1 (1235) and L, K to controller 2 (1236)
        makeSensor(pn, "A", "localhost", 1235, "CountA");
        makeSensor(pn, "C", "localhost", 1235, "CountC");
        makeSensor(pn, "E", "localhost", 1235, "CountE");

        makeSensor(pn, "L", "localhost", 1236, "CountL");
        makeSensor(pn, "K", "localhost", 1236, "CountK");

        pn.Delay = 2000;

        PetriNetWindow frame = new PetriNetWindow(false);
        frame.petriNet = pn;
        frame.setVisible(true);
    }

    static void makeSemaphore(PetriNet pn, String semaphoreName) {
        DataInteger sem = new DataInteger();
        sem.SetValue(0);
        sem.SetName(semaphoreName);
        pn.PlaceList.add(sem);
    }

    // Transitions from a place to another, based on a traffic light, list of destinations, list of conflicting places
    static void makeSemaphoreTransition(PetriNet pn, String fromPlace, String transitionName, String toPlace, String semaphoreName, String[] destinations, String[] badPlaces) {
        PetriTransition T = new PetriTransition(pn);
        T.TransitionName = transitionName;
        T.InputPlaceName.add(fromPlace);
        GuardMapping grd = new GuardMapping();

        Condition semaphoreGreenCondition = new Condition(T, semaphoreName, TransitionCondition.Equal, "GREEN");

        Condition intersectionFree = new Condition(T, badPlaces[0], TransitionCondition.HasNoCar);
        Condition canEnterNext = new Condition(T, toPlace, TransitionCondition.CanAddCars);
        semaphoreGreenCondition.SetNextCondition(LogicConnector.AND, canEnterNext);
        canEnterNext.SetNextCondition(LogicConnector.AND, intersectionFree);
        Condition ifc = intersectionFree;
        for(int i = 1; i < badPlaces.length; i++) {
            Condition next = new Condition(T, badPlaces[i], TransitionCondition.HasNoCar);
            ifc.SetNextCondition(LogicConnector.AND, next);
            ifc = next;
        }
        Condition destinationCondition = new Condition(T, fromPlace, TransitionCondition.NextCarGoesTo, destinations[0]);
        ifc.SetNextCondition(LogicConnector.OR, destinationCondition);
        Condition dc = destinationCondition;
        for(int i = 1; i < destinations.length; i++) {
            Condition next = new Condition(T, fromPlace, TransitionCondition.NextCarGoesTo, destinations[i]);
            dc.SetNextCondition(LogicConnector.OR, next);
            dc = next;
        }

        grd.condition = semaphoreGreenCondition;
        grd.Activations.add(new Activation(T, fromPlace, TransitionOperation.PopElementToQueue, toPlace));
        T.GuardMappingList.add(grd);
        pn.Transitions.add(T);
    }

    // Transitions that take cars out of the system
    static void makeExitTransition(PetriNet pn, String fromPlace, String transitionName) {
        PetriTransition T = new PetriTransition(pn);
        T.TransitionName = transitionName;
        T.InputPlaceName.add(fromPlace);
        GuardMapping grdT = new GuardMapping();
        grdT.condition = new Condition(T, fromPlace, TransitionCondition.HaveCar);
        grdT.Activations.add(new Activation(T, fromPlace, TransitionOperation.PopElement, ""));
        T.GuardMappingList.add(grdT);
        pn.Transitions.add(T);
    }

    // Transitions with no restrictions other than cars existing
    static void makeSimpleTransition(PetriNet pn, String fromPlace, String transitionName, String toPlace, String destination) {
        PetriTransition T = new PetriTransition(pn);
        T.TransitionName = transitionName;
        T.InputPlaceName.add(fromPlace);
        GuardMapping grdT = new GuardMapping();

        Condition c1 = new Condition(T, fromPlace, TransitionCondition.HaveCar);
        Condition c2 = new Condition(T, toPlace, TransitionCondition.CanAddCars);
        Condition c3 = new Condition(T, fromPlace, TransitionCondition.NextCarGoesTo, destination);
        c1.SetNextCondition(LogicConnector.AND, c2);
        c2.SetNextCondition(LogicConnector.AND, c3);
        grdT.condition = c1;

        grdT.Activations.add(new Activation(T, fromPlace, TransitionOperation.PopElementToQueue, toPlace));
        T.GuardMappingList.add(grdT);
        pn.Transitions.add(T);
    }

    // Transitions with no restrictions other than cars existing
    static void makeAlwaysTransition(PetriNet pn, String fromPlace, String transitionName, String toPlace) {
        PetriTransition T = new PetriTransition(pn);
        T.TransitionName = transitionName;
        T.InputPlaceName.add(fromPlace);
        GuardMapping grdT = new GuardMapping();

        Condition c1 = new Condition(T, fromPlace, TransitionCondition.HaveCar);
        Condition c2 = new Condition(T, toPlace, TransitionCondition.CanAddCars);
        c1.SetNextCondition(LogicConnector.AND, c2);
        grdT.condition = c1;

        grdT.Activations.add(new Activation(T, fromPlace, TransitionOperation.PopElementToQueue, toPlace));
        T.GuardMappingList.add(grdT);
        pn.Transitions.add(T);
    }

    static void makeSensor(PetriNet pn, String place, String host, int port, String remotePlaceName) { // Always transmit
        DataTransfer p = new DataTransfer();
        p.SetName("po_" + place);
        p.Value = new TransferOperation(host, ""+port, remotePlaceName);
        pn.PlaceList.add(p);

        PetriTransition T = new PetriTransition(pn);
        T.TransitionName = "to_" + place;
        T.InputPlaceName.add("po_" + place);
        GuardMapping grd = new GuardMapping();

        grd.condition = new Condition(T, place, TransitionCondition.NotNull);
        grd.Activations.add(new Activation(T, place, TransitionOperation.SendOverNetwork, "po_" + place));
        T.GuardMappingList.add(grd);

        pn.Transitions.add(T);
    }
}
