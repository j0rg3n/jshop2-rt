package com.gamalocus.jshop2rt;

/** Each variable symbol both at compile time and at run time, is an instance
 *  of this class.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
*/
public class TermVariable extends Term
{
  private static final long serialVersionUID = 2061700708938341887L;
  
  /** Variable symbols are mapped to integers at compile time, and these
   *  integers are used thereafter to represent the variable symbols.
  */
  private final int index;

  /** To initialize this variable symbol.
   *
   *  @param indexIn
   *          the integer associated with this variable symbol.
  */
  public TermVariable(int indexIn)
  {
    index = indexIn;
  }

  /** Since this term is a variable symbol, binding it basically means finding
   *  out whether or not this variable is already mapped to something in the
   *  input, and if so, returning the value this variable is mapped to.
  */
  public Term bind(Term[] binding)
  {
    //-- If this variable is already mapped to something in the input:
    if (binding[index] != null)
      return binding[index];

    //-- If this variable is not mapped to anything in the input, just return
    //-- the variable symbol itself.
    return this;
  }

  /** This function returns <code>false</code>.
  */
  public boolean equals(Object t)
  {
  	// WARNING: Identity equality is required to be able to use TermVariable objects as hash
  	// keys. However, this method previously returned false no matter the argument. The
  	// change MAY have broken the FOPL reasoning.
  	// FIXME Verify this.
    return t == this;
  }
  
  @Override
  public int hashCode()
  {
	  // No var equals any other var. This is the closest we can get to that.
	  return System.identityHashCode(this);
  }

/** Find a unifier between this variable symbol and another given term.
  */
  public boolean findUnifier(Term t, Term[] binding)
  {
    //-- If 't' is a variable symbol, skip it.
    if (t instanceof TermVariable)
      return true;

    //-- If the variable has not already been mapped to something, map it:
    if (binding[index] == null)
    {
      binding[index] = t;
      return true;
    }

    //-- If the variable has already been mapped to something, check if it is
    //-- unified with the same thing again.
    return t.equals(binding[index]);
  }

  /** To get the index for this variable symbol.
   *
   *  @return
   *          the integer associated with this variable symobl.
  */
  public int getIndex()
  {
    return index;
  }

  /** This function always returns <code>false</code> because a variable symbol
   *  is never ground by definition.
  */
  public boolean isGround()
  {
    return false;
  }

  /** This function produces Java code to create this variable symbol as a
   *  term.
  */
  public String toCode(String label)
  {
    return "owner.getTermVariable(" + index + ")";
  }

  /** This function is used to print this variable symbol.
  */
  @Override
  public String toString()
  {
    return "VAR" + index;
  }
}
