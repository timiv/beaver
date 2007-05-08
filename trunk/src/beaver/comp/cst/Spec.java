/**
* Beaver: compiler front-end construction toolkit
* Copyright (c) 2007 Alexander Demenchuk <alder@softanvil.com>
* All rights reserved.
*
* See the file "LICENSE" for the terms and conditions for copying,
* distribution and modification of Beaver.
*/
package beaver.comp.cst;

/**
 * @author <a href="http://beaver.sourceforge.net">Beaver</a> parser generator
 */
public class Spec extends beaver.util.Node
{
	public ParserSpec  parserSpec;
	public ScannerSpec scannerSpec;

	public Spec(ParserSpec parserSpec, ScannerSpec scannerSpec)
	{
		this.parserSpec  = parserSpec;
		this.scannerSpec = scannerSpec;
	}

	public void accept(NodeVisitor visitor)
	{
		visitor.visit(this);
	}
}
