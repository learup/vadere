package org.vadere.manager.traci.commands;

import org.vadere.manager.traci.TraCICmd;
import org.vadere.manager.traci.TraCIDataType;
import org.vadere.manager.traci.commandHandler.variables.PersonVar;
import org.vadere.manager.traci.reader.TraCICommandBuffer;
import org.vadere.manager.traci.writer.TraCIPacket;

/**
 * Sub class of {@link TraCICommand} which represents a set request to some API.
 *
 * An API in this context is for instance the Person(GET/SET), Simulation(GET/SET/SUB)
 *
 * Command Structure
 *
 * [ cmdIdentifier(based on API) ] [ variableId ] [ elementId] [ dataTypeId ] [ data ]
 *
 * - cmdIdentifier(based on API): see {@link TraCICmd} enum GET_****
 * - variableId: Id for the var. The numbers may be the same between different APIs
 *   see {@link PersonVar} enum
 * - elementId: String based id for the object (i.e. a pedestrianId)
 * - dataTypeId: see {@link TraCIDataType}
 * - data: data to be returned.
 *
 * see {@link org.vadere.manager.traci.commandHandler.PersonCommandHandler} for execution handing.
 *
 */
public class TraCISetCommand extends TraCICommand{

	protected int variableId;
	protected String elementId;
	protected TraCIDataType returnDataType;
	protected Object variableValue;


	public TraCISetCommand(TraCICmd traCICmd, TraCICommandBuffer cmdBuffer) {
		super(traCICmd);
		variableId = cmdBuffer.readUnsignedByte();
		elementId = cmdBuffer.readString();
		returnDataType = TraCIDataType.fromId(cmdBuffer.readUnsignedByte());
		variableValue = cmdBuffer.readTypeValue(returnDataType);

	}

	public Object getVariableValue(){
		return variableValue;
	}

	public int getVariableId() {
		return variableId;
	}

	public String getElementId() {
		return elementId;
	}

	public TraCIDataType getReturnDataType() {
		return returnDataType;
	}

	@Override
	public TraCIPacket buildResponsePacket() {
		return TraCIPacket.create().add_OK_StatusResponse(traCICmd);
	}
}