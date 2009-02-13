package beaver.util;

public class BitMath
{

	/**
     * Counts set bits in a 32-bit number
     * 
     * @param bits a bag of bits
     * @return number of bits in a bag
     */
    public static int countBits(int bits)
    {
    	bits = (bits & 0x55555555) + ((bits >>> 1) & 0x55555555);
    	bits = (bits & 0x33333333) + ((bits >>> 2) & 0x33333333);
    	bits = (bits & 0x0F0F0F0F) + ((bits >>> 4) & 0x0F0F0F0F);
    	bits = (bits & 0x00FF00FF) + ((bits >>> 8) & 0x00FF00FF);
    	return (bits & 0x0000FFFF) + (bits >>> 16);
    }

    /**
     * Calculates the mask (all lower bits are set) that covers bits of 
     * a given positive integer.
     * 
     * @param bits a bag of bits
     * @return mask that covers set bits
     */
    public static int getMask(int bits)
    {
		int mask = bits | bits >> 1;
		mask |= mask >> 2;
		mask |= mask >> 4;
		mask |= mask >> 8;
		mask |= mask >> 16;
		
    	return mask;
    }
    
}
