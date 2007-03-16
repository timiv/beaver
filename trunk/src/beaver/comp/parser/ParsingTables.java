/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */

package beaver.comp.parser;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import beaver.util.BitSet;

/**
 * @author Alexander Demenchuk
 *
 */
public class ParsingTables
{
	Terminal[]    terminals;
	NonTerminal[] nonterminals;
	int[]         ruleDefs;
	char          firstTerminalWithValueId;
	char          errorSymbolId;
	short[]       parserActions;
	char[]        actLookaheads;
	char[][]      offsets;
	char		  offsetShift;
	char          symbolIdToGroupIndexShift;
	Collection 	  defaultReduceRuleLookaheadsSets;
	char[]		  defaultReduceRuleLookaheadsSetIndexes;
	short[]       defaultActions;
	
    public ParsingTables(Grammar grammar, State firstState)
    {
    	this.terminals = grammar.terminals;
    	this.nonterminals = grammar.nonterminals;
    	
    	ruleDefs = new int[grammar.productions.length];
        for ( int i = 0; i < grammar.productions.length; i++ )
        {
        	ruleDefs[i] = (grammar.productions[i].lhs.id << 16) + grammar.productions[i].rhs.length;
        }
        
        for ( char i = 1; i < grammar.terminals.length; i++ )
        {
        	if ( !(grammar.terminals[i] instanceof Terminal.Const) )
        	{
        		firstTerminalWithValueId = i;
        		break;
        	}
        }
        errorSymbolId = grammar.error.id;
        
        ActionTableBuilder builder = new ActionTableBuilder(grammar, firstState);
        builder.buildTables();
        
        this.parserActions = builder.parserActions;
        this.actLookaheads = builder.actLookaheads;
        this.offsetShift   = builder.offsetShift;
        this.symbolIdToGroupIndexShift = (char) BitSet.countBits(~builder.grouppingMask);       
        this.offsets = builder.offsets;
        
        this.defaultReduceRuleLookaheadsSets = State.collateDefaultReduceRuleLookaheadsSets(firstState);       
        this.defaultReduceRuleLookaheadsSetIndexes = new char[builder.numStates + 1];
        for ( State s = firstState; s != null; s = s.next )
        {
        	defaultReduceRuleLookaheadsSetIndexes[s.id] = (char) s.defaultReduceRuleLookaheadsSetIndex;
        }
        
        this.defaultActions = new short[builder.numStates + 1];
        for ( State s = firstState; s != null; s = s.next )
        {
        	if ( s.defaultReduceRule != null )
        	{
        		defaultActions[s.id] = (short) ~s.defaultReduceRule.id;
        	}
        }
    }
    
    public void writeTo(DataOutput out) throws IOException
    {
    	out.writeByte('#');
    	out.writeByte('A'); // format version
    	
    	out.writeChar(parserActions.length);
    	for ( int i = 0; i < parserActions.length; i++ )
        {
	        out.writeShort(parserActions[i]);
        }   	

    	out.writeChar(actLookaheads.length);
    	for ( int i = 0; i < actLookaheads.length; i++ )
        {
	        out.writeChar(actLookaheads[i]);
        }
    	
    	out.writeChar(offsets.length);
    	out.writeChar(offsets[0].length);   	
		for ( int i = 0; i < offsets.length; i++ )
        {
	        for ( int j = 0; j < offsets[0].length; j++ )
            {
	            out.writeChar(offsets[i][j]);
            }
        }
		
    	out.writeChar(symbolIdToGroupIndexShift);
    	out.writeChar(offsetShift);    	
		
    	BitSetSerializer bitSetSerializer = new BitSetSerializer(terminals.length + nonterminals.length); 
		out.writeChar(defaultReduceRuleLookaheadsSets.size());
		for (Iterator i = defaultReduceRuleLookaheadsSets.iterator(); i.hasNext();)
        {
	        bitSetSerializer.reset();
	        ((BitSet) i.next()).forEachBitAccept(bitSetSerializer);
	        bitSetSerializer.writeTo(out);
        }
		
		out.writeChar(defaultReduceRuleLookaheadsSetIndexes.length);
		for ( int i = 0; i < defaultReduceRuleLookaheadsSetIndexes.length; i++ )
        {
            out.writeChar(defaultReduceRuleLookaheadsSetIndexes[i]);
        }
		
		out.writeChar(defaultActions.length);
    	for ( int i = 0; i < defaultActions.length; i++ )
    	{
    		out.writeShort(defaultActions[i]);
    	}
		
    	out.writeChar(ruleDefs.length);
    	for ( int i = 0; i < ruleDefs.length; i++ )
        {
	        out.writeInt(ruleDefs[i]);
        }
    	
    	out.writeChar(firstTerminalWithValueId);
    	out.writeChar(errorSymbolId);
    	
    	out.writeChar(terminals.length + nonterminals.length);
    	for ( int i = 0; i < terminals.length; i++ )
        {
	        out.writeUTF(terminals[i].getRepresentation());
        }
    	for ( int i = 0; i < nonterminals.length; i++ )
        {
	        out.writeUTF(nonterminals[i].getRepresentation());
        }
    }
    
