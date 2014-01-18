package webport;




import java.math.*;


/** Defines a 32-bit gauge, whose value "pegs" at the maximum if initialized with a larger
* value. For an indicator which wraps when it reaches its maximum value, use SNMPCounter32; 
* for a counter with a wider range, use SNMPCounter64.
* @see snmp.SNMPCounter32
* @see snmp.SNMPCounter64
*/


public class SNMPGauge32 extends SNMPInteger
{
	// maximum value is 2^32 - 1 (hack w/ 4*107...)
	private static BigInteger maxValue = new BigInteger("4294967295");
	
	/** Initialize value to 0.
	*/
	
	public SNMPGauge32()
	{
		this(0);	// initialize value to 0
	}
	
	
	public SNMPGauge32(long newValue)
	{
		tag = SNMPBERCodec.SNMPGAUGE32;
		
		value = new BigInteger(new Long(newValue).toString());
		
		// peg if value > maxValue
		value = value.min(maxValue);
	}
	
	
	
	
	/** Used to initialize from the BER encoding, usually received in a response from 
	* an SNMP device responding to an SNMPGetRequest.
	* @throws SNMPBadValueException Indicates an invalid BER encoding supplied. Shouldn't
	* occur in normal operation, i.e., when valid responses are received from devices.
	*/
	
	protected SNMPGauge32(byte[] enc)
		throws SNMPBadValueException
	{
		tag = SNMPBERCodec.SNMPGAUGE32;
		
		extractValueFromBEREncoding(enc);
		
		// peg if value > maxValue
		value = value.min(maxValue);
	}
	
	
	
	
	/** Used to set the value with an instance of java.lang.Integer or
	* java.lang.BigInteger. The value of the constructed SNMPGauge32 object is the
	* supplied value or 2^32, whichever is less.
	* @throws SNMPBadValueException Indicates an incorrect object type supplied.
	*/
	
	public void setValue(Object newValue)
		throws SNMPBadValueException
	{
		// plateau when value hits maxValue
		if (newValue instanceof BigInteger)
		{
			value = (BigInteger)newValue;
			value = value.min(maxValue);
		}
		else if (newValue instanceof Integer)
		{
			value = value = new BigInteger(newValue.toString());
			value = value.min(maxValue);
		}
		else if (newValue instanceof String)
		{
			value = value = new BigInteger((String)newValue);
			value = value.min(maxValue);
		}
		else
			throw new SNMPBadValueException(" Gauge32: bad object supplied to set value ");
	}
	
	
}