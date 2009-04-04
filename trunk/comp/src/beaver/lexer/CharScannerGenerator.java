package beaver.lexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CharScannerGenerator implements Opcodes
{
	private DFA dfa;
	private int eofTokenId;

	public CharScannerGenerator(DFA dfa, int eofTokenId)
	{
		this.dfa = dfa;
		this.eofTokenId = eofTokenId;
	}

	public CharScannerGenerator(DFA dfa)
	{
		this(dfa, 0);
	}

	public byte[] compile(String className)
	{
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cw.visit(V1_1, ACC_PUBLIC | ACC_SUPER, className, null, SUPER, null);

		generateScannerConstructors(cw);
		generateGetNextTokenMethod(cw);

		return cw.toByteArray();
	}

	private void generateScannerConstructors(ClassWriter cw)
	{
		// public Constructor(java.io.Reader)

		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/io/Reader;)V", null, null);
		mv.visitVarInsn(ALOAD, THIS);
		mv.visitVarInsn(ALOAD, ARG1);
		mv.visitMethodInsn(INVOKESPECIAL, SUPER, "<init>", "(Ljava/io/Reader;)V");
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		
		// public Constructor(java.io.Reader,int)

		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/io/Reader;I)V", null, null);
		mv.visitVarInsn(ALOAD, THIS);
		mv.visitVarInsn(ALOAD, ARG1);
		mv.visitVarInsn(ILOAD, ARG2);
		mv.visitMethodInsn(INVOKESPECIAL, SUPER, "<init>", "(Ljava/io/Reader;I)V");
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void generateGetNextTokenMethod(ClassWriter cw)
	{
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getNextToken", "()I", null, EXCEPTIONS);
		Label startScan = new Label();
		Label tokenIsRecognized = new Label();
		Label endOfScan = new Label();
		Label refillBuffer = new Label();

		generateGetNextTokenProlog(mv, startScan);
		generateGetNextTokenDFACode(mv, tokenIsRecognized, endOfScan, refillBuffer);
		generateGetNextTokenEpilog(mv, startScan, tokenIsRecognized, endOfScan);

		generateRefillBuffer(mv, refillBuffer);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private static void generateGetNextTokenProlog(MethodVisitor mv, Label startScan)
	{
		// char[] text = super.text;
		mv.visitVarInsn(ALOAD, THIS);
		mv.visitFieldInsn(GETFIELD, SUPER, "text", "[C");
		mv.visitVarInsn(ASTORE, LOCAL_TEXT);

		// int limit = super.limit;
		mv.visitVarInsn(ALOAD, THIS);
		mv.visitFieldInsn(GETFIELD, SUPER, "limit", "I");
		mv.visitVarInsn(ISTORE, LOCAL_LIMIT);

		// int cursor = super.cursor;
		mv.visitVarInsn(ALOAD, THIS);
		mv.visitFieldInsn(GETFIELD, SUPER, "cursor", "I");
		mv.visitVarInsn(ISTORE, LOCAL_CURSOR);

		mv.visitLabel(startScan);

		// super.start = cursor
		mv.visitVarInsn(ALOAD, THIS);
		mv.visitVarInsn(ILOAD, LOCAL_CURSOR);
		mv.visitFieldInsn(PUTFIELD, SUPER, "start", "I");

		// int marker = -1;
		generateLoadConstInsn(mv, -1);
		mv.visitVarInsn(ISTORE, LOCAL_MARKER);

		// int ctxptr = -1;
		generateLoadConstInsn(mv, -1);
		mv.visitVarInsn(ISTORE, LOCAL_CTXPTR);

		// int accept = 0;
		generateLoadConstInsn(mv, 0);
		mv.visitVarInsn(ISTORE, LOCAL_ACCEPT);
	}

	private static void generateGetNextTokenEpilog(MethodVisitor mv, Label startScan, Label tokenIsRecognized, Label endOfScan)
	{
		Label saveCursor = new Label();
		Label returnAccepted = new Label();

		mv.visitLabel(tokenIsRecognized);
		// if (ctxptr >= 0) marker = ctxptr;
		mv.visitVarInsn(ILOAD, LOCAL_CTXPTR);
		mv.visitJumpInsn(IFLT, endOfScan);
		mv.visitVarInsn(ILOAD, LOCAL_CTXPTR);
		mv.visitVarInsn(ISTORE, LOCAL_MARKER);

		mv.visitLabel(endOfScan);
		// if (marker >= 0) cursor = marker;
		mv.visitVarInsn(ILOAD, LOCAL_MARKER);
		mv.visitJumpInsn(IFLT, saveCursor);
		mv.visitVarInsn(ILOAD, LOCAL_MARKER);
		mv.visitVarInsn(ISTORE, LOCAL_CURSOR);

		mv.visitLabel(saveCursor);
		// super.cursor = cursor;
		mv.visitVarInsn(ALOAD, THIS);
		mv.visitVarInsn(ILOAD, LOCAL_CURSOR);
		mv.visitFieldInsn(PUTFIELD, SUPER, "cursor", "I");

		// if (accept == SKIP_ACCEPT) goto startScan;
		mv.visitVarInsn(ILOAD, LOCAL_ACCEPT);
		generateLoadConstInsn(mv, SKIP_ACCEPT);
		mv.visitJumpInsn(IF_ICMPEQ, startScan);

		// if (accept != 0) return accept;
		mv.visitVarInsn(ILOAD, LOCAL_ACCEPT);
		mv.visitJumpInsn(IFNE, returnAccepted);

		mv.visitTypeInsn(NEW, "beaver/UnexpectedCharacterException");
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, "beaver/UnexpectedCharacterException", "<init>", "()V");
		mv.visitInsn(ATHROW);

		mv.visitLabel(returnAccepted);
		mv.visitVarInsn(ILOAD, LOCAL_ACCEPT);
		mv.visitInsn(IRETURN);
	}

	private static void generateRefillBuffer(MethodVisitor mv, Label refillBuffer)
	{
		mv.visitLabel(refillBuffer);

		mv.visitVarInsn(ASTORE, LOCAL_RETURN);
		mv.visitVarInsn(ALOAD, THIS);
		mv.visitMethodInsn(INVOKEVIRTUAL, SUPER, "fill", "()I");
		mv.visitInsn(DUP);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ILOAD, LOCAL_CURSOR);
		mv.visitInsn(SWAP);
		mv.visitInsn(ISUB);
		mv.visitVarInsn(ISTORE, LOCAL_CURSOR);
		mv.visitVarInsn(ILOAD, LOCAL_MARKER);
		mv.visitInsn(SWAP);
		mv.visitInsn(ISUB);
		mv.visitVarInsn(ISTORE, LOCAL_MARKER);
		mv.visitVarInsn(ILOAD, LOCAL_CTXPTR);
		mv.visitInsn(SWAP);
		mv.visitInsn(ISUB);
		mv.visitVarInsn(ISTORE, LOCAL_CTXPTR);
		// update local limit
		mv.visitVarInsn(ALOAD, THIS);
		mv.visitFieldInsn(GETFIELD, SUPER, "limit", "I");
		mv.visitVarInsn(ISTORE, LOCAL_LIMIT);

		mv.visitVarInsn(RET, LOCAL_RETURN);
	}

	private void generateGetNextTokenDFACode(MethodVisitor mv, Label tokenIsRecognized, Label endOfScan, Label refillBuffer)
	{
		Label[] dfaStates = makeLabels(dfa.numStates);

		for (DFAState st = dfa.start; st != null; st = st.next)
		{
			mv.visitLabel(dfaStates[st.id]);

			if (st.firstTransition == null)
			{
				if (st.accept != 0)
				{
					generateLoadConstInsn(mv, st.accept);
					mv.visitVarInsn(ISTORE, LOCAL_ACCEPT);
					mv.visitJumpInsn(GOTO, tokenIsRecognized);
				}
				else
				{
					throw new IllegalStateException("Final state " + st.id + " has no accept code.");
				}
			}
			else
			{
				if (st.accept != 0)
				{
					generateLoadConstInsn(mv, st.accept);
					mv.visitVarInsn(ISTORE, LOCAL_ACCEPT);

					mv.visitVarInsn(ILOAD, LOCAL_CURSOR);
					mv.visitVarInsn(ISTORE, LOCAL_MARKER);
				}

				if (st.isPreCtx)
				{
					mv.visitVarInsn(ILOAD, LOCAL_CURSOR);
					mv.visitVarInsn(ISTORE, LOCAL_CTXPTR);
				}

				if (st.depth > 0)
				{
					Label endBufferCheck = new Label();

					mv.visitVarInsn(ILOAD, LOCAL_LIMIT);
					mv.visitVarInsn(ILOAD, LOCAL_CURSOR);
					mv.visitInsn(ISUB);
					generateLoadConstInsn(mv, st.depth);
					mv.visitJumpInsn(IF_ICMPGE, endBufferCheck);

					mv.visitJumpInsn(JSR, refillBuffer);

					if (st == dfa.start)
					{
						mv.visitVarInsn(ILOAD, LOCAL_LIMIT);
						mv.visitVarInsn(ILOAD, LOCAL_CURSOR);
						mv.visitJumpInsn(IF_ICMPGT, endBufferCheck);
						generateLoadConstInsn(mv, eofTokenId);
						mv.visitVarInsn(ISTORE, LOCAL_ACCEPT);
						mv.visitJumpInsn(GOTO, endOfScan);
					}
					mv.visitLabel(endBufferCheck);
				}

				// load current char
				mv.visitVarInsn(ALOAD, LOCAL_TEXT);
				mv.visitVarInsn(ILOAD, LOCAL_CURSOR);
				mv.visitInsn(CALOAD);
				mv.visitVarInsn(ISTORE, LOCAL_CHAR);

				mv.visitIincInsn(LOCAL_CURSOR, 1);

				CharTransition[] trans = CharTransition.find(st);
				if (trans != null)
				{
					if (trans.length == 1)
					{
						CharTransition tr = trans[0];

						mv.visitVarInsn(ILOAD, LOCAL_CHAR);
						generateLoadConstInsn(mv, tr.onChar);
						mv.visitJumpInsn(IF_ICMPEQ, dfaStates[tr.dest.id]);
					}
					else
					{
						Label[] caseLabels = makeCaseLabels(trans, dfaStates);
						Label defaultLabel = new Label();

						mv.visitVarInsn(ILOAD, LOCAL_CHAR);
						mv.visitLookupSwitchInsn(defaultLabel, getCharTransitionKeys(trans), caseLabels);
						mv.visitLabel(defaultLabel);
					}
				}
				if (compileCharSpanTransitions(mv, st, dfaStates, endOfScan) != endOfScan)
				{
					mv.visitJumpInsn(GOTO, endOfScan);
				}
			}
		}
		mv.visitJumpInsn(GOTO, endOfScan);
	}

	private static Label compileCharSpanTransitions(MethodVisitor mv, DFAState fromState, Label[] dfaStates, Label endOfScan)
	{
		CharSpanTransition root = CharSpanTransition.findRoot(fromState);
		return root == null ? null : compileCharSpanTransitions(mv, 0, root, dfaStates, endOfScan);
	}

	private static Label compileCharSpanTransitions(MethodVisitor mv, int depth, CharSpanTransition trans, Label[] dfaStates, Label endOfScan)
	{
		Label left = trans.left != null ? new Label() : null;

		mv.visitVarInsn(ILOAD, LOCAL_CHAR);
		generateLoadConstInsn(mv, trans.span.lb);
		mv.visitJumpInsn(IF_ICMPLT, trans.left != null ? left : endOfScan);

		mv.visitVarInsn(ILOAD, LOCAL_CHAR);
		generateLoadConstInsn(mv, trans.span.ub);
		mv.visitJumpInsn(IF_ICMPLT, dfaStates[trans.dest.id]);

		Label lastGoto = null;
		if (trans.right != null)
		{
			lastGoto = compileCharSpanTransitions(mv, depth + 1, trans.right, dfaStates, endOfScan);
		}
		else if (depth > 0 || trans.left != null)
		{
			mv.visitJumpInsn(GOTO, lastGoto = endOfScan);
		}
		if (trans.left != null)
		{
			mv.visitLabel(left);
			lastGoto = compileCharSpanTransitions(mv, depth + 1, trans.left, dfaStates, endOfScan);
		}
		return lastGoto;
	}

	private static int[] getCharTransitionKeys(CharTransition[] trans)
	{
		int[] keys = new int[trans.length];
		for (int i = 0; i < trans.length; i++)
		{
			keys[i] = trans[i].onChar;
		}
		return keys;
	}

	private static Label[] makeCaseLabels(CharTransition[] trans, Label[] dfaStates)
	{
		Label[] caseLabels = new Label[trans.length];
		for (int i = 0; i < trans.length; i++)
		{
			caseLabels[i] = dfaStates[trans[i].dest.id];
		}
		return caseLabels;
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

	private static void generateLoadConstInsn(MethodVisitor mv, int c)
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

	static class CharTransition implements Comparable
	{
		char     onChar;
		DFAState dest;

		CharTransition(char c, DFAState dest)
		{
			this.onChar = c;
			this.dest = dest;
		}

		public int compareTo(Object o)
		{
			return onChar - ((CharTransition) o).onChar;
		}

		static CharTransition[] find(DFAState fromState)
		{
			List trans = null;
			for (DFAStateTransition tr = fromState.firstTransition; tr != null; tr = tr.next)
			{
				CharSpan[] spans = tr.onChars.spans;
				for (int i = 0; i < spans.length; i++)
				{
					CharSpan charSpan = spans[i];
					if (charSpan.size() == 1)
					{
						if (trans == null)
						{
							trans = new ArrayList();
						}
						trans.add(new CharTransition(charSpan.lb, tr.toState));
					}
				}
			}
			if (trans == null)
			{
				return null;
			}
			Collections.sort(trans);
			return (CharTransition[]) trans.toArray(new CharTransition[trans.size()]);
		}
	}

	static class CharSpanTransition implements Comparable
	{
		CharSpan           span;
		DFAState           dest;
		CharSpanTransition left;
		CharSpanTransition right;

		CharSpanTransition(CharSpan span, DFAState dest)
		{
			this.span = span;
			this.dest = dest;
		}

		public int compareTo(Object o)
		{
			return span.lb - ((CharSpanTransition) o).span.lb;
		}

		static CharSpanTransition findRoot(DFAState fromState)
		{
			List trans = null;
			for (DFAStateTransition tr = fromState.firstTransition; tr != null; tr = tr.next)
			{
				CharSpan[] spans = tr.onChars.spans;
				for (int i = 0; i < spans.length; i++)
				{
					CharSpan charSpan = spans[i];
					if (charSpan.size() > 1)
					{
						if (trans == null)
						{
							trans = new ArrayList();
						}
						trans.add(new CharSpanTransition(charSpan, tr.toState));
					}
				}
			}
			if (trans == null)
			{
				return null;
			}
			Collections.sort(trans);
			return root(trans, 0, trans.size());
		}

		private static CharSpanTransition root(List trans, int from, int to)
		{
			int d = to - from;
			if (d == 1)
			{
				return (CharSpanTransition) trans.get(from);
			}
			else
			{
				int i = d / 2 + from;
				CharSpanTransition r = (CharSpanTransition) trans.get(i);
				if (i > from)
					r.left = root(trans, from, i);
				if (++i < to)
					r.right = root(trans, i, to);
				return r;
			}
		}
	}

	private static final String   SUPER        = "beaver/CharScanner";
	private static final String[] EXCEPTIONS   = { "beaver/UnexpectedCharacterException", "java/io/IOException" };
	
	private static final int      SKIP_ACCEPT  = Short.MIN_VALUE;

	private static final int      THIS         = 0;
	private static final int      ARG1         = 1;
	private static final int      ARG2         = 2;

	private static final int      LOCAL_CURSOR = 1;                                                               // points to the current character while scan is in progress
	private static final int      LOCAL_CHAR   = 2;                                                               // keeps the current character to minimize array accesses
	private static final int      LOCAL_ACCEPT = 3;
	private static final int      LOCAL_TEXT   = 4;                                                               // shadow copy of super.text
	private static final int      LOCAL_LIMIT  = 5;                                                               // shadow copy of super.limit
	private static final int      LOCAL_MARKER = 6;                                                               // marks cursor position when a token is recognized, 
	// but the recognition is not final
	private static final int      LOCAL_CTXPTR = 7;                                                               // marks cursor position at the beginning of a trailing context
	private static final int      LOCAL_RETURN = 8;                                                               // keeps return address for RET
}