    static class BitSetSerializer implements BitSet.BitVisitor
    {
    	int    lb;
    	char[] bits;
    	int    idx;
    	
    	BitSetSerializer(int maxCapacity)
    	{
    		bits = new char[maxCapacity / 16];
    		lb = -1;
    	}
    	
    	public void visit(int i)
    	{
    		if ( lb < 0 )
    			lb = i;
    		else
    		{
    			int dist = i - lb - 1; // lb is at bit index -1
    			bits[idx = dist >> 4] |= 1 << (dist & 15);
    		}
    	}
    	
    	void writeTo(DataOutput out) throws IOException
    	{
    		out.writeChar(lb);
    		out.writeChar(idx + 1);
    		for ( int i = 0; i <= idx; i++ )
    		{
    			out.writeChar(bits[i]);
    			bits[i] = '\0';
    		}   		
    	}
    	
    	void reset()
    	{
    		lb = -1;
    		for ( int i = 0; i <= idx; i++ )
    		{
    			bits[i] = '\0';
    		}   		
    	}
    }
    
    static class ActionTableBuilder
    {
    	short[]  parserActions;
		char[]   actLookaheads;
		char[][] offsets;

		short[]  backupActions;
		char[]   backupLookaheads;

		int      numStates;
		int      numTerminals;
		char     maxSymbolId;
		char     offsetShift;

		List     actionGroups;
		int      grouppingMask;
		int      groupsPerState;
		
    	ActionTableBuilder(Grammar grammar, State firstState)
    	{
    		numTerminals = grammar.terminals.length;
    		maxSymbolId  = grammar.error.id;
    		offsetShift  = (char) (maxSymbolId + 1);
    		
    		int ub = maxSymbolId | maxSymbolId >> 1;
    		ub |= ub >> 2;
    		ub |= ub >> 4;
    		ub |= ub >> 8;
    		ub += 1;
    		
    		grouppingMask = -ub;
    		groupsPerState = 1;
    		
            actionGroups = new ArrayList();
            List actions = new ArrayList();
            //
            // add ACCEPT action to the first state   
            //
			actions.add(new ActionEntry(grammar.goal.id, Action.getAcceptCode(grammar)));
			
            for ( State s = firstState; s != null; s = s.next )
            {
            	if ( s.shiftActions != null )
            	{
            		for ( Action act = (Action) s.shiftActions.first(); act != null; act = (Action) act.next() )
            		{
            			actions.add(new ActionEntry(act.lookahead.id, act.getCode()));
            		}
            	}
            	if ( s.reduceActions != null )
            	{
            		for ( Action act = (Action) s.reduceActions.first(); act != null; act = (Action) act.next() )
            		{
            			actions.add(new ActionEntry(act.lookahead.id, act.getCode()));
            		}
            	}
            	if ( actions.size() > 0 )
            	{
            		Collections.sort(actions);
            		actionGroups.add(new ActionGroup((short) s.id, (ActionEntry[]) actions.toArray(new ActionEntry[actions.size()]), (char) 0, (char) ub));
                	actions.clear();
            	}
            	numStates++;
            }
            Collections.sort(actionGroups);
            
            int backupSize = ((ActionGroup) actionGroups.get(0)).actions.length;
            backupActions = new short[backupSize];
            backupLookaheads = new char[backupSize];
            
    		parserActions = new short[Character.MAX_VALUE + 1 - offsetShift];
    		actLookaheads = new char[parserActions.length];
    	}
    	
