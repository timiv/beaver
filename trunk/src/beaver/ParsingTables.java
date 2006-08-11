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
	int[] terminalOffsets;

	/**
	 * Per state offset from the start of the actions table for non-terminal lookaheads.
	 */
	int[] nonTerminalOffsets;

	/**
	 * This array contains encoded information about grammar productions. Each element is a "struct":
	 * <ul>
	 * <li> rhs_length : char (bits 0-15)</li>
	 * <li> symbol_id  : char (bits 16-31)</li>
	 * </ul>    
	 */
	int[] ruleDefs;
	
	/**
	 * Number of RHS elements in the longest production.
	 */
	int maxRhsLength;
	
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
	 * Looks up an action code for the given terminal symbol when the parser is in the given state.
	 * 
	 * @param state current parser state
	 * @param symId ID of the terminal parser has just got fromt he scanner 
	 * @return action code
	 */
	short findTerminalAction(int state, char symId)
	{
		return findAction(terminalOffsets, state, symId);
	}
	
	/**
	 * Looks up an action code for the given non-terminal symbol when the parser is in the given state.
	 * 
	 * @param state current parser state
	 * @param symId ID of the non-terminal parser has just got fromt he scanner 
	 * @return action code
	 */
	short findNonterminalAction(int state, char symId)
	{
		return findAction(nonTerminalOffsets, state, symId);
	}

	/**
	 * Perform actual action code lookup.
	 * 
	 * @param offsets either terminal or non-teminal offsets table 
	 * @param state current parser state
	 * @param symId symbol for which parser looks up an action
	 * @return action code or 0 if the specified lookahead is unexpected in the specified state
	 */
	private short findAction(int[] offsets, int state, char symId)
	{
		int index = offsets[state] + symId;
		try
		{
			if (lookaheads[index] == symId)
			{
				return actions[index]; 
			}
		}
		catch (IndexOutOfBoundsException _)
		{
			// Try-catch eliminates an explicit bounds check for the most common case - correct syntax.
			// If the lookahead is not one that is expected parser will enter error recovery, and at
			// that point an exception toll is quite ignorable. 
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
		int offset = terminalOffsets[state];
		int prevId = -1;
		if (offset < 0)
		{
			prevId -= offset;
		}
		return findNextTerminal(state, prevId);
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
		int offset = terminalOffsets[state];

		int endIdx = offset + numTerminals;
		if (endIdx > lookaheads.length)
		{
			endIdx = lookaheads.length;
		}
		termId++;
		int i = offset + termId;
		if (i > 0)
		{
			while (i < endIdx)
			{
				if (lookaheads[i++] == termId)
					return termId;
				termId++;
			}
		}
		return -1;
	}
	
	ParsingTables(DataInput is) throws IOException
	{
		int n = is.readInt();
		actions    = new short[n];       for (int i = 0; i < n; i++) { actions[i] = is.readShort(); }
		lookaheads = new char[n];        for (int i = 0; i < n; i++) { lookaheads[i] = (char) is.readUnsignedShort(); }

		n = is.readShort();
		terminalOffsets    = new int[n]; for (int i = 0; i < n; i++) { terminalOffsets[i] = is.readInt(); }
		nonTerminalOffsets = new int[n]; for (int i = 0; i < n; i++) { nonTerminalOffsets[i] = is.readInt(); }

		int maxRhsLen = 0; 
		int minSymId = 0xffff;
		n = is.readShort();
		ruleDefs = new int[n];
		for (int i = 0; i < n; i++) 
		{ 
			ruleDefs[i] = is.readInt();
			
			char symId = (char) (ruleDefs[i] >>> 16);
			if (symId < minSymId)
			{
				minSymId = symId;
			}
			
			int rhsLen = ruleDefs[i] & 0xffff;
			if (maxRhsLen < rhsLen)
			{
				maxRhsLen = rhsLen;
			}
		}
		numTerminals = minSymId;
		maxRhsLength = maxRhsLen;

		firstTerminalWithValueId = (char) is.readUnsignedShort();
		errorSymbolId = (char) is.readUnsignedShort();
	}

	public static class Compressed extends ParsingTables
	{
		/** 
		 * Per state actions that will be taken if nothing is found in the actions table.
		 * This table is used (and applicable) only if actions are compressed and default
		 * actions has been selected.  
		 */
		short[] defaultActions;
		
		/**
		 * Looks up an action code for the given terminal symbol when the parser is in the given state.
		 */
		short findTerminalAction(int state, char symId)
		{
			return findAction(terminalOffsets, state, symId);
		}

		/**
		 * Looks up an action code for the given non-terminal symbol when the parser is in the given state.
		 */
		short findNonterminalAction(int state, char symId)
		{
			return findAction(nonTerminalOffsets, state, symId);
		}
		
		/**
		 * Perform actual action code lookup.
		 * 
		 * @param offsets either terminal or non-teminal offsets table 
		 * @param state current parser state
		 * @param symId symbol for which parser looks up an action
		 * @return action code or 0 if the specified lookahead is unexpected in the specified state
		 */
		private short findAction(int[] offsets, int state, char symId)
		{
			int index = offsets[state] + symId;
			if (index <= 0 && index < lookaheads.length && lookaheads[index] == symId)
			{
				return actions[index]; 
			}
			return defaultActions[state];
		}
		
		Compressed(DataInput is) throws IOException
		{
			super(is);
			int n = terminalOffsets.length;	 
			defaultActions = new short[n];  for (int i = 0; i < n; i++) { defaultActions[i] = is.readShort(); }
		}
	}
	
	public static ParsingTables from(DataInput is) throws IOException
	{
		switch (is.readByte())
		{
			case 'U':
				return new ParsingTables(is);
				
			case 'C':
				return new ParsingTables.Compressed(is);
		}
		throw new StreamCorruptedException("serialization type");
	}
}
