package net.ubuntudaily.quickquest.commons.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import net.ubuntudaily.quickquest.commons.collections.Lists;
import net.ubuntudaily.quickquest.commons.io.FileUtils;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理zip文件的便捷工具
 * 
 * @author bruce
 * 
 */
public class ZipUtils
{

    private static final Logger LOG = LoggerFactory.getLogger(ZipUtils.class);

    /**
     * 把源目录夹中的文件和文件夹打包成zip。zip文件中文件名称编码为utf-8。当源目录夹中存在空目录时，空目录被忽略。
     * <p>
     * 假设c:\目录下有个test1目录，结构如下：
     * 
     * <pre>
     * test1.
     *      |-- 1
     *      |   |-- 2
     *      |   |   `-- 3
     *      |   `-- readme
     *      `-- readme
     * </pre>
     * 
     * 运行 <code>zip(new File("c:\\test1", "c:\\test1.zip")</code>之后，test1.zip文件的结构如下：
     * 
     * <pre>
     * test1.zip.
     *          |-- 1
     *          |   |-- 2
     *          |   |   `-- 3
     *          |   `-- readme
     *          `-- readme
     * </pre>
     * 
     * @param srcDir 待打包的源目录
     * @param destZipFile 要生成的zip文件。如果该文件不存在则自动创建，如果该文件已经存在则覆盖。
     * @return 成功返回true，否则返回false并删除已经生成的zip文件。
     */
    public static boolean zip(final File srcDir, final File destZipFile)
    {
        return zip(srcDir, destZipFile, "UTF-8");
    }
    
    /**
     * 把源目录夹中的文件和文件夹打包成zip，当源目录夹中存在空目录时，空目录被忽略。
     * <p>
     * 假设c:\目录下有个test1目录，结构如下：
     * 
     * <pre>
     * test1.
     *      |-- 1
     *      |   |-- 2
     *      |   |   `-- 3
     *      |   `-- readme
     *      `-- readme
     * </pre>
     * 
     * 运行 <code>zip(new File("c:\\test1", "c:\\test1.zip")</code>之后，test1.zip文件的结构如下：
     * 
     * <pre>
     * test1.zip.
     *          |-- 1
     *          |   |-- 2
     *          |   |   `-- 3
     *          |   `-- readme
     *          `-- readme
     * </pre>
     * 
     * @param srcDir 待打包的源目录
     * @param destZipFile 要生成的zip文件。如果该文件不存在则自动创建，如果该文件已经存在则覆盖。
     * @return 成功返回true，否则返回false并删除已经生成的zip文件。
     */
    public static boolean zip(final File srcDir, final File destZipFile, String encoding)
    {
        try
        {
            if ((!destZipFile.exists() && !destZipFile.createNewFile()) || (destZipFile.isDirectory()))
            {
                LOG.error("failed to create the target zip file.");
                return false;

            }

            FileOutputStream fos = null;
            boolean success;
            try
            {
                fos = new FileOutputStream(destZipFile);
                success = zip(srcDir, fos, encoding);
            }
            finally
            {
                IOUtils.closeQuietly(fos);
            }

            if (!success)
            {
                FileUtils.deleteQuietly(destZipFile);
                return false;
            }

        }
        catch (Exception e)
        {
            FileUtils.deleteQuietly(destZipFile);
            return false;
        }

        return true;
    }
    
    /**
     * 把源目录夹中的文件和文件夹打包成zip，并输出到OutputStream。
     * <p>
     * 该方法会对os进行flush，但是不会close。 <b> 如果该方法执行过程中报错，则可能导致输出到OutputStream的内容不完整。
     * 
     * @see #zip(File, File)
     * @param srcDir
     * @param os
     * @return
     */
    public static boolean zip(final File srcDir, final OutputStream os)
    {
        return zip(srcDir, os, "UTF-8");
    }