    	void buildTables()
    	{   		
    		int curSize = parserActions.length, minSize = curSize;
    		int minTotalSize = Integer.MAX_VALUE;
    		int curTotalSize = getSerializedSize(curSize = rebuildTables());
    		char[][] curOffsets = getOffsets();
    		char[][] minOffsets;

    		short[] minParserActions = new short[curSize];
    		char[]  minActLookaheads = new char [curSize];
    		do 
    		{
    			System.arraycopy(parserActions, 0, minParserActions, 0, curSize);
    			System.arraycopy(actLookaheads, 0, minActLookaheads, 0, curSize);
    			minSize = curSize;    			
    			minOffsets = curOffsets;
    			minTotalSize = curTotalSize;
    		
    			splitGroups();    			
    			
    			curTotalSize = getSerializedSize(curSize = rebuildTables());
    			curOffsets = getOffsets();
    		}
    		while ( curTotalSize < minTotalSize  );
    		
    		grouppingMask <<= 1; // rollback last split changes
    		groupsPerState >>= 1;
    		offsets = minOffsets;
    		if ( offsets[0].length != groupsPerState )
    			throw new IllegalStateException("wrong offsets table dimensions");
    		
    		parserActions = new short[minSize];
    		actLookaheads = new char [minSize];
			System.arraycopy(minParserActions, 0, parserActions, 0, minSize);
			System.arraycopy(minActLookaheads, 0, actLookaheads, 0, minSize);
    	}
    	
    	private
    	int getSerializedSize(int numEntries)
    	{
    		return numEntries * 4 + numStates * groupsPerState * 2;
    	}
    	
    	private
    	char[][] getOffsets()
    	{
    		int groupIndexShift = BitSet.countBits(~grouppingMask);
    		
    		char[][] offs = new char[numStates + 1][groupsPerState];
    		for (Iterator i = actionGroups.iterator(); i.hasNext();)
            {
	            ActionGroup g = (ActionGroup) i.next();
	            offs[g.state][g.lb >> groupIndexShift] = g.offset;
            }
    		return offs;
    	}
    	
    	private
    	void splitGroups()
    	{		
    		grouppingMask >>= 1;
    		groupsPerState <<= 1;
    		
			Collection detachedGroups = new ArrayList();
			for (Iterator i = actionGroups.iterator(); i.hasNext();)
            {
                ActionGroup g = (ActionGroup) i.next();
                ActionGroup d = g.split();
                if ( d != null )
                {
                	detachedGroups.add(d);
                }
            }
			actionGroups.addAll(detachedGroups);
            Collections.sort(actionGroups);
    	}

    	private
    	int rebuildTables()
    	{
    		Arrays.fill(parserActions, (short) 0);
    		Arrays.fill(actLookaheads, Character.MAX_VALUE);
    		
			int lastIndex = 0;
			int startIndex = 0;
			
			for (Iterator i = actionGroups.iterator(); i.hasNext();)
            {
	            ActionGroup g = (ActionGroup) i.next();
	            
				startIndex = advanceStartIndex(startIndex, g.actions[0]);
				
				for ( int localStartIndex = startIndex; ; )
				{
					char offset = g.findOffset(localStartIndex);
					g.insert(offset);
					if ( !hasCollisions() )
					{
						lastIndex = Math.max(lastIndex, getIndex(g.offset, g.actions[g.actions.length - 1]));
						break;
					}
					g.rollback();
					localStartIndex = advanceStartIndex(getIndex(offset, g.actions[0]) + 1, g.actions[0]);
				}
			}
			return lastIndex + 1;
    	}
    	
    	private
    	boolean hasCollisions()
    	{
			for (Iterator i = actionGroups.iterator(); i.hasNext();)
            {
				ActionGroup g = (ActionGroup) i.next();
    			if ( g.hasCollisions() )
    			{
    				return true;
    			}			
    		}
    		return false;
    	}    	
    	
    	boolean canInsertAtOffset(char offset, ActionEntry act)
    	{
    		int index = getIndex(offset, act);
    		return canInsertAt(index, act.code)
    			&& canInsertAt(index, act.lookahead);
    	}
    	
    	private
    	int getIndex(char offset, ActionEntry act)
    	{
    		return act.lookahead + offset - offsetShift;
    	}

    	boolean canInsertAtIndex(int index, ActionEntry act)
    	{
    		return canInsertAt(index, act.code)
    			&& canInsertAt(index, act.lookahead);
    	}

    	boolean canInsertAt(int index, short act)
    	{
    		return parserActions[index] == 0 || parserActions[index] == act;
    	}

    	boolean canInsertAt(int index, char lookahead)
    	{
    		return actLookaheads[index] == Character.MAX_VALUE || actLookaheads[index] == lookahead;
    	}

    	private
    	int advanceStartIndex(int index, ActionEntry firstAction)
    	{
    		while ( index < parserActions.length && !canInsertAtIndex(index, firstAction) )
    			index++;
    		return index;
    	}
    	
    	
    	static class ActionEntry implements Comparable
    	{
    		char  lookahead;
    		short code;
    		
    		ActionEntry(char la, short code)
    		{
    			lookahead = la;
    			this.code = code;
    		}

