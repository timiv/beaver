/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver;

import java.io.DataInput;
import java.io.IOException;
import java.io.StreamCorruptedException;

/**
 * Tables used by the parser. 
 *
 * @author Alexander Demenchuk
 */
public class ParsingTables
{
	/** 
	 * A table with actions.
	 * A positive number in this table represents SHIFT and the its value indicates a new parser state.
	 * A negative number instructs a parser to reduce accumulated symbols using a production, which ID
	 * is represented by the absolute value of the action code.   
	 */
	short[] actions;

	/**
	 * A table containing the lookahead for each entry in the "actions" table.
	 * Used to detect "collisions".
	 */
	char[] lookaheads;

	/**
	 * Per state offset from the start of the actions table for terminal lookaheads.
	 */
	char[][] offsets;

	/**
	 * The number of bits to shift symbol id to the right to derive offset group index.
	 */
	char indexShift;
	
	/**
	 * The number to subtract from value in offsets table to get real offset into actions table.
	 */
	char offsetShift;
	
	/**
	 * Sets of lookaheads for default actions  
	 */
	BitSet[] defaultActionLookaheadsSets;
	      
	/**
	 * Per state indexes into the default lookaheads sets table 
	 */
	short[] defaultActionLookaheadsSetIndexes;
	
	/**
	 * Per state "default" actions
	 */
	short[] defaultActions;
	
	/**
	 * This array contains encoded information about grammar productions. Each element is a "struct":
	 * <ul>
	 * <li> rhs_length : char (bits 0-15)</li>
	 * <li> symbol_id  : char (bits 16-31)</li>
	 * </ul>    
	 */
	int[] ruleDefs;
	
	/** 
	 * Number of terminals
	 */
	int numTerminals;
	
	/**
	 * ID of the first terminal that carries a payload (value).
	 * Terminals with IDs that are less than this one [0..payload) do not need
	 * their value extracted from the scanner - their ID tells everything one needs
	 * to know. Terminals [payload..numTerminals) carry their value with them.   
	 */
	char firstTerminalWithValueId;
	
	/** 
	 * ID of the "error" nonterminal 
	 */
	char errorSymbolId;
	
	/**
	 * Textual representation of symbols.
	 */
	String[] symbolRepresentations;
	
	public ParsingTables(DataInput inp) throws IOException
	{
		if ( inp.readByte() != '#' )
			throw new StreamCorruptedException("signature");
		if ( inp.readByte() != 'A' )
			throw new StreamCorruptedException("version");
		
		actions = new short[inp.readUnsignedShort()];	
		for ( int i = 0; i < actions.length; i++ )
		{ 
			actions[i] = inp.readShort();
		}
		lookaheads = new char[inp.readUnsignedShort()];
		for ( int i = 0; i < lookaheads.length; i++ )
		{
			lookaheads[i] = inp.readChar(); 
		}

		int numStates = inp.readUnsignedShort();
		offsets = new char[numStates][inp.readUnsignedShort()];
		for (int i = 0; i < offsets.length; i++)
        {
	        for (int j = 0; j < offsets[0].length; j++)
            {
	        	offsets[i][j] = inp.readChar();
            }
        }
		
		indexShift = inp.readChar();
		offsetShift = inp.readChar();
		
		defaultActionLookaheadsSets = new BitSet[inp.readUnsignedShort()];
		for (int i = 0; i < defaultActionLookaheadsSets.length; i++)
        {
	        defaultActionLookaheadsSets[i] = new BitSet(inp);
        }
		
		defaultActionLookaheadsSetIndexes = new short[inp.readUnsignedShort()];
		if ( defaultActionLookaheadsSetIndexes.length != numStates )
			throw new StreamCorruptedException("default action indexes");
		
		for (int i = 0; i < defaultActionLookaheadsSetIndexes.length; i++)
		{
			defaultActionLookaheadsSetIndexes[i] = (short) (inp.readChar() - 1);
			if ( defaultActionLookaheadsSetIndexes[i] >= defaultActionLookaheadsSets.length )
				throw new StreamCorruptedException("bitset index");
		}
		
		defaultActions = new short[inp.readUnsignedShort()];
		if ( defaultActions.length != numStates )
			throw new StreamCorruptedException("default actions");
			
		for (int i = 0; i < defaultActions.length; i++)
		{
			defaultActions[i] = inp.readShort();
		}
		
		ruleDefs = new int[inp.readUnsignedShort()];
		for (int i = 0; i < ruleDefs.length; i++) 
		{ 
			ruleDefs[i] = inp.readInt();
		}

		firstTerminalWithValueId = inp.readChar();
		errorSymbolId = inp.readChar();
		
		symbolRepresentations = new String[inp.readUnsignedShort()];
		for ( int i = 0; i < symbolRepresentations.length; i++ )
        {
	        symbolRepresentations[i] = inp.readUTF();
        }
	}
	
