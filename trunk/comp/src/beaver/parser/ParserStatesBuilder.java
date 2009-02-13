package beaver.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class ParserStatesBuilder
{
	private Map         states = new HashMap();
	private ParserState last;

	ParserState buildParserStates(Grammar grammar)
	{
		ItemSet kernel = new ItemSet();
		kernel.getItem(grammar.productions[0], 0).addLookahead(grammar.getEOF());

		ParserState firstState = getState(kernel);

		int sid = 0;
		for (ParserState state = firstState; state != null; state = state.next)
		{
			state.id = ++sid;
			
			state.config.reverseEmitters();
			state.config.resetContributions();
		}
		propagateLookaheads(firstState);
		buildReduceActions(firstState, grammar.terminals);
		
		firstState.accept = new ParserAction.Accept(grammar);
		
		return firstState;
	}
	
	private ParserState getState(ItemSet kernel)
	{
		kernel.link();
		
		ParserState state = (ParserState) states.get(kernel);
		if (state != null)
		{
			state.config.copyEmitters(kernel);
		}
		else
		{
			kernel.buildClosure();
			state = newState(kernel);
			buildShiftsFrom(state);
		}
		return state;
	}

	private ParserState newState(ItemSet config)
	{
		ParserState state = new ParserState(config);
		if (last == null)
			last = state;
		else
			last = last.next = state;
		states.put(config, state);
		return state;
	}

	private void buildShiftsFrom(ParserState state)
	{
		state.config.resetContributions();

		for (Item item = state.config.getFirstItem(); item != null; item = item.next)
		{
			if (item.hasContributed || item.isDotAfterLastSymbol())
				continue;

			ItemSet kernel = new ItemSet();

			Symbol lookahead = item.getSymbolAfterDot();
			/*
			 * For every item in the "from" state which also has the same symbol after the dot add
			 * the same item to the core under construction but with the dot shifted one symbol to
			 * the right.
			 */
			for (Item x = item; x != null; x = x.next)
			{
				if (!x.hasContributed && !x.isDotAfterLastSymbol() && x.getSymbolAfterDot() == lookahead)
				{
					kernel.getItem(x.production, x.dot + 1).addEmitter(x);
					x.hasContributed = true;
				}
			}
			ParserState shiftTo = getState(kernel);

			// The state "shiftTo" is reached from the the current state by a shift action on the lookahead symbol
			state.add(new ParserAction.Shift(lookahead, shiftTo));
		}
	}

	private static void propagateLookaheads(ParserState firstState)
	{
		for (boolean propagated = true; propagated;)
		{
			propagated = false;

			for (ParserState state = firstState; state != null; state = state.next)
			{
				if (state.config.propagateLookaheads())
				{
					propagated = true;
				}
			}
		}
	}

	private static void buildReduceActions(ParserState firstState, Terminal[] terminals)
	{
		ReduceActionsBuilder builder = new ReduceActionsBuilder(terminals);

		for (ParserState state = firstState; state != null; state = state.next)
		{
			builder.set(state);

			for (Item item = state.config.getFirstItem(); item != null; item = item.next)
			{
				if (item.isDotAfterLastSymbol() && item.lookaheads != null)
				{
					builder.set(item.production);
					item.lookaheads.forEachBitAccept(builder);
				}
			}
		}
	}
	
	static void makeDefaultReduceActions(ParserState firstState, Grammar grammar)
	{
		int[] ruleUseCounts = new int[grammar.productions.length];
		for (ParserState state = firstState; state != null; state = state.next)
		{
			Arrays.fill(ruleUseCounts, 0);
			
			for (ParserAction.Reduce action = (ParserAction.Reduce) state.reduce; action != null; action = (ParserAction.Reduce) action.next)
			{
				ruleUseCounts[action.production.id]++;
			}
			
			int maxUse = 2; // avoid default reductions in minor cases
			int defaultProductionId = -1;
			for (int i = 0; i < ruleUseCounts.length; i++)
            {
				if (ruleUseCounts[i] > maxUse)
				{
					maxUse = ruleUseCounts[i];
					defaultProductionId = i;
				}
            }
			if (defaultProductionId >= 0)
			{
				state.createDefaultReduceAction(defaultProductionId);
			}
		}
	}
}
