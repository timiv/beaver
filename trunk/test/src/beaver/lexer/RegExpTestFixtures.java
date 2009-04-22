package beaver.lexer;

class RegExpTestFixtures
{
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
				makeRule(2, RegExp.matchText("print")),
				makeRule(3, RegExp.matchText("set")),
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
				makeRule(0, new RegExp.MatchRange(new CharRange(new CharReader(" \\t\\n\\r")))),
				makeRule(1, RegExp.matchText(";")),
				makeRule(2, RegExp.matchText("+")),
				makeRule(3, RegExp.matchText("-")),
				makeRule(4, RegExp.matchText("*")),
				makeRule(5, RegExp.matchText("/")),
				makeRule(6, RegExp.matchText("=")),
				makeRule(7, RegExp.matchText("(")),
				makeRule(8, RegExp.matchText(")")),
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
				makeRule(0, new RegExp.MatchRange(new CharRange(new CharReader(" \\t\\n\\r")))),
				makeRule(1, RegExp.matchText("and")),
				makeRule(2, RegExp.matchText("or")),
				makeRule(3, RegExp.matchText("not")),
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

	static RegExp getLogicCalculatorScannerWithEvents()
	{
		return compileRules(
				makeRule(0, new RegExp.MatchRange(new CharRange(new CharReader(" \\t")))),
				makeRule(0, new RegExp.Alt( RegExp.matchText("\r")
								          , new RegExp.Alt( RegExp.matchText("\n")
								                          ,	RegExp.matchText("\r\n")
								          				  )
								          )
							, "newLine"),
				makeRule(1, RegExp.matchText("and")),
				makeRule(2, RegExp.matchText("or")),
				makeRule(3, RegExp.matchText("not")),
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
	
	private static RegExp makeRule(int id, RegExp re)
	{
		return new RegExp.Rule(re, new RegExp.Null(), new Accept(id, Short.MAX_VALUE - id));
	}

	private static RegExp makeRule(int id, RegExp re, String event)
	{
		return new RegExp.Rule(re, new RegExp.Null(), new Accept(id, Short.MAX_VALUE - id, event));
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
