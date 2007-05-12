/***
 * Beaver: compiler front-end construction toolkit                       
 * Copyright (c) 2003-2007 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.lexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CharScannerClassWriter extends ScannerWriter implements Opcodes
{
	private static final String   SUPER       = "beaver/CharScanner";
	private static final String[] NEXT_THROWS = { "beaver/UnexpectedCharacterException", "java/io/IOException" };

	private static final int      ARG1        = 1;
	// private static final int ARG2 = 2;

	private static final int      THIS        = 0;
	private static final int      CURSOR      = 1;
	private static final int      CHR         = 2;
	private static final int      ACCEPT      = 3;
	private static final int      TEXT        = 4;
	private static final int      EVENT       = 5;

	protected byte[] assemble(DFA defaultDFA, String defaultName, DFA[] incDFAs, String[] incNames, DFA[] excDFAs, String[] excNames, String className)
	{
		DFA[] dfas = new DFA[1 + (incDFAs != null ? incDFAs.length : 0) + (excDFAs != null ? excDFAs.length : 0)];
		
		int lastIncDFAId = 0;	
		int lastDFAId = 0;
		dfas[lastDFAId] = defaultDFA;
		if ( incDFAs != null )
		{
			for ( int i = 0; i < incDFAs.length; i++ )
            {
				dfas[++lastDFAId] = incDFAs[i];
            }
			lastIncDFAId = lastDFAId;
		}
		if ( excDFAs != null )
		{
			for ( int i = 0; i < excDFAs.length; i++ )
            {
				dfas[++lastDFAId] = excDFAs[i];
            }
		}
		
		Map events = new HashMap();
		int maxFill = 0;
		for (int i = 0; i < dfas.length; i++)
		{
			DFA dfa = dfas[i];

			if (dfa.maxFill >= Byte.MAX_VALUE)
				throw new IllegalArgumentException("DFA's 'max fill' value is unacceptably large");
			
			if (maxFill < dfa.maxFill)
			{
				maxFill = dfa.maxFill;
			}
			
			for (DFA.State st = dfa.start; st != null; st = st.next)
			{
				if (st.eventName != null)
				{
					DFA.State orig = (DFA.State) events.get(st.eventName);
					if ( orig == null )
					{
						st.eventId = events.size();
						events.put(st.eventName, st);
					}
					else
					{
						st.eventId = orig.eventId;
					}
				}
			}
		}
		boolean lexerHasStates = dfas.length > 1;

		ClassWriter cw = new ClassWriter(0);
		cw.visit(V1_1, ACC_PUBLIC + ACC_SUPER + (events.isEmpty() ? 0 : ACC_ABSTRACT), className, null, SUPER, null);

		if (lexerHasStates)
		{
			FieldVisitor fv = cw.visitField(ACC_PROTECTED, "state", "I", null, null);
			fv.visitEnd();
			int id = 0;
			fv = cw.visitField(ACC_PROTECTED | ACC_FINAL, defaultName, "I", null, new Integer(id++));
			fv.visitEnd();
			if ( incNames != null )
			{
				for ( int i = 0; i < incNames.length; i++ )
                {
					fv = cw.visitField(ACC_PROTECTED | ACC_FINAL, incNames[i], "I", null, new Integer(id++));
					fv.visitEnd();
                }
			}
			if ( excNames != null )
			{
				for ( int i = 0; i < excNames.length; i++ )
                {
					fv = cw.visitField(ACC_PROTECTED | ACC_FINAL, excNames[i], "I", null, new Integer(id++));
					fv.visitEnd();
                }
			}
		}

		// public Constructor(java.io.Reader)
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/io/Reader;)V", null, null);
			mv.visitVarInsn(ALOAD, THIS);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(BIPUSH, maxFill);
			mv.visitMethodInsn(INVOKESPECIAL, SUPER, "<init>", "(Ljava/io/Reader;I)V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 2);
			mv.visitEnd();
		}

		if ( !events.isEmpty() )
		{
			// private int event(int eventId)

			MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "event", "(I)Z", null, null);

			mv.visitVarInsn(ILOAD, ARG1);
			Label returnTrue = new Label();
			Label[] eventCases = makeLabels(events.size());

			mv.visitTableSwitchInsn(0, events.size() - 1, returnTrue, eventCases);

			List eventDefStates = new ArrayList(events.values());
			Collections.sort(eventDefStates, new Comparator()
			{
                public int compare(Object o1, Object o2)
                {
	                return ((DFA.State) o1).eventId - ((DFA.State) o2).eventId;
                }
			});
			int n = 0;
			for ( Iterator i = eventDefStates.iterator(); i.hasNext(); n++ )
            {
	            DFA.State st = (DFA.State) i.next();
	            
				mv.visitLabel(eventCases[n]);
				mv.visitVarInsn(ALOAD, THIS);
				mv.visitMethodInsn(INVOKEVIRTUAL, className, "on" + st.eventName, "()Z");
				mv.visitInsn(IRETURN);
            }
			mv.visitLabel(returnTrue);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IRETURN);

			mv.visitMaxs(1, 2);
			mv.visitEnd();

			for ( Iterator i = events.keySet().iterator(); i.hasNext(); )
            {
	            String eventName = (String) i.next();
	            
				mv = cw.visitMethod(ACC_PROTECTED + ACC_ABSTRACT, "on" + eventName, "()Z", null, null);
				mv.visitEnd();
            }
		}

		// public int next() throws UnexpectedCharacterException, IOException
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "next", "()I", null, NEXT_THROWS);
			Label startScan = new Label()
		        , tryDefaultDFA = new Label()
			    , returnAccepted = new Label()
			    , unexpectedChar = new Label()
			    , throwUnexpectedCharException = new Label()
			    , throwIllegalStateException = new Label();

			// int cursor = super.cursor;
			mv.visitVarInsn(ALOAD, THIS);
			mv.visitFieldInsn(GETFIELD, SUPER, "cursor", "I");
			mv.visitVarInsn(ISTORE, CURSOR);

			// char[] text = super.text;
			mv.visitVarInsn(ALOAD, THIS);
			mv.visitFieldInsn(GETFIELD, SUPER, "text", "[C");
			mv.visitVarInsn(ASTORE, TEXT);

			mv.visitLabel(startScan);

			// super.start = cursor
			mv.visitVarInsn(ALOAD, THIS);
			mv.visitVarInsn(ILOAD, CURSOR);
			mv.visitFieldInsn(PUTFIELD, SUPER, "start", "I");

			// int accept = 0;
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, ACCEPT);
			if (!events.isEmpty())
			{
				mv.visitInsn(ICONST_M1);
				mv.visitVarInsn(ISTORE, EVENT);
			}

			if (lexerHasStates)
			{
				Label[] dfaLabels = makeLabels(dfas.length);
				mv.visitVarInsn(ALOAD, THIS);
				mv.visitFieldInsn(GETFIELD, className, "state", "I");
				mv.visitTableSwitchInsn(0, lastDFAId, throwIllegalStateException, dfaLabels);
				
				mv.visitLabel(tryDefaultDFA);
				// if (accept != 0)
				mv.visitVarInsn(ILOAD, ACCEPT);
				mv.visitJumpInsn(IFNE, returnAccepted);

				int id = 0; // default DFA
				{
    				mv.visitLabel(dfaLabels[id]); 
    				compile(mv, className, !events.isEmpty(), dfas[id], startScan, unexpectedChar);
    				id++;
				}				
				while ( id <= lastIncDFAId )
				{
					mv.visitLabel(dfaLabels[id]); 
					compile(mv, className, !events.isEmpty(), dfas[id], startScan, tryDefaultDFA);
					id++;
				}
				while ( id <= lastDFAId )
				{
					mv.visitLabel(dfaLabels[id]); 
					compile(mv, className, !events.isEmpty(), dfas[id], startScan, unexpectedChar);
					id++;
				}
			}
			else
			{
				compile(mv, className, !events.isEmpty(), defaultDFA, startScan, unexpectedChar);
			}
			
			mv.visitLabel(unexpectedChar);
			// if (accept == 0)
			mv.visitVarInsn(ILOAD, ACCEPT);
			mv.visitJumpInsn(IFEQ, throwUnexpectedCharException);
			// else
			mv.visitLabel(returnAccepted);
			// super.cursor = cursor = super.marker;
			mv.visitVarInsn(ALOAD, THIS);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(GETFIELD, SUPER, "marker", "I");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ISTORE, CURSOR);
			mv.visitFieldInsn(PUTFIELD, SUPER, "cursor", "I");
			if (!events.isEmpty())
			{
				Label endOfEvent = new Label();

				mv.visitVarInsn(ILOAD, EVENT);
				mv.visitJumpInsn(IFLT, endOfEvent);

				mv.visitVarInsn(ALOAD, THIS);
				mv.visitVarInsn(ILOAD, EVENT);
				mv.visitMethodInsn(INVOKESPECIAL, className, "event", "(I)Z");
				mv.visitJumpInsn(IFEQ, startScan);

				mv.visitLabel(endOfEvent);
			}
			// if (accept < 0)
			mv.visitVarInsn(ILOAD, ACCEPT);
			mv.visitJumpInsn(IFLT, startScan);
			// else
			// return accept;
			mv.visitVarInsn(ILOAD, ACCEPT);
			mv.visitInsn(IRETURN);

			mv.visitLabel(throwUnexpectedCharException);
			mv.visitVarInsn(ALOAD, THIS);
			mv.visitVarInsn(ILOAD, CURSOR);
			mv.visitFieldInsn(PUTFIELD, SUPER, "cursor", "I");
			mv.visitTypeInsn(NEW, "beaver/UnexpectedCharacterException");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "beaver/UnexpectedCharacterException", "<init>", "()V");
			mv.visitInsn(ATHROW);

			if (lexerHasStates)
			{
				mv.visitLabel(throwIllegalStateException);
				mv.visitTypeInsn(NEW, "java/lang/IllegalStateException");
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "()V");
				mv.visitInsn(ATHROW);
			}
			mv.visitMaxs(4, events.isEmpty() ? TEXT + 1 : EVENT + 1);
			mv.visitEnd();
		}

		// private int checkAvail(int cursor, int n)
//		 {
//			MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "checkAvail", "(II)I", null, null);
//			Label refill = new Label();
//			// if (super.limit - cursor < n)
//			mv.visitVarInsn(ALOAD, THIS);
//			mv.visitFieldInsn(GETFIELD, SUPER, "limit", "I");
//			mv.visitVarInsn(ILOAD, CURSOR);
//			mv.visitInsn(ISUB);
//			mv.visitVarInsn(ILOAD, ARG2);
//			mv.visitJumpInsn(IF_ICMPLT, refill);
//			mv.visitVarInsn(ILOAD, CURSOR);
//			mv.visitInsn(IRETURN);
//			mv.visitLabel(refill);
//			mv.visitVarInsn(ALOAD, THIS);
//			mv.visitVarInsn(ILOAD, ARG2);
//			mv.visitVarInsn(ILOAD, CURSOR);
//			mv.visitMethodInsn(INVOKEVIRTUAL, SUPER, "fill", "(II)I");
//			mv.visitInsn(IRETURN);
//			mv.visitMaxs(2, 3);
//			mv.visitEnd();
//		}

		// private saveCursor(int cursor)
		{
			MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "saveCursor", "(I)V", null, null);
			Label loadArg = new Label(), doSaveCursor = new Label();
			// super.cursor = super.ctxptr > 0 ? super.ctxptr : cursor;
			mv.visitVarInsn(ALOAD, THIS);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(GETFIELD, SUPER, "ctxptr", "I");
			mv.visitJumpInsn(IFLE, loadArg);

			mv.visitVarInsn(ALOAD, THIS);
			mv.visitFieldInsn(GETFIELD, SUPER, "ctxptr", "I");
			mv.visitJumpInsn(GOTO, doSaveCursor);

			mv.visitLabel(loadArg);
			mv.visitVarInsn(ILOAD, ARG1);

			mv.visitLabel(doSaveCursor);
			mv.visitFieldInsn(PUTFIELD, SUPER, "cursor", "I");

			// super.ctxptr = 0;
			mv.visitVarInsn(ALOAD, THIS);
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTFIELD, SUPER, "ctxptr", "I");

			mv.visitInsn(RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}

		// private newLine()
		{
			MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "newLine", "()V", null, null);
			// super.lineNum += 1;
			mv.visitVarInsn(ALOAD, THIS);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(GETFIELD, SUPER, "lineNum", "I");
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitFieldInsn(PUTFIELD, SUPER, "lineNum", "I");
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 1);
			mv.visitEnd();
		}

		return cw.toByteArray();
	}

	private static void compile(MethodVisitor mv, String className, boolean hasEvents, DFA dfa, Label startScan, Label unexpectedChar)
	{
		Label[] dfaStateLabels = makeLabels(dfa.nStates);

		for (DFA.State st = dfa.start; st != null; st = st.next)
		{
			mv.visitLabel(dfaStateLabels[st.sid]);
			if (st.move == null)
			{
				if (st.accept > 0)
				{
					mv.visitVarInsn(ALOAD, THIS);
					mv.visitVarInsn(ILOAD, CURSOR);
					mv.visitMethodInsn(INVOKESPECIAL, className, "saveCursor", "(I)V");

					if (st.eventId >= 0)
					{
						mv.visitVarInsn(ALOAD, THIS);
						compileLoadConst(mv, st.eventId);
						mv.visitMethodInsn(INVOKESPECIAL, className, "event", "(I)Z");
						mv.visitJumpInsn(IFEQ, startScan);
					}
					compileLoadConst(mv, st.accept);
					mv.visitInsn(IRETURN);
				}
				else if (st.accept == -1) // EOL
				{
					// super.offset = cursor;
					mv.visitVarInsn(ALOAD, THIS);
					mv.visitVarInsn(ILOAD, CURSOR);
					mv.visitFieldInsn(PUTFIELD, SUPER, "offset", "I");

					// if (accept == -1) goto startScan
					mv.visitVarInsn(ILOAD, ACCEPT);
					mv.visitInsn(ICONST_M1);
					mv.visitJumpInsn(IF_ICMPEQ, startScan);

					// this.newLine();
					mv.visitVarInsn(ALOAD, THIS);
					mv.visitMethodInsn(INVOKESPECIAL, className, "newLine", "()V");

					mv.visitJumpInsn(GOTO, startScan);
				}
				else if (st.accept == -2) // EOF
				{
					mv.visitInsn(ICONST_0);
					mv.visitInsn(IRETURN);
				}
				else if (st.accept <= -3) // ignorable token
				{
					mv.visitJumpInsn(GOTO, startScan);
				}
				else
				{
					throw new IllegalStateException("Invalid accept code (" + st.accept + ") in the final state.");
				}
			}
			else
			{
				if (st.accept != 0)
				{
					mv.visitVarInsn(ALOAD, THIS);
					mv.visitVarInsn(ILOAD, CURSOR);
					mv.visitFieldInsn(PUTFIELD, SUPER, "marker", "I");

					Label acceptSaved = new Label();
					if (st.accept == -1) // EOL
					{
						// this.offset = cursor;
						mv.visitVarInsn(ALOAD, THIS);
						mv.visitVarInsn(ILOAD, CURSOR);
						mv.visitFieldInsn(PUTFIELD, SUPER, "offset", "I");

						// if (accept != -1)
						mv.visitVarInsn(ILOAD, ACCEPT);
						mv.visitInsn(ICONST_M1);
						mv.visitJumpInsn(IF_ICMPEQ, acceptSaved);

						// this.newLine();
						mv.visitVarInsn(ALOAD, THIS);
						mv.visitMethodInsn(INVOKEVIRTUAL, className, "newLine", "()V");
					}
					compileLoadConst(mv, st.accept);
					mv.visitVarInsn(ISTORE, ACCEPT);
					if ( hasEvents )
					{
						compileLoadConst(mv, st.eventId);
						mv.visitVarInsn(ISTORE, EVENT);
					}
					mv.visitLabel(acceptSaved);
				}
				if (st.isPreCtx)
				{
					// save context info
					mv.visitVarInsn(ALOAD, THIS);
					mv.visitVarInsn(ILOAD, CURSOR);
					mv.visitFieldInsn(PUTFIELD, SUPER, "ctxptr", "I");
				}

				if (0 < st.depth && st.depth < Integer.MAX_VALUE)
				{
					compileBufferCheck(mv, st.depth);
				}
				compileLoadCurChar(mv);
				mv.visitIincInsn(CURSOR, 1);

				CharTransition[] ct = getCharTransitions(st);
				if (ct != null)
				{
					if (ct.length > 1)
					{
						compileCharTransitionSwitch(mv, dfaStateLabels, ct);
					}
					else
					{
						compileCharTransitionBranch(mv, dfaStateLabels, ct);
					}
				}
				if ( compileRangeTransitions(mv, dfaStateLabels, unexpectedChar, st) != unexpectedChar)
				{
					mv.visitJumpInsn(GOTO, unexpectedChar);
				}
			}
		}
	}

	private static Label[] makeLabels(int n)
	{
		Label[] labels = new Label[n];
		for (int i = 0; i < n; i++)
		{
			labels[i] = new Label();
		}
		return labels;
	}

//	private static void compileBufferCheck(MethodVisitor mv, String className, int n)
//	{
//		mv.visitVarInsn(ALOAD, THIS);
//		mv.visitVarInsn(ILOAD, CURSOR);
//		compileLoadConst(mv, n);
//		mv.visitMethodInsn(INVOKESPECIAL, className, "checkAvail", "(II)I");
//		mv.visitVarInsn(ISTORE, CURSOR);
//	}

	private static void compileBufferCheck(MethodVisitor mv, int n)
	{
		Label end = new Label();
		mv.visitVarInsn(ALOAD, THIS);
		mv.visitFieldInsn(GETFIELD, SUPER, "limit", "I");
		mv.visitVarInsn(ILOAD, CURSOR);
		mv.visitInsn(ISUB);
		compileLoadConst(mv, n);
		mv.visitJumpInsn(IF_ICMPGE, end);
		mv.visitVarInsn(ALOAD, THIS);
		compileLoadConst(mv, n);
		mv.visitVarInsn(ILOAD, CURSOR);
		mv.visitMethodInsn(INVOKEVIRTUAL, SUPER, "fill", "(II)I");
		mv.visitVarInsn(ISTORE, CURSOR);
		mv.visitLabel(end);
	}

	private static void compileLoadCurChar(MethodVisitor mv)
	{
		mv.visitVarInsn(ALOAD, TEXT);
		mv.visitVarInsn(ILOAD, CURSOR);
		mv.visitInsn(CALOAD);
		mv.visitVarInsn(ISTORE, CHR);
	}

	private static void compileLoadConst(MethodVisitor mv, int c)
	{
		if (Byte.MIN_VALUE <= c && c <= Byte.MAX_VALUE)
		{
			switch (c)
			{
				case -1:
					mv.visitInsn(ICONST_M1);
					break;
				case 0:
					mv.visitInsn(ICONST_0);
					break;
				case 1:
					mv.visitInsn(ICONST_1);
					break;
				case 2:
					mv.visitInsn(ICONST_2);
					break;
				case 3:
					mv.visitInsn(ICONST_3);
					break;
				case 4:
					mv.visitInsn(ICONST_4);
					break;
				case 5:
					mv.visitInsn(ICONST_5);
					break;
				default:
					mv.visitIntInsn(BIPUSH, c);
			}
		}
		else if (Short.MIN_VALUE <= c && c <= Short.MAX_VALUE)
			mv.visitIntInsn(SIPUSH, c);
		else
			mv.visitLdcInsn(new Integer(c));
	}

	private static void compileCharTransitionSwitch(MethodVisitor mv, Label[] stateLabels, CharTransition[] gotos)
	{
		Label[] caseLabels = makeCaseLabels(gotos, stateLabels);
		Label defaultLabel = new Label();

		mv.visitVarInsn(ILOAD, CHR);
		mv.visitLookupSwitchInsn(defaultLabel, getCharTransitionKeys(gotos), caseLabels);
		mv.visitLabel(defaultLabel);
	}

	private static Label[] makeCaseLabels(CharTransition[] gotos, Label[] stateLabels)
	{
		Label[] caseLabels = new Label[gotos.length];
		for (int i = 0; i < gotos.length; i++)
		{
			caseLabels[i] = stateLabels[gotos[i].to];
		}
		return caseLabels;
	}

	private static int[] getCharTransitionKeys(CharTransition[] gotos)
	{
		int[] keys = new int[gotos.length];
		for (int i = 0; i < gotos.length; i++)
		{
			keys[i] = gotos[i].c;
		}
		return keys;
	}

	private static void compileCharTransitionBranch(MethodVisitor mv, Label[] stateLabels, CharTransition[] ct)
	{
		mv.visitVarInsn(ILOAD, CHR);
		compileLoadConst(mv, ct[0].c);
		mv.visitJumpInsn(IF_ICMPEQ, stateLabels[ct[0].to]);
	}

	private static Label compileRangeTransitions(MethodVisitor mv, Label[] stateLabels, Label end, DFA.State st)
	{
		SpanTransition j = getSpanTransitionsTree(st);
		if (j == null)
			return null;
		
		return compile(mv, stateLabels, end, j, 0);
	}

	private static Label compile(MethodVisitor mv, Label[] stateLabels, Label end, SpanTransition s, int depth)
	{
		Label left = s.left != null ? new Label() : null;

		mv.visitVarInsn(ILOAD, CHR);
		compileLoadConst(mv, s.lb);
		mv.visitJumpInsn(IF_ICMPLT, s.left != null ? left : end);

		mv.visitVarInsn(ILOAD, CHR);
		compileLoadConst(mv, s.ub);
		mv.visitJumpInsn(IF_ICMPLT, stateLabels[s.to]);
		
		Label lastGoto = null;
		if (s.right != null)
		{
			lastGoto = compile(mv, stateLabels, end, s.right, depth + 1);
		}
		else if (depth > 0 || s.left != null)
		{
			mv.visitJumpInsn(GOTO, lastGoto = end);
		}
		if (s.left != null)
		{
			mv.visitLabel(left);
			lastGoto = compile(mv, stateLabels, end, s.left, depth + 1);
		}
		return lastGoto;
	}
}