    		public int compareTo(Object o)
            {
    	        return lookahead - ((ActionEntry) o).lookahead;
            }
    	}
    	
    	class ActionGroup implements Comparable
    	{
    		ActionEntry[] actions;
			short         state;
			char          lb, ub;
			char          offset;
    		
    		ActionGroup(short state, ActionEntry[] actions, char lb, char ub)
    		{
    			this.state   = state;
				this.actions = actions;
				this.lb      = lb;
				this.ub      = ub;
				this.offset  = 0;
    		}
    		
    		public int compareTo(Object o)
            {
    	        return ((ActionGroup) o).actions.length - actions.length;
            }
    		
    		ActionGroup split()
    		{
    			int m = (ub + lb) / 2;
    			int i = 0;
    			while ( i < actions.length && actions[i].lookahead < m ) i++;
    			
    			if ( i == 0 )
    			{
    				lb = (char) m;
    				return null;
    			}
    			if ( i == actions.length )
    			{
    				ub = (char) m;
    				return null;
    			}
    			
    			ActionEntry[] remainingActions = new ActionEntry[i];
    			System.arraycopy(actions, 0, remainingActions, 0, remainingActions.length);
    			ActionEntry[] detachedActions = new ActionEntry[actions.length - i];
    			System.arraycopy(actions, i, detachedActions, 0, detachedActions.length);

    			char detached_ub = ub; 
    			
    			actions = remainingActions;
    			offset = 0;
    			ub = (char) m;

    			return new ActionGroup(state, detachedActions, (char) m, detached_ub);
    		}
    		
    		char findOffset(int startIndex)
    		{
    			int minLa = actions[0].lookahead;    			
    			int range = actions[actions.length - 1].lookahead + 1 - minLa;
    			while (true)
    			{
    				char endOffset = (char) Math.min(parserActions.length - range - minLa + offsetShift, parserActions.length);
    				
    				for (char offset = (char) (startIndex - minLa + offsetShift); offset < endOffset; offset++)
    				{
    					if (tryInsertAt(offset))
    					{
    						return offset;
    					}
    				}
   					throw new IllegalStateException("cannot find place for some actions in parsing tables");
    			}
    		}

    		private
    		boolean tryInsertAt(char offset)
    		{
    			for (int i = 0; i < actions.length; i++)
    			{
    				if (!canInsertAtOffset(offset, actions[i]))
    				{
    					return false;
    				}
    			}
    			return true;
    		}
    		
    		short findAction(int lookahead)
    		{
    			int l = 0, u = actions.length - 1;
    			while ( u > l)
    			{
    				int m = (u + l) / 2;
    				ActionEntry a = actions[m];
    				if (lookahead == a.lookahead)
    					return a.code;
    				if (lookahead < a.lookahead)
    					u = m - 1;
    				else
    					l = m + 1;
    			}
    			return u >= 0 && lookahead == actions[u].lookahead ? actions[u].code : 0;  
    		}
    		
    		boolean hasCollisions()
    		{
    			if (offset > 0)
    			{
    				int termEnd = ub <= numTerminals ? ub : numTerminals;
    				if (termEnd + offset - offsetShift > actLookaheads.length)
    				{
    					termEnd = actLookaheads.length - offset + offsetShift;
    				}
    				int termStart = lb;
    				if (termStart + offset - offsetShift < 0)
    				{
    					termStart = offsetShift - offset;
    				}
    				for (int t = termStart; t < termEnd; t++)
    				{
    					int i = t + offset - offsetShift;
    					if (actLookaheads[i] == t && parserActions[i] != findAction(t))
    					{
    						return true;
    					}
    				}
    			}
    			return false;
    		}
    	
    		void insert(char offset)
    		{
    			for (int i = 0; i < actions.length; i++)
                {
    	            if (!canInsertAtOffset(offset, actions[i]))
    	            	throw new IllegalStateException("insert collision");
    	            
    	            int index = actions[i].lookahead + offset - offsetShift;
    	            
    	            backupActions[i] = parserActions[index];
    	            parserActions[index] = actions[i].code;
    	            backupLookaheads[i] = actLookaheads[index];
    	            actLookaheads[index] = actions[i].lookahead;
                }
    			this.offset = offset;
    		}
    		
    		void rollback()
    		{
    			for (int i = 0; i < actions.length; i++)
                {
    	            int index = actions[i].lookahead + offset - offsetShift;
    	            
    	            parserActions[index] = backupActions[i];
    	            actLookaheads[index] = backupLookaheads[i];
                }
    			this.offset = 0;
    		}
    	}
    }
}
