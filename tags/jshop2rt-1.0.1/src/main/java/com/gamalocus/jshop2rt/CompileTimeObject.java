package com.gamalocus.jshop2rt;

/** All the objects at compile time are instances of classes that are derived
 *  from this abstract class.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
*/
public abstract class CompileTimeObject
{
  /** The new line character in the platform JSHOP2 is running on.
  */
  final static String endl = System.getProperty("line.separator");

  /** This abstract function produces the Java code needed to implement this
   *  compile time element.
   * @param label 
   *            Descriptive label for the element, as seen from its parent. Used for comments.
   *            For small elements, this makes no sense, and <code>null</code> may be passed.
   *  @return
   *          the produced code as a <code>String</code>.
  */
  public abstract String toCode(String label);
}
