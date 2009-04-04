package beaver.lexer;

class RegExpTestFixtures
{
	private static final int SKIP_ACCEPT = Short.MIN_VALUE;
	
	static RegExp getDigitalScanner()
	{
		return compileRules(
				makeRule(1, new RegExp.MatchRange(new CharRange(new CharReader("0-9")))),
				makeRule(2, new RegExp.MatchRange(new CharRange(new CharReader(" \\t\\n\\r"))))
		);
	}
	
	static RegExp getDecimalScanner()
	{
		return compileRules(
				makeRule(1, // decimal
						new RegExp.Cat(
								new RegExp.Close(
										new RegExp.MatchRange(new CharRange(new CharReader("0-9"))),
										'+'
								),
								new RegExp.Close(
										new RegExp.Cat(
												new RegExp.MatchChar('.'),
												new RegExp.Close(
														new RegExp.MatchRange(new CharRange(new CharReader("0-9"))),
														'+'
												)
										),
										'?'
								)
						)	
				),
				makeRule(2, new RegExp.MatchRange(new CharRange(new CharReader(" \\t\\n\\r"))))
		);
	}
	
	static RegExp getIntegerScanner()
	{
		return compileRules(
				makeRule(1, // integer
						new RegExp.Alt(
								new RegExp.Cat(
										new RegExp.Close(
												new RegExp.MatchChar('-'),
												'?'
										),
										new RegExp.Cat(
												new RegExp.MatchRange(new CharRange(new CharReader("1-9"))),
												new RegExp.Close(
														new RegExp.MatchRange(new CharRange(new CharReader("0-9"))),
														'*'
												)
										)
								),
								new RegExp.Cat(
										new RegExp.Cat(
												new RegExp.MatchChar('0'),
												new RegExp.MatchChar('x')
										),
										new RegExp.Close(
												new RegExp.MatchRange(new CharRange(new CharReader("0-9a-fA-F"))),
												'+'
										)
								)
						)
				),
				makeRule(2, new RegExp.MatchRange(new CharRange(new CharReader(" \\t\\n\\r"))))
		);
	}
	
	static RegExp getCalculatorScanner()
	{
		return compileRules(
				makeRule(1, new RegExp.MatchRange(new CharRange(new CharReader(" \\t\\n\\r")))),
				makeRule(2, matchText("print")),
				makeRule(3, matchText("set")),
				makeRule(4, new RegExp.MatchChar('+')),
				makeRule(5, new RegExp.MatchChar('-')),
				makeRule(6, new RegExp.MatchChar('*')),
				makeRule(7, new RegExp.MatchChar('/')),
				makeRule(8, new RegExp.MatchChar('=')),
				makeRule(9, // literal number
    					new RegExp.Cat(
    							new RegExp.Close(
    									new RegExp.MatchRange(new CharRange(new CharReader("0-9"))),
    									'+'
    							),
    							new RegExp.Close(
    									new RegExp.Cat(
    											new RegExp.MatchChar('.'),
    											new RegExp.Close(
    													new RegExp.MatchRange(new CharRange(new CharReader("0-9"))),
    													'*'
    											)
    									),
    									'?'
    							)
    					)		
				),
				makeRule(10, // identifier
						new RegExp.Cat(
								new RegExp.MatchRange(new CharRange(new CharReader("a-zA-Z"))),
								new RegExp.Close(
										new RegExp.MatchRange(new CharRange(new CharReader("a-zA-Z0-9_"))),
										'*'
								)
						)
				)
		);
	}

	static RegExp getSimpleCalculatorScanner()
	{
		return compileRules(
				makeRule(SKIP_ACCEPT, new RegExp.MatchRange(new CharRange(new CharReader(" \\t\\n\\r")))),
				makeRule(1, matchText(";")),
				makeRule(2, matchText("+")),
				makeRule(3, matchText("-")),
				makeRule(4, matchText("*")),
				makeRule(5, matchText("/")),
				makeRule(6, matchText("=")),
				makeRule(7, matchText("(")),
				makeRule(8, matchText(")")),
				makeRule(9, // literal number
    					new RegExp.Cat(
    							new RegExp.Close(
    									new RegExp.MatchRange(new CharRange(new CharReader("0-9"))),
    									'+'
    							),
    							new RegExp.Close(
    									new RegExp.Cat(
    											new RegExp.MatchChar('.'),
    											new RegExp.Close(
    													new RegExp.MatchRange(new CharRange(new CharReader("0-9"))),
    													'*'
    											)
    									),
    									'?'
    							)
    					)		
				),
				makeRule(10, // identifier
						new RegExp.Cat(
								new RegExp.MatchRange(new CharRange(new CharReader("a-zA-Z"))),
								new RegExp.Close(
										new RegExp.MatchRange(new CharRange(new CharReader("a-zA-Z0-9_"))),
										'*'
								)
						)
				)
		);
	}
	
	static RegExp getLogicCalculatorScanner()
	{
		return compileRules(
				makeRule(SKIP_ACCEPT, new RegExp.MatchRange(new CharRange(new CharReader(" \\t\\n\\r")))),
				makeRule(1, matchText("and")),
				makeRule(2, matchText("or")),
				makeRule(3, matchText("not")),
				makeRule(4, // identifier
						new RegExp.Cat(
								new RegExp.MatchRange(new CharRange(new CharReader("a-zA-Z_"))),
								new RegExp.Close(
										new RegExp.MatchRange(new CharRange(new CharReader("a-zA-Z_0-9"))),
										'*'
								)
						)
				)
		);
	}
	
	private static RegExp matchText(String text)
	{
		if (text.isEmpty())
			return new RegExp.Null();
		
		RegExp re = new RegExp.MatchChar(text.charAt(0));
		int n = text.length();
		for (int i = 1; i < n; i++)
		{
			re = new RegExp.Cat(re, new RegExp.MatchChar(text.charAt(i)));
		}
		return re;
	}
	
	private static RegExp makeRule(int id, RegExp re)
	{
		return new RegExp.Rule(re, new RegExp.Null(), id);
	}
	
	private static RegExp compileRules(RegExp... rules)
	{
		RegExp re = new RegExp.Null();
		for (RegExp rule : rules)
		{
			re = new RegExp.Alt(re, rule);
		}
		return re;
	}
	
}
