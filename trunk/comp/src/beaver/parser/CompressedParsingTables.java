package beaver.parser;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class CompressedParsingTables
{
	ParserState[] states;
	ParserState   firstState;
	Symbol[]      symbols;
	int[]         symbolIdxMap;
	int[]         stateActionsMinIdx;
	int[]         stateActionsMaxIdx;
	int[]         stateActionsOffset;
	int           packedActionsSize;
	int[]         packedParserActions;
	int[]         packedParserActionCtrlIdxs;

	CompressedParsingTables()
	{
	}
	
	CompressedParsingTables(ParserState firstState)
	{
		init(firstState);
		packParserActions();
	}
	
	void init(ParserState firstState)
	{
		this.firstState = firstState;
		this.states = ParserState.toArray(firstState);
		Arrays.sort(states, ParserState.CMP_NUM_ACTIONS);
		
		this.symbols = collectLookaheads(firstState);
		Arrays.sort(symbols, Symbol.CMP_NUM_STATES);
		symbolIdxMap = new int[symbols.length];
		for (int i = 0; i < symbols.length; i++)
        {
			symbolIdxMap[symbols[i].id] = i;
        }
		
		// find action lookahead boundaries
		this.stateActionsMinIdx = new int[states.length];
		this.stateActionsMaxIdx = new int[states.length];
		int maxCompressedSize = 0;
		for (int i = 0; i < states.length; i++)
        {
			int min = symbols.length;
			int max = -1;
			ParserState state = states[i];
			
			for (ParserAction action = state.shift; action != null; action = action.next)
			{
				int idx = symbolIdxMap[action.lookahead.id];
				if (idx < min)
				{
					min = idx;
				}
				if (idx > max)
				{
					max = idx;
				}
			}
			for (ParserAction action = state.reduce; action != null; action = action.next)
			{
				int idx = symbolIdxMap[action.lookahead.id];
				if (idx < min)
				{
					min = idx;
				}
				if (idx > max)
				{
					max = idx;
				}
			}
			if (state.accept != null)
			{
				int idx = symbolIdxMap[state.accept.lookahead.id];
				if (idx < min)
				{
					min = idx;
				}
				if (idx > max)
				{
					max = idx;
				}
			}
			stateActionsMinIdx[i] = min;
			stateActionsMaxIdx[i] = max;
			
			maxCompressedSize += max - min + 1;
        }
		
		this.packedParserActions = new int[maxCompressedSize];
		this.packedParserActionCtrlIdxs = new int[maxCompressedSize];
		for (int i = 0; i < packedParserActionCtrlIdxs.length; i++)
        {
	        packedParserActionCtrlIdxs[i] = -1;
        }
		
		this.stateActionsOffset = new int[states.length];
	}
	
	void packParserActions()
	{
		int[] stateActions = new int[symbols.length];
		for (int i = 0; i < states.length; i++)
        {
			loadStateActions(i, stateActions);
			packStateActions(i, stateActions);
        }		
	}
	
	void writeTo(DataOutput data) throws IOException
	{
		data.writeChar(symbolIdxMap.length);
		for (int i = 0; i < symbolIdxMap.length; i++)
        {
			data.writeChar(symbolIdxMap[i]);
        }
		int[] stateIdxMap = new int[states.length + 1];
		for (int i = 0; i < states.length; i++)
        {
			stateIdxMap[states[i].id] = i;
        }
		data.writeChar(states.length);
		for (int stateId = 1; stateId <= states.length; stateId++)
		{
			data.writeChar(stateActionsMinIdx[stateIdxMap[stateId]]);
		}
		for (int stateId = 1; stateId <= states.length; stateId++)
		{
			data.writeChar(stateActionsMaxIdx[stateIdxMap[stateId]]);
		}
		int minStateActionsOffset = Integer.MAX_VALUE;
		for (int i = 0; i < stateActionsOffset.length; i++)
        {
	        if (stateActionsOffset[i] < minStateActionsOffset)
	        {
	        	minStateActionsOffset = stateActionsOffset[i];
	        }
        }
		data.writeShort(minStateActionsOffset);
		for (int stateId = 1; stateId <= states.length; stateId++)
		{
			data.writeChar(stateActionsOffset[stateIdxMap[stateId]] - minStateActionsOffset);
		}
		data.writeChar(packedActionsSize);
		for (int i = 0; i < packedActionsSize; i++)
        {
			data.writeShort(packedParserActions[i]);
        }
		for (int i = 0; i < packedActionsSize; i++)
        {
			data.writeShort(packedParserActionCtrlIdxs[i]);
        }
	}
	
	void loadStateActions(int stateIdx, int[] stateActions)
	{
		// prepare the array of actions
		for (int i = 0; i < stateActions.length; i++)
        {
	        stateActions[i] = 0;
        }
		ParserAction action;
		// set IDs of state actions
		for (action = states[stateIdx].shift; action != null; action = action.next)
		{
			stateActions[symbolIdxMap[action.lookahead.id]] = action.getId();
		}
		for (action = states[stateIdx].reduce; action != null; action = action.next)
		{
			stateActions[symbolIdxMap[action.lookahead.id]] = action.getId();
		}
		if ((action = states[stateIdx].accept) != null)
		{
			stateActions[symbolIdxMap[action.lookahead.id]] = action.getId();
		}
	}
	
	void packStateActions(int stateIdx, int[] stateActions)
	{
		int packedActionsIdx = findFirstStateActionPackingIndex(-1, stateIdx, stateActions);
		while (!canStateActionsBePackedAt(packedActionsIdx, stateIdx, stateActions))
		{
			packedActionsIdx = findFirstStateActionPackingIndex(packedActionsIdx, stateIdx, stateActions);
		}		
		stateActionsOffset[stateIdx] = packedActionsIdx - stateActionsMinIdx[stateIdx];
		for (int i = stateActionsMinIdx[stateIdx]; i <= stateActionsMaxIdx[stateIdx]; i++)
		{
			if (stateActions[i] != 0)
			{
				packedParserActions[packedActionsIdx] = stateActions[i];
				packedParserActionCtrlIdxs[packedActionsIdx] = i;
			}
			packedActionsIdx++;
		}
		if (packedActionsIdx > packedActionsSize)
		{
			packedActionsSize = packedActionsIdx; 
		}
	}
	
	private int findFirstStateActionPackingIndex(int lastTriedPackedActionsIdx, int stateIdx, int[] stateActions)
	{
		int packedActionsIdx = lastTriedPackedActionsIdx + 1;
		while (!canActionBePackedAt(packedActionsIdx, stateActions, stateActionsMinIdx[stateIdx]))
		{
			packedActionsIdx++;
		}
		return packedActionsIdx;
	}
	
	private boolean canActionBePackedAt(int packedActionsIdx, int[] stateActions, int stateActionIdx)
	{
		return packedParserActions[packedActionsIdx] == 0 
			   ||
			   packedParserActions[packedActionsIdx] == stateActions[stateActionIdx] 
			   && 
			   packedParserActionCtrlIdxs[packedActionsIdx] == stateActionIdx;
	}
	
	private boolean canStateActionsBePackedAt(int packedActionsIdx, int stateIdx, int[] stateActions)
	{
		for (int ai = stateActionsMinIdx[stateIdx], pi = packedActionsIdx; ai <= stateActionsMaxIdx[stateIdx]; ai++, pi++)
		{			
			if (stateActions[ai] != 0 && !canActionBePackedAt(pi, stateActions, ai))
			{
				return false;
			}
		}
		return !willStateActionsConflictWithAlreadyPackedActions(packedActionsIdx, stateIdx, stateActions);
	}
	
	private boolean willStateActionsConflictWithAlreadyPackedActions(int packedActionsIdx, int stateIdx, int[] stateActions)
	{
		int thisStateIdxRangeFrom = packedActionsIdx;
		int thisStateIdxRangeThru = packedActionsIdx + (stateActionsMaxIdx[stateIdx] - stateActionsMinIdx[stateIdx]);
		
		for (int si = 0; si < stateIdx; si++)
		{
			int checkStateIdxRangeFrom = stateActionsOffset[si] + stateActionsMinIdx[si];
			int checkStateIdxRangeThru = stateActionsOffset[si] + stateActionsMaxIdx[si];
			
			if (indexRangesIntersect(thisStateIdxRangeFrom, thisStateIdxRangeThru, checkStateIdxRangeFrom, checkStateIdxRangeThru))
			{
				int packIndexFrom = getIntersectionLowerBound(thisStateIdxRangeFrom, thisStateIdxRangeThru, checkStateIdxRangeFrom, checkStateIdxRangeThru);
				int packIndexThru = getIntersectionUpperBound(thisStateIdxRangeFrom, thisStateIdxRangeThru, checkStateIdxRangeFrom, checkStateIdxRangeThru);
				
				for (int pi = packIndexFrom; pi <= packIndexThru; pi++)
				{
					int thisStateLookaheadIndex = pi - thisStateIdxRangeFrom + stateActionsMinIdx[stateIdx];
					/*
					 * Collision occurs when an action slot of one state intersects with the empty slot of another. Normally
					 * an empty slot results in no-action for the state, i.e. in "unexpected token" in parsing. Collision
					 * indicates that the parser's action lookup algorithm will "borrow" an action from another state in cases
					 * when a no action should be returned.      
					 * 
					 * There can be two kinds of collisions:
					 *  (1) the previously packed state "borrows" an action from this state that we are trying to pack now
					 *  (2) this state will "borrow" an already packed action
					 */
					if (stateActions[thisStateLookaheadIndex] != 0)
					{
						/*
						 * To detect collisions of he first kind we check every empty slot in the intersection range and
						 * see if any erroneous lookaheads encountered while in the other state, i.e. the state this one
						 * intersects with, would result in a "valid" action if we put new action in that (currently empty)
						 * slot. 
						 */
						if (packedParserActions[pi] == 0)
						{
							int checkStateErrorneousLookaheadIndex = pi - checkStateIdxRangeFrom + stateActionsMinIdx[si];
							/*
							 * If indexes are different then parser will not see this action as a valid action in the other
							 * state, but if they are the same we have a collision. 
							 */
							if (thisStateLookaheadIndex == checkStateErrorneousLookaheadIndex)
							{
								return true;
							}
						}
					}
					else
					{
						/*
						 * To detect collisions of the second kind we simply check whether the index of the erroneous lookahead
						 * for the current state matches lookahead index of the packed action.  
						 */
						if (packedParserActionCtrlIdxs[pi] == thisStateLookaheadIndex)
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	private static boolean indexRangesIntersect(int from1, int thru1, int from2, int thru2)
	{
		return thru1 >= from2 && from1 <= thru2;
	}
	
	private static int getIntersectionLowerBound(int from1, int thru1, int from2, int thru2)
	{
		return from1 >= from2 ? from1 : from2;
	}

	private static int getIntersectionUpperBound(int from1, int thru1, int from2, int thru2)
	{
		return thru1 <= thru2 ? thru1 : thru2;
	}

	/**
	 * Collects lookahead symbols from all actions of all states.
	 * 
	 * @param states
	 * @return array of all lookahead symbols
	 */
	private static Symbol[] collectLookaheads(ParserState firstState)
	{
		Set symbols = new HashSet();
		for (ParserState state = firstState; state != null; state = state.next)
		{
			for (ParserAction action = state.shift; action != null; action = action.next)
			{
				symbols.add(action.lookahead);
			}
			for (ParserAction action = state.reduce; action != null; action = action.next)
			{
				symbols.add(action.lookahead);
			}
			if (state.accept != null)
			{
				symbols.add(state.accept.lookahead);
			}
		}
		return (Symbol[]) symbols.toArray(new Symbol[symbols.size()]);
	}
}
