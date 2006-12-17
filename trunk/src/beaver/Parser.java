/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver;

import java.io.IOException;

/**
 * LALR parsing engine.
 *
 * @author Alexander Demenchuk
 */
public abstract class Parser
{
	/**
	 * The automaton tables.
	 */
	ParsingTables tables;

	/**
	 * Cached code of the ACCEPT action.
	 */
	short accept;

	/**
	 * Index of the stack's top element, i.e. it's -1 when the stack is empty
	 */
	int top;

	/**
	 * Part of the parsing stack - states;
	 */
	short[] states;

	/**
	 * Part of the parsing stack - symbols;
	 */
	Symbol[] symbols;

	/**
	 * Start of the list of pooled symbols.
	 */
	Symbol pool;

	/**
	 * Initializes a parser instance.
	 *
	 * @param tables loaded parsing tables
	 */
	protected Parser(ParsingTables tables)
	{
		this.tables  = tables;
		this.accept  = (short) ~tables.ruleDefs.length;
	}

	/**
     * Parses an input and returns a semantic value of the accepted non-terminal.
	 *
	 * @param input Scanner
	 * @return value of the "goal" symbol
	 * @throws SyntaxErrorException
	 * @throws IOException
	 */
	public Object parse(Scanner input) throws SyntaxErrorException, IOException
	{
		if (states == null)
			states = new short[256];
		if (symbols == null)
			symbols = new Symbol[states.length];

		top = states.length - 1;
		states[top] = 1;
		symbols[top] = alloc((char) 0);

		while (true)
		{
			Symbol sym = read(input);
			while (true)
			{
				short act = tables.findAction(states[top], sym.id);
				if (act > 0)
				{
					shift(sym, act);
					break;
				}
				else if (act == accept)
				{
					reuse(1);
					return symbols[top].value;
				}
				else if (act < 0)
				{
					Symbol nt = reduce(~act);
					act = tables.findAction(states[top], nt.id);
					if (act > 0)
					{
						shift(nt, act);
					}
					else if (act == accept)
					{
						reuse(nt);
						return nt.value;
					}
					else
					{
						throw new IllegalStateException("Cannot shift new nonterminal");
					}
				}
				else // act == 0, i.e. this terminal is not expected in this state
				{
					onSyntaxError(sym);
					recoverFromError(sym, input);
					break; // sym is no longer valid (recovery altered input stream) and needs to be re-read
				}
			}
		}
	}

	/**
	 * Shift a symbol onto the stack and go to the new state.
	 *
	 * @param sym symbol that will be shifted
	 * @param state new parser state
	 */
	private void shift(Symbol sym, short state)
	{
		if (--top < 0)
		{
			increaseStackCapacity();
			top += states.length / 2;
		}
		symbols[top] = sym;
		states[top] = state;
	}

	/**
	 * Reduce a number of shifted symbols to a single symbol.
	 *
	 * @param rule number of the production by which to reduce
	 * @return non-terminal created by the reduction
	 */
	private Symbol reduce(int rule)
	{
		int ruleDef = tables.ruleDefs[rule];
		int rhsSize = ruleDef & 0xFFFF;
		int reduced = top + rhsSize;
		int rhsIndx = reduced - 1;

		Symbol lhs = reduce(symbols, rhsIndx, rule);
		lhs.id = (char) (ruleDef >>> 16);

		if (rhsSize == 0)
		{
			Symbol topSym = symbols[reduced];
			lhs.setLocation(topSym.endLine, topSym.endColumn, topSym.endLine, topSym.endColumn);
		}
		else
		{
			Symbol firstSym = symbols[rhsIndx], lastSym = symbols[top];
			lhs.setLocation(firstSym.startLine, firstSym.startColumn, lastSym.endLine, lastSym.endColumn);

			reuse(rhsSize);
		}
		top = reduced;

		return lhs;
	}

	/**
	 * This method contains a (generated) trampoline code to call actual semantic action methods.
	 *
	 * @param symbols entire stack
	 * @param at      index of the first RHS symbol
	 * @param rule    production ID whose action routine needs to be called to create a LHS symbol
	 * @return LHS symbol
	 */
	protected abstract Symbol reduce(Symbol[] symbols, int at, int rule);

	/**
	 * Increases the stack capacity if it has no room for new entries.
	 */
	void increaseStackCapacity()
	{
		int len = states.length * 2;
		states = expand(states, len);

		Symbol[] newSymbols = new Symbol[len];
		System.arraycopy(symbols, 0, newSymbols, len - symbols.length, symbols.length);
		symbols = newSymbols;
	}

	/**
	 * A "shortcut" alloc that also assigns a value to the allocated symbol. Useful for returning
	 * non-terminal symbols from production actions.
	 *
	 * @param value symbol's value
	 * @return allocated symbol
	 */
	protected Symbol symbol(Object value)
	{
		Symbol sym = alloc();
		sym.value = value;
		return sym;
	}

