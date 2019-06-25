package org.vadere.manager.traci.commandHandler;

import org.vadere.manager.RemoteManager;
import org.vadere.manager.traci.TraCICmd;
import org.vadere.manager.traci.TraCIDataType;
import org.vadere.manager.traci.commandHandler.annotation.PersonHandler;
import org.vadere.manager.traci.commandHandler.annotation.PersonHandlers;
import org.vadere.manager.traci.commandHandler.variables.PersonVar;
import org.vadere.manager.traci.commands.TraCICommand;
import org.vadere.manager.traci.commands.TraCIGetCommand;
import org.vadere.manager.traci.commands.TraCISetCommand;
import org.vadere.manager.traci.respons.TraCIGetResponse;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.util.geometry.Vector3D;
import org.vadere.util.logging.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Handel GET/SET {@link org.vadere.manager.traci.commands.TraCICommand}s for the Person API
 */
public class PersonCommandHandler extends CommandHandler<PersonVar>{

	private static Logger logger = Logger.getLogger(PersonCommandHandler.class);

	public static PersonCommandHandler instance;

	static {
		instance = new PersonCommandHandler();
	}

	private PersonCommandHandler(){
		super();
		init(PersonHandler.class, PersonHandlers.class);
	}

	@Override
	protected void init_HandlerSingle(Method m) {
		PersonHandler an = m.getAnnotation(PersonHandler.class);
		putHandler(an.cmd(), an.var(), m);
	}

	@Override
	protected void init_HandlerMult(Method m) {
		PersonHandler[] ans = m.getAnnotation(PersonHandlers.class).value();
		for(PersonHandler a : ans){
			putHandler(a.cmd(), a.var(), m);
		}
	}


	public static void main(String[] arg){
		PersonCommandHandler h = instance;

	}

	public TraCIGetResponse responseOK(TraCIDataType responseDataType, Object responseData){
		return  responseOK(responseDataType, responseData, TraCICmd.GET_PERSON_VALUE, TraCICmd.RESPONSE_GET_PERSON_VALUE);
	}

	public TraCIGetResponse responseERR(String err){
		return responseERR(err, TraCICmd.GET_PERSON_VALUE, TraCICmd.RESPONSE_GET_PERSON_VALUE);
	}

	public boolean checkIfPedestrianExists(Pedestrian ped, TraCIGetCommand cmd){
		if (ped == null) {
			cmd.setResponse(responseERR(CommandHandler.ELEMENT_ID_NOT_FOUND));
			logger.debugf("Pedestrian: %s not found.", cmd.getElementIdentifier());
			return false;
		}
		return true;
	}

	///////////////////////////// Handler /////////////////////////////

	@PersonHandler(cmd = TraCICmd.GET_PERSON_VALUE, var = PersonVar.ID_LIST, name = "getIDList")
	public TraCICommand process_getIDList(TraCIGetCommand cmd, RemoteManager remoteManager){
		// elementIdentifier ignored.
		remoteManager.accessState((manager, state) -> {
			List<String> data = state.getTopography().getPedestrianDynamicElements()
					.getElements()
					.stream()
					.map(p -> Integer.toString(p.getId()))
					.collect(Collectors.toList());
			TraCIGetResponse res = responseOK(PersonVar.ID_LIST.type, data);
			cmd.setResponse(res);
			logger.debugf("time: %f ID's: %s", state.getSimTimeInSec(), Arrays.toString(data.toArray(String[]::new)));
		});

		return cmd;
	}

	@PersonHandler(cmd = TraCICmd.GET_PERSON_VALUE, var = PersonVar.COUNT, name = "getIDCount")
	public TraCICommand process_getIDCount(TraCIGetCommand cmd, RemoteManager remoteManager){

		remoteManager.accessState((manager, state) -> {
			int numPeds = state.getTopography().getPedestrianDynamicElements().getElements().size();
			cmd.setResponse(responseOK(PersonVar.COUNT.type, numPeds));
		});

		return cmd;

	}

	@PersonHandler( cmd = TraCICmd.GET_PERSON_VALUE, var = PersonVar.SPEED, name = "getSpeed")
	public TraCICommand process_getSpeed(TraCIGetCommand cmd, RemoteManager remoteManager){

		remoteManager.accessState((manager, state) -> {
			Pedestrian ped = state.getTopography().getPedestrianDynamicElements()
					.getElement(Integer.parseInt(cmd.getElementIdentifier()));

			if (checkIfPedestrianExists(ped, cmd))
				cmd.setResponse(responseOK(PersonVar.SPEED.type, ped.getVelocity().getLength()));

		});

		return cmd;
	}

	@PersonHandler(cmd = TraCICmd.GET_PERSON_VALUE, var = PersonVar.POS_2D, name = "getPosition2D")
	public TraCICommand process_getPosition(TraCIGetCommand cmd, RemoteManager remoteManager){

		remoteManager.accessState((manager, state) -> {
			Pedestrian ped = state.getTopography().getPedestrianDynamicElements()
					.getElement(Integer.parseInt(cmd.getElementIdentifier()));

			if (checkIfPedestrianExists(ped, cmd)) {
				cmd.setResponse(responseOK(PersonVar.POS_2D.type, ped.getPosition()));
				logger.debugf("time: %f Pedestrian: %s Position: %s",
						state.getSimTimeInSec(),
						cmd.getElementIdentifier(),
						ped.getPosition().toString());
			}
		});

		return cmd;
	}

