/*************************************************************************
 * Title: Simplistic Database Driver Program
 * File: EliJames_HW8.java
 * Author: James Eli
 * Date: 9/12/2016
 *
 * This program is an attempt to implement as generic as possible, a 
 * simplistic database driver program. As a demonstration, the program 
 * uses the very basic Book class, which contains 4 fields (the ISBN 
 * field is set as the database key field).
 *
 * Notes:
 *  (1) The 2 nested classes of this program (Record and Database) 
 *  should be separated for readability/maintainability purposes in
 *  future versions. 
 *  (2) Compiled with java SE JDK 8, Update 102 (JDK 8u102).
 *  
 * Submitted in partial fulfillment of the requirements of PCC CIS-131.
 *************************************************************************
 * Change Log:
 *   09/12/2016: Initial release. JME
 *   09/14/2016: Modified with Generic database methods. JME
 *   10/02/2016: Added interfaces. JME
 *************************************************************************/
import java.io.File;
import java.io.RandomAccessFile;
import java.io.EOFException;
import java.io.IOException;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Date;
import java.text.SimpleDateFormat;

public class EliJames_HW8 {
  // Database filenames.
  private static final String filename = "Books", extension = ".dat";
  private static final String databaseFileName = filename + extension;
  private static final String tempDatabaseFileName = "temp.dat";
  // Size of database record in bytes. Set/checked inside read/write methods.
  private long recordSize = 0;
  
  // Database Record interface definitions.
  public static interface dbRecord<T, K> {
    // Constants.
    final static int STRING_LENGTH = 48;   // Required length of Strings.
    final long MAX_RECORDS = 1000;         // Maximum number of records in database.

    // Define required methods.
    public K getKey();                     // Returns db key value.
    
    //Must also include the following 2 constructors:
    //public Record() { super(); }         // Simple naked constructor.    
    //public Record( T t ) { super( t ); } // Passes db object onto base class.
  }
  
  /*********************************************************************
   * This class defines a Database Record based upon the inherited class.
   *********************************************************************/
  public static class Record<T, K> extends Book implements dbRecord<T, K> { 
    /*********************************************************************
     * Constructors.
     *********************************************************************/
    // Unique constructor passes database object onto base class.
    public Record( T t ) { super( t ); }
    // Naked constructor.    
    public Record() { super(); }

    /*********************************************************************
     * Class getters. Note, getKey method is customized for the particular 
     * database base class key value.
     *********************************************************************/
    // Key field and type is specific to the database.
    @SuppressWarnings( "unchecked" )                   // Suppressing cast to 'K' below.
    public K getKey() { return (K) getISBN(); }        // Return key field value (auto-boxed).
  } // End of Record class.

  /*********************************************************************
   * This interface defines the 2 required specific read/write methods 
   * which will be particular to the database base class. These 2 
   * methods need to be customized and must read and write the record 
   * fields in the same order. 
   *********************************************************************/
  public static interface dbReadWrite<T, K> {
    // Read all db records to random access file, returns a Record<T, K>
    public Record<T, K> read( RandomAccessFile file ) throws IOException, EOFException, Exception;
    // Write all db records from random access file.
    public void write( RandomAccessFile file, Record<T, K> record ) throws IOException, EOFException, Exception; 
  }
  
  /*********************************************************************
   * This class defines the required specific read/write methods which
   * are particular to this database of the base Book class. These 
   * methods need to be customized for each base class. These methods
   * should read and write the record fields in the same order. 
   *********************************************************************/
  public class ReadWrite<T, K> implements dbReadWrite<T, K> {
    // Read db record from RAF file. Note: caller must perform seek to correct location.
    public Record<T, K> read( final RandomAccessFile file ) throws IOException, EOFException, Exception { 
      Record<T, K> record = new Record<T, K>();

      long fp = file.getFilePointer(); // Used to assert correct record size.
      //Field #1.
      record.setISBN( file.readInt() );
      // Field #2.
      StringBuffer buffer = new StringBuffer();
      for ( int i=0; i<Record.STRING_LENGTH; i++ )
        buffer.append( file.readChar() );
      record.setTitle( buffer.toString() );
      // Field #3.
      record.setPrice( file.readDouble() );
      // Field #4.
      record.setYearPublished( file.readInt() );
      // Set or validate record size.
      if ( recordSize == 0 ) 
        recordSize = ( file.getFilePointer() - fp );
      else {
        // Assert proper record size read.
        assert ( (file.getFilePointer() - fp) == recordSize ) : "Record Size Violation.";
      }
      return record;
    }