	/**
	 * A "shortcut" alloc that also assigns a value to the allocated symbol. The value of the
	 * allocted symbol is set to the value of a passed symbol.
	 *
	 * @param param symbol on the stack, which value will be used by a new symbol
	 * @return allocated symbol
	 */
	protected Symbol copy(Symbol param)
	{
		Symbol sym = alloc();
		sym.value = param.value;
		return sym;
	}

	/**
	 * A "shortcut" alloc that also assigns an ID to the allocated symbol.
	 *
	 * @param id symbol ID
	 * @return allocated symbol
	 */
	Symbol alloc(char id)
	{
		Symbol sym = alloc();
		sym.id = id;
		return sym;
	}

	/**
	 * Allocates an instance of the Symbol that parser eventually shifts onto its stack.
	 *
	 * @return a symbol
	 */
	private Symbol alloc()
	{
		if (pool == null)
			return new Symbol();

		Symbol sym = pool;
		pool = sym.next;
		return sym;
	}

	/**
	 * Returns symbols from the production RHS for future reuse.
	 *
	 * @param n number of symbols from the top of the stack that should be reused
	 */
	private void reuse(int n)
	{
		if (n > 0)
		{
			int i = top;
			Symbol head = symbols[i];
			head.next = pool;
			while (--n > 0)
			{
				Symbol sym = symbols[++i];
				sym.next = head;
				head = sym;
			}
			pool = head;
		}
	}

	/**
	 * Returns a single symbol for future reuse.
	 * @param sym
	 */
	void reuse(Symbol sym)
	{
		sym.next = pool;
		pool = sym;
	}

	/**
	 * Reads next terminal symbol from the scanner. UnexpectedCharacterException are intercepted and
	 * reported via a callback.
	 * This method will keep trying to get a terminal from the scanner until it succeeds, hence if absolutely
	 * nothing can be recognized in the source, this method would report (many times) an unexpected character
	 * and return an EOF in the end.
	 *
	 * @param scanner Scanner instance
	 * @return next terminal symbol
	 * @throws IOException that originates in a scanner
	 */
	Symbol read(Scanner scanner) throws IOException
	{
		char id;
		while (true)
		{
			try
			{
				id = (char) scanner.next();
				break;
			}
			catch (UnexpectedCharacterException _)
			{
				onUnexpectedCharacterError(scanner.getValue(), scanner.getLine(), scanner.getColumn());
			}
		}
		Symbol sym = alloc(id);
		sym.value = id < tables.firstTerminalWithValueId ? null : makeTerm(scanner.getValue());

		// read token location
		int line = scanner.getLine();
		int startColumn = scanner.getColumn();
		
		sym.setLocation(line, startColumn, line, startColumn + scanner.getLength());
		return sym;
	}
	
	/**
	 * A factory method that an implementation must override to generate instances of
	 * a class of terminals that carries recognized text and that semantic action code
	 * might reference (store away).
	 * There are no constraints on what this object might be, but if it implements the
	 * Location protocol, Parser will use it to set token's location.
	 * 
	 * @param value a token value recognized by the scanner 
	 * @return an object that carries a terminal payload
	 */
	protected abstract Object makeTerm(Object value);

	/**
	 * Parser will try to recover from a syntax error here.
	 * If successful it'll alter a "stream" of tokens in such a way that at least it can continue parsing for a while.
	 * Otherwise it'll give up by throwing a syntax error exception.
	 *
	 * @param sym unexpected symbol
	 * @throws SyntaxErrorException
	 */
	protected void recoverFromError(Symbol sym, Scanner input) throws SyntaxErrorException, IOException
	{
		reuse(sym);
		throw new SyntaxErrorException();
	}

	/**
	 * A callback that is called by the parser when scanner failed to recognize next token and threw the
	 * UnrecognizedCharacterException
	 *
	 * @param text partially recognized token where the last character was unexpected
	 * @param line where the failed token was found
	 * @param column of the first character of the unrecognized token
	 */
	protected void onUnexpectedCharacterError(String text, int line, int column)
	{
		// Do nothing here. Implementation overrides it for reporting.
	}

	/**
	 * A callback that is called when the terminal symbol was not expected.
	 *
	 * @param sym terminal that was not expected
	 */
	protected void onSyntaxError(Symbol sym)
	{
		// Do nothing here. Implementation overrides it for reporting.
	}

	/**
	 * Expands states array
	 */
	static short[] expand(short[] array, int newLength)
	{
		short[] expanded = new short[newLength];
		System.arraycopy(array, 0, expanded, newLength - array.length, array.length);
		return expanded;
	}
}
