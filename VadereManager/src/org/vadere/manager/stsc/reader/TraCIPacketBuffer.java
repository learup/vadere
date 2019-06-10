package org.vadere.manager.stsc.reader;

import org.vadere.manager.stsc.TraCICmd;
import org.vadere.manager.stsc.respons.StatusResponse;
import org.vadere.manager.stsc.commands.TraCICommand;
import org.vadere.manager.stsc.respons.TraCIResponse;

import java.nio.ByteBuffer;

/**
 *  A simple Wrapper around a {@link TraCIReader} which knows how to retrieve *
 *  single commands from a TraCI byte[] array.
 *
 *  The class expects that the given buffer only contains commands. The packet length filed (int)
 *  must be removed before!
 *
 */
public class TraCIPacketBuffer extends TraCIByteBuffer {

	public  static TraCIPacketBuffer wrap(byte[] buf){
		return new TraCIPacketBuffer(buf);
	}

	public  static TraCIPacketBuffer wrap(ByteBuffer buf){
		return new TraCIPacketBuffer(buf);
	}

	public static TraCIPacketBuffer empty(){
		return new TraCIPacketBuffer(new byte[0]);
	}

	protected TraCIPacketBuffer(byte[] buf){
		super(buf);
	}

	protected TraCIPacketBuffer(ByteBuffer buf){
		super(buf);
	}

	public TraCICommand nextCommand(){
		if (!hasRemaining())
			return null;

		int cmdLen = getCommandDataLen();

		return TraCICommand.create(readByteBuffer(cmdLen));
	}

	public TraCIResponse nextResponse(){
		if (!hasRemaining())
			return null;

		int statusLen = getCommandDataLen();
		StatusResponse statusResponse = StatusResponse.createFromByteBuffer(readByteBuffer(statusLen));

		if (!hasRemaining()){
			// only StatusResponse
			return TraCIResponse.create(statusResponse);
		} else {
			if (statusResponse.getCmdIdentifier().equals(TraCICmd.SIM_STEP)){
				// The sim step command does follow the standard command structure.
				// After the status command follows a single int encoding the number of
				// subscription results which will follow. Thus in case of SIM_STEP
				// give all remaining data to the factory.
				return TraCIResponse.create(statusResponse, readByteBuffer(limit()));
			} else {
				int responseDataLen = getCommandDataLen();
				ByteBuffer buffer = readByteBuffer(responseDataLen);
				return TraCIResponse.create(statusResponse, buffer);
			}

		}
	}

	private int getCommandDataLen(){
		int cmdLen = readUnsignedByte();
		if (cmdLen == 0 ){
			// extended cmdLen field used.
			cmdLen = readInt() - 5; // subtract cmdLen field:  1 ubyte + 1 int (4)
		} else {
			cmdLen -= 1; // subtract cmdLen field: 1 ubyte
		}
		return cmdLen;
	}
}
