package Traffic;

import Components.PetriNet;
import Components.PetriNetWindow;
import DataObjects.DataInteger;

public class Controller2 extends ClosedLoopController {
    public static void main(String[] args) {
        PetriNet pn = new PetriNet();
        pn.PetriNetName = "Controller2";
        pn.NetworkPort = 1236;

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

        makeSensorPlace(pn, "CountL");
        makeSensorPlace(pn, "CountK");

        makeLight(pn, "localhost", 1234, "LightL");
        makeLight(pn, "localhost", 1234, "LightK");

        DataInteger s1 = makeLightState(pn, "LrKr1");
        s1.SetValue(1);
        DataInteger s2 = makeLightState(pn, "LrKg");
        DataInteger s3 = makeLightState(pn, "LrKr2");
        DataInteger s4 = makeLightState(pn, "LgKr");

        makeDoubleTransition(pn, "LrKr1", "LrKg", "T1", new String[] { "CountK" }, new Object[][] {{"LightL", 0}, {"LightK", 1}});
        makeSimpleTransition(pn, "LrKg", "LrKr2", "T2",                            new Object[][] {{"LightL", 0}, {"LightK", 0}});
        makeDoubleTransition(pn, "LrKr2", "LgKr", "T3", new String[] { "CountL" }, new Object[][] {{"LightL", 1}, {"LightK", 0}});
        makeSimpleTransition(pn, "LgKr", "LrKr1", "T4",                            new Object[][] {{"LightL", 0}, {"LightK", 0}});

        pn.Delay = 1000;

        PetriNetWindow frame = new PetriNetWindow(false);
        frame.petriNet = pn;
        frame.setVisible(true);
    }
}
