package thredds.crawlabledataset;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * An abstract CrawlableDataset implementation built around a path resolver method
 * <pre>
 *   File resolve( String path);
 * </pre>
 *
 * <p> The path handed to the resolve() method is the relative path of the CrDs.getDescendant()
 *   method.</p>
 *
 * <p><strong>NOTE:</strong> The current implementation does not support crawling a hierarchy. All
 *   relative paths must resolve to a dataset file, not a directory. This means that classes
 *   derived from this may only be used behind datasetRoot elements.</p>
 *
 * @author edavis
 * @since 4.2.9
 */
public abstract class PathResolverCrawlableDataset implements CrawlableDataset
{
  /**
   * Resolves the given path to a java.io.File with an absolute path. The path of the returned File
   * must start with the root directory path used to construct this PathResolverCrawlableDataset.
   *
   * @param path the path of the desired dataset/file relative to this PathResolverCrawlabledataset.
   * @return the java.io.File to which the given path resolves.
   */
  abstract File resolve( String path );

  final File rootDirFile;
  final String rootDirPath;
  final String rootDirName;

  /**
   * The constructor used by the TDS when creating all CrawlableDatset instances. All
   * implementations of CrawlableDataset must support this constructor signature.
   *
   * <p><strong>NOTE:</strong> The configObj parameter is ignored by this implementation because
   * it is not supported by datasetRoot elements in a configuration catalog.</p>
   *
   * @param rootDirectoryPath the absolute path of the directory for a particular dataset root. E.g., the datasetRoot@location value.
   * @param configObj a configuration object that is ignored.
   */
  public PathResolverCrawlableDataset( String rootDirectoryPath, Object configObj )
  {
    if ( rootDirectoryPath == null || rootDirectoryPath.equals( "" ) )
      throw new IllegalArgumentException( "Path may not be null or empty." );

    File tmpFile = new File( rootDirectoryPath );
    if ( ! tmpFile.exists() || ! tmpFile.isDirectory())
      throw new IllegalArgumentException( "Root file [" + tmpFile.getPath() + "] must exist and be a directory" );

    rootDirFile = tmpFile;
    rootDirPath = normalizePath( tmpFile.getPath() );
    rootDirName = rootDirPath.substring( rootDirPath.lastIndexOf( "/" ) + 1 );
  }

  @Override
  public Object getConfigObject() {
    return null;
  }

  @Override
  public String getPath() {
    return rootDirPath;
  }

  @Override
  public String getName()
  {
    return rootDirName;
  }

  @Override
  public CrawlableDataset getParentDataset() {
    throw new IllegalStateException( "This is a root dataset without parents." );
  }

  @Override
  public boolean exists() {
    return true;
  }

  @Override
  public boolean isCollection() {
    return true;
  }

  @Override
  public CrawlableDataset getDescendant( String relativePath ) {
    if ( relativePath == null || relativePath.equals( "" ) )
      throw new IllegalArgumentException( "Path may not be null or empty." );

    return new NonCollectionCrDs(  rootDirPath + "/" + relativePath );
  }

  @Override
  public List<CrawlableDataset> listDatasets() throws IOException {
    throw new UnsupportedOperationException( "This class does not support crawling the dataset hierarchy." );
  }

  @Override
  public List<CrawlableDataset> listDatasets( CrawlableDatasetFilter filter ) throws IOException {
    throw new UnsupportedOperationException( "This class does not support crawling the dataset hierarchy." );
  }

  @Override
  public long length() {
    return 0;
  }

  @Override
  public Date lastModified() {
    return null;
  }

  public String toString() {
    return rootDirPath;
  }

  /**
   *
   */
  private class NonCollectionCrDs implements CrawlableDataset, CheckableForFileRepresentation
  {
    private final String crDsPath;
    private final String crDsName;

    private final File dsFile;

    NonCollectionCrDs( String path ) {
      if ( path == null || path.equals( "" ))
        throw new IllegalArgumentException( "Path may not be null or empty." );

      String tmpPath = normalizePath( path);
      if ( ! tmpPath.startsWith( rootDirPath ) )
        throw new IllegalArgumentException( "Dataset path [" + tmpPath + "] not descendant of root dataset ["
                                            + rootDirPath + "]." );

      String remainingPath = tmpPath.substring( rootDirPath.length() + 1 );

      File tmpFile = resolve( remainingPath );
      if ( ! tmpFile.exists())
        throw new IllegalArgumentException( "Dataset backing file [" + tmpFile.getPath() + "] does not exist." );
      if ( tmpFile.exists() && tmpFile.isDirectory() )
        throw new IllegalArgumentException( "Dataset backing file [" + tmpFile.getPath() + "] is a directory." );

      crDsPath = tmpPath;
      crDsName = crDsPath.substring( crDsPath.lastIndexOf( "/" ) + 1 );
      dsFile = tmpFile;
    }

    @Override
    public boolean canRepresentAsFile() {
      return true;
    }

    /**
     * Returns the java.io.File representation of this dataset.
     * <p><strong>NOTE:</strong></p> The path of the returned File may not correspond to the path of this CrawlableDataset.
     *
     * @return  the java.io.File representation of this dataset.
     */
    @Override
    public File getFile() {
      return dsFile;
    }

    @Override
    public Object getConfigObject() {
      return null;
    }

    @Override
    public String getPath() {
      return crDsPath;
    }

    @Override
    public String getName() {
      return crDsName;
    }

    @Override
    public CrawlableDataset getParentDataset() {
      // Can only use with datasetRoot
      throw new UnsupportedOperationException( "This class does not support crawling the dataset hierarchy." );
    }
      @Override
    public boolean exists() {
      return true;
    }

    @Override
    public boolean isCollection() {
      return false;
    }

    @Override
    public CrawlableDataset getDescendant( String relativePath ) {
      throw new IllegalStateException( "Dataset is not a collection." );
    }

    @Override
    public List<CrawlableDataset> listDatasets() throws IOException
    {
      throw new IllegalStateException( "Dataset is not a collection." );
    }

    @Override
    public List<CrawlableDataset> listDatasets( CrawlableDatasetFilter filter ) throws IOException
    {
      throw new IllegalStateException( "Dataset is not a collection." );
    }

    @Override
    public long length()
    {
      return dsFile.length();
    }

    @Override
    public Date lastModified()
    {
      long lastModDate = dsFile.lastModified();
      if ( lastModDate == 0 ) return null;

      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.setTimeInMillis( lastModDate );
      return ( cal.getTime() );
    }

    public String toString() {
      return this.getPath();
    }
  }

  private String normalizePath( String path )
  {
    // Replace any occurance of a backslash ("\") with a slash ("/").
    // NOTE: Both String and Pattern escape backslash, so need four backslashes to find one.
    return path.replaceAll( "\\\\", "/" );
  }
}