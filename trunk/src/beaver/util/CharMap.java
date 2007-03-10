/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.util;

public class CharMap
{
	Entry[] table;
	int     size;
	int     resize_threshold;
	
	public CharMap()
	{
		useTable(new Entry[256]);
	}
	
	public void put(char c, Object value)
	{
		int i = getIndex(c, table);
		for (Entry e = table[i]; e != null; e = e.next)
		{
			if (e.c == c)
			{
				e.v = value;
				return;
			}
		}
		table[i] = new Entry(c, value, table[i]);
		if (++size > resize_threshold)
		{
			resize();
		}
	}
	
	public Object get(char c)
	{
		int i = getIndex(c, table);
		for (Entry e = table[i]; e != null; e = e.next)
		{
			if (e.c == c)
			{
				return e.v;
			}
		}
		return null;
	}
	
	public void clear()
	{
		for (int i = 0; i < table.length; i++)
		{
			table[i] = null;
		}
	}
	
	public void forEachEntryAccept(ValueVisitor proc)
	{
		for (int i = 0; i < table.length; i++)
		{
			for (Entry e = table[i]; e != null; e = e.next)
			{
				proc.visit(e.v);
			}
		}
	}
	
	public void forEachEntryAccept(EntryVisitor proc)
	{
		for (int i = 0; i < table.length; i++)
		{
			for (Entry e = table[i]; e != null; e = e.next)
			{
				proc.visit(e.c, e.v);
			}
		}
	}
	
	private void resize()
	{
		Entry[] new_table = new Entry[table.length * 2];
		for (int i = 0; i < table.length; i++)
		{
			Entry e = table[i];
			while (e != null)
			{
				Entry n = e.next;
				
				int idx = getIndex(e.c, new_table);
				e.next = new_table[idx];
				new_table[idx] = e; 
				
				e = n;
			}
		}
		useTable(new_table);
	}
	
	private void useTable(Entry[] t)
	{
		table = t;
		resize_threshold = (table.length >> 1) + (table.length >> 2) + (table.length >> 3);
	}
	
	private static int getIndex(int key, Entry[] table)
	{
        key += ~(key << 9);
        key ^=  (key >>> 14);
        key +=  (key << 4);
        key ^=  (key >>> 10);
        return key & (table.length - 1);
	}
	
	static class Entry
	{
		char   c;
		Object v;
		Entry  next;
		
		Entry(char c, Object v, Entry  next)
		{
			this.c = c;
			this.v = v;
			this.next = next;
		}
	}
	
	public static interface EntryVisitor
	{
		void visit(char key, Object value);
	}

	public static interface ValueVisitor
	{
		void visit(Object value);
	}
}