    /**
     * 把源目录夹中的文件和文件夹打包成zip，并输出到OutputStream。
     * <p>
     * 该方法会对os进行flush，但是不会close。 <b> 如果该方法执行过程中报错，则可能导致输出到OutputStream的内容不完整。
     * 
     * @see #zip(File, File)
     * @param srcDir
     * @param os
     * @param encoding
     * @return
     */
    public static boolean zip(final File srcDir, final OutputStream os, String encoding)
    {
        ZipArchiveOutputStream zaos = null;
        try
        {
            zaos = new ZipArchiveOutputStream(os);

            // Set the compression ratio
            zaos.setEncoding(encoding);
            zaos.setFallbackToUTF8(true);
            zaos.setUseLanguageEncodingFlag(true);

            Collection<File> filesDirsToZip = FileUtils.listFilesAndDirs(srcDir, CanReadFileFilter.CAN_READ,
                    DirectoryFileFilter.INSTANCE);

            for (File file : filesDirsToZip)
            {

                if (file.equals(srcDir))
                {
                    continue;
                }
                final String entryName = file.getPath().substring(srcDir.getPath().length() + 1);
                if (file.isFile())
                {
                    ZipArchiveEntry zipEntry = new ZipArchiveEntry(file, entryName);
                    zaos.putArchiveEntry(zipEntry);
                    FileInputStream fis = null;
                    try
                    {
                        fis = new FileInputStream(file);
                        IOUtils.copy(fis, zaos);
                        zaos.closeArchiveEntry();
                    }
                    finally
                    {
                        IOUtils.closeQuietly(fis);
                    }

                }
                else if (file.isDirectory())
                {

                    // commons-compress 不能在zip中添加空目录。暂时忽略空目录。
                    // out.putArchiveEntry(out.createArchiveEntry(file, entryName));
                    // out.closeArchiveEntry();
                }
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return false;
        }
        finally
        {
            try
            {
                zaos.finish();
                zaos.close();
                os.flush();
            }
            catch (IOException ignored)
            {
            }

        }

        return true;
    }

    /**
     * 
     * 把zip文件解压到指定的目录夹下（假设zip文件中的文件名称编码为GBK）。解压失败则删除已经解压出来的文件或者文件夹。
     * <p>
     * 当该方法执行时，如果同时还有其它进程或者线程也在对目标目录夹进行写操作，则该方法理论上是可能删除其它线程或者进程创建的文件或者文件夹的，就算对该方法加锁执行，也不能避免。
     * 
     * <p>
     * 我花了大约2小时研究了zip文件中的文件名称编码检测，发现这基本是不可能的。除非生成zip的工具往zip包中写入了 language encoding flag 或者 unicode extra
     * fields。所以，不要再想这个问题了，要想跨平台的检测zip文件名称编码，基本不可能。考虑采用7zip。
     * 
     * @param zipFile
     * @param destDir
     * @return 解压失败则返回 false， 否则返回 true。
     * @author bruce
     */
    public static boolean unzip(final File zipFile, final File destDir)
    {
        return unzip(zipFile, destDir, "GBK");
    }

    /**
     * 
     * 把zip文件解压到指定的目录夹下。解压失败则删除已经解压出来的文件或者文件夹。
     * <p>
     * 当该方法执行时，如果同时还有其它进程或者线程也在对目标目录夹进行写操作，则该方法理论上是可能删除其它线程或者进程创建的文件或者文件夹的，就算对该方法加锁执行，也不能避免。
     * 
     * @param zipFile
     * @param destDir
     * @param encoding zip包中文件名称编码
     * @return 解压失败则返回 false， 否则返回 true。
     */
    public static boolean unzip(final File zipFile, final File destDir, String encoding)
    {
        // 记录在解压过程中创建的文件和文件夹
        List<File> createdFilesDirs = Lists.newArrayList();
        ZipFile src = null;
        if (!zipFile.exists())
        {
            return false;
        }
        FileUtils.mkdirQuietly(destDir);
        try
        {
            src = new ZipFile(zipFile, encoding);
            Enumeration<ZipArchiveEntry> entries = src.getEntries();
            while (entries.hasMoreElements())
            {
                ZipArchiveEntry zipEntry = entries.nextElement();

                String entryName = zipEntry.getName();

                File fileOrDir = new File(destDir, entryName);
                if (zipEntry.isDirectory())
                {
                    if (!fileOrDir.exists())
                    {
                        if (!FileUtils.mkdirQuietly(fileOrDir))
                        {
                            throw new Exception("failed to create directory :" + fileOrDir.getPath()
                                    + " , stop the process and delete already-unzipped files.");
                        }
                        createdFilesDirs.add(fileOrDir);
                    }

                }
                else
                {
                    if (!src.canReadEntryData(zipEntry))
                    {
                        throw new Exception("failed to read zip file content :" + entryName
                                + " , stop the process and delete already-unzipped files.");
                    }
                    // 如果父目录夹不存在则创建
                    File parentDir = fileOrDir.getParentFile();
                    if (!parentDir.exists())
                    {
                        if (!FileUtils.mkdirQuietly(parentDir))
                        {
                            throw new Exception("failed to create directory :" + fileOrDir.getPath()
                                    + " , stop the process and delete already-unzipped files.");
                        }
                        createdFilesDirs.add(parentDir);
                    }
                    if (fileOrDir.createNewFile())
                    {
                        createdFilesDirs.add(fileOrDir);
                    }
                    else
                    {
                        throw new Exception(
                                "file :"
                                        + fileOrDir.getPath()
                                        + " already exists or failed to create it, stop the process and delete already-unzipped files.");
                    }

                    FileOutputStream fos = null;
                    InputStream entryContent = null;
                    try
                    {
                        fos = new FileOutputStream(fileOrDir);
                        entryContent = src.getInputStream(zipEntry);
                        org.apache.commons.io.IOUtils.copy(entryContent, fos);
                    }
                    finally
                    {
                        org.apache.commons.io.IOUtils.closeQuietly(fos);
                        org.apache.commons.io.IOUtils.closeQuietly(entryContent);
                    }
                }

            }

        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            for (File f : createdFilesDirs)
            {
                FileUtils.deleteQuietly(f);
            }
            return false;
        }
        finally
        {
            ZipFile.closeQuietly(src);
        }

        return true;
    }
}