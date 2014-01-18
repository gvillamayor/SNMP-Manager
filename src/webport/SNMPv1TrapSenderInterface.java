package webport;

import java.io.*;
import java.math.*;
import java.net.*;
import java.util.*;


/**
*	The class SNMPv1TrapSenderInterface implements an interface for sending trap messages to a remote SNMP manager.
*	The approach is that from version 1 of SNMP, using no encryption of data. Communication occurs
*	via UDP, using port 162, the standard SNMP trap port, as the destination port.
*/

public class SNMPv1TrapSenderInterface
{
	public static final int SNMP_TRAP_PORT = 162;
	
	// largest size for datagram packet payload; based on
	// RFC 1157, need to handle messages of at least 484 bytes
	public static final int MAXSIZE = 512;
	
	private DatagramSocket dSocket;
	
	
	/**
	*	Construct a new trap sender object to send traps to remote SNMP hosts.
	*/
	
	public SNMPv1TrapSenderInterface()
		throws SocketException
	{
		dSocket = new DatagramSocket();
	}
	

	
	/**
	*	Construct a new trap sender object to send traps to remote SNMP hosts, binding to
	*   the specified local port.
	*/
	
	public SNMPv1TrapSenderInterface(int localPort)
		throws SocketException
	{
		dSocket = new DatagramSocket(localPort);
	}
	

	
	/**
	*	Send the supplied trap pdu to the specified host, using the supplied version number
	*	and community name. Use version = 0 for SNMP version 1, or version = 1 for enhanced 
	*	capabilities provided through RFC 1157.
	*/
	
	public void sendTrap(int version, InetAddress hostAddress, String community, SNMPTrapPDU pdu)
		throws IOException
	{
		SNMPMessage message = new SNMPMessage(version, community, pdu);
		
		byte[] messageEncoding = message.getBEREncoding();
		
		/*
		System.out.println("Request Message bytes:");
		
		for (int i = 0; i < messageEncoding.length; ++i)
			System.out.print(hexByte(messageEncoding[i]) + " ");
		*/
		
		DatagramPacket outPacket = new DatagramPacket(messageEncoding, messageEncoding.length, hostAddress, SNMP_TRAP_PORT);
		
		/*
		System.out.println("Message bytes length (out): " + outPacket.getLength());
		
		System.out.println("Message bytes (out):");
		for (int i = 0; i < messageEncoding.length; ++i)
		{
			System.out.print(hexByte(messageEncoding[i]) + " ");
		}
		System.out.println("\n");
		*/
		
		dSocket.send(outPacket);
		
	}
	
	
	
	/**
	*	Send the supplied trap pdu to the specified host, using the supplied community name and
	*	using 0 for the version field in the SNMP message (corresponding to SNMP version 1).
	*/
	
	public void sendTrap(InetAddress hostAddress, String community, SNMPTrapPDU pdu)
		throws IOException
	{
		int version = 0;
	
		sendTrap(version, hostAddress, community, pdu);
	}
	
	
	
	private String hexByte(byte b)
	{
		int pos = b;
		if (pos < 0)
			pos += 256;
		String returnString = new String();
		returnString += Integer.toHexString(pos/16);
		returnString += Integer.toHexString(pos%16);
		return returnString;
	}
	
	
	
	
	
	
	private String getHex(byte theByte)
	{
		int b = theByte;
		
		if (b < 0)
			b += 256;
		
		String returnString = new String(Integer.toHexString(b));
		
		// add leading 0 if needed
		if (returnString.length()%2 == 1)
			returnString = "0" + returnString;
			
		return returnString;
	}
	
	
	
}