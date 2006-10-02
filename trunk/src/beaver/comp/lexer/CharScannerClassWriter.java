/***
 * Beaver: compiler builder framework for Java                       
 * Copyright (c) 2003-2006 Alexander Demenchuk <alder@softanvil.com>  
 * All rights reserved.                       
 *                          
 * See the file "LICENSE" for the terms and conditions for copying,    
 * distribution and modification of Beaver.                            
 */
package beaver.comp.lexer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CharScannerClassWriter extends ScannerWriter implements Opcodes
{
	private File dir;

	public CharScannerClassWriter(File destDir)
	{
		dir = destDir;
	}

	public void compile(String className, DFA[] allDFA) throws IOException
	{
		File clsFile = new File(dir, className + ".class");
		File outDir = clsFile.getParentFile();
		if (!outDir.exists())
		{
			outDir.mkdirs();
		}

		FileOutputStream out = new FileOutputStream(clsFile);
		try
		{
			out.write(compile(allDFA, className));
		}
		finally
		{
			out.close();
		}
	}

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

	public static byte[] compile(DFA[] allDFA, String className)
	{
		ArrayList events = new ArrayList();
		int maxFill = 0;
		for (int i = 0; i < allDFA.length; i++)
		{
			DFA dfa = allDFA[i];

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
					st.eventId = events.size();
					events.add(st.eventName);
				}
			}
		}
		boolean lexerHasStates = allDFA.length > 1;

		ClassWriter cw = new ClassWriter(false);
		cw.visit(V1_1, ACC_PUBLIC + ACC_SUPER + (events.isEmpty() ? 0 : ACC_ABSTRACT), className, null, SUPER, null);

		if (lexerHasStates)
		{
			FieldVisitor fv = cw.visitField(ACC_PROTECTED, "state", "I", null, null);
			fv.visitEnd();
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

		if (!events.isEmpty())
		{
			// private int event(int eventId)

			MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "event", "(I)Z", null, null);

			mv.visitVarInsn(ILOAD, ARG1);
			Label returnTrue = new Label();
			Label[] eventCases = makeLabels(events.size());

			mv.visitTableSwitchInsn(0, events.size() - 1, returnTrue, eventCases);

			int n = events.size();
			for (int i = 0; i < n; i++)
			{
				String eventName = (String) events.get(i);
				mv.visitLabel(eventCases[i]);
				mv.visitVarInsn(ALOAD, THIS);
				mv.visitMethodInsn(INVOKEVIRTUAL, className, eventName, "()Z");
				mv.visitInsn(IRETURN);
			}
			mv.visitLabel(returnTrue);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IRETURN);

			mv.visitMaxs(1, 2);
			mv.visitEnd();

			for (int i = 0; i < n; i++)
			{
				mv = cw.visitMethod(ACC_PROTECTED + ACC_ABSTRACT, (String) events.get(i), "()Z", null, null);
				mv.visitEnd();
			}
		}

		// public int next() throws UnexpectedCharacterException, IOException
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "next", "()I", null, NEXT_THROWS);
			Label startScan = new Label(), unexpectedChar = new Label(), throwUnexpectedCharException = new Label(), throwIllegalStateException = new Label();

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
				Label[] recognizerStateLabels = makeLabels(allDFA.length);
				int[] recognizerStateIds = new int[allDFA.length];
				for (int i = 0; i < allDFA.length; i++)
				{
					recognizerStateIds[i] = i;
				}
				mv.visitVarInsn(ALOAD, THIS);
				mv.visitFieldInsn(GETFIELD, className, "state", "I");
				mv.visitTableSwitchInsn(0, allDFA.length - 1, throwIllegalStateException, recognizerStateLabels);
				for (int i = 0; i < allDFA.length; i++)
				{
					mv.visitLabel(recognizerStateLabels[i]);
					compile(mv, className, events, allDFA[i], startScan, unexpectedChar);
				}
			}
			else
			{
				compile(mv, className, events, allDFA[0], startScan, unexpectedChar);
			}

			mv.visitLabel(unexpectedChar);
			// if (accept == 0)
			mv.visitVarInsn(ILOAD, ACCEPT);
			mv.visitJumpInsn(IFEQ, throwUnexpectedCharException);
			// else
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

	private static void compile(MethodVisitor mv, String className, ArrayList events, DFA dfa, Label startScan,
	    Label unexpectedChar)
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
					if (!events.isEmpty())
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
				compileRangeTransitions(mv, dfaStateLabels, unexpectedChar, st);
				mv.visitJumpInsn(GOTO, unexpectedChar);
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

	private static void compileRangeTransitions(MethodVisitor mv, Label[] stateLabels, Label end, DFA.State st)
	{
		SpanTransition j = getSpanTransitionsTree(st);
		if (j != null)
		{
			int depth = 0;
			compile(mv, stateLabels, end, j, depth);
		}
	}

	private static void compile(MethodVisitor mv, Label[] stateLabels, Label end, SpanTransition s, int depth)
	{
		Label left = s.left != null ? new Label() : null;

		mv.visitVarInsn(ILOAD, CHR);
		compileLoadConst(mv, s.lb);
		mv.visitJumpInsn(IF_ICMPLT, s.left != null ? left : end);

		mv.visitVarInsn(ILOAD, CHR);
		compileLoadConst(mv, s.ub);
		mv.visitJumpInsn(IF_ICMPLT, stateLabels[s.to]);
		if (s.right != null)
		{
			compile(mv, stateLabels, end, s.right, depth + 1);
		}
		else if (depth > 0 || s.left != null)
		{
			mv.visitJumpInsn(GOTO, end);
		}
		if (s.left != null)
		{
			mv.visitLabel(left);
			compile(mv, stateLabels, end, s.left, depth + 1);
		}
	}
}