	@PersonHandler(cmd = TraCICmd.GET_PERSON_VALUE, var = PersonVar.POS_3D, name = "getPosition3D")
	public TraCICommand process_getPosition3D(TraCIGetCommand cmd, RemoteManager remoteManager){

		remoteManager.accessState((manager, state) -> {
			Pedestrian ped = state.getTopography().getPedestrianDynamicElements()
					.getElement(Integer.parseInt(cmd.getElementIdentifier()));

			if (checkIfPedestrianExists(ped, cmd)) {
				cmd.setResponse(responseOK(PersonVar.POS_3D.type, new Vector3D(ped.getPosition().x, ped.getPosition().y, 0.0)));
				logger.debugf("time: %f Pedestrian: %s Position: %s",
						state.getSimTimeInSec(),
						cmd.getElementIdentifier(),
						ped.getPosition().toString());
			}
		});

		return cmd;
	}


	@PersonHandler( cmd = TraCICmd.GET_PERSON_VALUE, var = PersonVar.LENGTH, name = "getLength")
	public TraCICommand process_getLength(TraCIGetCommand cmd, RemoteManager remoteManager){

		remoteManager.accessState((manager, state) -> {
			Pedestrian ped = state.getTopography().getPedestrianDynamicElements()
					.getElement(Integer.parseInt(cmd.getElementIdentifier()));

			if (checkIfPedestrianExists(ped, cmd))
				cmd.setResponse(responseOK(PersonVar.LENGTH.type, ped.getRadius()*2));
		});

		return cmd;
	}


	@PersonHandler( cmd = TraCICmd.GET_PERSON_VALUE, var = PersonVar.WIDTH, name = "getWidth")
	public TraCICommand process_getWidth(TraCIGetCommand cmd, RemoteManager remoteManager){

		remoteManager.accessState((manager, state) -> {
			Pedestrian ped = state.getTopography().getPedestrianDynamicElements()
					.getElement(Integer.parseInt(cmd.getElementIdentifier()));

			if (checkIfPedestrianExists(ped, cmd))
				cmd.setResponse(responseOK(PersonVar.WIDTH.type, ped.getRadius()*2));
		});

		return cmd;
	}

	@PersonHandler( cmd = TraCICmd.GET_PERSON_VALUE, var = PersonVar.ROAD_ID, name = "getRoadId")
	public TraCICommand process_getRoadId(TraCIGetCommand cmd, RemoteManager remoteManager) {
		// return dummy value
		cmd.setResponse(responseOK(PersonVar.ROAD_ID.type, "road000"));
		return cmd;
	}

	@PersonHandler(	cmd = TraCICmd.GET_PERSON_VALUE, var = PersonVar.ANGLE, name = "getAngle" )
	public TraCICommand process_getAngle(TraCIGetCommand cmd, RemoteManager remoteManager) {
		// return dummy value
		cmd.setResponse(responseOK(PersonVar.ANGLE.type, 0.0));
		return cmd;
	}

	@PersonHandler(cmd = TraCICmd.GET_PERSON_VALUE,	var = PersonVar.TYPE, name = "getType" )
	public TraCICommand process_getType(TraCIGetCommand cmd, RemoteManager remoteManager) {
		// return dummy value
		cmd.setResponse(responseOK(PersonVar.TYPE.type, "pedestrian"));
		return cmd;
	}


	@PersonHandler(cmd=TraCICmd.GET_PERSON_VALUE, var= PersonVar.WAITING_TIME, name="getWaitingTime")
	@PersonHandler(cmd=TraCICmd.GET_PERSON_VALUE, var= PersonVar.COLOR, name="getColor")
	@PersonHandler(cmd=TraCICmd.GET_PERSON_VALUE, var= PersonVar.EDGE_POS, name="getEdgePos")
	@PersonHandler(cmd=TraCICmd.GET_PERSON_VALUE, var= PersonVar.MIN_GAP, name="getMinGap")
	@PersonHandler(cmd=TraCICmd.GET_PERSON_VALUE, var= PersonVar.NEXT_EDGE, name="getNextEdge")
	@PersonHandler(cmd=TraCICmd.GET_PERSON_VALUE, var= PersonVar.REMAINING_STAGES, name="getRemainingStages")
	@PersonHandler(cmd=TraCICmd.GET_PERSON_VALUE, var= PersonVar.VEHICLE, name="getVehicle")
	public TraCICommand process_NotImplemented(TraCIGetCommand cmd, RemoteManager remoteManager){
		return super.process_NotImplemented(cmd, remoteManager);
	}



	public TraCICommand processValueSub(TraCICommand rawCmd, RemoteManager remoteManager){
		return processValueSub(rawCmd, remoteManager, this::processGet,
				TraCICmd.GET_PERSON_VALUE, TraCICmd.RESPONSE_SUB_PERSON_VARIABLE);
	}

	public TraCICommand processGet(TraCICommand cmd, RemoteManager remoteManager){
		TraCIGetCommand getCmd = (TraCIGetCommand) cmd;

		PersonVar var = PersonVar.fromId(getCmd.getVariableIdentifier());
		Method m = getHandler(getCmd.getTraCICmd(), var);

		return invokeHandler(m, this, getCmd, remoteManager);
	}

	public TraCICommand processSet(TraCICommand cmd, RemoteManager remoteManager){
		TraCISetCommand setCmd = (TraCISetCommand) cmd;

		PersonVar var = PersonVar.fromId(setCmd.getVariableId());
		Method m = getHandler(setCmd.getTraCICmd(), var);

		return invokeHandler(m, this, setCmd, remoteManager);
	}


}