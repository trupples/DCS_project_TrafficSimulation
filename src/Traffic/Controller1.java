package Traffic;

import Components.*;
import DataObjects.DataInteger;

public class Controller1 extends ClosedLoopController {
    public static void main(String[] args) {
        PetriNet pn = new PetriNet();
        pn.PetriNetName = "Controller1";
        pn.NetworkPort = 1235;

        DataInteger one = new DataInteger();
        one.SetValue(1);
        one.SetName("one");
        pn.ConstantPlaceList.add(one);
        DataInteger zero = new DataInteger();
        zero.SetValue(0);
        zero.SetName("zero");
        pn.ConstantPlaceList.add(zero);
        DataInteger five = new DataInteger();
        five.SetValue(5);
        five.SetName("five");
        pn.ConstantPlaceList.add(five);

        makeSensorPlace(pn, "CountA");
        makeSensorPlace(pn, "CountC");
        makeSensorPlace(pn, "CountE");

        makeLight(pn, "localhost", 1234, "LightA");
        makeLight(pn, "localhost", 1234, "LightC");
        makeLight(pn, "localhost", 1234, "LightE");

        DataInteger s1 = makeLightState(pn, "ArCrEr1");
        s1.SetValue(1);
        DataInteger s2 = makeLightState(pn, "ArCgEr");
        DataInteger s3 = makeLightState(pn, "ArCrEr2");
        DataInteger s4 = makeLightState(pn, "AgCrEg");

        makeDoubleTransition(pn, "ArCrEr1", "ArCgEr", "T1", new String[] { "CountC" },           new Object[][] {{"LightA", 0}, {"LightC", 1}, {"LightE", 0}});
        makeDoubleTransition(pn, "ArCrEr2", "AgCrEg", "T3", new String[] { "CountA", "CountE" }, new Object[][] {{"LightA", 1}, {"LightC", 0}, {"LightE", 1}});
        makeSimpleTransition(pn, "AgCrEg", "ArCrEr1", "T4", new Object[][] {{"LightA", 0}, {"LightC", 0}, {"LightE", 0}});
        makeSimpleTransition(pn, "ArCgEr", "ArCrEr2", "T2", new Object[][] {{"LightA", 0}, {"LightC", 0}, {"LightE", 0}});

        pn.Delay = 1000;

        PetriNetWindow frame = new PetriNetWindow(false);
        frame.petriNet = pn;
        frame.setVisible(true);
    }
}
