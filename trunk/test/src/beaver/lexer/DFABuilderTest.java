package beaver.lexer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DFABuilderTest
{
	@Test
	public void testDecimalScannerStartClosure()
	{
		NFA nfa = new NFA(RegExpTestFixtures.getDecimalScanner());
		DFAClosure closure = new DFAClosure();
		nfa.start.accept(closure);
		assertArrayEquals(new NFANode[] { new NFANode.Char(new CharClass[] { (CharClass) nfa.cset.classes.get('0') }), new NFANode.Char(new CharClass[] { (CharClass) nfa.cset.classes.get(' ') }) }, closure.getKernel());
	}
	
	@Test
	public void testDecimalScannerFirstEdges()
	{
		NFA nfa = new NFA(RegExpTestFixtures.getDecimalScanner());
		DFAClosure closure = new DFAClosure();
		nfa.start.accept(closure);
		NFANode[] kernel = closure.getKernel();
		assertEquals(2, kernel.length);
		
		NFAEdgeCollector edgeCollector = new NFAEdgeCollector();
		kernel[0].accept(edgeCollector);
		assertNull(edgeCollector.accept);
		
		assertEquals(1, edgeCollector.transitions.size());
		NFANode.Array onDigitTransitionsArray = (NFANode.Array) edgeCollector.transitions.get('0'); 
		assertNotNull(onDigitTransitionsArray);
		NFANode[] onDigitTransitions = onDigitTransitionsArray.toArray();
		assertEquals(1, onDigitTransitions.length);
		assertTrue(onDigitTransitions[0] instanceof NFANode.Fork);
		
		edgeCollector.reset();
		kernel[1].accept(edgeCollector);
		assertNull(edgeCollector.accept);
		assertEquals(1, edgeCollector.transitions.size());
		NFANode.Array onWSTransitionsArray = (NFANode.Array) edgeCollector.transitions.get('\t'); 
		assertNotNull(onWSTransitionsArray);
		NFANode[] onWSTransitions = onWSTransitionsArray.toArray();
		assertEquals(1, onWSTransitions.length);
		assertTrue(onWSTransitions[0] instanceof NFANode.Term);
	}
	
	@Test
	public void testDecimalScannerFirstTransitions()
	{
		NFA nfa = new NFA(RegExpTestFixtures.getDecimalScanner());
		DFAClosure closure = new DFAClosure();
		nfa.start.accept(closure);
		NFANode[] kernel = closure.getKernel();
		assertEquals(2, kernel.length);
		
		NFAEdgeCollector edgeCollector = new NFAEdgeCollector();
		kernel[0].accept(edgeCollector);
		assertNull(edgeCollector.accept);
		
		assertEquals(1, edgeCollector.transitions.size());
		NFANode.Array transitionsArray = (NFANode.Array) edgeCollector.transitions.get('0'); 
		assertNotNull(transitionsArray);
		NFANode[] transitions = transitionsArray.toArray();
		assertEquals(1, transitions.length);
		assertTrue(transitions[0] instanceof NFANode.Fork);
		
		closure.reset();
		transitionsArray.accept(closure);
		kernel = closure.getKernel();		
		assertEquals(3, kernel.length);
		
		edgeCollector.reset();
		kernel[0].accept(edgeCollector);
		assertNull(edgeCollector.accept);
		
		assertEquals(1, edgeCollector.transitions.size());
		transitionsArray = (NFANode.Array) edgeCollector.transitions.get('.');
		assertNotNull(transitionsArray);
		transitions = transitionsArray.toArray();
		assertEquals(1, transitions.length);
		assertTrue(transitions[0] instanceof NFANode.Char);
		
		edgeCollector.reset();
		kernel[1].accept(edgeCollector);
		assertEquals(1, edgeCollector.accept.id);
		
		assertEquals(0, edgeCollector.transitions.size());

		edgeCollector.reset();
		kernel[2].accept(edgeCollector);
		assertNull(edgeCollector.accept);
		
		assertEquals(1, edgeCollector.transitions.size());
		transitionsArray = (NFANode.Array) edgeCollector.transitions.get('0');
		assertNotNull(transitionsArray);
		transitions = transitionsArray.toArray();
		assertEquals(1, transitions.length);
		
		// ...
	}
	
	@Test
	public void testCreateStates()
	{
		DFAStateBuilder builder = new DFAStateBuilder();
		builder.createStates(new NFA(RegExpTestFixtures.getDecimalScanner()));
		builder.assignStateIds();
		assertNotNull(builder.first);
	}
}
