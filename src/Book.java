/*************************************************************************
 * Title: Simplistic Database Class
 * File: Book.java
 * Author: James Eli
 * Date: 9/12/2016
 *
 * This is a simplistic database base class containing 4 fields (ISBN, 
 * title, publication year and price). The ISBN field is selected as the 
 * key field. The fields are all defined as private. The class includes 
 * just basic constructors, getters, setters and an overridden toString 
 * method for all of the fields. No input validation is performed.
 *
 * Notes:
 *  (1) Other than the special constructor (see below), this class
 *      is composed of just very basic constructor, getter/setter and
 *      overridden toString() methods.
 *  (2) See comments inside EliJames_HW8.java for more information.
 *  (3) Compiled with java SE JDK 8, Update 102 (JDK 8u102).
 *  
 * Submitted in partial fulfillment of the requirements of PCC CIS-131.
 *************************************************************************
 * Change Log:
 *   09/12/2016: Initial release. JME
 *   09/14/2016: Modified to work with Generic database. JME
 *   09/23/2016: Added IllegalArgumentException to constructor. JME
 *************************************************************************/
public class Book {
  /*********************************************************************
   * Instance fields (all private)
   *********************************************************************/
  private Integer isbn;      // Book ISBN.
  private String title;      // Book title.
  private int yearPublished; // Year book was published.
  private double price;      // Book price.
  
  /*********************************************************************
   * Class constructors.
   *********************************************************************/
  // Naked constructor.
  public Book() {
    setISBN( 0 );
    setTitle( "" );
    setYearPublished( 0 );
    setPrice( 0.0 );
  }
  
  // 4-parameter constructor.
  public Book( Integer isbn, String title, int year, double price ) {
    setISBN( isbn );
    setTitle( title );
    setYearPublished( year );
    setPrice( price );
  }

  // Note, this is a required special self-referential constructor.
  public Book( Object o ) throws IllegalArgumentException {
    if ( o instanceof Book ) {
      setISBN( ((Book)o).isbn );
      setTitle( ((Book)o).title );
      setYearPublished( ((Book)o).yearPublished );
      setPrice( ((Book)o).price );
    } else 
      throw new IllegalArgumentException( "Improper call to Book class constructor." );
  }

  /*********************************************************************
   * Class mutators.
   *********************************************************************/
  public void setISBN( Integer isbn ) { this.isbn = isbn; }
  public void setTitle( String title ) { this.title = title; }
  public void setPrice( double price ) { this.price = price; }
  public void setYearPublished( int year ) { this.yearPublished = year; }

  /*********************************************************************
   * Class accessors.
   *********************************************************************/
  public Integer getISBN() { return this.isbn; }
  public String getTitle() { return this.title; }
  public double getPrice() { return this.price; }
  public int getYearPublished() { return this.yearPublished; }

  /*********************************************************************
   * Overridden methods.
   *********************************************************************/
  @Override
  public String toString() {
    return String.format("ISBN%11s %d%n", ":", this.getISBN() ) +
      String.format("Title%10s %s%n", ":", this.getTitle() ) +
      String.format("Price%10s $%.2f%n", ":", this.getPrice() ) +
      String.format("Year published: %d%n", this.getYearPublished() );
  }
		
} // End of Book class.
