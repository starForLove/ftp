import java.io.*;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FavFTPUtil {



    /**
     * 下载文件
     * @param hostname FTP服务器地址
     * @param port FTP服务器端口号
     * @param username FTP登录帐号
     * @param password FTP登录密码
     * @param pathname FTP服务器文件目录
     * @param filename 文件名称
     * @param localpath 下载后的文件路径
     * @return
     */
    public static boolean downloadFile(String hostname, int port, String username, String password, String pathname, String filename, String localpath){
        boolean flag = false;
        boolean find =true;
        FTPClient ftpClient = new FTPClient();
        try {
            //连接FTP服务器
            ftpClient.connect(hostname, port);
            //登录FTP服务器
            ftpClient.login(username, password);
            //验证FTP服务器是否登录成功
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                return flag;
            }

            //FTPClient调用retrieveFileStream导致线程挂起(防火墙问题)；下载文件小于实际文件问题解决
           //ftpClient.enterLocalPassiveMode(); //开启本地被动模式
            ftpClient.enterLocalActiveMode();//开启主动模式

            //设置文件传输的类型，二进制的方式传输,ASCII方式会造成中文文件损坏，缺失
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setDataTimeout(50000);
            //切换FTP目录,下载文件所在目录
            //ftp默认获取的文件的编码是iso-8859-1，而changWorkingDirectory()方法不能进入中文目录，因此需要将pathname转换编码
            ftpClient.changeWorkingDirectory(new String(pathname.getBytes("utf-8"), "iso-8859-1"));
            //ftpClient.enterLocalPassiveMode();
            FTPFile[] ftpFiles = ftpClient.listFiles();
            System.out.println("上级目录："+pathname);
            System.out.println("下载目标:" + pathname + "/" + filename);

            for (FTPFile file : ftpFiles) {

                    String name = new String(file.getName().getBytes("iso-8859-1"), "utf-8");// 转换后的目录名或文件名。

                    if (file.isFile()) {
                        try {

                            if (filename.equalsIgnoreCase(name)) {
                                String filepath=new String((pathname+"/"+name).getBytes("utf-8"),"iso-8859-1");
                                InputStream in=ftpClient.retrieveFileStream(filepath);
                                OutputStream os=new FileOutputStream(localpath+"/"+name);
                                byte[] bytes=new byte[2048*20480];
                                int c=0;
                                long size=0;
                                while ((c=in.read(bytes))!=-1)
                                {
                                    os.write(bytes,0,c);
                                    size=size+c;
                                    System.out.println(name+" 已下载"+"size1:"+size);
                                }

                                os.flush();
                                if(in!=null)
                                {
                                    in.close();
                                }if(os!=null)
                                {
                                    os.close();
                                }
                                System.out.println(name+"下载完毕！");
                                ftpClient.completePendingCommand();

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (file.isDirectory() && filename.equalsIgnoreCase(name)) {
                        //下载目录的路径
                        String dirname = pathname + "/" + filename;
                        //新建目录，这个目录是要下载的目录
                        File dir = new File(localpath + "/" + filename);
                        dir.mkdir();
                        try {
                            //切换到下载的目录
                            System.out.println(dirname);
                            ftpClient.changeWorkingDirectory(new String(dirname.getBytes("utf-8"), "iso-8859-1"));

                            FTPFile[] filelist = ftpClient.listFiles();
                            //遍历整个目录
                            for (FTPFile file2 : filelist) {

                                String name2 = new String(file2.getName().getBytes("iso-8859-1"), "utf-8");// 转换后的目录名或文件名。
                                System.out.println(name2);
                                //如果是文件，直接将文件下载
                                if (file2.isFile()) {
                                    System.out.println(dir.getPath());
                                    String filepath2 = new String((dirname + "/" + name2).getBytes("utf-8"), "iso-8859-1");
                                    InputStream in2=ftpClient.retrieveFileStream(filepath2);

                                    String dirfile = dir.getPath() + "/" + name2;
                                    OutputStream os2 = new FileOutputStream(dirfile);
                                    byte[] bytes2=new byte[2048*20480];
                                    int d=0;
                                    long size =0;
                                    while ((d=in2.read(bytes2))!=-1)
                                    {
                                        os2.write(bytes2,0,d);
                                        size=size+d;
                                        System.out.println(name2+" 已下载"+"size2:"+size);
                                    }

                                    os2.flush();
                                    if(in2!=null)
                                    {
                                        in2.close();
                                    }if(os2!=null)
                                    {

                                        os2.close();
                                    }
                                    System.out.println(name2+"下载完毕！");
                                    ftpClient.completePendingCommand();

                                }
                                //如果是目录，继续遍历目录
                                if (file2.isDirectory()) {
                                    String dirname2 = dirname + "/" + name2;
                                    System.out.println(dir.getPath());
                                    //递归
                                    search(dirname2, ftpClient, dir.getPath(), name2);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

            }
           ftpClient.logout();
            flag = true;

        }catch (Exception e) {
            e.printStackTrace();
        } finally{
            if(ftpClient.isConnected()){
                try {
                    ftpClient.logout();
                } catch (IOException e) {

                }
            }
        }
        return flag;
    }
    /**
    * 遍历目录的递归方法
    * @param  dirname2需要遍历的目录路径
    * @param  dir下载文件本地的根目录路径
    **/
    public static void search(String dirname2, FTPClient ftpClient,String localdir,String name2){
        //新建目录
        File dir2=new File(localdir+"/"+name2);
        dir2.mkdir();
        //切换到需要遍历的目录
        try {
            ftpClient.changeWorkingDirectory(new String(dirname2.getBytes("utf-8"), "iso-8859-1"));
            FTPFile[] filelist2=ftpClient.listFiles();
            for(FTPFile file3:filelist2)
            {
                String name3 = new String(file3.getName().getBytes("iso-8859-1"), "utf-8");// 转换后的目录名或文件名。


                if(file3.isFile())
                {
                    System.out.println(dirname2+"/"+name3);
                    String filepath3=new String((dirname2+"/"+name3).getBytes("utf-8"),"iso-8859-1");
                    InputStream in3=ftpClient.retrieveFileStream(filepath3);
                    //System.out.println(name3+" :"+in3.read(new byte[1024]));
                    String dirfile2=dir2.getPath()+"/"+name3;
                    OutputStream os3=new FileOutputStream(dirfile2);
                    byte[] bytes3=new byte[2048*20480];
                    int e=0;
                    long size=0;
                    while((e=in3.read(bytes3))!=-1)
                    {
                        os3.write(bytes3,0,e);
                        size=size+e;
                       System.out.println(name3+" 已下载"+""+size);
                    }
                    os3.flush();
                    if(in3!=null)
                    {
                        in3.close();
                    }if(os3!=null)
                    {
                        os3.close();
                    }
                    System.out.println(name3+"下载完毕！");
                    ftpClient.completePendingCommand();

                }
                if(file3.isDirectory())
                {

                    String dirname3=dirname2+"/"+name3;
                  search(dirname3,ftpClient,dir2.getPath(),name3);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}