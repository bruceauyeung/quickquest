package us.suselinks.quickquest.commons.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Simple class to watch directory events.
public class JDK7DirectoryWatcher implements Callable<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(JDK7DirectoryWatcher.class);
    private Path path;
	private WatchService watchService;
 
    public JDK7DirectoryWatcher(Path path) {
        this.path = path;
    }
 
    // print the events and the affected file
    private void printEvent(WatchEvent<?> event) {
        Kind<?> kind = event.kind();
        if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
            Path pathCreated = (Path) event.context();
            System.out.println("Entry created:" + pathCreated);
        } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
            Path pathDeleted = (Path) event.context();
            System.out.println("Entry deleted:" + pathDeleted);
        } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
            Path pathModified = (Path) event.context();
            System.out.println("Entry modified:" + pathModified);
        }
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
    	
    	watchService = FileSystems.getDefault().newWatchService();
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
            	dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
	                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
                return FileVisitResult.CONTINUE;
            }
        });
    }
	@Override
	public Void call() throws Exception {
		
	       try {
	    	   registerAll(this.path);
	            // loop forever to watch directory
	            while (true) {
	                WatchKey watchKey;
	                watchKey = watchService.take(); // this call is blocking until events are present
				    
				    Watchable watchable = watchKey.watchable();
				    Path pPath = null;
				    if(watchable instanceof Path){
				    	pPath = (Path)watchable;
				    	
				    }
	                // poll for file system events on the WatchKey
	                for (final WatchEvent<?> event : watchKey.pollEvents()) {
	                    Kind<?> kind = event.kind();
						if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
							Path pathCreated = (Path) event.context();
						    final Path resolve = pPath.resolve(pathCreated);
							final File file = resolve.toFile();
						    if(file.isDirectory()){
						    	resolve.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
					                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
						    }
							LOG.debug("Entry created:{}", file.getAbsolutePath());
						    
						} else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
						    Path pathDeleted = (Path) event.context();
						    LOG.debug("Entry deleted:{}", pPath.resolve(pathDeleted).toFile().getAbsolutePath());
						} else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
						    Path pathModified = (Path) event.context();
						    LOG.debug("Entry modified:{}", pPath.resolve(pathModified).toFile().getAbsolutePath());
						}
	                }
	 
	                // if the watched directed gets deleted, get out of run method
	                if (!watchKey.reset()) {
	                    System.out.println("No longer valid");
	                    watchKey.cancel();
	                    watchService.close();
	                    break;
	                }
	            }
	 
	        } catch (InterruptedException ex) {
	            System.out.println("interrupted. Goodbye");
	            return null;
	        } catch (IOException ex) {
	            ex.printStackTrace();  // don't do this in production code. Use a loggin framework
	            return null;
	        }
		return null;
	}
}