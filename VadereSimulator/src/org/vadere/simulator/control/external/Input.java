package org.vadere.simulator.control.external;


import org.json.*;
import org.vadere.simulator.control.psychology.cognition.models.ICognitionModel;
import org.vadere.util.reflection.DynamicClassInstantiator;

public class Input {

    public static final String MODEL_CONTAINER = ".models.";


    public static IControlModel getModel(CtlCommand currentCommand) {

        String ClassName = currentCommand.getModelName();
        String classSearchPath = IControlModel.class.getPackageName();
        String fullyQualifiedClassName = classSearchPath + MODEL_CONTAINER + ClassName;
        DynamicClassInstantiator<ICognitionModel> instantiator = new DynamicClassInstantiator<>();

        IControlModel controlModel =  (IControlModel) instantiator.createObject(fullyQualifiedClassName);
        controlModel.addRawCommand(currentCommand);
        return controlModel;
    }


}