    // Write db record to RAF file. Note: caller must perform seek to correct location.
    public void write( final RandomAccessFile file, final Record<T, K> record ) throws IOException, EOFException, Exception { 
      StringBuffer buffer = new StringBuffer( record.getTitle() );
      long fp = file.getFilePointer(); // Used to assert correct record size.

      // Field #1.
      while ( buffer.length()< Record.STRING_LENGTH )
        buffer.append( ' ' );
      buffer.setLength( Record.STRING_LENGTH );
      file.writeInt( record.getISBN() );
      // Field #2.
      file.writeChars( buffer.toString() );
      // Field #3.
      file.writeDouble( record.getPrice() );
      // Field #4.
      file.writeInt( record.getYearPublished() );
      // Set or validate record size.
      if ( recordSize == 0 ) 
          recordSize = ( file.getFilePointer() - fp );
        else {
          // Assert proper record size read.
          assert ( (file.getFilePointer() - fp) == recordSize ) : "Record Size Violation.";
        }
    }
  }
  
  /*********************************************************************
   * This class defines the required generic Database functions.
   * Nothing in this class needs to be modified.
   *********************************************************************/
  public class Database<T, K> extends ReadWrite<T, K> implements AutoCloseable {
    private RandomAccessFile dbFile = null ; // Database file.
    private long numRecords = 0;             // Number of database records.

    // Database constructor, opens the db RAF file.
    public Database( final String file ) throws IOException {
      dbFile = new RandomAccessFile( file, "rw" );
    }
	    
    // Closes a RAF file.
    private void close( RandomAccessFile file ) {
      if ( file != null ) {
        try {
          file.close();
        } catch( Exception e ) {
          ; // Eat exception, nothing further to be done here.
        }
      }
    }
    
    // Closes "the" database RAF file.
    public void close() { close( dbFile ); }

    // Methods pertaining to the database record count and size.
    private long getRecords() { return numRecords; }         // Return number of db records.
    private long incrementRecords() { return ++numRecords; } // Increment db record count by 1.
    private long decrementRecords() { return numRecords <= 0 ? numRecords : --numRecords; } // Decrement db record count by 1.
    private long getRecordSize() { return recordSize; }      // Size of db record.

    // Add new record to database.
    public void addRecord( final T t ) {
      final Record<T, K> record = new Record<T, K>( t );

      if ( getRecords() >= Record.MAX_RECORDS )
       	return; // At maximum record limit.
      try  {
        // Seek to end of file.
        dbFile.seek( getRecords()*getRecordSize() ); 
        write( dbFile, record );
        incrementRecords();
      } catch ( IOException e ) {
        System.out.println( "An IOException occurred while attempting to add record." );
      } catch ( Exception e ) {
        System.out.println( "A generic Exception occurred attempting to add record." );
      }
    }

    // Display all database records.
    public void displayRecords() {
      try {
        dbFile.seek( 0 ); // Go to start of file.
        // Iterate through entire file.
        for ( int i=0; i<getRecords(); i++ )
          System.out.println( read( dbFile ) );
      } catch ( EOFException e ) {
          System.out.println( "Reached EOF " + e.toString() + "." );
      } catch ( IOException e ) {
          System.out.println( "Probably reached EOF " + e.getMessage() + "." );
      } catch ( Exception e ) {
          System.out.println( "An exception occurred displaying " + e.getMessage() + "." );
      }
    }

    // Return specified record, or null if not found. Note, getRecord parameter type needs to match key type.
    public Record<T, K> getRecord( final K key ) {
      try {
        Record<T, K> record = new Record<T, K>();
        // Iterate through all records in file.
        for ( int i=0; i<getRecords(); i++ ) {
          dbFile.seek( i*getRecordSize() ); 
          record = read( dbFile );
          // Check for match.
          if ( key.equals(record.getKey()) )
            return record;
        }  
      } catch ( EOFException e) {
        System.out.println("Reached EOF " + e.toString() + ".");
      } catch ( IOException e) {
        System.out.println("Probably reached EOF " + e.getMessage() + ".");
      } catch ( Exception e) {
        System.out.println("An exception occurred getting " + e.getMessage() + ".");
      }
      return null; // Matching record not found.
    }
    
    // Update record in database file (if it exists).
    public void updateRecord( final T t ) {
      final Record<T, K> record = new Record<T, K>( t );
      try {
        // Check record exists.
        if ( getRecord( (K) record.getKey() ) != null ) {
          // Backup file pointer to beginning of current record.
          dbFile.seek( dbFile.getFilePointer() - getRecordSize() );
          write( dbFile, record );
        }
      } catch ( EOFException e) {
        System.out.println("Reached EOF " + e.toString() + ".");
      } catch ( IOException e) {
        System.out.println("Probably reached EOF " + e.getMessage() + ".");
      } catch ( Exception e) {
        System.out.println("An exception occurred updating " + e.getMessage() + ".");
      }
    }
    
