package org.vadere.manager.traci.commands;

import org.vadere.manager.TraCIException;
import org.vadere.manager.traci.CmdType;
import org.vadere.manager.traci.TraCICmd;
import org.vadere.manager.traci.writer.TraCIPacket;
import org.vadere.manager.traci.commands.control.TraCILoadCommand;
import org.vadere.manager.traci.commands.control.TraCISendFileCommand;
import org.vadere.manager.traci.reader.TraCICommandBuffer;
import org.vadere.manager.traci.commands.control.TraCICloseCommand;
import org.vadere.manager.traci.commands.control.TraCIGetVersionCommand;
import org.vadere.manager.traci.commands.control.TraCISimStepCommand;

import java.nio.ByteBuffer;

/**
 * Abstract Class for TraCICommands.
 *
 * Object of this Class only hold the state (input parameters, response data) for each
 * command. The execution is handed by {@link org.vadere.manager.traci.commandHandler.CommandHandler}
 * classes.
 *
 * Each command has an Id managed as an enum {@link TraCICmd}. This enum also contains
 * the type of the command (i.e. GET, SET, Control). Depending on the type (and sometimes on
 * the var queried) different sub classes are used to manage the command.
 *
 * Construction Methods: (compare with {@link org.vadere.manager.traci.respons.TraCIResponse})
 *
 * 1) created from serialized data (byte[] / {@link ByteBuffer} / {@link TraCICommandBuffer})
 *
 * 2) created from simple static factories which are used by clients.
 *
 */
public abstract class TraCICommand {

	protected TraCICmd traCICmd;
	protected TraCIPacket NOK_response = null;

	public static TraCICommand create(ByteBuffer rawCmd){
		TraCICommandBuffer cmdBuffer = TraCICommandBuffer.wrap(rawCmd);

		int identifier = cmdBuffer.readCmdIdentifier();
		TraCICmd cmd = TraCICmd.fromId(identifier);

		switch (cmd.type){
			case CTRL:
				return createControlCommand(cmd, cmdBuffer);
			case VALUE_GET:
				return new TraCIGetCommand(cmd, cmdBuffer);
			case VALUE_SET:
				return new TraCISetCommand(cmd, cmdBuffer);
			case VALUE_SUB:
				return new TraCIValueSubscriptionCommand(cmd, cmdBuffer);
			case CONTEXT_SUB:
				throw new TraCIException("Subscription not implement. Command: 0x%02X", cmd.id);
			default:
				throw new IllegalStateException("Should not be reached. All CmdType enums are tested in switch statement");
		}

	}

	private static TraCICommand createControlCommand(TraCICmd cmd, TraCICommandBuffer cmdBuffer){

		switch (cmd){
			case GET_VERSION:
				return new TraCIGetVersionCommand();
			case SIM_STEP:
				return new TraCISimStepCommand(cmdBuffer);
			case CLOSE:
				return new TraCICloseCommand();
			case SEND_FILE:
				return new TraCISendFileCommand(cmdBuffer);
			case LOAD:
				return new TraCILoadCommand(cmdBuffer);
			default:
				throw  new IllegalStateException(String.format("Should not be reached. Only TraCI control commands expected: %0X", cmd.id));
		}

	}


	protected TraCICommand(TraCICmd traCICmd){
		this.traCICmd = traCICmd;
	}

	public TraCICmd getTraCICmd() {
		return traCICmd;
	}

	public CmdType getCmdType(){
		return traCICmd.type;
	}


	public TraCICommand setNOK_response(TraCIPacket NOK_response) {
		this.NOK_response = NOK_response;
		return this;
	}

	public abstract TraCIPacket buildResponsePacket();
}