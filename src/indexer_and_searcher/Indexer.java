package indexer_and_searcher;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class Indexer 
{
    private IndexWriter writer;
    
    public Indexer(String indexDir) throws IOException 
    {
        Directory dir = FSDirectory.open(Paths.get(indexDir));

        writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()));
    }
    
    
    public void close() throws IOException 
    {
        writer.close();
    }
    
    
    protected Document getDocument(File f) throws Exception 
    {
        Document doc = new Document();

        doc.add(new TextField("contents", new FileReader(f)));
        doc.add(new TextField("filename", f.getName(), Field.Store.YES));
        doc.add(new TextField("fullpath", f.getCanonicalPath(), Field.Store.YES));

        return doc;
    }
    
    
    private void indexFile(File f) throws Exception 
    {
        System.out.println("Indexing " + f.getCanonicalPath());
        Document doc = getDocument(f);
        writer.addDocument(doc);
    }
    
    
    public int index(String dataDir, FileFilter filter) throws Exception 
    {
        File[] files = new File(dataDir).listFiles();

        for(File f: files) 
        {
            if(!f.isDirectory() && !f.isHidden() && f.exists() && f.canRead() && (filter == null || filter.accept(f))) 
            {
                indexFile(f);
            }
        }
        
        return writer.numRamDocs();
    }
    
    
    private static class TextFilesFilter implements FileFilter 
    {
        @Override
        public boolean accept(File path) 
        {
            return path.getName().toLowerCase().endsWith(".txt");
        }
    }
    
    
    public static void main(String[] args) throws Exception 
    {
        if (args.length != 2) 
        {
            throw new IllegalArgumentException("Usage: java " + Indexer.class.getName() + " <index dir> <data dir>");
        }

        String indexDir = args[0];
        String dataDir = args[1];

        long start = System.currentTimeMillis();

        Indexer indexer = new Indexer(indexDir);
        int numIndexed;

        try 
        {
            numIndexed = indexer.index(dataDir, new TextFilesFilter());
        } 
        finally 
        {
            indexer.close();
        }

        long end = System.currentTimeMillis();

        System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
    }  
}
