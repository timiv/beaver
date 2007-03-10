/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver;

import java.io.IOException;

/**
 * Extends generic parser by adding error recovering capabilities.
 * 
 * @author Alexander Demenchuk
 */
public abstract class ParserWithErrorRecovery extends Parser
{
	/**
	 * A buffer that keeps terminals successfully shifted during parsing simulation. 
	 */
	private Symbol[] accumulator;
	
	/**
	 * An index of the next character to be "read" from the accumulator.
	 */
	private int readPtr;
	
	
	/**
	 * Creates an instance of a parser that capable of recovering from syntax errors.
	 * @param tables preloaded parsing tables
	 */
	protected ParserWithErrorRecovery(ParsingTables tables)
	{
		super(tables);
		accumulator = new Symbol[3];
		readPtr = accumulator.length;
	}
	
	/**
	 * If error recovery accumulated some terminals returns them first and then continues reading
	 * from the scanner.
	 */
	Symbol read(Scanner scanner) throws IOException
	{
		if (readPtr < accumulator.length)
			return accumulator[readPtr++];
		else
			return super.read(scanner);
	}

	/**
     * Implements error recovery. Tries several simple approches first, like deleting "unexpected" token
     * or replacing the latter with one of the expected in the current state (if possible). If simple methods did
     * not work tries to perform error phrase recovery.
	 */
	protected void recoverFromError(Symbol sym, Scanner input) throws SyntaxErrorException, IOException
	{
		if (sym.id == 0)
			throw new SyntaxErrorException("cannot recover from syntax error");

		readPtr = 0;
		Sim sim = new Sim(this);
		
		// insert missing terminal
		accumulator[1] = sym;
		accumulator[2] = super.read(input);
		
		if (insertExpectedTerminal(sim))
		{
			onMissingSymbolInserted(accumulator[0], sym);
			return;
		}
		
		// replace "misspelled" terminal
		accumulator[1] = accumulator[2];
		accumulator[2] = super.read(input);
		
		if (insertExpectedTerminal(sim))
		{
			onUnexpectedSymbolReplaced(accumulator[0], sym);
			reuse(sym);
			return;
		}
		
		// delete unexpected token
		accumulator[0] = accumulator[1];
		accumulator[1] = accumulator[2];
		accumulator[2] = super.read(input);
		
		if (parseAccumulator(sim))
		{
			onUnexpectedSymbolDeleted(sym);
			reuse(sym);
			return;
		}
		
		// delete error phrase
		Symbol error = sym; // reuse sym, as we do not need it anymore
		error.id = tables.errorSymbolId;
		error.value = null;		
		
		short nextState = findActionInErrorShiftingState(error);
		shiftError(error, nextState);
		
		Symbol endOfError = findErrorPhraseEnd(sim, input);
		error.endLine = endOfError.endLine;
		error.endColumn = endOfError.endColumn;
		reuse(endOfError);
		onErrorPhraseRemoved(error);
	}
	
	/**
	 * Inserts expected terminals - one by one - at the beginning of the (maybe already altered) input
	 * stream and simulates parsing.  
	 * 
	 * @param sim parsing simulator
	 * @return true if after an expected terminal was inserted simulator was able to parse alterred stream  
	 */
	private boolean insertExpectedTerminal(Sim sim)
	{
		int state = states[top];
		for (int symId = tables.findFirstTerminal(state); symId >= 0; symId = tables.findNextTerminal(state, symId))
		{
			accumulator[0] = alloc((char) symId);
			sim.reset(this);
			if (sim.parse(accumulator))
			{
				return true;
			}
			reuse(accumulator[0]);
		}
		return false;
	}
	
	/**
	 * Simulates parsing of the prepared accumulator.
	 * 
	 * @see Sim.parse
	 * @param sim parsing simulator
	 * @return true if all accumulated symbols were parsed (shifted)
	 */
	private boolean parseAccumulator(Sim sim)
	{
		sim.reset(this);
		return sim.parse(accumulator);
	}

	/**
	 * Reduces stack untill state is found in which "error" symbol can be shifted.
	 * 
	 * @param error error symbol
	 * @return state where error can be shifted
	 * @throws SyntaxErrorException
	 */
	private short findActionInErrorShiftingState(Symbol error) throws SyntaxErrorException
	{
		short state = tables.findAction(states[top], error.id);
		if (state <= 0)
		{
			Symbol sym = symbols[top];
			if (++top == symbols.length) throw new SyntaxErrorException("cannot recover from syntax error");
			
			while ((state = tables.findAction(states[top], error.id)) <= 0)
			{
				reuse(sym);
				sym = symbols[top];
				if (++top == symbols.length) throw new SyntaxErrorException("cannot recover from syntax error");
			}
			error.startLine   = sym.startLine;
			error.startColumn = sym.startColumn;
			reuse(sym);
		}
		return state;
	}
	
	/**
	 * Shifts "error" symbol.
	 *  
	 * @param error error symbol to shift
	 * @throws SyntaxErrorException
	 */
	private void shiftError(Symbol error, short state)
	{
		if (--top < 0)
		{
			increaseStackCapacity();
			top += states.length / 2;
		}
		symbols[top] = error;
		states[top] = state;
	}
	
	/**
	 * Discards input symbols until parsing, after having error symbol shifted, can continue.
	 * 
	 * @param sim parsing simulator
	 * @return last symbol of the error phrase
	 * @throws SyntaxErrorException
	 */
	private Symbol findErrorPhraseEnd(Sim sim, Scanner input) throws SyntaxErrorException, IOException
	{
		Symbol last = null;
		sim.reset(this);
		if (!sim.parse(accumulator))
		{
			last = shiftAccumulator(input);
			sim.reset(this);
			
			while (!sim.parse(accumulator))
			{
				reuse(last);
				last = shiftAccumulator(input);				
				sim.reset(this);
			}
		}
		return last;
	}
	
