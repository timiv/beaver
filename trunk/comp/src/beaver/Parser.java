package beaver;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class Parser
{
	/**
	 * Array of ranges [min,max] of lookahead IDs for each state.
	 * Each range is packed as (max << 16) | min.
	 */
	int[]   stateLookaheadBounds;

	/**
	 * How much offset, as read from state actions offset array,
	 * needs to be shifted.
	 */
	int     stateActionsOffsetShift;

	/**
	 * Offset to where the packed state actions are stored.
	 * To minimize storage (and memory usage) the offsets in this array
	 * are shifted, therefore action index is calculated with the shift:
	 *   index = lookahead_id + offset + shift;
	 */
	char[]  stateActionsOffsets;

	/**
	 * Array of packed parser actions.
	 * Positive numbers represent shift and the number itself a state ID
	 * parser will go to. Note, that actions table has state IDs in [1,NumStates]
	 * range while the parser internally uses [0,NumStates-1] range.
	 * Negative numbers are inverted production IDs that are used to reduce
	 * the stack.
	 * Zero is a non-action or error.  
	 */
	short[] actions;
	
	/**
	 * Control array to check that the action at a computed index is a valid
	 * one for the current lookahead. 
	 */
	char[]  actionLookaheads;
	
	/**
	 * Default action (reduce) to take in a state when nothing can be found
	 * in the packed actions array. 
	 */
	short[] defaultActions;
	
	/**
	 * Production info: number of symbols on the RHS
	 */
	short[]  productionSizes;
	
	/**
	 * Production info: ID of the LHS symbol
	 */
	short[] productionLhsId;
	
	/**
	 * Text representations of all symbols for error reporting.
	 */
	protected String[] symbols;
	
	/**
	 * Index of the top element;
	 */
	int stackTop;
	
	/**
	 * Parsing stack: states 
	 */
	short[] stackStates;
	
	/**
	 * Parsing stack: symbols "returned" by a scanner or created by stack reductions
	 */
	Object[] stackSymbols;

	protected Parser(InputStream bptInputStream) throws IOException
	{
		DataInputStream bptData = new DataInputStream(bptInputStream);
		try
		{
			loadTables(bptData);
		}
		finally
		{
			bptData.close();
		}
		// initialize stack
		stackStates = new short[256];
		stackSymbols = new Object[stackStates.length];
		stackTop = stackStates.length;
	}

	private void loadTables(DataInput in) throws IOException
	{
		// check prefix
		if (in.readByte() != 'B' || in.readByte() != 'P' || in.readByte() != 'T' || in.readByte() != '>')
		{
			throw new IllegalArgumentException("!BPT");
		}
		int numStates = in.readUnsignedShort();
		stateLookaheadBounds = new int[numStates];
		for (int i = 0; i < numStates; i++)
		{
			stateLookaheadBounds[i] = in.readInt();
		}
		stateActionsOffsetShift = in.readShort();

		stateActionsOffsets = new char[numStates];
		for (int i = 0; i < numStates; i++)
		{
			stateActionsOffsets[i] = in.readChar();
		}
		
		int packedActionsArraySize = in.readUnsignedShort();
		actions = new short[packedActionsArraySize];
		for (int i = 0; i < packedActionsArraySize; i++)
        {
	        actions[i] = in.readShort();
        }
		actionLookaheads = new char[packedActionsArraySize];
		for (int i = 0; i < packedActionsArraySize; i++)
        {
	        actionLookaheads[i] = in.readChar();
        }
		
		defaultActions = new short[numStates];
		for (int i = 0; i < numStates; i++)
		{
			defaultActions[i] = in.readShort();
		}
		
		int numProductions = in.readUnsignedShort();
		productionSizes = new short[numProductions];
		productionLhsId = new short[numProductions];
		for (int i = 0; i < numProductions; i++)
        {
			int info = in.readInt();
			productionSizes[i] = (short) (info & 0xffff); 
			productionLhsId[i] = (short) (info >>> 16); 
        }
		
		symbols = new String[in.readUnsignedShort() + 1];
		for (int i = 1; i < symbols.length; i++)
        {
	        symbols[i] = in.readUTF();
        }
		
		if (in.readByte() != 4)
		{
			throw new IllegalStateException("BPT structure");
		}
	}

	private void clearStack()
	{
		for (int i = 0; i < stackSymbols.length; i++)
        {
	        stackSymbols[i] = null;
        }
		stackTop = stackStates.length;
	}
	
	/**
	 * Parser attempts to get a terminal (read) from the scanner until it succeeds.
	 * If absolutely nothing can be recognized in the source, this method would report
	 * (many times) an unexpected character and return an EOF in the end.
	 * UnexpectedCharacterException are intercepted and reported as an "event".
	 *
	 * @param input Scanner instance
	 * @return ID of the just recognized terminal
	 * @throws IOException that originated in a scanner
	 */
	private int read(Scanner input) throws IOException
	{
		while (true)
		{
			try
			{
				return input.getNextToken(); 
			}
			catch (UnexpectedCharacterException _)
			{
				onUnexpectedCharacterError(input.getTokenText(), input.getTokenLine(), input.getTokenColumn());
			}
		}
	}
	
	/**
	 * Determines what the parser should do in the current state
	 * with the provided lookahead.
	 * 
	 * @param lookaheadId  ID of the lookahead symbol (note, this is always
	 *                     a positive number while scanner may return negative
	 *                     IDs for some tokens)  
	 * @return action ID or 0 if the lookahead is not expected in the current state
	 */
	int findAction(int lookaheadId)
	{
		int stateId = stackStates[stackTop];
		int stateLookaheadsRange = stateLookaheadBounds[stateId];
		int minLookaheadId = stateLookaheadsRange & 0xffff;
		int maxLookaheadId = stateLookaheadsRange >>> 16;
		if (lookaheadId < minLookaheadId || maxLookaheadId < lookaheadId)
		{
			return 0;
		}
		int index = stateActionsOffsets[stateId] + stateActionsOffsetShift + lookaheadId;
		if (actionLookaheads[index] == lookaheadId)
		{
			return actions[index];
		}
		return defaultActions[stateId];
	}

	public Object parse(Scanner input) throws SyntaxErrorException, IOException
	{
		stackStates[--stackTop] = 0; // starting state
		
		while (true)
		{
			int tokenId = read(input);
			int symbolId;
			Object symbol;
			if (tokenId < 0)
			{
				symbolId = -tokenId;
				symbol = null;
			}
			else
			{
				symbolId = tokenId;
				symbol = makeTerm(tokenId, input.getTokenText(), input.getTokenLine(), input.getTokenColumn());
			}
			while (true)
			{
				int action = findAction(symbolId);
				if (action > 0)
				{
					shift(symbol, action - 1); // action - 1 is a goto state
					break;
				}
				else if (action < 0)
				{
					int ruleId = ~action;
					Object newNonterminal = reduce(stackSymbols, stackTop, ruleId);
					stackTop += productionSizes[ruleId];
					action = findAction(productionLhsId[ruleId]);
					if (action > 0)
					{
						shift(newNonterminal, action - 1);
					}
					else if (action == ~productionLhsId.length) // accept
					{
						clearStack();
						return newNonterminal;
					}
					else
					{
						throw new IllegalStateException("Cannot shift new nonterminal");
					}
				}
				else // action == 0, i.e. this terminal is not expected in this state
				{
					Object tokenText = input.getTokenText();
					int tokenLine = input.getTokenLine();
					int tokenColumn = input.getTokenColumn();
					onSyntaxError(tokenId, tokenText, tokenLine, tokenColumn);
					recoverFromError(tokenId, tokenText, tokenLine, tokenColumn);
					break;
				}
			}
		}
	}
	
	private void shift(Object symbol, int gotoState)
	{
		if (--stackTop < 0)
		{
			// increase stack capacity
			int newStackSize = stackStates.length * 2;	
			
			short[] expandedStackStates = new short[newStackSize];
			System.arraycopy(stackStates, 0, expandedStackStates, stackStates.length, stackStates.length);		
			stackStates = expandedStackStates;
			
			Object[] expandedStackSymbols = new Object[newStackSize];
			System.arraycopy(stackSymbols, 0, expandedStackSymbols, stackSymbols.length, stackSymbols.length);		
			stackSymbols = expandedStackSymbols;
			
			stackTop = stackStates.length - 1;
		}
		stackSymbols[stackTop] = symbol;
		stackStates[stackTop] = (short) gotoState;
	}
	
	/**
	 * When a non-keyword terminal is recognized parser has to make a Term instance out of it.
	 * 
	 * @param id ID of the recognized terminal. 
	 * @param text Text of the recognized terminal. 
	 * @param line Line where the terminal text starts.
	 * @param column Column where the terminal text starts.
	 * @return Term instance
	 */
	protected abstract Object makeTerm(int id, Object text, int line, int column);
	
	/**
	 * Reduce the stack.
	 * 
	 * @param stack Stack of symbols
	 * @param top Index of the topmost element on the stack
	 * @param rule ID of the production that should reduce the stack
	 * @return new nonterminal
	 */
	protected abstract Object reduce(Object[] stack, int top, int rule);
	
	/**
	 * Unexpected character error event.
	 * 
	 * @param text Partially recognized token, where the last character is the unexpected one. 
	 * @param line Line where the partially recognized token starts.
	 * @param column Column where the partially recognized token starts.
	 */
	protected void onUnexpectedCharacterError(Object text, int line, int column)
	{
		// Parser implementation overrides this for error reporting
	}
	
	/**
	 * Unexpected terminal error event.
	 *
	 * @param text Text of the unexpected terminal. 
	 * @param line Line where the terminal text starts.
	 * @param column Column where the terminal text starts.
	 */
	protected void onSyntaxError(int symbolId, Object text, int line, int column)
	{
		// Do nothing here. Implementation overrides it for reporting.
	}	
	
	/**
	 * Parser will try to recover from a syntax error here.
	 * If successful it alters a "stream" of tokens so that it can continue parsing for a while.
	 * Otherwise it'll give up by throwing a syntax error exception.
	 *
	 * @param symbolId ID of the unexpected terminal. 
	 * @param text Text of the unexpected terminal. 
	 * @param line Line where the terminal text starts.
	 * @param column Column where the terminal text starts.
	 * @throws SyntaxErrorException
	 */
	protected void recoverFromError(int symbolId, Object text, int line, int column) throws SyntaxErrorException, IOException
	{
		throw new SyntaxErrorException("(" + line + "," + column + "): unexpected " + symbols[symbolId > 0 ? symbolId : -symbolId] + (symbolId > 0 ? " = " + text : ""));
	}
	
}
