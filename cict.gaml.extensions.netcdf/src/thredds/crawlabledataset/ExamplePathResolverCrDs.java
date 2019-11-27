package thredds.crawlabledataset;

import java.io.File;

/**
 * Very simple example of a PathResolverCrawlableDataset implementation.
 *
 * @author edavis
 * @since 4.2.9
 */
public class ExamplePathResolverCrDs extends PathResolverCrawlableDataset
{
  public ExamplePathResolverCrDs( String rootDirectoryPath, Object configObj ) {
    super( rootDirectoryPath, configObj);
  }

  /**
   * Very simple example: drop the first character in the given path.
   *
   * @param path
   * @return
   */
  @Override
  File resolve( String path ) {
    return new File( rootDirFile, removeFirstCharInEachPathSegment( path ) );
  }

  private String removeFirstCharInEachPathSegment( String path) {
    if ( path == null || path.equals( "" ) )
      throw new IllegalArgumentException( "Path may not be null or empty." );
    String[] segs = path.split( "/" );
    StringBuilder sb = new StringBuilder();
    for ( String s : segs ) {
      sb.append( s.substring( 1 ) ).append( "/" );
    }
    sb.deleteCharAt( sb.length() - 1 );
    return sb.toString();
  }
}