    // Delete record in db file (if it exists), and create a (date/time stamped) backup file.
    // Note, getRecord parameter type needs to match key type.
    public void deleteRecord( final K key ) {
      // Confirm record exists prior to attempting deletion.
      if ( getRecord( key ) == null )
        return; // Quiet fail occurs here...

      // Read entire db file contents into array (except record to be deleted).
      try {
        // Temporary holds db file contents (minus record to be deleted).
        List<Record<T, K>> list = new ArrayList<Record<T, K>>();
        Record<T, K> record = new Record<T, K>(); // Temporary storage.

        dbFile.seek( 0 ); // Seek to start of file.
        for ( int i=0; i<getRecords(); i++ ) {
         record = read( dbFile );
         // Copy all records, ignoring the record to be deleted. 
         if ( !key.equals( record.getKey()) )
           list.add( record );
        }

        // Create new db file, and copy list contents to it. 
        RandomAccessFile newDBFile = null;
        newDBFile = new RandomAccessFile( tempDatabaseFileName, "rw" );
        for ( int i=0; i<(getRecords() - 1); i++ ) 
          write( newDBFile, list.get( i ) );
        close( newDBFile ); // Close so it can be renamed.
          
        // Decrement db record count.
        decrementRecords();
      } catch ( EOFException e ) {
        System.out.println( "Reached EOF " + e.toString() + "." );
      } catch ( IOException e ) {
        System.out.println( "Probably reached EOF " + e.getMessage() + "." );
      } catch ( Exception e ) {
        System.out.println( "An exception occurred deleting " + e.getMessage() + "." );
      }

      // Rename old file for backup purposes.
      try {
        close(); // Close old file so we can rename it.
        final File oldFile = new File( databaseFileName ); // Existing database file become backup.
        final String timeStamp = new SimpleDateFormat( "yy.MM.dd.HH.mm.ss" ).format( new Date() );
        oldFile.renameTo( new File( filename + timeStamp + extension ) );
        // Rename new file before we use it.
        final File newFile = new File( tempDatabaseFileName ); // New database file.
        newFile.renameTo( oldFile ); // Rename new file to old file name.
        // Open this new database file.
        dbFile = new RandomAccessFile( databaseFileName, "rw" );
      } catch( Exception e ) { 
        System.err.println( "Exception occured deleting record." ); 
      }
    }
  
  } // End of Database class.

  /*********************************************************************
   * Start of the main database driver program. A very basic test of an
   * example (book) database. Note, command line arguments are ignored.
   *********************************************************************/
  public static void main( String[] args ) {
    // Instantiate ourself.
    EliJames_HW8 dbApp = new EliJames_HW8();
    // Instantiate new Book database (try-with-resources).
    try ( EliJames_HW8.Database<Book, Integer> database = dbApp.new Database<Book, Integer>( databaseFileName ) ) {
      List<Book> bookList = new ArrayList<Book>( 5 );

      // Create 5 books.
      bookList.add( new Book( 20, "Wild", 2012, 12.15 ) );
      bookList.add( new Book( 1, "Wild and Free", 2016, 9.89 ) );
      bookList.add( new Book( 3, "Into the Wild", 1997, 11.49 ) );
      bookList.add( new Book( 44, "The Wild Truth", 2015, 11.68 ) );
      bookList.add( new Book( 50, "The Sound of a Wild Snail Eating", 2016, 12.25 ) );
      // Add the 5 books to database.
      bookList.stream().forEach( e -> database.addRecord(e) );

      // Display all records in database.
      System.out.println( "Database records after creating and adding 5 books:" );
      database.displayRecords();
    
      // Change a book title.
      Book book = new Book();
      book = database.getRecord( 3 );
      if ( book != null ) {
        book.setTitle( "Not Into the Wild" );
        database.updateRecord( book );
      }
      // Change a book price.
      book = database.getRecord( 50 );
      if ( book != null ) {
        book.setPrice( 99.99 );
        database.updateRecord( book );
      }
      // Display all records in database.
      System.out.println( "Database records after modifying ISBN #3 title, and ISBN #50 price:" );
      database.displayRecords();

      // Delete a db record.
      database.deleteRecord( 44 );
      // Display all records in database.
      System.out.println( "Database records after deleting ISBN #44:" );
      database.displayRecords();

    } catch ( Exception e ) {
      System.out.println( "A generic Exception occurred while attempting to write data to " + databaseFileName + "." );
    }
  } // End main method.
  
} // End of EliJames_HW8 class.