	public String getSymbolRepresentation(char id)
	{
		return symbolRepresentations[id];
	}
	
	/**
	 * Looks up an action code for the given symbol when the parser is in the given state.
	 * 
	 * @param offsets either terminal or non-teminal offsets table 
	 * @param state current parser state
	 * @param symId symbol for which parser looks up an action
	 * @return action code or 0 if the specified lookahead is unexpected in the specified state
	 */
	short findAction(int state, char symId)
	{
		int index = offsets[state][symId >> indexShift] - offsetShift + symId;
		if (0 <= index && index < lookaheads.length && lookaheads[index] == symId)
		{
			return actions[index]; 
		}
		if ( (index = defaultActionLookaheadsSetIndexes[state]) >= 0 && defaultActionLookaheadsSets[index].isSet(symId) )
		{
			return defaultActions[state];
		}	
		return 0;
	}
	
	/**
	 * Returns the first terminal out of all terminals expected in a given state.
	 * Used in error recovery when an unexpected terminal is replaced with one that is expected.
	 * 
	 * @param state in which an error occured
	 * @return ID of the expected terminal symbol or -1 if there is none
	 */
	int findFirstTerminal(int state)
	{
		return findNextTerminal(state, -1); 
	}
	
	/**
	 * Returns the next terminal, after the specified one, out of all terminals expected in a given state.
	 * Used in error recovery when an unexpected terminal is replaced with one that is expected.
	 * 
	 * @param state in which an error occured
	 * @param termId start search after this ID
	 * @return ID of the expected terminal symbol or -1 if there is none
	 */
	int findNextTerminal(int state, int termId)
	{
		char[] stateOffsets = offsets[state];
		for ( char t = (char) (termId + 1); t < numTerminals; t++ )
		{
			int index = stateOffsets[t >> indexShift] - offsetShift + t;
			if (0 <= index && index < lookaheads.length && lookaheads[index] == t)
			{
				return t;
			}
		}
		
		BitSet deftActLaSet = defaultActionLookaheadsSetIndexes[state] >= 0 ? defaultActionLookaheadsSets[defaultActionLookaheadsSetIndexes[state]] : null;
		if ( deftActLaSet != null )
		{
			for ( char t = (char) (termId + 1); t < numTerminals; t++ )
			{
				if ( deftActLaSet.isSet(t) )
				{
					return t;
				}
			}
		}
		return -1;
	}
	
	static class BitSet
	{
		char   lb;
		char[] bits;
		
		BitSet(DataInput inp) throws IOException
		{
			lb = inp.readChar();
			bits = new char[inp.readUnsignedShort()];
			for ( int i = 0; i < bits.length; i++ )
            {
	            bits[i] = inp.readChar();
            }
		}
		
		boolean isSet(char i)
		{
			int dist = i - lb;
			if ( dist == 0 )
				return true;
			if ( dist < 0 )
				return false;
			//
			// lb is at bit index -1, hence the distance from
			// the first bit in a set is 1 less than what was
			// calculated for lb
			//
			dist--;
			
			int index = dist >> 4;
			if ( index >= bits.length )
				return false;
			
			return (bits[index] & (1 << (dist & 15))) != 0; 
		}
	}
}
