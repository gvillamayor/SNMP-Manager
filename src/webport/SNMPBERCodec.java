package webport;


import java.util.*;
import java.io.*;


/** 
*	SNMPBERCodec defines methods for converting from ASN.1 BER encoding to SNMPObject subclasses. The extraction
* 	process usually produces a tree structure of objects with an SNMPSequence object at the root; this
* 	is the usual behavior when a received encoded message is received from an SNMP device.
*/



public class SNMPBERCodec
{
	
	public static final byte SNMPINTEGER = 0x02;
	public static final byte SNMPBITSTRING = 0x03;
	public static final byte SNMPOCTETSTRING = 0x04;
	public static final byte SNMPNULL = 0x05;
	public static final byte SNMPOBJECTIDENTIFIER = 0x06;
	public static final byte SNMPSEQUENCE = 0x30;
	
	public static final byte SNMPIPADDRESS = (byte)0x40;
	public static final byte SNMPCOUNTER32 = (byte)0x41;
	public static final byte SNMPGAUGE32 = (byte)0x42;
	public static final byte SNMPTIMETICKS = (byte)0x43;
	public static final byte SNMPOPAQUE = (byte)0x44;
	public static final byte SNMPNSAPADDRESS = (byte)0x45;
	public static final byte SNMPCOUNTER64 = (byte)0x46;
	public static final byte SNMPUINTEGER32 = (byte)0x47;
	
	public static final byte SNMPGETREQUEST = (byte)0xA0;
	public static final byte SNMPGETNEXTREQUEST = (byte)0xA1;
	public static final byte SNMPGETRESPONSE = (byte)0xA2;
	public static final byte SNMPSETREQUEST = (byte)0xA3;
	public static final byte SNMPTRAP = (byte)0xA4;
	
	
	// SNMPv2p constants; unused!!
	public static final byte SNMPv2pCOMMUNICATION = (byte)0xA2;
	public static final byte SNMPv2pAUTHORIZEDMESSAGE = (byte)0xA1;
	public static final byte SNMPv2pENCRYPTEDMESSAGE = (byte)0xA1;
	public static final byte SNMPv2TRAP = (byte)0xA7;
	
	public static final byte SNMPv2pENCRYPTEDDATA = (byte)0xA1;
	
	
	public static final byte SNMPUNKNOWNOBJECT = 0x00;
	
	
	
	
	
	
	/** 
	*	Extracts an SNMP object given its type, length, value triple as an SNMPTLV object.
	*	Called by SNMPObject subclass constructors.
	* 	@throws SNMPBadValueException Indicates byte array in value field is uninterprettable for
	* 	specified SNMP object type.
	*/
	public static SNMPObject extractEncoding(SNMPTLV theTLV)
		throws SNMPBadValueException
	{
	
		
		switch (theTLV.tag)
		{
			case SNMPINTEGER:
			{
				return new SNMPInteger(theTLV.value);
			}
			
			case SNMPSEQUENCE:
			{
				return new SNMPSequence(theTLV.value);
			}
			
			case SNMPOBJECTIDENTIFIER:
			{
				return new SNMPObjectIdentifier(theTLV.value);
			}
			
			case SNMPOCTETSTRING:
			{
				return new SNMPOctetString(theTLV.value);
			}
			
			case SNMPBITSTRING:
			{
				return new SNMPBitString(theTLV.value);
			}
			
			case SNMPIPADDRESS:
			{
				return new SNMPIPAddress(theTLV.value);
			}
			
			case SNMPCOUNTER32:
			{
				return new SNMPCounter32(theTLV.value);
			}
			
			case SNMPGAUGE32:
			{
				return new SNMPGauge32(theTLV.value);
			}
			
			case SNMPTIMETICKS:
			{
				return new SNMPTimeTicks(theTLV.value);
			}
			
			case SNMPNSAPADDRESS:
			{
				return new SNMPNSAPAddress(theTLV.value);
			}
			
			case SNMPCOUNTER64:
			{
				return new SNMPCounter64(theTLV.value);
			}
			
			case SNMPUINTEGER32:
			{
				return new SNMPUInteger32(theTLV.value);
			}
				
			case SNMPGETREQUEST:
			case SNMPGETNEXTREQUEST:
			case SNMPGETRESPONSE:
			case SNMPSETREQUEST:
			{
				return new SNMPPDU(theTLV.value, theTLV.tag);
			}
			
			case SNMPTRAP:
			{
				return new SNMPTrapPDU(theTLV.value);
			}
			
			case SNMPNULL:
			case SNMPOPAQUE:
			{
				return new SNMPNull();
			}
			
			default:
			{
				System.out.println("Unrecognized tag");
				//return new SNMPOctetString(theTLV.value);
				return new SNMPUnknownObject(theTLV.value);
			}
		}
	
	}
	
	
	
	
	