	/**
	 * Shifts symbols in accumulator to the left and fills the vacant rightmost slot from the scanner.
	 * 
	 * @param input scanner
	 * @return pushed off symbol
	 * @throws SyntaxErrorException
	 * @throws IOException
	 */
	private Symbol shiftAccumulator(Scanner input) throws SyntaxErrorException, IOException
	{
		Symbol sym = accumulator[0];
		
		accumulator[0] = accumulator[1];
		accumulator[1] = accumulator[2];
		accumulator[2] = super.read(input);
		
		if (accumulator[2].id == 0) // EOF
		{
			throw new SyntaxErrorException("cannot recover from syntax error");
		}
		return sym;
	}

	/**
	 * A callback that is called when parser has successfully recovered from a syntax error by inserting a terminal
	 * symbol that was missing in the source.
	 * Note: an inserted symbol is synthetic and does not have any information attached to it - location in the
	 * source (line and column numbers) and the value. The former - location - can be extrapolated from the next
	 * symbol, which came from the scanner.
	 * 
	 * @param sym inserted symbol
	 * @param next source terminal before which new symbol has been inserted (this one has source line and column)
	 */
	protected void onMissingSymbolInserted(Symbol sym, Symbol next)
	{
		// Do nothing here. Implementation overrides it for reporting.
	}

	/**
	 * A callback that is called when parser has successfully recovered from a syntax error by replacing a terminal
	 * that was found in the source with one that is expected in the current parser state.
	 * 
	 * @param inserted synthetic symbol inserted in the "stream" to make it syntactically correect
	 * @param removed original symbol that was not expected where it was found
	 */
	protected void onUnexpectedSymbolReplaced(Symbol inserted, Symbol removed)
	{
		// Do nothing here. Implementation overrides it for reporting.
	}
	
	/**
	 * A callback that is called when parser has successfully recovered from a syntax error by deleting a terminal
	 * symbol that was not expected where it was found.
	 * 
	 * @param deleted unexpected symbol
	 */
	protected void onUnexpectedSymbolDeleted(Symbol deleted)
	{
		// Do nothing here. Implementation overrides it for reporting.
	}
	
	/**
	 * A callback that is called when parser has successfully recovered from a syntax error by deleting an entire
	 * phrase from the input and shifted an error symbol (non-terminal) in its place.
	 * 
	 * @param error symbol that represents a removed error phrase, which spans all removed terminals
	 */
	protected void onErrorPhraseRemoved(Symbol error)
	{
		// Do nothing here. Implementation overrides it for reporting.
	}
	
	/**
	 * A minimal version of the parser that does not execute semantic actions.
	 * 
	 * @author Alexander Demenchuk
	 */
	static class Sim
	{
		/**
		 * Parsing tables that are used by the real parser.
		 */
		private ParsingTables tables;
		
		/**
		 * Cached copy of the accept action.
		 */
		private short accept;
		
		/**
		 * copy of the real parser's stack
		 */
		private short[] states;
		
		/**
		 * Index of the top element on the stack
		 */
		private int top;
		
		/**
		 * Index of the topmost original stack element not touched by this parser
		 */
		private int bottom;
		
		Sim(Parser parser)
		{
			this.tables = parser.tables;
			this.accept = parser.accept;
			this.states = new short[parser.states.length - parser.top < 3 ? parser.states.length + 4 : parser.states.length];
			this.bottom = this.states.length;
		}
		
		/**
		 * Restores the top of the stack before the simulation starts.
		 * 
		 * @param parser original parser that tried to recover from a syntax error
		 */
		void reset(Parser parser)
		{
			this.top = parser.top + (this.states.length - parser.states.length);
			if (this.states.length - this.top < 3)
			{
				Parser.expand(this.states, this.states.length + 4);
				this.top    += 4;
				this.bottom += 4;
			}
			System.arraycopy(parser.states, parser.top, this.states, this.top, this.bottom - this.top);
			this.bottom = this.top;
		}
		
		/**
		 * Similates normal parsing without executing semantic actions. Stops when either entire input is
		 * shifted or the goal is reached.
		 * 
		 * @param input An array of symbols that this parser uses as an input instead of a scanner.
		 * @return true if either entire input is consumed or parser reached its goal, false if parser encounters
		 *         a syntax error
		 */
		boolean parse(Symbol[] input)
		{
			for(int readPtr = 0; readPtr < input.length; readPtr++)
			{
				char symId = input[readPtr].id;
				while (true)
				{
					short act = tables.findAction(states[top], symId);
					if (act > 0)
					{
						shift(act);
						break;
					}
					else if (act == accept)
					{
						return true;
					}
					else if (act < 0)
					{
						char ntId = reduce(~act);
						act = tables.findAction(states[top], ntId);
						if (act > 0)
						{
							shift(act);
						}
						else
						{
							return act == accept;
						}
					}
					else // syntax error (unexpected symbol)
					{
						return false;
					}
				}
			}
			return true;
		}
		
		private void shift(short state)
		{
			states[--top] = state;
		}

		private char reduce(int rule)
		{
			int ruleDef = tables.ruleDefs[rule];
			top += ruleDef & 0xFFFF;
			if (top > bottom) bottom = top;
			return (char) (ruleDef >>> 16);
		}
	}
}