	/** 
	*	Extracts the type, length and value of the SNMP object whose BER encoding begins at the
	* 	specified position in the given byte array. (??what about errors??)
	*/
	
	public static SNMPTLV extractNextTLV(byte[] enc, int position)
	{
		SNMPTLV nextTLV = new SNMPTLV();
		int currentPos = position;
		
		// get tag
		
		/*
		if ((enc[currentPos] % 32) < 31)
		{
			// single byte tag; extract value
			nextTLV.tag = (int)(enc[currentPos]);
		}
		else
		{
			// multiple byte tag; for now, just return value in subsequent bytes ...
			// but need to think about universal / application fields, etc...
			nextTLV.tag = 0;
			
			do
			{
				currentPos++;
				nextTLV.tag = nextTLV.tag * 128 + (int)(enc[currentPos] % 128);
			}
			while ((enc[currentPos]/128) >= 1);
		}
		*/
		
		// single byte tag; extract value
		nextTLV.tag = enc[currentPos];
		currentPos++;	// now at start of length info
		
		// get length of data
		
		int dataLength;
		
		int unsignedValue = enc[currentPos];
		if (unsignedValue < 0)
			unsignedValue += 256;
			
		if ((unsignedValue / 128) < 1)
		{
			// single byte length; extract value
			dataLength = unsignedValue;
		}
		else
		{
			// multiple byte length; first byte's value (minus first bit) is # of length bytes
			int numBytes = (unsignedValue % 128);
			
			dataLength = 0;
			
			for (int i = 0; i < numBytes; i++)
			{
				currentPos++;
				unsignedValue = enc[currentPos];
				if (unsignedValue < 0)
					unsignedValue += 256;
				dataLength = dataLength * 256 + unsignedValue;
			}
		}
		
		
		currentPos++;	// now at start of data
		
		// set total length
		nextTLV.totalLength = currentPos - position + dataLength;
		
		// extract data portion
		
		ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
		outBytes.write(enc, currentPos, dataLength);
		nextTLV.value = outBytes.toByteArray();
				
		
		return nextTLV;
			
	}
	
	
	
	
	/** 
	*	Utility function for encoding a length as a BER byte sequence
	*/
	
	public static byte[] encodeLength(int length)
	{
		ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
		
		// see if can be represented in single byte
		// don't forget the first bit is the "long field test" bit!!
		if (length < 128)
		{
			byte[] len = {(byte)length};
			outBytes.write(len, 0, 1);
		}
		else
		{
			// too big for one byte
			// see how many are needed:
			int numBytes = 0;
			int temp = length;
			while (temp > 0)
			{
				++numBytes;
				temp = (int)Math.floor(temp / 256);
			}
			
			byte num = (byte)numBytes;
			num += 128;		// set the "long format" bit
			outBytes.write(num);
			
			byte[] len = new byte[numBytes];
			for (int i = numBytes-1; i >= 0; --i)
			{
				len[i] = (byte)(length % 256);
				length = (int)Math.floor(length / 256);
			}
			outBytes.write(len, 0, numBytes);
			
		}
		
		return outBytes.toByteArray();
	}
	
	
	
	
}